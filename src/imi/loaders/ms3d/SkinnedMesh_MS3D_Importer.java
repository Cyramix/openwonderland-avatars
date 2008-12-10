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
package imi.loaders.ms3d;


import com.jme.math.Vector2f;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.scene.PMatrix;
import imi.scene.PTransform;
import imi.scene.animation.AnimationCycle;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.MS3D_JointChannel;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.utils.tree.MS3D_ConverterHelper;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;



/**
 *
 * @author Chris Nagle
 * @author Lou Hayt
 */
public class SkinnedMesh_MS3D_Importer
{
    // Data build through the loading process
    private SkeletonNode            m_skeleton              = null;
    private PPolygonSkinnedMesh     m_pMesh                 = null;
    // The milkshape file to load
    private MS3DFile                m_pFile                 = null;
    // Internally used mappings for index translation
    HashMap<Integer, String>        MS3DJointIndexToName    = null;
    HashMap<String, Integer>        MS3DJointNameToBFTIndex = null;
    // Location of the animation cycle meta-data file
    URL                             m_animationMeta         = null;
    
    //  Constructor.
    public SkinnedMesh_MS3D_Importer()
    {

    }

    /**
     * Load the skinned milkshape model from the specified file.
     * @param skeleton The (already allocated) SkeletonNode to receive the
     * model's skeleton and the skinned mesh(es) as children
     * @param location The file to load
     * @return True on success, false otherwise
     */
    public boolean load(SkeletonNode skeleton, URL location)
    {
        return load(skeleton, location, 1.0f);
    }

    /**
     * Load the skinned milkshape model from the specified file.
     * @param skeleton The (already allocated) SkeletonNode to receive the
     * model's skeleton and the skinned mesh(es) as children
     * @param location The file to load
     * @param scale A uniform scale to be applied to the model through the
     * loading process.
     * @return True on success, false otherwise
     */
    public boolean load(SkeletonNode skeleton, URL location, float scale)
    {
        boolean bResult;
        m_pFile = new MS3DFile();
        
        bResult = m_pFile.load(location);
        if (!bResult)
            return(false);
        generateAnimationMetaURL(location);
       
        m_skeleton = skeleton;
        m_pMesh = new PPolygonSkinnedMesh("MS3DSkinnedMesh");
        
        m_pMesh.beginBatch();
        populateSkinnedMesh(scale);
        
        m_pMesh.setDirty(true, true);
        m_pMesh.endBatch();
       
        return(true);
    }

    /**
     * Loads the model at the file specified
     * @param location The file to load
     * @return The completed SkeletonNode on success, null on failure.
     */
    public SkeletonNode loadMS3D(URL location) 
    {
        generateAnimationMetaURL(location);
        if (load(new SkeletonNode("DefaultSkeleton : " + location.getFile()), location, 1.0f) == true) // success
            return m_skeleton;
        else
            return null;
    }

    private void generateAnimationMetaURL(URL location)
    {
        // generate the animation metadata filename
        StringBuilder animMeta = new StringBuilder(location.toString().substring(0,location.toString().lastIndexOf('.')));
        animMeta.append(".anm");
        try
        {
            m_animationMeta = new URL(animMeta.toString());
        } catch (MalformedURLException ex)
        {
            // oh no!
        }
    }

