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
package imi.scene;

import imi.scene.utils.traverser.SkinnedMeshJointFlattener;
import imi.scene.utils.traverser.FlattenedHierarchyNodeProcessor;
import imi.scene.utils.traverser.TreeTraverser;
import imi.scene.utils.visualizations.DebugRenderer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastTable;


/**
 * This is the base node of the PScene dynamic scene graph.
 * It may or may not have a transform (it might be null).
 * It may have a single parent and multiple chidren.
 * 
 * @author Lou Hayt
 * @author Chris Nagle
 * @author Shawn Kendall
 * @author Ronald E Dahlgren (Serialization)
 */
public class PNode implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    protected static final Logger logger = Logger.getLogger(PNode.class.getName());
    
    private String  m_name  = "MysteryNode!";
    private transient PNode m_parent    = null;
    private final FastTable<PNode>  m_children  = new FastTable<PNode>();
    
    /**  A PNode may or may not have a PTransform. */
    private PTransform  m_transform     = null;
    
    /**  if true this node and its children will not be rendered. */
    private transient boolean     m_bRenderStop   = false;
    
    /** if true an update is required - meaning the JMonkey equivilent of this node is out of date */
    private transient boolean     m_bDirty        = true;
    
    /** external references counter */
    private int m_referenceCount = 0;
    
    /**
     * Construct a new instance, name defaults to "MysteryNode!"
     */
    public PNode()
    {
        // Diagnostic / Debugging output
//        logger.info("PNode created : " + getClass().getName());
    }

    /**
     * Creates a copy of this node. This does not include parent or child references.
     * @param other
     */
    public PNode(PNode other) {
        m_name = other.m_name;
        if (other.m_transform != null)
            m_transform = new PTransform(other.m_transform);
    }
    
    /**
     * Create a node with a name
     * @param name -    can be null
     */
    public PNode(String name) 
    {
        setName(name);
        // Debugging / Diagnostic info
//        logger.info("PNode created : " + m_name + " of type " + getClass().getName());
    }
    
    /**
     * Creates a node with a transform
     * @param transform -   can be null
     */
    public PNode(PTransform transform)
    {
        setTransform(transform);
    }
    
    /**
     * Create a node with a name and transform
     * @param name      -   can be null
     * @param transform -   can be null
     */
    public PNode(String name, PTransform transform)
    {
        setName(name);
        setTransform(transform);
        // Debugging / Diagnostic output
//        logger.info("PNode created : " + m_name + " of type " + getClass().getName());
    }
    
    /**
     * Create a node and fill it up with data
     * 
     * @param name      -   can be null
     * @param parent    -   can be null
     * @param children  -   can be null
     * @param transform -   can be null
     */
    public PNode(String name, PNode parent, FastTable<PNode> children, PTransform transform)
    {
        setName(name);
        
        if (parent != null)
            parent.addChild(this);
        
        setChildren(children);
        setTransform(transform);
        // Debugging / Diagnostic Output
//        logger.info("PNode created : " + m_name + " of type " + getClass().getName());
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
        {
            if (m_transform != null)
                m_transform.set(transform);
            else
                m_transform = new PTransform(transform);
        }
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
            try
            {
                // GC performance enhancement - prevents Iterator genertion SFK
                for ( int i = 0; i < m_children.size(); i++ )
                   m_children.get(i).setDirty(bDirty, true);
            }
            catch (java.util.ConcurrentModificationException exception)
            {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE,
                        "During setDirty() a ConcurrentModificationException was thrown! : " +
                        exception.getMessage());
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
     * called by a the node's parent or by a JScene,
     * draws skeleton bones if needed and sphere bounding volumes.
     */
    void debugDraw(DebugRenderer renderer)
    {
        if (m_bRenderStop)
            return;
        if (m_transform != null)
            renderer.setOrigin(m_transform.getWorldMatrix(false));
        renderer.draw(this);
        for (int i = 0; i < getChildrenCount(); i++)
        {
            PNode child = m_children.get(i);
            child.debugDraw(renderer);
        }
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
                // Debugging / Diagnostic output
//                logger.info("Child: " + child.getName()
//                        + " added to node: " + getName() );
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
     * @return m_children
     */
    public FastTable<PNode> getChildren()
    {
        return m_children;
    }
    
    /**
     * Set the children of this node by array reference
     * @param children
     */
    public void setChildren(FastTable<PNode> children)
    {
        // Out with the old
        m_children.clear();
        if (children != null)
            m_children.addAll(children);
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

        for (PNode kid : m_children)
        {
            if (name.equals(kid.getName()))
                return kid;
        }
        return null;
    }

    /**
     * Get an immediate child by name ignoring case on the name.
     * @param name
     * @return child (PNode)
     */
    public PNode getChildIgnoreCase(String name)
    {
        if (name == null)
            return null;

        for (PNode kid : m_children)
        {
            if (name.equalsIgnoreCase(kid.getName()))
                return kid;
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
        if (name.equals(getName())) // it's us!
            return this;

        // Search away!
        PNode result = null;
        for (PNode kid : m_children)
        {
            result = kid.findChild(name);
            if (result != null)
                break;
        }
        return result;
    }
    
    /**
     * Find all the PNodes that share the provided name and put them in a sack.
     * This sack is then conveniently returned to you! Do NOT call this method in
     * a performance critical section; it uses recursion and object creation
     * throughout.
     * @param name The name to match
     * @return Collection of PNodes.
     */
    public FastTable<PNode> findChildren(String name)
    {
       FastTable<PNode> resultCollection = new FastTable<PNode>();
       if (name == null)
           return resultCollection;

       if (name.equals(getName()))
           resultCollection.add(this);

       for (PNode kid : m_children)
           resultCollection.addAll(kid.findChildren(name));

       return resultCollection;
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
            child.setParent( null );
        return child;
    }
    
    /**
     * Traverse the hiearchy, breadth-first, for the child
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
            if (name.equals(current.getName())) // Found a match
            {
                PNode parent = current.getParent();
                if (parent == null) // No parent? not sure why this happened
                {
                    logger.severe("Removal found null parent PNode.java:425");
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
                    logger.severe("Recursive Removal found null parent PNode.java:425");
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
        
        for (PNode kid : m_children)
        {
            if (name.equals(kid.getName()))
                return removeChild(kid);
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
        for (PNode child : m_children)
            child.setParent(null);
        m_children.clear();
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
    public synchronized boolean replaceChild(PNode oldChild, PNode newChild, boolean bKeepOldGrandkids)
    {
        boolean result = false;
        // Grab oldChild's kids
        FastTable<PNode> oldChildren = oldChild.getChildren();
        // remove the old child

        int oldChildIndex = m_children.indexOf(oldChild);
        if (oldChildIndex != -1) // not found
        {
            result = true;
            // add our new one
            m_children.set(oldChildIndex, newChild);
            newChild.setParent(this);
            // coalesce the children if necessary
            if (bKeepOldGrandkids == true)
                newChild.getChildren().addAll(oldChildren);
        }
        // Dassit!
        return result;
        
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
    }
    
    /**
     * Returns true if this node is set to not render this branch
     * @return m_bRenderStop (boolean)
     */
    public boolean isRenderStop()
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

    /**
     * Gather the names of all SkinnedMeshJoints
     * @return
     */
    public FastTable<String> generateSkinnedMeshJointNames()
    {
        FastTable<String> names = new FastTable<String>();
        
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

    /**
     * Gather reference of all SkinnedMeshJoints
     * @return
     */
    public FastTable<SkinnedMeshJoint> generateSkinnedMeshJointReferences()
    {
        FastTable<SkinnedMeshJoint> names = new FastTable<SkinnedMeshJoint>();
        
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

    /**
     * Gather references to the mesh space transforms of all SkinnedMeshJoints
     * @return
     */
    public FastTable<PMatrix> generateSkinnedMeshJointTransformReferences()
    {
        FastTable<PMatrix> names = new FastTable<PMatrix>();
        
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

    /**
     * Gather references to the local modifiers of all SkinnedMeshJoints
     * @return
     */
    public FastTable<PMatrix> generateSkinnedMeshLocalModifierReferences()
    {
        FastTable<PMatrix> localModifiers = new FastTable<PMatrix>();
        
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
                localModifiers.add(((SkinnedMeshJoint)current).getLocalModifierMatrix());
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        return localModifiers;
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

    /**
     * Dump to the console
     * @param spacing
     */
    public void dump(String spacing)
    {
        System.out.println(spacing + this.getClass().toString() + " - " + this.getName());
        dumpChildren(spacing);
    }

    /**
     * Dump to the console
     * @param spacing
     */
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

    /****************************
     * SERIALIZATION ASSISTANCE *
     ****************************/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Tell all the children that I am their parent
        for (PNode kid : m_children)
            kid.m_parent = this;
        m_bDirty = true;
    }
}

