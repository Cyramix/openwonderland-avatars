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
package org.collada.xml_walker;

import java.util.logging.Logger;

import imi.loaders.collada.Collada;




/**
 *
 * @author paulby
 *         cnagle
 */
public abstract class Processor
{
    protected Logger logger = Logger.getLogger("org.collada.xml_walker");

    protected Collada   m_pCollada = null;
    protected Object    m_pColladaSchema = null;
    protected Processor m_pParent = null;



    //  Constructor.
    public Processor(Collada pCollada, Object pColladaSchema, Processor pParent)
    {
        m_pCollada = pCollada;
        m_pColladaSchema = pColladaSchema;
        m_pParent = pParent;
    }

}



