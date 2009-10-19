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

package imi.input;


import com.jme.math.Vector3f;
import imi.camera.FlexibleCameraProcessor;
import imi.character.VerletArm;
import imi.character.Character;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.gui.TreeExplorer;
import imi.objects.AvatarObjectCollection;
import imi.objects.ObjectCollectionBase;
import imi.scene.JScene;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.scene.utils.visualizations.InternalRendererEntity;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javolution.util.FastTable;
import org.jdesktop.mtgame.WorldManager;

/**
 * The default input scheme for the avatar system demonstrations.
 * @author Lou Hayt
 */
public class DefaultCharacterControls implements CharacterControls
{
    protected ObjectCollectionBase objectsBase    = null;
    protected WorldManager        wm                  = null;
    protected Character           character           = null;
    protected JScene              jscene              = null; // Some of the GUI tools are using jscenes.... this maintains compatability
    protected int                 selectedCharacter   = 0;
    protected FastTable<Character> characterTeam      = new FastTable<Character>();
    protected InputState          inputState          = new InputState();
    protected boolean     bCommandEntireTeam  = false;
    protected boolean     mouseDown           = false;
    protected int         currentMouseX = 0;
    protected int         currentMouseY = 0;
    protected int         lastMouseX    = 0;
    protected int         lastMouseY    = 0;
    boolean debugVertexNormalsEnabled   = false;

    /**
     * This will add this class to the WorldManager user data under CharacterControls.class
     * @param worldManager
     */
    public DefaultCharacterControls(WorldManager worldManager) {
        wm = worldManager;
        wm.addUserData(CharacterControls.class, this);
    }

    /**
     * {@inheritDoc InputClient}
     */
    public void processKeyEvent(KeyEvent ke)
    {
        // Store key state and tunnel input to avatars
        if (ke.getID() == KeyEvent.KEY_RELEASED)
        {
            // Alter the input state for random reference
            inputState.keyReleased(ke.getKeyCode());

            // Affect character actions
            if(bCommandEntireTeam)
            {
                for (int i = 0; i < characterTeam.size(); i++)
                    characterTeam.get(i).keyReleased(ke.getKeyCode());
            }
            else if (character != null)
                character.keyReleased(ke.getKeyCode());
        }
        else if (ke.getID() == KeyEvent.KEY_PRESSED)
        {
            // Alter the input state for random reference
            inputState.keyPressed(ke.getKeyCode());

            // Affect character actions
            if(bCommandEntireTeam)
            {
                for (int i = 0; i < characterTeam.size(); i++)
                    characterTeam.get(i).keyPressed(ke.getKeyCode());
            }
            else if (character != null)
                character.keyPressed(ke.getKeyCode());
        }
        // Utilities
//        processUtilityKeys(ke);
    }

    /**
     * Get the current jscene (independant or belongs to a character)
     * @return
     */
    public JScene getJscene() {
        return jscene;
    }

    /**
     * Set the current JScene to recieve utility input (wireframe on\off etc)
     * @param jscene
     */
    public void setJScene(JScene jscene) {
        this.jscene = jscene;
    }

    /**
     * Process general application input
     * @param ke
     */
    public void processUtilityKeys(KeyEvent ke)
    {
        if (ke.getID() == KeyEvent.KEY_PRESSED)
        {
            // Select the next character in the team
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP)
                controlNextCharacter();
            
            // Select the previous character in the team
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
                controlPreviousCharacter();

            // Remove a random chair from the object collection
            if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)
            {
                if(objectsBase != null && objectsBase instanceof AvatarObjectCollection)
                    ((AvatarObjectCollection)objectsBase).removeAChair();
            }

            // Take snap shot picture
            if (ke.getKeyCode() == KeyEvent.VK_P)
            {
                FlexibleCameraProcessor p = (FlexibleCameraProcessor) wm.getUserData(FlexibleCameraProcessor.class);
                if (p != null)
                    p.takeSnap();
                else
                    Logger.getLogger(DefaultCharacterControls.class.getName()).warning("No camera processor in the World Manager!");
            }

            // Shut the character's eyes
            if (ke.getKeyCode() == KeyEvent.VK_I)
            {
                if (character != null)
                    character.shutEyes(!character.isEyesShut());
            }

            // Open PNode explorer
            if (ke.getKeyCode() == KeyEvent.VK_N)
            {
                if (character != null)
                {
                    // Construct a tree explorer for analyzing the scene graph
                    TreeExplorer te = new TreeExplorer();
                    te.setExplorer(wm, character.getPScene());
                    te.setVisible(true);
                }
            }
            
