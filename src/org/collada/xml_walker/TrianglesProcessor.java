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

//import com.sun.j3d.utils.geometry.GeometryInfo;
//import com.sun.j3d.utils.geometry.Stripifier;
//import com.sun.j3d.utils.geometry.StripifierStats;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import org.collada.colladaschema.InputLocal;
import org.collada.colladaschema.InputLocalOffset;
import org.collada.colladaschema.Triangles;
import org.collada.colladaschema.Vertices;

import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.polygon.PPolygon;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class TrianglesProcessor extends Processor
{
    private MeshProcessor m_pMeshProcessor = null;

    private int m_VertexOffset = 0;

    private ArrayList<VertexDataSemantic> m_VertexDataSemantics = new ArrayList<VertexDataSemantic>();

    private VertexDataSemantic m_pPositionSemantic = null;
    private VertexDataSemantic m_pNormalSemantic = null;
    
    /** Make space for multiple sets of texture coordinates **/
    private VertexDataSemantic[] m_texCoordSemanticArray = new VertexDataSemantic[8];

    private int [] m_TriangleIndices = null;

    private String m_MaterialName = "";
    private PColladaMaterial m_pColladaMaterial = null; 




    /**
     * Constructor.
     * 
     * @param pCollada
     * @param pTriangles
     * @param pParent
     */
    public TrianglesProcessor(Collada pCollada, Triangles pTriangles, Processor pParent)
    {
        super(pCollada, pTriangles, pParent);

        if (pParent instanceof MeshProcessor)
            m_pMeshProcessor = (MeshProcessor)pParent;

        logger.info("Polylist " + pTriangles.getName());

        getMaterial(pTriangles);

//	    if (doesMaterialContainTexture())
//        {
//            int aaa = 0;
//        }

        buildVertexDataSemanticArray(pTriangles);
        cacheVertexDataSemantics();

//        System.out.println("VertexOffset:  " + m_VertexOffset);
//        System.out.flush();

        processTriangles(pTriangles.getP());
    }

    
    boolean doesMaterialContainTexture()
    {
        if (m_pColladaMaterial == null)
            return(false);

        if (m_pColladaMaterial.getEmissiveImageFilename().length() > 0)
            return(true);

        if (m_pColladaMaterial.getAmbientImageFilename().length() > 0)
            return(true);

        if (m_pColladaMaterial.getDiffuseImageFilename().size() > 0)
            return(true);

        if (m_pColladaMaterial.getSpecularImageFilename().length() > 0)
            return(true);

        if (m_pColladaMaterial.getReflectiveImageFilename().length() > 0)
            return(true);

        if (m_pColladaMaterial.getBumpMapImageFilename().length() > 0)
            return(true);

        if (m_pColladaMaterial.getNormalMapImageFilename().length() > 0)
            return(true);

        return(false);
    }
    

    /**
     * Gets the Material assigned to the Triangles.
     * 
     * @param pTriangles
     */
    public void getMaterial(Triangles pTriangles)
    {
        m_MaterialName = "";
        if (pTriangles.getMaterial().length() > 2)
        {
            m_MaterialName = pTriangles.getMaterial();//.substring(0, pTriangles.getMaterial().length()-2);
            //System.out.println("   MaterialName:  " + m_MaterialName);
            m_pColladaMaterial = m_pCollada.findColladaMaterial(m_MaterialName);
        }
    }

    /**
     * Processes a list of triangle indices.
     * The contents of the list of BigIntegers is converted to a array of ints.
     * 
     * @param triangleList
     */
    private void processTriangles(List<BigInteger> triangleList)
    {
        int count = triangleList.size();
        int a;
        BigInteger pBigInteger;
        
        m_TriangleIndices = new int[count];
        
        for (a=0; a<count; a++)
        {
            pBigInteger = (BigInteger)triangleList.get(a);

            m_TriangleIndices[a] = pBigInteger.intValue();
        }
    }


    /**
     * Gets array of triangle indices.
     * 
     * @return int[]
     */
    public int[] getTriangleIndices()
    {
        return(m_TriangleIndices);
    }


    //  Gets the name of the Mesh containing the triangles.
    public String getMeshName()
    {
        if (m_pMeshProcessor != null)
            return(m_pMeshProcessor.getName());
        return("");
    }


    /**
     * Populates the specified PPolygonMesh with the triangles.
     * 
     * @param pPolygonMesh
     */
    public void populatePolygonMesh(PPolygonMesh pPolygonMesh)
    {
        pPolygonMesh.setName(getMeshName());

        int []meshTriangles             = getTriangleIndices();

        int index                       = 0;
        int polygonIndex                = 0;
        
        int vertex1PositionIndex        = -1;
        int vertex1NormalIndex          = -1;
        int vertex1TexCoord1Index       = -1;
        int vertex1TexCoord2Index       = -1;
        int vertex1TexCoord3Index       = -1;
        int vertex1TexCoord4Index       = -1;
        
        int vertex2PositionIndex        = -1;
        int vertex2NormalIndex          = -1;
        int vertex2TexCoord1Index       = -1;
        int vertex2TexCoord2Index       = -1;
        int vertex2TexCoord3Index       = -1;
        int vertex2TexCoord4Index       = -1;
        
        int vertex3PositionIndex        = -1;
        int vertex3NormalIndex          = -1;
        int vertex3TexCoord1Index       = -1;
        int vertex3TexCoord2Index       = -1;
        int vertex3TexCoord3Index       = -1;
        int vertex3TexCoord4Index       = -1;


        PPolygon pPolygon;


        pPolygonMesh.beginBatch();



        //  Add all the Positions to the PolygonMesh.
        if (m_pPositionSemantic != null)
            populatePolygonMeshWithPositions(pPolygonMesh);

        //  Add all the Normals to the PolygonMesh.
        if (m_pNormalSemantic != null)
            populatePolygonMeshWithNormals(pPolygonMesh);

        //  Add all the TexCoords to the PolygonMesh.
        populatePolygonMeshWithTexCoords(pPolygonMesh);




        //  Loop through all the triangle indices.
        while (index < meshTriangles.length)
        {
            //  Read in data for Vertex1.
            if (m_pPositionSemantic != null)
                vertex1PositionIndex = meshTriangles[index + m_pPositionSemantic.m_Offset];
            if (m_pNormalSemantic != null)
                vertex1NormalIndex = meshTriangles[index + m_pNormalSemantic.m_Offset];
            if (m_texCoordSemanticArray[0] != null)
                vertex1TexCoord1Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[0].getVector2f(meshTriangles[index + m_texCoordSemanticArray[0].m_Offset]) );
            if (m_texCoordSemanticArray[1] != null)
                vertex1TexCoord2Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[1].getVector2f(meshTriangles[index + m_texCoordSemanticArray[1].m_Offset]) );
            if (m_texCoordSemanticArray[2] != null)
                vertex1TexCoord3Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[2].getVector2f(meshTriangles[index + m_texCoordSemanticArray[2].m_Offset]) );
            if (m_texCoordSemanticArray[3] != null)
                vertex1TexCoord4Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[3].getVector2f(meshTriangles[index + m_texCoordSemanticArray[3].m_Offset]) );
            index += m_VertexOffset;


            //  Read in data for Vertex2.
            if (m_pPositionSemantic != null)
                vertex2PositionIndex = meshTriangles[index + m_pPositionSemantic.m_Offset];
            if (m_pNormalSemantic != null)
                vertex2NormalIndex = meshTriangles[index + m_pNormalSemantic.m_Offset];
            if (m_texCoordSemanticArray[0] != null)
                vertex2TexCoord1Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[0].getVector2f(meshTriangles[index + m_texCoordSemanticArray[0].m_Offset]) );
            if (m_texCoordSemanticArray[1] != null)
                vertex2TexCoord2Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[1].getVector2f(meshTriangles[index + m_texCoordSemanticArray[1].m_Offset]) );
            if (m_texCoordSemanticArray[2] != null)
                vertex2TexCoord3Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[2].getVector2f(meshTriangles[index + m_texCoordSemanticArray[2].m_Offset]) );
            if (m_texCoordSemanticArray[3] != null)
                vertex2TexCoord4Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[3].getVector2f(meshTriangles[index + m_texCoordSemanticArray[3].m_Offset]) );
            index += m_VertexOffset;
            
            
            //  Read in data for Vertex3.
            if (m_pPositionSemantic != null)
                vertex3PositionIndex = meshTriangles[index + m_pPositionSemantic.m_Offset];
            if (m_pNormalSemantic != null)
                vertex3NormalIndex = meshTriangles[index + m_pNormalSemantic.m_Offset];
            if (m_texCoordSemanticArray[0] != null)
                vertex3TexCoord1Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[0].getVector2f(meshTriangles[index + m_texCoordSemanticArray[0].m_Offset]) );
            if (m_texCoordSemanticArray[1] != null)
                vertex3TexCoord2Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[1].getVector2f(meshTriangles[index + m_texCoordSemanticArray[1].m_Offset]) );
            if (m_texCoordSemanticArray[2] != null)
                vertex3TexCoord3Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[2].getVector2f(meshTriangles[index + m_texCoordSemanticArray[2].m_Offset]) );
            if (m_texCoordSemanticArray[3] != null)
                vertex3TexCoord4Index = pPolygonMesh.getTexCoord( m_texCoordSemanticArray[3].getVector2f(meshTriangles[index + m_texCoordSemanticArray[3].m_Offset]) );
            index += m_VertexOffset;

            //  Create a new Polygon.
            pPolygon = pPolygonMesh.createPolygon();

            pPolygon.beginBatch();

            //  Add the first Vertex to the Polygon.
            pPolygon.addVertex(vertex1PositionIndex,    //  PositionIndex
                               vertex1NormalIndex,      //  NormalIndex
                               vertex1TexCoord1Index,   //  TexCoord1Index
                               vertex1TexCoord2Index,                          //  TexCoord2Index
                               vertex1TexCoord3Index,                          //  TexCoord3Index
                               vertex1TexCoord4Index);                         //  TexCoord4Index

            //  Add the second Vertex to the Polygon.
            pPolygon.addVertex(vertex2PositionIndex,    //  PositionIndex
                               vertex2NormalIndex,      //  NormalIndex
                               vertex2TexCoord1Index,   //  TexCoord1Index
                               vertex2TexCoord2Index,                          //  TexCoord2Index
                               vertex2TexCoord3Index,                          //  TexCoord3Index
                               vertex2TexCoord4Index);                         //  TexCoord4Index

            //  Add the third Vertex to the Polygon.
            pPolygon.addVertex(vertex3PositionIndex,    //  PositionIndex
                               vertex3NormalIndex,      //  NormalIndex
                               vertex3TexCoord1Index,   //  TexCoord1Index
                               vertex3TexCoord2Index,                          //  TexCoord2Index
                               vertex3TexCoord3Index,                          //  TexCoord3Index
                               vertex3TexCoord4Index);                         //  TexCoord4Index

            pPolygon.endBatch();

            polygonIndex++;
        }

        if (m_pNormalSemantic != null && m_pNormalSemantic.getDataSize() > 0)
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
            {
                pPolygonMesh.setNumberOfTextures(3); // at least three....
                pPolygonMesh.setMaterial(pMaterial);
            }
        }
    }




    /**
     * Populates the PolygonMesh with Positions.
     * 
     * @param pPolygonMesh
     */
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

    /**
     * Populates the PolygonMesh with Normals.
     * 
     * @param pPolygonMesh
     */
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

    /**
     * Populates the PolygonMesh with TexCoords.
     * 
     * @param pPolygonMesh
     */
    void populatePolygonMeshWithTexCoords(PPolygonMesh pPolygonMesh)
    {
        for (int i = 0; i < m_texCoordSemanticArray.length; ++i)
        {
            if (m_texCoordSemanticArray[i] != null)
            {
                int TexCoordCount = m_texCoordSemanticArray[i].getVector2fCount();
                Vector2f pTexCoord;

                for (int j = 0; j < TexCoordCount; j++)
                {
                    pTexCoord = m_texCoordSemanticArray[i].getVector2f(j);

                    pPolygonMesh.addTexCoord(pTexCoord);
                }
            }
        }
    }
    
    

    /**
     * Gets the offset of an element in the vertex data.
     * 
     * @param elementName
     * @return int
     */
    int getElementOffset(String elementName)
    {
        int a;
        VertexDataSemantic pVertexDataSemantic;
        
        for (a=0; a<m_VertexDataSemantics.size(); a++)
        {
            pVertexDataSemantic = m_VertexDataSemantics.get(a);

            if (pVertexDataSemantic.m_Name.equals(elementName))
                return(pVertexDataSemantic.m_Offset);
        }

        return(-1);
    }
    

    /**
     * Finds the VertexDataSemantic with the specified name.
     * 
     * @param name
     * @return VertexDataSemantic
     */
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
    
    /**
     * Builds a VertexDataSemanticArray based on the contents of the collada
     * Triangles class.
     * 
     * @param pTriangles
     */
    private void buildVertexDataSemanticArray(Triangles pTriangles)
    {
        List<InputLocalOffset> inputs = pTriangles.getInputs();
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

    /**
     * Caches the VertexDataSemantics.
     */
    private void cacheVertexDataSemantics()
    {
        m_pPositionSemantic = findVertexDataSemantic("POSITION");
        m_pNormalSemantic = findVertexDataSemantic("NORMAL");
        
        int textureSemanticCount = 0;
        VertexDataSemantic semantic = null;
        
        for (int i = 0; i < m_VertexDataSemantics.size(); i++)
        {
            semantic = m_VertexDataSemantics.get(i);

            if (semantic.m_Name.equals("TEXCOORD"))
                m_texCoordSemanticArray[textureSemanticCount++] = semantic;
        }
        
        boolean breakpoint = false;
        breakpoint = !breakpoint;
        
    }

}



