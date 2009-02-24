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
package imi.scene.animation;
import java.io.Serializable;
import java.util.logging.Logger;
import javolution.util.FastTable;





/**
 * We use a single group broken down to cycles, additional groups
 * may offer alternate sets of animations.
 *
 * @author Chris Nagle
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class AnimationGroup implements Serializable
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(AnimationGroup.class.getName());
    /** The name of this animation group */
    private String                  m_name          = null;

    /** Contains the animation cycles that are defined for this animation group */
    private final FastTable<AnimationCycle> m_cycles       = new FastTable<AnimationCycle>();

    /**
     * Constructor
     * @param name
     */
    public AnimationGroup(String name)
    {
        if (name != null)
            m_name = name;
    }

    /**
     * Empty Constructor
     */
    public AnimationGroup()
    {
    }

    public Iterable<AnimationCycle> getCycles() {
        return m_cycles;
    }


    /**
     * Generates the current pose solution based on the specified state
     * of the animated thing.
     * @param animated That which is animated
     * @param animationStateIndex The animationStateIndex of the animation state to use for solving
     */
    private void calculateFrame(AnimationState state, Animated animated)
    {
        if (state.isPauseAnimation())
            return;

        int cycleIndex = state.getCurrentCycle();
        if (cycleIndex == -1 || m_cycles.isEmpty())
            return;

        AnimationCycle cycle = m_cycles.get(cycleIndex);
        clampCycleTime(state);

        // determine if a transition has happened
        int result = checkForTransitions(state);
        if (result == 1) // Transitioning
            calculateBlendedFrame(state, animated);
        else if (result == 0) // Not transitioning
            cycle.calculateFrame(state, animated);
    }

    /**
     * Generates the current "pose" solutions based on state input.
     * @param animated The thing to animate
     */
    public synchronized void calculateFrame(Animated animated)
    {
        calculateFrame(animated, 0);
    }

    /**
     * Calculates the frame for the provided animated thing.
     * @param animated
     * @param animationStateIndex
     */
    public synchronized void calculateFrame(Animated animated, int animationStateIndex)
    {
        AnimationState state = animated.getAnimationState(animationStateIndex);
        calculateFrame(state, animated);
    }

    /**
     * Calculates a pose solution as the interpolation of the current and the
     * transitioning cycle solutions.
     * @param state
     */
    private void calculateBlendedFrame(AnimationState state, Animated animated)
    {
        AnimationCycle cycle = m_cycles.get(state.getCurrentCycle());
        AnimationCycle transitionCycle = m_cycles.get(state.getTransitionCycle());
        // Calculate frame as usual with the current cycle
        cycle.calculateFrame(state, animated);
        // now blend this to the transition cycle
        transitionCycle.applyTransitionPose(state, animated);
    }
    
    /**
     * Find an animation currentCycle by name
     * @param cycleName or -1 if not found
     * @return currentCycle animationStateIndex
     */
    public int findAnimationCycleIndex(String cycleName)
    {
        for (int i = 0; i < m_cycles.size(); ++i)
            if (m_cycles.get(i).getName().equals(cycleName))
                return i;
        return -1;
    }


    /**
     * @return the number of animation cycles.
     */
    public int getCycleCount()
    {
        return m_cycles.size();
    }

    /**
     * Get an animation currentCycle by animationStateIndex
     * @param animationStateIndex
     * @return m_cycles[animationStateIndex] (animation at said animationStateIndex)
     */
    public AnimationCycle getCycle(int index)
    {
        if (index < 0 || index >= m_cycles.size())
        {
            Logger.getLogger(AnimationGroup.class.getName()).warning("Requested cycle does not exist :" +
                    " index was " + index + " collection size was " + m_cycles.size());
            return null;
        }
        return m_cycles.get(index);
    }

    /**
     * Gets the last animation currentCycle.
     * @return m_cycles[animationStateIndex] (animation at said animationStateIndex)
     */
    public AnimationCycle getLastCycle()
    {
        return m_cycles.getLast();
    }

    /**
     * Adds an AnimationCycle to the AnimationGroup.
     * @param cycle
     */
    public void addCycle(AnimationCycle cycle)
    {
        m_cycles.add(cycle);
    }

    //  Clears the AnimationGroup.
    public void clear()
    {
        m_name = null;
        m_cycles.clear();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("AnimationGroup: " + m_name + "\n");
        sb.append("Cycles:\n");
        for (AnimationCycle cycle : m_cycles)
            sb.append(cycle.toString() + "\n");
        return sb.toString();
    }

    /**
     * Check for transitions. The return value indicates whether the calculateBlendedFrame
     * method should be used rather than calculateFrame
     * @param state
     * @return 1 to transition, 0 to not transition, 2 on completion of a transition
     */
    private int checkForTransitions(AnimationState state)
    {
        int result = 0;
        if (state.getTransitionCycle() != -1)
        {
            // finished transitioning?
            if (state.getTimeInTransition() >= state.getTransitionDuration())
            {
                // do the switcheroo
                state.setCurrentCycle(state.getTransitionCycle());
                state.setTransitionCycle(-1);
                state.setCurrentCycleTime(state.getTransitionCycleTime());
                state.setTimeInTransition(0.0f);
                state.setReverseAnimation(state.isTransitionReverseAnimation());
                state.setCurrentCyclePlaybackMode(state.getCycleMode());
                state.sendMessage(AnimationListener.AnimationMessageType.TransitionComplete);
                state.getCursor().makeNegativeOne();
                result = 2;
            }
            else
                result = 1;
        }
        return result;
    }

    /**
     * Clamps the cycle times within the specified range; fires off messages
     * if applicable.
     * @param state
     */
    private void clampCycleTime(AnimationState state)
    {
        // Current cycle time first
        float fCurrentCycleTime = state.getCurrentCycleTime();
        AnimationCycle currentCycle = m_cycles.get(state.getCurrentCycle());

        // Outside the bounds?
        if (fCurrentCycleTime < 0 || fCurrentCycleTime > currentCycle.getDuration())
        {
            state.getCursor().makeNegativeOne();
            AnimationComponent.PlaybackMode mode = state.getCurrentCyclePlaybackMode();
            switch (mode)
            {
                case Loop:
                    if (state.isReverseAnimation() && fCurrentCycleTime < 0)
                        state.setCurrentCycleTime(currentCycle.getDuration() + currentCycle.getAverageTimeStep());
                    else if (!state.isReverseAnimation() && fCurrentCycleTime > (currentCycle.getDuration() + currentCycle.getAverageTimeStep()))
                        state.setCurrentCycleTime(0.0f);
                    break;
                case Oscillate:
                    if (state.isReverseAnimation() && fCurrentCycleTime < 0)
                        state.setCurrentCycleTime(0.0f);
                    else if (!state.isReverseAnimation() && fCurrentCycleTime > currentCycle.getDuration())
                        state.setCurrentCycleTime(currentCycle.getDuration());
                    // Flip the direction
                    state.setReverseAnimation(!state.isReverseAnimation());
                    break;
                case PlayOnce:
                    if (state.isReverseAnimation() && fCurrentCycleTime < 0)
                        state.setCurrentCycleTime(0);
                    else if (!state.isReverseAnimation() && fCurrentCycleTime > currentCycle.getDuration())
                        state.setCurrentCycleTime(currentCycle.getDuration());
                    // Let the listeners know
                    state.sendMessage(AnimationListener.AnimationMessageType.PlayOnceComplete);
                    break;
                default:
                    logger.warning("Unknown playback mode encountered. Mode was " + mode);
            }
        }

        // Now for transition cycle (if applicable)
        if (state.getTransitionCycle() != -1) // transitioning
        {
            float fTransitionCycleTime = state.getTransitionCycleTime();

            AnimationCycle transitionCycle = m_cycles.get(state.getTransitionCycle());

            if (fTransitionCycleTime < 0 || fTransitionCycleTime > transitionCycle.getDuration())
            {
                state.getCursor().makeNegativeOne();
                AnimationComponent.PlaybackMode mode = state.getCycleMode();
                switch (mode)
                {
                    case Loop:
                        if (state.isTransitionReverseAnimation() && fTransitionCycleTime < 0)
                            state.setTransitionCycleTime(transitionCycle.getDuration() + transitionCycle.getAverageTimeStep());
                        else if (!state.isTransitionReverseAnimation() && fTransitionCycleTime > (transitionCycle.getDuration() + transitionCycle.getAverageTimeStep()))
                            state.setTransitionCycleTime(0.0f);
                        break;
                    case Oscillate:
                        if (state.isTransitionReverseAnimation() && fTransitionCycleTime < 0)
                            state.setTransitionCycleTime(0.0f);
                        else if (!state.isTransitionReverseAnimation() && fTransitionCycleTime > transitionCycle.getDuration())
                            state.setTransitionCycleTime(transitionCycle.getDuration());
                        // Flip the direction
                        state.setTransitionReverseAnimation(!state.isTransitionReverseAnimation());
                        break;
                    case PlayOnce:
                        if (state.isTransitionReverseAnimation() && fTransitionCycleTime < 0)
                            state.setTransitionCycleTime(0.0f);
                        else if (!state.isTransitionReverseAnimation() && fTransitionCycleTime > transitionCycle.getDuration())
                            state.setTransitionCycleTime(transitionCycle.getDuration());
                        break;
                    default:
                        logger.warning("Unknown playback mode encountered. Mode was " + mode);
                }
            }
        }

    }
}



