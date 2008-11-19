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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.scene.utils.visualizations;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import imi.character.VerletArm;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class handles the visual representations of Verlet system components.
 * @author Ronald E Dahlgren
 */
public class VerletVisualManager extends Entity
{
    /** Convenience reference to the manager of OUR VERY WORLD **/
    private WorldManager        m_WM        = null;
    /** The root of our jME scene graph **/
    private Node                m_jmeRoot   = null;
    /** Collection of all the verlet objects we are tracking **/
    private FastList<VerletObjectVisualization> m_objects = null;

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

        // Create our render component
        RenderComponent rc = wm.getRenderManager().createRenderComponent(m_jmeRoot);
        rc.setLightingEnabled(false); // Unlight visualizations
        super.addComponent(RenderComponent.class, rc);
        // Add ourselves to the world manager
        wm.addEntity(this);
    }

    public int addVerletObject(VerletArm verletObject)
    {
        if (m_objects == null) // First object, allocate collection space
            m_objects = new FastList<VerletObjectVisualization>();

        VerletObjectVisualization visuals = new VerletObjectVisualization(verletObject);
        visuals.mapParticles(); // do the processing
        visuals.mapConstraints();

        m_objects.add(visuals);

        m_jmeRoot.attachChild(visuals.m_objectRoot);
        //m_jmeRoot.attachChild(visuals.m_constraintLine);

        // hard code a single update
        visuals.updateConstraintVisuals();
        m_jmeRoot.updateRenderState();
        return m_objects.size() - 1;

    }
    
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

    public void update() {
        for (VerletObjectVisualization obj : m_objects)
        {
            obj.updateParticlePositions();
            obj.updateConstraintVisuals();
        }
    }
}
