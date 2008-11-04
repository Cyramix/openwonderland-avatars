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
    
    private Vector3f goalPosition = new Vector3f(0.0f, 0.0f, 0.0f);
    
    private Vector3f sittingDirection = Vector3f.UNIT_Z;
    
    private float directionSensitivity = 0.05f;
    
    private float approvedDistanceFromGoal = 0.5f;
    
    private boolean reachedGoal = false;
    
    private int precisionCounter = 0;
    
    private boolean bAvoidObstacles = true;
    
    private boolean walkBackFlip = false;
    private float walkBack = -1.0f;
    private float walkBackTime = 2.0f;
    private float turnBackTime = 0.5f;
    
    private SpatialObject goal = null;
    
    private boolean bDoneTurning = false;
    private float   pullTime = 5.0f;
    
    private float    sampleCounter     = 0.0f;
    private float    sampleTimeFrame   = 3.0f;
    private int      samples           = 1;
    private Vector3f sampleAvgPos      = new Vector3f();
    private Vector3f samplePrevAvgPos  = new Vector3f();
    
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
        if (!enabledState)
            return;
        
        Vector3f characterPosition = ninjaContext.getController().getPosition();
        float distanceFromGoal     = goalPosition.distance(characterPosition);
        
        // Sample
        sampleCounter += deltaTime;
        samples++;
        sampleAvgPos.addLocal(characterPosition);
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
            sampleAvgPos.set(characterPosition);
            samples       = 1;
            sampleCounter = 0.0f;
        }
        
        // TODO : clean up and lay out combinations framework for steering behaviors
        
        if (!reachedGoal)
        {   
            // If the chair is occupied then abort mission
            if (goal != null && goal instanceof Chair && ((Chair)goal).isOccupied())
            {
                ninjaContext.getController().getWindow().setTitle("Chair is Occupied!");
                enabledState = false;
                ninjaContext.resetTriggersAndActions();
                return;
            }
            
            // Seek the goal
            if (distanceFromGoal > approvedDistanceFromGoal)
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
                    return;
                }
                else
                {
                    // If not walk forwards
                    ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());
                }
                
                boolean steerToGoal = true;

                if (bAvoidObstacles)
                {
                    // Is there an imminent obstacle?
                    SpatialObject obj = null;
                    if (ninjaContext.getNinja().getObjectCollection() != null)
                        obj = ninjaContext.getNinja().getObjectCollection().findNearestChair(ninjaContext.getNinja(), 2.5f, 0.4f, false); // distance should be scaled by velocity... but at the moment the velocity is pretty constant...
                    if (obj != null && obj != goal && distanceFromGoal > 2.0f)
                    {
                        PSphere obstacleBV = obj.getNearestObstacleSphere(characterPosition);
                        PSphere characterBV = ninjaContext.getNinja().getBoundingSphere();
                        ninjaContext.getNinja().getModelInst().setDebugSphere(obstacleBV, 0);
                        ninjaContext.getNinja().getModelInst().setDebugSphere(characterBV, 1);
                        if (characterBV.isColliding(obstacleBV) && obstacleBV.getCenter().distance(characterBV.getCenter()) < 2.0f)
                        {
                            // Initiate walk back if colliding
                            walkBack     = 0.0f;
                            walkBackTime = 1.0f;
                            walkBackFlip = false;
                            steerToGoal  = false;
                            ninjaContext.resetTriggersAndActions();

                            Vector3f rightVec = ninjaContext.getController().getRightVector();
                            Vector3f directionToObstacle = obstacleBV.getCenter().subtract(characterPosition);
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
                            Vector3f directionToObstacle = obstacleBV.getCenter().subtract(characterPosition);
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
                            steerToGoal = false;
                        }
                    }
                }
                
                if (steerToGoal)
                {
                    ninjaContext.getController().getWindow().setTitle("Seeking Goal");
                    // Steer towards the goal
                    Vector3f rightVec = ninjaContext.getController().getRightVector();
                    Vector3f desiredVelocity = goalPosition.subtract(characterPosition);
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
            Vector3f pull = goalPosition.subtract(characterPosition).normalize().mult(distanceFromGoal * deltaTime * pullTime);
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
            else if (distanceFromGoal < 0.01f)
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
    
    
}
