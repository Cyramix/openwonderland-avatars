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

import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.objects.SpatialObject;

/**
 *  Walk forward or backwards for a set amount of time
 * 
 * @author Lou Hayt
 */
public class Walk implements Task
{
    private String  description  = "walk forward/backward for X secounds";
    private float   timeLength   = 0.0f;
    private boolean bForward     = true;
    private float timeCounter = 0.0f;
    
    private float turnTime = 0.5f;
    
    private NinjaContext ninjaContext = null;
    
    public Walk(String description, float time, boolean bForward, NinjaContext context) 
    {
        System.out.println(description);
        
        ninjaContext = context;
        this.description = description;
        timeLength = time;
        this.bForward = bForward;
        timeCounter = 0.0f;
    }

    public boolean verify() {
        if (timeCounter > timeLength)
            return false;
        return true;
    }

    public void update(float deltaTime) 
    {
        timeCounter += deltaTime;
        
        if (bForward)
        {
            ninjaContext.triggerReleased(TriggerNames.Move_Back.ordinal());
            ninjaContext.triggerPressed(TriggerNames.Move_Forward.ordinal());      
        }
        else
        {
            ninjaContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
            ninjaContext.triggerPressed(TriggerNames.Move_Back.ordinal());   
        }

        // stop turning hack
        if (timeCounter > turnTime)
        {
            ninjaContext.triggerReleased(TriggerNames.Move_Right.ordinal());
            ninjaContext.triggerReleased(TriggerNames.Move_Left.ordinal());
        }

        // stop walking 
        if (timeCounter > timeLength)
        {
            System.out.println("stopping walk back");
            return;
        }

        ninjaContext.getController().getWindow().setTitle("Walking Back");
    }

    public void onHold() {
    }

    public String getDescription() {
        return description;
    }
    
    public String getStatus() {
        return "nothing interesting";
    }

    public SpatialObject getGoal() {
        return null;
    }

}
