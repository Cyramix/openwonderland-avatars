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
package imi.scene.polygonmodel.parts.polygon;

import imi.scene.polygonmodel.parts.*;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.utils.PMathUtils;
import java.io.Serializable;
import javolution.util.FastTable;

public class PPolygon implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /**
     * If vertex colors are not given this default color will be used.
     */
    static private   ColorRGBA      m_DefaultColor  = new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f);
    private          PPolygonMesh   m_pPolygonMesh  = null;
    private          Vector3f       m_Normal        = new Vector3f();
    private          Vector3f       m_Center        = new Vector3f();

    private FastTable<PPolygonVertexIndices> m_Vertices = new FastTable();

    //  Constructor.
    public PPolygon()
    {
    }
    
    public PPolygon(PPolygon RHS)
    {
        set(RHS);
    }
    
    public void set(PPolygon RHS)
    {
        m_Normal.set(RHS.m_Normal);
        m_Center.set(RHS.m_Center);
        // Copy all the vertices
        m_Vertices.clear();
        for (PPolygonVertexIndices vert : RHS.m_Vertices)
            m_Vertices.add(new PPolygonVertexIndices(vert));   
    }
    
    public PPolygon(PPolygon RHS, boolean bSkinned)
    {
        if (!bSkinned)
        {
            set(RHS);
            return;
        }

        m_Normal   = new Vector3f(RHS.m_Normal);
        m_Center   = new Vector3f(RHS.m_Center);
        // Copy all the vertices
        for (PPolygonVertexIndices vert : RHS.m_Vertices)
            m_Vertices.add(new PPolygonSkinnedVertexIndices(vert));
    }

    public PPolygon(PPolygonMesh pPolygonMesh)
    {
        setPolygonMesh(pPolygonMesh);
    }
    
    public void flipNormals()
    {
        int size = m_Vertices.size();
        int iterations = size / 2;
        for (int i = 0; i < iterations; i++)
        {
            PPolygonVertexIndices swap = m_Vertices.get(i);
            m_Vertices.set(i, m_Vertices.get(size - 1 - i));
            m_Vertices.set(size - 1 - i, swap);
        }
        
        calculateNormal();
    }

    public Vector3f getCenter()
    {
        return m_Center;
    }
    
    public Vector3f getNormal()
    {
        return m_Normal;
    }

    //  Gets the PolygonMesh.
    public PPolygonMesh getPolygonMesh()
    {
        return(m_pPolygonMesh);
    }

    public void setColor(ColorRGBA color) {
        m_DefaultColor.set(color);
    }

    public void setNormal(Vector3f normal) {
        m_Normal.set(normal);
    }

    //  Sets the PolygonMesh.
    public void setPolygonMesh(PPolygonMesh pPolygonMesh)
    {
        m_pPolygonMesh = pPolygonMesh;
    }

    //  Clears the Polygon.
    public void clear()
    {
        m_pPolygonMesh  = null;
        m_Vertices.clear();
        m_Normal = Vector3f.ZERO;
        m_Center = Vector3f.ZERO;
    }

    //  Begins a batch.
    public void beginBatch()
    {
    }

    //  Ends a batch.
    public void endBatch()
    {
        //  Calculate the Normal.
        calculateNormal();

        //  Calculate the Middle.
        calculateMiddle();
    }

    //  Gets the Position at the specified triangleIndex.
    public boolean getPosition(int index, Vector3f pPosition)
    {
        PPolygonVertexIndices pVertice = getVertex(index);
	if (pVertice != null)
	{
            PPolygonPosition pMeshPosition = m_pPolygonMesh.getPosition(pVertice.m_PositionIndex);
            pPosition.set(pMeshPosition.m_Position);

            return(true);
        }

        return(false);
    }

    public FastTable<PPolygonVertexIndices> getVertexCollection()
    {
        return m_Vertices;
    }
    
    //  Retrieves the vertex at the specified index.
    public PPolygonVertexIndices getVertex(int index)
    {
        return(m_Vertices.get(index));
    }

    //  Retrieves the number of vertices making up the Polygon.
    public int getVertexCount()
    {
        return(m_Vertices.size());
    }

    //  Checks to see if the Polygon is using the specified PositionIndex.
    public boolean isUsingPositionIndex(int PositionIndex)
    {
        for (PPolygonVertexIndices vert : m_Vertices)
        {
            if (vert.m_PositionIndex == PositionIndex)
                return true;
        }
        return false;
    }

    //  Calculates the Normal.
    public void calculateNormal()
    {
        m_Normal.zero();

        if (m_Vertices.size() <3 ) 
            return;

        PPolygonVertexIndices         pVertice1;
        PPolygonVertexIndices         pVertice2;
        PPolygonVertexIndices         pVertice3;
        PPolygonPosition    pPosition1;
        PPolygonPosition    pPosition2;
        PPolygonPosition    pPosition3;


        pVertice1 = (PPolygonVertexIndices)m_Vertices.get(0);
        pVertice2 = (PPolygonVertexIndices)m_Vertices.get(1);
        pVertice3 = (PPolygonVertexIndices)m_Vertices.get(2);

        //  Get the Positions for the first 3 vertices making up the Polygon.
        pPosition1 = m_pPolygonMesh.getPosition(pVertice1.m_PositionIndex);
        pPosition2 = m_pPolygonMesh.getPosition(pVertice2.m_PositionIndex);
        pPosition3 = m_pPolygonMesh.getPosition(pVertice3.m_PositionIndex);

        //  Calculate the normal of the Polygon.
        PMathUtils.calculateNormalOfTriangle(m_Normal,
					 pPosition1.m_Position,
					 pPosition2.m_Position,
					 pPosition3.m_Position);
    }

    //  Calculates the Middle.
    public void calculateMiddle()
    {
        m_Center.zero();

        int                     VerticeCount = m_Vertices.size();
        int                     a;
        PPolygonVertexIndices         pVertice;
        PPolygonPosition    pPosition;

        for (a=0; a<VerticeCount; a++)
        {
            pVertice  = (PPolygonVertexIndices)m_Vertices.get(a);
            pPosition = m_pPolygonMesh.getPosition(pVertice.m_PositionIndex);

            m_Center.add(pPosition.m_Position, m_Center);
        }

	m_Center = m_Center.divide((float)VerticeCount);
    }

    public void addVertex(PPolygonVertexIndices polygonIndices)
    {
        if (polygonIndices != null)
            m_Vertices.add(polygonIndices);
    }
    
    //  Adds a Vertice to the EditablePolygon.
    public void addVertex(int PositionIndex,
                           int NormalIndex,
			   int TexCoord1Index,
			   int TexCoord2Index,
			   int TexCoord3Index,
			   int TexCoord4Index)
    {
        PPolygonVertexIndices pVertice = new PPolygonVertexIndices();

	pVertice.initialize(PositionIndex,
                            NormalIndex,
                            TexCoord1Index,
                            TexCoord2Index,
                            TexCoord3Index,
                            TexCoord4Index);

	m_Vertices.add(pVertice);
    }
    
    /**
     * Adds a new vertex with color index data
     * @param PositionIndex
     * @param ColorIndex
     * @param NormalIndex
     * @param TexCoord1Index
     * @param TexCoord2Index
     * @param TexCoord3Index
     * @param TexCoord4Index
     */
    public void addVertex(int PositionIndex,
                           int ColorIndex,
                           int NormalIndex,
			   int TexCoord1Index,
			   int TexCoord2Index,
			   int TexCoord3Index,
			   int TexCoord4Index)
    {
        PPolygonVertexIndices pVertex = new PPolygonVertexIndices();

	pVertex.initialize(PositionIndex,
                            ColorIndex,
                            NormalIndex,
                            TexCoord1Index,
                            TexCoord2Index,
                            TexCoord3Index,
                            TexCoord4Index);

	m_Vertices.add(pVertex);
    }

    public void addVertex(Vector3f pPosition)
    {
        PPolygonVertexIndices pVertice = new PPolygonVertexIndices();

	int PositionIndex = m_pPolygonMesh.getPosition(pPosition);

	pVertice.initialize(PositionIndex, -1, -1, -1, -1, -1);

	m_Vertices.add(pVertice);
    }

    public void addVertex(Vector3f pPosition,
                           Vector2f pTexCoord1)
    {
        PPolygonVertexIndices pVertice = new PPolygonVertexIndices();

	int PositionIndex = m_pPolygonMesh.getPosition(pPosition);
	int TexCoord1Index = m_pPolygonMesh.getTexCoord(pTexCoord1);

        pVertice.initialize(PositionIndex, -1, TexCoord1Index, -1, -1, -1);

	m_Vertices.add(pVertice);
    }

    public void addVertex(Vector3f pPosition,
			   Vector2f pTexCoord1,
                           Vector2f pTexCoord2)
    {
	PPolygonVertexIndices pVertice = new PPolygonVertexIndices();

	int PositionIndex = m_pPolygonMesh.getPosition(pPosition);
	int TexCoord1Index = m_pPolygonMesh.getTexCoord(pTexCoord1);
	int TexCoord2Index = m_pPolygonMesh.getTexCoord(pTexCoord2);

	pVertice.initialize(PositionIndex, -1, TexCoord1Index, -1, -1, -1);

	m_Vertices.add(pVertice);
    }

    public void addVertex(Vector3f pPosition,
                           Vector2f pTexCoord1,
                           Vector2f pTexCoord2,
                           Vector2f pTexCoord3)
    {
        PPolygonVertexIndices pVertice = new PPolygonVertexIndices();

	int PositionIndex = m_pPolygonMesh.getPosition(pPosition);
	int TexCoord1Index = m_pPolygonMesh.getTexCoord(pTexCoord1);
	int TexCoord2Index = m_pPolygonMesh.getTexCoord(pTexCoord2);
	int TexCoord3Index = m_pPolygonMesh.getTexCoord(pTexCoord3);

	pVertice.initialize(PositionIndex, -1, TexCoord1Index, TexCoord2Index, TexCoord3Index, -1);

	m_Vertices.add(pVertice);
    }

    public void addVertex(Vector3f pPosition,
                           Vector2f pTexCoord1,
                           Vector2f pTexCoord2,
                           Vector2f pTexCoord3,
                           Vector2f pTexCoord4)
    {
	PPolygonVertexIndices pVertice = new PPolygonVertexIndices();

	int PositionIndex = m_pPolygonMesh.getPosition(pPosition);
	int TexCoord1Index = m_pPolygonMesh.getTexCoord(pTexCoord1);
	int TexCoord2Index = m_pPolygonMesh.getTexCoord(pTexCoord2);
	int TexCoord3Index = m_pPolygonMesh.getTexCoord(pTexCoord3);
	int TexCoord4Index = m_pPolygonMesh.getTexCoord(pTexCoord4);

	pVertice.initialize(PositionIndex, -1, TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);

        m_Vertices.add(pVertice);
    }

    //  Gets the number of triangles making up the Polygon.
    public int getTriangleCount()
    {
        return(m_Vertices.size()-2);
    }

    //  Fills in 'pTriangle' so it represents a triangle making up
    //  the polygon.
    public void getTriangle(int triangleIndex, PGeometryTriangle pTriangle)
    {
        if (triangleIndex == 0)
	{
            getVertex(0, pTriangle.m_Vertices[0]);
            getVertex(1, pTriangle.m_Vertices[1]);
            getVertex(2, pTriangle.m_Vertices[2]);
        }
        else
        {
            getVertex(triangleIndex+1, pTriangle.m_Vertices[0]);
            getVertex(triangleIndex+2, pTriangle.m_Vertices[1]);
            getVertex(0, pTriangle.m_Vertices[2]);
        }
    }

    //  Gets the Vertice at the specified index.
    public void getVertex(int Index, PGeometryVertex pVertice)
    {
        if (pVertice == null)
                return;
        
        PPolygonVertexIndices pPolygonVertice = (PPolygonVertexIndices)m_Vertices.get(Index);

        PPolygonPosition    pPosition   = m_pPolygonMesh.getPosition(pPolygonVertice.m_PositionIndex);
        PPolygonNormal      pNormal     = m_pPolygonMesh.getNormal(pPolygonVertice.m_NormalIndex);
        PPolygonColor       pColor      = null;
        
        if (pPolygonVertice.m_ColorIndex != -1) // Has this been set yet?
            pColor = m_pPolygonMesh.getColor(pPolygonVertice.m_ColorIndex);
        
	PPolygonTexCoord[]  pTexCoords = new PPolygonTexCoord[8];
        
	int i = 0; // Iteration counter
        
        for (i=0; i < m_pPolygonMesh.getNumberOfTexCoords(); i++)
        {
            if (    pPolygonVertice.m_TexCoordIndex[i] != -1 &&
                    pPolygonVertice.m_TexCoordIndex[i] < m_pPolygonMesh.getTexCoordsRef().size())
                pTexCoords[i] = m_pPolygonMesh.getTexCoord(pPolygonVertice.m_TexCoordIndex[i]);
            else
                pTexCoords[i] = null;
	}
        // fill up any remainders
        while (i < 8)
        {
            pTexCoords[i] = null;
            i++;
        }

        //  Setup the 'Vertice' struct.
	pVertice.clear();

        pVertice.m_Position.set(pPosition.m_Position);
        pVertice.m_Normal.set(pNormal.m_Normal);
        if (pColor != null)
            pVertice.m_Diffuse.set(pColor.m_Color.r, pColor.m_Color.g, pColor.m_Color.b, 1.0f);
        else // Use a default color
            pVertice.m_Diffuse.set(m_DefaultColor);
        pVertice.m_Specular.set(1.0f, 1.0f, 1.0f, 1.0f);

        for (i=0; i<8; i++)
        {
            if (pTexCoords[i] != null)
                pVertice.m_TexCoords[i].set(pTexCoords[i].m_TexCoord);
	}
    }

    //  Gets the position of the vertice at the specified index.
    public void getVertexPosition(int Index, Vector3f pVerticePosition)
    {
        PPolygonVertexIndices pVertice = (PPolygonVertexIndices)m_Vertices.get(Index);
        if (pVertice == null)
            return;

        PPolygonPosition pPosition = m_pPolygonMesh.getPosition(pVertice.m_PositionIndex);

        pVerticePosition.set(pPosition.m_Position);
    }

    //  Gets the normal of the vertice at the specified index.
    public void getVertexNormal(int Index, Vector3f pVerticeNormal)
    {
        PPolygonVertexIndices pVertice = (PPolygonVertexIndices)m_Vertices.get(Index);
        if (pVertice == null)
            return;

        PPolygonNormal pNormal = m_pPolygonMesh.getNormal(pVertice.m_NormalIndex);

        pVerticeNormal.set(pNormal.m_Normal);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PPolygon other = (PPolygon) obj;
        if (this.m_Vertices != other.m_Vertices && (this.m_Vertices == null || !this.m_Vertices.equals(other.m_Vertices))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.m_Vertices != null ? this.m_Vertices.hashCode() : 0);
        return hash;
    }

  
    public void dump(String spacing)
    {
        int b;
        PPolygonVertexIndices pPolygonVertice = null;
        PPolygonSkinnedVertexIndices pPolygonSkinnedVertice = null;

        System.out.println(spacing + "Normal:  (" + m_Normal.x + ", " + m_Normal.y + ", " + m_Normal.z + ")");
        System.out.println(spacing + "Middle:  (" + m_Center.x + ", " + m_Center.y + ", " + m_Center.z + ")");
        System.out.println(spacing + "Vertices:  " + getVertexCount());

        for (b=0; b<getVertexCount(); b++)
        {
            pPolygonVertice = getVertex(b);

            System.out.print(spacing + "   Vertice[" + b + "]:  ");
            System.out.print("P=" + pPolygonVertice.m_PositionIndex + ", ");
            System.out.print("N=" + pPolygonVertice.m_NormalIndex + ", ");
            System.out.print("T0=" + pPolygonVertice.m_TexCoordIndex[0] + ", ");
            System.out.print("T1=" + pPolygonVertice.m_TexCoordIndex[1] + ", ");
            System.out.print("T2=" + pPolygonVertice.m_TexCoordIndex[2] + ", ");
            System.out.print("T3=" + pPolygonVertice.m_TexCoordIndex[3] + ", ");
 
            if (pPolygonVertice instanceof PPolygonSkinnedVertexIndices)
            {
                pPolygonSkinnedVertice = (PPolygonSkinnedVertexIndices)pPolygonVertice;

                System.out.print("BWI=" + pPolygonSkinnedVertice.m_BoneWeightIndex + ", ");
                System.out.print("BII=" + pPolygonSkinnedVertice.m_BoneIndicesIndex + ", ");
            }

            System.out.println("");
        }
    }



