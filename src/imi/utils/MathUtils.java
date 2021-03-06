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
import imi.scene.polygonmodel.PGeometryTriangle;



public class MathUtils
{
     /** A value to multiply a degree value by, to convert it to radians. */
    public static final float DEG_TO_RAD = (float)Math.PI / 180.0f;

    /** A value to multiply a radian value by, to convert it to degrees. */
    public static final float RAD_TO_DEG = 180.0f / (float)Math.PI;

    public static Vector3f generateRandomNormalizedVector()
    {
        return new Vector3f(randomFloatInRange(-1.0f, 1.0f), randomFloatInRange(-1.0f, 1.0f), randomFloatInRange(-1.0f, 1.0f)).normalizeLocal();
    }
    
    public static Vector3f generateRandomNormalizedVectorXZOnly() 
    {
        float x = randomFloatInRange(-1.0f, 1.0f);
        float z = randomFloatInRange(-1.0f, 1.0f);
        return new Vector3f(x, 0.0f, z).normalizeLocal();
    }
    
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

    /**
     * Perform a look at with the given value/
     * @param target Target to look at
     * @param eyePosition Position of the observer
     * @param worldUp The world's up vector
     * @param resultOut Output
     * @param context context to operate within
     */
    public static void lookAt(Vector3f target, Vector3f eyePosition, Vector3f worldUp, PMatrix resultOut, MathUtilsContext context)
    {




//        // load it up manually
//        float[] floats = new float[16];
//        floats[ 0] = right.x;  floats[ 1] = realUp.x;  floats[ 2] = forward.x;  floats[ 3] = translation.x;
//        floats[ 4] = right.y;  floats[ 5] = realUp.y;  floats[ 6] = forward.y;  floats[ 7] = translation.y;
//        floats[ 8] = right.z;  floats[ 9] = realUp.z;  floats[10] = forward.z;  floats[11] = translation.z;
//        floats[12] = 0.0f;     floats[13] = 0.0f;      floats[14] = 0.0f;       floats[15] = 1.0f;
//
//        result = new PMatrix(floats);
//        resultOut.set(result);
        // perform lookat to focal point
        Vector3f forward = context.vectorOne;
        Vector3f right = context.vectorTwo;
        Vector3f realUp = context.vectorThree;
        Vector3f translation = context.vectorFour;
        // Calculate the forward as normalize(eye - target)
        //        Vector3f forward = eyePosition.subtract(target).normalize();
        forward.set(eyePosition);
        forward.subtractLocal(target);
        forward.normalizeLocal();
        // Calculate the right vector
        //        Vector3f right = worldUp.cross(forward).normalize();
        right.set(worldUp);
        right.crossLocal(forward); // y cross z = x
        right.normalizeLocal();
        // Calculate the up vector
        //        Vector3f realUp = forward.cross(right).normalize();
        realUp.set(forward);
        realUp.crossLocal(right); // z cross x = y
        realUp.normalizeLocal();
        // Grab the translation
        //        Vector3f translation = (eyePosition);
        translation.set(eyePosition);

        // Set it in the result
        float[] matrixFloats = context.floatArray;
        matrixFloats[0] = right.x; matrixFloats[1] = realUp.x; matrixFloats[2] = forward.x; matrixFloats[3] = translation.x;
        matrixFloats[4] = right.y; matrixFloats[5] = realUp.y; matrixFloats[6] = forward.y; matrixFloats[7] = translation.y;
        matrixFloats[8] = right.z; matrixFloats[9] = realUp.z; matrixFloats[10] =forward.z; matrixFloats[11] =translation.z;
        matrixFloats[12] = 0; matrixFloats[13] = 0; matrixFloats[14] = 0; matrixFloats[15] = 1;
        resultOut.set(matrixFloats);
    }

