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


/**
 *  A PTransform defines a world and local transforms
 * @author Lou Hayt
 * @author Ron Daulgren
 */
public class PTransform 
{
    private PMatrix     m_local               = new PMatrix();
    private PMatrix     m_world               = null;
    
    /** if false the world matrix needs to be recalculated */
    protected boolean   m_bDirtyWorldMat      = true;

    /**
     * Empty constructor
     * Sets the identity matrix for local and world transformations.
     */
    public PTransform() 
    {
        m_world = new PMatrix(m_local);
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
        m_world = new PMatrix(m_local);
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
        m_world = new PMatrix(m_local);
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
        m_world = new PMatrix(m_local);
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
        m_world = new PMatrix(m_local);
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
        m_world = new PMatrix(m_local);
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
        m_world = new PMatrix(m_local);
    }
    
    /**
     * Calculate the world matrix.
     * This will set m_bDirtyWorldMat to false
     * @param parentWorld
     */
    public void buildWorldMatrix(PMatrix parentWorld)
    {
        m_world.mul(parentWorld, m_local);
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

    /**
     * Set the local matrix of this transformation.
     * @param local
     */
    public void setLocalMatrix(PMatrix local) 
    {
        m_local          = local;
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
    
}
