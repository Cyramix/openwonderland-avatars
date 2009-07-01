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

import com.jme.scene.Node;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.character.VerletArm;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This class handles the visual representations of Verlet system components.
 * @author Ronald E Dahlgren
 */
@ExperimentalAPI
public class VerletVisualManager extends Entity
{
    /** Convenience reference to the manager of OUR VERY WORLD **/
    private WorldManager        m_WM        = null;
    /** The root of our jME scene graph **/
    private Node                m_jmeRoot   = null;
    /** Collection of all the verlet objects we are tracking **/
    private FastList<VerletObjectVisualization> m_objects = null;
    /** ProcessorComponent! **/
    private UpdateDriver m_updater = null;
    /** Used for toggling wireframe **/
    private WireframeState m_wireframeState = null;

    /**
     * Constuct a new verlet visualizer with the given worldmanager
     * @param name
     * @param wm
     */
    public VerletVisualManager(String name, WorldManager wm)
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
    public void addVerletObject(VerletArm verletObject)
    {
        if (m_objects == null) // First object, allocate collection space
            m_objects = new FastList<VerletObjectVisualization>();

        VerletObjectVisualization visuals = new VerletObjectVisualization(verletObject);
        visuals.mapParticles(); // do the processing
        visuals.mapConstraints();

        m_objects.add(visuals);

        m_jmeRoot.attachChild(visuals.m_objectRoot);
//        m_jmeRoot.attachChild(visuals.m_constraintLine);

        // hard code a single update
        visuals.updateConstraintVisuals();
        m_jmeRoot.updateRenderState();

    }

    /**
     * This method removes the specified verlet object from the manager
     * @param verletObject
     * @return True if found, false otherwise
     */
    public boolean removeVerletObject(VerletArm verletObject)
    {
        boolean result = false;
        if (m_objects != null) // Are there any objects?
        {
            int index = m_objects.indexOf(new VerletObjectVisualization(verletObject));
            if (index != -1) // did we find anything?
            {
                m_objects.remove(index);
                result = true;
            }
            else
                result = false;

            // was this the last one?
            if (m_objects.size() == 0) // Yes, free the memory
                m_objects = null;
        }
        else // no objects to remove
            result = false;

        return result;
    }

    public void clearVerletObjects()
    {
        // begone objects!
        m_objects.clear();
        m_objects = null;
    }

    public void update() {
        for (VerletObjectVisualization obj : m_objects)
        {
            obj.updateParticlePositions();
            obj.updateConstraintVisuals();
        }
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

        
        public void compute() {

        }

        
        public void commit() {

        }

    }
}
