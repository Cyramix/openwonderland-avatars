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
package imi.utils.instruments;

import com.jme.math.Vector3f;
import imi.character.CharacterAttributes;
import org.jdesktop.mtgame.WorldManager;

/**
 * The default implementation of the instrumentation interface
 * @author Ronald E Dahlgren
 */
public class DefaultInstrumentation implements Instrumentation
{
    /** The manager of the world.  **/
    private WorldManager worldManager = null;

    public DefaultInstrumentation(WorldManager wm)
    {
        worldManager = wm;
        worldManager.addUserData(Instrumentation.class, this);
    }

    @Override
    public boolean addInstancedAvatar(Vector3f translation) {
        return false;
    }

    @Override
    public boolean addNonInstancedAvatar(Vector3f translation) {
        return false;
    }

    @Override
    public boolean addNonInstancedAvatar(CharacterAttributes specification, Vector3f translation) {
        return false;
    }

    @Override
    public boolean disableSubsytem(InstrumentedSubsystem system) {
        return false;
    }

    @Override
    public boolean enableSubsystem(InstrumentedSubsystem system) {
        return false;
    }

    @Override
    public boolean enableAllSubsystems() {
        return false;
    }

    @Override
    public boolean disableAllSubsystems() {
        return false;
    }

    @Override
    public boolean isSubsystemEnabled(InstrumentedSubsystem system) {
        return false;
    }
}
