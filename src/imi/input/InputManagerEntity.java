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

package imi.input;

import imi.demos.DemoBase.SwingFrame;
import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javolution.util.FastTable;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * Gathers input from providers and tunnels it to input clients,
 * Should be a single entity of this type only.
 * This class is available as user data at the WorldManager
 *
 * @author Lou Hayt
 */
public class InputManagerEntity extends Entity
{
    AWTprovider AWTprovider;

    FastTable<InputClient> clients = new FastTable<InputClient>();

    /**
     * Default constructor, creates an AWT provider that captures keyboard
     * and mouse input from AWT.
     * @param wm
     */
    public InputManagerEntity(WorldManager wm)
    {
        super("InputManager");
        setupAWTprovider(wm);
        // Add the entity to the world manager
        wm.addEntity(this);
        // Add this input manager to the world manager for future access
        wm.addUserData(InputManagerEntity.class, this);
    }

    /**
     * Add an input client to be called back when there is input
     * @param client
     */
    public void addInputClient(InputClient client) {
        clients.add(client);
    }

    /**
     * Remove an input client from the list
     * @param client
     */
    public void removeInputClient(InputClient client) {
        clients.remove(client);
    }

    private void setupAWTprovider(WorldManager wm)
    {
        // Create AWT event listener
        Canvas canvas = ((OnscreenRenderBuffer)wm.getUserData(OnscreenRenderBuffer.class)).getCanvas();
        AWTInputComponent eventListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS);
        // Create AWT event processor
        AWTprovider  = new AWTprovider(eventListener, this);
        // Add the AWT components to the entity
        addComponent(ProcessorComponent.class, AWTprovider);
        addComponent(AWTInputComponent.class, eventListener);
    }

    void processKeyEvent(KeyEvent keyEvent) {
        for(int i = 0; i < clients.size(); i++)
            clients.get(i).processKeyEvent(keyEvent);
    }

    void processMouseEvent(MouseEvent mouseEvent) {
        for(int i = 0; i < clients.size(); i++)
            clients.get(i).processMouseEvent(mouseEvent);
    }

}
