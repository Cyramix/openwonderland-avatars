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
package imi.character;

import com.jme.math.Vector3f;
import javax.swing.JFrame;

/**
 * NinjaController contains most concerte code at this point
 * 
 * @author Lou Hayt
 */
public class CharacterController 
{
    protected boolean  bReverseHeading     = false;
    
    public void stop(){}
    
    public Vector3f getPosition() {
        return null;
    }
    
    public boolean isReverseHeading() {
        return bReverseHeading;
    }

    public void setReverseHeading(boolean bReverseHeading) {
        this.bReverseHeading = bReverseHeading;
    }
    
    public JFrame getWindow() {
        return null;
    }
}
