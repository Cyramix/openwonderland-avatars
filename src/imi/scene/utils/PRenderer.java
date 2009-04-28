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

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.utils.visualizations.DebuggerVisualization;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModel;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import java.util.LinkedList;
import javolution.util.FastTable;

/**
 *
 * @author Lou Hayt
 * @author Chris Nagle
 */
public class PRenderer 
{
    private Renderer    m_Renderer              = null;
    
    //private boolean     m_bDrawOrigin           = true;
    //private boolean     m_bRenderPolygonCenters = false;
    //private boolean     m_bRenderPolygonNormals = false;
    private boolean     m_bRenderVertexNormals  = false;
    //private boolean     m_bRenderBoundingBox    = true;
    private boolean     m_bRenderBoundingSphere = false;
    //private boolean     m_bRenderMesh           = true;

    //private FastTable<Vector3f> points          = new FastTable<Vector3f>();
    private FastTable<Vector3f> lines           = new FastTable<Vector3f>();
    
//    public enum BVMode   {    BV_Off, BV_Box, BV_Sphere     }
//    private BVMode  m_BoundingVolumeMode    = BVMode.BV_Off;

    private BoundingSphere jmeSphere = new BoundingSphere();

    // Render batch
    PMatrix             m_Origin         = new PMatrix();
    //FastTable<Vector3f> m_Positions      = new FastTable<Vector3f>();
    //FastTable<Vector3f> m_Centers        = new FastTable<Vector3f>();
    //FastTable<Vector3f> m_PolygonNormals = new FastTable<Vector3f>();
    //FastTable<Vector3f> m_VertexNormals  = new FastTable<Vector3f>();
    
    FastTable<Vector3f> m_SkeletonBones  = new FastTable<Vector3f>();
    FastTable<Vector3f> m_SkeletonX      = new FastTable<Vector3f>();
    FastTable<Vector3f> m_SkeletonY      = new FastTable<Vector3f>();
    FastTable<Vector3f> m_SkeletonZ      = new FastTable<Vector3f>();
    
    public PRenderer() 
    {
    }
    
    public void resetRenderer(Renderer r)
    {
        if (r != null && m_Renderer == null)
        {
            m_Renderer = r;
        }
        
//        m_Positions.clear();
//        m_Centers.clear();
//        m_PolygonNormals.clear();
//        m_VertexNormals.clear();
        
        m_SkeletonBones.clear();
        m_SkeletonX.clear();
        m_SkeletonY.clear();
        m_SkeletonZ.clear();
        
//        // draw origin
//        m_SkeletonX.add(Vector3f.ZERO);
//        m_SkeletonX.add(Vector3f.UNIT_X.mult(1000.0f));
//        m_SkeletonY.add(Vector3f.ZERO);
//        m_SkeletonY.add(Vector3f.UNIT_Y.mult(1000.0f));
//        m_SkeletonZ.add(Vector3f.ZERO);
//        m_SkeletonZ.add(Vector3f.UNIT_Z.mult(1000.0f));
    }
    
