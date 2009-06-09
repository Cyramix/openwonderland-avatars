/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.utils;

import com.jme.image.Texture.MinificationFilter;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.ColladaLoadingException;
import imi.loaders.repository.Repository;
import imi.scene.PScene;
import imi.scene.animation.AnimationCycle;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationState;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.shader.programs.EyeballShader;
import imi.scene.shader.programs.FleshShader;
import imi.scene.shader.programs.PhongFleshShader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Paul Viet Nguyen Truong & Ronald E Dahlgren
 */
public class BinaryExporterImporter {
    private static final Logger logger = Logger.getLogger(BinaryExporterImporter.class.getName());
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private Logger      m_logger                = Logger.getLogger(BinaryExporterImporter.class.getName());

    private URL         m_skeletonLocation      = null;
    private String[]    m_bodyAnimationFiles    = null;
    private String[]    m_facialAnimationFiles  = null;
    private File        m_outputFile            = null;
    private String      m_URLBase               = null;
    private float       m_animationQuality      = 0.9f;

    public SkeletonNode m_currentSkeleton       = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    /**
     * Default constructor does NOTHING.  If you use this then use the mutators
     * to set all the member variables before you serialize the data.
     */
    public BinaryExporterImporter() {

    }

    public BinaryExporterImporter(URL skeletonLoc, ArrayList<String> bodyAnimations, ArrayList<String> facialAnimations, File outputLoc, String urlBase) {
        m_skeletonLocation  = skeletonLoc;
        if (bodyAnimations != null) {
            m_bodyAnimationFiles = new String[bodyAnimations.size()];
            bodyAnimations.toArray(m_bodyAnimationFiles);
        }
        if (facialAnimations != null) {
            m_facialAnimationFiles = new String[facialAnimations.size()];
            facialAnimations.toArray(m_facialAnimationFiles);
        }

        m_outputFile        = outputLoc;
        m_URLBase           = urlBase;
    }

    public BinaryExporterImporter(URL skeletonLoc, String[] bodyAnimations, String[] facialAnimations, File outputLoc, String urlBase) {
        m_skeletonLocation      = skeletonLoc;
        m_bodyAnimationFiles    = bodyAnimations;
        m_facialAnimationFiles  = facialAnimations;
        m_outputFile            = outputLoc;
        m_URLBase               = urlBase;
    }

