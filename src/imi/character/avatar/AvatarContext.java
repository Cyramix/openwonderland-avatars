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
import imi.character.behavior.CharacterBehaviorManager;
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
import imi.objects.ChairObject;
import imi.objects.LocationNode;
import imi.objects.SpatialObject;
import imi.objects.TargetObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState.Action;
import imi.character.statemachine.corestates.ActionInfo;
import imi.character.statemachine.corestates.ActionState;
import imi.character.statemachine.corestates.CycleActionInfo;
import imi.character.statemachine.corestates.CycleActionState;
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.StrafeState;
import imi.character.statemachine.corestates.transitions.ActionToStrafe;
import imi.character.statemachine.corestates.transitions.IdleToStrafe;
import imi.character.statemachine.corestates.transitions.RunToWalk;
import imi.character.statemachine.corestates.transitions.StrafeToAction;
import imi.character.statemachine.corestates.transitions.StrafeToIdle;
import imi.character.statemachine.corestates.transitions.TurnToStrafe;
import imi.character.statemachine.corestates.transitions.WalkToFly;
import imi.character.statemachine.corestates.transitions.WalkToRun;
import imi.character.behavior.FollowBakedPath;
import imi.character.behavior.GoSit;
import imi.character.behavior.GoTo;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javolution.util.FastList;
import javolution.util.FastTable;

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
    private CharacterBehaviorManager behavior    = new CharacterBehaviorManager("Avatar Behavior", this);
    /** Current location node, if any. **/
    private LocationNode      location    = null;
    /** Animations that are using the ActionState to play out (such as wave, cheer etc) **/
    private FastTable<ActionInfo> genericAnimations = new FastTable<ActionInfo>();
    /** Used to queue up several generic animations **/
    private FastList<Integer> genericAnimationsQueue = new FastList<Integer>();
    /** Used for cycling through action animations **/
    private int genericActionIndex = 0;
    /** Map of generic animation API names to indecis  **/
    private HashMap<String, Integer> genericAnimationNames = new HashMap<String, Integer>();

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
        ToggleBehavior,
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
        Move_Strafe_Left,
        Move_Strafe_Right,
    }

    /**
     * The names of the actions.
     */
    public static enum ActionNames
    {
        Movement_Rotate_Y,
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
        gameStates.put(StrafeState.class,  new StrafeState(this));
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
        registerStateEntryPoint(gameStates.get(IdleState.class),  "toIdle");
        registerStateEntryPoint(gameStates.get(WalkState.class),  "toWalk");
        registerStateEntryPoint(gameStates.get(StrafeState.class),  "toSideStep");
        registerStateEntryPoint(gameStates.get(TurnState.class),  "toTurn");
        registerStateEntryPoint(gameStates.get(CycleActionState.class), "toAction");
        registerStateEntryPoint(gameStates.get(FlyState.class),   "toFly");
        registerStateEntryPoint(gameStates.get(SitOnGroundState.class),   "toSitOnGround");
        registerStateEntryPoint(gameStates.get(RunState.class),   "toRun");
                
        // Add transitions (exit points)
        gameStates.get(IdleState.class).addTransition(new IdleToTurn());
        gameStates.get(IdleState.class).addTransition(new IdleToWalk());
        gameStates.get(IdleState.class).addTransition(new IdleToStrafe());
        gameStates.get(IdleState.class).addTransition(new IdleToAction());
        gameStates.get(IdleState.class).addTransition(new IdleToFly());
        gameStates.get(IdleState.class).addTransition(new IdleToSitOnGround());
        gameStates.get(WalkState.class).addTransition(new WalkToIdle());
        gameStates.get(WalkState.class).addTransition(new WalkToAction());
        gameStates.get(WalkState.class).addTransition(new WalkToRun());
        gameStates.get(WalkState.class).addTransition(new WalkToFly());
        gameStates.get(TurnState.class).addTransition(new TurnToIdle());
        gameStates.get(TurnState.class).addTransition(new TurnToWalk());
        gameStates.get(TurnState.class).addTransition(new TurnToAction());
        gameStates.get(TurnState.class).addTransition(new TurnToStrafe());
        gameStates.get(CycleActionState.class).addTransition(new ActionToWalk());
        gameStates.get(CycleActionState.class).addTransition(new ActionToTurn());
        gameStates.get(CycleActionState.class).addTransition(new ActionToIdle());
        gameStates.get(CycleActionState.class).addTransition(new ActionToStrafe());
        gameStates.get(SitState.class).addTransition(new SitToIdle());
        gameStates.get(FlyState.class).addTransition(new FlyToIdle());
        gameStates.get(FallFromSitState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(SitOnGroundState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(RunState.class).addTransition(new RunToWalk());
        gameStates.get(StrafeState.class).addTransition(new StrafeToIdle());
        gameStates.get(StrafeState.class).addTransition(new StrafeToAction());
        
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
        actionMap.put(TriggerNames.Move_Left.ordinal(),     new Action(AvatarContext.ActionNames.Movement_Rotate_Y.ordinal(), -0.2f));
        actionMap.put(TriggerNames.Move_Right.ordinal(),    new Action(AvatarContext.ActionNames.Movement_Rotate_Y.ordinal(), 0.2f));
        actionMap.put(TriggerNames.Move_Strafe_Left.ordinal(),  new Action(AvatarContext.ActionNames.Movement_X.ordinal(), -0.4f));
        actionMap.put(TriggerNames.Move_Strafe_Right.ordinal(), new Action(AvatarContext.ActionNames.Movement_X.ordinal(), 0.4f));
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
        behavior.update(deltaTime);
        controller.update(deltaTime);
        if ( !(currentState instanceof CycleActionState) && !genericAnimationsQueue.isEmpty() )
        {
            performAction(genericAnimationsQueue.removeFirst());
            System.out.println("[AvatarContext.update()] pop from queue");
        }
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
        if (trigger == TriggerNames.ToggleBehavior.ordinal() && pressed)
            behavior.toggleEnable();
        
        // Toggle manual control over the right arm
        if (trigger == TriggerNames.ToggleRightArm.ordinal() && pressed)
        {
            avatar.setCameraOnMe();
            if (avatar.getRightArm() != null)
                avatar.getRightArm().toggleEnabled();
        }
        // Toggle manual control over the left arm
        if (trigger == TriggerNames.ToggleLeftArm.ordinal() && pressed)
        {
            avatar.setCameraOnMe();
            if (avatar.getLeftArm() != null)
                avatar.getLeftArm().toggleEnabled();
        }
        
        // Toggle manual control mode over the left arm
        if (trigger == TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal() && pressed)
        {
            if (avatar.getLeftArm() != null)
                avatar.getLeftArm().toggleManualDriveReachUp();
        }
        // Toggle manual control mode over the right arm
        if (trigger == TriggerNames.ToggleRightArmManualDriveReachMode.ordinal() && pressed)
        {
            if (avatar.getRightArm() != null)
                avatar.getRightArm().toggleManualDriveReachUp();
        }
    
        // Point at the nearest chair
        if (trigger == TriggerNames.Point.ordinal() && pressed)
        {
            if (avatar.getObjectCollection() == null || avatar.getRightArm() == null)
                return;

            SpatialObject obj = avatar.getObjectCollection().findNearestObjectOfType(ChairObject.class, avatar, 10000.0f, 1.0f, true);
            if (obj == null)
                return;

            avatar.getRightArm().setPointAtLocation(obj.getPositionRef());
        }
        
        // Find nearest chair and sit on it
        else if (trigger == TriggerNames.GoSit.ordinal() && pressed)
        {
//            AI.clearTasks();
//            goToNearestChair();
//
//            // Safely attempt to start a multiplayer game
//            if (avatar.getUpdateExtension() != null)
//            {
//                Updatable up = avatar.getUpdateExtension();
//                if (up instanceof Client && ((Client)up).getExtension(CahuaClientExtention.class) != null)
//                {
//                    CahuaClientExtention cahua = (CahuaClientExtention)((Client)up).getExtension(CahuaClientExtention.class);
//                    cahua.startGame(3);
//                }
//            }
        }
        
        // GoTo to location - if path is available from the current location
        else if (trigger == TriggerNames.GoTo1.ordinal() && pressed)
        {
           // System.out.println("fix: " + avatar.getPosition());
            
            behavior.clearTasks();
            goToNearestLocation();
            if (location != null)
                behavior.addTaskToBottom(new FollowBakedPath("yellowRoom", location, this));
        }
        else if (trigger == TriggerNames.GoTo2.ordinal() && pressed)
        {
            behavior.clearTasks();
            goToNearestLocation();
            if (location != null)
                behavior.addTaskToBottom(new FollowBakedPath("lobbyCenter", location, this));
        }
        else if (trigger == TriggerNames.GoTo3.ordinal() && pressed)
        {
            behavior.clearTasks();
            goToNearestChair();
        }

        // Facial expressions
        else if (trigger == TriggerNames.Smile.ordinal() && pressed)
            avatar.initiateFacialAnimation(1, 0.2f, 1.0f);
        else if (trigger == TriggerNames.Frown.ordinal() && pressed)
            avatar.initiateFacialAnimation(2, 0.2f, 1.5f);
        else if (trigger == TriggerNames.Scorn.ordinal() && pressed)
            avatar.initiateFacialAnimation(3, 0.2f, 1.5f);
                
        // Reverse the animation for the punch state (for testing)
        else if (trigger == TriggerNames.Reverse.ordinal() && pressed)
        {
            CycleActionState punch = (CycleActionState) gameStates.get(CycleActionState.class);
            punch.setTransitionReverseAnimation(!punch.isTransitionReverseAnimation());
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
    public CharacterBehaviorManager getBehaviorManager() {
        return behavior;
    }

    /**
     * Find the nearest location node and direct the avatar to go there.
     * @return The nearest location node, or null if none are found
     */
    public LocationNode goToNearestLocation()
    {   
        if (avatar.getObjectCollection() == null)
            return null;
        
        location = (LocationNode)avatar.getObjectCollection().findNearestObjectOfType(LocationNode.class, avatar, 10000.0f, 1.0f, false);
        if (location != null)
        {
            behavior.addTaskToTop(new GoTo(location.getPositionRef(), this));
            behavior.setEnable(true);
        }
        return location;
    }

    /**
     * Find the nearest unoccupied chair and direct the avatar to go to it.
     * @return True if an unoccupied chair was found, false otherwise
     */
    public boolean goToNearestChair()
    {
        if (avatar.getObjectCollection() == null)
            return false;

        SpatialObject obj = avatar.getObjectCollection().findNearestObjectOfType(ChairObject.class, avatar, 10000.0f, 1.0f, true);
        if (obj != null && !((TargetObject)obj).isOccupied())
        {
            GoSit task = new GoSit((TargetObject)obj, this);
            behavior.addTaskToTop(task);
            behavior.setEnable(true);
            return true;
        }
        return false;
    }

    public boolean goToTarget(TargetObject target, boolean occupiedMatters, boolean abandonCurrentTasks)
    {
        if (abandonCurrentTasks)
            behavior.clearTasks();

        if (avatar.getObjectCollection() == null)
            return false;

        if (target != null && !target.isOccupied(occupiedMatters))
        {
            GoTo task = new GoTo(target, this);
            behavior.addTaskToTop(task);
            behavior.setEnable(true);
            return true;
        }
        return false;
    }

    public boolean goSitOnChair(TargetObject chair, boolean occupiedMatters, boolean abandonCurrentTasks)
    {
        if (abandonCurrentTasks)
            behavior.clearTasks();

        if (avatar.getObjectCollection() == null)
            return false;

        if (chair != null && !chair.isOccupied(occupiedMatters))
        {
            GoSit task = new GoSit(chair, this);
            behavior.addTaskToTop(task);
            behavior.setEnable(true);
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
        if (currentState instanceof CycleActionState)
        {
            // que it up
            genericAnimationsQueue.add(actionInfoIndex);
            System.out.println("queu up action " + actionInfoIndex);
        }
        else
        {
            System.out.println("perform action " + actionInfoIndex);
            CycleActionState action = (CycleActionState) gameStates.get(CycleActionState.class);
            genericAnimations.get(actionInfoIndex).apply(action);
            setCurrentState(action);
        }
    }
    
    public FastTable<ActionInfo> getGenericAnimations() {
        return genericAnimations;
    }
    
    /**
     * Get the index for performAction() by name
     * @param name
     * @return
     */
    public int getGenericAnimationIndex(String name) {
        Integer inte = genericAnimationNames.get(name);
        if (inte == null)
            return -1;
        return inte.intValue();
    }
    
    /** Here we define the animation properties for the various animations
     that are using the ActionState to play out **/
    private void configureDefaultActionStateInfo() 
    {    
        ActionInfo info;
        CycleActionInfo cycleInfo;
        
        /** Note: There are many more settings possible to set! **/
        int index = 0;
        if (avatar.getCharacterParams().isMale())
        {
            info = new ActionInfo("Male_Wave", "1", 0.3f, 2.0f);
            genericAnimations.add(info);
            genericAnimationNames.put("Wave", index++);

            cycleInfo = new CycleActionInfo("Male_AnswerCell", "Male_Cell", "Male_AnswerCell", "1", 0.5f, 2.0f);
            cycleInfo.setExitAnimationReverse(true);
            cycleInfo.setTransitionDuration(0.1f);
            cycleInfo.setCycleTransitionDuration(0.2f);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("AnswerCell", index++);

            cycleInfo = new CycleActionInfo("Male_RaiseHand", "Male_RaiseHandIdle", "Male_RaiseHand");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("RaiseHand", index++);
            
            info = new ActionInfo("Male_No");
            genericAnimations.add(info);
            genericAnimationNames.put("No", index++);

            info = new ActionInfo("Male_Yes");
            genericAnimations.add(info);
            genericAnimationNames.put("Yes", index++);

            info = new ActionInfo("Male_Laugh", "1", 0.3f, 2.5f);
            genericAnimations.add(info);
            genericAnimationNames.put("Laugh", index++);

            info = new ActionInfo("Male_Cheer", "1", 0.3f, 3.0f);
            genericAnimations.add(info);
            genericAnimationNames.put("Cheer", index++);

            info = new ActionInfo("Male_Clap");
            genericAnimations.add(info);
            genericAnimationNames.put("Clap", index++);

            info = new ActionInfo("Male_Bow");
            genericAnimations.add(info);
            genericAnimationNames.put("Bow", index++);

            info = new ActionInfo("Male_Follow");
            genericAnimations.add(info);
            genericAnimationNames.put("Follow", index++);

            info = new ActionInfo("Male_TakeDamage");
            genericAnimations.add(info);
            genericAnimationNames.put("TakeDamage", index++);

            info = new ActionInfo("Male_PublicSpeaking");
            genericAnimations.add(info);
            genericAnimationNames.put("PublicSpeaking", index++);

            info = new ActionInfo("Male_ShakeHands");
            genericAnimations.add(info);
            genericAnimationNames.put("ShakeHands", index++);
        }
        else // female
        {
            info = new ActionInfo("Female_Wave", "1", 0.3f, 2.0f);
            genericAnimations.add(info);
            genericAnimationNames.put("Wave", index++);

            cycleInfo = new CycleActionInfo("Female_AnswerCell", "Female_Cell", "Female_AnswerCell", "1", 0.5f, 2.0f);
            cycleInfo.setCycleMode(PlaybackMode.PlayOnce);
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("AnswerCell", index++);

            cycleInfo = new CycleActionInfo("Female_RaiseHand", "Female_RaiseHandIdle", "Female_RaiseHand");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("RaiseHand", index++);

            info = new ActionInfo("Female_No");
            genericAnimations.add(info);
            genericAnimationNames.put("No", index++);

            info = new ActionInfo("Female_Yes");
            genericAnimations.add(info);
            genericAnimationNames.put("Yes", index++);

            info = new ActionInfo("Female_Laugh", "1", 0.3f, 2.5f);
            genericAnimations.add(info);
            genericAnimationNames.put("Laugh", index++);

            info = new ActionInfo("Female_Cheer", "1", 0.3f, 3.0f);
            genericAnimations.add(info);
            genericAnimationNames.put("Cheer", index++);

            info = new ActionInfo("Female_Clap");
            genericAnimations.add(info);
            genericAnimationNames.put("Clap", index++);

            info = new ActionInfo("Female_Bow");
            genericAnimations.add(info);
            genericAnimationNames.put("Bow", index++);

            info = new ActionInfo("Female_Follow");
            genericAnimations.add(info);
            genericAnimationNames.put("Follow", index++);
            
            info = new ActionInfo("Female_PublicSpeaking");
            genericAnimations.add(info);
            genericAnimationNames.put("PublicSpeaking", index++);
        }
    }

    
}
