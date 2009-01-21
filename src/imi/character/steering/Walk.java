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
package imi.character.steering;

import imi.character.*;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.objects.SpatialObject;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private float   timeCounter  = 0.0f;
    
    private float turnTime = 0.5f;
    
    private AvatarContext avatarContext = null;

    /**
     * Construct a new instance of the walk task
     * @param description A human-friendly description of this task instance
     * @param time How long to walk
     * @param bForward True for forward, false for reverse
     * @param context The context to operate on.
     */
    public Walk(String description, float time, boolean bForward, AvatarContext context) 
    {
        // Debugging / Diagnostic output
        //Logger.getLogger(Walk.class.getName()).log(Level.INFO, description);
        
        avatarContext = context;
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
            avatarContext.triggerReleased(TriggerNames.Move_Back.ordinal());
            avatarContext.triggerPressed(TriggerNames.Move_Forward.ordinal());      
        }
        else
        {
            avatarContext.triggerReleased(TriggerNames.Move_Forward.ordinal());
            avatarContext.triggerPressed(TriggerNames.Move_Back.ordinal());   
        }

        // stop turning hack
        if (timeCounter > turnTime)
        {
            avatarContext.triggerReleased(TriggerNames.Move_Right.ordinal());
            avatarContext.triggerReleased(TriggerNames.Move_Left.ordinal());
        }

        // stop walking 
        if (timeCounter > timeLength)
        {
            // Debugging / Diagnostic output
            //Logger.getLogger(Walk.class.getName()).log(Level.INFO, "Stopping walk back");
            return;
        }

        avatarContext.getController().getWindow().setTitle("Walking Back");
    }

    /**
     * This method is called whenever a task must be put on hold temporarily
     */
    public void onHold() {
    }

    /**
     * Return the human readable description of this walk task.
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Return a human readable string describing the status of this task.
     * @return
     */
    public String getStatus() {
        return "nothing interesting";
    }

    /**
     * This task has no goal. This method will always return null.
     * @return NULL
     */
    public SpatialObject getGoal() {
        return null;
    }

}
