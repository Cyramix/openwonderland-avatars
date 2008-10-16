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
package imi.scene.utils;

import com.jme.bounding.BoundingSphere;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.boundingvolumes.PCube;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Line;
import com.jme.scene.Point;
import com.jme.scene.state.ZBufferState;
import com.jme.util.geom.BufferUtils;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModel;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Lou Hayt
 * @author Chris Nagle
 */
public class PRenderer 
{
    private Renderer    m_Renderer              = null;
    
    private boolean     m_bDrawOrigin           = true;
    private boolean     m_bRenderPolygonCenters = false;
    private boolean     m_bRenderPolygonNormals = false;
    private boolean     m_bRenderVertexNormals  = false;
    private boolean     m_bRenderBoundingBox    = true;
    private boolean     m_bRenderBoundingSphere = true;
    private boolean     m_bRenderMesh           = true;


    public enum FillMode {    WireFrame     }
    private FillMode    m_FillMode              = FillMode.WireFrame;
    
    public enum BVMode   {    BV_Off, BV_Box, BV_Sphere     }
    private BVMode      m_BoundingVolumeMode    = BVMode.BV_Off;
    
    private Line        m_lines                 = new Line();   // wireframe mode
    private Point       m_points                = new Point();  // polygon centers
    private ColorRGBA   m_defaultColor          = new ColorRGBA(0.0f, 0.8f, 0.1f, 0.0f);
    
    PSphere boneSelectionSphere = new PSphere();
    
    // Render batch
    PMatrix             m_Origin              = new PMatrix();//null;
//    HashSet<PLine>      m_Positions           = new HashSet();    // see "PRenderer with HashSet.java" for that version... multi threading issues?
//    HashSet<PLine>      m_Centers           = new HashSet();
//    HashSet<PLine>      m_PolygonNormals           = new HashSet();
//    HashSet<PLine>      m_VertexNormals           = new HashSet();
    ArrayList<Vector3f> m_Positions      = new  ArrayList<Vector3f>();
    ArrayList<Vector3f> m_Centers        = new  ArrayList<Vector3f>();
    ArrayList<Vector3f> m_PolygonNormals = new  ArrayList<Vector3f>();
    ArrayList<Vector3f> m_VertexNormals  = new  ArrayList<Vector3f>();
    
    ArrayList<Vector3f> m_SkeletonBones  = new  ArrayList<Vector3f>();
    ArrayList<Vector3f> m_SkeletonX      = new  ArrayList<Vector3f>();
    ArrayList<Vector3f> m_SkeletonY      = new  ArrayList<Vector3f>();
    ArrayList<Vector3f> m_SkeletonZ      = new  ArrayList<Vector3f>();
    
    // Array for the present method
    Vector3f [] PositionsArray      = null;
    Vector3f [] CentersArray        = null;
    Vector3f [] PolygonNormalsArray = null;
    Vector3f [] VertexNormalsArray  = null;
    Vector3f [] YellowLinesArray    = null;
    Vector3f [] RedLinesArray       = null;
    Vector3f [] GreenLinesArray     = null;
    Vector3f [] BlueLinesArray      = null;
    
    public PRenderer() 
    {
        m_points.setPointSize(5.0f);
    }
    
    public void resetRenderer(Renderer r)
    {
        if (r != null && m_Renderer == null)
        {
            m_Renderer = r;
            
            ZBufferState buf = (ZBufferState) m_Renderer.createZBufferState();
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            
            m_lines.setRenderState(buf);
            m_points.setRenderState(buf);
            
            m_lines.setLineWidth(2);
            m_lines.setMode(Line.Mode.Segments);
            m_lines.setAntialiased(true);
            m_lines.setModelBound(new BoundingSphere());
            m_lines.updateModelBound();
            
            m_lines.setDefaultColor (m_defaultColor);
            m_points.setDefaultColor(m_defaultColor);
            
            m_lines.updateRenderState();
            m_points.updateRenderState();
        }
        
        m_Positions.clear();
        m_Centers.clear();
        m_PolygonNormals.clear();
        m_VertexNormals.clear();
        
        m_SkeletonBones.clear();
        m_SkeletonX.clear();
        m_SkeletonY.clear();
        m_SkeletonZ.clear();
        
        // draw origin
        m_SkeletonX.add(Vector3f.ZERO);
        m_SkeletonX.add(new Vector3f(1000.0f, 0.0f, 0.0f));
        m_SkeletonY.add(Vector3f.ZERO);
        m_SkeletonY.add(new Vector3f(0.0f, 1000.0f, 0.0f));
        m_SkeletonZ.add(Vector3f.ZERO);
        m_SkeletonZ.add(new Vector3f(0.0f, 0.0f, 1000.0f));
        present();
        // cleanup
        m_SkeletonX.clear();
        m_SkeletonY.clear();
        m_SkeletonZ.clear();
    }
    
