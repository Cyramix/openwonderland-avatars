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
package imi.utils;

import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.io.Serializable;

/**
 * Class for performing interpolations
 * @author Ronald E Dahlgren
 */
public class Interpolator implements Serializable
{
    /** Used to specify what internal strategy should be used for interpolation **/
    public enum InterpolationStrategy
    {
        ComponentInterpolation, // Slow
        ElementInterpolation, // Fast, possibly inaccurate
    }

    /** Internal state **/
    protected InterpolationStrategy strategy = InterpolationStrategy.ComponentInterpolation;
    /** Work space **/
    protected float[] floatMatrix = new float[16];
    protected final Vector3f m_leftBufferVector = new Vector3f();
    protected final Vector3f m_rightBufferVector = new Vector3f();
    /**
     * Construct a new instance defaulting to the ComponentInterpolation strategy
     */
    public Interpolator()
    {

    }

    /**
     * Interpolate by interpolationCoefficient from left to right, storing result
     * in the result
     * @param interpolationCoefficient value between 0 and 1
     * @param left Starting point
     * @param right Ending point
     * @param result Filled with interpolated data
     */
    public void interpolate(float interpolationCoefficient, PMatrix left, PMatrix right, PMatrix result)
    {
        switch(strategy)
        {
            case ComponentInterpolation:
                componentInterpolation(interpolationCoefficient, left, right, result);
                break;
            case ElementInterpolation:
                result.lerp(left, right, interpolationCoefficient);
                break;
        }
    }

    protected void componentInterpolation(float s, PMatrix left, PMatrix right, PMatrix result)
    {
        Quaternion rotationComponent = left.getRotationJME();
        rotationComponent.slerp(rotationComponent, right.getRotationJME(), s);

        // grab the translation and lerp it
        left.getTranslation(m_leftBufferVector);
        right.getTranslation(m_rightBufferVector);

        m_leftBufferVector.interpolate(m_rightBufferVector, s);
        result.set2(rotationComponent, m_leftBufferVector, 1.0f);
    }


    public InterpolationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(InterpolationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Interpolate the components of the two provided matrices using the provided
     * interpolation coefficient (0.0-1.0) and store the result in the output
     * matrix. The output matrix may also be either of the two provided for blending.
     * @param left
     * @param right
     * @param s
     * @param output
     */
    public static void elementInterpolation(Matrix3f left, Matrix3f right, float s, Matrix3f output)
    {
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                output.set(i, j, left.get(i, j) * (1 - s) + right.get(i, j) * s);
    }


    public static void elementInterpolation(PMatrix left, PMatrix right, float lerpCoefficient, PMatrix output) {
        output.lerp(left, right, lerpCoefficient);
    }

}
