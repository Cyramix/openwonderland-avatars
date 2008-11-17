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
 * $Revision$
 * $Date$
 * $State$
 */
package org.collada.xml_walker;


import java.util.logging.Logger;
import org.collada.colladaschema.LibraryNodes;
import org.collada.colladaschema.Node;
import org.collada.colladaschema.InstanceGeometry;
import org.collada.colladaschema.BindMaterial;
import org.collada.colladaschema.BindMaterial.TechniqueCommon;
import org.collada.colladaschema.InstanceMaterial;
import org.collada.colladaschema.InstanceWithExtra;

import imi.loaders.collada.Collada;



/**
 *
 * @author Chris Nagle
 */
public class LibraryNodesProcessor extends Processor
{

    //  Constructor.
    public LibraryNodesProcessor(Collada pCollada, LibraryNodes pNodes, Processor pParent)
    {
        super(pCollada, pNodes, pParent);

        //System.out.println("LibraryNodesProcessor");

        Node pNode;
        PColladaNode pColladaNode;

        //  Populate list of all the Images so we can look them up later.
        for (int a=0; a<pNodes.getNodes().size(); a++)
        {
            pNode = (Node)pNodes.getNodes().get(a);
            
            pColladaNode = new PColladaNode();
            
            processNode("", null, pNode, pColladaNode);
        }

        int a = 0;
    }

    private void processNode(String spacing, Node pParentNode, Node pNode, PColladaNode pColladaNode)
    {
        

        
        pColladaNode.setName(pNode.getId());


        m_pCollada.addFactoryColladaNode(pColladaNode);


        //  Process the instance_geometry property.
        processNodeGeometries(pNode, pColladaNode);


        //  Process the NodeInstance information for the Node.
        processNodeInstance(pNode, pColladaNode);


        //  Process all the child nodes.
        if (pNode.getNodes() != null && pNode.getNodes().size() > 0)
        {
            int a;
            Node pChildNode;
            PColladaNode pChildColladaNode;
            
            for (a=0; a<pNode.getNodes().size(); a++)
            {
                pChildNode = (Node)pNode.getNodes().get(a);
                
                pChildColladaNode = new PColladaNode();
                
                pColladaNode.addChildNode(pChildColladaNode);

                processNode(spacing + "   ", pNode, pChildNode, pChildColladaNode);
            }
        }

        //pCollada.addColladaImage(pImage.getId(), pImage.getInitFrom());
    }
 
    
    private void processNodeGeometries(Node pNode, PColladaNode pColladaNode)
    {
        if (pNode.getInstanceGeometries() != null && pNode.getInstanceGeometries().size() == 0)
            return;

        InstanceGeometry instancedGeometry = null;
        String meshURL = null;


        for (int i = 0; i < pNode.getInstanceGeometries().size(); i++)
        {
            instancedGeometry = (InstanceGeometry)pNode.getInstanceGeometries().get(i);

            meshURL = instancedGeometry.getUrl();
            if (meshURL.startsWith("#"))
                meshURL = meshURL.substring(1, meshURL.length());

            //pColladaNode.setMeshName(pInstanceGeometry.getName());
            pColladaNode.setMeshURL(meshURL);
            pColladaNode.setMeshName(instancedGeometry.getName());

            if (instancedGeometry.getBindMaterial() != null)
            {
                BindMaterial    pBindMaterial  = null;

                pBindMaterial = instancedGeometry.getBindMaterial();

                if (pBindMaterial.getTechniqueCommon() != null)
                {
                    TechniqueCommon pTechniqueCommon = pBindMaterial.getTechniqueCommon();

                    if (pTechniqueCommon.getInstanceMaterials() != null &&
                        pTechniqueCommon.getInstanceMaterials().size() > 0)
                    {
                        InstanceMaterial pInstanceMaterial = null;
                        String materialName = null;

                        for (int j = 0; j < pTechniqueCommon.getInstanceMaterials().size(); j++)
                        {
                            pInstanceMaterial = pTechniqueCommon.getInstanceMaterials().get(j);
                            
                            materialName = pInstanceMaterial.getTarget();
                            if (materialName.startsWith("#"))
                                materialName = materialName.substring(1);
                            if (materialName.endsWith("ID"))
                                materialName = materialName.substring(0, materialName.length()-2);

                            PColladaMaterialInstance pMaterialInstance = m_pCollada.findColladaMaterialInstance(materialName);

                            pColladaNode.setMaterialInstance(pMaterialInstance);
                        }
                    }
                }
            }
        }
    }

    
    //  Processes the NodeInstance information for the Node.
    private void processNodeInstance(Node pNode, PColladaNode pColladaNode)
    {
        if (pNode.getInstanceNodes() != null && pNode.getInstanceNodes().size() > 0)
        {
            int a;
            InstanceWithExtra pInstanceWithExtra;
            String instanceName;
            
            for (a=0; a<pNode.getInstanceNodes().size(); a++)
            {
                pInstanceWithExtra = pNode.getInstanceNodes().get(a);

                instanceName = pInstanceWithExtra.getUrl();
                if (instanceName.startsWith("#"))
                    instanceName = instanceName.substring(1);
            
                pColladaNode.setInstanceNodeName(instanceName);
            }
        }
    }

}





