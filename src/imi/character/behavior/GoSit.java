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
package imi.character.behavior;

import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.objects.SpatialObject;
import imi.objects.TargetObject;
import imi.character.statemachine.corestates.SitState;
import imi.scene.PMatrix;
import imi.utils.MathUtils;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *  Go to a chair, turn properly and sit down.
 *  Avoids other chairs as obstacles.
 * 
 * @author Lou Hayt
 */
@ExperimentalAPI
public class GoSit implements Task
{
    String  description  = "Go to the nearest Chair and sit on it";
    String  status = "Chilling";
    
    private AvatarContext context = null;
    
    private TargetObject goal = null;
    private boolean bDone = false;
    private boolean  bTryAgain = false;
    
    private GoTo go = null;
    private boolean goalReached = false;
    
    private float       currentDistanceFromGoal  = 0.0f;
    private Vector3f    currentCharacterPosition = new Vector3f();
    private Vector3f    goalPosition  = new Vector3f();
    private Vector3f    goalDirection = new Vector3f();
    
    private float pullPower = 5.0f;

    private float approvedDistanceFromGoal = 1.0f;

    public GoSit(TargetObject chair, AvatarContext context)
    {
        this.context = context;
        this.goal = chair;
        goalPosition.set(goal.getTargetPositionRef());
        goalDirection.set(goal.getTargetForwardVector());
        go = new GoTo(goalPosition, context);
        go.setGoal(goal);
    }
    
    public boolean verify() 
    {
        if (bDone)
            return false;
        
        // If the chair is occupied then try finding another or abort mission
        if ( (goal != null && goal.isOccupied() && goal.getOwner() != context.getCharacter()) || bTryAgain)
        {
            if (!context.goToNearestChair())
                System.out.println("Chair is Occupied! I give up! Can't find an empty chair in this damn virtual environment!");
            else
                System.out.println("Chair is Occupied! I WILL find another one!");
            
            bTryAgain = false;
            status = "chair is occupied";
            return false;
        }
        return true;
    }
    
    public void update(float deltaTime) 
    {
        if (goalReached)
        {
            // Update local variables
            currentCharacterPosition.set(context.getController().getPosition());
            currentDistanceFromGoal = goalPosition.distance(currentCharacterPosition);

            // Pull towards the goal
            PMatrix local = context.getController().getTransform().getLocalMatrix(true);
            Vector3f pull = goalPosition.subtract(currentCharacterPosition).normalize().mult(currentDistanceFromGoal * deltaTime * pullPower);
            local.setTranslation(local.getTranslation().add(pull)); 
         
            if (go.verify())
                go.update(deltaTime);   
            else
            {
                triggerRelease(TriggerNames.Move_Right.ordinal());
                triggerRelease(TriggerNames.Move_Left.ordinal());
            }
                
            if (currentDistanceFromGoal < 0.01f && !context.isTransitioning())
                done();
        }
        else
        {
            if (go.verify())
                go.update(deltaTime);   
            else
            {
                goalReached = true;
                triggerRelease(TriggerNames.Move_Forward.ordinal());
                go.reset(goalPosition, goalDirection);
                go.setAvoidObstacles(false);
                go.setApprovedDistanceFromGoal(approvedDistanceFromGoal);
                //go.setApprovedDistanceFromGoal(goal.getBoundingSphere().getRadius());
//                System.out.println("GoSit() goal bounding volume radius: " + goal.getBoundingSphere().getRadius());
//                System.out.println("GoSit() goal bounding volume center: " + goal.getBoundingSphere().getCenter());
                
                // Own the chair if no one else does, otherwise try another
                if (goal.getOwner() == null)
                    goal.setOwner(context.getavatar());
                else if (goal.getOwner() != context.getavatar())
                {
                    bTryAgain   = true;
                    goalReached = false;
                }
            }
        }
    }

    // RED : Added to reduce object creation
    /** Used with math utils to avoid object creation **/
    private MathUtils.MathUtilsContext mathContext = MathUtils.getContext();
    private void done()
    {
        bDone = true;
        
        PMatrix localMat = context.getController().getTransform().getLocalMatrix(true);
        PMatrix look = new PMatrix();
        // RED : Added to reduce object creation
        MathUtils.lookAt(goalPosition.add(goalDirection.mult(-1.0f)),
                            goalPosition,
                            Vector3f.UNIT_Y,
                            look,
                            mathContext);
        localMat.set(look);
        
        // Initiate SitState
        SitState sit = (SitState) context.getStateMapping().get(SitState.class);
        if (sit != null && sit.toSit(null))
            context.setCurrentState(sit);
    }
    
    private void triggerRelease(int trigger)
    {
        if (context.getTriggerState().isKeyPressed(trigger))
            context.triggerReleased(trigger);    
    }
    
    public String getDescription() {
        return description;
    }

    public void onHold() 
    {
        status = "On hold";
    }

    public String getStatus() {
        return status;
    }

    public SpatialObject getGoal() {
        return goal;
    }

    public float getApprovedDistanceFromGoal() {
        return approvedDistanceFromGoal;
    }

    public void setApprovedDistanceFromGoal(float approvedDistanceFromGoal) {
        this.approvedDistanceFromGoal = approvedDistanceFromGoal;
    }

}