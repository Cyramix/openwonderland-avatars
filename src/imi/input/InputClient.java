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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * InputClients are added to the InputManagerEntity that can be accssed from
 * the WorldManager as user data.
 * When additional input providers are added to the system this interface will
 * grow.
 * @author Lou Hayt
 */
public interface InputClient {
    /**
     * Process a key event
     * @param keyEvent
     */
    public void processKeyEvent(KeyEvent keyEvent);
    /**
     * Process a mouse event
     * @param mouseEvent
     */
    public void processMouseEvent(MouseEvent mouseEvent);
}
