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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This <code>PNode</code> is used for articulated hierarchies 
 * that can potentially animate.
 * 
 * You can safely assume that the transform member variable of this class 
 * is never null.
 * 
 * @author Lou Hayt
 */
public class PJoint extends PNode implements Serializable
{
    /** Mesh space's origin is the location of the mesh,
     *  this is a "world matrix" in "mesh space". */
    private PMatrix         m_meshSpace     = new PMatrix();
    
    /** Indicated whenever this joint is selected */
    private boolean         m_bSelected     = false;
    
    /** The local modifier matirx is used for effects that do not
     * cascade down the hierarchy, for e.g. body fat */
    private final PMatrix   m_localModifier = new PMatrix();
	
    /**
     * Empty constructor, for low level coding.
     */
    public PJoint()
    {
        super();
        setTransform(new PTransform());
    }

    /**
     * If null is passed in a default transform will be assigned.
     * 
     * @param transform -   can be null
     */
    public PJoint(PTransform transform)
    {
        super(transform);
        if (getTransform() == null)
            setTransform(new PTransform());
    }

    /**
     * If null is passed in a default transform will be assigned.
     * 
     * @param name      -   can be null
     * @param transform -   can be null
     */
    public PJoint(String name, PTransform transform) 
    {
        super(name, transform);
        if (getTransform() == null)
            setTransform(new PTransform());
    }

    /**
     *  If null is passed in a default transform will be assigned.
     * 
     * @param name      -   can be null
     * @param parent    -   can be null
     * @param children  -   can be null
     * @param transform -   can be null
     */
    public PJoint(String name, PNode parent, ArrayList<PNode> children, PTransform transform) 
    {
        super(name, parent, children, transform);
        if (getTransform() == null)
            setTransform(new PTransform());
    }

    /**
     * Create a copy of the provided PJoint
     * @param other
     */
    public PJoint(PJoint other)
    {
        super(other);
        // Copy mesh space and local modifier
        this.m_meshSpace.set(other.m_meshSpace);
        this.m_localModifier.set(other.m_localModifier);
    }
    
    /**
     * Selected joints will be visualized in the internal renderer (PRenderer)
     * with yellow spheres.
     * 
     * Selection also has context with the GUI for manipulation of the joint
     */
    public void select()
    {
        m_bSelected = true;
    }
    
    /**
     * Selected joints will be visualized in the internal renderer (PRenderer)
     * with yellow spheres.
     * 
     * Selection also has context with the GUI for manipulation of the joint
     */
    public void unselect()
    {
        m_bSelected = false;
    }
    
    /**
     * Selected joints will be visualized in the internal renderer (PRenderer)
     * with yellow spheres.
     * 
     * Selection also has context with the GUI for manipulation of the joint
     */
    public boolean isSelected()
    {
        return m_bSelected;
    }

    /**
     * Returns a transform matrix whos origin
     * is the location of the mesh.
     * @return "mesh space" "world matrix"
     */
    public PMatrix getMeshSpace() {
        return m_meshSpace;
    }

    /**
     * Returns a tansform matrix whos origin
     * is the location of the mesh.
     * @param mOut Receive the value of the transform
     * @return "mesh space" "world matrix"
     */
    public void getMeshSpace(PMatrix mOut) {
        mOut.set(m_meshSpace);
    }

    /**
     * Sets the transform for this joint
     * relevant to the location of the mesh.
     * @param meshSpace
     */
    public void setMeshSpace(PMatrix meshSpace) {
        m_meshSpace.set(meshSpace);
    }
    
    /**
     * The local modifier matirx is used for effects that do not
     * cascade down the hierarchy, for e.g. body fat
     * @return m_localModifier (PMatrix)
     */
    public PMatrix getLocalModifierMatrix()
    {
        return m_localModifier;
    }
    
    /**
     * The local modifier matirx is used for effects that do not
     * cascade down the hierarchy, for e.g. body fat
     * @param mat
     */
    public void setLocalModifierMatrix(PMatrix mat)
    {
        m_localModifier.set(mat);
    }
}
