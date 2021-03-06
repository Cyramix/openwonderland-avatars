//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.17 at 12:30:48 PM EST 
//


package imi.serialization.xml.bindings;

import com.jme.renderer.ColorRGBA;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for xmlColorRGBA complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="xmlColorRGBA">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="red" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="green" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="blue" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="alpha" type="{http://www.w3.org/2001/XMLSchema}float" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ColorRGBA")
public class xmlColorRGBA {

    @XmlAttribute
    protected Float red;
    @XmlAttribute
    protected Float green;
    @XmlAttribute
    protected Float blue;
    @XmlAttribute
    protected Float alpha;

    // Constructicon
    public xmlColorRGBA() // Default needed by jaxb
    {

    }

    public xmlColorRGBA(ColorRGBA color)
    {
        this.setColor(color);
    }
    /**
     * Gets the value of the red property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getRed() {
        return red;
    }

    /**
     * Sets the value of the red property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setRed(Float value) {
        this.red = value;
    }

    /**
     * Gets the value of the green property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getGreen() {
        return green;
    }

    /**
     * Sets the value of the green property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setGreen(Float value) {
        this.green = value;
    }

    /**
     * Gets the value of the blue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getBlue() {
        return blue;
    }

    /**
     * Sets the value of the blue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setBlue(Float value) {
        this.blue = value;
    }

    /**
     * Gets the value of the alpha property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getAlpha() {
        return alpha;
    }

    /**
     * Sets the value of the alpha property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setAlpha(Float value) {
        this.alpha = value;
    }

    // Convenience methods
    public void setColor(ColorRGBA color)
    {
        red = color.r;
        green = color.g;
        blue = color.b;
        alpha = color.a;
    }

    public void setColor(float r, float g, float b, float a)
    {
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }

    public ColorRGBA getColorRGBA()
    {
        return new ColorRGBA(red, green, blue, alpha);
    }

}
