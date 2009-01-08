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

import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 * This class represents the transition from the Punch state to the Walk state.
 * @author Lou
 */
public class ActionToWalk extends TransitionObject
{
    @Override
    protected boolean testCondition(GameState state) 
    {
        stateMessageName = "toWalk";
        
        if ( !state.getContext().getTriggerState().isKeyPressed(TriggerNames.Punch.ordinal()) &&
                (state.getContext().getTriggerState().isKeyPressed(TriggerNames.Move_Forward.ordinal())
                || state.getContext().getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal()) ))
            return state.getContext().excecuteTransition(this);
        
        return false;
    }

}
