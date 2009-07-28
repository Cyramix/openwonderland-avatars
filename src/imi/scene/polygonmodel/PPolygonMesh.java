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

import com.jme.bounding.BoundingSphere;
import imi.utils.MathUtils;
import imi.scene.PSphere;
import imi.scene.PCube;
import imi.scene.PNode;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TriMesh;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.repository.SharedAsset;
import imi.scene.PTransform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javolution.util.FastList;
import javolution.util.FastTable;

/**
 * The PPolygonMesh class is <code>PNode</code> derived type that represents
 * polygonal geometry. Internally, a jME TriMesh is kept for passing to the
 * <code>JScene</code> during the draw process.
 *
 * @author Chris Nagle
 * @author Loud Hayt
 * @author Ronald E Dahlgen
 */
public class PPolygonMesh extends PNode implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private TriMesh   m_Geometry          = new TriMesh("geometry"); // jME geometry
    
    /** A reference to the shared asset for this geometry, if null then the geometry was made procedurally */
    private transient SharedAsset                     m_SharedAsset       = null;
    
    /**
     * <code>PMeshMaterial</code> is a reference to the Material common to all <code>PPolygon</code>s in this mesh.
     */
    private PMeshMaterial   material         = null;

    //  Eventually we will choose one shape to serve as the bounding volume.
    private transient PCube     m_BoundingCube      = new PCube();
    private transient PSphere   m_BoundingSphere    = new PSphere();

    // Used to toggle smooth normal mode
    private boolean m_bSmoothNormals    = false;

    // The list of polygons for this mesh. The polygons in this list have
    // vertices that index into the following arrays
    protected FastTable<PPolygon>           m_Polygons          = new FastTable<PPolygon>();

    // The data members below are the master lists of the components
    // referenced by the vertices of the PPolygons contained herein.
    protected FastTable<PPolygonPosition>     m_Positions         = new FastTable<PPolygonPosition>();
    protected FastTable<PPolygonNormal>       m_Normals           = new FastTable<PPolygonNormal>();
    protected FastTable<PPolygonColor>        m_Colors            = new FastTable<PPolygonColor>();
    protected FastTable<PPolygonTexCoord>     m_TexCoords         = new FastTable<PPolygonTexCoord>();

    private boolean                         m_bUniformTexCoords = false;    //  if true all textures will get index copy of texture number 0 TexCoords
    private int                             m_NumberOfTextures  = 1;        //  index multi textured mesh will have more than one texture
   
    // Edge / Vert counting
    private int                             m_EdgeCount             = 0;
    // Because the vertex count can increase during triangulation and jME conversion,
    // we maintain this count for convenience
    private int                             m_TessalatedVertexCount = 0;
    

    private transient boolean m_bInBatch          = false;
    
    private transient boolean m_bDebugInfo        = false; // True to dump extra debugging information
    
    private transient boolean m_bSubmitGeometry   = true; // Geometry will be submited if this flag allows it and if its "dirty"

    /**
     * Copy Constructor - This version performs a deep copy
     * on all data in order to properly tie up polygon vert
     * to mesh references.
     * @param other The PPolygonMesh to copy.
     */
    public PPolygonMesh(PPolygonMesh other) 
    {
        setName(other.getName());
        setTransform(other.getTransform());
        
        beginBatch(); // signal that the geometry is currently in flux

        // polygons
        for (PPolygon poly : other.m_Polygons)
        {
            PPolygon newPoly = new PPolygon(poly);
            newPoly.setPolygonMesh(this);
            m_Polygons.add(newPoly);
        }
        
        material = new PMeshMaterial(other.material);

        m_BoundingCube.set(other.m_BoundingCube.getMin(), other.m_BoundingCube.getMax());
        m_BoundingSphere.set(other.m_BoundingSphere.getCenterRef(), other.m_BoundingSphere.getRadius());
        m_bSmoothNormals    = other.m_bSmoothNormals;
        
        // Vert data
        for (PPolygonPosition pos : other.m_Positions)
            m_Positions.add(new PPolygonPosition(pos.m_Position));
        for (PPolygonNormal norm : other.m_Normals)
            m_Normals.add(new PPolygonNormal(norm.m_Normal));
        for (PPolygonColor color : other.m_Colors)
            m_Colors.add(new PPolygonColor(color.m_Color));
        for (PPolygonTexCoord tex : other.m_TexCoords)
            m_TexCoords.add(new PPolygonTexCoord(tex.m_TexCoord));
        

        setUniformTexCoords(other.isUniformTexCoords()); // Actually do the copy
        m_NumberOfTextures  = other.getNumberOfTextures();
        
        // Great Success!
        endBatch();
        useVBO();
        m_Geometry.setModelBound(new BoundingSphere());
    }

    /**
     * Default Constructor - Sets the name to "Untitled" and gives an identity
     * matrix for the transform. Also creates an empty list of PPolygon objects.
     */
    public PPolygonMesh()
    {
        setName("Untitled");
        setTransform(new PTransform());
        m_Polygons = new FastTable<PPolygon>();
        useVBO();
        m_Geometry.setModelBound(new BoundingSphere());
    }
    
    /**
     * Constructs a new PPolygonMesh with the specified name, an identity
     * transform, and an empty list of polygons.
     * @param name The desired name
     */
    public PPolygonMesh(String name)
    {
        setName(name);
        setTransform(new PTransform());
        m_Polygons = new FastTable<PPolygon>();
        useVBO();
        m_Geometry.setModelBound(new BoundingSphere());
    }
    
    /**
     * This method sets the name to an empty string and clears the backing
     * lists of PPolygons as well as the lists of vertex components (normals 
     * texture coordinates, positions, etc). The bounding volumes are also cleared.
     */
    public void clear()
    {
        clear(true);
    }

    /**
     * This method sets the name to the empty string. If bClearData is true,
     * the collections of vertex components (positions, normals, colors, texture
     * coordinates) are also cleared out. The bounding volumes are also cleared.
     * @param bClearData
     */
    public void clear(boolean bClearData)
    {
        setName("");
        m_Polygons.clear();

        if (bClearData)
        {
            m_Positions.clear();
            m_Normals.clear();
            m_Colors.clear();
            m_TexCoords.clear();
        }

        m_BoundingCube.clear();
        m_BoundingSphere.clear();
    }

    /**
     * Get the internal polygons
     * @return
     */
    public Iterable<PPolygon> getPolygons() {
        return m_Polygons;
    }

    /**
     * This method reconstruct the jME TriMesh based on current PPolygon data
     * if the inherited PNode.isDirty() method returns true.
     * m_bSubmitGeometry is a flag that may turn this feature off.
     */
    public void submit() 
    {
        if (isDirty() && m_bSubmitGeometry)
        {
            PPolygonTriMeshAssembler.reconstructTriMesh(m_Geometry, this);
            setDirty(false, false); // No longer dirty now
        }
    }

    /**
     * Retrieve the underlying jME geometry.
     * @return The jME TriMesh
     */
    public TriMesh getGeometry()
    {
        return m_Geometry;
    }
 
    /**
     * Flipping the normals will make this mesh dirty
     */
    public void flipNormals()
    {
        for (int i = 0; i < m_Polygons.size(); i++)
            m_Polygons.get(i).flipNormals();
            
	//  re-calculate normals if not in index batch
        if (!m_bInBatch)
            calculatePolygonVertexNormals();
        
        setDirty(true, false);
    }

    /**
     * Returns a reference to this mesh's material
     * @return reference to the material
     */
    public PMeshMaterial getMaterialRef()
    {
        return material;
    }

    /**
     * Returns a newly constructed copy of this mesh's materia;
     * @return A copy of the material
     */
    public PMeshMaterial getMaterialCopy()
    {
        if (material != null)
            return new PMeshMaterial(material);
        else return null;
    }

    /**
     * Assigns a material to this mesh.
     * @param pMaterial The new material
     */
    public void setMaterial(PMeshMaterial pMaterial)
    {
      	if (material == pMaterial)
            return;

        material = pMaterial;
    }

    /**
     * Retrieves the cube bounding volume
     * @return Cube
     */
    public PCube getBoundingCube()
    {
        return(m_BoundingCube);
    }

    /**
     * Retrieves the spherical bounding volume
     * @return Sphere
     */
    public PSphere getBoundingSphere()
    {
        return(m_BoundingSphere);
    }

    /**
     * This method should be called to indicated when a mesh's geometry is being
     * modified, in an undefined state, or otherwise should not be touched.
     * Example: When adding new polygons to a mesh, the additions should happen
     * between a beginBatch and matching endBatch call.
     */
    public void beginBatch()
    {
        m_bInBatch = true;
    }

    /**
     * This method should be called to indicate that the geometry is safe to
     * poke again. This should have a matching beginBatch call. This method
     * recalculates the vertex normals, edge count, tesselated vert count, and
     * bounding volume(s)
     */
    public void endBatch()
    {
        endBatch(true);
    }

    /**
     * Indicates that the geometry is no longer being modified. Vertex normals,
     * the edge count, the tesselated vertex count, and the bounding volumes are
     * all recalculated. If the boolean is true, vertex normals will be \
     * recalculated.
     * @param bCalculatePolygonVertexNormals true to recalculate vertex normals
     */
    public void endBatch(boolean bCalculatePolygonVertexNormals)
    {
        m_bInBatch = false;

        if (bCalculatePolygonVertexNormals)
        {
            //  Calculate Polygon Vertice Normals.
            calculatePolygonVertexNormals();
        }

        //  Calculate edge count
        calculateEdgeCountAndTessalatedVertexCount();
        
        //  Calculate the BoundingBox.
        calculateBoundingBox();

        //  Calculate the BoundingSphere.
        calculateBoundingSphere();
    }


    /**
     * True if beginBatch has been called and not yet ended.
     * @return m_bInBatch (boolean)
     */
    public boolean inBatch()
    {
        return(m_bInBatch);
    }
    
    /**
     * Check if the debug flag is enabled
     * @return
     */
    public boolean isDebugEnabled()
    {
        return m_bDebugInfo;
    }
    
    /**
     * Determine whether or not this instance should
     * output debugging information.
     * @param bEnabled
     */
    public void setDebug(boolean bEnabled)
    {
        m_bDebugInfo = bEnabled;
    }

    //  ******************************
    //  PolygonMeshPosition management methods.
    //  ******************************
    
    public int addPosition(Vector3f pPosition)
    {
        PPolygonPosition pPolygonMeshPosition = new PPolygonPosition(pPosition);

        m_Positions.add(pPolygonMeshPosition);

        return(m_Positions.size()-1);
    }

    /**
     * Attempts to find the index of the specified position.
     * Returns -1 on failure to locate.
     * @param pPosition The position to check for.
     * @return The index, or -1 if the entry was not found
     */
    public int findPosition(Vector3f pPosition)
    {
        int                  index;
        PPolygonPosition pPolygonMeshPosition;

	for (index=0; index<getPositionCount(); index++)
	{
            pPolygonMeshPosition = getPosition(index);

            if (pPolygonMeshPosition.m_Position.equals(pPosition))
                return(index);
        }

        return(-1); // Not found
    }

    /**
     * Retrieves the index of the specified position vector. If the vector is
     * not found, it will be added and the new index returned.
     * @param pPosition The position to find /add
     * @return index of pPosition
     */
    public int getPosition(Vector3f pPosition)
    {
        int Index = findPosition(pPosition);
        if (Index == -1)
            Index = addPosition(pPosition);
        else
        {
            int a = 0;
        }

        return(Index);
    }

    /**
     * Retrieves the position at the specified index.
     * @param Index
     * @return PPolygonPosition
     */
    public PPolygonPosition getPosition(int Index)
    {
        return( (PPolygonPosition)m_Positions.get(Index));
    }

    /**
     * Returns the size of the mesh's (duplicate checked) position collection
     * @return int (size of the position collection)
     */
    public int getPositionCount()
    {
        return(m_Positions.size());
    }

    //  ******************************
    //  PolygonMeshNormal management methods.
    //  ******************************
    
    public int addNormal(Vector3f pNormal)
    {
        PPolygonNormal pPolygonMeshNormal = new PPolygonNormal(pNormal);

        m_Normals.add(pPolygonMeshNormal);
        
        return(m_Normals.size()-1);
    }

    /**
     * Attempts to find the index of the specified normal.
     * Returns -1 on failure to locate.
     * @param pNormal The normal to check for.
     * @return The index, or -1 if the entry was not found
     */
    public int findNormal(Vector3f pNormal)
    {
        int                a;
        PPolygonNormal pPolygonMeshNormal;

	for (a=0; a<getNormalCount(); a++)
	{
            pPolygonMeshNormal = getNormal(a);

            if (pPolygonMeshNormal.m_Normal.equals(pNormal))
                return(a);
        }

        return(-1);
    }

    /**
     * Retrieves the index of the specified normal vector. If the vector is
     * not found, it will be added and the new index returned.
     * @param pNormal The normal to find /add
     * @return index of pNormal
     */
    public int getNormal(Vector3f pNormal)
    {
        int Index = findNormal(pNormal);
	if (Index == -1)
            Index = addNormal(pNormal);

        return(Index);
    }

    
    /**
     * Retrieves the normal at the specified index.
     * @param Index
     * @return PPolygonNormal (normal at specified index)
     */
    public PPolygonNormal getNormal(int Index)
    {
        return( (PPolygonNormal)m_Normals.get(Index));
    }

    /**
     * Returns the size of the mesh's (duplicate checked) normal collection
     * @return int (size of the normal collection)
     */
    public int getNormalCount()
    {
        return(m_Normals.size());
    }

    //  ******************************
    //  PolygonMeshColor management methods.
    //  ******************************
    
    public int addColor(ColorRGBA pColor)
    {
        PPolygonColor pPolygonMeshColor = new PPolygonColor(pColor);

        m_Colors.add(pPolygonMeshColor);

        return(m_Colors.size()-1);
    }

    /**
     * Attempts to find the index of the specified color.
     * Returns -1 on failure to locate.
     * @param pColor The position to check for.
     * @return The index, or -1 if the entry was not found
     */
    public int findColor(ColorRGBA pColor)
    {
        int               a;
        PPolygonColor pPolygonMeshColor;

	for (a=0; a<getColorCount(); a++)
	{
            pPolygonMeshColor = getColor(a);

            if (pPolygonMeshColor.m_Color.equals(pColor))
                return(a);
        }

        return(-1);
    }

    /**
     * Retrieves the index of the specified color. If it is
     * not found, it will be added and the new index returned.
     * @param pColor The color to find /add
     * @return index of pColor
     */
    public int getColor(ColorRGBA pColor)
    {
        int Index = findColor(pColor);
	if (Index == -1)
            Index = addColor(pColor);

        return(Index);
    }

    /**
     * Retrieves the color at the specified index.
     * @param Index
     * @return PPolygonColor (color at specified index)
     */
    public PPolygonColor getColor(int Index)
    {
        return( (PPolygonColor)m_Colors.get(Index));
    }

    /**
     * Returns the size of the mesh's (duplicate checked) color collection
     * @return int (number of colors in the mesh)
     */
    public int getColorCount()
    {
        return(m_Colors.size());
    }

    //  ******************************
    //  PolygonMeshTexCoord management methods.
    //  ******************************
    
    public int addTexCoord(Vector2f pTexCoord)
    {
        PPolygonTexCoord pPolygonMeshTexCoord = new PPolygonTexCoord(pTexCoord);

        m_TexCoords.add(pPolygonMeshTexCoord);

        return(m_TexCoords.size()-1);
    }

    /**
     * Attempts to find the index of the specified texture coordinate.
     * Returns -1 on failure to locate.
     * @param pTexCoord The texture coordinates to check for.
     * @return The index, or -1 if the entry was not found
     */
    public int findTexCoord(Vector2f pTexCoord)
    {
        int result = m_TexCoords.indexOf(new PPolygonTexCoord(pTexCoord));
        return result;
    }

    /**
     * Retrieves the index of the specified texture coordinates. If the vector is
     * not found, it will be added and the new index returned.
     * @param pTexCoord The texture coordinates to find /add
     * @return index of pTexCoord
     */
    public int getTexCoord(Vector2f pTexCoord)
    {
        int Index = findTexCoord(pTexCoord);
	if (Index == -1)
            Index = addTexCoord(pTexCoord);

        return(Index);
    }

    /**
     * Retrieves the texture coordinate at the specified index.
     * @param Index
     * @return PPolygonTexCoord
     */
    public PPolygonTexCoord getTexCoord(int Index)
    {
        return( (PPolygonTexCoord)m_TexCoords.get(Index));
    }

    /**
     * Returns the size of the mesh's (duplicate checked) position collection
     * @return int (size of collection)
     */
    public int getTexCoordCount()
    {
        return(m_TexCoords.size());
    }
    
    //  ******************************
    //  Polygon methods.
    //  ******************************
    /**
     * Creates a new PPolygon object and sets the owning mesh to <code>this</code>.
     * The PPolygon is added to the internal collection, and then a reference is 
     * returned
     * @return Reference to the new PPolygon
     */
    public PPolygon createPolygon()
    {
        PPolygon pPolygon = new PPolygon(this);

	m_Polygons.add(pPolygon);

	return(pPolygon);
    }

    /**
     * Sets the owning mesh to <code>this</code> and adds a reference to pPolygon
     * to the internal PPolygon collection.
     * CAUTION: This method is unsafe if the vertices contained in pPolygon
     *  reference components in another mesh, as the indicies for the verts
     *  will potentially be invalid for this mesh.
     * @param pPolygon The polygon to add.
     */
    public void addPolygon(PPolygon pPolygon)
    {
        pPolygon.setPolygonMesh(this);

        m_Polygons.add(pPolygon);
    }

    /**
     * Retrives the PPolygon object at the specified index.
     * @param Index
     * @return PPolygon (ppolygon at specified index)
     */
    public PPolygon getPolygon(int Index)
    {
        return(m_Polygons.get(Index));
    }

    /**
     * Removes the specified PPolygon from this mesh's list.
     * @param poly The polygon to remove
     * @return True if found, false otherwise
     */
    public boolean removePolygon(PPolygon poly)
    {
        return m_Polygons.remove(poly);
    }

    /**
     * Retrieves the size of the internal PPolygon collection
     * @return int 
     */
    public int getPolygonCount()
    {
        return(m_Polygons.size());
    }

    /**
     * Retrieves the number of valid texture coordinate indicies that the
     * first vertex of the first polygon has, or -1 on failure.
     * @return int
     */
    public int getNumberOfTexCoords()
    {
        // Dahlgren - added the following safety checking
        if (m_Polygons.isEmpty() || getPolygon(0).getVertexCount() == 0) // fail
            return -1;

        PPolygon pPolygon               = getPolygon(0);
        PPolygonVertexIndices pPolygonVertice = pPolygon.getVertex(0);
        int NumberOfTexCoords           = 0;

        if (pPolygonVertice.m_TexCoordIndex[0] != -1)
            NumberOfTexCoords++;

        if (pPolygonVertice.m_TexCoordIndex[1] != -1)
            NumberOfTexCoords++;

        if (pPolygonVertice.m_TexCoordIndex[2] != -1)
            NumberOfTexCoords++;

        if (pPolygonVertice.m_TexCoordIndex[3] != -1)
            NumberOfTexCoords++;

        return(NumberOfTexCoords);
    }

    /**
     * Creates a new triangle made of three points (using the same color). All
     * texture coordinates for units other than 0 are set to -1. The requested
     * indicies should map to the internal collections of vertex components.
     * (Use get[Normal/Position/Color] methods to generate values)
     * @param Position1Index
     * @param Position2Index
     * @param Position3Index
     * @param ColorIndex
     * @param TexCoord1Index
     * @param TexCoord2Index
     * @param TexCoord3Index
     */
    public void addTriangle(int Position1Index,
                            int Position2Index,
                            int Position3Index,
                            int ColorIndex,
                            int TexCoord1Index,
                            int TexCoord2Index,
                            int TexCoord3Index)
    {
        PPolygon pPolygon = new PPolygon();

        pPolygon.setPolygonMesh(this);

        pPolygon.beginBatch();
        pPolygon.addVertex(Position1Index, ColorIndex, -1, TexCoord1Index, -1, -1, -1);
        pPolygon.addVertex(Position2Index, ColorIndex, -1, TexCoord2Index, -1, -1, -1);
        pPolygon.addVertex(Position3Index, ColorIndex, -1, TexCoord3Index, -1, -1, -1);
        pPolygon.endBatch();

        m_Polygons.add(pPolygon);
    }

    /**
     * Creates a new quad made from the supplied three points (sharing the same
     * color). All texture coordinates for units other than 0 are set to -1. The
     * requested indicies should map to the internal collections of vertex 
     * components.
     * (Use get[Normal/Position/Color] methods to generate values)
     * @param Position1Index
     * @param Position2Index
     * @param Position3Index
     * @param Position4Index
     * @param ColorIndex
     * @param TexCoord1Index
     * @param TexCoord2Index
     * @param TexCoord3Index
     * @param TexCoord4Index
     */
    public void addQuad(int Position1Index,
                        int Position2Index,
                        int Position3Index,
                        int Position4Index,
                        int ColorIndex,
                        int TexCoord1Index,
                        int TexCoord2Index,
                        int TexCoord3Index,
                        int TexCoord4Index)
    {
        PPolygon pPolygon = new PPolygon();

        pPolygon.setPolygonMesh(this);

        pPolygon.beginBatch();
    
        pPolygon.addVertex(Position1Index, ColorIndex, -1, TexCoord1Index, -1, -1, -1);
        pPolygon.addVertex(Position2Index, ColorIndex, -1, TexCoord2Index, -1, -1, -1);
        pPolygon.addVertex(Position3Index, ColorIndex, -1, TexCoord3Index, -1, -1, -1);
        pPolygon.addVertex(Position4Index, ColorIndex, -1, TexCoord4Index, -1, -1, -1);
        pPolygon.endBatch();

        m_Polygons.add(pPolygon);
    }

    /**
     * Creates and adds a polygon with VertexCount vertices made of the components
     * specified by pPositionIndices[n] and pTexCoordIndices[n]. All texture
     * coordinate indices for units other than 0 are set to -1;
     *
     * @param pPositionIndices
     * @param pTexCoordIndices
     * @param VertexCount number of vertices
     */
    public void addPolygon(int [] pPositionIndices,
                           int [] pTexCoordIndices,
                           int VertexCount)
    {
        // Dahlgren - Added safety checking
        if (    pPositionIndices == null ||
                pTexCoordIndices == null ||
                VertexCount < 0          ||
                VertexCount > pPositionIndices.length ||
                VertexCount > pTexCoordIndices.length)
            return; // Failed safety checking


        PPolygon	pPolygon = new PPolygon();

        pPolygon.setPolygonMesh(this);

        pPolygon.beginBatch();
        for (int i = 0; i < VertexCount; i++)
            pPolygon.addVertex(pPositionIndices[i], -1, pTexCoordIndices[i], -1, -1, -1);
        pPolygon.endBatch();

        m_Polygons.add(pPolygon);
    }

    /**
     *
     * @return true if smooth normals mode is on
     */
    public boolean getSmoothNormals()
    {
        return(m_bSmoothNormals);
    }

    /**
     * Calling this function will make the mesh dirty
     * @param bSmoothNormals
     */
    public void setSmoothNormals(boolean bSmoothNormals)
    {
        m_bSmoothNormals = bSmoothNormals;
        if (!m_bInBatch)
            calculatePolygonVertexNormals();
        setDirty(true, false);
    }
    
    /**
     * This method calculates the normals for each PPolygon of the mesh.
     */
    public void calculatePolygonVertexNormals()
    {
        //System.out.println("PPolygonMesh.calculatePolygonVerticeNormals()");

        if (m_bSmoothNormals)
            calculateSmoothPolygonVerticeNormals();
        else
            calculateFlatPolygonVerticeNormals();
    }

    /**
     * Retrieves the "number of textures". The value of this is 1 by default. It
     * will only vary if set explicitely by setNumberOfTextures,
     * or if the copy constructor was used (which copies the value from the other
     * mesh). This is generally used as a convenience method for tracking
     * multitexturing states.
     * @return m_NumberOfTextures (Integer)
     */
    public int getNumberOfTextures() 
    {
        return m_NumberOfTextures;
    }

    /**
     * Sets the number of texture units that this mesh should use when rendering.
     * @param NumberOfTextures
     */
    public void setNumberOfTextures(int NumberOfTextures) 
    {
        m_NumberOfTextures = NumberOfTextures;
    }

    /**
     * Multitexturing related.
     * @return True if uniform texture coordinates are being used.
     */
    public boolean isUniformTexCoords() 
    {
        return m_bUniformTexCoords;
    }

    /**
     * Sets the texture coordinates of all applicable texture units
     * to the UVs specified for texture unit 0
     * *WARNING* Setting bUniformTexCoords = true will eradicate UV data
     * for all texture units and replace it with the UV data for texture
     * unit 0.
     * @param bUnifromTexCoords true to use uniform texture coordinates
     */
    public void setUniformTexCoords(boolean bUnifromTexCoords) 
    {
        m_bUniformTexCoords = bUnifromTexCoords;
        if (m_bUniformTexCoords == true)
        {
            // Copy texture coordinates to the set number of units
            for (int i = 0; i < m_NumberOfTextures; i++)
            {
                m_Geometry.copyTextureCoordinates(0, i, 1.0f);
            }
        }
    }

    /**
     * Debugging utility to print information to system.out
     * @param spacing
     */
