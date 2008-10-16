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
package imi.scene.utils.tree;

import imi.scene.PNode;

/**
 * This is the interface needed to process a node during a tree traversal.
 * @author Ronald E Dahlgren
 */
public interface NodeProcessor 
{
    /**
     * This method processes a given node using any logic that is needed.
     * The return value is used by the TreeTraverser to determine whether or
     * not a branch should be pruned. This is useful for traversing (for instance)
     * only the joints in animation data and not bothering with any meshes
     * that may be hooked onto it.
     * @param currentNode The node currently being processed
     * @return false = PRUNE BRANCH, true = NORMAL PROCESSING
     */
    abstract public boolean processNode(PNode currentNode);
}
