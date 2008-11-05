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

import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class NinjaAvatar extends Ninja 
{
    public class NinjaAvatarAttributes extends Attributes
    {
        String [] m_animations = new String [0];
        
        public NinjaAvatarAttributes(String name) {
            super(name);
            setModelFile("assets/models/collada/Avatars/Male2/Male_Bind.dae");
            ArrayList<String> anims = new ArrayList<String>();
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Idle.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_StandToSit.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Wave.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Walk.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Sitting.dae");
            if (true)
            {
                anims.add("assets/models/collada/Avatars/MaleZip/Male_Run.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Bow.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Cheer.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Clap.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Follow.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Jump.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Laugh.dae");
            }
            m_animations = anims.toArray(m_animations);
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
        super(name, wm);
        
        // Tweak animation names and speeds
        m_context.getController().setReverseHeading(true);
        m_context.getStates().get(IdleState.class).setAnimationName("Male_Idle");
        m_context.getStates().get(PunchState.class).setAnimationName("Male_Wave");
        m_context.getStates().get(TurnState.class).setAnimationName("Male_Idle");
        m_context.getStates().get(WalkState.class).setAnimationName("Male_Walk");
        m_context.getStates().get(SitState.class).setAnimationName("Male_StandToSit");
        m_context.getStates().get(FlyState.class).setAnimationName("Male_Sitting");
        ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Male_Sitting");
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("Male_StandToSit");    
        m_context.getStates().get(IdleState.class).setTransitionDuration(0.1f);
        m_context.getStates().get(WalkState.class).setTransitionDuration(0.1f);
        
        //m_context.getStates().get(PunchState.class).setAnimationSpeed(0.3f);
        
        m_context.getStates().get(SitState.class).setAnimationSpeed(3.0f);
        m_context.getStates().get(SitState.class).setTransitionDuration(0.05f);
        ((SitState)m_context.getStates().get(SitState.class)).setSittingAnimationTime(0.7f);
        
        ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingTransitionDuration(0.05f);
        
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationSpeed(4.0f);
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationTime(0.725f);
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpTransitionDuration(0.05f);
        
        // For testing, no transitions
        if (false)
        {
            m_context.getStates().get(IdleState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(WalkState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(TurnState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(SitState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(PunchState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(FlyState.class).setTransitionDuration(0.0f);
            ((SitState)m_context.getStates().get(SitState.class)).setGettingUpTransitionDuration(0.0f);
            ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingTransitionDuration(0.0f);
        }
        
        // For testing
        //m_context.getStates().get(PunchState.class).setAnimationSpeed(1.0f);
        if (false)
        {
            m_context.getStates().get(IdleState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(PunchState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(TurnState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(WalkState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(SitState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(FlyState.class).setAnimationName("Male_Walk");
            ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Male_Walk");
            ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("Male_Walk"); 
        }
    }
        
    @Override
    protected Attributes createAttributes(String name)
    {
        return new NinjaAvatarAttributes(name);
    }

}
