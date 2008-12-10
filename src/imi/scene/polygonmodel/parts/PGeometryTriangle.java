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
package imi.scene.polygonmodel.parts;


import com.jme.math.Vector3f;


public class PGeometryTriangle
{
    public PMeshMaterial m_pMaterial    = null;
    public Vector3f m_Normal            = new Vector3f();
    public PGeometryVertex []m_Vertices = new PGeometryVertex[3];



    
    //  Constructor.
   public  PGeometryTriangle()
    {
        // Allocate space for the verts
       m_Vertices[0] = new PGeometryVertex();
       m_Vertices[1] = new PGeometryVertex();
       m_Vertices[2] = new PGeometryVertex();
    }

}



