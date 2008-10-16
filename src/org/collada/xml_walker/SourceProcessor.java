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

import java.util.List;
import com.jme.math.Vector3f;
import org.collada.colladaschema.Accessor;
import org.collada.colladaschema.BoolArray;
import org.collada.colladaschema.FloatArray;
import org.collada.colladaschema.IDREFArray;
import org.collada.colladaschema.IntArray;
import org.collada.colladaschema.Mesh;
import org.collada.colladaschema.NameArray;
import org.collada.colladaschema.Source;
import org.collada.colladaschema.Source.TechniqueCommon;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class SourceProcessor extends Processor {
    
    private Source source = null;
    
    private enum SourceType { BOOL_ARRAY, FLOAT_ARRAY, INT_ARRAY, NAME_ARRAY, ID_REF_ARRAY };
    private SourceType sourceType = null;
    
    private float[] processedFloatArray = null;
            
    /** Creates a new instance of MeshProcessor */
    public SourceProcessor(Collada collada, Mesh mesh, Processor parent)
    {
        super(collada, mesh, parent);
        logger.info("Processing Mesh ");
        List<Source> sources = mesh.getSources();
        for(Source s : sources) {
            ProcessorFactory.createProcessor(collada, s, this);
        }
                
        ProcessorFactory.createProcessor(collada, mesh.getVertices(), this);
    }
    
    public SourceProcessor(Collada collada, Source source, Processor parent)
    {
        super(collada, source, parent);
        logger.info("Processing Source id="+source.getId()+" name="+source.getName());
        
        this.source = source;
        ElementCache.cache().putSource(source.getId(), this);
        
        BoolArray boolArray = source.getBoolArray();
        FloatArray floatArray = source.getFloatArray();
        IntArray intArray = source.getIntArray();
        NameArray nameArray = source.getNameArray();
        IDREFArray idRefArray = source.getIDREFArray();
                
        if (boolArray!=null)
            logger.info("BoolArray ");
        else if (floatArray!=null) {
            List<Double> data = floatArray.getValues();
            logger.info("FloatArray "+data.size());  
            float[] floatData = new float[data.size()];
            int i=0;
            for(Double d : data)
                floatData[i++] = d.floatValue();
            ElementCache.cache().putFloatArray(floatArray.getId(), floatData);
            sourceType = SourceType.FLOAT_ARRAY;
        } else if (intArray!=null)
            logger.info("IntArray ");
        else if (nameArray!=null)
            logger.info("NameArray ");
        else if (idRefArray!=null)
            logger.info("IdRefArray ");
        
        TechniqueCommon techCommon = source.getTechniqueCommon();
        Accessor accessor = techCommon.getAccessor();
        
        switch(sourceType) {
            case FLOAT_ARRAY :
                // TODO Check ordering of x,y,z in accessor
                break;
            default :
                logger.warning("Unimplemented SourceType "+sourceType);
        }
    }
    
//    private void processFloatArray(Accessor accessor) {
//        int size = accessor.getCount().intValue();
//        int stride = accessor.getStride().intValue();
//        float[] data = ElementCache.cache().getFloatArray(accessor.getSource());
//    }
    
    Vector3f getTuple3f(Vector3f result, int index) {
        TechniqueCommon techCommon = source.getTechniqueCommon();
        Accessor accessor = techCommon.getAccessor();
        int size = accessor.getCount().intValue();
        int stride = accessor.getStride().intValue();
        int offset = accessor.getOffset().intValue();
        float[] data = ElementCache.cache().getFloatArray(accessor.getSource());
        
        index += offset;
        result.x = data[index];
        result.y = data[index+1];
        result.z = data[index+2];
        
        return result;
    }
    
    float[] getFloatArray() {
        TechniqueCommon techCommon = source.getTechniqueCommon();
        Accessor accessor = techCommon.getAccessor();
        
        // TODO, does offset apply here ?
        
        return ElementCache.cache().getFloatArray(accessor.getSource());        
    }
}
