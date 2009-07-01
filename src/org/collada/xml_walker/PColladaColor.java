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
package org.collada.xml_walker;

import com.jme.renderer.ColorRGBA;

/**
 *
 * @author Chris Nagle
 */
public class PColladaColor
{
    public float red = 1.0f;
    public float green = 1.0f;
    public float blue = 1.0f;
    public float alpha = 1.0f;



    /**
     * Default constructor.
     */
    public PColladaColor()
    {
    }

    /**
     * Constructor.
     * @param fRed
     * @param fGreen
     * @param fBlue
     * @param fAlpha
     */
    public PColladaColor(float fRed, float fGreen, float fBlue, float fAlpha)
    {
        red = fRed;
        green = fGreen;
        blue = fBlue;
        alpha = fAlpha;
    }



    /**
     * Sets the fields of the PColladaColor.  Alpha is set to 1.0f.
     * @param fRed
     * @param fGreen
     * @param fBlue
     */
    public void set(float fRed, float fGreen, float fBlue)
    {
        red = fRed;
        green = fGreen;
        blue = fBlue;
        alpha = 1.0f;
    }

    /**
     * Sets the fields of the PColladaColor.
     * @param fRed
     * @param fGreen
     * @param fBlue
     * @param fAlpha
     */
    public void set(float fRed, float fGreen, float fBlue, float fAlpha)
    {
        red = fRed;
        green = fGreen;
        blue = fBlue;
        alpha = fAlpha;
    }

    ColorRGBA toColorRGBA()
    {
        return new ColorRGBA(red, green, blue, alpha);
    }
}




