/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.tests;

import imi.scene.animation.AnimationGroup;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Testing of animation serialization and deserialization
 * @author Ronald E Dahlgren
 */
public class AnimationSerialization
{
    private static final Logger logger = Logger.getLogger(AnimationSerialization.class.getName());

    public static void main(String[] args)
    {
        AnimationSerialization as = new AnimationSerialization(args);
    }

    private AnimationSerialization(String[] args)
    {
        URL path = null;
        URL output = null;
        try
        {
            path = new URL("file://localhost/work/avatars/assets/models/collada/Avatars/Male/Male_Anim_FloorSitting.dae");
            output = new URL("file://localhost/work/avatars/assets/models/collada/Avatars/Male/Male_Anim_FloorSitting.baf");
        }
        catch (MalformedURLException ex)
        {
            // blah blah
            logger.severe("URL was messed up! " + ex.getMessage());
            ex.printStackTrace();
        }

        // To create the binary file
        convertToBinary(path, output);

        // to load the binary file
        AnimationGroup group = deserializeBinary(output);
    }
    private void convertToBinary(URL pathToDAE, URL pathForBinary)
    {
        // load it up

        // write it out
    }

    private AnimationGroup deserializeBinary(URL pathToBinaryFile)
    {
        AnimationGroup result = null;
        return result;
    }
}


