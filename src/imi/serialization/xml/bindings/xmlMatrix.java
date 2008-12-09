//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.08 at 12:40:05 PM EST 
//


package imi.serialization.xml.bindings;

import imi.scene.PMatrix;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for xmlMatrix complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="xmlMatrix">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RowOne" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}xmlFloatRow"/>
 *         &lt;element name="RowTwo" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}xmlFloatRow"/>
 *         &lt;element name="RowThree" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}xmlFloatRow"/>
 *         &lt;element name="RowFour" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}xmlFloatRow"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Matrix", propOrder = {
    "rowOne",
    "rowTwo",
    "rowThree",
    "rowFour"
})
public class xmlMatrix {

    @XmlElement(name = "RowOne", required = true)
    protected xmlFloatRow rowOne;
    @XmlElement(name = "RowTwo", required = true)
    protected xmlFloatRow rowTwo;
    @XmlElement(name = "RowThree", required = true)
    protected xmlFloatRow rowThree;
    @XmlElement(name = "RowFour", required = true)
    protected xmlFloatRow rowFour;

    /**
     * Gets the value of the rowOne property.
     * 
     * @return
     *     possible object is
     *     {@link xmlFloatRow }
     *     
     */
    public xmlFloatRow getRowOne() {
        return rowOne;
    }

    /**
     * Sets the value of the rowOne property.
     * 
     * @param value
     *     allowed object is
     *     {@link xmlFloatRow }
     *     
     */
    public void setRowOne(xmlFloatRow value) {
        this.rowOne = value;
    }

    /**
     * Gets the value of the rowTwo property.
     * 
     * @return
     *     possible object is
     *     {@link xmlFloatRow }
     *     
     */
    public xmlFloatRow getRowTwo() {
        return rowTwo;
    }

    /**
     * Sets the value of the rowTwo property.
     * 
     * @param value
     *     allowed object is
     *     {@link xmlFloatRow }
     *     
     */
    public void setRowTwo(xmlFloatRow value) {
        this.rowTwo = value;
    }

    /**
     * Gets the value of the rowThree property.
     * 
     * @return
     *     possible object is
     *     {@link xmlFloatRow }
     *     
     */
    public xmlFloatRow getRowThree() {
        return rowThree;
    }

    /**
     * Sets the value of the rowThree property.
     * 
     * @param value
     *     allowed object is
     *     {@link xmlFloatRow }
     *     
     */
    public void setRowThree(xmlFloatRow value) {
        this.rowThree = value;
    }

    /**
     * Gets the value of the rowFour property.
     * 
     * @return
     *     possible object is
     *     {@link xmlFloatRow }
     *     
     */
    public xmlFloatRow getRowFour() {
        return rowFour;
    }

    /**
     * Sets the value of the rowFour property.
     * 
     * @param value
     *     allowed object is
     *     {@link xmlFloatRow }
     *     
     */
    public void setRowFour(xmlFloatRow value) {
        this.rowFour = value;
    }

    // Convenience methods
    public void set(PMatrix mat)
    {
        float[] matrix = mat.getFloatArray();

        xmlFloatRow row = new xmlFloatRow();
        row.setValues(matrix[ 0], matrix[ 1], matrix[ 2], matrix[ 3]);
        rowOne = row;

        row = new xmlFloatRow();
        row.setValues(matrix[ 4], matrix[ 5], matrix[ 6], matrix[ 7]);
        rowTwo = row;

        row = new xmlFloatRow();
        row.setValues(matrix[ 8], matrix[ 9], matrix[10], matrix[11]);
        rowThree = row;

        row = new xmlFloatRow();
        row.setValues(matrix[12], matrix[13], matrix[14], matrix[15]);
        rowFour = row;
    }

    public PMatrix getPMatrix()
    {
        float[] fArray = new float[16];
        fArray[ 0] = rowOne.x;
        fArray[ 1] = rowOne.y;
        fArray[ 2] = rowOne.z;
        fArray[ 3] = rowOne.w;
        fArray[ 4] = rowTwo.x;
        fArray[ 5] = rowTwo.y;
        fArray[ 6] = rowTwo.z;
        fArray[ 7] = rowTwo.w;
        fArray[ 8] = rowThree.x;
        fArray[ 9] = rowThree.y;
        fArray[10] = rowThree.z;
        fArray[11] = rowThree.w;
        fArray[12] = rowFour.x;
        fArray[13] = rowFour.y;
        fArray[14] = rowFour.z;
        fArray[15] = rowFour.w;
        PMatrix result = new PMatrix(fArray);
        return result;
    }

}