            // Open JMonkey Scene Monitor
            if (ke.getKeyCode() == KeyEvent.VK_M)
            {
                if (character != null)
                {
//                    SceneMonitor.getMonitor().registerNode(character.getJScene(), "Character:" + character.getName());
//                    SceneMonitor.getMonitor().showViewer(true);
                }
            }

            // JMonkey Wireframe (on\off)
            if (ke.getKeyCode() == KeyEvent.VK_T)
            {
                if (jscene != null)
                    jscene.toggleWireframe();
                if (bCommandEntireTeam)
                {
                    for (int i = 0; i < characterTeam.size(); i++)
                    {
                        if (characterTeam.get(i).getJScene() != jscene)
                            characterTeam.get(i).getJScene().toggleWireframe();
                    }
                }
            }

            // Render Vertex normals
            if (ke.getKeyCode() == KeyEvent.VK_V)
            {
                InternalRendererEntity render = getDebugRenderer();
                if (render != null)
                {
                    debugVertexNormalsEnabled = !debugVertexNormalsEnabled;

                    if (jscene != null)
                    {
                        if (debugVertexNormalsEnabled)
                           render.addNSpatial(jscene);
                        else
                            render.removeNSpatial(jscene);
                    }

                    if (bCommandEntireTeam)
                    {
                        for (int i = 0; i < characterTeam.size(); i++)
                        {
                            if (jscene != characterTeam.get(i).getJScene())
                            {
                                if (debugVertexNormalsEnabled)
                                   render.addNSpatial(characterTeam.get(i).getJScene());
                                else
                                    render.removeNSpatial(characterTeam.get(i).getJScene());
                            }
                        }
                    }
                }
            }

            // Rendering mode (JMonkey, JMonkey and Debug, Debug)
            if (ke.getKeyCode() == KeyEvent.VK_R)
            {
                if (jscene != null)
                    jscene.debugRenderToggle();
                if (bCommandEntireTeam)
                {
                    for (int i = 0; i < characterTeam.size(); i++)
                    {
                        if (jscene != characterTeam.get(i).getJScene())
                            characterTeam.get(i).getJScene().debugRenderToggle();
                    }
                }
            }

