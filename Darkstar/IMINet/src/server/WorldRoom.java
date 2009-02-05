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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

/**
 * Represents a room in the {@link SwordWorld} example MUD.
 */
public class WorldRoom extends WorldObject implements Task
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /** The {@link Logger} for this class. */
    private static final Logger logger =
        Logger.getLogger(WorldRoom.class.getName());

    /** The delay before the first run of the update scheduled task. */
    public static final int DELAY_MS = 5000;
    /** The time to wait before repeating the update scheduled task. */
    public static final int PERIOD_MS = 50;

    /** The set of items in this room. */
    private final Set<ManagedReference<WorldObject>> items =
        new HashSet<ManagedReference<WorldObject>>();

    /** The set of players in this room. */
    private final Set<ManagedReference<WorldPlayer>> players =
        new HashSet<ManagedReference<WorldPlayer>>();

    /** The {@link Channel}. */
    private ManagedReference<Channel> channel = null;
    
    /** data for the game **/
    ManagedReference<GameData> gameDataRef = null;
    
    /** server world **/
    ManagedReference<ServerWorld> worldRef = null;
        
    /**
     * Creates a new room with the given name and description, initially
     * empty of items and players.
     *
     * @param name the name of this room
     * @param description a description of this room
     */
    public WorldRoom(String name, String description, ServerWorld world) 
    {
        super(name, description);
        
        // Create the game data object
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);
        worldRef    = dataManager.createReference(world);
        gameDataRef = dataManager.createReference(new GameData());
        
        // Schedule the update task
        TaskManager taskManager = AppContext.getTaskManager();
        taskManager.schedulePeriodicTask(this, DELAY_MS, PERIOD_MS);
        
        // Create a channel for the room
        ChannelManager channelMgr = AppContext.getChannelManager();
        // Create and keep a reference to the channel.
        Channel newChannel = channelMgr.createChannel(getName(),
                                                        new WorldRoomChannelListener(),
                                                        Delivery.RELIABLE);
        channel = dataManager.createReference(newChannel);
    }
    
    /**
     * Our "update" scheduled task 
     * @throws java.lang.Exception
     */
    public void run() throws Exception 
    {
        sendUpdate();
    }
    
    public void sendUpdate()
    {
        boolean right = false;
        boolean left  = false;
        // Send each player data update to all other players
        WorldPlayer player = null;
        for(ManagedReference<WorldPlayer> worldPlayer : players)
        {
            player = worldPlayer.get();
            PlayerData data = player.getPlayerData();
            List<WorldPlayer> others = getPlayersExcluding(player);
            for (WorldPlayer other : others)
            {
                right = data.isRightArmEnabled();
                left  = data.isLeftArmEnabled();
                if (right && left)
                    other.getClientSideUser().updatePositionAndArms(data.getID(), data.getPosX(), data.getPosY(), data.getPosZ(), data.getDirX(), data.getDirY(), data.getDirZ(), data.getRightArmX(), data.getRightArmY(), data.getRightArmZ(), data.getLeftArmX(), data.getLeftArmY(), data.getLeftArmZ());
                else if (right)
                    other.getClientSideUser().updatePositionAndArm(data.getID(), data.getPosX(), data.getPosY(), data.getPosZ(), data.getDirX(), data.getDirY(), data.getDirZ(), true, data.getRightArmX(), data.getRightArmY(), data.getRightArmZ());
                else if (left)
                    other.getClientSideUser().updatePositionAndArm(data.getID(), data.getPosX(), data.getPosY(), data.getPosZ(), data.getDirX(), data.getDirY(), data.getDirZ(), false, data.getLeftArmX(), data.getLeftArmY(), data.getLeftArmZ());
                else
                    other.getClientSideUser().updatePosition(data.getID(), data.getPosX(), data.getPosY(), data.getPosZ(), data.getDirX(), data.getDirY(), data.getDirZ());
                    
            }
        }
    }

