/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.tests;

import com.jme.math.Vector3f;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.camera.state.FirstPersonCamState;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import imi.utils.AvatarObjectInputStream;
import imi.utils.AvatarObjectOutputStream;

/**
 * Tests the performance gains of processing a collada file versus
 * loading the same data from a serialized pscene.
 * @author Ronald E Dahlgren
 */
public class ColladaSerializationTest extends DemoBase
{
    private static final Logger logger = Logger.getLogger(ColladaSerializationTest.class.getName());
    private static final File saveFile = new File("assets/models/collada/Avatars/Male/Male_Bind.baf");
    private static URL colladaFileLocation = null;
    static {
        try {
            colladaFileLocation = new URL("file://localhost/work/avatars/assets/models/collada/Avatars/Male/Male_Bind.dae");
        } catch (MalformedURLException ex) {
            Logger.getLogger(ColladaSerializationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ColladaSerializationTest(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ColladaSerializationTest worldTest = new ColladaSerializationTest(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) {
        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        long startTime = 0l;
        long endTime = 0l;
        float loaderDuration = -10.0f;
        float deserializeDuration = -10.0f;

        PScene sceneForLoading = new PScene(wm);

        ColladaLoaderParams loaderParams = new ColladaLoaderParams(true, true, false, false, 4, "ColladaTest", null);
        Collada colladaLoader = new Collada(loaderParams);
        startTime = System.nanoTime();
        colladaLoader.load(sceneForLoading, colladaFileLocation);
        endTime = System.nanoTime();
        // Wait until we are certain that everything has finished
//        for (int i = 0; i < 100; i++)
//        {
//            try {
//                Thread.sleep(100);
//            }
//            catch (InterruptedException ex)
//            {
//                // do some-ting
//            }
//        }
//        Thread.yield();
//        serializePScene(sceneForLoading);
//        pscene.addModelInstance(sceneForLoading, new PMatrix());
        loaderDuration = (endTime - startTime) / 1000000000.0f;
        System.out.println("Loading the old way took " + loaderDuration + " seconds.");

        // try the other way
        sceneForLoading = null;
        startTime = System.nanoTime();
        sceneForLoading = deserializePScene();
        endTime = System.nanoTime();
        pscene.addModelInstance(sceneForLoading, new PMatrix(new Vector3f(10, 0, 0)));

        deserializeDuration = (endTime - startTime) / 1000000000.0f;
        System.out.println("New method took " + deserializeDuration + " seconds.");
        System.out.println("Net savings " + (1.0f - (deserializeDuration / loaderDuration)) * 100.0f + " percent.");

        // Uncomment for a tree explorer
        TreeExplorer te = new TreeExplorer();
        SceneEssentials se = new SceneEssentials();
        se.setSceneData(pscene.getJScene(), pscene, null, wm, null);
        te.setExplorer(se);
        te.setVisible(true);
    }

    private void serializePScene(PScene sceneForLoading) {
        FileOutputStream fos = null;
        AvatarObjectOutputStream out = null;
        try
        {
          fos = new FileOutputStream(saveFile);
          out = new AvatarObjectOutputStream(fos);
          out.writeObject(sceneForLoading);
          out.close();
        }
        catch(IOException ex)
        {
          ex.printStackTrace();
        }

    }

    private PScene deserializePScene()
    {
        PScene result = null;
        FileInputStream fis = null;
        AvatarObjectInputStream in = null;

        try
        {
            fis = new FileInputStream(saveFile);
            in = new AvatarObjectInputStream(fis);
            result = (PScene)in.readObject();
            in.close();
        }
        catch(Exception ex)
        {
            logger.severe("Uh oh! " + ex.getMessage());
            ex.printStackTrace();
        }

        return result;
    }
}
