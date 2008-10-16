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

/**
 *
 * @author Lou
 */
public class NinjaSteeringHelm extends CharacterSteeringHelm
{
    private NinjaContext ninjaContext = null;
    
    private Vector3f goalPosition = new Vector3f(10.0f, 0.0f, 10.0f);
    
    private Vector3f sittingDirection = new Vector3f(1.0f, 0.0f, 0.0f);
    
    private float directionSensitivity = 0.05f;
    
    private float approvedDistanceFromGoal = 1.0f;
    
    private boolean reachedGoal = false;
    
    private int precisionCounter = 0;
        
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
    
        if (!reachedGoal)
        {
            // Seek the goal
            float distanceFromGoal = goalPosition.distance(ninjaContext.getController().getPosition());
            if (distanceFromGoal > approvedDistanceFromGoal)
            {
                ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());
                
                // Is there an imminent obstacle?
                boolean steerToGoal = true;
//                SpatialObject obj = ninjaContext.getNinja().getObjectCollection().findNearestChair(ninjaContext.getNinja(), 10.0f, 0.5f);
//                if (obj != null)
//                {
//                    // Steer away from the obstacle
//                    Vector3f rightVec = ninjaContext.getController().getRightVector();
//                    Vector3f myPos    = ninjaContext.getController().getPosition();
//                    Vector3f directionToObstacle = obj.getNearestObstaclePosition(myPos).subtract(myPos);
//                    directionToObstacle.normalizeLocal();
//                    float dot = directionToObstacle.dot(rightVec);
//                    if (dot > directionSensitivity)
//                    {
//                        //ninjaContext.triggerPressed(TriggerNames.Move_Left.ordinal());
//                        //ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
//                        ninjaContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
//                        ninjaContext.triggerPressed(TriggerNames.Move_Back.ordinal());
//                        steerToGoal = false;
//                    }
//                    else if (dot < -directionSensitivity)
//                    {
//                        //ninjaContext.triggerPressed(TriggerNames.Move_Right.ordinal());
//                        //ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
//                        ninjaContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
//                        ninjaContext.triggerPressed(TriggerNames.Move_Back.ordinal());
//                        steerToGoal = false;
//                    }
//                }
                if (steerToGoal)
                {
                    // Steer towards the goal
                    Vector3f rightVec = ninjaContext.getController().getRightVector();
                    Vector3f desiredVelocity = goalPosition.subtract(ninjaContext.getController().getPosition());
                    desiredVelocity.normalizeLocal();
                    float dot = desiredVelocity.dot(rightVec);
                    if (dot == 0.0f)
                        dot++;
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
            Vector3f forwardVec = ninjaContext.getController().getForwardVector();
            float dot = sittingDirection.dot(forwardVec);
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
