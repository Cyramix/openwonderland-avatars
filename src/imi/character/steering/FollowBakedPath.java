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
package imi.character.steering;

import imi.character.*;
import imi.character.objects.LocationNode;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;

/**
 * This task performs path following along location nodes.
 * @author Lou Hayt
 */
public class FollowBakedPath implements Task
{
    private GameContext context = null;
    
    private String  description  = "Follow a path";
    private String  status = "Chilling";
    
    private String path = null;
    private LocationNode location = null;
    
    private GoTo go = null;
    
    private boolean bDone = false;

    /**
     * Construct a new instance using the provided path name, location node,
     * and game context
     * @param pathName
     * @param node
     * @param context
     */
    public FollowBakedPath(String pathName, LocationNode node, GameContext context) 
    {
        this.context = context;
        if (context != null)
        {
            path = pathName;
            location = node;
            go = new GoTo(node.getPosition(), context);
        }
    }

    /**
     * Verify that the task is still valid (has not completed and has valid data)
     * @return
     */
    public boolean verify() 
    {
        if (path == null || location == null || bDone)
            return false;
        
        if (location.getName().equals(path))
        {
            status = "arrived to destination";
            return false;
        }
        
        return true;
    }

    public void update(float deltaTime) 
    {
        if (go.verify())
            go.update(deltaTime);
        else
        {
            LocationNode destination = location.findConnection(path);
            if (destination == null)
            {
                status = "was not able to find connection";
                bDone = true;
                return;
            }
            location = destination;
            go.reset(location.getPosition());
        }
    }

    public void onHold() {
        status = "on hold";
    }

    /**
     * This task has no singular goal, null will always be returned.
     * @return null
     */
    public SpatialObject getGoal() {
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

}
