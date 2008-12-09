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

import imi.character.CharacterAttributes;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class NinjaAvatar extends Ninja 
{
    public NinjaAvatar(CharacterAttributes attributes, WorldManager wm) 
    {
        super(attributes, wm);
        
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
        
        // Test
        //m_context.getStates().get(PunchState.class).setAnimationName("MaleSmile");
        
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

    /**
     * Construct a new instance configured with the specified file.
     * @param configurationFile
     * @param wm
     */
    public NinjaAvatar(URL configurationFile, WorldManager wm)
    {
        super(configurationFile, wm);
    }
    
}
