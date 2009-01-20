/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.character.networking;

import client.ClientSideUser;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.java.dev.jnag.sgs.client.JnagSession;
import net.java.dev.jnag.sgs.client.MessageOutputToServerSession;
import net.java.dev.jnag.sgs.common.MethodLogger;
import server.ServerSideUser;

/**
 *
 * @author Lou Hayt
 */
public class JNagClient implements SimpleClientListener, ClientSideUser
{
    /** Connected */
    protected boolean connected = false;
    
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link SimpleClient} instance for this client. */
    protected final SimpleClient simpleClient;
    
    /** our room player identifier */
    protected int ID = -1;
    
    /** user name ID to name map (for the current room) */
    protected HashMap<Integer, String> users = new HashMap<Integer, String>();
    
    /** Map that associates a channel name with a {@link ClientChannel}. */
    protected final Map<String, ClientChannel> channelsByName =
        new HashMap<String, ClientChannel>();
    
    /** Sequence generator for counting channels. */
    protected final AtomicInteger channelNumberSequence = new AtomicInteger(1);
    
    /** The JNag session object */
    protected JnagSession    jnagSession = null;;
    
    /** The server side user proxy obect */
    protected ServerSideUser serverProxy = null;;
    
    /** The name of the host property. */
    public static final String HOST_PROPERTY = "tutorial.host";

    /** The default hostname. */
    public static final String DEFAULT_HOST = "71.41.116.154";//"localhost";//

    /** The name of the port property. */
    public static final String PORT_PROPERTY = "tutorial.port";

    /** The default port. */
    public static final String DEFAULT_PORT = "1139";
    
    /** None jnag string text message encoding. */
    public static final String MESSAGE_CHARSET = "UTF-8";
    
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(JNagClient.class.getName());
    
    /** GUI **/
    protected JNagClientGUI2 gui = null;
    
    public JNagClient() 
    {
        simpleClient = new SimpleClient(this);
        // GUI is on by default
        gui = new JNagClientGUI2(this);
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
        setupJnagServerSession();
    }
    
    public void setupJnagServerSession() 
    {
        jnagSession = new JnagSession();
        jnagSession.setMethodLogger(new MethodLogger()); // Enable the log.
        jnagSession.setSessionOutput(new MessageOutputToServerSession(simpleClient));
        serverProxy = jnagSession.addToRemoteInterface(ServerSideUser.class);
        jnagSession.addToLocalInterface(this);
    }

    public void releaseJnagSession() {
        if (jnagSession != null)
            jnagSession.removeFromRemoteInterface(serverProxy);
    }
    
    public ServerSideUser getServerProxy()
    {
        return serverProxy;
    }
    
    /**
     * <p>
     * Returns dummy credentials where user is "guest-&lt;random&gt;"
     * and the password is "guest."  Real-world clients are likely
     * to pop up a login dialog to get these fields from the player.
     */
    public PasswordAuthentication getPasswordAuthentication() {
        String player = "guest-" + new Random().nextInt(1000);
        logger.info("Logging in as: " + player);
        String password = "guest";
        return new PasswordAuthentication(player, password.toCharArray());
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
        String string = "Removed from channel " + channel.getName();
        logger.info(string);
        channelsByName.remove(channel.getName());
        if (gui != null)
            gui.appendOutput(string);
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
        String string = "Whisper: " + message;
        logger.info(string);
        if (gui != null)
            gui.appendOutput(string);
    }
    
    public void updatePosition(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ) {
     //      logger.finest("UserID " + userID + ", posX " + posX + ", posZ " + posZ + ", dirX " + dirX + ", dirZ " + dirZ);
    }

    public void updatePositionAndArm(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, boolean right, float x, float y, float z) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updatePositionAndArms(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float rx, float ry, float rz, float lx, float ly, float lz) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void trigger(int userID, boolean pressed, int trigger) {
        String string = "UserID " + userID + ", pressed " + ", trigger " + trigger;
        logger.info(string);
        if (gui != null)
            gui.appendOutput(string);
    }
    
    public void listPlayers(int [] playerIDs, String [] playerNames, boolean [] male, int [] feet, int [] legs, int [] torso, int [] hair) {
        String string;
        for(int i = 0; i < playerIDs.length; i++)
        {
            string = "Listing Player with ID: " + playerIDs[i] + " called: " + playerNames[i] + " feet: " + feet[i] + " legs: " + legs[i] + " torso: " + torso[i] + " hair: " + hair[i] + " male: " + male[i];
            logger.info(string);
            users.put(playerIDs[i], playerNames[i]);
            if (gui != null)
                gui.appendOutput(string);
        }
    }

    public void addPlayer(int userID, String playerName, boolean male, int feet, int legs, int torso, int hair) {
        String string = "Adding Player with ID: " + userID + " called: " + playerName + " feet: " + feet + " legs: " + legs + " torso: " + torso + " hair: " + hair + " male: " + male;
        logger.info(string);
        users.put(userID, playerName);
        if (gui != null)
            gui.appendOutput(string);
    }

    public void removePlayer(int userID) {
        String string = "Removing Player with ID: " + userID;
        logger.info(string);
        users.remove(userID);
        if (gui != null)
            gui.appendOutput(string);
    }
    
    public void notifyLogin(int ID, String userName) {
        String string = "Notified login with ID: " + ID + " and user name: " + userName;
        logger.info(string);
        this.ID = ID;
        users.put(ID, userName);
        if (gui != null)
            gui.appendOutput(string);
        gui.addPlayerToBoards(userName, 3, 0, 0);
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
            String senderName = users.get(IDByte);
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
    
    /** GUI may be null **/
    public JNagClientGUI2 getGUI()
    {
        return gui;
    }

    public void updateBalls(int userID, float[] x, float[] y, float[] z, float[] velX, float[] velY, float[] velZ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remoteBallUpdate(int userID, int ballNumber, float x, float y, float z, float velX, float velY, float velZ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void gotHit(int userID, int byUserID, int ballID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void gameStarted(int byUserID, int hitPoints, float posX, float posY, float posZ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void gameEnded(int winnerID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}