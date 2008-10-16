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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import org.collada.colladaschema.InputLocalOffset;
import org.collada.colladaschema.Polylist;
import org.collada.colladaschema.Vertices;
import org.collada.colladaschema.InputLocal;

import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class PolylistProcessor extends Processor
{
    private MeshProcessor m_pMeshProcessor = null;

    private int m_VertexOffset = 0;

    private ArrayList<VertexDataSemantic> m_VertexDataSemantics = new ArrayList<VertexDataSemantic>();

    private VertexDataSemantic m_pPositionSemantic = null;
    private VertexDataSemantic m_pNormalSemantic = null;
    private VertexDataSemantic m_pTexCoordSemantic = null;


    private int[] m_PolygonVertexCounts = null;
    private int[] m_PolygonIndices;

    private String m_MaterialName = "";
    private PColladaMaterial m_pColladaMaterial = null; 



    //  Constructor.
    public PolylistProcessor(Collada pCollada, Polylist pPolylist, Processor pParent)
    {
        super(pCollada, pPolylist, pParent);

        if (pParent instanceof MeshProcessor)
            m_pMeshProcessor = (MeshProcessor)pParent;

        logger.info("Polylist " + pPolylist.getName());

        getMaterial(pPolylist);

        buildVertexDataSemanticArray(pPolylist);
        cacheVertexDataSemantics();

/*
        List<InputLocalOffset> inputs = pPolylist.getInputs();
        
        //  Calculate the VertexOffset.
        m_VertexOffset = 0;
        for(InputLocalOffset in : inputs)
        {
            m_VertexOffset = Math.max(in.getOffset().intValue(), m_VertexOffset);

            VertexDataSemantic pVertexDataSemantic = new VertexDataSemantic(in);

            m_VertexDataSemantics.add(pVertexDataSemantic);

            
            //  Gets the VertexDataArray with the specified name.
            pVertexDataSemantic.m_DataArray = m_pMeshProcessor.getVertexDataArray(pVertexDataSemantic.m_DataName);
            
            
            //  Cache pointers to the common Semantics.
            if (pVertexDataSemantic.m_Name.equals("VERTEX"))
                m_pPositionSemantic = pVertexDataSemantic;
            else if (pVertexDataSemantic.m_Name.equals("NORMAL"))
                m_pNormalSemantic = pVertexDataSemantic;
            else if (pVertexDataSemantic.m_Name.equals("TEXCOORD"))
                m_pTexCoordSemantic = pVertexDataSemantic;
        }

        m_VertexOffset++;
*/

//        System.out.println("VertexOffset:  " + m_VertexOffset);
//        System.out.flush();

        processPolygonVertexCounts(pPolylist.getVcount());
        processPolygonIndices(pPolylist.getP());
    }
    
    public void getMaterial(Polylist pPolylist)
    {
        m_MaterialName = "";
        if (pPolylist.getMaterial().length() > 2)
        {
            m_MaterialName = pPolylist.getMaterial();//.substring(0, pPolylist.getMaterial().length()-2);
            //System.out.println("   MaterialName:  " + m_MaterialName);
            m_pColladaMaterial = m_pCollada.findColladaMaterial(m_MaterialName);
        }
    }

    private void processPolygonVertexCounts(List<BigInteger> polygonVertexCountsList)
    {
        int count = polygonVertexCountsList.size();
        int a;
        BigInteger pBigInteger;
        
        m_PolygonVertexCounts = new int[count];
        
        for (a=0; a<count; a++)
        {
            pBigInteger = (BigInteger)polygonVertexCountsList.get(a);

            m_PolygonVertexCounts[a] = pBigInteger.intValue();
        }
    }

    private void processPolygonIndices(List<BigInteger> polygonIndicesList)
    {
        int count = polygonIndicesList.size();
        int a;
        BigInteger pBigInteger;
        
        m_PolygonIndices = new int[count];
        
        for (a=0; a<count; a++)
        {
            pBigInteger = (BigInteger)polygonIndicesList.get(a);

            m_PolygonIndices[a] = pBigInteger.intValue();
        }
    }

    public int[] getPolygonVertexCounts()
    {
        return(m_PolygonVertexCounts);
    }

    public int[] getPolygonIndices()
    {
        return(m_PolygonIndices);
    }




    public String getMeshName()
    {
        if (m_pMeshProcessor != null)
            return(m_pMeshProcessor.getName());
        return("");
    }


            
    //  Populates a PolygonMesh with geometry data.
    public void populatePolygonMesh(PPolygonMesh pPolygonMesh)
    {
        pPolygonMesh.setName(getMeshName());


        int [] polygonVertexCounts  = getPolygonVertexCounts();
        int [] polygonIndices       = getPolygonIndices();

        int index                   = 0;
        int polygonVertexCount      = 0;
        int polygonIndex            = 0;
        int vertexIndex             = 0;

        int positionIndex           = -1;
        int normalIndex             = -1;
        int texCoord0Index          = -1;

        Vector3f position           = null;
        Vector3f normal             = null;
        Vector2f texCoord0          = null;

        int meshPositionIndex       = -1;
        int meshNormalIndex         = -1;
        int meshTexCoord0Index      = -1;

        PPolygon pPolygon           = null;



        pPolygonMesh.beginBatch();

        
        //  Add all the Positions to the PolygonMesh.
        if (m_pPositionSemantic != null)
            populatePolygonMeshWithPositions(pPolygonMesh);

        //  Add all the Normals to the PolygonMesh.
        if (m_pNormalSemantic != null)
            populatePolygonMeshWithNormals(pPolygonMesh);

        //  Add all the TexCoords to the PolygonMesh.
        if (m_pTexCoordSemantic != null)
            populatePolygonMeshWithTexCoords(pPolygonMesh);


        for (polygonIndex=0; polygonIndex<polygonVertexCounts.length; polygonIndex++)
        {
            polygonVertexCount = polygonVertexCounts[polygonIndex];


            //  Create a new Polygon.
            pPolygon = pPolygonMesh.createPolygon();

            pPolygon.beginBatch();

            for (vertexIndex=0; vertexIndex<polygonVertexCount; vertexIndex++)
            {
                if (m_pPositionSemantic != null)
                    positionIndex = polygonIndices[index + m_pPositionSemantic.m_Offset];
                if (m_pNormalSemantic != null)
                    normalIndex = polygonIndices[index + m_pNormalSemantic.m_Offset];
                if (m_pTexCoordSemantic != null)
                    texCoord0Index = polygonIndices[index + m_pTexCoordSemantic.m_Offset];
                index += m_VertexOffset;

/*
                if (m_pPositionSemantic != null)
                    position = m_pPositionSemantic.getVector3f(positionIndex);
                if (m_pNormalSemantic != null)
                    normal = m_pNormalSemantic.getVector3f(normalIndex);
                if (m_pTexCoordSemantic != null)
                    texCoord0 = m_pTexCoordSemantic.getVector2f(texCoord0Index);


                if (m_pPositionSemantic != null)
                    meshPositionIndex = pPolygonMesh.getPosition(position);
                if (m_pNormalSemantic != null)
                    meshNormalIndex = pPolygonMesh.getNormal(normal);
                if (m_pTexCoordSemantic != null)
                    meshTexCoord0Index = pPolygonMesh.getTexCoord(texCoord0);
*/


                meshPositionIndex = positionIndex;
                meshNormalIndex = normalIndex;
                meshTexCoord0Index = texCoord0Index;


                //  Add the Vertex to the Polygon.
                pPolygon.addVertex(meshPositionIndex,       //  PositionIndex
                                   meshNormalIndex,         //  NormalIndex
                                   meshTexCoord0Index,      //  TexCoord1Index
                                   -1,                      //  TexCoord2Index
                                   -1,                      //  TexCoord3Index
                                   -1);                     //  TexCoord4Index

//                System.out.print("   Vert[" + vertexIndex + "]:  ");
//                System.out.print("Position: (" + position.x + ", " + position.y + ", " + position.z + "), ");
//                System.out.println("Normal: (" + normal.x + ", " + normal.y + ", " + normal.z + ")");
            }

            pPolygon.endBatch();
        }

        if (m_pNormalSemantic != null)
            pPolygonMesh.endBatch(false);
        else
        {
            pPolygonMesh.setSmoothNormals(true);
            pPolygonMesh.endBatch();
        }


        //System.out.println("PolyList.MaterialName:  " + m_MaterialName);
        if (m_pColladaMaterial != null)
        {
            //  Create the Material to be assigned to the PolygonMesh.
            PMeshMaterial pMaterial = m_pColladaMaterial.createMeshMaterial();
            if (pMaterial != null)
                pPolygonMesh.setMaterial(pMaterial);
        }
    }




    //  Populates the PolygonMesh with Positions.
    void populatePolygonMeshWithPositions(PPolygonMesh pPolygonMesh)
    {
        int a;
        int PositionCount = m_pPositionSemantic.getVector3fCount();
        Vector3f pPosition;

        for (a=0; a<PositionCount; a++)
        {
            pPosition = m_pPositionSemantic.getVector3f(a);
            
            pPolygonMesh.addPosition(pPosition);
        }
    }

    //  Populates the PolygonMesh with Normals.
    void populatePolygonMeshWithNormals(PPolygonMesh pPolygonMesh)
    {
        int a;
        int NormalCount = m_pNormalSemantic.getVector3fCount();
        Vector3f pNormal;

        for (a=0; a<NormalCount; a++)
        {
            pNormal = m_pNormalSemantic.getVector3f(a);
            
            pPolygonMesh.addNormal(pNormal);
        }
    }

    //  Populates the PolygonMesh with TexCoords.
    void populatePolygonMeshWithTexCoords(PPolygonMesh pPolygonMesh)
    {
        int a;
        int TexCoordCount = m_pTexCoordSemantic.getVector2fCount();
        Vector2f pTexCoord;

        for (a=0; a<TexCoordCount; a++)
        {
            pTexCoord = m_pTexCoordSemantic.getVector2f(a);
            
            pPolygonMesh.addTexCoord(pTexCoord);
        }
    }


    //  Finds the VertexDataSemantic with the specified name.
    private VertexDataSemantic findVertexDataSemantic(String name)
    {
        int a;
        VertexDataSemantic pVertexDataSemantic;

        for (a=0; a<m_VertexDataSemantics.size(); a++)
        {
            pVertexDataSemantic = m_VertexDataSemantics.get(a);

            if (pVertexDataSemantic.m_Name.equals(name))
                return(pVertexDataSemantic);
        }

        return(null);
    }

    private void buildVertexDataSemanticArray(Polylist pPolylist)
    {
        List<InputLocalOffset> inputs = pPolylist.getInputs();
        String name;
        String arrayName;
        int offset;
        Vertices pVertices;
        VertexDataArray pDataArray;
        VertexDataSemantic pVertexDataSemantic;


        m_VertexOffset = 0;
        for(InputLocalOffset in : inputs)
        {
            m_VertexOffset = Math.max(in.getOffset().intValue(), m_VertexOffset);

            name = in.getSemantic();
            arrayName = in.getSource();
            if (arrayName.startsWith("#"))
                arrayName = arrayName.substring(1);

            offset = in.getOffset().intValue();



            //  Get the Vertices with the specified name.
            pVertices = m_pMeshProcessor.getVertices(arrayName);
            if (pVertices != null)
            {
                int i;
                InputLocal pInputLocal;
                String inputLocalName;
                String inputLocalSourceName;

                for (i=0; i<pVertices.getInputs().size(); i++)
                {
                    pInputLocal = pVertices.getInputs().get(i);
                    
                    inputLocalName = pInputLocal.getSemantic();
                    inputLocalSourceName = pInputLocal.getSource();
                    if (inputLocalSourceName.startsWith("#"))
                        inputLocalSourceName = inputLocalSourceName.substring(1);

                    pDataArray = m_pMeshProcessor.getVertexDataArray(inputLocalSourceName);

                    //  Constructor.
                    pVertexDataSemantic = new VertexDataSemantic(inputLocalName, offset, inputLocalSourceName, pDataArray);

                    m_VertexDataSemantics.add(pVertexDataSemantic);
                }
            }
            else
            {
                //  Gets the VertexDataArray with the specified name.
                pDataArray = m_pMeshProcessor.getVertexDataArray(arrayName);
            
                pVertexDataSemantic = new VertexDataSemantic(name, offset, arrayName, pDataArray);

                m_VertexDataSemantics.add(pVertexDataSemantic);
            }
        }

        m_VertexOffset++;
    }

    private void cacheVertexDataSemantics()
    {
        m_pPositionSemantic = findVertexDataSemantic("POSITION");
        m_pNormalSemantic = findVertexDataSemantic("NORMAL");
        m_pTexCoordSemantic = findVertexDataSemantic("TEXCOORD");
    }

}




