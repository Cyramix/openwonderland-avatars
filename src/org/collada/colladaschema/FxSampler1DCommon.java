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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.03.17 at 08:38:02 AM PDT 
//


package org.collada.colladaschema;

import javolution.util.FastTable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			A one-dimensional texture sampler.
 * 			
 * 
 * <p>Java class for fx_sampler1D_common complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fx_sampler1D_common">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
 *         &lt;element name="wrap_s" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_wrap_common" minOccurs="0"/>
 *         &lt;element name="minfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="magfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="mipfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="border_color" type="{http://www.collada.org/2005/11/COLLADASchema}fx_color_common" minOccurs="0"/>
 *         &lt;element name="mipmap_maxlevel" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" minOccurs="0"/>
 *         &lt;element name="mipmap_bias" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_sampler1D_common", propOrder = {
    "source",
    "wrapS",
    "minfilter",
    "magfilter",
    "mipfilter",
    "borderColor",
    "mipmapMaxlevel",
    "mipmapBias",
    "extras"
})
public class FxSampler1DCommon {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String source;
    @XmlElement(name = "wrap_s", defaultValue = "WRAP")
    protected FxSamplerWrapCommon wrapS;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon minfilter;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon magfilter;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon mipfilter;
    @XmlList
    @XmlElement(name = "border_color", type = Double.class)
    protected List<Double> borderColor;
    @XmlElement(name = "mipmap_maxlevel", defaultValue = "0")
    protected Short mipmapMaxlevel;
    @XmlElement(name = "mipmap_bias", defaultValue = "0.0")
    protected Float mipmapBias;
    @XmlElement(name = "extra")
    protected List<Extra> extras;

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the wrapS property.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public FxSamplerWrapCommon getWrapS() {
        return wrapS;
    }

    /**
     * Sets the value of the wrapS property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public void setWrapS(FxSamplerWrapCommon value) {
        this.wrapS = value;
    }

    /**
     * Gets the value of the minfilter property.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public FxSamplerFilterCommon getMinfilter() {
        return minfilter;
    }

    /**
     * Sets the value of the minfilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public void setMinfilter(FxSamplerFilterCommon value) {
        this.minfilter = value;
    }

    /**
     * Gets the value of the magfilter property.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public FxSamplerFilterCommon getMagfilter() {
        return magfilter;
    }

    /**
     * Sets the value of the magfilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public void setMagfilter(FxSamplerFilterCommon value) {
        this.magfilter = value;
    }

    /**
     * Gets the value of the mipfilter property.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public FxSamplerFilterCommon getMipfilter() {
        return mipfilter;
    }

    /**
     * Sets the value of the mipfilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public void setMipfilter(FxSamplerFilterCommon value) {
        this.mipfilter = value;
    }

    /**
     * Gets the value of the borderColor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the borderColor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBorderColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getBorderColor() {
        if (borderColor == null) {
            borderColor = new FastTable<Double>();
        }
        return this.borderColor;
    }

    /**
     * Gets the value of the mipmapMaxlevel property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMipmapMaxlevel() {
        return mipmapMaxlevel;
    }

    /**
     * Sets the value of the mipmapMaxlevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMipmapMaxlevel(Short value) {
        this.mipmapMaxlevel = value;
    }

    /**
     * Gets the value of the mipmapBias property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMipmapBias() {
        return mipmapBias;
    }

    /**
     * Sets the value of the mipmapBias property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMipmapBias(Float value) {
        this.mipmapBias = value;
    }

    /**
     * Gets the value of the extras property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extras property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtras().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Extra }
     * 
     * 
     */
    public List<Extra> getExtras() {
        if (extras == null) {
            extras = new FastTable<Extra>();
        }
        return this.extras;
    }

}
