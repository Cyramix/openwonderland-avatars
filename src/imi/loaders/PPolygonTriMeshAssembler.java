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
package imi.loaders;

import imi.scene.polygonmodel.PPolygonSkinnedMesh;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PBoneIndices;
import imi.scene.polygonmodel.PGeometryVertex;
import imi.scene.polygonmodel.PPolygon;
import imi.scene.polygonmodel.PGeometryTriangle;
import imi.scene.polygonmodel.PPolygonSkinnedVertexIndices;
import imi.utils.MathUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastTable;

/**
 * Utility class used for converting various polygonal objects into jme
 * <code>TriMesh</code> representations.
 * @author Ronald E Dahlgren
 */
public class PPolygonTriMeshAssembler 
{
    /** Static logger reference **/
    private final static Logger logger = Logger.getLogger(PPolygonTriMeshAssembler.class.getName());
    
    /**
     * This function converts a <code>PPolygonModel</code> into a series of
     * <code>TriMesh</code> objects. The model may be <code>compile()</code>-ed
     * prior to the conversion if desired.
     * @param Model the model to convert
     * @param bCompile If true, the model will have its <code>compile()</code> 
     * method called prior to the conversion. This wil destroy any intended 
     * hierarchies using the model's composite meshes.
     * @return result (TriMesh[])
     */
//    public static TriMesh[] buildTriMeshes(PPolygonModel Model, boolean bCompile)
//    {
//        if (bCompile)
//            Model.compile();
//
//        TriMesh [] result = new TriMesh[Model.getChildrenCount()];
//        // Turn each child mesh into a TriMesh
//        for (int i = 0; i < Model.getChildrenCount(); i++)
//            result[i] = buildTriMesh((PPolygonMesh)Model.getChild(i));
//
//        return result;
//    }

    /**
     * Convert the provided mesh into a trimesh
     * @param Mesh
     * @return
     */
    public static TriMesh buildTriMesh(PPolygonMesh Mesh)
    {
        TriMesh result = new TriMesh();
        reconstructTriMesh(result, Mesh);
        return result;
    }

    /**
     * Reconstruct the provided trimesh to match the PPolygonMesh.
     * @param triMesh
     * @param Mesh
     */
    public static void reconstructTriMesh(TriMesh triMesh, PPolygonMesh Mesh)
    {
        // Different logic depending on the actual type
        if (Mesh instanceof PPolygonSkinnedMesh)
            reconstructTriMeshWithSkinningData(triMesh, (PPolygonSkinnedMesh)Mesh);
        else
            reconstructTriMeshWithoutSkinningData(triMesh, Mesh);
    }
    
