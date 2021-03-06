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
package imi.character.behavior;

import imi.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import java.util.Stack;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *  This class provides a convenient framework for adding movement tasks to
 * a character instance.
 * 
 * @author Lou Hayt
 */
@ExperimentalAPI
public class CharacterBehaviorManager
{
    private GameContext context = null;
    private Stack<Task> taskList = new Stack<Task>();
    private Task        currentTask = null;
    protected String name = "nameless";
    protected boolean isNamed = false;
    protected boolean enabledState = true;

    /**
     * Create a new instance with the given name using the provided GameContext
     * @param name
     * @param gameContext
     */
    public CharacterBehaviorManager(String name, GameContext gameContext)
    {
        context = gameContext;
        setName(name);
        stop(); // starts disabled
    }

    /**
     * This method updates the task list, making sure that the current and valid
     * task is the top of the stack.
     * @param deltaTime
     */
    public void update(float deltaTime)
    {
        if (!enabledState || taskList.isEmpty())
            return;
        
        // Get current task
        if (currentTask != null && currentTask != taskList.peek())
            currentTask.onHold();
        currentTask = taskList.peek();
        
        // Verify the task
        if (!currentTask.verify())
        {
            taskList.remove(currentTask);
            context.resetTriggersAndActions();
            currentTask = null;
            return;
        }
        
        // Do it!
        currentTask.update(deltaTime);
    }

    /**
     * enable / disable
     * @return
     */
    public boolean toggleEnable()
    {
        enabledState = !enabledState;
        
        if (enabledState == false)
            context.resetTriggersAndActions();
        
        return enabledState;
    }

    /**
     * Clears out the accumulated stack of tasks
     */
    public void clearTasks()
    {
        context.resetTriggersAndActions();
        taskList.clear();
    }

    /**
     * Adds a task to the stack with top priority
     * @param task
     */
    public void addTaskToTop(Task task) {
        taskList.add(task);
    }

    /**
     * Adds a task to the stack with the lowest priority
     * @param task
     */
    public void addTaskToBottom(Task task) {
        taskList.insertElementAt(task, 0);
    }

    /**
     * Return the SpatialObject that the current task is heading towards
     * @return
     */
    public SpatialObject getGoal()
    {
        if (currentTask != null)
            return currentTask.getGoal();
        return null;
    }

    /**
     * Return the currently processing task.
     * @return
     */
    public Task getCurrentTask()
    {
        return currentTask;
    }

    public void setName(String name_)
    {
        name = name_;
        isNamed = true;
    }

    public String getName()
    {
        if ( isNamed )
            return name;
        else
            return super.toString();
    }

    public void start() {
        enabledState = true;
    }

    public void stop() {
        enabledState = false;
    }

    public void setEnable( boolean state ) {
        enabledState = state;
    }

    public boolean isEnabled() {
        return enabledState;
    }
}
