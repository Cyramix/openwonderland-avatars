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
package imi.serialization.xml.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkinnedMeshAdditionParams", propOrder = {
    "skinnedMeshName",
    "subGroupName",
    "owningFileName"
})
public class xmlSkinnedMeshParams
{
    @XmlElement(name = "SkinnedMeshName", required = true)
    protected String skinnedMeshName;
    @XmlElement(name = "SubGroupName")
    protected String subGroupName;
    @XmlElement(name = "OwningFileName")
    protected String owningFileName;

    public String getSkinnedMeshName() {
        return skinnedMeshName;
    }


    public void setSkinnedMeshName(String value) {
        this.skinnedMeshName = value;
    }

    public String getSubGroupName() {
        return subGroupName;
    }


    public void setSubGroupName(String value) {
        this.subGroupName = value;
    }

    public String getOwningFileName() {
        return owningFileName;
    }

    public void setOwningFileName(String value) {
        this.owningFileName = value;
    }

}
