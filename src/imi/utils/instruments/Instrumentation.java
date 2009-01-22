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

/**
 * The interface to talk to an applications instrumentation object.
 * @author Ronald E Dahlgren
 */
public interface Instrumentation
{
    /**
     * Enumeration of the different instrumented subsystems.
     */
    public enum InstrumentedSubsystem
    {
        Texturing,
        VertexDeformation,
        PoseTransferToGPU,
        AnimationSystem,
    }

    /**
     * Add a well defined avatar with as much instanced as possible.
     * @param translation The world space position for the new avatar
     * @return True on success, false otherwise
     */
    public boolean addInstancedAvatar(Vector3f translation);

    /**
     * Add an avatar with as little sharing as possible using a random
     * configuration.
     * @param translation The world space position for the new avatar
     * @return True on success, false otherwise.
     */
    public boolean addNonInstancedAvatar(Vector3f translation);

    /**
     * Add am avatar with as little sharing as possible using the specified
     * configuration.
     * @param specification
     * @param translation The world space position for the new avatar
     * @return
     */
    public boolean addNonInstancedAvatar(CharacterAttributes specification,
                                        Vector3f translation);

    /**
     * Disables the specified subsystem.
     * @param system Subsystem to disable
     * @return True on success, false otherwise
     */
    public boolean disableSubsytem(InstrumentedSubsystem system);

    /**
     * Enables the specified subsystem
     * @param system System to enable
     * @return true on success, false otherwise.
     */
    public boolean enableSubsystem(InstrumentedSubsystem system);

    /**
     * Enable all the subsystems
     * @return True on success, false otherwise
     */
    public boolean enableAllSubsystems();

    /**
     * Enale all subsystems.
     * @return True on success, false otherwise
     */
    public boolean disableAllSubsystems();

    /**
     * Determine whether the specified subsystem is enabled or not.
     * @param system
     * @return
     */
    public boolean isSubsystemEnabled(InstrumentedSubsystem system);
}
