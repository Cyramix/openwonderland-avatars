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
 * <p>Java class for fx_surface_format_hint_range_enum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_surface_format_hint_range_enum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SNORM"/>
 *     &lt;enumeration value="UNORM"/>
 *     &lt;enumeration value="SINT"/>
 *     &lt;enumeration value="UINT"/>
 *     &lt;enumeration value="FLOAT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum FxSurfaceFormatHintRangeEnum {

    SNORM,
    UNORM,
    SINT,
    UINT,
    FLOAT;

    public String value() {
        return name();
    }

    public static FxSurfaceFormatHintRangeEnum fromValue(String v) {
        return valueOf(v);
    }

}
