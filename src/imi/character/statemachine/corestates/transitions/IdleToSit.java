/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 *
 * @author morrisford
 */
public class IdleToSit extends TransitionObject {

    @Override
    protected boolean testCondition(GameState state) {
        stateMessageName = "toSit";

        // If the sit on ground trigger is on
        if (state.getContext().getTriggerState().isKeyPressed(TriggerNames.GoSit.ordinal()))
            return state.getContext().excecuteTransition(this);


        return false;
    }
}
