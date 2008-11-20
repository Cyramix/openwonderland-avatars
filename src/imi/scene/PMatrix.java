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
package imi.scene;

import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * The most awesome matrix class ever.
 * @author Lou Hayt
 * @author Ronald Dahlgren
 * @author Sun Microsystems
 */
public class PMatrix 
{
    private float[] mat  = new float[16];
    
    static public PMatrix IDENTITY = new PMatrix();
    
    private float[] rot    = null;
    private float[] scales = null;
    
    private boolean autoNormalize = false;	// Don't auto normalize by default
    
    // Unknown until lazy classification is done
    private int type = 0;
    
    // Dirty bit for classification, this is used
    // for classify()
    private static final int AFFINE_BIT     = 0x01;
    private static final int ORTHO_BIT      = 0x02;
    private static final int CONGRUENT_BIT  = 0x04;
    private static final int RIGID_BIT      = 0x08;
    private static final int CLASSIFY_BIT   = 0x10;

    // this is used for scales[], rot[]
    private static final int SCALE_BIT      = 0x20;
    private static final int ROTATION_BIT   = 0x40;
    // set when SVD renormalization is necessary
    private static final int SVD_BIT        = 0x80;

    private static final int CLASSIFY_ALL_DIRTY = AFFINE_BIT |
                                                  ORTHO_BIT |
                                                  CONGRUENT_BIT |
                                                  RIGID_BIT |
                                                  CLASSIFY_BIT;
    private static final int ROTSCALESVD_DIRTY = SCALE_BIT |
                                                  ROTATION_BIT |
                                                  SVD_BIT;
    private static final int ALL_DIRTY = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

    private int dirtyBits;
    private static final float EPS = (float) 1.110223024E-16;

    private static final float EPSILON = (float) 1.0e-10;
    private static final float EPSILON_ABSOLUTE = (float) 1.0e-5;
    private static final float EPSILON_RELATIVE = (float) 1.0e-4;
    
    /**
     * A zero matrix.
     */
    private static final int ZERO = 0x01;

   /**
    * An identity matrix.
    */
    private static final int IDENTITY_TYPE = 0x02;


   /**
    * A Uniform scale matrix with no translation or other
    * off-diagonal components.
    */
    private static final int SCALE = 0x04;

   /**
    * A translation-only matrix with ones on the diagonal.
    *
    */
    private static final int TRANSLATION = 0x08;

   /**
    * The four row vectors that make up an orthogonal matrix form a basis,
    * meaning that they are mutually orthogonal; an orthogonal matrix with
    * positive determinant is a pure rotation matrix; a negative
    * determinant indicates a rotation and a reflection.
    */
    private static final int ORTHOGONAL = 0x10;

   /**
    * This matrix is a rotation and a translation with unity scale;
    * The upper 3x3 of the matrix is orthogonal, and there is a
    * translation component.
    */
    private static final int RIGID = 0x20;

   /**
    * This is an angle and length preserving matrix, meaning that it
    * can translate, rotate, and reflect
    * about an axis, and scale by an amount that is uniform in all directions.
    * These operations preserve the distance between any two points and the
    * angle between any two intersecting lines.
    */
    private static final int CONGRUENT = 0x40;

   /**
    * An affine matrix can translate, rotate, reflect, scale anisotropically,
    * and shear.  Lines remain straight, and parallel lines remain parallel,
    * but the angle between intersecting lines can change. In order for a
    * transform to be classified as affine, the 4th row must be: [0, 0, 0, 1].
    */
    private static final int AFFINE = 0x80;

   /**
    * This matrix has a negative determinant; an orthogonal matrix with
    * a positive determinant is a rotation matrix; an orthogonal matrix
    * with a negative determinant is a reflection and rotation matrix.
    */
    private static final int NEGATIVE_DETERMINANT = 0x100;

    /**
     * The upper 3x3 column vectors that make up an orthogonal
     * matrix form a basis meaning that they are mutually orthogonal.
     * It can have non-uniform or zero x/y/z scale as long as
     * the dot product of any two column is zero.
     * This one is used by Java3D internal only and should not
     * expose to the user.
     */
    private static final int ORTHO = 0x40000000;

    public PMatrix() 
    {
       setIdentity();
    }
    
    public PMatrix(PMatrix other) 
    {
       if (other != null)
           set(other);
       else
           setIdentity();
    }
    
    public PMatrix(Matrix4f matrix) 
    {
       if (matrix != null)
           set(matrix);
       else
           setIdentity();
    }
    
    public PMatrix(Vector3f rotation, Vector3f scale, Vector3f translation) 
    {
       if (rotation != null && scale != null && translation != null)
           set(rotation, translation, scale);
       else
           setIdentity();
    }
    
    public PMatrix(Vector3f translation)
    {
        setIdentity();
        if (translation != null)
            setTranslation(translation);
    }
    
    /**
     * Constructs and initializes a transform from the float array of
     * length 16; the top row of the matrix is initialized to the first
     * four elements of the array, and so on.  
     * @param matrix  a float array of 16
     */
    public PMatrix(float[] matrix) 
    {
        set(matrix);
    }

    /**
     * Sets this transform to the identity matrix.
     */
    public final void setIdentity() 
    {
	mat[0]  = 1.0f;  mat[1]  = 0.0f;  mat[2]  = 0.0f;  mat[3]  = 0.0f;
	mat[4]  = 0.0f;  mat[5]  = 1.0f;  mat[6]  = 0.0f;  mat[7]  = 0.0f;
	mat[8]  = 0.0f;  mat[9]  = 0.0f;  mat[10] = 1.0f;  mat[11] = 0.0f;
	mat[12] = 0.0f;  mat[13] = 0.0f;  mat[14] = 0.0f;  mat[15] = 1.0f;
        type = IDENTITY_TYPE | SCALE |  ORTHOGONAL | RIGID | CONGRUENT |
	       AFFINE | TRANSLATION | ORTHO;
	dirtyBits = SCALE_BIT | ROTATION_BIT;
	// No need to set SVD_BIT
    }
       
    /**
     * Sets the matrix values of this transform to the matrix values in the
     * single precision array parameter.  
     * @param matrix  the single precision array of length 16 in row major format
     */
    public final void set(float[] matrix) {
	mat[0] = matrix[0];
	mat[1] = matrix[1];
	mat[2] = matrix[2];
	mat[3] = matrix[3];
	mat[4] = matrix[4];
	mat[5] = matrix[5];
	mat[6] = matrix[6];
	mat[7] = matrix[7];
	mat[8] = matrix[8];
	mat[9] = matrix[9];
	mat[10] = matrix[10];
	mat[11] = matrix[11];
	mat[12] = matrix[12];
	mat[13] = matrix[13];
	mat[14] = matrix[14];
	mat[15] = matrix[15];
        
	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  
	    normalize();
    }
    
    /**
     * Sets the matrix, type, and state of this transform to the matrix,
     * type, and state of transform t1.
     * @param t1  the transform to be copied
     */
    public final void set(PMatrix t1){
        if (t1 == null)
            throw new NullPointerException("Null matrix provided to the set method!");
	mat[0] = t1.mat[0];
	mat[1] = t1.mat[1];
	mat[2] = t1.mat[2];
	mat[3] = t1.mat[3];
	mat[4] = t1.mat[4];
	mat[5] = t1.mat[5];
	mat[6] = t1.mat[6];
	mat[7] = t1.mat[7];
	mat[8] = t1.mat[8];
	mat[9] = t1.mat[9];
	mat[10] = t1.mat[10];
	mat[11] = t1.mat[11];
	mat[12] = t1.mat[12];
	mat[13] = t1.mat[13];
	mat[14] = t1.mat[14];
	mat[15] = t1.mat[15];
	type = t1.type;

	// don't copy rot[] and scales[]
	dirtyBits = t1.dirtyBits | ROTATION_BIT | SCALE_BIT;
        autoNormalize = t1.autoNormalize;
    }
    
    public final void set(Matrix4f matrix)
    {
        float [] array = new float [16];
        matrix.get(array);
        set(array);
    }

