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
import imi.scene.animation.Animated;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationState;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 * This processor handles animating a character. Do to our additive blending
 * system, these animations are handled in a special, and specific way.
 * @author Ronald E Dahlgren
 */
public class CharacterAnimationProcessor extends ProcessorComponent
{
    private Animated                m_animated  = null;
    private PPolygonModelInstance   m_modelInst = null;

    private double oldTime = 0.0f;
    private float deltaTime = 0.0f;
    
    private boolean bEnable = true;

    /**
     * This constructor receives the skeleton node
     * @param instance
     */
    public CharacterAnimationProcessor(SkeletonNode skeleton) 
    {
        m_animated = skeleton;
//        if(skeleton.getParent() instanceof PPolygonModelInstance) 
//            m_modelInst = (PPolygonModelInstance) skeleton.getParent();
    }
    public CharacterAnimationProcessor(PPolygonModelInstance modelInst) 
    {
        m_modelInst = modelInst;
    }

    @Override
    public void compute(ProcessorArmingCollection collection) 
    {
        if (!bEnable)
            return;

        double newTime = System.nanoTime() / 1000000000.0;
        deltaTime = (float) (newTime - oldTime);
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
        int numberOfGroups = m_animated.getAnimationComponent().getGroups().size();
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
                // The new awesome way
                //state.advanceAnimationTime(deltaTime);
                // old lame way
                state.advanceAnimationTime(0.01f);
                group.calculateFrame(m_animated, i);
            }
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

    public boolean isEnable() {
        return bEnable;
    }

    public void setEnable(boolean bEnable) {
        this.bEnable = bEnable;
    }
    
}