/*
//  ******************************
//  Render methods.
//  ******************************

//  Renders the Polygon.
void CPolygon::Render(CGraphicContext &dc)
{
	int				a;
	int				TriangleCount = m_Vertices.GetCount() - 2;
	CPolygonVertice	*pVertice1;
	CPolygonVertice	*pVertice2;
	CPolygonVertice	*pVertice3;

	dc.SetMaterial(m_pMaterial);

	//  Render the first triangle.
	pVertice1 = (CPolygonVertice *)m_Vertices.Get(0);
	pVertice2 = (CPolygonVertice *)m_Vertices.Get(1);
	pVertice3 = (CPolygonVertice *)m_Vertices.Get(2);

	RenderTriangle(dc, pVertice1, pVertice2, pVertice3);

	//  Render the remaining triangles.
	if (TriangleCount > 1)
	{
		for (a=2; a<TriangleCount+1; a++)
		{
			pVertice2 = (CPolygonVertice *)m_Vertices.Get(a);
			pVertice3 = (CPolygonVertice *)m_Vertices.Get(a+1);

			RenderTriangle(dc, pVertice1, pVertice2, pVertice3);
		}
	}
}



//  Renders Polygon Normal.
void CPolygon::RenderPolygonNormal(CGraphicContext &dc)
{
//	//  Batch Render the Polygon's Normal.
//	g_pBlr->RenderNormal(m_Center,
//						 m_Normal,
//						 g_EditableSceneRenderInfo.m_fPolygonNormalLength,
//						 g_EditableSceneRenderInfo.m_PolygonNormalStartColor,
//						 g_EditableSceneRenderInfo.m_PolygonNormalEndColor);
}


//  Renders Polygon Middle.
void CPolygon::RenderPolygonMiddle(CGraphicContext &dc)
{
//	//  Batch Renders a Point.
//	g_pBpr->RenderPoint(m_Center,
//						g_EditableSceneRenderInfo.m_PolygonMiddleColor);
}


//  Renders Polygon Vertice Normals.
void CPolygon::RenderPolygonVerticeNormals(CGraphicContext &dc)
{
	int						a;
	CPolygonVertice			*pVertice;
	CPolygonMeshPosition	*pPosition;
	CPolygonMeshNormal		*pNormal;

	for (a=0; a<m_Vertices.GetCount(); a++)
	{
		pVertice = (CPolygonVertice *)m_Vertices.FastGet(a);

		pPosition = m_pPolygonMesh->GetPosition(pVertice->m_PositionIndex);
		pNormal   = m_pPolygonMesh->GetNormal(pVertice->m_NormalIndex);


//		//  Batch Render the Vertice's Normal.
//		g_pBlr->RenderNormal(pPosition->m_RealtimePosition,
//							 pNormal->m_RealtimeNormal,
//							 g_EditableSceneRenderInfo.m_fPolygonVerticeNormalLength,
//							 g_EditableSceneRenderInfo.m_PolygonVerticeNormalStartColor,
//							 g_EditableSceneRenderInfo.m_PolygonVerticeNormalEndColor);
	}
}


//  Renders Polygon Vertices.
void CPolygon::RenderPolygonVertices(CGraphicContext &dc)
{
	int						a;
	CPolygonVertice			*pVertice;
	CPolygonMeshPosition	*pPosition;

	for (a=0; a<m_Vertices.GetCount(); a++)
	{
		pVertice = (CPolygonVertice *)m_Vertices.FastGet(a);

		pPosition = m_pPolygonMesh->GetPosition(pVertice->m_PositionIndex);

//		//  Batch Render the position.
//		g_pBpr->RenderPoint(pPosition->m_RealtimePosition,
//							g_EditableSceneRenderInfo.m_PolygonVerticeColor);
	}
}
*/
    




