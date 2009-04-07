/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.networking;

import client.ClientSideCahuaUser;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.character.Character;
import imi.character.VerletArm;
import imi.networking.CharacterClientExtension.CharacterDataExtension;
import imi.networking.Client.ClientAvatar;
import imi.networking.Client.UserData;
import imi.scene.PMatrix;
import imi.scene.utils.visualizations.VisuManager;
import imi.utils.PMathUtils;
import java.util.Collection;
import net.java.dev.jnag.sgs.client.JnagSession;
import server.ServerSideCahuaUser;

/**
 *
 * @author Lou Hayt
 */
public class CahuaClientExtention extends ClientExtension implements ClientSideCahuaUser
{
    private Client              masterClient = null;
    private JnagSession         jnagSession  = null;
    private ServerSideCahuaUser serverProxy  = null;
    private CharacterClientExtension   characterClientExtension = null;
    
    private imi.character.Character    character = null;
    private ClientGUI   gui                       = null;
    
    private VisuManager vis = null;
    
    private float pitcherTimer = 0;
    private float roomSize      = 10.0f;
    private int ballToPitch     = 0;
    private int numberOfBalls   = 5;
    private float handRadius    = 0.15f;
    private float ballRadius    = 0.5f;
    private Vector3f[] balls    = new Vector3f[numberOfBalls];
    private Vector3f[] ballsVel = new Vector3f[numberOfBalls];
    private Vector3f gravity    = new Vector3f(0.0f, -0.000098f, 0.0f);
    private Vector3f hitBoxPos  = new Vector3f();
    private Vector3f hitBoxMin  = new Vector3f(-0.25f, 0.0f, -0.25f);
    private Vector3f hitBoxMax  = new Vector3f(0.25f, 2.0f, 0.25f);
    private Vector3f ballMin    = new Vector3f(-ballRadius, -ballRadius, -ballRadius);
    private Vector3f ballMax    = new Vector3f(ballRadius, ballRadius, ballRadius);
    private int hitPoints       = 0;
    private Vector3f gamePos    = new Vector3f();
    
    // Arrays for packet delivery
    float [] ballBosX = new float[numberOfBalls];
    float [] ballPosY = new float[numberOfBalls];
    float [] ballPosZ = new float[numberOfBalls];
    float [] ballVelX = new float[numberOfBalls];
    float [] ballVelY = new float[numberOfBalls];
    float [] ballVelZ = new float[numberOfBalls];
    
    public CahuaClientExtention(JnagSession jnagSession, Client masterClient, CharacterClientExtension characterClientExtension) 
    {
        this.characterClientExtension = characterClientExtension;
        this.masterClient = masterClient;
        this.jnagSession  = jnagSession;
        
        serverProxy = jnagSession.addToRemoteInterface(ServerSideCahuaUser.class);
        jnagSession.addToLocalInterface(this);
        
        character     = characterClientExtension.getCharacter();
        gui           = masterClient.getGUI();
        
        // Initialize cahua user data extensions
        vis = new VisuManager("cahua visualization", characterClientExtension.getWorldManager());
        Collection<UserData> dataCollection = masterClient.getUsers().values();
        for (UserData data : dataCollection)
            data.extension.put(CahuaDataExtension.class ,new CahuaDataExtension());
        
        // Initialize balls
        for (int i = 0; i < numberOfBalls; i++)
        {
            balls[i]    = new Vector3f(10000.0f, 0.0f, 0.0f);
            ballsVel[i] = new Vector3f();
            vis.addPositionObject(balls[i], ColorRGBA.red, ballRadius);
        }
        
        // Visualize
        vis.addPositionObject(character.getLeftArm().getWristPosition(), ColorRGBA.magenta, handRadius);
        vis.addPositionObject(character.getRightArm().getWristPosition(), ColorRGBA.magenta, handRadius);
        vis.addBoxObject(hitBoxPos, hitBoxMin, hitBoxMax, ColorRGBA.lightGray);
    }

