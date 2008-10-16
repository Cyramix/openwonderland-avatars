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
package imi.gui;

import imi.scene.PNode;

/**
 * This interface provides a means of using a toolkit designed for a 
 * concrete subtype of PNode
 * @author Ronald E Dahlgren
 */
public interface PNodeSubtypeToolkit 
{
    /**
     * The "Target" of a toolkit is defined as the node instance that will
     * be operated on.
     * @return PNode
     */
    public PNode    getTarget();
 
    /**
     * The "Target" of a toolkit is defined as the node instance that will
     * be operated on.
     * @param targetNode
     */
    public void     setTarget(PNode targetNode);
    /**
     * 
     * The Default value represents a "safe" value to reset back 
     * to should the need arise.The semantic meaning is user defined.
     * @return int (A Width value)
     */
    public Integer  getDefaultWidth();
    public void     setDefaultWidth(Integer width);
    
    /**
     * 
     * The Default value represents a "safe" value to reset back 
     * to should the need arise.The semantic meaning is user defined.
     * @return int (A Height value)
     */
    public Integer  getDefaultHeight();
    public void     setDefaultHeight(Integer height);
    
    /**
     * The current value of the instance's height.
     * @return The current Height value.
     */
    public Integer  getInstanceHeight();
    public void     setInstanceHeight(Integer height);
    
    /**
     * The current value of the instance's width
     * @return The current width value
     */
    public Integer  getInstanceWidth();
    public void     setInstanceWidth(Integer width);
}
