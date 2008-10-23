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

import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaController;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 *
 * @author Lou
 */
public class FlyToIdle extends TransitionObject
{
    @Override
    protected boolean testCondition(GameState state) 
    {
        NinjaController controller = ((NinjaContext)state.getContext()).getController();
        if (controller == null)
            return false;
        
        if (controller.getAcceleration() == 0.0f)
        {    
            stateMessageName = "toIdle";
            return state.getContext().excecuteTransition(this);
        }
        return false;
    }
}
