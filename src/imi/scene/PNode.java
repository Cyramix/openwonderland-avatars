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
package imi.scene;

import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.utils.tree.SkinnedMeshJointFlattener;
import imi.scene.utils.PRenderer;
import imi.scene.utils.tree.FlattenedHierarchyNodeProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;


/**
 * This is the base node of the PScene dynamic scene graph.
 * It may or may not have a transform (it might be null).
 * It may have a single parent and multiple chidren.
 * 
 * @author Lou Hayt
 * @author Chris Nagle
 * @author Shawn Kendall
 */
public class PNode //implements Serializable
{
    protected static final Logger logger = Logger.getLogger(PNode.class.getName());
    
    private String              m_name          = "";
    private PNode               m_parent        = null;
    private ArrayList<PNode>    m_children      = null;
    
    /**  A PNode may or may not have a PTransform. */
    private PTransform          m_transform     = null;   
    
    /**  if true this node and its children will not be rendered. */
    private boolean             m_bRenderStop   = false;   
    
    /** if true an update is required - meaning the JMonkey equivilent of this node is out of date */
    private boolean             m_bDirty        = true;
    
    /** external references counter */
    private int m_referenceCount = 0;
    
    static
    {
       logger.setLevel(Level.SEVERE); 
    }
    
    /**
     * Empty constructor, insures m_children will not be null.
     */
    public PNode()
    {
        m_children = new ArrayList<PNode>();
        logger.info("PNode created : " + getClass().getName());    
    }
    
    /**
     * Insures m_children will not be null.
     * @param name -    can be null
     */
    public PNode(String name) 
    {
        m_children = new ArrayList<PNode>();
        setName(name);
        logger.info("PNode created : " + m_name + " of type " + getClass().getName());
    }
    
    /**
     *  Insures m_children will not be null.
     * 
     * @param transform -   can be null
     */
    public PNode(PTransform transform)
    {
        this();
        setTransform(transform);
    }
    
    /**
     *  Insures m_children will not be null.
     * 
     * @param name      -   can be null
     * @param transform -   can be null
     */
    public PNode(String name, PTransform transform)
    {
        m_children = new ArrayList<PNode>();
        setName(name);
        setTransform(transform);
        logger.info("PNode created : " + m_name + " of type " + getClass().getName());
    }
    
    /**
     *  Insures m_children will not be null.
     * 
     * @param name      -   can be null
     * @param parent    -   can be null
     * @param children  -   can be null
     * @param transform -   can be null
     */
    public PNode(String name, PNode parent, ArrayList<PNode> children, PTransform transform)
    {
        setName(name);
        
        if (parent != null)
            parent.addChild(this);
        
        setChildren(children);
        if (m_children == null)
            m_children = new ArrayList<PNode>();
        setTransform(transform);
        logger.info("PNode created : " + m_name + " of type " + getClass().getName());
    }
    
    /**
     * Override this to implement internal draw for instances
     * @see PPolygonMeshInstance : draw()
     * @param renderer
     */
    public void draw(PRenderer renderer)
    { }
    
    /**
     * Calls draw on this node and all of its children cascading down
     * @param renderer
     */
    public void drawAll(PRenderer renderer)
    {
        if (m_bRenderStop)
            return;
        
        draw(renderer);
        
        PNode child;
        for (int i = 0; i < getChildrenCount(); i++) 
        {
            child = m_children.get(i);

            child.drawAll(renderer);
        }
    }

    
    
    /**
     * Set the name for this node
     * @param name
     */
    public void setName(String name)
    {
        if (name != null)
            m_name = name;
    }
    
    /**
     * Get the name of this node
     * @return m_name (String)
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * Set the parent for this node
     * @param parent - it is possible to set the parent to null
     */
    public void setParent(PNode parent)
    {
        //if (parent != null) sometimes we want to set the parent to null (adding a child)
            m_parent = parent;
    }
    
    /**
     * Get the parent of this node
     * @return m_parent (PNode)
     */
    public PNode getParent()
    {
        return m_parent;
    }
    
    /**
     * Sets a transform for this node
     * @param transform - if null a default transform will be created
     */
    public void setTransform(PTransform transform)
    {
        if (transform != null)
            m_transform = new PTransform(transform);
    }
    
    /**
     * Get the transform of this node
     * @return m_transform (PTransform)
     */
    public PTransform getTransform()
    {
        return m_transform;
    }
    
