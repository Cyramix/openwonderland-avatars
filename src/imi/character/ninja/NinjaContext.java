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

import imi.character.FollowPath;
import imi.character.GoSit;
import imi.character.GoTo;
import imi.character.ninja.transitions.FlyToIdle;
import imi.character.ninja.transitions.IdleToFly;
import imi.character.ninja.transitions.IdleToPunch;
import imi.character.ninja.transitions.IdleToSitOnGround;
import imi.character.ninja.transitions.IdleToTurn;
import imi.character.ninja.transitions.IdleToWalk;
import imi.character.ninja.transitions.PunchToIdle;
import imi.character.ninja.transitions.PunchToTurn;
import imi.character.ninja.transitions.PunchToWalk;
import imi.character.ninja.transitions.SitOnGroundToIdle;
import imi.character.ninja.transitions.SitToIdle;
import imi.character.ninja.transitions.TurnToIdle;
import imi.character.ninja.transitions.TurnToPunch;
import imi.character.ninja.transitions.TurnToWalk;
import imi.character.ninja.transitions.WalkToIdle;
import imi.character.ninja.transitions.WalkToPunch;
import imi.character.objects.Chair;
import imi.character.objects.LocationNode;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState.Action;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.TransitionCommand;
import java.util.Hashtable;

/**
 *
 * @author Lou Hayt
 */
public class NinjaContext extends GameContext
{
    private Ninja             ninja       = null;
    private NinjaController   controller  = null;
    private NinjaSteeringHelm steering    = new NinjaSteeringHelm("Ninja Steering Helm", this);
    private LocationNode      location    = null;
    
    int punchActionIndex = 0;
    
    public static enum TriggerNames
    {
        Movement_Modifier,
        Move_Left, 
        Move_Right,
        Move_Forward,
        Move_Back,
        Move_Up,
        Move_Down,
        Punch,
        SitOnGround,
        ToggleSteering,
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
        Punch,
    }
    
