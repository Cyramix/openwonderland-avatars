//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.17 at 12:30:48 PM EST 
//


package imi.serialization.xml.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for xmlTextureAttributes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="xmlTextureAttributes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="URL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TextureUnit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="WrapS" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WrapT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AlphaCombiner" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MinificationFilter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MagnificationFilter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AnisotropicValue" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="TextureApplyMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextureAttributes", propOrder = {
    "url",
    "textureUnit",
    "wrapS",
    "wrapT",
    "alphaCombiner",
    "minificationFilter",
    "magnificationFilter",
    "anisotropicValue",
    "textureApplyMode"
})
public class xmlTextureAttributes {

    @XmlElement(name = "URL", required = true)
    protected String url;
    @XmlElement(name = "TextureUnit")
    protected int textureUnit;
    @XmlElement(name = "WrapS", required = true)
    protected String wrapS;
    @XmlElement(name = "WrapT", required = true)
    protected String wrapT;
    @XmlElement(name = "AlphaCombiner", required = true)
    protected String alphaCombiner;
    @XmlElement(name = "MinificationFilter", required = true)
    protected String minificationFilter;
    @XmlElement(name = "MagnificationFilter", required = true)
    protected String magnificationFilter;
    @XmlElement(name = "AnisotropicValue")
    protected float anisotropicValue;
    @XmlElement(name = "TextureApplyMode", required = true)
    protected String textureApplyMode;

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setURL(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the textureUnit property.
     * 
     */
    public int getTextureUnit() {
        return textureUnit;
    }

    /**
     * Sets the value of the textureUnit property.
     * 
     */
    public void setTextureUnit(int value) {
        this.textureUnit = value;
    }

    /**
     * Gets the value of the wrapS property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWrapS() {
        return wrapS;
    }

    /**
     * Sets the value of the wrapS property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWrapS(String value) {
        this.wrapS = value;
    }

    /**
     * Gets the value of the wrapT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWrapT() {
        return wrapT;
    }

    /**
     * Sets the value of the wrapT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWrapT(String value) {
        this.wrapT = value;
    }

    /**
     * Gets the value of the alphaCombiner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlphaCombiner() {
        return alphaCombiner;
    }

    /**
     * Sets the value of the alphaCombiner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlphaCombiner(String value) {
        this.alphaCombiner = value;
    }

    /**
     * Gets the value of the minificationFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinificationFilter() {
        return minificationFilter;
    }

    /**
     * Sets the value of the minificationFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinificationFilter(String value) {
        this.minificationFilter = value;
    }

    /**
     * Gets the value of the magnificationFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMagnificationFilter() {
        return magnificationFilter;
    }

    /**
     * Sets the value of the magnificationFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMagnificationFilter(String value) {
        this.magnificationFilter = value;
    }

    /**
     * Gets the value of the anisotropicValue property.
     * 
     */
    public float getAnisotropicValue() {
        return anisotropicValue;
    }

    /**
     * Sets the value of the anisotropicValue property.
     * 
     */
    public void setAnisotropicValue(float value) {
        this.anisotropicValue = value;
    }

    /**
     * Gets the value of the textureApplyMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureApplyMode() {
        return textureApplyMode;
    }

    /**
     * Sets the value of the textureApplyMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureApplyMode(String value) {
        this.textureApplyMode = value;
    }

}