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
package imi.scene.utils;

import imi.utils.PMathUtils;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.util.geom.BufferUtils;
import imi.loaders.PGeometryVertexBuffer;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.parts.PGeometryTriangle;
import imi.scene.polygonmodel.parts.PGeometryVertex;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.polygon.PPolygonPosition;
import imi.scene.polygonmodel.parts.polygon.PPolygonVertexIndices;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import java.util.ArrayList;

public class PMeshUtils 
{

    //  Creates a hill in a PolygonMesh.  ... only a grid can be passed in... right?
    static public void createHill(PPolygonMesh pPolygonMesh,
                                   Vector3f Center,
                                   float fHillRadius,
                                   float fHillHeight,
                                   float fHillMinorHeightRandomness)
    {
	int				VertexCount;
	PPolygonPosition            pPosition;
	float				fFraction;
	float				fDistance;
	float				fDistanceFromOuter  = 0.0f;
	float				fAngle;
	float				fVerticeHeight;
	float				fVerticeHeightAdjustment;
	float				h = 0.0f;
	float				x = 0.0f;
	float				y = 0.0f;
	boolean				bWasInBatch = pPolygonMesh.inBatch();

	if (bWasInBatch)
	{
            //  Begin a Batch.
            pPolygonMesh.beginBatch();
	}

	VertexCount = pPolygonMesh.getPositionCount();

	for (int i = 0; i < VertexCount; i++)
	{
            pPosition = pPolygonMesh.getPosition(i);

            //  Calculate the distance between two points.
            fDistance = pPosition.m_Position.distance(Center);
            if (fDistance > fHillRadius)
                continue;

            fFraction = fDistance / fHillRadius;
            x = 1.0f - fFraction;
		
            fAngle = 90.0f * x;
            fVerticeHeight = (float)Math.sin(fAngle * 3.141592f / 180.0f) * fHillHeight;

/*
            fDistance = 25.0f;

            fDistanceFromOuter = fHillRadius - fDistance;

            //fAngle = 90.0f * ((fHillRadius - fDistance) / fHillRadius);
            fAngle = 90.0f * (1.0f - (fDistance / fHillRadius)) - 90.0f;

            h = fDistanceFromOuter / CoMath.sine(fAngle);

            fVerticeHeight = (float)Math.sqrt(h * h - fDistanceFromOuter * fDistanceFromOuter);
*/
//          fVerticeHeight = (fHillRadius - fDistance) * (float)tan(fAngle * DEGREES_TO_RADIANS);

            if (fHillMinorHeightRandomness != 0)
            {
                fVerticeHeightAdjustment = PMathUtils.RandomFloatInRange(-fHillMinorHeightRandomness, fHillMinorHeightRandomness);

		fVerticeHeight += fVerticeHeightAdjustment;
            }

            pPosition.m_Position.y += fVerticeHeight;
	}

	pPolygonMesh.setSmoothNormals(true);

	if (bWasInBatch)
	{
            //  End a Batch.
            pPolygonMesh.endBatch();
	}
    }
    
    static public void calculateVertexTangents(PPolygonSkinnedMesh sourceMesh)
    {
        int VertCount = sourceMesh.getPolygonCount() * 4;
        PGeometryVertexBuffer vb = new PGeometryVertexBuffer();
        
        Vector3f [] tangentResult = new Vector3f [VertCount];
        
        ArrayList<Vector3f> tangents1 = new ArrayList<Vector3f>(VertCount);
        // initialize
        for (int i = 0; i < VertCount; ++i)
            tangents1.add(new Vector3f());
        
        
        ArrayList<Vector3f> tangents2 = new ArrayList<Vector3f>(VertCount);
        // initialize
        for (int i = 0; i < VertCount; ++i)
            tangents2.add(new Vector3f());
        
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>(VertCount);
        // initialize
        for (int i = 0; i < VertCount; ++i)
            normals.add(new Vector3f());
        
        // for each polygon
        for (int currentPolyIndex = 0; currentPolyIndex < sourceMesh.getPolygonCount(); ++currentPolyIndex)
        {
            // grab this polygon
            PPolygon currentPoly = sourceMesh.getPolygon(currentPolyIndex);
            // for each triangle
            for (int currentTriangleIndex = 0; currentTriangleIndex < currentPoly.getTriangleCount(); ++currentTriangleIndex)
            {
                Vector3f triangleNormal = currentPoly.getNormal();
                
                PGeometryTriangle currentTriangle = new PGeometryTriangle();
                currentPoly.getTriangle(currentTriangleIndex, currentTriangle);
                
                // grab this triangles verts and their indices
                PGeometryVertex vert1 = currentTriangle.m_Vertices[0];
                int index1 = vb.addVertex(vert1);
                PGeometryVertex vert2 = currentTriangle.m_Vertices[1];
                int index2 = vb.addVertex(vert2);
                PGeometryVertex vert3 = currentTriangle.m_Vertices[2];
                int index3 = vb.addVertex(vert3);
                
                // grab those verts texture coords
                Vector2f texCoordVert1 = vert1.m_TexCoords[0];
                Vector2f texCoordVert2 = vert2.m_TexCoords[0];
                Vector2f texCoordVert3 = vert3.m_TexCoords[0];
                
                float x1 = vert2.m_Position.x - vert1.m_Position.x;
                float x2 = vert3.m_Position.x - vert1.m_Position.x;
                float y1 = vert2.m_Position.y - vert1.m_Position.y;
                float y2 = vert3.m_Position.y - vert1.m_Position.y;
                float z1 = vert2.m_Position.z - vert1.m_Position.z;
                float z2 = vert3.m_Position.z - vert1.m_Position.z;

                float s1 = texCoordVert2.x - texCoordVert1.x;
                float s2 = texCoordVert3.x - texCoordVert1.x;
                float t1 = texCoordVert2.y - texCoordVert1.y;
                float t2 = texCoordVert3.y - texCoordVert1.y;

                float r = 1.0F / (s1 * t2 - s2 * t1);
                
                Vector3f sdir = new Vector3f((t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r,
                        (t2 * z1 - t1 * z2) * r);
                
                Vector3f tdir = new Vector3f((s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r,
                        (s1 * z2 - s2 * z1) * r);

                // set some stuff for whatever reason
                tangents1.get(index1).add(sdir);
                tangents1.get(index2).add(sdir);
                tangents1.get(index3).add(sdir);

                tangents2.get(index1).add(tdir);
                tangents2.get(index2).add(tdir);
                tangents2.get(index3).add(tdir);
            }
            
            for (int i = 0; i < vb.count(); ++i)
            {
                Vector3f n = normals.get(i);
                Vector3f t = tangents1.get(i);
                
                Vector3f vecTminusN = new Vector3f();
                vecTminusN.set(t);

                // Gram-Schmidt orthogonalize
                tangentResult[i] = (t.subtract(n).mult(n.dot(t))).normalize();
                // dump it
                //System.out.println(i + ": " + tangentResult[i]);

                // Calculate handedness
                //tangent[a].w = (Dot(Cross(n, t), tan2[a]) < 0.0F) ? -1.0F : 1.0F;
            }
            
            //sourceMesh.setVertexTangents(BufferUtils.createFloatBuffer(tangentResult));
        }
                /*
                 for (long a = 0; a < vertexCount; a++)
                {
                    const Vector3D& n = normal[a];
                    const Vector3D& t = tan1[a];

                    // Gram-Schmidt orthogonalize
                    tangent[a] = (t - n * Dot(n, t)).Normalize();

                    // Calculate handedness
                    tangent[a].w = (Dot(Cross(n, t), tan2[a]) < 0.0F) ? -1.0F : 1.0F;
                }
                */
    }

    //  Creates an PolygonMesh containing a Triangle.
    static public PPolygonMesh createTriangle(String meshName,
                                        Vector3f Point1,
                                        Vector3f Point2,
                                        Vector3f Point3,
                                        ColorRGBA Diffuse,
                                        Vector2f TexCoord1,
                                        Vector2f TexCoord2,
                                        Vector2f TexCoord3)
    {
        PPolygonMesh		pPolygonMesh    = null;
        int			Position1Index;
        int			Position2Index;
        int			Position3Index;
        int			DiffuseIndex	= -1;
        int			TexCoord1Index;
        int			TexCoord2Index;
        int			TexCoord3Index;

            
        //System.out.println("createTriangle()");
        //System.out.println("   Point1:  (" + Point1.x + ", " + Point1.y + ", " + Point1.z + ")");
        //System.out.println("   Point2:  (" + Point2.x + ", " + Point2.y + ", " + Point2.z + ")");
        //System.out.println("   Point3:  (" + Point3.x + ", " + Point3.y + ", " + Point3.z + ")");

        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin a Batch.
        pPolygonMesh.beginBatch();

        Position1Index = pPolygonMesh.getPosition(Point1);
        Position2Index = pPolygonMesh.getPosition(Point2);
        Position3Index = pPolygonMesh.getPosition(Point3);

    	DiffuseIndex = pPolygonMesh.getColor(Diffuse);

        TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord1);
        TexCoord2Index = pPolygonMesh.getTexCoord(TexCoord2);
        TexCoord3Index = pPolygonMesh.getTexCoord(TexCoord3);

            
        //  add the Triangle to the EditableMesh.
        pPolygonMesh.addTriangle(Position3Index, Position2Index, Position1Index,
                                 DiffuseIndex,
                                 TexCoord1Index, TexCoord2Index, TexCoord3Index);

        pPolygonMesh.setSmoothNormals(false);

        //  End the Batch.
        pPolygonMesh.endBatch();

        return(pPolygonMesh);
    }

    //  Creates an PolygonMesh containing a Quad.
    static public PPolygonMesh createQuad(String meshName,
                                        Vector3f Point1,
                                        Vector3f Point2,
                                        Vector3f Point3,
                                        Vector3f Point4,
                                        ColorRGBA Diffuse,
                                        Vector2f TexCoord1,
                                        Vector2f TexCoord2,
                                        Vector2f TexCoord3,
                                        Vector2f TexCoord4)
    {
        PPolygonMesh	pPolygonMesh        = null;
        int		Position1Index;
        int		Position2Index;
        int		Position3Index;
        int		Position4Index;
        int		DiffuseIndex        = -1;
        int		TexCoord1Index;
        int		TexCoord2Index;
        int		TexCoord3Index;
        int		TexCoord4Index;

        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();

        Position1Index = pPolygonMesh.getPosition(Point1);
        Position2Index = pPolygonMesh.getPosition(Point2);
        Position3Index = pPolygonMesh.getPosition(Point3);
        Position4Index = pPolygonMesh.getPosition(Point4);

    	DiffuseIndex = pPolygonMesh.getColor(Diffuse);

        TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord1);
        TexCoord2Index = pPolygonMesh.getTexCoord(TexCoord2);
        TexCoord3Index = pPolygonMesh.getTexCoord(TexCoord3);
        TexCoord4Index = pPolygonMesh.getTexCoord(TexCoord4);

        //  add the Quad to the PolygonMesh.
        pPolygonMesh.addQuad(Position4Index, Position3Index, Position2Index, Position1Index,
                             DiffuseIndex,
                             TexCoord4Index, TexCoord3Index, TexCoord2Index, TexCoord1Index);

        //  End the Batch.
        pPolygonMesh.endBatch();

        return(pPolygonMesh);
    }

