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
package imi.character.networking;

import imi.character.*;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.Character;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.statemachine.GameContextListener;
import imi.character.statemachine.corestates.IdleState;
import imi.scene.PMatrix;
import imi.scene.Updatable;
import imi.scene.utils.visualizations.VisuManager;
import imi.utils.PMathUtils;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class represents an individual client connected to the darkstar server.
 * @author Lou Hayt
 */
public class CharacterClient extends Client implements Updatable
{
    /** user ID to CharacterData map (for the current game room) */
    protected HashMap<Integer, UserData> characterData = new HashMap<Integer, UserData>();
    /** The avatar representation **/
    protected Character    character = null;
    /** Used for creating avatar attribute objects **/
    protected WorldManager worldManager = null;
        
    /** The extension may implement a JNag interface **/
    protected ClientExtension clientExtension = null;
    
    /** Updates messages occure at a fixed interval **/
    private float    clientTimer         = 0.0f;
    private float    clientUpdateTick    = 1.0f / 30.0f;

    /** History for smoothing **/
    private Vector3f prevPos = new Vector3f();
    private Vector3f prevDir = new Vector3f(0.0f, 0.0f, -1.0f);

    /** Assorted pieces for smoothing **/
    private float positionPullStrength          = 0.01f;
    private float positionMinDistanceForPull    = 0.1f;
    private float positionMaxDistanceForPull    = 3.0f;

    /** Smoothing for verlet hands **/
    private float handPullStrength          = 0.1f;
    private float handMaxDistanceForPull    = 1.0f;

    /** **/
    private boolean male = true;
    private int feet  = -1;
    private int legs  = -1;
    private int torso = -1;
    private int hair  = -1;
    private int head  = -1;
    private int skinTone  = -1;
    private int eyeColor  = -1;
    
    private String userName = null;
    private String password = null;
        
    // Test
    VisuManager vis = null;
        
    public CharacterClient(Character character)
    {
        super();
        this.character = character;
        worldManager = character.getWorldManager(); 
        
        // Test
        vis = new VisuManager(userName, worldManager);
        vis.setWireframe(false);
    }

    public CharacterClient(Character character, boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor) 
    {
        this(character);
        this.male  = male;
        this.feet  = feet;
        this.legs  = legs;
        this.torso = torso;
        this.hair  = hair;
        this.head  = head;
        this.skinTone  = skinTone;
        this.eyeColor  = eyeColor;
    }
    
    public void performAnimation(int actionIndex, boolean client, boolean allUsers, int specificUser)
    {
        if (client)
            ((AvatarContext)character.getContext()).performAction(actionIndex);
        
        if (allUsers)
        {
            for (UserData data : characterData.values())
                ((AvatarContext)data.user.getContext()).performAction(actionIndex);
        }
        else if (characterData.get(specificUser) != null)
        {
            Character user = characterData.get(specificUser).user;
            ((AvatarContext)user.getContext()).performAction(actionIndex);
        }
    }
    
