/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.utils.visualizations;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import imi.scene.boundingvolumes.PSphere;
import javolution.util.FastTable;

/**
 *
 * @author Lou Hayt
 */
public class InternalRendererNode extends Node
{
    // World origin
    float originScale = 1.0f;

    // Red marker sphere
    int redSphereOn = 0;
    BoundingSphere redSphere = new BoundingSphere();

    // JME bounding volumes
    FastTable<Spatial> jmeBB = new FastTable<Spatial>();

    // JME normals renderables
    FastTable<Spatial> jmeN = new FastTable<Spatial>();

    // PGraph bounding volumes

    // character skeleton
        // bones
        // triad joints
        // selecting joints

    // verlet paricles and constraints

    // AI heading triangles


    // Path

    // Spheres, Boxes, Lines
    FastTable<BoundingSphere> spehres = new FastTable<BoundingSphere>();
    FastTable<BoundingBox>    boxes   = new FastTable<BoundingBox>();
    FastTable<Vector3f>       lines   = new FastTable<Vector3f>();
    int userLinesOn = 0;
    FastTable<FastTable<Vector3f>>  userLines   = new FastTable<FastTable<Vector3f>>(); // yellow
    int cyanLinesOn = 0;
    FastTable<Vector3f> cyanLines     = new FastTable<Vector3f>();
    int pinkLinesOn = 0;
    FastTable<Vector3f> pinkLines    = new FastTable<Vector3f>();

    @Override
    public void draw(Renderer r)
    {
        // Draw world origin
        DebuggerVisualization.drawOrigin(r, originScale);

        // Draw jme bounding volumes
        DebuggerVisualization.setBoundsColor(ColorRGBA.orange);
        for (Spatial spatial : jmeBB)
            DebuggerVisualization.drawBounds(spatial, r);

        // Draw spheres
        DebuggerVisualization.setBoundsColor(ColorRGBA.blue);
        for (BoundingSphere sphere : spehres)
            DebuggerVisualization.drawBoundingSphere(sphere, r);

        // Draw boxes
        DebuggerVisualization.setBoundsColor(ColorRGBA.blue);
        for (BoundingBox box : boxes)
            DebuggerVisualization.drawBoundingBox(box, r);

        // Draw jme normals renderables
        for (Spatial spatial : jmeN)
            DebuggerVisualization.drawNormals(spatial, r);

        // Draw lines
        if (userLinesOn > 0)
        {
            userLinesOn--;
            lines.clear();
            for (FastTable<Vector3f> userLine : userLines)
                lines.addAll(userLine);
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, r, 3.0f, ColorRGBA.yellow, ColorRGBA.yellow);
        }

        if (pinkLinesOn > 0)
        {
            pinkLinesOn--;
            if (!pinkLines.isEmpty())
                DebuggerVisualization.drawLines(pinkLines, r, 5.0f, ColorRGBA.pink, ColorRGBA.pink);
        }

        if (cyanLinesOn > 0)
        {
            cyanLinesOn--;
            if (!cyanLines.isEmpty())
                DebuggerVisualization.drawLines(cyanLines, r, 4.0f, ColorRGBA.cyan, ColorRGBA.cyan);
        }

        // Draw red marker sphere
        if (redSphereOn > 0)
        {
            DebuggerVisualization.setBoundsColor(ColorRGBA.red);
            DebuggerVisualization.drawBoundingSphere(redSphere, r);
            redSphereOn--;
        }
    }

    public void setRedSphere(PSphere red) {
        redSphere.setCenter(red.getCenterRef());
        redSphere.setRadius(red.getRadius());
        redSphereOn = 20;
    }

    public void setRedSphere(BoundingSphere red) {
        redSphere.setCenter(red.getCenter());
        redSphere.setRadius(red.getRadius());
        redSphereOn = 20;
    }

    public FastTable<Spatial> getJmeBB() {
        return jmeBB;
    }
    public FastTable<Spatial> getJmeN() {
        return jmeN;
    }
    public FastTable<BoundingBox> getBoxes() {
        return boxes;
    }
    public FastTable<BoundingSphere> getSpheres() {
        return spehres;
    }
    /** Add and remove FastTable<Vector3f> - each represents a line (potentially with multiple segments)
     *  Calling this method makes the lines visible for 100 frames
     **/
    public FastTable<FastTable<Vector3f>> getUserLines() {
        userLinesOn = 100;
        return userLines;
    }

    public FastTable<Vector3f> getCyanLines() {
        cyanLinesOn = 20;
        return cyanLines;
    }

    public FastTable<Vector3f> getPinkLines() {
        pinkLinesOn = 20;
        return pinkLines;
    }
}