    //  Creates an PolygonMesh containing a Grid.
    static public PPolygonMesh createGrid(String meshName,
                                        Vector3f Center,
                                        float fWidth,
                                        float fDepth,
                                        int NumberOfDivisionsAlongWidth,
                                        int NumberOfDivisionsAlongDepth,
                                        ColorRGBA Diffuse,
                                        Vector2f MinTexCoord,
                                        Vector2f MaxTexCoord)
    {
        PPolygonMesh		pPolygonMesh		= null;
        Vector3f		Point1                  = new Vector3f();
        Vector3f		Point2                  = new Vector3f();
        Vector3f		Point3                  = new Vector3f();
        Vector3f		Point4                  = new Vector3f();
        Vector2f		TexCoord1               = new Vector2f();
        Vector2f		TexCoord2               = new Vector2f();
        Vector2f		TexCoord3               = new Vector2f();
        Vector2f		TexCoord4               = new Vector2f();
        int			Position1Index;
        int			Position2Index;
        int			Position3Index;
        int			Position4Index;
        int			DiffuseIndex		= -1;
        int			TexCoord1Index;
        int			TexCoord2Index;
        int			TexCoord3Index;
        int			TexCoord4Index;
        int			x;
        int			z;
        float			fHalfWidth		= fWidth * 0.5f;
        float			fHalfDepth		= fDepth * 0.5f;
        float			fX;
        float			fZ;
        float			fXStep;
        float			fZStep;
        float			fTexCoordU;
        float			fTexCoordV;
        float			fTexCoordStepU;
        float			fTexCoordStepV;


        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();

        if (NumberOfDivisionsAlongWidth < 1)
            NumberOfDivisionsAlongWidth = 1;

        if (NumberOfDivisionsAlongDepth < 1)
            NumberOfDivisionsAlongDepth = 1;

        fXStep = fWidth / NumberOfDivisionsAlongWidth;
        fZStep = fDepth / NumberOfDivisionsAlongDepth;

        fTexCoordStepU = (MaxTexCoord.x - MinTexCoord.x) / NumberOfDivisionsAlongWidth;
        fTexCoordStepV = (MaxTexCoord.y - MinTexCoord.y) / NumberOfDivisionsAlongDepth;

    	DiffuseIndex = pPolygonMesh.getColor(Diffuse);

        fZ = Center.z - fHalfDepth;
        fTexCoordV = MinTexCoord.y;

        for (z=0; z<NumberOfDivisionsAlongDepth; z++)
        {
            fX = Center.x - fHalfWidth;
            fTexCoordU = MinTexCoord.y;

            for (x=0; x<NumberOfDivisionsAlongWidth; x++)
            {
                Point1.set(fX, Center.y, fZ);
                Point2.set(fX + fXStep, Center.y, fZ);
                Point3.set(fX + fXStep, Center.y, fZ + fZStep);
                Point4.set(fX, Center.y, fZ + fZStep);

                TexCoord1.set(fTexCoordU, fTexCoordV);
                TexCoord2.set(fTexCoordU + fTexCoordStepU, fTexCoordV);
                TexCoord3.set(fTexCoordU + fTexCoordStepU, fTexCoordV + fTexCoordStepV);
                TexCoord4.set(fTexCoordU, fTexCoordV + fTexCoordStepV);


                Position1Index = pPolygonMesh.getPosition(Point1);
                Position2Index = pPolygonMesh.getPosition(Point2);
                Position3Index = pPolygonMesh.getPosition(Point3);
                Position4Index = pPolygonMesh.getPosition(Point4);

                TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord1);
                TexCoord2Index = pPolygonMesh.getTexCoord(TexCoord2);
                TexCoord3Index = pPolygonMesh.getTexCoord(TexCoord3);
                TexCoord4Index = pPolygonMesh.getTexCoord(TexCoord4);

                //  Add the polygon.
                pPolygonMesh.addQuad(Position4Index, Position3Index, Position2Index, Position1Index,
                                     DiffuseIndex,
                                     TexCoord4Index, TexCoord3Index, TexCoord2Index, TexCoord1Index);

                fX += fXStep;
                fTexCoordU += fTexCoordStepU;
            }

            fZ += fZStep;

            fTexCoordU = MinTexCoord.x;		
            fTexCoordV += fTexCoordStepU;
        }

