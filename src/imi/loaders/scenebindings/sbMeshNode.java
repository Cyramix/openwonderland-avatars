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

import imi.loaders.scenebindings.sbMaterial;
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
@XmlType(name = "MeshNode", propOrder = {
    "geometryFile",
    "geometryMaterial",
    "configuration",
    "skinningHooks"
})
public class sbMeshNode extends sbBaseNode
{
    @XmlElement(name = "GeometryFile", required = true)
    protected String geometryFile;
    @XmlElement(name = "GeometryMaterial")
    protected sbMaterial geometryMaterial;
    @XmlElement(name = "Configuration")
    protected sbConfigurationData configuration;
    @XmlElement(name = "SkinningHooks")
    protected List<sbBaseNode> skinningHooks;

    public String getGeometryFile() {
        return geometryFile;
    }

    public void setGeometryFile(String geometryFile) {
        this.geometryFile = geometryFile;
    }

    public sbConfigurationData getConfiguration() {
        return configuration;
    }

    public void setConfiguration(sbConfigurationData configuration) {
        this.configuration = configuration;
    }

    public sbMaterial getGeometryMaterial() {
        return geometryMaterial;
    }

    public void setGeometryMaterial(sbMaterial geometryMaterial) {
        this.geometryMaterial = geometryMaterial;
    }

    public List<sbBaseNode> getSkinningHooks() {
        return skinningHooks;
    }

    public void setSkinningHooks(List<sbBaseNode> skinningHooks) {
        this.skinningHooks = skinningHooks;
    }
    
}
