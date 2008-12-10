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

/**
 *
 * @author Lou Hayt
 */
public class Connection 
{
    private String      name        = null;
    private GraphNode   source      = null;
    private GraphNode   destination = null;
    private ConnectionDirection direction = ConnectionDirection.BothWays;
    
    public enum ConnectionDirection
    {
        OneWay,
        Reverse,
        BothWays,
    }
    
    public Connection(String name, GraphNode sourceNode, GraphNode destinationNode, ConnectionDirection connectionDirection)
    {
        this(sourceNode, destinationNode, connectionDirection);
        this.name = name;
    }
    
    /**
     * The connection will be added to the graph nodes.
     * @param sourceNode
     * @param destinationNode
     * @param connectionDirection
     */
    public Connection(GraphNode sourceNode, GraphNode destinationNode, ConnectionDirection connectionDirection)
    {
        if (connectionDirection == null || destinationNode == null || sourceNode == null)
            throw new NullPointerException("null pointer in graph node Connection constructor!");
        
        source      = sourceNode;
        destination = destinationNode;
        direction   = connectionDirection;
        source.addConnection(this);
        destination.addConnection(this);
    }

    public GraphNode getDestination() {
        return destination;
    }

    public void setDestination(GraphNode destination) {
        this.destination = destination;
    }

    public ConnectionDirection getDirection() {
        return direction;
    }

    public void setDirection(ConnectionDirection direction) {
        this.direction = direction;
    }

    public GraphNode getSource() {
        return source;
    }

    public void setSource(GraphNode source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