    public void present() 
    {
        if (m_bRenderMesh && m_Positions.size() > 0)
        {
            if (PositionsArray == null || PositionsArray.length != m_Positions.size())
                PositionsArray      = new Vector3f [m_Positions.size()];
            m_Positions.toArray(PositionsArray);
            
            m_lines.reconstruct(BufferUtils.createFloatBuffer( PositionsArray  ), null, null, null);
            m_lines.draw(m_Renderer);
        }
        
        if (m_bRenderPolygonCenters && m_Centers.size() > 0)
        {
            if (CentersArray == null || CentersArray.length != m_Centers.size())
                CentersArray      = new Vector3f [m_Centers.size()];
            m_Centers.toArray(CentersArray);
            
            m_points.reconstruct(BufferUtils.createFloatBuffer( CentersArray  ), null, null, null);
            m_points.draw(m_Renderer);
        }
        
        if (m_bRenderPolygonNormals)
        {
            if (PolygonNormalsArray == null || PolygonNormalsArray.length != m_PolygonNormals.size())
                PolygonNormalsArray      = new Vector3f [m_PolygonNormals.size()];
            m_PolygonNormals.toArray(PolygonNormalsArray);
            
            m_lines.reconstruct(BufferUtils.createFloatBuffer( PolygonNormalsArray  ), null, null, null);
            m_lines.draw(m_Renderer);
        }
        
        if (m_bRenderVertexNormals)
        {
            if (VertexNormalsArray == null || VertexNormalsArray.length != m_VertexNormals.size())
                VertexNormalsArray      = new Vector3f [m_VertexNormals.size()];
            m_VertexNormals.toArray(VertexNormalsArray);
            
            m_lines.reconstruct(BufferUtils.createFloatBuffer( VertexNormalsArray  ), null, null, null);
            m_lines.draw(m_Renderer);
        }
        
        // Present skeletons
        if (!m_SkeletonX.isEmpty()) // m_SkeletonXYZ is also used to draw origin
        {
            // present bones
            if (!m_SkeletonBones.isEmpty())
            {
                if (YellowLinesArray == null || YellowLinesArray.length != m_SkeletonBones.size())
                    YellowLinesArray      = new Vector3f [m_SkeletonBones.size()];
                m_SkeletonBones.toArray(YellowLinesArray);

                m_lines.reconstruct(BufferUtils.createFloatBuffer( YellowLinesArray  ), null, null, null);
                m_lines.setDefaultColor (ColorRGBA.yellow);
                m_lines.draw(m_Renderer);
            }
            
            // present x axes
            if (RedLinesArray == null || RedLinesArray.length != m_SkeletonX.size())
                RedLinesArray      = new Vector3f [m_SkeletonX.size()];
            m_SkeletonX.toArray(RedLinesArray);
            
            m_lines.reconstruct(BufferUtils.createFloatBuffer( RedLinesArray  ), null, null, null);
            m_lines.setDefaultColor (ColorRGBA.red);
            m_lines.draw(m_Renderer);
            
            // present y axes
            if (GreenLinesArray == null || GreenLinesArray.length != m_SkeletonY.size())
                GreenLinesArray      = new Vector3f [m_SkeletonY.size()];
            m_SkeletonY.toArray(GreenLinesArray);
            
            m_lines.reconstruct(BufferUtils.createFloatBuffer( GreenLinesArray  ), null, null, null);
            m_lines.setDefaultColor (ColorRGBA.green);
            m_lines.draw(m_Renderer);
            
            // present z axes
            if (BlueLinesArray == null || BlueLinesArray.length != m_SkeletonZ.size())
                BlueLinesArray      = new Vector3f [m_SkeletonZ.size()];
            m_SkeletonZ.toArray(BlueLinesArray);
            
            m_lines.reconstruct(BufferUtils.createFloatBuffer( BlueLinesArray  ), null, null, null);
            m_lines.setDefaultColor (ColorRGBA.blue);
            m_lines.draw(m_Renderer);
            
            // return to default line color
            m_lines.setDefaultColor (m_defaultColor);
        }
        
        
    }
    
