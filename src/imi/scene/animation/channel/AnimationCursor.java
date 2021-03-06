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
package imi.scene.animation.channel;

/**
 * Tracks the location in the animation collection.
 * @author Ronald E Dahlgren
 */
public class AnimationCursor
{
    private static final int DefaultIndexSize = 128;
    private int[] indices = null;
    private int[] transitionIndices = null;
    private int[] negativeOnes = null;

    private int jointIndex = -1;
    private int transitionJointIndex = -1;

    public AnimationCursor()
    {
        indices = new int[DefaultIndexSize];
        transitionIndices = new int[DefaultIndexSize];
        negativeOnes = new int[DefaultIndexSize];
        for (int i = 0; i < DefaultIndexSize; i++)
            negativeOnes[i] = -1;
        makeNegativeOne();
    }

    public AnimationCursor(int size)
    {
        indices = new int[size];
        transitionIndices = new int[size];
    }
    
    public void makeNegativeOne()
    {
        // src, pos, dest, pos, length
        System.arraycopy(negativeOnes, 0, indices, 0, negativeOnes.length);
        System.arraycopy(negativeOnes, 0, transitionIndices, 0, negativeOnes.length);
    }
    
    public void setJointIndex(int joint, int index)
    {
        indices[joint] = index;
    }

    public void setJointIndex(int newJointIndex) {
        jointIndex = newJointIndex;
        transitionJointIndex =newJointIndex;
    }

    public int getCurrentJointPosition()
    {
        return indices[jointIndex];
    }

    public void setCurrentJointPosition(int position)
    {
        indices[jointIndex] = position;
    }

    public void setTransitionJointIndex(int joint, int index)
    {
        transitionIndices[joint] = index;
    }

    public void setCurrentTransitionJointIndex(int newJointIndex) {
        transitionJointIndex = newJointIndex;
    }

    public int getCurrentTransitionJointPosition()
    {
        return transitionIndices[transitionJointIndex];
    }

    public void setCurrentTransitionJointPosition(int position)
    {
        transitionIndices[transitionJointIndex] = position;
    }
}