    public void present() 
    {
//        if (m_bRenderMesh && m_Positions.size() > 0)
//        {
//            lines.clear();
//            lines.addAll(m_Positions);
//            if (!lines.isEmpty())
//                DebuggerVisualization.drawLines(lines, m_Renderer, 1.0f, ColorRGBA.magenta, ColorRGBA.magenta);
//        }
//
//        if (m_bRenderPolygonCenters && m_Centers.size() > 0)
//        {
//            points.clear();
//            points.addAll(m_Centers);
//            if (!points.isEmpty())
//                DebuggerVisualization.drawPoints(points, m_Renderer, 5.0f, ColorRGBA.magenta);
//        }
//
//        if (m_bRenderPolygonNormals)
//        {
//            lines.clear();
//            lines.addAll(m_PolygonNormals);
//            if (!lines.isEmpty())
//                DebuggerVisualization.drawLines(lines, m_Renderer, 1.0f, ColorRGBA.darkGray, ColorRGBA.lightGray);
//        }
//
//        if (m_bRenderVertexNormals)
//        {
//            lines.clear();
//            lines.addAll(m_VertexNormals);
//            if (!lines.isEmpty())
//                DebuggerVisualization.drawLines(lines, m_Renderer, 1.0f, ColorRGBA.darkGray, ColorRGBA.lightGray);
//        }
        
        // Present skeletons
        if (!m_SkeletonX.isEmpty()) // m_SkeletonXYZ is also used to draw origin
        {
            // present bones
            if (!m_SkeletonBones.isEmpty())
            {
                lines.clear();
                lines.addAll(m_SkeletonBones);
                if (!lines.isEmpty())
                    DebuggerVisualization.drawLines(lines, m_Renderer, 2.0f, ColorRGBA.yellow, ColorRGBA.yellow);
            }
            
            // present x axes
            lines.clear();
            lines.addAll(m_SkeletonX);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, m_Renderer, 1.0f, ColorRGBA.red, ColorRGBA.red);

            // present y axes
            lines.clear();
            lines.addAll(m_SkeletonY);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, m_Renderer, 1.0f, ColorRGBA.green, ColorRGBA.green);

            // present z axes
            lines.clear();
            lines.addAll(m_SkeletonZ);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, m_Renderer, 1.0f, ColorRGBA.blue, ColorRGBA.blue);
        }
    }
    
//    public boolean isbDrawOrigin() {
//        return m_bDrawOrigin;
//    }
//
//    public void setDrawOrigin(boolean bDrawOrigin) {
//        m_bDrawOrigin = bDrawOrigin;
//    }
    
    public Renderer getJMERenderer()
    {
        return m_Renderer;
    }

    public void setOrigin(PMatrix origin)
    {
        m_Origin.set(origin);
    }
    
    public void drawSkeleton(PNode skeletonRoot)
    {
        if (skeletonRoot.getChildrenCount() > 0)
        {
            for (PNode kid : skeletonRoot.getChildren())
            {
                if (kid instanceof PJoint)
                    drawBones((PJoint)kid); //  Draw skeleton
            }
        }
        else
            System.out.println("PRenderer - No skeletonRoot to draw.");
    }
    
    public void drawPPolygonMesh(PPolygonMesh mesh)
    {
        if (m_bRenderBoundingSphere)
        {
            jmeSphere.setCenter(mesh.getBoundingSphere().getCenterRef().add(m_Origin.getTranslation()));
            jmeSphere.setRadius(mesh.getBoundingSphere().getRadius());
            DebuggerVisualization.drawBounds(jmeSphere, m_Renderer);
        }

        // Use jme wireframe... this is too slow to use

//        // TODO : check possible optimization in a similar way to drawSphere()'s use of HashSet
//        // removing duplicate lines in the cost of another memory copy, that will also mean
//        // lesss draw calls but performing the flatenning of the hierarchy CPU side
//
//         for (int i = 0; i < mesh.getPolygonCount(); i++)
//         {
//             drawPPolygon_Wireframe(mesh.getPolygon(i), mesh);
//
////             if (m_bRenderPolygonCenters)
////             {
////                 Vector3f center = new Vector3f(mesh.getPolygon(i).getCenter());
////                 m_Origin.transformPoint(center);
////                 m_Centers.add(center);
////             }
//
//             if (m_bRenderPolygonNormals)
//             {
//                 Vector3f normal = new Vector3f(mesh.getPolygon(i).getCenter());
//                 m_Origin.transformPoint(normal);
//                 m_PolygonNormals.add(normal);
//
//                 normal = new Vector3f(mesh.getPolygon(i).getCenter().add(mesh.getPolygon(i).getNormal()));
//                 m_Origin.transformPoint(normal);
//                 m_PolygonNormals.add(normal);
//             }
//         }
//
//        if (m_bRenderBoundingBox)
//            drawCube(mesh.getBoundingCube());
//
//        if (m_bRenderBoundingSphere)
//            drawSphere(mesh.getBoundingSphere(), 5, 5, false);
//
//         // this will only draw the first pose
//         //if (mesh instanceof PPolygonSkinnedMesh)
//         //    drawBones((PPolygonSkinnedMesh)mesh);
    }
    
    public void drawPPolygon_Wireframe(PPolygon poly, PPolygonMesh mesh)
    {
//        int vertCount = poly.getVertexCount();
//
//        int point1Index;
//        int point2Index;
//        Vector3f point1;
//        Vector3f point2;
//
//        point1Index = poly.getVertex(0).m_PositionIndex;
//        point1 = mesh.getPosition(point1Index).m_Position;
//
//        for (int i = 1; i < vertCount; i++)
//        {
//            point2Index = poly.getVertex(i).m_PositionIndex;
//            point2 = mesh.getPosition(point2Index).m_Position;
//
//            Vector3f position = new Vector3f(point1);
//            m_Origin.transformPoint(position);
//            m_Positions.add(position);
//
//            position = new Vector3f(point2);
//            m_Origin.transformPoint(position);
//            m_Positions.add(position);
//
//            if (m_bRenderVertexNormals)
//            {
//                Vector3f normal = new Vector3f(point1);
//                m_Origin.transformPoint(normal);
//                m_VertexNormals.add(normal);
//
//                normal = new Vector3f(point1.add( mesh.getNormal( poly.getVertex(i).m_NormalIndex ).m_Normal ));
//                m_Origin.transformPoint(normal);
//                m_VertexNormals.add(normal);
//            }
//
//            point1 = point2;
//        }
//
//        //  Add line connecting last vertice to first vertice.
//        //  point1 will point to last vertice.
//        point2Index = poly.getVertex(0).m_PositionIndex;
//        point2 = mesh.getPosition(point2Index).m_Position;
//
//        Vector3f position = new Vector3f(point1);
//        m_Origin.transformPoint(position);
//        m_Positions.add(position);
//
//
//        position = new Vector3f(point2);
//        m_Origin.transformPoint(position);
//        m_Positions.add(position);
    }
           
