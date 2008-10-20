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


import imi.scene.PNode;



/**
 *
 * @author Chris Nagle
 */
public class Instruction extends PNode
{
    String          m_Instruction;
    Object          m_Data;

    

    //  Constructor.
    public Instruction()
    {
    }
    public Instruction(String instruction)
    {
        setInstruction(instruction);
    }
    public Instruction(String instruction, Object data)
    {
        setInstruction(instruction);
        setData(data);
    }



    //  Adds an Instruction.
    public Instruction addInstruction(String instruction)
    {
        Instruction pNewInstruction = new Instruction(instruction);
    
        return(addInstruction(pNewInstruction));
    }
    
    public Instruction addInstruction(String instruction, Object data)
    {
        Instruction pNewInstruction = new Instruction(instruction, data);
        return(addInstruction(pNewInstruction));
    }
    public Instruction addInstruction(Instruction pInstruction)
    {
        addChild(pInstruction);
        
        return(pInstruction);
    }



    public String getInstruction()
    {
        return(m_Instruction);
    }
    public void setInstruction(String instruction)
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



