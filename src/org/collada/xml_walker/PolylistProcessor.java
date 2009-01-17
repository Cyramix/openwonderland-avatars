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
import org.collada.colladaschema.InputLocalOffset;
import org.collada.colladaschema.Polylist;
import org.collada.colladaschema.Vertices;
import org.collada.colladaschema.InputLocal;

import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.PMeshMaterial;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class PolylistProcessor extends Processor
{
    /** Store a convenience reference to the mesh processor **/
    private MeshProcessor m_pMeshProcessor = null;

    private int m_VertexOffset = 0;

    private ArrayList<VertexDataSemantic> m_VertexDataSemantics = new ArrayList<VertexDataSemantic>();

    private VertexDataSemantic m_positionSemantic  = null;
    private VertexDataSemantic m_normalSemantic    = null;
    private VertexDataSemantic m_colorSemantic     = null;
    private VertexDataSemantic[] m_texCoordSemantic  = new VertexDataSemantic[4]; // Support for up to eight textures


    private int[] m_PolygonVertexCounts = null;
    private int[] m_PolygonIndices = null;

    private String m_materialInstanceSymbol = null;
    private PColladaEffect m_effect = null;

    /** Work space **/
    private final Vector3f vecBuffer = new Vector3f();
    private final ColorRGBA colorBuffer = new ColorRGBA();

    //  Constructor.
    public PolylistProcessor(Collada pCollada, Polylist pPolylist, Processor pParent)
    {
        super(pCollada, pPolylist, pParent);

        if (pParent instanceof MeshProcessor)
            m_pMeshProcessor = (MeshProcessor)pParent;

        getMaterial(pPolylist);

        buildVertexDataSemanticArray(pPolylist);
        cacheVertexDataSemantics();

        processPolygonVertexCounts(pPolylist.getVcount());
        processPolygonIndices(pPolylist.getP());
    }
    
    public void getMaterial(Polylist polygonList)
    {
        if (polygonList.getMaterial() != null)
        {

            m_materialInstanceSymbol = polygonList.getMaterial();
            PColladaMaterialInstance matInst = m_colladaRef.findColladaMaterialInstanceBySymbol(m_materialInstanceSymbol);
            ColladaMaterial mat = m_colladaRef.findColladaMaterialByIdentifier(matInst.getTargetMaterialURL());
            m_effect = m_colladaRef.findColladaEffectByIdentifier(mat.getInstanceEffectTargetURL());
        }
    }

    private void processPolygonVertexCounts(List<BigInteger> polygonVertexCountsList)
    {
        m_PolygonVertexCounts = new int[polygonVertexCountsList.size()];
        int arrayIndex = 0;
        for (BigInteger bigInt : polygonVertexCountsList)
            m_PolygonVertexCounts[arrayIndex++] = bigInt.intValue();
    }

    private void processPolygonIndices(List<BigInteger> polygonIndicesList)
    {
        m_PolygonIndices = new int[polygonIndicesList.size()];
        int arrayIndex = 0;
        for (BigInteger bigInt : polygonIndicesList)
            m_PolygonIndices[arrayIndex++] = bigInt.intValue();
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
        return null;
    }


            
    //  Populates a PolygonMesh with geometry data.
    public void populatePolygonMesh(PPolygonMesh polyMesh)
    {
        polyMesh.setName(getMeshName());
        // Grab some convenience references
        int [] polygonVertexCounts  = getPolygonVertexCounts();
        int [] polygonIndices       = getPolygonIndices();

        int index                   = 0;
        int polygonVertexCount      = 0;
        int polygonIndex            = 0;
        int vertexIndex             = 0;

        int positionIndex           = -1;
        int colorIndex              = -1;
        int normalIndex             = -1;
        int texCoord0Index          = -1;
        int texCoord1Index          = -1;
        int texCoord2Index          = -1;
        int texCoord3Index          = -1;

        int meshPositionIndex       = -1;
        int meshNormalIndex         = -1;

        PPolygon pPolygon           = null;


        // Begin work on the mesh
        polyMesh.beginBatch();
        
        //  Add all the Positions to the PolygonMesh.
        if (m_positionSemantic != null)
            populatePolygonMeshWithPositions(polyMesh);

        //  Add all the Normals to the PolygonMesh.
        if (m_normalSemantic != null)
            populatePolygonMeshWithNormals(polyMesh);

        if (m_colorSemantic != null)
            populatePolygonMeshWithColors(polyMesh);

        //  Add all the TexCoords to the PolygonMesh.
        if (m_texCoordSemantic != null)
            populatePolygonMeshWithTexCoords(polyMesh);


        for (polygonIndex=0; polygonIndex<polygonVertexCounts.length; polygonIndex++)
        {
            polygonVertexCount = polygonVertexCounts[polygonIndex];


            //  Create a new Polygon.
            pPolygon = polyMesh.createPolygon();

            pPolygon.beginBatch();

            for (vertexIndex=0; vertexIndex<polygonVertexCount; vertexIndex++)
            {
                if (m_positionSemantic != null)
                    positionIndex = polygonIndices[index + m_positionSemantic.m_Offset];
                if (m_colorSemantic != null)
                    colorIndex = polygonIndices[index + m_colorSemantic.m_Offset];
                if (m_normalSemantic != null)
                    normalIndex = polygonIndices[index + m_normalSemantic.m_Offset];
                if (m_texCoordSemantic[0] != null)
                    texCoord0Index = polyMesh.getTexCoord(m_texCoordSemantic[0].getVector2f(polygonIndices[index + m_texCoordSemantic[0].m_Offset]));
                if (m_texCoordSemantic[1] != null)
                    texCoord1Index = polyMesh.getTexCoord(m_texCoordSemantic[1].getVector2f(polygonIndices[index + m_texCoordSemantic[1].m_Offset]));
                if (m_texCoordSemantic[2] != null)
                    texCoord2Index = polyMesh.getTexCoord(m_texCoordSemantic[2].getVector2f(polygonIndices[index + m_texCoordSemantic[2].m_Offset]));
                if (m_texCoordSemantic[3] != null)
                    texCoord3Index = polyMesh.getTexCoord(m_texCoordSemantic[3].getVector2f(polygonIndices[index + m_texCoordSemantic[3].m_Offset]));

                index += m_VertexOffset;

                meshPositionIndex = positionIndex;
                meshNormalIndex = normalIndex;

                //  Add the Vertex to the Polygon.
                pPolygon.addVertex(meshPositionIndex,       //  PositionIndex
                                   meshNormalIndex,         //  NormalIndex
                                   texCoord0Index,      //  TexCoord1Index
                                   texCoord1Index,                      //  TexCoord2Index
                                   texCoord2Index,                      //  TexCoord3Index
                                   texCoord3Index);                     //  TexCoord4Index
            }
            pPolygon.endBatch();
        }

        if (m_normalSemantic != null)
            polyMesh.endBatch(false);
        else
        {
            polyMesh.setSmoothNormals(true);
            polyMesh.endBatch();
        }


        //System.out.println("PolyList.MaterialName:  " + m_MaterialName);
        if (m_effect != null)
        {
            //  Create the Material to be assigned to the PolygonMesh.
            PMeshMaterial pMaterial = m_effect.createMeshMaterial();
            if (pMaterial != null)
            {
                polyMesh.setNumberOfTextures(3); // hack code
                polyMesh.setMaterial(pMaterial);
                
            }
        }
    }




    //  Populates the PolygonMesh with Positions.
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

    private void populatePolygonMeshWithColors(PPolygonMesh polyMesh)
    {
        int colorCount = m_colorSemantic.getColorRGBACount();
        for (int i = 0; i < colorCount; i++)
        {
            m_colorSemantic.getColorRGBA(i, colorBuffer);
            polyMesh.addColor(colorBuffer);
        }
    }

    //  Populates the PolygonMesh with Normals.
    void populatePolygonMeshWithNormals(PPolygonMesh pPolygonMesh)
    {
        int NormalCount = m_normalSemantic.getVector3fCount();

        for (int i = 0; i < NormalCount; i++)
        {
            m_normalSemantic.getVector3f(i, vecBuffer);
            pPolygonMesh.addNormal(vecBuffer);
        }
    }

    //  Populates the PolygonMesh with TexCoords.
    void populatePolygonMeshWithTexCoords(PPolygonMesh pPolygonMesh)
    {
        for (VertexDataSemantic texCoordSemantic : m_texCoordSemantic)
        {
            if (texCoordSemantic != null)
            {
                for (int i = 0; i < texCoordSemantic.getVector2fCount(); ++i)
                    pPolygonMesh.addTexCoord(texCoordSemantic.getVector2f(i));
            }
        }
    }


    //  Finds the VertexDataSemantic with the specified name.
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

    private void buildVertexDataSemanticArray(Polylist pPolylist)
    {
        List<InputLocalOffset> inputs = pPolylist.getInputs();
        String semanticAttribute = null;
        String sourceName = null;

        Vertices verts = null;
        VertexDataArray pDataArray = null;
        VertexDataSemantic pVertexDataSemantic = null;

        m_VertexOffset = 0;

        for(InputLocalOffset currentInput : inputs)
        {
            m_VertexOffset = Math.max(currentInput.getOffset().intValue(), m_VertexOffset);

            semanticAttribute = currentInput.getSemantic();
            sourceName = currentInput.getSource();
            if (sourceName.startsWith("#")) // Indicates a link
                sourceName = sourceName.substring(1);

            int semanticOffset = currentInput.getOffset().intValue();

            //  Get the Vertices with the specified identifier
            verts = m_pMeshProcessor.getVertices(sourceName);

            if (verts != null)
            {
                InputLocal pInputLocal = null;
                String inputLocalName = null;
                String inputLocalSourceName = null;

                for (int i =0; i < verts.getInputs().size(); i++)
                {
                    pInputLocal = verts.getInputs().get(i);
                    
                    inputLocalName = pInputLocal.getSemantic();
                    inputLocalSourceName = pInputLocal.getSource();
                    if (inputLocalSourceName.startsWith("#"))
                        inputLocalSourceName = inputLocalSourceName.substring(1);

                    pDataArray = m_pMeshProcessor.getVertexDataArray(inputLocalSourceName);

                    //  Constructor.
                    pVertexDataSemantic = new VertexDataSemantic(inputLocalName, semanticOffset, inputLocalSourceName, pDataArray);

                    m_VertexDataSemantics.add(pVertexDataSemantic);
                }
            }
            else
            {
                //  Gets the VertexDataArray with the specified name.
                pDataArray = m_pMeshProcessor.getVertexDataArray(sourceName);
            
                pVertexDataSemantic = new VertexDataSemantic(semanticAttribute, semanticOffset, sourceName, pDataArray);

                m_VertexDataSemantics.add(pVertexDataSemantic);
            }
        }

        m_VertexOffset++;
    }

    private void cacheVertexDataSemantics()
    {
        m_positionSemantic = findVertexDataSemantic("POSITION");
        m_normalSemantic = findVertexDataSemantic("NORMAL");
        m_colorSemantic = findVertexDataSemantic("COLOR");

        // Handle texture coordinates
        int textureSemanticCount = 0;
        VertexDataSemantic semantic = null;
        for (int i = 0; i < m_VertexDataSemantics.size(); i++)
        {
            semantic = m_VertexDataSemantics.get(i);

            if (semantic.m_Name.equals("TEXCOORD"))
                m_texCoordSemantic[textureSemanticCount++] = semantic;
        }
    }

}




