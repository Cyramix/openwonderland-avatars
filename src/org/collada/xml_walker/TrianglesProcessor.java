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
import java.util.logging.Logger;
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
    private VertexDataSemantic m_pTexCoordSemantic = null;

    private int [] m_TriangleIndices = null;

    private String m_MaterialName = "";
    private PColladaMaterial m_pColladaMaterial = null; 




    //  Constructor.
    public TrianglesProcessor(Collada pCollada, Triangles pTriangles, Processor pParent)
    {
        super(pCollada, pTriangles, pParent);

        if (pParent instanceof MeshProcessor)
            m_pMeshProcessor = (MeshProcessor)pParent;

        logger.info("Polylist " + pTriangles.getName());

        getMaterial(pTriangles);

        buildVertexDataSemanticArray(pTriangles);
        cacheVertexDataSemantics();


//        System.out.println("VertexOffset:  " + m_VertexOffset);
//        System.out.flush();


        processTriangles(pTriangles.getP());
    }



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



    public int[] getTriangleIndices()
    {
        return(m_TriangleIndices);
    }


    public String getMeshName()
    {
        if (m_pMeshProcessor != null)
            return(m_pMeshProcessor.getName());
        return("");
    }

/*
    public void populatePolygonMesh(PPolygonMesh pPolygonMesh)
    {
        pPolygonMesh.setName(getMeshName());
       
        ArrayList meshPositions     = getPositions();
        ArrayList meshNormals       = getNormals();
        ArrayList meshTexCoord1s    = getTexCoord1s();
        int       []meshTriangles   = getTriangleIndices();

        int polygonIndex                = 0;
        int triangleIndiceIndex         = 0;
        int vertex1PositionIndex        = 0;
        int vertex1NormalIndex          = 0;
        int vertex1TexCoord1Index       = 0;
        int vertex2PositionIndex        = 0;
        int vertex2NormalIndex          = 0;
        int vertex2TexCoord1Index       = 0;
        int vertex3PositionIndex        = 0;
        int vertex3NormalIndex          = 0;
        int vertex3TexCoord1Index       = 0;

        Vector3f vertex1Position        = null;
        Vector3f vertex1Normal          = null;
        Vector2f vertex1TexCoord1       = null;
        Vector3f vertex2Position        = null;
        Vector3f vertex2Normal          = null;
        Vector2f vertex2TexCoord1       = null;
        Vector3f vertex3Position        = null;
        Vector3f vertex3Normal          = null;
        Vector2f vertex3TexCoord1       = null;

        int meshVertex1PositionIndex    = -1;
        int meshVertex1NormalIndex      = -1;
        int meshVertex1TexCoord1Index   = -1;
        int meshVertex2PositionIndex    = -1;
        int meshVertex2NormalIndex      = -1;
        int meshVertex2TexCoord1Index   = -1;
        int meshVertex3PositionIndex    = -1;
        int meshVertex3NormalIndex      = -1;
        int meshVertex3TexCoord1Index   = -1;

        PPolygon pPolygon;


        pPolygonMesh.beginBatch();


        //  Loop through all the triangle indices.
        while (triangleIndiceIndex < meshTriangles.length)
        {
            vertex1PositionIndex = meshTriangles[triangleIndiceIndex];
            triangleIndiceIndex++;
            if (m_bNormalsIncluded)
            {
                vertex1NormalIndex = meshTriangles[triangleIndiceIndex];
                triangleIndiceIndex++;
            }
            if (m_bTexCoord1sIncluded)
            {
                vertex1TexCoord1Index = meshTriangles[triangleIndiceIndex];
                triangleIndiceIndex++;
            }


            vertex2PositionIndex = meshTriangles[triangleIndiceIndex];
            triangleIndiceIndex++;
            if (m_bNormalsIncluded)
            {
                vertex2NormalIndex = meshTriangles[triangleIndiceIndex];
                triangleIndiceIndex++;
            }
            if (m_bTexCoord1sIncluded)
            {
                vertex2TexCoord1Index = meshTriangles[triangleIndiceIndex];
                triangleIndiceIndex++;
            }
            

            vertex3PositionIndex = meshTriangles[triangleIndiceIndex];
            triangleIndiceIndex++;
            if (m_bNormalsIncluded)
            {
                vertex3NormalIndex = meshTriangles[triangleIndiceIndex];
                triangleIndiceIndex++;
            }
            if (m_bTexCoord1sIncluded)
            {
                vertex3TexCoord1Index = meshTriangles[triangleIndiceIndex];
                triangleIndiceIndex++;
            }
            

            try
            {
                vertex1Position = (Vector3f)meshPositions.get(vertex1PositionIndex);
                if (m_bNormalsIncluded)
                   vertex1Normal = (Vector3f)meshNormals.get(vertex1NormalIndex);
                if (m_bTexCoord1sIncluded)
                    vertex1TexCoord1 = (Vector2f)meshTexCoord1s.get(vertex1TexCoord1Index);

                vertex2Position = (Vector3f)meshPositions.get(vertex2PositionIndex);
                if (m_bNormalsIncluded)
                    vertex2Normal = (Vector3f)meshNormals.get(vertex2NormalIndex);
                if (m_bTexCoord1sIncluded)
                    vertex2TexCoord1 = (Vector2f)meshTexCoord1s.get(vertex2TexCoord1Index);

                vertex3Position = (Vector3f)meshPositions.get(vertex3PositionIndex);
                if (m_bNormalsIncluded)
                    vertex3Normal = (Vector3f)meshNormals.get(vertex3NormalIndex);
                if (m_bTexCoord1sIncluded)
                    vertex3TexCoord1 = (Vector2f)meshTexCoord1s.get(vertex3TexCoord1Index);
            

                //  Get indices of the vertex parameters from the PolygonMesh.
                meshVertex1PositionIndex = pPolygonMesh.getPosition(vertex1Position);
                if (m_bNormalsIncluded)
                    meshVertex1NormalIndex = pPolygonMesh.getNormal(vertex1Normal);
                if (m_bTexCoord1sIncluded)
                    meshVertex1TexCoord1Index = pPolygonMesh.getTexCoord(vertex1TexCoord1);

                meshVertex2PositionIndex = pPolygonMesh.getPosition(vertex2Position);
                if (m_bNormalsIncluded)
                    meshVertex2NormalIndex = pPolygonMesh.getNormal(vertex2Normal);
                if (m_bTexCoord1sIncluded)
                    meshVertex2TexCoord1Index = pPolygonMesh.getTexCoord(vertex2TexCoord1);

                meshVertex3PositionIndex = pPolygonMesh.getPosition(vertex3Position);
                if (m_bNormalsIncluded)
                    meshVertex3NormalIndex = pPolygonMesh.getNormal(vertex3Normal);
                if (m_bTexCoord1sIncluded)
                    meshVertex3TexCoord1Index = pPolygonMesh.getTexCoord(vertex3TexCoord1);
            }
            catch (Exception e)
            {
                System.out.println("Exception occured in TrianglesProcessor.populatePolygonMesh()!");
                System.out.flush();
                e.printStackTrace();
            }


            //  Create a new Polygon.
            pPolygon = pPolygonMesh.createPolygon();

            pPolygon.beginBatch();

            //  Add the first Vertex to the Polygon.
            pPolygon.addVertex(meshVertex1PositionIndex,    //  PositionIndex
                               meshVertex1NormalIndex,      //  NormalIndex
                               meshVertex1TexCoord1Index,   //  TexCoord1Index
                               -1,                          //  TexCoord2Index
                               -1,                          //  TexCoord3Index
                               -1);                         //  TexCoord4Index

            //  Add the second Vertex to the Polygon.
            pPolygon.addVertex(meshVertex2PositionIndex,    //  PositionIndex
                               meshVertex2NormalIndex,      //  NormalIndex
                               meshVertex2TexCoord1Index,   //  TexCoord1Index
                               -1,                          //  TexCoord2Index
                               -1,                          //  TexCoord3Index
                               -1);                         //  TexCoord4Index

            //  Add the third Vertex to the Polygon.
            pPolygon.addVertex(meshVertex3PositionIndex,    //  PositionIndex
                               meshVertex3NormalIndex,      //  NormalIndex
                               meshVertex3TexCoord1Index,   //  TexCoord1Index
                               -1,                          //  TexCoord2Index
                               -1,                          //  TexCoord3Index
                               -1);                         //  TexCoord4Index

            pPolygon.endBatch();

            polygonIndex++;
        }

        if (m_bNormalsIncluded)
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
*/

    public void populatePolygonMesh(PPolygonMesh pPolygonMesh)
    {
        pPolygonMesh.setName(getMeshName());

        int []meshTriangles             = getTriangleIndices();

        int index                       = 0;
        int polygonIndex                = 0;
        int vertex1PositionIndex        = 0;
        int vertex1NormalIndex          = 0;
        int vertex1TexCoord1Index       = 0;
        int vertex2PositionIndex        = 0;
        int vertex2NormalIndex          = 0;
        int vertex2TexCoord1Index       = 0;
        int vertex3PositionIndex        = 0;
        int vertex3NormalIndex          = 0;
        int vertex3TexCoord1Index       = 0;

        Vector3f vertex1Position        = null;
        Vector3f vertex1Normal          = null;
        Vector2f vertex1TexCoord1       = null;
        Vector3f vertex2Position        = null;
        Vector3f vertex2Normal          = null;
        Vector2f vertex2TexCoord1       = null;
        Vector3f vertex3Position        = null;
        Vector3f vertex3Normal          = null;
        Vector2f vertex3TexCoord1       = null;

        int meshVertex1PositionIndex    = -1;
        int meshVertex1NormalIndex      = -1;
        int meshVertex1TexCoord1Index   = -1;
        int meshVertex2PositionIndex    = -1;
        int meshVertex2NormalIndex      = -1;
        int meshVertex2TexCoord1Index   = -1;
        int meshVertex3PositionIndex    = -1;
        int meshVertex3NormalIndex      = -1;
        int meshVertex3TexCoord1Index   = -1;

        PPolygon pPolygon;


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




        //  Loop through all the triangle indices.
        while (index < meshTriangles.length)
        {
            //  Read in data for Vertex1.
            if (m_pPositionSemantic != null)
                vertex1PositionIndex = meshTriangles[index + m_pPositionSemantic.m_Offset];
            if (m_pNormalSemantic != null)
                vertex1NormalIndex = meshTriangles[index + m_pNormalSemantic.m_Offset];
            if (m_pTexCoordSemantic != null)
                vertex1TexCoord1Index = meshTriangles[index + m_pTexCoordSemantic.m_Offset];
            index += m_VertexOffset;


            //  Read in data for Vertex2.
            if (m_pPositionSemantic != null)
                vertex2PositionIndex = meshTriangles[index + m_pPositionSemantic.m_Offset];
            if (m_pNormalSemantic != null)
                vertex2NormalIndex = meshTriangles[index + m_pNormalSemantic.m_Offset];
            if (m_pTexCoordSemantic != null)
                vertex2TexCoord1Index = meshTriangles[index + m_pTexCoordSemantic.m_Offset];
            index += m_VertexOffset;
            
            
            //  Read in data for Vertex3.
            if (m_pPositionSemantic != null)
                vertex3PositionIndex = meshTriangles[index + m_pPositionSemantic.m_Offset];
            if (m_pNormalSemantic != null)
                vertex3NormalIndex = meshTriangles[index + m_pNormalSemantic.m_Offset];
            if (m_pTexCoordSemantic != null)
                vertex3TexCoord1Index = meshTriangles[index + m_pTexCoordSemantic.m_Offset];
            index += m_VertexOffset;


            if (m_pMeshProcessor.getName().equals("mesh7-geometry"))
            {
                System.out.print("Triangle[" + polygonIndex + "] = (" + vertex1PositionIndex + ", " + vertex1NormalIndex + ", " + vertex1TexCoord1Index + ") ");
                System.out.print("(" + vertex2PositionIndex + ", " + vertex2NormalIndex + ", " + vertex2TexCoord1Index + ") ");
                System.out.println("(" + vertex3PositionIndex + ", " + vertex3NormalIndex + ", " + vertex3TexCoord1Index + ")");

                if (vertex3PositionIndex == 361)
                {
                    int a = 0;
                }
            }
            
/*
            try
            {
                //  Get 'Position' information for the 3 vertices.
                if (m_pPositionSemantic != null)
                {
                    vertex1Position = m_pPositionSemantic.getVector3f(vertex1PositionIndex);
                    vertex2Position = m_pPositionSemantic.getVector3f(vertex2PositionIndex);
                    vertex3Position = m_pPositionSemantic.getVector3f(vertex3PositionIndex);
                }

                //  Get 'Normal' information for the 3 vertices.
                if (m_pNormalSemantic != null && m_pNormalSemantic.getDataSize() > 0)
                {
                    vertex1Normal = m_pNormalSemantic.getVector3f(vertex1NormalIndex);
                    vertex2Normal = m_pNormalSemantic.getVector3f(vertex2NormalIndex);
                    vertex3Normal = m_pNormalSemantic.getVector3f(vertex3NormalIndex);
                }

                //  Get 'TexCoord' information for the 3 vertices.
                if (m_pTexCoordSemantic != null)
                {
                    vertex1TexCoord1 = m_pTexCoordSemantic.getVector2f(vertex1TexCoord1Index);
                    vertex2TexCoord1 = m_pTexCoordSemantic.getVector2f(vertex2TexCoord1Index);
                    vertex3TexCoord1 = m_pTexCoordSemantic.getVector2f(vertex3TexCoord1Index);
                }



                //  Get indices of the vertex parameters from the PolygonMesh.
                if (m_pPositionSemantic != null)
                {
                    meshVertex1PositionIndex = pPolygonMesh.addPosition(vertex1Position);
                    meshVertex2PositionIndex = pPolygonMesh.addPosition(vertex2Position);
                    meshVertex3PositionIndex = pPolygonMesh.addPosition(vertex3Position);
                }
                if (m_pNormalSemantic != null && m_pNormalSemantic.getDataSize() > 0)
                {
                    meshVertex1NormalIndex = pPolygonMesh.addNormal(vertex1Normal);
                    meshVertex2NormalIndex = pPolygonMesh.addNormal(vertex2Normal);
                    meshVertex3NormalIndex = pPolygonMesh.addNormal(vertex3Normal);
                }
                if (m_pTexCoordSemantic != null)
                {
                    meshVertex1TexCoord1Index = pPolygonMesh.addTexCoord(vertex1TexCoord1);
                    meshVertex2TexCoord1Index = pPolygonMesh.addTexCoord(vertex2TexCoord1);
                    meshVertex3TexCoord1Index = pPolygonMesh.addTexCoord(vertex3TexCoord1);
                }
            }
            catch (Exception e)
            {
                System.out.println("Exception occured in TrianglesProcessor.populatePolygonMesh()!");
                System.out.flush();
                e.printStackTrace();
            }
*/


            //  Create a new Polygon.
            pPolygon = pPolygonMesh.createPolygon();

            pPolygon.beginBatch();

            
            meshVertex1PositionIndex    = vertex1PositionIndex;
            meshVertex1NormalIndex      = vertex1NormalIndex;
            meshVertex1TexCoord1Index   = vertex1TexCoord1Index;
                               
            meshVertex2PositionIndex    = vertex2PositionIndex;
            meshVertex2NormalIndex      = vertex2NormalIndex;
            meshVertex2TexCoord1Index   = vertex2TexCoord1Index;

            meshVertex3PositionIndex    = vertex3PositionIndex;
            meshVertex3NormalIndex      = vertex3NormalIndex;
            meshVertex3TexCoord1Index   = vertex3TexCoord1Index;


//            System.out.print("   Triangle[" + polygonIndex + "] = (" + meshVertex1PositionIndex + ", " + meshVertex1NormalIndex + ", " + meshVertex1TexCoord1Index + ") - ");
//            System.out.print("(" + meshVertex2PositionIndex + ", " + meshVertex2NormalIndex + ", " + meshVertex2TexCoord1Index + ") - ");
//            System.out.println("(" + meshVertex3PositionIndex + ", " + meshVertex3NormalIndex + ", " + meshVertex3TexCoord1Index + ")");



            //  Add the first Vertex to the Polygon.
            pPolygon.addVertex(meshVertex1PositionIndex,    //  PositionIndex
                               meshVertex1NormalIndex,      //  NormalIndex
                               meshVertex1TexCoord1Index,   //  TexCoord1Index
                               -1,                          //  TexCoord2Index
                               -1,                          //  TexCoord3Index
                               -1);                         //  TexCoord4Index

            //  Add the second Vertex to the Polygon.
            pPolygon.addVertex(meshVertex2PositionIndex,    //  PositionIndex
                               meshVertex2NormalIndex,      //  NormalIndex
                               meshVertex2TexCoord1Index,   //  TexCoord1Index
                               -1,                          //  TexCoord2Index
                               -1,                          //  TexCoord3Index
                               -1);                         //  TexCoord4Index

            //  Add the third Vertex to the Polygon.
            pPolygon.addVertex(meshVertex3PositionIndex,    //  PositionIndex
                               meshVertex3NormalIndex,      //  NormalIndex
                               meshVertex3TexCoord1Index,   //  TexCoord1Index
                               -1,                          //  TexCoord2Index
                               -1,                          //  TexCoord3Index
                               -1);                         //  TexCoord4Index

//            if (meshVertex1PositionIndex == 396 || meshVertex2PositionIndex == 396 || meshVertex3PositionIndex == 396)
//            {
//                int aaa = 0;
//            }

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
    
    
    //  Gets the offset of an element in vertex data.
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

    private void cacheVertexDataSemantics()
    {
        m_pPositionSemantic = findVertexDataSemantic("POSITION");
        m_pNormalSemantic = findVertexDataSemantic("NORMAL");
        m_pTexCoordSemantic = findVertexDataSemantic("TEXCOORD");
    }

}



