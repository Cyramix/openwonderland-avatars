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
package imi.camera;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import imi.collision.TransformUpdateManager;
import imi.input.InputClient;
import imi.input.InputManagerEntity;
import imi.scene.PMatrix;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLException;
import javolution.util.FastTable;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;

/**
 * This processor allows for manipulation of a camera node with
 * multiple states or models.
 *
 * <p>
 *  The processor can be provided any implementation pair of {@code CameraModel}
 * and {@code AbstractCameraState} that the user desires, provided that the model
 * is capable of operating on the state. In the event that the wrong type of state
 * is provided, an {@code IllegalArgumentException} is thrown. Methods that do
 * this are indicated with the appropriate throws tag in the Java Doc.
 * </p>
 * @author Ronald E Dahlgren
 */
public final class FlexibleCameraProcessor extends ProcessorComponent implements InputClient
{
    /** Transform that this process manipulates **/
    private final PMatrix m_transform = new PMatrix();
    /** The camera node to manipulate **/
    private final Node    m_jmeCameraNode;
    /** The skybox (must follow the camera) **/
    private Node    m_skyNode   = null;

    /** World manager ref **/
    private final WorldManager m_WM;

    private final ProcessorArmingCollection m_armingConditions;

    /** Collection of states for use with the current model **/
    private final List<AbstractCameraState> m_stateCollection = new FastTable<AbstractCameraState>();
    /** Cursor to indicate the current state **/
    private Integer currentStateIndex = 0;
    /** Behavior model currently in use **/
    private CameraModel m_model = null;

    /** Window panelHeight **/
    private int windowHeight = 0;
    private int windowWidth = 0;

    /** Used for calculating delta time values **/
    private double oldTime = 0.0;
    private double deltaTime = 0.0;

    /** for snap shots **/
    private int picNum = 0;
    private boolean takeSnap = false;
    private static final File screenShotFolder = new File(System.getProperty("user.home") + "/ScreenShots/");

    /** Input Buffer **/
    FastTable<Object> inputEvents = new FastTable<Object>();

