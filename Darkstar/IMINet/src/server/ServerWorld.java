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

    /** A reference to the one-and-only {@linkplain SwordWorldRoom room}. */
    private ManagedReference<WorldRoom> lobbyRoomRef = null;
    
    /**
     * {@inheritDoc}
     * <p>
     * Creates the world within the MUD.
     */
    public void initialize(Properties props) 
    {
        logger.info("Initializing World");
        
        // Create the lobby
        WorldRoom room = new WorldRoom("Lobby Room", "Wellcome to the lobby");

        // Create a chair
        WorldObject chair = new WorldObject("Chair", "a neat chair.");

        // Put the chair in the Room
        room.addItem(chair);

        // Keep a reference to the Room
        setLobby(room);

        logger.info("World Initialized");
    }

    /**
     * Gets the SwordWorld's One True Room.
     * <p>
     * @return the room for this {@code SwordWorld}
     */
    public WorldRoom getLobby() {
        if (lobbyRoomRef == null)
            return null;

        return lobbyRoomRef.get();
    }

    /**
     * Sets the SwordWorld's One True Room to the given room.
     * <p>
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
     * {@inheritDoc}
     * <p>
     * Obtains the {@linkplain WorldPlayer player} for this
     * {@linkplain ClientSession session}'s user, and puts the
     * player into the One True Room for this {@code ServerWorld}.
     */
    public ClientSessionListener loggedIn(ClientSession session) {
        logger.log(Level.INFO,
            "ServerWorld Client login: {0}", session.getName());

        // Delegate to a factory method on SwordWorldPlayer,
        // since player management really belongs in that class.
        WorldPlayer player = WorldPlayer.loggedIn(session);

        // Put player in room
        player.enter(getLobby());

        // return player object as listener to this client session
        return player;
    }
}