    @Override
    public void releaseJNagSession()
    {
        jnagSession.removeFromRemoteInterface(serverProxy);
        jnagSession.removeFromLocalInterface(this);
        vis.clearObjects();
        
        Collection<UserData> dataCollection = masterClient.getUsers().values();
        for (UserData data : dataCollection)
        {
            CahuaDataExtension dataExt = ((CahuaDataExtension)data.getExtension(CahuaDataExtension.class));
            if (dataExt != null)
            {
                dataExt.clean();
                data.extension.remove(CahuaDataExtension.class);
            }
        }
    }
    
    @Override
    void notifyLogin(String roomName)  {   
        
    }
    
    public void startGame(int lives)
    {
        serverProxy.startGame(lives);
    }
    
    public void pitchBall(Vector3f ballPosition, Vector3f ballVelocity)
    {
        if (hitPoints <= 0)
            return;
        balls[ballToPitch].set(ballPosition);
        ballsVel[ballToPitch].set(ballVelocity);
        ballToPitch++;
        if (ballToPitch >= numberOfBalls)
            ballToPitch = 0;
    }
    
    @Override
    public void update(float deltaTime, boolean updateTick)
    {
        // Update hitbox position
        hitBoxPos.set(character.getPositionRef());
        // If you are in the game you can't move
        if (hitPoints > 0)
        {
            PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
            local.setTranslation(gamePos);
        }
        
        if (!updateTick)
            return;
        
        // Pitch balls
        pitcherTimer += deltaTime;
        if (pitcherTimer > 3.0f)
        {
            pitcherTimer = 0.0f;
            Vector3f dir = character.getPositionRef().add(0.0f, 1.8f, 0.0f).subtract(Vector3f.ZERO).normalize();
            pitchBall(Vector3f.ZERO, dir.mult(0.1f));
        }
        
        Collection<UserData> dataCollection = masterClient.getUsers().values();
        for (UserData data : dataCollection)
        {
            CharacterDataExtension dataExt = characterClientExtension.getUserData(data);
            if (dataExt == null)
                continue;
            Character user = dataExt.character;
            if (user == null || !user.isInitialized() || user.getController().getModelInstance() == null)
                continue;

            // Predict the new position for the remote balls locally
            CahuaDataExtension ext = getUserData(data);
            for (int i = 0; i < numberOfBalls; i++)
            {   
                if (ext.balls[i] == null)
                        return;
                        
                Vector3f pos = ext.balls[i];
                Vector3f vel = ext.ballsVel[i].mult(0.999f); // decay so the network updates will push forward instead of backwards
                // Accelerate the velocity 
                vel.addLocal(gravity);
                // Add velocity to the ball's position
                pos.addLocal(vel);

                // Check for collision
                if (checkCollisionBallWithHitBox(pos))
                {
                    gui.appendOutput("OUCH! You got hit by " + masterClient.getUserName(data.userID) + "'s ball!");
                    hitPoints--;
                    serverProxy.gotHit(data.userID, i);
                    pos.set(hitBoxPos.add(0.0f, 2.0f + ballRadius, 0.0f));
                    vel.set(0.0f, 0.25f, 0.0f);
                    serverProxy.remoteBallUpdate(data.userID, i, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
                    if (hitPoints <= 0)
                        spawnOutside();
                }
                if (checkCollisionBallWithHand(character.getLeftArm(), pos, vel))
                        serverProxy.remoteBallUpdate(data.userID, i, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
                if (checkCollisionBallWithHand(character.getRightArm(), pos, vel))
                        serverProxy.remoteBallUpdate(data.userID, i, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
            }
        }
        
        // Compute local balls
        for (int i = 0; i < numberOfBalls; i++)
        {
            Vector3f pos = balls[i];
            Vector3f vel = ballsVel[i];

            // Accelerate the velocity 
            vel.addLocal(gravity);
            // Add velocity to the ball's position
            pos.addLocal(vel);

            // Check collision with the world bounds
            if (pos.y < 0.0f)
                vel.y *= -1.0f;
            if (pos.x < -roomSize || pos.x > roomSize)
                vel.x *= -1.0f;
            if (pos.z < -roomSize || pos.z > roomSize)
                vel.z *= -1.0f;

            // Check for collision
            if (checkCollisionBallWithHitBox(pos))
            {       
                gui.appendOutput("OUCH! You got hit by your own ball!");
                hitPoints--;
                serverProxy.gotHit(masterClient.getID(), i);
                pos.set(hitBoxPos.add(0.0f, 2.0f + ballRadius, 0.0f));
                vel.set(0.0f, 0.25f, 0.0f);
                if (hitPoints <= 0)
                    spawnOutside();
            }
            checkCollisionBallWithHand(character.getLeftArm(), pos, vel);
            checkCollisionBallWithHand(character.getRightArm(), pos, vel);
        }
        
        if (updateTick)
        {
            // Prepare the update for the server
            for (int i = 0; i < numberOfBalls; i++)
            {
                ballBosX[i] = balls[i].x;
                ballPosY[i] = balls[i].y;
                ballPosZ[i] = balls[i].z;
                ballVelX[i] = ballsVel[i].x;
                ballVelY[i] = ballsVel[i].y;
                ballVelZ[i] = ballsVel[i].z;
            }
            //serverProxy.updateBalls(ballBosX, ballPosY, ballPosZ, ballVelX, ballVelY, ballVelZ);
        }
    }

    public void updateBalls(int userID, float[] x, float[] y, float[] z, float[] velX, float[] velY, float[] velZ) 
    {
        UserData data = masterClient.getUserData(userID);
        if (data == null)
        {
            gui.appendOutput("null character balls update with ID: " + userID);
            return;
        }
        
        CahuaDataExtension ext = getUserData(data);
        for (int i = 0; i < numberOfBalls; i++)
        {
            ext.balls[i].set(x[i], y[i], z[i]);
            ext.ballsVel[i].set(velX[i], velY[i], velZ[i]);
        }
    }

    public void remoteBallUpdate(int userID, int ballNumber, float x, float y, float z, float velX, float velY, float velZ) 
    {
        if (userID == masterClient.getID())
        {
            Vector3f pos = balls[ballNumber];
            Vector3f vel = ballsVel[ballNumber];
            pos.set(x, y, z);
            vel.set(velX, velY, velZ);
        }
        else
        {
            UserData data = masterClient.getUserData(userID);
            CahuaDataExtension ext = getUserData(data);
            Vector3f pos = ext.balls[ballNumber];
            Vector3f vel = ext.ballsVel[ballNumber];
            pos.set(x, y, z);
            vel.set(velX, velY, velZ);
        }
    }

    public void gotHit(int userID, int byUserID, int ballID) 
    {
        UserData hitter = masterClient.getUserData(byUserID);
        UserData hit = masterClient.getUserData(userID);
        if (hitter == null || hit == null)
        {
            if (hitter == null)
                gui.appendOutput("null gotHit message by userID: " + byUserID);
            if (hit == null)
                gui.appendOutput("null gotHit message with userID: " + userID);
            return;
        }
        gui.appendOutput("HIT! " + masterClient.getUserName(userID) + " got hit by " + masterClient.getUserName(byUserID) + "'s ball and it hurts his pride!");
        
        // Set new position and velocity for that ball
        CahuaDataExtension ext = getUserData(hitter);
        ext.balls[ballID].set(characterClientExtension.getUserData(hit).character.getPositionRef().add(0.0f, 2.0f + ballRadius, 0.0f));
        ext.ballsVel[ballID].set(0.0f, 0.25f, 0.0f); 
    }

    public void gameStarted(int byUserID, int hitPoints, float posX, float posY, float posZ) 
    {
        gui.appendOutput("Game STARTED! by " + masterClient.getUserName(byUserID) + " and you get " + hitPoints + " hit points, good luck!");
        gamePos.set(posX, posY, posZ);
        Vector3f dir = Vector3f.ZERO.subtract(gamePos).normalize();
        PMatrix look = PMathUtils.lookAt(gamePos.subtract(dir), gamePos, Vector3f.UNIT_Y);
        PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
        local.set(look);
        
        this.hitPoints = hitPoints;
        for (int i = 0; i < numberOfBalls; i++)
        {
            balls[i].set(10000.0f, 0.0f, 0.0f);
            ballsVel[i].set(0.0f, 0.0f, 0.0f);
        }
    }

    public void gameEnded(int winnerID) 
    {
        hitPoints = 0;
        gui.appendOutput(masterClient.getUserName(winnerID) + " WINS THE GAME!!!");
        if (winnerID == masterClient.getID())
        {
            gui.appendOutput("YOU WON THE GAME! YOU ARE AWESOME!");
            PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
            local.setTranslation(Vector3f.ZERO);
        }
        else
            gui.appendOutput("Better luck next time");
        for (int i = 0; i < numberOfBalls; i++)
        {
            balls[i].set(10000.0f, 0.0f, 0.0f);
            ballsVel[i].set(0.0f, 0.0f, 0.0f);
        } 
    }
    
    private void spawnOutside() 
    {
        gui.appendOutput("You are out of the game!");
        float x = (float)Math.random() % 20 + 10;
        float z = (float)Math.random() % 20 + 10;
        if (Math.random() < 0.5f)
            x *= -1.0f;
        if (Math.random() < 0.5f)
            z *= -1.0f;
        PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
        local.setTranslation(new Vector3f(x, 0.0f, z));
    }
    
    private boolean checkCollisionBallWithHand(VerletArm arm, Vector3f pos, Vector3f vel) 
    {    
        if (arm == null || !arm.isEnabled())
            return false;
        
        Vector3f handPos = arm.getWristPosition();
        Vector3f handVel = arm.getWristVelocity();

        float distance = handPos.distance(pos);
        if (distance < handRadius + ballRadius)
        {
            // Calculate the collision normal
            Vector3f normal = pos.subtract(handPos).normalize();
            // Project the ball outside of the collision
            pos.set(pos.add(normal.mult(handRadius + ballRadius - distance)));

            Vector3f ballTransferXVel = normal.mult(normal.dot(handVel) + 0.05f);
            Vector3f handTransferXvel = normal.mult(normal.dot(vel));
            Vector3f ballYVel = vel.subtract(handTransferXvel);

            vel.set(ballTransferXVel.add(ballYVel));
            return true;
        }
        return false;
    }
    
    private boolean checkCollisionBallWithHitBox(Vector3f pos) 
    {
        Vector3f aMin = pos.add(ballMin);
        Vector3f aMax = pos.add(ballMax);
        Vector3f bMin = hitBoxPos.add(hitBoxMin);
        Vector3f bMax = hitBoxPos.add(hitBoxMax);
        
        // check if two Rectangles intersect
        return (aMax.x > bMin.x && aMin.x < bMax.x &&
                aMin.y < bMax.y && aMax.y > bMin.y &&
                aMin.z < bMax.z && aMax.z > bMin.z);
    }
    
    @Override
    UserDataExtension getNewUserDataExtension() {
        return new CahuaDataExtension();
    }
    
    public CahuaDataExtension getUserData(int userID) {
        return (CahuaDataExtension)masterClient.getUserData(userID).getExtension(CahuaDataExtension.class);
    }
        
    public CahuaDataExtension getUserData(UserData data) {
        return (CahuaDataExtension)data.getExtension(CahuaDataExtension.class);
    }
    
    public class CahuaDataExtension implements UserDataExtension
    {
        public Vector3f[] balls    = new Vector3f[numberOfBalls];
        public Vector3f[] ballsVel = new Vector3f[numberOfBalls];
        
        public CahuaDataExtension()
        {
            for (int i = 0; i < numberOfBalls; i++)
            {
                balls[i]    = new Vector3f(10000.0f, 0.0f, 0.0f);
                ballsVel[i] = new Vector3f();
                vis.addPositionObject(balls[i], ColorRGBA.red, ballRadius);
            }
        }
        
        public void added(UserData data, ClientAvatar clientAvatar) {
        }
        
        public void clean()
        {
            for (int i = 0; i < numberOfBalls; i++)
            {
                balls[i].set(10000.0f, 0.0f, 0.0f); // hehe
                vis.removePositionObject(balls[i]); // TODO
            }
        }
    }
}
