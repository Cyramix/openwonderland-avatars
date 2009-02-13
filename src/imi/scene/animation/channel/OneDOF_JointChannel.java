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

import com.jme.math.Vector3f;
import imi.scene.PJoint;
import imi.scene.animation.AnimationState;
import imi.utils.Interpolator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javolution.util.FastTable;

/**
 * This class represents a joint channel that contains only rotational
 * information.
 * @author Ronald E Dahlgren
 */
public class OneDOF_JointChannel implements PJointChannel, Serializable
{
    /** The unchanging translation vector for all key frames **/
    protected final Vector3f translationVector = new Vector3f();
    /** Indication of which axis has freedom **/
    protected int rotationAxis = -1; // x = 0; y = 1; z = 2;
    /** The name of the joint we affect **/
    protected String    targetJointName = null;
    /** Keyframe collection **/
    final FastTable<OneDOFKeyframe> keyframes = new FastTable();
    /** Cached duration **/
    protected transient float duration = 0;
    /** Cached average timestep **/
    protected transient float averageTimestep = 0;
    /** Used for calculations **/
    protected transient float[]  matrixFloats = new float[16];

    /**
     * Construct a new channel with the provided settings
     * @param targetJointName
     * @param axisOfFreedom 0 = x; 1 = y; 2 = z;
     */
    public OneDOF_JointChannel(String targetJointName, int axisOfFreedom)
    {
        this.targetJointName = targetJointName;
        this.rotationAxis = axisOfFreedom;
    }

    public void setTargetJointName(String jointName)
    {
        this.targetJointName = jointName;
    }

    public void setTranslationVector(Vector3f translation)
    {
        translationVector.set(translation);
    }
    
    public void addKeyframe(float time, float angle)
    {
        // maintain chronological ordering
        int index = 0;
        OneDOFKeyframe keyframe = new OneDOFKeyframe(time, angle);
        for (; index < keyframes.size(); ++index)
            if (keyframes.get(index).time > keyframe.time)
                break;
        // now index points to the first frame after this time
        keyframes.add(index, keyframe);
    }

    @Override
    public void calculateFrame(PJoint jointToAffect, AnimationState state) {
        float lerpValue = detectFrames(state.getCurrentCycleTime(), false,
                state.isReverseAnimation(), state.getCursor());
        if (lerpValue >= 0 && lerpValue <= 1)
        {
            float angle = (resultBuffer[0].angle * (1-lerpValue)) + (resultBuffer[1].angle * lerpValue);

            matrixFloats[3]  = translationVector.x;
            matrixFloats[7]  = translationVector.y;
            matrixFloats[11] = translationVector.z;

            matrixFloats[12] = 0;
            matrixFloats[13] = 0;
            matrixFloats[14] = 0;
            matrixFloats[15] = 1;

            switch (rotationAxis)
            {
                case 0:
                    matrixFloats[0] = 1;
                    matrixFloats[1] = 0;
                    matrixFloats[2] = 0;
                    matrixFloats[4] = 0;
                    matrixFloats[5] = (float)Math.cos(angle); // cos
                    matrixFloats[6] = (float)Math.sin(angle); // sin
                    matrixFloats[8] = 0;
                    matrixFloats[9] = -(float)Math.sin(angle); // -sin
                    matrixFloats[10] = (float)Math.cos(angle); // cos
                    break;
                case 1:
                    matrixFloats[0] = (float)Math.cos(angle); // cos
                    matrixFloats[1] = 0;
                    matrixFloats[2] = -(float)Math.sin(angle); //-sin
                    matrixFloats[4] = 0;
                    matrixFloats[5] = 1;
                    matrixFloats[6] = 0;
                    matrixFloats[8] = (float)Math.sin(angle); //sin
                    matrixFloats[9] = 0;
                    matrixFloats[10] = (float)Math.cos(angle); //cos
                    break;
                case 2:
                    matrixFloats[0] = (float)Math.cos(angle); //cos
                    matrixFloats[1] = (float)Math.sin(angle); //sin
                    matrixFloats[2] = 0;
                    matrixFloats[4] = -(float)Math.sin(angle); //-sin
                    matrixFloats[5] = (float)Math.cos(angle); //cos
                    matrixFloats[6] = 0;
                    matrixFloats[8] = 0;
                    matrixFloats[9] = 0;
                    matrixFloats[10] = 1;
                    break;
                default:
                    break;
            }
            jointToAffect.getTransform().setLocalMatrix(matrixFloats);
        }
    }

