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
import imi.scene.PSphere;
import javolution.util.FastTable;

/**
 * Debug rendering tool
 * @author Lou Hayt
 */
public class InternalRendererNode extends Node
{
    boolean enabled = true;

    // World origin
    float originScale = 1.0f;
    boolean drawOrigin = false;

    // Orange position markers
    BoundingSphere positionMarkerSphere = new BoundingSphere(0.25f, new Vector3f());
    FastTable<Vector3f>       positionMarkers   = new FastTable<Vector3f>();

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
    FastTable<BoundingSphere> spheres = new FastTable<BoundingSphere>();
    FastTable<BoundingBox>    boxes   = new FastTable<BoundingBox>();
    FastTable<Vector3f>       lines   = new FastTable<Vector3f>();
    int userLinesOn = 0;
    FastTable<FastTable<Vector3f>>  userLines   = new FastTable<FastTable<Vector3f>>(); // yellow
    int cyanLinesOn = 0;
    FastTable<Vector3f> cyanLines     = new FastTable<Vector3f>();
    int pinkLinesOn = 0;
    FastTable<Vector3f> pinkLines    = new FastTable<Vector3f>();

    FastTable<Vector3f> gridLines    = new FastTable<Vector3f>();
    ColorRGBA gridLinesColor = ColorRGBA.green;
    float gridLinesWidth = 2.0f;

    FastTable<Vector3f> customLines    = new FastTable<Vector3f>();
    ColorRGBA customLinesStartColor = ColorRGBA.red;
    ColorRGBA customLinesEndColor = ColorRGBA.pink;
    float customLinesWidth = 5.0f;

    FastTable<Vector3f> offsetLines    = new FastTable<Vector3f>();
    Vector3f offsetForOffsetLines = new Vector3f(0.0f, 1.0f, 0.0f);
    ColorRGBA offsetLinesStartColor = ColorRGBA.green;
    ColorRGBA offsetLinesEndColor = ColorRGBA.green;
    float offsetLinesWidth = 5.0f;


    public void clearAll()
    {
        positionMarkers.clear();
        jmeBB.clear();
        jmeN.clear();
        spheres.clear();
        boxes.clear();
        lines.clear();
        userLines.clear();
        cyanLines.clear();
        pinkLines.clear();
        gridLines.clear();
        customLines.clear();
        offsetLines.clear();
    }

    @Override
    public void draw(Renderer r)
    {
        if (!enabled)
            return;
        // Dahlgren : Removed for-each syntax. Since this is called every frame,
        // this was generating a ton of iterators.
        int i = 0;
        updateRenderState();

        // Draw world origin
        if (drawOrigin)
            DebuggerVisualization.drawOrigin(r, originScale);

        // Draw jme bounding volumes
        DebuggerVisualization.setBoundsColor(ColorRGBA.orange);
        for (i = 0; i < jmeBB.size(); ++i)
        {
            Spatial spatial = jmeBB.get(i);
            DebuggerVisualization.drawBounds(spatial, r);
        }

        // Draw spheres
        DebuggerVisualization.setBoundsColor(ColorRGBA.blue);
        for (i = 0; i < spheres.size(); ++i)
        {
            BoundingSphere sphere = spheres.get(i);
            DebuggerVisualization.drawBoundingSphere(sphere, r);
        }

        // Draw boxes
        DebuggerVisualization.setBoundsColor(ColorRGBA.blue);
        for (i = 0; i < boxes.size(); ++i)
        {
            BoundingBox box = boxes.get(i);
            DebuggerVisualization.drawBoundingBox(box, r);
        }

        // Draw jme normals renderables
        for (i = 0; i < jmeN.size(); ++i)
        {
            Spatial spatial = jmeN.get(i);
            DebuggerVisualization.drawNormals(spatial, r);
        }

        // Draw lines
        if (userLinesOn > 0)
        {
            userLinesOn--;
            lines.clear();
            for (i = 0; i < userLines.size(); ++i)
            {
                FastTable<Vector3f> userLine = userLines.get(i);
                lines.addAll(userLine);
            }
            if (!lines.isEmpty())
                DebuggerVisualization.drawLines(lines, r, 3.0f, ColorRGBA.yellow, ColorRGBA.yellow);
        }

        if (pinkLinesOn > 0 || pinkLinesOn < 0)
        {
            pinkLinesOn--;
            if (!pinkLines.isEmpty())
                DebuggerVisualization.drawLines(pinkLines, r, 5.0f, ColorRGBA.pink, ColorRGBA.pink);
        }

        if (cyanLinesOn > 0 || cyanLinesOn < 0)
        {
            cyanLinesOn--;
            if (!cyanLines.isEmpty())
                DebuggerVisualization.drawLines(cyanLines, r, 4.0f, ColorRGBA.cyan, ColorRGBA.cyan);
        }

        if (!customLines.isEmpty())
            DebuggerVisualization.drawLines(customLines, r, customLinesWidth, customLinesStartColor, customLinesEndColor);

        if (!offsetLines.isEmpty())
        {
            for (i = 0; i < offsetLines.size(); i++)
            {
                if (i % 2 == 1)
                    offsetLines.get(i).set(offsetLines.get(i-1).add(offsetForOffsetLines));
            }
            DebuggerVisualization.drawLines(offsetLines, r, offsetLinesWidth, offsetLinesStartColor, offsetLinesEndColor);
        }

        if (!gridLines.isEmpty())
            DebuggerVisualization.drawLines(gridLines, r, gridLinesWidth, gridLinesColor, gridLinesColor);

        // Draw orange marker spheres
        DebuggerVisualization.setBoundsColor(ColorRGBA.orange);
        for (i = 0; i <  positionMarkers.size(); ++i)
        {
            Vector3f mark = positionMarkers.get(i);
            positionMarkerSphere.setCenter(mark);
            DebuggerVisualization.drawBoundingSphere(positionMarkerSphere, r);
        }

        // Draw red marker sphere
        if (redSphereOn > 0 || redSphereOn < 0)
        {
            DebuggerVisualization.setBoundsColor(ColorRGBA.red);
            DebuggerVisualization.drawBoundingSphere(redSphere, r);
            redSphereOn--;
        }
    }

