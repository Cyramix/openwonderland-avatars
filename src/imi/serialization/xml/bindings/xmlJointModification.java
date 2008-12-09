//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.09 at 04:46:46 PM EST 
//


package imi.serialization.xml.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for xmlJointModification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="xmlJointModification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TargetJointName">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="LocalModifierMatrix" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}Matrix" minOccurs="0"/>
 *         &lt;element name="SkeletonModifierMatrix" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}Matrix" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JointModification", propOrder = {
    "targetJointName",
    "localModifierMatrix",
    "skeletonModifierMatrix"
})
public class xmlJointModification {

    @XmlElement(name = "TargetJointName", required = true)
    protected String targetJointName;
    @XmlElement(name = "LocalModifierMatrix")
    protected xmlMatrix localModifierMatrix;
    @XmlElement(name = "SkeletonModifierMatrix")
    protected xmlMatrix skeletonModifierMatrix;

    /**
     * Gets the value of the targetJointName property.
     * 
     * @return
     *     possible object is
     *     {@link xmlJointModification.TargetJointName }
     *     
     */
    public String getTargetJointName() {
        return targetJointName;
    }

    /**
     * Sets the value of the targetJointName property.
     * 
     * @param value
     *     allowed object is
     *     {@link xmlJointModification.TargetJointName }
     *     
     */
    public void setTargetJointName(String value) {
        this.targetJointName = value;
    }

    /**
     * Gets the value of the localModifierMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link Matrix }
     *     
     */
    public xmlMatrix getLocalModifierMatrix() {
        return localModifierMatrix;
    }

    /**
     * Sets the value of the localModifierMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link Matrix }
     *     
     */
    public void setLocalModifierMatrix(xmlMatrix value) {
        this.localModifierMatrix = value;
    }

    /**
     * Gets the value of the skeletonModifierMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link Matrix }
     *     
     */
    public xmlMatrix getSkeletonModifierMatrix() {
        return skeletonModifierMatrix;
    }

    /**
     * Sets the value of the skeletonModifierMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link Matrix }
     *     
     */
    public void setSkeletonModifierMatrix(xmlMatrix value) {
        this.skeletonModifierMatrix = value;
    }
}
