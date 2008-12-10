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

import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.NamedUpdatableObject;
import java.util.Stack;

/**
 *  
 * 
 * @author Lou Hayt
 */
public class CharacterSteeringHelm extends NamedUpdatableObject
{
    private GameContext context = null;
    private Stack<Task> taskList = new Stack<Task>();
    private Task        currentTask = null;
    
    public CharacterSteeringHelm(String name, GameContext gameContext)
    {
        context = gameContext;
        setName(name);
        stop(); // starts disabled
    }
    
    @Override
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
    
    @Override
    public boolean toggleEnable()
    {
        enabledState = !enabledState;
        
        if (enabledState == false)
            context.resetTriggersAndActions();
        
        return enabledState;
    }
    
    public void clearTasks()
    {
        taskList.clear();
    }
    
    public void addTaskToTop(Task task) {
        taskList.add(task);
    }
    
    public void addTaskToBottom(Task task) {
        taskList.insertElementAt(task, 0);
    }
    
    public SpatialObject getGoal()
    {
        if (currentTask != null)
            return currentTask.getGoal();
        return null;
    }
    
    public Task getCurrentTask()
    {
        return currentTask;
    }
}
