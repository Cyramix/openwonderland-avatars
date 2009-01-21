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
package imi.utils.input;

import imi.scene.JScene;

/**
 * Abstract base class for input mapping.
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public abstract class InputScheme 
{
    protected JScene m_jscene = null;

    abstract public void processEvents(Object[] events);

    public InputScheme(){}
    
    public InputScheme(JScene jscene)
    {
        if (jscene != null)
            m_jscene = jscene;
    }
    
    public void setJScene(JScene jscene) 
    {
        if (jscene != null)
            m_jscene = jscene;
    }
    
}
