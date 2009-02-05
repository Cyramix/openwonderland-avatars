package server;

import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Lou Hayt
 */
public class ServerWorld implements Serializable, AppListener
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /** The {@link Logger} for this class. */
    private static final Logger logger =
        Logger.getLogger(ServerWorld.class.getName());

    /** The prefix for world room bindings in the {@code DataManager}. */
    protected static final String WORLD_BIND_PREFIX = "World.";
    
    /** A reference to the lobby room. */
    private ManagedReference<WorldRoom> lobbyRoomRef = null;
    
    /** Reference to the other rooms. */
    private final Set<ManagedReference<WorldRoom>> roomRef = new HashSet<ManagedReference<WorldRoom>>();
    
    /** Reference to all users. */
    private final Set<ManagedReference<WorldPlayer>> userRef = new HashSet<ManagedReference<WorldPlayer>>();
    
    private int userIDSeed = 0;
    
    /**
     * Creates the server world.
     */
    public void initialize(Properties props) 
    {
        logger.info("Initializing World");
        
        // Get or create a lobby and keep the reference
        WorldRoom lobby = getRoom("Lobby Room");
        lobby.setDescription("Wellcome to the world between worlds");
        setLobby(lobby);
        
        logger.info("World Initialized");
    }

    /**
     * Gets the lobby room
     * @return the lobby room
     */
    public WorldRoom getLobby() {
        if (lobbyRoomRef == null)
            return null;

        return lobbyRoomRef.get();
    }

    /**
     * Sets the lobby room.
     * @param room the room to set
     */
    public void setLobby(WorldRoom room) {
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        if (room == null) {
            lobbyRoomRef = null;
            return;
        }
        lobbyRoomRef = dataManager.createReference(room);
    }

    /**
     * Lets a new user come in
     */
    public ClientSessionListener loggedIn(ClientSession session) {
        logger.log(Level.INFO,
            "ServerWorld Client login: {0}", session.getName());

        // Delegate to a factory method on WorldPlayer,
        // since player management really belongs in that class.
        WorldPlayer player = WorldPlayer.loggedIn(session, userIDSeed);

        // Keep a server wide reference to this user
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        userRef.add(dataManager.createReference(player));
        
        // Put player in room
        player.enter(getLobby());
        
        userIDSeed++;

        // return player object as listener to this client session
        return player;
    }
    
    /**
     * A user has disconnected
     * @param player
     */
    public void userDisconnected(WorldPlayer player)
    {
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        userRef.remove(dataManager.createReference(player));
    }
    
    public String [] getWorldNames()
    {   
        ArrayList<String> names = new ArrayList<String>();
     
        for (ManagedReference<WorldRoom> room : roomRef)
            names.add(room.get().getName());
        
        String [] list = new String[0];
        list = names.toArray(list);
        return list;
    }
    
    public WorldRoom getRoom(String roomName) 
    {
        String roomBinding = WORLD_BIND_PREFIX + roomName;
        DataManager dataMgr = AppContext.getDataManager();
        WorldRoom room;
        try {
            room = (WorldRoom) dataMgr.getBinding(roomBinding);
        } catch (NameNotBoundException ex) {
            // this is a new room
            dataMgr.markForUpdate(this);
            room = new WorldRoom(roomName, roomName + "'s description", this);
            logger.log(Level.INFO, "New room created: {0}", room);
            dataMgr.setBinding(roomBinding, room);
            roomRef.add(dataMgr.createReference(room));
        }
        return room;
    }
    
    public boolean removeWorldRoom(WorldRoom room) {
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        return roomRef.remove(dataManager.createReference(room));   
    }
}
