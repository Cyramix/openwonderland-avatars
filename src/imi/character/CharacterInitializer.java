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
package imi.character;

import javolution.util.FastTable;

/**
 *
 * @author Lou Hayt
 */
public class CharacterInitializer implements InitializationInterface
{
    public FastTable<InitializationInterface> initers = new FastTable<InitializationInterface>();

    public CharacterInitializer() {}
    public CharacterInitializer(InitializationInterface... initializers) 
    {
        initers.addAll(initers);
    }
    public CharacterInitializer(InitializationInterface initializer) 
    {
        initers.add(initializer);
    }

    public void addInitializer(InitializationInterface initializer)
    {
        initers.add(initializer);
    }

    public void initialize(Character character) {
        for(InitializationInterface i : initers)
            i.initialize(character);
    }

}
