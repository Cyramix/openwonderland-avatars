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
package imi.character.statemachine;

import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationComponent.PlaybackMode;

/**
 * Contains all of the detailed configuration info to set up
 * a GameState to perform a fully body animation and possibly
 * a facial animation as well.
 * @author Lou Hayt
 */
public class StateInfo 
{
    /** Name of the body animation **/
    private String      animationName       = null;
    /** Body animation speed **/
    private float       animationSpeed      = 1.0f;
    /** Length of time to transition into the body animation **/
    private float       transitionDuration  = 0.2f;
    /** True to transition in reverse into the body animation **/
    private boolean     bTransitionReverseAnimation = false;
    /** Sets when the transition into the body animation completes **/
    private AnimationComponent.PlaybackMode cycleMode = AnimationComponent.PlaybackMode.PlayOnce;
    
    /** The facial animation will play when entering the state if not null **/
    private String facialAnimationName      = null;
    /** How long the facial transitions should take **/
    private float  facialAnimationTransitionTime    = 0.4f;
    /** How long the expression should be held **/
    private float  facialAnimationExpressionTime   = 2.0f;

    /** Constructor with body animation only **/
    public StateInfo(String bodyAnimationName) {
        animationName = bodyAnimationName;
    }
    
    /** Constructor with body animation and facial animation **/
    public StateInfo(String bodyAnimationName, String facialAnimationName, float facialAnimationTransitionLength, float facialAnimationHoldTime) {
        this.animationName          = bodyAnimationName;
        this.facialAnimationName    = facialAnimationName;
        this.facialAnimationTransitionTime  = facialAnimationTransitionLength;
        this.facialAnimationExpressionTime = facialAnimationHoldTime;
    }
    
    /** Apply this configuration on that state **/
    public void apply(GameState state) {
        state.setAnimationName(animationName);
        state.setAnimationSpeed(animationSpeed);
        state.setTransitionDuration(transitionDuration);
        state.setTransitionReverseAnimation(bTransitionReverseAnimation);
        state.setCycleMode(cycleMode);
        state.setFacialAnimationName(facialAnimationName);
        state.setFacialAnimationTransitionTime(facialAnimationTransitionTime);
        state.setFacialAnimationExpressionHoldTime(facialAnimationExpressionTime);
    }
    
    /** Name of the body animation **/
    public String getAnimationName() {
        return animationName;
    }
    
    /** Name of the body animation **/
    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }
    
    /** Body animation speed **/
    public float getAnimationSpeed() {
        return animationSpeed;
    }
    
    /** Body animation speed **/
    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    
    /** True to transition in reverse into the body animation **/
    public boolean isTransitionReverseAnimation() {
        return bTransitionReverseAnimation;
    }
    
    /** True to transition in reverse into the body animation **/
    public void setTransitionReverseAnimation(boolean bTransitionReverseAnimation) {
        this.bTransitionReverseAnimation = bTransitionReverseAnimation;
    }
    
    /** When the transition into the body animation completes **/
    public PlaybackMode getCycleMode() {
        return cycleMode;
    }
    
    /** Sets when the transition into the body animation completes **/
    public void setCycleMode(PlaybackMode cycleMode) {
        this.cycleMode = cycleMode;
    }

    /** The facial animation will play when entering the state if not null **/
    public String getFacialAnimationName() {
        return facialAnimationName;
    }
    
    /** The facial animation will play when entering the state if not null **/
    public void setFacialAnimationName(String facialAnimationName) {
        this.facialAnimationName = facialAnimationName;
    }
    
    /** Length of time to transition into the body animation **/
    public float getTransitionDuration() {
        return transitionDuration;
    }
    
    /** Length of time to transition into the body animation **/
    public void setTransitionDuration(float transitionDuration) {
        this.transitionDuration = transitionDuration;
    }
    
}
