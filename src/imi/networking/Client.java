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

import client.ClientSideUser;
import server.ServerSideUser;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import imi.character.avatar.Avatar;
import imi.scene.Updatable;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.java.dev.jnag.sgs.client.JnagSession;
import net.java.dev.jnag.sgs.client.MessageOutputToServerSession;

/**
 *
 * @author Lou Hayt
 */
public class Client implements SimpleClientListener, ClientSideUser, Updatable
{
    /** Connected */
    private boolean connected = false;
    private boolean readyToSendUpdates = false;
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The SimpleClient instance for this client. */
    protected final SimpleClient simpleClient;
    
    /** player identifier */
    protected int ID = -1;
        
    /** user ID to UserData map (for the current game room remote users) */
    protected ConcurrentHashMap<Integer, UserData> users = new ConcurrentHashMap<Integer, UserData>();
    
    /** Map that associates a channel name with a ClientChannel. */
    protected final Map<String, ClientChannel> channelsByName =
        new HashMap<String, ClientChannel>();
    
    /** Sequence generator for counting channels. */
    protected final AtomicInteger channelNumberSequence = new AtomicInteger(1);
    
    /** The JNag session object */
    protected JnagSession    jnagSession = null;;
    
    /** The server side user proxy obect */
    protected ServerSideUser serverProxy = null;;
    
    /** The name of the host property. */
    public static final String HOST_PROPERTY = "imi.host";

    /** The default hostname. */
    public static final String DEFAULT_HOST = "localhost";//"71.41.116.154";//

    /** The name of the port property. */
    public static final String PORT_PROPERTY = "imi.port";

    /** The default port. */
    public static final String DEFAULT_PORT = "1139";
    
    /** None jnag string text message encoding. */
    public static final String MESSAGE_CHARSET = "UTF-8";
    
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    
    /** GUI **/
    protected ClientGUI gui = null;
    
    /** The avatar presets\customizations of this client **/
    ClientAvatar clientAvatar = null;
    
    /** Extensions may implement a JNag interface **/
    protected HashMap<Class, ClientExtension> clientExtension = new HashMap<Class, ClientExtension>();
    
    private String userName = null;
    private String password = null;
    
    /** Updates messages occure at a fixed interval **/
    private float    clientTimer         = 0.0f;
    private float    clientUpdateTick    = 1.0f / 30.0f;

    public Client() 
    {
        simpleClient = new SimpleClient(this);
        // GUI is on by default
        gui = new ClientGUI(this);
    }
    
    public Client(Avatar avatar, boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor) 
    {
        this();
        this.clientAvatar = new ClientAvatar(avatar, male, feet, legs, torso, hair, head, skinTone, eyeColor); 
    }
    
    /**
     * Initiates asynchronous login to the SGS server specified by
     * the host and port properties.
     */
    public void login() 
    {
        String host = System.getProperty(HOST_PROPERTY, DEFAULT_HOST);
        String port = System.getProperty(PORT_PROPERTY, DEFAULT_PORT);

        try {
            Properties connectProps = new Properties();
            connectProps.put("host", host);
            connectProps.put("port", port);
            simpleClient.login(connectProps);
        } catch (Exception e) {
            e.printStackTrace();
            disconnected(false, e.getMessage());
        }
        
        // server JNag
        readyToSendUpdates = false;
        setupJnagSession("Lobby Room");
    }
    
    public void setupJnagSession(String roomName) 
    {
        releaseJnagSession();
        
        jnagSession = new JnagSession();
        jnagSession.setSessionOutput(new MessageOutputToServerSession(simpleClient));
        
        serverProxy = jnagSession.addToRemoteInterface(ServerSideUser.class);
        jnagSession.addToLocalInterface(this);
             
        if (roomName.contains("cahua") || roomName.contains("Cahua"))
        {
            clientExtension.put(CharacterClientExtension.class, new CharacterClientExtension(jnagSession, this));
            CharacterClientExtension ext = (CharacterClientExtension)clientExtension.get(CharacterClientExtension.class);
            clientExtension.put(CahuaClientExtention.class, new CahuaClientExtention(jnagSession, this, (CharacterClientExtension)ext));
        }
        else 
            clientExtension.put(CharacterClientExtension.class, new CharacterClientExtension(jnagSession, this));
    }

