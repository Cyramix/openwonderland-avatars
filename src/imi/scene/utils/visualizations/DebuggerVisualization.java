/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.utils.visualizations;

import imi.scene.boundingvolumes.PSphere;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.jme.animation.SkinNode;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingCapsule;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.bounding.OrientedBoundingBox;
import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.TextureRenderer;
import com.jme.scene.Geometry;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.Point;
import com.jme.scene.Spatial;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Capsule;
import com.jme.scene.shape.OrientedBox;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import javolution.util.FastTable;

/**
 *
 * changes made by Lou Hayt
 *
 * <code>Debugger</code> provides tools for viewing scene data such as
 * boundings and normals.
 * 
 * @author Joshua Slack
 * @author Emond Papegaaij (normals ideas and previous normal tool)
 * @version $Id: Debugger.java,v 1.31 2007/09/21 15:45:28 nca Exp $
 */
public final class DebuggerVisualization {

    // -- **** METHODS FOR DRAWING BOUNDING VOLUMES **** -- //

    private static final Sphere boundingSphere = new Sphere("bsphere", 10, 10, 1);
    private static final Box boundingBox = new Box("bbox", new Vector3f(), 1, 1, 1);
    private static final OrientedBox boundingOB = new OrientedBox("bobox");
    private static final Capsule boundingCapsule = new Capsule("bcap", 3, 10, 10, 1, 1);

    static {
        boundingSphere.setRenderQueueMode(Renderer.QUEUE_SKIP);
        boundingBox.setRenderQueueMode(Renderer.QUEUE_SKIP);
        boundingOB.setRenderQueueMode(Renderer.QUEUE_SKIP);
        boundingCapsule.setRenderQueueMode(Renderer.QUEUE_SKIP);
    }

    private static WireframeState boundsWireState;
    private static ZBufferState boundsZState;

    /**
     * <code>drawBounds</code> draws the bounding volume for a given Spatial
     * and its children.
     * 
     * @param se
     *            the Spatial to draw boundings for.
     * @param r
     *            the Renderer to use to draw the bounding.
     */
    public static void drawBounds(Spatial se, Renderer r) {
        drawBounds(se, r, true);
    }

    private static void renderStatesSetup(Renderer r) {
        
        if (boundsWireState == null) {
            boundsWireState = r.createWireframeState();
            boundsZState = r.createZBufferState();
            boundingBox.setRenderState(boundsWireState);
            boundingBox.setRenderState(boundsZState);
            boundingBox.updateRenderState();
            boundingOB.setRenderState(boundsWireState);
            boundingOB.setRenderState(boundsZState);
            boundingOB.updateRenderState();
            boundingSphere.setRenderState(boundsWireState);
            boundingSphere.setRenderState(boundsZState);
            boundingSphere.updateRenderState();
            boundingCapsule.setRenderState(boundsWireState);
            boundingCapsule.setRenderState(boundsZState);
            boundingCapsule.updateRenderState();

            if (!boundingSphereWireframeOn)
                setBoundingSphereWireframeOn(false);
        }
    }

    static boolean boundingSphereWireframeOn = true;

    public static void setBoundingSphereWireframeOn(boolean on)
    {
        boundingSphereWireframeOn = on;
        WireframeState state = (WireframeState) boundingSphere.getRenderState(RenderState.StateType.Wireframe);
        if (state != null)
            state.setEnabled(on);
        boundingSphere.updateRenderState();
    }
    
    /**
     * <code>drawBounds</code> draws the bounding volume for a given Spatial
     * and optionally its children.
     * 
     * @param se
     *            the Spatial to draw boundings for.
     * @param r
     *            the Renderer to use to draw the bounding.
     * @param doChildren
     *            if true, boundings for any children will also be drawn
     */
    public static void drawBounds(Spatial se, Renderer r,
            boolean doChildren) {
        if (se == null)
            return;

        renderStatesSetup(r);

        if (se.getWorldBound() != null
                && se.getCullHint() != Spatial.CullHint.Always) {
            int state = r.getCamera().getPlaneState();
            if (r.getCamera().contains(se.getWorldBound()) != Camera.FrustumIntersect.Outside)
                drawBounds(se.getWorldBound(), r);
            else
                doChildren = false;
            r.getCamera().setPlaneState(state);
        }
        if (doChildren && se instanceof Node) {
            Node n = (Node) se;
            if (n.getChildren() != null) {
                for (int i = n.getChildren().size(); --i >= 0;)
                    drawBounds(n.getChild(i), r, true);
            }
        }
    }

