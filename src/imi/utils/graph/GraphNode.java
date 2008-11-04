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
package imi.utils.graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Lou Hayt
 */
public class GraphNode 
{
    private HashMap<Class, Object> value        = new HashMap<Class, Object>();
    protected ArrayList<Connection>  connections  = new ArrayList<Connection>();
    
    
    public void setValue(Class valueClass, Object valueObject)
    {
        value.put(valueClass, valueObject);
    }
    
    public Object getValue(Class valueClass)
    {
        return value.get(valueClass);
    }
    
    public ArrayList<Connection> getConnections()
    {
        return connections;
    }
    
    public Connection getConnection(int index)
    {
        return connections.get(index);
    }
    
    public void addConnection(Connection con)
    {
        if (connections.contains(con))
            return;
        
        connections.add(con);
    }
}
