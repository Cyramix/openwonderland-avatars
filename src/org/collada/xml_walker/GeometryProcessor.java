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


import org.collada.colladaschema.Geometry;
import org.collada.colladaschema.Mesh;
import org.collada.xml_walker.Processor;

import imi.loaders.collada.Collada;




/**
 *
 * @author Chris Nagle
 */
public class GeometryProcessor extends Processor
{
    private String m_Name = "";

    private Mesh m_pMesh = null;




    /** Creates a new instance of GeometryProcessor */
    public GeometryProcessor(Collada collada, Geometry geometry, Processor parent)
    {
        super(collada, geometry, parent);

        m_Name = geometry.getName();

        m_pMesh = geometry.getMesh();


        //  Create the MeshProcessor.
        ProcessorFactory.createProcessor(collada, m_pMesh, this);
    }

    //  Gets the name of the Geometry.
    public String getName()
    {
        return(m_Name);
    }
}