    /**
     * Perform a look at
     * @param target Target to look at
     * @param eyePosition The observer's position
     * @param worldUp The up axis of the world
     * @param yOffset Added to the forward vector
     * @param resultOut Calculation output
     * @param context Context to execute within
     */
    public static void lookAt(Vector3f target, Vector3f eyePosition, Vector3f worldUp, float yOffset, PMatrix resultOut, MathUtilsContext context)
    {
        // perform lookat to focal point
        Vector3f forward = context.vectorOne;
        Vector3f right = context.vectorTwo;
        Vector3f realUp = context.vectorThree;
        Vector3f translation = context.vectorFour;
        // Calculate the forward as normalize(eye - target)
        forward.set(target);
        forward.y += yOffset; // Account for the offset
        forward.subtractLocal(eyePosition);
        forward.normalizeLocal();
        // Calculate the right vector
        right.set(worldUp);
        right.crossLocal(forward); // y cross z = x
        right.normalizeLocal();
        // Calculate the up vector
        realUp.set(forward);
        realUp.crossLocal(right);
        realUp.normalizeLocal();
        // Grab the translation
        translation.set(eyePosition);

        // Set it in the result
        float[] matrixFloats = context.floatArray;
        matrixFloats[0] = right.x; matrixFloats[1] = realUp.x; matrixFloats[2] = forward.x; matrixFloats[3] = translation.x;
        matrixFloats[4] = right.y; matrixFloats[5] = realUp.y; matrixFloats[6] = forward.y; matrixFloats[7] = translation.y;
        matrixFloats[8] = right.z; matrixFloats[9] = realUp.z; matrixFloats[10] =forward.z; matrixFloats[11] =translation.z;
        matrixFloats[12] = 0; matrixFloats[13] = 0; matrixFloats[14] = 0; matrixFloats[15] = 1;
        resultOut.set(matrixFloats);

    }
    
    /**
     * Inspired by code from http://jerome.jouvie.free.fr/OpenGl/Lessons/Lesson8.php
     * @param face
     * @param tangent
     * @param binormal
     * @param normal
     */
    public static void computeFaceTBNBasis(PGeometryTriangle face,
                                            Vector3f tangent,
                                            Vector3f binormal,
                                            Vector3f normal,
                                            MathUtilsContext context)
    {
        
        face.verts[0].getPosition(context.vectorOne);
        face.verts[1].getPosition(context.vectorTwo);
        face.verts[2].getPosition(context.vectorThree);
        Vector3f p21  = context.vectorTwo.subtract(context.vectorOne);  //p2-p1
        Vector3f p31  = context.vectorThree.subtract(context.vectorOne);  //p3-p1

        // now grab the texture space vectors
        face.verts[0].getTexCoord(0, context.vector2dOne);
        face.verts[1].getTexCoord(0, context.vector2dTwo);
        face.verts[2].getTexCoord(0, context.vector2dThree);
        Vector2f uv21 = context.vector2dTwo.subtract(context.vector2dOne);       //uv2-uv1
        Vector2f uv31 = context.vector2dThree.subtract(context.vector2dOne);       //uv3-uv1

        tangent.set(p21.mult(uv31.getY()).subtract(p31.mult(uv21.getY())).normalize());
        binormal.set(p31.mult(uv21.getX()).subtract(p21.mult(uv31.getX())).normalize());
        normal.set(tangent.cross(binormal).normalize());
    }

    public static void generateTangentAndBinormal(PGeometryTriangle face, Vector3f tangent, Vector3f binormal, MathUtilsContext mathContext)
    {
        Vector3f normal = new Vector3f();
        computeFaceTBNBasis(face, tangent, binormal, normal, mathContext);

        //Gram-Schmidt orthogonalization
        tangent.subtract(normal.mult(normal.dot(tangent))).normalizeLocal();

        //Right handed TBN space ?
        boolean rightHandSystem = tangent.cross(binormal).dot(normal) >= 0;

        binormal = normal.cross(tangent);

        if(!rightHandSystem)
            binormal.mult(-1.0f);
    }

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
    
    static public float randomFloatInRange(float from, float to)
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
    
    /**
     * Retrieve a new context for use with the math utils.
     * @return
     */
    static public MathUtilsContext getContext()
    {
        return new MathUtilsContext();
    }

    /**
     * This class encapsulates the scratch space variables needed for calculations
     * within the math utils.
     */
    public static class MathUtilsContext
    {
        private final float[] floatArray = new float[16];
        private final Vector3f vectorOne = new Vector3f();
        private final Vector3f vectorTwo = new Vector3f();
        private final Vector3f vectorThree = new Vector3f();
        private final Vector3f vectorFour = new Vector3f();

        private final Vector2f vector2dOne = new Vector2f();
        private final Vector2f vector2dTwo = new Vector2f();
        private final Vector2f vector2dThree = new Vector2f();

        private MathUtilsContext()
        {

        }

        void reset()
        {
            for (int i = 0; i < 16; ++i)
                floatArray[i] = 0;
            vectorOne.set(Vector3f.ZERO);
            vectorTwo.set(Vector3f.ZERO);
            vectorThree.set(Vector3f.ZERO);
            vectorFour.set(Vector3f.ZERO);
        }
    }

}