    /**
     * Reconstruct a trimesh and do not attempt to extract skinning data from the mesh.
     * @param triMesh The triMesh to be reconstructed
     * @param Mesh The source data
     */
    private static void reconstructTriMeshWithoutSkinningData(TriMesh triMesh, PPolygonMesh Mesh)
    {
        MathUtils.MathUtilsContext context = MathUtils.getContext();
        if (Mesh.getPolygonCount() <= 0) // No geometry
        {
            logger.log(Level.SEVERE,
                        "Attempted to reconstruct a TriMesh from a mesh with no polygons!.");
            return;
        }
        
        // Buffers for containing the converted Mesh data
        PGeometryVertexBuffer   VertBuffer  = new PGeometryVertexBuffer();
        FastTable<Integer>      IndexBuffer = new FastTable<Integer>();
        PGeometryVertex vert = null;

        // Loop through each polygon, and build appropriate verts for it
        // For each Polygon
        for (PPolygon currentPoly : Mesh.getPolygons())
        {
            // For each triangle in the polygon
            for (int triIndex = 0; triIndex < currentPoly.getTriangleCount(); ++triIndex)
            {
                PGeometryTriangle curTri = new PGeometryTriangle();
                currentPoly.getTriangle(triIndex, curTri);

                // Generate the binormal and tangent for each vert in this triangle
                Vector3f binormal = new Vector3f();
                Vector3f tangent = new Vector3f();
                MathUtils.generateTangentAndBinormal(curTri, tangent, binormal, context);
                
                // For each vertex in the triangle
                for (int vertIndex = 0; vertIndex <3 ; ++vertIndex)
                {
                    // Out with the old
                    vert = new PGeometryVertex(curTri.verts[vertIndex]);
                    vert.setTangent(tangent);
                    // Now add this newly filled out vertex to the vertex buffer
                    int index = VertBuffer.addVertex(vert);
                    // Note the index
                    IndexBuffer.add(index);
                } // End vertex loop
            } // End triangle loop
        } // End polygon loop
    
        // Positions, normals and colors are an easy copy
        Vector3f[]  positions      = VertBuffer.getPositionArray();
        Vector3f[]  normals        = VertBuffer.getNormalArray();
        ColorRGBA[] colors         = VertBuffer.getDiffuseColorArray();
        
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(positions);
        FloatBuffer normalBuffer   = BufferUtils.createFloatBuffer(normals);
        FloatBuffer tangentBuffer  = BufferUtils.createFloatBuffer(VertBuffer.getTangentArray());
        FloatBuffer colorBuffer    = BufferUtils.createFloatBuffer(colors);
        
        ArrayList<TexCoords> textureCoordinates = new ArrayList<TexCoords>();
        
        for (int i = 0; i < Mesh.getNumberOfTextures(); ++i)
            textureCoordinates.add(TexCoords.makeNew(VertBuffer.getTextureCoordinateArray(i)));
        // Copy over indices
        int[] indices = new int[IndexBuffer.size()];
        for (int i = 0; i < IndexBuffer.size(); ++i)
            indices[i] = IndexBuffer.get(i).intValue();
        IntBuffer   indexBuffer    = BufferUtils.createIntBuffer(indices);
        
        triMesh.setName(Mesh.getName());
        // Rebuild the triMesh with the new data
        triMesh.reconstruct(positionBuffer, normalBuffer, colorBuffer, textureCoordinates.get(0), indexBuffer);
        triMesh.setTangentBuffer(tangentBuffer);
        
        if (Mesh.isUniformTexCoords())
        {
            for (int i = 1; i < Mesh.getNumberOfTextures(); i++)
                triMesh.setTextureCoords(textureCoordinates.get(0), i);
        }
        else
            triMesh.setTextureCoords(textureCoordinates);

        if (triMesh.getVBOInfo() != null)
            triMesh.getVBOInfo().resizeTextureIds(textureCoordinates.size());
    }
    
    /**
     * This method is used to generate trimesh data from a skinned mesh. Vertex
     * colors are lost, as this attribute is used to push weight data to the
     * vertex deformer.
     * @param triMesh The trimesh to reconstruct
     * @param Mesh The source data.
     */
    private static void reconstructTriMeshWithSkinningData(TriMesh triMesh, PPolygonSkinnedMesh Mesh)
    {
        if (Mesh.getPolygonCount() <= 0) // No source data
        {
            logger.log(Level.WARNING,
                    "Attempted to reconstruct TriMesh from a mesh with no polygons!");
            return;
        }
        if (Mesh.getBoneWeightArray() == null && Mesh.getBoneIndexArray() == null)
        {
            logger.log(Level.SEVERE, "reconstructTriMeshWithSkinningData was called on null data (mesh was already reconstructed before?)");
            return;
        }

        MathUtils.MathUtilsContext mathContext = MathUtils.getContext();
        
        // Buffers for containing the converted Mesh data
        PGeometryVertexBuffer VertBuffer = new PGeometryVertexBuffer();
        FastTable<Integer> IndexBuffer = new FastTable<Integer>();
        
        // Skinning data
        ColorRGBA[] weightArray     = new ColorRGBA[Mesh.getTessalatedVertexCount()]; // per vertex, weight of 4 influence from the indexed materices\bones
        float[]     boneIndexArray  = new float[Mesh.getTessalatedVertexCount() * 4]; // per vertex, 4 indices of bones in the flatened matrix stack
        
        List<Vector3f> sourceWeightFastTable = (List)Mesh.getBoneWeightArray();
        List<PBoneIndices> sourceIndexFastTable = (List)Mesh.getBoneIndexArray();
        
        PGeometryVertex vert = null;
        // Loop through each polygon, and build appropriate verts for it
        // For each Polygon
        for (PPolygon currentPoly : Mesh.getPolygons())
        {
            // For each triangle in the polygon
            for (int triIndex = 0; triIndex < currentPoly.getTriangleCount(); ++triIndex)
            {
                PGeometryTriangle curTri = new PGeometryTriangle();
                currentPoly.getTriangle(triIndex, curTri);

                // Generate the binormal for each vert in this triangle
                Vector3f binormal = new Vector3f();
                Vector3f tangent = new Vector3f();
                MathUtils.generateTangentAndBinormal(curTri, tangent, binormal, mathContext);
                
                // For each vertex in the triangle
                for (int vertIndex = 0; vertIndex < 3; ++vertIndex) 
                {
                    // Out with the old
                    vert = new PGeometryVertex(curTri.verts[vertIndex]);
                    vert.setTangent(tangent);
                    // Now add this newly filled out vertex to the vertex buffer
                    int index = VertBuffer.addVertex(vert);
                    // Note the index
                    IndexBuffer.add((Integer)index);
                    
                    // Extract skinning data and add it to our lists
                    int boneIndex = index * 4; // Four influences per vert
                    if (triIndex == 0)
                    {
                        PPolygonSkinnedVertexIndices skinnedVert = (PPolygonSkinnedVertexIndices)currentPoly.getVertex(vertIndex);
                        weightArray[index] = new ColorRGBA(sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).x,
                                sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).y,
                                sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).z,
                                0.0f);
                       
                        boneIndexArray[boneIndex]   = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[0];
                        boneIndexArray[boneIndex+1] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[1];
                        boneIndexArray[boneIndex+2] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[2];
                        boneIndexArray[boneIndex+3] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[3];
                    }
                    else
                    {
                        // The magic number featured below is due to the process that triangles are
                        // generated by a PPolygon. See the implementation for more details.
                        if (vertIndex == 2)  
                        {
                            PPolygonSkinnedVertexIndices skinnedVert = (PPolygonSkinnedVertexIndices)currentPoly.getVertex(vertIndex);
                            weightArray[index] = new ColorRGBA(sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).x,
                                sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).y,
                                sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).z,
                                0.0f);

