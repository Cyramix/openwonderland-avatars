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
package imi.loaders.collada;

import org.collada.colladaschema.VisualScene;

/**
 * This class provides static convenience methods for assistance in parsing
 * COLLADA documents.
 * --- WORK IN PROGRESS -- This class is intended for use with the second iteration
 * of the collada loader
 * @author Ronald E Dahlgren
 */
public class ColladaConvenienceUtils
{
    /**
     * Find the visual scene in the library that uses the provided identifier
     * @param visualSceneIdentifier
     * @return The VisualScene, or null if none matches
     */
    public static VisualScene findVisualSceneInLibrary(String visualSceneIdentifier)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
