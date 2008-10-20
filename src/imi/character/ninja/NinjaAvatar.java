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

import imi.scene.PMatrix;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class NinjaAvatar extends Ninja 
{

    public class NinjaAvatarAttributes extends Attributes
    {
        String [] m_animations = null;
        
        public NinjaAvatarAttributes(String name) {
            super(name);
            setModelFile("assets/models/collada/Avatars/Male2/Male_Bind.dae");
            String [] anims = new String[2];
            anims[0] = "assets/models/collada/Avatars/Male2/Male_Idle.dae";
            anims[1] = "assets/models/collada/Avatars/Male2/Male_Walk.dae";
            m_animations = anims;
        }

        public String[] getAnimations() {
            return m_animations;
        }

        public void setAnimations(String[] animations) {
            this.m_animations = animations;
        }
    }
    
    public NinjaAvatar(String name, WorldManager wm) 
    {
        super(name, null, null, null, 5.0f, wm);
        
        // Rotate around
        PMatrix origin = new PMatrix();
        origin.buildRotationY(160.0f);
        m_modelInst.getTransform().setLocalMatrix(origin);
        
        m_context.getController().setReverseHeading(true);
        m_context.getStates().get(IdleState.class).setAnimationName("Male_Walk2");
        m_context.getStates().get(PunchState.class).setAnimationName("Male_Walk2");
        m_context.getStates().get(SitState.class).setAnimationName("Male_Walk2");
        m_context.getStates().get(TurnState.class).setAnimationName("Male_Walk2");
        m_context.getStates().get(WalkState.class).setAnimationName("Male_Walk2");
        ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Male_Walk2");
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("Male_Walk2");
    }
    
    @Override
    protected Attributes createAttributes(String name)
    {
        return new NinjaAvatarAttributes(name);
    }

}
