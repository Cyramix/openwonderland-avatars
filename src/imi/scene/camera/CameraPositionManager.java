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
package imi.scene.camera;

import imi.scene.PMatrix;
import javolution.util.FastList;

/**
 * This class is responsible for managing various camera positions that a camera
 * can move to.
 * @author Ronald E Dahlgren
 */
public class CameraPositionManager
{
    /** Singleton pattern **/
    private static final CameraPositionManager instance = new CameraPositionManager();
    /** Collection of camera positions **/
    private final FastList<CameraPosition> camPositions = new FastList();

    /**
     * Retrieve a reference to the camera position manager.
     * @return
     */
    public static CameraPositionManager instance()
    {
        return instance;
    }
    
    ////////////////////////////////////////////
    /////////// List ops ///////////////////////
    ////////////////////////////////////////////
    public void clearPositions()
    {
        camPositions.clear();
    }
    
    public int getNumberOfPositions()
    {
        return camPositions.size();
    }
    
    public int addCameraPosition(PMatrix transform, String name)
    {
        CameraPosition newPos = new CameraPosition();
        newPos.transform.set(transform);
        newPos.name = name;
        camPositions.add(newPos);
        
        return camPositions.size() - 1;
    }
    
    public String getCameraPositionName(int index)
    {
        if (index < 0 || index >= camPositions.size())
            return null;
        else
            return camPositions.get(index).name;
    }
    
    /**
     * Retrieve the transform of the requestion camera transform. If the index
     * is invalid, the rotationOutput matrix will not be altered.
     * @param index
     * @param output
     * @return True on success, false otherwise
     */
    public boolean getCameraTransform(int index, PMatrix output)
    {
        if (index < 0 || index >= camPositions.size())
            return false;
        else
            output.set(camPositions.get(index).transform);
        return true;
    }

    public boolean getCameraTransform(String name, PMatrix output)
    {
        return getCameraTransform(getCameraPositionIndex(name), output);
    }
    
    public int getCameraPositionIndex(String name)
    {
        for (int i = 0; i < camPositions.size(); ++i)
            if (camPositions.get(i).name.equals(name))
                return i;
        return -1;
    }
    
    public boolean removeCameraPosition(String name)
    {
        return removeCameraPosition(getCameraPositionIndex(name));
    }
    
    public boolean removeCameraPosition(int index)
    {
        if (index < 0 || index > camPositions.size())
            return false;
        else
            camPositions.remove(index);
        return true;
    }

    private class CameraPosition
    {
        public final PMatrix transform = new PMatrix();
        public String name = null;
    }
}
