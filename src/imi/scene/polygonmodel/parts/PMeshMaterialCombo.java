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
package imi.scene.polygonmodel.parts;

import com.jme.renderer.ColorRGBA;

/**
 *
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class PMeshMaterialCombo 
{
    // Material references
    private PMeshMaterial   m_Material1     = null;
    private PMeshMaterial   m_Material2     = null;
    // Our own material
    private PMeshMaterial   m_MaterialCombo = new PMeshMaterial(m_DefaultMaterial); // TODO this is shity
    
    static private final PMeshMaterial m_DefaultMaterial = PMeshMaterial.DEFAULT_MATERIAL;
    
    static public enum MaterialCombineMode   {    MCM_UseMat1, MCM_UseMat2, MCM_Combine     }
    
    public MaterialCombineMode  m_Material_Diffuse   = MaterialCombineMode.MCM_UseMat1;
    public MaterialCombineMode  m_Material_Ambient   = MaterialCombineMode.MCM_UseMat1;
    public MaterialCombineMode  m_Material_Emissive  = MaterialCombineMode.MCM_UseMat1;
    public MaterialCombineMode  m_Material_Specular  = MaterialCombineMode.MCM_UseMat1;
    public MaterialCombineMode  m_Material_Shininess = MaterialCombineMode.MCM_UseMat1;
   
    public boolean  m_bUseMat1_Textures                     = true; 
    
    public boolean  m_bUseMat1_Material_VertexColorsMode    = true;
    public boolean  m_bUseMat1_Material_ApplyMaterialMode   = true;
    
    public PMeshMaterialCombo()
    {
        m_Material1 = m_DefaultMaterial;
        apply();
    }

    public PMeshMaterialCombo(PMeshMaterialCombo other)
    {
        m_Material1 = other.getMaterial1();
        m_Material2 = other.getMaterial2();
        
        m_Material_Diffuse   = other.m_Material_Diffuse;
        m_Material_Ambient   = other.m_Material_Ambient;
        m_Material_Emissive  = other.m_Material_Emissive;
        m_Material_Specular  = other.m_Material_Specular;
        m_Material_Shininess = other.m_Material_Shininess;
        
        m_bUseMat1_Textures = other.m_bUseMat1_Textures;
        
        m_bUseMat1_Material_VertexColorsMode  = other.m_bUseMat1_Material_VertexColorsMode;
        m_bUseMat1_Material_ApplyMaterialMode = other.m_bUseMat1_Material_ApplyMaterialMode;
        
        apply();
    }
    
    /**
     * apply() is called within this method
     * @param material1
     * @param material2
     */
    public PMeshMaterialCombo(PMeshMaterial material1, PMeshMaterial material2)
    {
        m_Material1 = material1;
        m_Material2 = material2;
        apply();
    }
    
    /**
     * this will update the material combo.
     * if one of the materials is null then the material combo will apply the values from the other one.
     * if both materials reference valid memory the material combo will be applied with a result of the material combinations
     * according to the set flags (by default material1 values are being used).
     */
    public void apply()
    {
        if (m_Material1 == null && m_Material2 == null)
        {
            m_MaterialCombo.set(m_DefaultMaterial);
            return;
        }
        
        else if (m_Material1 == null && m_Material2 != null)
        {
            m_MaterialCombo.set(m_Material2);
            return;
        }
        else if (m_Material1 != null && m_Material2 == null)
        {
            m_MaterialCombo.set(m_Material1);
            return;
        }
        
        // Combination time
        
        // TODO allocate m_MaterialCombo memory here instead of doing it on construction time
        // TODO combine shaders... somehow (choose which material to follow)
        
        switch(m_Material_Diffuse)
        {
            case MCM_UseMat1:
            {
                m_MaterialCombo.setDiffuse(m_Material1.getDiffuse());
                break;
            }
            case MCM_UseMat2:
            {
                m_MaterialCombo.setDiffuse(m_Material2.getDiffuse());
                break;
            }
            case MCM_Combine:
            {
                ColorRGBA color = new ColorRGBA(m_Material1.getDiffuse());
                m_MaterialCombo.setDiffuse(color.mult(m_Material2.getDiffuse()));
                break;
            }
        }
            
        switch(m_Material_Ambient)
        {
            case MCM_UseMat1:
            {
                m_MaterialCombo.setAmbient(m_Material1.getAmbient());
                break;
            }
            case MCM_UseMat2:
            {
                m_MaterialCombo.setAmbient(m_Material2.getAmbient());
                break;
            }
            case MCM_Combine:
            {
                ColorRGBA color = new ColorRGBA(m_Material1.getAmbient());
                m_MaterialCombo.setAmbient(color.mult(m_Material2.getAmbient()));
                break;
            }
        }
            
        switch(m_Material_Emissive)
        {
            case MCM_UseMat1:
            {
                m_MaterialCombo.setEmissive(m_Material1.getEmissive());
                break;
            }
            case MCM_UseMat2:
            {
                m_MaterialCombo.setEmissive(m_Material2.getEmissive());
                break;
            }
            case MCM_Combine:
            {
                ColorRGBA color = new ColorRGBA(m_Material1.getEmissive());
                m_MaterialCombo.setEmissive(color.mult(m_Material2.getEmissive()));
                break;
            }
        }
          
        switch(m_Material_Specular)
        {
            case MCM_UseMat1:
            {
                m_MaterialCombo.setSpecular(m_Material1.getSpecular());
                break;
            }
            case MCM_UseMat2:
            {
                m_MaterialCombo.setSpecular(m_Material2.getSpecular());
                break;
            }
            case MCM_Combine:
            {
                ColorRGBA color = new ColorRGBA(m_Material1.getSpecular());
                m_MaterialCombo.setSpecular(color.mult(m_Material2.getSpecular()));
                break;
            }
        }
        
        switch(m_Material_Shininess)
        {
            case MCM_UseMat1:
            {
                m_MaterialCombo.setShininess(m_Material1.getShininess());
                break;
            }
            case MCM_UseMat2:
            {
                m_MaterialCombo.setShininess(m_Material2.getShininess());
                break;
            }
            case MCM_Combine:
            {
                float avg   =  m_Material1.getShininess();
                avg         += m_Material2.getShininess();
                avg         *= 0.5f;
                m_MaterialCombo.setShininess((int)avg);
                break;
            }
        }
            
        if (m_bUseMat1_Textures)
            m_MaterialCombo.setTextures(m_Material1.getTextures());
        else
            m_MaterialCombo.setTextures(m_Material2.getTextures());
        
        if (m_bUseMat1_Material_VertexColorsMode)
            m_MaterialCombo.setColorMaterial(m_Material1.getColorMaterial());
        else
            m_MaterialCombo.setColorMaterial(m_Material2.getColorMaterial());
        
        if (m_bUseMat1_Material_ApplyMaterialMode)
            m_MaterialCombo.setMaterialFace(m_Material1.getMaterialFace());
        else
            m_MaterialCombo.setMaterialFace(m_Material2.getMaterialFace());
    }

    public PMeshMaterial getMaterial1() 
    {
        return m_Material1;
    }

    public void setMaterial(PMeshMaterial Material)
    {
        m_Material1 = Material;
        m_Material2 = null;
        apply(); // early exit so its cheap
    }
    
    public void setMaterial1(PMeshMaterial Material1) 
    {
        m_Material1 = Material1;
    }

    public PMeshMaterial getMaterial2() 
    {
        return m_Material2;
    }

    public void setMaterial2(PMeshMaterial Material2) 
    {
        m_Material2 = Material2;
    }

    /**
     * Returns a reference
     * @return
     */
    public PMeshMaterial getMaterial() 
    {
        return m_MaterialCombo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final PMeshMaterialCombo other = (PMeshMaterialCombo) obj;
        
        if (this.m_Material1 != other.m_Material1 && (this.m_Material1 == null || !this.m_Material1.equals(other.m_Material1))) {
            return false;
        }
        if (this.m_Material2 != other.m_Material2 && (this.m_Material2 == null || !this.m_Material2.equals(other.m_Material2))) {
            return false;
        }
        if (!this.m_Material_Diffuse.equals(other.m_Material_Diffuse)) {
            return false;
        }
        if (!this.m_Material_Ambient.equals(other.m_Material_Ambient)) {
            return false;
        }
        if (!this.m_Material_Emissive.equals(other.m_Material_Emissive)) {
            return false;
        }
        if (!this.m_Material_Specular.equals(other.m_Material_Specular)) {
            return false;
        }
        if (this.m_Material_Shininess != other.m_Material_Shininess) {
            return false;
        }
        if (this.m_bUseMat1_Textures != other.m_bUseMat1_Textures) {
            return false;
        }
        if (this.m_bUseMat1_Material_VertexColorsMode != other.m_bUseMat1_Material_VertexColorsMode) {
            return false;
        }
        if (this.m_bUseMat1_Material_ApplyMaterialMode != other.m_bUseMat1_Material_ApplyMaterialMode) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.m_Material1 != null ? this.m_Material1.hashCode() : 0);
        hash = 71 * hash + (this.m_Material2 != null ? this.m_Material2.hashCode() : 0);
        hash = 71 * hash + (this.m_Material_Diffuse != null ? this.m_Material_Diffuse.hashCode() : 0);
        hash = 71 * hash + (this.m_Material_Ambient != null ? this.m_Material_Ambient.hashCode() : 0);
        hash = 71 * hash + (this.m_Material_Emissive != null ? this.m_Material_Emissive.hashCode() : 0);
        hash = 71 * hash + (this.m_Material_Specular != null ? this.m_Material_Specular.hashCode() : 0);
        hash = 71 * hash + (this.m_Material_Shininess != null ? this.m_Material_Shininess.hashCode() : 0);
        hash = 71 * hash + (this.m_bUseMat1_Textures ? 1 : 0);
        hash = 71 * hash + (this.m_bUseMat1_Material_VertexColorsMode ? 1 : 0);
        hash = 71 * hash + (this.m_bUseMat1_Material_ApplyMaterialMode ? 1 : 0);
        return hash;
    }
    
    
    
}