    /**
     * This method is the core of the loading process. 
     * @param scale A uniform scale to apply to the model data as it is read.
     */
    private void populateSkinnedMesh(float scale)
    {
        System.out.println("*SkinnedMesh_MS3D_Importer.populateSkinnedMesh()* @ scale: " + scale);
        ///////////////////////////////////////////////////////////////////
        // Animations
        PPolygonSkinnedMesh pSkinnedMesh = m_pMesh;

        int jointCount = m_pFile.getJointCount();
        
        MS3D_JOINT fileJoint;
        SkinnedMeshJoint pJoint;

        MS3DJointIndexToName = new HashMap<Integer, String>();

        AnimationGroup pAnimationGroup = new AnimationGroup();
        
        MS3D_JointChannel pJointChannel = null;

        for (int currentJointIndex=0; currentJointIndex < jointCount; currentJointIndex++) // For each joint
        {
            fileJoint = m_pFile.getJoint(currentJointIndex);

            //  Create the SkinnedMeshBone that will represent the Joint.
            pJoint = new SkinnedMeshJoint(new PTransform(new PMatrix(fileJoint.mRelative)));

            pJoint.set(fileJoint.Name,
                       fileJoint.ParentName,
                       fileJoint.JointPosition,
                       fileJoint.JointRotation.mult(scale));

            MS3DJointIndexToName.put(currentJointIndex, fileJoint.Name);

            // Dahlgren- These two lines needed to be added in order to properly 
            // Because the joint's "mRelative" matrix does not contain translation data...
            pJoint.getTransform().getLocalMatrix(true).setTranslation(fileJoint.JointPosition.mult(scale));
            // add this newly finsihed joint to the skeleton we are building
            m_skeleton.addSkinnedMeshJoint(pJoint.m_ParentJointName, pJoint); 
            // Gather animation data
            pJointChannel = new MS3D_JointChannel(pJoint.getName());
            //  Process all the rotation keyframes.
            if (fileJoint.NumKeyFramesRot > 0)
            {
                for (int i = 0; i < fileJoint.NumKeyFramesRot; i++)
                {
                    //  Add the RotationKeyframe.
                    pJointChannel.addRotationKeyframe(fileJoint.pKeyFramesRot[i].fTime,
                                                       fileJoint.pKeyFramesRot[i].Rotation);
                }
            }

            //  Process all the translation keyframes.
            if (fileJoint.NumKeyFramesTrans > 0)
            {
                for (int i = 0; i < fileJoint.NumKeyFramesTrans; i++)
                {
                    //  Add the TranslationKeyframe.
                    pJointChannel.addTranslationKeyframe(fileJoint.pKeyFramesTrans[i].fTime,
                                                          fileJoint.pKeyFramesTrans[i].Translation.mult(scale));
                }
            }

            pJointChannel.setBindPose(pJoint.getTransform().getLocalMatrix(false));

            //  Add the BoneAnimation.
            pAnimationGroup.getChannels().add(pJointChannel);
        }
        
        // Regenerate skeleton mappings (see implementation for details)
        m_skeleton.refresh();

        // Determine BFT (Breadth First Traversal) index
        MS3D_ConverterHelper processor = new MS3D_ConverterHelper();
        
        // BROKEN --- The animation states are now kept at the skeleton node!
        // Maps MS3D index to a joint name
//        pSkinnedMesh.getAnimationComponent().getGroups().add(pAnimationGroup);
        m_skeleton.getAnimationComponent().getGroups().add(pAnimationGroup);
//        // split the animation loop into cycles
        MS3DAnimationMetaData animationFile = new MS3DAnimationMetaData(m_animationMeta);
        
        AnimationCycle[] cycleArray = animationFile.getCycles();
        AnimationGroup group = m_skeleton.getAnimationComponent().getGroup();
        
        for (int i = 0; i < cycleArray.length; ++i)
            group.addCycle(cycleArray[i]);
        
        m_skeleton.getAnimationComponent().getGroup().calculateDuration();
        
        
        ///////////////////////////////////////////////////////////////////
        // Do triangles
        MS3D_TRIANGLE sourceTriangle;
        MS3D_VERTEX pVertex1, pVertex2, pVertex3;
        
        PPolygon                        targetTriangle        = null;
        PPolygonSkinnedVertexIndices    targetVertex1Indices  = null;
        PPolygonSkinnedVertexIndices    targetVertex2Indices  = null;
        PPolygonSkinnedVertexIndices    targetVertex3Indices  = null;
        
        ArrayList<Integer> indexSet = new ArrayList<Integer>();
        
        for (int currentTriangleIndex = 0; 
                 currentTriangleIndex < m_pFile.getTriangleCount();
                 currentTriangleIndex++)
        {
            sourceTriangle = m_pFile.getTriangle(currentTriangleIndex);
            targetTriangle = new PPolygon(m_pMesh);
            targetTriangle.beginBatch();

            //  Get the vertices making up the triangle.
            pVertex1 = m_pFile.getVertex(sourceTriangle.VertexIndices[0]);
            pVertex2 = m_pFile.getVertex(sourceTriangle.VertexIndices[1]);
            pVertex3 = m_pFile.getVertex(sourceTriangle.VertexIndices[2]);

            // allocate memory for the vertices on the target triangle
            targetVertex1Indices  = new PPolygonSkinnedVertexIndices();
            targetVertex2Indices  = new PPolygonSkinnedVertexIndices();
            targetVertex3Indices  = new PPolygonSkinnedVertexIndices();
            
            // add positions to the targetTriangle
            targetVertex1Indices.m_PositionIndex = m_pMesh.getPosition(pVertex1.Position.mult(scale));
            targetVertex2Indices.m_PositionIndex = m_pMesh.getPosition(pVertex2.Position.mult(scale));
            targetVertex3Indices.m_PositionIndex = m_pMesh.getPosition(pVertex3.Position.mult(scale));

            // add normals to the targetTriangle
            targetVertex1Indices.m_NormalIndex = m_pMesh.getNormal(sourceTriangle.VertexNormals[0]);
            targetVertex2Indices.m_NormalIndex = m_pMesh.getNormal(sourceTriangle.VertexNormals[1]);
            targetVertex3Indices.m_NormalIndex = m_pMesh.getNormal(sourceTriangle.VertexNormals[2]);

            // add texCoords to the targetTriangle (texture unit 0)
            targetVertex1Indices.m_TexCoordIndex[0] = m_pMesh.getTexCoord(new Vector2f(sourceTriangle.s[0], sourceTriangle.t[0]));
            targetVertex2Indices.m_TexCoordIndex[0] = m_pMesh.getTexCoord(new Vector2f(sourceTriangle.s[1], sourceTriangle.t[1]));
            targetVertex3Indices.m_TexCoordIndex[0] = m_pMesh.getTexCoord(new Vector2f(sourceTriangle.s[2], sourceTriangle.t[2]));
            
            // add colors
            targetVertex1Indices.m_ColorIndex = m_pMesh.getColor(ColorRGBA.white);
            targetVertex2Indices.m_ColorIndex = m_pMesh.getColor(ColorRGBA.white);
            targetVertex3Indices.m_ColorIndex = m_pMesh.getColor(ColorRGBA.white);

            ///////////////////////////////////////////////////////////////////
            // Animations
            // add indices to the set of influences for this mesh
            int indexOne   = duplicateCheckAndAdd(Integer.valueOf(convertIndex(pVertex1.BoneId)), indexSet);
            int indexTwo   = duplicateCheckAndAdd(Integer.valueOf(convertIndex(pVertex2.BoneId)), indexSet);
            int indexThree = duplicateCheckAndAdd(Integer.valueOf(convertIndex(pVertex3.BoneId)), indexSet);
            // Set the influence indices to an index relevant for this mesh, rather than the
            // entire skeleton. We perform rigid skinned weighting because all of the MS3D
            // source data we have encountered is rigidly skinned.
            targetVertex1Indices.m_BoneIndicesIndex = pSkinnedMesh.getBoneIndices(
                    new PBoneIndices(indexOne, 0, 0, 0));
            targetVertex1Indices.m_BoneWeightIndex  = pSkinnedMesh.getBoneWeights(new Vector3f(1.0f, 0.0f, 0.0f));

            targetVertex2Indices.m_BoneIndicesIndex = pSkinnedMesh.getBoneIndices(
                    new PBoneIndices(indexTwo, 0, 0, 0));
            targetVertex2Indices.m_BoneWeightIndex  = pSkinnedMesh.getBoneWeights(new Vector3f(1.0f, 0.0f, 0.0f));

            targetVertex3Indices.m_BoneIndicesIndex = pSkinnedMesh.getBoneIndices(
                    new PBoneIndices(indexThree, 0, 0, 0));
            targetVertex3Indices.m_BoneWeightIndex  = pSkinnedMesh.getBoneWeights(new Vector3f(1.0f, 0.0f, 0.0f));

            ///////////////////////////////////////////////////////////////////
            // add triangle to this polygon
            targetTriangle.addVertex(targetVertex1Indices);
            targetTriangle.addVertex(targetVertex2Indices);
            targetTriangle.addVertex(targetVertex3Indices);
            targetTriangle.endBatch();
            
            m_pMesh.addPolygon(targetTriangle);
        }
        // Behold, smooth normals!
        m_pMesh.setSmoothNormals(true);
        // finished with mesh construction
        m_pMesh.endBatch();
        // generate jME geometry
        m_pMesh.submit(new PPolygonTriMeshAssembler());
        // ready our influence list
        m_pMesh.setInfluenceIndices(indexSet.toArray());
        // attach to our skeleton
        m_skeleton.addChild(m_pMesh);
    }
    
    /**
     * This helper method wraps add calls to the given array list and
     * performs duplicate checking. The rationale behind not simply using
     * a set is the ease of iteration and conversion to an array. 
     * @param obj The object to add
     * @param list The list to dupe check
     * @return
     */
    private int duplicateCheckAndAdd(Object obj, ArrayList list)
    {
        for (int i = 0; i < list.size(); ++i)
        {
            if (obj.equals(list.get(i))) // found a match
                return i;
        }
        list.add(obj);
        return list.size() - 1;
    }
     
    /**
     * This converts the mapping of bone indices
     * @param MS3DIndex The Milkshape index
     * @return The BFT index (our version)
     */
    private int convertIndex(int MS3DIndex)
    {
        String jointName = MS3DJointIndexToName.get(MS3DIndex);
        return m_skeleton.getSkinnedMeshJointIndex(jointName);//MS3DJointNameToBFTIndex.get(jointName);
    }
    
    
}