    @Override
    public void login()
    {
        character.getContext().removeAllGameContextListeners();
        releaseJnagSession();
        character.getContext().addGameContextListener(new ClientContextListener(this));
        super.login();
    }
    
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        String player;
        if (userName != null)
            player = userName;
        else
            player = getRandomName();
        setGUIStatus("Logging in as " + player);
        String pass;
        if (password != null)
            pass = password;
        else
            pass = "guest";
        return new PasswordAuthentication(player, pass.toCharArray());
    }
    
    @Override
    public void loggedIn() 
    {
        super.loggedIn();
        serverProxy.setAvatarInfo(male, feet, legs, torso, hair, head, skinTone, eyeColor);
    }
    
    @Override
    protected void setupGameSession(String message) 
    {
        if (clientExtension != null)
        {
            clientExtension.releaseJNagSession();
            clientExtension = null;
        }
        
        if (message.contains("cahua") || message.contains("Cahua"))
            clientExtension = new CahuaClientExtention(jnagSession, this);
    }
    
    @Override
    public void releaseJnagSession() 
    {
        super.releaseJnagSession();
        if (clientExtension != null)
            clientExtension.releaseJNagSession();   
    }
    
    /**
     * This update is called on the character's update and its job is to
     * send periodic updates to the server with the character's position
     * and possibly the arms positions (if the arms are enabled).
     * @param deltaTime
     */
    public void update(float deltaTime) 
    {    
        if (connected)
        {
            ////////////////////////////////////////////////////
            // Manage peers (can be on another update thread) //
            ////////////////////////////////////////////////////
            
            for (UserData data : characterData.values())
            {
                if (!data.user.isInitialized() || data.user.getController().getModelInstance() == null)
                    continue;
                
                // Pull towards the desired position
                PMatrix local = data.user.getController().getModelInstance().getTransform().getLocalMatrix(true);
                Vector3f currentPosition = local.getTranslation();
                float currentDistance = currentPosition.distance(data.desiredPosition); 
                
                if ( data.user.getContext().getCurrentState() instanceof IdleState ) 
                {
                    // Jump to the desired position if in idle and are very close
                    if (currentDistance < positionMinDistanceForPull)
                        local.setTranslation(data.desiredPosition); 
                    else
                    {
                        // If idle pull stronger
                        Vector3f pull = data.desiredPosition.subtract(currentPosition).normalize().mult(currentDistance * 0.25f);
                        local.setTranslation(currentPosition.add(pull)); 
                    }
                }
                else
                {
                    // If not idle pull lightly
                    Vector3f pull = data.desiredPosition.subtract(currentPosition).normalize().mult(currentDistance * deltaTime * 2.0f);
                    // Apply
                    local.setTranslation(currentPosition.add(pull)); 
                }
                
            }
            
            // Let the extension update
            clientTimer += deltaTime;
            if (clientExtension != null)
                clientExtension.update(deltaTime, clientTimer < clientUpdateTick);
            
            ////////////////////////////////////////////////////////
            // Send (should be on this character's update thread) //
            ////////////////////////////////////////////////////////
            
            // Updates messages occure at a fixed interval
            if (clientTimer < clientUpdateTick)
                return;
            clientTimer  = 0.0f;
            
            Vector3f pos = character.getController().getPosition();
            Vector3f dir = character.getController().getForwardVector();
            boolean updatePos       = !pos.equals(prevPos) || !dir.equals(prevDir);
            boolean updateLeftArm   = character.getLeftArm().isEnabled();
            boolean updateRightArm  = character.getRightArm().isEnabled();
            if (updateLeftArm && updateRightArm)
            {
                Vector3f rarm = character.getRightArm().getWristPosition();  
                Vector3f larm = character.getLeftArm().getWristPosition();
                getServerProxy().updatePositionAndArms(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, rarm.x, rarm.y, rarm.z, larm.x, larm.y, larm.z);
                prevPos.set(pos);
                prevDir.set(dir);
            }
            else if (updateRightArm)
            {
                Vector3f rarm = character.getRightArm().getWristPosition();
                getServerProxy().updatePositionAndArm(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, true, rarm.x, rarm.y, rarm.z);
                prevPos.set(pos);
                prevDir.set(dir);   
            }
            else if (updateLeftArm)
            {
                Vector3f larm = character.getLeftArm().getWristPosition();
                getServerProxy().updatePositionAndArm(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, false, larm.x, larm.y, larm.z);
                prevPos.set(pos);
                prevDir.set(dir);
            }
            else if (updatePos)
            {
                getServerProxy().updatePosition(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
                prevPos.set(pos);
                prevDir.set(dir);
            }
            
        }
    }
    
    /**
     * This call is recieved from the server to update another user's character
     */
    @Override
    public void updatePosition(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ) 
    {
        UserData data = characterData.get(userID);
        if (data == null)
            postGUILine("null character update with ID: " + userID);
        else if (data.user.isInitialized() && data.user.getController().getModelInstance() != null)
        {
            Character user = data.user;
            Vector3f pos  = new Vector3f(posX, posY, posZ);
            data.desiredPosition.set(pos);
            Vector3f dir  = new Vector3f(dirX, dirY, dirZ);
            updateUserPosition(user, pos, dir);
            user.getRightArm().setEnabled(false);
            user.getLeftArm().setEnabled(false);
        }
    }

    /**
     * This call is recieved from the server to update another user's character
     */
    @Override
    public void updatePositionAndArm(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, boolean right, float x, float y, float z) 
    {
        UserData data = characterData.get(userID);
        if (data == null)
            postGUILine("null character update with ID: " + userID);
        else if (data.user.isInitialized() && data.user.getController().getModelInstance() != null)
        {
            Character user = data.user;
            Vector3f pos  = new Vector3f(posX, posY, posZ);
            data.desiredPosition.set(pos);
            Vector3f dir  = new Vector3f(dirX, dirY, dirZ);
            updateUserPosition(user, pos, dir);
            Vector3f armPos = new Vector3f(x, y, z);
            if (right)
                updateUserArm(user.getRightArm(), armPos);
            else
                updateUserArm(user.getLeftArm(), armPos);
        }
    }

    /**
     * This call is recieved from the server to update another user's character
     */
    @Override
    public void updatePositionAndArms(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float rx, float ry, float rz, float lx, float ly, float lz) 
    {
        UserData data = characterData.get(userID);
        if (data == null)
            postGUILine("null character update with ID: " + userID);
        else if (data.user.isInitialized() && data.user.getController().getModelInstance() != null)
        {
            Character user = data.user;
            Vector3f pos  = new Vector3f(posX, posY, posZ);
            data.desiredPosition.set(pos);
            Vector3f dir  = new Vector3f(dirX, dirY, dirZ);
            updateUserPosition(user, pos, dir);
            updateUserArm(user.getRightArm(), new Vector3f(rx, ry, rz));
            updateUserArm(user.getLeftArm(),  new Vector3f(lx, ly, lz));
        }   
    }
    
    private void updateUserPosition(Character user, Vector3f pos, Vector3f dir) 
    {
        // If the user is being steered by AI, do not mess it up
        // (objects that the AI is dealing with gota be synced)
        if (user.getContext().getSteering().isEnabled() && user.getContext().getSteering().getCurrentTask() != null)
            return;

        // If the incoming position is too far jump to it
        PMatrix local = user.getController().getModelInstance().getTransform().getLocalMatrix(true);
        Vector3f currentPosition = local.getTranslation();
        float currentDistance = currentPosition.distance(pos); 
        if ( currentDistance < positionMaxDistanceForPull ) 
            pos.set(currentPosition);
        PMatrix look = PMathUtils.lookAt(pos.add(dir), pos, Vector3f.UNIT_Y);
        user.getModelInst().getTransform().getLocalMatrix(true).set(look);
        
//        GameContext context = user.getContext();
//        CharacterSteeringHelm steering = context.getSteering();
//        Vector3f pos = new Vector3f(posX, 0.0f, posZ);
//        Vector3f dir = new Vector3f(dirX, 0.0f, dirZ).normalize();
//        steering.setEnable(true);
//        Task currentTask = steering.getCurrentTask();
//        if (currentTask instanceof GoLook)
//        {
//            GoLook go = (GoLook)currentTask;
//            go.resetTask(pos, dir);
//        }
//        else if (pos.distance(context.getController().getPosition()) < 0.5f) 
//        {
//            PMatrix look = PMathUtils.lookAt(pos.add(dir), pos, Vector3f.UNIT_Y);
//            user.getModelInst().getTransform().getLocalMatrix(true).set(look);
//        }
//        else
//            steering.addTaskToBottom(new GoLook(pos, dir, (avatarContext)context));
    }
    
    private void updateUserArm(VerletArm arm, Vector3f pos) 
    {
        arm.setEnabled(true);
        // Pull towards the desired position
        Vector3f currentPosition = arm.getWristPosition();
        float currentDistance = currentPosition.distance(pos); 
        // Only pull if the desired position is nearby (but not too close) otherwise jump to it instead
        if ( currentDistance < handMaxDistanceForPull ) 
        {
            Vector3f pull = pos.subtract(currentPosition).normalize().mult(currentDistance * handPullStrength);
            pos.set(currentPosition.add(pull)); 
        }
        arm.getParticles().get(VerletArm.wrist).position(pos);
    }

    /**
     * This call is recieved from the server to update another user's character
     */
    @Override
    public void trigger(int userID, boolean pressed, int trigger) 
    {
        UserData data = characterData.get(userID);
        if (data == null)
            postGUILine("null character (trigger) with ID: " + userID);
        else if (data.user.isInitialized())
        {
            Character user = data.user;
            if (pressed)
                user.getContext().triggerPressed(trigger);
            else
                user.getContext().triggerReleased(trigger);
            if (data.extension != null)
                data.extension.trigger(pressed, trigger);
        }
    }
    
    @Override
    public void notifyLogin(int ID, String userName) {
        super.notifyLogin(ID, userName);
        if (users.isEmpty())
            return;
        // Remove all current players if there are any
        Set<Integer> currentUserIDs = users.keySet();
        for (Integer playerID : currentUserIDs)
        {
            UserData data = characterData.get(playerID);
            if (data != null)
            {
                if (data.extension != null)
                    data.extension.removed();
                characterData.get(playerID).user.destroy();
            }
            characterData.remove(playerID);
            users.remove(playerID);
        }   
    }
    
    @Override
    public void listPlayers(int [] playerIDs, String [] playerNames, boolean [] male, int [] feet, int [] legs, int [] torso, int [] hair, int [] head, int [] skinTone, int [] eyeColor) 
    {
        if (!users.isEmpty())
        {
            // Remove all current players if there are any
            Set<Integer> currentUserIDs = users.keySet();
            for (Integer playerID : currentUserIDs)
            {
                UserData data = characterData.get(playerID);
                if (data != null)
                {
                    data.extension.removed();
                    characterData.get(playerID).user.destroy();
                }
                characterData.remove(playerID);
                users.remove(playerID);
            }
        }
        
        // Add the new players
        for(int i = 0; i < playerIDs.length; i++)
        {
            postGUILine("Listing Player with ID: " + playerIDs[i] + " called: " + playerNames[i] + " feet: " + feet[i] + " legs: " + legs[i] + " torso: " + torso[i] + " hair: " + hair[i] + " head: " + head[i] + " skin: " + skinTone[i] + " eyes: " + eyeColor[i] + " male: " + male[i]);
            users.put(playerIDs[i], playerNames[i]);

            Character user;
            if (male[i])
            {
                user = new Avatar(new MaleAvatarAttributes(playerNames[i], feet[i], legs[i], torso[i], hair[i], head[i], skinTone[i], eyeColor[i], false), worldManager);
                //user.setBigHeadMode(2.0f);
            }
            else
                user = new Avatar(new FemaleAvatarAttributes(playerNames[i], feet[i], legs[i], torso[i], hair[i], head[i], skinTone[i], eyeColor[i], false), worldManager);
            
            UserData data = new UserData(user, playerIDs[i]);
            characterData.put(playerIDs[i], data);
            user.getController().addCharacterMotionListener(data);
            if (clientExtension != null)
                clientExtension.userAdded(data);
            // Test
            //vis.addPositionObject(data.desiredPosition, ColorRGBA.black);
            //vis.addPositionObject(data.currentPosition, ColorRGBA.white);
        }
    }

    @Override
    public void addPlayer(int userID, String playerName, boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor) 
    {
        postGUILine("Adding Player with ID: " + userID + " called: " + playerName + " feet: " + feet + " legs: " + legs + " torso: " + torso + " hair: " + hair + " head: " + head + " skin: " + skinTone + " eyes: " + eyeColor + " male: " + male);
        users.put(userID, playerName);

        Character user;
        if (male)
        {
            user = new Avatar(new MaleAvatarAttributes(playerName, feet, legs, torso, hair, head, skinTone, eyeColor, false), worldManager);
            //user.setBigHeadMode(2.0f);
        }
        else
            user = new Avatar(new FemaleAvatarAttributes(playerName, feet, legs, torso, hair, head, skinTone, eyeColor, false), worldManager);
        
        UserData data = new UserData(user, userID);
        characterData.put(userID, data);
        user.getController().addCharacterMotionListener(data);
        if (clientExtension != null)
            clientExtension.userAdded(data);
    }

    @Override
    public void removePlayer(int userID) 
    {
        postGUILine("Removing Player with ID: " + userID + " called: " + users.get(userID));
        
        UserData data = characterData.get(userID);
        if (data != null)
        {
            if (data.extension != null)
                data.extension.removed();
            data.user.destroy();
        }
        else
            System.out.println("Darkstar client: was not able to remove player with ID: " + userID);
        
        characterData.remove(userID);
        users.remove(userID);
    }
    
    protected class ClientContextListener implements GameContextListener
    {
        CharacterClient client = null;

        public ClientContextListener(CharacterClient client)
        {
            this.client = client;
        }

        public void trigger(boolean pressed, int trigger, Vector3f location, Quaternion rotation) 
        {
            if (client.isConnected())
            {
                // Special case for arm activation to let the server
                // throtel arm updates
                if (trigger == TriggerNames.ToggleRightArm.ordinal() && pressed)
                    client.getServerProxy().enableArm(true, character.getRightArm().isEnabled());
                if (trigger == TriggerNames.ToggleLeftArm.ordinal() && pressed)
                    client.getServerProxy().enableArm(false, character.getLeftArm().isEnabled());
                
                client.getServerProxy().trigger(pressed, trigger);
            }
        }

    }

    public class UserData implements CharacterMotionListener
    {
        public int userID                           =-1;
        public Character user                       = null;
        public Vector3f currentPosition             = new Vector3f();
        public Vector3f desiredPosition             = new Vector3f();
    //        public Vector3f desiredDirection            = new Vector3f();
    //        public Vector3f desiredRightHandPosition    = new Vector3f();
    //        public Vector3f desiredLeftHandPosition     = new Vector3f();
        public UserDataExtension extension           = null;
        
        public UserData(Character user, int userID) 
        {
            this.user = user;    
            this.userID = userID;
        }
        
        public void transformUpdate(Vector3f translation, PMatrix rotation) 
        {
            // The difference between the incoming position to the currently
            // known position will be applied on the desiredPosition.
            // This will move the desired position with the local
            // movement as a prediction.
            Vector3f offset = translation.subtract(currentPosition);
            desiredPosition.add(offset);
            currentPosition.set(translation);
        }

        public UserDataExtension getExtension() {
            return extension;
        }

        public void setExtension(UserDataExtension extension) {
            this.extension = extension;
        }
    }

    public String getRandomName()
    {
        String player;
        int r = new Random().nextInt(100);
        switch(r)
        {
            case 0:
                player = "Scrafy";
                break;
            case 1:
                player = "Co co";
                break;
            case 2:
                player = "Berta";
                break;
            case 3:
                player = "Mika";
                break;
            case 4:
                player = "Potter";
                break;
            case 5:
                player = "La Capitan";
                break;
            case 6:
                player = "Munchy";
                break;
            case 7:
                player = "Poo";
                break;
            case 8:
                player = "Fritz";
                break;
            case 9:
                player = "Bongo";
                break;
            case 10:
                player = "Bozo";
                break;
            case 11:
                player = "Goofy";
                break;
            case 12:
                player = "Lilo";
                break;
            case 13:
                player = "Sacha";
                break;
            case 14:
                player = "Lightning";
                break;
            case 15:
                player = "Thunder";
                break;
            case 16:
                player = "Super Person";
                break;
            case 17:
                player = "Dirty Monkey";
                break;
            case 18:
                player = "Holmes";
                break;
            case 19:
                player = "McBowser";
                break;
            case 20:
                player = "Dr. You";
                break;
            case 21:
                player = "GuitarMan15";
                break;
            case 22:
                player = "Drummer";
                break;
            case 23:
                player = "JazzMan";
                break;
            case 24:
                player = "DemoBoy";
                break;
            case 25:
                player = "CodeMaster";
                break;
            case 26:
                player = "N00b";
                break;
            case 27:
                player = "Mr. Pro";
                break;
            case 28:
                player = "Number 28";
                break;
            case 29:
                player = "Bitman";
                break;
            case 30:
                player = "p4wn ur4zz";
                break;
            case 31:
                player = "l33t";
                break;
            case 32:
                player = "RastaDude";
                break;
            case 33:
                player = "WhyMe";
                break;
            case 34:
                player = "WhyHim";
                break;
            case 35:
                player = "WhyThem";
                break;
            case 36:
                player = "Lou";
                break;
            case 37:
                player = "Supreme Commander";
                break;
            case 38:
                player = "Master Chief";
                break;
            case 39:
                player = "Gordon";
                break;
            case 40:
                player = "avatar";
                break;
            case 41:
                player = "Sir Bob";
                break;
            case 42:
                player = "Peach";
                break;
            case 43:
                player = "Dan";
                break;
            case 44:
                player = "Bela";
                break;
            case 45:
                player = "GuyBrush";
                break;
            case 46:
                player = "TomatoFace";
                break;
            case 47:
                player = "Perkings";
                break;
            case 48:
                player = "Ceaser";
                break;
            case 49:
                player = "Fishy the fish";
                break;
            case 50:
                player = "Boogieman";
                break;
            default:
                player = "guest-" + new Random().nextInt(1000);
        }
        return player;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnable(boolean state) {
        throw new UnsupportedOperationException("This method is not used.");
    }
    
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    public Character getCharacter() {
        return character;
    }
    
    public HashMap<Integer, UserData> getUserData() {
        return characterData;
    }
    
    public ClientExtension getExtension() {
        return clientExtension;
    }
}
