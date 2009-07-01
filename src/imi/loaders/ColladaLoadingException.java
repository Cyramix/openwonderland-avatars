/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.loaders;

/**
 *
 * @author Ronald E Dahlgren
 */
public class ColladaLoadingException extends Exception
{

    public ColladaLoadingException(String message, Throwable exception) {
        super(message, exception);
    }

    public ColladaLoadingException(String message) {
        super(message);
    }

    public ColladaLoadingException() {
    }

}
