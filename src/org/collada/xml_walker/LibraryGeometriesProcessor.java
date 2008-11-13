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

import java.util.ArrayList;
import java.util.List;
import org.collada.colladaschema.Geometry;
import org.collada.colladaschema.LibraryGeometries;

import imi.loaders.collada.Collada;



/**
 * The LibraryGeometriesProcessor class processes all geometries defined
 * in the collada file.
 * 
 * @author paulby
 *         Chris Nagle.
 */
public class LibraryGeometriesProcessor extends Processor
{
    private ArrayList<Processor> children = null;
    
    String m_CurrentMeshName = "";


    
    /**
     * Constructor.
     * 
     * @param pCollada
     * @param pGeometries
     * @param pParent
     */
    public LibraryGeometriesProcessor(Collada pCollada, LibraryGeometries pGeometries, Processor pParent)
    {
        super(pCollada, pGeometries, pParent);

        List<Geometry> geoms = pGeometries.getGeometries();
        children = new ArrayList();
        for(Geometry g : geoms)
        {
            m_CurrentMeshName = g.getId();

            //System.out.println("Processing Geometry "+g.getName());
            if (g.getConvexMesh()!=null)
            {
                //System.out.println("   ConvexMesh");
                children.add(ProcessorFactory.createProcessor(pCollada, g.getConvexMesh(), this));
            }

            if (g.getMesh()!=null)
            {
                //System.out.println("   Mesh");
                children.add(ProcessorFactory.createProcessor(pCollada, g.getMesh(), this));
            }

            if (g.getSpline()!=null)
            {
              //  System.out.println("   Spline");
                children.add(ProcessorFactory.createProcessor(pCollada, g.getSpline(), this));
            }

            // TODO: process the extras - for instance the double sided tag :)
        }
    }

    /**
     * Gets the name of the current Mesh.
     * 
     * @return String
     */
    public String getMeshName()
    {
        return(m_CurrentMeshName);
    }

}




