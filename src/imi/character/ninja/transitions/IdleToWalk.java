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
package imi.character.ninja.transitions;

import imi.character.ninja.IdleState;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 *
 * @author Lou
 */
public class IdleToWalk extends TransitionObject
{
    private float moveDelay = 0.075f; //  how long do we need to press forward\backward to exit idle
    
    @Override
    protected boolean testCondition(GameState state) 
    {
        if (!(state instanceof IdleState))
            return false;
        
        IdleState idle = (IdleState)state;
        
        if (idle.getMoveCounter() > moveDelay)
        {
            stateMessageName = "toWalk";
            return state.getContext().excecuteTransition(this);
        }
        
        return false;
    }
}
