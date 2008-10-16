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
package imi.scene.processors;

import imi.utils.input.DemoSchemeSigraph;
import org.jdesktop.mtgame.AWTInputComponent;
import imi.scene.JScene;
import imi.utils.input.DefaultScheme;
import imi.utils.input.InputScheme;
import java.awt.event.KeyEvent;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;
import org.jdesktop.mtgame.AwtEventCondition;

/**
 *
 * @author Lou Hayt
 */
public final class JSceneAWTEventProcessor extends AWTEventProcessorComponent
{
    private JScene      m_jscene = null;
    private InputScheme m_scheme = new DefaultScheme();
    private FastList<InputScheme> m_schemeList = new FastList<InputScheme>();
    
    public JSceneAWTEventProcessor(AWTInputComponent listener, JScene scene, Entity myEntity) 
    {
        super(listener);
        setJScene(scene);
        setEntity(myEntity);
        addScheme(m_scheme);
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new AwtEventCondition(this));
    }
    
    @Override
    public void initialize() 
    {
//        super.initialize();
//        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
//        collection.addCondition(new Awt(this));
        setArmingCondition(new AwtEventCondition(this));
        //setRunInRenderer(true);
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) 
    {
        Object[] events = getEvents();
        m_scheme.processEvents(events);
        for (int i=0; i<events.length; i++) 
        {
            if (events[i] instanceof KeyEvent) 
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
    }

    private void processKeyEvent(KeyEvent ke) 
    {
        int index = 0;
        
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            // Smooth normals toggle
            if (ke.getKeyCode() == KeyEvent.VK_ADD) 
            {
                index = m_schemeList.indexOf(m_scheme);
                index++;
                if (index > m_schemeList.size()-1)
                    m_scheme = m_schemeList.get(0);
                else
                    m_scheme = m_schemeList.get(index);
                
                m_scheme.setJScene(m_jscene);
            }
            
            // Toggle PRenderer mesh display
            if (ke.getKeyCode() == KeyEvent.VK_SUBTRACT)
            {
                index = m_schemeList.indexOf(m_scheme);
                index--;
                if (index < 0)
                    m_scheme = m_schemeList.get(m_schemeList.size()-1);
                else
                    m_scheme = m_schemeList.get(index);   
                
                m_scheme.setJScene(m_jscene);
            }
        }
    }
    
    public void clearSchemes()
    {
        m_scheme = m_schemeList.get(0);
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
    }
    
    public InputScheme setDefault(InputScheme defaultScheme) 
    {
        m_scheme = defaultScheme;
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
        return m_scheme;
    }
    
    public void addScheme(InputScheme scheme)
    {
        m_schemeList.add(scheme);
    }

    public JScene getJScene() 
    {
        return m_jscene;
    }

    public void setJScene(JScene jscene) 
    {
        m_jscene = jscene;
        m_scheme.setJScene(jscene);
    }
    
    public InputScheme getInputScheme()
    {
        return m_scheme;
    }
    
    
}
