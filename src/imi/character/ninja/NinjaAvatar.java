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

import imi.character.statemachine.corestates.WalkState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.statemachine.corestates.ActionState;
import imi.character.CharacterAttributes;
import imi.character.statemachine.corestates.RunState;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides a ready to use Ninja. 
 * It is designed to work with the NinjaAvatarAttributes class and sets up 
 * animation names for the state machine of the concrete ninja context etc
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
        m_context.getController().setReverseHeading(true);
        
        // Tweak animation names and speeds
        
        WalkState walk = (WalkState)m_context.getState(WalkState.class);
        walk.setImpulse(15.0f);
        walk.setWalkSpeedMax(2.5f);
        walk.setWalkSpeedFactor(1.3f);
        walk.setMinimumTimeBeforeTransition(0.05f);
        walk.setTransitionDuration(0.1f);
        
        RunState run = (RunState)m_context.getState(RunState.class);
        run.setImpulse(15.0f);
        run.setWalkSpeedMax(1.0f);
        run.setWalkSpeedFactor(1.0f);
        run.setMinimumTimeBeforeTransition(0.5f);
        run.setTransitionDuration(0.3f);
        
        TurnState turn = (TurnState)m_context.getState(TurnState.class);
        turn.setAnimationSpeed(1.5f);
        turn.setTransitionDuration(0.05f);
        turn.setMinimumTimeBeforeTransition(0.18f);
        
        SitState sit = (SitState)m_context.getState(SitState.class);
        sit.setSittingAnimationTime(0.7f);
        sit.setTransitionDuration(0.05f);
        sit.setIdleSittingTransitionDuration(0.3f);
        sit.setGettingUpTransitionDuration(0.05f);
        sit.setAnimationSpeed(3.0f);
        sit.setGettingUpAnimationSpeed(4.0f);
        sit.setGettingUpAnimationTime(0.8f);
        
        FallFromSitState fall = (FallFromSitState)m_context.getState(FallFromSitState.class);
        fall.setAnimationName("Male_FallFromSitting");
        fall.setIdleSittingAnimationName("Male_FloorSitting");
        fall.setGettingUpAnimationName("Male_FloorGetup");
        fall.setGettingUpAnimationTime(1.0f);
        fall.setTransitionDuration(0.05f);
        fall.setIdleSittingTransitionDuration(0.5f);
        fall.setGettingUpTransitionDuration(0.1f);
        fall.setAnimationSpeed(2.0f);
        fall.setGettingUpAnimationSpeed(2.0f);
        // Frown when entering the state
        fall.setFacialAnimationName("MaleFrown");
        fall.setFacialAnimationTimeIn(0.75f);
        fall.setFacialAnimationTimeOut(2.0f);
        
        SitOnGroundState sitGround = (SitOnGroundState)m_context.getState(SitOnGroundState.class);
        sitGround.setTransitionReverseAnimation(true);
        sitGround.setAnimationName("Male_FloorGetup");
        sitGround.setIdleSittingAnimationName("Male_FloorSitting");
        sitGround.setGettingUpAnimationName("Male_FloorGetup");
        sitGround.setSittingAnimationTime(0.7f);
        sitGround.setTransitionDuration(1.0f);
        sitGround.setIdleSittingTransitionDuration(0.5f);
        sitGround.setGettingUpTransitionDuration(0.1f);
        sitGround.setAnimationSpeed(1.5f);
        sitGround.setGettingUpAnimationSpeed(2.0f);
        sitGround.setGettingUpAnimationTime(1.0f);
        
        
        m_context.getStateMapping().get(IdleState.class).setAnimationName("Male_Idle");
        m_context.getStateMapping().get(ActionState.class).setAnimationName("Male_Wave");
        m_context.getStateMapping().get(TurnState.class).setAnimationName("Male_Idle");
        m_context.getStateMapping().get(WalkState.class).setAnimationName("Male_Walk");
        m_context.getStateMapping().get(RunState.class).setAnimationName("Male_Run");
        m_context.getStateMapping().get(SitState.class).setAnimationName("Male_StandToSit");
        m_context.getStateMapping().get(FlyState.class).setAnimationName("Male_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingAnimationName("Male_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpAnimationName("Male_StandToSit");

        // Make him smile when waving
        ((ActionState)m_context.getState(ActionState.class)).setFacialAnimationName("MaleSmile");

        // For testing, no transitions
        if (false)
        {
            m_context.getStateMapping().get(IdleState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(WalkState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(TurnState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(SitState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(ActionState.class).setTransitionDuration(0.0f);
            m_context.getStateMapping().get(FlyState.class).setTransitionDuration(0.0f);
            ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpTransitionDuration(0.0f);
            ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingTransitionDuration(0.0f);
        }

        // For testing
        //m_context.getStateMapping().get(PunchState.class).setAnimationSpeed(1.0f);
        if (false)
        {
            m_context.getStateMapping().get(IdleState.class).setAnimationName("Male_Walk");
            m_context.getStateMapping().get(ActionState.class).setAnimationName("Male_Walk");
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