    public static void drawBounds(BoundingVolume bv, Renderer r) {

        switch (bv.getType()) {
            case AABB:
                drawBoundingBox((BoundingBox) bv, r);
                break;
            case Sphere:
                drawBoundingSphere((BoundingSphere) bv, r);
                break;
            case OBB:
                drawOBB((OrientedBoundingBox) bv, r);
                break;
            case Capsule:
                drawBoundingCapsule((BoundingCapsule) bv, r);
                break;
            default:
                break;
        }
    }

    public static void setBoundsColor(ColorRGBA color) {
        boundingBox.setSolidColor(color);
        boundingOB.setSolidColor(color);
        boundingCapsule.setSolidColor(color);
        boundingSphere.setSolidColor(color);
    }

    public static void drawBoundingSphere(BoundingSphere sphere, Renderer r) {
        renderStatesSetup(r);
        // TODO TODO TODO TODO TODO
        // This method can occasionally get null from sphere.getCenter!!!
        Vector3f sphereCenter = sphere.getCenter();
        if (sphereCenter != null)
            boundingSphere.getCenter().set(sphereCenter);
        else
            boundingSphere.getCenter().set(new Vector3f());
        boundingSphere.updateGeometry(boundingSphere.getCenter(), 10, 10, sphere
                .getRadius()); // pass back bs center to prevent accidently
        // data access.
        boundingSphere.draw(r);
    }

    public static void drawBoundingBox(BoundingBox box, Renderer r) {
        boundingBox.getCenter().set(box.getCenter());
        boundingBox.updateGeometry(boundingBox.getCenter(),
                box.xExtent,
                box.yExtent,
                box.zExtent);
        boundingBox.draw(r);
    }

    public static void drawOBB(OrientedBoundingBox box, Renderer r) {
        boundingOB.getCenter().set(box.getCenter());
        boundingOB.getXAxis().set(box.getXAxis());
        boundingOB.getYAxis().set(box.getYAxis());
        boundingOB.getZAxis().set(box.getZAxis());
        boundingOB.getExtent().set(box.getExtent());
        boundingOB.updateGeometry();
        boundingOB.draw(r);
    }

    private static final Vector3f start = new Vector3f();
    private static final Vector3f end = new Vector3f();

    public static void drawBoundingCapsule(BoundingCapsule cap, Renderer r) {
        boundingCapsule.updateGeometry(
                cap.getLineSegment().getNegativeEnd(start),
                cap.getLineSegment().getPositiveEnd(end),
                cap.getRadius());
        boundingCapsule.draw(r);
    }

    // -- **** METHODS FOR DRAWING POINTS **** -- //

    private static final Point point = new Point();
    static {
        point.setVertexBuffer(BufferUtils.createVector3Buffer(500));
        point.setColorBuffer(BufferUtils.createColorBuffer(500));
    }
    private static final Vector3f _pointVect = new Vector3f();
    private static ZBufferState pointZState;

