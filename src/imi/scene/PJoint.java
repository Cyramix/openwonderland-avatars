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

import imi.scene.PNode;
import imi.scene.PTransform;
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
public class PJoint extends PNode 
{
    /** Mesh space's origin is the location of the mesh,
     *  this is a "world matrix" in "mesh space". */
    private PMatrix         m_meshSpace     = new PMatrix();
    
    /** Indicated whenever this joint is selected */
    private boolean         m_bSelected     = false;
    
    /** The local modifier matirx is used for effects that do not
     * cascade down the hierarchy, for e.g. body fat */
    private PMatrix         m_localModifier = new PMatrix();
    
    /**
     * The skeleton modifier is used for enabling compatability
     * with meshes that require re-positioning of skeleton nodes
     * which must cascade down the hierarchy during the flatening process
     * (such as heads)
     */
    private PMatrix         m_skeletonModifier = null;
	
    /**
     * Empty constructor, for low level coding.
     * 
     * Warnining : Do not use this constructor without insuring memory allocation
     * for the transform member variable.
     */
    public PJoint()
    {
        super();
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
     * Returns a tansform matrix whos origin
     * is the location of the mesh.
     * @return "mesh space" "world matrix" 
     */
    public PMatrix getMeshSpace() {
        return m_meshSpace;
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

    /**
     * The skeleton modifier is used for enabling compatability
     * with meshes that require re-positioning of skeleton nodes
     * which must cascade down the hierarchy during the flatening process
     * (such as heads)
     * @return
     */
    public PMatrix getSkeletonModifier() {
        return m_skeletonModifier;
    }

    /**
     * The skeleton modifier is used for enabling compatability
     * with meshes that require re-positioning of skeleton nodes
     * which must cascade down the hierarchy during the flatening process
     * (such as heads)
     * @param skeletonModifier
     */
    public void setSkeletonModifier(PMatrix skeletonModifier) {
        this.m_skeletonModifier = skeletonModifier;
    }
    
}
