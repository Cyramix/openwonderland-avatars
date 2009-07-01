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
package imi.camera;

import imi.scene.PMatrix;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages positions that can be stored and applied to a camera processor.
 * 
 * <p>These positions are stored with a {@code String} name associated with them.
 * Operations on this class are thread safe and may be freely used in concurrent
 * code.</p>
 *
 * @author Ronald E Dahlgren
 */
public final class CameraPositionManager
{
    /** Collection of camera positions mapped to string names **/
    private static final Map<String, PMatrix> camPositions = new ConcurrentHashMap();

    /**
     * Ensure this is locked.
     */
    private CameraPositionManager() {}
    
    ////////////////////////////////////////////
    /////////// Public API
    ////////////////////////////////////////////

    /**
     * Clear the collection of camera positions
     */
    public static void clearPositions()
    {
        synchronized (camPositions) // Changing state
        {
            camPositions.clear();
        }
    }

    /**
     * Retrieve the number of positions currently stored
     * @return The number of positions currently stored
     */
    public static int getNumberOfPositions()
    {
        return camPositions.size();
    }

    /**
     * Add the specified transform as a camera position with the provided name
     * @param transform A non-null transform
     * @param name A non-null name
     * @throws IllegalArgumentException If either parameter is null
     */
    public static void addCameraPosition(PMatrix transform, String name)
    {
        if (transform == null || name == null)
            throw new IllegalArgumentException("Provided a null parameter, transform: " + transform + ", name: " + name);
        camPositions.put(name, new PMatrix(transform)); // Defensive copy
    }

    /**
     * Retrieves the transform associated with the specified name.
     * @param name The name of the transform
     * @param output A non-null storage object
     * @return True if the transform was found, false otherwise
     * @throws IllegalArgumentException If either parameter is null
     */
    public static boolean getCameraTransform(String name, PMatrix output)
    {
        if (output == null || name == null)
            throw new IllegalArgumentException("Provided a null parameter, output: " + output + ", name: " + name);

        boolean result = false;
        synchronized(camPositions) // Non-atomic operation; checking state and then assuming that state is not changed
        {
            if (camPositions.containsKey(name))
            {
                output.set(camPositions.get(name));
                result = true;
            }
        }

        return result;
    }

    /**
     * Remove the specified camera position from the collection.
     *
     * <p> If the name provided does not map to any transforms, then nothing
     * happens to the collection and false is returned.</p>
     * @param name The name of the camera position to remove
     * @return True if the position was found, false otherwise
     */
    public static boolean removeCameraPosition(String name)
    {
        boolean result = false;
        synchronized (camPositions)
        {
            if (camPositions.containsKey(name))
            {
                camPositions.remove(name);
                result = true;
            }
        }

        return result;
    }
}
