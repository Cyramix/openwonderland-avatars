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
import java.util.ArrayList;
import javolution.util.FastList;

/**
 * This class encapsulates animation functionality. It is used to 
 * generate current "pose" solutions based on state input. The state is also
 * modified to stay (for instance) within bounds of the current animation cycle
 * 
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class AnimationComponent implements Serializable
{
    /**
     * This enumeration serves to describe the playback mode of an animation clip.
     */
    public enum PlaybackMode
    {
        PlayOnce, // Play the animation and stop on the final frame
        Loop, // Continually loop from the beginning to the end (relative to time)
        Oscillate, // Play from one end to the other, reverse direction and play to the beginning, repeat
    }
    
    /** The animation data is stored in groups, a group contains multiple animations that can be indexed via AnimationCycles */
    private final ArrayList<AnimationGroup> m_AnimationGroups = new ArrayList<AnimationGroup>();
    
    /**
     * Empty constructor
     */
    public AnimationComponent()
    {
        
    }
    
    /**
     * Copy Constructor
     * @param other The one to copy
     */
    public AnimationComponent(AnimationComponent other)
    {
        for (AnimationGroup group : other.m_AnimationGroups)
            m_AnimationGroups.add(new AnimationGroup(group));
    }
    
    /**
     * Initiate a transition to an animation cycle by index
     * The animation group is defaulted to index 0
     * @param cycleIndex - cycle to transition to
     * @param state      - the state of the instance to animate
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(int cycleIndex, AnimationState state, boolean bReverse) 
    {
        return transitionTo(cycleIndex, state, 0, bReverse);
    }
    
    /**
     * Initiate a transition to an animation cycle by index
     * @param cycleIndex - cycle to transition to
     * @param state      - the state of the instance to animate
     * @param animationGroupIndex - the animation group to use
     * @param bReverse   - true if the animation being transitioned to should run in reverse
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(int cycleIndex, AnimationState state, int animationGroupIndex, boolean bReverse)
    {
        if(cycleIndex == -1)
            return false;
        if (cycleIndex == state.getTransitionCycle()) // Already transitioning there!
            return false;
        
        
        state.setTimeInTransition(0.0f);
        state.setTransitionCycle(cycleIndex);
        state.setTransitionReverseAnimation(bReverse);
        if(bReverse)
            state.setTransitionCycleTime(m_AnimationGroups.get(animationGroupIndex).getCycle(cycleIndex).getEndTime());
        else
            state.setTransitionCycleTime(m_AnimationGroups.get(animationGroupIndex).getCycle(cycleIndex).getStartTime());
        state.setTransitionCycleStartTime(m_AnimationGroups.get(animationGroupIndex).getCycle(cycleIndex).getStartTime());
        state.setTransitionCycleEndTime(m_AnimationGroups.get(animationGroupIndex).getCycle(cycleIndex).getEndTime());
        
        return true;
    }
    
    /**
     * Initiate a transition to an animation cycle by name
     * The animation group is defaulted to index 0
     * @param cycleName  - cycle to transition to
     * @param state      - the state of the instance to animate
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(String cycleName, AnimationState state, boolean bReverse) 
    {
        return transitionTo(cycleName, state, 0, bReverse);
    }
    
    /**
     * Initiate a transition to an animation cycle by name
     * @param cycleName - cycle to transition to
     * @param state      - the state of the instance to animate
     * @param animationGroupIndex - the animation group to use
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(String cycleName, AnimationState state, int animationGroupIndex, boolean bReverse) 
    {
        int index = findCycle(cycleName, animationGroupIndex);
        
        if (index == -1)
            return false;
        
        return transitionTo(index, state, bReverse);
    }

    /**
     * Finds an animation cycle by name
     * @param cycleName
     * @param animationGroupIndex
     * @return the index of the cycle if found or -1 otherwise
     */
    public int findCycle(String cycleName, int animationGroupIndex) 
    {
        if (m_AnimationGroups.isEmpty())
        {
            //System.out.println("can't find animation cycle: " + cycleName + ", m_AnimationGroups is empty!");
            return -1;
        }
        
        AnimationGroup loop = m_AnimationGroups.get(animationGroupIndex);
        if (loop == null)
            return -1;
        
        return loop.findAnimationCycleIndex(cycleName);
    }
    
    /**
     * Retreives the first (index = 0) animation group, or null if size == 0
     * @return m_AnimationGroups.get(0)
     */
    public AnimationGroup getGroup()
    {
        if (m_AnimationGroups.size() > 1)
            return m_AnimationGroups.get(1);
        if (m_AnimationGroups.size() > 0)
            return m_AnimationGroups.get(0);
        else
            return null;
    }
    
    /**
     * @return the list of animation groups
     */
    public ArrayList<AnimationGroup> getGroups()
    {
        return m_AnimationGroups;
    }
    
}
