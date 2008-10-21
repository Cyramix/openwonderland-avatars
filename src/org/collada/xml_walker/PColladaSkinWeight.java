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
package org.collada.xml_walker;



/**
 * PColladaSkinWeight class represents up to 4 vertice joint weights.
 * 
 * @author Chris Nagle
 */
public class PColladaSkinWeight
{
    public int          []m_Joints = new int[4];
    public float        []m_Weights = new float[4];



    /**
     * Default constructor.
     */
    public PColladaSkinWeight()
    {
    }

}



