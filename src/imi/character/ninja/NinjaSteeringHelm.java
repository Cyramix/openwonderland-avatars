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
import imi.character.CharacterSteeringHelm;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.objects.Chair;
import imi.character.objects.SpatialObject;
import imi.scene.PMatrix;
import imi.scene.boundingvolumes.PSphere;

/**
 *
 * @author Lou
 */
public class NinjaSteeringHelm extends CharacterSteeringHelm
{
    private NinjaContext ninjaContext = null;
    
    private String        path = null;
    
    private SpatialObject goal = null;
    
    private Vector3f goalPosition = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f sittingDirection = Vector3f.UNIT_Z;
    
    private float approvedDistanceFromGoal = 0.5f;
    
    private float directionSensitivity = 0.1f;
    
    private boolean reachedGoal = false;
        
    private boolean bAvoidObstacles = true;
    
    private boolean walkBackFlip = false;
    private float walkBack = -1.0f;
    private float walkBackTime = 2.0f;
    private float turnBackTime = 0.5f;
    
    private int precisionCounter = 0;
    
    private boolean bDoneTurning = false;
    private float   pullTime = 5.0f;
    
    private float    sampleCounter     = 0.0f;
    private float    sampleTimeFrame   = 3.0f;
    private int      samples           = 1;
    private Vector3f sampleAvgPos      = new Vector3f();
    private Vector3f samplePrevAvgPos  = new Vector3f();
    
    private Vector3f    currentCharacterPosition = new Vector3f();
    private float       currentDistanceFromGoal  = 0.0f;
    
    public NinjaSteeringHelm(String name, NinjaContext gameContext)
    {
        super(name, gameContext);
        ninjaContext = gameContext;
    }
    
    private void initSteering()
    {
        reachedGoal     = false;
        
        Vector3f characterPosition = ninjaContext.getController().getPosition();
        
        // Samples
        sampleAvgPos.set(characterPosition);
        samplePrevAvgPos.set(characterPosition);
        samples       = 1;
        sampleCounter = 0.0f;
    }
    
