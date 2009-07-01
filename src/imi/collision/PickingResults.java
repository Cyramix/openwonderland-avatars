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
package imi.collision;

import javolution.util.FastTable;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * @author Lou Hayt
 */
@ExperimentalAPI
public class PickingResults
{
    // TODO - need a collection that supports insertion sort
    // (by distance from the origin)
    private FastTable<PickResult> results = new FastTable<PickResult>();

    PickResult closestPick = null;

    public void addPickResult(PickResult pickResult) {
        results.add(pickResult);
    }

    public PickResult getPickResult(int index) {
        return results.get(index);
    }

    public int getPickingResultsCount() {
        return results.size();
    }

    // TODO utilize insertion sort
    public PickResult getClosest()
    {
        if (results.isEmpty())
            return null;
        if (closestPick != null)
            return closestPick;
        closestPick  = results.getFirst();
        float      closestrange = results.getFirst().getClosestRange();
        for (PickResult pr : results)
        {
            float pickRange = pr.getClosestRange();
            if (pickRange < closestrange)
            {
                closestrange = pickRange;
                closestPick  = pr;
            }
        }
        return closestPick;
    }

    public void clear()
    {
        results.clear();
        closestPick = null;
    }
}
