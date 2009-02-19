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
    private AnimationGroup m_group = null;
    /** The list of animation commands being processed **/
    private final SynchronizedQueue<TransitionCommand>   m_commandQueue  = new SynchronizedQueue<TransitionCommand>();
    
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
     * @param animationStateAndGroupIndex
     */
    public TransitionQueue(Animated target, int animationStateAndGroupIndex)
    {
        m_target = target;
        m_state = m_target.getAnimationState(animationStateAndGroupIndex);
        m_group = m_target.getAnimationGroup(animationStateAndGroupIndex);
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
        if (m_state == null)
            return;
        
        /**
         * If the target is not currently transitioning, then there is no
         * reason to enqueue this command. Otherwise we place it on the queue
         * to wait its turn.
         */
        if (m_state.isTransitioning())
            m_commandQueue.enqueue(newCommand);
        else if (m_state.getCurrentCycle() == -1)
        {
            m_state.getCursor().makeNegativeOne();
//            System.out.println("setting facial pose for the first time:");
//            System.out.println("      animation " + newCommand.getAnimationIndex() + " reverse is " + newCommand.isReverse());
            
            // This is not a transition... it's the first animation to play
            AnimationCycle newCycle = m_group.getCycle(newCommand.getAnimationIndex());
            m_state.setCurrentCycle(newCommand.getAnimationIndex());
            
            if (newCommand.isReverse())
                m_state.setCurrentCycleTime(newCycle.getDuration());
            else
                m_state.setCurrentCycleTime(0);

            m_state.setReverseAnimation(newCommand.isReverse());
            m_state.setCurrentCyclePlaybackMode(newCommand.getPlaybackMode());
        }
        else // Do it now!
        {
//            System.out.println("setting facial pose:");
//            System.out.println("      animation " + newCommand.getAnimationIndex() + " reverse is " + newCommand.isReverse());
            m_state.getCursor().makeNegativeOne();
            AnimationCycle newCycle = m_group.getCycle(newCommand.getAnimationIndex());
            m_state.setTransitionCycle(newCommand.getAnimationIndex());
            
            if (newCommand.isReverse())
                m_state.setTransitionCycleTime(newCycle.getDuration());
            else
                m_state.setTransitionCycleTime(0);

            m_state.setTransitionDuration(newCommand.getTransitionLength());
            m_state.setTransitionReverseAnimation(newCommand.isReverse());
            m_state.setCycleMode(newCommand.getPlaybackMode());
            m_state.setTimeInTransition(0.0f);
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
     * Check to see if the transition queue is empty
     * @return true if empty
     */
    public boolean isQueueEmpty()
    {
        return m_commandQueue.isEmpty();
    }
    
    /**
     * Set the target for this queue
     * @param target
     * @param animationStateAndGroupIndex
     */
    public void setTarget(Animated target, int animationStateAndGroupIndex)
    {
        if (isTargetSet() == true)
        {
            // out with the old
            m_state.removeListener(this);
            m_target = null;
            m_state = null;
            m_group = null;
        }

        m_target = target;
        m_state = target.getAnimationState(animationStateAndGroupIndex);
        m_group = target.getAnimationGroup(animationStateAndGroupIndex);
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
        if (m_commandQueue == null || m_commandQueue.isEmpty())
            return;
        
        TransitionCommand nextCommand = m_commandQueue.dequeue();
        
        //System.out.println("animation " + nextCommand.getAnimationIndex() + " reverse is " + nextCommand.isReverse());
        
        if (nextCommand != null && isTargetSet() == true) // if there is a command to process...
        {   
            if (m_state != null) // Is there a state set for this fellow yet?
            {
                m_state.getCursor().makeNegativeOne();
                m_state.setTransitionCycle(nextCommand.getAnimationIndex());
                m_state.setTransitionDuration(nextCommand.getTransitionLength());
                m_state.setTransitionReverseAnimation(nextCommand.isReverse());
                m_state.setCycleMode(nextCommand.getPlaybackMode());
                m_state.setTimeInTransition(0.0f);
                
                AnimationCycle newCycle = m_group.getCycle(nextCommand.getAnimationIndex());
                if (newCycle == null) // uh oh
                {
                    Logger.getLogger(TransitionQueue.class.getName()).severe("Could not initiate transition!");
                    return;
                }
                if (nextCommand.isReverse())
                    m_state.setTransitionCycleTime(newCycle.getDuration());
                else
                    m_state.setTransitionCycleTime(0);
            }
            else
                Logger.getLogger(this.getClass().toString()).log(Level.FINE, "Animated target had no animation state!");
        }
    }

    public void receiveAnimationMessage(AnimationMessageType message, int stateID)
    {
//        if (m_commandQueue == null || m_commandQueue.isEmpty())
//            return;
        // dump debug info
        //System.out.println("Received an animation message: " + message.toString());
        // next command
//        TransitionCommand command = m_commandQueue.peek();
//        if (command != null)
//            System.out.println("Next command is: " + command.toString());
        
        
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
    
    /**
     * Calculates the total of all transitions remaining in the queue and accounts
     * for the amount of time into the current transition.
     * @return Total time until the queue is emptied given its current state
     */
    public float calculateTotalRemainingTime()
    {
        float result = 0.0f;
        if (m_state.isTransitioning()) // Make sure something relevant is happening
        {
            // Account for the time aleady spent transitioning
            result -= m_state.getTimeInTransition();
            // Make sure we have a reliable state
            synchronized(m_commandQueue)
            {
                for (int i = 0; i < m_commandQueue.size(); ++i)
                {
                    result += m_commandQueue.get(i).getTransitionLength();
                }
            }
        }
        return result;
    }

    
}