    @Override
    public void update(float deltaTime)
    {
        // TODO : clean up and lay out combinations framework for steering behaviors
        
        if (!enabledState)
            return;
        
        updateGoal();
        
        if (!verifyDestination())
            return;

        sampleProgress(deltaTime);
        
        if (!reachedGoal)
        {   
            // If goal not reached
            if (currentDistanceFromGoal > approvedDistanceFromGoal)
            {   
                // React if needed
                if (!react(deltaTime));
                {
                    // If not walk forwards
                    ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());
                }
                
                // Avoid obstacles or Seek the goal
                if (!bAvoidObstacles || !avoidObstacles() )
                {
                    seekGoal(deltaTime);
                }
            }
            else
            {   
                // Goal reached!
                reachedGoal = true;
                ninjaContext.resetTriggersAndActions();
                bDoneTurning = false;
            }
        }
        else
        {
            // Pull towards the goal
            PMatrix local = ninjaContext.getController().getTransform().getLocalMatrix(true);
            Vector3f pull = goalPosition.subtract(currentCharacterPosition).normalize().mult(currentDistanceFromGoal * deltaTime * pullTime);
            local.setTranslation(local.getTranslation().add(pull)); 
                        
            if (!bDoneTurning)
            {
                ninjaContext.getController().getWindow().setTitle("Turning to goal orientation");

                // We have reached the goal, rotate to sitting direction
                Vector3f rightVec = ninjaContext.getController().getRightVector();
                float dot = sittingDirection.dot(rightVec);
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
                }
            }
            else if (currentDistanceFromGoal < 0.01f && !ninjaContext.isTransitioning())
            {
                done();
            }
        }
    }
    
    private void done()
    {
        ninjaContext.getController().getWindow().setTitle("Done");

        // Set position
        PMatrix local = ninjaContext.getController().getTransform().getLocalMatrix(true);
        local.setTranslation(goalPosition); 
        
        // Positioned properlly!
        enabledState = false;
        ninjaContext.resetTriggersAndActions();

        // Trigger sitting
        ninjaContext.triggerPressed(TriggerNames.Sit.ordinal());
    }

    /**
     * 
     * @param deltaTime
     * @return true if reacting includes controling the character, false if other behaviors can control it
     */
    private boolean react(float deltaTime) 
    {    
        // Are we walking backwards to get away from an obstacle?
        if (walkBack >= 0.0f)
        {
            if (walkBackFlip)
            {
                ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());
                ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());      
            }
            else
            {
                ninjaContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
                ninjaContext.triggerPressed(TriggerNames.Move_Back.ordinal());   
            }

            walkBack += deltaTime;

            // stop truning while walking backwards
            if (walkBack > turnBackTime)
            {
                ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
            }

            // stop walking backwards
            if (walkBack > walkBackTime)
            {
                walkBack = -1.0f;
                initSteering();
            }

            ninjaContext.getController().getWindow().setTitle("Walking Back");
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private void seekGoal(float deltaTime)
    {
        ninjaContext.getController().getWindow().setTitle("Seeking Goal");
        // Steer towards the goal
        Vector3f rightVec = ninjaContext.getController().getRightVector();
        Vector3f desiredVelocity = goalPosition.subtract(currentCharacterPosition);
        desiredVelocity.normalizeLocal();
        float dot = desiredVelocity.dot(rightVec);
        if (dot == 0.0f)
        {
            // Check if inside the front half of space
            Vector3f fwdVec = ninjaContext.getController().getForwardVector();
            float frontHalfDot = desiredVelocity.dot(fwdVec);
            if (frontHalfDot > 0.0f)
                dot++;

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
            if (precisionCounter > 200)
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
                // Initiate walk back if colliding
                walkBack     = 0.0f;
                walkBackTime = 1.0f;
                walkBackFlip = false;
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
        
    @Override
    public boolean toggleEnable()
    {
        enabledState = !enabledState;
        
        if (enabledState)
            initSteering();
        else
            ninjaContext.resetTriggersAndActions();
        
        return enabledState;
    }

    public Vector3f getGoalPosition() {
        return goalPosition;
    }

    public void setGoalPosition(Vector3f goalPosition) {
        this.goalPosition = goalPosition;
    }

    public Vector3f getSittingDirection() {
        return sittingDirection;
    }

    public void setSittingDirection(Vector3f sittingDirection) {
        this.sittingDirection = sittingDirection;
    }
    
    public boolean isReachedGoal() {
        return reachedGoal;
    }

    public void setReachedGoal(boolean reachedGoal) {
        this.reachedGoal = reachedGoal;
        if (reachedGoal)
            bDoneTurning = false;
        else
            initSteering();
    }

    public SpatialObject getGoal() {
        return goal;
    }

    public void setGoal(SpatialObject goal) {
        this.goal = goal;
    }

    // TODO finer increments and better analasys
    private void sampleProgress(float deltaTime) 
    {
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
                walkBackFlip = true;
                walkBack     = 0.0f;
                walkBackTime = 0.5f;
                ninjaContext.resetTriggersAndActions();
                System.out.println("sample tick: get out of loop");
            }
            else
                System.out.println("sample tick");
            
            samplePrevAvgPos.set(sampleAvgPos);
            sampleAvgPos.set(currentCharacterPosition);
            samples       = 1;
            sampleCounter = 0.0f;
        }
    }

    private void updateGoal() 
    {
        currentCharacterPosition.set(ninjaContext.getController().getPosition());
        currentDistanceFromGoal = goalPosition.distance(currentCharacterPosition);
    }

    private boolean verifyDestination() 
    {
        // If the chair is occupied then try finding another or abort mission
        if (goal != null && goal instanceof Chair && ((Chair)goal).isOccupied())
        {
            ninjaContext.getController().getWindow().setTitle("Chair is Occupied! I WILL find another one!");
            if (!ninjaContext.GoToNearestChair())
            {
                ninjaContext.getController().getWindow().setTitle("Chair is Occupied! I give up! Can't find an empty chair in this damn virtual environment!");
                enabledState = false;
                ninjaContext.resetTriggersAndActions();
                return false;
            }
            return true;
        }
        return true;
    }
    
    
}
