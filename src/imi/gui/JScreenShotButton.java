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
package imi.gui;

import imi.scene.processors.FlexibleCameraProcessor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 * @author Ronald E Dahlgren
 */
public class JScreenShotButton extends JButton implements ActionListener
{
    /** The camera for screen shotting **/
    private FlexibleCameraProcessor camProcessor = null;

    public JScreenShotButton(FlexibleCameraProcessor camera) {
        super();
        if (camera == null)
            throw new ExceptionInInitializerError("Must have a valid camera for screen shotting");
        camProcessor = camera;
        this.setActionCommand("screenSnap");
        this.addActionListener(this);
        this.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/CameraIcon.png")));
        this.setPressedIcon(new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/CameraIconPressed.png")));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == this && arg0.getActionCommand().equals("screenSnap"))
            screenShot();
    }

    private void screenShot()
    {
        camProcessor.takeSnap();
    }

}