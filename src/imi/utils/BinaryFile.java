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


import java.io.BufferedInputStream;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class provides a simple framework for handling binary files
 * @author Chris Nagle
 * @author Ronald E Dahlgren
 */
public class BinaryFile
{
    /** The location of this file **/
    private URL m_location = null;
    /** The stream we use to retrieve data from **/
    BufferedInputStream     m_BufferedStream = null;
    /** Buffer of bytes! **/
    byte[]  m_ByteData = null;



    /**
     * Construct a new instance
     */
    public BinaryFile()
    {

    }



    /**
     * Attempt to open a stream to the specified location
     * @param location
     * @return
     */
    public boolean open(URL location)
    {
        try
        {
            m_BufferedStream = new BufferedInputStream(location.openStream());

            m_location = location;
            
            m_ByteData = new byte[1024];
            return(true);
        }
        catch (Exception e)
        {
            System.out.println("File Error:  Unable to open file '" + location.toString() + "'!");
            //System.out.println(e.getMessage());
            return(false);
        }
    }


    /**
     * Close the current connection and clear out all persistent state.
     */
    public void close()
    {
        if (m_BufferedStream != null)
        {
            try
            {
                m_BufferedStream.close();
            } catch (IOException ex)
            {
                Logger.getLogger(BinaryFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        m_BufferedStream = null;
        m_location = null;
        m_ByteData = null;
        
    }
    
    //  Reads in an int.
    public int readInt()
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, 4);
            int Value = (int)arr2long(m_ByteData, 0);
            return(Value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return(0);
        }
    }


    //  Reads in a short.
    public int readShort()
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, 2);
            int Value = (int)arr2int(m_ByteData, 0);
            return(Value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return(0);
        }
    }


    //  Reads in an unsigned short.
    public int readUShort()
    {
        return(readShort());
    }


    //  Reads in a float.
    public float readFloat()
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, 4);
            float fValue = arr2float(m_ByteData, 0);
            return(fValue);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return(0.0f);
        }
    }
    

    //  Reads in a double.
    public double readDouble()
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, 8);
            double fValue = arr2double(m_ByteData, 0);
            return(fValue);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return(0.0);
        }
    }
    
    public String readString(int charArrayLength)
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, charArrayLength);
            for (int a=0; a<charArrayLength; a++)
            {
                if (m_ByteData[a] == 0)
                {
                    charArrayLength = a;
                    break;
                }
            }

            String Value = new String(m_ByteData, 0, charArrayLength);
            return(Value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return("");
        }
    }
    
    
    public int readChar()
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, 1);
            int Value = (int)m_ByteData[0];
            return(Value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return(0);
        }
    }

    public int readUChar()
    {
        try
        {
            int BytesRead = m_BufferedStream.read(m_ByteData, 0, 1);
            int Value = (m_ByteData[0] & 0xFF);
            return(Value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return(0);
        }
    }

    public Vector2f readVector2f()
    {
        Vector2f Value = new Vector2f();
        try
        {
            Value.x = readFloat();
            Value.y = readFloat();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(Value);
    }

    public Vector3f readVector3f()
    {
        Vector3f Value = new Vector3f();
        try
        {
            Value.x = readFloat();
            Value.y = readFloat();
            Value.z = readFloat();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(Value);
    }
    
    public ColorRGBA readColorRGBA()
    {
        ColorRGBA Value = new ColorRGBA();
        try
        {
            Value.r = readFloat();
            Value.g = readFloat();
            Value.b = readFloat();
            Value.a = readFloat();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(Value);
    }    
    


    public static long arr2long (byte[] arr, int start)
    {
        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++)
        {
            tmp[cnt] = arr[i];
            cnt++;
        }
        long accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 )
        {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return accum;
    }

    public static int arr2int (byte[] arr, int start)
    {
        int low = arr[start] & 0xff;
        int high = arr[start+1] & 0xff;
        return (int)( high << 8 | low );
    }

    public static float arr2float (byte[] arr, int start)
    {
        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++)
        {
            tmp[cnt] = arr[i];
            cnt++;
        }
        int accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 )
        {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return Float.intBitsToFloat(accum);
    }

    public static double arr2double (byte[] arr, int start)
    {
        int i = 0;
        int len = 8;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++)
        {
            tmp[cnt] = arr[i];
            //System.out.println(java.lang.Byte.toString(arr[i]) + " " + i);
            cnt++;
        }
        long accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 64; shiftBy += 8 )
        {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return Double.longBitsToDouble(accum);
    }

}



