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

import imi.character.statemachine.corestates.WalkState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.CharacterController;
import imi.character.networking.CahuaClientExtention;
import imi.character.networking.CharacterClient;
import imi.character.statemachine.corestates.transitions.FlyToIdle;
import imi.character.statemachine.corestates.transitions.IdleToFly;
import imi.character.statemachine.corestates.transitions.IdleToAction;
import imi.character.statemachine.corestates.transitions.IdleToSitOnGround;
import imi.character.statemachine.corestates.transitions.IdleToTurn;
import imi.character.statemachine.corestates.transitions.IdleToWalk;
import imi.character.statemachine.corestates.transitions.ActionToIdle;
import imi.character.statemachine.corestates.transitions.ActionToTurn;
import imi.character.statemachine.corestates.transitions.ActionToWalk;
import imi.character.statemachine.corestates.transitions.SitOnGroundToIdle;
import imi.character.statemachine.corestates.transitions.SitToIdle;
import imi.character.statemachine.corestates.transitions.TurnToIdle;
import imi.character.statemachine.corestates.transitions.TurnToAction;
import imi.character.statemachine.corestates.transitions.TurnToWalk;
import imi.character.statemachine.corestates.transitions.WalkToIdle;
import imi.character.statemachine.corestates.transitions.WalkToAction;
import imi.character.objects.Chair;
import imi.character.objects.LocationNode;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState.Action;
import imi.character.statemachine.corestates.ActionInfo;
import imi.character.statemachine.corestates.ActionState;
import imi.character.statemachine.corestates.CycleActionInfo;
import imi.character.statemachine.corestates.CycleActionState;
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.transitions.RunToWalk;
import imi.character.statemachine.corestates.transitions.WalkToRun;
import imi.character.steering.FollowBakedPath;
import imi.character.steering.GoSit;
import imi.character.steering.GoTo;
import imi.scene.Updatable;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This is a concrete GameContext.
 * @author Lou Hayt
 */
public class AvatarContext extends GameContext
{
    /** The relevant avatar **/
    private Avatar             avatar       = null;
    /** Controller for the above **/
    private AvatarController   controller  = null;
    /** The steering controller for the avatar **/
    private AvatarSteeringHelm AI    = new AvatarSteeringHelm("avatar Steering Helm", this);
    /** Current location node, if any. **/
    private LocationNode      location    = null;
    /** Animations that are using the ActionState to play out (such as wave, cheer etc) **/
    private ArrayList<ActionInfo> genericAnimations = new ArrayList<ActionInfo>();
    /** Used for cycling through action animations **/
    private int genericActionIndex = 0;

    /**
     * The names of the triggers.
     */
    public static enum TriggerNames
    {
        Movement_Modifier,
        Move_Left, 
        Move_Right,
        Move_Forward,
        Move_Back,
        Move_Up,
        Move_Down,
        MiscAction,
        SitOnGround,
        ToggleSteering,
        ToggleRightArm,
        ToggleLeftArm,
        ToggleRightArmManualDriveReachMode,
        ToggleLeftArmManualDriveReachMode,
        Point,
        GoSit,
        GoTo1,
        GoTo2,
        GoTo3,
        NextAction,
        Reverse,
        Smile,
        Frown,
        Scorn,
    }

    /**
     * The names of the actions.
     */
    public static enum ActionNames
    {
        Movement_X,
        Movement_Y,
        Movement_Z,
        Action,
    }

