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
package imi.utils.input;

import java.util.Hashtable;

/**
 * This class maintains input state.
 * 
 * @author Lou
 */
public class InputState 
{
    private Hashtable<Integer, Boolean> keys = new Hashtable<Integer, Boolean>();
    
    public void keyPressed(int key)
    {
        keys.put(key, true);
    }
    
    public void keyReleased(int key)
    {
        keys.put(key, false);   
    }
    
    public boolean isKeyPressed(int key)
    {
        Boolean result = keys.get(key);
        if (result == null)
            return false;
        return result;
    }
    
    public void clear()
    {
        keys.clear();
    }
}
