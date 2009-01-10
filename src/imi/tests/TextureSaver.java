/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imi.tests;

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import com.jme.util.export.Savable;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import java.io.File;
import java.net.URL;

/**
 *
 * @author skendall
 */
public class TextureSaver {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Texture tex = null;
        try {
//            URL url = new URL("file:///work/Projects/Avatars/trunk/assets/models/collada/Avatars/Male/MaleCHeadCLR.png");
//
//            tex = TextureManager.loadTexture(url,
//                    Texture.MinificationFilter.Trilinear,
//                    Texture.MagnificationFilter.Bilinear);
//            tex.setStoreTexture(true);
//            BinaryExporter jmee = new BinaryExporter();
//            jmee.save(tex, new File("texture.bin"));



            Long timeA1, timeA2, timeAtotal;
            Long timeB1, timeB2, timeBtotal;
            Long timeC1, timeC2, timeCtotal;
            URL urlA, urlB, urlC;

            urlA = new URL("file:///work/Projects/Avatars/trunk/assets/models/collada/Avatars/Male/MaleCHeadCLR.png");
            urlB = new URL("file:///work/Projects/Avatars/trunk/assets/models/collada/Avatars/Male/MaleCHeadCLR.jpg");
            urlC = new URL("file:///work/Projects/Avatars/trunk/texture.bin");

            BinaryImporter bim = new BinaryImporter();
            BinaryExporter jmee2 = new BinaryExporter();
            File file = null;
            tex = null;
            {
                timeAtotal = timeBtotal = timeCtotal = 0l;
                for (int i = 0; i < 100; i++) {
                    TextureManager.clearCache();

                    jmee2 = new BinaryExporter();
                    timeA1 = System.nanoTime();
                    tex = TextureManager.loadTexture(urlA);
                    timeA2 = System.nanoTime();
                    timeAtotal += (timeA2 - timeA1);
                    tex.setStoreTexture(true);
                    file = new File("texture2.bin");
                    jmee2.save(tex, file);

                    jmee2 = new BinaryExporter();
                    timeB1 = System.nanoTime();
                    tex = TextureManager.loadTexture(urlB);
                    timeB2 = System.nanoTime();
                    timeBtotal += (timeB2 - timeB1);
                    tex.setStoreTexture(true);
                    jmee2.save(tex, new File("texture3.bin"));

                    jmee2 = new BinaryExporter();
                    timeC1 = System.nanoTime();
                    tex = (Texture) bim.load(urlC);
                    timeC2 = System.nanoTime();
                    timeCtotal += (timeC2 - timeC1);
                    tex.setStoreTexture(true);
                    jmee2.save(tex, new File("texture4.bin"));
                    System.out.print(i+ " ");
                }
            }
            System.out.println();
            System.out.println("Time elapsed png load =    " + timeAtotal);
            System.out.println("Time elapsed jpg load =    " + timeBtotal);
            System.out.println("Time elapsed binary load = " + timeCtotal);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}