    public void releaseJnagSession() 
    {
        if (jnagSession != null)
        {
            jnagSession.removeFromRemoteInterface(serverProxy);
            jnagSession.removeFromLocalInterface(this);
        }
        
        for (ClientExtension ext : clientExtension.values())
            ext.releaseJNagSession();
        clientExtension.clear();
    }
    
    public PasswordAuthentication getPasswordAuthentication() 
    {
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
    
    public void loggedIn() {
        connected = true;
        String string = "Logged in";
        logger.info(string);
        if (gui != null)
        {
            gui.setEnableInput(true);
            gui.setStatus(string);
        }
    }

    public void loginFailed(String reason) {
        String string = "Login failed: " + reason;
        logger.info(string);
        if (gui != null)
            gui.setStatus(string);
    }

    public ClientChannelListener joinedChannel(ClientChannel channel) 
    {   
        String channelName = channel.getName();
        channelsByName.put(channelName, channel);
        logger.info("Joined channel: " + channelName);
        
        if (gui != null)
            gui.joinedChannel(channel);
        
        return new ChannelListener();
    }
    
    public void statusLeftChannel(ClientChannel channel) {
        String name = channel.getName();
        String string = "Removed from channel " + name;
        logger.info(string);
        channelsByName.remove(name);
        if (gui != null)
            gui.leftChannel(name);
    }
    
    /**
     * Direct client to server message
     * @param message
     */
    public void receivedMessage(ByteBuffer message) 
    {
        // recieving jnag messages on the direct line
        jnagSession.decodeAndExecute(message);
    }
    
    public void receivedChannelMessage(ClientChannel channel, ByteBuffer message)
    {
        String string = channel.getName() + "| " + decodeStringWithUserID(message);
        logger.info(string);
        if (gui != null)
            gui.appendOutput(string);
    }
    
    public void reconnecting() {
        String string = "Reconnecting";
        logger.info(string);
        if (gui != null)
            gui.setStatus(string);
    }

    public void reconnected() {
        String string = "Reconnected";
        logger.info(string);
        if (gui != null)
            gui.setStatus(string);
    }

    public void disconnected(boolean graceful, String reason) {
        connected = false;
        readyToSendUpdates = false;
        String string = "Disconnected: " + reason;
        logger.info(string);
        if (gui != null)
        {
            gui.setEnableInput(false);
            gui.setStatus(string);
        }
    }
    
    // jnag client side user implemintation

    public void notifyMessageOfTheDay(String messageOfTheDay) {
        String string = "Message of the day: " + messageOfTheDay;
        logger.info(string);
        if (gui != null)
            gui.appendOutput(string);
    }

    public void whisper(String message) {
        
        if (!consoleCommand(message))
        {
            String string = "Whisper: " + message;
            logger.info(string);
            if (gui != null)
                gui.appendOutput(string);
        }
    }

    public boolean consoleCommand(String message) 
    {    
        if (message.startsWith("list"))
        {
            serverProxy.requestWorldList();
            return true;
        }
        else if (message.startsWith("join"))
        {
            if (message.substring(4).startsWith(" "))
                message = message.substring(5);
            else
                message = message.substring(4);
            
            readyToSendUpdates = false;
            setupJnagSession(message);
            serverProxy.enterWorld(message);
            return true;
        }
        return false;
    }
    
    
    public void recieveWorldList(String [] worldNames)
    {
        String string = "Recieving the list of worlds: ";
        gui.appendOutput(string);
        logger.info(string);
        if (gui != null)
        {
            for (String name : worldNames)
                gui.appendOutput(name);
            gui.appendOutput("-=-");
        }
    }
    
    public void listPlayers(int [] playerIDs, String [] playerNames, boolean [] male, int [] feet, int [] legs, int [] torso, int [] hair, int [] head, int [] skinTone, int [] eyeColor) 
    {
        // Remove all current players if there are any
        if (!users.isEmpty())
        {
            Set<Integer> currentUserIDs = users.keySet();
            for (Integer playerID : currentUserIDs)
            {
                UserData data = getUserData(playerID);
                data.clean();
                users.remove(playerID);
            }
        }
        
        // Add the new players
        for(int i = 0; i < playerIDs.length; i++)
        {
            postGUILine("Listing Player with ID: " + playerIDs[i] + " called: " + playerNames[i] + " feet: " + feet[i] + " legs: " + legs[i] + " torso: " + torso[i] + " hair: " + hair[i] + " head: " + head[i] + " skin: " + skinTone[i] + " eyes: " + eyeColor[i] + " male: " + male[i]);
            users.put(playerIDs[i], new UserData(playerIDs[i], playerNames[i]));
            UserData data = users.get(playerIDs[i]);
            for (ClientExtension clientExt : clientExtension.values())
            {
                UserDataExtension ext = clientExt.getNewUserDataExtension();
                data.extension.put(ext.getClass(), ext);
                ext.added(data, new ClientAvatar(null, male[i], feet[i], legs[i], torso[i], hair[i], head[i], skinTone[i], eyeColor[i]));
            }
        }
    }

    public void addPlayer(int userID, String playerName, boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor) 
    {
        postGUILine("Adding Player with ID: " + userID + " called: " + playerName + " feet: " + feet + " legs: " + legs + " torso: " + torso + " hair: " + hair + " head: " + head + " skin: " + skinTone + " eyes: " + eyeColor + " male: " + male);
        users.put(userID, new UserData(userID, playerName));
        UserData data = users.get(userID);
        for (ClientExtension clientExt : clientExtension.values())
        {
            UserDataExtension ext = clientExt.getNewUserDataExtension();
            data.extension.put(ext.getClass(), ext);
            ext.added(data, new ClientAvatar(null, male, feet, legs, torso, hair, head, skinTone, eyeColor));
        }
    }

    public void removePlayer(int userID) 
    {
        if (users.get(userID) == null)
        {
            postGUILine("Was not able to remove player " + userID);
            return;
        }
        postGUILine("Removing Player with ID: " + userID + " called: " + users.get(userID).name);
        UserData data = getUserData(userID);
        data.clean();
        users.remove(userID);
    }
       
    public void notifyLogin(int ID, String userName, String roomName) 
    {
        String string = "Joining with ID: " + ID + " and user name: " + userName + " to the room: " + roomName;
        logger.info(string);
        this.ID = ID;
        users.put(ID, new UserData(ID, userName));
        if (gui != null)
            gui.appendOutput(string);
        
        readyToSendUpdates = true;
        for (ClientExtension ext : clientExtension.values())
            ext.notifyLogin(roomName);
    }
    
    public void notifyLogout(boolean graceful) {
        String string = "Notified logout, graceful: " + graceful;
        logger.info(string);
        if (gui != null)
            gui.appendOutput(string);
    }
    
    /**
     * A simple listener for channel events.
     */
    public class ChannelListener implements ClientChannelListener
    {
        /**
         * An example of per-channel state, recording the number of
         * channel joins when the client joined this channel.
         */
        private final int channelNumber;

        /**
         * Creates a new {@code HelloChannelListener}. Note that
         * the listener will be given the channel on its callback
         * methods, so it does not need to record the channel as
         * state during the join.
         */
        public ChannelListener() {
            channelNumber = channelNumberSequence.getAndIncrement();
        }

        public void leftChannel(ClientChannel channel) {
            statusLeftChannel(channel);
        }

        public void receivedMessage(ClientChannel channel, ByteBuffer message) {
            receivedChannelMessage(channel, message);
        }
    }
    
    // non jnag string utility
    
    /**
     * Encodes a {@code String} into a {@link ByteBuffer}.
     *
     * @param s the string to encode
     * @return the {@code ByteBuffer} which encodes the given string
     */
    protected static ByteBuffer encodeString(String s) {
        try {
            return ByteBuffer.wrap( s.getBytes(MESSAGE_CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character set " + MESSAGE_CHARSET +
                " not found", e);
        }
    }
    
    /**
     * Encodes a {@code String} into a {@link ByteBuffer} and adds
     * an ID integer prefix encoded into a byte.
     * @param ID 
     * @param s the string to encode
     * @return the {@code ByteBuffer} which encodes the given string
     */
    protected static ByteBuffer encodeString(Integer ID, String s) {
        try {
            byte [] string = s.getBytes(MESSAGE_CHARSET);
            byte [] message = new byte[string.length+1];
            message[0] = ID.byteValue();
            System.arraycopy(string, 0, message, 1, string.length);
            return ByteBuffer.wrap(message);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character set " + MESSAGE_CHARSET +
                " not found", e);
        }
    }

    /**
     * Decodes a {@link ByteBuffer} into a {@code String}.
     *
     * @param buf the {@code ByteBuffer} to decode
     * @return the decoded string
     */
    protected static String decodeString(ByteBuffer buf) {
        try {
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            return new String(bytes, MESSAGE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character set " + MESSAGE_CHARSET +
                " not found", e);
        }
    }
    
    /**
     * Decodes a {@link ByteBuffer} into a {@code String}.
     *
     * @param buf the {@code ByteBuffer} to decode
     * @return the decoded string
     */
    protected String decodeStringWithUserID(ByteBuffer buf) {
        try {
            int IDByte = buf.get(0);
            String senderName = users.get(IDByte).name;
            if (senderName == null)
                senderName = "null index " + IDByte;
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            return senderName + ": " + new String(bytes, MESSAGE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character set " + MESSAGE_CHARSET +
                " not found", e);
        }
    }
    
    public ClientChannel getChannel(String name)
    {
        return channelsByName.get(name);
    }
    
    public boolean isConnected()
    {
        return connected;
    }
    
    public int getID()
    {
        return ID;
    }
    
    public void postGUILine(String line)
    {
        if (gui != null)
            gui.appendOutput(line);
    }
    
    public void setGUIStatus(String status)
    {
        if (gui != null)
            gui.setStatus(status);
    }
    
    public String getUserName(int userID) {
        if (users.get(userID) != null)
            return users.get(userID).name;
        return "ERROR: can't find user ID " + userID;
    }
    
    public UserData getUserData(int userID) {
        return users.get(userID);
    }
    
    public HashMap<Class, UserDataExtension> getUserDataExtention(int userID) {
        return users.get(userID).extension;
    }
    
    public ConcurrentHashMap<Integer, UserData> getUsers() {
        return users;
    }
    
    /** GUI may be null **/
    public ClientGUI getGUI()
    {
        return gui;
    }
    
    public ServerSideUser getServerProxy() {
        return serverProxy;
    }
    
    public void update(float deltaTime) 
    {
        if (connected)
        {
            // Let the extensions update
            clientTimer += deltaTime;
            for(ClientExtension ext : clientExtension.values())
                ext.update(deltaTime, clientTimer < clientUpdateTick && readyToSendUpdates);
            if (clientTimer >= clientUpdateTick)
                clientTimer  = 0.0f;
        }
    }
    
    public ClientExtension getExtension(Class extensionClass) {
        return clientExtension.get(extensionClass);
    }
    
    public class UserData
    {
        public int userID  = -1;
        public String name = "nameless";
        public HashMap<Class, UserDataExtension> extension = new HashMap<Class, UserDataExtension>();
        
        public UserData(int userID, String name) 
        {
            this.userID = userID;
            this.name   = name;
        }

        public UserDataExtension getExtension(Class extensionClass) {
            return extension.get(extensionClass);
        }
        
        public void clean() 
        {
            for (UserDataExtension ext : extension.values())
                ext.clean();
        }
    }

    public class ClientAvatar
    {
        public Avatar avatar;
        public boolean male;
        public int feet, legs, torso, hair, head, skinTone, eyeColor;
        public ClientAvatar(Avatar avatar, boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor) {
            this.avatar = avatar;
            this.male = male;
            this.feet = feet;
            this.legs = legs;
            this.torso = torso;
            this.hair = hair;
            this.head = head;
            this.skinTone = skinTone;
            this.eyeColor = eyeColor;
        }
    }

    public ClientAvatar getClientAvatar() {
        return clientAvatar;
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
        throw new UnsupportedOperationException("Method not used.");
    }
}