            // PRenderer Bounding volumes (off, box, sphere)
            if (ke.getKeyCode() == KeyEvent.VK_B)
            {
                if (jscene != null)
                    jscene.setBoundingSphereDrawEnabled(!jscene.isBoundingSphereDrawEnabled());
                if (bCommandEntireTeam)
                {
                    for (int i = 0; i < characterTeam.size(); i++)
                    {
                        if (jscene != characterTeam.get(i).getJScene())
                            characterTeam.get(i).getJScene().setBoundingSphereDrawEnabled(!characterTeam.get(i).getJScene().isBoundingSphereDrawEnabled());
                    }
                }
            }
        }
    }

    Vector3f offset = new Vector3f();
    /**
     * {@inheritDoc InputClient}
     */
    public void processMouseEvent(MouseEvent me)
    {
        // Verlet arm mouse input
        if (character != null && character.getSkeletonManipulator() != null && character.getSkeletonManipulator().isArmsEnabled())
        {
            offset.set(0.0f, 0.0f, 0.0f);
            if (me.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isRightMouseButton(me))
            {
                // Mouse pressed, reset initial settings
                currentMouseX = me.getX();
                currentMouseY = me.getY();
                lastMouseX    = me.getX();
                lastMouseY    = me.getY();
                mouseDown = !mouseDown;
            }

            if (mouseDown)//me.getID() == MouseEvent.MOUSE_DRAGGED)
            {
                // Set the current
                currentMouseX = me.getX();
                currentMouseY = me.getY();

                // Calculate delta
                int deltaX = currentMouseX - lastMouseX;
                int deltaY = currentMouseY - lastMouseY;

                // Translate to input offset
                offset.x = deltaX * -0.0075f;
                offset.z = deltaY * -0.0075f;

                // Set the last
                lastMouseX    = me.getX();
                lastMouseY    = me.getY();
            }

            if (me.getID() == MouseEvent.MOUSE_WHEEL)
            {
                if (me instanceof MouseWheelEvent)
                {
                    int scroll = ((MouseWheelEvent)me).getWheelRotation();
                    offset.y   = scroll * -0.05f;
                }
            }

            VerletArm rightArm = character.getRightArm();
            VerletArm leftArm  = character.getLeftArm();

            if (rightArm != null)
            {
                if (me.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isMiddleMouseButton(me))
                {
                    character.getContext().triggerPressed(TriggerNames.ToggleRightArmManualDriveReachMode.ordinal());
                    character.getContext().triggerReleased(TriggerNames.ToggleRightArmManualDriveReachMode.ordinal());
                }

                rightArm.addInputOffset(offset);
            }
            if (leftArm != null)
            {
                if (me.getID() == MouseEvent.MOUSE_PRESSED &&SwingUtilities.isMiddleMouseButton(me))
                {
                    character.getContext().triggerPressed(TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal());
                    character.getContext().triggerReleased(TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal());
                }

                leftArm.addInputOffset(offset);
            }
        }
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized void setCharacter(Character character) {
        Character previouslySelected = this.character;
        this.character = character;
        jscene = character.getJScene();
        if (!characterTeam.contains(character))
            characterTeam.add(character);
        selectedCharacter = characterTeam.indexOf(character);
        characterSelected(character, previouslySelected);
    }

    /**
     * Get the debug rendering entity
     * @return
     */
    public synchronized InternalRendererEntity getDebugRenderer()
    {
        InternalRendererEntity ir = (InternalRendererEntity) wm.getUserData(InternalRendererEntity.class);
        if (ir == null)
            ir = new InternalRendererEntity(wm);
        return ir;
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public boolean isCommandingEntireTeam() {
        return bCommandEntireTeam;
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public void setCommandEntireTeam(boolean bCommandEntireTeam) {
        this.bCommandEntireTeam = bCommandEntireTeam;
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized void clearCharacterTeam() {
        characterTeam.clear();
        character = null;
        jscene = null;
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized void addCharacterToTeam(Character character) {
        if (!characterTeam.contains(character))
        {
            characterTeam.add(character);
            if (this.character == null)
            {
               this.character = character;
               jscene = character.getJScene();
               characterSelected(character, null);
            }
        }
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized void removeCharacterFromTeam(Character characterToRemove) {
        characterTeam.remove(characterToRemove);
        if (character == characterToRemove)
        {
            if (characterTeam.isEmpty())
            {
                character = null;
                jscene = null;
            }
            else
                controlNextCharacter();
        }
        if (character != null)
        {
            selectedCharacter = characterTeam.indexOf(character);
            jscene = character.getJScene();
        }
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized void controlNextCharacter()
    {
        if (!characterTeam.isEmpty())
            offsetSelectedCharacter(1);
        else
        {
            jscene = null;
            character = null;
        }
    }


    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized void controlPreviousCharacter()
    {
        if (!characterTeam.isEmpty())
            offsetSelectedCharacter(-1);
        else
        {
            jscene = null;
            character = null;
        }
    }

    private void offsetSelectedCharacter(int offset) {
        // cache the previously selected guy
        Character previouslySelected = characterTeam.get(selectedCharacter);
        // Change selected index and perform bounds checking
        selectedCharacter += offset;
        if (selectedCharacter > characterTeam.size()-1)
                selectedCharacter = 0;
        else if (selectedCharacter < 0)
            selectedCharacter = characterTeam.size()-1;
        // Select the new guy for input and make him smile
        character = characterTeam.get(selectedCharacter);
        character.selectForInput();
        character.initiateFacialAnimation(1, 0.25f, 3.0f);
        jscene = character.getJScene();
        characterSelected(character, previouslySelected);
    }

    /**
     * Removes this scene from being selected and if a character in the team
     * is found to own it that character will be removed from the team.
     * @param Rjscene
     */
    public synchronized void removeJScene(JScene Rjscene)
    {
        if (jscene == Rjscene)
        {
            removeCharacterFromTeam(character);
            return;
        }
        for (int i = 0; i < characterTeam.size(); i++)
        {
            if (characterTeam.get(i).getJScene() == Rjscene)
            {
                removeCharacterFromTeam(character);
                return;
            }
        }
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public synchronized Character getCurrentlySelectedCharacter() {
        return character;
    }

    /**
     * When the mouse movement is activated the verlet arms will move with it,
     * this usually activates by right clicking.
     */
    public void activateMouseMovement() {
        mouseDown = true;
    }

    /**
     * {@inheritDoc CharacterControls}
     */
    public void setObjectCollection(ObjectCollectionBase objectCollection) {
        objectsBase = objectCollection;
    }

    /**
     * Perform actions based on the characterSelected event
     * @param selected
     */
    protected void characterSelected(Character selected, Character previouslySelected) {
        // override friendly
    }

    /**
     * {@inheritDoc InputClient}
     */
    public void focusChanged(boolean currentlyInFocus) {
       if (character != null && character instanceof Avatar)
           ((Avatar)character).stop();
       else
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Focus changed!");

    }

}
