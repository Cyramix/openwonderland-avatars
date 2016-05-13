/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
package imi.character.avatar;

import imi.character.statemachine.corestates.WalkState;
import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.corestates.SitState;
import imi.character.statemachine.corestates.SitOnGroundState;
import imi.character.statemachine.corestates.IdleState;
import imi.character.statemachine.corestates.FlyState;
import imi.character.statemachine.corestates.FallFromSitState;
import imi.character.CharacterController;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.statemachine.corestates.transitions.FlyToIdle;
import imi.character.statemachine.corestates.transitions.IdleToFly;
import imi.character.statemachine.corestates.transitions.IdleToAction;
import imi.character.statemachine.corestates.transitions.IdleToSitOnGround;
import imi.character.statemachine.corestates.transitions.IdleToTurn;
import imi.character.statemachine.corestates.transitions.IdleToWalk;
import imi.character.statemachine.corestates.transitions.ActionToIdle;
import imi.character.statemachine.corestates.transitions.ActionToTurn;
import imi.character.statemachine.corestates.transitions.ActionToWalk;
import imi.character.statemachine.corestates.transitions.SitOnGroundToIdle;
import imi.character.statemachine.corestates.transitions.SitToIdle;
import imi.character.statemachine.corestates.transitions.TurnToIdle;
import imi.character.statemachine.corestates.transitions.TurnToAction;
import imi.character.statemachine.corestates.transitions.TurnToWalk;
import imi.character.statemachine.corestates.transitions.WalkToIdle;
import imi.character.statemachine.corestates.transitions.WalkToAction;
import imi.objects.ChairObject;
import imi.objects.LocationNode;
import imi.objects.SpatialObject;
import imi.objects.TargetObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState.Action;
import imi.character.statemachine.corestates.ActionInfo;
import imi.character.statemachine.corestates.ActionState;
import imi.character.statemachine.corestates.CycleActionInfo;
import imi.character.statemachine.corestates.CycleActionState;
import imi.character.statemachine.corestates.RunState;
import imi.character.statemachine.corestates.StrafeState;
import imi.character.statemachine.corestates.transitions.ActionToStrafe;
import imi.character.statemachine.corestates.transitions.IdleToSit;
import imi.character.statemachine.corestates.transitions.IdleToStrafe;
import imi.character.statemachine.corestates.transitions.RunToWalk;
import imi.character.statemachine.corestates.transitions.StrafeToAction;
import imi.character.statemachine.corestates.transitions.StrafeToIdle;
import imi.character.statemachine.corestates.transitions.TurnToStrafe;
import imi.character.statemachine.corestates.transitions.WalkToFly;
import imi.character.statemachine.corestates.transitions.WalkToRun;
import imi.character.behavior.FollowBakedPath;
import imi.character.behavior.GoSit;
import imi.character.behavior.GoTo;
import imi.character.statemachine.corestates.FallState;
import imi.character.statemachine.corestates.transitions.ActionToSit;
import imi.character.statemachine.corestates.transitions.FallToFly;
import imi.character.statemachine.corestates.transitions.FallToIdle;
import imi.character.statemachine.corestates.transitions.FallToWalk;
import imi.character.statemachine.corestates.transitions.FlyToFall;
import imi.character.statemachine.corestates.transitions.IdleToFall;
import imi.character.statemachine.corestates.transitions.SitToAction;
import imi.character.statemachine.corestates.transitions.WalkToFall;
import imi.objects.AvatarObjectCollection;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastTable;

