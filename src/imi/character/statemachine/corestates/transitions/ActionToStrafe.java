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
package imi.character.statemachine.corestates.transitions;

import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 * This class represents the transition from the Punch state to the Walk state.
 * @author Lou
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class ActionToStrafe extends TransitionObject
{
    @Override
    protected boolean testCondition(GameState state) 
    {
        stateMessageName = "toSideStep";
        
        if ( !state.getContext().getTriggerState().isKeyPressed(TriggerNames.MiscAction.ordinal()) &&
                !state.getContext().getTriggerState().isKeyPressed(TriggerNames.MiscActionInSitting.ordinal()) &&
                (state.getContext().getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Left.ordinal())
                || state.getContext().getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Right.ordinal()) ))
            return state.getContext().excecuteTransition(this);
        
        return false;
    }

}