//    public void setRenderPRendererMesh(boolean on)
//    {
//        m_bRenderMesh = on;
//    }
//
//    public void toggleRenderPRendererMesh()
//    {
//         m_bRenderMesh = !m_bRenderMesh;
//    }
//
//    public void renderPolygonNormals(boolean on)
//    {
//        m_bRenderPolygonNormals =   on;
//    }

    public void renderVertexNormals(boolean on)
    {
        m_bRenderVertexNormals  =   on;
    }

//    public void renderPolygonCenters(boolean on)
//    {
//        m_bRenderPolygonCenters =   on;
//    }

//    public void renderBoundingBox(boolean on)
//    {
//        m_bRenderBoundingBox    =   on;
//    }

    public void renderBoundingSphere(boolean on)
    {
        m_bRenderBoundingSphere =   on;
    }
    
//    public void renderBoundingVolumeToggle()
//    {
//        switch (m_BoundingVolumeMode)
//        {
//            case BV_Off:
//                m_BoundingVolumeMode    = BVMode.BV_Box;
//                m_bRenderBoundingBox    = true;
//                m_bRenderBoundingSphere = false;
//                break;
//            case BV_Box:
//                m_BoundingVolumeMode    = BVMode.BV_Sphere;
//                m_bRenderBoundingBox    = false;
//                m_bRenderBoundingSphere = true;
//                break;
//            case BV_Sphere:
//                m_BoundingVolumeMode    = BVMode.BV_Off;
//                m_bRenderBoundingBox    = false;
//                m_bRenderBoundingSphere = false;
//                break;
//        }
//    }
    
//    public boolean getRenderPolygonNormals()
//    {
//        return m_bRenderPolygonNormals;
//    }

    public boolean getRenderVertexNormals()
    {
        return m_bRenderVertexNormals;
    }

//    public boolean getRenderPolygonCenters()
//    {
//        return m_bRenderPolygonCenters;
//    }

