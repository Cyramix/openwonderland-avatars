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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.scene.processors;

import imi.scene.PNode;
import imi.scene.animation.AnimationState;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.TransitionQueue;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.NewFrameCondition;

/**
 *
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class SkinnedAnimationProcessor extends ProcessorComponent
{
    private SkeletonNode m_animated = null;
    private PPolygonModelInstance m_modelInst = null;

    private float fAnimationTimeStep = 0.01f;

    /**
     * This constructor receives the skeleton node
     * @param instance
     */
    public SkinnedAnimationProcessor(SkeletonNode skeleton) 
    {
        m_animated = skeleton;
    }
    public SkinnedAnimationProcessor(PPolygonModelInstance modelInst) 
    {
        m_modelInst = modelInst;
    }

    @Override
    public void compute(ProcessorArmingCollection collection) 
    {
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
        
        // Slightly hardcoded section follows. Avert your eyes!
        AnimationGroup AnimationGroup = m_animated.getAnimationComponent().getGroup();
        AnimationState AnimationState = m_animated.getAnimationState();

        // advance animation time
        if (!AnimationState.isPauseAnimation())
        {
            AnimationState.advanceAnimationTime(fAnimationTimeStep);

            // calculate frame
            if (AnimationGroup != null)
                AnimationGroup.calculateFrame(m_animated);
        }
    }

    @Override
    public void commit(ProcessorArmingCollection collection) 
    {
        if (m_animated != null)
            m_animated.setDirty(true, true);
    }

    @Override
    public void initialize() 
    {
        setArmingCondition(new NewFrameCondition(this));
    }

    /**
     * The amount of time to advance every frame
     * @return
     */
    public float getAnimationTimeStep() {
        return fAnimationTimeStep;
    }

    /**
     * The amount of time to advance every frame
     * @param fAnimationTimeStep
     */
    public void setAnimationTimeStep(float fAnimationTimeStep) {
        this.fAnimationTimeStep = fAnimationTimeStep;
    }
    
}
