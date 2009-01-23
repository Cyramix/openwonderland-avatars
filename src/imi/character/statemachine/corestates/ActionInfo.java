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

import imi.character.statemachine.StateInfo;

/**
 * Contains all of the detailed configuration info to set up
 * the ActionState to perform a fully body animation and possibly
 * a facial animation as well.
 * @author Lou Hayt
 */
public class ActionInfo extends StateInfo
{
    /** true to keep repeating the body animation */
    private boolean bRepeat                 = false;
    /** true to have an oscilating repeat, false to have it loop */
    private boolean bRepeatWillOscilate     = false;

    /** Constructor with body animation only **/
    public ActionInfo(String bodyAnimationName) {
        super(bodyAnimationName);
    }
    
    /** Constructor with body animation and facial animation **/
    public ActionInfo(String bodyAnimationName, String facialAnimationName, float facialAnimationTimeIn, float facialAnimationTimeOut) {
        super(bodyAnimationName, facialAnimationName, facialAnimationTimeIn, facialAnimationTimeOut);
    }
    
    /** Apply this configuration on that state **/
    public void apply(ActionState state) {
        super.apply(state);
        state.setRepeat(bRepeat);
        state.setRepeatWillOscilate(bRepeatWillOscilate);
        if (state instanceof CycleActionState)
            ((CycleActionState)state).setSimpleAction(true);
    }
   
    /** true to keep repeating the body animation */
    public boolean isRepeat() {
        return bRepeat;
    }
    
    /** true to keep repeating the body animation */
    public void setRepeat(boolean bRepeat) {
        this.bRepeat = bRepeat;
    }
    
    /** true to have an oscilating repeat, false to have it loop */
    public boolean isRepeatWillOscilate() {
        return bRepeatWillOscilate;
    }
    
    /** true to have an oscilating repeat, false to have it loop */
    public void setRepeatWillOscilate(boolean bRepeatWillOscilate) {
        this.bRepeatWillOscilate = bRepeatWillOscilate;
    }
}
