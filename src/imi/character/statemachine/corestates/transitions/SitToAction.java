/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
package imi.character.statemachine.corestates.transitions;

import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 *
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class SitToAction extends TransitionObject
{
    @Override
    protected boolean testCondition(GameState state) 
    {
        stateMessageName = "toAction";
        return state.getContext().excecuteTransition(this);
    }
}
