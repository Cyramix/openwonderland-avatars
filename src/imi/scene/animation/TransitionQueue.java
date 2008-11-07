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

import imi.utils.SynchronizedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class takes care of receiving requests for animation transitions
 * and gives these commands to it's target as necessary.
 * @author Ronald E Dahlgren
 */
public class TransitionQueue implements AnimationListener
{
    private Animated    m_target = null; // The target to affect
    private AnimationState m_state = null; // The animation state of the target
    /** The list of animation commands being processed **/
    private SynchronizedQueue<TransitionCommand>   m_commandQueue  = new SynchronizedQueue<TransitionCommand>();
    
    /**
     * Default constructor. A target must be specified before use
     */
    public TransitionQueue()
    {
        // Do nothing!
    }
    
    /**
     * Construct a new transition queue to deal with this target. The target's
     * AnimationState object should not be null.
     * @param target
     * @param animationStateIndex
     */
    public TransitionQueue(Animated target, int animationStateIndex)
    {
        m_target = target;
        m_state = m_target.getAnimationState(animationStateIndex);
        m_state.addListener(this);
    }
    
    /**
     * Queue up a new transition command. This will be executed as other commands
     * in the queue are processed. If the current target is not transitioning, then
     * the command is sent immediately.
     * @param newCommand Must not be null.
     */
    public void addTransition(TransitionCommand newCommand)
    {
        if (m_target == null) // is there a target to affect
            return;

        // Is there a state to operate on?
        AnimationState state = m_target.getAnimationState();
        if (state == null)
            return;
        
        /**
         * If the target is not currently transitioning, then there is no
         * reason to enqueue this command. Otherwise we place it on the queue
         * to wait its turn.
         */
        if (m_target.getAnimationState().isTransitioning())
            m_commandQueue.enqueue(newCommand);
        else // Do it now!
        {
            AnimationCycle newCycle = m_target.getAnimationComponent().getGroup().getCycle(newCommand.getAnimationIndex());
            state.setTransitionCycle(newCommand.getAnimationIndex());
            
            if (newCommand.isReverse())
                state.setTransitionCycleTime(newCycle.getEndTime());
            else
                state.setTransitionCycleTime(newCycle.getStartTime());

            state.setTransitionDuration(newCommand.getTransitionLength());
            state.setTransitionReverseAnimation(newCommand.isReverse());
            state.setTransitionPlaybackMode(newCommand.getPlaybackMode());
            state.setTimeInTransition(0.0f);
        }
    }
    
    /**
     * Clear out the queue and set the target to null;
     */
    public void clear()
    {
        m_commandQueue.clear();
        m_target = null;
    }
    
    /**
     * Set the target for this queue
     * @param target
     * @param animationStateIndex
     */
    public void setTarget(Animated target, int animationStateIndex)
    {
        if (isTargetSet() == true)
        {
            // out with the old
            m_state.removeListener(this);
            m_target = null;
            m_state = null;
        }

        m_target = target;
        m_state = target.getAnimationState(animationStateIndex);
        m_state.addListener(this);
    }
    
    /**
     * Determine if a target has been set for this queue yet.
     * @return
     */
    public boolean isTargetSet()
    {
        return (m_target != null);
    }
    
    
    public void applyNextTransition()
    {
        TransitionCommand nextCommand = m_commandQueue.dequeue();
        
        if (nextCommand != null && isTargetSet() == true) // if there is a command to process...
        {
            AnimationState state = m_target.getAnimationState();
            
            if (state != null) // Is there a state set for this fellow yet?
            {
                state.setTransitionCycle(nextCommand.getAnimationIndex());
                state.setTransitionDuration(nextCommand.getTransitionLength());
                state.setTransitionReverseAnimation(nextCommand.isReverse());
                state.setTransitionPlaybackMode(nextCommand.getPlaybackMode());
                state.setTimeInTransition(0.0f);
            }
            else
                Logger.getLogger(this.getClass().toString()).log(Level.FINE, "Animated target had no animation state!");
        }
    }

    public void receiveAnimationMessage(AnimationMessageType message)
    {
        if (m_commandQueue == null || m_commandQueue.isEmpty())
            return; // No relevance
        // dump debug info
        System.out.println("Received an animation message: " + message.toString());
        // next command
        TransitionCommand command = m_commandQueue.peek();
        if (command != null)
            System.out.println("Next command is: " + command.toString());
        switch (message)
        {
            case EndOfCycle:
                // Do nothing just yet!
                break;
            case TransitionComplete:
                applyNextTransition();
                break;
            case PlayOnceComplete: // <-- TODO!
                //applyNextTransition();
                break;
            default:
                break;
        }
    }
    
}
