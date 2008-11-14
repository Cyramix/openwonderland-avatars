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
import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.ninja.SitState;
import imi.character.objects.Chair;
import imi.character.objects.SpatialObject;
import imi.scene.PMatrix;
import imi.scene.boundingvolumes.PSphere;

/**
 *  Go to a chair, turn properly and sit down.
 *  Avoids other chairs as obstacles.
 * 
 * @author Lou Hayt
 */
public class GoSit implements Task
{
    String  description  = "Go to the nearest Chair and sit on it";
    String  status = "Chilling";
    
    private NinjaContext ninjaContext = null;
    
    private boolean bDone = false;
    
    private Chair goal = null;
    private Vector3f goalPosition = new Vector3f(Vector3f.ZERO);
    private Vector3f sittingDirection = new Vector3f(Vector3f.UNIT_Z);
            
    private boolean bAvoidObstacles = true;
    
    private float approvedDistanceFromGoal = 0.5f;
    private float directionSensitivity = 0.1f;
    private float   pullTime = 5.0f;
    
    private float       currentDistanceFromGoal  = 0.0f;
    private Vector3f    currentCharacterPosition = new Vector3f();
    
    private boolean bDoneTurning = false;
    private int precisionCounter = 0;
            
    private float    sampleCounter     = 0.0f;
    private float    sampleTimeFrame   = 3.0f;
    private int      samples           = 1;
    private Vector3f sampleAvgPos      = new Vector3f();
    private Vector3f samplePrevAvgPos  = new Vector3f();

    private boolean  bTryAgain         = false;
    
    public GoSit(Chair chair, NinjaContext context) 
    {
        ninjaContext = context;
        
        goal = chair;
        goalPosition.set(chair.getGoalPosition());
        sittingDirection.set(chair.getGoalForwardVector());
    }
    
    public boolean verify() 
    {
        if (bDone)
            return false;
        
        // If the chair is occupied then try finding another or abort mission
        if (goal != null && goal.isOccupied() || bTryAgain)
        {
            if (!ninjaContext.GoToNearestChair())
                System.out.println("Chair is Occupied! I give up! Can't find an empty chair in this damn virtual environment!");
            
            bTryAgain = false;
            System.out.println("Chair is Occupied! I WILL find another one!");
            status = "chair is occupied";
            return false;
        }
        return true;
    }
    
    public void update(float deltaTime) 
    {
        // Update local variables
        currentCharacterPosition.set(ninjaContext.getController().getPosition());
        currentDistanceFromGoal = goalPosition.distance(currentCharacterPosition);
        
        // Detect looping
        if (sampleProgress(deltaTime))
            return;
        
        // Go for the goal
        if(reachGoal(deltaTime))
            atGoal(deltaTime);
    }
    
