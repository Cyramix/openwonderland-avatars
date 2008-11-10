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
package imi.character;

import com.jme.math.Vector3f;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;

/**
 *
 * @author Lou Hayt
 */
public class GoTo implements Task 
{
    String  description  = "Go to an object";
    String  status = "Chilling";
    
    private GameContext context = null;
    
    private boolean bDone = false;
    
    private SpatialObject goal = null;
    private Vector3f goalPosition = new Vector3f(Vector3f.ZERO);
    private Vector3f goalDirection = new Vector3f(Vector3f.UNIT_Z);
            
    private boolean bAvoidObstacles = false;
    
    private float approvedDistanceFromGoal = 1.0f;
    private float directionSensitivity = 0.1f;
    
    private float       currentDistanceFromGoal  = 0.0f;
    private Vector3f    currentCharacterPosition = new Vector3f();
    
    private boolean bDoneTurning = false;
    private int precisionCounter = 0;
    
    public GoTo(SpatialObject goTo, GameContext context) 
    {
        this.context = context;
        
        goal = goTo;
        goalPosition.set(goal.getPosition());
        goalDirection.set(goal.getForwardVector());
        
        if (goal.getBoundingSphere() != null)
            approvedDistanceFromGoal = goal.getBoundingSphere().getRadius() * 0.5f;
    }
    
    public boolean verify() 
    {
        if (bDone)
            return false;
        
        return true;
    }
    
    public void update(float deltaTime) 
    {
        // Update local variables
        currentCharacterPosition.set(context.getController().getPosition());
        currentDistanceFromGoal = goalPosition.distance(currentCharacterPosition);
        
//        // Detect looping
//        if (sampleProgress(deltaTime))
//            return;
        
        // GoTo for the goal
        if(reachGoal(deltaTime))
            bDone = true;
    }
    
    public boolean reachGoal(float deltaTime)
    {   
        if (currentDistanceFromGoal <= approvedDistanceFromGoal)
        {
            context.triggerReleased(TriggerNames.Move_Forward.ordinal());
            return true;
        }
        
        // Walk forwards
        context.triggerPressed(TriggerNames.Move_Forward.ordinal());
        context.triggerReleased(TriggerNames.Move_Back.ordinal());

        // Avoid obstacles or Seek the goal
        if (bAvoidObstacles)
        {
            if (!avoidObstacles())
                seekGoal(deltaTime);
        }
        else
            seekGoal(deltaTime);
        
        // Have we reached the goal?
        if (currentDistanceFromGoal > approvedDistanceFromGoal)
            return false;
        
        // We have reached the goal!
        context.resetTriggersAndActions();
        bDoneTurning = false;
        return true;
    }
    
    private void seekGoal(float deltaTime)
    {
        status = "seeking goal";
        context.getController().getWindow().setTitle("Seeking Goal");
        
        // Steer towards the goal
        Vector3f rightVec = context.getController().getRightVector();
        Vector3f desiredVelocity = goalPosition.subtract(currentCharacterPosition);
        desiredVelocity.normalizeLocal();
        float dot = desiredVelocity.dot(rightVec);
        if (dot < Float.MIN_VALUE && dot > -Float.MIN_VALUE)
        {
             System.out.println("dot ~= 0 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // Check if inside the front half of space
            Vector3f fwdVec = context.getController().getForwardVector();
            float frontHalfDot = desiredVelocity.dot(fwdVec);
            if (frontHalfDot > 0.0f)
            {
                dot++;
                System.out.println("seekGoal dot == 0.0f dot++");
            }
        }
        if (dot > directionSensitivity)
        {
            context.triggerPressed(TriggerNames.Move_Right.ordinal());
            context.triggerReleased(TriggerNames.Move_Left.ordinal());
        }
        else if (dot < -directionSensitivity)
        {
            context.triggerPressed(TriggerNames.Move_Left.ordinal());
            context.triggerReleased(TriggerNames.Move_Right.ordinal()); 
        }
        else
        {
            context.triggerReleased(TriggerNames.Move_Left.ordinal());
            context.triggerReleased(TriggerNames.Move_Right.ordinal()); 

            precisionCounter++;
            if (precisionCounter > 60)
            {
                precisionCounter = 0;
                if (dot > Float.MIN_VALUE)
                {
                    context.triggerPressed(TriggerNames.Move_Right.ordinal());
                    context.triggerReleased(TriggerNames.Move_Left.ordinal());
                }
                else if (dot < -Float.MIN_VALUE)
                {
                    context.triggerPressed(TriggerNames.Move_Left.ordinal());
                    context.triggerReleased(TriggerNames.Move_Right.ordinal()); 
                }
            }
        }
    }
    
    private boolean avoidObstacles()
    {
        boolean bNeedToAvoid = false;
        
        
        return bNeedToAvoid;
    }

    public void onHold() {
        status = "on hold";
    }

    public SpatialObject getGoal() {
        return goal;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}
