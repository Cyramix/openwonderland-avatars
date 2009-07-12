/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.character.behavior;

import imi.character.avatar.AvatarContext;
import imi.character.statemachine.corestates.IdleState;
import imi.objects.SpatialObject;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *  Play an animation
 * 
 * @author Lou Hayt
 */
@ExperimentalAPI
public class PlayAnimation implements Task
{
    private String  description  = "play an animation";
    private String  animation = null;
    private int animationIndex = -1;
    private AvatarContext avatarContext = null;
    private boolean started = false;
    private boolean done = false;
    
    public PlayAnimation(String name, AvatarContext context) 
    {
        avatarContext = context;
        animation = name;
        animationIndex = context.getGenericAnimationIndex(name);
        if (animationIndex == -1)
            throw new IllegalArgumentException("animation index not found");
    }

    public boolean verify() {
        if (done)
            return false;
        return true;
    }

    public void update(float deltaTime) 
    {
        if (!started && !avatarContext.isTransitioning())
        {
            avatarContext.performAction(animationIndex);
            started = true;
        }
        else if (!avatarContext.isTransitioning())
            done = true;
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