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
package org.collada.xml_walker;

import org.collada.colladaschema.Controller;


/**
 * Wraps up the data from a skinned controller; morph controllers are not
 * currently supported.
 * @author Ronald E Dahlgren
 */
public class ColladaController
{
    private String  m_identifier = null;
    private String  m_name = null;
    public ColladaController(Controller colladaController)
    {
        m_identifier = colladaController.getId();
        m_name = colladaController.getName();
//        if (colladaController.getSkin() != null)
//            m_skin = new PColladaSkin(colladaController.getSkin());
    }

    public String getID()
    {
        return m_identifier;
    }

    public String getName()
    {
        return m_name;
    }


}
