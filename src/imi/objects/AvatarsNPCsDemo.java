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

package imi.objects;

import com.jme.math.Vector3f;
import imi.character.Character;
import imi.character.CharacterInitializationInterface;
import imi.character.MaleAvatarParams;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarContext;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoTo;
import imi.character.behavior.PlayAnimation;
import imi.scene.PMatrix;
import imi.utils.Updatable;
import imi.utils.UpdateProcessor;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * @author Lou Hayt
 */
@ExperimentalAPI
public class AvatarsNPCsDemo implements CharacterInitializationInterface
{
    Vector3f origin;
    Avatar npc1, npc2;
    Brain brain1, brain2;
    
    public AvatarsNPCsDemo(WorldManager wm, Vector3f origin, String baseURL)
    {
        this.origin = origin;
        
        // Create a simple floor
        //DemoBase.createSimpleFloor(wm, 10.0f, 10.0f, 3.5f, origin.add(new Vector3f(2.5f, 0.0f, 2.5f)), baseURL);
        
        // Get the object collection and generate some chairs
        float block = 3.0f;
        float halfBlock = 0.5f * block;
        AvatarObjectCollection objects = (AvatarObjectCollection)wm.getUserData(AvatarObjectCollection.class);
        if (objects == null)
                objects = new AvatarObjectCollection("Avatar Objects", wm);
        objects.addChair(origin.add(new Vector3f(1.5f, 0.0f, 1.5f)), Vector3f.UNIT_X, baseURL);
        objects.addChair(origin.add(new Vector3f(3.5f, 0.0f, 3.5f)), Vector3f.UNIT_Z, baseURL);
        
        // Create locations
        LocationNode chairGame1 = new LocationNode("Location 1", origin.add(Vector3f.ZERO), halfBlock, objects);
        LocationNode chairGame2 = new LocationNode("Location 2", origin.add(Vector3f.UNIT_X.mult(block)),  halfBlock, objects);
        LocationNode chairGame3 = new LocationNode("Location 3", origin.add(new Vector3f(block, 0.0f, block)),  halfBlock, objects);
        LocationNode chairGame4 = new LocationNode("Location 4", origin.add(Vector3f.UNIT_Z.mult(block)),  halfBlock, objects);

        // Create graph paths
//        objects.createConnection(chairGame1, chairGame2);
//        objects.createConnection(chairGame2, chairGame3);
//        objects.createConnection(chairGame3, chairGame4);
//        objects.createConnection(chairGame4, chairGame1);

        // Create baked paths
        chairGame1.addBakedConnection("yellowRoom", chairGame2);
        chairGame2.addBakedConnection("yellowRoom", chairGame3);
        chairGame3.addBakedConnection("yellowRoom", chairGame4);
        chairGame4.addBakedConnection("yellowRoom", chairGame1);

        chairGame1.addBakedConnection("lobbyCenter", chairGame4);
        chairGame4.addBakedConnection("lobbyCenter", chairGame3);
        chairGame3.addBakedConnection("lobbyCenter", chairGame2);
        chairGame2.addBakedConnection("lobbyCenter", chairGame1);
        
        // Set common traits for fast loading
        MaleAvatarParams params = new MaleAvatarParams("NPC 1");
        params.setBaseURL(baseURL);
        params.configureFeet(0);
        params.configureHead(2);
        params.configureLegs(0);
        params.configureTorso(0);
        params.setEyeballTexturePreset(0);
        
        // Create the first avatar NPC
        params.configureHair(3);
        params.setHairColorPreset(0);
        params.setShirtColorPreset(7, true);
        params.setShoesColorPreset(3, true);
        npc1 = new Avatar.AvatarBuilder(params.build(), wm).initializer(this).transform(new PMatrix(origin)).build();
        npc1.setObjectCollection(objects);
        
        // Set common traits for fast loading
        params = new MaleAvatarParams("NPC 1");
        params.setBaseURL(baseURL);
        params.configureFeet(0);
        params.configureHead(2);
        params.configureLegs(0);
        params.configureTorso(0);
        params.setEyeballTexturePreset(0);
        
        // Create the secound avatar NPC
        params.setName("NPC 2");
        params.configureHair(9);
        params.setHairColorPreset(5);
        params.setShirtColorPreset(13, true);
        params.setShoesColorPreset(0, true);
        npc2 = new Avatar.AvatarBuilder(params.build(), wm).initializer(this).transform(new PMatrix(origin.add(new Vector3f(2.5f, 0.0f, 2.5f)))).build();
        npc2.setObjectCollection(objects);
        
        // Match friends :)
        while(brain1 == null || brain2 == null)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(AvatarsNPCsDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Setting NPC friends");
        brain1.setFriend(npc2);
        brain2.setFriend(npc1);
    }
    
    public void initialize(Character character) {
        System.out.println("Initializing NPC: " + character.getName());
        if (character.getName().equals("NPC 1"))
            brain1 = new Brain(character, origin);
        else
            brain2 = new Brain(character, origin);
        origin.setX(origin.getX() + 2.0f);
    }
    
    private static class Brain extends Entity implements Updatable
    {
        Vector3f origin = new Vector3f();
        Character friend = null;
        UpdateProcessor updateProc = null;
        Character npc = null;
        float actionTimer = 0.0f;
        float actionTick  = 10.0f;
        
        String [] animations = {
                    "Bow",
                    "Clap",
                    "Cheer",
                    "Laugh",
                    "RaiseHand",
                    "Wave",
                    "AnswerCell",
                    "PublicSpeaking",
                    "PublicSpeaking",
                    "PublicSpeaking",
        };
        
        public Brain(Character npc, Vector3f origin) 
        {
            super(npc.getName() + "'s brain");
            this.origin.set(origin);
            this.npc = npc;
            actionTick = (float) (Math.random() * 10000 % 5 + 5);
            updateProc = new UpdateProcessor(this);
            addComponent(updateProc.getClass(), updateProc);
            npc.getWorldManager().addEntity(this);
        }
        
        public void update(float deltaTime) 
        {
            actionTimer += deltaTime;
            if (actionTimer > actionTick)
            {
                actionTimer = 0.0f;
                // Take action
                int action = (int)((Math.random() * 1000) % 3);
                switch(action)
                {
                    case 0:
                        playAnimation();
                        break;
                    case 1:
                        playAnimation();
//                        int direction = (int)((Math.random() * 1000) % 2);
//                        if (direction == 0)
//                            goRound();
//                        else
//                            goRoundOtherWay();
                        break;
                    case 2:
                        goSit();
                        break;
                }
            }
        }
        
        void goRound()
        {
            npc.keyPressed(KeyEvent.VK_1);
            npc.keyReleased(KeyEvent.VK_1);
        }
        
        void goRoundOtherWay()
        {
            npc.keyPressed(KeyEvent.VK_2);
            npc.keyReleased(KeyEvent.VK_2);
        }
        
        void goSit()
        {
            npc.keyPressed(KeyEvent.VK_3);
            npc.keyReleased(KeyEvent.VK_3);
        }

        void playAnimation()
        {
            String animation = animations[(int)((Math.random() * 1000) % animations.length)];
            CharacterBehaviorManager behavior = npc.getContext().getBehaviorManager();
            behavior.clearTasks();
            float randomFloat = (float)((Math.random() * 1000) % 5);
            Vector3f go = origin.add(new Vector3f(0.6f, 0.0f, randomFloat));
            if (friend != null)
            {
                Vector3f dir = (friend.getPositionRef().subtract(go)).normalize();
                behavior.addTaskToBottom(new GoTo(go, dir, npc.getContext()));
            }
            else
                behavior.addTaskToBottom(new GoTo(go, npc.getContext()));
            PlayAnimation task = new PlayAnimation(animation, (AvatarContext)npc.getContext());
            behavior.addTaskToBottom(task);
            behavior.setEnable(true);
        }

        public void setFriend(Character friend) {
            this.friend = friend;
        }
        
        public boolean isEnabled() {
            return updateProc.isEnabled();
        }

        public void setEnable(boolean state) {
            updateProc.setEnabled(state);
        }
    }
}
