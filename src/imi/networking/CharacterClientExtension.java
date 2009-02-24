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
package imi.networking;

import client.ClientSideCharacterUser;
import imi.character.*;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.Character;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.networking.Client.ClientAvatar;
import imi.networking.Client.UserData;
import imi.character.statemachine.GameContextListener;
import imi.character.statemachine.corestates.IdleState;
import imi.scene.PMatrix;
import imi.utils.PMathUtils;
import java.util.Collection;
import java.util.HashSet;
import net.java.dev.jnag.sgs.client.JnagSession;
import org.jdesktop.mtgame.WorldManager;
import server.ServerSideCharacterUser;

/**
 * This is an extention to the Client that adds characters
 * @author Lou Hayt
 */
public class CharacterClientExtension extends ClientExtension implements ClientSideCharacterUser, GameContextListener
{
    Client masterClient = null;
    private JnagSession         jnagSession  = null;
    private ServerSideCharacterUser serverProxy  = null;
    private ClientGUI   gui                       = null;
    
    /** The avatar representation **/
    protected Character    character = null;
    /** Used for creating avatar attribute objects **/
    protected WorldManager worldManager = null;
        
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
   
    public CharacterClientExtension(JnagSession jnagSession, Client masterClient) 
    {
        super();
        this.masterClient = masterClient;
        this.jnagSession  = jnagSession;
        gui               = masterClient.getGUI();
        this.character    = masterClient.getClientAvatar().avatar;
        worldManager      = character.getWorldManager(); 
        
        serverProxy = jnagSession.addToRemoteInterface(ServerSideCharacterUser.class);
        jnagSession.addToLocalInterface(this);
        
        // Initialize character user data extensions for remote users (needed?)
        Collection<UserData> dataCollection = masterClient.getUsers().values();
        for (UserData data : dataCollection)
            data.extension.put(CharacterDataExtension.class ,new CharacterDataExtension());
        
        character.getContext().addGameContextListener(this);
    }
    
    @Override
    void notifyLogin(String roomName)  
    {   
        ClientAvatar clientAvatar = masterClient.getClientAvatar();
        serverProxy.setAvatarInfo(clientAvatar.male, clientAvatar.feet, clientAvatar.legs, clientAvatar.torso, clientAvatar.hair, clientAvatar.head, clientAvatar.skinTone, clientAvatar.eyeColor);
    }
    
    @Override
    void releaseJNagSession() 
    {
        jnagSession.removeFromRemoteInterface(serverProxy);
        jnagSession.removeFromLocalInterface(this);
        
        character.getContext().removeGameContextListener(this);
                
        Collection<UserData> dataCollection = masterClient.getUsers().values();
        for (UserData data : dataCollection)
        {
            CharacterDataExtension dataExt = ((CharacterDataExtension)data.getExtension(CharacterDataExtension.class));
            if (dataExt != null)
            {
                dataExt.clean();
                data.extension.remove(CharacterDataExtension.class);
            }
        }
    }
    
    public void performAnimation(int actionIndex, boolean client, boolean allUsers, int specificUser)
    {
        if (client)
            ((AvatarContext)character.getContext()).performAction(actionIndex);
        
        if (allUsers)
        {
            Collection<UserData> dataCollection = masterClient.getUsers().values();
            for (UserData data : dataCollection)
                if (getUserData(data).character != null)
                ((AvatarContext)getUserData(data).character.getContext()).performAction(actionIndex);
        }
        else if (masterClient.getUserData(specificUser) != null)
        {
            Character user = getUserData(specificUser).character;
            ((AvatarContext)user.getContext()).performAction(actionIndex);
        }
    }
       