    public static void drawPoints(FastTable<Vector3f> points, Renderer r, float size, ColorRGBA color)
    {
        if (points == null)
            return;

        if (pointZState == null) {
            pointZState = r.createZBufferState();
            point.setRenderState(pointZState);
            point.updateRenderState();
        }

        point.setPointSize(size);

        FloatBuffer pointVerts = point.getVertexBuffer();
        if (pointVerts.capacity() < points.size())
        {
            point.setVertexBuffer(null);
            System.gc();
            pointVerts = BufferUtils.createVector3Buffer(points.size());
            point.setVertexBuffer(pointVerts);
        }
        else
        {
            point.setVertexCount(points.size());
            pointVerts.clear();
        }

        FloatBuffer pointColors = point.getColorBuffer();
        if (pointColors.capacity() < (4 * (points.size())))
        {
            point.setColorBuffer(null);
            System.gc();
            pointColors = BufferUtils.createColorBuffer(points.size());
            point.setColorBuffer(pointColors);
        }
        else
        {
            pointColors.clear();
        }

        IntBuffer pointInds = point.getIndexBuffer();
        if (pointInds == null || pointInds.capacity() < (point.getVertexCount()))
        {
            point.setIndexBuffer(null);
            System.gc();
            pointInds = BufferUtils.createIntBuffer(points.size());
            point.setIndexBuffer(pointInds);
        }
        else
        {
            pointInds.clear();
            pointInds.limit(point.getVertexCount());
        }

        pointVerts.rewind();
        pointInds.rewind();

        int index = 0;
        for (int x = 0; x < points.size(); x++)
        {
            _pointVect.set(points.get(index));
            index++;
            pointVerts.put(_pointVect.x);
            pointVerts.put(_pointVect.y);
            pointVerts.put(_pointVect.z);

            pointColors.put(color.r);
            pointColors.put(color.g);
            pointColors.put(color.b);
            pointColors.put(color.a);

            pointInds.put(x);
        }

        point.setLocalTranslation(Vector3f.ZERO);
        point.getLocalRotation().loadIdentity();
        point.onDraw(r);
    }

    // -- **** METHODS FOR DRAWING NORMALS **** -- //

    private static final Line normalLines = new Line("normLine");
    static {
        normalLines.setLineWidth(3.0f);
        normalLines.setMode(Line.Mode.Segments);
        normalLines.setVertexBuffer(BufferUtils.createVector3Buffer(500));
        normalLines.setColorBuffer(BufferUtils.createColorBuffer(500));
    }
    private static final Vector3f _normalVect = new Vector3f();
    private static ZBufferState normZState;
    public static ColorRGBA NORMAL_COLOR_BASE = ColorRGBA.red.clone();
    public static ColorRGBA NORMAL_COLOR_TIP = ColorRGBA.pink.clone();
    public static ColorRGBA TANGENT_COLOR_BASE = ColorRGBA.red.clone();
    public static BoundingBox measureBox = new BoundingBox();
    public static float AUTO_NORMAL_RATIO = .05f;
    
    /**
     * <code>drawNormals</code> draws lines representing normals for a given
     * Spatial and its children.
     * 
     * @param element
     *            the Spatial to draw normals for.
     * @param r
     *            the Renderer to use to draw the normals.
     */
    public static void drawNormals(Spatial element, Renderer r) {
        drawNormals(element, r, -1f, true);
    }

    public static void drawTangents(Spatial element, Renderer r) {
        drawTangents(element, r, -1f, true);
    }

    public static void drawLines(FastTable<Vector3f> lines, Renderer r, float width, ColorRGBA baseColor, ColorRGBA tipColor)
    {
        if (lines == null)
            return;

        if (normZState == null) {
            normZState = r.createZBufferState();
            //normZState.
            normalLines.setRenderState(normZState);
            normalLines.updateRenderState();
        }

        r.setPolygonOffset(1.0f, 0.001f);

        normalLines.setLineWidth(width);

        FloatBuffer lineVerts = normalLines.getVertexBuffer();
        if (lineVerts.capacity() < (3 * (lines.size())))
        {
            normalLines.setVertexBuffer(null);
            System.gc();
            lineVerts = BufferUtils.createVector3Buffer(lines.size());
            normalLines.setVertexBuffer(lineVerts);
        }
        else
        {
            normalLines.setVertexCount(lines.size());
            lineVerts.clear();
        }

        FloatBuffer lineColors = normalLines.getColorBuffer();
        if (lineColors.capacity() < (4 * (lines.size())))
        {
            normalLines.setColorBuffer(null);
            System.gc();
            lineColors = BufferUtils.createColorBuffer(lines.size());
            normalLines.setColorBuffer(lineColors);
        }
        else
        {
            lineColors.clear();
        }

        IntBuffer lineInds = normalLines.getIndexBuffer();
        if (lineInds == null || lineInds.capacity() < (normalLines.getVertexCount()))
        {
            normalLines.setIndexBuffer(null);
            System.gc();
            lineInds = BufferUtils.createIntBuffer(lines.size());
            normalLines.setIndexBuffer(lineInds);
        }
        else
        {
            lineInds.clear();
            lineInds.limit(normalLines.getVertexCount());
        }

        lineVerts.rewind();
        lineInds.rewind();

        int index = 0;
        for (int x = 0; x < lines.size() * 0.5f; x++)
        {
            _normalVect.set(lines.get(index));
            index++;
            lineVerts.put(_normalVect.x);
            lineVerts.put(_normalVect.y);
            lineVerts.put(_normalVect.z);

            lineColors.put(baseColor.r);
            lineColors.put(baseColor.g);
            lineColors.put(baseColor.b);
            lineColors.put(baseColor.a);

            lineInds.put(x * 2);

            _normalVect.set(lines.get(index));
            index++;
            lineVerts.put(_normalVect.x);
            lineVerts.put(_normalVect.y);
            lineVerts.put(_normalVect.z);

            lineColors.put(tipColor.r);
            lineColors.put(tipColor.g);
            lineColors.put(tipColor.b);
            lineColors.put(tipColor.a);

            lineInds.put((x * 2) + 1);
        }

        normalLines.setLocalTranslation(Vector3f.ZERO);
        normalLines.getLocalRotation().loadIdentity();
        normalLines.onDraw(r);

        r.setPolygonOffset(1.0f, 0.0f);
    }


