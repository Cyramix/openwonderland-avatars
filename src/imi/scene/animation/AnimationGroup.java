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

import imi.scene.PJoint;
import java.io.Serializable;
import java.util.logging.Logger;
import javolution.util.FastList;





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
    /** The name of this animation group */
    private String                  m_name          = null;

    /** For every joint we have a channel that contains all of the frames for that joint during the animation duration */
    private final FastList<PJointChannel> m_JointChannels = new FastList<PJointChannel>();

    /** Contains the animation cycles that are defined for this animation group */
    private final FastList<AnimationCycle> m_cycles       = new FastList<AnimationCycle>();

    /** The overall duration of the entire animation in this group */
    private float                   m_Duration      = 0.0f;

    /** Used for seperatoin when appending groups */
    private final float             m_fTimePadding  = 10.0f;

    /**
     * Constructor
     * @param name
     */
    public AnimationGroup(String name)
    {
        if (name != null)
            m_name = name;
        // Debug info
        System.out.println("AnimationGroup created.");
        Thread.dumpStack();
    }

    /**
     * Empty Constructor
     */
    public AnimationGroup()
    {
        this((String)null);
    }

    /**
     * Copy constructor
     * @param other
     */
    public AnimationGroup(AnimationGroup other)
    {
        this(other.m_name);

        m_JointChannels.clear();
        for (PJointChannel jointChannel : other.m_JointChannels)
            m_JointChannels.add(jointChannel.copy());

        m_cycles.clear();
        for (AnimationCycle cycle : other.m_cycles)
            m_cycles.add(new AnimationCycle(cycle));

        m_Duration = other.m_Duration;

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
     * Generates the current pose solution based on the specified state
     * of the animated thing.
     * @param animated That which is animated
     * @param animationStateIndex The animationStateIndex of the animation state to use for solving
     */
    private void calculateFrame(AnimationState state, Animated animated)
    {
        // This was changed in order to support calculating frames on
        // multiple animation groups within a single animated thing
        if (state.isPauseAnimation())
            return;

        int cycleIndex = state.getCurrentCycle();
        if (cycleIndex == -1 || m_cycles.isEmpty())
            return;


        AnimationCycle cycle = m_cycles.get(cycleIndex);

        float fTime = clampCycleTime(cycle, state, true);
        state.setCurrentCycleTime(fTime);
        state.setCurrentCycleStartTime(cycle.getStartTime());
        state.setCurrentCycleEndTime(cycle.getEndTime());

        boolean bTransitioning = false;
        if (state.getTransitionCycle() != -1)
        {
            bTransitioning = true;


            AnimationCycle transitionCycle = m_cycles.get(state.getTransitionCycle());

            float fTransitionTime = clampCycleTime(transitionCycle, state, false);

            state.setTransitionCycleTime(fTransitionTime);

            state.setTransitionCycleStartTime(transitionCycle.getStartTime());
            state.setTransitionCycleEndTime(transitionCycle.getEndTime());
        }

        //  Iterate through all the Joint channels and apply the state to the joint.
        int jointIndex = 0;
        for (PJointChannel jointChannel : m_JointChannels)
        {
            PJoint joint = animated.getJoint(jointChannel.getTargetJointName());
            if (joint == null)
                System.out.println("Unable to locate joint " + jointChannel.getTargetJointName());

            state.getCursor().setJointIndex(jointIndex);

            if (bTransitioning)
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
                    state.sendMessage(AnimationListener.AnimationMessageType.TransitionComplete);
                    state.getCursor().makeNegativeOne();
                    return;
                }
                else
                    jointChannel.calculateBlendedFrame(joint, state);
            }
            else
            {
                jointChannel.calculateFrame(joint, state);
            }
            jointIndex++;
        }
    }

    public synchronized void calculateFrame(Animated animated, int animationStateIndex)
    {
        AnimationState state = animated.getAnimationState(animationStateIndex);
        calculateFrame(state, animated);
    }

    /**
     * Clamps the currentCycle time intelligently based on the provided state.
     * @param currentCycle
     * @param state
     * @param bClampForCurrentCycle
     * @return
     */
    private float clampCycleTime(AnimationCycle cycle, AnimationState state, boolean bClampForCurrentCycle)
    {
        // Variables assigned meaningful values based on bClampForCurrentCycle
        boolean bReverse  = false;
        AnimationComponent.PlaybackMode mode = null;

        float fTime = 0.0f;

        if (bClampForCurrentCycle == true) // Use current currentCycle info
        {
            bReverse = state.isReverseAnimation();
            mode = state.getCurrentCyclePlaybackMode();
            fTime = state.getCurrentCycleTime();
        }
        else // For transition currentCycle
        {
            bReverse = state.isTransitionReverseAnimation();
            mode = state.getTransitionPlaybackMode();
            fTime = state.getTransitionCycleTime();
        }

        if (bReverse)
        {
            if (fTime < cycle.getStartTime()) // Reverse left edge
            {
                if (mode == AnimationComponent.PlaybackMode.Loop)
                    fTime = cycle.getEndTime() - Float.MIN_VALUE;
                else if (mode == AnimationComponent.PlaybackMode.PlayOnce)
                {
                    fTime = cycle.getStartTime();
                    state.sendMessage(AnimationListener.AnimationMessageType.PlayOnceComplete);
                }
                else if (mode == AnimationComponent.PlaybackMode.Oscillate)
                {
                    fTime = cycle.getStartTime() + Float.MIN_VALUE;
                    // Reverse the currentCycle!
                    if (bClampForCurrentCycle)
                        state.setReverseAnimation(!state.isReverseAnimation());
                    else
                        state.setTransitionReverseAnimation(!state.isTransitionReverseAnimation());
                }
                state.getCursor().makeNegativeOne();
            }
            else if (fTime > cycle.getEndTime()) // Reverse right edge, clamp to the right
            {
                fTime = cycle.getEndTime();
                state.getCursor().makeNegativeOne();
            }
        }
        else // Not in reverse
        {
            if (fTime < cycle.getStartTime()) // Forward, left edge, clamp to the left
            {
                fTime = cycle.getStartTime();
                state.getCursor().makeNegativeOne();
            }
            else if (fTime > cycle.getEndTime()) // Forward, right edge
            {
                if (mode == AnimationComponent.PlaybackMode.Loop)
                    fTime = cycle.getStartTime() + Float.MIN_VALUE;
                else if (mode == AnimationComponent.PlaybackMode.PlayOnce)
                {
                    fTime = cycle.getEndTime();
                    state.sendMessage(AnimationListener.AnimationMessageType.PlayOnceComplete);
                }
                else if (mode == AnimationComponent.PlaybackMode.Oscillate)
                {
                    fTime = cycle.getEndTime() - Float.MIN_VALUE;
                    // Reverse the currentCycle!
                    if (bClampForCurrentCycle)
                        state.setReverseAnimation(!state.isReverseAnimation());
                    else
                        state.setTransitionReverseAnimation(!state.isTransitionReverseAnimation());
                }
                state.getCursor().makeNegativeOne();
            }
        }

        return fTime;
    }

    /**
     * Calculates the duration of the entire animation data stored in this group
     */
    public void calculateDuration()
    {
        for (PJointChannel channel : m_JointChannels)
            m_Duration = Math.max(m_Duration, channel.calculateDuration());
    }

    /**
     * @return the duration of the entire animation data stored in this group
     */
    public float getDuration()
    {
        return m_Duration;
    }

    /**
     * @return the channels for all the joints (channels contain all the frames for a particular joint)
     */
    public FastList<PJointChannel> getChannels()
    {
        return m_JointChannels;
    }

    /**
     * @return the channel targeting the specified joint.
     */
    public PJointChannel findChannel(String targetJointName)
    {
        for (PJointChannel channel : m_JointChannels)
            if (targetJointName.equals(channel.getTargetJointName()))
                return channel;
        return null;
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
        for (PJointChannel channel : m_JointChannels)
            channel.closeCycle(cycle);
    }

    /**
     * Creates a default currentCycle for the AnimationGroup. Also clears any cycles
     * that are already stored.
     */
    public void createDefaultCycle()
    {
        calculateDuration();

        AnimationCycle pAnimationCycle = new AnimationCycle("default", getStartTime(), m_Duration);
        m_cycles.clear();
        m_cycles.add(pAnimationCycle);
    }

    /**
     * Updates the default currentCycle.
     */
    public void updateDefaultCycle()
    {
        calculateDuration();

        m_cycles.getFirst().setStartTime(0.0f);
        m_cycles.getFirst().setEndTime(calculateLastFrameTime());
    }


    /**
     * Trims all the JointChannels.
     * @param fMaxTime The max keyframe time that should remain in the JointChannel.
     */
    public void trim(float fMaxTime)
    {
        for (PJointChannel channel : m_JointChannels)
            channel.trim(fMaxTime);
    }

    private Iterable<AnimationCycle> getCycles() {
        return m_cycles;
    }


    private float getStartTime()
    {
        float fStartTime = Float.MAX_VALUE;

        for (PJointChannel channel : m_JointChannels)
            fStartTime = Math.min(fStartTime, channel.getStartTime());

        return fStartTime;
    }

    private float calculateLastFrameTime()
    {
        float fEndTime = 0.0f;
        float fLocalEndTime = 0.0f;

        for (PJointChannel channel : m_JointChannels)
        {
            fLocalEndTime = channel.getEndTime();

            if (fLocalEndTime > fEndTime)
                fEndTime = fLocalEndTime;
        }

        return fEndTime;
    }

        //  Appends an AnimationGroup to the end of this AnimationGroup.
    public void appendAnimationGroup(AnimationGroup otherAnimationGroup)
    {
        PJointChannel pOriginalJointChannel = null;

        // grab the first animation currentCycle from this group
        AnimationCycle firstAnimationCycle = this.getCycle(0);

        //  Create duplicate of first Cycle if we only have one.
        if (this.getCycleCount() == 1)
        {
            //  Create a new AnimationCycle= copying the existing (only other) currentCycle
            AnimationCycle newAnimationCycle = new AnimationCycle(firstAnimationCycle);
            addCycle(newAnimationCycle);

            firstAnimationCycle.setName("All Cycles");
        }

        // find the time of the last keyframe in this animation group
        float fEndOfInitialKeyframes = calculateLastFrameTime() + m_fTimePadding;

        // Iterate through every currentCycle from the new group
        for (AnimationCycle currentCycle : otherAnimationGroup.getCycles())
        {
            currentCycle.setStartTime(currentCycle.getStartTime() + (fEndOfInitialKeyframes));
            currentCycle.setEndTime(currentCycle.getEndTime() + (fEndOfInitialKeyframes));

            this.addCycle(currentCycle);
        }

        for (PJointChannel channel : otherAnimationGroup.getChannels())
        {
            // Did this joint already have a channel in out group?
            pOriginalJointChannel = findChannel(channel.getTargetJointName());

            if (pOriginalJointChannel != null)
            {
                pOriginalJointChannel.append(channel, fEndOfInitialKeyframes);
                for (AnimationCycle cycle : m_cycles)
                    pOriginalJointChannel.closeCycle(cycle);
            }
            else
                addJointChannel(otherAnimationGroup, channel, fEndOfInitialKeyframes);
        }

        updateDefaultCycle(); // calls calculateDuration()

        otherAnimationGroup.clear();
    }

    //  Clears the AnimationGroup.
    public void clear()
    {
        m_name = "";
        m_JointChannels.clear();
        m_cycles.clear();
        m_Duration = 0.0f;
    }

    public void addJointChannel(AnimationGroup animationGroup, PJointChannel jointChannel, float fStartTime)
    {
        //  Adjust all the keyframes in 'jointChannel' by 'fStartTime'.
        // That is, shift all start times to the right such that they are added to the end of the group
        jointChannel.adjustKeyframeTimes(fStartTime);

        //  Remove the JointChannel from the AnimationGroup we're moving it from.
        animationGroup.getChannels().remove(jointChannel);

        // Close all existing cycles
        for (AnimationCycle cycle : m_cycles)
            jointChannel.closeCycle(cycle);

        //  Add the JointChannel to this AnimationGroup.
        m_JointChannels.add(jointChannel);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("AnimationGroup: " + m_name + "\n");
        sb.append("Cycles:\n");
        for (AnimationCycle cycle : m_cycles)
            sb.append(cycle.toString() + "\n");
        sb.append("JointChannels:\n");
        for (PJointChannel channel : m_JointChannels)
            sb.append(channel.toString() + "\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AnimationGroup other = (AnimationGroup) obj;
        if ((this.m_name == null) ? (other.m_name != null) : !this.m_name.equals(other.m_name))
        {
            return false;
        }
        if (this.m_JointChannels != other.m_JointChannels && (this.m_JointChannels == null || !this.m_JointChannels.equals(other.m_JointChannels)))
        {
            return false;
        }
        if (this.m_cycles != other.m_cycles && (this.m_cycles == null || !this.m_cycles.equals(other.m_cycles)))
        {
            return false;
        }
        if (this.m_Duration != other.m_Duration)
        {
            return false;
        }
        if (this.m_fTimePadding != other.m_fTimePadding)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + (this.m_name != null ? this.m_name.hashCode() : 0);
        hash = 11 * hash + (this.m_JointChannels != null ? this.m_JointChannels.hashCode() : 0);
        hash = 11 * hash + (this.m_cycles != null ? this.m_cycles.hashCode() : 0);
        hash = 11 * hash + Float.floatToIntBits(this.m_Duration);
        hash = 11 * hash + Float.floatToIntBits(this.m_fTimePadding);
        return hash;
    }

    

}



