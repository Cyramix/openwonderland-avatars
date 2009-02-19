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

import imi.scene.animation.channel.AnimationCursor;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import java.util.ArrayList;

/**
 * This class stores the state for a given animated instance.
 * 
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class AnimationState
{
    private int     m_ID                        = -1;
    
    // Current cycle information
    private int     m_CurrentCycle              = 0;
    private float   m_CurrentCycleTime          = 0.0f;
    /** Describes the playback mode of the current animation cycle **/
    private AnimationComponent.PlaybackMode m_currentCycleMode = AnimationComponent.PlaybackMode.Loop;
    
    // Transition cycle information
    private int     m_TransitionCycle           = -1;
    private float   m_TransitionCycleTime       = 0.0f; // goes from start to end of the trainsition animation
    private float   m_TransitionDuration        = 0.5f; // how long the transition will last
    private float   m_TimeInTransition          = 0.0f; // goes 0.0f to trainsition duration
    /** Describes the playback mode of the transitioning animation cycle **/
    private AnimationComponent.PlaybackMode m_cycleMode = AnimationComponent.PlaybackMode.Loop;
    
    private float   m_AnimationSpeed            = 1.0f; // 1.0f
    
    // booleans
    private boolean m_bReverseAnimation         = false;
    private boolean m_bPauseAnimation           = false;
    private boolean m_bTransitionReverseAnimation = false;
    
    /** The list of listeners to inform of messages **/
    private transient ArrayList<AnimationListener> m_listeners = null;

    /** **/
    private transient AnimationCursor m_animCursor = new AnimationCursor();
    /**
     * Empty Constructor
     */
    public AnimationState(int id)
    {
        m_ID = id;
    }

    public AnimationState(AnimationState other)
    {
        m_ID = other.m_ID;
        
        // Current cycle information
        m_CurrentCycle = other.m_CurrentCycle;
        m_CurrentCycleTime = other.m_CurrentCycleTime;
        
        // Transition cycle information 
        m_TransitionCycle = other.m_TransitionCycle;
        m_TransitionCycleTime = other.m_TransitionCycleTime;
        m_TransitionDuration = other.m_TransitionDuration;
        m_TimeInTransition = other.m_TimeInTransition;
        
        m_AnimationSpeed = other.m_AnimationSpeed;
        
        // booleans
        m_bReverseAnimation = other.m_bReverseAnimation;
        m_bPauseAnimation = other.m_bPauseAnimation;
        m_bTransitionReverseAnimation = other.m_bTransitionReverseAnimation;
    }
    
    /**
     * Advances time for this state,
     * this affects currentCycleTime, TransitionCycleTime and TimeInTransition
     * @param fTimeStep
     */
    public void advanceAnimationTime(float fTimeStep) 
    {
        float advance = fTimeStep * m_AnimationSpeed;
        
        m_TimeInTransition    += fTimeStep;
        
        // Negate the time step if in reverse
        if (m_bReverseAnimation)
            m_CurrentCycleTime -= advance;
        else
            m_CurrentCycleTime += advance;
        
        
        if (m_bTransitionReverseAnimation)
            m_TransitionCycleTime -= advance;
        else
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
    
    public boolean isTransitionReverseAnimation() {
        return m_bTransitionReverseAnimation;
    }

    public void setTransitionReverseAnimation(boolean bTransitionReverseAnimation) {
        this.m_bTransitionReverseAnimation = bTransitionReverseAnimation;
    }

    public PlaybackMode getCycleMode()
    {
        return m_cycleMode;
    }
    
    public void setCycleMode(PlaybackMode playbackMode)
    {
        m_cycleMode = playbackMode;
    }
    
    public PlaybackMode getCurrentCyclePlaybackMode()
    {
        return m_currentCycleMode;
    }
    
    public void setCurrentCyclePlaybackMode(PlaybackMode playbackMode)
    {
        m_currentCycleMode = playbackMode;
    }
    
    /**
     * Adds a listener to the state
     * @param listener
     * @return True if added, false otherwise (including already being in the list)
     */
    public boolean addListener(AnimationListener listener)
    {
        boolean result = false;
        if (m_listeners == null)
        {
            m_listeners = new ArrayList<AnimationListener>();
            m_listeners.add(listener);
            result = true;
        }
        else if (m_listeners.indexOf(listener) == -1)
        {
            m_listeners.add(listener);
            result = true;
        }
        else
            result = false;
        
        return result;
    }

    /**
     * Removes the specified listener from the list. 
     * @param listener
     * @return True if found, false otherwise
     */
    public boolean removeListener(AnimationListener listener)
    {
        if (m_listeners == null)
            return false;
        
        int index = m_listeners.indexOf(listener);
        
        if (index == -1)
            return false;
        else
            m_listeners.remove(listener);
        
        return true;
    }
    
    /**
     * Remove all registered listeners
     */
    public void clearListeners()
    {
        if (m_listeners != null)
        {
            m_listeners.clear();
            m_listeners = null;
        }
    }
    
    /**
     * Send the message to all registered animation listeners
     * @param message
     */
    public void sendMessage(AnimationListener.AnimationMessageType message)
    {
        m_animCursor.makeNegativeOne();
        if (m_listeners == null)
            return;
        for (AnimationListener listener : m_listeners)
            listener.receiveAnimationMessage(message, m_ID);
    }

    public int getID() {
        return m_ID;
    }

    public void setID(int m_ID) {
        this.m_ID = m_ID;
    }
    
    public AnimationCursor getCursor()
    {
        return m_animCursor;
    }
}
