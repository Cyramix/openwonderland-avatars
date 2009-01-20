/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.character.networking;

import imi.character.*;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.character.Character;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.character.ninja.NinjaContext;
import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.ninja.NinjaFemaleAvatarAttributes;
import imi.character.statemachine.GameContextListener;
import imi.character.statemachine.corestates.IdleState;
import imi.scene.PMatrix;
import imi.scene.Updatable;
import imi.scene.camera.behaviors.ThirdPersonCamModel;
import imi.scene.camera.state.ThirdPersonCamState;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.utils.visualizations.VisuManager;
import imi.utils.PMathUtils;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Random;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class DarkstarClient extends JNagClient implements Updatable
{
    /** user ID to CharacterData map (for the current game room) */
    protected HashMap<Integer, UserData> characterData = new HashMap<Integer, UserData>();
    
    protected Character    character = null;
    protected WorldManager worldManager = null;
        
    // Updates messages occure at a fixed interval
    private float    clientTimer         = 0.0f;
    private float    clientUpdateTick    = 1.0f / 30.0f;
    private Vector3f prevPos = new Vector3f();
    private Vector3f prevDir = new Vector3f(0.0f, 0.0f, -1.0f);
    
    private float positionPullStrength          = 0.01f;
    private float positionMinDistanceForPull    = 0.1f;
    private float positionMaxDistanceForPull    = 3.0f;
    
    private float handPullStrength          = 0.1f;
    private float handMaxDistanceForPull    = 1.0f;
    
    private boolean male = true;
    private int feet  = -1;
    private int legs  = -1;
    private int torso = -1;
    private int hair  = -1;
    
    private String userName = null;
    private String password = null;
        
    // Test
    VisuManager vis = null;
    
    // Balls
    private float roomSize      = 10.0f;
    private int ballToPitch     = 0;
    private int numberOfBalls   = 5;
    private float handRadius    = 0.15f;
    private float ballRadius    = 0.5f;
    private Vector3f[] balls    = new Vector3f[numberOfBalls];
    private Vector3f[] ballsVel = new Vector3f[numberOfBalls];
    private Vector3f gravity    = new Vector3f(0.0f, -0.000098f, 0.0f);
    private Vector3f hitBoxPos  = new Vector3f();
    private Vector3f hitBoxMin  = new Vector3f(-0.15f, 0.0f, -0.15f);
    private Vector3f hitBoxMax  = new Vector3f(0.15f, 2.0f, 0.15f);
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
        
    public DarkstarClient(Character character)
    {
        super();
        this.character = character;
        worldManager = character.getWorldManager(); 
        
        // Test
        vis = new VisuManager(userName, worldManager);
        vis.setWireframe(false);
        vis.addPositionObject(character.getLeftArm().getWristPosition(), ColorRGBA.magenta, handRadius);
        vis.addPositionObject(character.getRightArm().getWristPosition(), ColorRGBA.magenta, handRadius);
        //vis.addBoxObject(hitBoxPos, hitBoxMin, hitBoxMax, ColorRGBA.lightGray);
        for (int i = 0; i < numberOfBalls; i++)
        {
            balls[i]    = new Vector3f(10000.0f, 0.0f, 0.0f);
            ballsVel[i] = new Vector3f();
            vis.addPositionObject(balls[i], ColorRGBA.red, ballRadius);
        }
    }

    public DarkstarClient(Character character, boolean male, int feet, int legs, int torso, int hair) 
    {
        this(character);
        this.male  = male;
        this.feet  = feet;
        this.legs  = legs;
        this.torso = torso;
        this.hair  = hair;
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
    public void gotHit(int userID, int byUserID, int ballID) 
    {
        UserData hitter = characterData.get(byUserID);
        UserData hit = characterData.get(byUserID);
        if (hitter == null)
        {
            postGUILine("null gotHit message by userID: " + userID);
            return;
        }
        if (hit == null)
        {
            postGUILine("null gotHit message with userID: " + userID);
            return;
        }
        
        postGUILine("HIT! " + users.get(userID) + " got hit by " + users.get(byUserID) + "'s ball and it hurts his pride!");
        
        performAnimation(1, false, false, userID); // shake head
        
        // Set new position and velocity for that ball
        hitter.balls[ballID].set(hit.user.getPosition().add(0.0f, 2.0f + ballRadius, 0.0f));
        hitter.ballsVel[ballID].set(0.0f, 0.25f, 0.0f);
    }

    @Override
    public void gameStarted(int byUserID, int hitPoints, float posX, float posY, float posZ) 
    {
        postGUILine("Game STARTED! by " + users.get(byUserID) + " and you get " + hitPoints + " hit points, good luck!");
        for (int i = 0; i < users.size(); i++) {
            gui.setPlayerStatsInBoards(users.get(byUserID), hitPoints, -1, -1);
        }
        gamePos.set(posX, posY, posZ);
        Vector3f dir = Vector3f.ZERO.subtract(gamePos).normalize();
        PMatrix look = PMathUtils.lookAt(gamePos.subtract(dir), gamePos, Vector3f.UNIT_Y);
        PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
        local.set(look);
        
        character.makeFist(true, true);
        character.getRightArm().setEnabled(true);
        
        ThirdPersonCamState camState = (ThirdPersonCamState)((FlexibleCameraProcessor)worldManager.getUserData(FlexibleCameraProcessor.class)).getState();
        Vector3f toCam = new Vector3f(camState.getToCamera());
        local.transformNormal(toCam);
        Vector3f camPos = toCam.add(local.getTranslation());
        ThirdPersonCamModel camModel = (ThirdPersonCamModel)((FlexibleCameraProcessor)worldManager.getUserData(FlexibleCameraProcessor.class)).getModel();
        camModel.moveTo(camPos, camState);
        
        this.hitPoints = hitPoints;
        for (int i = 0; i < numberOfBalls; i++)
        {
            balls[i].set(10000.0f, 0.0f, 0.0f);
            ballsVel[i].set(0.0f, 0.0f, 0.0f);
        }
        
        performAnimation(7, true, true, 0); // bow
    }

    public void performAnimation(int actionIndex, boolean client, boolean allUsers, int specificUser)
    {
        if (client)
            ((NinjaContext)character.getContext()).performAction(actionIndex);
        
        if (allUsers)
        {
            for (UserData data : characterData.values())
                ((NinjaContext)data.user.getContext()).performAction(actionIndex);
        }
        else if (characterData.get(specificUser) != null)
        {
            Character user = characterData.get(specificUser).user;
            ((NinjaContext)user.getContext()).performAction(actionIndex);
        }
    }
    
    @Override
    public void gameEnded(int winnerID) 
    {
        hitPoints = 0;
        postGUILine(users.get(winnerID) + " WINS THE GAME!!!");
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).equals(users.get(winnerID)))
                gui.setPlayerWins(users.get(winnerID), 1);
            else
                gui.setPlayerLosses(users.get(i), 1);
        }

        if (winnerID == ID)
        {
            postGUILine("YOU WON THE GAME! YOU ARE AWESOME!");
            PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
            local.setTranslation(Vector3f.ZERO);
            
            performAnimation(5, true, false, 0); // cheer
            performAnimation(6, false, true, 0); // clap
        }
        else
        {
            postGUILine("Better luck next time");
            performAnimation(6, true, true, 0); // clap
            performAnimation(5, false, false, winnerID); // cheer
        }
        for (int i = 0; i < numberOfBalls; i++)
        {
            balls[i].set(10000.0f, 0.0f, 0.0f);
            ballsVel[i].set(0.0f, 0.0f, 0.0f);
        }
    }
    
    @Override
    public void updateBalls(int userID, float [] posX, float [] posY, float [] posZ, float [] velX, float [] velY, float [] velZ)
    {
        UserData data = characterData.get(userID);
        if (data == null)
            postGUILine("null character balls update with ID: " + userID);
        
        for (int i = 0; i < numberOfBalls; i++)
        {
            data.balls[i].set(posX[i], posY[i], posZ[i]);
            data.ballsVel[i].set(velX[i], velY[i], velZ[i]);
        }
    }
    
    @Override
    public void remoteBallUpdate(int userID, int ballNumber, float x, float y, float z, float velX, float velY, float velZ) 
    {
        if (userID == ID)
        {
            Vector3f pos = balls[ballNumber];
            Vector3f vel = ballsVel[ballNumber];
            pos.set(x, y, z);
            vel.set(velX, velY, velZ);
        }
        else
        {
            UserData data = characterData.get(userID);
            Vector3f pos = data.balls[ballNumber];
            Vector3f vel = data.ballsVel[ballNumber];
            pos.set(x, y, z);
            vel.set(velX, velY, velZ);
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
        serverProxy.setAvatarInfo(male, feet, legs, torso, hair);
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
            // Update hitbox position
            hitBoxPos.set(character.getPosition());
            // If you are in the game you can't move
            if (hitPoints > 0)
            {
                PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
                local.setTranslation(gamePos);
            }
            
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
                
                // Predict the new position for the remote balls locally
                for (int i = 0; i < numberOfBalls; i++)
                {   
                    Vector3f pos = data.balls[i];
                    Vector3f vel = data.ballsVel[i].mult(0.999f); // decay so the network updates will push forward instead of backwards
                    // Accelerate the velocity 
                    vel.addLocal(gravity);
                    // Add velocity to the ball's position
                    pos.addLocal(vel);
                    
                    // Check for collision
                    if (checkCollisionBallWithHitBox(pos))
                    {
                        postGUILine("OUCH! You got hit by " + users.get(data.userID) + "'s ball!");
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
            
            ////////////////////////////////////////////////////////
            // Send (should be on this character's update thread) //
            ////////////////////////////////////////////////////////
            
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
                    performAnimation(1, true, false, 0); // shake head
                    postGUILine("OUCH! You got hit by your own ball!");
                    hitPoints--;
                    serverProxy.gotHit(ID, i);
                    pos.set(hitBoxPos.add(0.0f, 2.0f + ballRadius, 0.0f));
                    vel.set(0.0f, 0.25f, 0.0f);
                    if (hitPoints <= 0)
                        spawnOutside();
                }
                checkCollisionBallWithHand(character.getLeftArm(), pos, vel);
                checkCollisionBallWithHand(character.getRightArm(), pos, vel);
            }
            
            // Send the new data to the server
            for (int i = 0; i < numberOfBalls; i++)
            {
                ballBosX[i] = balls[i].x;
                ballPosY[i] = balls[i].y;
                ballPosZ[i] = balls[i].z;
                ballVelX[i] = ballsVel[i].x;
                ballVelY[i] = ballsVel[i].y;
                ballVelZ[i] = ballsVel[i].z;
            }
            serverProxy.updateBalls(ballBosX, ballPosY, ballPosZ, ballVelX, ballVelY, ballVelZ);
            
            
                
            // Updates messages occure at a fixed interval
            clientTimer += deltaTime;
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
    
    private void spawnOutside() 
    {
        postGUILine("You are out of the game!");
        
        PMatrix local = character.getController().getModelInstance().getTransform().getLocalMatrix(true);
        Vector3f out = local.getTranslation().subtract(Vector3f.ZERO).normalize();
        out.multLocal(10.0f);
        local.setTranslation(out);
        
        character.makeFist(false, false);
        character.getRightArm().setEnabled(false);
        character.getLeftArm().setEnabled(false);
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

            Vector3f ballTransferXVel = normal.mult(normal.dot(handVel) + 0.15f);
            Vector3f handTransferXvel = normal.mult(normal.dot(vel));
            Vector3f ballYVel = vel.subtract(handTransferXvel);

            vel.set(ballTransferXVel.add(ballYVel));
            return true;
        }
        return false;
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
//            steering.addTaskToBottom(new GoLook(pos, dir, (NinjaContext)context));
    }
    
    private void updateUserArm(VerletArm arm, Vector3f pos) 
    {
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
            Character user = characterData.get(userID).user;
            if (pressed)
                user.getContext().triggerPressed(trigger);
            else
                user.getContext().triggerReleased(trigger);
        }
    }

    @Override
    public void listPlayers(int [] playerIDs, String [] playerNames, boolean [] male, int [] feet, int [] legs, int [] torso, int [] hair) 
    {
        for(int i = 0; i < playerIDs.length; i++)
        {
            postGUILine("Listing Player with ID: " + playerIDs[i] + " called: " + playerNames[i] + " feet: " + feet[i] + " legs: " + legs[i] + " torso: " + torso[i] + " hair: " + hair[i] + " male: " + male[i]);
            users.put(playerIDs[i], playerNames[i]);

            Character user;
            if (male[i])
            {
                user = new NinjaAvatar(new NinjaAvatarAttributes(playerNames[i], feet[i], legs[i], torso[i], hair[i], 0), worldManager);
                user.setBigHeadMode(2.0f);
            }
            else
                user = new NinjaAvatar(new NinjaFemaleAvatarAttributes(playerNames[i], feet[i], legs[i], torso[i], hair[i], 0), worldManager);
            
            UserData data = new UserData(user, playerIDs[i]);
            characterData.put(playerIDs[i], data);
            user.getController().addCharacterMotionListener(data);
            gui.addPlayerToBoards(playerNames[i], 3, 0, 0);
            // Test
            //vis.addPositionObject(data.desiredPosition, ColorRGBA.black);
            //vis.addPositionObject(data.currentPosition, ColorRGBA.white);
        }
    }

    @Override
    public void addPlayer(int userID, String playerName, boolean male, int feet, int legs, int torso, int hair) 
    {
        postGUILine("Adding Player with ID: " + userID + " called: " + playerName + " feet: " + feet + " legs: " + legs + " torso: " + torso + " hair: " + hair + " male: " + male);
        users.put(userID, playerName);

        Character user;
        if (male)
        {
            user = new NinjaAvatar(new NinjaAvatarAttributes(playerName, feet, legs, torso, hair, 0), worldManager);
            user.setBigHeadMode(2.0f);
        }
        else
            user = new NinjaAvatar(new NinjaFemaleAvatarAttributes(playerName, feet, legs, torso, hair, 0), worldManager);
        
        UserData data = new UserData(user, userID);
        characterData.put(userID, data);
        user.getController().addCharacterMotionListener(data);
        gui.addPlayerToBoards(playerName, 0, 0, 0);
    }

    @Override
    public void removePlayer(int userID) 
    {
        postGUILine("Removing Player with ID: " + userID + " called: " + users.get(userID));
        gui.removePlayerFromBoards(users.get(userID));
        users.remove(userID);

        characterData.get(userID).user.die();
        characterData.get(userID).removeBalls();
        characterData.remove(userID);
    }
    
    protected class ClientContextListener implements GameContextListener
    {
        DarkstarClient client = null;

        public ClientContextListener(DarkstarClient client)
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
        
        public Vector3f[] balls    = new Vector3f[numberOfBalls];
        public Vector3f[] ballsVel = new Vector3f[numberOfBalls];

        public UserData(Character user, int userID) 
        {
            this.user = user;    
            this.userID = userID;
            for (int i = 0; i < numberOfBalls; i++)
            {
                balls[i]    = new Vector3f(10000.0f, 0.0f, 0.0f);
                ballsVel[i] = new Vector3f();
                vis.addPositionObject(balls[i], ColorRGBA.red, ballRadius);
            }
        }
        
        public void removeBalls()
        {
            for (int i = 0; i < numberOfBalls; i++)
            {
                balls[i].set(10000.0f, 0.0f, 0.0f); // hehe
                vis.removePositionObject(balls[i]);
            }
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
    }

    public String getRandomName()
    {
        String player;
        int r = new Random().nextInt(100);
        switch(r)
        {
            case 0:
                player = "Scrafy";
            case 1:
                player = "Co co";
            case 2:
                player = "Berta";
            case 3:
                player = "Mika";
            case 4:
                player = "Potter";
            case 5:
                player = "La Capitan";
            case 6:
                player = "Munchy";
            case 7:
                player = "Poo";
            case 8:
                player = "Fritz";
            case 9:
                player = "Bongo";
            case 10:
                player = "Bozo";
            case 11:
                player = "Goofy";
            case 12:
                player = "Lilo";
            case 13:
                player = "Sacha";
            case 14:
                player = "Lightning";
            case 15:
                player = "Thunder";
            case 16:
                player = "Super Person";
            case 17:
                player = "Dirty Monkey";
            case 18:
                player = "Holmes";
            case 19:
                player = "McBowser";
            case 20:
                player = "Dr. You";
            case 21:
                player = "GuitarMan15";
            case 22:
                player = "Drummer";
            case 23:
                player = "JazzMan";
            case 24:
                player = "DemoBoy";
            case 25:
                player = "CodeMaster";
            case 26:
                player = "N00b";
            case 27:
                player = "Mr. Pro";
            case 28:
                player = "Number 28";
            case 29:
                player = "Bitman";
            case 30:
                player = "p4wn ur4zz";
            case 31:
                player = "l33t";
            case 32:
                player = "RastaDude";
            case 33:
                player = "WhyMe";
            case 34:
                player = "WhyHim";
            case 35:
                player = "WhyThem";
            case 36:
                player = "Lou";
            case 37:
                player = "Supreme Commander";
            case 38:
                player = "Master Chief";
            case 39:
                player = "Gordon";
            case 40:
                player = "Ninja";
            case 41:
                player = "Sir Bob";
            case 42:
                player = "Peach";
            case 43:
                player = "Dan";
            case 44:
                player = "Bela";
            case 45:
                player = "GuyBrush";
            case 46:
                player = "TomatoFace";
            case 47:
                player = "Perkings";
            case 48:
                player = "Ceaser";
            case 49:
                player = "Fishy the fish";
            case 50:
                player = "Boogieman";
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
}
