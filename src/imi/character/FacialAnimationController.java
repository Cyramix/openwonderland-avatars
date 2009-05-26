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
package imi.character;

import imi.character.avatar.*;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationState;
import javolution.util.FastList;

/**
 * This class provides a convenient method for applying and queing up facial
 * animations in an avatar.
 * @author Ronald E Dahlgren
 */
public class FacialAnimationController
{
    /** How long should the space between chained facial expressions be **/
    private static final float TimeBetweenAnimations = 0.3f;
    /** Default playback speed **/
    private static final float FacialAnimationSpeed = 0.24f;
    /** The avatar who's face we are controlling **/
    private Character parentAvatar = null;
    /** The facial animation state **/
    private AnimationState animState = null;
    /** List of pending animation states **/
    private final FastList<FacialAnimationCommand> commandQueue = new FastList<FacialAnimationCommand>();
    /** Used for tracking timing **/
    private float fTimeInCurrentAnimation = 0;
    /** Indicates that an animation has totally completed (played forward and backed out) **/
    private boolean animationComplete = true;
    /** the currently playing animation, if there is one **/
    private FacialAnimationCommand currentCommand = null;

    /**
     * Construct a new facial animation controller to operate on the provided
     * avatar.
     * @param parentAvatar The avatar who's face we shall control.
     * @param facialAnimationStateIndex Which animation state controls the face
     */
    FacialAnimationController(Character parentAvatar, int facialAnimationStateIndex)
    {
        this.parentAvatar = parentAvatar;
        animState = parentAvatar.getSkeleton().getAnimationState(facialAnimationStateIndex);
    }

    /**
     * Add a facial animation to play, queueing it up if another animation is already
     * in the pipe. The position in the queue is returned; note that this index
     * will no longer be valid once another animation completes. The return value
     * should just be used as an indication of how many animations will be played
     * before this one shows up, not as some absolute index.
     * @param fTimeIn
     * @param fTimeOut
     * @param fExpressionTime How long the expression should be held
     * @param cycleIndex Animation to play
     * @return Index in the queue currently.
     */
    public int queueFacialAnimation(float fTimeIn,
                                    float fTimeOut,
                                    float fExpressionTime,
                                    int cycleIndex,
                                    PlaybackMode playback)
    {
        FacialAnimationCommand command = new FacialAnimationCommand();
        int result = -1;

        command.transitionTimeIn = fTimeIn;
        command.transitionTimeOut = fTimeOut;
        command.expressionHoldTime = fExpressionTime;
        command.facialAnimationIndex = cycleIndex;
        command.mode = playback;
        synchronized(commandQueue)
        {
            commandQueue.add(command);
            result = commandQueue.size();
        }
        return result;
    }

    /**
     * Clears out the pending queue of facial animations.
     */
    public void clearPendingAnimations()
    {
        commandQueue.clear();
    }

    /**
     * Determine how many animations are remaining in the queue; does not count
     * the currently playing expression if there is one.
     * @return
     */
    public int getNumberOfRemainingExpressions()
    {
        return commandQueue.size();
    }

    /**
     * Play a facial animation immediately and clear out the pending list. If
     * there is an animation playing currently that has not completed, it will
     * complete before playing the requested one.
     * @param fTimeIn
     * @param fTimeOut
     * @param fExpressionTime
     * @param cycleIndex
     */
    public void postFacialAnimation(float fTimeIn,
                                    float fTimeOut,
                                    float fExpressionTime,
                                    int cycleIndex,
                                    PlaybackMode playback)
    {
        clearPendingAnimations(); // clear everything else out
        FacialAnimationCommand command = new FacialAnimationCommand();
        command.transitionTimeIn = fTimeIn;
        command.transitionTimeOut = fTimeOut;
        command.expressionHoldTime = fExpressionTime;
        command.facialAnimationIndex = cycleIndex;
        command.mode = playback;
        if (animationComplete) // do it now!
            processFacialAnimationCommand(command);
        else // add it to the queue
            commandQueue.add(command);
    }

    /**
     * Call this method to allow the controller to react to time.
     * @param deltaT
     */
    public void update(float deltaT)
    {
        if (animationComplete && commandQueue.isEmpty() == false) // not in the middle of something, so start a new animation
            processFacialAnimationCommand(commandQueue.removeFirst());
        else if (animationComplete == false) // could be complete with empty list
            updateCurrentAnimation(deltaT);
    }

    /**
     * Continue driving the current facial expression
     * @param deltaT
     */
    private void updateCurrentAnimation(float deltaT)
    {
        if (fTimeInCurrentAnimation < 0)
        {
            if (animState.isTransitioning() == false) // finished transitioning in
                fTimeInCurrentAnimation = 0;
        }
        else if (fTimeInCurrentAnimation < currentCommand.expressionHoldTime)
        {
            fTimeInCurrentAnimation += deltaT;
            if (fTimeInCurrentAnimation >= currentCommand.expressionHoldTime)
            {
                float cycleLength = parentAvatar.getSkeleton().getAnimationGroup(1).getCycle(currentCommand.facialAnimationIndex).getDuration();
                // start to transition out
                animState.setTransitionDuration(currentCommand.transitionTimeOut);
                animState.setAnimationSpeed(FacialAnimationSpeed);
                animState.setTransitionCycle(currentCommand.facialAnimationIndex);
                animState.setTimeInTransition(0);
                animState.setCycleMode(PlaybackMode.PlayOnce);
                animState.setTransitionCycleTime(cycleLength); 
                animState.setTransitionReverseAnimation(true);
            }
        }
        else if (animState.isTransitioning() == false)
        {
            // the animation has completed
            fTimeInCurrentAnimation = -1;
            animationComplete = true;
            currentCommand = null;
        }
    }
    /**
     * Apply this facial animation to the animation state
     * @param command
     */
    private void processFacialAnimationCommand(FacialAnimationCommand command)
    {
        currentCommand = command;
        animationComplete = false;
        // Now apply all of these things to the actual state
        animState.setTransitionDuration(command.transitionTimeIn);
        animState.setAnimationSpeed(FacialAnimationSpeed);
        animState.setTransitionCycle(command.facialAnimationIndex);
        animState.setTimeInTransition(0);
        animState.setCycleMode(PlaybackMode.PlayOnce);
        animState.setTransitionCycleTime(0); // This will need to be different for reverse
        animState.setTransitionReverseAnimation(false);
    }

    /**
     * This class represents an individual animation command.
     */
    private class FacialAnimationCommand
    {
        /** Time to take transitioning in **/
        public float transitionTimeIn = 0;
        /** Time to take transitioning out**/
        public float transitionTimeOut = 0;
        /** Time to hold this expression once the transition is complete **/
        public float expressionHoldTime = 0;
        /** Cycle index for the desired facial animation **/
        public int facialAnimationIndex = -1;
        /** How should this animation be played **/
        public PlaybackMode mode = PlaybackMode.PlayOnce;
    }

}