    /**
     * <code>drawNormals</code> draws the normals for a given Spatial and
     * optionally its children.
     * 
     * @param element
     *            the Spatial to draw normals for.
     * @param r
     *            the Renderer to use to draw the normals.
     * @param size
     *            the length of the drawn normal (default is -1.0f which means
     *            autocalc based on boundings - if any).
     * @param doChildren
     *            if true, normals for any children will also be drawn
     */
    public static void drawNormals(Spatial element, Renderer r,
            float size, boolean doChildren) {
        if (element == null)
            return;

        if (normZState == null) {
            normZState = r.createZBufferState();
            normalLines.setRenderState(normZState);
            normalLines.updateRenderState();
        }

        int state = r.getCamera().getPlaneState();
        if (element.getWorldBound() != null
                && r.getCamera().contains(element.getWorldBound()) == Camera.FrustumIntersect.Outside) {
            r.getCamera().setPlaneState(state);
            return;
        }
        r.getCamera().setPlaneState(state);
        if (element instanceof Geometry
                && element.getCullHint() != Spatial.CullHint.Always) {
            Geometry geom = (Geometry) element;

            float rSize = size;
            if (rSize == -1) {
                BoundingVolume vol = element.getWorldBound();
                if (vol != null) {
                    measureBox.setCenter(vol.getCenter());
                    measureBox.xExtent = 0;
                    measureBox.yExtent = 0;
                    measureBox.zExtent = 0;
                    measureBox.mergeLocal(vol);
                    rSize = AUTO_NORMAL_RATIO
                            * ((measureBox.xExtent + measureBox.yExtent + measureBox.zExtent) / 3f);
                } else
                    rSize = 1.0f;
            }

            FloatBuffer norms = geom.getNormalBuffer();
            FloatBuffer verts = geom.getVertexBuffer();
            if (norms != null && verts != null
                    && norms.limit() == verts.limit()) {
                FloatBuffer lineVerts = normalLines.getVertexBuffer();
                if (lineVerts.capacity() < (3 * (2 * geom.getVertexCount()))) {
                    normalLines.setVertexBuffer(null);
                    System.gc();
                    lineVerts = BufferUtils.createVector3Buffer(geom
                            .getVertexCount() * 2);
                    normalLines.setVertexBuffer(lineVerts);
                } else {
                    normalLines.setVertexCount(2 * geom.getVertexCount());
                    lineVerts.clear();
                }

                FloatBuffer lineColors = normalLines.getColorBuffer();
                if (lineColors.capacity() < (4 * (2 * geom.getVertexCount()))) {
                    normalLines.setColorBuffer(null);
                    System.gc();
                    lineColors = BufferUtils.createColorBuffer(geom
                            .getVertexCount() * 2);
                    normalLines.setColorBuffer(lineColors);
                } else {
                    lineColors.clear();
                }

                IntBuffer lineInds = normalLines.getIndexBuffer();
                if (lineInds == null
                        || lineInds.capacity() < (normalLines.getVertexCount())) {
                    normalLines.setIndexBuffer(null);
                    System.gc();
                    lineInds = BufferUtils.createIntBuffer(geom
                            .getVertexCount() * 2);
                    normalLines.setIndexBuffer(lineInds);
                } else {
                    lineInds.clear();
                    lineInds.limit(normalLines.getVertexCount());
                }

                verts.rewind();
                norms.rewind();
                lineVerts.rewind();
                lineInds.rewind();

                for (int x = 0; x < geom.getVertexCount(); x++) {
                    _normalVect.set(verts.get(), verts.get(), verts.get());
                    _normalVect.multLocal(geom.getWorldScale());
                    lineVerts.put(_normalVect.x);
                    lineVerts.put(_normalVect.y);
                    lineVerts.put(_normalVect.z);

                    lineColors.put(NORMAL_COLOR_BASE.r);
                    lineColors.put(NORMAL_COLOR_BASE.g);
                    lineColors.put(NORMAL_COLOR_BASE.b);
                    lineColors.put(NORMAL_COLOR_BASE.a);

                    lineInds.put(x * 2);

                    _normalVect.addLocal(norms.get() * rSize, norms.get()
                            * rSize, norms.get() * rSize);
                    lineVerts.put(_normalVect.x);
                    lineVerts.put(_normalVect.y);
                    lineVerts.put(_normalVect.z);

                    lineColors.put(NORMAL_COLOR_TIP.r);
                    lineColors.put(NORMAL_COLOR_TIP.g);
                    lineColors.put(NORMAL_COLOR_TIP.b);
                    lineColors.put(NORMAL_COLOR_TIP.a);

                    lineInds.put((x * 2) + 1);
                }

                normalLines.setLocalTranslation(geom.getWorldTranslation());
                normalLines.setLocalRotation(geom.getWorldRotation());
                normalLines.onDraw(r);
            }

        }

        if (doChildren && element instanceof Node) {
            Node n = (Node) element;
            if (n.getChildren() != null) {
                for (int i = n.getChildren().size(); --i >= 0;)
                    drawNormals(n.getChild(i), r, size, true);
            }
        }
    }

