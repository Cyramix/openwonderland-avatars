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

package imi.demos;

import com.jme.math.Vector3f;
import imi.camera.FirstPersonCamState;
import imi.character.avatar.Avatar;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.scene.PMatrix;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * Loads all the avatar configurations
 * @author Lou Hayt
 */
public class ConfiguredAvatarParty extends DemoBase
{
    /** Configuration folder **/
    private static final File configurationFolder = new File("assets/configurations/");
    /** Maximum avatars to load **/
    private static final int maxAvatars = 5;
    /** First avatar index to load **/
    private static final int firstAvatarIndex = 0;

    public ConfiguredAvatarParty(String[] args) {
        super(args);
    }

    public static void main(final String[] args) {
        new ConfiguredAvatarParty(args);
    }

    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);

        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);

        // Collect configuration files
        File [] files = configurationFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if(name.contains(".xml") || name.contains(".XML"))
                    return true;
                return false;
            }
        });
        // Load configurations
        Vector3f pos = new Vector3f(-20.0f, 0.0f, 3.0f);
        for (int i = firstAvatarIndex; i < files.length && i < maxAvatars; i++)
        {
            System.out.println("Avatar " + i + " file: " + files[i]);
            try {
                Avatar newAvatar = new Avatar.AvatarBuilder(files[i].toURI().toURL(), wm).transform(new PMatrix(pos)).build();
                control.addCharacterToTeam(newAvatar);
                pos.x += 1.0f;
            } catch (MalformedURLException ex) {
                Logger.getLogger(ConfiguredAvatarParty.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}

