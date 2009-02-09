/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import client.ClientSideCahuaUser;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import java.util.List;
import net.java.dev.jnag.sgs.app.JnagSession;
import server.utils.CircleBuilder;
import server.utils.Vector3;

/**
 *
 * @author Lou Hayt
 */
public class CahuaPlayerExtention extends WorldPlayerExtension implements ServerSideCahuaUser
{    
    private JnagSession                           jnagSession        = null;
    private ManagedReference<WorldPlayer>         playerRef          = null;
    private ManagedReference<ClientSideCahuaUser> clientSideUserRef  = null;

    public CahuaPlayerExtention(JnagSession jnagSession, WorldPlayer player) 
    {   
        this.jnagSession = jnagSession;
        jnagSession.addToLocalInterface(this);
        DataManager dataManager = AppContext.getDataManager();
        playerRef = (ManagedReference<WorldPlayer>)dataManager.createReference(player);
        clientSideUserRef = dataManager.createReference(jnagSession.addToRemoteInterface(ClientSideCahuaUser.class));
    }

    @Override
    public void releaseJNagSession() 
    {
        try { // may throw object not found when initializing first time
        jnagSession.removeFromRemoteInterface(clientSideUserRef.get());
        jnagSession.removeFromLocalInterface(this); } catch (Exception ex) { }
    }
    
    @Override
    public void trigger(boolean pressed, int trigger) {
    }

    @Override
    public void playerRemoved(WorldPlayer player) 
    {
        if (player.getPlayerData().isPlayingGame())
            player.getRoom().getGameData().adjustNumberOfPlayers(-1);
    }
    
    public void updateBalls(float[] x, float[] y, float[] z, float[] velX, float[] velY, float[] velZ) 
    {
        WorldPlayer player = playerRef.get();
        // Send message to all other players
        PlayerData data = player.getPlayerData();
        List<WorldPlayer> others = player.getRoom().getPlayersExcluding (player);
        for(WorldPlayer other : others)
        {
            if (other.getExtension() != null && other.getExtension().getClientProxy() != null)
                ((ClientSideCahuaUser)other.getExtension().getClientProxy()).updateBalls(data.getID(), x, y, z, velX, velY, velZ);
        }
    }

    public void remoteBallUpdate(int userID, int ballNumber, float x, float y, float z, float velX, float velY, float velZ) 
    {
        WorldPlayer player = playerRef.get();
        // Send message to all other players
        List<WorldPlayer> others = player.getRoom().getPlayersExcluding (player);
        for(WorldPlayer other : others)
            ((ClientSideCahuaUser)other.getExtension().getClientProxy()).remoteBallUpdate(userID, ballNumber, x, y, z, velX, velY, velZ);
    }

    public void gotHit(int byUserID, int ballID) 
    {
        WorldPlayer player = playerRef.get();
        PlayerData data = player.getPlayerData();
        data.adjustHitPoints(-1);
        boolean out = false;
        if (data.getHitPoints() <= 0)
        {
            data.setPlayingGame(false);
            out = true;
        }
        
        // Send message to all other players
        WorldRoom room = player.getRoom();
        List<WorldPlayer> others = room.getPlayersExcluding (player);
        for(WorldPlayer other : others)
            ((ClientSideCahuaUser)other.getExtension().getClientProxy()).gotHit(data.getID(), byUserID, ballID);
        
        if (out)
        {
            GameData game = room.getGameData();
            game.adjustNumberOfPlayers(-1);
            if (game.getNumberOfPlayers() == 1)
            {
                // Find the winner
                List<WorldPlayer> players = room.getPlayers();
                for(WorldPlayer p : players)
                {
                    PlayerData pd = p.getPlayerData();
                    if (pd.isPlayingGame()) // last man standing
                    {
                        int winnerID = pd.getID();
                        for(WorldPlayer l : players)
                            ((ClientSideCahuaUser)l.getExtension().getClientProxy()).gameEnded(winnerID);
                        return;
                    }
                }
            }
        }
    }

    public void startGame(int hitPoints) 
    {
        WorldPlayer       player  = playerRef.get();
        WorldRoom         room    = player.getRoom();
        GameData          game    = room.getGameData();
        List<WorldPlayer> players = room.getPlayers();
        
        if (game.isGameOn() && game.getNumberOfPlayers() > 1)
            player.getClientSideUser().whisper("A game is running with " + game.getNumberOfPlayers() + " players");
        else if (players.size() > 1)
        {
            game.setGameOn(true);
            int numberOfPlayers = players.size();
            game.setNumberOfPlayers(numberOfPlayers);
            Vector3 [] circle = new CircleBuilder(numberOfPlayers, 5.0f).calculatePoints();
            // Send message to all players
            int gameStarter = player.getPlayerData().getID();
            int i = 0;
            for(WorldPlayer pl : players)
            {
                ((ClientSideCahuaUser)pl.getExtension().getClientProxy()).gameStarted(gameStarter, hitPoints, circle[i].x, circle[i].y, circle[i].z);
                PlayerData data = pl.getPlayerData();
                data.setPlayingGame(true);
                data.setHitPoints(hitPoints);
                i++;
            }
        }
        else
            player.getClientSideUser().whisper("Not enough players to start a game"); 
    }
    
    public ClientSideCahuaUser getClientProxy() {
        if (clientSideUserRef == null)
            return null;
        return clientSideUserRef.get();
    }

}
