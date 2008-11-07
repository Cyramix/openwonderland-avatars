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

    private static float fAnimationTimeStep = 0.01f;



    /**
     * This constructor receives the skeleton node
     * @param instance
     */
    public CharacterAnimationProcessor(SkeletonNode skeleton) 
    {
        //m_animated = skeleton;
    }
    public CharacterAnimationProcessor(PPolygonModelInstance modelInst) 
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
                        //m_animated = (SkeletonNode) kid;
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
        // TODO: Map animation groups to the appropriate states
        
        AnimationGroup AnimationGroup = m_animated.getAnimationComponent().getGroup();
        int index = 0;
        while (true) // advance all times
        {
            AnimationState state = m_animated.getAnimationState(index);
            
            if (state == null)
                break;
            // TODO: use animatedcharacter interface
            if (!state.isPauseAnimation())
                state.advanceAnimationTime(fAnimationTimeStep);
        }

        // Calculate the final pose
        if (AnimationGroup != null)
            AnimationGroup.calculateDecalFrame(m_animated);
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
    
}
