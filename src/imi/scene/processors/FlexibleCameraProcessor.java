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
package imi.scene.processors;

import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import imi.collision.TransformUpdateManager;
import imi.scene.PMatrix;
import imi.scene.camera.behaviors.CameraModel;
import imi.scene.camera.behaviors.WrongStateTypeException;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.CameraPositionManager;
import imi.scene.camera.behaviors.ChaseCamModel;
import imi.utils.input.InputScheme;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLException;
import javolution.util.FastTable;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.AwtEventCondition;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;

/**
 * This processor allows for manipulation of a camera node with
 * multiple states or models.
 * @author Ronald E Dahlgren
 */
public class FlexibleCameraProcessor extends AWTEventProcessorComponent
{
    /** Transform that this process manipulates **/
    private PMatrix m_transform = new PMatrix();

    /** The camera node to manipulate **/
    private Node    m_jmeCameraNode   = null;
    /** The skybox (must follow the camera) **/
    private Node    m_skyNode   = null;
    
    private WorldManager m_WM   = null;
    
    private ProcessorArmingCollection m_armingConditions = null;
    
    /** Collection of states for use with the current model **/
    private final FastTable<CameraState> m_stateCollection = new FastTable<CameraState>();
    /** Cursor to indicate the current state **/
    private Integer currentStateIndex = 0;
    /** Behavior model currently in use **/
    private CameraModel m_model = null;
    /** Control system **/
    private InputScheme avatarControl = null;
    /** Window width and height **/
    private int windowWidth  = 0;
    private int windowHeight = 0;

    /** Used for calculating delta time values **/
    private double oldTime = 0.0;
    private double deltaTime = 0.0;

