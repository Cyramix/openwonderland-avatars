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


import com.jme.math.Vector3f;
import imi.character.Character;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.character.ninja.NinjaFemaleAvatarAttributes;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;



/**
 *
 * 
 * 
 *  VerletArm has its own test now imi.tests.VerletArmTest
 * 
 *  testing loading heads with different bind pose settings
 * 
 * 
 * @author Lou Hayt
 */
public class COLLADA_CharacterTest extends DemoBase
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(COLLADA_CharacterTest.class.getName());

    public COLLADA_CharacterTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        COLLADA_CharacterTest worldTest = new COLLADA_CharacterTest(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create avatar
        long startTime = System.nanoTime();
        NinjaAvatarAttributes attribs = new NinjaAvatarAttributes("WeirdGuy", 2, 3, 5, 10, 1);
//        NinjaFemaleAvatarAttributes attribs = new NinjaFemaleAvatarAttributes("WeirdChick", 0, 1, 1, 1, 1);
        NinjaAvatar avatar = new NinjaAvatar(attribs, wm);
        float time = (float)((System.nanoTime() - startTime) / 1000000000.0f);
        System.out.println("Constructing the male took: " + time);
        
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();
 
        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        bigHeadMode(avatar);
        makeAFist(avatar);
//        swapAvatarHead(avatar);
        // give me a tree explorer!
        TreeExplorer te = new TreeExplorer();
        SceneEssentials se = new SceneEssentials();
        se.setSceneData(avatar.getJScene(), avatar.getPScene(), avatar, wm, null);
        te.setExplorer(se);
        te.setVisible(true);
    }

    private void swapAvatarHead(Character avatar)
    {
        URL newHeadLocation = null;
        String fileProtocol = "file:///" + System.getProperty("user.dir") + "/";
        try {
            newHeadLocation = new URL(fileProtocol + "assets/models/collada/Heads/CaucasianHead/MaleCHead-NS.dae");
//            newHeadLocation = new URL(fileProtocol + "assets/models/collada/Heads/CaucasianHead/MaleMonkeyHead.dae");
        }
        catch (MalformedURLException ex) {
            logger.severe("Unable to form head URL");
        }

        if (newHeadLocation != null)
        {
            avatar.installHead(newHeadLocation, "Neck");
        }
    }

    private void bigHeadMode(Character avatar)
    {
        SkinnedMeshJoint joint = avatar.getSkeleton().getSkinnedMeshJoint("Head");
        avatar.getSkeleton().displaceJoint("Head", new Vector3f(0, 0.07f, 0));
        joint.getBindPose().setScale(2.0f);

        joint = avatar.getSkeleton().getSkinnedMeshJoint("rightHand");
        joint.getBindPose().setScale(2.0f);

        joint = avatar.getSkeleton().getSkinnedMeshJoint("leftHand");
        joint.getBindPose().setScale(2.0f);
    }

    public void makeAFist(Character avatar)
    {
        PMatrix xRotMat = new PMatrix();
        Vector3f xRotation = new Vector3f((float)(Math.PI * -0.4), 0.01f, 0);
        xRotMat.setRotation(xRotation);
        FastList<PNode> queue = new FastList<PNode>();
        queue.addAll(avatar.getSkeleton().getSkinnedMeshJoint("leftPalm").getChildren());
        queue.add(avatar.getSkeleton().getSkinnedMeshJoint("leftHandThumb2"));
        while (queue.isEmpty() == false)
        {
            PNode current = queue.removeFirst();
            if (!(current instanceof SkinnedMeshJoint))
                continue;

            SkinnedMeshJoint joint = (SkinnedMeshJoint)current;
            PMatrix bindPose = joint.getBindPose();
            bindPose.mul(xRotMat);
            if (current.getChildrenCount() > 0)
                queue.addAll(current.getChildren());
        }

        // Set palm transform
        float[] matFloats =
        {
            0.927f,-0.375f, 0f,         -0.018f,
            0.366f, 0.907f, -0.208f,    0.024f,
            0.078f, 0.193f, 0.978f,		0.004f,
            0,		0,		0,          1
        };

        avatar.getSkeleton().getSkinnedMeshJoint("leftPalm").getBindPose().set(matFloats);
    }
}
