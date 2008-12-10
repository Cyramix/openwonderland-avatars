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


import org.collada.colladaschema.Geometry;
import org.collada.colladaschema.Mesh;
import org.collada.xml_walker.Processor;

import imi.loaders.collada.Collada;




/**
 * The GeometryProcessor class processes a piece of geometry in the collada file.
 * 
 * @author Chris Nagle
 */
public class GeometryProcessor extends Processor
{
    private String m_Name = "";

    private Mesh m_pMesh = null;




    /**
     * Constructor.
     * 
     * @param collada
     * @param geometry
     * @param parent
     */
    public GeometryProcessor(Collada pCollada, Geometry pGeometry, Processor pParent)
    {
        super(pCollada, pGeometry, pParent);

        m_Name = pGeometry.getName();

        m_pMesh = pGeometry.getMesh();


        //  Create the MeshProcessor.
        ProcessorFactory.createProcessor(pCollada, m_pMesh, this);
    }

    //  Gets the name of the Geometry.
    public String getName()
    {
        return(m_Name);
    }
}



