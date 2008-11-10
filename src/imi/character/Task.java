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
package imi.character;

import imi.character.objects.SpatialObject;

/**
 *
 * @author Lou Hayt
 */
public interface Task 
{
    public String getDescription();
    public String getStatus();

    public boolean verify();
    public void update(float deltaTime);
    public void onHold();
    public SpatialObject getGoal();
}