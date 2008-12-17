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


/**
 * This class is the preferred method of extended the run-time engine.  Extend this class and add it to the
 * ExecutionManager and it will receive the regular synchronized per-frame update call.  Example apps use this as their main
 * game logic controller class.  Derived classes are used to perform Entity updating, networking, camera management, etc.
 *
 * @author Shawn Kendall
 */
public class NamedUpdatableObject implements Updatable, Named
{
    protected String objectName = "nameless";
    protected boolean isNamed = false;
    protected boolean enabledState = true;
    
    public void setName(String name_)
    {
        objectName = name_;
        isNamed = true;
    }
    
    public String getName()
    {
        if ( isNamed )
            return objectName;
        else
            return super.toString();
    }
    
    @Override
    public String toString()
    {
        if ( isNamed )
            return objectName;
        else
            return super.toString();
    }
    
    public void start()
    {
        enabledState = true;
    }
    
    public void stop()
    {
        enabledState = false;
    }

    public void setEnable( boolean state )
    {
        enabledState = state;
    }
    
    public boolean toggleEnable()
    {
        enabledState = !enabledState;
        return enabledState;
    }
    
    public boolean getEnable()
    {
        return enabledState;
    }
    
    public void initialize()
    {
        //Debug.println(this +": initialize()");
    }
        
    public void update(float deltaTime)
    {
        //Debug.println(this +": update()");
    }
}