    public static void drawTangents(Spatial element, Renderer r,
            float size, boolean doChildren) {
        if (element == null)
            return;

        if (normZState == null) {
            normZState = r.createZBufferState();
            normalLines.setRenderState(normZState);
            normalLines.updateRenderState();
        }

        int state = r.getCamera().getPlaneState();
        if (element.getWorldBound() != null
                && r.getCamera().contains(element.getWorldBound()) == Camera.FrustumIntersect.Outside) {
            r.getCamera().setPlaneState(state);
            return;
        }
        r.getCamera().setPlaneState(state);
        if (element instanceof Geometry
                && element.getCullHint() != Spatial.CullHint.Always) {
            Geometry geom = (Geometry) element;

            float rSize = size;
            if (rSize == -1) {
                BoundingVolume vol = element.getWorldBound();
                if (vol != null) {
                    measureBox.setCenter(vol.getCenter());
                    measureBox.xExtent = 0;
                    measureBox.yExtent = 0;
                    measureBox.zExtent = 0;
                    measureBox.mergeLocal(vol);
                    rSize = AUTO_NORMAL_RATIO
                            * ((measureBox.xExtent + measureBox.yExtent + measureBox.zExtent) / 3f);
                } else
                    rSize = 1.0f;
            }

            FloatBuffer norms = geom.getTangentBuffer();
            FloatBuffer verts = geom.getVertexBuffer();
            if (norms != null && verts != null
                    && norms.limit() == verts.limit()) {
                FloatBuffer lineVerts = normalLines.getVertexBuffer();
                if (lineVerts.capacity() < (3 * (2 * geom.getVertexCount()))) {
                    normalLines.setVertexBuffer(null);
                    System.gc();
                    lineVerts = BufferUtils.createVector3Buffer(geom
                            .getVertexCount() * 2);
                    normalLines.setVertexBuffer(lineVerts);
                } else {
                    normalLines.setVertexCount(2 * geom.getVertexCount());
                    lineVerts.clear();
                }

                FloatBuffer lineColors = normalLines.getColorBuffer();
                if (lineColors.capacity() < (4 * (2 * geom.getVertexCount()))) {
                    normalLines.setColorBuffer(null);
                    System.gc();
                    lineColors = BufferUtils.createColorBuffer(geom
                            .getVertexCount() * 2);
                    normalLines.setColorBuffer(lineColors);
                } else {
                    lineColors.clear();
                }

                IntBuffer lineInds = normalLines.getIndexBuffer();
                if (lineInds == null
                        || lineInds.capacity() < (normalLines.getVertexCount())) {
                    normalLines.setIndexBuffer(null);
                    System.gc();
                    lineInds = BufferUtils.createIntBuffer(geom
                            .getVertexCount() * 2);
                    normalLines.setIndexBuffer(lineInds);
                } else {
                    lineInds.clear();
                    lineInds.limit(normalLines.getVertexCount());
                }

                verts.rewind();
                norms.rewind();
                lineVerts.rewind();
                lineInds.rewind();

                for (int x = 0; x < geom.getVertexCount(); x++) {
                    _normalVect.set(verts.get(), verts.get(), verts.get());
                    _normalVect.multLocal(geom.getWorldScale());
                    lineVerts.put(_normalVect.x);
                    lineVerts.put(_normalVect.y);
                    lineVerts.put(_normalVect.z);

                    lineColors.put(TANGENT_COLOR_BASE.r);
                    lineColors.put(TANGENT_COLOR_BASE.g);
                    lineColors.put(TANGENT_COLOR_BASE.b);
                    lineColors.put(TANGENT_COLOR_BASE.a);

                    lineInds.put(x * 2);

                    _normalVect.addLocal(norms.get() * rSize, norms.get()
                            * rSize, norms.get() * rSize);
                    lineVerts.put(_normalVect.x);
                    lineVerts.put(_normalVect.y);
                    lineVerts.put(_normalVect.z);

                    lineColors.put(TANGENT_COLOR_BASE.r);
                    lineColors.put(TANGENT_COLOR_BASE.g);
                    lineColors.put(TANGENT_COLOR_BASE.b);
                    lineColors.put(TANGENT_COLOR_BASE.a);

                    lineInds.put((x * 2) + 1);
                }

                if (geom != null) {
                    normalLines.setLocalTranslation(geom.getWorldTranslation());
                    normalLines.setLocalRotation(geom.getWorldRotation());
                    normalLines.onDraw(r);
                }
            }

        }

        if (doChildren && element instanceof Node) {
            Node n = (Node) element;
            if (n.getChildren() != null) {
                for (int i = n.getChildren().size(); --i >= 0;)
                    drawTangents(n.getChild(i), r, size, true);
            }
        }
    }

