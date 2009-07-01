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

/**
 * Contains three geometry (non-indexed) vertices.
 * @author Lou Hayt
 */
public class PGeometryTriangle
{

    public final PGeometryVertex[] verts = new PGeometryVertex[3];

    /**
     * Construct a new instance with three default verts (zero vectors).
     */
   public  PGeometryTriangle()
    {
        // Allocate space for the verts
       verts[0] = new PGeometryVertex();
       verts[1] = new PGeometryVertex();
       verts[2] = new PGeometryVertex();
    }

}

