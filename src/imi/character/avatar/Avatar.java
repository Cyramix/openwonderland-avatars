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

import com.jme.math.Vector3f;
import imi.character.CharacterAttributes;
import imi.character.CharacterSteeringHelm;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.objects.LocationNode;
import imi.character.objects.ObjectCollectionBase;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.WalkState;
import imi.character.steering.FollowBakedPath;
import imi.character.steering.FollowPath;
import imi.character.steering.GoTo;
import imi.scene.PMatrix;
import imi.utils.input.InputScheme;
import imi.scene.processors.JSceneEventProcessor;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.utils.input.AvatarControls;
import java.awt.event.KeyEvent;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/*
 * Avatar! This class is a concrete Character e.g.
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
        if (m_attributes.isMale())
            maleContextSetup();
        else
            femaleContextSetup();
    }

    public Avatar(URL configurationFile, WorldManager wm, String baseURL, PMatrix transform)
    {
        super(configurationFile, wm, baseURL, transform);
        if (m_attributes.isMale())
            maleContextSetup();
        else
            femaleContextSetup();
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
        m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.ToggleSteering.ordinal());
        m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.GoSit.ordinal());
        m_keyBindings.put(KeyEvent.VK_ADD,          TriggerNames.Move_Down.ordinal());
        m_keyBindings.put(KeyEvent.VK_SUBTRACT,     TriggerNames.Move_Up.ordinal());
        m_keyBindings.put(KeyEvent.VK_EQUALS,       TriggerNames.Move_Down.ordinal());
        m_keyBindings.put(KeyEvent.VK_MINUS,        TriggerNames.Move_Up.ordinal());
        m_keyBindings.put(KeyEvent.VK_COMMA,        TriggerNames.Reverse.ordinal());
        m_keyBindings.put(KeyEvent.VK_PERIOD,       TriggerNames.NextAction.ordinal());
        m_keyBindings.put(KeyEvent.VK_1,            TriggerNames.GoTo1.ordinal());
        m_keyBindings.put(KeyEvent.VK_2,            TriggerNames.GoTo2.ordinal());
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
     * initiate a go to steering task
     * @param pos
     * @param dir - may be null
     */
    public void goTo(Vector3f pos, Vector3f dir)
    {
        CharacterSteeringHelm steering = m_context.getSteering();
        steering.clearTasks();
        steering.setEnable(true);
        steering.addTaskToTop(new GoTo(pos, dir, m_context));
    }

    /**
     * Follow a pre baked path
     * @param pathName
     */
    public void followBakedPath(String pathName)
    {
        CharacterSteeringHelm steering = m_context.getSteering();
        steering.clearTasks();
        steering.setEnable(true);
        AvatarContext ac = ((AvatarContext)m_context);
        LocationNode location = ac.GoToNearestLocation();
        if (location != null)
            steering.addTaskToBottom(new FollowBakedPath(pathName, location, m_context));
    }
    
    public void findPath(String locationName) 
    {
        CharacterSteeringHelm steering = m_context.getSteering();
        steering.clearTasks();
        steering.setEnable(true);
        AvatarContext ac = ((AvatarContext)m_context);
        ObjectCollectionBase objs = ac.getavatar().getObjectCollection();
        LocationNode source = (LocationNode)objs.findNearestObjectOfType(LocationNode.class, this, 10000.0f, 1.0f, false);
        if (source != null)
            steering.addTaskToBottom(new FollowPath(objs.findPath(source, locationName), m_context));
    }
     
    
    /**
     * Stop in place and clear steering tasks
     */
    public void stop()
    {
        m_context.getController().stop();
        m_context.getSteering().clearTasks();
    }
    
    
    
    /**
     * This avatar will be selected for input.
     */
    @Override
    public void selectForInput()
    {
        super.selectForInput();
        
        InputScheme scheme = ((JSceneEventProcessor)m_wm.getUserData(JSceneEventProcessor.class)).getInputScheme();
        if (scheme instanceof AvatarControls)
            ((AvatarControls)scheme).setAvatar(this);
    }
    
    private void maleContextSetup()
    {
        commonContextSetup();

        FallFromSitState fall = (FallFromSitState)m_context.getState(FallFromSitState.class);
        fall.setAnimationName("Male_FallFromSitting");
        fall.setIdleSittingAnimationName("Male_FloorSitting");
        fall.setGettingUpAnimationName("Male_FloorGetup");
        fall.setFacialAnimationName("MaleFrown");

        SitOnGroundState sitGround = (SitOnGroundState)m_context.getState(SitOnGroundState.class);
        sitGround.setAnimationName("Male_FloorGetup");
        sitGround.setIdleSittingAnimationName("Male_FloorSitting");
        sitGround.setGettingUpAnimationName("Male_FloorGetup");
        
        m_context.getStateMapping().get(IdleState.class).setAnimationName("Male_Idle");
        m_context.getStateMapping().get(TurnState.class).setAnimationName("Male_Rotate");
        m_context.getStateMapping().get(WalkState.class).setAnimationName("Male_Walk");
        m_context.getStateMapping().get(RunState.class).setAnimationName("Male_Run");
        m_context.getStateMapping().get(SitState.class).setAnimationName("Male_StandToSit");
        m_context.getStateMapping().get(FlyState.class).setAnimationName("Male_Cheer");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingAnimationName("Male_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpAnimationName("Male_StandToSit");
    }
    
    private void femaleContextSetup()
    {
        commonContextSetup();

        FallFromSitState fall = (FallFromSitState)m_context.getState(FallFromSitState.class);
        fall.setAnimationName("Female_FallFromSitting");
        fall.setIdleSittingAnimationName("Female_FloorSitting");
        fall.setGettingUpAnimationName("Female_FloorGetup");
        fall.setFacialAnimationName("FemaleC_Frown");

        SitOnGroundState sitGround = (SitOnGroundState)m_context.getState(SitOnGroundState.class);
        sitGround.setAnimationName("Female_FloorGetup");
        sitGround.setIdleSittingAnimationName("Female_FloorSitting");
        sitGround.setGettingUpAnimationName("Female_FloorGetup");


        // Tweak animation names and speeds
        m_context.getController().setReverseHeading(true);
        m_context.getStateMapping().get(IdleState.class).setAnimationName("Female_Idle");
        m_context.getStateMapping().get(TurnState.class).setAnimationName("Female_Rotate");
        m_context.getStateMapping().get(WalkState.class).setAnimationName("Female_Walk");
        m_context.getStateMapping().get(RunState.class).setAnimationName("Female_Run");
        m_context.getStateMapping().get(SitState.class).setAnimationName("Female_StandtoSit");
        m_context.getStateMapping().get(FlyState.class).setAnimationName("Female_Cheer");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setIdleSittingAnimationName("Female_Sitting");
        ((SitState)m_context.getStateMapping().get(SitState.class)).setGettingUpAnimationName("Female_StandtoSit");
    }

    private void commonContextSetup()
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
        turn.setAnimationSpeed(1.0f);
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
        fall.setGettingUpAnimationTime(1.0f);
        fall.setTransitionDuration(0.05f);
        fall.setIdleSittingTransitionDuration(0.5f);
        fall.setGettingUpTransitionDuration(0.1f);
        fall.setAnimationSpeed(2.0f);
        fall.setGettingUpAnimationSpeed(2.0f);
        // Frown when entering the state
        fall.setFacialAnimationTransitionTime(0.75f);
        fall.setFacialAnimationExpressionHoldTime(2.0f);

        SitOnGroundState sitGround = (SitOnGroundState)m_context.getState(SitOnGroundState.class);
        sitGround.setTransitionReverseAnimation(true);
        sitGround.setSittingAnimationTime(0.7f);
        sitGround.setTransitionDuration(1.0f);
        sitGround.setIdleSittingTransitionDuration(0.5f);
        sitGround.setGettingUpTransitionDuration(0.1f);
        sitGround.setAnimationSpeed(1.5f);
        sitGround.setGettingUpAnimationSpeed(2.0f);
        sitGround.setGettingUpAnimationTime(1.0f);
    }
}
