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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.character.ninja;

import com.jme.math.Vector3f;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.objects.Goal;
import imi.utils.input.InputScheme;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.WorldManager;

/*
 * Ninja!
 * 
 * @author Lou Hayt
 */
public class Ninja extends imi.character.Character
{
    public class NinjaAttributes extends Attributes
    {
        public NinjaAttributes(String name) {
            super(name);
            setModelFile("ninja.ms3d");
            setTextureFile("checkerboard2.PNG");
        }
    }
    
    public Ninja(String name, WorldManager wm)
    {
        super(name, wm);
        m_context = new NinjaContext(this);
    }
    
    public Ninja(String name, WorldManager wm, String configuration) 
    {
        super(name, wm, configuration);
        m_context = new NinjaContext(this);
    }
        
    // modelIMI can be null
    public Ninja(String name, PMatrix origin, String modelIMI, WorldManager wm)
    {
        super(name, origin, modelIMI, wm);
        m_context = new NinjaContext(this);
    }
    
    public Ninja(String name, PMatrix origin, String modelFile, String textureFile, float visualScale, WorldManager wm)
    {
        super(name, origin, modelFile, textureFile, visualScale, wm);
        m_context = new NinjaContext(this);
    }
    
    @Override
    protected Attributes createAttributes(String name)
    {
        return new NinjaAttributes(name);
    }
    
    @Override
    protected void initKeyBindings() 
    {   
        m_keyBindings.put(KeyEvent.VK_SHIFT,    TriggerNames.Movement_Modifier.ordinal());
        m_keyBindings.put(KeyEvent.VK_LEFT,     TriggerNames.Move_Left.ordinal());
        m_keyBindings.put(KeyEvent.VK_RIGHT,    TriggerNames.Move_Right.ordinal());
        m_keyBindings.put(KeyEvent.VK_UP,       TriggerNames.Move_Forward.ordinal());
        m_keyBindings.put(KeyEvent.VK_DOWN,     TriggerNames.Move_Back.ordinal());
        m_keyBindings.put(KeyEvent.VK_CONTROL,  TriggerNames.Punch.ordinal());
        m_keyBindings.put(KeyEvent.VK_ENTER,    TriggerNames.GoSit.ordinal());
        m_keyBindings.put(KeyEvent.VK_BACK_SPACE, TriggerNames.PositionGoalPoint.ordinal());
        m_keyBindings.put(KeyEvent.VK_HOME, TriggerNames.SelectNearestGoalPoint.ordinal());
    }
            
    /**
     * This Ninja will be selected for input.
     */
    @Override
    public void selectForInput()
    {
        super.selectForInput();
        
        InputScheme scheme = ((JSceneAWTEventProcessor)m_wm.getUserData(JSceneAWTEventProcessor.class)).getInputScheme();
        if (scheme instanceof NinjaControlScheme)
        {
            ((NinjaControlScheme)scheme).setNinja(this);
            Goal goalPoint = (Goal)m_wm.getUserData(Goal.class);
            if (goalPoint != null)
            {
                ((NinjaContext)m_context).getSteering().setGoalPosition(goalPoint.getTransform().getLocalMatrix(false).getTranslation());
                ((NinjaContext)m_context).getSteering().setSittingDirection(goalPoint.getTransform().getLocalMatrix(false).getLocalZ().mult(-1.0f));
            }
        }
    }

}
