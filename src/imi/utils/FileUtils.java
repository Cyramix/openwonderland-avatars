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


import imi.character.Character;
import imi.character.CharacterInitializationInterface;
import imi.character.avatar.Avatar;
import imi.imaging.ImageLibraryExt;
import imi.input.CharacterControls;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javolution.util.FastTable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;


/**
 *
 * @author Chris Nagle
 */
public class FileUtils
{
    private static final Logger logger      = Logger.getLogger(FileUtils.class.getName());
    public static final File    rootPath    = new File(System.getProperty("user.dir"));
    private static JFileChooser fileChooser = new JFileChooser();

    //  Returns a string containing just the short filename.
    public static final String getShortFilename(String fullFilename) {
        if (fullFilename == null)
            return null;

        int lastDirectoryDividerIndex = fullFilename.lastIndexOf('/');
        if (lastDirectoryDividerIndex == -1)
            lastDirectoryDividerIndex = fullFilename.lastIndexOf('\\');

        if (lastDirectoryDividerIndex == -1)
            return(fullFilename);

        String shortFilename = null;
        if (lastDirectoryDividerIndex != -1)
            shortFilename = fullFilename.substring(lastDirectoryDividerIndex+1, fullFilename.length());
        else
            shortFilename = fullFilename;

        return(shortFilename);
    }

    public static URL convertRelativePathToFileURL(String relativePath) {
       File newFile = new File(System.getProperty("user.dir"), relativePath);
       URL result = null;
       try 
       {
           result = newFile.toURI().toURL();
       }
       catch (MalformedURLException ex)
       {
           Logger.getLogger(FileUtils.class.toString()).log(Level.SEVERE, "Unable to convert to URL: " + ex.getMessage());
       }
       return result;
    }

    //  Returns a string containing the directory path of the file.
    public static String getDirectoryPath(String fullFilename) {
        if (fullFilename == null)
            return("");

        int lastDirectoryDividerIndex = fullFilename.lastIndexOf('/');
        if (lastDirectoryDividerIndex == -1)
            lastDirectoryDividerIndex = fullFilename.lastIndexOf('\\');

        if (lastDirectoryDividerIndex == -1)
            return("");

        return(fullFilename.substring(0, lastDirectoryDividerIndex));
    }

    //  Builds of list of filenames in the specified directory.
    public static FastTable getAllChildFilenames(String baseDirectory) {
        File rootDirectory = new File(baseDirectory);

        FastTable childFilenames = new FastTable();

        File [] allChildren = rootDirectory.listFiles();
        for (int i=0; i<allChildren.length; i++)
        {
            if (!allChildren[i].isDirectory() && !allChildren[i].isHidden())
            {
                String filePath = allChildren[i].getPath();
                childFilenames.add(filePath);
            }
        }

        return(childFilenames);
    }

    //  Builds of list of directories in the specified directory.
    public static FastTable getAllChildDirectories(String baseDirectory) {
        File rootDirectory = new File(baseDirectory);

        FastTable childDirectories = new FastTable();

        File [] allChildren = rootDirectory.listFiles();
        for (int i=0; i<allChildren.length; i++)
        {
            if (allChildren[i].isDirectory() && !allChildren[i].isHidden())
            {
                String filePath = allChildren[i].getPath();
                childDirectories.add(filePath);
            }
        }

        return(childDirectories);
    }
    
    //  Attempts to find a file in a directory and it's sub directories.
    public static String findFile(String baseDirectory, String shortFilename) {
        String fullFilename = baseDirectory + File.separator + shortFilename;
        File physicalFile = new File(fullFilename);
        
        //  Does the file exist in this directory.
        if (physicalFile.exists())
            return(fullFilename);

        
        //  Otherwise, search through all subdirectories.
        FastTable subDirectories = FileUtils.getAllChildDirectories(baseDirectory);
        
        
        for (int i=0; i<subDirectories.size(); i++)
        {
            String subBaseDirectory = (String)subDirectories.get(i);
            
            fullFilename = FileUtils.findFile(subBaseDirectory, shortFilename);
            if (fullFilename.length() != 0)
                return(fullFilename);
        }

        return("");
    }
    
    //  Attempts to find the Texture file.
    public static String findTextureFile(String shortTextureFilename) {
        String fullFilename = FileUtils.findFile("assets/", shortTextureFilename);
        if (fullFilename.length() == 0)
            return("");

//        fullFilename = fullFilename.substring(4, fullFilename.length());

        return(fullFilename);
    }
    
