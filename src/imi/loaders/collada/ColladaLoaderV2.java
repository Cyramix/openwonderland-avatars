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
package imi.loaders.collada;

import imi.scene.PScene;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.collada.colladaschema.*;
import org.collada.colladaschema.COLLADA.Scene;
import org.jdesktop.mtgame.WorldManager;

/**
 * The new and improved COLLADA loader.
 * --- WORK IN PROGRESS ---
 * @author Ronald E Dahlgren
 */
public class ColladaLoaderV2
{
    /** The PScene object to instantiate into **/
    private PScene m_pscene = null;
    /** Convenience reference to the WorldManager **/
    private WorldManager m_WM = null;
    /** The root node for the COLLADA document **/
    private COLLADA m_documentRoot = null;

    /** Library references **/
    private LibraryCameras      m_libraryCameras       = null;
    private LibraryImages       m_libraryImages        = null;
    private LibraryEffects      m_libraryEffects       = null;
    private LibraryMaterials    m_libraryMaterials     = null;
    private LibraryAnimations   m_libraryAnimations    = null;
    private LibraryVisualScenes m_libraryVisualScenes  = null;
    private LibraryGeometries   m_libraryGeometries    = null;
    private LibraryControllers  m_libraryControllers   = null;
    private LibraryNodes        m_libraryNodes         = null;

    /**
     * Construct a new instance of the loader
     * @param wm
     */
    public ColladaLoaderV2(WorldManager wm)
    {
        m_WM = wm;
        m_pscene = new PScene("Collada Fodder PScene", wm);
    }

    /**
     * Load the specified collada file.
     * @param file
     * @throws javax.xml.bind.JAXBException
     */
    public void loadCollada(URL file) throws JAXBException
    {
        // Out with any residual state
        clear();
        // Use a new PScene
        m_pscene = new PScene("Collada Fodder PScene", m_WM);
        // Let JAXB have a go at it
        javax.xml.bind.JAXBContext jc = javax.xml.bind.JAXBContext.newInstance("org.collada.colladaschema");
        javax.xml.bind.Unmarshaller unmarshaller = jc.createUnmarshaller();
        m_documentRoot = (org.collada.colladaschema.COLLADA) unmarshaller.unmarshal(file);

        instantiateDocument();
    }

    /**
     * Clear out any residual state, maintaining only the WorldManager reference
     */
    public void clear()
    {
        m_pscene = null;
        m_documentRoot = null;
    }

    /**
     * Instantiate the target scenes of the current collada file.
     */
    private void instantiateDocument()
    {
        // instantiate the scene into the pscene
        Scene colladaScene = m_documentRoot.getScene();
        if (colladaScene != null)
        {
            // Instantiate the visual scene
            InstanceWithExtra visualScene = colladaScene.getInstanceVisualScene();
            instantiateVisualScene(visualScene);
            // Instantiate the physical scenes
            List<InstanceWithExtra> physicsScenes = colladaScene.getInstancePhysicsScenes();
            if (physicsScenes != null)
            {
                for (InstanceWithExtra physicsScene : physicsScenes)
                    instantiatePhysicsScene(physicsScene);
            }
        }

    }

    /**
     * This method builds the requested physics scene
     * @param physicsScene
     */
    private void instantiatePhysicsScene(InstanceWithExtra physicsScene)
    {
        // TODO
    }

    /**
     * Instantiate the provided scene into the PScene member variable
     * @param visualScene
     */
    private void instantiateVisualScene(InstanceWithExtra visualSceneInstance)
    {
        // The URL will point to the appropriate visual scene to instantiate
        //VisualScene theScene = ColladaConvenienceUtils.findVisualSceneInLibrary(visualSceneInstance.getUrl(), m_documentRoot.);
    }
}
