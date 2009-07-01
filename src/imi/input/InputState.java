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

import java.util.Hashtable;

/**
 * This class maintains input state.
 * 
 * @author Lou
 */
public class InputState 
{
    private Hashtable<Integer, Boolean> keys = new Hashtable<Integer, Boolean>();

    /**
     * Set a particular key's state to being pressed
     * @param key
     */
    public void keyPressed(int key)
    {
        keys.put(key, true);
    }

    /**
     * Set a particular key's state to being not pressed
     * @param key
     */
    public void keyReleased(int key)
    {
        keys.put(key, false);   
    }

    /**
     * Check if a particular key is pressed
     * @param key
     * @return
     */
    public boolean isKeyPressed(int key)
    {
        Boolean result = keys.get(key);
        if (result == null)
            return false;
        return result;
    }

    /**
     * Clear the current state
     */
    public void clear()
    {
        keys.clear();
    }
}
