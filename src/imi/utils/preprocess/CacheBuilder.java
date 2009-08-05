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

import imi.character.AvatarSystem;
import imi.repository.AssetDescriptor;
import imi.repository.Repository;
import imi.repository.RepositoryUser;
import imi.repository.SharedAsset;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.WorldManager;

/**
 * CacheBuilder class is
 * @author Ronald E Dahlgren
 */
public class CacheBuilder {
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(CacheBuilder.class.getName());
    /** Where the compressed archive should be saved **/
    private File archiveOutput = new File("cache.car");
    /** Where processing should start **/
    private File assetRoot = new File("assets/");
    /** **/
    private final List<String> excludedFolderNames = new FastList<String>();
    /** File extensions to match **/
    private String[] meshFileExtensions = new String [] { "dae" };

    private WorldManager wm;
    private Repository repo;

    private int loadedFileCount = 0;
    private int filesNeeded = -1;

    /**
     * Construct a new cache builder.
     */
    public CacheBuilder() {
        init();
    }

    /**
     * Process the cache from the specified folder and optionally create a
     * compressed archive (if an archive output is set).
     */
    public void processCache() {
        // perform a breadth-first traversal of the directory tree starting
        // at the assetRoot
        logger.info("Processing cache...");
        FastList<File> queue = new FastList();
        queue.add(assetRoot);
        // Keep track of how many files we have encountered
        int localFilesNeeded = 0;
        while (!queue.isEmpty())
        {
            File current = queue.removeFirst();

            // Does extension match the check list?
            String fileName = current.getName().toLowerCase();
            for (String ext : meshFileExtensions) {
                // if so, load the file
                if (fileName.endsWith(ext)) {
                    localFilesNeeded++;
                    logger.info("Processing " + fileName);
                    AssetDescriptor desc = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, current);
                    RepositoryUser user = new RepositoryUser() {
                        public void receiveAsset(SharedAsset asset) {
                            logger.info("Received asset : " + asset.getDescriptor().getLocation().getFile());
                            loadedFileCount++;
                            // has been set and we are done
                            if (filesNeeded > 0 && loadedFileCount == filesNeeded) 
                                complete();
                        }
                    };
                    repo.loadSharedAsset(new SharedAsset(repo, desc), user);
                }
            }
            // make sure we shouldn't exclude this
            boolean skipMe = false;
            for (String exclusion : excludedFolderNames) {
                if (current.getName().matches(exclusion))
                    skipMe = true;
            }
            if (current.isDirectory() && !skipMe)
                for (File f : current.listFiles())
                    queue.add(f);
        } // end traversal
        logger.info("All files processed.");
        filesNeeded = localFilesNeeded;
    }

    private void complete() {
        logger.info("Loading complete. Writing cache file to " + archiveOutput);
        // All files are loaded, time to write the cache file out
        try {
            repo.saveCache(new FileOutputStream(archiveOutput));
        } catch (FileNotFoundException ex) {
            logger.severe("Unable to save cache archive: " + ex.getMessage());
        }
    }

    private void init() {
        // create the world manager and get it up and running
        wm = new WorldManager("Manager of the World!");
        // create a repository
        repo = new Repository(wm);
        repo.setLoadTextures(false);
        repo.setToolMode(true);
        // add repository to the user data of the world manager
        wm.addUserData(Repository.class, repo);

        // Create avatar repository component
        AvatarSystem.initialize(wm);
    }
    /**
     * The main running method to start this tool.
     * <p>It accepts the following command line options:
     * <ul>
     * <li>-assetRoot &lt;file&gt; : Specify the root folder to start loading in</li>
     * <li>-archive &lt;file&gt; : Archive the cache to the specifed location</li>
     * </ul>
     * </p>
     *
     * @param args Command line args
     */
    public static void main(String[] args) {
        CacheBuilder.logger.setLevel(Level.FINEST);
        CacheBuilder builder = new CacheBuilder();
        parseArgs(args, builder);
        builder.processCache();
    }

    private static void parseArgs(String[] args, CacheBuilder builder) {
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("-assetRoot"))
                builder.assetRoot = new File(args[++i]);
            else if (arg.equals("-archive"))
                builder.archiveOutput = new File(args[++i]);
            else if (arg.equals("-exclude")) {
                String exclusionList = args[++i];
                // split on whitespace
                String[] exclusions = exclusionList.split(" ");
                // add each result into exclusion list
                for (String exclusion : exclusions) {
                    builder.excludedFolderNames.add(exclusion);
                }
            }

        }
    }
}
