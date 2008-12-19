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
package imi.scene.utils.tree;

import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.ShaderProperty;
import imi.serialization.xml.bindings.xmlColorRGBA;
import imi.serialization.xml.bindings.xmlJointModification;
import imi.serialization.xml.bindings.xmlMaterial;
import imi.serialization.xml.bindings.xmlMatrix;
import imi.serialization.xml.bindings.xmlShader;
import imi.serialization.xml.bindings.xmlShaderProgram;
import java.util.ArrayList;

/**
 * This class traverses a given tree and accumulates the data needed for
 * serialization.
 * @author Ronald E Dahlgren
 */
public class SerializationHelper implements NodeProcessor
{
    // Accumulate herein!
    private ArrayList<xmlJointModification> modifiers = new ArrayList<xmlJointModification>();
    private ArrayList<xmlMaterial> materials = new ArrayList<xmlMaterial>();

    /**
     * Construct a new instance
     */
    public SerializationHelper()
    {
        // Do nothing special currently
    }

    public boolean processNode(PNode currentNode) 
    {
        if (currentNode instanceof SkinnedMeshJoint)
            processJoint((SkinnedMeshJoint)currentNode);
        else if (currentNode instanceof PPolygonMeshInstance)
            processMeshInstance((PPolygonMeshInstance)currentNode);
        return true;
    }

    public Iterable<xmlJointModification> getJointModifierList()
    {
        return modifiers;
    }

    public Iterable<xmlMaterial> getMaterials() {
        return materials;
    }

    /**
     * Clear out internally cached data.
     */
    public void clearLists()
    {
        modifiers.clear();
        materials.clear();
    }

    private void processJoint(SkinnedMeshJoint joint) {
         // Check to see if the joint contains useful data
        if ((joint.getLocalModifierMatrix() != null) ||
            (joint.getBindPose() != null))
        {
            boolean worthSaving = false; // Useless until proven useful :)

            xmlJointModification jointMod = new xmlJointModification();
            // first write the name
            jointMod.setTargetJointName(joint.getName());

            PMatrix mat = null;
            xmlMatrix xmlMat = null;
            // Local modifier matrix
            if (joint.getLocalModifierMatrix().equals(PMatrix.IDENTITY) == false)
            {
                worthSaving = true;
                // Local modifier matrix
                mat = joint.getLocalModifierMatrix();
                xmlMat = new xmlMatrix();
                xmlMat.set(mat);
                jointMod.setLocalModifierMatrix(xmlMat);
            }

            // Bind pose matrix
            if (joint.getBindPose().equals(PMatrix.IDENTITY) == false)
            {
                worthSaving = true;
                mat = joint.getBindPose();
                xmlMat = new xmlMatrix();
                xmlMat.set(mat);
                jointMod.setBindPoseMatrix(xmlMat);
            }

            if (worthSaving)
                modifiers.add(jointMod);
        }
    }

    private void processMeshInstance(PPolygonMeshInstance meshInst) {
        // process the material
        PMeshMaterial meshMaterial = meshInst.getMaterialRef().getMaterial();
        xmlMaterial xmlMat = new xmlMaterial();
        // Target mesh name
        xmlMat.setTargetMeshName(meshInst.getName());
        // Textures
        if (meshMaterial.getTextures() != null)
        {
            for (TextureMaterialProperties texture : meshMaterial.getTextures())
            {
                if (texture != null)
                    xmlMat.addTexture(texture.generateTexturePropertiesDOM());
            }
        }
        // Diffuse Color
        if (meshMaterial.getDiffuse() != null)
            xmlMat.setDiffuseColor(new xmlColorRGBA(meshMaterial.getDiffuse()));
        else
            xmlMat.setDiffuseColor(null);
        // Ambient Color
        if (meshMaterial.getAmbient() != null)
            xmlMat.setAmbientColor(new xmlColorRGBA(meshMaterial.getAmbient()));
        else
            xmlMat.setAmbientColor(null);
        // Emissive Color
        if (meshMaterial.getEmissive() != null)
            xmlMat.setEmissiveColor(new xmlColorRGBA(meshMaterial.getEmissive()));
        else
            xmlMat.setEmissiveColor(null);
        // Specular Color
        if (meshMaterial.getSpecular() != null)
            xmlMat.setSpecularColor(new xmlColorRGBA(meshMaterial.getSpecular()));
        else
            xmlMat.setSpecularColor(null);
        // Transparency Color
        if (meshMaterial.getTransparencyColor() != null)
            xmlMat.setTransparencyColor(new xmlColorRGBA(meshMaterial.getTransparencyColor()));
        else
            xmlMat.setTransparencyColor(null);
        // Shaders
        if (meshMaterial.getShaders() != null)
        {
            for (AbstractShaderProgram shader : meshMaterial.getShaders())
            {
                if (shader != null)
                    processShader(meshMaterial, meshInst, shader, xmlMat);
            }
        }
        // Shininess
        xmlMat.setShininess(meshMaterial.getShininess());
        // Color Material
        xmlMat.setColorMaterial(meshMaterial.getColorMaterial());
        // Cull Face
        xmlMat.setCullFace(meshMaterial.getCullFace());
        // Name
        xmlMat.setName(meshMaterial.getName());
        materials.add(xmlMat);
    }

    private void processShader(PMeshMaterial meshMaterial, PPolygonMeshInstance meshInst, AbstractShaderProgram shader, xmlMaterial xmlMat)
    {
        xmlShader shaderDOM = new xmlShader();
        // fill it out
        xmlShaderProgram shaderProgramDOM = shader.generateShaderProgramDOM();
        shaderDOM.setProgram(shaderProgramDOM);

        if (shader.getProperties() != null)
        {
            for (ShaderProperty prop : shader.getProperties())
            {
                if (prop.name.equals("pose")) // do not serialize the pose
                    continue;
                else
                    shaderDOM.addShaderProperty(prop.generateShaderPropertyDOM());
            }
        }
        // add it to the xml material
        xmlMat.addShader(shaderDOM);
    }

}
