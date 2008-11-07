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
package imi.utils;

import javolution.util.FastList;

/**
 * This class simply exposes stack functionality of the underlying.
 * @author Ronald E Dahlgren
 */
public class SynchronizedQueue <E>
{
    FastList m_collection = new FastList<E>();
    
    public SynchronizedQueue()
    {
    }
    
    /**
     * Remove and return the first element. 
     * @return The first element, null if empty container
     */
    public synchronized E dequeue()
    {
        if (m_collection.isEmpty() == false)
            return (E)m_collection.removeFirst();
        else
            return null;
    }
    
    /**
     * Add an element to the end
     * @param object
     */
    public synchronized void enqueue(E object)
    {
        m_collection.add(object);
    }
    
    public synchronized void clear()
    {
        m_collection.clear();
    }
    
    public synchronized boolean isEmpty()
    {
        return m_collection.isEmpty();
    }
    
    
}

