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

/**
 *
 * @author Lou Hayt
 */
public class BooleanPointer 
{
    boolean m_bool;
    
    public BooleanPointer(boolean bool)
    {
        m_bool = bool;
    }
    
    public boolean get()
    {
        return m_bool;
    }
    
    public void set(boolean bool)
    {
        m_bool = bool;
    }
    
    @Override
    public String toString()
    {
        return Boolean.toString(m_bool);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BooleanPointer other = (BooleanPointer) obj;
        if (this.m_bool != other.m_bool) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.m_bool ? 1 : 0);
        return hash;
    }
       
    
}