    /**
     * All children of this branch will implicitly become dirty if bDirty is true
     * (Note : it propogates down and not up)
     * 
     * @param bDirty  
     * @param bAffectKids - if true the set will propegate all the way down
     */
    public void setDirty(boolean bDirty, boolean bAffectKids) 
    {
        m_bDirty = bDirty;
        
        if (bDirty && bAffectKids)
        {
            try{
            for (PNode kid : getChildren())
                kid.setDirty(bDirty, bAffectKids);
            }
            catch (java.util.ConcurrentModificationException exception)
            {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "During setDirty() a ConcurrentModificationException was thrown!");
            }
        }
    }

    /**
     * m_bDirty may mean different things for defferent derrived types
     * overall it is true if an update is needed.
     * For e.g. dirty geometry in a pscene needs to reconstruct its
     * JME objects (TriMeshes)
     * @return m_bDirty (boolean)
     */
    public boolean isDirty() 
    {
        return m_bDirty;
    }
    
    /**
     * Add a child to this node
     * @param child - the child to add   
     * @return int  - the index of the added child
     */
    public int addChild(PNode child)
    {
        if (child != null) 
        {
            PNode childsParent = child.getParent();
            if (childsParent != this) 
            {
                if (childsParent != null) 
                {
                    childsParent.removeChild(child);
                }
                child.setParent(this);
                m_children.add(child);
                
                if (logger.isLoggable(Level.INFO)) 
                {
                    logger.info("Child: " + child.getName()
                            + " added to node: " + getName() );
                }
            }
        }
        
        return m_children.size()-1;
    }
    
    /**
     * Add a child to this node at a specific index
     * @param index
     * @param child
     */
    public void addChildAt(int index, PNode child)
    {
      if (child != null) 
      {
            PNode childsParent = child.getParent();
            if (childsParent != this) 
            {
                if (childsParent != null) 
                {
                    childsParent.removeChild(child);
                }
                child.setParent(this);
                m_children.add(index, child);
                
                if (logger.isLoggable(Level.INFO)) 
                {
                    logger.info("Child: " + child.getName()
                            + " added to node: " + getName()
                            + " at index: " + String.valueOf(index) );
                }
            }
        }
    }
    
    /**
     * Returns the number of immediate children this node have
     * @return size (int)
     */
    public int getChildrenCount()
    {
        return m_children.size();
    }

    /**
     * Returns the total number of nodes
     * @return count (int)
     */
    public int getTotalCount()
    {
        int totalCount = 1;
        PNode child;

        for (int i = 0; i < getChildrenCount(); i++) 
        {
            child = m_children.get(i);
            totalCount += child.getTotalCount();
        }

        return(totalCount);
    }

    /**
     * Returns the array of children for this node
     * @return m_children (ArrayList<PNode>)
     */
    public ArrayList<PNode> getChildren()
    {
        return m_children;
    }
    
    /**
     * Set the children of this node by array reference
     * @param children
     */
    public void setChildren(ArrayList<PNode> children)
    {
        if (children != null)
        {
            m_children = children;
            logger.info("setChildren");
        }
    }
    
    /**
     * Get an immediate child by index
     * @param index
     * @return m_children (PNode)
     * @throws indexOutOfBoundsException
     */
    public PNode getChild(int index)
    {
        return m_children.get(index);
    }
    
    /**
     * Get an immediate child by name.
     * (use findChild() for a recursive search)
     * @param name
     * @return child (PNode)
     */
    public PNode getChild(String name)
    {
        if (name == null) 
            return null;
        
        PNode child;
        for (int i = 0; i < getChildrenCount(); i++) 
        {
            child = m_children.get(i);
            if (name.equals(child.getName())) 
                return child;
        }
        return null;
    }
    
    /**
     * Find child by name.
     * This method will find a child by recursivly searching for it in child nodes
     * if it wasn't found as an immediate child (in m_children).
     * @param name      -   the name of the child to find
     * @return PNode    -   the found child or null
     */
    public PNode findChild(String name)
    {
        if (name == null) 
            return null;
        
        PNode child;
        for (int i = 0; i < getChildrenCount(); i++) 
        {
            child = m_children.get(i);
            if (name.equals(child.getName())) 
            {
                return child;
            }
            else
            {
                PNode subChild = child.findChild(name);
                if (subChild != null)
                {
                    return subChild;
                }    
            }
        }
        return null; 
    }
    
    /**
     * Remove an immediate child by index
     * @param index
     * @return child (PNode)
     */
    public PNode removeChild(int index)
    {
        PNode child = m_children.remove(index);
        if ( child != null ) 
        {
            child.setParent( null );
            logger.info("Child removed.");
        }
        return child;
    }
    
    /**
     * Recursively search the hiearchy for the child
     * with the specified name and kill them. There
     * shall be no escape!
     * @param name 
     * @return The node that was found and removed, or null if not found
     */
    public PNode findAndRemoveChild(String name)
    {
        // Recurse and find
        
        FastList<PNode> kids = new FastList<PNode>();
        // Add ourselves
        kids.add(this);
        PNode current = null;
        
        while (kids.isEmpty() == false)
        {
            // process
            current = kids.removeFirst();
            if (current.getName().equals(name)) // Found a match
            {
                PNode parent = current.getParent();
                if (parent == null) // No parent? not sure why this happened
                {
                    System.out.println("Recursive Removal found null parent PNode.java:425");
                    return null;
                }
                else
                {
                    parent.removeChild(current);
                    return current;
                }
            }
            // add the kids
            kids.addAll(current.getChildren());
        }
        
        return null; // No match
    }
    
    /**
     * Recursively search the hiearchy for the child
     * and remove them.
     * @param toRemove The node to remove
     * @return The node that was found and removed, or null if not found
     */
    public PNode findAndRemoveChild(PNode toRemove)
    {
        // Recurse and find
        FastList<PNode> kids = new FastList<PNode>();
        // Add ourselves
        kids.add(this);
        PNode current = null;
        
        while (kids.isEmpty() == false)
        {
            // process
            current = kids.removeFirst();
            if (current == toRemove) // Found a match
            {
                PNode parent = current.getParent();
                if (parent == null) // No parent? not sure why this happened
                {
                    System.out.println("Recursive Removal found null parent PNode.java:425");
                    return null;
                }
                else
                {
                    parent.removeChild(current);
                    return current;
                }
            }
            // add the kids
            kids.addAll(current.getChildren());
        }
        
        return null; // No match
    }
    
    /**
     * Remove an immediate child by name
     * @param name
     * @return child (PNode)
     */
    public PNode removeChild(String name)
    {
        if (name == null)
            return null;
        
        for (int i = 0; i < m_children.size(); i++) 
        {
            PNode child =  m_children.get(i);
            if (name.equals(child.getName())) 
            {
                return removeChild( i );
            }
        }
        return null;
    }
    
    /**
     * Remove an immediate child by reference
     * @param child
     * @return child (PNode)
     */
    public PNode removeChild(PNode child)
    {
        if (child == null)
            return null;
        
        if (child.getParent() == this) 
        {
            int index = m_children.indexOf(child);
            if (index != -1) 
            {
                return removeChild(index);
            }
        } 
            
        return null;  
    }
    
    /**
     * Remove all immediate children from this node
     */
    public void removeAllChildren()
    {
        m_children.clear();
        
        logger.info("All children removed.");
    }
    
    /**
     * Returns the index of an immediate child by name
     * @param name
     * @return index (int)
     */
    public int getIndexOf(String name)
    {
        return m_children.indexOf(getChild(name));
    }
    
    /**
     * Returns the index of an immediate child by reference
     * @param child
     * @return index (int)
     */
    public int getIndexOf(PNode child)
    {
        return m_children.indexOf(child);
    }
    
    /**
     * This method replaces the specified child with another.
     * @param oldChild The child to replace
     * @param newChild The replacement (Life as a PNode is tough)
     * @param bKeepOldGrandkids If true, grandkids are coalesced, otherwise the oldChild's kids are lost.
     */
    public synchronized void replaceChild(PNode oldChild, PNode newChild, boolean bKeepOldGrandkids)
    {
        // Grab oldChild's kids
        ArrayList<PNode> oldChildren = oldChild.getChildren();
        // remove the old child
        removeChild(oldChild);
        // add our new one
        addChild(newChild);
        // coalesce the children if necessary
        if (bKeepOldGrandkids == true)
            newChild.getChildren().addAll(oldChildren);
        // Dassit!
        
    }
    
    /**
     * Swaps children by index
     * @param index1
     * @param index2
     */
    public void swapChildren(int index1, int index2)
    {
        // TODO : error checking? if so: return a boolean
        PNode kid2 =  m_children.get(index2);
        PNode kid1 =  m_children.remove(index1);
        m_children.add(index1, kid2);
        m_children.remove(index2);
        m_children.add(index2, kid1);
        
        logger.info("Children swaped.");
    }
    
    /**
     * Returns true if this node is set to not render this branch
     * @return m_bRenderStop (boolean)
     */
    public boolean getRenderStop()
    {
        return m_bRenderStop;
    }
    
    /**
     * Set whenever to render this node and its children or not
     * @param on
     */
    public void setRenderStop(boolean on)
    {
        m_bRenderStop = on;
    }
    
    /**
     * Returns the count of external references to this node
     * @return m_referenceCount (int)
     */
    public int getReferenceCount()
    {
        return m_referenceCount;
    }
    
    /**
     * Sets the number of external references to this node
     * @param count
     */
    public void setReferenceCount(int count)
    {
        m_referenceCount = count;
    }
    
    /**
     * Adjust the number of external references to this node
     * @param count - the number to add to the count
     */
    public int adjustReferenceCount(int count)
    {
        m_referenceCount += count;
        return m_referenceCount;
    }
    
    /**
     * Flatten this <code>PNode</code>'s hierarchy (build the transform world matrices)
     * and return it as an array of <code>PMatrix</code>.
     */
    public PMatrix[] buildFlattenedHierarchy()
    {
        FlattenedHierarchyNodeProcessor processor = new FlattenedHierarchyNodeProcessor(10, false);
        TreeTraverser.breadthFirst(this, processor);
        return processor.getPMatrixArray();
    }
    
    /**
     * Flatten this <code>PNode</code>'s hierarchy (build the transform world matrices)
     * and return an array of inversed <code>PMatrix</code>.
     */
    public PMatrix[] buildInverseFlattenedHierarchy() 
    {
        FlattenedHierarchyNodeProcessor processor = new FlattenedHierarchyNodeProcessor(10, true);
        TreeTraverser.breadthFirst(this, processor);
        return processor.getPMatrixArray();
    }
    
    /**
     * Flatten this <code>PNode</code>'s hierarchy (build the transform world matrices)
     * This method will not excecute on branches of nodes that are not PJoint or its derrived types.
     * and return it as an array of <code>PMatrix</code>.
     */
    public PMatrix[] buildFlattenedSkinnedMeshJointHierarchy()
    {
        SkinnedMeshJointFlattener processor = new SkinnedMeshJointFlattener(10, false);
        TreeTraverser.breadthFirst(this, processor);
        return processor.getPMatrixArray();
    }
    
    public ArrayList<String> generateSkinnedMeshJointNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(this);
        
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him!
            if (!(current instanceof SkinnedMeshJoint)) // Prune this branch?
                continue;
            else
                names.add(current.getName());
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        return names;
    }
    
    public ArrayList<SkinnedMeshJoint> generateSkinnedMeshJointReferences()
    {
        
        ArrayList<SkinnedMeshJoint> names = new ArrayList<SkinnedMeshJoint>();
        
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(this);
        
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him!
            if (!(current instanceof SkinnedMeshJoint)) // Prune this branch?
                continue;
            else
                names.add(((SkinnedMeshJoint)current));
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        return names;
    }
    
    public ArrayList<PMatrix> generateSkinnedMeshJointTransformReferences()
    {
        
        ArrayList<PMatrix> names = new ArrayList<PMatrix>();
        
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(this);
        
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him!
            if (!(current instanceof SkinnedMeshJoint)) // Prune this branch?
                continue;
            else
                names.add(((SkinnedMeshJoint)current).getMeshSpace());
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        return names;
    }
    
    public ArrayList<PMatrix> generateSkinnedMeshLocalModifierReferences()
    {
        ArrayList<PMatrix> names = new ArrayList<PMatrix>();
        
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(this);
        
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him!
            if (!(current instanceof SkinnedMeshJoint)) // Prune this branch?
                continue;
            else
                names.add(((SkinnedMeshJoint)current).getLocalModifierMatrix());
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        return names;
    }
    
    /**
     * Flatten this <code>PNode</code>'s hierarchy (build the transform world matrices)
     * This method will not excecute on branches of nodes that are not PJoint or its derrived types.
     * and return an array of inversed <code>PMatrix</code>.
     */
    public PMatrix[] buildInverseFlattenedSkinnedMeshJointHierarchy()
    {
        SkinnedMeshJointFlattener processor = new SkinnedMeshJointFlattener(30, true);
        TreeTraverser.breadthFirst(this, processor);
        return processor.getPMatrixArray();
    }


    //  Dumps the Node and all it's children.
    public void dump()
    {
        dump("");
    }
    public void dump(String spacing)
    {
        System.out.println(spacing + this.getClass().toString() + " - " + this.getName());
        dumpChildren(spacing);
    }
    public void dumpChildren(String spacing)
    {
        if (this.getChildrenCount() > 0)
        {
            PNode pChildNode;
            
            for (int i=0; i<this.getChildrenCount(); i++)
            {
                pChildNode = this.getChild(i);
                
                pChildNode.dump(spacing + "   ");
            }
        }
    }

}

