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
package imi.tests;

import imi.gui.JPanel_ShaderProperties;
import javax.swing.JFrame;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 */
public class TestDynamicCellRenderer 
{
    public static void main(String[] args)
    {
        
        JFrame frame = new JFrame();
        JPanel_ShaderProperties props = new JPanel_ShaderProperties(null, new WorldManager("nameless"));
        frame.add(props);
        frame.setSize(350, 400);
        frame.setVisible(true);
    }
}
