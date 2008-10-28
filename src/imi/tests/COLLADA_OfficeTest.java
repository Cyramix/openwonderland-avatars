package imi.tests;

import com.jme.math.Vector3f;
import imi.character.ninja.NinjaAvatar;
import imi.character.objects.ObjectCollection;
import imi.environments.ColladaEnvironment;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PScene;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * Testing environments
 * @author Ronald E Dahlgren
 */
public class COLLADA_OfficeTest extends DemoBase2 
{
    public COLLADA_OfficeTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        COLLADA_OfficeTest worldTest = new COLLADA_OfficeTest(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors)
    {
        
        Logger.getLogger("com.jme.scene").setLevel(Level.OFF);
        Logger.getLogger("org.collada").setLevel(Level.OFF);
        Logger.getLogger("com.jme.renderer.jogl").setLevel(Level.OFF);
        
        URL modelLocation = null;
        try
        {
            modelLocation = new File("assets/models/collada/environments/MPK20/MPK20.dae").toURI().toURL();
            modelLocation = new File("assets/models/collada/environments/BusinessObjects/BusinessObjectsCenter.dae").toURI().toURL();
            modelLocation = new File("assets/models/collada/environments/Milan/DSI.dae").toURI().toURL();
            //modelLocation = new File("assets/models/collada/Objects/Chairs/Sofa.dae").toURI().toURL();
            //modelLocation = new File("assets/models/collada/environments/MaldenLabs/MaldenLabs.dae").toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SharedAsset colladaAsset = new SharedAsset(pscene.getRepository(),
                new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelLocation));
        
        colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 0, "MPK20", null));
        
        pscene.setUseRepository(true);
        
        ColladaEnvironment ourEnv = new ColladaEnvironment(wm, colladaAsset, "MaldenLabs");
        
        
        ///////////
        
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Create an object collection for the musical chairs game
        ObjectCollection objs = new ObjectCollection("Musical Chairs Game Objects", wm);
        objs.generateChairs(Vector3f.ZERO, 25.0f, 2);
        
        NinjaAvatar avatar = new NinjaAvatar("Avatar", wm);
        avatar.selectForInput();
        avatar.setObjectCollection(objs);
        
    }
    
    
}