    public void setOffsetForOffsetLines(Vector3f offset) {
        offsetForOffsetLines = offset;
    }

    public void setRedSphereOff() {
        redSphereOn = 0;
    }

    /**
     * -1 to keep it on
     */
    public void setRedSphereOff(int framesDelay) {
        redSphereOn = framesDelay;
    }

    /**
     * -1 to keep it on
     */
    public void setRedSphere(PSphere red, int framesOn) {
        setRedSphere(red);
        redSphereOn = framesOn;
    }

    public void setRedSphere(PSphere red) {
        redSphere.setCenter(red.getCenterRef());
        redSphere.setRadius(red.getRadius());
        redSphereOn = 20;
    }

    /**
     * -1 to keep it on
     */
    public void setRedSphere(BoundingSphere red, int framesOn) {
        setRedSphere(red);
        redSphereOn = framesOn;
    }

    /**
     * -1 to keep it on
     */
    public void setRedSphere(Vector3f center, float radius, int framesOn) {
        redSphere.setCenter(center);
        redSphere.setRadius(radius);
        redSphereOn = framesOn;
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
        return spheres;
    }
    /** Add and remove FastTable<Vector3f> - each represents a line (potentially with multiple segments)
     *  Calling this method makes the lines visible for 100 frames
     **/
    public FastTable<FastTable<Vector3f>> getUserLines() {
        userLinesOn = 500;
        return userLines;
    }

    public void setCyanLinesOff() {
        cyanLinesOn = 0;
    }

    /**
     * -1 to keep it on
     */
    public void setCyanLinesOff(int framesDelay) {
        cyanLinesOn = framesDelay;
    }

    public void setPinkLinesOff() {
        pinkLinesOn = 0;
    }

    /**
     * -1 to keep it on
     */
    public void setPinkLinesOff(int framesDelay) {
        pinkLinesOn = framesDelay;
    }

    public FastTable<Vector3f> getCyanLines() {
        cyanLinesOn = 20;
        return cyanLines;
    }

    public FastTable<Vector3f> getCustomLines() {
        return customLines;
    }

    public FastTable<Vector3f> getOffsetLines() {
        return offsetLines;
    }

    public void setOffsetLinesColor(ColorRGBA start, ColorRGBA end) {
        offsetLinesEndColor = end;
        offsetLinesStartColor = start;
    }

    public void setOffsetLinesWidth(float width) {
        offsetLinesWidth = width;
    }

    public ColorRGBA getCustomLinesEndColor() {
        return customLinesEndColor;
    }

    public void setCustomLinesEndColor(ColorRGBA customLinesEndColor) {
        this.customLinesEndColor = customLinesEndColor;
    }

    public ColorRGBA getCustomLinesStartColor() {
        return customLinesStartColor;
    }

    public void setCustomLinesStartColor(ColorRGBA customLinesStartColor) {
        this.customLinesStartColor = customLinesStartColor;
    }

    public float getCustomLinesWidth() {
        return customLinesWidth;
    }

    public void setCustomLinesWidth(float customLinesWidth) {
        this.customLinesWidth = customLinesWidth;
    }
    
    public FastTable<Vector3f> getPinkLines() {
        pinkLinesOn = 20;
        return pinkLines;
    }

    public FastTable<Vector3f> getGridLines() {
        return gridLines;
    }

    public ColorRGBA getGridLinesColor() {
        return gridLinesColor;
    }

    public void setGridLinesColor(ColorRGBA gridLinesColor) {
        this.gridLinesColor = gridLinesColor;
    }

    public float getGridLinesWidth() {
        return gridLinesWidth;
    }

    public void setGridLinesWidth(float gridLinesWidth) {
        this.gridLinesWidth = gridLinesWidth;
    }

    public BoundingSphere getPositionMarkerSphere() {
        return positionMarkerSphere;
    }

    public FastTable<Vector3f> getPositionMarkers() {
        return positionMarkers;
    }

    public boolean isDrawOrigin() {
        return drawOrigin;
    }

    public void setDrawOrigin(boolean drawOrigin) {
        this.drawOrigin = drawOrigin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
