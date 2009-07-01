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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}vertices"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}lines"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}linestrips"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}polygons"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}polylist"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}triangles"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}trifans"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tristrips"/>
 *         &lt;/choice>
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
@XmlType(name = "", propOrder = {
    "sources",
    "vertices",
    "trianglesAndLinestripsAndPolygons",
    "extras"
})
@XmlRootElement(name = "mesh")
public class Mesh {

    @XmlElement(name = "source", required = true)
    protected List<Source> sources;
    @XmlElement(required = true)
    protected Vertices vertices;
    @XmlElements({
        @XmlElement(name = "linestrips", type = Linestrips.class),
        @XmlElement(name = "trifans", type = Trifans.class),
        @XmlElement(name = "triangles", type = Triangles.class),
        @XmlElement(name = "tristrips", type = Tristrips.class),
        @XmlElement(name = "polylist", type = Polylist.class),
        @XmlElement(name = "polygons", type = Polygons.class),
        @XmlElement(name = "lines", type = Lines.class)
    })
    protected List<Object> trianglesAndLinestripsAndPolygons;
    @XmlElement(name = "extra")
    protected List<Extra> extras;

    /**
     * 
     * 						The mesh element must contain one or more source elements.
     * 						Gets the value of the sources property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sources property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSources().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Source }
     * 
     * 
     */
    public List<Source> getSources() {
        if (sources == null) {
            sources = new FastTable<Source>();
        }
        return this.sources;
    }

    /**
     * 
     * 						The mesh element must contain one vertices element.
     * 						
     * 
     * @return
     *     possible object is
     *     {@link Vertices }
     *     
     */
    public Vertices getVertices() {
        return vertices;
    }

    /**
     * 
     * 						The mesh element must contain one vertices element.
     * 						
     * 
     * @param value
     *     allowed object is
     *     {@link Vertices }
     *     
     */
    public void setVertices(Vertices value) {
        this.vertices = value;
    }

    /**
     * 
     * 							The mesh element may contain any number of triangles elements.
     * 							Gets the value of the trianglesAndLinestripsAndPolygons property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trianglesAndLinestripsAndPolygons property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrianglesAndLinestripsAndPolygons().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Linestrips }
     * {@link Trifans }
     * {@link Triangles }
     * {@link Tristrips }
     * {@link Polylist }
     * {@link Polygons }
     * {@link Lines }
     * 
     * 
     */
    public List<Object> getTrianglesAndLinestripsAndPolygons() {
        if (trianglesAndLinestripsAndPolygons == null) {
            trianglesAndLinestripsAndPolygons = new FastTable<Object>();
        }
        return this.trianglesAndLinestripsAndPolygons;
    }

    /**
     * 
     * 						The extra element may appear any number of times.
     * 						Gets the value of the extras property.
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
