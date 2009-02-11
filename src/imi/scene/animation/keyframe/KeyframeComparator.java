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
package imi.scene.animation.keyframe;

/**
 * Compare different implementors of the KeyframeInterface.
 * @author Ronald E Dahlgren
 */
public class KeyframeComparator
{
    /**
     * Compare the values of the two provided keyframe interfaces.
     * @param left
     * @param right
     * @return True if known to be equal, false otherwise.
     */
    public static boolean valueEquals(KeyframeInterface left, KeyframeInterface right)
    {
        if (left instanceof PMatrixKeyframe)
            return valueEquals((PMatrixKeyframe)left, right);
        else if (left instanceof VectorKeyframe)
            return valueEquals((VectorKeyframe)left, right);
        else
            return false;
    }

    private static boolean valueEquals(PMatrixKeyframe left, KeyframeInterface right)
    {
        if (right instanceof PMatrixKeyframe)
            return left.m_Value.equals(((PMatrixKeyframe)right).m_Value);
        else if (right instanceof VectorKeyframe) // Assume to be comparing translation
            return left.m_Value.getTranslation().equals(((VectorKeyframe)right).m_Value);
        else return false;
    }

    private static boolean valueEquals(VectorKeyframe left, KeyframeInterface right)
    {
        if (right instanceof PMatrixKeyframe) // Assume to compare translation
            return left.m_Value.equals(((PMatrixKeyframe)right).m_Value.getTranslation());
        else if (right instanceof VectorKeyframe)
            return left.m_Value.equals(((VectorKeyframe)right).m_Value);
        else
            return false;
    }
}