    /** for snap shots **/
    private int picNum = 0;
    private boolean takeSnap = false;
    private static File screenShotFolder = new File(System.getProperty("user.home") + "/ScreenShots/");
    private final RenderUpdater screenShotter = new RenderUpdater() {
        @Override
        public void update(Object arg0) {
            if (takeSnap)
            {
                takeSnap = false;

                File file = new File(screenShotFolder, "pic" + picNum + ".jpg");
                System.out.println("Taking screen shot " + file.getName());
                try {
                    if (file.mkdirs() || file.exists())
                        com.sun.opengl.util.Screenshot.writeToFile(file, 800, 600);
                } catch (IOException ex) {
                    Logger.getLogger(FlexibleCameraProcessor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GLException ex) {
                    Logger.getLogger(FlexibleCameraProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
                picNum++;
                System.out.println("Done taking screen shot " + file.getPath());
            }
        }
    };
    
    /**
     * Constructs a new flexible camera processor with the provided goodies.
     * @param listener This component receives input events from AWT
     * @param cameraNode The camera node to drive around
     * @param wm The world manager
     * @param myEntity The entity that this processor will associate itself with
     * @param skyboxNode The skybox, will be a child of the camera node.
     */
    public FlexibleCameraProcessor(AWTInputComponent listener,
                                                Node cameraNode,
                                                WorldManager wm,
                                                Entity myEntity,
                                                Node skyboxNode,
                                                int width,
                                                int height)
    {
        super(listener);
        setEntity(myEntity);
        setRunInRenderer(true);

        windowWidth     = width;
        windowHeight    = height;
        m_jmeCameraNode = cameraNode;
        m_WM            = wm;
        m_skyNode       = skyboxNode;
        
        m_armingConditions = new ProcessorArmingCollection(this);
        //m_armingConditions.addCondition(new AwtEventCondition(this));
        m_armingConditions.addCondition(new NewFrameCondition(this));

        // Set the picNum for snap shots
        if (screenShotFolder.mkdirs() || screenShotFolder.exists())
        {
            File [] files = screenShotFolder.listFiles();
            for(File f : files)
            {
                if (f.getName().startsWith("pic"))
                {
                    String name = f.getName();
                    int start = name.indexOf("pic") + 3;
                    int end   = name.indexOf(".");
                    String number = name.substring(start, end);
                    //System.out.println("The number is: " + number);
                    int n = 0;
                    if (number.equals("") == false)
                        n = Integer.parseInt(number);
                    if (picNum < n)
                        picNum = n+1;
                }
            }
        }
    }

    @Override
    public void initialize()
    {
        setArmingCondition(m_armingConditions);
    }

    Quaternion rot = new Quaternion();

    @Override
    public void compute(ProcessorArmingCollection arg0)
    {
        computeCode();
        if (takeSnap)
            m_WM.addRenderUpdater(screenShotter, null);
    }

    public void computeCode()
    {
        double newTime = System.nanoTime() / 1000000000.0;
        deltaTime = (newTime - oldTime);
        oldTime = newTime;

        if (m_model != null)
        {
            try
            {
                Object [] events = getEvents();
                checkForStateChangeEvents(events);
                if (avatarControl != null)
                    avatarControl.processMouseEvents(events);

                if (m_stateCollection.isEmpty() == false && currentStateIndex >= 0)
                {
                    triggerUpdates((float)deltaTime);
                    m_model.handleInputEvents(m_stateCollection.get(currentStateIndex), events);
                    m_model.update(m_stateCollection.get(currentStateIndex), (float)deltaTime);
                    m_model.determineTransform(m_stateCollection.get(currentStateIndex), m_transform);
                    if (m_model instanceof ChaseCamModel)
                        ((ChaseCamModel)m_model).getRotation(rot);
                }
            } catch (WrongStateTypeException ex)
            {
                Logger.getLogger(FlexibleCameraProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void commit(ProcessorArmingCollection arg0)
    {
        if (m_model instanceof ChaseCamModel)
        {
            m_jmeCameraNode.setLocalRotation(rot);
            m_jmeCameraNode.setLocalTranslation(m_transform.getTranslation());
        }
        else
        {
            m_jmeCameraNode.setLocalRotation(m_transform.getRotation());
            m_jmeCameraNode.setLocalTranslation(m_transform.getTranslation());
        }
        
        if (m_skyNode != null)
        {
            m_skyNode.setLocalTranslation(m_transform.getTranslation());
            m_WM.addToUpdateList(m_skyNode);
        }
        m_jmeCameraNode.updateGeometricState((float) deltaTime, true);
        m_jmeCameraNode.updateRenderState();
        //m_WM.addToUpdateList(m_jmeCameraNode);
    }
    
    /**
     * Set the current state to the next state in the collection, this method
     * will roll over to the first state if already at the end.
     */
    public void nextState()
    {
        currentStateIndex++;
        currentStateIndex %= m_stateCollection.size();
    }

    /**
     * This method will set the current state to the previous state in the
     * collection. It will roll over to the last state if the current state
     * is the first state.
     */
    public void previousState()
    {
        currentStateIndex--;
        if (currentStateIndex < 0)
            currentStateIndex = m_stateCollection.size() - 1;
    }

    /**
     * Make the current state the one indicated by the provided index, if
     * it is valid.
     * @param index
     */
    public void setCurrentState(int index)
    {
        if (index < 0 || index >= m_stateCollection.size())
            return;
        else
            currentStateIndex = index;
    }

    public void takeSnap() {
        if (!takeSnap)
            takeSnap = true;
        dumpTransform();
    }

    public void dumpTransform() {
        System.out.println(getState().getCameraTransform());
    }

    public void setSkyNode(Node skyNode)
    {
        this.m_skyNode = skyNode;
    }
    /////////////////////////////////////////////////////////////////
    ///////////// Standard Collection Encapsulation /////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Retrieve the camera state in the collection at the specified index.
     * @param index
     * @return The state, or null if the index is invalid
     */
    public CameraState getState(int index)
    {
        if (index < 0 || index >= m_stateCollection.size())
            return null;
        else
            return m_stateCollection.get(index);
    }

    /**
     * Add the provided state to the collection of available states for this
     * model.
     * @param state The state to add
     * @return The index of the state in the collection
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public int addState(CameraState state) throws WrongStateTypeException
    {
        if (m_model.getRequiredStateType() != state.getType())
            throw new WrongStateTypeException("Wrong state type provided for add.");
        m_stateCollection.add(state);
        return m_stateCollection.size() - 1;
    }

    /**
     * Clear the internal collection of camera states.
     */
    public void clearStateCollection()
    {
        m_stateCollection.clear();
        currentStateIndex = -1;
    }

    /**
     * Set the state at the specified index to the provided state. If the index
     * is not valid for the current collection of states, no assignment is made.
     * If the state type is not correct for the current model, a WrongStateTypeException
     * is throw.
     * @param index The index to set
     * @param state The new state (Must be compatible with the current behavior model!)
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void setState(int index, CameraState state) throws WrongStateTypeException
    {
        if (index < 0 || index > m_stateCollection.size())
            return;
        if (state.getType() != m_model.getRequiredStateType())
            throw new WrongStateTypeException("Incorrect state type provided");
        // Otherwise, we can actually set the state.
        m_stateCollection.set(index, state);
    }

    ////////////////////////////////////////////////////////////
    ////////// DUNGEON - Getters, Setters, Etc /////////////////
    ////////////////////////////////////////////////////////////

    /**
     * Set the control scheme
     * @param control
     */
    public void setControl(InputScheme control)
    {
        avatarControl = control;
    }

    /**
     * Sets the model and default state for the camera processor. This method
     * will also clear out the existing list of states. This is because it should
     * not be assumed that the old states are compatible with the new model
     * @param newModel The new behavior model to use
     * @param newState The new state to use
     */
    public void setCameraBehavior(CameraModel newModel, CameraState newState)
    {
        m_model = newModel;
        clearStateCollection();
        m_stateCollection.add(newState);
        currentStateIndex = 0;
    }

    /**
     * Retrieve the current behavior model
     * @return
     */
    public CameraModel getModel()
    {
        return m_model;
    }

    /**
     * Retrieve the current camera state.
     * @return
     */
    public CameraState getState()
    {
        return m_stateCollection.get(currentStateIndex);
    }

    /**
     * Set the current camera state
     * @param stateToMatchModel
     */
    public void setState(CameraState stateToMatchModel)
    {
        m_stateCollection.set(currentStateIndex, stateToMatchModel);
    }

    public PMatrix getTransform()
    {
        return m_transform;
    }

    private void checkForStateChangeEvents(Object[] events)
    {
        for (Object obj : events)
        {
            if (obj instanceof KeyEvent)
            {
                KeyEvent ke = (KeyEvent)obj;
                if (ke.getID() != KeyEvent.KEY_RELEASED)
                    continue; // Only interested in key up events
                if (ke.getKeyCode() == KeyEvent.VK_C)
                {
                    previousState();
                    break;
                }
                else if (ke.getKeyCode() == KeyEvent.VK_V)
                {
                    nextState();
                    break;
                }
            }
        }
    }
    public void setCameraPosition(String name)
    {
        CameraState state = m_stateCollection.get(currentStateIndex);
        PMatrix transformBuffer = new PMatrix();
        CameraPositionManager.instance().getCameraTransform(name, transformBuffer);
        state.setCameraTransform(transformBuffer);
    }
    public void getCameraPosition(Vector3f output)
    {
        //CameraPositionManager.instance().getCameraPosition(currentStateIndex, output); doesn't work

        CameraState state = m_stateCollection.get(currentStateIndex);
        PMatrix mat = new PMatrix();
        try {
            m_model.determineTransform(state, mat);
        } catch (WrongStateTypeException ex) {
            Logger.getLogger(FlexibleCameraProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        output.set(mat.getTranslation());
    }

    protected Camera getCamera()
    {
        CameraComponent cc = (CameraComponent) getEntity().getComponent(CameraComponent.class);
        if (cc != null)
            return cc.getCameraNode().getCamera();
        return null;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void getWorldSpaceRay(int mouseX, int mouseY, Vector3f origin, Vector3f direction)
    {
        Camera camera = getCamera();

        Vector2f sc = new Vector2f(mouseX, windowHeight - mouseY);
        camera.getWorldCoordinates(sc, 0.0f, origin);
        camera.getWorldCoordinates(sc, 1.0f, direction);
        direction.set(direction.subtract(origin));
        direction.normalizeLocal();

//        System.out.println("Ray starts at: " + origin.x + ", " + origin.y + ", " + origin.z);
//        System.out.println("And has direction: " + direction.x + ", " + direction.y + ", " + direction.z);
    }

    private void triggerUpdates(float deltaTime)
    {
        TransformUpdateManager tum = (TransformUpdateManager) m_WM.getUserData(TransformUpdateManager.class);
        if (tum != null)
            tum.update(deltaTime);
    }
}
