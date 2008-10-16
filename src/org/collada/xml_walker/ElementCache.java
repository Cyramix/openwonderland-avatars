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
package org.collada.xml_walker;

import java.util.HashMap;

/**
 *
 * @author paulby
 */
public class ElementCache {
    
    private HashMap<String, SourceProcessor> sourceMap = new HashMap();
    private HashMap<String, VerticesProcessor> verticesMap = new HashMap();
    private HashMap<String, float[]> floatArrayMap = new HashMap();
    private HashMap<String, LightProcessor> lightMap = new HashMap();
    
    private HashMap<String, Processor> map = new HashMap();
    
    private static ElementCache elementCache=new ElementCache();
    
    private ElementCache() {
    }
    
    public static ElementCache cache() {
        return elementCache;
    }
    
    public void putSource(String id, SourceProcessor element) {
        //System.out.println("---> adding source "+id);
        sourceMap.put(id, element);
    }
    
    public SourceProcessor getSource(String id) {
        return sourceMap.get(trim(id));
    }
    
    public void putVertices(String id, VerticesProcessor element) {
        verticesMap.put(id, element);
    }
    
    public VerticesProcessor getVertices(String id) {
        return verticesMap.get(trim(id));
    }

    public void putFloatArray(String id, float[] floatArray) {
        assert(id!=null);
        floatArrayMap.put(id, floatArray);
    }
    
    public float[] getFloatArray(String id) {
        return floatArrayMap.get(trim(id));
    }
    
    public void putLight(String id, LightProcessor light) {
        lightMap.put(id, light);
    }
    
    public LightProcessor getLight(String id) {
        return lightMap.get(trim(id));
    }
    
    public void put(String id, Processor proc) {
        map.put(id, proc);
    }
    
    public Processor get(String id) {
        return map.get(trim(id));
    }
    
    /**
     *  Trim leading # 
     */
    private String trim(String id) {
        return id.substring(1, id.length());
    }
    

}