    // -- **** METHODS FOR DRAWING AXIS **** -- //

    private static final AxisRods rods = new AxisRods("debug_rods", true, 1);
    static {
        rods.setRenderQueueMode(Renderer.QUEUE_SKIP);
    }
    private static boolean axisInited = false;

    public static void drawAxis(Spatial spat, Renderer r) {
        drawAxis(spat, r, true, false);
    }

    public static void initAxis(Renderer r)
    {
        if (boundsZState == null)
            boundsZState = r.createZBufferState();
        rods.setRenderState(boundsZState);

        BlendState blendState = r.createBlendState();
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        rods.setRenderState(blendState);
        rods.updateRenderState();
        rods.updateGeometricState(0, false);
        axisInited = true;
    }

    public static void drawOrigin(Renderer r, float scale)
    {
        if (!axisInited)
            initAxis(r);
        rods.getLocalTranslation().set(Vector3f.ZERO);
        rods.getLocalScale().set(Vector3f.UNIT_XYZ.mult(scale));
        rods.getLocalRotation().loadIdentity();
        rods.updateGeometricState(0, false);
        rods.draw(r);
    }

    public static void drawAxis(Spatial spat, Renderer r, boolean drawChildren, boolean drawAll) {
        if (!axisInited)
            initAxis(r);

        if (drawAll
                || (spat instanceof Geometry && !(spat.getParent() instanceof SkinNode))
                || (spat instanceof SkinNode)) {
            if (spat.getWorldBound() != null) {
                float rSize;
                BoundingVolume vol = spat.getWorldBound(); 
                if (vol != null) {
                    measureBox.setCenter(vol.getCenter());
                    measureBox.xExtent = 0;
                    measureBox.yExtent = 0;
                    measureBox.zExtent = 0;
                    measureBox.mergeLocal(vol);
                    rSize = 1f * ((measureBox.xExtent + measureBox.yExtent + measureBox.zExtent) / 3f);
                } else
                    rSize = 1.0f;

                rods.getLocalTranslation().set(spat.getWorldBound().getCenter());
                rods.getLocalScale().set(rSize, rSize, rSize);
            } else {
                rods.getLocalTranslation().set(spat.getWorldTranslation());
                rods.getLocalScale().set(spat.getWorldScale());
            }
            rods.getLocalRotation().set(spat.getWorldRotation());
            rods.updateGeometricState(0, false);
    
            rods.draw(r);
        }

        if ((spat instanceof Node) && drawChildren) {
            Node n = (Node) spat;
            if (n.getChildren() == null) return;
            for (int x = 0, count = n.getChildren().size(); x < count; x++) {
                drawAxis(n.getChild(x), r, drawChildren, drawAll);
            }
        }
    }


