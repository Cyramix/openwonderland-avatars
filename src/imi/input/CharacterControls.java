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

package imi.input;

import imi.character.Character;
import imi.objects.ObjectCollectionBase;

/**
 * Character input is driven by the character controls.
 * This class can be acceesed by WorldManger.getUserData(CharacterControls.class)
 * @author Lou Hayt
 */
public interface CharacterControls extends InputClient
{
    /**
     * Set the currently selected character and add to the character team if needed
     * @param character
     */
    public void setCharacter(Character character);
    /**
     * Returns true if the controls are set to command the entire character team
     * @return
     */
    public boolean isCommandingEntireTeam();
    /**
     * Set if the input controls will command the entire character team
     * @param commandEntireTeam
     */
    public void setCommandEntireTeam(boolean commandEntireTeam);
    /**
     * Clear the team of characters for input control and null the selected character
     */
    public void clearCharacterTeam();
    /**
     * Add a character to the input control team without selecting it for input
     * @param character
     */
    public void addCharacterToTeam(Character character);
    /**
     * Remove a character from the input control team. Select the next one.
     * @param characterToRemove
     */
    public void removeCharacterFromTeam(Character characterToRemove);
    /**
     * Select the next character in the team for input
     */
    public void controlNextCharacter();
    /**
     * Select the previous character in the team for input
     */
    public void controlPreviousCharacter();
    /**
     * Get the currently selected (for input) character
     * @return
     */
    public Character getCurrentlySelectedCharacter();
    /**
     * Set the object collection associated with this input control team
     * @param objectCollection
     */
    public void setObjectCollection(ObjectCollectionBase objectCollection);
}
