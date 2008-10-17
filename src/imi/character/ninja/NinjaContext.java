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
    private Ninja           ninja       = null;
    private NinjaController controller  = null;
    private NinjaSteeringHelm steering  = new NinjaSteeringHelm("Ninja Steering Helm", this);
    
    public static enum TriggerNames
    {
        Movement_Modifier,
        Move_Left, 
        Move_Right,
        Move_Forward,
        Move_Back,
        Punch,
        Sit,
        GoSit,
        PositionGoalPoint,
        SelectNearestGoalPoint,
    }
    
    public static enum ActionNames
    {
        Movement_X,
        //Movement_Y,
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
        
        // Set the state to start with
        setCurrentState(gameStates.get(IdleState.class));
        
        // Register validation methods (entry points)
        RegisterStateEntryPoint(gameStates.get(IdleState.class),  "toIdle");
        RegisterStateEntryPoint(gameStates.get(WalkState.class),  "toWalk");
        RegisterStateEntryPoint(gameStates.get(TurnState.class),  "toTurn");
        RegisterStateEntryPoint(gameStates.get(PunchState.class), "toPunch");
                
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
        if (trigger == TriggerNames.GoSit.ordinal() && pressed)
            steering.toggleEnable();
     
        else if (trigger == TriggerNames.PositionGoalPoint.ordinal() && pressed)
        {
            // Inform steering
            steering.setGoalPosition(controller.getPosition());
            steering.setSittingDirection(controller.getTransform().getWorldMatrix(false).getLocalZ().mult(-1.0f));
            
            // Inform global goal point
            Goal goalPoint = (Goal) ninja.getWorldManager().getUserData(Goal.class);
            if (goalPoint != null)
            {
                goalPoint.getTransform().getLocalMatrix(true).set(controller.getTransform().getWorldMatrix(false));
                goalPoint.getTransform().getLocalMatrix(true).setScale(1.0f);
                PScene GPScene = goalPoint.getPScene();
                GPScene.setDirty(true, true);
                GPScene.submitTransforms();
            }
        }
        
        else if (trigger == TriggerNames.SelectNearestGoalPoint.ordinal() && pressed)
        {
            if (ninja.getObjectCollection() == null)
                return;
            
            SpatialObject obj = ninja.getObjectCollection().findNearestChair(ninja, 10000.0f, 1.0f);
            if (obj != null)
            {
                Vector3f pos = ((Chair)obj).getGoalPosition();
                Vector3f direction = ((Chair)obj).getGoalForwardVector();
                steering.setGoalPosition(pos);
                steering.setSittingDirection(direction);
                Goal goalPoint = (Goal) ninja.getWorldManager().getUserData(Goal.class);
                if (goalPoint != null)
                {
                    PMatrix goal = new PMatrix(); 
                    goal.lookAt(pos, pos.add(direction), Vector3f.UNIT_Y);
                    goal.invert();
                    goalPoint.getTransform().setLocalMatrix(goal);
                    goalPoint.getTransform().getLocalMatrix(true).setScale(1.0f);
                    //goalPoint.getTransform().getLocalMatrix(true).setTranslation(pos);
                    PScene GPScene = goalPoint.getPScene();;
                    GPScene.setDirty(true, true);
                    GPScene.submitTransforms();
                }
                // Go and sit there
                steering.setEnable(true);
                steering.setReachedGoal(false);
            }
        }
        
        else if (trigger == TriggerNames.Sit.ordinal() && pressed)
        {
            setCurrentState(gameStates.get(SitState.class));
            triggerReleased(TriggerNames.Sit.ordinal()); 
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

    
}
