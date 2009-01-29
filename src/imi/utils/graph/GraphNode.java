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
package imi.utils.graph;

import java.util.HashMap;

/**
 *
 * @author Lou Hayt
 */
public class GraphNode 
{
    private HashMap<Class, Object> values        = new HashMap<Class, Object>();
    
    public void setValue(Class valueClass, Object valueObject)
    {
        values.put(valueClass, valueObject);
    }
    
    public Object getValue(Class valueClass)
    {
        return values.get(valueClass);
    }
}