/*
//  ******************************
//  Collision methods.
//  ******************************

//  Checks to see if the Polygon is colliding with the specified Line.
bool CPolygon::IsColliding(CLine &Line)
{
	CVector PointOfIntersection;

	if (GetPointOfIntersectionWithPlane(Line, PointOfIntersection))
	{
		if (IsPointWithinEdges(PointOfIntersection))
			return(true);
	}

	return(false);
}


//  Checks to see if the Polygon is colliding with the specified Region.
bool CPolygon::IsColliding(CArbitraryRegion &Region)
{
	int		a;
	CVector Position;

	for (a=0; a<m_Vertices.GetCount(); a++)
	{
		GetPosition(a, Position);

		//  Check to see the Polygon's Position is colliding with the Region.
		if (Region.IsColliding(Position))
			return(true);
	}


	CVector Position1;
	CVector Position2;

	GetPosition(0, Position1);

	for (a=1; a<m_Vertices.GetCount(); a++)
	{
		GetPosition(a, Position2);

		//  Check to see the Edge is colliding with the Region.
		if (Region.IsColliding(Position1, Position2))
			return(true);

		Position1 = Position2;
	}

	GetPosition(0, Position2);

	//  Check to see the Edge is colliding with the Region.
	if (Region.IsColliding(Position1, Position2))
		return(true);

	return(false);
}
*/




