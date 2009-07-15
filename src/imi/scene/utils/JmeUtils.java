/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.utils;

import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.BlendState.DestinationFunction;
import com.jme.scene.state.BlendState.SourceFunction;
import com.jme.scene.state.BlendState.TestFunction;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import imi.loaders.ColladaLoaderParams;
import imi.repository.AssetDescriptor;
import imi.repository.Repository;
import imi.repository.SharedAsset;
import imi.repository.SharedAssetPlaceHolder;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class JmeUtils
{
    private static final Logger logger = Logger.getLogger(JmeUtils.class.getName());

////////////////////////////////////////////////////////////////////////////////
// Alpha
////////////////////////////////////////////////////////////////////////////////

    public static void enableAlpha(Spatial spat, WorldManager wm) {
        enableAlpha(spat, BlendState.SourceFunction.SourceAlpha, BlendState.DestinationFunction.OneMinusSourceAlpha, BlendState.TestFunction.GreaterThan, wm);
    }

    public static void enableAlpha(Spatial spat, SourceFunction src, DestinationFunction dst, TestFunction tst, WorldManager wm) {
        if (spat == null) {
            Logger.getLogger(JmeUtils.class.getName()).log(Level.SEVERE, "spat param == null... you need a spatial object");
            return;
        }

        BlendState blendState = (BlendState)wm.getRenderManager().createRendererState(RenderState.StateType.Blend);

        if(src == null)
            src = BlendState.SourceFunction.SourceAlpha;
        if(dst == null)
            dst = BlendState.DestinationFunction.One;
        if (tst == null)
            tst = BlendState.TestFunction.GreaterThan;

        blendState.setEnabled(true);
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(src);
        blendState.setDestinationFunction(dst);
        blendState.setTestEnabled(true);
        blendState.setTestFunction(tst);

        spat.setRenderState(blendState);
        spat.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_TRANSPARENT);
        spat.updateRenderState();
    }

    public static void disableAlpha(Spatial spat, WorldManager wm) {
        BlendState blendState = (BlendState)wm.getRenderManager().createRendererState(RenderState.StateType.Blend);

        blendState.setEnabled(true);
        blendState.setBlendEnabled(false);
        blendState.setTestEnabled(false);

        spat.setRenderState(blendState);
        spat.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
        spat.updateRenderState();
    }

////////////////////////////////////////////////////////////////////////////////
// Orientation
////////////////////////////////////////////////////////////////////////////////

    public static void setDirection(Spatial s, Vector3f heading) {
        Matrix3f rot = new Matrix3f();
        rot.fromAxes(Vector3f.UNIT_Y.cross(heading), Vector3f.UNIT_Y, heading);
        s.setLocalRotation(rot);
    }

