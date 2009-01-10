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
package imi.tests;

import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Box;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryImporter;
import imi.scene.PScene;
import java.net.URL;
import java.util.ArrayList;
import org.jdesktop.mtgame.*;

/**
 * This class provides a simple test ground for loading a texture and bump
 * mapping it as well as specular mapping
 * @author Ronald E Dahlgren
 */
public class TextureBinaryLoad extends DemoBase {

    public TextureBinaryLoad(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        TextureBinaryLoad worldTest = new TextureBinaryLoad(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) {

        Vector3f max = new Vector3f(2, 2, 2);
        Vector3f min = new Vector3f(-2, -2, -2);

        Box t = new Box("Box", min, max);
        t.setModelBound(new BoundingSphere());
        t.updateModelBound();

        t.setLocalTranslation(new Vector3f(0, 0, 0));
        pscene.getJScene().attachChild(t);

        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        ts.setEnabled(true);
        Texture t1 = TextureManager.loadTexture("/work/Projects/Avatars/trunk/assets/models/collada/Avatars/Male/MaleCBodyCLRSweater.png",
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        ts.setTexture(t1, 0);

        BinaryImporter bim = new BinaryImporter();
        try {
        URL urlC = new URL("file:///work/Projects/Avatars/trunk/texture.bin");
        Texture t2 = (Texture) bim.load(urlC);
        
//        Texture t2 = TextureManager.loadTexture("/work/Projects/Avatars/trunk/texture4.bin"),
//                Texture.MinificationFilter.BilinearNearestMipMap,
//                Texture.MagnificationFilter.Bilinear);
        
        ts.setTexture(t2, 1);
        }catch (Exception e) { e.printStackTrace(); }
        t.copyTextureCoordinates(0, 1, 1.0f);
        pscene.getJScene().setRenderState(ts);
        
        WireframeState ws = (WireframeState) wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(false);
        t.setRenderState(ts);
        t.setRenderState(ws);
        pscene.getJScene().setRenderState(ws);
        pscene.getJScene().updateRenderState();
    }
}
