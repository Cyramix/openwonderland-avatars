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
package imi.scene.polygonmodel;

import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;

/**
 * Self contained vertex (no external indexing is required to access the data)
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class PGeometryVertex
{
    final Vector3f position   = new Vector3f();
    final Vector3f normal     = new Vector3f();
    final Vector3f tangent    = new Vector3f();
    final ColorRGBA diffuse   = new ColorRGBA(ColorRGBA.pink);
    final ColorRGBA specular  = new ColorRGBA(ColorRGBA.pink);

    private final Vector2f[]  texCoords     = new Vector2f[8];

    /**
     * Initialize the vertex with default values
     */
    public PGeometryVertex()
    {
        for (int a=0; a<8; a++)
            texCoords[a] = new Vector2f();
    }
    
    /**
     * Initializes this vertex to match the specified vertex.
     * @param other A non-null vertex to copy.
     * @throws NullPointerException If other == null
     */
    public PGeometryVertex(PGeometryVertex other)
    {
        set(other);
    }

    /**
     * Set the internal state to match that of the specified vert.
     * @param other A non-null vertex to copy.
     * @throws NullPointerException If other == null
     */
    public void set(PGeometryVertex other) {
        position.set(other.position);
        normal.set(other.normal);
        tangent.set(other.tangent);
        diffuse.set(other.diffuse);
        specular.set(other.specular);
        for (int a=0; a<8; a++)
            texCoords[a] = new Vector2f(other.texCoords[a]);
    }

    /**
     * Set the tex coords to match the provided values
     * @param values
     * @throws NullPointerException If values == null
     */
    public void setTexCoords(Vector2f[] values) {
        int loopCount = Math.min(values.length, 8);
        for (int i = 0; i < loopCount; i++)
            texCoords[i].set(values[i]);
    }

    /**
     * Set the tex coord at the provided index to the specified value.
     * @param index The tex coordinate to set, must be between zero and 7 (inclusive)
     * @param value A vector to set
     * @throws ArrayIndexOutOfBoundsException If the provided index is invalid
     */
    public void setTexCoord(int index, Vector2f value) {
        texCoords[index].set(value);
    }
    
    /**
     * Clear everything, set zero values and pink for colors (debug value)
     */
    public void clear()
    {
        position.zero();
        normal.zero();
        diffuse.set(ColorRGBA.pink);
        diffuse.set(ColorRGBA.pink);
        texCoords[0].zero();
        texCoords[1].zero();
        texCoords[2].zero();
        texCoords[3].zero();
        texCoords[4].zero();
        texCoords[5].zero();
        texCoords[6].zero();
        texCoords[7].zero();
    }

    /**
     * Retrieve the current value of the position vector.
     * @param vOut A non-null storage object
     * @throws NullPointerException If vOut == null
     */
    public void getPosition(Vector3f vOut) {
        vOut.set(position);
    }
    /**
     * Retrieve the current value of the normal vector.
     * @param vOut A non-null storage object
     * @throws NullPointerException If vOut == null
     */
    public void getNormal(Vector3f vOut) {
        vOut.set(normal);
    }
    /**
     * Retrieve the current value of the tangent vector.
     * @param vOut A non-null storage object
     * @throws NullPointerException If vOut == null
     */
    public void getTangent(Vector3f vOut) {
        vOut.set(tangent);
    }

    /**
     * Set the value of the tangent for this vertex
     * @param tangent A tangent vector (should be normalized)
     */
    public void setTangent(Vector3f tangent) {
        this.tangent.set(tangent);
    }
    /**
     * Retrieve the current value of the diffuse color.
     * @param out A non-null storage object
     * @throws NullPointerException If out == null
     */
    public void getDiffuseColor(ColorRGBA out) {
        out.set(diffuse);
    }

    /**
     * Retrieve the current value of the specular color.
     * @param out A non-null storage object
     * @throws NullPointerException If out == null
     */
    public void getSpecularColor(ColorRGBA out) {
        out.set(specular);
    }

    /**
     * Retrieve the specified texture coordinate vector.
     * @param index The texture unit to check, must be between 0 and 7 (inclusive)
     * @param out A non-null storage object
     * @throws ArrayIndexOutOfBoundsException If index less than 0 or greater than 7
     * @throws NullPointerException If out == null
     */
    public void getTexCoord(int index, Vector2f out) {
        out.set(texCoords[index]);
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
        if (obj == this) // same instance?
            return true;
        PGeometryVertex RHS = (PGeometryVertex) obj;
        // Try for early outs as much as possible
        if (!position.equals(RHS.position))
            return false;
        if (!normal.equals(RHS.normal))
            return false;
        if (diffuse.r != RHS.diffuse.r ||
            diffuse.g != RHS.diffuse.g ||
            diffuse.b != RHS.diffuse.b ||
            diffuse.a != RHS.diffuse.a)
            return false;
        
        // Do need to compare tex coords!
        // Order was chosen to help early out as much
        // as possible
        if (!texCoords[0].equals(RHS.texCoords[0]))
            return false;
        if (!texCoords[3].equals(RHS.texCoords[3]))
            return false;
        if (!texCoords[5].equals(RHS.texCoords[5]))
            return false;
        if (!texCoords[1].equals(RHS.texCoords[1]))
            return false;
        if (!texCoords[4].equals(RHS.texCoords[4]))
            return false;
        if (!texCoords[2].equals(RHS.texCoords[2]))
            return false;
        if (!texCoords[6].equals(RHS.texCoords[6]))
            return false;
        if (!texCoords[7].equals(RHS.texCoords[7]))
            return false;
        // If we got here, they are equal
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.position != null ? this.position.hashCode() : 0);
        hash = 37 * hash + (this.normal != null ? this.normal.hashCode() : 0);
        hash = 37 * hash + (this.diffuse != null ? this.diffuse.hashCode() : 0);
        for (int i = 0; i < texCoords.length; i++)
            hash = 37 * hash + (this.texCoords[i] != null ? this.texCoords[i].hashCode() : 0);
        return hash;
    }

     @Override
    public String toString()
    {
       return "Position: " + position.toString() + "\n"
               + "Normal: " + normal.toString() + "\n"
               + "Color: " + diffuse.toString() + "\n";
    }

}