    public BinaryExporterImporter(String relativeSkelPath, String[] bodyAnimations, String[] facialAnimations, File outputLoc, String urlBase) {
        try {
            m_skeletonLocation = new URL(urlBase + relativeSkelPath);
        } catch (MalformedURLException ex) {
            Logger.getLogger(BinaryExporterImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_bodyAnimationFiles    = bodyAnimations;
        m_facialAnimationFiles  = facialAnimations;
        m_outputFile            = outputLoc;
        m_URLBase               = urlBase;
    }

    public BinaryExporterImporter(String relativeSkelPath, ArrayList<String> bodyAnimations, ArrayList<String> facialAnimations, File outputLoc, String urlBase) {
        try {
            m_skeletonLocation = new URL(urlBase + relativeSkelPath);
        } catch (MalformedURLException ex) {
            Logger.getLogger(BinaryExporterImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (bodyAnimations != null) {
            m_bodyAnimationFiles = new String[bodyAnimations.size()];
            bodyAnimations.toArray(m_bodyAnimationFiles);
        }
        if (facialAnimations != null) {
            m_facialAnimationFiles = new String[facialAnimations.size()];
            facialAnimations.toArray(m_facialAnimationFiles);
        }
        m_outputFile    = outputLoc;
        m_URLBase       = urlBase;
    }

    public void serialize(WorldManager wm) {

        // Create parameters for the collada loader we will use
        ColladaLoaderParams params  = new ColladaLoaderParams(true,     true,   // load skeleton,   load geometry
                                                              false,    false,  // load animations, show debug info
                                                              4,                // max influences per-vertex
                                                              "Skeleton",       // 'name'
                                                              null);            // existing skeleton (if applicable)

        // Load the skeleton
        Collada loader              = new Collada(params);
        try {
            loader.load(new PScene(wm), m_skeletonLocation);
        }
        catch (ColladaLoadingException ex)
        {
            logger.severe(ex.getMessage());
        }
        m_currentSkeleton           = loader.getSkeletonNode();
        m_currentSkeleton.setName(m_outputFile.getName());

        // Create animation instruction & processor
        InstructionProcessor processor      = new InstructionProcessor(wm);
        processor.setUseBinaryFiles(false);
        Instruction animationInstruction    = new Instruction();                                        // Grouping instruction node
        animationInstruction.addChildInstruction(Instruction.InstructionType.setSkeleton, m_currentSkeleton);    // Set the m_currentSkeleton we just got

        // Set animation instructions
        if (m_bodyAnimationFiles != null)
            setInstructions(animationInstruction, Instruction.InstructionType.loadAnimation, m_bodyAnimationFiles);         // setting body animations
        if (m_facialAnimationFiles != null)
            setInstructions(animationInstruction, Instruction.InstructionType.loadFacialAnimation, m_facialAnimationFiles); // Setting facial animations

        processor.execute(animationInstruction);    // execute the processes

        // Optimize all of the cycles
        for (int i = 0; i < m_currentSkeleton.getAnimationGroupCount(); i++) {
            for (AnimationCycle cycle : m_currentSkeleton.getAnimationGroup(i).getCycles())
                    cycle.optimizeChannels(m_animationQuality);
        }

        // The real serializtion write out
        serializeIT(m_currentSkeleton, m_outputFile);
        System.out.println("Binary export complete");
    }

    public void serializeBinaryHead(SkeletonNode skeleton, WorldManager wm) {
        SkeletonNode copy = skeleton.deepCopy(new PScene(wm));
        copy.clearSubGroup("UpperBody");
        copy.clearSubGroup("Hair");

        AnimationGroup bodyAnims    = copy.getAnimationGroup(0);
        copy.getAnimationComponent().removeGroup(bodyAnims);

        serializeIT(copy, m_outputFile);
        System.out.println("Binary Export complete");
    }

    public SkeletonNode processBinaryData(String relativePath) {
        URL resourcePath    = null;
        String basePath     = "file:///" + System.getProperty("user.dir");
        try {   // try looking for local file...
            resourcePath            = new URL(basePath + relativePath);
            InputStream inStream    = resourcePath.openStream();
            inStream.close();
        } catch (MalformedURLException ex) {    // try looking inside jar files
            m_logger.log(Level.SEVERE, null, ex);
            resourcePath = getClass().getResource(relativePath);
            if (resourcePath == null)
                return null;
        } catch (IOException ex) {  // try looking inside jar files
            m_logger.log(Level.SEVERE, null, ex);
            resourcePath = getClass().getResource(relativePath);
            if (resourcePath == null)
                return null;
        }
        
        return processBinaryData(resourcePath);
    }

    public SkeletonNode processBinaryData(URL resourcePath) {
        ObjectInputStream inStream  = null;
        SkeletonNode skeleton       = null;

        try {
            inStream = new AvatarObjectInputStream(resourcePath.openStream());
            skeleton = (SkeletonNode) inStream.readObject();
            inStream.close();

        } catch (Exception ex) {
            m_logger.severe("Error loading binary file: " + ex.getMessage()+"  "+resourcePath.toExternalForm());
            ex.printStackTrace();
        }

        return skeleton;
    }

    public void attatchHeadSkeleton(SkeletonNode bodySkeleton, SkeletonNode headSkeleton, PScene pscene, WorldManager wm) {
        List<PPolygonSkinnedMeshInstance> skinnedMeshList           = headSkeleton.getSkinnedMeshInstances();
        SkinnedMeshJoint copyJoint                                  = headSkeleton.getSkinnedMeshJoint("Neck");

        SkinnedMeshJoint originalJoint                              = bodySkeleton.getSkinnedMeshJoint("Neck");
        bodySkeleton.clearSubGroup("Head");
        bodySkeleton.getAnimationGroup(1).clear();
        
        originalJoint.getParent().replaceChild(originalJoint, copyJoint, false);
        bodySkeleton.refresh();

        if (skinnedMeshList.size() == 0)
            logger.warning("No skinned mesh instances found in skeleton. Do you have meshes instead?");

        for (PPolygonSkinnedMeshInstance meshInst : skinnedMeshList)
            bodySkeleton.addToSubGroup(meshInst, "Head");

        for (int i = 0; i < headSkeleton.getAnimationGroupCount(); i++) {
            for (AnimationCycle cycle : headSkeleton.getAnimationGroup(i).getCycles())
                bodySkeleton.getAnimationGroup(1).addCycle(cycle);
        }

        // synch up animation states with groups
        while (bodySkeleton.getAnimationComponent().getGroupCount() < bodySkeleton.getAnimationStateCount())
            bodySkeleton.addAnimationState(new AnimationState(bodySkeleton.getAnimationStateCount()));

        //setDefaultHeadShaders(wm, bodySkeleton, null, 0);
        //pscene.addInstanceNode(bodySkeleton);
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public URL getSkeletonLocation() {
        return m_skeletonLocation;
    }

    public String[] getBodyAnimFiles() {
        return m_bodyAnimationFiles;
    }

    public String[] getFacialAnimFiles() {
        return m_facialAnimationFiles;
    }

    public File getOutputLocation() {
        return m_outputFile;
    }

    public String getURLBase() {
        return m_URLBase;
    }

    public float getAnimQuality() {
        return m_animationQuality;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setSkeletonLocation(URL skeletonLocation) {
        m_skeletonLocation = skeletonLocation;
    }

    public void setSkeletonLocation(String skeletonLocation) {
        try {
            m_skeletonLocation = new URL(skeletonLocation);
        } catch (MalformedURLException ex) {
            m_logger.log(Level.SEVERE, null, ex);
        }
    }

    public void setBodyAnimFiles(String[] bodyAnimationFiles) {
        m_bodyAnimationFiles = bodyAnimationFiles;
    }

    public void setBodyAnimFiles(ArrayList<String> bodyAnimationFiles) {
        bodyAnimationFiles.toArray(m_bodyAnimationFiles);
    }

    public void setFacialAnimFiles(String[] facialAnimationFiles) {
        m_facialAnimationFiles = facialAnimationFiles;
    }

    public void setFacialAnimFiles(ArrayList<String> facialAnimationFiles) {
        facialAnimationFiles.toArray(m_facialAnimationFiles);
    }

    public void setOutputLocation(File outputFile) {
        m_outputFile = outputFile;
    }

    public void setOutputLocation(String relativePath) {
        m_outputFile = new File(relativePath);
    }

    public void setURLBase(String urlBase) {
        m_URLBase = urlBase;
    }

    public void setAnimQuality(float animationQuality) {
        m_animationQuality = animationQuality;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void setDefaultHeadShaders(WorldManager wm, SkeletonNode skeleton, float[] skinColor, int shaderType) {
        Repository repo = (Repository) wm.getUserData(Repository.class);
        Class fleshShader   = null;

        switch(shaderType)
        {
            case 0:
            {
                fleshShader = FleshShader.class;
                break;
            }
            case 1:
            {
                fleshShader = PhongFleshShader.class;
                break;
            }
        }

        Iterable<PPolygonSkinnedMeshInstance> smInstances = skeleton.retrieveSkinnedMeshes("Head");
        if (smInstances == null) // no subgroup found
            m_logger.warning("No \"Head\" meshes found!");
        else
        {
            for (PPolygonSkinnedMeshInstance meshInst : smInstances) {

                PMeshMaterial meshMat = meshInst.getMaterialRef();
                String tempName = meshInst.getName().toLowerCase();

                // is this an eyeball? (also used for tongue and teeth)
                if (tempName.contains("eyegeoshape")) {
                    meshMat.setShader(repo.newShader(EyeballShader.class));
                    if (meshMat.getTexture(0) != null)
                        meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                } else if (tempName.contains("tongue") || tempName.contains("teeth")) {
                    if (meshMat.getTexture(0) != null)
                        meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                    meshMat.setShader(repo.newShader(EyeballShader.class));
                } else {
                    meshMat.setShader(repo.newShader(fleshShader));
                }
                // Apply it!
                meshInst.applyShader();
                meshInst.applyMaterial();
            }
        }
    }

    private void setInstructions(Instruction instruction, Instruction.InstructionType type, String[] resources) {
        if (resources != null) {
            URL resourcePath = null;
            for (String filePath : resources) {
                if (m_URLBase != null) {
                    instruction.addChildInstruction(type, m_URLBase + filePath);
                } else {
                    resourcePath = getClass().getResource(filePath);
                    if (resourcePath != null) {
                        instruction.addChildInstruction(type, resourcePath);
                    }
                }
            }
        }
    }

    private void setInstruction(Instruction instruction, Instruction.InstructionType type, String resource) {
        String[] singleResource = new String[] { resource };
        setInstructions(instruction, type, singleResource);
    }

    private void serializeIT(SkeletonNode skeleton, File outputFile) {
        FileOutputStream fos            = null;
        AvatarObjectOutputStream out    = null;
        try {

          fos = new FileOutputStream(outputFile);
          out = new AvatarObjectOutputStream(fos);
          out.writeObject(skeleton);
          out.close();

        } catch(IOException ex) {
            m_logger.severe("Serialization problem encountered : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
