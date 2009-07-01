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
package imi.character;

import imi.scene.animation.Animated;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationState;
import imi.utils.instruments.Instrumentation;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This processor handles animating a {@code Character}. Do to our additive blending
 * system, these animations are handled in a special way.
 * @author Ronald E Dahlgren
 */
public final class CharacterAnimationProcessor extends ProcessorComponent
{
    /** System wide instrumentation hook! **/
    private final Instrumentation     instruments;
    /** That which we animate **/
    private final Animated      m_animated;

    /** Time caching data members **/
    private double oldTime = 0.0f;
    private double deltaTime = 0.0f;

    /** Enable control facial animation **/
    private boolean animateFace = true;

    /**
     * This constructor receives the skeleton node
     * @param skeleton
     * @param wm
     */
    public CharacterAnimationProcessor(Animated animated, WorldManager wm)
    {
        m_animated = animated;
        instruments = (Instrumentation)wm.getUserData(Instrumentation.class);
    }

    /**
     * {@inheritDoc ProcessorComponent}
     */
    @Override
    public synchronized void compute(ProcessorArmingCollection collection)
    {

    }

    /**
     * {@inheritDoc ProcessorComponent}
     */
    @Override
    public void commit(ProcessorArmingCollection collection)
    {
        if (!isEnabled()) // Are we enabled?
            return;
        // Are instruments turning us off?
        if (instruments != null &&
            instruments.isSubsystemEnabled(Instrumentation.InstrumentedSubsystem.AnimationSystem) == false)
            return;

        double newTime = System.nanoTime() / 1000000000.0;
        deltaTime = (newTime - oldTime);
        oldTime = newTime;

        // If the modelInst constructor was used
        if (m_animated == null)
            throw new IllegalStateException("Animating null is undefined.");

        // Assuming a one to one relationship between groups and states,
        // each AnimationState is holding state for the animation stored in
        // that same AnimationGroup index
        // Current groups in use: 0 - full body animations, 1 - face only animations
        AnimationState state = null;
        AnimationGroup group = null;

        int numberOfGroups;
        if (!animateFace)
            numberOfGroups = 1; // Ignore the face group
        else
            numberOfGroups = m_animated.getAnimationComponent().getGroupCount();

        for (int i = 0; i < numberOfGroups; i++)
        {
            state = m_animated.getAnimationState(i);
            group = m_animated.getAnimationGroup(i);

            if (state == null || group == null)
                throw new IllegalStateException("The Animated's states do not map to it's groups. " +
                        "state: " + state + ", group: " + group + ", index: " + i);
            else if (!state.isPauseAnimation())
            {
                state.advanceAnimationTime((float)deltaTime);
                group.calculateFrame(m_animated, i);
            }
        }

        m_animated.setDirty(true, true);
    }

    /**
     * {@inheritDoc ProcessorComponent}
     */
    @Override
    public void initialize()
    {
        setArmingCondition(new NewFrameCondition(this));
    }

    /**
     * Determine if facial animations are being used.
     * @return True if enabled
     */
    public boolean isAnimatingFace() {
        return animateFace;
    }

    /**
     * Enable / Disable facial animations.
     * @param animateFace True to enable
     */
    public void setAnimateFace(boolean animateFace) {
        this.animateFace = animateFace;
    }
}
