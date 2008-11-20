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
package imi.scene.animation;

import imi.scene.PJoint;
import javolution.util.FastList;





/**
 * We use a single group broken down to cycles, additional groups 
 * may offer alternate sets of animations.
 * 
 * @author Chris Nagle
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class AnimationGroup
{
    /** The name of this animation group */
    private String                  m_name          = null;
    
    /** For every joint we have a channel that contains all of the frames for that joint during the animation duration */
    private FastList<PJointChannel> m_JointChannels = new FastList<PJointChannel>();

    /** Contains the animation cycles that are defined for this animation group */
    private AnimationCycle []       m_cycles        = null;
    
    /** The overall duration of the entire animation in this group */
    private float                   m_Duration      = 0.0f;
    
    /** Used for seperatoin when appending groups */
    private final float             m_fTimePadding  = 10.0f;
    
    /**
     * Empty Constructor
     */
    public AnimationGroup()
    {

    }

    /**
     * Copy constructor
     * @param other
     */
    public AnimationGroup(AnimationGroup other)
    {
        m_JointChannels.clear();
        m_cycles = null;
        if (other.m_name != null)
            m_name = new String(other.m_name);
        
        for (PJointChannel jointChannel : other.m_JointChannels)
        {
            m_JointChannels.add(jointChannel.copy());
        }
        
        m_cycles = new AnimationCycle[other.m_cycles.length];
        for (int i = 0; i < other.m_cycles.length; ++i)
            m_cycles[i] = new AnimationCycle(other.m_cycles[i]);
        
        m_Duration = other.m_Duration;
            
    }
    
    /**
     * Constructor
     * @param name
     */
    public AnimationGroup(String name)
    {
        if (name != null)
            m_name = name;
    }
    
    public void dump()
    {
        int a;
        PJointChannel pJointChannel;

        System.out.println("AnimationGroup:");

        for (a=0; a<m_JointChannels.size(); a++)
        {
            pJointChannel = m_JointChannels.get(a);
            pJointChannel.dump("   ");
        }
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
        if (cycleIndex == -1 || m_cycles == null)
            return;

        AnimationCycle cycle = m_cycles[cycleIndex];

        float fTime = clampCycleTime(cycle, state, true);
        state.setCurrentCycleTime(fTime);
        state.setCurrentCycleStartTime(cycle.getStartTime());
        state.setCurrentCycleEndTime(cycle.getEndTime());

        boolean bTransitioning = false;
        if (state.getTransitionCycle() != -1)
        {
            bTransitioning = true;

            
            AnimationCycle transitionCycle = m_cycles[state.getTransitionCycle()];
            
            float fTransitionTime = clampCycleTime(transitionCycle, state, false);
            
            state.setTransitionCycleTime(fTransitionTime);
            
            state.setTransitionCycleStartTime(transitionCycle.getStartTime());
            state.setTransitionCycleEndTime(transitionCycle.getEndTime());
        }
        
        //  Iterate through all the Joint channels and apply the state to the joint.
//        for (int jointIndex = 0; jointIndex < numberOfJoints; jointIndex++)
//        {
        for (PJointChannel jointChannel : m_JointChannels)
        {
            PJoint pJoint = animated.getJoint(jointChannel.getTargetJointName());
    
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
                    return;
                }
                else
                    jointChannel.calculateBlendedFrame(pJoint, state);
            }
            else
            {
                jointChannel.calculateFrame(pJoint, state);
            }
        }
    }
    
    public synchronized void calculateFrame(Animated animated, int animationStateIndex)
    {
        AnimationState state = animated.getAnimationState(animationStateIndex);
        calculateFrame(state, animated);
    }
    
    /**
     * Clamps the cycle time intelligently based on the provided state.
     * @param cycle
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
        
        if (bClampForCurrentCycle == true) // Use current cycle info
        {
            bReverse = state.isReverseAnimation();
            mode = state.getCurrentCyclePlaybackMode();
            fTime = state.getCurrentCycleTime();
        }
        else // For transition cycle
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
                    // Reverse the cycle!
                    if (bClampForCurrentCycle)
                        state.setReverseAnimation(!state.isReverseAnimation());
                    else
                        state.setTransitionReverseAnimation(!state.isTransitionReverseAnimation());
                }
                    
            }
            else if (fTime > cycle.getEndTime()) // Reverse right edge, clamp to the right
                fTime = cycle.getEndTime();
        }
        else
        {
            if (fTime < cycle.getStartTime()) // Forward, left edge, clamp to the left
            {
                fTime = cycle.getStartTime();
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
                    // Reverse the cycle!
                    if (bClampForCurrentCycle)
                        state.setReverseAnimation(!state.isReverseAnimation());
                    else
                        state.setTransitionReverseAnimation(!state.isTransitionReverseAnimation());
                }
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
        int a;
        PJointChannel pJointChannel;
        
        for (a=0; a<m_JointChannels.size(); a++)
        {
            pJointChannel = m_JointChannels.get(a);
            
            if (pJointChannel instanceof MS3D_JointChannel)
            {
                MS3D_JointChannel pMS3DJointChannel = (MS3D_JointChannel)pJointChannel;

                if (pMS3DJointChannel.getTargetJointName().equals(targetJointName))
                    return(pJointChannel);
            }
            else if (pJointChannel instanceof COLLADA_JointChannel)
            {
                COLLADA_JointChannel pColladaJointChannel = (COLLADA_JointChannel)pJointChannel;

                if (pColladaJointChannel.getTargetJointName().equals(targetJointName))
                    return(pJointChannel);
            }
        }

        return null;
    }
    

    /**
     * Find an animation cycle by name
     * @param cycleName
     * @return cycle animationStateIndex
     */
    public int findAnimationCycle(String cycleName) 
    {
        if (m_cycles == null || cycleName == null)
            return -1;
        
        for (int i = 0; i < m_cycles.length; i++)
        {
            if (m_cycles[i].getName().equals(cycleName))
                return i;
        }
        
        return -1;
    }
    
    
    /**
     * @return the number of animation cycles.
     */
    public int getCycleCount()
    {
        if (m_cycles == null)
            return 0;
        return m_cycles.length;
    }

    /**
     * Get an animation cycle by animationStateIndex
     * @param animationStateIndex
     * @return m_cycles[animationStateIndex] (animation at said animationStateIndex)
     */
    public AnimationCycle getCycle(int index)
    {
        if (m_cycles == null)
            return null;
        if (index < 0 || index > m_cycles.length)
            return null;
        return m_cycles[index];
    }

    /**
     * Gets the last animation cycle.
     * @return m_cycles[animationStateIndex] (animation at said animationStateIndex)
     */
    public AnimationCycle getLastCycle()
    {
        if (m_cycles == null)
            return null;
        return m_cycles[m_cycles.length-1];
    }

    /**
     * Adds an AnimationCycle to the AnimationGroup.
     * @param pAnimationCycle
     */
    public void addCycle(AnimationCycle cycle)
    {
        // Grow array
        int cycleCount = getCycleCount();
        
        AnimationCycle[] cycles = new AnimationCycle[cycleCount + 1];

        if (m_cycles != null)
        {
            for (int a=0; a<cycleCount; a++)
                cycles[a] = m_cycles[a];
        }
        cycles[cycleCount] = cycle;
        
        // This will tell each channel to close this cycle!
        for (PJointChannel channel : m_JointChannels)
            channel.closeCycle(cycle);
        
        m_cycles = cycles;
    }

    /**
     * Creates a default cycle for the AnimationGroup.
     */
    public void createDefaultCycle()
    {
        calculateDuration();

        AnimationCycle pAnimationCycle = new AnimationCycle("default", getStartTime(), m_Duration);
     
        m_cycles = new AnimationCycle[1];
        m_cycles[0] = pAnimationCycle;
    }

    /**
     * Updates the default cycle.
     */
    public void updateDefaultCycle()
    {
        calculateDuration();

        m_cycles[0].setStartTime(0.0f);
        m_cycles[0].setEndTime(calculateLastFrameTime());
    }
    

    /**
     * Trims all the JointChannels.
     * @param fMaxTime The max keyframe time that should remain in the JointChannel.
     */
    public void trim(float fMaxTime)
    {
        int a;
        PJointChannel pJointChannel;
        
        for (a=0; a<m_JointChannels.size(); a++)
        {
            pJointChannel = m_JointChannels.get(a);
            pJointChannel.trim(fMaxTime);
        }
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
        PJointChannel pJointChannel;
        float fEndTime = 0.0f;
        float fLocalEndTime = 0.0f;

        for (int i = 0; i < m_JointChannels.size(); i++)
        {
            pJointChannel = m_JointChannels.get(i);

            fLocalEndTime = pJointChannel.getEndTime();

            if (fLocalEndTime > fEndTime)
                fEndTime = fLocalEndTime;
        }

        return fEndTime;
    }

    //  Appends an AnimationGroup to the end of this AnimationGroup.
    public void appendAnimationGroup(AnimationGroup pAnimationGroup)
    {
        COLLADA_JointChannel pOriginalJointChannel;
        COLLADA_JointChannel pJointChannel;
        
        // grab the first animation cycle from this group
        AnimationCycle pFirstAnimationCycle = this.getCycle(0);
        
        //  Create duplicate of first Cycle if we only have one.
        if (this.getCycleCount() == 1)
        {
            //  Create a new AnimationCycle= copying the existing (only other) cycle
            AnimationCycle pNewAnimationCycle = new AnimationCycle(pFirstAnimationCycle);
            addCycle(pNewAnimationCycle);

            pFirstAnimationCycle.setName("All Cycles");
        }
        
        // find the time of the last keyframe in this animation group
        float fEndOfInitialKeyframes = calculateLastFrameTime() + m_fTimePadding;
        
        // Iterate through every cycle from the new group
        for (int i = 0; i < pAnimationGroup.getCycleCount(); ++i)
        {
            // grab the next one
            AnimationCycle currentCycle = pAnimationGroup.getCycle(i);
            
            currentCycle.setStartTime(currentCycle.getStartTime() + (fEndOfInitialKeyframes));
            currentCycle.setEndTime(currentCycle.getEndTime() + (fEndOfInitialKeyframes));
            
            this.addCycle(currentCycle);
        }
        
        
        for (int i = 0; i < pAnimationGroup.getChannels().size(); i++) // For each new channel
        {
            // grab the next channel
            pJointChannel = (COLLADA_JointChannel)pAnimationGroup.getChannels().get(i);
            // Did this joint already have a channel in out group?
            pOriginalJointChannel = (COLLADA_JointChannel)findChannel(pJointChannel.getTargetJointName());
            
            if (pOriginalJointChannel != null)
            {
                pOriginalJointChannel.append(pJointChannel, fEndOfInitialKeyframes);
                for (AnimationCycle cycle : m_cycles)
                    pOriginalJointChannel.closeCycle(cycle);
            }
            else
                addJointChannel(pAnimationGroup, pJointChannel, fEndOfInitialKeyframes);
        }
        
        //calculateDuration();

        updateDefaultCycle(); // calls calculateDuration()
        
        pAnimationGroup.clear();
    }

    //  Clears the AnimationGroup.
    public void clear()
    {
        m_name = "";
        m_JointChannels.clear();
        m_cycles = null;
        m_Duration = 0.0f;
    }

    public void addJointChannel(AnimationGroup pAnimationGroup, PJointChannel pJointChannel, float fStartTime)
    {
        //  Adjust all the keyframes in 'pJointChannel' by 'fStartTime'.
        // That is, shift all start times to the right such that they are added to the end of the group
        pJointChannel.adjustKeyframeTimes(fStartTime);

        //  Remove the JointChannel from the AnimationGroup we're moving it from.
        pAnimationGroup.getChannels().remove(pJointChannel);

        // Close all existing cycles
        for (int i = 0; i < m_cycles.length; ++i)
            pJointChannel.closeCycle(m_cycles[i]);
        //  Add the JointChannel to this AnimationGroup.
        m_JointChannels.add(pJointChannel);
    }
                
}



