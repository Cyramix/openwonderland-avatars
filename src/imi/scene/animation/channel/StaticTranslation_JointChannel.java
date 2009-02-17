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
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.animation.AnimationState;
import imi.utils.Interpolator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javolution.util.FastList;
import javolution.util.FastTable;

/**
 * This class represents a joint channel that contains only rotational
 * information.
 * @author Ronald E Dahlgren
 */
public class StaticTranslation_JointChannel implements PJointChannel, Serializable
{
    /** The unchanging translation vector for all key frames **/
    protected final Vector3f translationVector = new Vector3f();
    /** The name of the joint we affect **/
    protected String    targetJointName = null;
    /** Keyframe collection **/
    final FastTable<RotationKeyframe> keyframes = new FastTable();
    /** Cached duration **/
    protected transient float duration = 0;
    /** Cached average timestep **/
    protected transient float averageTimestep = 0;
    /** Used for calculations **/
    protected transient Matrix3f blendBuffer = new Matrix3f();
    protected transient PMatrix  transitionBuffer = new PMatrix();
    protected transient float[]  matrixFloats = new float[16];

    /**
     * Construct a new instance
     */
    public StaticTranslation_JointChannel(String targetJointName)
    {
        this.targetJointName = targetJointName;
    }

    public void setTargetJointName(String jointName)
    {
        this.targetJointName = jointName;
    }

    public void setTranslationVector(Vector3f translation)
    {
        translationVector.set(translation);
    }

    public void addKeyframe(float time, Matrix3f rotation)
    {
        // maintain chronological ordering
        int index = 0;
        RotationKeyframe keyframe = new RotationKeyframe(time, rotation);
        for (; index < keyframes.size(); ++index)
            if (keyframes.get(index).time > keyframe.time)
                break;
        // now index points to the first frame after this time
        keyframes.add(index, keyframe);
    }

    @Override
    public void calculateFrame(PJoint jointToAffect, AnimationState state) {
        if (calculateBlendedRotation(state.getCurrentCycleTime(), blendBuffer, state, false) == true)
            jointToAffect.getTransform().setLocalMatrix(blendBuffer, translationVector);
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
        FastList<RotationKeyframe> removals = new FastList();
        int numFrames = keyframes.size();
        if (numFrames < ratio * 3)
            return; // Too small to bother
        for (int i = 0; i < numFrames; ++i)
        {
            if (i == 0 || i == numFrames - 1 || i % ratio == 0)
                continue;
            else
                removals.add(keyframes.get(i));
        }
        // Now remove all of those
        for (RotationKeyframe frame : removals)
            keyframes.remove(frame);
        // Refresh metrics
        calculateAverageStepTime();
    }

    @Override
    public void timeBasedReduction(int newSampleFPS) {
        float spacing = 1.0f / (float)newSampleFPS;

        FastList<RotationKeyframe> removals = new FastList();

        float lastTime = 0.0f;
        RotationKeyframe firstFrame = keyframes.getFirst();
        RotationKeyframe lastFrame = keyframes.getLast();
        for (RotationKeyframe frame : keyframes)
        {
            if (    frame == firstFrame ||
                    frame == lastFrame  ||
                    frame.time - lastTime >= spacing)
            {
                lastTime = frame.time;
                continue;
            }
            else
                removals.add(frame);
        }
        // Now remove all of those
        for (RotationKeyframe frame : removals)
            keyframes.remove(frame);
        // refresh metrics
        calculateAverageStepTime();
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
        addKeyframe(calculateDuration() + calculateAverageStepTime(), keyframes.getFirst().rotation);
    }