    public boolean isbDrawOrigin() {
        return m_bDrawOrigin;
    }

    public void setDrawOrigin(boolean bDrawOrigin) {
        m_bDrawOrigin = bDrawOrigin;
    }
    
    public Renderer getJMERenderer()
    {
        return m_Renderer;
    }

    public void setOrigin(PMatrix origin) 
    {
        m_Origin = origin;
        
//        m_lines.setLocalRotation(rotation);
//        m_lines.setLocalScale(scale);
//        m_lines.setLocalTranslation(translation);
//        
//        m_points.setLocalRotation(rotation);
//        m_points.setLocalScale(scale);
//        m_points.setLocalTranslation(translation);
    }
    
    public void drawPPolygonModel(PPolygonModel model)
    {
        if (m_FillMode == FillMode.WireFrame)
            drawPPolygonModel_Wireframe(model);
    }
    
    public void drawPPolygonMesh(PPolygonMesh mesh)
    {
        if (m_FillMode == FillMode.WireFrame)
            drawPPolygonMesh_Wireframe(mesh);
    }
    
    public void drawPPolygonSkinnedMesh(PPolygonSkinnedMesh mesh, PNode TransformHierarchy)
    {
        if (m_FillMode == FillMode.WireFrame)
            drawPPolygonMesh_Wireframe(mesh, TransformHierarchy);
    }
    
    public void drawPPolygonModel_Wireframe(PPolygonModel model)
    {
        // TODO draw bounding volumes
    }
    
    public void drawPPolygonMesh_Wireframe(PPolygonSkinnedMesh mesh, PNode TransformHierarchy)
    {
        drawPPolygonMesh_Wireframe(mesh);
        if (TransformHierarchy.getChildrenCount() > 0)
        {
            for (PNode kid : TransformHierarchy.getChildren())
                drawBones((PJoint)kid);
        }
        else
            System.out.println("PRenderer - No TransformHierarchy to draw.");
    }
    
    public void drawPPolygonMesh_Wireframe(PPolygonMesh mesh)
    {
        // TODO : check possible optimization in a similar way to drawSphere()'s use of HashSet
        // removing duplicate lines in the cost of another memory copy, that will also mean
        // lesss draw calls but performing the flatenning of the hierarchy CPU side
        
         for (int i = 0; i < mesh.getPolygonCount(); i++)
         {
             drawPPolygon_Wireframe(mesh.getPolygon(i), mesh);
             
//             if (m_bRenderPolygonCenters)
//             {
//                 Vector3f center = new Vector3f(mesh.getPolygon(i).getCenter());
//                 m_Origin.transformPoint(center);
//                 m_Centers.add(center);
//             }
            
             if (m_bRenderPolygonNormals)
             {
                 Vector3f normal = new Vector3f(mesh.getPolygon(i).getCenter());
                 m_Origin.transformPoint(normal);
                 m_PolygonNormals.add(normal);
                 
                 normal = new Vector3f(mesh.getPolygon(i).getCenter().add(mesh.getPolygon(i).getNormal()));
                 m_Origin.transformPoint(normal);
                 m_PolygonNormals.add(normal);
             }
         }
        
        if (m_bRenderBoundingBox)
            drawCube(mesh.getBoundingCube());
        
        if (m_bRenderBoundingSphere)
            drawSphere(mesh.getBoundingSphere(), 5, 5, false);
         
         // this will only draw the first pose
         //if (mesh instanceof PPolygonSkinnedMesh)
         //    drawBones((PPolygonSkinnedMesh)mesh);
    }
    