                            boneIndexArray[boneIndex]   = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[0];
                            boneIndexArray[boneIndex+1] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[1];
                            boneIndexArray[boneIndex+2] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[2];
                            boneIndexArray[boneIndex+3] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[3];
                        }
                        else
                        {
                            PPolygonSkinnedVertexIndices skinnedVert = (PPolygonSkinnedVertexIndices)currentPoly.getVertex(triIndex + vertIndex);
                            weightArray[index] = new ColorRGBA(sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).x,
                                sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).y,
                                sourceWeightFastTable.get(skinnedVert.m_BoneWeightIndex).z,
                                0.0f);
                       
                            boneIndexArray[boneIndex]   = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[0];
                            boneIndexArray[boneIndex+1] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[1];
                            boneIndexArray[boneIndex+2] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[2];
                            boneIndexArray[boneIndex+3] = sourceIndexFastTable.get(skinnedVert.m_BoneIndicesIndex).index[3];
                        }
                    }
                    
                } // End vertex loop
            } // End triangle loop
        } // End polygon loop
        // Processing complete
        // Diagnostic / Debugging info
//        logger.log(Level.INFO, "Contents of Vertex Buffer:\n");
//        for (int i = 0; i < VertBuffer.size(); i++)
//            logger.log(Level.INFO, VertBuffer.getVertex(i).toString());
//        System.out.println("Contents of Index Buffer:\n");
//        for (int i = 0; i < IndexBuffer.m_Indices.size(); i++)
//        {
//            logger.log(Level.INFO, IndexBuffer.m_Indices.get(i).toString());
//            logger.log(Level.INFO, "\n");
//        }
        
    
        // Set skin data in PPolygonSkinnedMesh
        Mesh.setSkinningData(null, BufferUtils.createFloatBuffer(boneIndexArray));


        // Positions, normals and colors are an easy copy
        Vector3f[]  positions      = VertBuffer.getPositionArray();
        Vector3f[]  normals        = VertBuffer.getNormalArray();
        
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(positions);
        FloatBuffer normalBuffer   = BufferUtils.createFloatBuffer(normals);
        FloatBuffer tangentBuffer  = BufferUtils.createFloatBuffer(VertBuffer.getTangentArray());
        TexCoords texCoordBuffer   = TexCoords.makeNew(VertBuffer.getTextureCoordinateArray(0));

        int[] indices = new int[IndexBuffer.size()];
        for (int i = 0; i < IndexBuffer.size(); ++i)
            indices[i] = IndexBuffer.get(i).intValue();
        IntBuffer   indexBuffer    = BufferUtils.createIntBuffer(indices);
        
        // Apply data to TriMesh
        triMesh.setName(Mesh.getName());
        
        // Rebuild the triMesh with the new data
        triMesh.reconstruct(positionBuffer, normalBuffer, BufferUtils.createFloatBuffer(weightArray), texCoordBuffer, indexBuffer);
        triMesh.setTangentBuffer(tangentBuffer);
        triMesh.setBinormalBuffer(Mesh.getBoneIndexBuffer());
    }

}
