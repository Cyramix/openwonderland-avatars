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
package imi.character.steering;

import imi.character.*;
import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.corestates.SitState;
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
    
    private AvatarContext avatarContext = null;
    
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
    private float    sampleTimeFrame   = 0.75f;
    private int      samples           = 1;
    private Vector3f sampleAvgPos      = new Vector3f();
    private Vector3f samplePrevAvgPos  = new Vector3f();
    private int      sampleStreak      = 0;
    private int      samplePrevStreak  = 0;

    private boolean  bTryAgain         = false;
    
    public GoSit(Chair chair, AvatarContext context) 
    {
        avatarContext = context;
        
        goal = chair;
        goalPosition.set(chair.getGoalPosition());
        sittingDirection.set(chair.getGoalForwardVector());
        
        //context.getCharacter().getLeftEyeBall().setTarget(goalPosition.add(0.0f, 1.0f, 0.0f));
        //context.getCharacter().getRightEyeBall().setTarget(goalPosition.add(0.0f, 1.0f, 0.0f));
    }
    
    public boolean verify() 
    {
        if (bDone)
            return false;
        
        // If the chair is occupied then try finding another or abort mission
        if (goal != null && goal.isOccupied() || bTryAgain)
        {
            if (!avatarContext.GoToNearestChair())
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
        currentCharacterPosition.set(avatarContext.getController().getPosition());
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
            avatarContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
            return true;
        }
        
        // Walk forwards
        avatarContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
        avatarContext.triggerReleased(TriggerNames.Move_Back.ordinal());

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
        avatarContext.resetTriggersAndActions();
        bDoneTurning = false;
        return true;
    }
    
    public void atGoal(float deltaTime)
    {
        // Own the chair if no one else does
        if (goal.getOwner() == null)
            goal.setOwner(avatarContext.getavatar());
        else if (goal.getOwner() != avatarContext.getavatar())
            bTryAgain = true;
        
        // Pull towards the goal
        PMatrix local = avatarContext.getController().getTransform().getLocalMatrix(true);
        Vector3f pull = goalPosition.subtract(currentCharacterPosition).normalize().mult(currentDistanceFromGoal * deltaTime * pullTime);
        local.setTranslation(local.getTranslation().add(pull)); 

        if (!bDoneTurning)
        {
            status = "turning at goal";
            //avatarContext.getController().getWindow().setTitle("Turning to goal orientation");

            // We have reached the goal, rotate to sitting direction
            Vector3f rightVec = avatarContext.getController().getRightVector();
            float dot = sittingDirection.dot(rightVec);
            
            if (dot > directionSensitivity)
            {
                avatarContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
            }
            else if (dot < -directionSensitivity)
            {
                avatarContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 
            }
            else 
            {
                // Check if inside the front half of space
                Vector3f fwdVec = avatarContext.getController().getForwardVector();
                float frontHalfDot = sittingDirection.dot(fwdVec);
                if (frontHalfDot > 0.0f)
                {
                    System.out.println("turning around");
                    if ( !avatarContext.getTriggerState().isKeyPressed(TriggerNames.Move_Right.ordinal()) && 
                         !avatarContext.getTriggerState().isKeyPressed(TriggerNames.Move_Left.ordinal()) )
                        avatarContext.triggerPressed(TriggerNames.Move_Right.ordinal());        
                    return;
                }
                
                avatarContext.getController().getWindow().setTitle("Pulling towards the goal point");
                avatarContext.resetTriggersAndActions();
                bDoneTurning = true;
                status = "Done turning at goal";
            }
        }
        else if (currentDistanceFromGoal < 0.01f && !avatarContext.isTransitioning())
        {
            avatarContext.getController().getWindow().setTitle("Done");

            // Set position
            PMatrix localMat = avatarContext.getController().getTransform().getLocalMatrix(true);
            localMat.setTranslation(goalPosition);

            // Positioned properlly!
            bDone = true;
            //enabledState = false;
            avatarContext.resetTriggersAndActions();

            // Initiate SitState
            SitState sit = (SitState) avatarContext.getStateMapping().get(SitState.class);
            if (sit != null && sit.toSit(null))
                avatarContext.setCurrentState(sit);
        }
    }
    
    private void seekGoal(float deltaTime)
    {
        status = "seeking goal";
        avatarContext.getController().getWindow().setTitle("Seeking Goal");
        
        // Steer towards the goal
        Vector3f rightVec = avatarContext.getController().getRightVector();
        Vector3f desiredVelocity = goalPosition.subtract(currentCharacterPosition);
        desiredVelocity.normalizeLocal();
        float dot = desiredVelocity.dot(rightVec);
        if (dot < Float.MIN_VALUE && dot > -Float.MIN_VALUE)
        {
             System.out.println("dot ~= 0 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // Check if inside the front half of space
            Vector3f fwdVec = avatarContext.getController().getForwardVector();
            float frontHalfDot = desiredVelocity.dot(fwdVec);
            if (frontHalfDot > 0.0f)
            {
                dot++;
                System.out.println("seekGoal dot == 0.0f dot++");
            }

        }
        if (dot > directionSensitivity)
        {
            avatarContext.triggerPressed(TriggerNames.Move_Right.ordinal());
            avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
        }
        else if (dot < -directionSensitivity)
        {
            avatarContext.triggerPressed(TriggerNames.Move_Left.ordinal());
            avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 
        }
        else
        {
            avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
            avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 

            precisionCounter++;
            if (precisionCounter > 60)
            {
                precisionCounter = 0;
                if (dot > Float.MIN_VALUE)
                {
                    avatarContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                    avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                }
                else if (dot < -Float.MIN_VALUE)
                {
                    avatarContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                    avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal()); 
                }
            }
        }
    }
    
    private boolean avoidObstacles()
    {
        boolean bNeedToAvoid = false;
        
        // Is there an imminent obstacle?
        SpatialObject obj = null;
        if (avatarContext.getavatar().getObjectCollection() != null)
            obj = avatarContext.getavatar().getObjectCollection().findNearestChair(avatarContext.getavatar(), 2.5f, 0.4f, false); // distance should be scaled by velocity... but at the moment the velocity is pretty constant...
        if (obj != null && obj != goal && currentDistanceFromGoal > 2.0f)
        {
            PSphere obstacleBV = obj.getNearestObstacleSphere(currentCharacterPosition);
            PSphere characterBV = avatarContext.getavatar().getBoundingSphere();
            avatarContext.getavatar().getModelInst().setDebugSphere(obstacleBV, 0);
            avatarContext.getavatar().getModelInst().setDebugSphere(characterBV, 1);
            if (characterBV.isColliding(obstacleBV) && obstacleBV.getCenter().distance(characterBV.getCenter()) < 2.0f)
            {
                status = "collided with obstacle";
                // Initiate walk back if colliding
                Task walk = (Task) new Walk("Walking away from an obstacle", 1.0f, false, avatarContext);
                avatarContext.getSteering().addTaskToTop(walk);
                bNeedToAvoid  = true;
                avatarContext.resetTriggersAndActions();

                Vector3f rightVec = avatarContext.getController().getRightVector();
                Vector3f directionToObstacle = obstacleBV.getCenter().subtract(currentCharacterPosition);
                directionToObstacle.normalizeLocal();
                float dot = directionToObstacle.dot(rightVec);

                // Turn away
                if (dot > 0.0f)
                {
                    avatarContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                    avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                }
                else
                {
                    avatarContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                    avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                }
            }
            else
            {
                status = "avoiding obstacle";
                // Try to prevent a collision with an obstacle
                Vector3f rightVec = avatarContext.getController().getRightVector();
                Vector3f directionToObstacle = obstacleBV.getCenter().subtract(currentCharacterPosition);
                directionToObstacle.normalizeLocal();
                float dot = directionToObstacle.dot(rightVec);

                if (dot > 0.0f)
                {
                    avatarContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                    avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                }
                else
                {
                    avatarContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                    avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                }

                avatarContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                avatarContext.triggerReleased(TriggerNames.Move_Back.ordinal());

                avatarContext.getController().getWindow().setTitle("Avoiding");
                bNeedToAvoid = true;
            }
        }
        
        return bNeedToAvoid;
    }

    public void resetSamples()
    {
        System.out.println("samples reset");
        Vector3f characterPosition = avatarContext.getController().getPosition();
        
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
            float currentAvgDistance  = sampleAvgPos.distanceSquared(goalPosition);
            float previousAvgDistance = samplePrevAvgPos.distanceSquared(goalPosition);
            
            // which is closer to the goal? the current sample average position or the previous one?
            if (currentAvgDistance > previousAvgDistance)
            {
                sampleStreak++;
                if (sampleStreak > 3)
                {
                    samplePrevStreak = sampleStreak;
                    sampleStreak = 0;
                    // we are not closer to the goal after sampleTimeFrame secounds... let's try to get out of this loop
                    //Task walk = (Task) new Walk("Walking away from loop", 0.5f, true, avatarContext);
                    //avatarContext.getSteering().addTaskToTop(walk);
                    avatarContext.getController().stop();

                    System.out.println("sample tick: stop getting away from the target");
                    status = "loop detected";
                    result = true;
                }
            }
            else
            {
                if (samplePrevStreak > 0 && sampleStreak > 0)
                {
                    System.out.println("fishy sample tick: stop the loop");    
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");    
                    avatarContext.getController().stop();
                }
                else
                    System.out.println("sample tick: prev streak " + samplePrevStreak + " current streak " + sampleStreak);
                
                samplePrevStreak = sampleStreak;
                sampleStreak = 0;
            }
            
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