    public void drawPPolygon_Wireframe(PPolygon poly, PPolygonMesh mesh)
    {
        try
        {
        int vertCount = poly.getVertexCount();

        int point1Index;
        int point2Index;
        Vector3f point1;
        Vector3f point2;
        
        point1Index = poly.getVertex(0).m_PositionIndex;
        point1 = mesh.getPosition(point1Index).m_Position;
        
        for (int i = 1; i < vertCount; i++)
        {   
            point2Index = poly.getVertex(i).m_PositionIndex;
            point2 = mesh.getPosition(point2Index).m_Position;
            
            Vector3f position = new Vector3f(point1);
            m_Origin.transformPoint(position);
            m_Positions.add(position);
            
            position = new Vector3f(point2);
            m_Origin.transformPoint(position);
            m_Positions.add(position);
         
            if (m_bRenderVertexNormals)
            {
                Vector3f normal = new Vector3f(point1);
                m_Origin.transformPoint(normal);
                m_VertexNormals.add(normal);
                
                normal = new Vector3f(point1.add( mesh.getNormal( poly.getVertex(i).m_NormalIndex ).m_Normal ));
                m_Origin.transformPoint(normal);
                m_VertexNormals.add(normal);
            }
            
            point1 = point2;
        }
        
        //  Add line connecting last vertice to first vertice.
        //  point1 will point to last vertice.
        point2Index = poly.getVertex(0).m_PositionIndex;
        point2 = mesh.getPosition(point2Index).m_Position;
        
        Vector3f position = new Vector3f(point1);
        m_Origin.transformPoint(position);
        m_Positions.add(position);
        
        
        position = new Vector3f(point2);
        m_Origin.transformPoint(position);
        m_Positions.add(position);
        }
        catch (Exception e)
        {
            
        }
    }
           
    public void setRenderPRendererMesh(boolean on)
    {
        m_bRenderMesh = on;
    }
    
    public void toggleRenderPRendererMesh() 
    {
         m_bRenderMesh = !m_bRenderMesh;
    }
    
    public void renderPolygonNormals(boolean on)
    {
        m_bRenderPolygonNormals =   on;
    }
    
    public void renderVertexNormals(boolean on)
    {
        m_bRenderVertexNormals  =   on;
    }
    
    public void renderPolygonCenters(boolean on)
    {
        m_bRenderPolygonCenters =   on;
    }
    
    public void renderBoundingBox(boolean on)
    {
        m_bRenderBoundingBox    =   on;
    }
    
    public void renderBoundingSphere(boolean on)
    {
        m_bRenderBoundingSphere =   on;
    }
    
    public void renderBoundingVolumeToggle()
    {
        switch (m_BoundingVolumeMode)
        {
            case BV_Off:
                m_BoundingVolumeMode    = BVMode.BV_Box;
                m_bRenderBoundingBox    = true;
                m_bRenderBoundingSphere = false;
                break;
            case BV_Box:
                m_BoundingVolumeMode    = BVMode.BV_Sphere;
                m_bRenderBoundingBox    = false;
                m_bRenderBoundingSphere = true;
                break;
            case BV_Sphere:
                m_BoundingVolumeMode    = BVMode.BV_Off;
                m_bRenderBoundingBox    = false;
                m_bRenderBoundingSphere = false;
                break;
        }
    }
    
    public boolean getRenderPolygonNormals()
    {
        return m_bRenderPolygonNormals;
    }
    
    public boolean getRenderVertexNormals()
    {
        return m_bRenderVertexNormals;
    }
    
    public boolean getRenderPolygonCenters()
    {
        return m_bRenderPolygonCenters;
    }
    
    public boolean getRenderBoundingBox()
    {
        return m_bRenderBoundingBox;
    }
    
    public boolean getRenderBoundingSphere()
    {
        return m_bRenderBoundingSphere;
    }
    
    public void setFillMode(FillMode mode)
    {
        m_FillMode = mode;
    }
    
    public FillMode getFillMode()
    {
        return m_FillMode;
    }
    