    /**
     * Construct a new instance with the provided avatar
     * @param theAvatar
     */
    public AvatarContext(Avatar theAvatar)
    {
        super(theAvatar);
        avatar = theAvatar;
        controller = (AvatarController) instantiateController();
        actions    = new float [ActionNames.values().length];
                
        // Add states to this context
        gameStates.put(IdleState.class,  new IdleState(this));
        gameStates.put(WalkState.class,  new WalkState(this));
        gameStates.put(TurnState.class,  new TurnState(this));
        gameStates.put(CycleActionState.class, new CycleActionState(this));
        gameStates.put(SitState.class,   new SitState(this));
        gameStates.put(FlyState.class,   new FlyState(this));
        gameStates.put(FallFromSitState.class,   new FallFromSitState(this));
        gameStates.put(SitOnGroundState.class,   new SitOnGroundState(this));
        gameStates.put(RunState.class,   new RunState(this));
        
        // Set the state to start with
        setCurrentState(gameStates.get(IdleState.class));
        
        // Register validation methods (entry points)
        RegisterStateEntryPoint(gameStates.get(IdleState.class),  "toIdle");
        RegisterStateEntryPoint(gameStates.get(WalkState.class),  "toWalk");
        RegisterStateEntryPoint(gameStates.get(TurnState.class),  "toTurn");
        RegisterStateEntryPoint(gameStates.get(CycleActionState.class), "toAction");
        RegisterStateEntryPoint(gameStates.get(FlyState.class),   "toFly");
        RegisterStateEntryPoint(gameStates.get(SitOnGroundState.class),   "toSitOnGround");
        RegisterStateEntryPoint(gameStates.get(RunState.class),   "toRun");
                
        // Add transitions (exit points)
        gameStates.get(IdleState.class).addTransition(new IdleToTurn());
        gameStates.get(IdleState.class).addTransition(new IdleToWalk());
        gameStates.get(IdleState.class).addTransition(new IdleToAction());
        gameStates.get(WalkState.class).addTransition(new WalkToIdle());
        gameStates.get(WalkState.class).addTransition(new WalkToAction());
        gameStates.get(WalkState.class).addTransition(new WalkToRun());
        gameStates.get(TurnState.class).addTransition(new TurnToIdle());
        gameStates.get(TurnState.class).addTransition(new TurnToWalk());
        gameStates.get(TurnState.class).addTransition(new TurnToAction());
        gameStates.get(CycleActionState.class).addTransition(new ActionToWalk());
        gameStates.get(CycleActionState.class).addTransition(new ActionToTurn());
        gameStates.get(CycleActionState.class).addTransition(new ActionToIdle());
        gameStates.get(SitState.class).addTransition(new SitToIdle());
        gameStates.get(FlyState.class).addTransition(new FlyToIdle());
        gameStates.get(IdleState.class).addTransition(new IdleToFly());
        gameStates.get(IdleState.class).addTransition(new IdleToSitOnGround());
        gameStates.get(FallFromSitState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(SitOnGroundState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(RunState.class).addTransition(new RunToWalk());
        
        // Set default info for animations utilizing the ActionState
        configureDefaultActionStateInfo();
        if (!genericAnimations.isEmpty())
            genericAnimations.get(0).apply((CycleActionState) gameStates.get(CycleActionState.class));
    }


    /**
     * Performs the default mapping of actions to triggers
     * @param actionMap
     */
    @Override
    public void initDefaultActionMap(Hashtable<Integer, Action> actionMap)
    {
        actionMap.put(TriggerNames.Move_Left.ordinal(),     new Action(AvatarContext.ActionNames.Movement_X.ordinal(), -0.4f));
        actionMap.put(TriggerNames.Move_Right.ordinal(),    new Action(AvatarContext.ActionNames.Movement_X.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Forward.ordinal(),  new Action(AvatarContext.ActionNames.Movement_Z.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Back.ordinal(),     new Action(AvatarContext.ActionNames.Movement_Z.ordinal(), -0.4f));
        actionMap.put(TriggerNames.MiscAction.ordinal(),         new Action(AvatarContext.ActionNames.Action.ordinal(), 1.0f));
        actionMap.put(TriggerNames.Move_Up.ordinal(),       new Action(AvatarContext.ActionNames.Movement_Y.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Down.ordinal(),     new Action(AvatarContext.ActionNames.Movement_Y.ordinal(), -0.4f));
    }

    /**
     * Update the context
     * @param deltaTime The timestep
     */
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        AI.update(deltaTime);
        controller.update(deltaTime);
    }

    /**
     * Received when the state of a trigger changes
     * @param trigger
     * @param pressed
     */
    @Override
    protected void triggerAlert(int trigger, boolean pressed)
    {
        // Force the action if the action button is pressed
        if (trigger == TriggerNames.MiscAction.ordinal() && pressed)
            setCurrentState((ActionState) gameStates.get(CycleActionState.class));
        
        // Toggle automatic steering behavior towards the current goal
        if (trigger == TriggerNames.ToggleSteering.ordinal() && pressed)
            AI.toggleEnable();
        
        // Toggle manual control over the right arm
        if (trigger == TriggerNames.ToggleRightArm.ordinal() && pressed)
        {
            avatar.setCameraOnMe();
            avatar.getRightArm().toggleEnabled();
        }
        // Toggle manual control over the left arm
        if (trigger == TriggerNames.ToggleLeftArm.ordinal() && pressed)
        {
            avatar.setCameraOnMe();
            avatar.getLeftArm().toggleEnabled();
        }
        
        // Toggle manual control mode over the left arm
        if (trigger == TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal() && pressed)
        {
            avatar.getLeftArm().toggleManualDriveReachUp();
        }
        // Toggle manual control mode over the right arm
        if (trigger == TriggerNames.ToggleRightArmManualDriveReachMode.ordinal() && pressed)
        {
            avatar.getRightArm().toggleManualDriveReachUp();
        }
    
        // Point at the nearest chair
        if (trigger == TriggerNames.Point.ordinal() && pressed)
        {
            if (avatar.getObjectCollection() == null || avatar.getRightArm() == null)
                return;
            
            SpatialObject obj = avatar.getObjectCollection().findNearestChair(avatar, 10000.0f, 1.0f, true);
            if (obj == null)
                return;
            
            avatar.getRightArm().setPointAtLocation(obj.getPosition());
        }
        
        // Find nearest chair and sit on it
        else if (trigger == TriggerNames.GoSit.ordinal() && pressed)
        {
            AI.clearTasks();
            GoToNearestChair();
            
            // Safely attempt to start a multiplayer game
            if (avatar.getUpdateExtension() != null)
            {
                Updatable up = avatar.getUpdateExtension();
                if (up instanceof CharacterClient && ((CharacterClient)up).getExtension() != null)
                {
                    CharacterClient client = ((CharacterClient)up);
                    if (client.getExtension() instanceof CahuaClientExtention)
                        ((CahuaClientExtention)client.getExtension()).startGame(3);
                }
            }
        }
        
        // GoTo to location - if path is available from the current location
        else if (trigger == TriggerNames.GoTo1.ordinal() && pressed)
        {
           // System.out.println("fix: " + avatar.getPosition());
            
            AI.clearTasks();
            GoToNearestLocation();
            if (location != null)
                AI.addTaskToBottom(new FollowBakedPath("yellowRoom", location, this));
        }
        else if (trigger == TriggerNames.GoTo2.ordinal() && pressed)
        {
            AI.clearTasks();
            GoToNearestLocation();
            if (location != null)
                AI.addTaskToBottom(new FollowBakedPath("lobbyCenter", location, this));
        }
        else if (trigger == TriggerNames.GoTo3.ordinal() && pressed)
        {
            AI.clearTasks();
            GoToNearestChair();
        }
        
        else if (trigger == TriggerNames.Smile.ordinal() && pressed)
        {
            if (avatar.getFacialAnimationQ() != null)
            {
                if (avatar.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                    avatar.initiateFacialAnimation(0, 1.0f, 1.0f);
            }
        }   
        
        else if (trigger == TriggerNames.Frown.ordinal() && pressed)
        {
            if (avatar.getFacialAnimationQ() != null)
            {
                if (avatar.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                    avatar.initiateFacialAnimation(1, 3.0f, 1.5f);
            }
        }   
        
        else if (trigger == TriggerNames.Scorn.ordinal() && pressed)
        {
            if (avatar.getFacialAnimationQ() != null)
            {
                if (avatar.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                    avatar.initiateFacialAnimation(2, 3.0f, 1.5f);
            }
        }   
                
        // Reverse the animation for the punch state (for testing)
        else if (trigger == TriggerNames.Reverse.ordinal() && pressed)
        {
            CycleActionState punch = (CycleActionState) gameStates.get(CycleActionState.class);
            punch.setReverseAnimation(!punch.isReverseAnimation());
        }
        
        // Select the next animation to play for the facial animation test
        else if (trigger == TriggerNames.NextAction.ordinal() && pressed)
        {
            CycleActionState action = (CycleActionState) gameStates.get(CycleActionState.class);
            action.setAnimationSetBoolean(false);
            
            genericActionIndex++;
            if (genericActionIndex >= genericAnimations.size())
                genericActionIndex = 0;
            
            genericAnimations.get(genericActionIndex).apply(action);
        }
    }
    
    public Avatar getavatar() {
        return avatar;
    }
    
    public void setavatar(Avatar avatar) {
        this.avatar = avatar;
    }
    
    @Override
    public AvatarController getController() {
        return controller;
    }

    @Override
    protected CharacterController instantiateController() {
        return new AvatarController(avatar);
    }

    @Override
    public AvatarSteeringHelm getSteering() {
        return AI;
    }

    /**
     * Find the nearest location node and direct the avatar to go there.
     * @return The nearest location node, or null if none are found
     */
    public LocationNode GoToNearestLocation() 
    {   
        if (avatar.getObjectCollection() == null)
            return null;
        
        location = avatar.getObjectCollection().findNearestLocation(avatar, 10000.0f, 1.0f, false);
        if (location != null)
        {
            AI.addTaskToTop(new GoTo(location.getPosition(), this));
            AI.setEnable(true);
        }
        return location;
    }

    /**
     * Find the nearest unoccupied chair and direct the avatar to go to it.
     * @return True if an unoccupied chair was found, false otherwise
     */
    public boolean GoToNearestChair()
    {
        if (avatar.getObjectCollection() == null)
            return false;

        SpatialObject obj = avatar.getObjectCollection().findNearestChair(avatar, 10000.0f, 1.0f, true);
        if (obj != null && !((Chair)obj).isOccupied())
        {
            GoSit task = new GoSit((Chair)obj, this);
            AI.addTaskToTop(task);
            AI.setEnable(true);
            return true;
        }
        
        return false;
    }

    public LocationNode getLocation() {
        return location;
    }

    public void setLocation(LocationNode location) {
        this.location = location;
    }

    /**
     * Perform the action associated with the provided index.
     * @param actionInfoIndex
     */
    public void performAction(int actionInfoIndex)
    {
        CycleActionState action = (CycleActionState) gameStates.get(CycleActionState.class);
        action.setAnimationSetBoolean(false);
        genericAnimations.get(actionInfoIndex).apply(action);
        setCurrentState(action);
    }
    
    protected Iterable<ActionInfo> getGenericAnimations() {
        return genericAnimations;
    }
    
    /** Here we define the animation properties for the various animations
     that are using the ActionState to play out **/
    private void configureDefaultActionStateInfo() 
    {    
        ActionInfo info;
        CycleActionInfo cycleInfo;
        
        /** Note: There are many more settings possible to set! **/
     
        if (avatar.getAttributes().isMale())
        {
            info = new ActionInfo("Male_Wave", "MaleSmile4Frame", 1.0f, 2.0f);
            genericAnimations.add(info);

            cycleInfo = new CycleActionInfo("Male_AnswerCell", "Male_Cell", "Male_AnswerCell", "MaleSmile4Frame", 0.5f, 2.0f);
            cycleInfo.setExitAnimationReverse(true);
            cycleInfo.setTransitionDuration(0.1f);
            cycleInfo.setCycleTransitionDuration(0.2f);
            genericAnimations.add(cycleInfo);

            cycleInfo = new CycleActionInfo("Male_RaiseHand", "Male_RaiseHandIdle", "Male_RaiseHand");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            
            info = new ActionInfo("Male_No");
            genericAnimations.add(info);

            info = new ActionInfo("Male_Yes");
            genericAnimations.add(info);

            info = new ActionInfo("Male_Laugh", "MaleSmile4Frame", 0.5f, 2.5f);
            genericAnimations.add(info);

            info = new ActionInfo("Male_Cheer", "MaleSmile4Frame", 0.5f, 3.0f);
            genericAnimations.add(info);

            info = new ActionInfo("Male_Clap");
            genericAnimations.add(info);

            info = new ActionInfo("Male_Bow");
            genericAnimations.add(info);

            info = new ActionInfo("Male_Follow");
            genericAnimations.add(info);

            info = new ActionInfo("Male_TakeDamage");
            genericAnimations.add(info);

            info = new ActionInfo("Male_PublicSpeaking");
            genericAnimations.add(info);

            info = new ActionInfo("Male_ShakeHands");
            genericAnimations.add(info);
        }
        else // female
        {
            info = new ActionInfo("Female_Wave", "FemaleC_Smile", 1.0f, 2.0f);
            genericAnimations.add(info);

            cycleInfo = new CycleActionInfo("Female_AnswerCell", "Female_Cell", "Female_AnswerCell", "FemaleC_Smile", 0.5f, 2.0f);
            cycleInfo.setCycleMode(PlaybackMode.PlayOnce);
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);

            cycleInfo = new CycleActionInfo("Female_RaiseHand", "Female_RaiseHandIdle", "Female_RaiseHand");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);

            info = new ActionInfo("Female_No");
            genericAnimations.add(info);

            info = new ActionInfo("Female_Yes");
            genericAnimations.add(info);

            info = new ActionInfo("Female_Laugh", "FemaleC_Smile", 0.5f, 2.5f);
            genericAnimations.add(info);

            info = new ActionInfo("Female_Cheer", "FemaleC_Smile", 0.5f, 3.0f);
            genericAnimations.add(info);

            info = new ActionInfo("Female_Clap");
            genericAnimations.add(info);

            info = new ActionInfo("Female_Bow");
            genericAnimations.add(info);

            info = new ActionInfo("Female_Follow");
            genericAnimations.add(info);
            
            info = new ActionInfo("Female_PublicSpeaking");
            genericAnimations.add(info);
        }
    }

    
}
