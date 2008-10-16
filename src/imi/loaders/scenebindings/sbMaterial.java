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

import imi.loaders.scenebindings.sbShaderPair;
import imi.loaders.scenebindings.sbTexture;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Lou Hayt
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Material", propOrder = {
    "textureFiles",
    "shaderPair"
})
public class sbMaterial 
{
    @XmlElement(name = "TextureFiles")   
    protected List<sbTexture> textureFiles;
    @XmlElement(name = "ShaderPair")
    protected sbShaderPair shaderPair;

    public sbShaderPair getShaderPair() {
        return shaderPair;
    }

    public void setShaderPair(sbShaderPair shaderPair) {
        this.shaderPair = shaderPair;
    }

    public List<sbTexture> getTextureFiles() {
        return textureFiles;
    }

    public void setTextureFiles(List<sbTexture> textureFiles) {
        this.textureFiles = textureFiles;
    }

}
