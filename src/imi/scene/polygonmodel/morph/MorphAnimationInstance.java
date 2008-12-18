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
package imi.scene.polygonmodel.morph;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import imi.loaders.MeshBuffer;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimationInstance extends Node //implements Savable 
{
    private MorphAnimation  m_pAnimation    = null;       //  the animation this is an instance of
    private TriMesh         m_mesh          = null;       //  the animated m_mesh

    private boolean m_bPlay                 = true;       //  on\off
    private boolean m_bForward              = true;       //  direction
    private float   m_fSpeed                = 1.0f;       //  1.0f means 100% of the original animation speed
    private float   m_AnimationDuration     = 0.0f;       //  length
    private float   m_AnimationStartTime    = 0.0f;       //  the time the animation will start at every loop
    private float   m_AnimationCurrentTime  = 0.0f;       //  current time
    private int     m_PositionsCount        = 0;          //  number of vertices

    public enum PlayType{ PLAY_ONCE, LOOP, OSCILATE }
    private PlayType m_PlayType = PlayType.OSCILATE;
    
    private int m_CurrentLoopIndex          = 0;
    private int m_CurrentKeyFrameIndex      = 0;
    private int m_NextLoopIndex             = 0;
    private int m_NextKeyFrameIndex         = 0;
    
    private MorphAnimationLoop currentLoop  = null;       //  | updated before interpolation in update()
    private MorphAnimationLoop nextLoop     = null;       //  |

    public MorphAnimationInstance(MorphAnimation pAnimation)
    {
        setMorphAnimation(pAnimation);

        //  Test
        populateWithTestData();

        this.attachChild(m_mesh);
    }

    public void populateWithTestData()
    {
        //  Test
        m_pAnimation.populateWithTestData();

        if (resetPositionsCount() == 0)
            return;

        chooseAnimation(1, 0, 1, 1);

        // setTriMesh
        MeshBuffer mb = new MeshBuffer();
        int index1, index2, index3;
        // first vertex
        index1 = mb.addPosition(0.0f, -5.0f, 1.0f);
        mb.addNormal(0.0f, 1.0f, 0.0f);
        mb.addTexCoord(0, 0.5f, 0.0f);
        // secound vertex
        index2 = mb.addPosition(1.0f, -5.0f, -1.0f);
        mb.addNormal(0.0f, 1.0f, 0.0f);
        mb.addTexCoord(0, 1.0f, 1.0f);
        // third vertex
        index3 = mb.addPosition(-1.0f, -5.0f, -1.0f);
        mb.addNormal(0.0f, 1.0f, 0.0f);
        mb.addTexCoord(0, 0.0f, 1.0f);
        // first triangle
        mb.addTriangle(index1, index2, index3);
        // Build the m_mesh for the first time (TexCoords and indecies should not change past this point)
        m_mesh = new TriMesh("test trimesh", BufferUtils.createFloatBuffer(mb.getPositions()), BufferUtils.createFloatBuffer(mb.getNormals()), null, TexCoords.makeNew(mb.getTexCoordZero()), BufferUtils.createIntBuffer(mb.getIndices()));
    }

    /**
     *  Choose the animation to play, if you want to set a custom duration or start time use relevant set functions.
     * Note : this function will reset m_AnimationCurrentTime and m_AnimationStartTime to 0.0f.
     * Note : the animation will only play if m_bPlay is set to true.
     * 
     * @param beginLoop     -   index of the loop that has the begining keyframe
     * @param beginKeyframe -   index of the beginning keyframe to use in the interpolation
     * @param endLoop       -   index of the loop that has the end keyframe
     * @param endKeyframe   -   index of the end keyframe to use in the interpolation
     */
    public void chooseAnimation(int beginLoop, int beginKeyframe, int endLoop, int endKeyframe)
    {
        m_CurrentLoopIndex      = beginLoop;
        m_CurrentKeyFrameIndex  = beginKeyframe;
        m_NextLoopIndex         = endLoop;
        m_NextKeyFrameIndex     = endKeyframe;

        m_AnimationCurrentTime  = 0.0f;
        m_AnimationStartTime    = 0.0f;

        if (m_pAnimation.getMorphAnimationLoop(beginLoop) != null) 
        {
            m_AnimationDuration = m_pAnimation.getMorphAnimationLoop(beginLoop).getDuration();
        }
    }

    private Vector3f interpolatePosition(int index)
    {
        Vector3f initialPosition = currentLoop.getMorphAnimationKeyframe(m_CurrentKeyFrameIndex).getPosition(index);
        Vector3f finalPosition   = nextLoop.getMorphAnimationKeyframe(m_NextKeyFrameIndex).getPosition(index);

        // Interpolate the positions using linear interpolation:
        // CurrentPosition = InitialPosition(Duration-Time) + FinalPosition*Time   when (0 <= Time <= Duration)
        Vector3f currentPosition = new Vector3f();
        currentPosition.x = initialPosition.x * (m_AnimationDuration - m_AnimationCurrentTime) + finalPosition.x * m_AnimationCurrentTime;
        currentPosition.y = initialPosition.y * (m_AnimationDuration - m_AnimationCurrentTime) + finalPosition.y * m_AnimationCurrentTime;
        currentPosition.z = initialPosition.z * (m_AnimationDuration - m_AnimationCurrentTime) + finalPosition.z * m_AnimationCurrentTime;

        return currentPosition;
    }

    private Vector3f interpolateNormal(int index)
    {
        Vector3f initialNormal = currentLoop.getMorphAnimationKeyframe(m_CurrentKeyFrameIndex).getNormal(index);
        Vector3f finalNormal   = nextLoop.getMorphAnimationKeyframe(m_NextKeyFrameIndex).getNormal(index);

        // Interpolate the Normals using linear interpolation:
        // CurrentNormal = InitialNormal(Duration-Time) + FinalNormal*Time     when (0 <= Time <= Duration)
        Vector3f currentNormal = new Vector3f();
        currentNormal.x = initialNormal.x * (m_AnimationDuration - m_AnimationCurrentTime) + finalNormal.x * m_AnimationCurrentTime;
        currentNormal.y = initialNormal.y * (m_AnimationDuration - m_AnimationCurrentTime) + finalNormal.y * m_AnimationCurrentTime;
        currentNormal.z = initialNormal.z * (m_AnimationDuration - m_AnimationCurrentTime) + finalNormal.z * m_AnimationCurrentTime;

        return currentNormal.normalize();
    }

    /**
     * updateGeometricState overrides Spatials updateGeometric state to update m_mesh
     * Note : comment this function if you are calling updateAnimation somewhere else  
     * 
     * @param time
     *            the time that has passed between calls.
     * @param initiator
     *            true if this is the top level being called.
     */
//     @Override
//    public void updateGeometricState(float time, boolean initiator) 
//    {
//         updateAnimation(time);
//         super.updateGeometricState(time, initiator);
//    }
    
    /**
     * call this function once a frame
     * Note : no update will occure if m_bPlay is false
     * 
     * @param deltaTime - elapsed time
     */
    public void updateAnimation(float deltaTime)
    {
        if (!m_bPlay) 
            return;
        
        m_AnimationCurrentTime += deltaTime * m_fSpeed;

        //  Are we past the duration time?
        if (m_AnimationCurrentTime > m_AnimationDuration) 
        {
            switch (m_PlayType) 
            {
                case PLAY_ONCE: 
                {
                    m_bPlay = false;
                    return;
                    //break;
                }
                case LOOP: 
                {
                    m_AnimationCurrentTime = m_AnimationStartTime;
                    break;
                }
                case OSCILATE: 
                {
                    m_bForward = !m_bForward;
                    // TODO : m_AnimationStartTime might be beyond beginKeyFrame time...
                    chooseAnimation(m_NextLoopIndex, m_NextKeyFrameIndex, m_CurrentLoopIndex, m_CurrentKeyFrameIndex);
                    if (m_bForward) 
                    {
                        m_AnimationCurrentTime = m_AnimationStartTime;
                    }
                    break;
                }
            }
        }

        //  Get ready for interpolation
        currentLoop = m_pAnimation.getMorphAnimationLoop(m_CurrentLoopIndex);
        nextLoop    = m_pAnimation.getMorphAnimationLoop(m_NextLoopIndex);
        
        if (currentLoop == null || nextLoop == null)
            return;
        
        MeshBuffer mb = new MeshBuffer();
        for (int i = 0; i < m_PositionsCount; i = i + 3) 
        {
            // interpolate positions
            mb.addPosition(interpolatePosition(i));
            // interpolate the normals
            mb.addNormal(interpolateNormal(i));

            mb.addPosition(interpolatePosition(i + 1));
            mb.addNormal(interpolateNormal(i + 1));

            mb.addPosition(interpolatePosition(i + 2));
            mb.addNormal(interpolateNormal(i + 2));
        }

        // Reconstruct the m_mesh - only positions and normals
        // TODO: Make functional under jME 2.0
//        TriangleBatch triBatch = m_mesh.getBatch(0);
//        triBatch.setVertexBuffer(BufferUtils.createFloatBuffer(mb.getPositions()));
//        triBatch.setNormalBuffer(BufferUtils.createFloatBuffer(mb.getNormals()));
    }
    
    public void setMorphAnimation(MorphAnimation animation)
    {
        if (animation != null) 
        {
            m_pAnimation = animation;
        }
    }

    public MorphAnimation getMorphAnimation()
    {
        return m_pAnimation;
    }

    public void setMesh(TriMesh mesh)
    {
        if (mesh != null) 
        {
            m_mesh = mesh;
        }
    }

    public TriMesh getMesh()
    {
        return m_mesh;
    }
    
    public void Stop()
    {
        m_bPlay = false;
    }

    public void Play()
    {
        m_bPlay = true;
    }

    public void Play(boolean play)
    {
        m_bPlay = play;
    }

    public boolean isPlaying()
    {
        return m_bPlay;
    }
    
    public boolean isDirectionForward()
    {
        return m_bForward;
    }

    public void setSpeed(float speed)
    {
        m_fSpeed = speed; // error checking needed?
    }

    public float getSpeed()
    {
        return m_fSpeed;
    }
    
    public float getAnimationDuration()
    {
        return m_AnimationDuration;
    }

    /**
     * if you want to set a custom duration time for this animation use this function after the call to chooseAnimation
     * @param duration  -   the duration of the animation
     */
    public void setAnimationDuration(float duration)
    {
        if (duration > 0.0f) 
        {
            m_AnimationDuration = duration;
        }
    }

    public float getAnimationStartTime()
    {
        return m_AnimationStartTime;
    }

    /**
     * if you want to set a custom start time for this animation use this function after the call to chooseAnimation
     * @param time  -   the start time of each animation loop
     */
    public void setAnimationStartTime(float time)
    {
        if (time >= 0.0f) 
        {
            m_AnimationStartTime = time;
        }
    }

    public float getAnimationCurrentTime()
    {
        return m_AnimationCurrentTime;
    }

    public void setAnimationCurrentTime(float time)
    {
        m_AnimationCurrentTime = time;
    }

    public int getPositionsCount()
    {
        return m_PositionsCount;
    }
    
    public int resetPositionsCount()
    {
        MorphAnimationLoop animationLoop = m_pAnimation.getMorphAnimationLoop(0);
                
        if (m_pAnimation != null && animationLoop != null)
        {
            m_PositionsCount = animationLoop.getPositionsCount();
        }
        
        return m_PositionsCount;
    }
    
    public void setPlayType(PlayType type)
    {
        m_PlayType = type;
    }
    
    public PlayType getPlayType()
    {
        return m_PlayType;
    }
    
    public void setCurrentLoopIndex(int index)
    {
        if (index > 0)
            m_CurrentLoopIndex = index;
    }
    
    public int getCurrentLoopIndex()
    {
        return m_CurrentLoopIndex;
    }
    
    public void setCurrentKeyFrameIndex(int index)
    {
        if (index > 0)
            m_CurrentKeyFrameIndex = index;
    }
    
    public int getCurrentKeyFrameIndex()
    {
        return m_CurrentKeyFrameIndex;
    }
    
    public void setNextLoopIndex(int index)
    {
        if (index > 0)
            m_NextLoopIndex = index;
    }
    
    public int getNextLoopIndex()
    {
        return m_NextLoopIndex;
    }
    
    public void setNextKeyFrameIndex(int index)
    {
        if (index > 0)
            m_NextKeyFrameIndex = index;
    }
    
    public int getNextKeyFrameIndex()
    {
        return m_NextKeyFrameIndex;
    }
    
    
    
    
    
//       - taken for reference from AnimationController.java - 
//    public void write(JMEExporter e) throws IOException {
//        super.write(e);
//        OutputCapsule cap = e.getCapsule(this);
//        cap.writeSavableArrayList(animationSets, "animationSets", null);
//        cap.write(skeleton, "skeleton", null);
//        cap.write(activeAnimation, "activeAnimation", null);
//    }
//
//    @SuppressWarnings("unchecked")
//    public void read(JMEImporter e) throws IOException {
//        super.read(e);
//        InputCapsule cap = e.getCapsule(this);
//        animationSets = cap.readSavableArrayList("animationSets", null);
//        skeleton = (Bone)cap.readSavable("skeleton", null);
//        activeAnimation = (BoneAnimation)cap.readSavable("activeAnimation", null);
//    }
    
}
