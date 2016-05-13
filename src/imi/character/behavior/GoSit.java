/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package imi.character.behavior;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameState;
import imi.character.statemachine.GameStateChangeListener;
import imi.character.statemachine.GameStateChangeListenerRegisterar;
import imi.character.statemachine.corestates.CycleActionState;
import imi.character.statemachine.corestates.SitState;
import imi.objects.SpatialObject;
import imi.objects.TargetObject;
import imi.scene.PMatrix;
import imi.utils.MathUtils;
import java.util.logging.Logger;
//import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;

/**
 * Go to a chair, turn properly and sit down. Avoids other chairs as obstacles.
 *
 * @author Lou Hayt
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class GoSit implements Task {
private static final Logger logger = Logger.getLogger(GoSit.class.getName());
    String description = "Go to the nearest Chair and sit on it";
    String status = "Chilling";

    private AvatarContext context = null;

    private TargetObject goal = null;
    private boolean bDone = false;
    private boolean bTryAgain = false;

    private GoTo go = null;
    private boolean goalReached = false;

    private float currentDistanceFromGoal = 0.0f;
    private Vector3f currentCharacterPosition = new Vector3f();
    private Vector3f goalPosition = new Vector3f();
    private Vector3f goalDirection = new Vector3f();

    private Vector3f goalLieDownPosition = new Vector3f();
    private Quaternion goalLieDownDirection = new Quaternion();
    private Vector3f goalSitPosition = new Vector3f();
    private Quaternion goalSitDirection = new Quaternion();
    private boolean lieDownEnable = false;
    private boolean lieDownImmediately = false;
    private String thisUserId;
    
    private boolean shakeHands = false;
    
    private float pullPower = 5.0f;
    private float approvedDistanceFromGoal = 1.0f;

    public GoSit(TargetObject chair, AvatarContext context
            , boolean lieDownEnable, boolean lieDownImmediately) {
        this.context = context;
        this.goal = chair;
        goalPosition.set(goal.getTargetPositionRef());
        goalDirection.set(goal.getTargetForwardVector());

        go = new GoTo(goalPosition, context);
        go.setApprovedDistanceFromGoal(0.5f);
        go.setGoal(goal);
        this.lieDownEnable = lieDownEnable;
        this.lieDownImmediately = lieDownImmediately;
    }
    //Gosit used while Shake Hand
    public GoSit(TargetObject chair, AvatarContext context, boolean shakeHands) {
        this.context = context;
        this.goal = chair;
        goalPosition.set(goal.getTargetPositionRef());
        goalDirection.set(goal.getTargetForwardVector());
        
        go = new GoTo(goalPosition, context);
        go.setApprovedDistanceFromGoal(1.5f);
        go.setGoal(goal);
        this.shakeHands = shakeHands;
    }
   
    public GoSit(TargetObject chair, AvatarContext context
            , boolean lieDownEnable, boolean lieDownImmediately, String thisUserId) {
        this.context = context;
        this.goal = chair;
        goalPosition.set(goal.getTargetPositionRef());
        goalDirection.set(goal.getTargetForwardVector());

        go = new GoTo(goalPosition, context);
        go.setApprovedDistanceFromGoal(0.5f);
        go.setGoal(goal);
        this.lieDownEnable = lieDownEnable;
        this.lieDownImmediately = lieDownImmediately;
        this.thisUserId = thisUserId;
    }

    public GoSit(TargetObject chair, AvatarContext context
            , boolean lieDownEnable, boolean lieDownImmediately, Vector3f sitPos
            , Quaternion sitDir, Vector3f liePos, Quaternion lieDir, String thisUserId) {
        this.context = context;
        this.goal = chair;
        goalPosition.set(goal.getTargetPositionRef());
        goalDirection.set(goal.getTargetForwardVector());
        if (lieDownEnable) {
            goalLieDownPosition = liePos;
            goalLieDownDirection = lieDir;
        }
        goalSitDirection = sitDir;
        goalSitPosition = sitPos;
        go = new GoTo(goalPosition, context);
        go.setApprovedDistanceFromGoal(0.5f);
        go.setGoal(goal);
        this.lieDownEnable = lieDownEnable;
        this.lieDownImmediately = lieDownImmediately;
        this.thisUserId = thisUserId;
    }

    public boolean verify() {
        if (bDone) {
            return false;
        }

        // If the chair is occupied then try finding another or abort mission
        if ((goal != null && goal.isOccupied() && goal.getOwner() != context.getCharacter()) || bTryAgain) {
            if (!context.goToNearestChair()) {
                logger.info("Chair is Occupied! I give up! Can't find an empty chair in this damn virtual environment!");
            } else {
                logger.info("Chair is Occupied! I WILL find another one!");
            }

            bTryAgain = false;
            status = "chair is occupied";
            return false;
        }
        return true;
    }

    public void update(float deltaTime) {
        if (goalReached) {
            // Update local variables
            Vector3f v3f = context.getController().getPosition();
            currentCharacterPosition.set(v3f.x, goalPosition.y, v3f.z);
            currentDistanceFromGoal = goalPosition.distance(currentCharacterPosition);

            // Pull towards the goal
            PMatrix local = context.getController().getTransform().getLocalMatrix(true);
            Vector3f pull = goalPosition.subtract(currentCharacterPosition).normalize().mult(currentDistanceFromGoal * deltaTime * pullPower);
            local.setTranslation(local.getTranslation().add(pull));

            if (go.verify()) {
                go.update(deltaTime);
            } else {
                triggerRelease(TriggerNames.Move_Right.ordinal());
                triggerRelease(TriggerNames.Move_Left.ordinal());
            }

            if (currentDistanceFromGoal < 0.01f && !context.isTransitioning()) {
                done();
            }
        } else {
            if (go.verify()) {
                go.update(deltaTime);
            } else {
                goalReached = true;
                triggerRelease(TriggerNames.Move_Forward.ordinal());
                go.reset(goalPosition);
                go.setAvoidObstacles(false);
                go.setApprovedDistanceFromGoal(approvedDistanceFromGoal);
                
                // Own the chair if no one else does, otherwise try another
                if (goal.getOwner() == null) {
                    goal.setOwner(context.getavatar());
                } else if (goal.getOwner() != context.getavatar()) {
                    bTryAgain = true;
                    goalReached = false;
                }
            }
        }
    }

    // RED : Added to reduce object creation
    /**
     * Used with math utils to avoid object creation *
     */
    private MathUtils.MathUtilsContext mathContext = MathUtils.getContext();

    private void done() {
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
        if (context.getCurrentState() instanceof CycleActionState) {
            GameStateChangeListenerRegisterar.registerListener(new GameStateChangeListener() {

                public void enterInState(GameState state) {
                }

                public void exitfromState(GameState state) {
                    String stateChangeUserId = context.getCharacter().getCharacterParams().getId();
                    if (thisUserId.equals(stateChangeUserId) && (state instanceof CycleActionState)) {
                        pressTrigger();
                        GameStateChangeListenerRegisterar.deRegisterListener(this);
                    }
                }

                public void changeInState(GameState state, String animName, boolean animFinished, String animType) {
                }
            });
        }
        
        pressTrigger();
        
    }
    
    private void pressTrigger() {
        //set liedown/sit position and trigger press
        SitState sit = (SitState) context.getStateMapping().get(SitState.class);
        if (lieDownEnable) {
            sit.setSittingPosition(goalSitPosition, goalSitDirection);
            sit.setLieDownPosition(goalLieDownPosition, goalLieDownDirection);
            if (lieDownImmediately) {
                logger.info("GoSit : Trigger : LieDown");
                triggerPress(TriggerNames.LieDown.ordinal());
            } else {
                logger.info("GoSit : Trigger : GoSitLieDown");
                sit.setSittingPosition(goalSitPosition, goalSitDirection);
                triggerPress(TriggerNames.GoSitLieDown.ordinal());
            }
        } else {
            //if its for shakeHand then just go there not get Sit.
            if(shakeHands){
                logger.info("GoSit not pressed");
                shakeHands = false;                
            }else{
               logger.info("GoSit : Trigger : GoSit");
               triggerPress(TriggerNames.GoSit.ordinal());  
            }
        }
    }
    private void triggerPress(int trigger) {
        if (!context.getTriggerState().isKeyPressed(trigger)) {
            context.triggerPressed(trigger);
        }
    }

    private void triggerRelease(int trigger) {
        if (context.getTriggerState().isKeyPressed(trigger)) {
            context.triggerReleased(trigger);
        }

    }

    public String getDescription() {
        return description;
    }

    public void onHold() {
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
