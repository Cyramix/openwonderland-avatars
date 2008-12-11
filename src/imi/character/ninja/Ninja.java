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
package imi.character.ninja;

import imi.character.CharacterAttributes;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.utils.input.InputScheme;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.awt.event.KeyEvent;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/*
 * Ninja!
 * 
 * @author Lou Hayt
 */
public class Ninja extends imi.character.Character
{   
    public Ninja(CharacterAttributes attributes, WorldManager wm)
    {
        super(attributes, wm);
        m_context = instantiateContext();       // Initialize m_context
    }

    /**
     * Construct a new instance with the provided configuration file.
     * @param configurationFile
     * @param wm
     */
    public Ninja(URL configurationFile, WorldManager wm)
    {
        super(configurationFile, wm);
    }
     
    protected GameContext instantiateContext() {
        return new NinjaContext(this);
    }
   
    @Override
    protected void initKeyBindings() 
    {   
        m_keyBindings.put(KeyEvent.VK_SHIFT,        TriggerNames.Movement_Modifier.ordinal());
        m_keyBindings.put(KeyEvent.VK_LEFT,         TriggerNames.Move_Left.ordinal());
        m_keyBindings.put(KeyEvent.VK_RIGHT,        TriggerNames.Move_Right.ordinal());
        m_keyBindings.put(KeyEvent.VK_UP,           TriggerNames.Move_Forward.ordinal());
        m_keyBindings.put(KeyEvent.VK_DOWN,         TriggerNames.Move_Back.ordinal());
        m_keyBindings.put(KeyEvent.VK_CONTROL,      TriggerNames.Punch.ordinal());
        m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.ToggleSteering.ordinal());
        m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.GoSit.ordinal());
        m_keyBindings.put(KeyEvent.VK_ADD,          TriggerNames.Move_Down.ordinal());
        m_keyBindings.put(KeyEvent.VK_SUBTRACT,     TriggerNames.Move_Up.ordinal());
        m_keyBindings.put(KeyEvent.VK_COMMA,        TriggerNames.Reverse.ordinal());
        m_keyBindings.put(KeyEvent.VK_PERIOD,       TriggerNames.NextAction.ordinal());
        m_keyBindings.put(KeyEvent.VK_1,            TriggerNames.GoTo1.ordinal());
        m_keyBindings.put(KeyEvent.VK_2,            TriggerNames.GoTo2.ordinal());
        m_keyBindings.put(KeyEvent.VK_3,            TriggerNames.GoTo3.ordinal());
        m_keyBindings.put(KeyEvent.VK_G,            TriggerNames.SitOnGround.ordinal());
        m_keyBindings.put(KeyEvent.VK_0,            TriggerNames.Smile.ordinal());
        m_keyBindings.put(KeyEvent.VK_9,            TriggerNames.Frown.ordinal());
        m_keyBindings.put(KeyEvent.VK_8,            TriggerNames.Scorn.ordinal());
        m_keyBindings.put(KeyEvent.VK_BACK_QUOTE,   TriggerNames.ToggleArm.ordinal());
        m_keyBindings.put(KeyEvent.VK_P,            TriggerNames.Point.ordinal());
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
//            Goal goalPoint = (Goal)m_wm.getUserData(Goal.class);
//            if (goalPoint != null)
//            {
//                ((NinjaContext)m_context).getSteering().setGoalPosition(goalPoint.getTransform().getLocalMatrix(false).getTranslation());
//                ((NinjaContext)m_context).getSteering().setSittingDirection(goalPoint.getTransform().getLocalMatrix(false).getLocalZ());
//                ((NinjaContext)m_context).getSteering().setGoal(goalPoint.getGoal());
//            }
//            
////            if (m_wm.getUserData(JFrame.class) != null)
////                ((DemoBase2)m_wm.getUserData(JFrame.class)).setGUI(m_jscene, m_wm, null, this);
        }
    }

}
