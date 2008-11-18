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
import org.collada.colladaschema.Mesh;
import org.collada.colladaschema.Source;
import org.collada.xml_walker.Processor;
import org.collada.colladaschema.Vertices;

import imi.scene.polygonmodel.PPolygonMesh;

import imi.loaders.collada.Collada;



/**
 * The MeshProcessor processes a Mesh.  A Mesh contains positions, normals,
 * texcoords, (triangle lists or polygon lists).
 * 
 * @author paulby
 * @author Chris Nagle.
 */
public class MeshProcessor extends Processor
{
    private String                      m_Name = null;
    
    private ArrayList<VertexDataArray>  m_VertexDataArrays = new ArrayList<VertexDataArray>();

    private PPolygonMesh                m_rootPolyMesh = null;

    public MeshProcessor(Collada colladaDoc, Mesh pMesh, Processor pParent)
    {
        super(colladaDoc, pMesh, pParent);

        if (pParent instanceof LibraryGeometriesProcessor)
            m_Name = ((LibraryGeometriesProcessor)pParent).getMeshName();


        if (m_pCollada.getPrintStats())
            System.out.println("Mesh:  " + m_Name);

        List<Source> pSources = pMesh.getSources();   // 1 or more
        for(Source s : pSources)
            processSourceData(s);


        ProcessorFactory.createProcessor(colladaDoc, pMesh.getVertices(), this);  // exactly 1 vertices

        List primitives = pMesh.getTrianglesAndLinestripsAndPolygons();  // 0 or more

        // lines, linestrips, polygons, polylist, triangles, trifans, tristrips
        for(Object prim : primitives)
        {
            Processor currentProcessor = ProcessorFactory.createProcessor(colladaDoc, prim, this);

            PPolygonMesh polyMesh = null;

            //  Mesh contains Polygons?
            if (currentProcessor instanceof PolylistProcessor)
            {
                //  Create a PolygonMesh.
                polyMesh = colladaDoc.createPolygonMesh();

                ((PolylistProcessor)currentProcessor).populatePolygonMesh(polyMesh);
            }
            //  Mesh contains Triangles?
            else if (currentProcessor instanceof TrianglesProcessor)
            {
                polyMesh = colladaDoc.createPolygonMesh();

                //  Populate the PolygonMesh.
                ((TrianglesProcessor)currentProcessor).populatePolygonMesh(polyMesh);
            }
            else if (currentProcessor instanceof PolygonsProcessor)
            {
                polyMesh = colladaDoc.createPolygonMesh();
                ((PolygonsProcessor)currentProcessor).populatePolygonMesh(polyMesh);
            }

            // TODO: Handle lines and points

            //  If we haven't created a PolygonMesh yet, this is the root PolygonMesh.
            if (polyMesh != null)
            {
                if (m_rootPolyMesh == null)
                {
                    m_rootPolyMesh = polyMesh;
                    colladaDoc.addPolygonMesh(polyMesh);
                }
                //  Otherwise, assign the created PolygonMesh as a child of the root PolygonMesh.
                else
                {
                    m_rootPolyMesh.addChild(polyMesh);
                }
            }
        }
    }



    /**
     * Processes a Source element for the Mesh.
     * 
     * @param pSource
     */
    private void processSourceData(Source pSource)
    {
        VertexDataArray pVertexDataArray = new VertexDataArray(pSource);

        m_VertexDataArrays.add(pVertexDataArray);

        if (m_pCollada.getPrintStats())
            System.out.println("   VertexDataArray:  " + pVertexDataArray.m_Name + ", " + pVertexDataArray.m_Data.length);
    }

    /**
     * Gets the VertexDataArray with the specified name.
     * 
     * @param name
     * @return
     */
    VertexDataArray getVertexDataArray(String name)
    {
        int a;
        VertexDataArray pVertexDataArray;

        for (a=0; a<m_VertexDataArrays.size(); a++)
        {
            pVertexDataArray = m_VertexDataArrays.get(a);

            if (pVertexDataArray.m_Name.equals(name))
                return(pVertexDataArray);
        }

        return(null);
    }

    /**
     * Gets the Vertices with the specified name.
     * 
     * @param name
     * @return Vertices
     */
    public Vertices getVertices(String identifier)
    {
        Mesh colladaMesh = (Mesh)m_pColladaSchema;
        
        if (colladaMesh.getVertices() != null)
        {
            if (colladaMesh.getVertices().getId().equals(identifier))
                return(colladaMesh.getVertices());
        }
        
        return(null);
    }

    /**
     * Gets the name of the Mesh.
     * 
     * @return String
     */
    public String getName()
    {
        return(m_Name);
    }
}




