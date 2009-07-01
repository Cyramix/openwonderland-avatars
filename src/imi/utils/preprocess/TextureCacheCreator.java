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
package imi.utils.preprocess;

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import imi.repository.CacheBehavior;
import imi.repository.Repository;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javolution.util.FastList;

/**
 * This utility class loads all of the textures under a given root folder and creates
 * a cache out of it for the (jME) TextureManager.
 * @author Ronald E Dahlgren
 */
public final class TextureCacheCreator
{
    /** Default folder to search for textures in **/
    private static final File defaultAssetRoot = new File("assets/models/collada/Avatars");
    /** Default file to write out **/
    private static final File defaultOutputFile = new File("assets/textures/textures.bin");
    
    /** Disabled! **/
    private TextureCacheCreator(){}
    /**
     * Run the tool, generate the cache.
     * <p>Command line args that are used are:
     * -assetRoot to specify the folder to start in
     * -outputFile to specify a non-default output file
     * Note that using a non-standard output file will cause the system to not
     * recognize it at load time, unless the developer changes the behavior of
     * the CacheBehavior.</p>
     * @param args
     * @see CacheBehavior
     * @see Repository
     */
    public static void main(String[] args)
    {
        // may be assigned from command line args
        String assetRoot = null;
        String outputFile = null;

        // Parse command line args
        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].equalsIgnoreCase("-assetRoot"))
            {
                if (++i >= args.length)
                    throw new IllegalArgumentException("Must specify a folder to use!");
                else
                    assetRoot = args[i];
            }
            else if (args[i].equalsIgnoreCase("-outputFile"))
            {
                
                if (++i >= args.length)
                    throw new IllegalArgumentException("Must specify an output file!");
                else
                    outputFile = args[i];
            }
        }
        
        File assetRootFile = defaultAssetRoot;
        if (assetRoot != null)
            assetRootFile = new File(assetRoot);

        List<File> fileCollection = new ArrayList<File>();
        fileCollection.add(assetRootFile);
        
        File out = defaultOutputFile;
        if (outputFile != null)
            out = new File(outputFile);
        
        try {
            buildTextureCache(fileCollection, out);
        } catch (IOException ex) {
            throw new RuntimeException("Encountered IO exception: " + ex.getMessage());
        }
    }

    /**
     * This method looks for textures in each provided folder (and all its children)
     * and then creates a binary texture cache outputFile.
     * @param folders A non-null (may be empty) file collection
     * @param outputFile A non-null file specifying the output location
     * @throws IOException If an error occurs with file IO
     * @throws IllegalArgumentException If any parameter is null
     */
    public static void buildTextureCache(Iterable<File> folders, File outputFile) throws IOException {
        if (folders == null || outputFile == null)
            throw new IllegalArgumentException("Null parameter encountered.");
        Texture.DEFAULT_STORE_TEXTURE = true;
        for (File folder : folders)
            loadAllFiles(folder);
        TextureManager.writeCache(outputFile);
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
