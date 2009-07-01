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
package imi.character.statemachine;

/**
 * Defines a possible transition to exit from a state
 * @author Lou Hayt
 */
public abstract class TransitionObject 
{
    protected String  stateMessageName   = null;
    protected Object  stateMessageArgs    = null;
    
    protected String  contextMessageName = null;
    protected Object  contextMessageArgs  = null;

    /**
     * Transition from the provided state instance.
     * @param state
     * @return True on success, false on failure
     */
    public boolean transition(GameState state)
    {
        if (testCondition(state))
            return true;
        return false;
    }

    /**
     * Override to implement your validation logic against the provided GameState,
     * which is the current state.
     * @param state
     * @return
     */
    protected abstract boolean testCondition(GameState state);

    /**
     * Get the name of the context validation\entry point method
     * @return
     */
    public String getContextMessageName() {
        return contextMessageName;
    }

    /**
     * Set the name of the context validation\entry point method
     * @param contextMessageName
     */
    public void setContextMessageName(String contextMessageName) {
        this.contextMessageName = contextMessageName;
    }

    /**
     * Get the argument for the context validation\entry point method
     * @return
     */
    public Object getContextMessageArgs() {
        return contextMessageArgs;
    }

    /**
     * Set the argumet for the context validation\entry point method
     * @param contextMessgeArgs
     */
    public void setContextMessageArgs(Object contextMessgeArgs) {
        this.contextMessageArgs = contextMessgeArgs;
    }

    /**
     * Get the name of the state validation\entry point method
     * @return
     */
    public String getStateMessageName() {
        return stateMessageName;
    }

    /**
     * Set the name of the state validation\entry point method
     * @param stateMessageName
     */
    public void setStateMessageName(String stateMessageName) {
        this.stateMessageName = stateMessageName;
    }

    /**
     * Get the argument for the state validation\entry point method
     * @return
     */
    public Object getStateMessageArgs() {
        return stateMessageArgs;
    }

    /**
     * Set the arugment for the state validation\entry point method
     * @param stateMessgeArgs
     */
    public void setStateMessageArgs(Object stateMessgeArgs) {
        this.stateMessageArgs = stateMessgeArgs;
    }
    
    
}
