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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows for running a tree traversal on its own thread.
 * Additionally, the TreeTraverser can be daemonized to continually process
 * the given tree until it is told to stop.<br>
 * NOTE: Because of the threading, the target tree should only be
 * read at any given instance, not written to.
 * @author Ronald E Dahlgren
 */
public class ThreadedTreeTraverser extends Thread 
{
    /**
     * This enumeration is used to specify the desired traversal type
     */
    public static enum eTraversalType 
    { 
        BREADTH_FIRST,
        DEPTH_FIRST_PRE,
        DEPTH_FIRST_POST
    }
    
    private eTraversalType m_TraversalType = eTraversalType.BREADTH_FIRST; // How should the tree be traversed?
    private boolean m_bDaemonize = false; // If true, the tree will be continually traversed.
    private boolean m_bDie = false; // This determines when the thread should exit
    private PNode m_RootNode = null; // The root of the tree to traverse
    private NodeProcessor m_Processor = null; // The processing of the nodes!
    private long m_SleepTime = 100; // Specifies how long the thread should sleep when applicable
    private boolean m_bDebug = false; // Output debugging information
    
    public ThreadedTreeTraverser()
    {
        // Do nothing
    }
    
    /**
     * Constructor
     * @param root The root of the tree to be processed
     */
    public ThreadedTreeTraverser(PNode root)
    {
        m_RootNode = root;
    }
    
    /**
     * Constructor
     * @param root The root of the tree to be traversed
     * @param processor Contains the node processing logic
     */
    public ThreadedTreeTraverser(PNode root, NodeProcessor processor)
    {
        m_RootNode = root;
        m_Processor = processor;
    }
    
    /**
     * Constructor
     * @param root The root of the tree to be traversed
     * @param processor Contains the node processing logic
     * @param TraversalMethod Determines how to traverse the tree
     */
    public ThreadedTreeTraverser(PNode root, NodeProcessor processor, eTraversalType TraversalMethod)
    {
        m_RootNode = root;
        m_Processor = processor;
    }
    
    public void init()
    {
        this.start();
    }
    
    @Override
    public void run()
    {
        while (true) // Exit conditions at the end of the loop
        {
            // Check the kill switch
            if (m_bDie == true)
                break;
            // If the proper settings have not been made, just sleep for a bit
            if (m_RootNode == null ||  m_Processor == null)
            {
                output("RootNode or Processor is null.");
                try 
                {
                    ThreadedTreeTraverser.sleep(m_SleepTime);
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(ThreadedTreeTraverser.class.getName()).log(Level.SEVERE, "Sleep was interrupted!", ex);
                }
                continue;
            }
            // Otherwise, let's traverse this tree!
            switch (m_TraversalType)
            {
                case BREADTH_FIRST:
                    breadthFirst();
                    break;
                case DEPTH_FIRST_PRE:
                    depthFirstPre(m_RootNode);
                    break;
                case DEPTH_FIRST_POST:
                    depthFirstPost(m_RootNode);
                    break;
                default:
                    Logger.getLogger(ThreadedTreeTraverser.class.getName()).log(Level.SEVERE, "Unknown traversal type!");
                    break;
            }
            output("Finished a tree traversal");
            // Check exit conditions
            if (m_bDie == true || m_bDaemonize == false)
            {
                output("Exiting loop");
                break;
            }
            try 
            {
                // Otherwise sleep for a bit and do it all over again... what a life ;)
                ThreadedTreeTraverser.sleep(m_SleepTime);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(ThreadedTreeTraverser.class.getName()).log(Level.SEVERE, "Sleep Interrupted! How rude...", ex);
            }
            output("Finished sleeping, starting again!");
        }
        
    }
    
    /**
     * This method throws the kill switch, when next checked the thread will exit.
     */
    public synchronized void kill()
    {
        m_bDie = true;
    }

    public void setDebug(boolean bDebug) 
    {
        m_bDebug = bDebug;
    }
    
    public synchronized void setSleepTimeout(long milliseconds)
    {
        assert (milliseconds > 0);
        m_SleepTime = milliseconds;
    }
    
    /**
     * This method sets whether or not the tree should be continuously processed.
     * @param bDaemonize If true, the tree will be processed until the thread's
     * kill switch is engaged, or until this state is changed.
     */
    public synchronized void setDaemonized(boolean bDaemonize)
    {
        m_bDaemonize = bDaemonize;
    }
    
    /**
     * This method sets the root of the tree to traverse. If another tree
     * is being traversed, it will finish being processed before the new
     * tree is considered.
     * @param root The root of the tree to traverse
     */
    public synchronized void setRoot(PNode root)
    {
        m_RootNode = root;
    }
    
    /**
     * This method sets the processor to use on each node as the
     * tree is traversed.
     * @param processor
     */
    public synchronized void setProcessor(NodeProcessor processor)
    {
        m_Processor = processor;
    }
    
    /**
     * This method tells the traverser how it should traverse the tree.
     * @param TraversalMethod
     */
    public synchronized void setTraversalMethod(eTraversalType TraversalMethod)
    {
        m_TraversalType = TraversalMethod;
    }
    
    // Accessors
    /**
     * No reference type should be modified, these accessors are provided for
     * reading values only. To modify settings, use the synchronized set methods.
     * @return m_Processor (NodeProcessor)
     */
    public NodeProcessor getProcessor() 
    {
        return m_Processor;
    }

    /**
     * No reference type should be modified, these accessors are provided for
     * reading values only. To modify settings, use the synchronized set methods.
     * @return m_RootNode (PNode)
     */
    public PNode getRootNode() 
    {
        return m_RootNode;
    }

    /**
     * No reference type should be modified, these accessors are provided for
     * reading values only. To modify settings, use the synchronized set methods.
     * @return m_TraversalType (eTraversalType)
     */
    public eTraversalType getTraversalType() 
    {
        return m_TraversalType;
    }

    /**
     * True if the kill-switch has been flipped.
     * @return m_bDie (Boolean)
     */
    public boolean isDying() 
    {
        return m_bDie;
    }
    
    /**
     * This method traverses the subtree (Breadth First) formed from <code>root</code> and
     * performs the <code>processor.processNode(current)</code> method on 
     * each <code>PNode</code> that is encountered.
     * @param root The root of the tree
     * @param processor Defines how to handle each node
     */
    private void breadthFirst()
    {
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(m_RootNode);
        
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him!
            if (m_Processor.processNode(current) == false) // Prune this branch?
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
    private void depthFirstPre(PNode currentNode)
    {
        // Process myself first
        if (m_Processor.processNode(currentNode) == false) // Prune this branch?
            return;
        // then the kids
        for (PNode kid : currentNode.getChildren())
            depthFirstPre(kid);
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
    private void depthFirstPost(PNode currentNode)
    {
        // Process the kids first
        for (PNode kid : currentNode.getChildren())
            depthFirstPost(kid);
        // then myself
        m_Processor.processNode(currentNode);
    }

    private void output(String string) 
    {
        if (m_bDebug == true)
            System.out.println(string);
    }
}
