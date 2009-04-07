/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.cache;

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import imi.utils.MD5HashUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Default implementation of caching behavior for the cache.
 * @author Ronald E Dahlgren
 */
public class DefaultAvatarCache implements CacheBehavior
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(DefaultAvatarCache.class.getName());
    /** The folder we will be searching for and storing cache files in. **/
    private File cacheFolder = null;
    /**
     * Construct a new cache using the specified folder.
     * @param cacheFolder
     */
    public DefaultAvatarCache(File cacheFolder)
    {
        if (cacheFolder == null)
            throw new ExceptionInInitializerError("Cannot have a null cache folder!");
        else
            this.cacheFolder = cacheFolder;
    }

    @Override
    public boolean initialize(Object[] params) {
        // Nothing needs to be done currently.
        return true;
    }

    @Override
    public boolean shutdown() {
        return true;
    }

    @Override
    public boolean isFileCached(URL location) {
        File cacheFile = urlToCacheFile(location);
        return cacheFile.exists();
    }

    @Override
    public InputStream getStreamToResource(URL location) {
        File cacheFile = urlToCacheFile(location);
        InputStream result = null;
        if (cacheFile != null && cacheFile.exists())
        {
            try {
                result = new FileInputStream(cacheFile);
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
        for (File file : cacheFolder.listFiles())
            file.delete();
        if (cacheFolder.listFiles().length == 0)
            return true; // Success
        else
            return false;
    }

    private File urlToCacheFile(URL location)
    {
        File localFile = null; // If a local version exists.

        // If the URL points to a local file, check the last modified time
        if (location.getProtocol().equalsIgnoreCase("file"))
        {
            try {
                localFile = new File(location.toURI());
            } catch (URISyntaxException ex) {
                logger.severe("Unable to form a file object from the URI");
            }
        }

        // Get the path relative to the "assets" folder
        String urlString = location.toString();
        int assetsIndex = urlString.indexOf("assets/");

        if (assetsIndex != 1) // If found, truncate
            urlString = urlString.substring(assetsIndex + 7);

        String hashFileName = MD5HashUtils.getStringFromHash(urlString.getBytes());
        File result = new File(cacheFolder, hashFileName);

        if (localFile != null)
        {
            // Determine which one is newer, if the cache version is older,
            // then we will delete it.
            if (localFile.lastModified() > result.lastModified())
                result.delete();
        }
        return result;
    }
}
