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
 * This class represents an animation command.
 * @author Ronald E Dahlgren
 */
public class TransitionCommand 
{
    
    /** How should the animation be performed? **/
    private AnimationComponent.PlaybackMode  m_playbackMode    = null;
    
    /** How long should the transition take before this becomes the primary animation **/
    private float           m_transitionLength  = 0.0f;
    /** 
     * After completing the transition, play the animaion for this minimum amount of time.
     * The amount of time specified here is absolute and not affected by the playback
     * speed. 
     */
    private float           m_minimumPlayDuration = 0.0f; // <-- unused, determine necessity before implementing
    /** Index of the desired animation (relative to the target animation component) **/
    private int             m_animationIndex    = -1;
    /** Should this animation be played in reverse? **/
    private boolean         m_bReverse          = false; 
    
    /**
     * Transition to the desired animation over the course of the specified length
     * of time. This constructor defaults to using the "Loop" playback mode and
     * forward playback.
     * @param animationIndex The index of the desired animation
     * @param transitionLength How long should the transition blend take
     */
    public TransitionCommand(int animationIndex, float transitionLength)
    {
        this(animationIndex, transitionLength, AnimationComponent.PlaybackMode.Loop, false);
    }
    
    /**
     * Transition to the desired animation over the course of the specified length
     * of time. Use the specified playback mode with the new animation. This constructor
     * defaults to forward playback.
     * @param animationIndex
     * @param transitionLength
     * @param type
     */
    public TransitionCommand(int animationIndex, float transitionLength, AnimationComponent.PlaybackMode type)
    {
        this(animationIndex, transitionLength, type, false);
    }
    
    /**
     * The most explicit constructor. Sets all internal state to the parameters provided.
     * @param animationIndex The animation to play
     * @param transitionLength How long should the transition take?
     * @param type Playback mode
     * @param playReversed True to play in reverse.
     */
    public TransitionCommand(int animationIndex, float transitionLength, AnimationComponent.PlaybackMode type, boolean playReversed)
    {
        m_playbackMode = type;
        m_animationIndex = animationIndex;
        m_transitionLength = transitionLength;
        m_bReverse = playReversed;
    }
    /***************************************************
     * Accessors, Mutators, and a whole lotta fun!
     ***************************************************/
    
    /**
     * Retrieve the playback mode for this animation command
     * @return
     */
    public AnimationComponent.PlaybackMode getPlaybackMode()
    {
        return m_playbackMode;
    }
    
    /**
     * Retrieve the desired transition length.
     * @return
     */
    public float getTransitionLength()
    {
        return m_transitionLength;
    }
    
    /**
     * Should the animation be played in reverse?
     * @return
     */
    public boolean isReverse()
    {
        return m_bReverse;
    }
    
    /**
     * What is the animation index
     * @return
     */
    public int getAnimationIndex()
    {
        return m_animationIndex;
    }
    
    // Mutators
    
    public void setPlaybackMode(AnimationComponent.PlaybackMode mode)
    {
        m_playbackMode = mode;
    }
    
    public void setTransitionLength(float length)
    {
        m_transitionLength = length;
    }
    
    public void setReverseMode(boolean isReversed)
    {
        m_bReverse = isReversed;
    }
    
    public void setAnimationIndex(int animationIndex)
    {
        m_animationIndex = animationIndex;
    }
}
