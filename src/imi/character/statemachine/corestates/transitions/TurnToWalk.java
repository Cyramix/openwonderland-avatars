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
package imi.character.statemachine.corestates.transitions;

import imi.character.statemachine.corestates.TurnState;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 * This class represents the transition from the Turn state to the Walk state.
 * @author Lou Hayt
 */
public class TurnToWalk extends TransitionObject
{
    private float moveDelay = 0.075f; //  how long do we need to press forward\backward to exit turn
    
    @Override
    protected boolean testCondition(GameState state) 
    {
        if (!(state instanceof TurnState))
            return false;
        
        TurnState turn = (TurnState)state;
        
        if (turn.getMoveCounter() > moveDelay)
        {
            stateMessageName = "toWalk";
            return state.getContext().excecuteTransition(this);
        }
        
        return false;
    }

}
