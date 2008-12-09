//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.09 at 04:46:46 PM EST 
//


package imi.serialization.xml.bindings;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for xmlCharacter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="xmlCharacter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Attributes" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}CharacterAttributes"/>
 *         &lt;element name="SkeletonDetails" type="{http://xml.netbeans.org/schema/CharacterXMLSchema}xmlJointModification" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Character", propOrder = {
    "attributes",
    "skeletonDetails"
})
public class xmlCharacter {

    @XmlElement(name = "Attributes", required = true)
    protected xmlCharacterAttributes attributes;
    @XmlElement(name = "SkeletonDetails")
    protected List<xmlJointModification> skeletonDetails;

    public void addJointModification(xmlJointModification jMod) {
        if (skeletonDetails == null)
            skeletonDetails = new ArrayList<xmlJointModification>();
        skeletonDetails.add(jMod);
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link CharacterAttributes }
     *     
     */
    public xmlCharacterAttributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link CharacterAttributes }
     *     
     */
    public void setAttributes(xmlCharacterAttributes value) {
        this.attributes = value;
    }

    /**
     * Gets the value of the skeletonDetails property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the skeletonDetails property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSkeletonDetails().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link xmlJointModification }
     * 
     * 
     */
    public List<xmlJointModification> getSkeletonDetails() {
        if (skeletonDetails == null) {
            skeletonDetails = new ArrayList<xmlJointModification>();
        }
        return this.skeletonDetails;
    }

}