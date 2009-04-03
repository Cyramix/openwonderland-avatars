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
package imi.character.objects;

import java.util.ArrayList;
import org.jdesktop.mtgame.Entity;

/**
 *
 * @author Lou Hayt
 */
public class ObjectCollectionBase extends Entity
{

    public ObjectCollectionBase(String name)
    {
        super(name);
    }

    public void addObject(SpatialObject obj) {
    }

    public void removeObject(SpatialObject obj) {
    }

    public void addLocation(LocationNode location) {
    }

    public void removeLocation(LocationNode location) {
    }

    public ArrayList<LocationNode> findPath(LocationNode source, String locationName) {
        return null;
    }
    
    public LocationNode findConnection(LocationNode source, String targetName, boolean allowBaked) {
        return null;
    }

    public SpatialObject findNearestObject(SpatialObject obj, float consideredRange, float searchCone, boolean occupiedMatters) {
        return null;
    }

    public SpatialObject findNearestObjectOfType(Class type, SpatialObject obj, float consideredRange, float searchCone, boolean occupiedMatters) {
        return null;
    }

}