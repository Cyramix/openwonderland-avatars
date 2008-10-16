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
package imi.utils;

import imi.scene.processors.JSceneAWTEventProcessor;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides some utilities for easy manipulation of the WorldManager
 * @author Ronald E Dahlgren
 */
public class WorldManagerUtils 
{
    /**
     * Initializes a world manager through the following steps:
     *      Add our input manager
     *      Add the repository
     * @param wm
     */
    public static void initializeWorldManager(WorldManager wm)
    {
        //wm.addUserData(JSceneAWTEventProcessor.class, new JSceneAWTEventProcessor(listener, scene, myEntity));
    }
}
