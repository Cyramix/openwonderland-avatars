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
package imi.utils;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.parts.PGeometryTriangle;



public class PMathUtils
{
     /** A value to multiply a degree value by, to convert it to radians. */
    public static final float DEG_TO_RAD = (float)Math.PI / 180.0f;

    /** A value to multiply a radian value by, to convert it to degrees. */
    public static final float RAD_TO_DEG = 180.0f / (float)Math.PI;
    
    static public void min(Vector3f result, Vector3f a, Vector3f b)
    {
        result.x = Math.min(a.x, b.x);
        result.y = Math.min(a.y, b.y);
        result.z = Math.min(a.z, b.z);
    }
    
    static public void min(Vector3f result, Vector3f a)
    {
        result.x = Math.min(a.x, result.x);
        result.y = Math.min(a.y, result.y);
        result.z = Math.min(a.z, result.z);
    }
    
    static public void max(Vector3f result, Vector3f a, Vector3f b)
    {
        result.x = Math.max(a.x, b.x);
        result.y = Math.max(a.y, b.y);
        result.z = Math.max(a.z, b.z);
    }
    
    static public void max(Vector3f result, Vector3f a)
    {
        result.x = Math.max(a.x, result.x);
        result.y = Math.max(a.y, result.y);
        result.z = Math.max(a.z, result.z);
    }
    
    static public int clamp(int value, int ceiling)
    {
        if (value > ceiling)
            return ceiling;
        else
            return value;
    }
    
    static public float clamp(float value, float ceiling)
    {
        if (value > ceiling)
            return ceiling;
        else
            return value;
    }

    public static PMatrix lookAt(Vector3f target, Vector3f eyePosition, Vector3f worldUp)
    {
        PMatrix result = null;
        // perform lookat to focal point
        Vector3f forward = eyePosition.subtract(target).normalize();
        Vector3f right = worldUp.cross(forward).normalize();
        Vector3f realUp = forward.cross(right).normalize();
        Vector3f translation = (eyePosition);
        // load it up manually
        float[] floats = new float[16];
        floats[ 0] = right.x;  floats[ 1] = realUp.x;  floats[ 2] = forward.x;  floats[ 3] = translation.x;
        floats[ 4] = right.y;  floats[ 5] = realUp.y;  floats[ 6] = forward.y;  floats[ 7] = translation.y;
        floats[ 8] = right.z;  floats[ 9] = realUp.z;  floats[10] = forward.z;  floats[11] = translation.z;
        floats[12] = 0.0f;     floats[13] = 0.0f;      floats[14] = 0.0f;       floats[15] = 1.0f;

        result = new PMatrix(floats);
        return result;
    }
    
    public static PMatrix lookAt(Vector3f target, Vector3f eyePosition, Vector3f worldUp, float yOffset)
    {
        PMatrix result = null;
        // perform lookat to focal point
        Vector3f forward = eyePosition.subtract(target).normalize();
        forward.y += yOffset;
        forward.normalizeLocal();
        
        Vector3f right = worldUp.cross(forward).normalize();
        Vector3f realUp = forward.cross(right).normalize();
        Vector3f translation = (eyePosition);
        // load it up manually
        float[] floats = new float[16];
        floats[ 0] = right.x;  floats[ 1] = realUp.x;  floats[ 2] = forward.x;  floats[ 3] = translation.x;
        floats[ 4] = right.y;  floats[ 5] = realUp.y;  floats[ 6] = forward.y;  floats[ 7] = translation.y;
        floats[ 8] = right.z;  floats[ 9] = realUp.z;  floats[10] = forward.z;  floats[11] = translation.z;
        floats[12] = 0.0f;     floats[13] = 0.0f;      floats[14] = 0.0f;       floats[15] = 1.0f;

        result = new PMatrix(floats);
        return result;
    }
    
