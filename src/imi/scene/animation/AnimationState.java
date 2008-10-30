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

/**
 * This class stores the state for a given animated instance.
 * 
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class AnimationState 
{
    private int         m_CurrentCycle              = 0;
    private float       m_CurrentCycleTime          = 0.0f;
    
    private int         m_TransitionCycle           = -1;
    private float       m_TransitionCycleTime       = 0.0f; // goes from start to end of the trainsition animation
    
    private float       m_TransitionDuration        = 0.5f; // how long the transition will last
    private float       m_TimeInTransition          = 0.0f; // goes 0.0f to trainsition duration
    
    private float       m_AnimationSpeed            = 1.0f; // 1.0f
    private boolean     m_bReverseAnimation         = false;
    private boolean     m_bPauseAnimation           = false;

    /**
     * Empty Constructor
     */
    public AnimationState()
    {
        
    }

    public AnimationState(AnimationState other)
    {
        m_CurrentCycle = other.m_CurrentCycle;
        m_CurrentCycleTime = other.m_CurrentCycleTime;
        m_TransitionCycle = other.m_TransitionCycle;
        m_TransitionCycleTime = other.m_TransitionCycleTime;
        m_TransitionDuration = other.m_TransitionDuration;
        m_TimeInTransition = other.m_TimeInTransition;
        m_AnimationSpeed = other.m_AnimationSpeed;
        m_bReverseAnimation = other.m_bReverseAnimation;
        m_bPauseAnimation = other.m_bPauseAnimation;
    }
    
    /**
     * Advances time for this state, animation speed is taken into consideration here.
     * Affects currentCycleTime, TransitionCycleTime and TimeInTransition.
     * @param fTimeStep
     */
    public void advanceAnimationTime(float fTimeStep) 
    {
        float advance = fTimeStep * m_AnimationSpeed;
        
        m_TimeInTransition    += advance;
        if (m_bReverseAnimation)
            fTimeStep *= -1.0f;
        
        m_CurrentCycleTime    += advance;
        m_TransitionCycleTime += advance;
    }
    
    /**
     * @return the current cycle's index
     */
    public int getCurrentCycle() {
        return m_CurrentCycle;
    }
    
    /**
     * Sets the current cycle.
     * Warning: This will "pop" the animation, for a smooth transition
     * use transitionTo() on the instance
     * @param index
     */
    public void setCurrentCycle(int index)
    {
        m_CurrentCycle = index;
    }
    
    /**
     * @return the current time of the animation cycle
     */
    public float getCurrentCycleTime()
    {
        return m_CurrentCycleTime;
    }
    
    /**
     * Set the current cycle time
     * @param fTime
     */
    public void setCurrentCycleTime(float fTime) {
        m_CurrentCycleTime = fTime;
    }
    
    /**
     * @return the index of the cycle being transitioned to
     */
    public int getTransitionCycle() {
        return m_TransitionCycle;
    }

    /**
     * Set the index of the cycle to transition to
     * Warning: This is a low level API implemintation method,
     * you might want to use transitionTo() on the instance instead
     * @param index
     */
    public void setTransitionCycle(int index) {
        m_TransitionCycle = index;
    }
    
    /**
     * @return the time of the transition cycle
     */
    public float getTransitionCycleTime() {
        return m_TransitionCycleTime;
    }

    /**
     * Set the time of the transition cycle
     * @param fTime
     */
    public void setTransitionCycleTime(float fTime) {
        m_TransitionCycleTime = fTime;
    }
    
    /**
     * @return the transition duration - how long the transition will last
     */
    public float getTransitionDuration()
    {
        return m_TransitionDuration;
    }
    
    /**
     * Set the transition duration - how long the transition will last
     * @param fDuration
     */
    public void setTransitionDuration(float fDuration)
    {
        m_TransitionDuration = fDuration;
    }
    
    /**
     * @return time in transition
     */
    public float getTimeInTransition() {
        return m_TimeInTransition;
    }

    /**
     * Set the current time of the transition
     * @param fTime
     */
    public void setTimeInTransition(float fTime) {
        m_TimeInTransition = fTime;
    }
    
    /**
     * @return animation speed
     */
    public float getAnimationSpeed() {
        return m_AnimationSpeed;
    }

    /**
     * Set the animation speed
     * @param AnimationSpeed
     */
    public void setAnimationSpeed(float AnimationSpeed) {
        m_AnimationSpeed = AnimationSpeed;
    }
    
    /**
     * @return true if the animation is set to play in reverse
     */
    public boolean isReverseAnimation() {
        return m_bReverseAnimation;
    }

    /**
     * Set if to play the animation in reverse
     * @param ReverseAnimation
     */
    public void setReverseAnimation(boolean ReverseAnimation) {
        m_bReverseAnimation = ReverseAnimation;
    }
    
    /**
     * @return true if the animation is paused
     */
    public boolean isPauseAnimation() {
        return m_bPauseAnimation;
    }

    /**
     * Set if to pause the animation
     * @param PauseAnimation
     */
    public void setPauseAnimation(boolean PauseAnimation) {
        m_bPauseAnimation = PauseAnimation;
    }
    
    /**
     * Returns true if currently transitioning
     * @return
     */
    public boolean isTransitioning()
    {
        if (m_TransitionCycle == -1)
            return false;
        return true;
    }
    
}
