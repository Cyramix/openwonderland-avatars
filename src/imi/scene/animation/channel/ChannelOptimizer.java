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

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;

/**
 * This class is responsible for optimizing joint channels.
 * @author Ronald E Dahlgren
 */
public class ChannelOptimizer
{
    /** True if the translation does not change **/
    private boolean bStaticTranslation = false;
    /** DOF indicators **/
    private boolean bConstantAxis_X = false;
    private boolean bConstantAxis_Y = false;
    private boolean bConstantAxis_Z = false;
    /** static angle checks **/
    private boolean bConstantAngle_X = false;
    private boolean bConstantAngle_Y = false;
    private boolean bConstantAngle_Z = false;
    /** Calculation scratch space **/
    private final Matrix3f matrix3fBuffer = new Matrix3f();

    public ChannelOptimizer()
    {

    }

    /**
     * Optimizes the provided channel using the provided quality guidelines. The
     * quality float should be a normalized value with 1.0 representing lossless
     * techniques only and 0.0 representing maximum compression and optimization.
     * @param channel
     * @param quality
     * @return
     */
    public PJointChannel optimize(PJointChannel channel, float quality) {
        if (channel instanceof PMatrix_JointChannel)
            return optimizeMatrixChannel((PMatrix_JointChannel)channel);
        else
            return channel;
    }

    private PJointChannel optimizeMatrixChannel(PMatrix_JointChannel channel)
    {
        PJointChannel result = null;
        float[] matrixFloats = new float[16];
        // Determine properties of the channel
        bStaticTranslation = true;
        bConstantAxis_X = true;
        bConstantAxis_Y = true;
        bConstantAxis_Z = true;
        bConstantAngle_X = true;
        bConstantAngle_Y = true;
        bConstantAngle_Z = true;

        // Grab some defaults to compare against
        channel.m_KeyFrames.getFirst().value.getFloatArray(matrixFloats);
        Vector3f translationVec = channel.m_KeyFrames.getFirst().value.getTranslation();
        float xAngle = (float)Math.acos(matrixFloats[5]);
        float yAngle = (float)Math.acos(matrixFloats[0]);
        float zAngle = yAngle;

        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : channel.m_KeyFrames)
        {
            keyframe.value.getFloatArray(matrixFloats);
            if (keyframe.value.getTranslation().equals(translationVec) == false)// difference
                bStaticTranslation = false;

            // x axis dof
            if (matrixFloats[1] > 0.02 || matrixFloats[2] > 0.02 ||
                matrixFloats[4] > 0.02 || matrixFloats[8] > 0.02 ||
                unreasonableCosOrSinValues(matrixFloats, 0))
                bConstantAxis_X = false;
            // y axis dof
            if (matrixFloats[1] > 0.02 || matrixFloats[6] > 0.02 ||
                matrixFloats[4] > 0.02 || matrixFloats[9] > 0.02||
                unreasonableCosOrSinValues(matrixFloats, 1))
                bConstantAxis_Y = false;
            // z axis dof
            if (matrixFloats[2] > 0.02 || matrixFloats[8] > 0.02 ||
                matrixFloats[6] > 0.02 || matrixFloats[9] > 0.02||
                unreasonableCosOrSinValues(matrixFloats, 2))
                bConstantAxis_Z = false;
            // look for static angles
            if ((float)Math.acos(matrixFloats[5]) != xAngle)
                bConstantAngle_X = false;
            if ((float)Math.acos(matrixFloats[0]) != yAngle)
                bConstantAngle_Y = false;
            if ((float)Math.acos(matrixFloats[0]) != zAngle)
                bConstantAngle_Z = false;

        }

        return createChannel(channel);
    }

    private PJointChannel createChannel(PMatrix_JointChannel channel)
    {
        // categorize
        if (bStaticTranslation)
        {
            Vector3f translation = channel.m_KeyFrames.getFirst().value.getTranslation();
            // TODO : Fix bug with the single DOF channel.
//            if (bConstantAxis_X)
//                return createSingleDOFChannel(channel, 0, translation);
//            if (bConstantAxis_Y)
//                return createSingleDOFChannel(channel, 1, translation);
//            if (bConstantAxis_Z)
//                return createSingleDOFChannel(channel, 2, translation);
            // Not a dof, but with static translation
            return createStaticTranslationChannel(channel, translation);
        }
        else
            return channel;
    }



    private OneDOF_JointChannel createSingleDOFChannel(PMatrix_JointChannel channel, int axis, Vector3f translation) {
        OneDOF_JointChannel result = new OneDOF_JointChannel(channel.getTargetJointName(), axis);
        result.setTranslationVector(translation);
        float[] matrixFloats  = new float[16];
        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : channel.m_KeyFrames)
        {
            keyframe.value.getFloatArray(matrixFloats);
            switch (axis)
            {
                case 0:
                    result.addKeyframe(keyframe.time, (float)Math.acos(matrixFloats[5]));
                    break;
                case 1:
                    result.addKeyframe(keyframe.time, (float)Math.acos(matrixFloats[0]));
                    break;
                case 2:
                    result.addKeyframe(keyframe.time, (float)Math.acos(matrixFloats[0]));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private StaticTranslation_JointChannel createStaticTranslationChannel(PMatrix_JointChannel channel, Vector3f translation)
    {
        StaticTranslation_JointChannel result = new StaticTranslation_JointChannel(channel.getTargetJointName());
        result.setTranslationVector(translation);
        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : channel.m_KeyFrames)
        {
            keyframe.value.getRotation(matrix3fBuffer);
            result.addKeyframe(keyframe.time, matrix3fBuffer);
        }
        return result;
    }

    private boolean unreasonableCosOrSinValues(float[] matrixFloats, int axis) {
        boolean result = false;
        switch (axis)
        {
            case 0: // x
                if (matrixFloats[5] > 1 || matrixFloats[6] > 1 || matrixFloats[9] > 1 || matrixFloats[10] > 1)
                    result = false;
                break;
            case 1: // y
                if (matrixFloats[0] > 1 || matrixFloats[2] > 1 || matrixFloats[8] > 1 || matrixFloats[10] > 1)
                    result = false;
                break;
            case 2: // z
                if (matrixFloats[0] > 1 || matrixFloats[1] > 1 || matrixFloats[4] > 1 || matrixFloats[5] > 1)
                    result = false;
                break;
            default:
                break;
        }
        return result;
    }
}
