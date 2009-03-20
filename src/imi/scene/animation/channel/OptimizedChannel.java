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
import imi.scene.PMatrix;
import imi.scene.animation.AnimationState;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;

/**
 * This class represents a joint channel that contains only rotational
 * information.
 * @author Ronald E Dahlgren
 */
public class OptimizedChannel implements PJointChannel, Serializable
{
    /** The unchanging basis for all key frames **/
    protected final float[]     matrixSkeleton = new float[16];

    /** Indication of state **/
    static final int CONSTANT_TRANSLATION = 0;
    static final int CONSTANT_X_AXIS = 1;
    static final int CONSTANT_Y_AXIS = 2;
    static final int CONSTANT_Z_AXIS = 3;
    protected final BitSet stateBits = new BitSet(8);
    /** The name of the joint we affect **/
    protected String    targetJointName = null;
    /** Keyframe data **/
    protected float[] data = new float[0];
    /** Determine keyframe size **/
    protected int floatsPerFrame = 0;
    /** Cached duration **/
    protected transient float duration = 0;
    /** Cached average timestep **/
    protected transient float averageTimestep = 0;
    /** Cached number of frames **/
    protected transient int numberOfFrames = 0;

    /**
     * Construct a new optimized joint channel
     * @param targetJointName Name of the joint to affect
     * @param indicationBits Indicates the state (constant axes, etc)
     * @param defaultMatrix
     * @param numberOfKeyframes
     */
    OptimizedChannel(String targetJointName,
                    BitSet indicationBits,
                    PMatrix defaultMatrix,
                    int numberOfKeyframes)
    {
        // Joint name
        this.targetJointName = targetJointName;
        // Copy state specification
        for (int i = 0; i < 8; ++i)
            stateBits.set(i, indicationBits.get(i));
        // Determine size of keyframe data
        floatsPerFrame = 1; // time
        if (!stateBits.get(CONSTANT_TRANSLATION))
            floatsPerFrame += 3;
        if (!stateBits.get(CONSTANT_X_AXIS))
            floatsPerFrame += 3;
        if (!stateBits.get(CONSTANT_Y_AXIS))
            floatsPerFrame += 3;
        if (!stateBits.get(CONSTANT_Z_AXIS))
            floatsPerFrame += 3;
        // Copy over default transform floats
        defaultMatrix.getFloatArray(matrixSkeleton);
    }

    /**
     * MUST BE ADDED IN CHRONOLOGICAL ORDER!
     * @param time
     * @param transform
     */
    public void addKeyframe(float time, PMatrix transform)
    {
        float[] matrixFloats = new float[16];
        transform.getFloatArray(matrixFloats);

        // If this is the first frame, then it's time WILL be zero
        if (data.length == 0)
            time = 0;
        // resize our data collection
        float[] oldData = data;
        data = new float[oldData.length + floatsPerFrame];
        System.arraycopy(oldData, 0, data, 0, oldData.length);

        // Make a new keyframe
        float[] newFrame = new float[floatsPerFrame];
        int newFrameArrayIndex = 0; // Keep track of what index we are on
        newFrame[newFrameArrayIndex++] = time;
        if (!stateBits.get(CONSTANT_TRANSLATION))
        {
            newFrame[newFrameArrayIndex++] = matrixFloats[3];
            newFrame[newFrameArrayIndex++] = matrixFloats[7];
            newFrame[newFrameArrayIndex++] = matrixFloats[11];
        }
        if (!stateBits.get(CONSTANT_X_AXIS))
        {
            newFrame[newFrameArrayIndex++] = matrixFloats[0];
            newFrame[newFrameArrayIndex++] = matrixFloats[4];
            newFrame[newFrameArrayIndex++] = matrixFloats[8];
        }
        if (!stateBits.get(CONSTANT_Y_AXIS))
        {
            newFrame[newFrameArrayIndex++] = matrixFloats[1];
            newFrame[newFrameArrayIndex++] = matrixFloats[5];
            newFrame[newFrameArrayIndex++] = matrixFloats[9];
        }
        if (!stateBits.get(CONSTANT_Z_AXIS))
        {
            newFrame[newFrameArrayIndex++] = matrixFloats[2];
            newFrame[newFrameArrayIndex++] = matrixFloats[6];
            newFrame[newFrameArrayIndex++] = matrixFloats[10];
        }

        // add the new keyframe
        System.arraycopy(newFrame, 0, data, oldData.length, floatsPerFrame);
    }

