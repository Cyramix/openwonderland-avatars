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
     * Construct a new transition queue to deal with this target
     * @param target
     */
    public TransitionQueue(Animated target)
    {
        m_target = target;
    }
    
    /**
     * Queue up a new transition command. This will be executed as other commands
     * in the queue are processed.
     * @param newCommand Must not be null.
     */
    public void addTransition(TransitionCommand newCommand)
    {
        m_commandQueue.enqueue(newCommand);
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
     */
    public void setTarget(Animated target)
    {
        m_target = target;
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
        switch (message)
        {
            case EndOfCycle:
                applyNextTransition();
                break;
            default:
                break;
        }
    }
    
}
