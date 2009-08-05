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
package imi.character;

import imi.repository.AvatarRepoComponent;
import imi.repository.Repository;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides system wide configuration.
 * @author Ronald E Dahlgren
 */
public class AvatarSystem {

    /**
     * Initialize the system using the provided world manager
     * @param wm A non-null world manager ref with a valid repository
     * @throws IllegalArgumentException if no repository is found in the world manager
     */
    public static void initialize(WorldManager wm)
    {
        Repository repo = (Repository)wm.getUserData(Repository.class);
        if (repo == null)
            throw new IllegalArgumentException("Provided WorldManager had no repository!");
        repo.addRepositoryComponent(AvatarRepoComponent.class, new AvatarRepoComponent());
    }

    /**
     * Shut down the system.
     * @param wm A non-null world manager ref
     */
    public static void shutdown(WorldManager wm)
    {
        Repository repo = (Repository)wm.getUserData(Repository.class);
        if (repo == null)
            throw new IllegalArgumentException("Provided WorldManager had no repository!");
        repo.removeRepositoryComponent(AvatarRepoComponent.class);
    }
}
