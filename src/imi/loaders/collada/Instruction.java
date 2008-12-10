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
package imi.loaders.collada;


import imi.scene.PMatrix;
import imi.scene.PNode;



/**
 * This class is used to represent an instruction that will be interpreted by the
 * instruction processor.
 * @author Chris Nagle
 */
public class Instruction extends PNode
{
    private InstructionNames    m_Instruction = null;
    private Object              m_Data = null;

    public enum InstructionNames
    {
        instructions,
        loadBindPose,
        loadGeometry,
        deleteSkinnedMesh,
        addSkinnedMesh,
        addAttachment,
        loadAnimation,
        loadFacialAnimation,
        setSkeleton,
    }
     
    //  Constructor.
    public Instruction()
    {
        setInstruction(InstructionNames.instructions);
    }
    public Instruction(InstructionNames instruction)
    {
        setInstruction(instruction);
    }
    public Instruction(InstructionNames instruction, Object data)
    {
        setInstruction(instruction);
        setData(data);
    }



    //  Adds an Instruction.
    public Instruction addInstruction(InstructionNames instruction)
    {
        Instruction pNewInstruction = new Instruction(instruction);
    
        return(addInstruction(pNewInstruction));
    }

    /**
     * This method is used to create, add to the tree, and return a new instruction.
     * @param instruction
     * @param data
     * @return
     */
    public Instruction addInstruction(InstructionNames instruction, Object data)
    {
        Instruction pNewInstruction = new Instruction(instruction, data);
        return(addInstruction(pNewInstruction));
    }

    /**
     * Add a
     * @param pInstruction
     * @return
     */
    public Instruction addInstruction(Instruction pInstruction)
    {
        addChild(pInstruction);
        
        return(pInstruction);
    }

    /**
     *
     * @param meshName
     * @param jointName
     * @param orientation
     */
    public void addAttachmentInstruction( String meshName, String jointName, PMatrix orientation )
    {
        Instruction inst = new Instruction(Instruction.InstructionNames.addAttachment);
        
        Object[] array = new Object [3];
        array[0] = meshName;
        array[1] = jointName;
        array[2] = orientation;
        inst.setData(array);
        
        addChild(inst);
    }

    /**
     *
     * @param meshName
     * @param subGroupName
     */
    public void addSkinnedMeshInstruction(String meshName, String subGroupName)
    {
        Instruction inst = new Instruction(InstructionNames.addSkinnedMesh);
        Object[] array = new Object[2];
        array[0] = meshName;
        array[1] = subGroupName;
        inst.setData(array);
        addChild(inst);
    }
    
    public InstructionNames getInstruction()
    {
        return(m_Instruction);
    }
    public void setInstruction(InstructionNames instruction)
    {
        m_Instruction = instruction;
    }



    public String getDataAsString()
    {
        if (m_Data != null)
            return(m_Data.toString());
        return null;
    }
    
    public Object getData()
    {
        return m_Data;
    }
    
    public void setData(Object data)
    {
        m_Data = data;
    }

}