/**
 * This is a concrete GameContext.
 *
 * @author Lou Hayt
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class AvatarContext extends GameContext {

    /**
     * The relevant avatar *
     */
    private Avatar avatar = null;
    /**
     * Controller for the above *
     */
    private AvatarController controller = null;
    /**
     * The steering controller for the avatar *
     */
    private CharacterBehaviorManager behavior = new CharacterBehaviorManager("Avatar Behavior", this);
    /**
     * Current location node, if any. *
     */
    private LocationNode location = null;
    /**
     * Animations that are using the ActionState to play out (such as wave,
     * cheer etc) *
     */
    private FastTable<ActionInfo> genericAnimations = new FastTable<ActionInfo>();
    /**
     * Used to queue up several generic animations *
     */
    private FastList<Integer> genericAnimationsQueue = new FastList<Integer>();
    /**
     * Used for cycling through action animations *
     */
    private int genericActionIndex = 0;
    /**
     * Map of generic animation API names to indecis  *
     */
    private HashMap<String, Integer> genericAnimationNames = new HashMap<String, Integer>();
   private static final Logger logger = Logger.getLogger(AvatarContext.class.getName());
    /**
     * The names of the triggers.
     * Kindly add your trigger at last so others' index dont change.
     */
    public static enum TriggerNames {

        Movement_Modifier,//0
        Move_Left,//1
        Move_Right,//2
        Move_Forward,//3
        Move_Back,//4
        Move_Up,//5
        Move_Down,//6
        MiscAction,//7
        SitOnGround,//8
        ToggleBehavior,//9
        ToggleRightArm,//10
        ToggleLeftArm,//11
        ToggleRightArmManualDriveReachMode,//12
        ToggleLeftArmManualDriveReachMode,//13
        Point,//14
        GoSit,//15
        GoSitLieDown,//16
        LieDown,//17
        LieDownOnClick,//18
        GoTo1,//19
        GoTo2,//20
        GoTo3,//21
        NextAction,//22
        Reverse,//23
        Smile,//24
        Frown,//25
        Scorn,//26
        Move_Strafe_Left,//27
        Move_Strafe_Right,//28
        Idle,//29
        MiscActionInSitting,//30
    }

    /**
     * The names of the actions.
     */
    public static enum ActionNames {

        Movement_Rotate_Y,
        Movement_X,
        Movement_Y,
        Movement_Z,
        Action,
    }

    /**
     * Construct a new instance with the provided avatar
     *
     * @param theAvatar
     */
    public AvatarContext(Avatar theAvatar) {
        super(theAvatar);
        avatar = theAvatar;
        controller = (AvatarController) instantiateController();
        actions = new float[ActionNames.values().length];

        // Add states to this context
        gameStates.put(IdleState.class, new IdleState(this));
        gameStates.put(WalkState.class, new WalkState(this));
        gameStates.put(StrafeState.class, new StrafeState(this));
        gameStates.put(TurnState.class, new TurnState(this));
        gameStates.put(CycleActionState.class, new CycleActionState(this));
        gameStates.put(SitState.class, new SitState(this));
        gameStates.put(FlyState.class, new FlyState(this));
        gameStates.put(FallState.class, new FallState(this));
        gameStates.put(FallFromSitState.class, new FallFromSitState(this));
        gameStates.put(SitOnGroundState.class, new SitOnGroundState(this));
        gameStates.put(RunState.class, new RunState(this));
        gameStates.put(SitState.class, new SitState(this));

        // Set the state to start with
        setCurrentState(gameStates.get(IdleState.class));

        // Register validation methods (entry points)
        registerStateEntryPoint(gameStates.get(IdleState.class), "toIdle");
        registerStateEntryPoint(gameStates.get(WalkState.class), "toWalk");
        registerStateEntryPoint(gameStates.get(StrafeState.class), "toSideStep");
        registerStateEntryPoint(gameStates.get(TurnState.class), "toTurn");
        registerStateEntryPoint(gameStates.get(CycleActionState.class), "toAction");
        registerStateEntryPoint(gameStates.get(FlyState.class), "toFly");
        registerStateEntryPoint(gameStates.get(FallState.class), "toFall");
        registerStateEntryPoint(gameStates.get(SitOnGroundState.class), "toSitOnGround");
        registerStateEntryPoint(gameStates.get(RunState.class), "toRun");
        registerStateEntryPoint(gameStates.get(SitState.class), "toSit");

        // Add transitions (exit points)
        //Add two transitions
        gameStates.get(IdleState.class).addTransition(new IdleToTurn());
        gameStates.get(IdleState.class).addTransition(new IdleToWalk());
        gameStates.get(IdleState.class).addTransition(new IdleToStrafe());
        gameStates.get(IdleState.class).addTransition(new IdleToAction());
        gameStates.get(IdleState.class).addTransition(new IdleToFly());
        gameStates.get(IdleState.class).addTransition(new IdleToFall());
        gameStates.get(IdleState.class).addTransition(new IdleToSitOnGround());
        gameStates.get(IdleState.class).addTransition(new IdleToSit());
        gameStates.get(WalkState.class).addTransition(new WalkToIdle());
        gameStates.get(WalkState.class).addTransition(new WalkToAction());
        gameStates.get(WalkState.class).addTransition(new WalkToRun());
        gameStates.get(WalkState.class).addTransition(new WalkToFly());
        gameStates.get(WalkState.class).addTransition(new WalkToFall());
        gameStates.get(TurnState.class).addTransition(new TurnToIdle());
        gameStates.get(TurnState.class).addTransition(new TurnToWalk());
        gameStates.get(TurnState.class).addTransition(new TurnToAction());
        gameStates.get(TurnState.class).addTransition(new TurnToStrafe());
        gameStates.get(CycleActionState.class).addTransition(new ActionToWalk());
        gameStates.get(CycleActionState.class).addTransition(new ActionToTurn());
        gameStates.get(CycleActionState.class).addTransition(new ActionToIdle());
        gameStates.get(CycleActionState.class).addTransition(new ActionToSit());
        gameStates.get(CycleActionState.class).addTransition(new ActionToStrafe());
        gameStates.get(SitState.class).addTransition(new SitToIdle());
        gameStates.get(SitState.class).addTransition(new SitToAction());
        gameStates.get(FlyState.class).addTransition(new FlyToIdle());
        gameStates.get(FlyState.class).addTransition(new FlyToFall());
        gameStates.get(FallState.class).addTransition(new FallToIdle());
        gameStates.get(FallState.class).addTransition(new FallToFly());
        gameStates.get(FallState.class).addTransition(new FallToWalk());
        gameStates.get(FallFromSitState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(SitOnGroundState.class).addTransition(new SitOnGroundToIdle());
        gameStates.get(RunState.class).addTransition(new RunToWalk());
        gameStates.get(StrafeState.class).addTransition(new StrafeToIdle());
        gameStates.get(StrafeState.class).addTransition(new StrafeToAction());

        // Set default info for animations utilizing the ActionState
        configureDefaultActionStateInfo();
        if (!genericAnimations.isEmpty()) {
            genericAnimations.get(0).apply((CycleActionState) gameStates.get(CycleActionState.class));
        }
    }

    /**
     * Performs the default mapping of actions to triggers
     *
     * @param actionMap
     */
    @Override
    public void initDefaultActionMap(Hashtable<Integer, Action> actionMap) {
        actionMap.put(TriggerNames.Move_Left.ordinal(), new Action(AvatarContext.ActionNames.Movement_Rotate_Y.ordinal(), -0.1f));
        actionMap.put(TriggerNames.Move_Right.ordinal(), new Action(AvatarContext.ActionNames.Movement_Rotate_Y.ordinal(), 0.1f));
        actionMap.put(TriggerNames.Move_Strafe_Left.ordinal(), new Action(AvatarContext.ActionNames.Movement_X.ordinal(), -0.4f));
        actionMap.put(TriggerNames.Move_Strafe_Right.ordinal(), new Action(AvatarContext.ActionNames.Movement_X.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Forward.ordinal(), new Action(AvatarContext.ActionNames.Movement_Z.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Back.ordinal(), new Action(AvatarContext.ActionNames.Movement_Z.ordinal(), -0.4f));
        actionMap.put(TriggerNames.MiscAction.ordinal(), new Action(AvatarContext.ActionNames.Action.ordinal(), 1.0f));
        actionMap.put(TriggerNames.MiscActionInSitting.ordinal(), new Action(AvatarContext.ActionNames.Action.ordinal(), 1.0f));
        actionMap.put(TriggerNames.Move_Up.ordinal(), new Action(AvatarContext.ActionNames.Movement_Y.ordinal(), 0.4f));
        actionMap.put(TriggerNames.Move_Down.ordinal(), new Action(AvatarContext.ActionNames.Movement_Y.ordinal(), -0.4f));
    }

    /**
     * Update the context
     *
     * @param deltaTime The timestep
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        behavior.update(deltaTime);
        controller.update(deltaTime);
        if (!(currentState instanceof CycleActionState) && !genericAnimationsQueue.isEmpty()) {
            performAction(genericAnimationsQueue.removeFirst());
            //System.out.println("[AvatarContext.update()] pop from queue");
        }
    }

    /**
     * Received when the state of a trigger changes
     *
     * @param trigger
     * @param pressed
     */
    @Override
    protected void triggerAlert(int trigger, boolean pressed) {
        // only make changes on press
        if (!pressed) {
            return;
        }

        switch (TriggerNames.values()[trigger]) {

            case MiscAction:
                // Force the action if the action button is pressed
                setCurrentState((ActionState) gameStates.get(CycleActionState.class));
                break;

            case Idle:
                // Force idle state
                setCurrentStateByType(IdleState.class);
                break;

            case ToggleBehavior:
                behavior.toggleEnable();
                break;

            case ToggleRightArm:
                avatar.setCameraOnMe();
                if (avatar.getRightArm() != null) {
                    avatar.getRightArm().toggleEnabled();
                }
                break;

            case ToggleLeftArm:
                avatar.setCameraOnMe();
                if (avatar.getLeftArm() != null) {
                    avatar.getLeftArm().toggleEnabled();
                }
                break;

            case ToggleLeftArmManualDriveReachMode:
                if (avatar.getLeftArm() != null) {
                    avatar.getLeftArm().toggleManualDriveReachUp();
                }
                break;

            case ToggleRightArmManualDriveReachMode:
                if (avatar.getRightArm() != null) {
                    avatar.getRightArm().toggleManualDriveReachUp();
                }
                break;

            case Point:
                if (avatar.getObjectCollection() == null || avatar.getRightArm() == null) {
                    return;
                }

                SpatialObject obj = avatar.getObjectCollection().findNearestObjectOfType(ChairObject.class, avatar, 10000.0f, 1.0f, true);
                if (obj == null) {
                    return;
                }

                avatar.getRightArm().setPointAtLocation(obj.getPositionRef());
                break;

            case GoSit:
                break;

            case GoTo1:
                behavior.clearTasks();
                goToNearestLocation();
                if (location != null) {
                    behavior.addTaskToBottom(new FollowBakedPath("yellowRoom", location, this));
                }
                break;

            case GoTo2:
                behavior.clearTasks();
                goToNearestLocation();
                if (location != null) {
                    behavior.addTaskToBottom(new FollowBakedPath("lobbyCenter", location, this));
                }
                break;

            case GoTo3:
                logger.info("Sit on the nearest chair");
                behavior.clearTasks();
                goToNearestChair();
                break;

            // Facial expressions
            case Smile:
                avatar.initiateFacialAnimation(1, 0.2f, 1.0f);
                break;

            case Frown:
                avatar.initiateFacialAnimation(2, 0.2f, 1.5f);
                break;

            case Scorn:
                avatar.initiateFacialAnimation(3, 0.2f, 1.5f);
                break;

            case Reverse:
                CycleActionState punch = (CycleActionState) gameStates.get(CycleActionState.class);
                punch.setTransitionReverseAnimation(!punch.isTransitionReverseAnimation());
                break;

            case NextAction:
                CycleActionState action = (CycleActionState) gameStates.get(CycleActionState.class);
                action.setAnimationSetBoolean(false);

                genericActionIndex++;
                if (genericActionIndex >= genericAnimations.size()) {
                    genericActionIndex = 0;
                }

                genericAnimations.get(genericActionIndex).apply(action);
                break;
        }
    }

    public Avatar getavatar() {
        return avatar;
    }

    public void setavatar(Avatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public AvatarController getController() {
        return controller;
    }

    @Override
    protected CharacterController instantiateController() {
        return new AvatarController(avatar);
    }

    @Override
    public CharacterBehaviorManager getBehaviorManager() {
        return behavior;
    }

    /**
     * Find the nearest location node and direct the avatar to go there.
     *
     * @return The nearest location node, or null if none are found
     */
    public LocationNode goToNearestLocation() {
        if (avatar.getObjectCollection() == null) {
            return null;
        }

        location = (LocationNode) avatar.getObjectCollection().findNearestObjectOfType(LocationNode.class, avatar, 10000.0f, 1.0f, false);
        if (location != null) {
            behavior.addTaskToTop(new GoTo(location.getPositionRef(), this));
            behavior.setEnable(true);
        }
        return location;
    }

    /**
     * Find the nearest unoccupied chair and direct the avatar to go to it.
     *
     * @return True if an unoccupied chair was found, false otherwise
     */
    public boolean goToNearestChair() {
        AvatarObjectCollection objects = (AvatarObjectCollection) avatar.getWorldManager().getUserData(AvatarObjectCollection.class);
        if (objects != null) {
            avatar.setObjectCollection(objects);
        }

        if (avatar.getObjectCollection() == null) {
            logger.info("Will not go to the nearest chair because the object collection is null " + getCharacter());
            return false;
        }

        SpatialObject obj = avatar.getObjectCollection().findNearestObjectOfType(ChairObject.class, avatar, 10000.0f, 1.0f, true);
        if (obj != null && !((TargetObject) obj).isOccupied()) {
            GoSit task = new GoSit((TargetObject) obj, this, false, false);
            behavior.addTaskToTop(task);
            behavior.setEnable(true);
            return true;
        }
        return false;
    }

    public boolean goToTarget(TargetObject target, boolean occupiedMatters, boolean abandonCurrentTasks) {
        if (abandonCurrentTasks) {
            behavior.clearTasks();
        }

        if (avatar.getObjectCollection() == null) {
            return false;
        }

        if (target != null && !target.isOccupied(occupiedMatters)) {
            GoTo task = new GoTo(target, this);
            behavior.addTaskToTop(task);
            behavior.setEnable(true);
            return true;
        }
        return false;
    }

    public boolean goSitOnChair(TargetObject chair, boolean occupiedMatters, boolean abandonCurrentTasks) {
        if (abandonCurrentTasks) {
            behavior.clearTasks();
        }

        if (avatar.getObjectCollection() == null) {
            return false;
        }

        if (chair != null && !chair.isOccupied(occupiedMatters)) {
            GoSit task = new GoSit(chair, this, false, false);
            behavior.addTaskToTop(task);
            behavior.setEnable(true);
            return true;
        }
        return false;
    }

    public LocationNode getLocation() {
        return location;
    }

    public void setLocation(LocationNode location) {
        this.location = location;
    }

    /**
     * Perform the action associated with the provided index.
     *
     * @param actionInfoIndex
     */
    public void performAction(int actionInfoIndex) {
        if (currentState instanceof CycleActionState) {
            // que it up
            genericAnimationsQueue.add(actionInfoIndex);
            //System.out.println("queu up action " + actionInfoIndex);
        } else {
            //System.out.println("perform action " + actionInfoIndex);
            CycleActionState action = (CycleActionState) gameStates.get(CycleActionState.class);
            genericAnimations.get(actionInfoIndex).apply(action);
            setCurrentState(action);
        }
    }

    public FastTable<ActionInfo> getGenericAnimations() {
        return genericAnimations;
    }

    /**
     * Get the index for performAction() by name
     *
     * @param name
     * @return
     */
    public int getGenericAnimationIndex(String name) {
        Integer inte = genericAnimationNames.get(name);
        if (inte == null) {
            return -1;
        }
        return inte.intValue();
    }

    /**
     * Here we define the animation properties for the various animations that
     * are using the ActionState to play out *
     */
    private void configureDefaultActionStateInfo() {
        ActionInfo info;
        CycleActionInfo cycleInfo;

        /**
         * Note: There are many more settings possible to set! *
         */
        int index = 0;
        if (avatar.getCharacterParams().isMale()) {
            //------------------
            //head gesture
            cycleInfo = new CycleActionInfo("Male_No", "Male_No", "Male_No");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("No", index++);

            cycleInfo = new CycleActionInfo("Male_Yes", "Male_Yes", "Male_Yes");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Yes", index++);
            
            cycleInfo = new CycleActionInfo("Male_Wink","Male_Wink","Male_Wink");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Wink", index++);
            
            //torso gesture
            cycleInfo = new CycleActionInfo("Male_AnswerCellStanding", "Male_AnswerCellStandingIdle", "Male_AnswerCellStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("AnswerCellStanding", index++);

            cycleInfo = new CycleActionInfo("Male_AnswerCellSitting", "Male_AnswerCellSittingIdle", "Male_AnswerCellSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("AnswerCellSitting", index++);

            cycleInfo = new CycleActionInfo("Male_Bow", "Male_Bow", "Male_Bow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Bow", index++);

            cycleInfo = new CycleActionInfo("Male_BreathingSittingLow", "Male_BreathingSittingLow", "Male_BreathingSittingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingSittingLow", index++);

            cycleInfo = new CycleActionInfo("Male_BreathingSittingMedium", "Male_BreathingSittingMedium", "Male_BreathingSittingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingSittingMedium", index++);

            cycleInfo = new CycleActionInfo("Male_BreathingSittingHigh", "Male_BreathingSittingHigh", "Male_BreathingSittingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingSittingHigh", index++);

            cycleInfo = new CycleActionInfo("Male_BreathingStandingLow", "Male_BreathingStandingLow", "Male_BreathingStandingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingStandingLow", index++);

            cycleInfo = new CycleActionInfo("Male_BreathingStandingMedium", "Male_BreathingStandingMedium", "Male_BreathingStandingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingStandingMedium", index++);

            cycleInfo = new CycleActionInfo("Male_BreathingStandingHigh", "Male_BreathingStandingHigh", "Male_BreathingStandingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingStandingHigh", index++);

            cycleInfo = new CycleActionInfo("Male_CheerStanding", "Male_CheerStandingIdle", "Male_CheerStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CheerStanding", index++);

            cycleInfo = new CycleActionInfo("Male_CheerSitting", "Male_CheerSittingIdle", "Male_CheerSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CheerSitting", index++);

            cycleInfo = new CycleActionInfo("Male_ClapStanding", "Male_ClapStandingIdle", "Male_ClapStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("ClapStanding", index++);

            cycleInfo = new CycleActionInfo("Male_ClapSitting", "Male_ClapSittingIdle", "Male_ClapSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("ClapSitting", index++);

            cycleInfo = new CycleActionInfo("Male_CrossHands", "Male_CrossHandsIdle", "Male_CrossHands");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CrossHands", index++);

            cycleInfo = new CycleActionInfo("Male_CryingStandingLow", "Male_CryingStandingLowIdle", "Male_CryingStandingLow");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingStandingLow", index++);

            cycleInfo = new CycleActionInfo("Male_CryingStandingMedium", "Male_CryingStandingMediumIdle", "Male_CryingStandingMedium");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingStandingMedium", index++);

            cycleInfo = new CycleActionInfo("Male_CryingStandingHigh", "Male_CryingStandingHighIdle", "Male_CryingStandingHigh");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingStandingHigh", index++);

            cycleInfo = new CycleActionInfo("Male_CryingSittingLow", "Male_CryingSittingLowIdle", "Male_CryingSittingLow");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingSittingLow", index++);

            cycleInfo = new CycleActionInfo("Male_CryingSittingMedium", "Male_CryingSittingMediumIdle", "Male_CryingSittingMedium");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingSittingMedium", index++);

            cycleInfo = new CycleActionInfo("Male_CryingSittingHigh", "Male_CryingSittingHighIdle", "Male_CryingSittingHigh");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingSittingHigh", index++);

            cycleInfo = new CycleActionInfo("Male_FoldArmsSitting", "Male_FoldArmsSittingIdle", "Male_FoldArmsSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("FoldArmsSitting", index++);

            cycleInfo = new CycleActionInfo("Male_FoldArmsStanding", "Male_FoldArmsStandingIdle", "Male_FoldArmsStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("FoldArmsStanding", index++);

            cycleInfo = new CycleActionInfo("Male_Follow", "Male_Follow", "Male_Follow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Follow", index++);
            
            cycleInfo = new CycleActionInfo("Male_PublicSpeakingStanding", "Male_PublicSpeakingStandingIdle", "Male_PublicSpeakingStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("PublicSpeakingStanding", index++);

            cycleInfo = new CycleActionInfo("Male_PublicSpeakingSitting", "Male_PublicSpeakingSittingIdle", "Male_PublicSpeakingSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("PublicSpeakingSitting", index++);


            cycleInfo = new CycleActionInfo("Male_GesticulatingStandingLow", "Male_GesticulatingStandingLowIdle", "Male_GesticulatingStandingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingStandingLow", index++);

            cycleInfo = new CycleActionInfo("Male_GesticulatingStandingMedium", "Male_GesticulatingStandingMediumIdle", "Male_GesticulatingStandingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingStandingMedium", index++);

            cycleInfo = new CycleActionInfo("Male_GesticulatingStandingHigh", "Male_GesticulatingStandingHighIdle", "Male_GesticulatingStandingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingStandingHigh", index++);

            cycleInfo = new CycleActionInfo("Male_GesticulatingSittingLow", "Male_GesticulatingSittingLowIdle", "Male_GesticulatingSittingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingSittingLow", index++);

            cycleInfo = new CycleActionInfo("Male_GesticulatingSittingMedium", "Male_GesticulatingSittingMediumIdle", "Male_GesticulatingSittingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingSittingMedium", index++);

            cycleInfo = new CycleActionInfo("Male_GesticulatingSittingHigh", "Male_GesticulatingSittingHighIdle", "Male_GesticulatingSittingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingSittingHigh", index++);

            cycleInfo = new CycleActionInfo("Male_HunchStanding", "Male_HunchStandingIdle", "Male_HunchStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("HunchStanding", index++);

            cycleInfo = new CycleActionInfo("Male_HunchSitting", "Male_HunchSittingIdle", "Male_HunchSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("HunchSitting", index++);

            cycleInfo = new CycleActionInfo("Male_LaughStanding", "Male_LaughStandingIdle", "Male_LaughStanding");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("LaughStanding", index++);

            cycleInfo = new CycleActionInfo("Male_LaughSitting", "Male_LaughSittingIdle", "Male_LaughSitting");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("LaughSitting", index++);

            cycleInfo = new CycleActionInfo("Male_RaiseHandStanding", "Male_RaiseHandStandingIdle", "Male_RaiseHandStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("RaiseHandStanding", index++);

            cycleInfo = new CycleActionInfo("Male_RaiseHandSitting", "Male_RaiseHandSittingIdle", "Male_RaiseHandSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("RaiseHandSitting", index++);

            cycleInfo = new CycleActionInfo("Male_WaveStanding", "Male_WaveStandingIdle", "Male_WaveStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("WaveStanding", index++);

            cycleInfo = new CycleActionInfo("Male_WaveSitting", "Male_WaveSittingIdle", "Male_WaveSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("WaveSitting", index++);

            cycleInfo = new CycleActionInfo("Male_CrossAnkles", "Male_CrossAnklesIdle", "Male_CrossAnkles");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CrossAnkles", index++);

            cycleInfo = new CycleActionInfo("Male_CrossLegs", "Male_CrossLegsIdle", "Male_CrossLegs");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CrossLegs", index++);
            
            //-----------
            info = new ActionInfo("Male_TakeDamage");
            genericAnimations.add(info);
            genericAnimationNames.put("TakeDamage", index++);

            info = new ActionInfo("Male_ShakeHands");
            genericAnimations.add(info);
            genericAnimationNames.put("ShakeHands", index++);
            
            info = new ActionInfo("Male_TouchArmStanding");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchArmStanding", index++);
            
            info = new ActionInfo("Male_TouchArmSitting");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchArmSitting", index++);
            
            info = new ActionInfo("Male_TouchShoulderStanding");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchShoulderStanding", index++);
            
            info = new ActionInfo("Male_TouchShoulderSitting");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchShoulderSitting", index++);
            
        } else // female
        {
            cycleInfo = new CycleActionInfo("Female_No", "Female_No", "Female_No");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("No", index++);

            cycleInfo = new CycleActionInfo("Female_Yes", "Female_Yes", "Female_Yes");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Yes", index++);

            cycleInfo = new CycleActionInfo("Female_Wink","Female_Wink","Female_Wink");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Wink", index++);
            
            //torso gesture
            cycleInfo = new CycleActionInfo("Female_AnswerCellStanding", "Female_AnswerCellStandingIdle", "Female_AnswerCellStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("AnswerCellStanding", index++);

            cycleInfo = new CycleActionInfo("Female_AnswerCellSitting", "Female_AnswerCellSittingIdle", "Female_AnswerCellSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("AnswerCellSitting", index++);

            cycleInfo = new CycleActionInfo("Female_Bow", "Female_Bow", "Female_Bow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Bow", index++);

            cycleInfo = new CycleActionInfo("Female_BreathingSittingLow", "Female_BreathingSittingLow", "Female_BreathingSittingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingSittingLow", index++);

            cycleInfo = new CycleActionInfo("Female_BreathingSittingMedium", "Female_BreathingSittingMedium", "Female_BreathingSittingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingSittingMedium", index++);

            cycleInfo = new CycleActionInfo("Female_BreathingSittingHigh", "Female_BreathingSittingHigh", "Female_BreathingSittingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingSittingHigh", index++);

            cycleInfo = new CycleActionInfo("Female_BreathingStandingLow", "Female_BreathingStandingLow", "Female_BreathingStandingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingStandingLow", index++);

            cycleInfo = new CycleActionInfo("Female_BreathingStandingMedium", "Female_BreathingStandingMedium", "Female_BreathingStandingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingStandingMedium", index++);

            cycleInfo = new CycleActionInfo("Female_BreathingStandingHigh", "Female_BreathingStandingHigh", "Female_BreathingStandingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("BreathingStandingHigh", index++);

            cycleInfo = new CycleActionInfo("Female_CheerStanding", "Female_CheerStandingIdle", "Female_CheerStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CheerStanding", index++);

            cycleInfo = new CycleActionInfo("Female_CheerSitting", "Female_CheerSittingIdle", "Female_CheerSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CheerSitting", index++);

            cycleInfo = new CycleActionInfo("Female_ClapStanding", "Female_ClapStandingIdle", "Female_ClapStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("ClapStanding", index++);

            cycleInfo = new CycleActionInfo("Female_ClapSitting", "Female_ClapSittingIdle", "Female_ClapSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("ClapSitting", index++);

            cycleInfo = new CycleActionInfo("Female_CrossHands", "Female_CrossHandsIdle", "Female_CrossHands");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CrossHands", index++);

            cycleInfo = new CycleActionInfo("Female_CryingStandingLow", "Female_CryingStandingLowIdle", "Female_CryingStandingLow");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingStandingLow", index++);

            cycleInfo = new CycleActionInfo("Female_CryingStandingMedium", "Female_CryingStandingMediumIdle", "Female_CryingStandingMedium");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingStandingMedium", index++);

            cycleInfo = new CycleActionInfo("Female_CryingStandingHigh", "Female_CryingStandingHighIdle", "Female_CryingStandingHigh");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingStandingHigh", index++);

            cycleInfo = new CycleActionInfo("Female_CryingSittingLow", "Female_CryingSittingLowIdle", "Female_CryingSittingLow");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingSittingLow", index++);

            cycleInfo = new CycleActionInfo("Female_CryingSittingMedium", "Female_CryingSittingMediumIdle", "Female_CryingSittingMedium");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingSittingMedium", index++);

            cycleInfo = new CycleActionInfo("Female_CryingSittingHigh", "Female_CryingSittingHighIdle", "Female_CryingSittingHigh");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CryingSittingHigh", index++);

            cycleInfo = new CycleActionInfo("Female_FoldArmsSitting", "Female_FoldArmsSittingIdle", "Female_FoldArmsSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("FoldArmsSitting", index++);

            cycleInfo = new CycleActionInfo("Female_FoldArmsStanding", "Female_FoldArmsStandingIdle", "Female_FoldArmsStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("FoldArmsStanding", index++);

            cycleInfo = new CycleActionInfo("Female_Follow", "Female_Follow", "Female_Follow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("Follow", index++);
            
            cycleInfo = new CycleActionInfo("Female_PublicSpeakingStanding", "Female_PublicSpeakingStandingIdle", "Female_PublicSpeakingStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("PublicSpeakingStanding", index++);
            
            cycleInfo = new CycleActionInfo("Female_PublicSpeakingSitting", "Female_PublicSpeakingSittingIdle", "Female_PublicSpeakingSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("PublicSpeakingSitting", index++);

            cycleInfo = new CycleActionInfo("Female_GesticulatingStandingLow", "Female_GesticulatingStandingLowIdle", "Female_GesticulatingStandingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingStandingLow", index++);

            cycleInfo = new CycleActionInfo("Female_GesticulatingStandingMedium", "Female_GesticulatingStandingMediumIdle", "Female_GesticulatingStandingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingStandingMedium", index++);

            cycleInfo = new CycleActionInfo("Female_GesticulatingStandingHigh", "Female_GesticulatingStandingHighIdle", "Female_GesticulatingStandingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingStandingHigh", index++);

            cycleInfo = new CycleActionInfo("Female_GesticulatingSittingLow", "Female_GesticulatingSittingLowIdle", "Female_GesticulatingSittingLow");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingSittingLow", index++);

            cycleInfo = new CycleActionInfo("Female_GesticulatingSittingMedium", "Female_GesticulatingSittingMediumIdle", "Female_GesticulatingSittingMedium");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingSittingMedium", index++);

            cycleInfo = new CycleActionInfo("Female_GesticulatingSittingHigh", "Female_GesticulatingSittingHighIdle", "Female_GesticulatingSittingHigh");
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("GesticulatingSittingHigh", index++);

            cycleInfo = new CycleActionInfo("Female_HunchStanding", "Female_HunchStandingIdle", "Female_HunchStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("HunchStanding", index++);

            cycleInfo = new CycleActionInfo("Female_HunchSitting", "Female_HunchSittingIdle", "Female_HunchSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("HunchSitting", index++);

            cycleInfo = new CycleActionInfo("Female_LaughStanding", "Female_LaughStandingIdle", "Female_LaughStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("LaughStanding", index++);

            cycleInfo = new CycleActionInfo("Female_LaughSitting", "Female_LaughSittingIdle", "Female_LaughSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("LaughSitting", index++);

            cycleInfo = new CycleActionInfo("Female_RaiseHandStanding", "Female_RaiseHandStandingIdle", "Female_RaiseHandStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("RaiseHandStanding", index++);

            cycleInfo = new CycleActionInfo("Female_RaiseHandSitting", "Female_RaiseHandSittingIdle", "Female_RaiseHandSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("RaiseHandSitting", index++);

            cycleInfo = new CycleActionInfo("Female_WaveStanding", "Female_WaveStandingIdle", "Female_WaveStanding");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("WaveStanding", index++);

            cycleInfo = new CycleActionInfo("Female_WaveSitting", "Female_WaveSittingIdle", "Female_WaveSitting");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("WaveSitting", index++);

            cycleInfo = new CycleActionInfo("Female_CrossAnkles", "Female_CrossAnklesIdle", "Female_CrossAnkles");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CrossAnkles", index++);

            cycleInfo = new CycleActionInfo("Female_CrossLegs", "Female_CrossLegsIdle", "Female_CrossLegs");
            cycleInfo.setExitAnimationReverse(true);
            genericAnimations.add(cycleInfo);
            genericAnimationNames.put("CrossLegs", index++);
            
            info = new ActionInfo("Female_ShakeHands");
            genericAnimations.add(info);
            genericAnimationNames.put("ShakeHands", index++);
            
            info = new ActionInfo("Female_TouchArmStanding");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchArmStanding", index++);
            
            info = new ActionInfo("Female_TouchArmSitting");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchArmSitting", index++);
            
            info = new ActionInfo("Female_TouchShoulderStanding");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchShoulderStanding", index++);
            
            info = new ActionInfo("Female_TouchShoulderSitting");
            genericAnimations.add(info);
            genericAnimationNames.put("TouchShoulderSitting", index++);
        }
    }
}
