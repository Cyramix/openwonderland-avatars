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

import imi.loaders.scenebindings.sbBaseNode;
import imi.loaders.scenebindings.sbConfigurationData;
import imi.loaders.scenebindings.sbJointNode;
import imi.loaders.scenebindings.sbLocalModifier;
import imi.loaders.scenebindings.sbMatrix;
import imi.loaders.scenebindings.sbMeshNode;
import imi.loaders.scenebindings.sbModelFile;
import imi.loaders.scenebindings.sbScale;
import imi.loaders.scenebindings.sbShaderPair;
import imi.loaders.scenebindings.sbTexture;
import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the imi.loaders.scenebindings package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory 
{
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: imi.loaders.scenebindings
     * 
     */
    public ObjectFactory() {
    }
    
    public sbBaseNode createBaseNode() {
        return new sbBaseNode();
    }
    
    public sbConfigurationData createConfigurationData() {
        return new sbConfigurationData();
    }
    
    public sbMaterial createMaterial() {
        return new sbMaterial();
    }
    
    public sbMaterial createGeometryMaterial() { // who knows, this might be required
        return new sbMaterial();
    }
    
    public sbJointNode createJointNode() {
        return new sbJointNode();
    }
    
    public sbLocalModifier createLocalModifier() {
        return new sbLocalModifier();
    }
    
    public sbMatrix createMatrix() {
        return new sbMatrix();
    }
    
    public sbMeshNode createMeshNode() {
        return new sbMeshNode();
    }
    
    public sbModelFile createModelFile() {
        return new sbModelFile();
    }
    
    public sbScale createScale() {
        return new sbScale();
    }
    
    public sbShaderPair createShaderPair() {
        return new sbShaderPair();
    }
    
    public sbTexture createTexture() {
        return new sbTexture();
    }
    
}
