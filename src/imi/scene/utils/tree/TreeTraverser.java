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
package imi.scene.utils.tree;

import imi.scene.PNode;
import java.util.LinkedList;

/**
 * This class will traverse the scene graph using the specified traversal
 * algorithm and process each node along the way with the specified object's
 * <code>processNode(PNode current)</code> method.
 * @author Ronald E Dahlgren
 */
public class TreeTraverser 
{
    /////////////// WARNING ////////////////////
    // These methods *MAY* fail when static and
    // used by multiple threads. Need to look
    // into the possibility!
    ///////////////////////////////////////////
    /**
     * This method traverses the subtree (Breadth First) formed from <code>root</code> and
     * performs the <code>processor.processNode(current)</code> method on 
     * each <code>PNode</code> that is encountered.
     * @param root The root of the tree
     * @param processor Defines how to handle each node
     */
    static public void breadthFirst(PNode root, NodeProcessor processor)
    {
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(root);
        
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him!
            if (processor.processNode(current) == false) // Prune this branch?
                continue;
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
    }
    
    /**
     * This method traverses the subtree (Depth First, self then child) formed from <code>root</code> and
     * performs the <code>processor.processNode(current)</code> method on 
     * each <code>PNode</code> that is encountered.
     * @param root The root of the tree
     * @param processor Defines how to handle each node
     */
    static public void depthFirstPre(PNode root, NodeProcessor processor)
    {
        // Process myself first
        if (processor.processNode(root) == false) // Prune this branch?
            return;
        // then the kids
        for (PNode kid : root.getChildren())
            depthFirstPre(kid, processor);
    }
    
    /**
     * This method traverses the subtree (Depth First, children then self) formed from <code>root</code> and
     * performs the <code>processor.processNode(current)</code> method on 
     * each <code>PNode</code> that is encountered. <br>
     * NOTE: Branches cannot be predictively pruned with this method! Since
     * the processing of each node happens after it's children, pruning a 
     * "branch" will likely accomplish nothing in terms of saving processing.
     * @param root The root of the tree
     * @param processor Defines how to handle each node
     */
    static public void depthFirstPost(PNode root, NodeProcessor processor)
    {
        // Process the kids first
        for (PNode kid : root.getChildren())
            depthFirstPost(kid, processor);
        // then myself
        processor.processNode(root);
    }   
}
