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

import imi.loaders.collada.Collada;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.collada.colladaschema.InputLocal;
import org.collada.colladaschema.InputLocalOffset;
import org.collada.colladaschema.Polygons;
import org.collada.colladaschema.Vertices;

/**
 * Processes a polygonal data
 * @author Ronald E Dahlgren
 */
public class PolygonsProcessor extends Processor
{
    /** Convenience reference to the processor that spawned us **/
    private MeshProcessor       m_meshProcessor = null;
    /** Track the material **/
    private PColladaEffect    m_meshEffect = null;
    /** List of semantics associated with the data pool **/
    private ArrayList<VertexDataSemantic> m_VertexDataSemantics = new ArrayList<VertexDataSemantic>();

    /** Convenience reference to the position semantic **/
    private VertexDataSemantic m_positionSemantic  = null;
    /** Convenience reference to the surface normal semantic **/
    private VertexDataSemantic m_normalSemantic    = null;
    /** Array of references to the texture coordinate semantics**/
    private VertexDataSemantic[] m_texCoordSemantic  = new VertexDataSemantic[4]; // Support for up to eight textures
    /** List of the indices for each polygon **/
    private ArrayList<ArrayList<BigInteger>> m_polyIndices = new ArrayList<ArrayList<BigInteger>>();
    /** Offset of the vertex data **/
    private int m_VertexSize = -1;
    /** Total number of polygons **/
    private int m_polyCount = -1;
    /**
     * Construct a new instance and process the provided list
     * @param pCollada
     * @param pPolylist
     * @param pParent
     */
    public PolygonsProcessor(Collada pCollada, Polygons polygonsData, Processor pParent)
    {
        super(pCollada, polygonsData, pParent);

        if (pParent instanceof MeshProcessor)
            m_meshProcessor = (MeshProcessor)pParent;

        // Determine how many polygons we are drawing
        m_polyCount = polygonsData.getCount().intValue();
        // Grab the material
        if (polygonsData.getMaterial() != null)
        { 
            String instanceSymbol = polygonsData.getMaterial();
            PColladaMaterialInstance materialInstance = m_colladaRef.findColladaMaterialInstanceBySymbol(instanceSymbol);
            ColladaMaterial material = m_colladaRef.findColladaMaterialByIdentifier(materialInstance.getTargetMaterialURL());
            m_meshEffect = m_colladaRef.findColladaEffectByIdentifier(material.getInstanceEffectTargetURL());
        }

        buildVertexDataSemanticArray(polygonsData);
        grabSemanticReferences();

        processPolygonIndices(polygonsData.getPhsAndPS());
    }

    private void processPolygonIndices(List<JAXBElement<?>> polygonIndicesList)
    {
        for (JAXBElement<?> element : polygonIndicesList)
        {
            if (element.getName().getLocalPart().equals("p"))
            {
                // for 'p' tags, just process directly
                ArrayList<BigInteger> newArray = new ArrayList<BigInteger>();
                newArray.addAll((ArrayList<BigInteger>)element.getValue());
                // add this to the master collection
                m_polyIndices.add(newArray);
            }
            else // for 'ph' tags, just process their child 'p' tags
            {
                Polygons.Ph phTag = null; // TODO : need test data to verify this
            }
        }
    }

    public ArrayList<ArrayList<BigInteger>> getPolygonIndices()
    {
        return m_polyIndices;
    }

    public String getMeshName()
    {
        if (m_meshProcessor != null)
            return(m_meshProcessor.getName());
        return null;
    }