//    public boolean getRenderBoundingBox()
//    {
//        return m_bRenderBoundingBox;
//    }

    public boolean getRenderBoundingSphere()
    {
        return m_bRenderBoundingSphere;
    }
    
//    private void drawCube(PCube cube)
//    {
//        Vector3f        Center              = cube.getCenter();
//
//        Vector3f	Point1              = new Vector3f();
//        Vector3f	Point2              = new Vector3f();
//        Vector3f	Point3              = new Vector3f();
//        Vector3f	Point4              = new Vector3f();
//        Vector3f	Point5              = new Vector3f();
//        Vector3f	Point6              = new Vector3f();
//        Vector3f	Point7              = new Vector3f();
//        Vector3f	Point8              = new Vector3f();
//
//        float		fHalfWidth          = cube.getWidth()  * 0.5f;
//        float		fHalfHeight	    = cube.getHeight() * 0.5f;
//        float		fHalfDepth	    = cube.getDepth()  * 0.5f;
//
//        Point1.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
//        Point2.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z - fHalfDepth);
//        Point3.set(Center.x + fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);
//        Point4.set(Center.x - fHalfWidth, Center.y + fHalfHeight, Center.z + fHalfDepth);
//
//        Point5.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
//        Point6.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z - fHalfDepth);
//        Point7.set(Center.x + fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);
//        Point8.set(Center.x - fHalfWidth, Center.y - fHalfHeight, Center.z + fHalfDepth);
//
//        //  'Top' Quad.
//        transformAndAddToBatch(Point4, Point3);
//        //m_Positions.add(Point4);
//        //m_Positions.add(Point3);
//
//        transformAndAddToBatch(Point3, Point2);
//        //m_Positions.add(Point3);
//        //m_Positions.add(Point2);
//
//        transformAndAddToBatch(Point2, Point1);
//        //m_Positions.add(Point2);
//        //m_Positions.add(Point1);
//
//        transformAndAddToBatch(Point1, Point4);
//        //m_Positions.add(Point1);
//        //m_Positions.add(Point4);
//
//        //  'Left' Quad. (with duplicates removed)
//        transformAndAddToBatch(Point5, Point8);
//        //m_Positions.add(Point5);
//        //m_Positions.add(Point8);
//
//        transformAndAddToBatch(Point8, Point4);
//        //m_Positions.add(Point8);
//        //m_Positions.add(Point4);
//
//        transformAndAddToBatch(Point1, Point5);
//        //m_Positions.add(Point1);
//        //m_Positions.add(Point5);
//
//        //  'Back' Quad. (with duplicates removed)
//        transformAndAddToBatch(Point6, Point5);
//        //m_Positions.add(Point6);
//        //m_Positions.add(Point5);
//
//        transformAndAddToBatch(Point2, Point6);
//        //m_Positions.add(Point2);
//        //m_Positions.add(Point6);
//
//        //  'Right' Quad. (with duplicates removed)
//        transformAndAddToBatch(Point7, Point6);
//        //m_Positions.add(Point7);
//        //m_Positions.add(Point6);
//
//        transformAndAddToBatch(Point3, Point7);
//        //m_Positions.add(Point3);
//        //m_Positions.add(Point7);
//
//        //  'Front' Quad. (with duplicates removed)
//        transformAndAddToBatch(Point8, Point7);
//        //m_Positions.add(Point8);
//        //m_Positions.add(Point7);
//    }
//
//    //  Draws a single triangle.
//    public void drawTriangle(Vector3f point1, Vector3f point2, Vector3f point3)
//    {
//        transformAndAddToBatch(point1, point2);
//        transformAndAddToBatch(point2, point3);
//        transformAndAddToBatch(point3, point1);
//    }
//
//    private void transformAndAddToBatch(PLine line)
//    {
//        transformAndAddToBatch(line.m_point1, line.m_point2);
//    }
//
//    private void transformAndAddToBatch(Vector3f point1, Vector3f point2)
//    {
//        Vector3f position = new Vector3f(point1);
//        m_Origin.transformPoint(position);
//        m_Positions.add(position);
//
//        position = new Vector3f(point2);
//        m_Origin.transformPoint(position);
//        m_Positions.add(position);
//    }
//
//    private void drawSphere(PSphere sphere, int NumberOfSlices, int NumberOfStacks, boolean boneSelection)
//    {
//        Vector3f		TopCenter           = new Vector3f();
//        Vector3f		BottomCenter        = new Vector3f();
//        float			fTopRadius;
//        float			fBottomRadius;
//        float			fTopHeight;
//        float			fBottomHeight;
//        float			fStackStep;
//        float			fStackYHeight;
//        int			SliceIndex;
//        int			StackIndex;
//        float			fAngle;
//        float			fNextAngle;
//        float			fAngleStep;
//        Vector3f		TopPoint1           = new Vector3f();
//        Vector3f		TopPoint2           = new Vector3f();
//        Vector3f		BottomPoint1        = new Vector3f();
//        Vector3f		BottomPoint2        = new Vector3f();
//
//        float                   fRadius             = sphere.getRadius();
//        Vector3f                Center              = sphere.getCenterRef();
//
//        HashSet<PLine>          Positions           = new HashSet();
//        //int                     PositionIndex       = 0;
//        //Vector3f []             m_Positions      = new Vector3f [2 * (6 * NumberOfSlices + 8 * NumberOfSlices + 4 * NumberOfSlices * NumberOfStacks)]; // booboo that's bad, every edge has a duplicate
//
//        if (NumberOfSlices < 3)
//            NumberOfSlices = 3;
//
//        if (NumberOfStacks < 3)
//            NumberOfStacks = 3;
//
//        fStackStep = (fRadius * 2.0f) / (float)NumberOfStacks;
//        fAngleStep = (float)(2.0f * Math.PI) / (float)NumberOfSlices;
//
//        //  **********************
//        //  Draw the top part of the sphere.
//        //  **********************
//
//        TopCenter.set(Center);
//        TopCenter.y -= fRadius;
//
//        BottomCenter.set(TopCenter);
//        BottomCenter.y += fStackStep * 0.5f;
//
//        fStackYHeight = fRadius - fStackStep * 0.5f;
//
//        fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);
//
//        fAngle = 0.0f;
//
//        TopPoint1.set(TopCenter);
//
//        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
//        {
//            fNextAngle = fAngle + fAngleStep;
//
//            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
//            BottomPoint1.y = BottomCenter.y;
//            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);
//
//            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
//            BottomPoint2.y = BottomCenter.y;
//            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);
//
//            // Triangle lines
//            Positions.add(  new PLine(TopPoint1, BottomPoint1)       );
//            Positions.add(  new PLine(BottomPoint1, BottomPoint2)    );
//            Positions.add(  new PLine(BottomPoint2, TopPoint1)       );
//
//            fAngle = fNextAngle;
//        }
//
//        TopCenter.set(BottomCenter);
//
//        BottomCenter.y += fStackStep * 0.5f;
//
//        fTopRadius = fBottomRadius;
//
//        fStackYHeight -= fStackStep * 0.5f;
//
//        fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);
//
//        fAngle = 0.0f;
//
//        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
//        {
//            fNextAngle = fAngle + fAngleStep;
//
//            TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
//            TopPoint1.y = TopCenter.y;
//            TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);
//
//            TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
//            TopPoint2.y = TopCenter.y;
//            TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);
//
//            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
//            BottomPoint1.y = BottomCenter.y;
//            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);
//
//            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
//            BottomPoint2.y = BottomCenter.y;
//            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);
//
//            // Quad lines
//            Positions.add(  new PLine(TopPoint1, TopPoint2)       );
//            Positions.add(  new PLine(TopPoint2, BottomPoint1)    );
//            Positions.add(  new PLine(BottomPoint1, BottomPoint2) );
//            Positions.add(  new PLine(BottomPoint2, TopPoint1)    );
//
//            fAngle += fAngleStep;
//        }
//
//        //  **********************
//        //  Draw the center part of the sphere.
//        //  **********************
//
//        TopCenter.set(BottomCenter);
//
//        BottomCenter.y += fStackStep;
//
//        fTopHeight    = fRadius - fStackStep;
//        fBottomHeight = fTopHeight - fStackStep;
//
//        for (StackIndex=0; StackIndex<NumberOfStacks-2; StackIndex++)
//        {
//            fTopRadius = (float)Math.sqrt(fRadius * fRadius - fTopHeight * fTopHeight);
//
//            fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fBottomHeight * fBottomHeight);
//
//
//            fAngle = 0.0f;
//
//            for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
//            {
//                fNextAngle = fAngle + fAngleStep;
//
//                TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
//                TopPoint1.y = TopCenter.y;
//                TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);
//
//                TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
//                TopPoint2.y = TopCenter.y;
//                TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);
//
//                BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
//                BottomPoint1.y = BottomCenter.y;
//                BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);
//
//                BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
//                BottomPoint2.y = BottomCenter.y;
//                BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);
//
//                // Quad lines
//                Positions.add(  new PLine(TopPoint1, TopPoint2)       );
//                Positions.add(  new PLine(TopPoint2, BottomPoint1)    );
//                Positions.add(  new PLine(BottomPoint1, BottomPoint2) );
//                Positions.add(  new PLine(BottomPoint2, TopPoint1)    );
//
//                fAngle += fAngleStep;
//            }
//
//            TopCenter.set(BottomCenter);
//            BottomCenter.y += fStackStep;
//
//            fTopHeight    = fBottomHeight;
//            fBottomHeight = fTopHeight - fStackStep;
//        }
//
//        //  **********************
//        //  Draw the bottom part of the sphere.
//        //  **********************
//
//        TopCenter.set(Center);
//        TopCenter.y += fRadius - fStackStep;
//
//        BottomCenter.set(TopCenter);
//        BottomCenter.y += fStackStep * 0.5f;
//
//        fStackYHeight = fRadius - fStackStep;
//        fTopRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);
//
//        fStackYHeight += fStackStep * 0.5f;
//        fBottomRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);
//
//        fAngle = 0.0f;
//
//        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
//        {
//            fNextAngle = fAngle + fAngleStep;
//
//            TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
//            TopPoint1.y = TopCenter.y;
//            TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);
//
//            TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
//            TopPoint2.y = TopCenter.y;
//            TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);
//
//            BottomPoint1.x = BottomCenter.x + (float)(Math.cos(fAngle) * fBottomRadius);
//            BottomPoint1.y = BottomCenter.y;
//            BottomPoint1.z = BottomCenter.z + (float)(Math.sin(fAngle) * fBottomRadius);
//
//            BottomPoint2.x = BottomCenter.x + (float)(Math.cos(fNextAngle) * fBottomRadius);
//            BottomPoint2.y = BottomCenter.y;
//            BottomPoint2.z = BottomCenter.z + (float)(Math.sin(fNextAngle) * fBottomRadius);
//
//            // Quad lines
//            Positions.add(  new PLine(TopPoint1, TopPoint2)       );
//            Positions.add(  new PLine(TopPoint2, BottomPoint1)    );
//            Positions.add(  new PLine(BottomPoint1, BottomPoint2) );
//            Positions.add(  new PLine(BottomPoint2, TopPoint1)    );
//
//            fAngle += fAngleStep;
//        }
//
//        TopCenter.set(Center);
//        TopCenter.y += fRadius - fStackStep * 0.5f;
//
//        BottomCenter.set(Center);
//        BottomCenter.y += fRadius;
//
//        fStackYHeight = fRadius - fStackStep * 0.5f;
//        fTopRadius = (float)Math.sqrt(fRadius * fRadius - fStackYHeight * fStackYHeight);
//
//        fAngle = 0.0f;
//
//        for (SliceIndex=0; SliceIndex<NumberOfSlices; SliceIndex++)
//        {
//            fNextAngle = fAngle + fAngleStep;
//
//            TopPoint1.x = TopCenter.x + (float)(Math.cos(fNextAngle) * fTopRadius);
//            TopPoint1.y = TopCenter.y;
//            TopPoint1.z = TopCenter.z + (float)(Math.sin(fNextAngle) * fTopRadius);
//
//            TopPoint2.x = TopCenter.x + (float)(Math.cos(fAngle) * fTopRadius);
//            TopPoint2.y = TopCenter.y;
//            TopPoint2.z = TopCenter.z + (float)(Math.sin(fAngle) * fTopRadius);
//
//            // Triangle lines
//            Positions.add(  new PLine(TopPoint1, TopPoint2)       );
//            Positions.add(  new PLine(TopPoint2, BottomCenter)    );
//            Positions.add(  new PLine(BottomCenter, TopPoint1)    );
//
//            fAngle += fAngleStep;
//        }
//
//        // Draw
//        if (boneSelection)
//        {
//            Iterator<PLine> iter = Positions.iterator();
//            while (iter.hasNext())
//            {
//                PLine line = iter.next();
//
//                //Vector3f position = new Vector3f(point1);
//                //m_Origin.transformPoint(position);
//                m_SkeletonBones.add(line.m_point1);
//
//                //position = new Vector3f(point2);
//                //m_Origin.transformPoint(position);
//                m_SkeletonBones.add(line.m_point2);
//            }
//        }
//        else
//        {
//            //int i = 0;
//            //Vector3f [] positionsArray = new Vector3f [2 * Positions.size()];
//            Iterator<PLine> iter = Positions.iterator();
//            while (iter.hasNext())
//            {
//                PLine line = iter.next();
//                transformAndAddToBatch(line);
//                //positionsArray[i]   = line.m_point1;
//                //positionsArray[i+1] = line.m_point2;
//                //i += 2;
//            }
//
//            //m_lines.reconstruct( BufferUtils.createFloatBuffer(positionsArray), null, null, null );
//            //m_lines.draw(m_Renderer);
//        }
//    }
    
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

                boolean currentSelected = false;
                // Draw selected bones
                if (current instanceof PJoint)
                {
                    PJoint selected = (PJoint)current;
                    if (selected.isSelected())
                    {
                        currentSelected = true;
                        jmeSphere.setCenter(currentTransform.getWorldMatrix(false).getTranslation());
                        jmeSphere.setRadius(0.05f);
                        //DebuggerVisualization.setBoundsColor(ColorRGBA.blue); doesn't workie...
                        DebuggerVisualization.drawBounds(jmeSphere, m_Renderer);
                    }
                }

                // Add this bone to the skeleton to draw at present()
                if (parent instanceof PJoint)
                    addToSkeleton(currentTransform.getWorldMatrix(false), parentTransform.getWorldMatrix(false), currentSelected);
                else
                    addToSkeleton(currentTransform.getWorldMatrix(false), null, currentSelected);
            }
            
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
    }
    
    private void addToSkeleton(PMatrix bone, PMatrix parentWorldMatrix, boolean selected)
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

        float triadScalar = 0.1f;
        if (selected)
            triadScalar *= 3.0f;

        // add X axis
        point1   = bone.getTranslation();
        point2   = bone.getTranslation().add(bone.getLocalX().mult(triadScalar));
        
        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        m_SkeletonX.add(position);
        
        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        m_SkeletonX.add(position);
        
        // add Y axis
        point2   = bone.getTranslation().add(bone.getLocalY().mult(triadScalar));
        
        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        m_SkeletonY.add(position);
        
        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        m_SkeletonY.add(position);
        
        // add Z axis
        point2   = bone.getTranslation().add(bone.getLocalZ().mult(triadScalar));
        
        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        m_SkeletonZ.add(position);
        
        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        m_SkeletonZ.add(position);
    }
    
}
