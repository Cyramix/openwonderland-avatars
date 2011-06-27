/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package imi.character.statemachine.corestates.transitions;

import imi.character.CharacterController;
import imi.character.avatar.AvatarContext.ActionNames;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 * From walking to falling
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class WalkToFall extends TransitionObject
{
    @Override
    protected boolean testCondition(GameState state)
    {
        stateMessageName = "toFall";

        // If the fly action is active
        CharacterController controller = state.getContext().getController();
        if (controller != null && controller.getHeight() > 1.5f && 
            state.getContext().getActions()[ActionNames.Movement_Y.ordinal()] == 0f)
        {
            return state.getContext().excecuteTransition(this);
        }
            
        return false;
    }
}
