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

import com.jme.math.Vector3f;
import imi.character.statemachine.corestates.WalkState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.statemachine.corestates.ActionState;
import imi.character.CharacterController;
import imi.character.networking.DarkstarClient;
import imi.character.steering.GoSit;
import imi.character.steering.GoTo;
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
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.transitions.RunToWalk;
import imi.character.statemachine.corestates.transitions.WalkToRun;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This is a game context concrete e.g.
 * @author Lou Hayt
 */
public class NinjaContext extends GameContext
{
    private Ninja             ninja       = null;
    private NinjaController   controller  = null;
    private NinjaSteeringHelm AI    = new NinjaSteeringHelm("Ninja Steering Helm", this);
    private LocationNode      location    = null;
    /** Animations that are using the ActionState to play out (such as wave, cheer etc) **/
    private ArrayList<ActionInfo> genericAnimations = new ArrayList<ActionInfo>();
    private int genericActionIndex = 0;
    
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
    
    public static enum ActionNames
    {
        Movement_X,
        Movement_Y,
        Movement_Z,
        Action,
    }
    
    @Override
    public void initDefaultActionMap(Hashtable<Integer, Action> actionMap) 
    {
        actionMap.put(TriggerNames.Move_Left.ordinal(),     new Action(NinjaContext.ActionNames.Movement_X.ordinal(), -0.4f));
        actionMap.put(TriggerNames.Move_Right.ordinal(),    new Action(NinjaContext.ActionNames.Movement_X.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Forward.ordinal(),  new Action(NinjaContext.ActionNames.Movement_Z.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Back.ordinal(),     new Action(NinjaContext.ActionNames.Movement_Z.ordinal(), -0.4f));
        actionMap.put(TriggerNames.MiscAction.ordinal(),         new Action(NinjaContext.ActionNames.Action.ordinal(), 1.0f));
        actionMap.put(TriggerNames.Move_Up.ordinal(),       new Action(NinjaContext.ActionNames.Movement_Y.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Down.ordinal(),     new Action(NinjaContext.ActionNames.Movement_Y.ordinal(), -0.4f));
    }
         
    public NinjaContext(Ninja master)
    {
        super(master);
        ninja = master;
        controller = (NinjaController) instantiateController();
        actions    = new float [ActionNames.values().length];
                
        // Add states to this context
        gameStates.put(IdleState.class,  new IdleState(this));
        gameStates.put(WalkState.class,  new WalkState(this));
        gameStates.put(TurnState.class,  new TurnState(this));
        gameStates.put(ActionState.class, new ActionState(this));
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
        RegisterStateEntryPoint(gameStates.get(ActionState.class), "toPunch");
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
        gameStates.get(ActionState.class).addTransition(new ActionToWalk());
        gameStates.get(ActionState.class).addTransition(new ActionToTurn());
        gameStates.get(ActionState.class).addTransition(new ActionToIdle());
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
            genericAnimations.get(0).apply((ActionState) gameStates.get(ActionState.class));
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        AI.update(deltaTime);
        controller.update(deltaTime);
    }
        
    @Override
    protected void triggerAlert(int trigger, boolean pressed)
    {
        // Toggle automatic steering behavior towards the current goal
        if (trigger == TriggerNames.ToggleSteering.ordinal() && pressed)
            AI.toggleEnable();
        
        // Toggle manual control over the right arm
        if (trigger == TriggerNames.ToggleRightArm.ordinal() && pressed)
        {
            ninja.setCameraOnMe();
            ninja.getRightArm().toggleEnabled();
        }
        // Toggle manual control over the left arm
        if (trigger == TriggerNames.ToggleLeftArm.ordinal() && pressed)
        {
            ninja.setCameraOnMe();
            ninja.getLeftArm().toggleEnabled();
        }
        
        // Toggle manual control mode over the left arm
        if (trigger == TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal() && pressed)
        {
            ninja.getLeftArm().toggleManualDriveReachUp();
        }
        // Toggle manual control mode over the right arm
        if (trigger == TriggerNames.ToggleRightArmManualDriveReachMode.ordinal() && pressed)
        {
            ninja.getRightArm().toggleManualDriveReachUp();
        }
    
        // Point at the nearest chair
        if (trigger == TriggerNames.Point.ordinal() && pressed)
        {
            if (ninja.getObjectCollection() == null || ninja.getRightArm() == null)
                return;
            
            SpatialObject obj = ninja.getObjectCollection().findNearestChair(ninja, 10000.0f, 1.0f, true);
            if (obj == null)
                return;
            
            ninja.getRightArm().setPointAtLocation(obj.getPosition());
        }
        
        // Find nearest chair and sit on it
        else if (trigger == TriggerNames.GoSit.ordinal() && pressed)
        {
            AI.clearTasks();
            GoToNearestChair();
        }
        
        // GoTo to location - if path is available from the current location
        else if (trigger == TriggerNames.GoTo1.ordinal() && pressed)
        {
            if (ninja.getUpdateExtension() != null)
            {
                Vector3f dir = ninja.getPosition().add(0.0f, 1.8f, 0.0f).subtract(Vector3f.ZERO).normalize();
                ((DarkstarClient)ninja.getUpdateExtension()).pitchBall(Vector3f.ZERO, dir.mult(0.1f));
             //   ((DarkstarClient)ninja.getUpdateExtension()).pitchBall(controller.getPosition().add(new Vector3f(-5.0f, 1.8f, 0.0f)), new Vector3f(0.1f, 0.0f, 0.0f));
            }
           //ninja.getObjectCollection().testLightToggle(); // test
           
           // System.out.println("fix: " + ninja.getPosition());
            
//            AI.clearTasks();
//            GoToNearestLocation();
//            if (location != null)
//                AI.addTaskToBottom(new FollowPath("yellowRoom", location, this));
        }
        else if (trigger == TriggerNames.GoTo2.ordinal() && pressed)
        {
            if (ninja.getUpdateExtension() != null)
            {
                ((DarkstarClient)ninja.getUpdateExtension()).getServerProxy().startGame(3);
            }
            
//            AI.clearTasks();
//            GoToNearestLocation();
//            if (location != null)
//                AI.addTaskToBottom(new FollowPath("lobbyCenter", location, this));
        }
        else if (trigger == TriggerNames.GoTo3.ordinal() && pressed)
        {
            AI.clearTasks();
            GoToNearestChair();
        }
        
        else if (trigger == TriggerNames.Smile.ordinal() && pressed)
        {
            if (ninja.getFacialAnimationQ() != null)
            {
                if (ninja.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                    ninja.initiateFacialAnimation(1, 1.0f, 1.0f);
            }
        }   
        
        else if (trigger == TriggerNames.Frown.ordinal() && pressed)
        {
            if (ninja.getFacialAnimationQ() != null)
            {
                if (ninja.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                    ninja.initiateFacialAnimation(2, 3.0f, 1.5f);
            }
        }   
        
        else if (trigger == TriggerNames.Scorn.ordinal() && pressed)
        {
            if (ninja.getFacialAnimationQ() != null)
            {
                if (ninja.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                    ninja.initiateFacialAnimation(3, 3.0f, 1.5f);
            }
        }   
                
        // Reverse the animation for the punch state
        else if (trigger == TriggerNames.Reverse.ordinal() && pressed)
        {
            ActionState punch = (ActionState) gameStates.get(ActionState.class);
            punch.setReverseAnimation(!punch.isReverseAnimation());
        }
        
        // Select the next animation to play for the facial animation test
        else if (trigger == TriggerNames.NextAction.ordinal() && pressed)
        {
            ActionState action = (ActionState) gameStates.get(ActionState.class);
            action.setAnimationSetBoolean(false);
            
            genericActionIndex++;
            if (genericActionIndex >= genericAnimations.size())
                genericActionIndex = 0;
            
            genericAnimations.get(genericActionIndex).apply(action);
//            switch (genericActionIndex)
//            {
//                case 0:
//                    action.setAnimationName("Male_Wave");
//                    break;
//                case 1:
//                    action.setAnimationName("Male_Laugh");
//                    break;
//                case 2:
//                    action.setAnimationName("Male_Cheer");
//                    break;
//                case 3:
//                    action.setAnimationName("Male_Clap");
//                    break;
//                case 4:
//                    action.setAnimationName("Male_Bow");
//                    break;
//                case 5:
//                    action.setAnimationName("Male_Follow");
//                    break;
//                case 6:
//                    punch.setAnimationName("Male_Jump");
//                    break;
//                case 7:
//                    punch.setAnimationName("Male_Idle");
//                    break;
//                case 8:
//                    punch.setAnimationName("Male_Run");
//                    break;
//                case 9:
//                    punch.setAnimationName("Male_Walk");
//                    break;
//                case 10:
//                    punch.setAnimationName("Male_StandToSit");
//                    break;
//                case 11:
//                    punch.setAnimationName("Male_Sitting");
//                    break;
//                case 12:
//                    punch.setAnimationName("Male_FallFromSitting");
//                    break;
//                case 13:
//                    punch.setAnimationName("Male_FloorSitting");
//                    break;
//                case 14:
//                    punch.setAnimationName("Male_FloorGetup");
//                    break;
//            }
        }
    }
    
    public Ninja getNinja() {
        return ninja;
    }
    
    public void setNinja(Ninja ninja) {
        this.ninja = ninja;
    }
    
    @Override
    public NinjaController getController() {
        return controller;
    }

    @Override
    protected CharacterController instantiateController() {
        return new NinjaController(ninja);
    }

    @Override
    public NinjaSteeringHelm getSteering() {
        return AI;
    }

    public void GoToNearestLocation() 
    {   
        if (ninja.getObjectCollection() == null)
            return;
        
        location = ninja.getObjectCollection().findNearestLocation(ninja, 10000.0f, 1.0f, false);
        if (location != null)
        {
            AI.addTaskToTop(new GoTo(location, this));
            
            ///steering.addTaskToTop(new )
//            Vector3f pos = location.getPosition();
//            Vector3f direction = location.getForwardVector();
//            steering.setGoalPosition(pos);
//            steering.setSittingDirection(direction);
//            steering.setGoal(location);
//            // GoTo and sit there
            AI.setEnable(true);
//            steering.setReachedGoal(false);
//
//            // Update global goal point
//            Goal goalPoint = (Goal) ninja.getWorldManager().getUserData(Goal.class);
//            if (goalPoint != null)
//            {
//                goalPoint.setGoal(location);
//                PMatrix goal = new PMatrix(pos); 
//                goal.lookAt(pos, pos.add(direction), Vector3f.UNIT_Y);
//                goal.invert();
//                goalPoint.getTransform().setLocalMatrix(goal);
//                goalPoint.getTransform().getLocalMatrix(true).setScale(1.0f);
//                PScene GPScene = goalPoint.getPScene();
//                GPScene.setDirty(true, true);
//                GPScene.submitTransforms();
//            }
        }
    }
        
    public boolean GoToNearestChair()
    {
        if (ninja.getObjectCollection() == null)
            return false;

        SpatialObject obj = ninja.getObjectCollection().findNearestChair(ninja, 10000.0f, 1.0f, true);
        if (obj != null && !((Chair)obj).isOccupied())
        {
            //Vector3f pos = ((Chair)obj).getGoalPosition();
            //Vector3f direction = ((Chair)obj).getGoalForwardVector();
            
            GoSit task = new GoSit((Chair)obj, this);
            AI.addTaskToTop(task);
            
//            steering.setGoalPosition(pos);
//            steering.setSittingDirection(direction);
//            steering.setGoal(obj);
            
            // GoTo and sit there
            AI.setEnable(true);
            //steering.setReachedGoal(false);

//            // Update global goal point
//            Goal goalPoint = (Goal) ninja.getWorldManager().getUserData(Goal.class);
//            if (goalPoint != null)
//            {
//                goalPoint.setGoal(obj);
//                PMatrix goal = new PMatrix(pos); 
//                goal.lookAt(pos, pos.add(direction), Vector3f.UNIT_Y);
//                goal.invert();
//                goalPoint.getTransform().setLocalMatrix(goal);
//                goalPoint.getTransform().getLocalMatrix(true).setScale(1.0f);
//                PScene GPScene = goalPoint.getPScene();
//                GPScene.setDirty(true, true);
//                GPScene.submitTransforms();
//            }
            
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

    public void performAction(int actionInfoIndex)
    {
        ActionState action = (ActionState) gameStates.get(ActionState.class);
        action.setAnimationSetBoolean(false);
        genericAnimations.get(actionInfoIndex).apply(action);
        setCurrentState(action);
    }
    
    /** Here we define the animation properties for the various animations
     that are using the ActionState to play out **/
    private void configureDefaultActionStateInfo() {
        
        ActionInfo info;
        
        /** Note: There are many more settings possible to set! **/
        
        info = new ActionInfo("Male_Wave", "MaleSmile", 1.0f, 2.0f);
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_No");
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Yes");
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Cell");
        info.setRepeat(true);
        info.setRepeatWillOscilate(true);
        info.setTransitionDuration(0.5f);
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Laugh", "MaleSmile", 0.5f, 2.5f);
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Cheer", "MaleSmile", 0.5f, 3.0f);
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Clap");
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Bow");
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_Follow");
        genericAnimations.add(info);
        
        info = new ActionInfo("Male_TakeDamage");
        genericAnimations.add(info);
    }

    
}
