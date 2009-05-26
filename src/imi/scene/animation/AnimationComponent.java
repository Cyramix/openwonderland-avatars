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
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;
    

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
            state.setTransitionCycleTime(m_AnimationGroups.get(animationGroupIndex).getCycle(cycleIndex).getDuration());
        else
            state.setTransitionCycleTime(0);
        
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
        
        return transitionTo(index, state, animationGroupIndex, bReverse);
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
        if (m_AnimationGroups.size() > 0)
            return m_AnimationGroups.get(0);
        else
            return null;
    }

    /**
     * Return the last group in this component, or null if there are no groups
     * loaded.
     * @return
     */
    public AnimationGroup getLastGroup()
    {
        if (m_AnimationGroups.size() > 0)
            return m_AnimationGroups.get(m_AnimationGroups.size() - 1);
        else
            return null;
    }

    /**
     * Retreives the specified animation group, or null if out of bounds
     * @return m_AnimationGroups.get(0)
     */
    public AnimationGroup getGroup(int index)
    {
        if (m_AnimationGroups.size() > index)
            return m_AnimationGroups.get(index);
        else
            return null;
    }

    /**
     * Retrieve the number of animation groups currently loaded.
     * @return
     */
    public int getGroupCount()
    {
        return m_AnimationGroups.size();
    }

    /**
     * Append the provided group to the end of the collection of animation
     * groups.
     * @param groupToAdd
     * @return index of the newly added group
     */
    public int addGroup(AnimationGroup groupToAdd)
    {
        m_AnimationGroups.add(groupToAdd);
        return m_AnimationGroups.size() - 1;
    }

    /**
     * Remove the provided group from the collection
     * @param groupToRemove
     * @return True if found, false otherwise
     */
    public boolean removeGroup(AnimationGroup groupToRemove)
    {
        return m_AnimationGroups.remove(groupToRemove);
    }
    
    /**
     * @return the list of animation groups
     */
    public Iterable<AnimationGroup> getGroups()
    {
        return m_AnimationGroups;
    }
}
