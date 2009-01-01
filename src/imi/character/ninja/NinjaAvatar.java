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
package imi.character.ninja;

import imi.character.CharacterAttributes;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides a ready to use Ninja. 
 * It is designed to work with the NinjaAvatarAttributes class and sets up 
 * animation names for the state machine.
 * @author Lou Hayt
 */
public class NinjaAvatar extends Ninja 
{
    /**
     * Construct a new NinjaAvatar with the provided attributes and world manager.
     * @param attributes
     * @param wm
     */
    public NinjaAvatar(CharacterAttributes attributes, WorldManager wm) {
        this(attributes, wm , true);
    }
    /**
     * Construct a new NinjaAvatar with the provided attributes and world manager.
     * @param attributes
     * @param wm
     */
    public NinjaAvatar(CharacterAttributes attributes, WorldManager wm, boolean addEntity)
    {
        super(attributes, wm, addEntity);
        
        // For female loading test\demo
        if (attributes instanceof NinjaFemaleAvatarAttributes)
            femaleContextSetup();
        else
            maleContextSetup();
    }

    /**
     * Construct a new instance configured with the specified file.
     * @param configurationFile
     * @param wm
     */
    public NinjaAvatar(URL configurationFile, WorldManager wm)
    {
        super(configurationFile, wm);
        maleContextSetup();
    }

    private void maleContextSetup()
    {
        // Tweak animation names and speeds
        m_context.getController().setReverseHeading(true);
        m_context.getStateMapping().get(IdleState.class).setAnimationName("Male_Idle");
        m_context.getStateMapping().get(PunchState.class).setAnimationName("Male_Wave");
        m_context.getStateMapping().get(TurnState.class).setAnimationName("Male_Idle");
        m_context.getStateMapping().get(WalkState.class).setAnimationName("Male_Walk");
        m_context.getStateMapping().get(SitState.class).setAnimationName("Male_StandToSit");
        m_context.getStateMapping().get(FlyState.class).setAnimationName("Male_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingAnimationName("Male_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpAnimationName("Male_StandToSit");

        // Make him smile when waving
        ((PunchState)m_context.getState(PunchState.class)).setFacialAnimationName("MaleSmile");

        // For testing, no transitions
        if (false)
        {
            m_context.getStateMapping().get(IdleState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(WalkState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(TurnState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(SitState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(PunchState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(FlyState.class).setTransitionDuration(0.0f);
            ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpTransitionDuration(0.0f);
            ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingTransitionDuration(0.0f);
        }

        // For testing
        //m_context.getStateMapping().get(PunchState.class).setAnimationSpeed(1.0f);
        if (false)
        {
            m_context.getStateMapping().get(IdleState.class).setAnimationName("Male_Walk");
            m_context.getStateMapping().get(PunchState.class).setAnimationName("Male_Walk");
            m_context.getStateMapping().get(TurnState.class).setAnimationName("Male_Walk");
            m_context.getStateMapping().get(WalkState.class).setAnimationName("Male_Walk");
            m_context.getStateMapping().get(SitState.class).setAnimationName("Male_Walk");
            m_context.getStateMapping().get(FlyState.class).setAnimationName("Male_Walk");
            ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingAnimationName("Male_Walk");
            ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpAnimationName("Male_Walk");
        }
    }
    
    private void femaleContextSetup()
    {
        // Tweak animation names and speeds
        m_context.getController().setReverseHeading(true);
        m_context.getStateMapping().get(IdleState.class).setAnimationName("Female_Idle");
        //m_context.getStateMapping().get(PunchState.class).setAnimationName("Female_Wave");
        m_context.getStateMapping().get(TurnState.class).setAnimationName("Female_Idle");
        m_context.getStateMapping().get(WalkState.class).setAnimationName("Female_Walk");
        m_context.getStateMapping().get(SitState.class).setAnimationName("Female_StandToSit");
        m_context.getStateMapping().get(FlyState.class).setAnimationName("Female_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingAnimationName("Female_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpAnimationName("Female_StandToSit");

        // Make him smile when waving
        //((PunchState)m_context.getState(PunchState.class)).setFacialAnimationName("MaleSmile");
    }
}
