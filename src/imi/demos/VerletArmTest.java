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
import imi.character.avatar.Avatar;
import imi.character.MaleAvatarParams;
import imi.objects.AvatarObjectCollection;
import imi.camera.FirstPersonCamState;
import imi.character.FemaleAvatarParams;
import imi.gui.JFrame_InstrumentationGUI;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import org.jdesktop.mtgame.WorldManager;
import imi.input.InputManagerEntity;
import imi.scene.PMatrix;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lou Hayt
 */
public class VerletArmTest  extends DemoBase
{
    public VerletArmTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        new VerletArmTest(args);
    }

    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);

        int numberOfAvatars = 1;
        
        // Create one object collection for all to use (for testing)
        AvatarObjectCollection objects = new AvatarObjectCollection("Objects", wm);

        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);
        
        // Make a chair and let the control the collection so it can delete it
        objects.generateChairs(new Vector3f(0.0f, 0.0f, 3.0f), 0.0f, 1);
        control.setObjectCollection(objects);
        
        // change camera speed and position it
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        for (int i = 0; i < numberOfAvatars; i++)
        {
            //FemaleAvatarParams params = new FemaleAvatarParams("Avatar" + i);
            MaleAvatarParams params = new MaleAvatarParams("Avatar" + i);
            params.configureTorso(2);

            //params.configureHead(preset++);
//            pos.x += 1.0f;
//            params.setOrigin(new PMatrix(pos));
            Avatar avatar = new Avatar.AvatarBuilder(params.build(), wm).build();
            control.addCharacterToTeam(avatar);
            avatar.setObjectCollection(objects);
        }
        
//        JFrame_InstrumentationGUI instruments = new JFrame_InstrumentationGUI(wm);
//        instruments.setVisible(true);
    }
}