//    public void sendHandGesture(WorldPlayer player, boolean right, int gestureID) 
//    {
//        PlayerData data = player.getPlayerData();
//        List<WorldPlayer> others = getPlayersExcluding(player);
//        for (WorldPlayer other : others)
//            other.getClientSideUser().setHandGesture(data.getID(), right, gestureID);
//    }

    public void sendTrigger(WorldPlayer player, boolean pressed, int trigger) 
    {    
        PlayerData data = player.getPlayerData();
        List<WorldPlayer> others = getPlayersExcluding(player);
        for (WorldPlayer other : others)
            other.getClientSideUser().trigger(data.getID(), pressed, trigger);
    }
    
    /**
     * Adds an item to this room.
     * 
     * @param item the item to add to this room.
     * @return {@code true} if the item was added to the room
     */
    public boolean addItem(WorldObject item) {
        logger.log(Level.INFO, "{0} placed in {1}",
            new Object[] { item, this });

        // NOTE: we can't directly save the item in the list, or
        // we'll end up with a local copy of the item. Instead, we
        // must save a ManagedReference to the item.

        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);

        return items.add(dataManager.createReference(item));
    }

    /**
     * Adds a player to this room.
     * The player will join the room's channel
     *
     * @param player the player to add
     * @return {@code true} if the player was added to the room
     */
    public boolean addPlayer(WorldPlayer player) {
        logger.log(Level.INFO, "{0} enters {1} with ID {2}",
            new Object[] { player, this, player.getPlayerData().getID() });

        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);

        // Add player to the room channel
        channel.getForUpdate().join(player.getSession());
        // Setup the jnag session for the new player
        player.setupJNagSession(player.getSession(), channel.get()); 
        // Send a message to the new player with all the current ones
        sendPlayerList(player);
        // Add the player to the room and be done
        return players.add(dataManager.createReference(player));
    }

    public void sendAddPlayer(WorldPlayer player)
    {
        // Send message to all other players
        PlayerData data = player.getPlayerData();
        List<WorldPlayer> others = getPlayersExcluding (player);
        for(WorldPlayer other : others)
            other.getClientSideUser().addPlayer(data.getID(), data.getName(), data.isMale(), data.getFeet(), data.getLegs(), data.getTorso(), data.getHair(), data.getHead(), data.getSkinTone(), data.getEyeColor());
    }
    
    /**
     * Removes a player from this room and the server world.
     * The player will leave the room's channel.
     *
     * @param player the player to remove
     * @return {@code true} if the player was in the room
     */
    public boolean removePlayer(WorldPlayer player, boolean disconnected) 
    {
        logger.log(Level.INFO, "{0} leaves {1}",
            new Object[] { player, this });

        PlayerData data = player.getPlayerData();
        
        DataManager dataManager = AppContext.getDataManager();
        dataManager.markForUpdate(this);

        channel.getForUpdate().leave(player.getSession());
        // Send a message to all other players about this one leaving
        List<WorldPlayer> others = getPlayersExcluding (player); 
        for(WorldPlayer other : others)
            other.getClientSideUser().removePlayer(data.getID());
        
        if (disconnected)
            worldRef.get().userDisconnected(player);
        
        boolean removed = players.remove(dataManager.createReference(player));
        
        if (players.isEmpty())
            roomIsNowEmpty();
        
        return removed;
    }

    protected void roomIsNowEmpty() {
        logger.info("Room is now empty: " + getName());
        // By default if this is not the lobby remove it 
        ServerWorld world = worldRef.get();
        if ( !(world.getLobby().getName().equals(getName())) )
            world.removeWorldRoom(this);
    }
    
    /**
     * Returns a description of what the given player sees in this room.
     *
     * @param looker the player looking in this room
     * @return a description of what the given player sees in this room
     */
    public String look(WorldPlayer looker) {
        logger.log(Level.INFO, "{0} looks at {1}",
            new Object[] { looker, this });

        StringBuilder output = new StringBuilder();
        output.append("You are in ").append(getDescription()).append(".\n");

        List<WorldPlayer> otherPlayers =
            getPlayersExcluding(looker);

        if (! otherPlayers.isEmpty()) {
            output.append("Also in here are ");
            appendPrettyList(output, otherPlayers);
            output.append(".\n");
        }

        if (! items.isEmpty()) {
            output.append("On the floor you see:\n");
            for (ManagedReference<WorldObject> itemRef : items) {
                WorldObject item = itemRef.get();
                output.append(item.getDescription()).append('\n');
            }
        }

        return output.toString();
    }

    protected ServerWorld getWorld() {
        if (worldRef == null)
            return null;

        return worldRef.get();
    }

    /**
     * Appends the names of the {@code SwordWorldObject}s in the list
     * to the builder, separated by commas, with an "and" before the final
     * item.
     *
     * @param builder the {@code StringBuilder} to append to
     * @param list the list of items to format
     */
    private void appendPrettyList(StringBuilder builder,
        List<? extends WorldObject> list)
    {
        if (list.isEmpty())
            return;

        int lastIndex = list.size() - 1;
        WorldObject last = list.get(lastIndex);

        Iterator<? extends WorldObject> it =
            list.subList(0, lastIndex).iterator();
        if (it.hasNext()) {
            WorldObject other = it.next();
            builder.append(other.getName());
            while (it.hasNext()) {
                other = it.next();
                builder.append(" ,");
                builder.append(other.getName());
            }
            builder.append(" and ");
        }
        builder.append(last.getName());
    }

    /**
     * Returns a list of players in this room excluding the given
     * player.
     *
     * @param player the player to exclude
     * @return the list of players
     */
    private List<WorldPlayer>
            getPlayersExcluding(WorldPlayer player)
    {
        if (players.isEmpty())
            return Collections.emptyList();

        ArrayList<WorldPlayer> otherPlayers =
            new ArrayList<WorldPlayer>(players.size());

        for (ManagedReference<WorldPlayer> playerRef : players) {
            WorldPlayer other = playerRef.get();
            if (! player.equals(other))
                otherPlayers.add(other);
        }

        return Collections.unmodifiableList(otherPlayers);
    }
    
    private void sendPlayerList(WorldPlayer player) 
    {
        if (players.isEmpty())
            return;
        
        ArrayList<PlayerData> playerList = new ArrayList<PlayerData>();
        List<WorldPlayer> otherList = getPlayersExcluding(player);
        for (WorldPlayer other : otherList) 
            playerList.add(other.getPlayerData());    
        
        int i = 0;
        int size = playerList.size();
        int     [] ids      = new int[size];
        String  [] names    = new String[size];
        boolean [] male     = new boolean[size];
        int     [] feet     = new int[size];
        int     [] legs     = new int[size];
        int     [] torso    = new int[size];
        int     [] hair     = new int[size];
        int     [] head     = new int[size];
        int     [] skinTone     = new int[size];
        int     [] eyeColor     = new int[size];
        for (PlayerData p : playerList)
        {
            ids[i]   = p.getID();
            names[i] = p.getName();
            male[i]  = p.isMale();
            feet[i]  = p.getFeet();
            legs[i]  = p.getLegs();
            torso[i] = p.getTorso();
            hair[i]  = p.getHair();
            head[i]  = p.getHead();
            skinTone[i]  = p.getSkinTone();
            eyeColor[i]  = p.getEyeColor();
            i++;
        }
        player.getClientSideUser().listPlayers(ids, names, male, feet, legs, torso, hair, head, skinTone, eyeColor);
    }
    
    public class Vector3
    {
        public float x = 0.0f;
        public float y = 0.0f;
        public float z = 0.0f;

        public Vector3() {}
        public Vector3(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    public class CircleUtil 
    {
        private int     m_points = 0;
        private float   m_radius = 0.0f;
        Vector3 []      m_circle = null;

        public CircleUtil(int points, float radius)
        {
            m_points = points;
            m_radius = radius;
        }

        public Vector3 [] calculatePoints()
        {
            m_circle = new Vector3 [m_points];

            double angleStep = Math.PI * 2 / m_points;
            double theta = 0.0f;

            for (int i = 0; i < m_points; i++)
            {
                m_circle[i] = new Vector3();
                m_circle[i].x = (float)Math.cos(theta) * -1.0f * m_radius;
                m_circle[i].z = (float)Math.sin(theta) * m_radius;

                theta += angleStep;
            }

            return m_circle;
        }

        public Vector3 [] getCircleRef()
        {
            return m_circle;
        }

        public Vector3 getPoint(int index)
        {
            if (m_circle != null)
                return m_circle[index];
            return null;
        }
    }
    
}
