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
package imi.loaders.ms3d;


import java.util.ArrayList;

import com.jme.math.Vector3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import imi.utils.BinaryFile;
import java.net.URL;
import org.lwjgl.util.vector.Vector4f;


/**
 *
 * @author Chris Nagle
 * @author Lou Hayt
 */


class MS3D_HEADER
{
    String              Id;             //  Always "MS3D000000"
    int                 Version;        //  3
}

class MS3D_VERTEX
{
    int 		Flags;                                  //  SELECTED | SELECTED2 | HIDDEN
    Vector3f            Position;                               //
    int                 BoneId;                                 //  -1 = no bone
    int                 ReferenceCount;
}

class MS3D_TRIANGLE
{
    int                 Flags;					//  SELECTED | SELECTED2 | HIDDEN
    int                 []VertexIndices = new int[3];		//
    Vector3f            []VertexNormals = new Vector3f[3];	//
    float               []s = new float[3];                     //
    float               []t = new float[3];                     //
    int                 SmoothingGroup;                         //  1 - 32
    int                 GroupIndex;				//

    public MS3D_TRIANGLE()
    {
        for (int a=0; a<3; a++)
            VertexNormals[a] = new Vector3f();
    }

}

class MS3D_EDGE
{
    int                 []EdgeIndices = new int[2];
}

class MS3D_GROUP
{
    int                 Flags;				//  SELECTED | HIDDEN
    String              Name;				//
    int                 NumTriangles;			//
    int                 []pTriangleIndices;		//  The groups group the triangles
    int                 MaterialIndex;			//  -1 = no material
}

class MS3D_MATERIAL
{
    String              Name;                           //
    Vector4f            Ambient;			//
    Vector4f            Diffuse;			//
    Vector4f            Specular;			//
    Vector4f            Emissive;			//
    float               Shininess;			//  0.0f - 128.0f
    float               Transparency;			//  0.0f - 1.0f
    int                 Mode;				//  0, 1, 2 is unused now
    String              Texture;			//  texture.bmp
    String              AlphaMap;			//  alpha.bmp
}

class MS3D_KEYFRAME_ROT
{
    float               fTime;                          //  time in seconds
    Vector3f            Rotation;                       //  x, y, z angles
}

class MS3D_KEYFRAME_TRANS
{
    float               fTime;                          //  Time in seconds
    Vector3f            Translation;                    //  Local translation
}

class MS3D_JOINT
{
    int                 Flags;                          //  SELECTED | DIRTY
    String              Name;
    String              ParentName;
    int                 ParentBoneIndex;

    Vector3f            JointRotation;
    Vector3f            JointPosition;

    Quaternion          InitialRotation = new Quaternion();
    Vector3f            InitialPosition = new Vector3f();

    //  Not read in, calculated.
    Matrix4f            mRelative = new Matrix4f();
    Matrix4f            mAbsolute = new Matrix4f();
    Matrix4f            mRelativeFinal = new Matrix4f();
    Matrix4f            mFinal = new Matrix4f();

    int         	NumKeyFramesRot;		//
    int                 NumKeyFramesTrans;		//
    MS3D_KEYFRAME_ROT   []pKeyFramesRot;                //  local animation matrices
    MS3D_KEYFRAME_TRANS []pKeyFramesTrans;      	//  local animation matrices
}




public class MS3DFile
{
    ArrayList   m_Vertices  = new ArrayList();

    ArrayList   m_Triangles = new ArrayList();

    ArrayList   m_Edges     = new ArrayList();

    ArrayList   m_Groups    = new ArrayList();

    ArrayList   m_Materials = new ArrayList();


    float   m_fAnimationFPS;
    float   m_fCurrentTime;
    int m_iTotalFrames;
    
    ArrayList           m_Joints = new ArrayList();
    
    //  Constructor.
    public MS3DFile()
    {
        m_fAnimationFPS		= 24.0f;
        m_fCurrentTime		= 0.0f;
        m_iTotalFrames		= 0;   
    }
    
    //  Gets the vertex count.
    public int getVertexCount()
    {
        return(m_Vertices.size());
    }
    
