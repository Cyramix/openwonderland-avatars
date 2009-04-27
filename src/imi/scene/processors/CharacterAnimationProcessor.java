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
package imi.scene.processors;

import imi.scene.PNode;
import imi.scene.animation.Animated;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationState;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.utils.instruments.Instrumentation;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This processor handles animating a character. Do to our additive blending
 * system, these animations are handled in a special, and specific way.
 * @author Ronald E Dahlgren
 */
public class CharacterAnimationProcessor extends ProcessorComponent
{
    private Instrumentation         instruments = null;
    private Animated                m_animated  = null;
    private PPolygonModelInstance   m_modelInst = null;

    private double oldTime = 0.0f;
    private double deltaTime = 0.0f;

    private boolean synchronizer = false;
    /** Enable control facial animation **/
    private boolean animateFace = true;

    /**
     * This constructor receives the skeleton node
     * @param skeleton
     * @param wm
     */
    public CharacterAnimationProcessor(SkeletonNode skeleton, WorldManager wm)
    {
        m_animated = skeleton;
        instruments = (Instrumentation)wm.getUserData(Instrumentation.class);
//        setRunInRenderer(true);
//        if(skeleton.getParent() instanceof PPolygonModelInstance)
//            m_modelInst = (PPolygonModelInstance) skeleton.getParent();
    }
    public CharacterAnimationProcessor(PPolygonModelInstance modelInst, WorldManager wm)
    {
        m_modelInst = modelInst;
        instruments = (Instrumentation)wm.getUserData(Instrumentation.class);
    }

    @Override
    public synchronized void compute(ProcessorArmingCollection collection)
    {

    }

    @Override
    public void commit(ProcessorArmingCollection collection)
    {
        if (!isEnabled())
            return;
        if (instruments!=null && instruments.isSubsystemEnabled(Instrumentation.InstrumentedSubsystem.AnimationSystem) == false)
        {
            m_modelInst.setDirty(true, true);
            return;
        }

        double newTime = System.nanoTime() / 1000000000.0;
        deltaTime = (newTime - oldTime);
        oldTime = newTime;

        // If the modelInst constructor was used
        if (m_animated == null && m_modelInst != null)
        {
            // try to grab the skeleton
            if (m_modelInst.getChildren() != null)
            {
                for (PNode kid : m_modelInst.getChildren()) // for each child
                {
                    if (kid instanceof SkeletonNode)
                    {
                        m_animated = (SkeletonNode) kid;
                    }
                }
            }
            // any luck?
            if (m_animated == null)
                return;
        }
        else if (m_animated == null && m_modelInst == null)
            return;

        // Assuming a one to one relationship between groups and states,
        // each AnimationState is holding state for the animation stored in
        // that same AnimationGroup index
        // Current groups in use: 0 - full body animations, 1 - face only animations
        AnimationState state = null;
        AnimationGroup group = null;
        int numberOfGroups = m_animated.getAnimationComponent().getGroupCount();
        if (!animateFace)
            numberOfGroups = 1; // Ignore the face group
        for (int i = 0; i < numberOfGroups; i++)
        {
            state = m_animated.getAnimationState(i);
            group = m_animated.getAnimationGroup(i);

            if (state == null || group == null)
            {
                Logger.getLogger(this.getClass().toString()).severe("Character animation processor iterated on a null state or group");
                return;
            }
            else if (!state.isPauseAnimation())
            {
                state.advanceAnimationTime((float)deltaTime);
//                state.advanceAnimationTime(0.01666f);
                group.calculateFrame(m_animated, i);
            }
        }

        m_animated.setDirty(true, true);
    }

    @Override
    public void initialize()
    {
        setArmingCondition(new NewFrameCondition(this));
    }

    public boolean isAnimateFace() {
        return animateFace;
    }

    public void setAnimateFace(boolean animateFace) {
        this.animateFace = animateFace;
    }


    
    public void compute() {

    }

    
    public void commit() {

    }

}