    @Override
    public void calculateFrame(PJoint jointToAffect, AnimationState state) {
        float lerpValue = detectFrames(state.getCurrentCycleTime(), false,
                state.isReverseAnimation(), state.getCursor());
        if (lerpValue != -10) // Sane lerp value?
        {
            float oneMinusLerp = (1-lerpValue);
            ++leftResult; // discount time
            ++rightResult; // ditto
            // Process
            if (!stateBits.get(CONSTANT_TRANSLATION))
            {
                matrixSkeleton[ 3] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[ 7] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[11] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            if (!stateBits.get(CONSTANT_X_AXIS))
            {
                matrixSkeleton[0] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[4] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[8] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            if (!stateBits.get(CONSTANT_Y_AXIS))
            {
                matrixSkeleton[1] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[5] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[9] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            if (!stateBits.get(CONSTANT_Z_AXIS))
            {
                matrixSkeleton[ 2] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[ 6] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[10] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            jointToAffect.getTransform().setLocalMatrix(matrixSkeleton);
        }
    }

    private float getFrameTime(int keyFrameIndex)
    {
        return data[floatsPerFrame * keyFrameIndex];
    }

    private transient int leftResult = -1;
    private transient int rightResult = -1;
    /**
     * Determines what two frames the given time is between, and returns the
     * interpolation coefficient
     * @return <0 indicates no usable data
     */
    private float detectFrames( float fTime, boolean bTransitionCycle,
                                boolean reverse, AnimationCursor cursor)
    {
        float result = -10; // Result is the lerp value, -10 is my safe 'bad value'

        // Use these two numbers as indicies for the frames to blend
        leftResult = -1;
        rightResult = -1;

        // Get cursor to optimize the keyframe search
        int currentIndex = -1;
        if (bTransitionCycle) // If we are detecting the frames for a transition cycle, use its cursor
            currentIndex = cursor.getCurrentTransitionJointPosition();
        else
            currentIndex = cursor.getCurrentJointPosition();

        // determine the validity of the index
        if (currentIndex < 0 || currentIndex >= numberOfFrames || numberOfFrames < 3) 
        {
            if (reverse)
                currentIndex = numberOfFrames - 1; // start at the end
            else
                currentIndex = 0; // start at the beginning
        }

        
        if (reverse)
        { // In reverse, iterate backwards
            while (currentIndex >= 0)
            {
                if (getFrameTime(currentIndex) <= fTime) // passed the mark, this frame will be the one blended from
                {
                    leftResult = currentIndex;
                    rightResult = currentIndex + 1;
                    if (bTransitionCycle)
                        cursor.setCurrentTransitionJointPosition(rightResult);
                    else
                        cursor.setCurrentJointPosition(rightResult);
                    break; // finished checking
                }
                currentIndex--;
            }

            // if the time value was too high, then use the first frame
            if (rightResult == numberOfFrames || rightResult == -1) 
            {
                rightResult = 0;
                if (leftResult != -1) // Ensure validity before use
                    result = (fTime - getFrameTime(leftResult)) / averageTimestep;
            }
            else if (leftResult != -1)
                result = (fTime - getFrameTime(leftResult)) / (getFrameTime(rightResult) - getFrameTime(leftResult));
        }
        else // playing forward
        {
            while (currentIndex < numberOfFrames)
            {
                // If this frame has a greater time value, then it is the right side
                // and the previous frame is the left side
                if (getFrameTime(currentIndex) > fTime)
                {
                    rightResult = currentIndex;
                    leftResult = currentIndex - 1;
                    if (bTransitionCycle)
                        cursor.setCurrentTransitionJointPosition(leftResult);
                    else
                        cursor.setCurrentJointPosition(leftResult);
                    break; // finished checking
                }
                currentIndex++;
            }

            // If over the right edge, blend towards the first frame
            if (rightResult == -1) 
            {
                rightResult = 0;
                if (leftResult != -1)
                    result = (fTime - duration) / (duration + averageTimestep - getFrameTime(leftResult));
            }
            else if (leftResult != -1)
                result = (fTime - getFrameTime(leftResult)) / (getFrameTime(rightResult) - getFrameTime(leftResult));
        }

        // Scale indices so that they index into the data array now
        leftResult *= floatsPerFrame;
        rightResult *= floatsPerFrame;

        return result;
    }

    @Override
    public float calculateDuration() {
        numberOfFrames = data.length / floatsPerFrame;
        duration = getFrameTime((data.length / floatsPerFrame) - 1);
        return duration;
    }

    @Override
    public float calculateAverageStepTime() {
        averageTimestep =  calculateDuration() / (float)(data.length / floatsPerFrame);
        return averageTimestep;
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

    private transient float[] otherFloatBuffer = new float[16];
    @Override
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient) {
        float lerpValue = detectFrames(state.getTransitionCycleTime(), true,
                state.isTransitionReverseAnimation(), state.getCursor());
        if (lerpValue >= 0 && lerpValue <= 1)
        {
            joint.getTransform().getLocalMatrix(true).getFloatArray(otherFloatBuffer);
            
            float oneMinusLerp = (1-lerpValue);
            leftResult++; // Time
            rightResult++; // Time
            // Process
            if (!stateBits.get(CONSTANT_TRANSLATION))
            {
                matrixSkeleton[ 3] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[ 7] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[11] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            if (!stateBits.get(CONSTANT_X_AXIS))
            {
                matrixSkeleton[0] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[4] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[8] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            if (!stateBits.get(CONSTANT_Y_AXIS))
            {
                matrixSkeleton[1] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[5] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[9] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            if (!stateBits.get(CONSTANT_Z_AXIS))
            {
                matrixSkeleton[ 2] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[ 6] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
                matrixSkeleton[10] = data[leftResult++] * oneMinusLerp + data[rightResult++] * lerpValue;
            }
            // lerp from otherFloatBuffer to matrixSkeleton
            for (int i = 0; i < 12; ++i)
                otherFloatBuffer[i] = otherFloatBuffer[i] * (1-lerpCoefficient) + matrixSkeleton[i] * lerpCoefficient;
            joint.getTransform().setLocalMatrix(otherFloatBuffer);
        }
    }

    @Override
    public String toString() {
        StringBuilder resultBuilder = new StringBuilder();
        if (stateBits.get(CONSTANT_TRANSLATION))
            resultBuilder.append(" Translation");
        if (stateBits.get(CONSTANT_X_AXIS))
            resultBuilder.append(" X");
        if (stateBits.get(CONSTANT_Y_AXIS))
            resultBuilder.append(" Y");
        if (stateBits.get(CONSTANT_Z_AXIS))
            resultBuilder.append(" Z");
        resultBuilder.append(" - floatsPerFrame: " + floatsPerFrame + " data.length: " + data.length);
        return resultBuilder.toString();
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
        leftResult = -1;
        rightResult = -1;
        otherFloatBuffer = new float[16];
        calculateAverageStepTime();
        numberOfFrames = data.length / floatsPerFrame;
    }
}
