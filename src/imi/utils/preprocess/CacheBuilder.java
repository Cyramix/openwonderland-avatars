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

import imi.repository.Repository;
import java.io.File;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * CacheBuilder class is
 * @author Ronald E Dahlgren
 */
public class CacheBuilder {
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(CacheBuilder.class.getName());

    /** Where the compressed archive should be saved (if it is made) **/
    private File archiveOutput = null;
    /** Where processing should start **/
    private File assetRoot = new File("assets/");
    /** File extensions to match **/
    private String[] fileExtensions = new String [] { "dae", "ms3d" };

    // State for gathering metrics
    private float secondsToProcess = 0;
    private float compressionRatio = 0;

    private WorldManager wm;
    private Repository repo;

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
        // for each file encountered
        // Does extension match the check list?
        // if so, load the file
        // if false, continue
        // finally, if an archive was specified compress the existing cache and
        // store the compression ratio
    }


    private void init() {
        // create the world manager and get it up and running
        // create a repository
        // add repository to the user data of the world manager
    }
    /**
     * The main running method to start this tool.
     * <p>It accepts the following command line options:
     * <ul>
     * <li>-assetRoot &lt;file&gt; : Specify the root folder to start loading in</li>
     * <li>-compress &lt;file&gt; : Compress the cache and write it to the specifed location</li>
     * <li>-extensions "dae ms3d etc" : Load files that end with the provided strings</li>
     * </ul>
     * </p>
     *
     * @param args Command line args
     */
    public static void main(String[] args) {
        CacheBuilder builder = new CacheBuilder();
        parseArgs(args, builder);
    }

    private static void parseArgs(String[] args, CacheBuilder builder) {
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("-assetRoot"))
                builder.assetRoot = new File(args[++i]);
            else if (arg.equals("-compress"))
                builder.archiveOutput = new File(args[++i]);
            else if (arg.equals("-extensions")) {
                // split string at ++i across whitespace for individual extensions
                // make an array out of these for use with the fileExtensions variable
            }

        }
    }
}
