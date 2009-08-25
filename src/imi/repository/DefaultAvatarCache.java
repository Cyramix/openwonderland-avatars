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
package imi.repository;

import imi.repository.CacheBehavior;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import imi.utils.MD5HashUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Default implementation of caching behavior for the cache.
 * @author Ronald E Dahlgren
 */
public class DefaultAvatarCache implements CacheBehavior
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(DefaultAvatarCache.class.getName());
    /** Used to tune the buffer size for the max **/
    private static final int BUFFER_SIZE = 1024 * 64; // 64K
    /** The folder we will be searching for and storing cache files in. **/
    private File cacheFolder = null;
    /** Instrumentation **/
    private final CacheInstrumentation instruments;

    /**
     * Construct a new cache using the specified folder.
     * @param cacheFolder
     * @throws ExceptionInInitializerError If (cacheFolder == null)
     */
    public DefaultAvatarCache(File cacheFolder)
    {
        this(cacheFolder, false);
    }

    /**
     * Construct a new default avatar caching behavior.
     * @param cacheFolder The cache folder to use.
     * @param instrument True to instrument
     * @throws ExceptionInInitializerError If (cacheFolder == null)
     */
    public DefaultAvatarCache(File cacheFolder, boolean instrument)
    {

        if (cacheFolder == null)
            throw new ExceptionInInitializerError("Cannot have a null cache folder!");
        else
            this.cacheFolder = cacheFolder;

        if (instrument)
            instruments = new CacheInstrumentation();
        else
            instruments = null;

    }

    @Override
    public boolean initialize(Object[] params) {
        // Determine if the directory exists. If not, create it.
        if (cacheFolder.exists() == false)
            if (cacheFolder.mkdir() == false) // error
                logger.severe("Cache is unavailable!");
        return true;
    }

    @Override
    public boolean shutdown() {
        if (instruments != null)
            instruments.dumpStats();
        return true;
    }

    @Override
    public boolean isFileCached(URL location) {
        boolean fileFound;
        File cacheFile = urlToCacheFile(location);
        fileFound = cacheFile.exists();
        if (instruments != null && !fileFound)
            instruments.cacheMisses++;
        return fileFound;
    }

    @Override
    public InputStream getStreamToResource(URL location) {
        File cacheFile = urlToCacheFile(location);
        InputStream result = null;
        if (cacheFile != null && cacheFile.exists())
        {
            try {
                result = new FileInputStream(cacheFile);
                if (instruments != null)
                    instruments.cacheHits++;
            }
            catch (FileNotFoundException ex)
            {
                logger.severe("Although the cache file exists, a FileNotFoundException" +
                        "was thrown.");
            }
        }
        return result;
    }

    @Override
    public OutputStream getStreamForWriting(URL location) {
        File cacheFile = urlToCacheFile(location);
        OutputStream result = null;
        if (cacheFile != null)
        {
            try {
                result = new FileOutputStream(cacheFile);
            }
            catch (FileNotFoundException ex)
            {
                logger.severe("Although the cache file exists, a FileNotFoundException" +
                        "was thrown.");
            }
        }
        return result;
    }

    @Override
    public Texture loadTexture(URL location)
    {
        return TextureManager.loadTexture(location);
    }

    @Override
    public boolean clearCache() 
    {
        boolean deletion    = false;
        for (File file : cacheFolder.listFiles()) {
            deletion    = file.delete();
            if (!deletion)
                logger.log(Level.SEVERE, "CACHE FILE DELETION FAILED: " + file.getName());
        }
            
        if (cacheFolder.listFiles().length == 0)
            return true; // Success
        else
            return false;
    }

    void dumpStats() {
        if (instruments != null)
            instruments.dumpStats();
    }

    /**
     * {@inheritDoc CacheBehavior}
     */
    public void createCachePackage(OutputStream output) {
        // variables used for the transfer
        int bytesRead = 0;
        byte[] transferBuffer = new byte[BUFFER_SIZE];

        try {
            // create a zip output stream out of the provided stream
            ZipOutputStream zos = new ZipOutputStream(output);
            // for each file in the cache folder
            for (File file : cacheFolder.listFiles()) {
                // add a zip entry
                ZipEntry fileEntry = new ZipEntry(file.getName());
                zos.putNextEntry(fileEntry);
                // open a buffered input stream via a file stream
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                // write everything from the input stream to the zip stream
                while ((bytesRead = bis.read(transferBuffer)) != -1) {
                    zos.write(transferBuffer, 0, bytesRead); // buffer, startOffset, length
                }
                // close the entry
                zos.closeEntry();
            }
            zos.close();
            // close the zip stream
        } catch (IOException ex) {
            logger.severe("Caught an IOException while making cache package: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc CacheBehavior}
     */
    public void loadCachePackage(InputStream input) {
        // load a ZipFile from the input stream
        // this will be memory mapped
        // for each entry
            // reconstitute!
    }



    private File urlToCacheFile(URL location)
    {
        // Get the path relative to the "assets" folder
        String urlString = location.toString();
        int assetsIndex = urlString.indexOf("assets/");

        if (assetsIndex != 1) // If found, truncate
            urlString = urlString.substring(assetsIndex + 7);

        String hashFileName = MD5HashUtils.getStringFromHash(urlString.getBytes());
        File result = new File(cacheFolder, hashFileName);

        File localFile = new File("assets/" + urlString);
        if (localFile != null && result.exists())
        {
            // Determine which one is newer, if the cache version is older,
            // then we will delete it.
            if (localFile.lastModified() > result.lastModified()) {
                boolean deletion    = result.delete();
                if (!deletion)
                    logger.log(Level.SEVERE, "Deletion of old cache file (" + result.getName() + ") failed: ");
            }
        }
        return result;
    }

    private class CacheInstrumentation {
        int cacheHits;
        int cacheMisses;

        void dumpStats() {
            System.out.println("Cache Data, hits: " + cacheHits + ", misses:" + cacheMisses);
        }
    }
}