    public boolean reachGoal(float deltaTime)
    {   
        if (currentDistanceFromGoal <= approvedDistanceFromGoal)
        {
            ninjaContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
            return true;
        }
        
        // Walk forwards
        ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
        ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());

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
        ninjaContext.resetTriggersAndActions();
        bDoneTurning = false;
        return true;
    }
    
    public void atGoal(float deltaTime)
    {
        // Own the chair if no one else does
        if (goal.getOwner() == null)
            goal.setOwner(ninjaContext.getNinja());
        else if (goal.getOwner() != ninjaContext.getNinja())
            bTryAgain = true;
        
        // Pull towards the goal
        PMatrix local = ninjaContext.getController().getTransform().getLocalMatrix(true);
        Vector3f pull = goalPosition.subtract(currentCharacterPosition).normalize().mult(currentDistanceFromGoal * deltaTime * pullTime);
        local.setTranslation(local.getTranslation().add(pull)); 

        if (!bDoneTurning)
        {
            status = "turning at goal";
            //ninjaContext.getController().getWindow().setTitle("Turning to goal orientation");

            // We have reached the goal, rotate to sitting direction
            Vector3f rightVec = ninjaContext.getController().getRightVector();
            float dot = sittingDirection.dot(rightVec);

            if (dot < Float.MIN_VALUE && dot > -Float.MIN_VALUE)
            {
                System.out.println("turning around dot ~= 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                // Check if inside the front half of space
                Vector3f fwdVec = ninjaContext.getController().getForwardVector();
                float frontHalfDot = sittingDirection.dot(fwdVec);
                if (frontHalfDot > 0.0f)
                {
                    System.out.println("turning around dot++");
                    dot++;
                }
            }

            if (dot > directionSensitivity)
            {
                ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
            }
            else if (dot < -directionSensitivity)
            {
                ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 
            }
            else 
            {    
                ninjaContext.getController().getWindow().setTitle("Pulling towards the goal point");
                ninjaContext.resetTriggersAndActions();
                bDoneTurning = true;
                status = "Done turning at goal";
            }
        }
        else if (currentDistanceFromGoal < 0.01f && !ninjaContext.isTransitioning())
        {
            ninjaContext.getController().getWindow().setTitle("Done");

            // Set position
            PMatrix localMat = ninjaContext.getController().getTransform().getLocalMatrix(true);
            localMat.setTranslation(goalPosition); 

            // Positioned properlly!
            bDone = true;
            //enabledState = false;
            ninjaContext.resetTriggersAndActions();

            // Initiate SitState
            SitState sit = (SitState) ninjaContext.getStates().get(SitState.class);
            if (sit != null && sit.toSit(null))
                ninjaContext.setCurrentState(sit);
        }
    }
    
    private void seekGoal(float deltaTime)
    {
        status = "seeking goal";
        ninjaContext.getController().getWindow().setTitle("Seeking Goal");
        
        // Steer towards the goal
        Vector3f rightVec = ninjaContext.getController().getRightVector();
        Vector3f desiredVelocity = goalPosition.subtract(currentCharacterPosition);
        desiredVelocity.normalizeLocal();
        float dot = desiredVelocity.dot(rightVec);
        if (dot < Float.MIN_VALUE && dot > -Float.MIN_VALUE)
        {
             System.out.println("dot ~= 0 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // Check if inside the front half of space
            Vector3f fwdVec = ninjaContext.getController().getForwardVector();
            float frontHalfDot = desiredVelocity.dot(fwdVec);
            if (frontHalfDot > 0.0f)
            {
                dot++;
                System.out.println("seekGoal dot == 0.0f dot++");
            }

        }
        if (dot > directionSensitivity)
        {
            ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
            ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
        }
        else if (dot < -directionSensitivity)
        {
            ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
            ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 
        }
        else
        {
            ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
            ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 

            precisionCounter++;
            if (precisionCounter > 60)
            {
                precisionCounter = 0;
                if (dot > Float.MIN_VALUE)
                {
                    ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                }
                else if (dot < -Float.MIN_VALUE)
                {
                    ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 
                }
            }
        }
    }
    
    private boolean avoidObstacles()
    {
        boolean bNeedToAvoid = false;
        
        // Is there an imminent obstacle?
        SpatialObject obj = null;
        if (ninjaContext.getNinja().getObjectCollection() != null)
            obj = ninjaContext.getNinja().getObjectCollection().findNearestChair(ninjaContext.getNinja(), 2.5f, 0.4f, false); // distance should be scaled by velocity... but at the moment the velocity is pretty constant...
        if (obj != null && obj != goal && currentDistanceFromGoal > 2.0f)
        {
            PSphere obstacleBV = obj.getNearestObstacleSphere(currentCharacterPosition);
            PSphere characterBV = ninjaContext.getNinja().getBoundingSphere();
            ninjaContext.getNinja().getModelInst().setDebugSphere(obstacleBV, 0);
            ninjaContext.getNinja().getModelInst().setDebugSphere(characterBV, 1);
            if (characterBV.isColliding(obstacleBV) && obstacleBV.getCenter().distance(characterBV.getCenter()) < 2.0f)
            {
                status = "collided with obstacle";
                // Initiate walk back if colliding
                Task walk = (Task) new Walk("Walking away from an obstacle", 1.0f, false, ninjaContext);
                ninjaContext.getSteering().addTaskToTop(walk);
                bNeedToAvoid  = true;
                ninjaContext.resetTriggersAndActions();

                Vector3f rightVec = ninjaContext.getController().getRightVector();
                Vector3f directionToObstacle = obstacleBV.getCenter().subtract(currentCharacterPosition);
                directionToObstacle.normalizeLocal();
                float dot = directionToObstacle.dot(rightVec);

                // Turn away
                if (dot > 0.0f)
                {
                    ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                }
                else
                {
                    ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                }
            }
            else
            {
                status = "avoiding obstacle";
                // Try to prevent a collision with an obstacle
                Vector3f rightVec = ninjaContext.getController().getRightVector();
                Vector3f directionToObstacle = obstacleBV.getCenter().subtract(currentCharacterPosition);
                directionToObstacle.normalizeLocal();
                float dot = directionToObstacle.dot(rightVec);

                if (dot > 0.0f)
                {
                    ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                }
                else
                {
                    ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                }

                ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());

                ninjaContext.getController().getWindow().setTitle("Avoiding");
                bNeedToAvoid = true;
            }
        }
        
        return bNeedToAvoid;
    }

    public void resetSamples()
    {
        System.out.println("samples reset");
        Vector3f characterPosition = ninjaContext.getController().getPosition();
        
        // Samples
        sampleAvgPos.set(characterPosition);
        samplePrevAvgPos.set(characterPosition);
        samples       = 1;
        sampleCounter = 0.0f;
    }
    
    // TODO finer increments and better analasys
    private boolean sampleProgress(float deltaTime) 
    {
        boolean result = false;
        sampleCounter += deltaTime;
        samples++;
        sampleAvgPos.addLocal(currentCharacterPosition);
        if (sampleCounter > sampleTimeFrame)
        {
            // Sample "tick"
            sampleAvgPos.divideLocal(samples);
            float currentAvgDistance  = sampleAvgPos.distance(goalPosition);
            float previousAvgDistance = samplePrevAvgPos.distance(goalPosition);
            
            // which is closer to the goal? the current sample average position or the previous one?
            if (currentAvgDistance > previousAvgDistance)
            {
                // we are not closer to the goal after sampleTimeFrame secounds... let's try to get out of this loop
                //Task walk = (Task) new Walk("Walking away from loop", 0.5f, true, ninjaContext);
                //ninjaContext.getSteering().addTaskToTop(walk);
                ninjaContext.getController().stop();
                
                System.out.println("sample tick: get out of loop");
                status = "loop detected";
                result = true;
            }
            else
                System.out.println("sample tick");
            
            samplePrevAvgPos.set(sampleAvgPos);
            sampleAvgPos.set(currentCharacterPosition);
            samples       = 1;
            sampleCounter = 0.0f;
        }
        return result;
    }
    
    public String getDescription() {
        return description;
    }

    public void onHold() 
    {
        status = "On hold";
    }

    public boolean checkGoalReached() {
        if (currentDistanceFromGoal > approvedDistanceFromGoal)
            return false;
        return true;
    }

    public String getStatus() {
        return status;
    }

    public SpatialObject getGoal() {
        return goal;
    }
}
