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

import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.objects.Goal;
import imi.utils.input.InputScheme;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
import imi.tests.DemoBase2;
import imi.utils.input.NinjaControlScheme;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
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
            setModelFile("assets/models/ms3d/ninja.ms3d");
            setTextureFile("assets/textures/checkerboard2.PNG");
        }
    }
    
    public Ninja(String name, WorldManager wm)
    {
        super(name, wm);
        m_context = new NinjaContext(this);
    }
            
    public Ninja(String name, PMatrix origin, float visualScale, WorldManager wm)
    {
        super(name, origin, null, null, visualScale, wm);
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
        m_keyBindings.put(KeyEvent.VK_SHIFT,        TriggerNames.Movement_Modifier.ordinal());
        m_keyBindings.put(KeyEvent.VK_LEFT,         TriggerNames.Move_Left.ordinal());
        m_keyBindings.put(KeyEvent.VK_RIGHT,        TriggerNames.Move_Right.ordinal());
        m_keyBindings.put(KeyEvent.VK_UP,           TriggerNames.Move_Forward.ordinal());
        m_keyBindings.put(KeyEvent.VK_DOWN,         TriggerNames.Move_Back.ordinal());
//        m_keyBindings.put(KeyEvent.VK_W,        TriggerNames.Move_Forward.ordinal());
//        m_keyBindings.put(KeyEvent.VK_S,        TriggerNames.Move_Back.ordinal());
        m_keyBindings.put(KeyEvent.VK_CONTROL,      TriggerNames.Punch.ordinal());
        m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.GoSit.ordinal());
        m_keyBindings.put(KeyEvent.VK_BACK_SPACE,   TriggerNames.PositionGoalPoint.ordinal());
        m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.SelectNearestGoalPoint.ordinal());
        m_keyBindings.put(KeyEvent.VK_ADD,          TriggerNames.Move_Down.ordinal());
        m_keyBindings.put(KeyEvent.VK_SUBTRACT,     TriggerNames.Move_Up.ordinal());
        m_keyBindings.put(KeyEvent.VK_COMMA,        TriggerNames.Reverse.ordinal());
        m_keyBindings.put(KeyEvent.VK_PERIOD,       TriggerNames.NextAction.ordinal());
    }
            
    /**
     * This Ninja will be selected for input.
     */
    @Override
    public void selectForInput()
    {
        super.selectForInput();
        
        InputScheme scheme = ((JSceneEventProcessor)m_wm.getUserData(JSceneEventProcessor.class)).getInputScheme();
        if (scheme instanceof NinjaControlScheme)
        {
            ((NinjaControlScheme)scheme).setNinja(this);
            Goal goalPoint = (Goal)m_wm.getUserData(Goal.class);
            if (goalPoint != null)
            {
                ((NinjaContext)m_context).getSteering().setGoalPosition(goalPoint.getTransform().getLocalMatrix(false).getTranslation());
                ((NinjaContext)m_context).getSteering().setSittingDirection(goalPoint.getTransform().getLocalMatrix(false).getLocalZ());
                ((NinjaContext)m_context).getSteering().setGoal(goalPoint.getGoal());
            }
            
//            if (m_wm.getUserData(JFrame.class) != null)
//                ((DemoBase2)m_wm.getUserData(JFrame.class)).setGUI(m_jscene, m_wm, null, this);
        }
    }

}