    public void drawCube(PCube cube)
    {
        Vector3f        Center              = cube.getCenter();
        
        Vector3f	Point1              = new Vector3f();
        Vector3f	Point2              = new Vector3f();
        Vector3f	Point3              = new Vector3f();
        Vector3f	Point4              = new Vector3f();
        Vector3f	Point5              = new Vector3f();
        Vector3f	Point6              = new Vector3f();
        Vector3f	Point7              = new Vector3f();
        Vector3f	Point8              = new Vector3f();
        
        float		fHalfWidth          = cube.getWidth()  * 0.5f;
        float		fHalfHeight	    = cube.getHeight() * 0.5f;
        float		fHalfDepth	    = cube.getDepth()  * 0.5f;
        
        Point1.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
        Point2.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
        Point3.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);
        Point4.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);

        Point5.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
        Point6.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
        Point7.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);
        Point8.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);
        
        //  'Top' Quad.
        transformAndAddToBatch(Point4, Point3);
        //m_Positions.add(Point4);
        //m_Positions.add(Point3);
        
        transformAndAddToBatch(Point3, Point2);
        //m_Positions.add(Point3);
        //m_Positions.add(Point2);
        
        transformAndAddToBatch(Point2, Point1);
        //m_Positions.add(Point2);
        //m_Positions.add(Point1);
        
        transformAndAddToBatch(Point1, Point4);
        //m_Positions.add(Point1);
        //m_Positions.add(Point4);
        
        //  'Left' Quad. (with duplicates removed)
        transformAndAddToBatch(Point5, Point8);
        //m_Positions.add(Point5);
        //m_Positions.add(Point8);
        
        transformAndAddToBatch(Point8, Point4);
        //m_Positions.add(Point8);
        //m_Positions.add(Point4);
        
        transformAndAddToBatch(Point1, Point5);
        //m_Positions.add(Point1);
        //m_Positions.add(Point5);
        
        //  'Back' Quad. (with duplicates removed)
        transformAndAddToBatch(Point6, Point5);
        //m_Positions.add(Point6);
        //m_Positions.add(Point5);
        
        transformAndAddToBatch(Point2, Point6);
        //m_Positions.add(Point2);
        //m_Positions.add(Point6);
        
        //  'Right' Quad. (with duplicates removed)
        transformAndAddToBatch(Point7, Point6);
        //m_Positions.add(Point7);
        //m_Positions.add(Point6);
        
        transformAndAddToBatch(Point3, Point7);
        //m_Positions.add(Point3);
        //m_Positions.add(Point7);
        
        //  'Front' Quad. (with duplicates removed)
        transformAndAddToBatch(Point8, Point7);
        //m_Positions.add(Point8);
        //m_Positions.add(Point7);
    }

    //  Draws a single triangle.
    public void drawTriangle(Vector3f point1, Vector3f point2, Vector3f point3)
    {
        transformAndAddToBatch(point1, point2);
        transformAndAddToBatch(point2, point3);
        transformAndAddToBatch(point3, point1);
    }

    private void transformAndAddToBatch(PLine line)
    {
        transformAndAddToBatch(line.m_point1, line.m_point2);
    }
    
    private void transformAndAddToBatch(Vector3f point1, Vector3f point2)
    {
        Vector3f position = new Vector3f(point1);
        m_Origin.transformPoint(position);
        m_Positions.add(position);

        position = new Vector3f(point2);
        m_Origin.transformPoint(position);
        m_Positions.add(position);
    }
    
    public void drawSphere(PSphere sphere, int NumberOfSlices, int NumberOfStacks, boolean boneSelection)
    {
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
        
        float                   fRadius             = sphere.getRadius();
        Vector3f                Center              = sphere.getCenter();
        
        HashSet<PLine>          Positions           = new HashSet();
        //int                     PositionIndex       = 0;
        //Vector3f []             m_Positions      = new Vector3f [2 * (6 * NumberOfSlices + 8 * NumberOfSlices + 4 * NumberOfSlices * NumberOfStacks)]; // booboo that's bad, every edge has a duplicate
        
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

        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
        {
            fNextAngle = fAngle + fAngleStep;

            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
            BottomPoint1.y = BottomCenter.y;
            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);

            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
            BottomPoint2.y = BottomCenter.y;
            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);

            // Triangle lines
            Positions.add(  new PLine(TopPoint1, BottomPoint1)       );
            Positions.add(  new PLine(BottomPoint1, BottomPoint2)    );
            Positions.add(  new PLine(BottomPoint2, TopPoint1)       );
            
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

            // Quad lines
            Positions.add(  new PLine(TopPoint1, TopPoint2)       );
            Positions.add(  new PLine(TopPoint2, BottomPoint1)    );
            Positions.add(  new PLine(BottomPoint1, BottomPoint2) );
            Positions.add(  new PLine(BottomPoint2, TopPoint1)    );

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

                // Quad lines
                Positions.add(  new PLine(TopPoint1, TopPoint2)       );
                Positions.add(  new PLine(TopPoint2, BottomPoint1)    );
                Positions.add(  new PLine(BottomPoint1, BottomPoint2) );
                Positions.add(  new PLine(BottomPoint2, TopPoint1)    );

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

            // Quad lines
            Positions.add(  new PLine(TopPoint1, TopPoint2)       );
            Positions.add(  new PLine(TopPoint2, BottomPoint1)    );
            Positions.add(  new PLine(BottomPoint1, BottomPoint2) );
            Positions.add(  new PLine(BottomPoint2, TopPoint1)    );

            fAngle += fAngleStep;
        }

        TopCenter.set(Center);
        TopCenter.y += fRadius - fStackStep * 0.5f;

        BottomCenter.set(Center);
        BottomCenter.y += fRadius;

        fStackYHeight = fRadius - fStackStep * 0.5f;
        fTopRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);

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

            // Triangle lines
            Positions.add(  new PLine(TopPoint1, TopPoint2)       );
            Positions.add(  new PLine(TopPoint2, BottomCenter)    );
            Positions.add(  new PLine(BottomCenter, TopPoint1)    );

            fAngle += fAngleStep;
        }
        
        // Draw
        if (boneSelection)
        {
            Iterator<PLine> iter = Positions.iterator();
            while (iter.hasNext())
            {
                PLine line = iter.next();
                
                //Vector3f position = new Vector3f(point1);
                //m_Origin.transformPoint(position);
                m_SkeletonBones.add(line.m_point1);

                //position = new Vector3f(point2);
                //m_Origin.transformPoint(position);
                m_SkeletonBones.add(line.m_point2);
            }
        }
        else
        {
            //int i = 0;
            //Vector3f [] positionsArray = new Vector3f [2 * Positions.size()];
            Iterator<PLine> iter = Positions.iterator();
            while (iter.hasNext())
            {
                PLine line = iter.next();
                transformAndAddToBatch(line);
                //positionsArray[i]   = line.m_point1;
                //positionsArray[i+1] = line.m_point2;
                //i += 2;
            }

            //m_lines.reconstruct( BufferUtils.createFloatBuffer(positionsArray), null, null, null );
            //m_lines.draw(m_Renderer);
        }
    }
    
    private void drawBones(PJoint skeleton) 
    {
        LinkedList<PNode> list = new LinkedList<PNode>();
        
        list.add(skeleton);
        
        // Dahlgren - Moved reference declarations to outside the while loop
        // in order to avoid unnecessary memory thrashing by the references.
        PNode current = null;
        PNode parent  = null;
        PTransform currentTransform = null;
        PTransform parentTransform  = null;
        while(!list.isEmpty())
        {
            current = list.poll();
            parent  = current.getParent();

            // Get the parent's world matrix
            parentTransform = null;
            if (parent != null)
                parentTransform = parent.getTransform();
            if (parentTransform == null)
                parentTransform = new PTransform();

            // Build the world matrix for the current instance
            currentTransform = current.getTransform();
            if (currentTransform != null) // This node may not have a transform at all
            {
                if (current.isDirty() || currentTransform.isDirtyWorldMat())
                {
                    currentTransform.buildWorldMatrix(parentTransform.getWorldMatrix(false));
                    current.setDirty(false, false);
                }

                // Add this bone to the skeleton to draw at present()
                if (parent instanceof PJoint)
                    addToSkeleton(currentTransform.getWorldMatrix(false), parentTransform.getWorldMatrix(false));
                else
                    addToSkeleton(currentTransform.getWorldMatrix(false), null);
                
                // Draw selected bones
                if (current instanceof PJoint)
                {
                    PJoint selected = (PJoint)current;
                    if (selected.isSelected())
                    {
                        boneSelectionSphere.set(currentTransform.getWorldMatrix(false).getTranslation(), 0.25f);
                        drawSphere(boneSelectionSphere, 3, 3, true);
                    }
                }
            }
            
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
    }
    
    private void addToSkeleton(PMatrix bone, PMatrix parentWorldMatrix)
    {   
        Vector3f point1   = null;
        Vector3f point2   = null;
        Vector3f position = null;
        
        // add bone
        if (parentWorldMatrix != null)
        {
            point1   = parentWorldMatrix.getTranslation();
            point2   = bone.getTranslation();
        
            position = new Vector3f(point1);
            //m_Origin.transformPoint(position);
            m_SkeletonBones.add(position);

            position = new Vector3f(point2);
            //m_Origin.transformPoint(position);
            m_SkeletonBones.add(position);
        }
        
        // add X axis
        point1   = bone.getTranslation();
        point2   = bone.getTranslation().add(bone.getLocalXNormalized());
        
        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        m_SkeletonX.add(position);
        
        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        m_SkeletonX.add(position);
        
        // add Y axis
        point2   = bone.getTranslation().add(bone.getLocalYNormalized());
        
        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        m_SkeletonY.add(position);
        
        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        m_SkeletonY.add(position);
        
        // add Z axis
        point2   = bone.getTranslation().add(bone.getLocalZNormalized());
        
        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        m_SkeletonZ.add(position);
        
        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        m_SkeletonZ.add(position);
    }
    
}
