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

import org.collada.colladaschema.Light;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class LightProcessor extends Processor {

    public LightProcessor(Collada collada, Light light, Processor parent)
    {
        super(collada, light, parent);
        
        String id = light.getId();
        if (id!=null) {
            ElementCache.cache().putLight(id, this);
        }
        
        //logger.warning("LightProcessor not implemented");
    }

}