    /**
     * This method converts a path relative to the asset folder into a string
     * URL.
     * @param path The asset relative path
     * @return The url string to the asset, or null if the resource was not located
     */
    public static String convertAssetPathToFullURLString(String path) {
        String szResource   = null;
        URL resourcePath    = FileUtils.class.getResource(File.separatorChar + path);
        if (resourcePath != null) {
            szResource = resourcePath.toString();
        }
        else
            throw new RuntimeException(path);
        return szResource;
    }

    /**
     * Check the validity of a string path
     * @param path The path to check
     * @return true if the path points to a valid resource, false otherwise
     */
    public static boolean checkURLPath(String path) {
        boolean success = false;
        InputStream is  = null;
        try {
            URL urlPath = new URL(path);
            is          = urlPath.openStream();
            success     = true;
        } catch (MalformedURLException ex) {
            success = false;
        } catch (IOException ex) {
            success = false;
        } finally {
            close(is);
        }
        return success;
    }

    /**
     * Check the validity of a URL
     * @param path The path to check
     * @return true if the path points to a valid resource, false otherwise
     */
    public static boolean checkURL(URL url) {
        boolean success = false;
        InputStream is  = null;
        try {
            is          = url.openStream();
            success     = true;
        } catch (MalformedURLException ex) {
            success = false;
        } catch (IOException ex) {
            success = false;
        } finally {
            close(is);
        }
        return success;
    }

    /**
     * Determine if the URL provided points to a file that we deem to be binary.
     * This is done by matching the "extension" (part of the string following the
     * last index)
     * @param fileLocation - may not be null
     * @return
     */
    @ExperimentalAPI
    public static boolean doesURLReferToBinaryFile(URL fileLocation) {

        int index  = fileLocation.toString().lastIndexOf(".");
        String ext = fileLocation.toString().substring(index);
        if (ext.toLowerCase().contains("bin") || ext.toLowerCase().contains("bhf"))
            return true;
        return false;
    }

    /**
     * Determine if the path provided points to a file that we deem to be binary.
     * This is done by matching the "extension" (part of the string following the
     * last index)
     * @param fileLocation - may not be null
     * @return
     */
    @ExperimentalAPI
    public static boolean doesPathReferToBinaryFile(String fileLocation) {
        int index  = fileLocation.lastIndexOf(".");
        String ext = fileLocation.substring(index);
        if (ext.toLowerCase().contains("bin") || ext.toLowerCase().contains("bhf"))
            return true;
        return false;
    }

    /**
     * Close a stream.
     * @param stream
     */
    private static void close(InputStream stream) {
        if (stream != null)
        {
            try {
                stream.close();
            }
            catch (IOException ex){
                logger.warning("Could not close stream: " + ex.getMessage());
            }
        }
    }

