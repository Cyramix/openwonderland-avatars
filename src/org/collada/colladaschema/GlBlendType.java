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

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for gl_blend_type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_blend_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ZERO"/>
 *     &lt;enumeration value="ONE"/>
 *     &lt;enumeration value="SRC_COLOR"/>
 *     &lt;enumeration value="ONE_MINUS_SRC_COLOR"/>
 *     &lt;enumeration value="DEST_COLOR"/>
 *     &lt;enumeration value="ONE_MINUS_DEST_COLOR"/>
 *     &lt;enumeration value="SRC_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_SRC_ALPHA"/>
 *     &lt;enumeration value="DST_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_DST_ALPHA"/>
 *     &lt;enumeration value="CONSTANT_COLOR"/>
 *     &lt;enumeration value="ONE_MINUS_CONSTANT_COLOR"/>
 *     &lt;enumeration value="CONSTANT_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_CONSTANT_ALPHA"/>
 *     &lt;enumeration value="SRC_ALPHA_SATURATE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum GlBlendType {

    ZERO,
    ONE,
    SRC_COLOR,
    ONE_MINUS_SRC_COLOR,
    DEST_COLOR,
    ONE_MINUS_DEST_COLOR,
    SRC_ALPHA,
    ONE_MINUS_SRC_ALPHA,
    DST_ALPHA,
    ONE_MINUS_DST_ALPHA,
    CONSTANT_COLOR,
    ONE_MINUS_CONSTANT_COLOR,
    CONSTANT_ALPHA,
    ONE_MINUS_CONSTANT_ALPHA,
    SRC_ALPHA_SATURATE;

    public String value() {
        return name();
    }

    public static GlBlendType fromValue(String v) {
        return valueOf(v);
    }

}
