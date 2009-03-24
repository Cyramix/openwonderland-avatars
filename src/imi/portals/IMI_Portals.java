/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.portals;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.CullState;
import com.jme.scene.state.CullState.Face;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.scene.PMatrix;
import imi.scene.SkyBox;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.tests.DemoBase.SwingFrame;
import imi.utils.FileUtils;
import java.awt.Canvas;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFrame;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.JMECollisionComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class IMI_Portals extends Entity {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private Node                m_portal            = null;
    private CameraNode          cameraNode          = null;
    private ColorRGBA           m_clearColor        = new ColorRGBA(173.0f/255.0f, 195.0f/255.0f, 205.0f/255.0f, 1.0f);

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    IMI_Portals (String id) {
        super(id);
    }

    public void createPortal(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState,
           Vector3f portalViewPosition, WorldManager worldManager, int camWidth, int camHeight, Quaternion rotation, int offset) {

        Node portal = createPortalGeometry(portalName, transform, portalDimensions, zBufferState, worldManager, offset);
//        TextureState ts = createCameraEntity(worldManager, camWidth, camHeight, portalViewPosition);
        createCameraComponent(worldManager, camWidth, camHeight, portalViewPosition, rotation);
        TextureState ts = createRenderToTexture(worldManager);
        portal.setRenderState(ts);
        
        m_portal = new Node(portalName);
        m_portal.attachChild(portal);

        RenderComponent rc  = worldManager.getRenderManager().createRenderComponent(m_portal);
        rc.setOrtho(false);
        rc.setLightingEnabled(false);
        addComponent(RenderComponent.class, rc);
    }
    
    public Node createPortalGeometry(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState, WorldManager wm, int offset) {
        Node portalObject = new Node(portalName + "Group");
        Box  portalFrame  = null;
        Vector3f center   = null;

        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        Quad portal  = new Quad(portalName, portalDimensions.x, portalDimensions.y);
        portal.setLocalTranslation(transform.getTranslation());
        portal.setLocalRotation(transform.getRotationJME());
        portal.setLocalScale(transform.getScaleVector());
        portal.setRenderState(cs);
        portal.setModelBound(new BoundingBox());
        portal.updateModelBound();
        portal.updateWorldBound();
        portalObject.attachChild(portal);

        TextureState ts = wm.getRenderManager().createTextureState();
        Texture t0 = TextureManager.loadTexture(
                getClass().getResource("/jmetest/data/texture/dirt.jpg"),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t0);

        center      = transform.getTranslation().add(new Vector3f((portalDimensions.x/2), 0.0f, 1.3f * offset));
        portalFrame = new Box("rightSide", center, portalDimensions.z, portalDimensions.y/2, portalDimensions.x/3);
        portalFrame.setModelBound(new BoundingBox());
        portalFrame.updateModelBound();
        portalFrame.updateModelBound();
        portalFrame.setRenderState(ts);
        portalObject.attachChild(portalFrame);

        center      = transform.getTranslation().add(new Vector3f(-(portalDimensions.x/2), 0.0f, 1.3f * offset));
        portalFrame = new Box("leftSide", center, portalDimensions.z, portalDimensions.y/2, portalDimensions.x/3);
        portalFrame.setModelBound(new BoundingBox());
        portalFrame.updateModelBound();
        portalFrame.updateModelBound();
        portalFrame.setRenderState(ts);
        portalObject.attachChild(portalFrame);

        center      = transform.getTranslation().add(new Vector3f(0.0f, (portalDimensions.y/1.9f), 1.3f * offset));
        portalFrame = new Box("topSide", center, portalDimensions.x/1.9f, portalDimensions.z, portalDimensions.y/2.2f);
        portalFrame.setModelBound(new BoundingBox());
        portalFrame.updateModelBound();
        portalFrame.updateModelBound();
        portalFrame.setRenderState(ts);
        portalObject.attachChild(portalFrame);

        center      = transform.getTranslation().add(new Vector3f(0.0f, 0.0f, 3.4f * offset));
        portalFrame = new Box("backSide", center, portalDimensions.x/2, portalDimensions.y/2, portalDimensions.z/2);
        portalFrame.setModelBound(new BoundingBox());
        portalFrame.updateModelBound();
        portalFrame.updateModelBound();
        portalFrame.setRenderState(ts);
        portalObject.attachChild(portalFrame);

        portalObject.setRenderState(zBufferState);
        return portalObject;
    }

    public void createCameraComponent(WorldManager worldManager, int camWidth, int camHeight, Vector3f cameraPosition, Quaternion rotation) {
        CameraNode cn = new CameraNode(getName() + "Cam", null);
        Node cameraSG = new Node();
        cameraSG.attachChild(cn);
        cameraSG.setLocalTranslation(cameraPosition);
        if (rotation != null)
            cameraSG.setLocalRotation(rotation);
        CameraComponent cc = worldManager.getRenderManager().createCameraComponent(cameraSG, cn,
                camWidth, camHeight, 45.0f, camWidth/camHeight, 0.01f, 100000.0f, false);
        addComponent(CameraComponent.class, cc);
    }

    public TextureState createRenderToTexture(WorldManager worldManager) {
        CameraComponent cc = getComponent(CameraComponent.class);
        int width   = cc.getViewportWidth();
        int height  = cc.getViewportHeight();

        RenderBuffer rb = worldManager.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, width, height);
        rb.setBackgroundColor(m_clearColor);
        rb.setCameraComponent(cc);
        worldManager.getRenderManager().addRenderBuffer(rb);

        TextureState ts = worldManager.getRenderManager().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(rb.getTexture(), 0);
        return ts;
    }

    public void createCollisionComponent(JMECollisionSystem collisionSystem) {
        JMECollisionComponent cc = collisionSystem.createCollisionComponent(m_portal);
        addComponent(CollisionComponent.class, cc);
    }

    protected TextureState createCameraEntity(WorldManager wm, int camWidth, int camHeight, Vector3f cameraPosition) {
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraNode cn = new CameraNode(getName() + "Cam", null);
        Node cameraSG = new Node();
        cameraSG.attachChild(cn);
        cameraSG.setLocalTranslation(cameraPosition);
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn,
                camWidth, camHeight, 45.0f, camWidth/camHeight, 1.0f, 1000.0f, false);

        int width   = cc.getViewportWidth();
        int height  = cc.getViewportHeight();

        RenderBuffer rb = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, width, height);
        rb.setCameraComponent(cc);
        wm.getRenderManager().addRenderBuffer(rb);

        TextureState ts = wm.getRenderManager().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(rb.getTexture(), 0);

        // Skybox
        SkyBox skyBox = createSkyBox(camera, wm);

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        Canvas canvas = rb.getCanvas();
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);

        FlexibleCameraProcessor cameraProcessor = new FlexibleCameraProcessor(cameraListener, cameraNode, wm, camera, null);

        assignCameraType(wm, cameraProcessor);
        wm.addUserData(FlexibleCameraProcessor.class, cameraProcessor);
        wm.addUserData(CameraState.class, cameraProcessor.getState());
        cameraProcessor.setRunInRenderer(true);
        camera.addComponent(FlexibleCameraProcessor.class, cameraProcessor);

        wm.addEntity(camera);
        addComponent(CameraComponent.class, cc);
        return ts;
    }


    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);

        return (cameraSG);
    }

    protected SkyBox createSkyBox(Entity camera, WorldManager worldManager) {
        String [] skyboxAssets = new String[] { "/textures/skybox/Front.png",
                                        "/textures/skybox/Right.png",
                                        "/textures/skybox/Back.png",
                                        "/textures/skybox/Left.png",
                                        "/textures/skybox/default.png",
                                        "/textures/skybox/Top.png" };

        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, worldManager);
        sky.setTexture(SkyBox.NORTH,    loadSkyboxTexture(skyboxAssets[0]));  // +Z side
        sky.setTexture(SkyBox.EAST,     loadSkyboxTexture(skyboxAssets[1]));  // -X side
        sky.setTexture(SkyBox.SOUTH,    loadSkyboxTexture(skyboxAssets[2]));  // -Z side
        sky.setTexture(SkyBox.WEST,     loadSkyboxTexture(skyboxAssets[3]));  // +X side
        sky.setTexture(SkyBox.DOWN,     loadSkyboxTexture(skyboxAssets[4]));  // -Y Side
        sky.setTexture(SkyBox.UP,       loadSkyboxTexture(skyboxAssets[5]));  // +Y side

        RenderComponent sc2 = worldManager.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);

        return sky;
    }

    protected Texture loadSkyboxTexture(String filePath) {
        Texture monkeyTexture = null;
        if (filePath.contains("assets")) {
            try
            {
                monkeyTexture = TextureManager.loadTexture(new File(FileUtils.rootPath, filePath).toURI().toURL(), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
            } catch (MalformedURLException ex)
            {
                System.out.println(ex.getMessage());
            }
        } else {
            URL imageLocation   = getClass().getResource(filePath);
            monkeyTexture = TextureManager.loadTexture(imageLocation, Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
        }

        if (monkeyTexture != null)
        {
            monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            monkeyTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        }
        return monkeyTexture;
    }

    protected void assignCameraType(WorldManager wm, FlexibleCameraProcessor cameraProcessor) {
        FirstPersonCamState state = new FirstPersonCamState();
        state.setCameraPosition(new Vector3f(0, 2.2f, -6));
        FirstPersonCamModel model = new FirstPersonCamModel();
        cameraProcessor.setCameraBehavior(model, state);
    }
}
