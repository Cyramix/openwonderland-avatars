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
package imi.character.avatar;

import imi.character.CharacterAttributes;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.corestates.ActionState;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.WalkState;
import imi.utils.input.InputScheme;
import imi.scene.processors.JSceneEventProcessor;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.utils.input.AvatarControlScheme;
import java.awt.event.KeyEvent;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/*
 * avatar! This class is a concrete Character e.g. 
 * Things such as input mechanisms and a state machine
 * layout are provided as a starting point.
 * 
 * @author Lou Hayt
 */
public class Avatar extends imi.character.Character
{
    /**
     * Construct a new avatar with the provided attributes and world manager.
     * @param attributes
     * @param wm
     */
    public Avatar(CharacterAttributes attributes, WorldManager wm)
    {
        this(attributes, wm, true);
    }
    /**
     * Construct a new avatar with the provided attributes and world manager.
     * @param attributes
     * @param wm
     */
    public Avatar(CharacterAttributes attributes, WorldManager wm, boolean addEntity)
    {
        super(attributes, wm, addEntity);
//        m_context = instantiateContext();       // Initialize m_context
        // For female loading test\demo
        if (attributes instanceof FemaleAvatarAttributes)
            femaleContextSetup();
        else
            maleContextSetup();
    }

    /**
     * Construct a new instance with the provided configuration file.
     * @param configurationFile
     * @param wm
     */
    public Avatar(URL configurationFile, WorldManager wm)
    {
        super(configurationFile, wm);
        maleContextSetup();
    }
     
    protected GameContext instantiateContext() {
        return new AvatarContext(this);
    }

    @Override
    protected void finalizeInitialization(xmlCharacter characterDOM) {
        m_context = instantiateContext();
        super.finalizeInitialization(characterDOM);
    }
   
    @Override
    protected void initKeyBindings() 
    {   
        m_keyBindings.put(KeyEvent.VK_SHIFT,        TriggerNames.Movement_Modifier.ordinal());
        m_keyBindings.put(KeyEvent.VK_A,            TriggerNames.Move_Left.ordinal());
        m_keyBindings.put(KeyEvent.VK_D,            TriggerNames.Move_Right.ordinal());
        m_keyBindings.put(KeyEvent.VK_W,            TriggerNames.Move_Forward.ordinal());
        m_keyBindings.put(KeyEvent.VK_S,            TriggerNames.Move_Back.ordinal());
        m_keyBindings.put(KeyEvent.VK_CONTROL,      TriggerNames.MiscAction.ordinal());
        m_keyBindings.put(KeyEvent.VK_2,            TriggerNames.ToggleSteering.ordinal());
        m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.GoSit.ordinal());
        m_keyBindings.put(KeyEvent.VK_ADD,          TriggerNames.Move_Down.ordinal());
        m_keyBindings.put(KeyEvent.VK_SUBTRACT,     TriggerNames.Move_Up.ordinal());
        m_keyBindings.put(KeyEvent.VK_COMMA,        TriggerNames.Reverse.ordinal());
        m_keyBindings.put(KeyEvent.VK_PERIOD,       TriggerNames.NextAction.ordinal());
        m_keyBindings.put(KeyEvent.VK_1,            TriggerNames.GoTo1.ordinal());
        m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.GoTo2.ordinal());
        m_keyBindings.put(KeyEvent.VK_3,            TriggerNames.GoTo3.ordinal());
        m_keyBindings.put(KeyEvent.VK_G,            TriggerNames.SitOnGround.ordinal());
        m_keyBindings.put(KeyEvent.VK_0,            TriggerNames.Smile.ordinal());
        m_keyBindings.put(KeyEvent.VK_9,            TriggerNames.Frown.ordinal());
        m_keyBindings.put(KeyEvent.VK_8,            TriggerNames.Scorn.ordinal());
        m_keyBindings.put(KeyEvent.VK_Q,            TriggerNames.ToggleLeftArm.ordinal());
        m_keyBindings.put(KeyEvent.VK_E,            TriggerNames.ToggleRightArm.ordinal());
        m_keyBindings.put(KeyEvent.VK_P,            TriggerNames.Point.ordinal());
    }
            
    /**
     * This avatar will be selected for input.
     */
    @Override
    public void selectForInput()
    {
        super.selectForInput();
        
        InputScheme scheme = ((JSceneEventProcessor)m_wm.getUserData(JSceneEventProcessor.class)).getInputScheme();
        if (scheme instanceof AvatarControlScheme)
        {
            ((AvatarControlScheme)scheme).setavatar(this);
//            Goal goalPoint = (Goal)m_wm.getUserData(Goal.class);
//            if (goalPoint != null)
//            {
//                ((avatarContext)m_context).getSteering().setGoalPosition(goalPoint.getTransform().getLocalMatrix(false).getTranslation());
//                ((avatarContext)m_context).getSteering().setSittingDirection(goalPoint.getTransform().getLocalMatrix(false).getLocalZ());
//                ((avatarContext)m_context).getSteering().setGoal(goalPoint.getGoal());
//            }
//            
////            if (m_wm.getUserData(JFrame.class) != null)
////                ((DemoBase2)m_wm.getUserData(JFrame.class)).setGUI(m_jscene, m_wm, null, this);
        }
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