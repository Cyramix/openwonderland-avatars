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
package imi.character.statemachine;

/**
 * Defines a possible exit transition from a state
 * @author Lou
 */
public abstract class TransitionObject 
{
    protected String  stateMessageName   = null;
    protected Object  stateMessgeArgs    = null;
    
    protected String  contextMessageName = null;
    protected Object  contextMessgeArgs  = null;
    
    public boolean transition(GameState state)
    {
        if (testCondition(state))
            return true;
        return false;
    }
    
    protected abstract boolean testCondition(GameState state);

    public String getContextMessageName() {
        return contextMessageName;
    }

    public void setContextMessageName(String contextMessageName) {
        this.contextMessageName = contextMessageName;
    }

    public Object getContextMessgeArgs() {
        return contextMessgeArgs;
    }

    public void setContextMessgeArgs(Object contextMessgeArgs) {
        this.contextMessgeArgs = contextMessgeArgs;
    }

    public String getStateMessageName() {
        return stateMessageName;
    }

    public void setStateMessageName(String stateMessageName) {
        this.stateMessageName = stateMessageName;
    }

    public Object getStateMessgeArgs() {
        return stateMessgeArgs;
    }

    public void setStateMessgeArgs(Object stateMessgeArgs) {
        this.stateMessgeArgs = stateMessgeArgs;
    }
    
    
}
