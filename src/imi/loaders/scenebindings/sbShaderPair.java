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
 * This file represents a shader pair entry at run-time
 * @author Ronald E Dahlgren
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShaderPair", propOrder = {
    "vertexShaderPath",
    "fragmentShaderPath"
})
public class sbShaderPair 
{
    @XmlElement(name = "VertexShaderPath", required = true)
    private String vertexShaderPath = new String();
    @XmlElement(name = "FragmentShaderPath", required = true)
    private String fragmentShaderPath = new String();

    public sbShaderPair()
    {
        // do nothing
    }

    public sbShaderPair(String vertShader, String fragShader)
    {
        vertexShaderPath = vertShader;
        fragmentShaderPath = fragShader;
    }

    public String getFragmentShaderPath() {
        return fragmentShaderPath;
    }

    public void setFragmentShaderPath(String fragmentShaderPath) {
        this.fragmentShaderPath = fragmentShaderPath;
    }

    public String getVertexShaderPath() {
        return vertexShaderPath;
    }

    public void setVertexShaderPath(String vertexShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
    }
    
}