    @Override
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient) {
        if (lerpCoefficient > 0.01f)
        {
            if (calculateBlendedRotation(state.getTransitionCycleTime(), blendBuffer, state, true))
            {
                if (lerpCoefficient > 0.9f)
                    joint.getTransform().getLocalMatrix(true).setRotation(blendBuffer);
                else
                {
                    transitionBuffer.setRotation(blendBuffer);
                    transitionBuffer.setTranslation(translationVector);
                    Interpolator.elementInterpolation(joint.getTransform().getLocalMatrix(true),
                            transitionBuffer, lerpCoefficient, joint.getTransform().getLocalMatrix(true));
                    joint.getTransform().getLocalMatrix(true).normalizeAxes();
                }
            }
        }
    }

    private transient RotationKeyframe[] resultBuffer = new RotationKeyframe[2];
    /**
     * Calculate the result of blending the transform matrices found between the
     * specified boundary times at the provided time.
     * @param fTime
     * @param output This matrix is used to receive the calculation.
     * @return 
     */
    private boolean calculateBlendedRotation(float fTime, Matrix3f output, AnimationState state,
            boolean bTransitionCycle)
    {
        boolean bReverse = (bTransitionCycle && state.isTransitionReverseAnimation()) ||
                            (!bTransitionCycle && state.isReverseAnimation());
        float lerpValue = detectFrames(fTime, bTransitionCycle, bReverse, state.getCursor());
        if (lerpValue >= 0 && lerpValue < 1)
        {
            Interpolator.elementInterpolation(resultBuffer[0].rotation, resultBuffer[1].rotation, lerpValue, output);
            return true;
        }
        else return false;
    }
    
    /**
     * Determines what two frames the given time is between, and returns the
     * interpolation coefficient
     * @return <0 indicates no usable data
     */
    private float detectFrames( float fTime, boolean bTransitionCycle,
                                boolean reverse, AnimationCursor cursor)
    {
        float result = -1;
        float relativeTime = 0;
        boolean overTheEdge = false;

        // determine what two keyframes to interpolate between
        RotationKeyframe leftFrame = null;
        RotationKeyframe rightFrame = null;
        RotationKeyframe currentFrame = null;
        // Get cursor
        int currentIndex = -1;
        if (bTransitionCycle)
            currentIndex = cursor.getCurrentTransitionJointPosition();
        else
            currentIndex = cursor.getCurrentJointPosition();

        int numKeyframes = keyframes.size();
        if (currentIndex < 0 || currentIndex >= numKeyframes - 1) // Current index valid?
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
            if (leftFrame == null)
            {
                overTheEdge = true;
                leftFrame = keyframes.getLast();
                relativeTime = fTime;
            }
            else
                relativeTime = fTime - leftFrame.time;
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
            if (leftFrame != null && rightFrame == null)
            {
                overTheEdge = true;
                rightFrame = keyframes.getFirst();
            }
            else if (leftFrame != null)
                relativeTime = fTime - leftFrame.time;
        }

        if (leftFrame != null && rightFrame != null) // Need to blend between two poses
        {
            if (overTheEdge)
            {
                result = relativeTime / averageTimestep;
//                if (targetJointName.equals("rightHand"))
//                    System.out.println("Over the edge, avgTimeStep: " + averageTimestep + " lerp: " + result + " time: " + fTime);
            }
            else
                result = relativeTime / (rightFrame.time - leftFrame.time);


            resultBuffer[0] = leftFrame;
            resultBuffer[1] = rightFrame;

        }
        return result;
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
        blendBuffer = new Matrix3f();
        transitionBuffer = new PMatrix();
        matrixFloats = new float[16];
        resultBuffer = new RotationKeyframe[2];
        calculateAverageStepTime();
    }

    protected class RotationKeyframe implements Serializable
    {
        /** The rotational information **/
        public transient Matrix3f rotation = new Matrix3f();
        /** Time of this keyframe **/
        public float time = 0;

        /**
         * Convenience constructor
         * @param time
         * @param value
         */
        public RotationKeyframe(float time, Matrix3f value)
        {
            this.time = time;
            rotation = new Matrix3f(value);
        }
        // Serialization helpers
        private void writeObject(ObjectOutputStream out) throws IOException
        {
            out.defaultWriteObject();
            for (int i = 0; i < 3; ++i)
                for (int j = 0; j < 3; j++)
                    out.writeFloat(rotation.get(i, j));
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            rotation = new Matrix3f();
            for (int i = 0; i < 3; ++i)
                for (int j = 0; j < 3; j++)
                    rotation.set(i, j, in.readFloat());
        }
    }
}
