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
 * This is the run-time representation of an xml entry for a joint's
 * local modifier matrix
 * @author Ronald E Dahlgren
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocalModifier", propOrder = {
    "targetJointName",
    "transform"
})
public class sbLocalModifier 
{
    @XmlElement(name = "TargetJointName", required = true)
    protected String    targetJointName = new String();
    @XmlElement(name = "Transform", required = true)
    protected sbMatrix  transform = new sbMatrix();

    public sbLocalModifier()
    {
        // Do nothing!
    }

    public String getTargetJointName() {
        return targetJointName;
    }

    public void setTargetJointName(String targetJointName) {
        this.targetJointName = targetJointName;
    }

    public sbMatrix getTransform() {
        return transform;
    }

    public void setTransform(sbMatrix transform) {
        this.transform = transform;
    }

}