    /**
     * Inspired by code from http://jerome.jouvie.free.fr/OpenGl/Lessons/Lesson8.php
     * @param face
     * @param tangent
     * @param binormal
     * @param normal
     */
    public static void computeFaceTBNBasis(PGeometryTriangle face, Vector3f tangent, Vector3f binormal, Vector3f normal)
    {
        Vector3f p21  = face.m_Vertices[1].m_Position.subtract(face.m_Vertices[0].m_Position);  //p2-p1
        Vector3f p31  = face.m_Vertices[2].m_Position.subtract(face.m_Vertices[0].m_Position);  //p3-p1
        
        Vector2f uv21 = face.m_Vertices[1].m_TexCoords[0].subtract(face.m_Vertices[0].m_TexCoords[0]);       //uv2-uv1
        Vector2f uv31 = face.m_Vertices[2].m_TexCoords[0].subtract(face.m_Vertices[0].m_TexCoords[0]);       //uv3-uv1

        
        if(tangent != null)
        {
            tangent.set(p21.mult(uv31.getY()).subtract(p31.mult(uv21.getY())).normalize());
            //tangent.copyFrom( normalize(/*multiply(*/multiply(p21, uv31.getY()).subtract(multiply(p31, uv21.getY()))/*, f)*/));
        }
            
        if(binormal != null)
        {
            binormal.set(p31.mult(uv21.getX()).subtract(p21.mult(uv31.getX())).normalize());
            //binormal.copyFrom(normalize(/*multiply(*/multiply(p31, uv21.getX()).subtract(multiply(p21, uv31.getX()))/*, f)*/));
        }
        
        if(normal != null)
        {
            normal.set(tangent.cross(binormal).normalize());
            //normal.copyFrom( /*normalize(*/crossProduct(tangent, binormal)/*)*/);
        }
    }
    
    public static void generateTangentAndBinormal(PGeometryTriangle face, Vector3f tangent, Vector3f binormal)
    {
        //Note: mesh normals are already calculated (with normal smoothing).
        //      For demonstration here, I use the normal calculated from computeFaceTBNBasis.
        //
        /*Vector3f normal = mesh.normals[face.normalId[0]];
        computeFaceTBNBasis(mesh, face, tangent, binormal, null);*/
        Vector3f normal = new Vector3f();
        computeFaceTBNBasis(face, tangent, binormal, normal);

        //Gram-Schmidt orthogonalization
        tangent.subtract(normal.mult(normal.dot(tangent))).normalizeLocal();

        //Right handed TBN space ?
        boolean rightHandSystem = tangent.cross(binormal).dot(normal) >= 0;
        
        binormal = normal.cross(tangent);
        
        if(!rightHandSystem)
            binormal.mult(-1.0f);
    }
    
    
//    static public float calculateDistanceBetweenTwoPoints(Vector3f point1, Vector3f point2)
//    {
        // there is no need for this function, instead use:
//        return point1.distance(point2);
//    }
    
    //  Calculate the Normal of the Triangle.
    static public void calculateNormalOfTriangle(Vector3f pNormal,
						 Vector3f pVertice1,
						 Vector3f pVertice2,
						 Vector3f pVertice3)
    {
        
        Vector3f vector1 = new Vector3f();
        Vector3f vector2 = new Vector3f();

        pVertice2.subtract(pVertice1, vector1);
        pVertice3.subtract(pVertice1, vector2);

        vector1.cross(vector2, pNormal);

        Vector3f normalizedNormal = pNormal.normalize();

        pNormal.x = normalizedNormal.x;
        pNormal.y = normalizedNormal.y;
        pNormal.z = normalizedNormal.z;
    }
    
    static public float RandomFloatInRange(float from, float to)
    {
        // Calculate the scale factor
        float scale = to - from;
        // The offset equals 'from'
        // Great success! 
        return (float)Math.random() * scale + from; // (the random function returns a value between 0.0 and 1.0)
    }
    
    
    //  Lerps a Vector2f between two values.
    static public void lerpVector3f(Vector2f pResult, Vector2f a, Vector2f b, float fT)
    {
        pResult.x = a.x + (b.x - a.x) * fT;
        pResult.y = a.y + (b.y - a.y) * fT;
    }


    //  Lerps a Vector3f between two values.
    static public void lerpVector3f(Vector3f pResult, Vector3f a, Vector3f b, float fT)
    {
        pResult.x = a.x + (b.x - a.x) * fT;
        pResult.y = a.y + (b.y - a.y) * fT;        
        pResult.z = a.z + (b.z - a.z) * fT;        
    }

}
