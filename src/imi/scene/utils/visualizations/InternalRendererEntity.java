/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.utils.visualizations;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.scene.boundingvolumes.PSphere;
import java.util.ArrayList;
import javolution.util.FastTable;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class InternalRendererEntity extends Entity
{
    private InternalRendererNode root = null;

    public InternalRendererEntity(WorldManager wm)
    {
        super("Internal Renderer");
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();

        // The JME root with the magical rendering capabilities
        root = new InternalRendererNode();

        // Create a scene component and set the root to our jscene
        RenderComponent rc = wm.getRenderManager().createRenderComponent(root);

        // Add the scene component with our jscene to the entity
        addComponent(RenderComponent.class, rc);

        // Add our processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));

        // Add the processor collection component to the entity
        addComponent(ProcessorCollectionComponent.class, processorCollection);

        // Make available through the world manager
        wm.addUserData(InternalRendererEntity.class, this);

        // Add the entity to the world manager
        wm.addEntity(this);


        // Use default render states (unless that method is overriden)
        setRenderStates(wm);
        wm.addToUpdateList(root);
    }

    public void addBBSpatial(Spatial s) {
        if (!root.getJmeBB().contains(s))
            root.getJmeBB().add(s);
    }

    public void addYellowLine(Vector3f startPoint, Vector3f endPoint) {
        FastTable<Vector3f> lineTable = new FastTable <Vector3f>();
        lineTable.add(startPoint);
        lineTable.add(endPoint);
        root.getUserLines().add(lineTable);
    }

    public void clearYellowLines() {
        root.getUserLines().clear();
    }

    public void removeBBSpatial(Spatial s) {
        root.getJmeBB().remove(s);
    }

    public void addNSpatial(Spatial s) {
        if (!root.getJmeN().contains(s))
            root.getJmeN().add(s);
    }

    public void removeNSpatial(Spatial s) {
        root.getJmeN().remove(s);
    }

    public void setOriginVisible(boolean b) {
        root.setDrawOrigin(b);
    }

    public void setRedSphere(PSphere sphereCheck) {
        root.setRedSphere(sphereCheck);
    }
    
    public void setCyanTriangles(FastTable<Vector3f> tris)
    {
        FastTable<Vector3f> greyLines = root.getCyanLines();
        greyLines.clear();

        int size = tris.size()-2;
        if (size == 0 && !tris.isEmpty())
        {
            greyLines.add(tris.get(0));
            greyLines.add(tris.get(1));

            greyLines.add(tris.get(1));
            greyLines.add(tris.get(2));

            greyLines.add(tris.get(2));
            greyLines.add(tris.get(0));
        }
        else
        for (int i = 0; i < size; i += 3)
        {
            greyLines.add(tris.get(i));
            greyLines.add(tris.get(i+1));

            greyLines.add(tris.get(i+1));
            greyLines.add(tris.get(i+2));

            greyLines.add(tris.get(i+2));
            greyLines.add(tris.get(i));
        }
    }
    
    public void setPinkTriangles(FastTable<Vector3f> tris)
    {
        FastTable<Vector3f> whiteLines = root.getPinkLines();
        whiteLines.clear();

        int size = tris.size()-2;
        if (size == 0 && !tris.isEmpty())
        {
            whiteLines.add(tris.get(0));
            whiteLines.add(tris.get(1));

            whiteLines.add(tris.get(1));
            whiteLines.add(tris.get(2));

            whiteLines.add(tris.get(2));
            whiteLines.add(tris.get(0));
        }
        else
        for (int i = 0; i < size; i += 3)
        {
            whiteLines.add(tris.get(i));
            whiteLines.add(tris.get(i+1));

            whiteLines.add(tris.get(i+1));
            whiteLines.add(tris.get(i+2));

            whiteLines.add(tris.get(i+2));
            whiteLines.add(tris.get(i));
        }
    }

    public InternalRendererNode getInternalRoot() {
        return root;
    }

    private void setRenderStates(WorldManager worldManager)
    {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(ColorRGBA.white);

        // Light state
        LightState ls = (LightState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Light);
        ls.setEnabled(true);

        // Cull State
        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        // Wireframe State
        WireframeState ws = (WireframeState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Wireframe);
        ws.setEnabled(false);

        // Push 'em down the pipe
        root.setRenderState(matState);
        root.setRenderState(buf);
        root.setRenderState(cs);
        root.setRenderState(ws);
        root.setRenderState(ls);
        root.setRenderQueueMode(Renderer.QUEUE_SKIP);
    }

}
