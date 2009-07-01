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
import imi.character.CharacterParams;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.CharacterInitializationInterface;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.objects.LocationNode;
import imi.objects.ObjectCollectionBase;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.WalkState;
import imi.character.behavior.FollowBakedPath;
import imi.character.behavior.FollowPath;
import imi.character.behavior.GoTo;
import imi.scene.PMatrix;
import imi.serialization.xml.bindings.xmlCharacter;
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
     * Constructor for the avatar class which can only be called by the Builder
     * class.  The builder class has default paramaters for building an avatar
     * which can be overriden by the user depending on their preference on
     * construction.
     * @param builder   - AvatarBuilder subclass that will construct an avatar
     */
    protected Avatar(AvatarBuilder builder) {
        super(builder);
        if (characterParams.isMale())
            maleContextSetup();
        else
            femaleContextSetup();
    }

////////////////////////////////////////////////////////////////////////////////
// Builder
////////////////////////////////////////////////////////////////////////////////

    public static class AvatarBuilder extends CharacterBuilder {

        public AvatarBuilder(CharacterParams attributeParams, WorldManager worldManager) {
            super(attributeParams, worldManager);
        }

        public AvatarBuilder(URL configurationFile, WorldManager worldManager) {
            super(configurationFile, worldManager);
        }

        @Override
        public AvatarBuilder addEntity(boolean addEntity) {
            this.addEntity = addEntity;
            return this;
        }

        @Override
        public AvatarBuilder xmlCharDom(xmlCharacter xmlCharDom) {
            this.xmlCharDom = xmlCharDom;
            return this;
        }

        @Override
        public AvatarBuilder baseURL(String baseURL) {
            this.baseURL    = baseURL;
            return this;
        }

        @Override
        public AvatarBuilder transform(PMatrix transform) {
            this.transform  = transform;
            return this;
        }

        @Override
        public AvatarBuilder initializer(CharacterInitializationInterface initializer) {
            this.initializer    = initializer;
            return this;
        }

        @Override
        public Avatar build() {
            return new Avatar(this);
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Path Finding
////////////////////////////////////////////////////////////////////////////////

    /**
     * initiate a go to steering task
     * @param pos
     * @param dir - may be null
     */
    public void goTo(Vector3f pos, Vector3f dir) {
        CharacterBehaviorManager steering = m_context.getBehaviorManager();
        steering.clearTasks();
        steering.setEnable(true);
        steering.addTaskToTop(new GoTo(pos, dir, m_context));
    }

    /**
     * Stop in place and clear steering tasks
     */
    public void stop() {
        m_context.getController().stop();
        m_context.getBehaviorManager().clearTasks();
    }

    /**
     * Follow a pre baked path
     * @param pathName
     */
    public void followBakedPath(String pathName) {
        CharacterBehaviorManager steering = m_context.getBehaviorManager();
        steering.clearTasks();
        steering.setEnable(true);
        AvatarContext ac = ((AvatarContext)m_context);
        LocationNode location = ac.goToNearestLocation();
        if (location != null)
            steering.addTaskToBottom(new FollowBakedPath(pathName, location, m_context));
    }
    
    public void findPath(String locationName) {
        CharacterBehaviorManager steering = m_context.getBehaviorManager();
        steering.clearTasks();
        steering.setEnable(true);
        AvatarContext ac = ((AvatarContext)m_context);
        ObjectCollectionBase objs = ac.getavatar().getObjectCollection();
        LocationNode source = (LocationNode)objs.findNearestObjectOfType(LocationNode.class, this, 10000.0f, 1.0f, false);
        if (source != null)
            steering.addTaskToBottom(new FollowPath(objs.findPath(source, locationName), m_context));
    }

////////////////////////////////////////////////////////////////////////////////
// Context Setup
////////////////////////////////////////////////////////////////////////////////
    
    private void maleContextSetup() {
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
    
    private void femaleContextSetup() {
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

    private void commonContextSetup() {
        m_context.getController().setReverseHeading(true);

        // Tweak animation names and speeds

        WalkState walk = (WalkState)m_context.getState(WalkState.class);
        walk.setImpulse(15.0f);
        // RED: Werent actually used in calculations, methods removed until used
//        walk.setWalkSpeedMax(2.5f);
//        walk.setWalkSpeedFactor(1.3f);
        walk.setMinimumTimeBeforeTransition(0.05f);
        walk.setTransitionDuration(0.1f);
        walk.setAnimationSpeed(1.6f);

        RunState run = (RunState)m_context.getState(RunState.class);
        run.setImpulse(15.0f);
        // RED: Werent actually used in calculations, methods removed until used
//        run.setWalkSpeedMax(1.0f);
//        run.setWalkSpeedFactor(1.0f);
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

////////////////////////////////////////////////////////////////////////////////
// Override Methods
////////////////////////////////////////////////////////////////////////////////

    @Override
    protected GameContext instantiateContext() {
        return new AvatarContext(this);
    }

    @Override
    protected void finalizeInitialization(xmlCharacter characterDOM) {
        m_context = instantiateContext();
        super.finalizeInitialization(characterDOM);
    }

    @Override
    protected void initKeyBindings() {
        m_keyBindings.put(KeyEvent.VK_SHIFT,        TriggerNames.Movement_Modifier.ordinal());
        m_keyBindings.put(KeyEvent.VK_A,            TriggerNames.Move_Left.ordinal());
        m_keyBindings.put(KeyEvent.VK_D,            TriggerNames.Move_Right.ordinal());
        m_keyBindings.put(KeyEvent.VK_W,            TriggerNames.Move_Forward.ordinal());
        m_keyBindings.put(KeyEvent.VK_S,            TriggerNames.Move_Back.ordinal());
        m_keyBindings.put(KeyEvent.VK_Q,            TriggerNames.Move_Strafe_Left.ordinal());
        m_keyBindings.put(KeyEvent.VK_E,            TriggerNames.Move_Strafe_Right.ordinal());
        m_keyBindings.put(KeyEvent.VK_CONTROL,      TriggerNames.MiscAction.ordinal());
        m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.ToggleBehavior.ordinal());
        m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.GoSit.ordinal());
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
        m_keyBindings.put(KeyEvent.VK_Z,            TriggerNames.ToggleLeftArm.ordinal());
        m_keyBindings.put(KeyEvent.VK_X,            TriggerNames.ToggleRightArm.ordinal());
        m_keyBindings.put(KeyEvent.VK_F,            TriggerNames.Point.ordinal());
    }

}