/*

//  Renders a Triangle.
void CPolygon::RenderTriangle(CGraphicContext &dc,
							  CPolygonVertice *pVertice1,
							  CPolygonVertice *pVertice2,
							  CPolygonVertice *pVertice3)
{
	CPolygonMeshPosition *pPosition1 = m_pPolygonMesh->GetPosition(pVertice1->m_PositionIndex);
	CPolygonMeshPosition *pPosition2 = m_pPolygonMesh->GetPosition(pVertice2->m_PositionIndex);
	CPolygonMeshPosition *pPosition3 = m_pPolygonMesh->GetPosition(pVertice3->m_PositionIndex);
	CPolygonMeshNormal   *pNormal1   = m_pPolygonMesh->GetNormal(pVertice1->m_NormalIndex);
	CPolygonMeshNormal   *pNormal2   = m_pPolygonMesh->GetNormal(pVertice2->m_NormalIndex);
	CPolygonMeshNormal   *pNormal3   = m_pPolygonMesh->GetNormal(pVertice3->m_NormalIndex);
	CPolygonMeshTexCoord *pTexCoord1 = m_pPolygonMesh->GetTexCoord(pVertice1->m_TexCoordIndex[0]);
	CPolygonMeshTexCoord *pTexCoord2 = m_pPolygonMesh->GetTexCoord(pVertice2->m_TexCoordIndex[0]);
	CPolygonMeshTexCoord *pTexCoord3 = m_pPolygonMesh->GetTexCoord(pVertice3->m_TexCoordIndex[0]);

	//  Batch Renders a Textured Triangle.
	dc.BatchRenderTexturedTriangle(pPosition1->m_RealtimePosition,
								   pPosition2->m_RealtimePosition,
								   pPosition3->m_RealtimePosition,
								   pNormal1->m_RealtimeNormal,
								   pNormal2->m_RealtimeNormal,
								   pNormal3->m_RealtimeNormal,
								   CColor::white,
								   CColor::white,
								   CColor::white,
								   pTexCoord1->m_RealtimeTexCoord,
								   pTexCoord2->m_RealtimeTexCoord,
								   pTexCoord3->m_RealtimeTexCoord);
}






//  Checks to see if the Polygon is colliding with the specified Line.
bool CPolygon::GetPointOfIntersectionWithPlane(CLine &Line,
											   CVector &PointOfIntersection)
{
	float	fFraction		= 1000.0f;
	CVector Position1;

	GetPosition(0, Position1);

	CPlane  PolygonPlane(Position1, m_Normal);

	//  Get the Percentage along the line that a collision occurs.
	if (PolygonPlane.GetPercentageAlongLine(Line, fFraction))
	{
		//  Get the PointOfIntersection along the Line.
		PolygonPlane.GetPointOfIntersectionAlongLine(Line, fFraction, PointOfIntersection);

		return(true);
	}

	return(false);
}


//  Checks to see if the specified Point is within the edges.
bool CPolygon::IsPointWithinEdges(const CVector &Point)
{
	CVector EdgePlaneNormal;
	CVector Position1, Position2, PositionDir;
	CPlane  EdgePlane;
	int		a;
	int		Count			= m_Vertices.GetCount();


	GetPosition(0, Position1);

	for (a=1; a<Count; a++)
	{
		GetPosition(a, Position2);

		PositionDir = Position2 - Position1;
		PositionDir.Normalize();

		CVector::Cross(EdgePlaneNormal, m_Normal, PositionDir);

		EdgePlane.Set(Position1, EdgePlaneNormal);

        //  Classifies a Point.
		if (EdgePlane.ClassifyPoint(Point) == POINT_BEHIND_PLANE)
			return(false);

		Position1 = Position2;
	}

	GetPosition(0, Position2);

	PositionDir = Position2 - Position1;
	PositionDir.Normalize();

	CVector::Cross(EdgePlaneNormal, m_Normal, PositionDir);

	EdgePlane.Set(Position1, EdgePlaneNormal);

	//  Classifies a Point.
	if (EdgePlane.ClassifyPoint(Point) == POINT_BEHIND_PLANE)
		return(false);


	return(true);
}
*/


}
        
        
        
        
