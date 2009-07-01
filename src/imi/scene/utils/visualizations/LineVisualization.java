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

import com.jme.scene.Line;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.util.geom.BufferUtils;
import javolution.util.FastTable;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Quick and dirty debug rendering
 * @author Lou Hayt
 */
@ExperimentalAPI
public class LineVisualization 
{
    /** Root of the object **/
    Node objectRoot = null;
    /** Reference to the overall object's position **/
    FastTable<Vector3f> origin = null;
    FastTable<Vector3f> point  = null;
    /** The visualization **/
    Line line = null;
    
    /**
     * Construct a new visualization object.
     * @param verletObject
     */
    public LineVisualization(FastTable<Vector3f> origin, FastTable<Vector3f> point, ColorRGBA color, float width)
    {
        this.origin = origin;
        this.point  = point;
        initialize(color, width);
    }
    
    public LineVisualization(FastTable<Vector3f> path, ColorRGBA color, float width) 
    {
        this.origin = new FastTable<Vector3f>();
        this.point  = new FastTable<Vector3f>();
        for (int i = 1; i < path.size(); i++)
        {
            origin.add(path.get(i-1));
            point.add(path.get(i));
        }
        initialize(color, width);
    }

    public void initialize(ColorRGBA color, float width)
    {
        objectRoot = new Node("Line visu");
        objectRoot.setLocalTranslation(Vector3f.ZERO);
        
        // clear out the old
        objectRoot.detachAllChildren();
        System.out.println();
        // quick and ugly
        int index = 0;
        Vector3f [] linePoint = new Vector3f [origin.size()+point.size()];
        for (int i = 0; i < origin.size(); i++)
        {
            linePoint[index] = origin.get(i);
            index++;
            linePoint[index] = point.get(i);
            index++;
        }
        line = new Line("Line visu", linePoint, null, null, null);
        line.reconstruct(BufferUtils.createFloatBuffer( linePoint  ), null, null, null);
        line.setDefaultColor(color);
        line.setLineWidth(width);
        line.setMode(Line.Mode.Segments);
        line.setAntialiased(true);

        // Attach the sphere to the scene root
        objectRoot.attachChild(line);
    }
    
    public void updatePositions()
    {
    }

}
