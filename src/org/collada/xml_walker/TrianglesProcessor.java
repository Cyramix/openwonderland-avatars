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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
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

    private VertexDataSemantic m_positionSemantic = null;
    private VertexDataSemantic m_normalSemantic   = null;
    private VertexDataSemantic m_colorSemantic     = null;
    
    /** Make space for multiple sets of texture coordinates **/
    private VertexDataSemantic[] m_texCoordSemanticArray = new VertexDataSemantic[8];

    private int [] m_TriangleIndices = null;

    private String m_InstanceMaterialSymbol = null;
    private PColladaEffect m_effect = null;

    private final ColorRGBA color = new ColorRGBA();
    private final Vector3f  vecBuffer = new Vector3f();




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

        assignMaterial(pTriangles);

        buildVertexDataSemanticArray(pTriangles);
        cacheVertexDataSemantics();

        processTriangles(pTriangles.getP());
    }

    /**
     * Gets the Material assigned to the Triangles.
     * 
     * @param pTriangles
     */
    public void assignMaterial(Triangles trianglesData)
    {
        m_InstanceMaterialSymbol = null;
        if (trianglesData.getMaterial() != null)
        {
            m_InstanceMaterialSymbol = trianglesData.getMaterial();
            PColladaMaterialInstance materialInstance = m_colladaRef.findColladaMaterialInstanceBySymbol(m_InstanceMaterialSymbol);
            ColladaMaterial material = null;
            if (materialInstance != null)
                material = m_colladaRef.findColladaMaterialByIdentifier(materialInstance.getTargetMaterialURL());
            else
                logger.warning("Unable to lcoate material with symbol " + m_InstanceMaterialSymbol);
            if (material != null)
                m_effect = m_colladaRef.findColladaEffectByIdentifier(material.getInstanceEffectTargetURL());
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
        m_TriangleIndices = new int[triangleList.size()];
        int arrayIndex = 0;
        for (BigInteger bigInt : triangleList)
            m_TriangleIndices[arrayIndex++] = bigInt.intValue();
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
        return null;
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
        int vertex1ColorIndex           = -1;
        int vertex1TexCoord1Index       = -1;
        int vertex1TexCoord2Index       = -1;
        int vertex1TexCoord3Index       = -1;
        int vertex1TexCoord4Index       = -1;
        
        int vertex2PositionIndex        = -1;
        int vertex2NormalIndex          = -1;
        int vertex2ColorIndex           = -1;
        int vertex2TexCoord1Index       = -1;
        int vertex2TexCoord2Index       = -1;
        int vertex2TexCoord3Index       = -1;
        int vertex2TexCoord4Index       = -1;
        
        int vertex3PositionIndex        = -1;
        int vertex3NormalIndex          = -1;
        int vertex3ColorIndex           = -1;
        int vertex3TexCoord1Index       = -1;
        int vertex3TexCoord2Index       = -1;
        int vertex3TexCoord3Index       = -1;
        int vertex3TexCoord4Index       = -1;


        PPolygon polygon = null;


        pPolygonMesh.beginBatch();



        //  Add all the Positions to the PolygonMesh.
        if (m_positionSemantic != null)
            populatePolygonMeshWithPositions(pPolygonMesh);

        //  Add all the Normals to the PolygonMesh.
        if (m_normalSemantic != null)
            populatePolygonMeshWithNormals(pPolygonMesh);

        if (m_colorSemantic != null)
            populatePolygonMeshWithColors(pPolygonMesh);

        //  Add all the TexCoords to the PolygonMesh.
        populatePolygonMeshWithTexCoords(pPolygonMesh);




        //  Loop through all the triangle indices.
        while (index < meshTriangles.length)
        {
            //  Read in data for Vertex1.
            if (m_positionSemantic != null)
                vertex1PositionIndex = meshTriangles[index + m_positionSemantic.m_Offset];
            if (m_normalSemantic != null)
                vertex1NormalIndex = meshTriangles[index + m_normalSemantic.m_Offset];
            if (m_colorSemantic != null)
                vertex1ColorIndex = meshTriangles[index + m_colorSemantic.m_Offset];
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
            if (m_positionSemantic != null)
                vertex2PositionIndex = meshTriangles[index + m_positionSemantic.m_Offset];
            if (m_normalSemantic != null)
                vertex2NormalIndex = meshTriangles[index + m_normalSemantic.m_Offset];
            if (m_colorSemantic != null)
                vertex2ColorIndex = meshTriangles[index + m_colorSemantic.m_Offset];
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
            if (m_positionSemantic != null)
                vertex3PositionIndex = meshTriangles[index + m_positionSemantic.m_Offset];
            if (m_normalSemantic != null)
                vertex3NormalIndex = meshTriangles[index + m_normalSemantic.m_Offset];
            if (m_colorSemantic != null)
                vertex3ColorIndex = meshTriangles[index + m_colorSemantic.m_Offset];
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
            polygon = pPolygonMesh.createPolygon();

            polygon.beginBatch();
            
            //  Add the first Vertex to the Polygon.
            polygon.addVertex(vertex1PositionIndex,     //  PositionIndex
                               vertex1ColorIndex,       // Color
                               vertex1NormalIndex,      //  NormalIndex
                               vertex1TexCoord1Index,   //  TexCoord1Index
                               vertex1TexCoord2Index,   //  TexCoord2Index
                               vertex1TexCoord3Index,   //  TexCoord3Index
                               vertex1TexCoord4Index);  //  TexCoord4Index

            //  Add the second Vertex to the Polygon.
            polygon.addVertex(vertex2PositionIndex,     //  PositionIndex
                               vertex2ColorIndex,       // Color
                               vertex2NormalIndex,      //  NormalIndex
                               vertex2TexCoord1Index,   //  TexCoord1Index
                               vertex2TexCoord2Index,   //  TexCoord2Index
                               vertex2TexCoord3Index,   //  TexCoord3Index
                               vertex2TexCoord4Index);  //  TexCoord4Index

            //  Add the third Vertex to the Polygon.
            polygon.addVertex(vertex3PositionIndex,     //  PositionIndex
                               vertex3ColorIndex,       // Color
                               vertex3NormalIndex,      //  NormalIndex
                               vertex3TexCoord1Index,   //  TexCoord1Index
                               vertex3TexCoord2Index,   //  TexCoord2Index
                               vertex3TexCoord3Index,   //  TexCoord3Index
                               vertex3TexCoord4Index);  //  TexCoord4Index

            polygon.endBatch();

            polygonIndex++;
        }

        if (m_normalSemantic != null && m_normalSemantic.getDataSize() > 0)
            pPolygonMesh.endBatch(false);
        else
        {
            pPolygonMesh.setSmoothNormals(true);
            pPolygonMesh.endBatch();
        }

        if (m_effect != null)
        {
            PMeshMaterial pMaterial = m_effect.createMeshMaterial();
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
        int PositionCount = m_positionSemantic.getVector3fCount();

        for (a=0; a<PositionCount; a++)
        {
            m_positionSemantic.getVector3f(a, vecBuffer);
            
            pPolygonMesh.addPosition(vecBuffer);
        }
    }

    private void populatePolygonMeshWithColors(PPolygonMesh polygonMesh)
    {
        int colorCount = m_colorSemantic.getColorRGBACount();
        for (int i = 0; i < colorCount; i++)
        {
            m_colorSemantic.getColorRGBA(i, color);
            polygonMesh.addColor(color);
        }
    }

    /**
     * Populates the PolygonMesh with Normals.
     * 
     * @param pPolygonMesh
     */
    void populatePolygonMeshWithNormals(PPolygonMesh pPolygonMesh)
    {
        int NormalCount = m_normalSemantic.getVector3fCount();

        for (int i = 0; i < NormalCount; i++)
        {
            m_normalSemantic.getVector3f(i, vecBuffer);
            pPolygonMesh.addNormal(vecBuffer);
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
     * Finds the VertexDataSemantic with the specified name.
     * 
     * @param name
     * @return VertexDataSemantic
     */
    private VertexDataSemantic findVertexDataSemantic(String name)
    {
        VertexDataSemantic result = null;
        for (VertexDataSemantic semantic : m_VertexDataSemantics)
        {
            if (name.equals(semantic.m_Name))
            {
                result = semantic;
                break;
            }
        }

        return result;
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
        m_positionSemantic = findVertexDataSemantic("POSITION");
        m_normalSemantic = findVertexDataSemantic("NORMAL");
        m_colorSemantic = findVertexDataSemantic("COLOR");

        int textureSemanticCount = 0;
        VertexDataSemantic semantic = null;
        
        for (int i = 0; i < m_VertexDataSemantics.size(); i++)
        {
            semantic = m_VertexDataSemantics.get(i);
            String name = semantic.m_Name;
            if (name == null)
                name = semantic.m_DataName;
            if (name != null && name.equals("TEXCOORD"))
                m_texCoordSemanticArray[textureSemanticCount++] = semantic;
        }        
    }
}



