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
package imi.scene;

import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 *  A PTransform defines a world and local transforms
 * @author Lou Hayt
 * @author Ron Daulgren
 */
public class PTransform implements Serializable
{
    private transient PMatrix     m_local = new PMatrix();
    private transient PMatrix     m_world = new PMatrix();
    
    /** if false the world matrix needs to be recalculated */
    protected transient boolean   m_bDirtyWorldMat      = true;

    /**
     * Empty constructor
     * Sets the identity matrix for local and world transformations.
     */
    public PTransform() 
    {
    }
    
    /**
     * Constructor
     * @param rotation
     * @param translation
     * @param scale
     */
    public PTransform(Vector3f rotation, Vector3f translation, Vector3f scale) 
    {
        m_local.set(rotation, translation, scale);
        m_world.set(m_local);
    }
    
    /**
     * Constructor
     * @param rotation
     * @param translation
     * @param scale
     */
    public PTransform(Vector3f rotation, Vector3f translation, float scale) 
    {
        m_local.set(rotation, translation, scale);
        m_world.set(m_local);
    }
    
    /**
     * Constructor
     * @param rotation
     * @param translation
     * @param scale
     */
    public PTransform(Quaternion rotation, Vector3f translation, Vector3f scale) 
    {
        m_local.set(rotation, translation, scale);
        m_world.set(m_local);
    }
    
    /**
     * Constructor
     * @param rotation
     * @param translation
     * @param scale
     */
    public PTransform(Quaternion rotation, Vector3f translation, float scale) 
    {
        m_local.set(rotation, translation, scale);
        m_world.set(m_local);
    }
    
    /**
     * Constructor
     * @param local
     */
    public PTransform(PMatrix local) 
    {
        if (local == null)
            local = new PMatrix();
        m_local.set(local);
        m_world.set(m_local);
    }
    
    /**
     * Constructor
     * @param other
     */
    public PTransform(PTransform other)
    {
        this(other.getLocalMatrix(false));
    }
    
    /**
     * Constructor
     * @param local
     */
    public PTransform(Matrix4f local) 
    {
        m_local.set(local);
        m_world.set(m_local);
    }
    
    /**
     * Calculate the world matrix.
     * This will set m_bDirtyWorldMat to false
     * @param parentWorld
     */
    public void buildWorldMatrix(PMatrix parentWorld)
    {
        m_world.fastMul(parentWorld, m_local);
        m_bDirtyWorldMat = false;
    }

    /**
     * Returns true of the world matrix is dirty
     * which means this transform changed since it was last calculated
     * @return m_bDirtyWorldMat (boolean)
     */
    public boolean isDirtyWorldMat() 
    {
        return m_bDirtyWorldMat;
    }

    /**
     * This boolean deterimins if the world matrix
     * needs to be calculated again.
     * @param bDirtyWorldMat
     */
    public void setDirtyWorldMat(boolean bDirtyWorldMat) 
    {
        m_bDirtyWorldMat = bDirtyWorldMat;
    }

    /**
     * This exposes the rich functionality of PMatrix.
     * If you specify that you will not change the matrix
     * but end up changing it; remember to call setDirtyWorldMat().
     * This prevents unnecessary calculations.
     * @param willBeModified
     * @return m_local (PMatrix)
     */
    public PMatrix getLocalMatrix(boolean willBeModified) 
    {
        if (willBeModified)
            m_bDirtyWorldMat = willBeModified;
        
        return m_local;
    }

    public void setLocalMatrix(float[] matrixFloats) {
        m_local.set(matrixFloats);
    }

    /**
     * Set the local matrix of this transformation.
     * @param local
     */
    public void setLocalMatrix(PMatrix local) 
    {
        m_local.set(local);
        m_bDirtyWorldMat = true;
    }

    /**
     * This exposes the rich functionality of PMatrix.
     * If you specify that you will not change the matrix
     * but end up changing it; remember to call setDirtyWorldMat().
     * This prevents unnecessary calculations.
     * @param willBeModified
     * @return m_world (PMatrix)
     */
    public PMatrix getWorldMatrix(boolean willBeModified) 
    {
        if (willBeModified)
            m_bDirtyWorldMat = willBeModified;
        
        return m_world;
    }
    
    /**
     * Get the world matrix without translation data.
     * @return matrix (Matrix3f)
     */
    public Matrix3f getNormalWorldMatrix()
    {
        Matrix3f matrix = new Matrix3f();
        m_world.getMatrixRotationScale(matrix);
        return matrix;
    }

    public void setLocalMatrix(Matrix3f rotation, Vector3f translationVector) {
        m_local.set(rotation, translationVector, 1.0f);
    }

    public void setLocalMatrix(Quaternion rotation, Vector3f translationVector) {
        m_local.set(rotation, translationVector, 1.0f);
    }

    /**
     * Set the values of this transform to the one provided
     * @param transform
     */
    void set(PTransform transform)
    {
        m_local.set(transform.getLocalMatrix(false));
        m_world.set(transform.getWorldMatrix(false));
    }

    /****************************
     * SERIALIZATION ASSISTANCE *
     ****************************/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        // just write the top three rows of the local matrix
        float[] matrix = new float[16];
        m_local.getFloatArray(matrix);
        for (int i = 0; i < 12; ++i)
            out.writeFloat(matrix[i]);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        float[] matrix = new float[16];
        for (int i = 0; i < 12; ++i)
            matrix[i] = in.readFloat();

        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;

        m_local = new PMatrix(matrix);
        m_world = new PMatrix();
        m_bDirtyWorldMat = true;
    }
}
