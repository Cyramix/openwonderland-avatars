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
package imi.scene.polygonmodel.parts;


import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;




public class PGeometryVertex
{
    public Vector3f             m_Position      = new Vector3f();
    public Vector3f             m_Normal        = new Vector3f();
    public Vector3f             m_Tangent       = new Vector3f();
    public ColorRGBA             m_Diffuse       = new ColorRGBA();
    public ColorRGBA             m_Specular      = new ColorRGBA();
    public Vector2f             []m_TexCoords   = new Vector2f[8];



    //  Constructor.
    public PGeometryVertex()
    {
        for (int a=0; a<8; a++)
            m_TexCoords[a] = new Vector2f();
    }
    
     /**
     * Overloaded constructor
     * @param Position
     * @param Normal
     * @param DiffuseColor
     * @param TexCoords Array of eight (8) Vector2f's
     */
    public PGeometryVertex(Vector3f Position, Vector3f Normal, Vector3f DiffuseColor, Vector2f[] TexCoords)
    {
        m_Position = Position;
        m_Normal = Normal;
        m_Diffuse = new ColorRGBA(DiffuseColor.x, DiffuseColor.y, DiffuseColor.z, 1.0f);
        m_TexCoords = TexCoords;
    }
    
     /**
     * Overloaded constructor
     * @param Position
     * @param Normal
     * @param DiffuseColor
     * @param TexCoords Array of eight (8) Vector2f's
     */
    public PGeometryVertex(Vector3f Position, Vector3f Normal, ColorRGBA DiffuseColor, Vector2f[] TexCoords)
    {
        m_Position = Position;
        m_Normal = Normal;
        m_Diffuse = new ColorRGBA(DiffuseColor);
        m_TexCoords = TexCoords;
    }



    //  Clears the Vertice.
    public void clear()
    {
        m_Position.zero();
        m_Normal.zero();
        m_Diffuse = new ColorRGBA();
        m_Specular = new ColorRGBA();
        m_TexCoords[0].zero();
        m_TexCoords[1].zero();
        m_TexCoords[2].zero();
        m_TexCoords[3].zero();
        m_TexCoords[4].zero();
        m_TexCoords[5].zero();
        m_TexCoords[6].zero();
        m_TexCoords[7].zero();
    }
    
    /**
     * Compares the two PVert, true if equal, false otherwise
     * @param obj (RHS The vertex to compare with)
     * @return true on equality, false otherwise
     */
    @Override
    public boolean equals(Object obj)
    {
        if ((obj instanceof PGeometryVertex) == false)
            return false;
        PGeometryVertex RHS = (PGeometryVertex) obj;
        // Try for early outs as much as possible
        if (!m_Position.equals(RHS.m_Position))
            return false;
        if (m_Diffuse.r != RHS.m_Diffuse.r ||
                m_Diffuse.g != RHS.m_Diffuse.g ||
                m_Diffuse.b != RHS.m_Diffuse.b ||
                m_Diffuse.a != RHS.m_Diffuse.a)
            return false;
        if (!m_Normal.equals(RHS.m_Normal))
            return false;
        
        // Do need to compare tex coords!
        // Order was chosen to help early out as much
        // as possible
        if (!m_TexCoords[0].equals(RHS.m_TexCoords[0]))
            return false;
        if (!m_TexCoords[3].equals(RHS.m_TexCoords[3]))
            return false;
        if (!m_TexCoords[5].equals(RHS.m_TexCoords[5]))
            return false;
        if (!m_TexCoords[1].equals(RHS.m_TexCoords[1]))
            return false;
        if (!m_TexCoords[4].equals(RHS.m_TexCoords[4]))
            return false;
        if (!m_TexCoords[2].equals(RHS.m_TexCoords[2]))
            return false;
        if (!m_TexCoords[6].equals(RHS.m_TexCoords[6]))
            return false;
        if (!m_TexCoords[7].equals(RHS.m_TexCoords[7]))
            return false;
        // If we got here, they are equal
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.m_Position != null ? this.m_Position.hashCode() : 0);
        hash = 37 * hash + (this.m_Normal != null ? this.m_Normal.hashCode() : 0);
        hash = 37 * hash + (this.m_Diffuse != null ? this.m_Diffuse.hashCode() : 0);
        hash = 37 * hash + (this.m_TexCoords != null ? this.m_TexCoords.hashCode() : 0);
        return hash;
    }
    
     @Override
    public String toString()
    {
       return new String(
               "Position: " + m_Position.toString() + "\n"
               + "Normal: " + m_Normal.toString() + "\n"
               + "Color: " + m_Diffuse.toString() + "\n"); 
    }

}



