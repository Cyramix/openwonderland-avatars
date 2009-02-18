/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.applet;

import imi.character.CharacterAttributes;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class SingleAvatarTest_Applet extends AppletTest {

////////////////////////////////////////////////////////////////////////////////
// JApplet Specific Code
////////////////////////////////////////////////////////////////////////////////
    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    @Override
    public void init() {
        super.init();
    }

    // TODO overwrite start(), stop() and destroy() methods
    @Override
    public void start() {
        
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        //Execute a job on the event-dispatching thread:
        //destroying this applet's GUI.
        super.destroy();
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    @Override
    public void createDemoEntities(WorldManager wm) {
        CharacterAttributes male    = new MaleAvatarAttributes("Robert", Boolean.TRUE);
        Avatar avatar               = new Avatar(male, wm);
    }
}
