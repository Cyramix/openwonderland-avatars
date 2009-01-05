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
package imi.loaders;


import imi.scene.PMatrix;
import imi.scene.PNode;



/**
 * This class is used to represent an type that will be interpreted by the
 * type processor.
 * @author Chris Nagle
 */
public class Instruction extends PNode
{
    /**
     * The types of grouping that can be processed.
     */
    public enum InstructionType
    {
        grouping, // Used to indicate a grouping type node, no payload
        loadHumanoidAvatarBindPose, // Loads an avatar and then sorts its skinned meshes
        loadGeometry, // Load a piece of geometry
        deleteSkinnedMesh, // Remove a particular skinned mesh
        addSkinnedMesh, // Add a skinned mesh to the current skeleton
        addAttachment, // Add an attachment to the current skeleton
        loadAnimation, // Load the specified animation on the current skeleton
        loadFacialAnimation, // Load the specified facial animation on the current skeleton
        setSkeleton, // Set the current skeleton
    }

    /** The type of this type **/
    private InstructionType m_type = null;
    /** Payload **/
    private Object  m_data = null;
     
    /**
     * Construct a new type of grouping type
     */
    public Instruction()
    {
        setInstructionType(InstructionType.grouping);
    }

    /**
     * Construct a new instance of the specified type
     * @param type The type of type this is
     */
    public Instruction(InstructionType instruction)
    {
        setInstructionType(instruction);
    }

    /**
     * Construct a new instance of the specified type with the provided payload.
     * @param type The type of this type
     * @param data The payload
     */
    public Instruction(InstructionType instruction, Object data)
    {
        setInstructionType(instruction);
        setData(data);
    }

    /**
     * Add a new instruction of the specified type with a null data payload as a
     * child of this instruction.
     * @param type
     * @return The newly created instruction
     */
    public Instruction addChildInstructionOfType(InstructionType type)
    {
        Instruction newInstruction = new Instruction(type);
    
        return addInstruction(newInstruction);
    }

    /**
     * Add a new instruction of the specified type with the provided payload as
     * a child of this instruction
     * @param type The type
     * @param data The payload
     * @return The newly created instruction.
     */
    public Instruction addChildInstruction(InstructionType type, Object data)
    {
        Instruction newInstruction = new Instruction(type, data);
        return(addInstruction(newInstruction));
    }

    /**
     * Add the provided instruction as a child of this instance and return it.
     * @param instruction
     * @return
     */
    public Instruction addInstruction(Instruction instruction)
    {
        addChild(instruction);
        return(instruction);
    }

    /**
     * Add an attachment instruction as a child of this instance.
     * @param meshName The name of the mesh to attach
     * @param jointName The name of the joint to attach on
     * @param orientation The transform that should be used for attachment
     */
    public void addAttachmentInstruction( String meshName, String jointName, PMatrix orientation )
    {
        Instruction inst = new Instruction(Instruction.InstructionType.addAttachment);
        
        Object[] array = new Object [3];
        array[0] = meshName;
        array[1] = jointName;
        array[2] = orientation;
        inst.setData(array);
        
        addChild(inst);
    }

    /**
     * Add an addSkinnedMeshInstruction as a child of this instance.
     * @param meshName The mesh to add
     * @param subGroupName The sub group to sort the mesh into
     */
    public void addSkinnedMeshInstruction(String meshName, String subGroupName)
    {
        Instruction inst = new Instruction(InstructionType.addSkinnedMesh);

        Object[] array = new Object[2];
        array[0] = meshName;
        array[1] = subGroupName;
        inst.setData(array);

        addChild(inst);
    }
    
    public InstructionType getInstructionType()
    {
        return m_type;
    }
    public void setInstructionType(InstructionType type)
    {
        m_type = type;
    }



    public String getDataAsString()
    {
        if (m_data != null)
            return(m_data.toString());
        return null;
    }
    
    public Object getData()
    {
        return m_data;
    }
    
    public void setData(Object data)
    {
        m_data = data;
    }

}



