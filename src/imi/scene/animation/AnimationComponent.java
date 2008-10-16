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
public class AnimationComponent 
{
    /** The animation data is stored in groups, a group contains multiple animations that can be indexed via AnimationCycles */
    private ArrayList<AnimationGroup> m_AnimationGroups = new ArrayList<AnimationGroup>();
    
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
    public boolean transitionTo(int cycleIndex, AnimationState state) 
    {
        return transitionTo(cycleIndex, state, 0);
    }
    
    /**
     * Initiate a transition to an animation cycle by index
     * @param cycleIndex - cycle to transition to
     * @param state      - the state of the instance to animate
     * @param animationGroupIndex - the animation group to use
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(int cycleIndex, AnimationState state, int animationGroupIndex)
    {
        if (cycleIndex == state.getCurrentCycle() || cycleIndex == state.getTransitionCycle())
            return false;
        
        state.setTransitionCycle(cycleIndex);
        state.setTimeInTransition(0.0f);
        state.setTransitionCycleTime(m_AnimationGroups.get(animationGroupIndex).getCycle(cycleIndex).getStartTime());
        
        if(cycleIndex == -1)
            return false;
        
        return true;
    }
    
    /**
     * Initiate a transition to an animation cycle by name
     * The animation group is defaulted to index 0
     * @param cycleName  - cycle to transition to
     * @param state      - the state of the instance to animate
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(String cycleName, AnimationState state) 
    {
        return transitionTo(cycleName, state, 0);
    }
    
    /**
     * Initiate a transition to an animation cycle by name
     * @param cycleName - cycle to transition to
     * @param state      - the state of the instance to animate
     * @param animationGroupIndex - the animation group to use
     * @return false if the transition is already happening or not possible
     */
    public boolean transitionTo(String cycleName, AnimationState state, int animationGroupIndex) 
    {
        int index = findCycle(cycleName, animationGroupIndex);
        
        if (index == -1)
            return false;
        
        return transitionTo(index, state);
    }

    /**
     * Finds an animation cycle by name
     * @param cycleName
     * @param animationGroupIndex
     * @return the index of the cycle if found or -1 otherwise
     */
    public int findCycle(String cycleName, int animationGroupIndex) 
    {
        AnimationGroup loop = m_AnimationGroups.get(animationGroupIndex);
        if (loop == null)
            return -1;
        
        return loop.findAnimationCycle(cycleName);
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
