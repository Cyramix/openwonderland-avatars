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
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class LineVisualization 
{
    /** Root of the object **/
    Node objectRoot = null;
    /** Reference to the overall object's position **/
    ArrayList<Vector3f> origin = null;
    ArrayList<Vector3f> point  = null;
    /** The visualization **/
    Line line = null;
    
    /**
     * Construct a new visualization object.
     * @param verletObject
     */
    public LineVisualization(ArrayList<Vector3f> origin, ArrayList<Vector3f> point, ColorRGBA color, float width) 
    {
        objectRoot = new Node("Line visu");
        this.origin = origin;
        this.point = point;
        objectRoot.setLocalTranslation(Vector3f.ZERO);
        
        // clear out the old
        objectRoot.detachAllChildren();
        
        // quick and ugly
        int index = 0;
        Vector3f [] linePoint = new Vector3f [origin.size()+point.size()];
        for (int i = 0; i < origin.size(); i++)
        {
            linePoint[index] = origin.get(i); index++;
            linePoint[index] = point.get(i);  index++;
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
