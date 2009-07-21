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
import imi.character.MaleAvatarParams;
import imi.character.UnimeshCharacterParams;
import imi.character.avatar.Avatar;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * E.g. for a unimesh avatar loading, the unimesh model must have a skeleton.
 * @author Lou Hayt
 */
public class UnimeshTest extends DemoBase
{
    public UnimeshTest(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        new UnimeshTest(args);
    }

    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        // Create a simple floor
        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);

        // Change camera speed and position it
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.0f, -5.0f));

        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);

        // Load a unimesh avatar configuration
        UnimeshCharacterParams params = new UnimeshCharacterParams("Avatar", "assets/models/collada/Clothing/MaleClothing/MaleTennisShoes.dae", "TennisShoesShape", UnimeshCharacterParams.Sex.Male);
        Avatar avatar = new Avatar.AvatarBuilder(params.build(), wm).build();
        control.addCharacterToTeam(avatar);
    }
}
