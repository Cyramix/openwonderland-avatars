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
 *
 * @author Chris Nagle
 */
public class PColladaColor
{
    public float Red = 1.0f;
    public float Green = 1.0f;
    public float Blue = 1.0f;
    public float Alpha = 1.0f;



    //  Constructor.
    public PColladaColor()
    {
    }

    public PColladaColor(float fRed, float fGreen, float fBlue, float fAlpha)
    {
        Red = fRed;
        Green = fGreen;
        Blue = fBlue;
        Alpha = fAlpha;
    }



    public void set(float fRed, float fGreen, float fBlue)
    {
        Red = fRed;
        Green = fGreen;
        Blue = fBlue;
        Alpha = 1.0f;
    }

    public void set(float fRed, float fGreen, float fBlue, float fAlpha)
    {
        Red = fRed;
        Green = fGreen;
        Blue = fBlue;
        Alpha = fAlpha;
    }
}




