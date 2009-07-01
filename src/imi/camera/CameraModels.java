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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class provides publicly accessible factory methods for getting camera
 * models.
 *
 * @author Ronald E Dahlgren
 */
public class CameraModels
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(CameraModels.class.getName());

    /** Mapping of instantiated models to their class types **/
    private static final Map<Class<? extends CameraModel>, CameraModel> modelMap =
                    new HashMap<Class<? extends CameraModel>, CameraModel>();

    /**
     * Get a camera model of the specified type.
     * @param classz The type of camera model to retrieve
     * @return A CameraModel of the requested type, or null if the model could
     * not be instantiated
     */
    public static CameraModel getCameraModel(Class<? extends CameraModel> classz)
    {
        CameraModel result = null;
        result = modelMap.get(classz);

        if (result == null) // First request, instantiate and store
        {
            try {
                result = classz.newInstance();
                modelMap.put(classz, result);
            } catch (InstantiationException ex) {
                logger.severe("Could not instantiate " + classz.getName() + ", reason: " + ex.getCause());
            } catch (IllegalAccessException ex) {
                logger.severe("Could not instantiate " + classz.getName() + ", reason: " + ex);
            }
        }

        return result;
    }
}
