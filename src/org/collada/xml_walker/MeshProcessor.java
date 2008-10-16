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
 *
 * @author paulby
 */
public class MeshProcessor extends Processor
{
    private String                      m_Name = "";
    
    private ArrayList<VertexDataArray>  m_VertexDataArrays = new ArrayList<VertexDataArray>();

    private ArrayList<Primitive>        m_PrimitiveProcessors = new ArrayList();

    private PPolygonMesh                m_pRootPolygonMesh = null;



    //  Constructor.
    public MeshProcessor(Collada pCollada, Mesh pMesh, Processor pParent)
    {
        super(pCollada, pMesh, pParent);

        if (pParent instanceof LibraryGeometriesProcessor)
            m_Name = ((LibraryGeometriesProcessor)pParent).getMeshName();


        if (m_pCollada.getPrintStats())
            System.out.println("Mesh:  " + m_Name);

        List<Source> pSources = pMesh.getSources();   // 1 or more
        for(Source s : pSources)
            processSourceData(s);


        ProcessorFactory.createProcessor(pCollada, pMesh.getVertices(), this);  // exactly 1 vertices

        List primitives = pMesh.getTrianglesAndLinestripsAndPolygons();  // 0 or more
        // lines, linestrips, polygons, polylist, triangles, trifans, tristrips

        for(Object prim : primitives)
        {
            Processor pProcessor = ProcessorFactory.createProcessor(pCollada, prim, this);

            PPolygonMesh pPolygonMesh = null;

            if (pProcessor instanceof PolylistProcessor)
            {
                //  Create a PolygonMesh.
                pPolygonMesh = pCollada.createPolygonMesh();

                ((PolylistProcessor)pProcessor).populatePolygonMesh(pPolygonMesh);
            }
            else if (pProcessor instanceof TrianglesProcessor)
            {
                pPolygonMesh = pCollada.createPolygonMesh();

                //  Populate the PolygonMesh.
                ((TrianglesProcessor)pProcessor).populatePolygonMesh(pPolygonMesh);
            }

            //  If we haven't created a PolygonMesh yet, this is the root PolygonMesh.
            if (pPolygonMesh != null)
            {
                if (m_pRootPolygonMesh == null)
                {
                    m_pRootPolygonMesh = pPolygonMesh;
                    pCollada.addPolygonMesh(pPolygonMesh);
                }
                //  Otherwise, assign the created PolygonMesh as a child of the root PolygonMesh.
                else
                {
                    m_pRootPolygonMesh.addChild(pPolygonMesh);
                }
            }

            if (pProcessor instanceof Primitive)
                m_PrimitiveProcessors.add((Primitive)pProcessor);
        }
    }



    //  Process a Source element for the Mesh.
    private void processSourceData(Source pSource)
    {
        VertexDataArray pVertexDataArray = new VertexDataArray(pSource);

        m_VertexDataArrays.add(pVertexDataArray);

        if (m_pCollada.getPrintStats())
            System.out.println("   VertexDataArray:  " + pVertexDataArray.m_Name + ", " + pVertexDataArray.m_Data.length);
    }

    //  Gets the VertexDataArray with the specified name.
    public VertexDataArray getVertexDataArray(String name)
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

    //  Gets the Vertices with the specified name.
    public Vertices getVertices(String name)
    {
        Mesh pMesh = (Mesh)m_pColladaSchema;
        
        if (pMesh.getVertices() != null)
        {
            if (pMesh.getVertices().getId().equals(name))
                return(pMesh.getVertices());
        }
        
        return(null);
    }


    //  Gets the name of the Mesh.
    public String getName()
    {
        return(m_Name);
    }
}




