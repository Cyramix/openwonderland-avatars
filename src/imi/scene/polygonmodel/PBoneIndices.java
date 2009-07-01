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
package imi.scene.polygonmodel;

import java.io.Serializable;

/**
 * This class wraps up a set of four ints
 * @author Lou Hayt
 */
public class PBoneIndices implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /** The actual data **/
    public final int [] index = new int[4];

    
    public PBoneIndices()
    {
        // Do nothing!
    }

    public PBoneIndices(PBoneIndices bones)
    {
        index[0] = bones.index[0];
        index[1] = bones.index[1];
        index[2] = bones.index[2];
        index[3] = bones.index[3];
    }

    public PBoneIndices(int index0, int index1, int index2, int index3)
    {
        index[0] = index0;
        index[1] = index1;
        index[2] = index2;
        index[3] = index3;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj == null) 
        {
            return false;
        }

        if (getClass() != obj.getClass()) 
        {
            return false;
        }

        final PBoneIndices other = (PBoneIndices) obj;

        for (int i = 0; i < 4; i++)
        {
            if (index[i] != other.index[i])
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() 
    {
        int hash = 7;
        for (int i = 0; i < 4; i++)
            hash = 7 * hash + index[i];
        return hash;
    }

}