    // -- **** METHODS FOR DISPLAYING BUFFERS **** -- //
    public static final int NORTHWEST = 0;
    public static final int NORTHEAST = 1;
    public static final int SOUTHEAST = 2;
    public static final int SOUTHWEST = 3;

    private static final Quad bQuad = new Quad("", 128, 128);
    private static Texture2D bufTexture;
    private static TextureRenderer bufTexRend;

    static {
        bQuad.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        bQuad.setCullHint(Spatial.CullHint.Never);
    }

    public static void drawBuffer(Texture.RenderToTextureType rttSource, int location, Renderer r) {
        drawBuffer(rttSource, location, r, r.getWidth() / 6.25f);
    }

    public static void drawBuffer(Texture.RenderToTextureType rttSource, int location, Renderer r,
            float size) {
        r.flush();
        float locationX = r.getWidth(), locationY = r.getHeight();
        bQuad.resize(size, (r.getHeight() / (float) r.getWidth()) * size);
        if (bQuad.getRenderState(RenderState.StateType.Texture) == null) {
            TextureState ts = r.createTextureState();
            bufTexture = new Texture2D();
            ts.setTexture(bufTexture);
            bQuad.setRenderState(ts);
            bQuad.updateRenderState();
        }

        bufTexture.setRenderToTextureType(rttSource);

        if (bufTexRend == null) {
            bufTexRend = DisplaySystem.getDisplaySystem()
                    .createTextureRenderer(256, 256,
                            TextureRenderer.Target.Texture2D);
            bufTexRend.setupTexture(bufTexture);
        }
        int width = r.getWidth();
        if (!FastMath.isPowerOfTwo(width)) {
            int newWidth = 2;
            do {
                newWidth <<= 1;

            } while (newWidth < width);
            bQuad.getTextureCoords(0).coords.put(4, width / (float) newWidth);
            bQuad.getTextureCoords(0).coords.put(6, width / (float) newWidth);
            width = newWidth;
        }

        int height = r.getHeight();
        if (!FastMath.isPowerOfTwo(height)) {
            int newHeight = 2;
            do {
                newHeight <<= 1;

            } while (newHeight < height);
            bQuad.getTextureCoords(0).coords.put(1, height / (float) newHeight);
            bQuad.getTextureCoords(0).coords.put(7, height / (float) newHeight);
            height = newHeight;
        }

        bufTexRend.copyToTexture(bufTexture, width, height);

        float loc = size * .75f;
        switch (location) {
            case NORTHWEST:
                locationX = loc;
                locationY -= loc;
                break;
            case NORTHEAST:
                locationX -= loc;
                locationY -= loc;
                break;
            case SOUTHEAST:
                locationX -= loc;
                locationY = loc;
                break;
            case SOUTHWEST:
            default:
                locationX = loc;
                locationY = loc;
                break;
        }

        bQuad.getWorldTranslation().set(locationX, locationY, 0);

        bQuad.onDraw(r);
        r.flush();
    }
}