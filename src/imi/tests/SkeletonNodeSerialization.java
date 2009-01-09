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
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;
import org.jdesktop.wonderland.common.comms.WonderlandObjectOutputStream;

/**
 * Serialize the skeleton!
 * @author Ronald E Dahlgren
 */
public class SkeletonNodeSerialization extends DemoBase
{
    private final static Logger logger = Logger.getLogger(SkeletonNodeSerialization.class.getName());
    private final static File saveFile = new File("/work/avatars/assets/skeletons/maleSkeleton.bs");

    public SkeletonNodeSerialization(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        SkeletonNodeSerialization worldTest = new SkeletonNodeSerialization(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create avatar
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar", false, false), wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();

        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Wait for the avatar to load
        while (!avatar.isInitialized() || avatar.getModelInst() == null)
        {
            try {
            Thread.sleep(25000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(COLLADA_CharacterTest.class.getName()).log(Level.SEVERE, null, ex);
                                                  }
        }
        SkeletonNode oldSkeleton = avatar.getSkeleton();
        // All of this is required to remove the avatar without null pointer-ing somewhere
        control.getNinjaTeam().clear();
        control.setNinja(null);
        avatar.die();
        avatar = null;

        // Do a little preparation
        PNode skeletonRoot = oldSkeleton.getSkeletonRoot();
        oldSkeleton.removeAllChildren();
        oldSkeleton.setSkeletonRoot(skeletonRoot);
        oldSkeleton.dump();
        logger.info("Serializing skeleton!");
        serializeSkeleton(oldSkeleton);

        SkeletonNode resultSkeleton = deserializeSkeleton();
        if (resultSkeleton != null)
        {
            logger.info("Reconstituted skeleton!");
            resultSkeleton.dump();
        }

        Entity psceneEntity = new Entity("ThePscene");

        PScene ps = new PScene("thePscene", wm);
        JScene js = new JScene(ps);
        ps.setJScene(js);
        js.setRenderBothBool(true);
        RenderComponent rc = wm.getRenderManager().createRenderComponent(js);
        psceneEntity.addComponent(RenderComponent.class, rc);
        wm.addEntity(psceneEntity);

        ps.addModelInstance(resultSkeleton, new PMatrix());
    }

    private void serializeSkeleton(SkeletonNode skeleton)
    {
        FileOutputStream fos = null;
        WonderlandObjectOutputStream out = null;
        try
        {
          fos = new FileOutputStream(saveFile);
          out = new WonderlandObjectOutputStream(fos);
          out.writeObject(skeleton);
          out.close();
        }
        catch(IOException ex)
        {
          ex.printStackTrace();
        }
    }

    private SkeletonNode deserializeSkeleton()
    {
        SkeletonNode result = null;
        FileInputStream fis = null;
        WonderlandObjectInputStream in = null;

        try
        {
            fis = new FileInputStream(saveFile);
            in = new WonderlandObjectInputStream(fis);
            result = (SkeletonNode)in.readObject();
            in.close();
        }
        catch(Exception ex)
        {
            logger.severe("Uh oh! " + ex.getMessage());
            ex.printStackTrace();
        }

        return result;
    }
}


