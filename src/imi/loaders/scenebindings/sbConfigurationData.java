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
@XmlType(name = "ConfigurationData", propOrder = {
    "scale",
    "material",
    "localModifiers"
})
public class sbConfigurationData 
{
    @XmlElement(name = "Scale")   
    protected sbScale scale;
    
    @XmlElement(name = "Material")
    protected sbMaterial material;
    
    @XmlElement(name = "LocalModifiers")   
    protected List<sbLocalModifier> localModifiers;

    public List<sbLocalModifier> getLocalModifiers() {
        return localModifiers;
    }

    public void setLocalModifiers(List<sbLocalModifier> localModifiers) {
        this.localModifiers = localModifiers;
    }

    public sbScale getScale() {
        return scale;
    }

    public void setScale(sbScale scale) {
        this.scale = scale;
    }

    public sbMaterial getMaterial() {
        return material;
    }

    public void setMaterial(sbMaterial material) {
        this.material = material;
    }
    
}