    public int getAxis()
    {
        return rotationAxis;
    }

    private transient OneDOFKeyframe[] resultBuffer = new OneDOFKeyframe[2];
    /**
     * Determines what two frames the given time is between, and returns the
     * interpolation coefficient
     * @return <0 indicates no usable data
     */
    private float detectFrames( float fTime, boolean bTransitionCycle,
                                boolean reverse, AnimationCursor cursor)
    {
        float result = -1;
        boolean overTheEdgeInReverse = false;

        // determine what two keyframes to interpolate between
        OneDOFKeyframe leftFrame = null;
        OneDOFKeyframe rightFrame = null;
        OneDOFKeyframe currentFrame = null;
        // Get cursor
        int currentIndex = -1;
        if (bTransitionCycle)
            currentIndex = cursor.getCurrentTransitionJointPosition();
        else
            currentIndex = cursor.getCurrentJointPosition();

        int numKeyframes = keyframes.size();
        if (currentIndex < 0 || currentIndex >= numKeyframes) // Current index valid?
        {
            if (reverse)
                currentIndex = numKeyframes - 1; // start at the end
            else
                currentIndex = 0;
        }

        // GC optimization, removed Iterator usage
        if (reverse)
        { // In reverse, iterate backwards
            while (currentIndex > 0)
            {
                currentFrame = keyframes.get(currentIndex);
                if (currentFrame.time > fTime)
                    rightFrame = currentFrame;
                else // passed the mark
                {
                    leftFrame = currentFrame;
                    if (bTransitionCycle)
                        cursor.setCurrentTransitionJointIndex(currentIndex + 1);
                    else
                        cursor.setCurrentJointPosition(currentIndex + 1);
                    break; // finished checking
                }
                currentIndex--;
            }
            if (rightFrame == null)
            {
                overTheEdgeInReverse = true;
                rightFrame = keyframes.getLast();
            }
        }
        else // playing forward
        {
            while (currentIndex < numKeyframes-1)
            {
                currentFrame = keyframes.get(currentIndex);
                if (currentFrame.time <= fTime)
                    leftFrame = currentFrame;
                else // passed the mark
                {
                    rightFrame = currentFrame;
                    if (bTransitionCycle)
                        cursor.setCurrentTransitionJointIndex(currentIndex - 1);
                    else
                        cursor.setCurrentJointPosition(currentIndex - 1);
                    break; // finished checking
                }
                currentIndex++;
            }
        }

        if (leftFrame != null && rightFrame != null) // Need to blend between two poses
        {
            if (!overTheEdgeInReverse)
                result = (fTime - leftFrame.time) / (rightFrame.time - leftFrame.time);
            else
                result = (fTime - leftFrame.time) / averageTimestep;
            resultBuffer[0] = leftFrame;
            resultBuffer[1] = rightFrame;
            
        }
        else
            result = -1;
        return result;
    }

    @Override
    public float calculateDuration() {
        duration = keyframes.getLast().time;
        return duration;
    }

    @Override
    public float calculateAverageStepTime() {
        averageTimestep =  calculateDuration() / (float)(keyframes.size());
        return averageTimestep;
    }

    @Override
    public void fractionalReduction(int ratio) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void timeBasedReduction(int newSampleFPS) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getAverageStepTime() {
        return averageTimestep;
    }

    @Override
    public String getTargetJointName() {
        return targetJointName;
    }

    @Override
    public float getEndTime() {
        if (duration == 0)
            calculateDuration();
        return duration;
    }

    @Override
    public void closeChannel() {
        // Duplicate the first frame at the end
        addKeyframe(calculateDuration() + calculateAverageStepTime(), keyframes.getFirst().angle);
    }

