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

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.scene.polygonmodel.PGeometryVertex;
import java.util.List;
import javolution.util.FastTable;

/**
 * This class is used to simplify the collecting of PGeometryVertex objects
 * using duplicate checking.
 * @author Ronald E Dahlgren
 */
public class PGeometryVertexBuffer {
    /** Collection of verts **/
    private final List<PGeometryVertex> verts = new FastTable<PGeometryVertex>();

    public PGeometryVertexBuffer() {}

    /**
     * Attempts to add a vertex to the master list.
     * If a duplicate vertex is found, its index is
     * returned, otherwise the vert is added to the
     * end of the list and the new index is returned.
     * @param newVert The non-null vertex to add
     * @return The index of the vertex within the master list
     * @throws IllegalArgumentException If (newVert == null)
     */
    public int addVertex(PGeometryVertex newVert)
    {
        if (newVert == null)
            throw new IllegalArgumentException("Null vertex provided");
        int result = verts.indexOf(newVert);
        if (result == -1) {
            verts.add(newVert);
            result = verts.size() - 1;
        }
        return result;
    }
    
    /**
     * Determine the number of vertices
     * Same as <code>count</code>
     * @return The number of vertices contained
     */
    public int size()
    {
        return verts.size();
    }

    /**
     * Clears out the master list
     */
    public void clear()
    {
        verts.clear();
    }

    /**
     * Retrieve a collection of all the position values contained by verts within
     * this buffer.
     * @return An array of position vectors
     */
    public Vector3f[] getPositionArray()
    {
        Vector3f[] result = new Vector3f[verts.size()];
        for (int i = 0; i < verts.size(); ++i) {
            Vector3f newVec = new Vector3f();
            verts.get(i).getPosition(newVec);
            result[i] = newVec;
        }
        return result;
    }


    /**
     * Retrieve a collection of all the normal values contained by verts within
     * this buffer.
     * @return An array of position vectors
     */
    public Vector3f[] getNormalArray()
    {
        Vector3f[] result = new Vector3f[verts.size()];
        for (int i = 0; i < verts.size(); ++i) {
            Vector3f newVec = new Vector3f();
            verts.get(i).getNormal(newVec);
            result[i] = newVec;
        }
        return result;
    }


    /**
     * Retrieve a collection of all the tangent values contained by verts within
     * this buffer.
     * @return An array of position vectors
     */
    public Vector3f[] getTangentArray()
    {
        Vector3f[] result = new Vector3f[verts.size()];
        for (int i = 0; i < verts.size(); ++i) {
            Vector3f newVec = new Vector3f();
            verts.get(i).getTangent(newVec);
            result[i] = newVec;
        }
        return result;
    }

    /**
     * Retrieve a collection of all the diffuse colors contained by verts within
     * this collection.
     * @return Collection of colors!
     */
    public ColorRGBA[] getDiffuseColorArray()
    {
        ColorRGBA[] result = new ColorRGBA[verts.size()];
        for (int i = 0; i < verts.size(); ++i)
        {
            ColorRGBA newColor = new ColorRGBA();
            verts.get(i).getDiffuseColor(newColor);
            result[i] = newColor;
        }
        return result;
    }
    
    /**
     * Retrieves the texture coordinates for the specified
     * texture target.
     * @param nTextureTarget The texture target to retieve
     * @return The array
     * @throws ArrayIndexOutOfBoundsException If nTextureTarget less than 0 or greater than 7
     */
    public Vector2f[] getTextureCoordinateArray(int nTextureTarget)
    {
        Vector2f[] result = new Vector2f[verts.size()];
        for (int i = 0; i < verts.size(); ++i) {
            Vector2f newVec = new Vector2f();
            verts.get(i).getTexCoord(nTextureTarget, newVec);
            result[i] = newVec;
        }
        return result;
    }
}