    public static List getPathList(File file) {
        List list = new ArrayList();
        File copy;
        try {
            copy = file.getCanonicalFile();
            while (copy != null) {
                list.add(copy.getName());
                copy = copy.getParentFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    public static String matchPathLists(List r, List f) {
        int i;
        int j;
        StringBuilder s = new StringBuilder();
        // start at the beginning of the lists
        // iterate while both lists are equal
        s.append("");
        i = r.size() - 1;
        j = f.size() - 1;

        // first eliminate common root
        while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for (; i >= 0; i--) {
            s.append("..")
             .append(File.separator);
        }

        // for each level in the file path, add the path
        for (; j >= 1; j--) {
            s.append(f.get(j))
             .append(File.separator);
        }

        // file name
        s.append(f.get(j));
        return s.toString();
    }

    public static String getRelativePath(File home, File f) {
        List homelist;
        List filelist;
        String s;

        homelist = getPathList(home);
        filelist = getPathList(f);
        s = matchPathLists(homelist, filelist);

        return s;
    }

    /**
     * Load a previously saved configuration file containing avatar metrics
     * @param parent - parent component that called this function
     */
    public static void loadAvatarConfiguration(Character character, WorldManager wm, Component parent, CharacterInitializationInterface init) {
        FileFilter filter   = createFileFilter(".xml", "Extensible Markup Language (*.xml)");
        File fileDirectory  = new File("." + File.separatorChar + "assets" + File.separatorChar);
        setFileChooserProperty(fileChooser, filter, "Load Avatar Configuration File", fileDirectory);
        
        int retVal = fileChooser.showOpenDialog(parent);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                File configfile = fileChooser.getSelectedFile();
                URL configURL   = configfile.toURI().toURL();
                if (character != null) {
                    character.destroy();
                }
                
                character = new Avatar.AvatarBuilder(configURL, wm)
                                      .initializer(init)
                                      .build();

                character.getSkeleton().resetAllJointsToBindPose();

                CharacterControls input = (CharacterControls)wm.getUserData(CharacterControls.class);
                if (input != null)
                    input.setCharacter(character);
                else
                    throw new RuntimeException("Was not able to find Character Controls in the world manager user data");

            } catch (MalformedURLException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Save out the current configuration of the avatar into a xml file
     * @param parent - parent component that called this function
     */
    public static void saveAvatarConfiguration(final Character character, Component arg0) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character parmater passed in is invalid" +
                    " || character = " + character);
        }
        
        FileFilter filter   = createFileFilter(".xml", "Extensible Markup Language (*.xml)");
        File fileDirectory  = new File("." + File.separatorChar + "assets" + File.separatorChar);
        File saveFile       = new File("saveme.xml");
        setFileChooserProperty(fileChooser, filter, "Save Avatar Configuration File", fileDirectory);
        
        fileChooser.setSelectedFile(saveFile);

        int retVal = fileChooser.showSaveDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            saveFile = fileChooser.getSelectedFile();
            final File file = saveFile;
            if (character != null) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    character.saveConfiguration(fos);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JAXBException ex) {
                    Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    public static void setFileChooserProperty(JFileChooser fileChooser, FileFilter fileFilter, String dialogTitle, File fileDirectory) {
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setCurrentDirectory(fileDirectory);
        fileChooser.setDoubleBuffered(true);
        fileChooser.setDragEnabled(true);
        fileChooser.addChoosableFileFilter(fileFilter);
    }
    
    public static FileFilter createFileFilter(final String filterExt, final String description) {
        return new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(filterExt)) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = description;
                return szDescription;
            }
        };
    }

    public static JFileChooser getFileChooser() {
        return fileChooser;
    }

    /**
     * Filters the sourceDirectory passed in for image files of type png, jpg &
     * gif and then normalizes the pictures and places them in a processed
     * directory (/assets/processed) and creates a text file with the RGB values
     * of all the processed images (/assets/colors.txt).
     * @param sourceDirectory
     */
    public static void processImages(File sourceDirectory) {
        FilenameFilter filter   = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".png") ||
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") ||
                    name.toLowerCase().endsWith(".gif"))
                    return true;
                return false;
            }
        };

        File[] images           = sourceDirectory.listFiles(filter);
        FileMetrics[] metrics   = new FileMetrics[images.length];
        for (int i = 0; i < images.length; i++) {
            metrics[i]  = new FileMetrics(images[i]);
        }

        List changes    = new ArrayList<Object[]>();
        for (int i = 0; i < images.length; i++) {
            BufferedImage image = ImageLibraryExt.load(images[i].getPath());
            BufferedImage norm  = ImageLibraryExt.normalize(image);

            Object[] objects    = new Object[4];
            objects[0]          = metrics[i].fName;
            for (int j = 0; j < 3; j++) {
                objects[j+1]  = ImageLibraryExt.object[j];
            }
            changes.add(objects);

            File destination    = new File("./assets/processed/" + metrics[i].fName);
            ImageLibraryExt.save(norm, destination, metrics[i].fExt);
        }

        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(new File("./assets/colors.txt")));
            for (int i = 0; i < changes.size(); i++) {
                Object[] data               = (Object[]) changes.get(i);
                StringBuilder saveString    = new StringBuilder();
                saveString.append(data[0])
                          .append("\t\t")
                          .append(data[1])
                          .append("\t")
                          .append(data[2])
                          .append("\t")
                          .append(data[3])
                          .append("\n");
                output.write(saveString.toString());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
//  Helper Classes
////////////////////////////////////////////////////////////////////////////////

    public static class FileMetrics {
        public File    file    = null;
        public String  bPath   = null;
        public String  aPath   = null;
        public String  rPath   = null;
        public String  fName   = null;
        public String  fExt    = null;

        public FileMetrics(File f) {
            file = f;
            setPaths();
            setFileName();
        }

        public FileMetrics copy() {
            return new FileMetrics(this.file);
        }

        private void setPaths() {
            bPath = System.getProperty("user.dir");
            aPath = file.getAbsolutePath();
            rPath = getRelativePath(new File(bPath), file);
        }

        private void setFileName() {
            int bIndex  = file.getAbsolutePath().lastIndexOf(File.separatorChar);
            int cIndex  = file.getAbsolutePath().lastIndexOf(".");
            fName       = file.getAbsolutePath().substring(bIndex + 1);
            fExt        = file.getAbsolutePath().substring(cIndex + 1);
        }
    }
}
