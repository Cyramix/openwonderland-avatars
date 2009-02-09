/*
 * Copyright 2007-2008 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package server;

import client.ClientSideUser;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import net.java.dev.jnag.sgs.app.JnagSession;
import net.java.dev.jnag.sgs.app.MessageOutputToClientSession;
import net.java.dev.jnag.sgs.common.MethodLogger;

/**
 * Represents a user 
 */
public class WorldPlayer extends WorldObject implements ClientSessionListener, ServerSideUser
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /** The {@link Logger} for this class. */
    private static final Logger logger =
        Logger.getLogger(WorldPlayer.class.getName());

    /** The message encoding. */
    public static final String MESSAGE_CHARSET = "UTF-8";

    /** The prefix for player bindings in the {@code DataManager}. */
    protected static final String PLAYER_BIND_PREFIX = "Player.";

    /** The {@code ClientSession} for this player, or null if logged out. */
    private ManagedReference<ClientSession> currentSessionRef = null;

    /** The {@link WorldRoom} this player is in, or null if none. */
    private ManagedReference<WorldRoom> currentRoomRef = null;
    
    // jnag integration
    private JnagSession                      jnagSession       = null;
    private ManagedReference<ClientSideUser> clientSideUserRef = null;

    /** The extension may implement a JNag interface **/
    protected ManagedReference<WorldPlayerExtension> serverExtensionRef = null;
    
    // data to keep for each player
    ManagedReference<PlayerData> playerDataRef = null;
    
    /**
     * Find or create the player object for the given session, and mark
     * the player as logged in on that session.
     *
     * @param session which session to find or create a player for
     * @return a player for the given session
     */
    public static WorldPlayer loggedIn(ClientSession session, int userID) 
    {   
        String playerBinding = PLAYER_BIND_PREFIX + session.getName();

        // try to find player object, if non existent then create
        DataManager dataMgr = AppContext.getDataManager();
        WorldPlayer player;

        try {
            player = (WorldPlayer) dataMgr.getBinding(playerBinding);
        } catch (NameNotBoundException ex) {
            // this is a new player
            player = new WorldPlayer(playerBinding, session, userID);
            logger.log(Level.INFO, "New player created: {0}", player);
            dataMgr.setBinding(playerBinding, player);
        }
        player.setSession(session);
        
        return player;
    }

    /**
     * Creates a new {@code WorldPlayer} with the given name.
     *
     * @param name the name of this player
     */
    protected WorldPlayer(String name, ClientSession session, int userID) 
    {
        super(name, "a friendly chap");
        
        // Create the player data object
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        playerDataRef = dataManager.createReference(new PlayerData(userID, getName()));
    }
    
    protected void setupJNagSession(ClientSession session, Channel channel)
    {   
        releaseJNagSession();
        // Create the jnag session and initialize its output.
        jnagSession = new JnagSession();
        jnagSession.setMethodLogger(new MethodLogger()); // Enable the log.
        //jnagSession.setSessionOutput(new MessageOutputToChannel(session, channel));
        //jnagSession.setSessionOutput(new MessageOutputToChannel(channel));
        jnagSession.setSessionOutput(new MessageOutputToClientSession(session));

        // Declare that we will receive the calls of the client.
        jnagSession.addToLocalInterface(this);

        // Create a proxy to encode the method calls to the object on the client.
        ClientSideUser clientSideUserProxy = jnagSession.addToRemoteInterface(ClientSideUser.class);
        clientSideUserRef = AppContext.getDataManager().createReference(clientSideUserProxy);  
        
        // jnag proxy methods
        clientSideUserProxy.notifyLogin(playerDataRef.get().getID(), getName());
        if (getRoom() != null)
            clientSideUserProxy.notifyMessageOfTheDay(getRoom().getDescription());
    }
    
    /**
     * Returns the session for this listener.
     * 
     * @return the session for this listener
     */
    protected ClientSession getSession() {
        if (currentSessionRef == null)
            return null;

        return currentSessionRef.get();
    }

    /**
     * Mark this player as logged in on the given session.
     *
     * @param session the session this player is logged in on
     */
    protected void setSession(ClientSession session) 
    {
        if (session == null)
        {
            currentSessionRef = null;
            return;
        }
        
        DataManager dataMgr = AppContext.getDataManager();
        dataMgr.markForUpdate(this);

        currentSessionRef = dataMgr.createReference(session);

        logger.log(Level.INFO,
            "Set session for {0} to {1}",
            new Object[] { this, session });
    }

    /**
     * Handles a player entering a room.
     *
     * @param room the room for this player to enter
     */
    public void enter(WorldRoom room) 
    {
        logger.log(Level.INFO, "{0} enters {1}",
            new Object[] { this, room } );
        setRoom(room);
        // Deal with a possible extension
        DataManager dataMngr = AppContext.getDataManager();
        dataMngr.markForUpdate(this);
        if (serverExtensionRef != null)
        {
            WorldPlayerExtension ext = serverExtensionRef.get();
            ext.releaseJNagSession();
            dataMngr.removeObject(ext);
            serverExtensionRef = null;
        }
        if (room.getName().contains("cahua") || room.getName().contains("Cahua"))
        {
            WorldPlayerExtension ext = new CahuaPlayerExtention(jnagSession, this);
            serverExtensionRef = dataMngr.createReference(ext);
        }
    }
    
    public WorldPlayerExtension getExtension() {
        if (serverExtensionRef == null)
            return null;
        return serverExtensionRef.get();
    }
    
    /** {@inheritDoc} */
    public void receivedMessage(ByteBuffer message) 
    {
        // JNag
        // Decode the messages and call their targeted method on their targeted object.
        jnagSession.decodeAndExecute(message);
        
//        // None jnag proccessing
//        String command = decodeString(message);
//
//        logger.log(Level.INFO,
//            "{0} received command: {1}",
//            new Object[] { this, command } );
//
//        if (command.equalsIgnoreCase("look")) {
//            String reply = getRoom().look(this);
//            getSession().send(encodeString(reply));
//        } else {
//            logger.log(Level.WARNING,
//                "{0} unknown command: {1}",
//                new Object[] { this, command }
//            );
//            // We could disconnect the rogue player at this point.
//            //currentSession.disconnect();
//        }
    }

    public void disconnected(boolean graceful) 
    {    
        clientSideUserRef.get().notifyLogout(graceful);
        releaseJNagSession();
        
        // None jnag preccessing
        getRoom().removePlayer(this, true);
        setSession(null);
        setRoom(null);
        logger.log(Level.INFO, "Disconnected: {0}", this);
    }
    
    public void releaseJNagSession()
    {
        if (jnagSession == null)
            return;
        
        // Delete the serverSideUserImpl object from the data store.
        jnagSession.removeFromLocalInterface(this);

        // Delete the clientSideUserProxy object from the data store.
        ClientSideUser clientSideUserProxy = clientSideUserRef.get();
        jnagSession.removeFromRemoteInterface(clientSideUserProxy);
        AppContext.getDataManager().removeObject(clientSideUserProxy);

        if (serverExtensionRef != null)
            serverExtensionRef.get().releaseJNagSession();
        
        // Delete the managed objects owned by the implementation of the jnag session.
        jnagSession.releaseManagedObjects();
    }

    public void enterWorld(String worldName)
    {
        WorldRoom room = getRoom();
        if (room != null)
        {
            if (room.getWorld().getRoom(worldName).getName().equals(room.getName()))
                return;
            getRoom().removePlayer(this, false);
            enter(room.getWorld().getRoom(worldName));
            getRoom().sendAddPlayer(this);
        }
    }
    
    public void requestWorldList()
    {
        WorldRoom room = getRoom();
        if (room != null)   
            clientSideUserRef.get().recieveWorldList(room.getWorld().getWorldNames());
    }
    
    /**
     * Returns the room this player is currently in, or {@code null} if
     * this player is not in a room.
     * <p>
     * @return the room this player is currently in, or {@code null}
     */
    protected WorldRoom getRoom() {
        if (currentRoomRef == null)
            return null;

        return currentRoomRef.get();
    }
    
    protected ClientSideUser getClientSideUser()
    {
        if (clientSideUserRef == null)
            return null;

        return clientSideUserRef.get();
    }

    /**
     * Sets the room this player is currently in.  If the room given
     * is null, marks the player as not in any room.
     * <p>
     * @param room the room this player should be in, or {@code null}
     */
    protected void setRoom(WorldRoom room) 
    {
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        if (room == null) 
        {
            currentRoomRef = null;
            return;
        }
        currentRoomRef = dataManager.createReference(room);
        room.addPlayer(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(getName());
        buf.append('@');
        if (getSession() == null) {
            buf.append("null");
        } else {
            buf.append(currentSessionRef.getId());
        }
        return buf.toString();
    }

    /**
     * Encodes a {@code String} into a {@link ByteBuffer}.
     *
     * @param s the string to encode
     * @return the {@code ByteBuffer} which encodes the given string
     */
    protected static ByteBuffer encodeString(String s) {
        try {
            return ByteBuffer.wrap(s.getBytes(MESSAGE_CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character set " + MESSAGE_CHARSET +
                " not found", e);
        }
    }

    /**
     * Decodes a message into a {@code String}.
     *
     * @param message the message to decode
     * @return the decoded string
     */
    protected static String decodeString(ByteBuffer message) {
        try {
            byte[] bytes = new byte[message.remaining()];
            message.get(bytes);
            return new String(bytes, MESSAGE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character set " + MESSAGE_CHARSET +
                " not found", e);
        }
    }

    public PlayerData getPlayerData() {
            return playerDataRef.get();
        }
    
    // Jnag integration
    
    public void serverCommand(String string) 
    {
        // Do nothing, whisper back
        clientSideUserRef.get().whisper(string);
        
        
        //
    }

    public void updatePosition(float posX, float posY, float posZ, float dirX, float dirY, float dirZ) 
    {
        playerDataRef.getForUpdate().updatePosition(posX, posY, posZ, dirX, dirY, dirZ);
    }

    public void updatePositionAndArm(float posX, float posY, float posZ, float dirX, float dirY, float dirZ, boolean right, float x, float y, float z) {
        PlayerData data = playerDataRef.getForUpdate();
        data.updatePosition(posX, posY, posZ, dirX, dirY, dirZ);
        data.updateArm(right, x, y, z);
    }

    public void updatePositionAndArms(float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float rx, float ry, float rz, float lx, float ly, float lz) {
        PlayerData data = playerDataRef.getForUpdate();
        data.updatePosition(posX, posY, posZ, dirX, dirY, dirZ);
        data.updateArm(true, rx, ry, rz);
        data.updateArm(false, lx, ly, lz);
    }
    /**
     * Required to throtel arm update messages
     * @param right  - true for the right arm, false for the left one
     * @param enable - true to enable, false to disable
     */
    public void enableArm(boolean right, boolean enable) {
        playerDataRef.getForUpdate().enableArm(right, enable);
    }
    
//    // should be a trigger
//    public void setHandGesture(boolean right, int gestureID) {
//        getRoom().sendHandGesture(this, right, gestureID);
//    }

    public void trigger(boolean pressed, int trigger) {
        getRoom().sendTrigger(this, pressed, trigger);
    }

    public void setAvatarInfo(boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor) {
        playerDataRef.getForUpdate().setAvatarInfo(male, feet, legs, torso, hair, head, skinTone, eyeColor);
        getRoom().sendAddPlayer(this);
    }
}