    /**
     * This update is called on the character's update and its job is to
     * send periodic updates to the server with the character's position
     * and possibly the arms positions (if the arms are enabled).
     * @param deltaTime
     */
    @Override
    public void update(float deltaTime, boolean updateTick)
    {
        ////////////////////////////////////////////////////
        // Manage peers (can be on another update thread) //
        ////////////////////////////////////////////////////

        Collection<UserData> dataCollection = masterClient.getUsers().values();
        for (UserData UData : dataCollection)
        {
            CharacterDataExtension data = (CharacterDataExtension) UData.getExtension(CharacterDataExtension.class);
            if (data == null || data.character == null || !data.character.isInitialized() || data.character.getController().getModelInstance() == null)
                continue;

            // Pull towards the desired position
            PMatrix local = data.character.getController().getModelInstance().getTransform().getLocalMatrix(true);
            Vector3f currentPosition = local.getTranslation();
            float currentDistance = currentPosition.distance(data.desiredPosition); 

            if ( data.character.getContext().getCurrentState() instanceof IdleState ) 
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

        ////////////////////////////////////////////////////////
        // Send (should be on this character's update thread) //
        ////////////////////////////////////////////////////////
        if (!updateTick)
            return;
        
        Vector3f pos = character.getController().getPosition();
        Vector3f dir = character.getController().getForwardVector();
        boolean updatePos       = !pos.equals(prevPos) || !dir.equals(prevDir);
        boolean updateLeftArm   = character.getLeftArm().isEnabled();
        boolean updateRightArm  = character.getRightArm().isEnabled();
        if (updateLeftArm && updateRightArm)
        {
            Vector3f rarm = character.getRightArm().getWristPosition();  
            Vector3f larm = character.getLeftArm().getWristPosition();
            serverProxy.updatePositionAndArms(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, rarm.x, rarm.y, rarm.z, larm.x, larm.y, larm.z);
            prevPos.set(pos);
            prevDir.set(dir);
        }
        else if (updateRightArm)
        {
            Vector3f rarm = character.getRightArm().getWristPosition();
            serverProxy.updatePositionAndArm(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, true, rarm.x, rarm.y, rarm.z);
            prevPos.set(pos);
            prevDir.set(dir);   
        }
        else if (updateLeftArm)
        {
            Vector3f larm = character.getLeftArm().getWristPosition();
            serverProxy.updatePositionAndArm(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, false, larm.x, larm.y, larm.z);
            prevPos.set(pos);
            prevDir.set(dir);
        }
        else if (updatePos)
        {
            serverProxy.updatePosition(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
            prevPos.set(pos);
            prevDir.set(dir);
        }
    }
    
    /**
     * This call is recieved from the server to update another user's character
     */
    @Override
    public void updatePosition(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ) 
    {
        CharacterDataExtension data = getUserData(userID);
        if (data == null)
            System.out.println("null character update with ID: " + userID);
        else if (data.character == null)
            System.out.println("null character during update with ID: " + userID);
        else if (data.character.isInitialized() && data.character.getController().getModelInstance() != null)
        {
            Character user = data.character;
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
        CharacterDataExtension data = getUserData(userID);
        if (data == null)
            gui.appendOutput("null character update with ID: " + userID);
        else if (data.character.isInitialized() && data.character.getController().getModelInstance() != null)
        {
            Character user = data.character;
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
        CharacterDataExtension data = getUserData(userID);
        if (data == null)
            gui.appendOutput("null character update with ID: " + userID);
        else if (data.character.isInitialized() && data.character.getController().getModelInstance() != null)
        {
            Character user = data.character;
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
        CharacterDataExtension data = getUserData(userID);
        if (data == null)
            gui.appendOutput("null character (trigger) with ID: " + userID);
        else if (data.character.isInitialized())
        {
            Character user = data.character;
            if (pressed)
                user.getContext().triggerPressed(trigger);
            else
                user.getContext().triggerReleased(trigger);
            
            data.trigger(pressed, trigger);
        }
    }
        
    public void trigger(boolean pressed, int trigger, Vector3f location, Quaternion rotation) 
    {
        if (masterClient.isConnected())
        {
            // Special case for arm activation to let the server
            // throtel arm updates
            if (trigger == TriggerNames.ToggleRightArm.ordinal() && pressed)
                serverProxy.enableArm(true, character.getRightArm().isEnabled());
            if (trigger == TriggerNames.ToggleLeftArm.ordinal() && pressed)
                serverProxy.enableArm(false, character.getLeftArm().isEnabled());

            serverProxy.trigger(pressed, trigger);
        }
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
    
    public ServerSideCharacterUser getServerProxy() {
        return serverProxy;
    }
    
    @Override
    UserDataExtension getNewUserDataExtension() {
        return new CharacterDataExtension();
    }
    
    public CharacterDataExtension getUserData(int userID) {
        if (masterClient.getUserData(userID) == null)
        {
            System.out.println("was not able to getUserData(" + userID + ")");
            return null;
        }
        return (CharacterDataExtension)masterClient.getUserData(userID).getExtension(CharacterDataExtension.class);
    }
        
    public CharacterDataExtension getUserData(UserData data) {
        return (CharacterDataExtension)data.getExtension(CharacterDataExtension.class);
    }
    
    public class CharacterDataExtension implements UserDataExtension, CharacterMotionListener
    {
        public UserData data                        = null;
        public Character character                  = null;
        public Vector3f currentPosition             = new Vector3f();
        public Vector3f desiredPosition             = new Vector3f();
//        public Vector3f desiredDirection            = new Vector3f();
//        public Vector3f desiredRightHandPosition    = new Vector3f();
//        public Vector3f desiredLeftHandPosition     = new Vector3f();
        
        /** List of listeners **/
        private HashSet<CharacterDataExtensionListener> listeners = null;
        
        public CharacterDataExtension()
        {
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
        
        /**
         * Add a CharacterDataExtensionListener to this character's data.
         * @param listener to be added
         */
        public void addCharacterDataExtensionListener(CharacterDataExtensionListener listener) {
            if (listeners == null)
                listeners = new HashSet();
            synchronized(listeners) {
                listeners.add(listener);
            }
        }

        /**
         * Remove the CharacterDataExtensionListener from the set of listeners for this
         * character's data. If the listener was not registered previously this method
         * simply returns.
         * @param listener to be removed
         */
        public void removeCharacterDataExtensionListener(CharacterDataExtensionListener listener) {
            if (listener!=null) {
                synchronized(listeners) {
                    listeners.remove(listener);
                }
            }
        }
        
        public void added(UserData data, ClientAvatar clientAvatar)
        {
            this.data = data;
            if (clientAvatar.male)
                character = new Avatar(new MaleAvatarAttributes(data.name, clientAvatar.feet, clientAvatar.legs, clientAvatar.torso, clientAvatar.hair, clientAvatar.head, clientAvatar.skinTone, clientAvatar.eyeColor, false), worldManager);
            else
                character = new Avatar(new FemaleAvatarAttributes(data.name, clientAvatar.feet, clientAvatar.legs, clientAvatar.torso, clientAvatar.hair, clientAvatar.head, clientAvatar.skinTone, clientAvatar.eyeColor, false), worldManager);
            
            character.getController().addCharacterMotionListener(this);
        }
        
        public void clean()
        {
            if (character != null)
                character.destroy();
        }
        
        public void trigger(boolean pressed, int trigger) 
        {
            if (listeners==null)
                return;

            synchronized(listeners) {
                for(CharacterDataExtensionListener l : listeners)
                    l.trigger(pressed, trigger);
            }
        }
    }
}
