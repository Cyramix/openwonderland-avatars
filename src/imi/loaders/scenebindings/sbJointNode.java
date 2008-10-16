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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Lou Hayt
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JointNode", propOrder = {
    "modifer"
})
public class sbJointNode extends sbBaseNode
{
    @XmlElement(name = "Modifier")
    protected sbMatrix modifer;

    public sbMatrix getModifer() {
        return modifer;
    }

    public void setModifer(sbMatrix modifer) {
        this.modifer = modifer;
    }
    
    // PMatrix overload
    public void setModifier(PMatrix modifier)
    {
        if (modifer == null)
            modifer = new sbMatrix();
        modifer.setRow0(new sbFloatRow(modifier.getRow(0)));
        modifer.setRow1(new sbFloatRow(modifier.getRow(1)));
        modifer.setRow2(new sbFloatRow(modifier.getRow(2)));
        modifer.setRow3(new sbFloatRow(modifier.getRow(3)));
    }
    
}
