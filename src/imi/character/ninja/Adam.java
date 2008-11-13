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
package imi.character.ninja;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class Adam extends Ninja
{

    public class AdamAttributes extends Attributes
    {
        public AdamAttributes(String name) {
            super(name);
            setBindPoseFile("assets/models/ms3d/Adam.ms3d");
            setTextureFile("assets/textures/AdamDiffuse.png");
        }
    }
    
    public Adam(String name, WorldManager wm)
    {
        //super(name, null, null, null, 0.06f, wm); // ninja scale
        super(name, null, 0.0135f, wm); // avatar scale
        
        // Rotate around
        PMatrix origin = new PMatrix();
        origin.buildRotationY(160.0f);
        m_modelInst.getTransform().setLocalMatrix(origin);
        
        // Tweak animation names and speeds
        m_context.getController().setReverseHeading(true);
        m_context.getStates().get(PunchState.class).setAnimationSpeed(3.0f);
        ((PunchState)m_context.getStates().get(PunchState.class)).setMinimumTimeBeforeTransition(1.25f);
        m_context.getStates().get(TurnState.class).setAnimationName("StrafeRight");
        m_context.getStates().get(TurnState.class).setAnimationSpeed(5.0f);
        ((WalkState)m_context.getStates().get(WalkState.class)).setWalkSpeedFactor(5.0f);
        ((WalkState)m_context.getStates().get(WalkState.class)).setWalkSpeedMax(5.0f);
        m_context.getStates().get(SitState.class).setAnimationName("CrouchWalk");
        ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Crawl");
        ((SitState)m_context.getStates().get(SitState.class)).setSittingAnimationTime(2.0f);
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("CrouchWalk");
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationTime(2.0f);
    }
    
    @Override
    protected Attributes createAttributes(String name)
    {
        return new AdamAttributes(name);
    }
}
