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
package imi.character;

import imi.character.statemachine.GameContext;
import imi.character.statemachine.NamedUpdatableObject;

/**
 *  NinjaSteeringHelm contains most concrete code at this point
 * 
 * @author Lou
 */
public class CharacterSteeringHelm extends NamedUpdatableObject
{
    GameContext context = null;
    
    public CharacterSteeringHelm(String name, GameContext gameContext)
    {
        context = gameContext;
        setName(name);
        stop(); // starts disabled
    }
    
    @Override
    public void update(float deltaTime)
    {
        if (!enabledState)
            return;
    }

}