    private transient float[] otherFloatBuffer = new float[16];
    @Override
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient) {
        if (true)
            return;
        float lerpValue = detectFrames(state.getTransitionCycleTime(), true,
                state.isTransitionReverseAnimation(), state.getCursor());
        if (lerpValue >= 0 && lerpValue <= 1)
        {
            joint.getTransform().getLocalMatrix(true).getFloatArray(otherFloatBuffer);
            float angle = (resultBuffer[0].angle * (1-lerpValue)) + (resultBuffer[1].angle * lerpValue);
            // translation vec
            otherFloatBuffer[3]  *= 1 - lerpCoefficient;
            otherFloatBuffer[3]  += translationVector.x * lerpCoefficient;
            otherFloatBuffer[7]  *= 1 - lerpCoefficient;
            otherFloatBuffer[7]  += translationVector.y * lerpCoefficient;
            otherFloatBuffer[11] *= 1 - lerpCoefficient;
            otherFloatBuffer[11] += translationVector.z * lerpCoefficient;

            switch (rotationAxis)
            {
                case 0:
                    matrixFloats[0] = 1;
                    matrixFloats[1] = 0;
                    matrixFloats[2] = 0;
                    matrixFloats[4] = 0;
                    matrixFloats[5] = (float)Math.cos(angle); // cos
                    matrixFloats[6] = (float)Math.sin(angle); // sin
                    matrixFloats[8] = 0;
                    matrixFloats[9] = -(float)Math.sin(angle); // -sin
                    matrixFloats[10] = (float)Math.cos(angle); // cos
                    break;
                case 1:
                    matrixFloats[0] = (float)Math.cos(angle); // cos
                    matrixFloats[1] = 0;
                    matrixFloats[2] = -(float)Math.sin(angle); //-sin
                    matrixFloats[4] = 0;
                    matrixFloats[5] = 1;
                    matrixFloats[6] = 0;
                    matrixFloats[8] = (float)Math.sin(angle); //sin
                    matrixFloats[9] = 0;
                    matrixFloats[10] = (float)Math.cos(angle); //cos
                    break;
                case 2:
                    matrixFloats[0] = (float)Math.cos(angle); //cos
                    matrixFloats[1] = (float)Math.sin(angle); //sin
                    matrixFloats[2] = 0;
                    matrixFloats[4] = -(float)Math.sin(angle); //-sin
                    matrixFloats[5] = (float)Math.cos(angle); //cos
                    matrixFloats[6] = 0;
                    matrixFloats[8] = 0;
                    matrixFloats[9] = 0;
                    matrixFloats[10] = 1;
                    break;
                default:
                    break;
            }

            // Blend the upper 3x3
            otherFloatBuffer[0] *= 1 - lerpCoefficient;
            otherFloatBuffer[0] += lerpCoefficient * matrixFloats[0];

            otherFloatBuffer[1] *= 1 - lerpCoefficient;
            otherFloatBuffer[1] += lerpCoefficient * matrixFloats[1];

            otherFloatBuffer[2] *= 1 - lerpCoefficient;
            otherFloatBuffer[2] += lerpCoefficient * matrixFloats[2];

            otherFloatBuffer[4] *= 1 - lerpCoefficient;
            otherFloatBuffer[4] += lerpCoefficient * matrixFloats[4];

            otherFloatBuffer[5] *= 1 - lerpCoefficient;
            otherFloatBuffer[5] += lerpCoefficient * matrixFloats[5];

            otherFloatBuffer[6] *= 1 - lerpCoefficient;
            otherFloatBuffer[6] += lerpCoefficient * matrixFloats[6];

            otherFloatBuffer[8] *= 1 - lerpCoefficient;
            otherFloatBuffer[8] += lerpCoefficient * matrixFloats[8];

            otherFloatBuffer[9] *= 1 - lerpCoefficient;
            otherFloatBuffer[9] += lerpCoefficient * matrixFloats[9];

            otherFloatBuffer[10] *= 1 - lerpCoefficient;
            otherFloatBuffer[10] += lerpCoefficient * matrixFloats[10];

            joint.getTransform().setLocalMatrix(otherFloatBuffer);
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
        // Re-allocate all transient objects
        resultBuffer = new OneDOFKeyframe[2];
        matrixFloats = new float[16];
        otherFloatBuffer = new float[16];
        calculateAverageStepTime();
    }

    protected class OneDOFKeyframe implements Serializable
    {
        /** The rotational information **/
        public float angle = 0;
        /** Time of this keyframe **/
        public float time = 0;

        /**
         * Convenience constructor
         * @param time
         * @param value
         */
        public OneDOFKeyframe(float time, float angle)
        {
            this.time = time;
            this.angle = angle;
        }
    }
}