    //  Gets the vertex at the specified index.
    public MS3D_VERTEX getVertex(int Index)
    {
        return( (MS3D_VERTEX)m_Vertices.get(Index));
    }



    //  Gets the triangle count.
    public int getTriangleCount()
    {
        return(m_Triangles.size());
    }

    //  Gets the triangle at the specified index.
    public MS3D_TRIANGLE getTriangle(int Index)
    {
        return( (MS3D_TRIANGLE)m_Triangles.get(Index));
    }



    //  Gets the joint count.
    public int getJointCount()
    {
        return(m_Joints.size());
    }

    //  Gets the joint at the specified index.
    public MS3D_JOINT getJoint(int Index)
    {
        return( (MS3D_JOINT)m_Joints.get(Index));
    }




    //  Loads a file.
    public boolean load(URL location)
    {
        BinaryFile Stream = new BinaryFile();
	if (!Stream.open(location))
            return(false);
        
	boolean bResult = read(Stream);
        Stream.close();
        return(bResult);
    }

    
    boolean read(BinaryFile Stream)
    {
     	MS3D_HEADER header = new MS3D_HEADER();
	int i, j;
        MS3D_VERTEX pVertex = null;
        MS3D_TRIANGLE pTriangle = null;
        MS3D_GROUP pGroup = null;
        MS3D_MATERIAL pMaterial = null;
        MS3D_JOINT pJoint = null;
        MS3D_KEYFRAME_ROT pKeyFrameRot = null;
        MS3D_KEYFRAME_TRANS pKeyframeTrans = null;



        //  Read in the file header.
	header.Id = Stream.readString(10);
        header.Version = Stream.readInt();

	if (!header.Id.equals("MS3D000000"))
        {
            System.out.println("   File is not an '.ms3d' formatted file!");
            return(false);
        }

        if (header.Version != 4)
        {
            System.out.println("   '.ms3d' file is not version 4!");
            return(false);
        }

        
        //  ****************************
        //  Read in the vertices.
        //  ****************************
	int NumberOfVertices = Stream.readUShort();

        for (i=0; i<NumberOfVertices; i++)
        {
            pVertex = new MS3D_VERTEX();

            pVertex.Flags = Stream.readUChar();
            pVertex.Position = Stream.readVector3f();
            pVertex.BoneId = Stream.readChar();
            pVertex.ReferenceCount = Stream.readUChar();

            m_Vertices.add(pVertex);
        }



        //  ****************************
        //  Read in the Triangles
        //  ****************************
	int NumberOfTriangles = Stream.readUShort();

        for (i=0; i<NumberOfTriangles; i++)
        {
            pTriangle = new MS3D_TRIANGLE();
            
            pTriangle.Flags = Stream.readUShort();
            pTriangle.VertexIndices[0] = Stream.readUShort();
            pTriangle.VertexIndices[1] = Stream.readUShort();
            pTriangle.VertexIndices[2] = Stream.readUShort();
            pTriangle.VertexNormals[0] = Stream.readVector3f();
            pTriangle.VertexNormals[1] = Stream.readVector3f();
            pTriangle.VertexNormals[2] = Stream.readVector3f();

            //  Texture Coordinates seem to be flipped vertically.
            pTriangle.s[0] = Stream.readFloat();
            pTriangle.s[1] = Stream.readFloat();
            pTriangle.s[2] = Stream.readFloat();
            pTriangle.t[0] = 1.0f - Stream.readFloat();
            pTriangle.t[1] = 1.0f - Stream.readFloat();
            pTriangle.t[2] = 1.0f - Stream.readFloat();

            pTriangle.SmoothingGroup = Stream.readUChar();
            pTriangle.GroupIndex = Stream.readUChar();

            m_Triangles.add(pTriangle);
        }

/*
//  Skip reading edges. calculated, not stored in file.
class MS3D_EDGE
{
    int                 []EdgeIndices = new int[2];
}
*/
        
        //  ****************************
        //  Read in the Groups.
        //  ****************************
        int NumberOfGroups = Stream.readUShort();
	for (i=0; i<NumberOfGroups; i++)
	{
            pGroup = new MS3D_GROUP();

            pGroup.Flags = Stream.readUChar();
            pGroup.Name = Stream.readString(32);
            pGroup.NumTriangles = Stream.readUShort();
            pGroup.pTriangleIndices = new int[pGroup.NumTriangles];
            for (j=0; j<pGroup.NumTriangles; j++)
                pGroup.pTriangleIndices[j] = Stream.readUShort();
            pGroup.MaterialIndex = Stream.readChar();

            m_Groups.add(pGroup);
        }

        
        
        //  ****************************
	//  Read in the Materials.
        //  ****************************
	int NumberOfMaterials = Stream.readUShort();
        for (i=0; i<NumberOfMaterials; i++)
        {
            pMaterial = new MS3D_MATERIAL();

            pMaterial.Name = Stream.readString(32);
            pMaterial.Ambient = Stream.readVector4f();
            pMaterial.Diffuse = Stream.readVector4f();
            pMaterial.Specular = Stream.readVector4f();
            pMaterial.Emissive = Stream.readVector4f();
            pMaterial.Shininess = Stream.readFloat();
            pMaterial.Transparency = Stream.readFloat();
            pMaterial.Mode = Stream.readChar();
            pMaterial.Texture = Stream.readString(128);
            pMaterial.AlphaMap = Stream.readString(128);
        
            m_Materials.add(pMaterial);

/*
            System.out.println("Material:  '" + pMaterial.Name + "'");
            System.out.println("   Ambient:       (" + pMaterial.Ambient.x + ", " + pMaterial.Ambient.y + ", " + pMaterial.Ambient.z + ", " + pMaterial.Ambient.w + ")");
            System.out.println("   Diffuse:       (" + pMaterial.Diffuse.x + ", " + pMaterial.Diffuse.y + ", " + pMaterial.Diffuse.z + ", " + pMaterial.Diffuse.w + ")");
            System.out.println("   Specular:      (" + pMaterial.Specular.x + ", " + pMaterial.Specular.y + ", " + pMaterial.Specular.z + ", " + pMaterial.Specular.w + ")");
            System.out.println("   Emissive:      (" + pMaterial.Emissive.x + ", " + pMaterial.Emissive.y + ", " + pMaterial.Emissive.z + ", " + pMaterial.Emissive.w + ")");
            System.out.println("   Shininess:     " + pMaterial.Shininess);
            System.out.println("   Transparency:  " + pMaterial.Transparency);
            System.out.println("   Mode:          " + pMaterial.Mode);
            System.out.println("   Texture:       " + pMaterial.Texture);
            System.out.println("   AlphaMap:      " + pMaterial.AlphaMap);
*/
        }

        m_fAnimationFPS = Stream.readFloat();
	m_fCurrentTime = Stream.readFloat();
        m_iTotalFrames = Stream.readInt();


        
        //  ****************************
        //  Read in the Joints
        //  ****************************
        int NumberOfJoints = Stream.readUShort();
        Vector3f JointRotation = new Vector3f();
        Vector3f JointPosition = new Vector3f();

        
        for (i=0; i<NumberOfJoints; i++)
	{
            pJoint = new MS3D_JOINT();

            pJoint.Flags = Stream.readUChar();
            pJoint.Name = Stream.readString(32);
            pJoint.ParentName = Stream.readString(32);

            pJoint.JointRotation = Stream.readVector3f();
            pJoint.JointPosition = Stream.readVector3f();

            pJoint.InitialRotation.fromAngles(pJoint.JointRotation.x, pJoint.JointRotation.y, pJoint.JointRotation.z);

            pJoint.InitialPosition.set(pJoint.JointPosition);

            pJoint.NumKeyFramesRot = Stream.readUShort();
            pJoint.NumKeyFramesTrans = Stream.readUShort();

            pJoint.pKeyFramesRot = new MS3D_KEYFRAME_ROT[pJoint.NumKeyFramesRot];
            pJoint.pKeyFramesTrans = new MS3D_KEYFRAME_TRANS[pJoint.NumKeyFramesTrans];


            for (j=0; j<pJoint.NumKeyFramesRot; j++)
            {
                pKeyFrameRot = new MS3D_KEYFRAME_ROT();
                
                pKeyFrameRot.fTime = Stream.readFloat();
                pKeyFrameRot.Rotation = Stream.readVector3f();
                
                pJoint.pKeyFramesRot[j] = pKeyFrameRot;
            }

            for (j=0; j<pJoint.NumKeyFramesTrans; j++)
            {
                pKeyframeTrans = new MS3D_KEYFRAME_TRANS();
 
                pKeyframeTrans.fTime = Stream.readFloat();
                pKeyframeTrans.Translation = Stream.readVector3f();
                
                pJoint.pKeyFramesTrans[j] = pKeyframeTrans;
            }

            m_Joints.add(pJoint);
        }


        return(true);
    }
    
    
    