////////////////////////////////////////////////////////////////////////////////
// Texturing
////////////////////////////////////////////////////////////////////////////////

    public static Texture createTexture(String localPath, Texture.ApplyMode mode) {
        Texture jmeTexture  = null;
        URL     urlPath     = null;

        try {
            urlPath = PMeshUtils.class.getResource(localPath);
            if (urlPath != null)
                jmeTexture  = TextureManager.loadTexture(urlPath);
            else
                jmeTexture  = TextureManager.loadTexture(new File(localPath).toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(PMeshUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (mode != null)
            jmeTexture.setApply(mode);

        jmeTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
        jmeTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        jmeTexture.setWrap(Texture.WrapMode.Repeat);

        return jmeTexture;
    }

    public static void normalizeNormalsOnGraph(Node node) {
        FastList<Spatial> q = new FastList<Spatial>(1);
        q.add(node);
        Vector3f normal = new Vector3f();
        while (!q.isEmpty())
        {
            Spatial current = q.removeFirst();
            if (current instanceof TriMesh)
            {
                TriMesh mesh = (TriMesh)current;
                FloatBuffer norms = mesh.getNormalBuffer();
                norms.rewind();
                for (int i = 0; i < norms.limit(); )
                {
                    normal.set(norms.get(i), norms.get(i+1), norms.get(i+2));
                    normal.normalizeLocal();
                    norms.put(normal.x);
                    norms.put(normal.y);
                    norms.put(normal.z);
                    i += 3;
                }
                mesh.setNormalBuffer(norms);
            }
            if (current instanceof Node && ((Node)current).getChildren() != null)
                q.addAll(((Node)current).getChildren());
        }
    }

    public static void textureGraph(Node node, Object texture, WorldManager wm) {
        FastList<Spatial> q = new FastList<Spatial>(1);
        q.add(node);
        while (!q.isEmpty())
        {
            Spatial current = q.removeFirst();
            textureMesh(current, texture, wm);
            if (current instanceof Node && ((Node)current).getChildren() != null)
                q.addAll(((Node)current).getChildren());
        }
    }

    public static void textureMesh(Spatial mesh, Object texture, WorldManager wm, int textureUnit) {
        URL url = getURL(texture);
        if (mesh instanceof SharedMesh || mesh instanceof TriMesh)
        {
            MaterialState matState = (MaterialState) (mesh).getRenderState(StateType.Material);
            if (matState == null)
                matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
            matState.setColorMaterial(MaterialState.ColorMaterial.None);
            matState.setMaterialFace(MaterialState.MaterialFace.Front);

            TextureState texState = (TextureState) mesh.getRenderState(StateType.Texture);
            if (texState == null)
                texState = (TextureState) wm.getRenderManager().createRendererState(StateType.Texture);

            Texture base = TextureManager.loadTexture(url);
            base.setApply(Texture.ApplyMode.Modulate);
            base.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
            base.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
            base.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            texState.setTexture(base, textureUnit);
            mesh.setRenderState(texState);
        }
        mesh.updateRenderState();
    }

    public static void textureMesh(Spatial mesh, Object texture, WorldManager wm) {
        textureMesh(mesh, texture, wm, 0);
    }

    public static URL getURL(Object stringUrlFile)
    {
        URL url = null;
        try {
            if (stringUrlFile instanceof String)
            {
                url = JmeUtils.class.getResource((String)stringUrlFile);
                if (url == null)
                    url = new File((String)stringUrlFile).toURI().toURL();
            }
            else if (stringUrlFile instanceof File)
                url = ((File)stringUrlFile).toURI().toURL();
            else if (stringUrlFile instanceof URL)
                url = (URL)stringUrlFile;
        } catch (MalformedURLException ex) { logger.severe("texture URL failed"); }
        if (url == null)
            throw new IllegalArgumentException("was not able to get texture URL");
        return url;
    }

    public static void applyTextureToTextureState(TextureState texState, Object texture, int textureUnit)
    {
        URL url = getURL(texture);
        Texture jmeTexture = TextureManager.loadTexture(url);
        jmeTexture.setApply(Texture.ApplyMode.Modulate);
        jmeTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
        jmeTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
        jmeTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
        texState.setTexture(jmeTexture, textureUnit);
    }

////////////////////////////////////////////////////////////////////////////////
// Render States
////////////////////////////////////////////////////////////////////////////////

    public static void setDefaultRenderStatesOnGraph(Node root, MaterialState.ColorMaterial colorMat, WorldManager wm) {
        FastList<Spatial> q = new FastList<Spatial>(1);
        q.add(root);
        while (!q.isEmpty())
        {
            Spatial current = q.removeFirst();
            setDefaultRenderStates(current, colorMat, wm);
            if (current instanceof Node && ((Node)current).getChildren() != null)
                q.addAll(((Node)current).getChildren());
        }
    }

    public static void setDefaultRenderStates(Spatial spat, WorldManager wm) {
        setDefaultRenderStates(spat, MaterialState.ColorMaterial.None, wm);
    }

    public static void setDefaultRenderStates(Spatial spat, MaterialState.ColorMaterial colorMat, WorldManager wm) {
        WorldManager worldManager = wm;

        // Z Buffer State
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) worldManager.getRenderManager().createRendererState(StateType.Material);
        matState.setAmbient(ColorRGBA.white);
        matState.setDiffuse(ColorRGBA.white);
        matState.setMaterialFace(MaterialState.MaterialFace.Front);
        matState.setColorMaterial(colorMat);
        matState.setEnabled(true);

        // Cull State
        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        // Bounding volume
        spat.setModelBound(new BoundingSphere());
        spat.updateModelBound();

        spat.setRenderState(buf);
        spat.setRenderState(matState);
        spat.setRenderState(cs);
        spat.updateRenderState();
    }

    public static RenderState getRenderState(Spatial spat, StateType type) {
        return spat.getRenderState(type);
    }

    /**
     * Load a collada file into a jme Node
     * @param resource - may be a String, File or URL
     * @return
     */
    public static Node fromCOLLADA(Object resource, WorldManager wm)
    {
        Repository repo = (Repository)wm.getUserData(Repository.class);
        AssetDescriptor descriptor = null;
        if (resource instanceof String)
            descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, (String)resource);
        else if (resource instanceof File)
            descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, (File)resource);
        else if (resource instanceof URL)
            descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, (URL)resource);
        else
            throw new IllegalArgumentException("resource passed in is not a String, File or URL");
        SharedAsset worldAsset = new SharedAsset(repo, descriptor, null);

        ColladaLoaderParams params = new ColladaLoaderParams.Builder().setLoadSkeleton(false).setLoadGeometry(true).setLoadAnimation(false).setName("createdFromCOLLADA").build();
        worldAsset.setUserData(params);

        PScene scene = new PScene(wm);
        PPolygonModelInstance modInst = scene.addModelInstance(worldAsset, new PMatrix());
        while (modInst.getChild(0) instanceof SharedAssetPlaceHolder)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(JmeUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        SceneGraphConvertor convertor = new SceneGraphConvertor();
        Node jmeRoot = convertor.convert(scene);

//        if (scaleConditioner)
//            ScaleConditioner.traverse(jmeRoot);

        return jmeRoot;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Serializing
    ////////////////////////////////////////////////////////////////////////////////

    public static boolean serializeJmeGraph(File file, Node node)
    {
        BinaryExporter exporter = new BinaryExporter();
        boolean result = false;
        try {
            result = exporter.save(node, file);
        } catch (IOException ex) {
            Logger.getLogger(JmeUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Node loadSerializedJmeGraph(File file)
    {
        BinaryImporter importer = new BinaryImporter();
        Node node = null;
        try {
            node = (Node) importer.load(file);
        } catch (IOException ex) {
            Logger.getLogger(JmeUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return node;
    }

    public static Node loadSerializedJmeGraph(URL url)
    {
        BinaryImporter importer = new BinaryImporter();
        Node node = null;
        try {
            node = (Node) importer.load(url);
        } catch (IOException ex) {
            Logger.getLogger(JmeUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return node;
    }
}
