/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.utils;

import com.jme.scene.state.CullState;
import imi.repository.Repository;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.shader.AbstractShaderProgram;
import imi.shader.NoSuchPropertyException;
import imi.shader.ShaderProperty;
import imi.shader.dynamic.GLSLDataType;
import imi.shader.programs.ClothingShaderDiffuseAsSpec;
import imi.shader.programs.ClothingShaderSpecColor;
import imi.shader.programs.EyeballShader;
import imi.shader.programs.FleshShader;
import imi.shader.programs.HairShader;
import imi.shader.programs.PhongFleshShader;
import imi.shader.programs.SimpleTNLWithAmbient;
import java.awt.Color;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class MaterialMeshUtils {

    public static enum ShaderType {
        FleshShader(0),
        ClothingShaderSpecColor(1),
        ClothingShaderDiffuseAsSpec(2),
        SimpleTNLWithAmbient(3),
        HairShader(4),
        PhongFleshShader(5),
        EyeballShader(6);

        int shaderIndex;

        ShaderType(int index) {
            this.shaderIndex    = index;
        }
    }
    
    public static enum TextureType {
        Color(0),
        Normal(1),
        Specular(2);
        
        int textureIndex;
        
        TextureType(int index) {
            this.textureIndex   = index;
        }
    }

    /**
     * Creates, sets and updates the shader and color modulation on the specified
     * PPolygonMeshInstance.
     * @param worldManager  - used to grab the repository to get the shaders
     * @param meshInst      - used to get the material and to apply changes
     * @param type          - the specified shader to use
     * @param color         - the specified color to modulate
     */
    public static void setColorOnMeshInstance(WorldManager worldManager, PPolygonMeshInstance meshInst, ShaderType type, Color color) {
        // assign a texture to the mesh instance
        float[] fColorArray             = new float[3];
        color.getRGBColorComponents(fColorArray);

        Repository repo = (Repository)worldManager.getUserData(Repository.class);
        PMeshMaterial material          = meshInst.getMaterialRef();
        AbstractShaderProgram shader    = null;

        try {
            // Setting the new color property onto the model here
            switch(type)
            {
                case FleshShader:
                {
                    shader = repo.newShader(FleshShader.class);
                    shader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    break;
                }
                case ClothingShaderSpecColor:
                {
                    shader = repo.newShader(ClothingShaderSpecColor.class);
                    shader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    break;
                }
                case ClothingShaderDiffuseAsSpec:
                {
                    shader = repo.newShader(ClothingShaderDiffuseAsSpec.class);
                    shader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    break;
                }
                case SimpleTNLWithAmbient:
                {
                    shader = repo.newShader(SimpleTNLWithAmbient.class);
                    shader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    shader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    material.setCullFace(CullState.Face.None);
                    break;
                }
                case HairShader:
                {
                    shader = repo.newShader(HairShader.class);
                    shader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    shader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    material.setCullFace(CullState.Face.None);
                    break;
                }
                case PhongFleshShader:
                {
                    shader = repo.newShader(PhongFleshShader.class);
                    shader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, fColorArray));
                    break;
                }
            }

        } catch (NoSuchPropertyException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        material.setDefaultShader(shader);
        meshInst.applyShader();
    }

    public static void setTextureOnMeshInstance(PPolygonMeshInstance meshInst, String relativeTexturePath, TextureType textureIndex) {
        String baseURL          = System.getProperty("user.dir");
        PMeshMaterial material  = meshInst.getMaterialRef();
        material.setTexture(relativeTexturePath, textureIndex.textureIndex, baseURL);
        meshInst.applyMaterial();
    }

    public static void setShaderOnMeshInstance(WorldManager worldManager, PPolygonMeshInstance meshInst, ShaderType type) {
        // assign a texture to the mesh instance
        Repository repo = (Repository)worldManager.getUserData(Repository.class);
        PMeshMaterial material          = meshInst.getMaterialRef();
        AbstractShaderProgram shader    = null;

        // Setting the new color property onto the model here
        switch(type)
        {
            case FleshShader:
            {
                shader = repo.newShader(FleshShader.class);
                break;
            }
            case ClothingShaderSpecColor:
            {
                shader = repo.newShader(ClothingShaderSpecColor.class);
                break;
            }
            case ClothingShaderDiffuseAsSpec:
            {
                shader = repo.newShader(ClothingShaderDiffuseAsSpec.class);
                break;
            }
            case SimpleTNLWithAmbient:
            {
                shader = repo.newShader(SimpleTNLWithAmbient.class);
                material.setCullFace(CullState.Face.None);
                break;
            }
            case HairShader:
            {
                shader = repo.newShader(HairShader.class);
                material.setCullFace(CullState.Face.None);
                break;
            }
            case PhongFleshShader:
            {
                shader = repo.newShader(PhongFleshShader.class);
                break;
            }
            case EyeballShader:
            {
                shader = repo.newShader(EyeballShader.class);
                break;
            }
        }

        material.setDefaultShader(shader);
        meshInst.applyShader();
    }
}