    //  Dumps the contents of the MS3DFile class.
    public void dump()
    {
	int i, j;
        MS3D_VERTEX pVertex = null;
        MS3D_TRIANGLE pTriangle = null;
        MS3D_GROUP pGroup = null;
        MS3D_MATERIAL pMaterial = null;
        MS3D_JOINT pJoint = null;
        MS3D_KEYFRAME_ROT pKeyframeRot = null;
        MS3D_KEYFRAME_TRANS pKeyframeTrans = null;
        

        //  ****************************
        //  Dump the vertices.
        //  ****************************
        System.out.println("Vertices:  " + m_Vertices.size());
        for (i=0; i<m_Vertices.size(); i++)
        {
            pVertex = (MS3D_VERTEX)m_Vertices.get(i);

            System.out.print("   Vertex[" + i + "]:  ");
            System.out.print("Flags=" + pVertex.Flags + ", ");
            System.out.print("Vertex=(" + pVertex.Position.x + ", " + pVertex.Position.y + ", " + pVertex.Position.z + "), ");
            System.out.print("BoneId='" + pVertex.BoneId + "', ");
            System.out.println("ReferenceCount=" + pVertex.ReferenceCount + ")");
        }


        //  ****************************
        //  Dump the Triangles
        //  ****************************
	System.out.println("Triangles:  " + m_Triangles.size());
        for (i=0; i<m_Triangles.size(); i++)
        {
            pTriangle = (MS3D_TRIANGLE)m_Triangles.get(i);

            System.out.print("   Triangle[" + i + "]:  ");
            System.out.print("Flags=" + pTriangle.Flags + ", ");
            System.out.print("VertexIndices=(" + pTriangle.VertexIndices[0] + ", " + pTriangle.VertexIndices[1] + ", " + pTriangle.VertexIndices[2] + "), ");
            System.out.print("VertexNormals=(" + pTriangle.VertexNormals[0] + ", " + pTriangle.VertexNormals[1] + ", " + pTriangle.VertexNormals[2] + "), ");
            System.out.print("s=(" + pTriangle.s[0] + ", " + pTriangle.s[1] + ", " + pTriangle.s[2] + "), ");
            System.out.print("t=(" + pTriangle.t[0] + ", " + pTriangle.t[1] + ", " + pTriangle.t[2] + "), ");
            System.out.print("SmoothingGroup=" + pTriangle.SmoothingGroup + ", ");
            System.out.println("GroupIndex=" + pTriangle.GroupIndex + ")");
        }


        //  ****************************
	//  Print out the Materials.
        //  ****************************
	System.out.println("Materials:  " + m_Materials.size());
        for (i=0; i<m_Materials.size(); i++)
        {
            pMaterial = (MS3D_MATERIAL)m_Materials.get(i);

            System.out.println("   Material[" + i + "]:");
            System.out.println("      Name:  '" + pMaterial.Name + "'");
            System.out.println("      Ambient:       (" + pMaterial.Ambient.x + ", " + pMaterial.Ambient.y + ", " + pMaterial.Ambient.z + ", " + pMaterial.Ambient.w + ")");
            System.out.println("      Diffuse:       (" + pMaterial.Diffuse.x + ", " + pMaterial.Diffuse.y + ", " + pMaterial.Diffuse.z + ", " + pMaterial.Diffuse.w + ")");
            System.out.println("      Specular:      (" + pMaterial.Specular.x + ", " + pMaterial.Specular.y + ", " + pMaterial.Specular.z + ", " + pMaterial.Specular.w + ")");
            System.out.println("      Emissive:      (" + pMaterial.Emissive.x + ", " + pMaterial.Emissive.y + ", " + pMaterial.Emissive.z + ", " + pMaterial.Emissive.w + ")");
            System.out.println("      Shininess:     " + pMaterial.Shininess);
            System.out.println("      Transparency:  " + pMaterial.Transparency);
            System.out.println("      Mode:          " + pMaterial.Mode);
            System.out.println("      Texture:       " + pMaterial.Texture);
            System.out.println("      AlphaMap:      " + pMaterial.AlphaMap);
        }

        System.out.println("AnimationFPS:  " + m_fAnimationFPS);
        System.out.println("CurrentTime:  " + m_fCurrentTime);
        System.out.println("TotalFrames:  " + m_iTotalFrames);   


        //  ****************************
        //  Print out the Groups.
        //  ****************************
        System.out.println("Groups:  " + m_Groups.size());
        for (i=0; i<m_Groups.size(); i++)
	{
            pGroup = (MS3D_GROUP)m_Groups.get(i);

            System.out.print("   Group[" + i + "]:  ");
            System.out.print("Name='" + pGroup.Name + "', ");
            System.out.print("Flags=" + pGroup.Flags + ", ");
            System.out.println("MaterialIndex=" + pGroup.MaterialIndex);
            System.out.println("      Triangles:  " + pGroup.NumTriangles);
            
            int TriangleIndex = 0;
            for (j=0; j<pGroup.NumTriangles; j++)
            {
                if (TriangleIndex == 0)
                    System.out.print("         (" + pGroup.pTriangleIndices[j] + ", ");
                else if (TriangleIndex == 15)
                {
                    System.out.println(pGroup.pTriangleIndices[j] + ")");
                    TriangleIndex = 0;
                }
                else
                    System.out.print(pGroup.pTriangleIndices[j] + ", ");
            }

            if (TriangleIndex != 0)
                System.out.println(")");
        }




        //  ****************************
	//  Print out the Joints.
        //  ****************************
	System.out.println("Joints:  " + m_Joints.size());
        for (i=0; i<m_Joints.size(); i++)
        {
            pJoint = (MS3D_JOINT)m_Joints.get(i);

            System.out.println("   Joint[" + i + "]:");
            System.out.println("      Name:           '" + pJoint.Name + "'");
            System.out.println("      Flags:          " + pJoint.Flags);
            System.out.println("      ParentName:     '" + pJoint.ParentName + "')");
            System.out.println("      JointRotation:  (" + pJoint.JointRotation.x + ", " + pJoint.JointRotation.y + ", " + pJoint.JointRotation.z + ")");
            System.out.println("      JointPosition:  (" + pJoint.JointPosition.x + ", " + pJoint.JointPosition.y + ", " + pJoint.JointPosition.z + ")");
            System.out.println("      NumKeyFramesRot:  " + pJoint.NumKeyFramesRot);
            
            for (j=0; j<pJoint.NumKeyFramesRot; j++)
            {
                pKeyframeRot = pJoint.pKeyFramesRot[j];
                
                System.out.print("         Keyframe[" + j + "]:  Time=" + pKeyframeRot.fTime + ", ");
                System.out.println("Rotation=(" + pKeyframeRot.Rotation.x + ", " + pKeyframeRot.Rotation.y + ", " + pKeyframeRot.Rotation.z + ")");
            }

            System.out.println("      NumKeyFramesTrans:  " + pJoint.NumKeyFramesTrans);
            for (j=0; j<pJoint.NumKeyFramesTrans; j++)
            {
                pKeyframeTrans = pJoint.pKeyFramesTrans[j];
                
                System.out.print("         Keyframe[" + j + "]:  Time=" + pKeyframeTrans.fTime + ", ");
                System.out.println("Translation=(" + pKeyframeTrans.Translation.x + ", " + pKeyframeTrans.Translation.y + ", " + pKeyframeTrans.Translation.z + ")");
            }
        }
    }
}



