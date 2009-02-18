/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.applet;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.scene.PMatrix;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.CircleUtil;
import imi.utils.input.AvatarControlScheme;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class TenAvatarParty_Applet extends AppletTest {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private static final Integer    numberOfAvatars = Integer.valueOf(10);
    private static final String[]   configFiles     = new String[numberOfAvatars];

    static {
        String fileProtocol = "file://localhost" + System.getProperty("user.dir") + "/";
        configFiles[ 0] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 1] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 2] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 3] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 4] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 5] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 6] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 7] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
//        configFiles[ 8] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
//        configFiles[ 9] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
    }

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    
    // TODO overwrite start(), stop() and destroy() methods
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        //Execute a job on the event-dispatching thread:
        //destroying this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    remove(getContentPane());
                }
            });
        } catch (Exception e) { }
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////


    @Override
    public void createDemoEntities(WorldManager wm)
    {
        CircleUtil circle = new CircleUtil(numberOfAvatars, 4);
        Vector2f[] displacements = circle.calculatePoints();

        // Create avatar input scheme
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));

        // Create testCharacter
        Avatar testCharacter = null;
        float totalLoadTime = 0.0f;
        for (int i = 0; i < numberOfAvatars; ++i)
        {
            try {
                long startTime = System.nanoTime();
                if (Math.random() < 0.5)
                    testCharacter = new Avatar(new MaleAvatarAttributes("Name", true), wm);//new avatarAvatar(new URL(configFiles[i]), wm);
                else
                    testCharacter = new Avatar(new FemaleAvatarAttributes("Name", true), wm);//new avatarAvatar(new URL(configFiles[i]), wm);
                long stopTime = System.nanoTime();
                float length = (stopTime - startTime) / 1000000000.0f;
                totalLoadTime += length;
                System.out.println("Loading avatar " + i + " took " + length + " seconds.");

            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
            if (testCharacter != null)
            {
                testCharacter.selectForInput();
                PMatrix localXForm = testCharacter.getModelInst().getTransform().getLocalMatrix(true);
                localXForm.setTranslation(new Vector3f(
                                        displacements[i].x,
                                        0,
                                        displacements[i].y));
                control.getAvatarTeam().add(testCharacter);
                control.getMouseEventsFromCamera();
            }
        }
        System.out.println("Took " + totalLoadTime + " seconds overall to load the avatars.");

        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
    }
}