        pPolygonMesh.setSmoothNormals(false);

        //  End the Batch.
        pPolygonMesh.endBatch();


        return(pPolygonMesh);
    }

    //  Creates an PolygonMesh containing a Box.
    static public PPolygonMesh createBox(String meshName,
                                       Vector3f Center,
                                       float fWidth,
                                       float fHeight,
                                       float fDepth,
                                       ColorRGBA Diffuse)
    {
        PPolygonMesh	pPolygonMesh        = null;
        Vector3f	Point1              = new Vector3f();
        Vector3f	Point2              = new Vector3f();
        Vector3f	Point3              = new Vector3f();
        Vector3f	Point4              = new Vector3f();
        Vector3f	Point5              = new Vector3f();
        Vector3f	Point6              = new Vector3f();
        Vector3f	Point7              = new Vector3f();
        Vector3f	Point8              = new Vector3f();
        Vector2f	TexCoord1           = new Vector2f();
        Vector2f	TexCoord2           = new Vector2f();
        Vector2f	TexCoord3           = new Vector2f();
        Vector2f	TexCoord4           = new Vector2f();

        int             Position1Index;
        int		Position2Index;
        int		Position3Index;
        int		Position4Index;
        int		Position5Index;
        int		Position6Index;
        int		Position7Index;
        int		Position8Index;
        int		DiffuseIndex		= -1;
        int		TexCoord1Index;
        int		TexCoord2Index;
        int		TexCoord3Index;
        int		TexCoord4Index;
        float		fHalfWidth              = fWidth * 0.5f;
        float		fHalfHeight		= fHeight * 0.5f;
        float		fHalfDepth		= fDepth * 0.5f;

        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();

        Point1.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
        Point2.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
        Point3.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);
        Point4.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);

        Point5.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
        Point6.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
        Point7.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);
        Point8.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);

        TexCoord1.set(0.0f, 0.0f);
        TexCoord2.set(1.0f, 0.0f);
        TexCoord3.set(1.0f, 1.0f);
        TexCoord4.set(0.0f, 1.0f);

        Position1Index = pPolygonMesh.getPosition(Point1);
        Position2Index = pPolygonMesh.getPosition(Point2);
        Position3Index = pPolygonMesh.getPosition(Point3);
        Position4Index = pPolygonMesh.getPosition(Point4);
        Position5Index = pPolygonMesh.getPosition(Point5);
        Position6Index = pPolygonMesh.getPosition(Point6);
        Position7Index = pPolygonMesh.getPosition(Point7);
        Position8Index = pPolygonMesh.getPosition(Point8);

    	DiffuseIndex = pPolygonMesh.getColor(Diffuse);

        TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord1);
        TexCoord2Index = pPolygonMesh.getTexCoord(TexCoord2);
        TexCoord3Index = pPolygonMesh.getTexCoord(TexCoord3);
        TexCoord4Index = pPolygonMesh.getTexCoord(TexCoord4);

        //  'Top' Quad.
        pPolygonMesh.addQuad(Position4Index, Position3Index, Position2Index, Position1Index,
                             DiffuseIndex,
                             TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        //  'Left' Quad.
        pPolygonMesh.addQuad(Position5Index, Position8Index, Position4Index, Position1Index,
                             DiffuseIndex,
                             TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        //  'Back' Quad.
        pPolygonMesh.addQuad(Position6Index, Position5Index, Position1Index, Position2Index,
                             DiffuseIndex,
                             TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        //  'Right' Quad.
        pPolygonMesh.addQuad(Position7Index, Position6Index, Position2Index, Position3Index,
                             DiffuseIndex,
                             TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        //  'Front' Quad.
        pPolygonMesh.addQuad(Position8Index, Position7Index, Position3Index, Position4Index,
                             DiffuseIndex,
                             TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        //  'Bottom' Quad.
        pPolygonMesh.addQuad(Position7Index, Position8Index, Position5Index, Position6Index,
                             DiffuseIndex,
                             TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        pPolygonMesh.setSmoothNormals(true);

        //  End the Batch.
        pPolygonMesh.endBatch();

        return(pPolygonMesh);
    }
    
    /**
     * Create a table from two cylinders
     * @param name The name for the model
     * @param fTableRadius Radius of the table top, the bottom is 1/3 that size
     * @param fTableHeight How tall is the table
     * @return A brand new mesh!
     */
    static public PPolygonMesh createTable(String name,
                                            float fTableRadius,
                                            float fTableHeight)
    {
        // make the top
        PPolygonMesh result = createCylinder("Table", 
                    Vector3f.UNIT_Y.mult(fTableHeight), fTableHeight * 0.13f, 16, 1, // Center, Thickness, slices, stacks
                    fTableRadius, fTableRadius, true, true, ColorRGBA.white); // top radius, bottom radius, cap top, cap bottom, diffuse
        // make the bottom
        PPolygonMesh base = createCylinder("TableBase",
                    Vector3f.ZERO, fTableHeight, 8, 3, 
                    fTableRadius * 0.3f, fTableRadius * 0.3f, false, true, ColorRGBA.white);
        // combine them
        result.combinePolygonMesh(base, true);
        
        return result;
    }

    /**
     * This method creates and returns a room with a door on the -Z wall
     * The "Additional verts" generated represent the following points on the -z wall (as seen facing it)
     * -------------1-------2------------
     * |                                |
     * |            3-------4           |
     * |            |       |           |
     * |            |       |           |
     * |            |       |           |
     * |            |       |           |
     * |____________5       6___________|
     * @param meshName The name of the mesh
     * @param Center Where this mesh should be located
     * @param fWidth The width of the room
     * @param fHeight The height of the room
     * @param fDepth The depth
     * @param fDoorWidth The width of the doorway
     * @param fDoorHeight The height of the doorway
     * @return A brand new room-type mesh ;)
     */
    static public PPolygonMesh createRoomWithDoor(String meshName,
                                                  Vector3f Center,
                                                  float fWidth,
                                                  float fHeight,
                                                  float fDepth,
                                                  float fDoorWidth,
                                                  float fDoorHeight)
    {
        PPolygonMesh result = createCubeBox(meshName, Center, fWidth, fHeight, fDepth, ColorRGBA.white);
        // Remove the -Z wall
        PPolygon removedWall = result.getPolygon(3);
        
        // reconstruct the wall polygon
        PPolygon partOne = new PPolygon();
        PPolygon partTwo = new PPolygon();
        PPolygon partThree = new PPolygon();
        partOne.setPolygonMesh(result);
        partTwo.setPolygonMesh(result);
        partThree.setPolygonMesh(result);
        
        
        PPolygonVertexIndices v1 = new PPolygonVertexIndices();
        PPolygonVertexIndices v2 = new PPolygonVertexIndices();
        PPolygonVertexIndices v3 = new PPolygonVertexIndices();
        PPolygonVertexIndices v4 = new PPolygonVertexIndices();
        PPolygonVertexIndices v5 = new PPolygonVertexIndices();
        PPolygonVertexIndices v6 = new PPolygonVertexIndices();
        Vector3f vecBuffer = new Vector3f();
        
        //TODO - FIX TEXTURE COORDS
        final float fUMin = 0.25f;
        final float fUMax = 0.5f;
        final float fVMin = 0.75f;
        final float fVMax = 1.0f;
        final float fURange = (fUMax - fUMin);
        final float fVRange = (fVMax - fVMin);
        
        
        
        // Set up first additional vert
        vecBuffer.setX(Center.x + fDoorWidth * 0.5f);
        vecBuffer.setY(Center.y + fHeight * 0.5f);
        vecBuffer.setZ(Center.z - fDepth * 0.5f);
        
        v1.m_PositionIndex = result.getPosition(vecBuffer);
        v1.m_TexCoordIndex[0] = result.getTexCoord(
                new Vector2f(
                    fUMin + ((fWidth * 0.5f - fDoorWidth * 0.5f) / fWidth) * fURange,
                    fVMin + (1 * fVRange))
                );
        v1.m_ColorIndex = result.getColor(ColorRGBA.white);
        
        // set up second additional vert
        vecBuffer.setX(Center.x - fDoorWidth * 0.5f);
        vecBuffer.setY(Center.y + fHeight * 0.5f);
        vecBuffer.setZ(Center.z - fDepth * 0.5f);
        
        v2.m_PositionIndex = result.getPosition(vecBuffer);
        v2.m_TexCoordIndex[0] = result.getTexCoord(
                new Vector2f(
                    fUMin + ((fWidth * 0.5f + fDoorWidth * 0.5f) / fWidth) * fURange,
                    fVMin + (1 * fVRange))
                );
        v2.m_ColorIndex = result.getColor(ColorRGBA.white);
        
        // set up third additional vert
        vecBuffer.setX(Center.x + fDoorWidth * 0.5f);
        vecBuffer.setY(Center.y - fHeight * 0.5f + fDoorHeight);
        vecBuffer.setZ(Center.z - fDepth * 0.5f);
        
        v3.m_PositionIndex = result.getPosition(vecBuffer);
        v3.m_TexCoordIndex[0] = result.getTexCoord(
                new Vector2f(
                    fUMin + ((fWidth * 0.5f - fDoorWidth * 0.5f) / fWidth) * fURange,
                    fVMin + ((fDoorHeight / fHeight) * fVRange))
                );
        v3.m_ColorIndex = result.getColor(ColorRGBA.white);
        
        
        // set up fourth additional vert
        vecBuffer.setX(Center.x - fDoorWidth * 0.5f);
        vecBuffer.setY(Center.y - fHeight * 0.5f + fDoorHeight);
        vecBuffer.setZ(Center.z - fDepth * 0.5f);
        
        v4.m_PositionIndex = result.getPosition(vecBuffer);
        v4.m_TexCoordIndex[0] = result.getTexCoord( 
                new Vector2f(
                    fUMin + ((fWidth * 0.5f + fDoorWidth * 0.5f) / fWidth) * fURange,
                    fVMin + ((fDoorHeight / fHeight) * fVRange))
                );
        v4.m_ColorIndex = result.getColor(ColorRGBA.white);
        
        // set up fifth additional vert
        vecBuffer.setX(Center.x + fDoorWidth * 0.5f);
        vecBuffer.setY(Center.y - fHeight * 0.5f);
        vecBuffer.setZ(Center.z - fDepth * 0.5f);
        
        v5.m_PositionIndex = result.getPosition(vecBuffer);
        v5.m_TexCoordIndex[0] = result.getTexCoord(
                new Vector2f(
                    fUMin + ((fWidth * 0.5f - fDoorWidth * 0.5f) / fWidth) * fURange,
                    fVMin + 0)
                );
        v5.m_ColorIndex = result.getColor(ColorRGBA.white);
        
        // set up sixth additional vert
        vecBuffer.setX(Center.x - fDoorWidth * 0.5f);
        vecBuffer.setY(Center.y - fHeight * 0.5f);
        vecBuffer.setZ(Center.z - fDepth * 0.5f);
        
        v6.m_PositionIndex = result.getPosition(vecBuffer);
        v6.m_TexCoordIndex[0] = result.getTexCoord(
                new Vector2f(
                    fUMin + ((fWidth * 0.5f + fDoorWidth * 0.5f) / fWidth) * fURange,
                    fVMin + 0)
                );
        v6.m_ColorIndex = result.getColor(ColorRGBA.white);
        

        // generate the three filler quads
        partOne.addVertex(removedWall.getVertex(3));
        partOne.addVertex(removedWall.getVertex(0));
        partOne.addVertex(v5);
        partOne.addVertex(v1);
        result.addPolygon(partOne);
        
        partTwo.addVertex(v1);
        partTwo.addVertex(v3);
        partTwo.addVertex(v4);
        partTwo.addVertex(v2);
        result.addPolygon(partTwo);
        
        partThree.addVertex(v2);
        partThree.addVertex(v6);
        partThree.addVertex(removedWall.getVertex(1));
        partThree.addVertex(removedWall.getVertex(2));
        result.addPolygon(partThree);
        
        
        result.removePolygon(removedWall);
        
        result.calculateSmoothPolygonVerticeNormals();
        result.flipNormals();
        return result;
    }
    
    /**
     * This method generates a very specific sort of ground plane that has hills
     * located throughout. 
     * @param fWidth Width of the whole ground plane
     * @param fLength Length of the whole ground plane
     * @param fRepeats How many times the texture should repeat to look good
     * @return All of this in one wonderful polygon mesh =)
     */
    static public PPolygonMesh createGroundPlane(float fWidth,
                                                float fLength,
                                                float fRepeats)
    {
        // Create the grid
        PPolygonMesh result = new PPolygonMesh("GroundPlane");
        
        result = createGrid("GroundPlane", Vector3f.ZERO, fWidth, fLength, (int)fWidth / 10, (int)fLength / 10, ColorRGBA.white, new Vector2f(0.0f, 0.0f), new Vector2f(fRepeats, fRepeats));
        // add hills to it
        // H1
        createHill(result, new Vector3f(fWidth * 0.4f, 0.0f, fLength * 0.5f), fWidth * 0.25f, 20.0f, 2.0f);
        // H2
        createHill(result, new Vector3f(fWidth * 0.4f, 0.0f, fLength * -0.4f), fWidth * 0.08f, 12.0f, 1.5f);
        // H3
        createHill(result, new Vector3f(fWidth * 0.3f, 0.0f, fLength * -0.5f), fWidth * 0.08f, 11.0f, 1.2f);
        // H4
        createHill(result, new Vector3f(0.0f, 0.0f, fLength * -0.33f), fWidth * 0.1f, 8.0f, 0.7f);
        // H5
        createHill(result, new Vector3f(fWidth * -0.46f, 0.0f, fLength * -0.25f), fWidth * 0.2f, 30.0f, 3.5f);
        // H6
        createHill(result, new Vector3f(fWidth * -0.35f, 0.0f, fLength * 0.4f), fWidth * 0.2f, 8.0f, 0.5f);
        
        return result;
    }

    //  Creates an PolygonMesh containing a Box.
    static public PPolygonMesh createCubeBox(String meshName,
                                           Vector3f Center,
                                           float fWidth,
                                           float fHeight,
                                           float fDepth,
                                           ColorRGBA Diffuse)
    {
        PPolygonMesh		pPolygonMesh		= null;
        Vector3f		Point1                  = new Vector3f();
        Vector3f		Point2                  = new Vector3f();
        Vector3f		Point3                  = new Vector3f();
        Vector3f		Point4                  = new Vector3f();
        Vector3f		Point5                  = new Vector3f();
        Vector3f		Point6                  = new Vector3f();
        Vector3f		Point7                  = new Vector3f();
        Vector3f		Point8                  = new Vector3f();
        Vector2f		[]Box1TexCoords         = new Vector2f[8];
        Vector2f		[]Box2TexCoords         = new Vector2f[8];
        Vector2f		[]Box3TexCoords         = new Vector2f[8];
        Vector2f		[]Box4TexCoords         = new Vector2f[8];
        Vector2f		[]Box5TexCoords         = new Vector2f[8];
        Vector2f		[]Box6TexCoords         = new Vector2f[8];

        int			[]Box1TexCoordIndexes   = new int[8];
        int			[]Box2TexCoordIndexes   = new int[8];
        int			[]Box3TexCoordIndexes   = new int[8];
        int			[]Box4TexCoordIndexes   = new int[8];
        int			[]Box5TexCoordIndexes   = new int[8];
        int			[]Box6TexCoordIndexes   = new int[8];

        int			Position1Index;
        int			Position2Index;
        int			Position3Index;
        int			Position4Index;
        int			Position5Index;
        int			Position6Index;
        int			Position7Index;
        int			Position8Index;
        int			DiffuseIndex		= -1;
        float			fHalfWidth		= fWidth * 0.5f;
        float			fHalfHeight		= fHeight * 0.5f;
        float			fHalfDepth		= fDepth * 0.5f;

        int			a;

        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();

        Point1.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
        Point2.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
        Point3.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);
        Point4.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);

        Point5.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
        Point6.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
        Point7.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);
        Point8.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);


        //  Allocate all the Vector2fs representing TexCoords.
        for (a=0; a<4; a++)
        {
            Box1TexCoords[a] = new Vector2f();
            Box2TexCoords[a] = new Vector2f();
            Box3TexCoords[a] = new Vector2f();
            Box4TexCoords[a] = new Vector2f();
            Box5TexCoords[a] = new Vector2f();
            Box6TexCoords[a] = new Vector2f();  
        }


        Box1TexCoords[0].set(0.25f, 0.0f);
        Box1TexCoords[1].set(0.5f, 0.0f);
        Box1TexCoords[2].set(0.5f, 0.25f);
        Box1TexCoords[3].set(0.25f, 0.25f);

        Box2TexCoords[0].set(0.25f, 0.25f);
        Box2TexCoords[1].set(0.5f, 0.25f);
        Box2TexCoords[2].set(0.5f, 0.5f);
        Box2TexCoords[3].set(0.25f, 0.5f);

        Box3TexCoords[0].set(0.25f, 0.5f);
        Box3TexCoords[1].set(0.5f, 0.5f);
        Box3TexCoords[2].set(0.5f, 0.75f);
        Box3TexCoords[3].set(0.25f, 0.75f);

        Box4TexCoords[0].set(0.25f, 0.75f);
        Box4TexCoords[1].set(0.5f, 0.75f);
        Box4TexCoords[2].set(0.5f, 1.0f);
        Box4TexCoords[3].set(0.25f, 1.0f);

        Box5TexCoords[0].set(0.0f, 0.25f);
        Box5TexCoords[1].set(0.25f, 0.25f);
        Box5TexCoords[2].set(0.25f, 0.5f);
        Box5TexCoords[3].set(0.0f, 0.5f);

        Box6TexCoords[0].set(0.5f, 0.25f);
        Box6TexCoords[1].set(0.75f, 0.25f);
        Box6TexCoords[2].set(0.75f, 0.5f);
        Box6TexCoords[3].set(0.5f, 0.5f);



        Position1Index = pPolygonMesh.getPosition(Point1);
        Position2Index = pPolygonMesh.getPosition(Point2);
        Position3Index = pPolygonMesh.getPosition(Point3);
        Position4Index = pPolygonMesh.getPosition(Point4);
        Position5Index = pPolygonMesh.getPosition(Point5);
        Position6Index = pPolygonMesh.getPosition(Point6);
        Position7Index = pPolygonMesh.getPosition(Point7);
        Position8Index = pPolygonMesh.getPosition(Point8);

    	DiffuseIndex = pPolygonMesh.getColor(Diffuse);

        for (a=0; a<4; a++)
        {
            Box1TexCoordIndexes[a] = pPolygonMesh.getTexCoord(Box1TexCoords[a]);
            Box2TexCoordIndexes[a] = pPolygonMesh.getTexCoord(Box2TexCoords[a]);
            Box3TexCoordIndexes[a] = pPolygonMesh.getTexCoord(Box3TexCoords[a]);
            Box4TexCoordIndexes[a] = pPolygonMesh.getTexCoord(Box4TexCoords[a]);
            Box5TexCoordIndexes[a] = pPolygonMesh.getTexCoord(Box5TexCoords[a]);
            Box6TexCoordIndexes[a] = pPolygonMesh.getTexCoord(Box6TexCoords[a]);
        }

        //  'Top' Quad.
        pPolygonMesh.addQuad(Position4Index, Position3Index, Position2Index, Position1Index,
                             DiffuseIndex,
                             Box1TexCoordIndexes[0], Box1TexCoordIndexes[1], Box1TexCoordIndexes[2], Box1TexCoordIndexes[3]);

        //  'Front' Quad.
        pPolygonMesh.addQuad(Position8Index, Position7Index, Position3Index, Position4Index,
                             DiffuseIndex,
                             Box2TexCoordIndexes[0], Box2TexCoordIndexes[1], Box2TexCoordIndexes[2], Box2TexCoordIndexes[3]);

        //  'Bottom' Quad.
        pPolygonMesh.addQuad(Position7Index, Position8Index, Position5Index, Position6Index,
                             DiffuseIndex,
                             Box3TexCoordIndexes[0], Box3TexCoordIndexes[1], Box3TexCoordIndexes[2], Box3TexCoordIndexes[3]);

        //  'Back' Quad.
        pPolygonMesh.addQuad(Position6Index, Position5Index, Position1Index, Position2Index,
                             DiffuseIndex,
                             Box4TexCoordIndexes[0], Box4TexCoordIndexes[1], Box4TexCoordIndexes[2], Box4TexCoordIndexes[3]);

        //  'Left' Quad.
        pPolygonMesh.addQuad(Position5Index, Position8Index, Position4Index, Position1Index,
                             DiffuseIndex,
                             Box5TexCoordIndexes[0], Box5TexCoordIndexes[1], Box5TexCoordIndexes[2], Box5TexCoordIndexes[3]);

        //  'Right' Quad.
        pPolygonMesh.addQuad(Position7Index, Position6Index, Position2Index, Position3Index,
                             DiffuseIndex,
                             Box6TexCoordIndexes[0], Box6TexCoordIndexes[1], Box6TexCoordIndexes[2], Box6TexCoordIndexes[3]);


        pPolygonMesh.setSmoothNormals(true);

        //  End the Batch.
        pPolygonMesh.endBatch();


        return(pPolygonMesh);
    }

    //  Creates an PolygonMesh containing a Sphere.
    static public  PPolygonMesh createSphere(String meshName,
                                              Vector3f Center,
                                              float fRadius,
                                              int NumberOfSlices,
                                              int NumberOfStacks,
                                              ColorRGBA Diffuse)
    {
        PPolygonMesh            pPolygonMesh;
        Vector3f		TopCenter           = new Vector3f();
        Vector3f		BottomCenter        = new Vector3f();
        float			fTopRadius;
        float			fBottomRadius;
        float			fTopHeight;
        float			fBottomHeight;
        float			fStackStep;
        float			fStackYHeight;
        int			SliceIndex;
        int			StackIndex;
        float			fAngle;
        float			fNextAngle;
        float			fAngleStep;
        Vector3f		TopPoint1           = new Vector3f();
        Vector3f		TopPoint2           = new Vector3f();
        Vector3f		BottomPoint1        = new Vector3f();
        Vector3f		BottomPoint2        = new Vector3f();
        int			Position1Index;
        int			Position2Index;
        int			Position3Index;
        int			Position4Index;
        int			DiffuseIndex;
        int			TexCoord1Index;
        int			TexCoord2Index;
        int			TexCoord3Index;
        int			TexCoord4Index;
        Vector2f                TexCoord            = new Vector2f(0.0f, 0.0f);


        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();

        DiffuseIndex   = pPolygonMesh.getColor(Diffuse);
        TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord);
        TexCoord2Index = TexCoord1Index;
        TexCoord3Index = TexCoord1Index;
        TexCoord4Index = TexCoord1Index;

        if (NumberOfSlices < 3)
            NumberOfSlices = 3;

        if (NumberOfStacks < 3)
            NumberOfStacks = 3;

        fStackStep = (fRadius * 2.0f) / (float)NumberOfStacks;
        fAngleStep = (float)(2.0f * Math.PI) / (float)NumberOfSlices;

        //  **********************
        //  Draw the top part of the sphere.
        //  **********************
        TopCenter.set(Center);
        TopCenter.y -= fRadius;

        BottomCenter.set(TopCenter);
        BottomCenter.y += fStackStep * 0.5f;

        fStackYHeight = fRadius - fStackStep * 0.5f;

        fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);

        fAngle = 0.0f;

        TopPoint1.set(TopCenter);

        Position1Index = pPolygonMesh.getPosition(TopPoint1);

        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
        {
            fNextAngle = fAngle + fAngleStep;

            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
            BottomPoint1.y = BottomCenter.y;
            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);

            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
            BottomPoint2.y = BottomCenter.y;
            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);

            Position2Index = pPolygonMesh.getPosition(BottomPoint1);
            Position3Index = pPolygonMesh.getPosition(BottomPoint2);

            pPolygonMesh.addTriangle(Position1Index, Position2Index, Position3Index,
                                     DiffuseIndex,
                                     TexCoord1Index, TexCoord2Index, TexCoord3Index);

            fAngle = fNextAngle;
        }

        
        TopCenter.set(BottomCenter);

        BottomCenter.y += fStackStep * 0.5f;

        fTopRadius = fBottomRadius;

        fStackYHeight -= fStackStep * 0.5f;

        fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);

        fAngle = 0.0f;

        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
        {
            fNextAngle = fAngle + fAngleStep;

            TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
            TopPoint1.y = TopCenter.y;
            TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);

            TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
            TopPoint2.y = TopCenter.y;
            TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);

            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
            BottomPoint1.y = BottomCenter.y;
            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);

            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
            BottomPoint2.y = BottomCenter.y;
            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);

            Position1Index = pPolygonMesh.getPosition(TopPoint1);
            Position2Index = pPolygonMesh.getPosition(TopPoint2);
            Position3Index = pPolygonMesh.getPosition(BottomPoint1);
            Position4Index = pPolygonMesh.getPosition(BottomPoint2);

            pPolygonMesh.addQuad(Position1Index, Position2Index, Position3Index, Position4Index,
                                 DiffuseIndex,
                                 TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

            fAngle += fAngleStep;
        }



        //  **********************
        //  Draw the center part of the sphere.
        //  **********************
        TopCenter.set(BottomCenter);

        BottomCenter.y += fStackStep;

        fTopHeight    = fRadius - fStackStep;
        fBottomHeight = fTopHeight - fStackStep;

        for (StackIndex=0; StackIndex<NumberOfStacks-2; StackIndex++)
        {
            fTopRadius = (float)Math.sqrt(fRadius * fRadius - fTopHeight * fTopHeight);

            fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fBottomHeight * fBottomHeight);


            fAngle = 0.0f;

            for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
            {
                fNextAngle = fAngle + fAngleStep;

                TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
                TopPoint1.y = TopCenter.y;
                TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);

                TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
                TopPoint2.y = TopCenter.y;
                TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);

                BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
                BottomPoint1.y = BottomCenter.y;
                BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);

                BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
                BottomPoint2.y = BottomCenter.y;
                BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);

                Position1Index = pPolygonMesh.getPosition(TopPoint1);
                Position2Index = pPolygonMesh.getPosition(TopPoint2);
                Position3Index = pPolygonMesh.getPosition(BottomPoint1);
                Position4Index = pPolygonMesh.getPosition(BottomPoint2);

                pPolygonMesh.addQuad(Position1Index, Position2Index, Position3Index, Position4Index,
                                     DiffuseIndex,
                                     TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

                fAngle += fAngleStep;
            }

            TopCenter.set(BottomCenter);
            BottomCenter.y += fStackStep;

            fTopHeight    = fBottomHeight;
            fBottomHeight = fTopHeight - fStackStep;
        }



        //  **********************
        //  Draw the bottom part of the sphere.
        //  **********************
        TopCenter.set(Center);
        TopCenter.y += fRadius - fStackStep;

        BottomCenter.set(TopCenter);
        BottomCenter.y += fStackStep * 0.5f;

        fStackYHeight = fRadius - fStackStep;
        fTopRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);

        fStackYHeight += fStackStep * 0.5f;
        fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);

        fAngle = 0.0f;

        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
        {
            fNextAngle = fAngle + fAngleStep;

            TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
            TopPoint1.y = TopCenter.y;
            TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);

            TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
            TopPoint2.y = TopCenter.y;
            TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);

            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
            BottomPoint1.y = BottomCenter.y;
            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);

            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
            BottomPoint2.y = BottomCenter.y;
            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);

            Position1Index = pPolygonMesh.getPosition(TopPoint1);
            Position2Index = pPolygonMesh.getPosition(TopPoint2);
            Position3Index = pPolygonMesh.getPosition(BottomPoint1);
            Position4Index = pPolygonMesh.getPosition(BottomPoint2);

            pPolygonMesh.addQuad(Position1Index, Position2Index, Position3Index, Position4Index,
                                 DiffuseIndex,
                                 TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

            fAngle += fAngleStep;
        }


        TopCenter.set(Center);
        TopCenter.y += fRadius - fStackStep * 0.5f;

        BottomCenter.set(Center);
        BottomCenter.y += fRadius;

        fStackYHeight = fRadius - fStackStep * 0.5f;
        fTopRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);

        fAngle = 0.0f;

        Position3Index = pPolygonMesh.getPosition(BottomCenter);

        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
        {
            fNextAngle = fAngle + fAngleStep;

            TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
            TopPoint1.y = TopCenter.y;
            TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);

            TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
            TopPoint2.y = TopCenter.y;
            TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);

            Position1Index = pPolygonMesh.getPosition(TopPoint1);
            Position2Index = pPolygonMesh.getPosition(TopPoint2);

            pPolygonMesh.addTriangle(Position1Index, Position2Index, Position3Index,
                                     DiffuseIndex,
                                     TexCoord1Index, TexCoord2Index, TexCoord3Index);

            fAngle += fAngleStep;
        }


        pPolygonMesh.setSmoothNormals(false);

        //  End the Batch.
        pPolygonMesh.endBatch();


        return(pPolygonMesh);
    }

    //  Creates an PolygonMesh containing a Cone.
    static public  PPolygonMesh createCone(String meshName,
                                            Vector3f Bottom,
                                            float fHeight,
                                            int NumberOfSlices,
                                            int NumberOfStacks,
                                            float fBottomRadius,
                                            boolean bCapBottom,
                                            ColorRGBA Diffuse)
    {
        PPolygonMesh            pPolygonMesh;
        Vector3f		StackTopCenter              = new Vector3f();
        Vector3f		StackBottomCenter           = new Vector3f();
        float			fStackStep;
        int			SliceIndex;
        int			StackIndex;
        float			fAngle;
        float			fNextAngle;
        float			fAngleStep;
        Vector3f		TopPoint1                   = new Vector3f();
        Vector3f		TopPoint2                   = new Vector3f();
        Vector3f		BottomPoint1                = new Vector3f();
        Vector3f		BottomPoint2                = new Vector3f();
        int			Position1Index;
        int			Position2Index;
        int			Position3Index;
        int			Position4Index;
        int			DiffuseIndex;
        int			TexCoord1Index;
        int			TexCoord2Index;
        int			TexCoord3Index;
        int			TexCoord4Index;
        Vector2f                TexCoord                    = new Vector2f(0.0f, 0.0f);
        float			fStackTopRadius;
        float			fStackBottomRadius;
        float			fStackRadiusStep;


        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();

        DiffuseIndex   = pPolygonMesh.getColor(Diffuse);
        TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord);
        TexCoord2Index = TexCoord1Index;
        TexCoord3Index = TexCoord1Index;
        TexCoord4Index = TexCoord1Index;

        if (NumberOfSlices < 3)
            NumberOfSlices = 3;

        if (NumberOfStacks < 1)
            NumberOfStacks = 1;

        fStackStep = fHeight / (float)NumberOfStacks;
        fAngleStep = (float)(2.0f * Math.PI) / (float)NumberOfSlices;

        fStackRadiusStep = fBottomRadius / (float)NumberOfStacks;



        //  **********************
        //  Create the very top part of the cone.
        //  **********************
        StackTopCenter.set(Bottom);
        StackTopCenter.y += fHeight;

        StackBottomCenter.set(StackTopCenter);
        StackBottomCenter.y -= fStackStep;

        fStackBottomRadius = fStackRadiusStep;

        fAngle = 0.0f;

        TopPoint1.set(StackTopCenter);

        Position1Index = pPolygonMesh.getPosition(TopPoint1);

        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
        {
            fNextAngle = fAngle + fAngleStep;

            BottomPoint1.x = StackBottomCenter.x + (float)(Math.cos(fAngle) * fStackBottomRadius);
            BottomPoint1.y = StackBottomCenter.y;
            BottomPoint1.z = StackBottomCenter.z + (float)(Math.sin(fAngle) * fStackBottomRadius);

            BottomPoint2.x = StackBottomCenter.x + (float)(Math.cos(fNextAngle) * fStackBottomRadius);
            BottomPoint2.y = StackBottomCenter.y;
            BottomPoint2.z = StackBottomCenter.z + (float)(Math.sin(fNextAngle) * fStackBottomRadius);

            Position2Index = pPolygonMesh.getPosition(BottomPoint1);
            Position3Index = pPolygonMesh.getPosition(BottomPoint2);

            pPolygonMesh.addTriangle(Position1Index, Position2Index, Position3Index,
                                     DiffuseIndex,
                                     TexCoord1Index, TexCoord2Index, TexCoord3Index);

            fAngle = fNextAngle;
        }



        //  **********************
        //  Create the middle part of the cone.
        //  **********************
        if (NumberOfStacks > 1)
        {
            StackTopCenter.set(StackBottomCenter);
            StackBottomCenter.y -= fStackStep;

            fStackTopRadius = fStackBottomRadius;
            fStackBottomRadius	+= fStackRadiusStep;


            for (StackIndex=0; StackIndex<NumberOfStacks-1; StackIndex++)
            {
                fAngle = 0.0f;

                for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
                {
                    fNextAngle = fAngle + fAngleStep;

                    TopPoint1.x = StackTopCenter.x + (float)(Math.cos(fNextAngle) * fStackTopRadius);
                    TopPoint1.y = StackTopCenter.y;
                    TopPoint1.z = StackTopCenter.z + (float)(Math.sin(fNextAngle) * fStackTopRadius);

                    TopPoint2.x = StackTopCenter.x + (float)(Math.cos(fAngle) * fStackTopRadius);
                    TopPoint2.y = StackTopCenter.y;
                    TopPoint2.z = StackTopCenter.z + (float)(Math.sin(fAngle) * fStackTopRadius);

                    BottomPoint1.x = StackBottomCenter.x + (float)(Math.cos(fAngle) * fStackBottomRadius);
                    BottomPoint1.y = StackBottomCenter.y;
                    BottomPoint1.z = StackBottomCenter.z + (float)(Math.sin(fAngle) * fStackBottomRadius);

                    BottomPoint2.x = StackBottomCenter.x + (float)(Math.cos(fNextAngle) * fStackBottomRadius);
                    BottomPoint2.y = StackBottomCenter.y;
                    BottomPoint2.z = StackBottomCenter.z + (float)(Math.sin(fNextAngle) * fStackBottomRadius);

                    Position1Index = pPolygonMesh.getPosition(TopPoint1);
                    Position2Index = pPolygonMesh.getPosition(TopPoint2);
                    Position3Index = pPolygonMesh.getPosition(BottomPoint1);
                    Position4Index = pPolygonMesh.getPosition(BottomPoint2);

                    pPolygonMesh.addQuad(Position1Index, Position2Index, Position3Index, Position4Index,
                                         DiffuseIndex,
                                         TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

                    fAngle += fAngleStep;
                }

                fStackTopRadius		= fStackBottomRadius;
                fStackBottomRadius	= fStackBottomRadius + fStackRadiusStep;

                StackTopCenter.set(StackBottomCenter);
                StackBottomCenter.y -= fStackStep;
            }
        }



        //  **********************
        //  Cap the bottom of the cone.
        //  **********************
        if (bCapBottom)
        {
            StackBottomCenter.set(Bottom);

            fStackBottomRadius = fBottomRadius;

            fAngle = 0.0f;

            TopPoint1.set(StackBottomCenter);

            Position1Index = pPolygonMesh.getPosition(TopPoint1);

            for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
            {
                fNextAngle = fAngle + fAngleStep;

                BottomPoint1.x = StackBottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
                BottomPoint1.y = StackBottomCenter.y;
                BottomPoint1.z = StackBottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);

                BottomPoint2.x = StackBottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
                BottomPoint2.y = StackBottomCenter.y;
                BottomPoint2.z = StackBottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);

                Position2Index = pPolygonMesh.getPosition(BottomPoint1);
                Position3Index = pPolygonMesh.getPosition(BottomPoint2);

                pPolygonMesh.addTriangle(Position1Index, Position2Index, Position3Index,
                                         DiffuseIndex,
                                         TexCoord1Index, TexCoord2Index, TexCoord3Index);

                fAngle = fNextAngle;
            }
        }


        pPolygonMesh.setSmoothNormals(true);

        //  End the Batch.
        pPolygonMesh.endBatch();


        return(pPolygonMesh);
    }

    //  Creates an PolygonMesh containing a Cylinder.
    static public  PPolygonMesh createCylinder(String meshName,
                                            Vector3f Bottom,
                                            float fHeight,
                                            int NumberOfSlices,
                                            int NumberOfStacks,
                                            float fTopRadius,
                                            float fBottomRadius,
                                            boolean bCapTop,
                                            boolean bCapBottom,
                                            ColorRGBA Diffuse)
    {
        PPolygonMesh            pPolygonMesh;
        Vector3f		StackTopCenter          = new Vector3f();
        Vector3f		StackBottomCenter       = new Vector3f();
        float			fStackStep;
        int			SliceIndex;
        int			StackIndex;
        float			fAngle;
        float			fNextAngle;
        float			fAngleStep;
        Vector3f		TopPoint1               = new Vector3f();
        Vector3f		TopPoint2               = new Vector3f();
        Vector3f		BottomPoint1            = new Vector3f();
        Vector3f		BottomPoint2            = new Vector3f();
        int			Position1Index;
        int			Position2Index;
        int			Position3Index;
        int			Position4Index;
        int			DiffuseIndex;
        int			TexCoord1Index;
        int			TexCoord2Index;
        int			TexCoord3Index;
        int                     TexCoord4Index;
        Vector2f                TexCoord                = new Vector2f(0.0f, 0.0f);
        float			fStackTopRadius;
        float			fStackBottomRadius;
        float			fStackRadiusStep;

        pPolygonMesh = new PPolygonMesh(meshName);

        //  Begin the Batch.
        pPolygonMesh.beginBatch();


        DiffuseIndex   = pPolygonMesh.getColor(Diffuse);
        TexCoord1Index = pPolygonMesh.getTexCoord(TexCoord);
        TexCoord2Index = TexCoord1Index;
        TexCoord3Index = TexCoord1Index;
        TexCoord4Index = TexCoord1Index;


        if (NumberOfSlices < 3)
            NumberOfSlices = 3;

        if (NumberOfStacks < 1)
            NumberOfStacks = 1;

        fStackStep = fHeight / (float)NumberOfStacks;
        fAngleStep = (float)(2.0f * Math.PI) / (float)NumberOfSlices;

        fStackRadiusStep = (fTopRadius - fBottomRadius) / (float)NumberOfStacks;


        //  **********************
        //  Cap the bottom of the cylinder.
        //  **********************
        if (bCapBottom)
        {
            StackBottomCenter.set(Bottom);

            fStackBottomRadius = fBottomRadius;

            fAngle = 0.0f;

            TopPoint1.set(StackBottomCenter);

            Position1Index = pPolygonMesh.getPosition(TopPoint1);

            for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
            {
                fNextAngle = fAngle + fAngleStep;

                BottomPoint1.x = StackBottomCenter.x + (float)(Math.cos(fNextAngle) * fStackBottomRadius);
                BottomPoint1.y = StackBottomCenter.y;
                BottomPoint1.z = StackBottomCenter.z + (float)(Math.sin(fNextAngle) * fStackBottomRadius);

                BottomPoint2.x = StackBottomCenter.x + (float)(Math.cos(fAngle) * fStackBottomRadius);
                BottomPoint2.y = StackBottomCenter.y;
                BottomPoint2.z = StackBottomCenter.z + (float)(Math.sin(fAngle) * fStackBottomRadius);

                Position2Index = pPolygonMesh.getPosition(BottomPoint1);
                Position3Index = pPolygonMesh.getPosition(BottomPoint2);

                pPolygonMesh.addTriangle(Position1Index, Position2Index, Position3Index,
                                         DiffuseIndex,
                                         TexCoord1Index, TexCoord2Index, TexCoord3Index);

                fAngle = fNextAngle;
            }
        }



        //  **********************
        //  Draw the middle part of the cylinder.
        //  **********************
        StackBottomCenter.set(Bottom);
        StackTopCenter.set(StackBottomCenter);
        StackTopCenter.y	+= fStackStep;

        fStackBottomRadius	= fBottomRadius;
        fStackTopRadius		= fStackBottomRadius + fStackRadiusStep;


        for (StackIndex=0; StackIndex<NumberOfStacks; StackIndex++)
        {
            fAngle = 0.0f;

            for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
            {
                fNextAngle = fAngle + fAngleStep;

                BottomPoint1.x = StackBottomCenter.x + (float)(Math.cos(fAngle) * fStackBottomRadius);
                BottomPoint1.y = StackBottomCenter.y;
                BottomPoint1.z = StackBottomCenter.z + (float)(Math.sin(fAngle) * fStackBottomRadius);

                BottomPoint2.x = StackBottomCenter.x + (float)(Math.cos(fNextAngle) * fStackBottomRadius);
                BottomPoint2.y = StackBottomCenter.y;
                BottomPoint2.z = StackBottomCenter.z + (float)(Math.sin(fNextAngle) * fStackBottomRadius);

                TopPoint1.x = StackTopCenter.x + (float)(Math.cos(fNextAngle) * fStackTopRadius);
                TopPoint1.y = StackTopCenter.y;
                TopPoint1.z = StackTopCenter.z + (float)(Math.sin(fNextAngle) * fStackTopRadius);

                TopPoint2.x = StackTopCenter.x + (float)(Math.cos(fAngle) * fStackTopRadius);
                TopPoint2.y = StackTopCenter.y;
                TopPoint2.z = StackTopCenter.z + (float)(Math.sin(fAngle) * fStackTopRadius);


                Position1Index = pPolygonMesh.getPosition(BottomPoint1);
                Position2Index = pPolygonMesh.getPosition(BottomPoint2);
                Position3Index = pPolygonMesh.getPosition(TopPoint1);
                Position4Index = pPolygonMesh.getPosition(TopPoint2);

                pPolygonMesh.addQuad(Position1Index, Position2Index, Position3Index, Position4Index,
                                     DiffuseIndex,
                                     TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

                fAngle += fAngleStep;
            }

            fStackBottomRadius	= fStackTopRadius;
            fStackTopRadius	= fStackTopRadius + fStackRadiusStep;

            StackBottomCenter.set(StackTopCenter);
            StackTopCenter.y += fStackStep;
        }



        //  **********************
        //  Cap the top of the cylinder.
        //  **********************
        if (bCapTop)
        {
            StackTopCenter.set(Bottom);
            StackTopCenter.y += fHeight;

            fStackTopRadius = fTopRadius;

            fAngle = 0.0f;

            TopPoint1.set(StackTopCenter);

            Position1Index = pPolygonMesh.getPosition(TopPoint1);

            for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
            {
                fNextAngle = fAngle + fAngleStep;

                BottomPoint1.x = StackTopCenter.x + (float)(Math.cos(fAngle) * fStackTopRadius);
                BottomPoint1.y = StackTopCenter.y;
                BottomPoint1.z = StackTopCenter.z + (float)(Math.sin(fAngle) * fStackTopRadius);

                BottomPoint2.x = StackTopCenter.x + (float)(Math.cos(fNextAngle) * fStackTopRadius);
                BottomPoint2.y = StackTopCenter.y;
                BottomPoint2.z = StackTopCenter.z + (float)(Math.sin(fNextAngle) * fStackTopRadius);

                Position2Index = pPolygonMesh.getPosition(BottomPoint1);
                Position3Index = pPolygonMesh.getPosition(BottomPoint2);

                pPolygonMesh.addTriangle(Position1Index, Position2Index, Position3Index,
                                         DiffuseIndex,
                                         TexCoord1Index, TexCoord2Index, TexCoord3Index);

                fAngle = fNextAngle;
            }
        }


        pPolygonMesh.setSmoothNormals(true);
        
        //  End the Batch.
        pPolygonMesh.endBatch();

        pPolygonMesh.flipNormals(); // TODO need a fix!
        
        return(pPolygonMesh);
    }

}