/*
    public void dump(String spacing)
    {
        dumpHeader(spacing);
        
        dumpPolygons(spacing);
    }
*/

    /**
     * Debugging utility to print information to system.out
     * @param spacing
     */
    public void dumpHeader(String spacing)
    {
        int a;
        PPolygonPosition pPosition = null;
        PPolygonNormal pNormal = null;
        PPolygonColor pColor = null;
        PPolygonTexCoord pTexCoord = null;


        System.out.println(spacing + "PolygonMesh:");
        System.out.println(spacing + "   Name:           " + getName());
        System.out.println(spacing + "   Material:       " + ((material != null) ? material.getName() : "(None)"));
        m_BoundingCube.dump(spacing + "   ", "BoundingCube");
        m_BoundingSphere.dump(spacing + "   ", "BoundingSphere");
        System.out.println(spacing + "   SmoothNormals:  " + ((m_bSmoothNormals) ? "Yes" : "No"));

        //  Dump the Positions.
        System.out.println(spacing + "   Positions:  " + getPositionCount());
        for (a=0; a<getPositionCount(); a++)
        {
            pPosition = getPosition(a);
            
            System.out.println(spacing + "      Position[" + a + "]:  (" + pPosition.m_Position.x + ", " + pPosition.m_Position.y + ", " + pPosition.m_Position.z + ")");
        }

        //  Dump the Normals.
        System.out.println(spacing + "   Normals:  " + getNormalCount());
        for (a=0; a<getNormalCount(); a++)
        {
            pNormal = getNormal(a);
            
            System.out.println(spacing + "      Normal[" + a + "]:  (" + pNormal.m_Normal.x + ", " + pNormal.m_Normal.y + ", " + pNormal.m_Normal.z + ")");
        }

        //  Dump the Colors.
        System.out.println(spacing + "   Colors:  " + getColorCount());
        for (a=0; a<getColorCount(); a++)
        {
            pColor = getColor(a);
            
            System.out.println(spacing + "      Color[" + a + "]:  (" + pColor.m_Color.r + ", " + pColor.m_Color.g + ", " + pColor.m_Color.b + ")");
        }

        //  Dump the TexCoords.
        System.out.println(spacing + "   TexCoords:  " + getTexCoordCount());
        for (a=0; a<getTexCoordCount(); a++)
        {
            pTexCoord = getTexCoord(a);
            
            System.out.println(spacing + "      TexCoord[" + a + "]:  (" + pTexCoord.m_TexCoord.x + ", " + pTexCoord.m_TexCoord.y + ")");
        }
    }

    /**
     * Debugging utility to output information about this mesh's PPolygon objects to System.out
     * @param spacing
     */
    public void dumpPolygons(String spacing)
    {
        int a;
        PPolygon pPolygon = null;
        
        System.out.println(spacing + "   Polygons:  " + getPolygonCount());
        for (a=0; a<getPolygonCount(); a++)
        {
            pPolygon = getPolygon(a);

            System.out.println(spacing + "      Polygon[" + a + "]  (" + pPolygon.getVertexCount() + " vertices)");
            pPolygon.dump(spacing + "         ");
        }
    }

    /**
     * Calculates the smooth normals for each vertex of this mesh's PPolygons.
     */
    public void calculateSmoothPolygonVerticeNormals()
    {
         // Index tracking integers
        int				PositionIndex = 0;
        final int                       PositionCount = m_Positions.size();
        Vector3f                        SmoothNormal = new Vector3f(0.0f, 0.0f, 0.0f);
        float				SmoothNormalCount; // Used for averaging once accumulation is complete
        int				SmoothNormalIndex;
        // This collection keeps references to verts that need to have their normal index updated during
        // each iteration through the position collection
        FastList<PPolygonVertexIndices> vertsNeedingUpdates = new FastList<PPolygonVertexIndices>();

        //  Release all the Normals.
        m_Normals.clear();


        for (;PositionIndex < PositionCount; PositionIndex++)
        {
            // Clear out update list and smooth normal data
            vertsNeedingUpdates.clear();

            SmoothNormal.zero();
            SmoothNormalCount = 0.0f;

            //  For each Polygon using the position, include it's normal in the
            //  SmoothNormal calculation.
            for (PPolygon poly : m_Polygons)
            {
                if (poly.isUsingPositionIndex(PositionIndex))
                {
                    // update the state of the smooth normal
                    SmoothNormal.addLocal(poly.getNormal());
                    SmoothNormalCount++;
                    // tell each vert in this polygon that this is now their
                    // normal's index
                    for (PPolygonVertexIndices vert : poly.getVertexCollection())
                    {
                        if (vert.m_PositionIndex == PositionIndex)
                            vertsNeedingUpdates.add(vert);
                    }
                }
            }
            // Compute average
            SmoothNormal.divideLocal(SmoothNormalCount);
            // Normalize this madness -- unnecessary, length will never exceed one
            //SmoothNormal.normalizeLocal();
            // get a good index for it
            SmoothNormalIndex = getNormal(SmoothNormal);
            // Insert this normal into the appropriate verts
            for (PPolygonVertexIndices vert : vertsNeedingUpdates)
                vert.m_NormalIndex = SmoothNormalIndex;
        }


//        if (m_bDebugInfo)
//            System.out.println("   PPolygonMesh.calculateSmoothPolygonVerticeNormals()");
//
//        int				PositionIndex;
//        int				PolygonIndex;
//        PPolygon                        pPolygon;
//        int                             VerticeIndex;
//        PPolygonVertexIndices                 pVertice;
//        Vector3f                        SmoothNormal = new Vector3f();
//        int				SmoothNormalCount;
//        int				SmoothNormalIndex;
//
//        //  Release all the Normals.
//        m_Normals.clear();
//
//        for (PositionIndex=0; PositionIndex<m_Positions.size(); PositionIndex++)
//        {
//            SmoothNormal.zero();
//            SmoothNormalCount = 0;
//
//            //  For each Polygon using the position, include it's normal in the
//            //  SmoothNormal calculation.
//            for (PolygonIndex=0; PolygonIndex<m_Polygons.size(); PolygonIndex++)
//            {
//                pPolygon = (PPolygon)m_Polygons.get(PolygonIndex);
//
//                if (pPolygon.isUsingPositionIndex(PositionIndex))
//                {
//                    Vector3f normal = new Vector3f();
//
////                    pPolygon.getVertexNormal(PositionIndex, normal);
//                    SmoothNormal.add(pPolygon.getNormal(), SmoothNormal);
//                    SmoothNormalCount++;
//
////                    System.out.println("pPolygon.m_Normal = (" + pPolygon.m_Normal.x + ", " + pPolygon.m_Normal.y + ", " + pPolygon.m_Normal.y + ")");
//
////                    System.out.println("SmoothNormal = (" + SmoothNormal.x + ", " + SmoothNormal.y + ", " + SmoothNormal.y + ")");
////                    System.out.println("SmoothNormalCount = " + SmoothNormalCount);
//                }
//            }
//
////            System.out.println("Calculating SmoothNormal.");
////            System.out.println("   SmoothNormal = (" + SmoothNormal.x + ", " + SmoothNormal.y + ", " + SmoothNormal.y + ")");
////            System.out.println("   SmoothNormalCount = " + SmoothNormalCount);
//
//
//            SmoothNormal = SmoothNormal.divide((float)SmoothNormalCount);
////            System.out.println("   SmoothNormal = (" + SmoothNormal.x + ", " + SmoothNormal.y + ", " + SmoothNormal.y + ")");
//
//            SmoothNormal = SmoothNormal.normalize();
////            System.out.println("   SmoothNormal = (" + SmoothNormal.x + ", " + SmoothNormal.y + ", " + SmoothNormal.y + ")");
//            SmoothNormalIndex = getNormal(SmoothNormal);
////            System.out.println("   SmoothNormalIndex = " + SmoothNormalIndex);
//
//
//            //  Loop through all the Polygons.
//            for (PolygonIndex=0; PolygonIndex<m_Polygons.size(); PolygonIndex++)
//            {
//                pPolygon = (PPolygon)m_Polygons.get(PolygonIndex);
//
//                //  Loop through all the vertices making up the Polygon.
//                for (VerticeIndex=0; VerticeIndex<pPolygon.getVertexCount(); VerticeIndex++)
//                {
//                    pVertice = pPolygon.getVertex(VerticeIndex);
//
//                    if (pVertice.m_PositionIndex == PositionIndex)
//                        pVertice.m_NormalIndex = SmoothNormalIndex;
//                }
//            }
//        }
    }

    /**
     * Calculates flat normals for the verts of the component PPolygon objects
     */
    public void calculateFlatPolygonVerticeNormals()
    {
        if (m_bDebugInfo)
            System.out.println("   PPolygonMesh.calculateFlatPolygonVerticeNormals()");
        
        int                     a;
        PPolygon		pPolygon;
        int			NormalIndex;
        int			VerticeIndex;
        PPolygonVertexIndices		pVertice;

        //  Release all the Normals.
        m_Normals.clear();

        for (a=0; a<m_Polygons.size(); a++)
        {
            pPolygon = (PPolygon)m_Polygons.get(a);

            //  Get the index of the normal.
            NormalIndex = getNormal(pPolygon.getNormal());

            //  Assign the same normal index to all the vertices.	
            for (VerticeIndex=0; VerticeIndex<pPolygon.getVertexCount(); VerticeIndex++)
            {
                pVertice = pPolygon.getVertex(VerticeIndex);

                pVertice.m_NormalIndex = NormalIndex;
            }
        }
    }

    /**
     * Clears any old data in the cubic bounding volume and recalculates it
     * using a min / max algorithm.
     */
    public void calculateBoundingBox()
    {
        m_BoundingCube.clear();

        if (getPositionCount() == 0)
            return;
        
        PPolygonPosition    pPosition;
        Vector3f                MinCorner = new Vector3f();
        Vector3f                MaxCorner = new Vector3f();
        
        pPosition = getPosition(0);
        
        MinCorner.set(pPosition.m_Position);
        MaxCorner.set(pPosition.m_Position);
        
        for (int i = 0; i < getPositionCount(); i++)
        {
            pPosition = getPosition(i);
            
            MathUtils.min(MinCorner, pPosition.m_Position);
            MathUtils.max(MaxCorner, pPosition.m_Position);
        }
        
        m_BoundingCube.set(MinCorner, MaxCorner);
        
    }

    /**
     * Clears out any old information in the spherical bounding volume and
     * recalculates it using the calculated center of the mesh and the furthest
     * point from that center as the radius.
     */
    public void calculateBoundingSphere()
    {
        m_BoundingSphere.clear();
        
        if (getPositionCount() == 0)
            return;
        
        PPolygonPosition    pPosition;
        Vector3f                Center  = new Vector3f(Vector3f.ZERO);
        float                   fRadius = 0.0f;
        float                   fLocalRadius;
     
        // Calculate the center of the BoundingSphere.
        for (int i = 0; i < getPositionCount(); i++)
        {
            pPosition   = getPosition(i);
            pPosition.m_Position.add(Center, Center);
        }
        
        Center = Center.divide((float)getPositionCount());
        
        // Calculate the radius of the BoundingSphere.
        for (int i = 0; i < getPositionCount(); i++)
        {
                pPosition = getPosition(i);
                
                // Calclate the distance between the center and the position.
                fLocalRadius = Center.distance(pPosition.m_Position);
                if (fLocalRadius > fRadius)
                    fRadius = fLocalRadius;
        }
        
        m_BoundingSphere.set(Center, fRadius);
    }

    /**
     * Retrieves the number of edges on the mesh.
     * @return m_EdgeCount (Integer)
     */
    public int getEdgeCount() 
    {
        return m_EdgeCount;
    }

    /**
     * Recalculates the edge count and tesselated vertex count members.
     */
    private void calculateEdgeCountAndTessalatedVertexCount() 
    {
        m_EdgeCount = 0;
        m_TessalatedVertexCount = 0;
        PPolygon poly;
        for (int i = 0; i < getPolygonCount(); i++)
        {
            poly = getPolygon(i);
            m_EdgeCount += poly.getVertexCount();
            m_TessalatedVertexCount += poly.getTriangleCount() * 3;
            
        } 
    }

    /**
     * Retrieves the current value of the tesselated vertex count member.
     * @return m_TessalatedVertexCount
     */
    public int getTessalatedVertexCount()
    {
        return m_TessalatedVertexCount;
    }
    
    /**
     * Provides access to the internal collection via direct reference
     * @return The collection
     */
    public FastTable<PPolygonPosition> getPositionsRef()
    {
        return m_Positions;
    }
    
    /**
     * Provides access to the internal collection via direct reference
     * @return The collection
     */
    public FastTable<PPolygonNormal> getNormalsRef()
    {
        return m_Normals;
    }
    
    /**
     * Provides access to the internal collection via direct reference
     * @return The collection
     */
    public FastTable<PPolygonColor> getColorsRef()
    {
        return m_Colors;
    }
    
    /**
     * Provides access to the internal collection via direct reference
     * @return The collection
     */
    public FastTable<PPolygonTexCoord> getTexCoordsRef()
    {
        return m_TexCoords;
    }
    
    /**
     * Combines the passed in mesh with this mesh. The results
     * are that the <code>other</code> parameter is unchanged, and
     * <code>this</code> now contains the original geometry in addition
     * to the parameter mesh.
     * @param other The mesh to add
     * @param bKeepOriginalMaterial  If true, <code>this</code>'s material is used
     * for the resulting combination, otherwise the <code>other</code> parameter
     * is used.
     */
    public void combinePolygonMesh(PPolygonMesh other, boolean bKeepOriginalMaterial)
    {
        // TODO this should reposition the mesh in world space before combining...
        // probably not functionall at this point
        
        if (bKeepOriginalMaterial == false)
        {
            setMaterial(other.getMaterialCopy());
            //m_pMaterial = new PMeshMaterial(other.m_pMaterial);
            setNumberOfTextures(other.getNumberOfTextures());
            setUniformTexCoords(other.isUniformTexCoords());
        }
        
        
        PPolygon newPoly = null;
        PPolygon curPoly = null;
        // For each polygon in other
        for (int i = 0; i < other.getPolygonCount(); ++i)
        {
            newPoly = new PPolygon();
            curPoly = other.getPolygon(i);
            // For each vertex in this polygon
            PPolygonVertexIndices newVert = null;
            PPolygonVertexIndices curVert = null;
            for (int vertIter = 0; vertIter < curPoly.getVertexCount(); ++vertIter)
            {
                newVert = new PPolygonVertexIndices();
                curVert = curPoly.getVertex(vertIter);
                newVert.m_PositionIndex = addPosition(
                        new Vector3f(other.getPosition(curVert.m_PositionIndex).m_Position));
                newVert.m_NormalIndex = addNormal(
                        new Vector3f(other.getNormal(curVert.m_NormalIndex).m_Normal));
                // Color is index bit trickier
                newVert.m_ColorIndex = addColor(
                        new ColorRGBA(other.getColor(curVert.m_ColorIndex).m_Color.r,
                                other.getColor(curVert.m_ColorIndex).m_Color.g,
                                other.getColor(curVert.m_ColorIndex).m_Color.b,
                                other.getColor(curVert.m_ColorIndex).m_Color.a));
                // And the texture coordinates
                for (int texCoordIter = 0; texCoordIter < 8; texCoordIter++)
                {
                    // Special case if the index is -1
                    if (curVert.m_TexCoordIndex[texCoordIter] == -1)
                        newVert.m_TexCoordIndex[texCoordIter] = -1;
                    else
                    {
                        newVert.m_TexCoordIndex[texCoordIter] = addTexCoord(
                            new Vector2f(other.getTexCoord(curVert.m_TexCoordIndex[texCoordIter]).m_TexCoord));
                    }
                }
                
                // Now add this vertex to our polygon
                newPoly.addVertex(newVert.m_PositionIndex, newVert.m_ColorIndex, newVert.m_NormalIndex, 
                        newVert.m_TexCoordIndex[0], newVert.m_TexCoordIndex[1], newVert.m_TexCoordIndex[2], newVert.m_TexCoordIndex[3]);
            }
            
            addPolygon(newPoly);
        }
    }

    /**
     * Convenience method for setting up the vertex buffer object associated with
     * this mesh.
     */
    private void useVBO()
    {
//        if (m_Geometry == null)
//            logger.warning("Cannot make null geometry use VBOs!");
//        else
//        {
//            VBOInfo info = new VBOInfo(true);
//            m_Geometry.setVBOInfo(info);
//        }
    }
    /**
     * Retrievs the shared asset reference for this PPolygonMesh. This is only
     * used in tracking what file (if any) this mesh originated from. This is
     * used to help facilitate instancing
     * @return m_SharedAsset (SharedAsset)
     */
    public SharedAsset getSharedAsset() {
        return m_SharedAsset;
    }

    /**
     * Sets the shared asset reference for this mesh. This SharedAsset is assumed
     * to contain a valid descriptor indicating what file this mesh originated from.
     * If the mesh has no file (is procedural), set SharedAsset to null
     * @param asset
     */
    public void setSharedAsset(SharedAsset asset) {
        m_SharedAsset = asset;
    }

    public boolean isSubmitGeometry() {
        return m_bSubmitGeometry;
    }

    public void setSubmitGeometry(boolean bSubmitGeometry) {
        this.m_bSubmitGeometry = bSubmitGeometry;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PPolygonMesh other = (PPolygonMesh) obj;
        if (this.material != other.material && (this.material == null || !this.material.equals(other.material))) {
            return false;
        }
        if (this.m_bSmoothNormals != other.m_bSmoothNormals) {
            return false;
        }
        if (this.m_Positions != other.m_Positions && (this.m_Positions == null || !this.m_Positions.equals(other.m_Positions))) {
            return false;
        }
        if (this.m_Normals != other.m_Normals && (this.m_Normals == null || !this.m_Normals.equals(other.m_Normals))) {
            return false;
        }
        if (this.m_Colors != other.m_Colors && (this.m_Colors == null || !this.m_Colors.equals(other.m_Colors))) {
            return false;
        }
        if (this.m_TexCoords != other.m_TexCoords && (this.m_TexCoords == null || !this.m_TexCoords.equals(other.m_TexCoords))) {
            return false;
        }
        if (this.m_bUniformTexCoords != other.m_bUniformTexCoords) {
            return false;
        }
        if (this.m_NumberOfTextures != other.m_NumberOfTextures) {
            return false;
        }
        if (this.m_Polygons != other.m_Polygons && (this.m_Polygons == null || !this.m_Polygons.equals(other.m_Polygons))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.material != null ? this.material.hashCode() : 0);
        hash = 73 * hash + (this.m_bSmoothNormals ? 1 : 0);
        hash = 73 * hash + (this.m_Positions != null ? this.m_Positions.hashCode() : 0);
        hash = 73 * hash + (this.m_Normals != null ? this.m_Normals.hashCode() : 0);
        hash = 73 * hash + (this.m_Colors != null ? this.m_Colors.hashCode() : 0);
        hash = 73 * hash + (this.m_TexCoords != null ? this.m_TexCoords.hashCode() : 0);
        hash = 73 * hash + (this.m_bUniformTexCoords ? 1 : 0);
        hash = 73 * hash + this.m_NumberOfTextures;
        hash = 73 * hash + (this.m_Polygons != null ? this.m_Polygons.hashCode() : 0);
        return hash;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all transient objects
        m_SharedAsset = null;
        m_bInBatch = false;
        m_bDebugInfo = false;
        m_bSubmitGeometry = true;

        m_BoundingCube = new PCube();
        m_BoundingSphere = new PSphere();
        calculateBoundingBox();
        calculateBoundingSphere();
        setDirty(true, false);
        useVBO();
    }

}