    @Override
    public void initDefaultActionMap(Hashtable<Integer, Action> actionMap) 
    {
        actionMap.put(TriggerNames.Move_Left.ordinal(),     new Action(NinjaContext.ActionNames.Movement_X.ordinal(), -0.4f));
        actionMap.put(TriggerNames.Move_Right.ordinal(),    new Action(NinjaContext.ActionNames.Movement_X.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Forward.ordinal(),  new Action(NinjaContext.ActionNames.Movement_Z.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Back.ordinal(),     new Action(NinjaContext.ActionNames.Movement_Z.ordinal(), -0.4f));
        actionMap.put(TriggerNames.Punch.ordinal(),         new Action(NinjaContext.ActionNames.Punch.ordinal(), 1.0f));
        actionMap.put(TriggerNames.Move_Up.ordinal(),       new Action(NinjaContext.ActionNames.Movement_Y.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Down.ordinal(),     new Action(NinjaContext.ActionNames.Movement_Y.ordinal(), -0.4f));
    }
         
    public NinjaContext(Ninja master)
    {
        super(master);
        ninja = master;
        controller = new NinjaController(master);
        actions    = new float [ActionNames.values().length];
                
        // Add states to this context
        gameStates.put(IdleState.class,  new IdleState(this));
        gameStates.put(WalkState.class,  new WalkState(this));
        gameStates.put(TurnState.class,  new TurnState(this));
        gameStates.put(PunchState.class, new PunchState(this));
        gameStates.put(SitState.class,   new SitState(this));
        gameStates.put(FlyState.class,   new FlyState(this));
        gameStates.put(FallFromSitState.class,   new FallFromSitState(this));
        gameStates.put(SitOnGroundState.class,   new SitOnGroundState(this));
        
        // Set the state to start with
        setCurrentState(gameStates.get(IdleState.class));
        
        // Register validation methods (entry points)
        RegisterStateEntryPoint(gameStates.get(IdleState.class),  "toIdle");
        RegisterStateEntryPoint(gameStates.get(WalkState.class),  "toWalk");
        RegisterStateEntryPoint(gameStates.get(TurnState.class),  "toTurn");
        RegisterStateEntryPoint(gameStates.get(PunchState.class), "toPunch");
        RegisterStateEntryPoint(gameStates.get(FlyState.class),   "toFly");
        RegisterStateEntryPoint(gameStates.get(SitOnGroundState.class),   "toSitOnGround");
                
        // Add transitions (exit points)
        gameStates.get(IdleState.class).addTransition(new IdleToTurn());
        gameStates.get(IdleState.class).addTransition(new IdleToWalk());
        gameStates.get(IdleState.class).addTransition(new IdleToPunch());
        gameStates.get(WalkState.class).addTransition(new WalkToIdle());
        gameStates.get(WalkState.class).addTransition(new WalkToPunch());
        gameStates.get(TurnState.class).addTransition(new TurnToIdle());
        gameStates.get(TurnState.class).addTransition(new TurnToWalk());
        gameStates.get(TurnState.class).addTransition(new TurnToPunch());
        gameStates.get(PunchState.class).addTransition(new PunchToWalk());
        gameStates.get(PunchState.class).addTransition(new PunchToTurn());
        gameStates.get(PunchState.class).addTransition(new PunchToIdle());
        gameStates.get(SitState.class).addTransition(new SitToIdle());
        gameStates.get(FlyState.class).addTransition(new FlyToIdle());
        gameStates.get(IdleState.class).addTransition(new IdleToFly());
        gameStates.get(IdleState.class).addTransition(new IdleToSitOnGround());
        gameStates.get(FallFromSitState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(SitOnGroundState.class).addTransition(new SitOnGroundToIdle());
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        steering.update(deltaTime);
        controller.update(deltaTime);
    }
        
    @Override
    protected void triggerAlert(int trigger, boolean pressed)
    {
        // Toggle steering behavior towards the current goal
        if (trigger == TriggerNames.ToggleSteering.ordinal() && pressed)
            steering.toggleEnable();
    
        // Find nearest chair and sit on it
        else if (trigger == TriggerNames.GoSit.ordinal() && pressed)
        {
            steering.clearTasks();
            GoToNearestChair();
        }
        
        // GoTo to location - if path is available from the current location
        else if (trigger == TriggerNames.GoTo1.ordinal() && pressed)
        {
           //ninja.getObjectCollection().testLightToggle(); // test
           
            steering.clearTasks();
            GoToNearestLocation();
            if (location != null)
                steering.addTaskToBottom(new FollowPath("MyPath", location, this));
        }
        else if (trigger == TriggerNames.GoTo2.ordinal() && pressed)
        {
            steering.clearTasks();
            GoToNearestLocation();
            if (location != null)
                steering.addTaskToBottom(new FollowPath("MyReversePath", location, this));
        }
        else if (trigger == TriggerNames.GoTo3.ordinal() && pressed)
        {
            steering.clearTasks();
            GoToNearestChair();
        }
        
        else if (trigger == TriggerNames.Smile.ordinal() && pressed)
        {
            if (ninja.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                ninja.initiateFacialAnimation(1, 1.0f, 1.0f);
        }   
        
        else if (trigger == TriggerNames.Frown.ordinal() && pressed)
        {
            if (ninja.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                ninja.initiateFacialAnimation(2, 3.0f, 1.5f);
        }   
        
        else if (trigger == TriggerNames.Scorn.ordinal() && pressed)
        {
            if (ninja.getFacialAnimationQ().calculateTotalRemainingTime() < 1.0f)
                ninja.initiateFacialAnimation(3, 3.0f, 1.5f);
        }   
                
        // Reverse the animation for the punch state
        else if (trigger == TriggerNames.Reverse.ordinal() && pressed)
        {
            PunchState punch = (PunchState) gameStates.get(PunchState.class);
            punch.setReverseAnimation(!punch.isReverseAnimation());
        }
        
        // Select the next animation to play for the facial animation test
        else if (trigger == TriggerNames.NextAction.ordinal() && pressed)
        {
            PunchState punch = (PunchState) gameStates.get(PunchState.class);
            punch.setAnimationSetBoolean(false);
            
            punchActionIndex++;
            if (punchActionIndex > 14)
                punchActionIndex = 0;
            switch (punchActionIndex)
            {
                case 0:
                    punch.setAnimationName("Male_Wave");
                    break;
                case 1:
                    punch.setAnimationName("Male_Run");
                    break;
                case 2:
                    punch.setAnimationName("Male_Bow");
                    break;
                case 3:
                    punch.setAnimationName("Male_Cheer");
                    break;
                case 4:
                    punch.setAnimationName("Male_Follow");
                    break;
                case 5:
                    punch.setAnimationName("Male_Jump");
                    break;
                case 6:
                    punch.setAnimationName("Male_Laugh");
                    break;
                case 7:
                    punch.setAnimationName("Male_Clap");
                    break;
                case 8:
                    punch.setAnimationName("Male_Idle");
                    break;
                case 9:
                    punch.setAnimationName("Male_Walk");
                    break;
                case 10:
                    punch.setAnimationName("Male_StandToSit");
                    break;
                case 11:
                    punch.setAnimationName("Male_Sitting");
                    break;
                case 12:
                    punch.setAnimationName("Male_FallFromSitting");
                    break;
                case 13:
                    punch.setAnimationName("Male_FloorSitting");
                    break;
                case 14:
                    punch.setAnimationName("Male_FloorGetup");
                    break;
            }
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
    public NinjaSteeringHelm getSteering() {
        return steering;
    }

    public void GoToNearestLocation() 
    {   
        if (ninja.getObjectCollection() == null)
            return;
        
        location = ninja.getObjectCollection().findNearestLocation(ninja, 10000.0f, 1.0f, false);
        if (location != null)
        {
            steering.addTaskToTop(new GoTo(location, this));
            
            ///steering.addTaskToTop(new )
//            Vector3f pos = location.getPosition();
//            Vector3f direction = location.getForwardVector();
//            steering.setGoalPosition(pos);
//            steering.setSittingDirection(direction);
//            steering.setGoal(location);
//            // GoTo and sit there
            steering.setEnable(true);
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
            steering.addTaskToTop(task);
            
//            steering.setGoalPosition(pos);
//            steering.setSittingDirection(direction);
//            steering.setGoal(obj);
            
            // GoTo and sit there
            steering.setEnable(true);
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

    
}
