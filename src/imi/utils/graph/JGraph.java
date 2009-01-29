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

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * Listenable Directed Multigraph using GraphNode and Connection 
 * as vertex and edge classes respectivly.
 * @author Lou Hayt
 */
public class JGraph extends DefaultListenableGraph<GraphNode, Connection> implements DirectedGraph<GraphNode, Connection>
{
    private static final long serialVersionUID = 1L;

    public JGraph()
    {
        super(new DirectedMultigraph<GraphNode, Connection>(Connection.class));
    }
    
    public JGraph(Class<Connection> edgeClass)
    {
        super(new DirectedMultigraph<GraphNode, Connection>(edgeClass));
    }
    
}