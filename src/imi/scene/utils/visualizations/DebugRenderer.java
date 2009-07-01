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
package imi.scene.utils.visualizations;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.SkeletonNode;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import java.util.LinkedList;
import javolution.util.FastTable;

/**
 * This debug renderer is set on a JScene to enable skeleton rendering,
 * on the jscene's overriden jme draw call it gets reset, drawen and then presented.
 *
 * @author Lou Hayt
 */
public class DebugRenderer
{
    private boolean             boundingSphereDrawEnabled = false;
    private Renderer            renderer      = null;
    private PMatrix             origin        = new PMatrix();
    private BoundingSphere      jmeSphere     = new BoundingSphere();
    private FastTable<Vector3f> lines         = new FastTable<Vector3f>();
    private FastTable<Vector3f> skeletonBones = new FastTable<Vector3f>();
    private FastTable<Vector3f> skeletonX     = new FastTable<Vector3f>();
    private FastTable<Vector3f> skeletonY     = new FastTable<Vector3f>();
    private FastTable<Vector3f> skeletonZ     = new FastTable<Vector3f>();

    /**
     * Empty constructor
     */
    public DebugRenderer() {
    }

    /**
     * Will set local data to draw skeleton bones, selected joints (spheres) and sphere bounding volumes.
     */
    public void draw(PNode node)
    {
        if (node instanceof SkeletonNode)
            drawSkeleton(((SkeletonNode)node).getSkeletonRoot());
        else if (node instanceof PPolygonMeshInstance)
            drawPPolygonMesh(((PPolygonMeshInstance)node).getGeometry());
    }

    /**
     * Sets the renderer and clears local data.
     */
    public void resetRenderer(Renderer r)
    {
        if (r != null && renderer == null)
            renderer = r;
        origin.setIdentity();
        lines.clear();
        skeletonBones.clear();
        skeletonX.clear();
        skeletonY.clear();
        skeletonZ.clear();
    }

    /**
     * jme draw calls for all local data using the DebuggerVisualization class
     */
    public void present()
    {
        // Present skeletons
        if (!skeletonX.isEmpty())
        {
            // present bones
            if (!skeletonBones.isEmpty())
            {
                lines.clear();
                lines.addAll(skeletonBones);
                if (!lines.isEmpty())
                    DebuggerVisualization.drawLines(lines, renderer, 2.0f, ColorRGBA.yellow, ColorRGBA.yellow);
            }

            // present x axes
            lines.clear();
            lines.addAll(skeletonX);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, renderer, 1.0f, ColorRGBA.red, ColorRGBA.red);

            // present y axes
            lines.clear();
            lines.addAll(skeletonY);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, renderer, 1.0f, ColorRGBA.green, ColorRGBA.green);

            // present z axes
            lines.clear();
            lines.addAll(skeletonZ);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, renderer, 1.0f, ColorRGBA.blue, ColorRGBA.blue);
        }
    }

    /**
     * Get the jmonkey renderer
     */
    public Renderer getJMERenderer()
    {
        return renderer;
    }

    /**
     * Set the referene point for the sphere bounding volumes
     */
    public void setOrigin(PMatrix origin)
    {
        this.origin.set(origin);
    }

    /**
     * true if drawing sphere bounding volumes is enabled
     */
    public boolean isBoundingSphereDrawEnabled() {
        return boundingSphereDrawEnabled;
    }

    /**
     * Enable drawing sphere bounding volumes
     */
    public void setBoundingSphereDrawEnabled(boolean boundingSphereDrawEnabled) {
        this.boundingSphereDrawEnabled = boundingSphereDrawEnabled;
    }

    void drawSkeleton(PNode skeletonRoot)
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
            System.out.println("DebugRenderer - No skeleton to draw.");
    }

    void drawPPolygonMesh(PPolygonMesh mesh)
    {
        if (boundingSphereDrawEnabled)
        {
            //DebuggerVisualization.setBoundsColor(ColorRGBA.lightGray);
            jmeSphere.setCenter(mesh.getBoundingSphere().getCenterRef().add(origin.getTranslation()));
            jmeSphere.setRadius(mesh.getBoundingSphere().getRadius());
            DebuggerVisualization.drawBounds(jmeSphere, renderer);
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
                        DebuggerVisualization.drawBounds(jmeSphere, renderer);
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
            skeletonBones.add(position);

            position = new Vector3f(point2);
            //m_Origin.transformPoint(position);
            skeletonBones.add(position);
        }

        float triadScalar = 0.1f;
        if (selected)
            triadScalar *= 3.0f;

        // add X axis
        point1   = bone.getTranslation();
        point2   = bone.getTranslation().add(bone.getLocalX().mult(triadScalar));

        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        skeletonX.add(position);

        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        skeletonX.add(position);

        // add Y axis
        point2   = bone.getTranslation().add(bone.getLocalY().mult(triadScalar));

        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        skeletonY.add(position);

        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        skeletonY.add(position);

        // add Z axis
        point2   = bone.getTranslation().add(bone.getLocalZ().mult(triadScalar));

        position = new Vector3f(point1);
        //m_Origin.transformPoint(position);
        skeletonZ.add(position);

        position = new Vector3f(point2);
        //m_Origin.transformPoint(position);
        skeletonZ.add(position);
    }

}