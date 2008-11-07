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

import com.jme.math.Vector3f;
import imi.character.GoSit;
import imi.character.ninja.transitions.FlyToIdle;
import imi.character.ninja.transitions.IdleToFly;
import imi.character.ninja.transitions.IdleToPunch;
import imi.character.ninja.transitions.IdleToTurn;
import imi.character.ninja.transitions.IdleToWalk;
import imi.character.ninja.transitions.PunchToIdle;
import imi.character.ninja.transitions.PunchToTurn;
import imi.character.ninja.transitions.PunchToWalk;
import imi.character.ninja.transitions.SitToIdle;
import imi.character.ninja.transitions.TurnToIdle;
import imi.character.ninja.transitions.TurnToPunch;
import imi.character.ninja.transitions.TurnToWalk;
import imi.character.ninja.transitions.WalkToIdle;
import imi.character.ninja.transitions.WalkToPunch;
import imi.character.objects.Chair;
import imi.character.objects.Goal;
import imi.character.objects.LocationNode;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState.Action;
import imi.scene.PMatrix;
import imi.scene.PScene;
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
    
    int testHack = 0;
    
    public static enum TriggerNames
    {
        Movement_Modifier,
        Move_Left, 
        Move_Right,
        Move_Forward,
        Move_Back,
        Punch,
        Sit,
        ToggleSteering,
        PositionGoalPoint,
        SelectNearestGoalPoint,
        Move_Up,
        Move_Down,
        NextAction,
        Reverse,
        GoTo1,
        GoTo2,
        GoTo3,
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
        
        // Set the state to start with
        setCurrentState(gameStates.get(IdleState.class));
        
        // Register validation methods (entry points)
        RegisterStateEntryPoint(gameStates.get(IdleState.class),  "toIdle");
        RegisterStateEntryPoint(gameStates.get(WalkState.class),  "toWalk");
        RegisterStateEntryPoint(gameStates.get(TurnState.class),  "toTurn");
        RegisterStateEntryPoint(gameStates.get(PunchState.class), "toPunch");
        RegisterStateEntryPoint(gameStates.get(FlyState.class),   "toFly");
                
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
     
        // Position the goal point for yourself and other characters you will switch controls to
        else if (trigger == TriggerNames.PositionGoalPoint.ordinal() && pressed)
        {
//            // Inform steering
//            steering.setGoalPosition(controller.getPosition());
//            steering.setSittingDirection(controller.getForwardVector());
//            
//            // Inform global goal point for visualization
//            Goal goalPoint = (Goal) ninja.getWorldManager().getUserData(Goal.class);
//            if (goalPoint != null)
//            {
//                goalPoint.getTransform().getLocalMatrix(true).set(controller.getTransform().getWorldMatrix(false));
//                goalPoint.getTransform().getLocalMatrix(true).setScale(1.0f);
//                PScene GPScene = goalPoint.getPScene();
//                GPScene.setDirty(true, true);
//                GPScene.submitTransforms();
//            }
        }
        
        // Find nearest chair and sit on it
        else if (trigger == TriggerNames.SelectNearestGoalPoint.ordinal() && pressed)
        {
            GoToNearestChair();
        }
        
        // Set the current state to the sit state
        else if (trigger == TriggerNames.Sit.ordinal() && pressed)
        {
            SitState sit = (SitState) gameStates.get(SitState.class);
            if (sit.toSit(null))
            {
                setCurrentState(sit);
                triggerReleased(TriggerNames.Sit.ordinal()); 
            }
        }
        
        // Reverse the animation for the punch state
        else if (trigger == TriggerNames.Reverse.ordinal() && pressed)
        {
            PunchState punch = (PunchState) gameStates.get(PunchState.class);
            punch.setReverseAnimation(!punch.isReverseAnimation());
        }
        
        // Go to location - if path is available from the current location
        else if (trigger == TriggerNames.GoTo1.ordinal() && pressed)
        {
            GoToNearestLocation();
        }
        else if (trigger == TriggerNames.GoTo2.ordinal() && pressed)
        {
            GoToNearestLocation();
        }
        else if (trigger == TriggerNames.GoTo3.ordinal() && pressed)
        {
            GoToNearestLocation();
        }
        
        // Select the next animation to play for the punch state
        else if (trigger == TriggerNames.NextAction.ordinal() && pressed)
        {
            PunchState punch = (PunchState) gameStates.get(PunchState.class);
            punch.setAnimationSetBoolean(false);
            
            testHack++;
            if (testHack > 11)
                testHack = 0;
            switch (testHack)
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

    public NinjaSteeringHelm getSteering() {
        return steering;
    }

    public void GoToNearestLocation() 
    {   
//        if (ninja.getObjectCollection() == null)
//            return;
//        
//        location = ninja.getObjectCollection().findNearestLocation(ninja, 10000.0f, 1.0f, false);
//        if (location != null)
//        {
//            Vector3f pos = location.getPosition();
//            Vector3f direction = location.getForwardVector();
//            steering.setGoalPosition(pos);
//            steering.setSittingDirection(direction);
//            steering.setGoal(location);
//            // Go and sit there
//            steering.setEnable(true);
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
//        }
    }
        
    public boolean GoToNearestChair()
    {
        if (ninja.getObjectCollection() == null)
            return false;

        SpatialObject obj = ninja.getObjectCollection().findNearestChair(ninja, 10000.0f, 1.0f, true);
        if (obj != null && !((Chair)obj).isOccupied())
        {
            Vector3f pos = ((Chair)obj).getGoalPosition();
            Vector3f direction = ((Chair)obj).getGoalForwardVector();
            
            GoSit task = new GoSit((Chair)obj, this);
            steering.addTask(task);
            
//            steering.setGoalPosition(pos);
//            steering.setSittingDirection(direction);
//            steering.setGoal(obj);
            
            // Go and sit there
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
