/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
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

/**
 * This interface provides a mechanism for receiving events from the animation
 * system about when an animation cycle has started, looped, or completed.
 * @author Ronald E Dahlgren
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public interface AnimationListener 
{
    /**
     * This enumeration is used to indicate the type of message
     */
    public enum AnimationMessageType
    {
        // Used to indicate a cycle has completed; could be one end of an oscillation, or a completed loop iteration
        EndOfCycle, 
        TransitionComplete,
        PlayOnceComplete, // This will potentially fire multiple times in succession, write code to handle this!
        Restart,//restart the cycle action state
        RestartAndSave,//restart the cycle action state and save the current animation
        EndOfCycleWithoutExitAnim,//exit from the current animation and don't play exit animmation
        ExitAnimation,//play the exit animation of current animation
        // Others...?
    }
    
    /**
     * Receive notice that an animation event has occured.
     * <p>Handle this message in a *VERY* time efficient manner. No implementation
     * should be performing any complex calculations, just simple variable and
     * reference assignments. The stateID parameter is the "ID" of the state, which
     * is meaningful within the AnimationComponent(s) using it.</p>
     * @param message The animation type
     * @param stateID The "ID" of the affected state.
     * @param messageString The message string
     */
    public void receiveAnimationMessage(AnimationMessageType message, int stateID, String messageString);
    
    public void receiveAnimationMessage(AnimationMessageType message, int stateID);
}
