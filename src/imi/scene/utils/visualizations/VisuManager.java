/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package imi.scene.utils.visualizations;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import java.util.ArrayList;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class VisuManager extends Entity
{
    /** Convenience reference to the manager of OUR VERY WORLD **/
    private WorldManager        m_WM        = null;
    /** The root of our jME scene graph **/
    private Node                m_jmeRoot   = null;
    /** Collection of all the position objects we are tracking **/
    private FastList<PositionVisualization> m_positionObjects = null;
    /** Collection of all the box objects we are tracking **/
    private FastList<BoxVisualization> m_boxObjects = null;
    /** Collection of all the line objects we are tracking **/
    private FastList<LineVisualization> m_lineObjects = null;
    /** ProcessorComponent! **/
    private UpdateDriver m_updater = null;
    /** Used for toggling wireframe **/
    private WireframeState m_wireframeState = null;

    /**
     * Constuct a new verlet visualizer with the given worldmanager
     * @param name
     * @param wm
     */
    public VisuManager(String name, WorldManager wm)
    {
        super(name); // Call the super!
        // Allocate data members
        m_WM = wm;
        m_jmeRoot = new Node("VerletVisualsROOT");
        m_updater = new UpdateDriver();
        // Get the ZBufferState
        ZBufferState zstate = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        m_jmeRoot.setRenderState(zstate);
        m_wireframeState = (WireframeState)wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        m_jmeRoot.setRenderState(m_wireframeState);
        // Create our render component
        RenderComponent rc = wm.getRenderManager().createRenderComponent(m_jmeRoot);
        rc.setLightingEnabled(false); // Unlight visualizations
        super.addComponent(RenderComponent.class, rc);
        // Add ourselves to the world manager
        wm.addEntity(this);
        // Add our processor component on
        super.addComponent(UpdateDriver.class, m_updater);
    }

    /**
     * This method adds a verlet object to the collection
     * @param verletObject The object to add
     */
    public void addPositionObject(Vector3f position)
    {
        if (m_positionObjects == null) // First object, allocate collection space
            m_positionObjects = new FastList<PositionVisualization>();

        PositionVisualization visuals = new PositionVisualization(position, m_WM);
        m_positionObjects.add(visuals);
        m_jmeRoot.attachChild(visuals.objectRoot);
        m_jmeRoot.updateRenderState();
    }

    public void addPositionObject(Vector3f position, ColorRGBA color, float radius) 
    {
        if (m_positionObjects == null) // First object, allocate collection space
            m_positionObjects = new FastList<PositionVisualization>();

        PositionVisualization visuals = new PositionVisualization(position, radius, m_WM);
        visuals.getSphere().setDefaultColor(color);
        m_positionObjects.add(visuals);
        m_jmeRoot.attachChild(visuals.objectRoot);
        m_jmeRoot.updateRenderState();  
    }
    
    public void addBoxObject(Vector3f origin, Vector3f min, Vector3f max, ColorRGBA color) 
    {
        if (m_boxObjects == null) // First object, allocate collection space
            m_boxObjects = new FastList<BoxVisualization>();

        BoxVisualization visuals = new BoxVisualization(origin, min, max, color);
        m_boxObjects.add(visuals);
        m_jmeRoot.attachChild(visuals.objectRoot);
        m_jmeRoot.updateRenderState();  
    }
    
    public void addLineObject(ArrayList<Vector3f> origin, ArrayList<Vector3f> point, ColorRGBA color, float width) 
    {
        if (m_lineObjects == null) // First object, allocate collection space
            m_lineObjects = new FastList<LineVisualization>();

        LineVisualization visuals = new LineVisualization(origin, point, color, width);
        m_lineObjects.add(visuals);
        m_jmeRoot.attachChild(visuals.objectRoot);
        m_jmeRoot.updateRenderState();  
    }
    
    public boolean removeBoxObject(Vector3f position)
    {
        return false; // TODO
    }
    /**
     * This method removes the specified verlet object from the manager
     * @param verletObject
     * @return True if found, false otherwise
     */
    public boolean removePositionObject(Vector3f position)
    {
        boolean result = false;
        if (m_positionObjects != null) // Are there any objects?
        {
            return m_positionObjects.remove(position); // TODO
            
            
//            int index = m_objects.indexOf(new PositionVisualization(position));
//            if (index != -1) // did we find anything?
//            {
//                m_objects.remove(index);
//                result = true;
//            }
//            else
//                result = false;
//
//            // was this the last one?
//            if (m_objects.size() == 0) // Yes, free the memory
//                m_objects = null;
        }
        else // no objects to remove
            result = false;

        return result;
    }

    public void clearObjects()
    {
        // begone objects!
        if (m_positionObjects != null)
            m_positionObjects.clear();
        m_positionObjects = null;
        if (m_boxObjects != null)
            m_boxObjects.clear();
        if (m_lineObjects != null)
            m_lineObjects.clear();
        m_boxObjects = null;
        m_lineObjects = null;
        m_positionObjects = null;
        m_jmeRoot.detachAllChildren();
    }

    public void update() 
    {
        if (m_positionObjects == null)
            return;
        
        for (PositionVisualization obj : m_positionObjects)
            obj.updatePositions();
        
        if (m_boxObjects == null)
            return;
        
        for (BoxVisualization obj : m_boxObjects)
            obj.updatePositions();
    }

    public void setWireframe(boolean bWireframeOn)
    {
        m_wireframeState.setEnabled(bWireframeOn);
        m_jmeRoot.updateRenderState();
    }

    private class UpdateDriver extends ProcessorComponent
    {
        public UpdateDriver()
        {
        }

        @Override
        public void compute(ProcessorArmingCollection arg0) {
            update();
        }

        @Override
        public void commit(ProcessorArmingCollection arg0) {
            // Do nothing!
        }

        @Override
        public void initialize() {
            ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
            collection.addCondition(new NewFrameCondition(this));
            setArmingCondition(collection);
        }

        @Override
        public void compute() {

        }

        @Override
        public void commit() {

        }

    }
}
