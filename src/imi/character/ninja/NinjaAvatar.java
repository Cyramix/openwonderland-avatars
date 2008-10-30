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
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Walk.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Wave.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_StandToSit.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Sitting.dae");
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
        m_context.getStates().get(WalkState.class).setTransitionDuration(0.15f);
        m_context.getStates().get(SitState.class).setAnimationSpeed(5.0f);
        ((SitState)m_context.getStates().get(SitState.class)).setSittingAnimationTime(0.2f);
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationSpeed(1.5f);
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationTime(0.005f);
        
        // For testing
        if (false)
        {
            m_context.getStates().get(IdleState.class).setAnimationName("Male_Idle");
            m_context.getStates().get(PunchState.class).setAnimationName("Male_Idle");
            m_context.getStates().get(TurnState.class).setAnimationName("Male_Idle");
            m_context.getStates().get(WalkState.class).setAnimationName("Male_Idle");
            m_context.getStates().get(SitState.class).setAnimationName("Male_Idle");
            m_context.getStates().get(FlyState.class).setAnimationName("Male_Idle");
            ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Male_Idle");
            ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("Male_Idle"); 
        }
    }
        
    @Override
    protected Attributes createAttributes(String name)
    {
        return new NinjaAvatarAttributes(name);
    }

}
