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
import org.collada.colladaschema.InputLocal;
import org.collada.colladaschema.Vertices;

import imi.loaders.collada.Collada;




/**
 *
 * @author paulby
 */
public class VerticesProcessor extends Processor {
    
    private SourceProcessor positionSource = null;
    
    /** Creates a new instance of VerticesProcessor */
    public VerticesProcessor(Collada collada, Vertices vert, Processor parent)
    {
        super(collada, vert, parent);
        logger.info("Processing Vertices");
        ElementCache.cache().putVertices(vert.getId(), this);
        List<InputLocal> inputs = vert.getInputs();
        for(InputLocal input : inputs) {
            logger.info("Semantic "+input.getSemantic()+"  source "+input.getSource());
            if (input.getSemantic().equals("POSITION"))
                positionSource = ElementCache.cache().getSource(input.getSource());
            else
                logger.warning("Ignoring vertices semantic "+input.getSemantic());
        }
    }
    
    Vector3f getTuple3f(Vector3f result, int index) {
        return positionSource.getTuple3f(result, index);
    }
    
    float[] getFloatArray() {
        return positionSource.getFloatArray();
    }
    
}
