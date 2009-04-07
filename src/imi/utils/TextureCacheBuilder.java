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
package imi.utils;

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;

/**
 *
 * @author Ronald E Dahlgren
 */
public class TextureCacheBuilder
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(TextureCacheBuilder.class.getName());
    
    /**
     * Run the tool
     * @param args
     */
    public static void main(String[] args)
    {
        String assetRoot = null;
        String outputFile = null;
        
        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].equalsIgnoreCase("-assetRoot"))
                assetRoot = args[++i];
            else if (args[i].equalsIgnoreCase("-outputFile"))
                outputFile = args[++i];
        }
        
        File assetRootFile = null;
        if (assetRoot != null)
            assetRootFile = new File(assetRoot);
        else
            assetRootFile = new File("assets/models/collada/Avatars");
        
        File out = null;
        if (outputFile != null)
            out = new File(outputFile);
        else
            out = new File("assets/textures/textures.bin");
        
        try {
            Texture.DEFAULT_STORE_TEXTURE = true;

            loadAllFiles(assetRootFile);
            assetRootFile = new File("assets/models/collada/Clothing");
            loadAllFiles(assetRootFile);
            assetRootFile = new File("assets/models/collada/Heads");
            loadAllFiles(assetRootFile);
            
            System.out.println("Writing the cache out to " + out.toString());
            TextureManager.writeCache(out);
            
        } catch (IOException ex) {
            Logger.getLogger(TextureCacheBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Traverse the asset hierarchy and cache all the encountered collada files
     * @param docRoot
     */
    private static boolean loadAllFiles(File docRoot) throws MalformedURLException
    {
        boolean returnValue = true;
        // Make a file filter to use
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File fileVersion = new File(dir, name);
                if (name.endsWith(".png") || name.endsWith(".jpg") ||
                        name.endsWith(".tga") ||  name.endsWith(".gif") ||
                        name.endsWith(".bmp") || name.endsWith(".rgb") ||
                        name.endsWith(".jpeg")) // image?
                    return true;
                else if (fileVersion.isDirectory() && !fileVersion.getName().equalsIgnoreCase("animations"))
                    return true;
                else
                    return false;
            }
        };
        // Scratch references
        File current = null;
        File[] fileList = null;
        // Our queue!
        FastList<File> queue = new FastList<File>();
        queue.add(docRoot);
        while (!queue.isEmpty())
        {
            current = queue.removeFirst();
            if (current.isFile())
            {
                System.out.println("Processing " + current.getName());
                TextureManager.loadTexture(current.toURI().toURL());
            }
            else if (current.isDirectory())
            {
                fileList = current.listFiles(filter);
                for (File file : fileList)
                    queue.add(file);
            }
        }
        System.out.println("Completed processing textures.");
        return returnValue;
    }

}
