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
package imi.loaders.scenebindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a texture within an XML file.
 * @author Ronald E Dahlgren
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Texture", propOrder = {
    "path",
    "textureUnit"
})
public class sbTexture
{
    @XmlElement(name = "Path", required = true)
    private String  path = new String();
    @XmlElement(name = "TextureUnit", required = true)
    private int     textureUnit = 0;

    public sbTexture()
    {
        // Do nothing
    }

    public sbTexture(String texture, int unit)
    {
        path = texture;
        textureUnit = unit;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public void setTextureUnit(int textureUnit) {
        this.textureUnit = textureUnit;
    }

    
}