    /**
     * This RenderUpdater is used for taking screen shots, as this must be done
     * on the Render Thread.
     */
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
     * @param panelHeight Height of the rendering panel
     * @throws ExceptionInInitializerError If the InputManagerEntity is not found in the WorldManager user data.
     */
    public FlexibleCameraProcessor( Node cameraNode,
                                    WorldManager wm,
                                    Entity myEntity,
                                    Node skyboxNode,
                                    int panelWidth,
                                    int panelHeight)
    {
        super();
        setEntity(myEntity);
        setRunInRenderer(true);
        windowHeight    = panelHeight;
        windowWidth     = panelWidth;
        m_jmeCameraNode = cameraNode;
        m_WM            = wm;
        m_skyNode       = skyboxNode;

        InputManagerEntity ime = (InputManagerEntity)m_WM.getUserData(InputManagerEntity.class);
        if (ime != null)
            ime.addInputClient(this);
        else
            throw new ExceptionInInitializerError("InputManagerEntity is not found!");

        m_armingConditions = new ProcessorArmingCollection(this);
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

    /**
     * {@inheritDoc AWTEventProcessorComponent}
     */
    @Override
    public void initialize()
    {
        setArmingCondition(m_armingConditions);
    }

    /**
     * {@inheritDoc AWTEventProcessorComponent}
     * @param arg0
     */
    @Override
    public void compute(ProcessorArmingCollection arg0)
    {
        updateModel();
        if (takeSnap)
            m_WM.addRenderUpdater(screenShotter, null);
    }

    @Override
    public void commit(ProcessorArmingCollection arg0)
    {
        m_jmeCameraNode.setLocalRotation(m_transform.getRotation());
        m_jmeCameraNode.setLocalTranslation(m_transform.getTranslation());


        if (m_skyNode != null)
        {
            m_skyNode.setLocalTranslation(m_transform.getTranslation());
            m_WM.addToUpdateList(m_skyNode);
        }
        m_jmeCameraNode.updateGeometricState((float) deltaTime, true);
        m_jmeCameraNode.updateRenderState();
    }

    /**
     * Set the current state to the next state in the collection, this method
     * will roll over to the first state if already at the end.
     */
    public synchronized void nextState()
    {
        currentStateIndex++;
        currentStateIndex %= m_stateCollection.size();
    }

    /**
     * This method will set the current state to the previous state in the
     * collection. It will roll over to the last state if the current state
     * is the first state.
     */
    public synchronized void previousState()
    {
        currentStateIndex--;
        if (currentStateIndex < 0)
            currentStateIndex = m_stateCollection.size() - 1;
    }

    /**
     * Make the current state the one indicated by the provided index, if
     * it is valid.
     * @param index
     * @throws IndexOutOfBoundsException If the provided index is invalid
     */
    public void setCurrentState(int index)
    {
        if (index < 0 || index >= m_stateCollection.size())
            throw new IndexOutOfBoundsException("index: " + index + ", collection size: " + m_stateCollection.size());
        else
            currentStateIndex = index;
    }

    /**
     * Takes a screen capture of the camera's current view and saves it to the
     * screen shot folder. This method also outputs the camera's current transform
     * to the console.
     */
    public void takeSnap() {
        if (!takeSnap)
            takeSnap = true;
        dumpTransform();
    }

    /**
     * Output the camera's current transform to the console.
     */
    public void dumpTransform() {
        System.out.println(getState().getCameraTransform());
    }

    /**
     * Set the jME Node that should be used as a root for skybox geometry.
     * @param skyNode
     */
    public void setSkyNode(Node skyNode)
    {
        this.m_skyNode = skyNode;
    }

    private void updateModel()
    {
        double newTime = System.nanoTime() / 1000000000.0;
        deltaTime = (newTime - oldTime);
        oldTime = newTime;

        if (m_model != null)
        {
            if (m_stateCollection.isEmpty() == false && currentStateIndex >= 0)
            {
                triggerUpdates((float)deltaTime);
                synchronized(this)
                {
                    m_model.handleInputEvents(m_stateCollection.get(currentStateIndex), inputEvents.toArray());
                    inputEvents.clear();
                }
                m_model.update(m_stateCollection.get(currentStateIndex), (float)deltaTime);
                m_model.determineTransform(m_stateCollection.get(currentStateIndex), m_transform);
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///////////// Standard Collection Encapsulation /////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Retrieve the camera state in the collection at the specified index.
     * @param index Must be {@code (index > 0 && index < getNumberOfStates())}
     * @return The state
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     */
    public AbstractCameraState getState(int index)
    {
        if (index < 0 || index >= m_stateCollection.size())
            throw new IndexOutOfBoundsException("index: " + index + ", collection size: " + m_stateCollection.size());
        else
            return m_stateCollection.get(index);
    }

    /**
     *
     * Retrieve the number of states this camera is tracking.
     * @return
     */
    public int getNumberOfStates()
    {
        return m_stateCollection.size();
    }

    /**
     * Add the provided state to the collection of available states for this
     * model.
     * @param state The state to add
     * @return The index of the state in the collection
     * @throws IllegalArgumentException If the state type is not compatible with the current model
     */
    public int addState(AbstractCameraState state)
    {
        if (m_model != null && !m_model.isStateClassValid(state.getClass()))
            throw new IllegalArgumentException(state.getClass().getName() +
                                        "is not the correct type for " + m_model.getClass().getName() + "!");
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
     * Set the state at the specified index to the provided state.
     * @param index The index to set
     * @param state The new state (Must be compatible with the current behavior model!)
     * @throws IndexOutOfBoundsException If the index is invalid
     * @throws IllegalArgumentException If the provided state is not compatible with the current model
     */
    public void setState(int index, AbstractCameraState state)
    {
        if (index < 0 || index > m_stateCollection.size())
            throw new IndexOutOfBoundsException("index: " + index + ", collection size: " + m_stateCollection.size());
        if (m_model != null && !m_model.isStateClassValid(state.getClass()))
            throw new IllegalArgumentException(state.getClass().getName() +
                                        "is not the correct type for " + m_model.getClass().getName() + "!");
        // Otherwise, we can actually set the state.
        m_stateCollection.set(index, state);
    }

    private void checkForStateChangeEvents(KeyEvent ke)
    {
        if (ke.getID() != KeyEvent.KEY_RELEASED)
            return; // Only interested in key up events
        if (ke.getKeyCode() == KeyEvent.VK_C)
            previousState();
        else if (ke.getKeyCode() == KeyEvent.VK_V)
            nextState();
    }

    private Camera getCamera()
    {
        Camera result = null;
        CameraComponent cc = (CameraComponent) getEntity().getComponent(CameraComponent.class);
        if (cc != null)
            result = cc.getCameraNode().getCamera();
        return result;
    }


    private void triggerUpdates(float deltaTime)
    {
//        TransformUpdateManager tum = (TransformUpdateManager) m_WM.getUserData(TransformUpdateManager.class);
//        if (tum != null)
//            tum.update(deltaTime);
    }

    ////////////////////////////////////////////////////////////
    ////////// DUNGEON - Getters, Setters, Etc /////////////////
    ////////////////////////////////////////////////////////////


    ////////////////////////////////
    //////// Public API
    ////////////////////////////////

    /**
     * Sets the model and default state for the camera processor. This method
     * will also clear out the existing list of states. This is because it should
     * not be assumed that the old states are compatible with the new model
     * @param newModel The new behavior model to use
     * @param newState The new state to use
     * @throws IllegalArgumentException If the provided state is not compatible with
     * the provided model, or either parameter if null.
     */
    public void setCameraBehavior(CameraModel newModel, AbstractCameraState newState)
    {
        if (newModel == null || !newModel.isStateClassValid(newState.getClass()))
            throw new IllegalArgumentException("Illegal model / state combination provided.");
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
     * @return The current camera state or null if none is present.
     */
    public AbstractCameraState getState()
    {
        AbstractCameraState result = null;
        if (currentStateIndex >= 0)
            result = m_stateCollection.get(currentStateIndex);
        return result;
    }

    /**
     * Set the current camera state.
     *
     * <p>The provided state's class <b>MUST</b> be compatible with the current
     * camera model</p>
     * @param stateToMatchModel The new current camera state
     * @throws IllegalArgumentException If the state type is not valid with the current model
     */
    public void setState(AbstractCameraState stateToMatchModel)
    {
        if (m_model != null && !m_model.isStateClassValid(stateToMatchModel.getClass()))
            throw new IllegalArgumentException(stateToMatchModel.getClass().getName() +
                                        "is not the correct type for " + m_model.getClass().getName() + "!");
        m_stateCollection.set(currentStateIndex, stateToMatchModel);
    }

    /**
     * This method retrieves the camera's current transform
     * @param mOut a non-null storage object
     */
    public void getTransform(PMatrix mOut)
    {
        mOut.set(m_transform);
    }

    /**
     * Sets the camera transform to the corresponding transform mapped to {@code name}
     * in the {@code CameraPositionManager}.
     *
     * <p>If the specified position is not found, nothing changes.</p>
     *
     * @param name The string identifier of the position requested
     */
    public void setCameraPosition(String name)
    {
        AbstractCameraState state = m_stateCollection.get(currentStateIndex);
        PMatrix transformBuffer = new PMatrix();
        CameraPositionManager.getCameraTransform(name, transformBuffer);
        state.setCameraTransform(transformBuffer);
    }

    /**
     * Retrieve the current position of the camera.
     * @param output A non-null storage object
     */
    public void getCameraPosition(Vector3f output)
    {
        m_transform.getTranslation(output);
    }

    /**
     * Get a normalized vector in world space from the mouse position on the screen along
     * the negative Z axis.
     * @param mouseX The mouse X position
     * @param mouseY The mouse Y position
     * @param originOfResult A non-null storage object
     * @param directionOfRay A non-null storage object
     */
    public void getWorldSpaceRay(int mouseX, int mouseY, Vector3f originOfResult, Vector3f directionOfRay)
    {
        Camera camera = getCamera();

        Vector2f sc = new Vector2f(mouseX, windowHeight - mouseY);
        camera.getWorldCoordinates(sc, 0.0f, originOfResult);
        camera.getWorldCoordinates(sc, 1.0f, directionOfRay);
        directionOfRay.set(directionOfRay.subtract(originOfResult));
        directionOfRay.normalizeLocal();

//        System.out.println("Ray starts at: " + originOfResult.x + ", " + originOfResult.y + ", " + originOfResult.z);
//        System.out.println("And has directionOfRay: " + directionOfRay.x + ", " + directionOfRay.y + ", " + directionOfRay.z);
    }

    public synchronized void processKeyEvent(KeyEvent keyEvent) {
        checkForStateChangeEvents(keyEvent);
        inputEvents.add(keyEvent);
    }

    public synchronized void processMouseEvent(MouseEvent mouseEvent) {
        inputEvents.add(mouseEvent);
    }

    public void focusChanged(boolean currentlyInFocus) {
        
    }
}
