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
import java.util.ArrayList;
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
     * @param index The index of the animation state to use for solving
     */
    public synchronized void calculateFrame(Animated animated, int index)
    {  
        // This was changed in order to support calculating frames on
        // multiple animation groups within a single animated thing
        AnimationState state = animated.getAnimationState(index);

        if (state.isPauseAnimation())
            return;

        int cycleIndex = state.getCurrentCycle();
        if (cycleIndex == -1 || m_cycles == null)
            return;

        AnimationCycle cycle = m_cycles[cycleIndex];

        float fTime = clampCycleTime(state.getCurrentCycleTime(), cycle, state.isReverseAnimation());
        
        // Apply the time constraints
        state.setCurrentCycleTime(fTime);
        state.setCurrentCycleStartTime(cycle.getStartTime());
        state.setCurrentCycleEndTime(cycle.getEndTime());

        boolean bTransitioning = false;
        if (state.getTransitionCycle() != -1)
        {
            bTransitioning = true;
            AnimationCycle transitionCycle = m_cycles[state.getTransitionCycle()];
            float fTransitionTime = clampCycleTime(state.getTransitionCycleTime(), transitionCycle, state.isReverseAnimation());
            
            state.setTransitionCycleTime(fTransitionTime);
            state.setTransitionCycleStartTime(transitionCycle.getStartTime());
            state.setTransitionCycleEndTime(transitionCycle.getEndTime());
            
        }


        ArrayList<PJointChannel> jointChannels = new ArrayList<PJointChannel>();
        for (int iii=0; iii<m_JointChannels.size(); iii++)
            jointChannels.add(m_JointChannels.get(iii));

        
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
                    state.setCurrentCycleStartTime(state.getTransitionCycleStartTime());
                    state.setCurrentCycleEndTime(state.getTransitionCycleEndTime());
                    
                    state.setTimeInTransition(0.0f);
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

        animated.setDirty(true, true);
    }
    
    /**
     * The animation jumps from the end to the beginning
     * @param fTime - current animation time
     * @param cycle - current animation cycle
     * @param bReverse - true to play the animation in reverse
     * @return
     */
    // TODO : reverse needs more testing
    // TODO : oscilate and single run
    private float clampCycleTime(float fTime, AnimationCycle cycle, boolean bReverse)
    {
        if (bReverse)
        {
            if (fTime < cycle.getStartTime())
                fTime = cycle.getEndTime();
            else if (fTime > cycle.getEndTime())
                fTime = cycle.getEndTime();   
        }
        else
        {
            if (fTime < cycle.getStartTime())
                fTime = cycle.getStartTime();
            else if (fTime > cycle.getEndTime())
                fTime = cycle.getStartTime();
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
     * @return cycle index
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
     * Set the animation cycles for this group
     * @param cycles
     */
    public void setCycles(AnimationCycle [] cycles)
    {
        m_cycles = cycles;
    }
    
    /**
     * @return the animation cycles of this group
     */
    public AnimationCycle[] getCycles()
    {
        return m_cycles;
    }

    /**
     * @return the number of animation cycles.
     */
    public int getCycleCount()
    {
        return m_cycles.length;
    }

    /**
     * Get an animation cycle by index
     * @param index
     * @return m_cycles[index] (animation at said index)
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
     * @return m_cycles[index] (animation at said index)
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
    public void addCycle(AnimationCycle pAnimationCycle)
    {
        int cycleCount = getCycleCount();
        AnimationCycle []cycles = new AnimationCycle[cycleCount + 1];

        if (m_cycles != null)
        {
            for (int a=0; a<cycleCount; a++)
                cycles[a] = m_cycles[a];
        }

        cycles[cycleCount] = pAnimationCycle;
        
        m_cycles = cycles;
    }

    /**
     * Creates a default cycle for the AnimationGroup.
     */
    public void createDefaultCycle()
    {
        calculateDuration();

//        float fDuration = 10.0f;//this.getDuration();
        AnimationCycle pAnimationCycle = new AnimationCycle("default", 0.033333f, m_Duration);
     
        m_cycles = new AnimationCycle[1];
        m_cycles[0] = pAnimationCycle;
    }

    /**
     * Updates the default cycle.
     */
    public void updateDefaultCycle()
    {
        calculateDuration();

        m_cycles[0].setStartTime(0.033333f);
        m_cycles[0].setEndTime(m_Duration);
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
        int a;
        PJointChannel pJointChannel;
        float fStartTime = 1000.0f;
        float fLocalStartTime = 0.0f;

        for (a=0; a<m_JointChannels.size(); a++)
        {
            pJointChannel = m_JointChannels.get(a);

            fLocalStartTime = pJointChannel.getStartTime();

            if (fLocalStartTime < fStartTime)
                fStartTime = fLocalStartTime;
        }

        return fStartTime;
    }

    private float getEndTime()
    {
        int a;
        PJointChannel pJointChannel;
        float fEndTime = 0.0f;
        float fLocalEndTime = 0.0f;

        for (a=0; a<m_JointChannels.size(); a++)
        {
            pJointChannel = m_JointChannels.get(a);

            fLocalEndTime = pJointChannel.getEndTime();

            if (fLocalEndTime > fEndTime)
                fEndTime = fLocalEndTime;
        }

        return fEndTime;
    }


    public float getAverageStepTime()
    {
        if (m_JointChannels.size() == 0)
            return(0.0f);

        PJointChannel pJointChannel = m_JointChannels.get(0);
        
        return pJointChannel.getAverageStepTime();
    }
        

    
    //  Appends an AnimationGroup to the end of this AnimationGroup.
    public void appendAnimationGroup(AnimationGroup pAnimationGroup)
    {
        int a;
        COLLADA_JointChannel pOriginalJointChannel;
        COLLADA_JointChannel pJointChannel;
        float fStartTime = getEndTime();
        float fAverageStepTime = getAverageStepTime();

        for (a=0; a<pAnimationGroup.getChannels().size(); a++)
        {
            pJointChannel = (COLLADA_JointChannel)pAnimationGroup.getChannels().get(a);

            pOriginalJointChannel = (COLLADA_JointChannel)findChannel(pJointChannel.getTargetJointName());
            if (pOriginalJointChannel != null)
            {
//                System.out.println("Appending JointChannel '" + pJointChannel.getTargetJointName() + "' onto existing JointChannel.");
                pOriginalJointChannel.append(pJointChannel);
            }
            else
            {
//                System.out.println("Adding JointChannel '" + pJointChannel.getTargetJointName() + "' to AnimationGroup.");
                addJointChannel(pAnimationGroup, pJointChannel, fStartTime);
            }
        }

        AnimationCycle pFirstAnimationCycle = this.getCycle(0);

        float fEndTime = getEndTime();

        //  Create duplicate of first Cycle if we only have one.
        if (this.getCycleCount() == 1)
        {
            //  Create a new AnimationCycle.
            AnimationCycle pNewAnimationCycle = new AnimationCycle(this.getCycle(0));
            pNewAnimationCycle.setName(this.getCycle(0).getName());
            pNewAnimationCycle.setEndTime(fStartTime);
            addCycle(pNewAnimationCycle);

            this.getCycle(0).setName("All Cycles");
        }

        AnimationCycle pLastAnimationCycle = getLastCycle();
//        if (pLastAnimationCycle != null)
//            pLastAnimationCycle.setEndTime(fStartTime);

        //  Create a new AnimationCycle.
        String newCycleName = pAnimationGroup.getCycle(0).getName();
        AnimationCycle pNewAnimationCycle = new AnimationCycle(newCycleName, fStartTime+fAverageStepTime, fEndTime);
        addCycle(pNewAnimationCycle);

        pFirstAnimationCycle.setEndTime(fEndTime);


        //  Clear the AnimationGroup that was added to this AnimationGroup.
        pAnimationGroup.clear();

        updateDefaultCycle();
    }

    //  Clears the AnimationGroup.
    public void clear()
    {
        m_name = "";
        m_JointChannels.clear();
        m_cycles = null;
        m_Duration = 0.0f;
    }

    public void appendJointChannelOntoAnother(PJointChannel pOriginalJointChannel, PJointChannel pJointChannel, float fStartTime)
    {
        //  Adjust all the keyframes in 'pJointChannel' by 'fStartTime'.
        pJointChannel.adjustKeyframeTimes(fStartTime);
    }

    public void addJointChannel(AnimationGroup pAnimationGroup, PJointChannel pJointChannel, float fStartTime)
    {
        //  Adjust all the keyframes in 'pJointChannel' by 'fStartTime'.
        pJointChannel.adjustKeyframeTimes(fStartTime);

        //  Remove the JointChannel from the AnimationGroup we're moving it from.
        pAnimationGroup.getChannels().remove(pJointChannel);

        //  Add the JointChannel to this AnimationGroup.
        m_JointChannels.add(pJointChannel);
    }
                
}