    public final void set(Vector3f rotation, Vector3f translation,Vector3f scale) 
    {
        setRotation(rotation);
        setScale(scale);
        setTranslation(translation);
    }
    
    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3f m1, Vector3f t1, float s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0f;
	mat[13]=0.0f;
	mat[14]=0.0f;
	mat[15]=1.0f;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    // input matrix may not normalize
	    normalize();
	}
    }

    
    public void set2(Quaternion rotation, Vector3f translation, float scale)
    {
        Matrix4f jMonkeyMatrix = new Matrix4f();

        jMonkeyMatrix.setTranslation(translation);
        jMonkeyMatrix.multLocal(rotation);
        
        Matrix4fToPMatrix(jMonkeyMatrix, this);
    }
    
    void set(Quaternion rotation, Vector3f translation, Vector3f scale) 
    {
        setRotation(rotation);
        setTranslation(translation);
        setScale(scale);
    }
  
    void set(Vector3f rotation, Vector3f translation, float scale) 
    {
        setRotation(rotation);
        setTranslation(translation);
        setScale(scale);
    }
    
     /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quaternion q1, Vector3f t1, float s) {
	if(scales == null)
	    scales = new float[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z)*s;
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z)*s;
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0f;
        mat[13] = 0.0f;
        mat[14] = 0.0f;
        mat[15] = 1.0f;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }
    
     /**
     * Sets the value of this transform to a counter clockwise rotation
     * about the x axis. All of the non-rotational components are set as
     * if this were an identity matrix.
     * @param angle the angle to rotate about the X axis in radians
     */
    public void buildRotationX(float angle) {
        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);

        mat[0] = 1.0f;
        mat[1] = 0.0f;
        mat[2] = 0.0f;
        mat[3] = 0.0f;

        mat[4] = 0.0f;
        mat[5] = cosAngle;
        mat[6] = -sinAngle;
        mat[7] = 0.0f;

        mat[8] = 0.0f;
        mat[9] = sinAngle;
        mat[10] = cosAngle;
        mat[11] = 0.0f;

        mat[12] = 0.0f;
        mat[13] = 0.0f;
        mat[14] = 0.0f;
        mat[15] = 1.0f;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(angle)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }
    
    /**
     * Sets the value of this transform to a counter clockwise rotation about
     * the y axis. All of the non-rotational components are set as if this
     * were an identity matrix.
     * @param angle the angle to rotate about the Y axis in radians
     */
    public void buildRotationY(float angle) {
        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);

        mat[0] = cosAngle;
        mat[1] = 0.0f;
        mat[2] = sinAngle;
        mat[3] = 0.0f;

        mat[4] = 0.0f;
        mat[5] = 1.0f;
        mat[6] = 0.0f;
        mat[7] = 0.0f;

        mat[8] = -sinAngle;
        mat[9] = 0.0f;
        mat[10] = cosAngle;
        mat[11] = 0.0f;

        mat[12] = 0.0f;
        mat[13] = 0.0f;
        mat[14] = 0.0f;
        mat[15] = 1.0f;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(angle)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }


    /**
     * Sets the value of this transform to a counter clockwise rotation
     * about the z axis.  All of the non-rotational components are set
     * as if this were an identity matrix.
     * @param angle the angle to rotate about the Z axis in radians
     */
    public void buildRotationZ(float angle)  {
        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);

        mat[0] = cosAngle;
        mat[1] = -sinAngle;
        mat[2] = 0.0f;
        mat[3] = 0.0f;

        mat[4] = sinAngle;
        mat[5] = cosAngle;
        mat[6] = 0.0f;
        mat[7] = 0.0f;

        mat[8] = 0.0f;
        mat[9] = 0.0f;
        mat[10] = 1.0f;
        mat[11] = 0.0f;

        mat[12] = 0.0f;
        mat[13] = 0.0f;
        mat[14] = 0.0f;
        mat[15] = 1.0f;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(angle)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }
    
    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * rotation matrix converted from the Euler angles provided; the other
     * <b>non-rotational elements are set as if this were an identity matrix.</b>
     * The euler parameter is a Vector3d consisting of three rotation angles
     * applied first about the X, then Y then Z axis.
     * These rotations are applied using a static frame of reference. In
     * other words, the orientation of the Y rotation axis is not affected
     * by the X rotation and the orientation of the Z rotation axis is not
     * affected by the X or Y rotation.
     * @param euler  the Vector3d consisting of three rotation angles about X,Y,Z
     *
     */
    public final void setRotation(Vector3f euler) {
	float sina, sinb, sinc;
	float cosa, cosb, cosc;

	sina =  (float) Math.sin(euler.x);
	sinb = (float) Math.sin(euler.y);
	sinc = (float) Math.sin(euler.z);
	cosa = (float) Math.cos(euler.x);
	cosb = (float) Math.cos(euler.y);
	cosc = (float) Math.cos(euler.z);

	mat[0] = cosb * cosc;
	mat[1] = -(cosa * sinc) + (sina * sinb * cosc);
	mat[2] = (sina * sinc) + (cosa * sinb *cosc);
	mat[3] = 0.0f;

	mat[4] = cosb * sinc;
	mat[5] = (cosa * cosc) + (sina * sinb * sinc);
	mat[6] = -(sina * cosc) + (cosa * sinb *sinc);
	mat[7] = 0.0f;

	mat[8] = -sinb;
	mat[9] = sina * cosb;
	mat[10] = cosa * cosb;
	mat[11] = 0.0f;

	mat[12] = 0.0f;
	mat[13] = 0.0f;
	mat[14] = 0.0f;
	mat[15] = 1.0f;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(euler)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = AFFINE | CONGRUENT | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
    }
    
    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the single precision Matrix3f argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * will be preserved; the argument matrix m1 will be checked for proper
     * normalization when this transform is internally classified.
     * @param m1   the single precision 3x3 matrix
     */
     public final void setRotation(Matrix3f m1) {

	 if ((dirtyBits & SCALE_BIT)!= 0) {
	     computeScales(false);
	 }

	 mat[0] = m1.m00*scales[0];
	 mat[1] = m1.m01*scales[1];
	 mat[2] = m1.m02*scales[2];
	 mat[4] = m1.m10*scales[0];
	 mat[5] = m1.m11*scales[1];
	 mat[6] = m1.m12*scales[2];
	 mat[8] = m1.m20*scales[0];
	 mat[9] = m1.m21*scales[1];
	 mat[10]= m1.m22*scales[2];

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
     }
     
    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the quaternion argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param q1    the quaternion that specifies the rotation
    */
    public final void setRotation(Quaternion q1) {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z)*scales[0];
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z))*scales[0];
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y))*scales[0];

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z))*scales[1];
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z)*scales[1];
        mat[9] = (2.0f*(q1.y * q1.z + q1.w * q1.x))*scales[1];

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y))*scales[2];
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x))*scales[2];
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y)*scales[2];

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(q1)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

        dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY_TYPE|SCALE|TRANSLATION|SCALE|ZERO);
      }
    
    /**
     * Sets the possibly non-uniform scale component of the current
     * transform; any existing scale is first factored out of the
     * existing transform before the new scale is applied.
     * @param scale  the new x,y,z scale values
     */
     public final void setScale(Vector3f scale) {

	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

	scales[0] = scale.x;
	scales[1] = scale.y;
	scales[2] = scale.z;

	mat[0] = rot[0]*scale.x;
	mat[1] = rot[1]*scale.y;
	mat[2] = rot[2]*scale.z;
	mat[4] = rot[3]*scale.x;
	mat[5] = rot[4]*scale.y;
	mat[6] = rot[5]*scale.z;
	mat[8] = rot[6]*scale.x;
	mat[9] = rot[7]*scale.y;
	mat[10] = rot[8]*scale.z;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(scale)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	dirtyBits |= (CLASSIFY_BIT | RIGID_BIT | CONGRUENT_BIT | SVD_BIT);
	dirtyBits &= ~SCALE_BIT;
    }
    
    /**
     * Sets the scale component of the current transform; any existing
     * scale is first factored out of the existing transform before
     * the new scale is applied.
     * @param scale  the new scale amount
     */
    public final void setScale(float scale) {
	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

	scales[0] = scales[1] = scales[2] = scale;
	mat[0]  = rot[0]*scale;
	mat[1]  = rot[1]*scale;
	mat[2]  = rot[2]*scale;
	mat[4]  = rot[3]*scale;
	mat[5]  = rot[4]*scale;
	mat[6]  = rot[5]*scale;
	mat[8]  = rot[6]*scale;
	mat[9]  = rot[7]*scale;
	mat[10] = rot[8]*scale;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(scale)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	dirtyBits |= (CLASSIFY_BIT | RIGID_BIT | CONGRUENT_BIT | SVD_BIT);
	dirtyBits &= ~SCALE_BIT;
    }
    
    public final PMatrix setTranslation(Vector3f translation) 
    {
        mat[3]  = translation.x;
        mat[7]  = translation.y;
        mat[11] = translation.z;
        return this;
    }
    
    /**
     * Sets this transform to all zeros.
     */
    public final void setZero() {
	mat[0] = 0.0f;  mat[1] = 0.0f;  mat[2] = 0.0f;  mat[3] = 0.0f;
	mat[4] = 0.0f;  mat[5] = 0.0f;  mat[6] = 0.0f;  mat[7] = 0.0f;
	mat[8] = 0.0f;  mat[9] = 0.0f;  mat[10] = 0.0f; mat[11] = 0.0f;
	mat[12] = 0.0f; mat[13] = 0.0f; mat[14] = 0.0f; mat[15] = 0.0f;

	type = ZERO | ORTHO;
	dirtyBits = SCALE_BIT | ROTATION_BIT;
    }
    
    final boolean isZeroTranslation() {
	return (almostZero(mat[3]) &&
		almostZero(mat[7]) &&
		almostZero(mat[11]));
    }

    final boolean isZeroRotation() {
	return (almostZero(mat[1]) && almostZero(mat[2]) &&
		almostZero(mat[4]) && almostZero(mat[6]) &&
		almostZero(mat[8]) && almostZero(mat[9]));
    }
    
    /**
     * Gets the upper 3x3 values of this matrix and places them into
     * the matrix m1.
     * @param m1  the matrix that will hold the values
     */
    public final void getMatrixRotationScale(Matrix3f m1) {
	m1.m00 = (float) mat[0];
	m1.m01 = (float) mat[1];
	m1.m02 = (float) mat[2];
	m1.m10 = (float) mat[4];
	m1.m11 = (float) mat[5];
	m1.m12 = (float) mat[6];
	m1.m20 = (float) mat[8];
	m1.m21 = (float) mat[9];
	m1.m22 = (float) mat[10];
    }
    
    public float [] getFloatArray()
    {
        float [] result = new float[16];
        result[ 0] = mat[ 0];
        result[ 1] = mat[ 1];
        result[ 2] = mat[ 2];
        result[ 3] = mat[ 3];
        result[ 4] = mat[ 4];
        result[ 5] = mat[ 5];
        result[ 6] = mat[ 6];
        result[ 7] = mat[ 7];
        result[ 8] = mat[ 8];
        result[ 9] = mat[ 9];
        result[10] = mat[10];
        result[11] = mat[11];
        result[12] = mat[12];
        result[13] = mat[13];
        result[14] = mat[14];
        result[15] = mat[15];
        return result;
        //return mat.clone();
    }
    
    public Matrix4f getMatrix4f() 
    {
        Matrix4f result = new Matrix4f();
        // PMatrix.transpose = Matrix4f
        result.m00 = mat[ 0];
	result.m01 = mat[ 4];
	result.m02 = mat[ 8];
	result.m03 = mat[12];
	result.m10 = mat[ 1];
	result.m11 = mat[ 5];
	result.m12 = mat[ 9];
	result.m13 = mat[13];
	result.m20 = mat[ 2];
	result.m21 = mat[ 6];
	result.m22 = mat[10];
	result.m23 = mat[14];
	result.m30 = mat[ 3];
	result.m31 = mat[ 7];
	result.m32 = mat[11];
	result.m33 = mat[15];
        return result;
    }
    
    public PMatrix getInversedPMatrix() 
    {
        PMatrix result  = new PMatrix(this);
        result.invert();
        return result;
    }
    
    public Matrix4f getInversedMatrix4f() 
    {
        PMatrix result  = new PMatrix(this);
        result.invert();
        return result.getMatrix4f();
    }

    /**
     * Returns the uniform scale factor of this matrix.
     * If the matrix has non-uniform scale factors, the largest of the
     * x, y, and z scale factors will be returned.
     * @return  the scale factor of this matrix
     */
    public final float getScale() {
	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}
	return max3(scales);
   }
    
    public final Vector3f getScaleVector() 
    {
        Vector3f result = new Vector3f();
        getScale(result);
        return result;
    }
    
    /**
     * Gets the possibly non-uniform scale components of the current
     * transform and places them into the scale vector.
     * @param scale  the vector into which the x,y,z scale values will be placed
     */
    public final void getScale(Vector3f scale) {
	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}
	scale.x = scales[0];
	scale.y = scales[1];
	scale.z = scales[2];
    }

    /**
     * Returns a new vector with the translation of this matrix
     * @return
     */
    public final Vector3f getTranslation()
    {
        Vector3f result = new Vector3f();
        getTranslation(result);
        return result;
    }

    /**
     * Retrieves the translational components of this transform.
     * @param trans  the vector that will receive the translational component
     */
    public final void getTranslation(Vector3f trans) {
	trans.x = (float)mat[3];
	trans.y = (float)mat[7];
	trans.z = (float)mat[11];
    }
    
    public Vector3f getLocalX() 
    {
        Vector3f result = new Vector3f(mat[0], mat[4], mat[8]);
        return result;
    }
    
    public Vector3f getLocalXNormalized() 
    {
        Vector3f result = new Vector3f(mat[0], mat[4], mat[8]);
        result.normalizeLocal();
        return result;
    }
    
    public Vector3f getLocalY() 
    {
        Vector3f result = new Vector3f(mat[1], mat[5], mat[9]);
        return result;
    }
    
    public Vector3f getLocalYNormalized() 
    {
        Vector3f result = new Vector3f(mat[1], mat[5], mat[9]);
        result.normalizeLocal();
        return result;
    }
    
    public Vector3f getLocalZ() 
    {
        Vector3f result = new Vector3f(mat[2], mat[6], mat[10]);
        return result;
    }
    
    public Vector3f getLocalZNormalized() 
    {
        Vector3f result = new Vector3f(mat[2], mat[6], mat[10]);
        result.normalizeLocal();
        return result;
    }
    
    public final Quaternion getRotation() 
    {
        Quaternion result = new Quaternion();
        getRotation(result);
        return result;
    }
    
    /**
     * Gets the rotation with code derived from JME methods
     * @return
     */
    public Quaternion getRotationJME()
    {
        Quaternion quat = new Quaternion();
        quat.fromRotationMatrix(mat[0], mat[1], mat[2],
                                mat[4], mat[5], mat[6],
                                mat[8], mat[9], mat[10]);

        return(quat);
    }
    
    public Vector4f getRow(int nRowIndex)
    {
        assert(nRowIndex >= 0 && nRowIndex <= 3) : "Row index requested is out of bounds!"; // Bounds checking
        Vector4f result = new Vector4f(
                                        mat[(nRowIndex * 4)],
                                        mat[(nRowIndex * 4) + 1],
                                        mat[(nRowIndex * 4) + 2],
                                        mat[(nRowIndex * 4) + 3]
                                        );
        return result;
    }
    
    /**
     * Places the quaternion equivalent of the normalized rotational
     * component of this transform into the quaternion parameter.
     * @param q1  the quaternion into which the rotation component is placed
     */
    public final void getRotation(Quaternion q1) {
	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	}

	float ww = 0.25f*(1.0f + rot[0] + rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.w = (float)Math.sqrt(ww);
	    ww = 0.25f/q1.w;
	    q1.x = ((rot[7] - rot[5])*ww);
	    q1.y = ((rot[2] - rot[6])*ww);
	    q1.z = ((rot[3] - rot[1])*ww);
	    return;
        }

        q1.w = 0.0f;
        ww = -0.5f*(rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.x =  (float)Math.sqrt(ww);
	    ww = 0.5f/q1.x;
	    q1.y = (rot[3]*ww);
	    q1.z = (rot[6]*ww);
	    return;
        }

        q1.x = 0.0f;
        ww = 0.5f*(1.0f - rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.y =  (float)Math.sqrt(ww);
	    q1.z = (rot[7]/(2.0f*q1.y));
	    return;
        }

        q1.y = 0.0f;
        q1.z = 1.0f;
    }
    
   /**
     * Multiplies each element of this transform by a scalar.
     * @param scalar  the scalar multiplier
     */
    public final void mul(float scalar) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] *= scalar;
	}
	dirtyBits = ALL_DIRTY;
    }
    
    /**
     * Sets the value of this transform to the result of multiplying itself
     * with transform t1 (this = this * t1).
     * @param t1 the other transform
     */
    public final void mul(PMatrix t1) {
	float tmp0, tmp1, tmp2, tmp3;
	float tmp4, tmp5, tmp6, tmp7;
	float tmp8, tmp9, tmp10, tmp11;
	boolean aff = false;

	if (t1.isAffine()) {
	    tmp0 = mat[0]*t1.mat[0] + mat[1]*t1.mat[4] + mat[2]*t1.mat[8];
	    tmp1 = mat[0]*t1.mat[1] + mat[1]*t1.mat[5] + mat[2]*t1.mat[9];
	    tmp2 = mat[0]*t1.mat[2] + mat[1]*t1.mat[6] + mat[2]*t1.mat[10];
	    tmp3 = mat[0]*t1.mat[3] + mat[1]*t1.mat[7] + mat[2]*t1.mat[11] + mat[3];
	    tmp4 = mat[4]*t1.mat[0] + mat[5]*t1.mat[4] + mat[6]*t1.mat[8];
	    tmp5 = mat[4]*t1.mat[1] + mat[5]*t1.mat[5] + mat[6]*t1.mat[9];
	    tmp6 = mat[4]*t1.mat[2] + mat[5]*t1.mat[6] + mat[6]*t1.mat[10];
	    tmp7 = mat[4]*t1.mat[3] + mat[5]*t1.mat[7] + mat[6]*t1.mat[11] + mat[7];
	    tmp8 = mat[8]*t1.mat[0] + mat[9]*t1.mat[4] + mat[10]*t1.mat[8];
	    tmp9 = mat[8]*t1.mat[1] + mat[9]*t1.mat[5] + mat[10]*t1.mat[9];
	    tmp10 = mat[8]*t1.mat[2] + mat[9]*t1.mat[6] + mat[10]*t1.mat[10];
	    tmp11 = mat[8]*t1.mat[3] + mat[9]*t1.mat[7] + mat[10]*t1.mat[11] + mat[11];
	    if (isAffine()) {
		mat[12] =  mat[13] = mat[14] = 0;
		mat[15] = 1;
		aff = true;
	    } else {
		float tmp12 = mat[12]*t1.mat[0] + mat[13]*t1.mat[4] +
		               mat[14]*t1.mat[8];
		float tmp13 = mat[12]*t1.mat[1] + mat[13]*t1.mat[5] +
		               mat[14]*t1.mat[9];
		float tmp14 = mat[12]*t1.mat[2] + mat[13]*t1.mat[6] +
		               mat[14]*t1.mat[10];
                float tmp15 = mat[12]*t1.mat[3] + mat[13]*t1.mat[7] +
		               mat[14]*t1.mat[11] + mat[15];
		mat[12] = tmp12;
		mat[13] = tmp13;
		mat[14] = tmp14;
		mat[15] = tmp15;
	    }
	} else {
	    tmp0 = mat[0]*t1.mat[0] + mat[1]*t1.mat[4] + mat[2]*t1.mat[8] +
                   mat[3]*t1.mat[12];
	    tmp1 = mat[0]*t1.mat[1] + mat[1]*t1.mat[5] + mat[2]*t1.mat[9] +
                   mat[3]*t1.mat[13];
	    tmp2 = mat[0]*t1.mat[2] + mat[1]*t1.mat[6] + mat[2]*t1.mat[10] +
                   mat[3]*t1.mat[14];
	    tmp3 = mat[0]*t1.mat[3] + mat[1]*t1.mat[7] + mat[2]*t1.mat[11] +
                   mat[3]*t1.mat[15];
            tmp4 = mat[4]*t1.mat[0] + mat[5]*t1.mat[4] + mat[6]*t1.mat[8] +
		   mat[7]*t1.mat[12];
	    tmp5 = mat[4]*t1.mat[1] + mat[5]*t1.mat[5] + mat[6]*t1.mat[9] +
                   mat[7]*t1.mat[13];
	    tmp6 = mat[4]*t1.mat[2] + mat[5]*t1.mat[6] + mat[6]*t1.mat[10] +
                   mat[7]*t1.mat[14];
	    tmp7 = mat[4]*t1.mat[3] + mat[5]*t1.mat[7] + mat[6]*t1.mat[11] +
                   mat[7]*t1.mat[15];
            tmp8 = mat[8]*t1.mat[0] + mat[9]*t1.mat[4] + mat[10]*t1.mat[8] +
                   mat[11]*t1.mat[12];
            tmp9 = mat[8]*t1.mat[1] + mat[9]*t1.mat[5] + mat[10]*t1.mat[9] +
                   mat[11]*t1.mat[13];
            tmp10 = mat[8]*t1.mat[2] + mat[9]*t1.mat[6] +
		    mat[10]*t1.mat[10]+ mat[11]*t1.mat[14];
            tmp11 = mat[8]*t1.mat[3] + mat[9]*t1.mat[7] +
		    mat[10]*t1.mat[11] + mat[11]*t1.mat[15];

	    if (isAffine()) {
		mat[12] = t1.mat[12];
		mat[13] = t1.mat[13];
		mat[14] = t1.mat[14];
		mat[15] = t1.mat[15];
	    } else {
		float tmp12 = mat[12]*t1.mat[0] + mat[13]*t1.mat[4] +
		               mat[14]*t1.mat[8] +  mat[15]*t1.mat[12];
		float tmp13 = mat[12]*t1.mat[1] + mat[13]*t1.mat[5] +
		               mat[14]*t1.mat[9] + mat[15]*t1.mat[13];
		float tmp14 = mat[12]*t1.mat[2] + mat[13]*t1.mat[6] +
		               mat[14]*t1.mat[10] + mat[15]*t1.mat[14];
		float tmp15 = mat[12]*t1.mat[3] + mat[13]*t1.mat[7] +
		               mat[14]*t1.mat[11] + mat[15]*t1.mat[15];
		mat[12] = tmp12;
		mat[13] = tmp13;
		mat[14] = tmp14;
		mat[15] = tmp15;
	    }
	}

	mat[0] = tmp0;
	mat[1] = tmp1;
	mat[2] = tmp2;
	mat[3] = tmp3;
	mat[4] = tmp4;
	mat[5] = tmp5;
	mat[6] = tmp6;
	mat[7] = tmp7;
	mat[8] = tmp8;
	mat[9] = tmp9;
	mat[10] = tmp10;
	mat[11] = tmp11;

	if (((dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((type & CONGRUENT) != 0) &&
	    ((t1.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t1.type & CONGRUENT) != 0)) {
	    type &= t1.type;
	    dirtyBits |= t1.dirtyBits | CLASSIFY_BIT |
		ROTSCALESVD_DIRTY | RIGID_BIT;
	} else {
	    if (aff) {
		dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
                            CLASSIFY_BIT | ROTSCALESVD_DIRTY;
	    } else {
		dirtyBits = ALL_DIRTY;
	    }
	}

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Sets the value of this transform to the result of multiplying transform
     * t1 by transform t2 (this = t1*t2).
     * @param t1  the left transform
     * @param t2  the right transform
     */
    public final void mul(PMatrix t1, PMatrix t2) {
	boolean aff = false;
	if ((this != t1)  &&  (this != t2)) {
	    if (t2.isAffine()) {

		mat[0] = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] + t1.mat[2]*t2.mat[8];
		mat[1] = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] + t1.mat[2]*t2.mat[9];
		mat[2] = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] + t1.mat[2]*t2.mat[10];
		mat[3] = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
                         t1.mat[2]*t2.mat[11] + t1.mat[3];
		mat[4] = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] + t1.mat[6]*t2.mat[8];
		mat[5] = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] + t1.mat[6]*t2.mat[9];
	        mat[6] = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] + t1.mat[6]*t2.mat[10];
	        mat[7] = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
                         t1.mat[6]*t2.mat[11] + t1.mat[7];
		mat[8] = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] + t1.mat[10]*t2.mat[8];
	        mat[9] = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] + t1.mat[10]*t2.mat[9];
		mat[10] = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] + t1.mat[10]*t2.mat[10];
	        mat[11] = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
                          t1.mat[10]*t2.mat[11] + t1.mat[11];
		if (t1.isAffine()) {
		    aff = true;
		    mat[12] =  mat[13] = mat[14] = 0;
		    mat[15] = 1;
		} else {
		    mat[12] = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
			      t1.mat[14]*t2.mat[8];
		    mat[13] = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		              t1.mat[14]*t2.mat[9];
		    mat[14] = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
		              t1.mat[14]*t2.mat[10];
		    mat[15] = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
			      t1.mat[14]*t2.mat[11] + t1.mat[15];
		}
	    } else {
		mat[0] = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] +
		         t1.mat[2]*t2.mat[8] + t1.mat[3]*t2.mat[12];
		mat[1] = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] +
		         t1.mat[2]*t2.mat[9] + t1.mat[3]*t2.mat[13];
		mat[2] = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] +
		         t1.mat[2]*t2.mat[10] + t1.mat[3]*t2.mat[14];
		mat[3] = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
		         t1.mat[2]*t2.mat[11] + t1.mat[3]*t2.mat[15];
		mat[4] = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] +
		         t1.mat[6]*t2.mat[8] + t1.mat[7]*t2.mat[12];
		mat[5] = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] +
		         t1.mat[6]*t2.mat[9] + t1.mat[7]*t2.mat[13];
		mat[6] = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] +
		         t1.mat[6]*t2.mat[10] + t1.mat[7]*t2.mat[14];
		mat[7] = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
		         t1.mat[6]*t2.mat[11] + t1.mat[7]*t2.mat[15];
		mat[8] = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] +
		         t1.mat[10]*t2.mat[8] + t1.mat[11]*t2.mat[12];
		mat[9] = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] +
		         t1.mat[10]*t2.mat[9] + t1.mat[11]*t2.mat[13];
		mat[10] = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] +
		          t1.mat[10]*t2.mat[10] + t1.mat[11]*t2.mat[14];
		mat[11] = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
		          t1.mat[10]*t2.mat[11] + t1.mat[11]*t2.mat[15];
		if (t1.isAffine()) {
		    mat[12] = t2.mat[12];
		    mat[13] = t2.mat[13];
		    mat[14] = t2.mat[14];
		    mat[15] = t2.mat[15];
		} else {
		    mat[12] = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
		              t1.mat[14]*t2.mat[8] + t1.mat[15]*t2.mat[12];
		    mat[13] = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		              t1.mat[14]*t2.mat[9] + t1.mat[15]*t2.mat[13];
		    mat[14] = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
			      t1.mat[14]*t2.mat[10] + t1.mat[15]*t2.mat[14];
		    mat[15] = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
		              t1.mat[14]*t2.mat[11] + t1.mat[15]*t2.mat[15];
		}
	    }
	} else {
	    float tmp0, tmp1, tmp2, tmp3;
	    float tmp4, tmp5, tmp6, tmp7;
	    float tmp8, tmp9, tmp10, tmp11;

	    if (t2.isAffine()) {
		tmp0 = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] + t1.mat[2]*t2.mat[8];
		tmp1 = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] + t1.mat[2]*t2.mat[9];
		tmp2 = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] + t1.mat[2]*t2.mat[10];
		tmp3 = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
		       t1.mat[2]*t2.mat[11] + t1.mat[3];
		tmp4 = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] + t1.mat[6]*t2.mat[8];
		tmp5 = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] + t1.mat[6]*t2.mat[9];
	        tmp6 = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] + t1.mat[6]*t2.mat[10];
	        tmp7 = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
                       t1.mat[6]*t2.mat[11] + t1.mat[7];
		tmp8 = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] + t1.mat[10]*t2.mat[8];
	        tmp9 = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] + t1.mat[10]*t2.mat[9];
		tmp10 = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] + t1.mat[10]*t2.mat[10];
	        tmp11 = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
                        t1.mat[10]*t2.mat[11] + t1.mat[11];
		if (t1.isAffine()) {
		    aff = true;
		    mat[12] =  mat[13] = mat[14] = 0;
		    mat[15] = 1;
		} else {
		    float tmp12 = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
			           t1.mat[14]*t2.mat[8];
		    float tmp13 = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		                   t1.mat[14]*t2.mat[9];
		    float tmp14 = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
		                   t1.mat[14]*t2.mat[10];
		    float tmp15 = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
			           t1.mat[14]*t2.mat[11] + t1.mat[15];
		    mat[12] = tmp12;
		    mat[13] = tmp13;
		    mat[14] = tmp14;
		    mat[15] = tmp15;
		}
	    } else {
		tmp0 = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] +
		       t1.mat[2]*t2.mat[8] + t1.mat[3]*t2.mat[12];
		tmp1 = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] +
		       t1.mat[2]*t2.mat[9] + t1.mat[3]*t2.mat[13];
		tmp2 = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] +
		       t1.mat[2]*t2.mat[10] + t1.mat[3]*t2.mat[14];
		tmp3 = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
		       t1.mat[2]*t2.mat[11] + t1.mat[3]*t2.mat[15];
		tmp4 = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] +
		       t1.mat[6]*t2.mat[8] + t1.mat[7]*t2.mat[12];
		tmp5 = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] +
		       t1.mat[6]*t2.mat[9] + t1.mat[7]*t2.mat[13];
		tmp6 = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] +
		       t1.mat[6]*t2.mat[10] + t1.mat[7]*t2.mat[14];
		tmp7 = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
		       t1.mat[6]*t2.mat[11] + t1.mat[7]*t2.mat[15];
		tmp8 = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] +
		       t1.mat[10]*t2.mat[8] + t1.mat[11]*t2.mat[12];
		tmp9 = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] +
		       t1.mat[10]*t2.mat[9] + t1.mat[11]*t2.mat[13];
		tmp10 = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] +
		        t1.mat[10]*t2.mat[10] + t1.mat[11]*t2.mat[14];
		tmp11 = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
		        t1.mat[10]*t2.mat[11] + t1.mat[11]*t2.mat[15];

		if (t1.isAffine()) {
		    mat[12] = t2.mat[12];
		    mat[13] = t2.mat[13];
		    mat[14] = t2.mat[14];
		    mat[15] = t2.mat[15];
		} else {
		    float tmp12 = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
		                   t1.mat[14]*t2.mat[8] + t1.mat[15]*t2.mat[12];
		    float tmp13 = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		                   t1.mat[14]*t2.mat[9] + t1.mat[15]*t2.mat[13];
		    float tmp14 = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
			           t1.mat[14]*t2.mat[10] + t1.mat[15]*t2.mat[14];
		    float tmp15 = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
		                   t1.mat[14]*t2.mat[11] + t1.mat[15]*t2.mat[15];
		    mat[12] = tmp12;
		    mat[13] = tmp13;
		    mat[14] = tmp14;
		    mat[15] = tmp15;
		}
	    }
	    mat[0] = tmp0;
	    mat[1] = tmp1;
	    mat[2] = tmp2;
	    mat[3] = tmp3;
	    mat[4] = tmp4;
	    mat[5] = tmp5;
	    mat[6] = tmp6;
	    mat[7] = tmp7;
	    mat[8] = tmp8;
	    mat[9] = tmp9;
	    mat[10] = tmp10;
	    mat[11] = tmp11;
	}


	if (((t1.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t1.type & CONGRUENT) != 0) &&
	    ((t2.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t2.type & CONGRUENT) != 0)) {
	    type = t1.type & t2.type;
	    dirtyBits = t1.dirtyBits | t2.dirtyBits | CLASSIFY_BIT |
		        ROTSCALESVD_DIRTY | RIGID_BIT;
	} else {
	    if (aff) {
		dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
                            CLASSIFY_BIT | ROTSCALESVD_DIRTY;
	    } else {
		dirtyBits = ALL_DIRTY;
	    }
	}

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Multiplies this transform by the inverse of transform t1. The final
     * value is placed into this matrix (this = this*t1^-1).
     * @param t1  the matrix whose inverse is computed.
     */
    public final void mulInverse(PMatrix t1) {
	PMatrix t2 = new PMatrix();
	t2.autoNormalize = false;
	t2.invert(t1);
	this.mul(t2);
    }

    /**
     * Multiplies transform t1 by the inverse of transform t2. The final
     * value is placed into this matrix (this = t1*t2^-1).
     * @param t1  the left transform in the multiplication
     * @param t2  the transform whose inverse is computed.
     */
    public final void mulInverse(PMatrix t1, PMatrix t2) {
        PMatrix t3 = new PMatrix();
	t3.autoNormalize = false;
        t3.invert(t2);
        this.mul(t1,t3);
    }

    /**
     * Multiplies transform t1 by the transpose of transform t2 and places
     * the result into this transform (this = t1 * transpose(t2)).
     * @param t1  the transform on the left hand side of the multiplication
     * @param t2  the transform whose transpose is computed
     */
    public final void mulTransposeRight(PMatrix t1, PMatrix t2) {
	PMatrix t3 = new PMatrix();
	t3.autoNormalize = false;
	t3.transpose(t2);
	mul(t1, t3);
    }


    /**
     * Multiplies the transpose of transform t1 by transform t2 and places
     * the result into this matrix (this = transpose(t1) * t2).
     * @param t1  the transform whose transpose is computed
     * @param t2  the transform on the right hand side of the multiplication
     */
    public final void mulTransposeLeft(PMatrix t1, PMatrix t2){
	PMatrix t3 = new PMatrix();
	t3.autoNormalize = false;
	t3.transpose(t1);
	mul(t3, t2);
    }


    /**
     * Multiplies the transpose of transform t1 by the transpose of
     * transform t2 and places the result into this transform
     * (this = transpose(t1) * transpose(t2)).
     * @param t1  the transform on the left hand side of the multiplication
     * @param t2  the transform on the right hand side of the multiplication
     */
    public final void mulTransposeBoth(PMatrix t1, PMatrix t2) {
	PMatrix t3 = new PMatrix();
	PMatrix t4 = new PMatrix();
	t3.autoNormalize = false;
	t4.autoNormalize = false;
	t3.transpose(t1);
	t4.transpose(t2);
	mul(t3, t4);
    }
    
    /**
     * Transform the vector vec using this transform and place the
     * result into vecOut.
     * @param vec  the double precision vector to be transformed
     * @param vecOut  the vector into which the transformed values are placed
     */
    public final void transform(Vector4f vec, Vector4f vecOut) {

	if (vec != vecOut) {
	    vecOut.x = (mat[0]*vec.x + mat[1]*vec.y
			+ mat[2]*vec.z + mat[3]*vec.w);
	    vecOut.y = (mat[4]*vec.x + mat[5]*vec.y
			+ mat[6]*vec.z + mat[7]*vec.w);
	    vecOut.z = (mat[8]*vec.x + mat[9]*vec.y
			+ mat[10]*vec.z + mat[11]*vec.w);
	    vecOut.w = (mat[12]*vec.x + mat[13]*vec.y
			+ mat[14]*vec.z + mat[15]*vec.w);
	} else {
	    transform(vec);
	}
    }


    /**
     * Transform the vector vec using this Transform and place the
     * result back into vec.
     * @param vec  the double precision vector to be transformed
     */
    public final void transform(Vector4f vec) {
	float x = (mat[0]*vec.x + mat[1]*vec.y
		    + mat[2]*vec.z + mat[3]*vec.w);
	float y = (mat[4]*vec.x + mat[5]*vec.y
		    + mat[6]*vec.z + mat[7]*vec.w);
	float z = (mat[8]*vec.x + mat[9]*vec.y
		    + mat[10]*vec.z + mat[11]*vec.w);
	vec.w = (mat[12]*vec.x + mat[13]*vec.y
		 + mat[14]*vec.z + mat[15]*vec.w);
	vec.x = x;
	vec.y = y;
	vec.z = z;
    }
    
    
    /**
     * Transforms the normal parameter by this transform and places the value
     * into normalOut.  The fourth element of the normal is assumed to be zero.
     * @param normal   the input normal to be transformed
     * @param normalOut  the transformed normal
     */
    public final void transformNormal(Vector3f normal, Vector3f normalOut) {
	if (normalOut != normal) {
	    normalOut.x =  mat[0]*normal.x + mat[1]*normal.y + mat[2]*normal.z;
	    normalOut.y =  mat[4]*normal.x + mat[5]*normal.y + mat[6]*normal.z;
	    normalOut.z =  mat[8]*normal.x + mat[9]*normal.y + mat[10]*normal.z;
	} else {
	    transformNormal(normal);
	}
    }


    /**
     * Transforms the normal parameter by this transform and places the value
     * back into normal.  The fourth element of the normal is assumed to be zero.
     * @param normal   the input normal to be transformed
     */
    public final void transformNormal(Vector3f normal) {
        float x =  mat[0]*normal.x + mat[1]*normal.y + mat[2]*normal.z;
        float y =  mat[4]*normal.x + mat[5]*normal.y + mat[6]*normal.z;
        normal.z =  mat[8]*normal.x + mat[9]*normal.y + mat[10]*normal.z;
        normal.x = x;
        normal.y = y;
    }

     /**
     * Transforms the point parameter with this transform and
     * places the result into pointOut.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     * @param pointOut  the transformed point
     */
    public final void transformPoint(Vector3f point, Vector3f pointOut) {
	if (point != pointOut) {
	    pointOut.x = mat[0]*point.x + mat[1]*point.y +
		         mat[2]*point.z + mat[3];
	    pointOut.y = mat[4]*point.x + mat[5]*point.y +
		         mat[6]*point.z + mat[7];
	    pointOut.z = mat[8]*point.x + mat[9]*point.y +
		         mat[10]*point.z + mat[11];
	} else {
	    transformPoint(point);
	}
    }

    /**
     * Transforms the point parameter with this transform and
     * places the result back into point.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     */
    public final void transformPoint(Vector3f point) {
        float x = mat[0]*point.x + mat[1]*point.y + mat[2]*point.z + mat[3];
        float y = mat[4]*point.x + mat[5]*point.y + mat[6]*point.z + mat[7];
        point.z =  mat[8]*point.x + mat[9]*point.y + mat[10]*point.z + mat[11];
        point.x = x;
        point.y = y;
    }
    
   /**
     * Transposes this matrix in place.
     */
    public final void transpose() {
        float temp;

        temp = mat[4];
        mat[4] = mat[1];
        mat[1] = temp;

        temp = mat[8];
        mat[8] = mat[2];
        mat[2] = temp;

        temp = mat[12];
        mat[12] = mat[3];
        mat[3] = temp;

        temp = mat[9];
        mat[9] = mat[6];
        mat[6] = temp;

        temp = mat[13];
        mat[13] = mat[7];
        mat[7] = temp;

        temp = mat[14];
        mat[14] = mat[11];
        mat[11] = temp;

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Transposes transform t1 and places the value into this transform.
     * The transform t1 is not modified.
     * @param t1  the transform whose transpose is placed into this transform
     */
    public final void transpose(PMatrix t1) {

       if (this != t1) {
           mat[0] =  t1.mat[0];
           mat[1] =  t1.mat[4];
           mat[2] =  t1.mat[8];
           mat[3] =  t1.mat[12];
           mat[4] =  t1.mat[1];
           mat[5] =  t1.mat[5];
           mat[6] =  t1.mat[9];
           mat[7] =  t1.mat[13];
           mat[8] =  t1.mat[2];
           mat[9] =  t1.mat[6];
           mat[10] = t1.mat[10];
           mat[11] = t1.mat[14];
           mat[12] = t1.mat[3];
           mat[13] = t1.mat[7];
           mat[14] = t1.mat[11];
           mat[15] = t1.mat[15];

	   dirtyBits = ALL_DIRTY;

	   if (autoNormalize) {
	       normalize();
	   }
       } else {
           this.transpose();
       }

    }

    /**
     * Normalizes the rotational components (upper 3x3) of this matrix
     * in place using a Singular Value Decomposition (SVD).
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     */
    public final void normalize() 
    {
        // Issue 253: Unable to normalize matrices with infinity or NaN
        if (!isAffine() && isInfOrNaN()) 
            return;
        
	if ((dirtyBits & (ROTATION_BIT|SVD_BIT)) != 0) {
	    computeScaleRotation(true);
	} else 	if ((dirtyBits & (SCALE_BIT|SVD_BIT)) != 0) {
	    computeScales(true);
	}

	mat[0]  = rot[0]*scales[0];
	mat[1]  = rot[1]*scales[1];
	mat[2]  = rot[2]*scales[2];
	mat[4]  = rot[3]*scales[0];
	mat[5]  = rot[4]*scales[1];
	mat[6]  = rot[5]*scales[2];
	mat[8]  = rot[6]*scales[0];
	mat[9]  = rot[7]*scales[1];
	mat[10] = rot[8]*scales[2];
	dirtyBits |= CLASSIFY_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
    }
    
    /**
     * Normalizes the rotational components (upper 3x3) of this transform
     * in place using a Cross Product (CP) normalization.
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     */
    public final void normalizeCP()  {
        // Issue 253: Unable to normalize matrices with infinity or NaN
        if (!isAffine() && isInfOrNaN()) {
            return;
        }

	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

	float mag = mat[0]*mat[0] + mat[4]*mat[4] +
  	             mat[8]*mat[8];

	if (mag != 0) {
	    mag = (float) (1.0f / Math.sqrt(mag));
	    mat[0] = mat[0]*mag;
	    mat[4] = mat[4]*mag;
	    mat[8] = mat[8]*mag;
	}

	mag = mat[1]*mat[1] + mat[5]*mat[5] +
	      mat[9]*mat[9];

	if (mag != 0) {
	    mag = (float) (1.0f / Math.sqrt(mag));
	    mat[1] = mat[1]*mag;
	    mat[5] = mat[5]*mag;
	    mat[9] = mat[9]*mag;
	}
	mat[2] = (mat[4]*mat[9] - mat[5]*mat[8])*scales[0];
	mat[6] = (mat[1]*mat[8] - mat[0]*mat[9])*scales[1];
	mat[10] = (mat[0]*mat[5] - mat[1]*mat[4])*scales[2];

	mat[0] *= scales[0];
	mat[1] *= scales[0];
	mat[4] *= scales[1];
	mat[5] *= scales[1];
	mat[8] *= scales[2];
	mat[9] *= scales[2];

	// leave the AFFINE bit
	dirtyBits |= CONGRUENT_BIT | RIGID_BIT | CLASSIFY_BIT | ROTATION_BIT | SVD_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
    }


    /**
     * Normalizes the rotational components (upper 3x3) of transform t1
     * using a Cross Product (CP) normalization, and
     * places the result into this transform.
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     *
     * @param t1 the transform to be normalized
     */
    public final void normalizeCP(PMatrix t1) {
	set(t1);
	normalizeCP();
    }
    
    /**
     * 
     * @return the inverse of this matrix (without changing this one)
     */
    public PMatrix inverse()
    {
        PMatrix result = new PMatrix(this);
        result.invert();
        return result;
    }
    
     /**
     * Sets the value of this transform to the inverse of the passed
     * Transform3D parameter.  This method uses the transform type
     * to determine the optimal algorithm for inverting transform t1.
     * @param t1  the transform to be inverted
     * @exception SingularMatrixException thrown if transform t1 is
     * not invertible
     */
    public final void invert(PMatrix t1) {
	if (t1 == this) {
	    invert();
	} else if (t1.isAffine()) {
	    // We can't use invertOrtho() because of numerical
	    // instability unless we set tolerance of ortho test to 0
	    invertAffine(t1);
	} else {
	    invertGeneral(t1);
	}
    }

    /**
     * Inverts this transform in place.  This method uses the transform
     * type to determine the optimal algorithm for inverting this transform.
     * @exception SingularMatrixException thrown if this transform is
     * not invertible
     */
    public final void invert() {
	if (isAffine()) {
	    invertAffine();
	} else {
	    invertGeneral(this);
	}
    }
    
    /**
     * Calculates and returns the determinant of this transform.
     * @return  the float precision determinant
     */
     public final float determinant() {

	 if (isAffine()) {
	     return mat[0]*(mat[5]*mat[10] - mat[6]*mat[9]) -
	 	    mat[1]*(mat[4]*mat[10] - mat[6]*mat[8]) +
		    mat[2]*(mat[4]*mat[ 9] - mat[5]*mat[8]);
	 }
	 // cofactor exapainsion along first row
	 return mat[0]*(mat[5]*(mat[10]*mat[15] - mat[11]*mat[14]) -
			mat[6]*(mat[ 9]*mat[15] - mat[11]*mat[13]) +
			mat[7]*(mat[ 9]*mat[14] - mat[10]*mat[13])) -
	        mat[1]*(mat[4]*(mat[10]*mat[15] - mat[11]*mat[14]) -
	                mat[6]*(mat[ 8]*mat[15] - mat[11]*mat[12]) +
			mat[7]*(mat[ 8]*mat[14] - mat[10]*mat[12])) +
	        mat[2]*(mat[4]*(mat[ 9]*mat[15] - mat[11]*mat[13]) -
			mat[5]*(mat[ 8]*mat[15] - mat[11]*mat[12]) +
			mat[7]*(mat[ 8]*mat[13] - mat[ 9]*mat[12])) -
	        mat[3]*(mat[4]*(mat[ 9]*mat[14] - mat[10]*mat[13]) -
			mat[5]*(mat[ 8]*mat[14] - mat[10]*mat[12]) +
			mat[6]*(mat[ 8]*mat[13] - mat[ 9]*mat[12]));
     }
     
     
    /**
     * Returns true if all of the data members of transform t1 are
     * equal to the corresponding data members in this Transform3D.
     * @param t1  the transform with which the comparison is made
     * @return  true or false
     */
    public boolean equals(PMatrix t1) {
	return (t1 != null) &&
	       (mat[0] == t1.mat[0]) && (mat[1] == t1.mat[1]) &&
	       (mat[2] == t1.mat[2]) && (mat[3] == t1.mat[3]) &&
	       (mat[4] == t1.mat[4]) && (mat[5] == t1.mat[5]) &&
	       (mat[6] == t1.mat[6]) && (mat[7] == t1.mat[7]) &&
	       (mat[8] == t1.mat[8]) && (mat[9] == t1.mat[9]) &&
	       (mat[10] == t1.mat[10]) && (mat[11] == t1.mat[11]) &&
	       (mat[12] == t1.mat[12]) && (mat[13] == t1.mat[13]) &&
	       (mat[14] == t1.mat[14]) && ( mat[15] == t1.mat[15]);
    }

   /**
     * Returns true if the Object o1 is of type Transform3D and all of the
     * data members of o1 are equal to the corresponding data members in
     * this Transform3D.
     * @param o1  the object with which the comparison is made.
     * @return  true or false
     */
    @Override
    public boolean equals(Object o1) {
	return (o1 instanceof PMatrix) && equals((PMatrix) o1);
    }

    /**
     * Returns true if the L-infinite distance between this matrix
     * and matrix m1 is less than or equal to the epsilon parameter,
     * otherwise returns false.  The L-infinite
     * distance is equal to
     * MAX[i=0,1,2,3 ; j=0,1,2,3 ; abs[(this.m(i,j) - m1.m(i,j)]
     * @param t1  the transform to be compared to this transform
     * @param epsilon  the threshold value
     */
    public boolean epsilonEquals(PMatrix t1, float epsilon) {
        float diff;

        for (int i=0 ; i<16 ; i++) {
	    diff = mat[i] - t1.mat[i];
	    if ((diff < 0 ? -diff : diff) > epsilon) {
		return false;
	    }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.mat != null ? this.mat.hashCode() : 0);
        return hash;
    }
    
    /**
     * Helping function that specifies the position and orientation of a
     * view matrix. The inverse of this transform can be used to control
     * the ViewPlatform object within the scene graph.
     * @param eye the location of the eye
     * @param center a point in the virtual world where the eye is looking
     * @param up an up vector specifying the frustum's up direction
     */
    public void lookAt(Vector3f eye, Vector3f center, Vector3f up) 
    {
        Vector4f eye4 = new Vector4f(eye.x, eye.y, eye.z, 1.0f);
        Vector4f center4 = new Vector4f(center.x, center.y, center.z, 1.0f);
        lookAt(eye4, center4, up);
    }
    
    /**
     * Helping function that specifies the position and orientation of a
     * view matrix. The inverse of this transform can be used to control
     * the ViewPlatform object within the scene graph.
     * @param eye the location of the eye
     * @param center a point in the virtual world where the eye is looking
     * @param up an up vector specifying the frustum's up direction
     */
    public void lookAt(Vector4f eye, Vector4f center, Vector3f up) {
        float forwardx,forwardy,forwardz,invMag;
        float upx,upy,upz;
        float sidex,sidey,sidez;

        forwardx =  eye.x - center.x;
        forwardy =  eye.y - center.y;
        forwardz =  eye.z - center.z;

        invMag = (float) (1.0f / Math.sqrt(forwardx * forwardx + forwardy * forwardy + forwardz * forwardz));
        forwardx = forwardx*invMag;
        forwardy = forwardy*invMag;
        forwardz = forwardz*invMag;


        invMag = (float) (1.0f / Math.sqrt(up.x * up.x + up.y * up.y + up.z * up.z));
        upx = up.x*invMag;
        upy = up.y*invMag;
        upz = up.z*invMag;

	// side = Up cross forward
	sidex = upy*forwardz-forwardy*upz;
	sidey = upz*forwardx-upx*forwardz;
	sidez = upx*forwardy-upy*forwardx;

	invMag = (float) (1.0f / Math.sqrt(sidex * sidex + sidey * sidey + sidez * sidez));
	sidex *= invMag;
	sidey *= invMag;
	sidez *= invMag;

	// recompute up = forward cross side

	upx = forwardy*sidez-sidey*forwardz;
	upy = forwardz*sidex-forwardx*sidez;
	upz = forwardx*sidey-forwardy*sidex;

	// transpose because we calculated the inverse of what we want
        mat[0] = sidex;
        mat[1] = sidey;
        mat[2] = sidez;

	mat[4] = upx;
	mat[5] = upy;
	mat[6] = upz;

	mat[8] =  forwardx;
	mat[9] =  forwardy;
	mat[10] = forwardz;

        mat[3] = -eye.x*mat[0] + -eye.y*mat[1] + -eye.z*mat[2];
        mat[7] = -eye.x*mat[4] + -eye.y*mat[5] + -eye.z*mat[6];
        mat[11] = -eye.x*mat[8] + -eye.y*mat[9] + -eye.z*mat[10];

	mat[12] = mat[13] = mat[14] = 0;
	mat[15] = 1;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }
    
    /**
     * Creates a perspective projection transform that mimics a standard,
     * camera-based,
     * view-model.  This transform maps coordinates from Eye Coordinates (EC)
     * to Clipping Coordinates (CC).  Note that unlike the similar function
     * in OpenGL, the clipping coordinates generated by the resulting
     * transform are in a right-handed coordinate system
     * (as are all other coordinate systems in Java 3D).
     * <p>
     * The frustum function-call establishes a view model with the eye
     * at the apex of a symmetric view frustum. The arguments
     * define the frustum and its associated perspective projection:
     * (left, bottom, -near) and (right, top, -near) specify the
     * point on the near clipping plane that maps onto the
     * lower-left and upper-right corners of the window respectively,
     * assuming the eye is located at (0, 0, 0).
     * @param left the vertical line on the left edge of the near
     * clipping plane mapped to the left edge of the graphics window
     * @param right the vertical line on the right edge of the near
     * clipping plane mapped to the right edge of the graphics window
     * @param bottom the horizontal line on the bottom edge of the near
     * clipping plane mapped to the bottom edge of the graphics window
     * @param top the horizontal line on the top edge of the near
     * @param near the distance to the frustum's near clipping plane.
     * This value must be positive, (the value -near is the location of the
     * near clip plane).
     * @param far the distance to the frustum's far clipping plane.
     * This value must be positive, and must be greater than near.
     */
    public void frustum(float left, float right,
			float bottom, float top,
			float near, float far) {
	float dx = 1/(right - left);
	float dy = 1/(top - bottom);
	float dz = 1/(far - near);

	mat[0] = (2*near)*dx;
	mat[5] = (2*near)*dy;
	mat[10] = (far+near)*dz;
	mat[2] = (right+left)*dx;
	mat[6] = (top+bottom)*dy;
	mat[11] = (2*far*near)*dz;
	mat[14] = -1;
	mat[1] = mat[3] = mat[4] = mat[7] = mat[8] = mat[9] = mat[12]
	    = mat[13] = mat[15] = 0;

	// Matrix is a projection transform
	type = 0;
	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }


    /**
     * Creates a perspective projection transform that mimics a standard,
     * camera-based,
     * view-model.  This transform maps coordinates from Eye Coordinates (EC)
     * to Clipping Coordinates (CC).  Note that unlike the similar function
     * in OpenGL, the clipping coordinates generated by the resulting
     * transform are in a right-handed coordinate system
     * (as are all other coordinate systems in Java 3D). Also note that the
     * field of view is specified in radians.
     * @param fovx specifies the field of view in the x direction, in radians
     * @param aspect specifies the aspect ratio and thus the field of
     * view in the x direction. The aspect ratio is the ratio of x to y,
     * or width to height.
     * @param zNear the distance to the frustum's near clipping plane.
     * This value must be positive, (the value -zNear is the location of the
     * near clip plane).
     * @param zFar the distance to the frustum's far clipping plane
     */
    public void perspective(float fovx, float aspect,
			    float zNear, float zFar) {
	float sine, cotangent, deltaZ;
	float half_fov = fovx * 0.5f;

	deltaZ = zFar - zNear;
	sine = (float) Math.sin(half_fov);
//	if ((deltaZ == 0.0) || (sine == 0.0) || (aspect == 0.0)) {
//	    return;
//	}
	cotangent = (float) (Math.cos(half_fov) / sine);

	mat[0] = cotangent;
	mat[5] = cotangent * aspect;
	mat[10] = (zFar + zNear) / deltaZ;
	mat[11] = 2.0f * zNear * zFar / deltaZ;
	mat[14] = -1.0f;
	mat[1] = mat[2] = mat[3] = mat[4] = mat[6] = mat[7] = mat[8] =
	    mat[9] = mat[12] = mat[13] = mat[15] = 0;

	// Matrix is a projection transform
	type = 0;
	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }


    /**
     * Creates an orthographic projection transform that mimics a standard,
     * camera-based,
     * view-model.  This transform maps coordinates from Eye Coordinates (EC)
     * to Clipping Coordinates (CC).  Note that unlike the similar function
     * in OpenGL, the clipping coordinates generated by the resulting
     * transform are in a right-handed coordinate system
     * (as are all other coordinate systems in Java 3D).
     * @param left the vertical line on the left edge of the near
     * clipping plane mapped to the left edge of the graphics window
     * @param right the vertical line on the right edge of the near
     * clipping plane mapped to the right edge of the graphics window
     * @param bottom the horizontal line on the bottom edge of the near
     * clipping plane mapped to the bottom edge of the graphics window
     * @param top the horizontal line on the top edge of the near
     * clipping plane mapped to the top edge of the graphics window
     * @param near the distance to the frustum's near clipping plane
     * (the value -near is the location of the near clip plane)
     * @param far the distance to the frustum's far clipping plane
     */
    public void ortho(float left, float right, float bottom,
                        float top, float near, float far) {
	float deltax = 1/(right - left);
	float deltay = 1/(top - bottom);
	float deltaz = 1/(far - near);

//	if ((deltax == 0.0) || (deltay == 0.0) || (deltaz == 0.0)) {
//	    return;
//	}

	mat[0] = 2.0f * deltax;
	mat[3] = -(right + left) * deltax;
	mat[5] = 2.0f * deltay;
	mat[7] = -(top + bottom) * deltay;
	mat[10] = 2.0f * deltaz;
	mat[11] = (far + near) * deltaz;
	mat[1] = mat[2] =  mat[4] = mat[6] = mat[8] =
	    mat[9] = mat[12] = mat[13] = mat[14] = 0;
	    mat[15] = 1;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }
    
    public boolean isAutoNormalize() 
    {
        return autoNormalize;
    }

    public void setAutoNormalize(boolean autoNormalize) 
    {
        this.autoNormalize = autoNormalize;
    }
    
    
   /**
     * Adds this transform to transform t1 and places the result into
     * this: this = this + t1.
     * @param t1  the transform to be added to this transform
     */
    public final void add(PMatrix t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] += t1.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Adds transforms t1 and t2 and places the result into this transform.
     * @param t1  the transform to be added
     * @param t2  the transform to be added
     */
    public final void add(PMatrix t1, PMatrix t2) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] + t2.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Subtracts transform t1 from this transform and places the result
     * into this: this = this - t1.
     * @param t1  the transform to be subtracted from this transform
     */
    public final void sub(PMatrix t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] -= t1.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }


    /**
     * Subtracts transform t2 from transform t1 and places the result into
     * this: this = t1 - t2.
     * @param t1   the left transform
     * @param t2   the right transform
     */
    public final void sub(PMatrix t1, PMatrix t2) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] - t2.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }
   
    final boolean isAffine() 
    {
        if ((dirtyBits & AFFINE_BIT) != 0) 
        {
            classifyAffine();
        }
        return ((type & AFFINE) != 0);
    }
    
    private final void classifyAffine() 
    {
        if (!isInfOrNaN() &&
                almostZero(mat[12]) &&
                almostZero(mat[13]) &&
                almostZero(mat[14]) &&
                almostOne(mat[15])) {
	    type |= AFFINE;
	} else {
	    type &= ~AFFINE;
	}
	dirtyBits &= ~AFFINE_BIT;
    }

    // Fix for Issue 167 -- don't classify matrices with Infinity or NaN values
    // as affine
    private final boolean isInfOrNaN() 
    {
        // The following is a faster version of the check.
        // Instead of 3 tests per array element (float.isInfinite is 2 tests),
        // for a total of 48 tests, we will do 16 multiplies and 1 test.
        float d = 0.0f;
        for (int i = 0; i < 16; i++) {
            d *= mat[i];
        }

        return d != 0.0;
    }
    
    private boolean isInfOrNaN(float val) {
        return Float.isNaN(val) || Float.isInfinite(val);
    }
     
    private boolean isInfOrNaN(Vector3f v) {
        return (Float.isNaN(v.x) || Float.isInfinite(v.x) ||
                Float.isNaN(v.y) || Float.isInfinite(v.y) ||
                Float.isNaN(v.z) || Float.isInfinite(v.z));
    }
    
     // Fix for Issue 253
    // Methods to check input parameters for Infinity or NaN values
    private final boolean isInfOrNaN(Quaternion q) {
        return (Float.isNaN(q.x) || Float.isInfinite(q.x) ||
                Float.isNaN(q.y) || Float.isInfinite(q.y) ||
                Float.isNaN(q.z) || Float.isInfinite(q.z) ||
                Float.isNaN(q.w) || Float.isInfinite(q.w));
    }
     
    private static final boolean almostZero(float a) {
	return ((a < EPSILON_ABSOLUTE) && (a > -EPSILON_ABSOLUTE));
    }
    
    private static final boolean almostOne(float a) {
	return ((a < 1+EPSILON_ABSOLUTE) && (a > 1-EPSILON_ABSOLUTE));
    }

    private static final boolean almostEqual(float a, float b) {
	float diff = a-b;

	if (diff >= 0) {
	    if (diff < EPSILON) {
		return true;
	    }
	    // a > b
	    if ((b > 0) || (a > -b)) {
		return (diff < EPSILON_RELATIVE*a);
	    } else {
		return (diff < -EPSILON_RELATIVE*b);
	    }

	} else {
	    if (diff > -EPSILON) {
		return true;
	    }
	    // a < b
	    if ((b < 0) || (-a > b)) {
		return (diff > EPSILON_RELATIVE*a);
	    } else {
		return (diff > -EPSILON_RELATIVE*b);
	    }
	}
    }
    
    final private void computeScaleRotation(boolean forceSVD) {

	if(rot == null)
	    rot = new float[9];

	if(scales == null)
	    scales = new float[3];

	if ((!forceSVD || ((dirtyBits & SVD_BIT) == 0)) && isAffine()) {
	    if (isCongruent()) {
		if (((dirtyBits & RIGID_BIT) == 0) &&
                    ((type & RIGID) != 0)) {
		    rot[0] = mat[0];
		    rot[1] = mat[1];
		    rot[2] = mat[2];
		    rot[3] = mat[4];
		    rot[4] = mat[5];
		    rot[5] = mat[6];
		    rot[6] = mat[8];
		    rot[7] = mat[9];
		    rot[8] = mat[10];
		    scales[0] = scales[1] = scales[2] = 1;
		    dirtyBits &= (~ROTATION_BIT | ~SCALE_BIT);
		    return;
		}
		float s = (float) Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8]);
		if (s == 0) {
		    compute_svd(this, scales, rot);
		    return;
		}
		scales[0] = scales[1] = scales[2] = s;
		s = 1/s;
		rot[0] = mat[0]*s;
		rot[1] = mat[1]*s;
		rot[2] = mat[2]*s;
		rot[3] = mat[4]*s;
		rot[4] = mat[5]*s;
		rot[5] = mat[6]*s;
		rot[6] = mat[8]*s;
		rot[7] = mat[9]*s;
		rot[8] = mat[10]*s;
		dirtyBits &= (~ROTATION_BIT | ~SCALE_BIT);
		return;
	    }
	    if (isOrtho()) {
		float s;

		scales[0] = (float) Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8]);
		scales[1] = (float) Math.sqrt(mat[1]*mat[1] + mat[5]*mat[5] + mat[9]*mat[9]);
		scales[2] = (float) Math.sqrt(mat[2]*mat[2] + mat[6]*mat[6] + mat[10]*mat[10]);

		if ((scales[0] == 0) || (scales[1] == 0) || (scales[2] == 0)) {
		    compute_svd(this, scales, rot);
		    return;
		}
		s = 1/scales[0];
		rot[0] = mat[0]*s;
		rot[3] = mat[4]*s;
		rot[6] = mat[8]*s;
		s = 1/scales[1];
		rot[1] = mat[1]*s;
		rot[4] = mat[5]*s;
		rot[7] = mat[9]*s;
		s = 1/scales[2];
		rot[2] = mat[2]*s;
		rot[5] = mat[6]*s;
		rot[8] = mat[10]*s;
		dirtyBits &= (~ROTATION_BIT | ~SCALE_BIT);
		return;
	    }
	}
	// fall back to use SVD decomposition
	compute_svd(this, scales, rot);
	dirtyBits &= ~ROTSCALESVD_DIRTY;
    }
    
    final boolean isCongruent() {
            if ((dirtyBits & CONGRUENT_BIT) != 0) {
                // This will also classify AFFINE
                    classifyRigid();
            }
            return ((type & CONGRUENT) != 0);
        }
    
    private void compute_svd(PMatrix matrix, float[] outScale,
			     float[] outRot) {

	int i,j;
	float g,scale;
	float m[] = new float[9];

	// if (!svdAllocd) {
	float[] u1 = new float[9];
	float[] v1 = new float[9];
	float[] t1 = new float[9];
	float[] t2 = new float[9];
	// float[] ts = new float[9];
	// float[] svdTmp = new float[9]; It is replaced by t1
	float[] svdRot = new float[9];
	// float[] single_values = new float[3]; replaced by t2

	float[] e = new float[3];
	float[] svdScales = new float[3];


	// XXXX: initialize to 0's if alread allocd? Should not have to, since
	// no operations depend on these being init'd to zero.

	int converged, negCnt=0;
	float cs,sn;
	float c1,c2,c3,c4;
	float s1,s2,s3,s4;
	float cl1,cl2,cl3;


        svdRot[0] = m[0] = matrix.mat[0];
	svdRot[1] = m[1] = matrix.mat[1];
	svdRot[2] = m[2] = matrix.mat[2];
	svdRot[3] = m[3] = matrix.mat[4];
	svdRot[4] = m[4] = matrix.mat[5];
	svdRot[5] = m[5] = matrix.mat[6];
	svdRot[6] = m[6] = matrix.mat[8];
	svdRot[7] = m[7] = matrix.mat[9];
	svdRot[8] = m[8] = matrix.mat[10];

	// u1

	if( m[3]*m[3] < EPS ) {
	    u1[0] = 1.0f; u1[1] = 0.0f; u1[2] = 0.0f;
	    u1[3] = 0.0f; u1[4] = 1.0f; u1[5] = 0.0f;
	    u1[6] = 0.0f; u1[7] = 0.0f; u1[8] = 1.0f;
	} else if( m[0]*m[0] < EPS ) {
	    t1[0] = m[0];
	    t1[1] = m[1];
	    t1[2] = m[2];
	    m[0] = m[3];
	    m[1] = m[4];
	    m[2] = m[5];

	    m[3] = -t1[0]; // zero
	    m[4] = -t1[1];
	    m[5] = -t1[2];

	    u1[0] =  0.0f; u1[1] = 1.0f;  u1[2] = 0.0f;
	    u1[3] = -1.0f; u1[4] = 0.0f;  u1[5] = 0.0f;
	    u1[6] =  0.0f; u1[7] = 0.0f;  u1[8] = 1.0f;
	} else {
	    g = (float) (1.0 / Math.sqrt(m[0] * m[0] + m[3] * m[3]));
	    c1 = m[0]*g;
	    s1 = m[3]*g;
	    t1[0] = c1*m[0] + s1*m[3];
	    t1[1] = c1*m[1] + s1*m[4];
	    t1[2] = c1*m[2] + s1*m[5];

	    m[3] = -s1*m[0] + c1*m[3]; // zero
	    m[4] = -s1*m[1] + c1*m[4];
	    m[5] = -s1*m[2] + c1*m[5];

	    m[0] = t1[0];
	    m[1] = t1[1];
	    m[2] = t1[2];
	    u1[0] = c1;  u1[1] = s1;  u1[2] = 0.0f;
	    u1[3] = -s1; u1[4] = c1;  u1[5] = 0.0f;
	    u1[6] = 0.0f; u1[7] = 0.0f; u1[8] = 1.0f;
	}

	// u2

	if( m[6]*m[6] < EPS  ) {
	} else if( m[0]*m[0] < EPS ){
	    t1[0] = m[0];
	    t1[1] = m[1];
	    t1[2] = m[2];
	    m[0] = m[6];
	    m[1] = m[7];
	    m[2] = m[8];

	    m[6] = -t1[0]; // zero
	    m[7] = -t1[1];
	    m[8] = -t1[2];

	    t1[0] = u1[0];
	    t1[1] = u1[1];
	    t1[2] = u1[2];
	    u1[0] = u1[6];
	    u1[1] = u1[7];
	    u1[2] = u1[8];

	    u1[6] = -t1[0]; // zero
	    u1[7] = -t1[1];
	    u1[8] = -t1[2];
	} else {
	    g =         (float) (1.0 / Math.sqrt(m[0] * m[0] + m[6] * m[6]));
	    c2 = m[0]*g;
	    s2 = m[6]*g;
	    t1[0] = c2*m[0] + s2*m[6];
	    t1[1] = c2*m[1] + s2*m[7];
	    t1[2] = c2*m[2] + s2*m[8];

	    m[6] = -s2*m[0] + c2*m[6];
	    m[7] = -s2*m[1] + c2*m[7];
	    m[8] = -s2*m[2] + c2*m[8];
	    m[0] = t1[0];
	    m[1] = t1[1];
	    m[2] = t1[2];

	    t1[0] = c2*u1[0];
	    t1[1] = c2*u1[1];
	    u1[2]  = s2;

	    t1[6] = -u1[0]*s2;
	    t1[7] = -u1[1]*s2;
	    u1[8] = c2;
	    u1[0] = t1[0];
	    u1[1] = t1[1];
	    u1[6] = t1[6];
	    u1[7] = t1[7];
	}

	// v1

	if( m[2]*m[2] < EPS ) {
	    v1[0] = 1.0f; v1[1] = 0.0f; v1[2] = 0.0f;
	    v1[3] = 0.0f; v1[4] = 1.0f; v1[5] = 0.0f;
	    v1[6] = 0.0f; v1[7] = 0.0f; v1[8] = 1.0f;
	} else if( m[1]*m[1] < EPS ) {
	    t1[2] = m[2];
	    t1[5] = m[5];
	    t1[8] = m[8];
	    m[2] = -m[1];
	    m[5] = -m[4];
	    m[8] = -m[7];

	    m[1] = t1[2]; // zero
	    m[4] = t1[5];
	    m[7] = t1[8];

	    v1[0] =  1.0f; v1[1] = 0.0f;  v1[2] = 0.0f;
	    v1[3] =  0.0f; v1[4] = 0.0f;  v1[5] =-1.0f;
	    v1[6] =  0.0f; v1[7] = 1.0f;  v1[8] = 0.0f;
	} else {
	    g = (float) (1.0 / Math.sqrt(m[1] * m[1] + m[2] * m[2]));
	    c3 = m[1]*g;
	    s3 = m[2]*g;
	    t1[1] = c3*m[1] + s3*m[2];  // can assign to m[1]?
	    m[2] =-s3*m[1] + c3*m[2];  // zero
	    m[1] = t1[1];

	    t1[4] = c3*m[4] + s3*m[5];
	    m[5] =-s3*m[4] + c3*m[5];
	    m[4] = t1[4];

	    t1[7] = c3*m[7] + s3*m[8];
	    m[8] =-s3*m[7] + c3*m[8];
	    m[7] = t1[7];

	    v1[0] = 1.0f; v1[1] = 0.0f; v1[2] = 0.0f;
	    v1[3] = 0.0f; v1[4] =  c3; v1[5] = -s3;
	    v1[6] = 0.0f; v1[7] =  s3; v1[8] =  c3;
	}

	// u3

	if( m[7]*m[7] < EPS ) {
	} else if( m[4]*m[4] < EPS ) {
	    t1[3] = m[3];
	    t1[4] = m[4];
	    t1[5] = m[5];
	    m[3] = m[6];   // zero
	    m[4] = m[7];
	    m[5] = m[8];

	    m[6] = -t1[3]; // zero
	    m[7] = -t1[4]; // zero
	    m[8] = -t1[5];

	    t1[3] = u1[3];
	    t1[4] = u1[4];
	    t1[5] = u1[5];
	    u1[3] = u1[6];
	    u1[4] = u1[7];
	    u1[5] = u1[8];

	    u1[6] = -t1[3]; // zero
	    u1[7] = -t1[4];
	    u1[8] = -t1[5];

	} else {
	    g = (float) (1.0 / Math.sqrt(m[4] * m[4] + m[7] * m[7]));
	    c4 = m[4]*g;
	    s4 = m[7]*g;
	    t1[3] = c4*m[3] + s4*m[6];
	    m[6] =-s4*m[3] + c4*m[6];  // zero
	    m[3] = t1[3];

	    t1[4] = c4*m[4] + s4*m[7];
	    m[7] =-s4*m[4] + c4*m[7];
	    m[4] = t1[4];

	    t1[5] = c4*m[5] + s4*m[8];
	    m[8] =-s4*m[5] + c4*m[8];
	    m[5] = t1[5];

	    t1[3] = c4*u1[3] + s4*u1[6];
	    u1[6] =-s4*u1[3] + c4*u1[6];
	    u1[3] = t1[3];

	    t1[4] = c4*u1[4] + s4*u1[7];
	    u1[7] =-s4*u1[4] + c4*u1[7];
	    u1[4] = t1[4];

	    t1[5] = c4*u1[5] + s4*u1[8];
	    u1[8] =-s4*u1[5] + c4*u1[8];
	    u1[5] = t1[5];
	}

	t2[0] = m[0];
	t2[1] = m[4];
	t2[2] = m[8];
	e[0] = m[1];
	e[1] = m[5];

	if( e[0]*e[0]>EPS || e[1]*e[1]>EPS ) {
	    compute_qr( t2, e, u1, v1);
	}

	svdScales[0] = t2[0];
	svdScales[1] = t2[1];
	svdScales[2] = t2[2];


	// Do some optimization here. If scale is unity, simply return the rotation matric.
	if(almostOne(Math.abs(svdScales[0])) &&
	   almostOne(Math.abs(svdScales[1])) &&
	   almostOne(Math.abs(svdScales[2]))) {

	    for(i=0;i<3;i++)
		if(svdScales[i]<0.0)
		    negCnt++;

	    if((negCnt==0)||(negCnt==2)) {
		//System.err.println("Optimize!!");
		outScale[0] = outScale[1] = outScale[2] = (float) 1.0;
		for(i=0;i<9;i++)
		    outRot[i] = svdRot[i];

		return;
	    }
	}

	// XXXX: could eliminate use of t1 and t1 by making a new method which
	// transposes and multiplies two matricies
	transpose_mat(u1, t1);
	transpose_mat(v1, t2);


	svdReorder( m, t1, t2, svdRot, svdScales, outRot, outScale);
    }
    
    // True if type is ORTHO
    // Since ORTHO didn't take into account the last row.
    final boolean isOrtho() {
	if ((dirtyBits & ORTHO_BIT) != 0) {
	    if ((almostZero(mat[0]*mat[2] + mat[4]*mat[6] +
			    mat[8]*mat[10]) &&
		 almostZero(mat[0]*mat[1] + mat[4]*mat[5] +
			    mat[8]*mat[9]) &&
		 almostZero(mat[1]*mat[2] + mat[5]*mat[6] +
			    mat[9]*mat[10]))) {
		type |= ORTHO;
		dirtyBits &= ~ORTHO_BIT;
		return true;
	    } else {
		type &= ~ORTHO;
		dirtyBits &= ~ORTHO_BIT;
		return false;
	    }
	}
	return ((type & ORTHO) != 0);
    }
    
    private int compute_qr( float[] s, float[] e, float[] u, float[] v) {
	int i,j,k;
	boolean converged;
	float shift,ssmin,ssmax,r;

	float utemp,vtemp;
	float f,g;

	final int MAX_INTERATIONS = 10;
	final float CONVERGE_TOL = (float) 4.89E-15;

	float[]   cosl  = new float[2];
	float[]   cosr  = new float[2];
	float[]   sinl  = new float[2];
	float[]   sinr  = new float[2];
	float[]   qr_m  = new float[9];


	float c_b48 = 1.0f;
	float c_b71 = -1.0f;
	int first;
	converged = false;

	first = 1;

	if( Math.abs(e[1]) < CONVERGE_TOL || Math.abs(e[0]) < CONVERGE_TOL) converged = true;

	for(k=0;k<MAX_INTERATIONS && !converged;k++) {
	    shift = compute_shift( s[1], e[1], s[2]);
	    f = (Math.abs(s[0]) - shift) * (d_sign(c_b48, s[0]) + shift/s[0]);
	    g = e[0];
	    r = compute_rot(f, g, sinr, cosr,  0, first);
	    f = cosr[0] * s[0] + sinr[0] * e[0];
	    e[0] = cosr[0] * e[0] - sinr[0] * s[0];
	    g = sinr[0] * s[1];
	    s[1] = cosr[0] * s[1];

	    r = compute_rot(f, g, sinl, cosl, 0, first);
	    first = 0;
	    s[0] = r;
	    f = cosl[0] * e[0] + sinl[0] * s[1];
	    s[1] = cosl[0] * s[1] - sinl[0] * e[0];
	    g = sinl[0] * e[1];
	    e[1] =  cosl[0] * e[1];

	    r = compute_rot(f, g, sinr, cosr, 1, first);
	    e[0] = r;
	    f = cosr[1] * s[1] + sinr[1] * e[1];
	    e[1] = cosr[1] * e[1] - sinr[1] * s[1];
	    g = sinr[1] * s[2];
	    s[2] = cosr[1] * s[2];

	    r = compute_rot(f, g, sinl, cosl, 1, first);
	    s[1] = r;
	    f = cosl[1] * e[1] + sinl[1] * s[2];
	    s[2] = cosl[1] * s[2] - sinl[1] * e[1];
	    e[1] = f;

	    // update u  matrices
	    utemp = u[0];
	    u[0] = cosl[0]*utemp + sinl[0]*u[3];
	    u[3] = -sinl[0]*utemp + cosl[0]*u[3];
	    utemp = u[1];
	    u[1] = cosl[0]*utemp + sinl[0]*u[4];
	    u[4] = -sinl[0]*utemp + cosl[0]*u[4];
	    utemp = u[2];
	    u[2] = cosl[0]*utemp + sinl[0]*u[5];
	    u[5] = -sinl[0]*utemp + cosl[0]*u[5];

	    utemp = u[3];
	    u[3] = cosl[1]*utemp + sinl[1]*u[6];
	    u[6] = -sinl[1]*utemp + cosl[1]*u[6];
	    utemp = u[4];
	    u[4] = cosl[1]*utemp + sinl[1]*u[7];
	    u[7] = -sinl[1]*utemp + cosl[1]*u[7];
	    utemp = u[5];
	    u[5] = cosl[1]*utemp + sinl[1]*u[8];
	    u[8] = -sinl[1]*utemp + cosl[1]*u[8];

	    // update v  matrices

	    vtemp = v[0];
	    v[0] = cosr[0]*vtemp + sinr[0]*v[1];
	    v[1] = -sinr[0]*vtemp + cosr[0]*v[1];
	    vtemp = v[3];
	    v[3] = cosr[0]*vtemp + sinr[0]*v[4];
	    v[4] = -sinr[0]*vtemp + cosr[0]*v[4];
	    vtemp = v[6];
	    v[6] = cosr[0]*vtemp + sinr[0]*v[7];
	    v[7] = -sinr[0]*vtemp + cosr[0]*v[7];

	    vtemp = v[1];
	    v[1] = cosr[1]*vtemp + sinr[1]*v[2];
	    v[2] = -sinr[1]*vtemp + cosr[1]*v[2];
	    vtemp = v[4];
	    v[4] = cosr[1]*vtemp + sinr[1]*v[5];
	    v[5] = -sinr[1]*vtemp + cosr[1]*v[5];
	    vtemp = v[7];
	    v[7] = cosr[1]*vtemp + sinr[1]*v[8];
	    v[8] = -sinr[1]*vtemp + cosr[1]*v[8];

	    // if(debug)System.err.println("\n*********************** iteration #"+k+" ***********************\n");

	    qr_m[0] = s[0];  qr_m[1] = e[0]; qr_m[2] = 0.0f;
	    qr_m[3] =  0.0f;  qr_m[4] = s[1]; qr_m[5] =e[1];
	    qr_m[6] =  0.0f;  qr_m[7] =  0.0f; qr_m[8] =s[2];

	    if( Math.abs(e[1]) < CONVERGE_TOL || Math.abs(e[0]) < CONVERGE_TOL) converged = true;
	}

	if( Math.abs(e[1]) < CONVERGE_TOL ) {
	    compute_2X2( s[0],e[0],s[1],s,sinl,cosl,sinr,cosr, 0);

	    utemp = u[0];
	    u[0] = cosl[0]*utemp + sinl[0]*u[3];
	    u[3] = -sinl[0]*utemp + cosl[0]*u[3];
	    utemp = u[1];
	    u[1] = cosl[0]*utemp + sinl[0]*u[4];
	    u[4] = -sinl[0]*utemp + cosl[0]*u[4];
	    utemp = u[2];
	    u[2] = cosl[0]*utemp + sinl[0]*u[5];
	    u[5] = -sinl[0]*utemp + cosl[0]*u[5];

	    // update v  matrices

	    vtemp = v[0];
	    v[0] = cosr[0]*vtemp + sinr[0]*v[1];
	    v[1] = -sinr[0]*vtemp + cosr[0]*v[1];
	    vtemp = v[3];
	    v[3] = cosr[0]*vtemp + sinr[0]*v[4];
	    v[4] = -sinr[0]*vtemp + cosr[0]*v[4];
	    vtemp = v[6];
	    v[6] = cosr[0]*vtemp + sinr[0]*v[7];
	    v[7] = -sinr[0]*vtemp + cosr[0]*v[7];
	} else {
	    compute_2X2( s[1],e[1],s[2],s,sinl,cosl,sinr,cosr,1);

	    utemp = u[3];
	    u[3] = cosl[0]*utemp + sinl[0]*u[6];
	    u[6] = -sinl[0]*utemp + cosl[0]*u[6];
	    utemp = u[4];
	    u[4] = cosl[0]*utemp + sinl[0]*u[7];
	    u[7] = -sinl[0]*utemp + cosl[0]*u[7];
	    utemp = u[5];
	    u[5] = cosl[0]*utemp + sinl[0]*u[8];
	    u[8] = -sinl[0]*utemp + cosl[0]*u[8];

	    // update v  matrices

	    vtemp = v[1];
	    v[1] = cosr[0]*vtemp + sinr[0]*v[2];
	    v[2] = -sinr[0]*vtemp + cosr[0]*v[2];
	    vtemp = v[4];
	    v[4] = cosr[0]*vtemp + sinr[0]*v[5];
	    v[5] = -sinr[0]*vtemp + cosr[0]*v[5];
	    vtemp = v[7];
	    v[7] = cosr[0]*vtemp + sinr[0]*v[8];
	    v[8] = -sinr[0]*vtemp + cosr[0]*v[8];
	}

	return(0);
    }
    
    static int compute_2X2( float f, float g, float h, float[] single_values,
			    float[] snl, float[] csl, float[] snr, float[] csr, int index)  {

	float c_b3 = 2.0f;
	float c_b4 = 1.0f;

	float d__1;
	int pmax;
	float temp;
	boolean swap;
	float a, d, l, m, r, s, t, tsign, fa, ga, ha;
	float ft, gt, ht, mm;
	boolean gasmal;
	float tt, clt, crt, slt, srt;
	float ssmin,ssmax;

	ssmax = single_values[0];
	ssmin = single_values[1];
	clt = 0.0f;
	crt = 0.0f;
	slt = 0.0f;
	srt = 0.0f;
	tsign = 0.0f;

	ft = f;
	fa = Math.abs(ft);
	ht = h;
	ha = Math.abs(h);

	pmax = 1;
	if( ha > fa)
	    swap = true;
	else
	    swap = false;

	if (swap) {
	    pmax = 3;
	    temp = ft;
	    ft = ht;
	    ht = temp;
	    temp = fa;
	    fa = ha;
	    ha = temp;

	}
	gt = g;
	ga = Math.abs(gt);
	if (ga == 0.) {

	    single_values[1] = ha;
	    single_values[0] = fa;
	    clt = 1.0f;
	    crt = 1.0f;
	    slt = 0.0f;
	    srt = 0.0f;
	} else {
	    gasmal = true;

	    if (ga > fa) {
		pmax = 2;
		if (fa / ga < EPS) {

		    gasmal = false;
		    ssmax = ga;
		    if (ha > 1.) {
			ssmin = fa / (ga / ha);
		    } else {
			ssmin = fa / ga * ha;
		    }
		    clt = 1.0f;
		    slt = ht / gt;
		    srt = 1.0f;
		    crt = ft / gt;
		}
	    }
	    if (gasmal) {

		d = fa - ha;
		if (d == fa) {

		    l = 1.0f;
		} else {
		    l = d / fa;
		}

		m = gt / ft;

		t = 2 - l;

		mm = m * m;
		tt = t * t;
		s = (float) Math.sqrt(tt + mm);

		if (l == 0.) {
		    r = Math.abs(m);
		} else {
		    r = (float) Math.sqrt(l * l + mm);
		}

		a = (s + r) * 0.5f;

		if (ga > fa) {
		    pmax = 2;
		    if (fa / ga < EPS) {

			gasmal = false;
			ssmax = ga;
			if (ha > 1.) {
			    ssmin = fa / (ga / ha);
			} else {
			    ssmin = fa / ga * ha;
			}
			clt = 1.0f;
			slt = ht / gt;
			srt = 1.0f;
			crt = ft / gt;
		    }
		}
		if (gasmal) {

		    d = fa - ha;
		    if (d == fa) {

			l = 1.0f;
		    } else {
			l = d / fa;
		    }

		    m = gt / ft;

		    t = 2 - l;

		    mm = m * m;
		    tt = t * t;
		    s = (float) Math.sqrt(tt + mm);

		    if (l == 0.) {
			r = Math.abs(m);
		    } else {
			r = (float) Math.sqrt(l * l + mm);
		    }

		    a = (s + r) * 0.5f;


		    ssmin = ha / a;
		    ssmax = fa * a;
		    if (mm == 0.) {

			if (l == 0.) {
			    t = d_sign(c_b3, ft) * d_sign(c_b4, gt);
			} else {
			    t = gt / d_sign(d, ft) + m / t;
			}
		    } else {
			t = (float) ((m / (s + t) + m / (r + l)) * (a + 1.));
		    }
		    l = (float) Math.sqrt(t * t + 4);
		    crt = 2 / l;
		    srt = t / l;
		    clt = (crt + srt * m) / a;
		    slt = ht / ft * srt / a;
		}
	    }
	    if (swap) {
		csl[0] = srt;
		snl[0] = crt;
		csr[0] = slt;
		snr[0] = clt;
	    } else {
		csl[0] = clt;
		snl[0] = slt;
		csr[0] = crt;
		snr[0] = srt;
	    }

	    if (pmax == 1) {
		tsign = d_sign(c_b4, csr[0]) * d_sign(c_b4, csl[0]) * d_sign(c_b4, f);
	    }
	    if (pmax == 2) {
		tsign = d_sign(c_b4, snr[0]) * d_sign(c_b4, csl[0]) * d_sign(c_b4, g);
	    }
	    if (pmax == 3) {
		tsign = d_sign(c_b4, snr[0]) * d_sign(c_b4, snl[0]) * d_sign(c_b4, h);
	    }
	    single_values[index] = d_sign(ssmax, tsign);
	    d__1 = tsign * d_sign(c_b4, f) * d_sign(c_b4, h);
	    single_values[index+1] = d_sign(ssmin, d__1);


	}
	return 0;
    }
    
    static final float max( float a, float b) {
	return ( a > b ? a : b);
    }

    static final float min( float a, float b) {
	return ( a < b ? a : b);
    }

    static final float d_sign(float a, float b) {
        float x =  (a >= 0 ? a : - a);
	return( b >= 0 ? x : -x);
    }
    
    static  float compute_rot( float f, float g, float[] sin, float[] cos, int index, int first) {
	int i__1;
	float d__1, d__2;
	float cs,sn;
	int i;
	float scale;
	int count;
	float f1, g1;
	float r;
	final float safmn2 = (float) 2.002083095183101E-146;
	final float safmx2 = (float) 4.994797680505588E+145;

	if (g == 0.) {
	    cs = 1.0f;
	    sn = 0.0f;
	    r = f;
	} else if (f == 0.0f) {
	    cs = 0.0f;
	    sn = 1.0f;
	    r = g;
	} else {
	    f1 = f;
	    g1 = g;
	    scale = max(Math.abs(f1),Math.abs(g1));
	    if (scale >= safmx2) {
		count = 0;
		while(scale >= safmx2) {
		    ++count;
		    f1 *= safmn2;
		    g1 *= safmn2;
		    scale = max(Math.abs(f1),Math.abs(g1));
		}
		r = (float) Math.sqrt(f1*f1 + g1*g1);
		cs = f1 / r;
		sn = g1 / r;
		i__1 = count;
		for (i = 1; i <= count; ++i) {
		    r *= safmx2;
		}
	    } else if (scale <= safmn2) {
		count = 0;
		while(scale <= safmn2) {
		    ++count;
		    f1 *= safmx2;
		    g1 *= safmx2;
		    scale = max(Math.abs(f1),Math.abs(g1));
		}
		r = (float) Math.sqrt(f1*f1 + g1*g1);
		cs = f1 / r;
		sn = g1 / r;
		i__1 = count;
		for (i = 1; i <= count; ++i) {
		    r *= safmn2;
		}
	    } else {
		r = (float) Math.sqrt(f1*f1 + g1*g1);
		cs = f1 / r;
		sn = g1 / r;
	    }
	    if (Math.abs(f) > Math.abs(g) && cs < 0.) {
		cs = -cs;
		sn = -sn;
		r = -r;
	    }
	}
	sin[index] = sn;
	cos[index] = cs;
	return r;
    }
    
    // same amount of work to classify rigid and congruent
    private final void classifyRigid() {

	if ((dirtyBits & AFFINE_BIT) != 0) {
	    // should not touch ORTHO bit
	    type &= ORTHO;
	    classifyAffine();
	} else {
	    // keep the affine bit if there is one
	    // and clear the others (CONGRUENT/RIGID) bit
	    type &= (ORTHO | AFFINE);
	}

	if ((type & AFFINE) != 0) {
	    // checking orthogonal condition
	    if (isOrtho()) {
		if ((dirtyBits & SCALE_BIT) != 0) {
		    float s0 = mat[0]*mat[0] + mat[4]*mat[4] +
			mat[8]*mat[8];
		    float s1 = mat[1]*mat[1] + mat[5]*mat[5] +
			mat[9]*mat[9];
		    if (almostEqual(s0, s1)) {
			float s2 = mat[2]*mat[2] + mat[6]*mat[6] +
			    mat[10]*mat[10];
			if (almostEqual(s2, s0)) {
			    type |= CONGRUENT;
			    // Note that scales[0] = sqrt(s0);
			    if (almostOne(s0)) {
				type |= RIGID;
			    }
			}
		    }
		} else {
		    if(scales == null)
			scales = new float[3];

		    float s = scales[0];
		    if (almostEqual(s, scales[1]) &&
			almostEqual(s, scales[2])) {
			type |= CONGRUENT;
			if (almostOne(s)) {
			    type |= RIGID;
			}
		    }
		}
	    }
	}
	dirtyBits &= (~RIGID_BIT | ~CONGRUENT_BIT);
    }
    
    private void svdReorder( float[] m, float[] t1, float[] t2, float[] rot,
			     float[] scales, float[] outRot, float[] outScale) {

	int in0, in1, in2, index,i;
	int[] svdOut = new int[3];
	float[] svdMag = new float[3];


	// check for rotation information in the scales
	if(scales[0] < 0.0 ) {   // move the rotation info to rotation matrix
	    scales[0] = -scales[0];
	    t2[0] = -t2[0];
	    t2[1] = -t2[1];
	    t2[2] = -t2[2];
	}
	if(scales[1] < 0.0 ) {   // move the rotation info to rotation matrix
	    scales[1] = -scales[1];
	    t2[3] = -t2[3];
	    t2[4] = -t2[4];
	    t2[5] = -t2[5];
	}
	if(scales[2] < 0.0 ) {   // move the rotation info to rotation matrix
	    scales[2] = -scales[2];
	    t2[6] = -t2[6];
	    t2[7] = -t2[7];
	    t2[8] = -t2[8];
	}


	mat_mul(t1,t2,rot);

	// check for equal scales case  and do not reorder
	if(almostEqual(Math.abs(scales[0]), Math.abs(scales[1])) &&
	   almostEqual(Math.abs(scales[1]), Math.abs(scales[2]))   ){
	    for(i=0;i<9;i++){
		outRot[i] = rot[i];
	    }
	    for(i=0;i<3;i++){
		outScale[i] = scales[i];
	    }

	}else {

	    // sort the order of the results of SVD
	    if( scales[0] > scales[1]) {
		if( scales[0] > scales[2] ) {
		    if( scales[2] > scales[1] ) {
			svdOut[0] = 0; svdOut[1] = 2; svdOut[2] = 1; // xzy
		    } else {
			svdOut[0] = 0; svdOut[1] = 1; svdOut[2] = 2; // xyz
		    }
		} else {
		    svdOut[0] = 2; svdOut[1] = 0; svdOut[2] = 1; // zxy
		}
	    } else {  // y > x
		if( scales[1] > scales[2] ) {
		    if( scales[2] > scales[0] ) {
			svdOut[0] = 1; svdOut[1] = 2; svdOut[2] = 0; // yzx
		    } else {
			svdOut[0] = 1; svdOut[1] = 0; svdOut[2] = 2; // yxz
		    }
		} else  {
		    svdOut[0] = 2; svdOut[1] = 1; svdOut[2] = 0; // zyx
		}
	    }


	    // sort the order of the input matrix
	    svdMag[0] = (m[0]*m[0] + m[1]*m[1] + m[2]*m[2]);
	    svdMag[1] = (m[3]*m[3] + m[4]*m[4] + m[5]*m[5]);
	    svdMag[2] = (m[6]*m[6] + m[7]*m[7] + m[8]*m[8]);


	    if( svdMag[0] > svdMag[1]) {
		if( svdMag[0] > svdMag[2] ) {
		    if( svdMag[2] > svdMag[1] )  {
			// 0 - 2 - 1
			in0 = 0; in2 = 1; in1 = 2;// xzy
		    } else {
			// 0 - 1 - 2
			in0 = 0; in1 = 1; in2 = 2; // xyz
		    }
		} else {
		    // 2 - 0 - 1
		    in2 = 0; in0 = 1; in1 = 2;  // zxy
		}
	    } else {  // y > x   1>0
		if( svdMag[1] > svdMag[2] ) {  // 1>2
		    if( svdMag[2] > svdMag[0] )  { // 2>0
			// 1 - 2 - 0
			in1 = 0; in2 = 1; in0 = 2; // yzx
		    } else {
			// 1 - 0 - 2
			in1 = 0; in0 = 1; in2 = 2; // yxz
		    }
		} else  {
		    // 2 - 1 - 0
		    in2 = 0; in1 = 1; in0 = 2; // zyx
		}
	    }


	    index = svdOut[in0];
	    outScale[0] = scales[index];

	    index = svdOut[in1];
	    outScale[1] = scales[index];

	    index = svdOut[in2];
	    outScale[2] = scales[index];

	    index = svdOut[in0];
	    if (outRot == null) {
//                MasterControl.getCoreLogger().severe("outRot == null");
            }
	    if (rot == null) {
 //               MasterControl.getCoreLogger().severe("rot == null");
            }

	    outRot[0] = rot[index];

	    index = svdOut[in0]+3;
	    outRot[0+3] = rot[index];

	    index = svdOut[in0]+6;
	    outRot[0+6] = rot[index];

	    index = svdOut[in1];
	    outRot[1] = rot[index];

	    index = svdOut[in1]+3;
	    outRot[1+3] = rot[index];

	    index = svdOut[in1]+6;
	    outRot[1+6] = rot[index];

	    index = svdOut[in2];
	    outRot[2] = rot[index];

	    index = svdOut[in2]+3;
	    outRot[2+3] = rot[index];

	    index = svdOut[in2]+6;
	    outRot[2+6] = rot[index];
	}

    }
    
    static private void  mat_mul(float[] m1, float[] m2, float[] m3) {

	float[] result = m3;
	if ((m1 == m3) || (m2 == m3)) {
	    result = new float[9];
	}

	result[0] =  m1[0]*m2[0] + m1[1]*m2[3] + m1[2]*m2[6];
	result[1] =  m1[0]*m2[1] + m1[1]*m2[4] + m1[2]*m2[7];
	result[2] =  m1[0]*m2[2] + m1[1]*m2[5] + m1[2]*m2[8];

	result[3] =  m1[3]*m2[0] + m1[4]*m2[3] + m1[5]*m2[6];
	result[4] =  m1[3]*m2[1] + m1[4]*m2[4] + m1[5]*m2[7];
	result[5] =  m1[3]*m2[2] + m1[4]*m2[5] + m1[5]*m2[8];

	result[6] =  m1[6]*m2[0] + m1[7]*m2[3] + m1[8]*m2[6];
	result[7] =  m1[6]*m2[1] + m1[7]*m2[4] + m1[8]*m2[7];
	result[8] =  m1[6]*m2[2] + m1[7]*m2[5] + m1[8]*m2[8];

	if (result != m3) {
	    for(int i=0;i<9;i++) {
		m3[i] = result[i];
	    }
	}
    }
    
    static final float compute_shift( float f, float g, float h) {
	float d__1, d__2;
	float fhmn, fhmx, c, fa, ga, ha, as, at, au;
	float ssmin;

	fa = Math.abs(f);
	ga = Math.abs(g);
	ha = Math.abs(h);
	fhmn = min(fa,ha);
	fhmx = max(fa,ha);
	if (fhmn == 0.) {
	    ssmin = 0.0f;
	    if (fhmx == 0.) {
	    } else {
		d__1 = min(fhmx,ga) / max(fhmx,ga);
	    }
	} else {
	    if (ga < fhmx) {
		as = fhmn / fhmx + 1.0f;
		at = (fhmx - fhmn) / fhmx;
		d__1 = ga / fhmx;
		au = d__1 * d__1;
		c = (float) (2 / (Math.sqrt(as * as + au) + Math.sqrt(at * at + au)));
		ssmin = fhmn * c;
	    } else {
		au = fhmx / ga;
		if (au == 0.) {


		    ssmin = fhmn * fhmx / ga;
		} else {
		    as = fhmn / fhmx + 1.0f;
		    at = (fhmx - fhmn) / fhmx;
		    d__1 = as * au;
		    d__2 = at * au;
		    c =     (float) (1 / (Math.sqrt(d__1 * d__1 + 1) + Math.sqrt(d__2 * d__2 + 1)));
		    ssmin = fhmn * c * au;
		    ssmin += ssmin;
		}
	    }
	}

	return(ssmin);
    }
    
    final private void computeScales(boolean forceSVD) {

	if(scales == null)
	    scales = new float[3];

	if ((!forceSVD || ((dirtyBits & SVD_BIT) == 0)) && isAffine()) {
	    if (isCongruent()) {
		if (((dirtyBits & RIGID_BIT) == 0) &&
                    ((type & RIGID) != 0)) {
		    scales[0] = scales[1] = scales[2] = 1;
		    dirtyBits &= ~SCALE_BIT;
		    return;
		}
		scales[0] = scales[1] = scales[2] =
		    (float) Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] +
			      mat[8]*mat[8]);
		dirtyBits &= ~SCALE_BIT;
		return;
	    }
	    if (isOrtho()) {
		scales[0] = (float) Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] +
				      mat[8]*mat[8]);
		scales[1] = (float) Math.sqrt(mat[1]*mat[1] + mat[5]*mat[5] +
				      mat[9]*mat[9]);
		scales[2] = (float) Math.sqrt(mat[2]*mat[2] + mat[6]*mat[6] +
				      mat[10]*mat[10]);
		dirtyBits &= ~SCALE_BIT;
		return;
	    }
	}
	// fall back to use SVD decomposition
	if (rot == null)
	    rot = new float[9];

	compute_svd(this, scales, rot);
	dirtyBits &= ~ROTSCALESVD_DIRTY;
    }
    
    static private void  transpose_mat(float[] in, float[] out) {
	out[0] = in[0];
	out[1] = in[3];
	out[2] = in[6];

	out[3] = in[1];
	out[4] = in[4];
	out[5] = in[7];

	out[6] = in[2];
	out[7] = in[5];
	out[8] = in[8];
    }
    
     static final private float max3( float[] values) {
	if( values[0] > values[1] ) {
	    if( values[0] > values[2] )
		return(values[0]);
	    else
		return(values[2]);
	} else {
	    if( values[1] > values[2] )
		return(values[1]);
	    else
		return(values[2]);
	}
    }
     
     // given that this matrix is affine
    final float affineDeterminant() {
	return mat[0]*(mat[5]*mat[10] - mat[6]*mat[9]) -
	       mat[1]*(mat[4]*mat[10] - mat[6]*mat[8]) +
	       mat[2]*(mat[4]*mat[ 9] - mat[5]*mat[8]);
    }
    
    /**
     * Affine invert routine.  Inverts t1 and places the result in "this".
     */
    final void invertAffine(PMatrix t1) {
	float determinant = t1.affineDeterminant();

	if (determinant == 0.0)
	    throw new UnsupportedOperationException("My ass is not yet inverted with your crap");
            //throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
        
	float s = (t1.mat[0]*t1.mat[0] + t1.mat[1]*t1.mat[1] +
		    t1.mat[2]*t1.mat[2] + t1.mat[3]*t1.mat[3])*
	           (t1.mat[4]*t1.mat[4] + t1.mat[5]*t1.mat[5] +
	            t1.mat[6]*t1.mat[6] + t1.mat[7]*t1.mat[7])*
	           (t1.mat[8]*t1.mat[8] + t1.mat[9]*t1.mat[9] +
		    t1.mat[10]*t1.mat[10] + t1.mat[11]*t1.mat[11]);

	if ((determinant*determinant) < (EPS * s)) {
	    // using invertGeneral is numerically more stable for
	    //this case  see bug 4227733
	    invertGeneral(t1);
	    return;
	}
	s = 1.0f / determinant;

	mat[0] =  (t1.mat[5]*t1.mat[10] - t1.mat[9]*t1.mat[6]) * s;
	mat[1] = -(t1.mat[1]*t1.mat[10] - t1.mat[9]*t1.mat[2]) * s;
	mat[2] =  (t1.mat[1]*t1.mat[6] - t1.mat[5]*t1.mat[2]) * s;
	mat[4] = -(t1.mat[4]*t1.mat[10] - t1.mat[8]*t1.mat[6]) * s;
	mat[5] =  (t1.mat[0]*t1.mat[10] - t1.mat[8]*t1.mat[2]) * s;
	mat[6] = -(t1.mat[0]*t1.mat[6] -  t1.mat[4]*t1.mat[2]) * s;
	mat[8] =  (t1.mat[4]*t1.mat[9] - t1.mat[8]*t1.mat[5]) * s;
	mat[9] = -(t1.mat[0]*t1.mat[9] - t1.mat[8]*t1.mat[1]) * s;
	mat[10]=  (t1.mat[0]*t1.mat[5] - t1.mat[4]*t1.mat[1]) * s;
	mat[3] = -(t1.mat[3] * mat[0] + t1.mat[7] * mat[1] +
		   t1.mat[11] * mat[2]);
	mat[7] = -(t1.mat[3] * mat[4] + t1.mat[7] * mat[5] +
		   t1.mat[11] * mat[6]);
	mat[11]= -(t1.mat[3] * mat[8] + t1.mat[7] * mat[9] +
		   t1.mat[11] * mat[10]);

	mat[12] = mat[13] = mat[14] = 0.0f;
	mat[15] = 1.0f;

	dirtyBits = t1.dirtyBits | ROTSCALESVD_DIRTY | CLASSIFY_BIT | ORTHO_BIT;
	type = t1.type;
    }

    /**
     * Affine invert routine.  Inverts "this" matrix in place.
     */
    final void invertAffine() {
	float determinant = affineDeterminant();

	if (determinant == 0.0)
	    throw new UnsupportedOperationException("My ass is not yet inverted with your crap");
        //throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));

	float s = (mat[0]*mat[0] + mat[1]*mat[1] +
	            mat[2]*mat[2] + mat[3]*mat[3])*
	           (mat[4]*mat[4] + mat[5]*mat[5] +
	            mat[6]*mat[6] + mat[7]*mat[7])*
	           (mat[8]*mat[8] + mat[9]*mat[9] +
		    mat[10]*mat[10] + mat[11]*mat[11]);

	if ((determinant*determinant) < (EPS * s)) {
	    invertGeneral(this);
	    return;
	}
	s = 1.0f / determinant;
	float tmp0 =  (mat[5]*mat[10] - mat[9]*mat[6]) * s;
	float tmp1 = -(mat[1]*mat[10] - mat[9]*mat[2]) * s;
	float tmp2 =  (mat[1]*mat[6] -  mat[5]*mat[2]) * s;
	float tmp4 = -(mat[4]*mat[10] - mat[8]*mat[6]) * s;
	float tmp5 =  (mat[0]*mat[10] - mat[8]*mat[2]) * s;
	float tmp6 = -(mat[0]*mat[6]  - mat[4]*mat[2]) * s;
	float tmp8 =  (mat[4]*mat[9]  - mat[8]*mat[5]) * s;
	float tmp9 = -(mat[0]*mat[9]  - mat[8]*mat[1]) * s;
	float tmp10=  (mat[0]*mat[5]  - mat[4]*mat[1]) * s;
	float tmp3 = -(mat[3] * tmp0 + mat[7] * tmp1 + mat[11] * tmp2);
	float tmp7 = -(mat[3] * tmp4 + mat[7] * tmp5 + mat[11] * tmp6);
	mat[11]= -(mat[3] * tmp8 + mat[7] * tmp9 + mat[11] * tmp10);

	mat[0]=tmp0; mat[1]=tmp1; mat[2]=tmp2; mat[3]=tmp3;
	mat[4]=tmp4; mat[5]=tmp5; mat[6]=tmp6; mat[7]=tmp7;
	mat[8]=tmp8; mat[9]=tmp9; mat[10]=tmp10;
	mat[12] = mat[13] = mat[14] = 0.0f;
	mat[15] = 1.0f;
	dirtyBits |= ROTSCALESVD_DIRTY | CLASSIFY_BIT | ORTHO_BIT;
    }

    /**
     * General invert routine.  Inverts t1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    final void invertGeneral(PMatrix t1) {
	float tmp[] = new float[16];
	int row_perm[] = new int[4];
	int i, r, c;

	// Use LU decomposition and backsubstitution code specifically
	// for floating-point 4x4 matrices.

	// Copy source matrix to tmp
	System.arraycopy(t1.mat, 0, tmp, 0, tmp.length);

	// Calculate LU decomposition: Is the matrix singular?
	if (!luDecomposition(tmp, row_perm)) {
	    // Matrix has no inverse
	    throw new UnsupportedOperationException("My ass is not yet inverted with your crap");
            //throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
	}

	// Perform back substitution on the identity matrix
	// luDecomposition will set rot[] & scales[] for use
	// in luBacksubstituation
	mat[0] = 1.0f;  mat[1] = 0.0f;  mat[2] = 0.0f;  mat[3] = 0.0f;
	mat[4] = 0.0f;  mat[5] = 1.0f;  mat[6] = 0.0f;  mat[7] = 0.0f;
	mat[8] = 0.0f;  mat[9] = 0.0f;  mat[10] = 1.0f; mat[11] = 0.0f;
	mat[12] = 0.0f; mat[13] = 0.0f; mat[14] = 0.0f; mat[15] = 1.0f;
	luBacksubstitution(tmp, row_perm, this.mat);

	type = 0;
	dirtyBits = ALL_DIRTY;
    }

    /**
     * Given a 4x4 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[4]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always float.
     *
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 4x4 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 40-45.
    //
    static boolean luDecomposition(float[] matrix0,
				   int[] row_perm) {

	// Can't re-use this temporary since the method is static.
	float row_scale[] = new float[4];

	// Determine implicit scaling information by looping over rows
	{
	    int i, j;
	    int ptr, rs;
	    float big, temp;

	    ptr = 0;
	    rs = 0;

	    // For each row ...
	    i = 4;
	    while (i-- != 0) {
		big = 0.0f;

		// For each column, find the largest element in the row
		j = 4;
		while (j-- != 0) {
		    temp = matrix0[ptr++];
		    temp = Math.abs(temp);
		    if (temp > big) {
			big = temp;
		    }
		}

		// Is the matrix singular?
		if (big == 0.0) {
		    return false;
		}
		row_scale[rs++] = 1.0f / big;
	    }
	}

	{
	    int j;
	    int mtx;

	    mtx = 0;

	    // For all columns, execute Crout's method
	    for (j = 0; j < 4; j++) {
		int i, imax, k;
		int target, p1, p2;
		float sum, big, temp;

		// Determine elements of upper diagonal matrix U
		for (i = 0; i < j; i++) {
		    target = mtx + (4*i) + j;
		    sum = matrix0[target];
		    k = i;
		    p1 = mtx + (4*i);
		    p2 = mtx + j;
		    while (k-- != 0) {
			sum -= matrix0[p1] * matrix0[p2];
			p1++;
			p2 += 4;
		    }
		    matrix0[target] = sum;
		}

		// Search for largest pivot element and calculate
		// intermediate elements of lower diagonal matrix L.
		big = 0.0f;
		imax = -1;
		for (i = j; i < 4; i++) {
		    target = mtx + (4*i) + j;
		    sum = matrix0[target];
		    k = j;
		    p1 = mtx + (4*i);
		    p2 = mtx + j;
		    while (k-- != 0) {
			sum -= matrix0[p1] * matrix0[p2];
			p1++;
			p2 += 4;
		    }
		    matrix0[target] = sum;

		    // Is this the best pivot so far?
		    if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
			big = temp;
			imax = i;
		    }
		}

		if (imax < 0) {
		    return false;
		}

		// Is a row exchange necessary?
		if (j != imax) {
		    // Yes: exchange rows
		    k = 4;
		    p1 = mtx + (4*imax);
		    p2 = mtx + (4*j);
		    while (k-- != 0) {
			temp = matrix0[p1];
			matrix0[p1++] = matrix0[p2];
			matrix0[p2++] = temp;
		    }

		    // Record change in scale factor
		    row_scale[imax] = row_scale[j];
		}

		// Record row permutation
		row_perm[j] = imax;

		// Is the matrix singular
		if (matrix0[(mtx + (4*j) + j)] == 0.0) {
		    return false;
		}

		// Divide elements of lower diagonal matrix L by pivot
		if (j != (4-1)) {
		    temp = 1.0f / (matrix0[(mtx + (4*j) + j)]);
		    target = mtx + (4*(j+1)) + j;
		    i = 3 - j;
		    while (i-- != 0) {
			matrix0[target] *= temp;
			target += 4;
		    }
		}
	    }
	}

	return true;
    }
    
    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD4x4 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 4x4 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     *
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 44-45.
    //
    static void luBacksubstitution(float[] matrix1,
				   int[] row_perm,
				   float[] matrix2) {

	int i, ii, ip, j, k;
	int rp;
	int cv, rv;

	//	rp = row_perm;
	rp = 0;

	// For each column vector of matrix2 ...
	for (k = 0; k < 4; k++) {
	    //	    cv = &(matrix2[0][k]);
	    cv = k;
	    ii = -1;

	    // Forward substitution
	    for (i = 0; i < 4; i++) {
		float sum;

		ip = row_perm[rp+i];
		sum = matrix2[cv+4*ip];
		matrix2[cv+4*ip] = matrix2[cv+4*i];
		if (ii >= 0) {
		    //		    rv = &(matrix1[i][0]);
		    rv = i*4;
		    for (j = ii; j <= i-1; j++) {
			sum -= matrix1[rv+j] * matrix2[cv+4*j];
		    }
		}
		else if (sum != 0.0) {
		    ii = i;
		}
		matrix2[cv+4*i] = sum;
	    }

	    // Backsubstitution
	    //	    rv = &(matrix1[3][0]);
	    rv = 3*4;
	    matrix2[cv+4*3] /= matrix1[rv+3];

	    rv -= 4;
	    matrix2[cv+4*2] = (matrix2[cv+4*2] -
			    matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+2];

	    rv -= 4;
	    matrix2[cv+4*1] = (matrix2[cv+4*1] -
			    matrix1[rv+2] * matrix2[cv+4*2] -
			    matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+1];

	    rv -= 4;
	    matrix2[cv+4*0] = (matrix2[cv+4*0] -
			    matrix1[rv+1] * matrix2[cv+4*1] -
			    matrix1[rv+2] * matrix2[cv+4*2] -
			    matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+0];
	}
    }
    
    /**
     * Returns the matrix elements of this transform as a string.
     * @return  the matrix elements of this transform
     */
    @Override
    public String toString() {
	// also, print classification?
	return
	    mat[0] + ", " + mat[1] + ", " + mat[2] + ", " + mat[3] + "\n" +
	    mat[4] + ", " + mat[5] + ", " + mat[6] + ", " + mat[7] + "\n" +
	    mat[8] + ", " + mat[9] + ", " + mat[10] + ", " + mat[11] + "\n" +
	    mat[12] + ", " + mat[13] + ", " + mat[14] + ", " + mat[15]
	    + "\n";
    }

    
    public void lerp(PMatrix a, PMatrix b, float fFraction)
    {
        float fOneMinusFraction = 1.0f - fFraction;

        for (int i=0; i<16; i++)
            mat[i] = a.mat[i] * fFraction + b.mat[i] * fOneMinusFraction;
    }

    /**
     * <code>fromAngleAxis</code> sets this matrix4f to the values specified
     * by an angle and an axis of rotation.  This method creates an object, so
     * use fromAngleNormalAxis if your axis is already normalized.
     *
     * @param angle
     *            the angle to rotate (in radians).
     * @param axis
     *            the axis of rotation.
     */
    public void fromAngleAxis(float angle, Vector3f axis)
    {
        Vector3f normAxis = axis.normalize();
        fromAngleNormalAxis(angle, normAxis);
    }

    /**
     * <code>fromAngleNormalAxis</code> sets this matrix4f to the values
     * specified by an angle and a normalized axis of rotation.
     *
     * @param angle
     *            the angle to rotate (in radians).
     * @param axis
     *            the axis of rotation (already normalized).
     */
    public void fromAngleNormalAxis(float angle, Vector3f axis)
    {
        float fCos = (float)Math.cos(angle);
        float fSin = (float)Math.sin(angle);
        float fOneMinusCos = ((float)1.0)-fCos;
        float fX2 = axis.x*axis.x;
        float fY2 = axis.y*axis.y;
        float fZ2 = axis.z*axis.z;
        float fXYM = axis.x*axis.y*fOneMinusCos;
        float fXZM = axis.x*axis.z*fOneMinusCos;
        float fYZM = axis.y*axis.z*fOneMinusCos;
        float fXSin = axis.x*fSin;
        float fYSin = axis.y*fSin;
        float fZSin = axis.z*fSin;

        mat[0] = fX2*fOneMinusCos+fCos;
        mat[1] = fXYM-fZSin;
        mat[2] = fXZM+fYSin;
        mat[3] = 0.0f;

        mat[4] = fXYM+fZSin;
        mat[5] = fY2*fOneMinusCos+fCos;
        mat[6] = fYZM-fXSin;
        mat[7] = 0.0f;

        mat[8] = fXZM-fYSin;
        mat[9] = fYZM+fXSin;
        mat[10] = fZ2*fOneMinusCos+fCos;
        mat[11] = 0.0f;
        
        mat[12] = 0.0f;
        mat[13] = 0.0f;
        mat[14] = 0.0f;
        mat[15] = 1.0f;
    }

        public static void Matrix4fToPMatrix(Matrix4f matrix4f, PMatrix sourceMatrix)
    {
        float []matrixFloats = sourceMatrix.mat;

        matrixFloats[0] = matrix4f.m00;
        matrixFloats[1] = matrix4f.m01;
        matrixFloats[2] = matrix4f.m02;
        matrixFloats[3] = matrix4f.m03;

        matrixFloats[4] = matrix4f.m10;
        matrixFloats[5] = matrix4f.m11;
        matrixFloats[6] = matrix4f.m12;
        matrixFloats[7] = matrix4f.m13;

        matrixFloats[8] = matrix4f.m20;
        matrixFloats[9] = matrix4f.m21;
        matrixFloats[10] = matrix4f.m22;
        matrixFloats[11] = matrix4f.m23;

        matrixFloats[12] = matrix4f.m30;
        matrixFloats[13] = matrix4f.m31;
        matrixFloats[14] = matrix4f.m32;
        matrixFloats[15] = matrix4f.m33;
    }


}
