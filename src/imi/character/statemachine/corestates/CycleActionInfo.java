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
package imi.character.statemachine.corestates;

/**
 *
 * @author Lou Hayt
 */
public class CycleActionInfo extends ActionInfo
{
    /** The basic animation settings will be used for
     the animation that enteres the state **/
    
    /** The animation that cycles once the enter animation is done **/
    private String  cycleAnimationName      = null;
    private float   cycleTransitionDuration = 0.2f;
    private float   cycleAnimationSpeed     = 1.0f;
    
    /** The animation to get out of the state **/
    private String  exitAnimationName       = null;
    private float   exitTransitionDuration  = 0.2f;
    private float   exitAnimationSpeed      = 1.0f;
    private boolean bExitAnimationReverse   = false;
    
    /** Constructor with body animation only **/
    public CycleActionInfo(String bodyEnterAnimationName, String bodyCycleAnimationName, String bodyExitAnimationName) 
    {
        super(bodyEnterAnimationName);
        cycleAnimationName = bodyCycleAnimationName;
        exitAnimationName  = bodyExitAnimationName;
    }
    
    /** Constructor with body animation and facial animation **/
    public CycleActionInfo(String bodyEnterAnimationName, String bodyCycleAnimationName, String bodyExitAnimationName, String facialAnimationName, float facialAnimationTimeIn, float facialAnimationTimeOut) 
    {
        super(bodyEnterAnimationName, facialAnimationName, facialAnimationTimeIn, facialAnimationTimeOut);
        cycleAnimationName = bodyCycleAnimationName;
        exitAnimationName  = bodyExitAnimationName;
    }
    
    /** Apply this configuration on that state,
     *   state must be an instance of CycleActionState **/
    @Override
    public void apply(ActionState state) 
    {
        if (!(state instanceof CycleActionState))
            return;
        super.apply(state);
        CycleActionState cycleAction = (CycleActionState)state;
        cycleAction.setSimpleAction(false);
        cycleAction.setCycleAnimationName(cycleAnimationName);
        cycleAction.setCycleTransitionDuration(cycleTransitionDuration);
        cycleAction.setCycleAnimationSpeed(cycleAnimationSpeed);
        cycleAction.setExitAnimationName(exitAnimationName);
        cycleAction.setExitTransitionDuration(exitTransitionDuration);
        cycleAction.setExitAnimationSpeed(exitAnimationSpeed);
        cycleAction.setExitAnimationReverse(bExitAnimationReverse);
    }

    public String getCycleAnimationName() {
        return cycleAnimationName;
    }

    public void setCycleAnimationName(String cycleAnimationName) {
        this.cycleAnimationName = cycleAnimationName;
    }

    public float getCycleAnimationSpeed() {
        return cycleAnimationSpeed;
    }

    public void setCycleAnimationSpeed(float cycleAnimationSpeed) {
        this.cycleAnimationSpeed = cycleAnimationSpeed;
    }

    public float getCycleTransitionDuration() {
        return cycleTransitionDuration;
    }

    public void setCycleTransitionDuration(float cycleTransitionDuration) {
        this.cycleTransitionDuration = cycleTransitionDuration;
    }

    public String getExitAnimationName() {
        return exitAnimationName;
    }

    public void setExitAnimationName(String exitAnimationName) {
        this.exitAnimationName = exitAnimationName;
    }

    public float getExitAnimationSpeed() {
        return exitAnimationSpeed;
    }

    public void setExitAnimationSpeed(float exitAnimationSpeed) {
        this.exitAnimationSpeed = exitAnimationSpeed;
    }

    public float getExitTransitionDuration() {
        return exitTransitionDuration;
    }

    public void setExitTransitionDuration(float exitTransitionDuration) {
        this.exitTransitionDuration = exitTransitionDuration;
    }

    public boolean isExitAnimationReverse() {
        return bExitAnimationReverse;
    }

    public void setExitAnimationReverse(boolean bExitAnimationReverse) {
        this.bExitAnimationReverse = bExitAnimationReverse;
    }
}
