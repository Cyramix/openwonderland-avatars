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
package imi.loaders;

import imi.scene.SkeletonNode;
import javolution.lang.Immutable;

/**
 * This class provides convenient storage for the assorted
 * state use by the collada loader
 * @author Ronald E Dahlgren
 */
public class ColladaLoaderParams implements Immutable
{
    // All data members are package access to provide easy access to the loaders
    /** Determine whether or not the skeleton should load **/
    final boolean     m_bLoadSkeleton;
    /** Load geometry? **/
    final boolean     m_bLoadGeometry;
    /** Load animation? **/
    final boolean     m_bLoadAnimation;
    /** Print debugging info **/
    final boolean     m_bShowDebugInfo;
    /** The maximum number of weights allowed by any skinned items **/
    final int         m_nMaxWeights;
    /** The 'name' **/
    final String      m_name;
    /** The skeleton node to use for loading skinned meshes onto **/
    final SkeletonNode  m_skeletonNode;
    /** Used to control texture persuit **/
    final boolean       m_loadTextures;
    
    /**
     * Builds collada loader parameters
     */
    public static class Builder
    {
        /** Determine whether or not the skeleton should load **/
        private boolean     m_bLoadSkeleton  = false;
        /** Load geometry? **/
        private boolean     m_bLoadGeometry  = false;
        /** Load animation? **/
        private boolean     m_bLoadAnimation = false;
        /** Print debugging info **/
        private boolean     m_bShowDebugInfo = false;
        /** The maximum number of weights allowed by any skinned items **/
        private int         m_nMaxWeights    = 4;
        /** The 'name' **/
        private String      m_name  = "NamelessCollada";
        /** The skeleton node to use for loading skinned meshes onto **/
        private SkeletonNode    m_skeletonNode = null;
        /** Used to control texture persuit **/
        private boolean       loadTextures = true;

        /**
         * Get a builder!
         */
        public Builder() {}

        public Builder setLoadAnimation(boolean loadAnimation) {
            this.m_bLoadAnimation = loadAnimation;
            return this;
        }

        public Builder setLoadGeometry(boolean loadGeometry) {
            this.m_bLoadGeometry = loadGeometry;
            return this;
        }

        public Builder setLoadSkeleton(boolean loadSkeleton) {
            this.m_bLoadSkeleton = loadSkeleton;
            return this;
        }

        public Builder setShowDebugInfo(boolean showDebugInfo) {
            this.m_bShowDebugInfo = showDebugInfo;
            return this;
        }

        public Builder setLoadTextures(boolean loadTextures) {
            this.loadTextures = loadTextures;
            return this;
        }

        public Builder setMaxWeights(int maxWeights) {
            this.m_nMaxWeights = maxWeights;
            return this;
        }

        public Builder setName(String name) {
            this.m_name = name;
            return this;
        }

        public Builder setSkeletonNode(SkeletonNode skeletonNode) {
            this.m_skeletonNode = skeletonNode;
            return this;
        }

        /**
         * Construct a new ColladaLoaderParams instance
         * @return New loader params
         */
        public ColladaLoaderParams build() {
            return new ColladaLoaderParams(this);
        }
    }

    /**
     * Default constructor, make a new instance
     */
    private ColladaLoaderParams(Builder builder)
    {
        m_bLoadSkeleton = builder.m_bLoadSkeleton;
        m_bLoadGeometry = builder.m_bLoadGeometry;
        m_bLoadAnimation = builder.m_bLoadAnimation;
        m_bShowDebugInfo = builder.m_bShowDebugInfo;
        m_nMaxWeights = builder.m_nMaxWeights;
        m_name = builder.m_name;
        m_skeletonNode = builder.m_skeletonNode;
        m_loadTextures = builder.loadTextures;
    }


}
