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
import imi.character.objects.SpatialObject;
import imi.scene.boundingvolumes.PSphere;

/**
 *
 * @author Lou
 */
public class NinjaSteeringHelm extends CharacterSteeringHelm
{
    private NinjaContext ninjaContext = null;
    
    private Vector3f goalPosition = new Vector3f(10.0f, 0.0f, 10.0f);
    
    private Vector3f sittingDirection = Vector3f.UNIT_Z;
    
    private float directionSensitivity = 0.05f;
    
    private float approvedDistanceFromGoal = 0.05f;
    
    private boolean reachedGoal = false;
    
    private int precisionCounter = 0;
    
    private boolean bAvoidObstacles = false;
    
    private float walkBack = -1.0f;
    private float walkBackTime = 2.0f;
    private float turnBackTime = 0.4f;
        
    public NinjaSteeringHelm(String name, NinjaContext gameContext)
    {
        super(name, gameContext);
        ninjaContext = gameContext;
    }
    
    @Override
    public void update(float deltaTime)
    {
        if (!enabledState)
            return;
        
        // TODO : clean up and lay out combinations framework for steering behaviors
        
        if (!reachedGoal)
        {
            // Seek the goal
            float distanceFromGoal = goalPosition.distance(ninjaContext.getController().getPosition());
            if (distanceFromGoal > approvedDistanceFromGoal)
            {
                boolean steerToGoal = true;
                // Are we walking backwards to get away from an obstacle?
                if (walkBack >= 0.0f)
                {
                    ninjaContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
                    ninjaContext.triggerPressed(TriggerNames.Move_Back.ordinal());

                    walkBack += deltaTime;

                    // stop truning while walking backwards
                    if (walkBack > turnBackTime)
                    {
                        ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                        ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                    }

                    // stop walking backwards
                    if (walkBack > walkBackTime)
                        walkBack = -1.0f;

                    return;
                }
                else
                {
                    // If not walk forwards
                    ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                    ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());
                }

                if (bAvoidObstacles)
                {
                    // Is there an imminent obstacle?
                    SpatialObject obj = null;
                    if (ninjaContext.getNinja().getObjectCollection() != null)
                        obj = ninjaContext.getNinja().getObjectCollection().findNearestChair(ninjaContext.getNinja(), 3.0f, 0.4f);
                    if (obj != null && distanceFromGoal > 2.0f)
                    {
                        PSphere obstacleBV = obj.getNearestObstacleSphere(ninjaContext.getController().getPosition());
                        PSphere characterBV = ninjaContext.getNinja().getBoundingSphere();
                        if (characterBV.isColliding(obstacleBV) && obstacleBV.getCenter().distance(characterBV.getCenter()) < 2.0f)
                        {
                            // Walk back if colliding
                            walkBack    = 0.0f;
                            steerToGoal = false;
                            ninjaContext.resetTriggersAndActions();

                            Vector3f rightVec = ninjaContext.getController().getRightVector();
                            Vector3f directionToObstacle = obstacleBV.getCenter().subtract(ninjaContext.getController().getPosition());
                            directionToObstacle.normalizeLocal();
                            float dot = directionToObstacle.dot(rightVec);

                            // Turn away
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
                        }
                        else
                        {
                            // Try to prevent a collision with an obstacle
                            Vector3f rightVec = ninjaContext.getController().getRightVector();
                            Vector3f directionToObstacle = obstacleBV.getCenter().subtract(ninjaContext.getController().getPosition());
                            directionToObstacle.normalizeLocal();
                            float dot = directionToObstacle.dot(rightVec);

                            if (dot > directionSensitivity)
                            {
                                ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
                                ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
                            }
                            else if (dot < -directionSensitivity)
                            {
                                ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
                                ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
                            }

                            ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                            ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());
                            steerToGoal = false;
                        }
                    }
                }
                
                if (steerToGoal)
                {
                    // Steer towards the goal
                    Vector3f rightVec = ninjaContext.getController().getRightVector();
                    Vector3f desiredVelocity = goalPosition.subtract(ninjaContext.getController().getPosition());
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
            }
        }
        else
        {
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
                // Positioned properlly!
                enabledState = false;
                ninjaContext.resetTriggersAndActions();
                
                // Trigger sitting
                ninjaContext.triggerPressed(TriggerNames.Sit.ordinal());
            }
        }
    }
    
    @Override
    public boolean toggleEnable()
    {
        enabledState = !enabledState;
        
        if (!enabledState)
            ninjaContext.resetTriggersAndActions();
        else
            reachedGoal = false;
        
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
    }
    
    
}
