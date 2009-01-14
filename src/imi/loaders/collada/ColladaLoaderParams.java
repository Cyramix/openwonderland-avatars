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
package imi.loaders.collada;

import imi.scene.PMatrix;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 * This class provides convenient storage for the assorted
 * state use by the collada loader
 * @author Ronald E Dahlgren
 */
public class ColladaLoaderParams 
{
    /** Determine whether or not the skeleton should load **/
    private boolean     m_bLoadSkeleton  = false;
    /** True if using a skeleton (if so skinned meshed will be handled for it) **/
    private boolean     m_bUsingSkeleton  = true;
    /** Load geometry? **/
    private boolean     m_bLoadGeometry  = false;
    /** Load animation? **/
    private boolean     m_bLoadAnimation = false;
    /** Print debugging info **/
    private boolean     m_bShowDebugInfo = false;
    /** Check for a serialized version first **/
    private boolean     m_bUseCache      = true;
    /** The maximum number of weights allowed by any skinned items **/
    private int         m_nMaxWeights    = -1;
    /** The 'name' **/
    private String      m_name           = null;
    
    /** The skeleton node to use for loading skinned meshes onto **/
    private SkeletonNode    m_skeletonNode = null;
    
    /** Not used in the loader, you may use it in the asset initializer) **/
    private PMatrix         m_origin        = null;
    
    /**
     * Default constructor, make a new instance
     */
    public ColladaLoaderParams()
    {
        
    }
    
    /**
     * Explicit construction
     * @param loadSkeleton True to load a skeleton from the file
     * @param loadGeometry True to load geometry data from the file
     * @param loadAnimations True to load animations
     * @param useCache True to attempt loading predigested collada files if available
     * @param showDebugInfo True to show debugging output
     * @param maxInfluencesPerVertex Maximum number of influences to track
     * @param name The name
     */
    public ColladaLoaderParams(boolean  loadSkeleton,
                               boolean  loadGeometry,
                               boolean  loadAnimations,
                               boolean  useCache,
                               boolean  showDebugInfo,
                               int      maxInfluencesPerVertex,
                               String   name,
                               SkeletonNode skeleton)
    {
        this.setLoadSkeleton(loadSkeleton);
        this.setLoadGeometry(loadGeometry);
        this.setLoadAnimation(loadAnimations);
        this.setUseCache(useCache);
        this.setShowDebugInfo(showDebugInfo);
        this.setMaxInfluences(maxInfluencesPerVertex);
        this.setName(name);
        this.setSkeletonNode(skeleton);
    }
    /**
     * Explicit construction
     * @param loadSkeleton True to load a skeleton from the file
     * @param loadGeometry True to load geometry data from the file
     * @param loadAnimations True to load animations
     * @param showDebugInfo True to show debugging output
     * @param maxInfluencesPerVertex Maximum number of influences to track
     * @param name The name
     */

    public ColladaLoaderParams(boolean  loadSkeleton,
                               boolean  loadGeometry,
                               boolean  loadAnimations,
                               boolean  showDebugInfo,
                               int      maxInfluencesPerVertex,
                               String   name,
                               SkeletonNode skeleton)
    {
        this(loadSkeleton, loadGeometry, loadAnimations,
             true, showDebugInfo, maxInfluencesPerVertex,
             name, skeleton);
    }
        
    public ColladaLoaderParams(ColladaLoaderParams other)
    {
        this.setLoadAnimation(other.isLoadingAnimations());
        this.setLoadGeometry(other.isLoadingGeometry());
        this.setLoadSkeleton(other.isLoadingSkeleton());
        
    }

    public void setUsingSkeleton(boolean willUse) {
        m_bUsingSkeleton = willUse;
    }
    
    public boolean isUsingSkeleton() {
        return m_bUsingSkeleton;
    }
    
    //////////////////////////////////////////////////////////
    //  The Dungeon! Getters, Setters, and assorted helpers //
    //////////////////////////////////////////////////////////
    
    /**
     * Set whether or not the skeleton should load
     * @param bLoadSkeleton
     */
    public void setLoadSkeleton(boolean bLoadSkeleton)
    {
        m_bLoadSkeleton = bLoadSkeleton;
    }
    
    /**
     * Set whether or not geometry should load
     * @param bLoadGeometry
     */
    public void setLoadGeometry(boolean bLoadGeometry)
    {
        m_bLoadGeometry = bLoadGeometry;
    }
    
    /**
     * Set whether or not the animation should load
     * @param bLoadAnimation
     */
    public void setLoadAnimation(boolean bLoadAnimation)
    {
        m_bLoadAnimation = bLoadAnimation;
    }
    
    /**
     * Set to true to check for pre-digested collada files.
     * @param bUseCache
     */
    private void setUseCache(boolean bUseCache)
    {
        m_bUseCache = bUseCache;
    }
    /**
     * Sets whether or not debugging info should be displayed
     * @param bShowDebugInfo
     */
    public void setShowDebugInfo(boolean bShowDebugInfo)
    {
        m_bShowDebugInfo = bShowDebugInfo;
    }
    
    /**
     * Sets the name assigned in various places of the loader
     * @param name
     */
    public void setName(String name)
    {
        m_name = name;
    }
    
    /**
     * Set the maximum number of influences that are relevant per vertex
     * @param max
     */
    public void setMaxInfluences(int max)
    {
        m_nMaxWeights = max;
    }
    
    /**
     * Sets the skeleton node
     * @param skeleton
     */
    public void setSkeletonNode(SkeletonNode skeleton)
    {
        m_skeletonNode = skeleton;
    }
    
    /**
     * Is the skeleton being loaded?
     * @return
     */
    public boolean isLoadingSkeleton()
    {
        return m_bLoadSkeleton;
    }
    
    /**
     * Is the geometry being loaded?
     * @return
     */
    public boolean isLoadingGeometry()
    {
        return m_bLoadGeometry;
    }
    
    /**
     * Should the binary serialized collada files be used if available?
     * @return
     */
    public boolean isUsingCache()
    {
        return m_bUseCache;
    }
    /**
     * Is animation being loaded?
     * @return
     */
    public boolean isLoadingAnimations()
    {
        return m_bLoadAnimation;
    }
    
    /**
     * Is the debug info being shown?
     * @return
     */
    public boolean isShowingDebugInfo()
    {
        return m_bShowDebugInfo;
    }
    
    /**
     * Gets the current name
     * @return
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * Retrieve the current maximum number of influences per vertex
     * @return
     */
    public int getMaxInfluences()
    {
        return m_nMaxWeights;
    }
    
    /**
     * Retrieve the skeleton node
     * @return
     */
    public SkeletonNode getSkeletonNode()
    {
        return m_skeletonNode;
    }
    
}
