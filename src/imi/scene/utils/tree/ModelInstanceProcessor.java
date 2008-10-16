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
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * ModelInstanceProcessor Class
 * @author Viet Nguyen Truong
 */
public class ModelInstanceProcessor implements NodeProcessor {
    // Class Data Members
    private static DefaultMutableTreeNode dmtTop = null;
    private static DefaultMutableTreeNode dmtCurNode = null;
    private static DefaultMutableTreeNode dmtSearchNode = null;

    // Accessors
    /**
     * Gives access to the private DefaultMutableTreeNode dmtTop
     * @return root (DefaultMutableTreeNode)
     */
    public DefaultMutableTreeNode getTopNode() {
        return dmtTop;
    }
    
    // Mutators
    /**
     * Sets the root root DefaultMutableTreeNode and the current parent 
     * DefaultMutableTreeNode
     * @param root (PNode)
     */
    public void setTopNode(PNode root) {
        dmtTop = new DefaultMutableTreeNode(root);
        dmtCurNode = dmtTop;
    }
    
    // Helper Functions
    /**
     * Recursive function to find a node in the tree
     * @param root (DefaultMutableTreeNode)
     * @param target (DefaultMutableTreeNode)
     */
    public void depthFirstPre(DefaultMutableTreeNode root, DefaultMutableTreeNode target) {
        // Process myself first
        if (this.search(root, target) == false) // Prune this branch?
            return;
        // then the kids
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode kid = ((DefaultMutableTreeNode)root.getChildAt(i));
            depthFirstPre(kid, target);
        }
    }
    
    /**
     * Compares two nodes; if nodes are the same then it sets the dmtSearchNode
     * @param start (DefaultMutableSearchNode)
     * @param target (DefaultMutableSearchNode)
     * @return true if not found
     */
    public boolean search(DefaultMutableTreeNode start, DefaultMutableTreeNode target) {
        if(start.getUserObject().equals(target.getUserObject())) {
            dmtSearchNode = start;
            return false;
        }
        return true;
    }
    
    /**
     * Adds the node into the model of the jTree
     * @param currentNode (PNode)
     * @return true
     */
    public boolean processNode(PNode currentNode) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(currentNode);
        if(dmtCurNode.getUserObject().equals(currentNode)) {
            // we skip it
        }
        else if(currentNode.getParent().equals(((PNode)dmtCurNode.getUserObject()))) {
            dmtCurNode.add(node);
            if(currentNode.getChildrenCount() > 0) {
                dmtCurNode = node;
            }
        }
        else {
            depthFirstPre(dmtTop, new DefaultMutableTreeNode(currentNode.getParent()));
            dmtSearchNode.add(node);
        }          
        return true;
    }
}
