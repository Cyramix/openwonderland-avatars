/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
package imi.character.statemachine.corestates.transitions;

import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;
import imi.character.statemachine.corestates.ActionState;

/**
 *
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class ActionToSit extends TransitionObject
{
    @Override
    protected boolean testCondition(GameState state) 
    {
        ActionState action = (ActionState)state;
        if (action.isRepeat())
            return false;
        
        stateMessageName = "toSit";
        
        return state.getContext().excecuteTransition(this);
        
        //return false;
    }

}
