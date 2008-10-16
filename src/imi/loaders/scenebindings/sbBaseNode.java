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

import imi.scene.PMatrix;
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
@XmlType(name = "BaseNode", propOrder = {
    "name",
    "transform",
    "kids",
    "parentName"
})
public class sbBaseNode 
{
    @XmlElement(name = "Name", required = true)
    protected String           name;
    @XmlElement(name = "Transform")
    protected sbMatrix         transform;
    @XmlElement(name = "Kids")
    protected List<sbBaseNode> kids;
    @XmlElement(name = "ParentName")
    protected String parentName;

    public List<sbBaseNode> getKids() {
        return kids;
    }

    public void setKids(List<sbBaseNode> kids) {
        this.kids = kids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public sbMatrix getTransform() {
        return transform;
    }

    public void setTransform(PMatrix localMatrix) {
        if (transform == null)
            transform = new sbMatrix();
        transform.setRow0(new sbFloatRow(localMatrix.getRow(0)));
        transform.setRow1(new sbFloatRow(localMatrix.getRow(1)));
        transform.setRow2(new sbFloatRow(localMatrix.getRow(2)));
        transform.setRow3(new sbFloatRow(localMatrix.getRow(3)));
    }

    public void setTransform(sbMatrix transform) {
        this.transform = transform;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
}
