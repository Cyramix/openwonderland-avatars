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
 * This class contains four float rows, representing the rows in the matrix.
 * Convenient accessing functions are provided.
 * @author Ronald E Dahlgren
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Matrix", propOrder = {
    "row0",
    "row1",
    "row2",
    "row3"
})
public class sbMatrix 
{
    @XmlElement(name = "Row0", required = true)
    private sbFloatRow  row0 = new sbFloatRow();
    @XmlElement(name = "Row1", required = true)
    private sbFloatRow  row1 = new sbFloatRow();
    @XmlElement(name = "Row2", required = true)
    private sbFloatRow  row2 = new sbFloatRow();
    @XmlElement(name = "Row3", required = true)
    private sbFloatRow  row3 = new sbFloatRow();
    
    // C-tors
    public sbMatrix()
    {
        // Do nothing
    }


    // Accessors
    public void set(sbFloatRow row0, sbFloatRow row1, sbFloatRow row2, sbFloatRow row3)
    {
        row0.set(row0);
        row1.set(row1);
        row2.set(row2);
        row3.set(row3);
    }

    public sbFloatRow getRow0() {
        return row0;
    }

    public void setRow0(sbFloatRow m_Row0) {
        this.row0 = m_Row0;
    }

    public sbFloatRow getRow1() {
        return row1;
    }

    public void setRow1(sbFloatRow m_Row1) {
        this.row1 = m_Row1;
    }

    public sbFloatRow getRow2() {
        return row2;
    }

    public void setRow2(sbFloatRow m_Row2) {
        this.row2 = m_Row2;
    }

    public sbFloatRow getRow3() {
        return row3;
    }

    public void setRow3(sbFloatRow m_Row3) {
        this.row3 = m_Row3;
    }
    
    public PMatrix asPMatrix()
    {
        float [] fArray = new float [16];
        fArray[ 0] = this.row0.getX();
        fArray[ 1] = this.row0.getY();
        fArray[ 2] = this.row0.getZ();
        fArray[ 3] = this.row0.getW();

        fArray[ 4] = this.row1.getX();
        fArray[ 5] = this.row1.getY();
        fArray[ 6] = this.row1.getZ();
        fArray[ 7] = this.row1.getW();

        fArray[ 8] = this.row2.getX();
        fArray[ 9] = this.row2.getY();
        fArray[10] = this.row2.getZ();
        fArray[11] = this.row2.getW();

        fArray[12] = this.row3.getX();
        fArray[13] = this.row3.getY();
        fArray[14] = this.row3.getZ();
        fArray[15] = this.row3.getW();
        return new PMatrix(fArray);
    }


}
