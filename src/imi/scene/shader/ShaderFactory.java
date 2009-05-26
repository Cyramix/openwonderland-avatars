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
package imi.scene.shader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class controls creation of shader programs. This is necessary in order
 * to have all shader creation occur in one code location, as well as making
 * sure that the default shaders are only compiled once.
 * @author Ronald E Dahlgren
 */
public class ShaderFactory
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(ShaderFactory.class.getName());
    /** Collection of Prototypes **/
    private final Map<Class, AbstractShaderProgram> m_prototypes =
            new HashMap<Class, AbstractShaderProgram>();
    /** WorldManager, needed for creating shader programs **/
    private WorldManager m_wm = null;

    /**
     * Construct a new ShaderFactory
     * @param wm
     */
    public ShaderFactory(WorldManager wm)
    {
        m_wm = wm;
    }

    public AbstractShaderProgram newShader(Class shaderType)
    {
        AbstractShaderProgram result = null;
        if (AbstractShaderProgram.class.isAssignableFrom(shaderType))
        {
            // Have we built this already?
            synchronized (m_prototypes) {
                result = m_prototypes.get(shaderType);
            }
            if (result == null) // Not yet loaded, time to actually construct a new one
            {
                try {
                    // Grab the single parameter constructor
                    Constructor cons = shaderType.getConstructor(WorldManager.class);
                    result = (AbstractShaderProgram) cons.newInstance(m_wm);
                    // Now add it to the collection
                    synchronized (m_prototypes) {
                        m_prototypes.put(shaderType, result);
                    }
                }
                catch (Exception ex) {
                    logger.severe("Problem instantiating new shader.");
                    logger.severe(ex.getMessage());
                    ex.printStackTrace();
                }
            }
            result = result.duplicate(); // Don't want to send back the original
        }
        else // Wrong type of class
            logger.severe("ShaderFactory cannot construct class \"" + shaderType.getName() + "\"");
        return result;
    }
}
