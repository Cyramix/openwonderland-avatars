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

/**
 * Initialization component for Characters, set the implementing object
 * on the CharacterAttributes object that is passed to the Character constructor.
 *
 * The initialize() will be called once the character is done loading.
 * 
 * It is possible to use multiple initializers under a single initializer
 * object to be passed to the CharacterAttributes, to have them all excecute.
 * 
 * @author Lou Hayt
 */
public interface CharacterInitializationInterface
{
    public void initialize(Character character);
}
