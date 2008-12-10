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

import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Lou Hayt
 */
public class MsgBox 
{
    ArrayList<String> messages = new ArrayList<String>();
    
   /** 
    *   use this constructor to initizalize a message object
    */
   public MsgBox()
   {

   }
   
   /** 
    *   use this constructor to display a single message
    * @param message - the single message to display
    */
   public MsgBox(String message)
   {
        messages.add(message);
        JOptionPane.showMessageDialog(null, message);
   }
   
   /** 
    *   add a message to display in a list fashion
    * @param message - the message to add to the list
    */
   public void addMessage(String message)
   {
       if (message != null)
       {
            messages.add(message + "\n");   
       }
   }
   
   /** 
    *   clears all the messages in the list
    */
   public void clearAllMessages(String message)
   {
       messages.clear();
   }
   
   /** 
    *   displays the message list in a message box with an OK button
    */
   public void displayAllMessages()
   {
       String message = "";
       
       for (int i = 0; i < messages.size(); i++)
       {
           message += messages.get(i);
       }
       
       if (message != null)
       {
           JOptionPane.showMessageDialog(null, message);
       }
   }
   
}