    //  Populates a PolygonMesh with geometry data.
    public void populatePolygonMesh(PPolygonMesh polyMesh)
    {
        polyMesh.setName(getMeshName());

        int positionIndex           = -1;
        int normalIndex             = -1;
        int texCoord0Index          = -1;
        int texCoord1Index          = -1;
        int texCoord2Index          = -1;
        int texCoord3Index          = -1;

        PPolygon pPolygon           = null;


        // Begin work on the mesh
        polyMesh.beginBatch();

        //  Add all the Positions to the PolygonMesh.
        if (m_positionSemantic != null)
            populatePolygonMeshWithPositions(polyMesh);

        //  Add all the Normals to the PolygonMesh.
        if (m_normalSemantic != null)
            populatePolygonMeshWithNormals(polyMesh);

        //  Add all the TexCoords to the PolygonMesh.
        if (m_texCoordSemantic != null)
            populatePolygonMeshWithTexCoords(polyMesh);


        for (int polygonIndex=0; polygonIndex < m_polyCount; polygonIndex++)
        {
            //  Create a new Polygon.
            int indexIntoPrimitiveData = 0;
            pPolygon = polyMesh.createPolygon();
            pPolygon.beginBatch();

            int numberOfVerts = m_polyIndices.get(polygonIndex).size() / (m_VertexSize);
            for (int vertexIndex=0; indexIntoPrimitiveData < numberOfVerts; vertexIndex++)
            {
                if (m_positionSemantic != null)
                    positionIndex = m_polyIndices.get(polygonIndex).get(indexIntoPrimitiveData + m_positionSemantic.m_Offset).intValue();
                if (m_normalSemantic != null)
                    normalIndex = m_polyIndices.get(polygonIndex).get(indexIntoPrimitiveData + m_normalSemantic.m_Offset).intValue();
                if (m_texCoordSemantic[0] != null)
                    texCoord0Index = polyMesh.getTexCoord(m_texCoordSemantic[0].getVector2f(m_polyIndices.get(polygonIndex).get(indexIntoPrimitiveData + m_texCoordSemantic[0].m_Offset).intValue()));
                if (m_texCoordSemantic[1] != null)
                    texCoord1Index = polyMesh.getTexCoord(m_texCoordSemantic[1].getVector2f(m_polyIndices.get(polygonIndex).get(indexIntoPrimitiveData + m_texCoordSemantic[1].m_Offset).intValue()));
                if (m_texCoordSemantic[2] != null)
                    texCoord2Index = polyMesh.getTexCoord(m_texCoordSemantic[2].getVector2f(m_polyIndices.get(polygonIndex).get(indexIntoPrimitiveData + m_texCoordSemantic[2].m_Offset).intValue()));
                if (m_texCoordSemantic[3] != null)
                    texCoord3Index = polyMesh.getTexCoord(m_texCoordSemantic[3].getVector2f(m_polyIndices.get(polygonIndex).get(indexIntoPrimitiveData + m_texCoordSemantic[3].m_Offset).intValue()));

                indexIntoPrimitiveData += m_VertexSize;

                // Dereference the position and normal indices into mesh space
                positionIndex = polyMesh.getPosition(m_positionSemantic.getVector3f(positionIndex));
                normalIndex = polyMesh.getPosition(m_normalSemantic.getVector3f(normalIndex));
                //  Add the Vertex to the Polygon
                pPolygon.addVertex(positionIndex,       //  PositionIndex
                                   normalIndex,         //  NormalIndex
                                   texCoord0Index,      //  TexCoord1Index
                                   texCoord1Index,      //  TexCoord2Index
                                   texCoord2Index,      //  TexCoord3Index
                                   texCoord3Index);     //  TexCoord4Index
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

        if (m_meshEffect != null)
        {
            //  Create the Material to be assigned to the PolygonMesh.
            PMeshMaterial pMaterial = m_meshEffect.createMeshMaterial();
            if (pMaterial != null)
            {
                polyMesh.setNumberOfTextures(3); // hack code
                polyMesh.setMaterial(pMaterial);

            }
        }
    }




    //  Populates the PolygonMesh with Positions.
    void populatePolygonMeshWithPositions(PPolygonMesh polyMesh)
    {
        for (int i = 0; i < m_positionSemantic.getVector3fCount(); ++i)
            polyMesh.addPosition(m_positionSemantic.getVector3f(i));
    }

    //  Populates the PolygonMesh with Normals.
    void populatePolygonMeshWithNormals(PPolygonMesh polyMesh)
    {
        for (int i = 0; i < m_normalSemantic.getVector3fCount(); ++i)
            polyMesh.addNormal(m_normalSemantic.getVector3f(i));
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

    private void buildVertexDataSemanticArray(Polygons polygonsData)
    {
        // Grab the list of inpiut semantics
        List<InputLocalOffset> inputs = polygonsData.getInputs();

        String semanticAttribute    = null;
        String sourceName           = null;

        Vertices verts                      = null;
        VertexDataArray vertDataArray       = null;
        VertexDataSemantic vertDataSemantic = null;

        for(InputLocalOffset currentInput : inputs)
        {
            m_VertexSize = Math.max(currentInput.getOffset().intValue(), m_VertexSize);

            semanticAttribute = currentInput.getSemantic();

            sourceName = currentInput.getSource();
            if (sourceName.startsWith("#")) // Indicates a link
                sourceName = sourceName.substring(1);

            int semanticOffset = currentInput.getOffset().intValue();

            //  Get the Vertices with the specified identifier
            verts = m_meshProcessor.getVertices(sourceName);

            if (verts != null)
            {
                InputLocal inputLocal       = null;
                String inputLocalName       = null;
                String inputLocalSourceName = null;

                for (int i =0; i < verts.getInputs().size(); i++)
                {
                    inputLocal = verts.getInputs().get(i);

                    inputLocalName = inputLocal.getSemantic();
                    inputLocalSourceName = inputLocal.getSource();

                    if (inputLocalSourceName.startsWith("#"))
                        inputLocalSourceName = inputLocalSourceName.substring(1);

                    vertDataArray = m_meshProcessor.getVertexDataArray(inputLocalSourceName);

                    vertDataSemantic = new VertexDataSemantic(inputLocalName, semanticOffset, inputLocalSourceName, vertDataArray);

                    m_VertexDataSemantics.add(vertDataSemantic);
                }
            }
            else
            {
                //  Gets the VertexDataArray with the specified name.
                vertDataArray = m_meshProcessor.getVertexDataArray(sourceName);

                vertDataSemantic = new VertexDataSemantic(semanticAttribute, semanticOffset, sourceName, vertDataArray);

                m_VertexDataSemantics.add(vertDataSemantic);
            }
        }
        m_VertexSize++;
    }

    private void grabSemanticReferences()
    {
        m_positionSemantic = findVertexDataSemantic("POSITION");
        m_normalSemantic = findVertexDataSemantic("NORMAL");

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
