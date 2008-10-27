package imi.tests;

import imi.environments.ColladaEnvironment;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PScene;
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
public class COLLADA_MapTest extends DemoBase2 
{
    public COLLADA_MapTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        COLLADA_MapTest worldTest = new COLLADA_MapTest(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors)
    {
        URL modelLocation = null;
        try
        {
            //modelLocation = new File("assets/models/collada/environments/MPK20/MPK20.dae").toURI().toURL();
            //modelLocation = new File("assets/models/collada/Objects/Chairs/LoungeChair.dae").toURI().toURL();
            modelLocation = new File("assets/models/collada/environments/MaldenLabs/MaldenLabs.dae").toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SharedAsset colladaAsset = new SharedAsset(pscene.getRepository(),
                new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelLocation));
        
        colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 0, "MPK20", null));
        
        pscene.setUseRepository(true);
        
        ColladaEnvironment ourEnv = new ColladaEnvironment(wm, colladaAsset, "MPK20 BIOTCH");
        
    }
    
    
}
