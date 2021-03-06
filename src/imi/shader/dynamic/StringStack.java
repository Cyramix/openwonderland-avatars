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
package imi.shader.dynamic;

import javolution.util.FastTable;


/**
 * Utility class for stack based string accumulation
 * @author Ronald E Dahlgren
 */
class StringStack 
{
    /**
     * The backing data collection
     */
    private FastTable<String> strings = new FastTable<String>();

    /**
     * Construct a new instance
     */
    public StringStack()
    {
    }
    
    /**
     * Add this string to the top of the stack.
     * @param s
     */
    public void push(String s)
    {
        strings.add(s);
    }
    
    /**
     * Remove the top of the stack
     */
    public void pop()
    {
        strings.remove(strings.size() - 1);
    }
    
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        for (String s : strings)
            result.append(s);
        return result.toString();
    }

    /**
     * Clear the backing data structure
     */
    public void clear()
    {
        strings.clear();
    }
    
}
