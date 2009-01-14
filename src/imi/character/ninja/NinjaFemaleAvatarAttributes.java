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
package imi.character.ninja;

import com.jme.math.Vector3f;
import imi.character.AttachmentParams;
import imi.character.CharacterAttributes;
import imi.scene.PMatrix;
import java.util.ArrayList;

/**
 * This class represents concrete attribute settings for the NinjaAvatar. It is
 * basically a well-defined CharacterAttributes starting point for using the
 * primary avatar geometry and animations.
 * @author Lou Hayt
 */
public class NinjaFemaleAvatarAttributes extends CharacterAttributes
{
    public String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

    /**
     * Construct a new attributes instance.
     * @param name The name of the avatar
     * @param bRandomCustomizations ignored (test)
     * @param bForceDressShirt ignored (test)
     */
    public NinjaFemaleAvatarAttributes(String name, boolean bRandomCustomizations, boolean bForceDressShirt) 
    {
        super(name);
        setGender(2);
        loadDefaultBind();
    }

    public NinjaFemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair) 
    {
        super(name);
        setGender(2);
        loadDefaultBind();
    }
    
    private void customizeFeetPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Tennis shoes
                load.add(new String("assets/models/collada/Shoes/TennisShoes_M/MaleTennisShoes.dae"));
                add.add(new SkinnedMeshParams("TennisShoesShape", "Feet"));
            }
            break;
            case 1:
            {
                // Flip flops
                load.add(new String("assets/models/collada/Clothing/FlipFlopsFeet.dae"));
                add.add(new SkinnedMeshParams("LFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("RFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("LFlipFlopShape", "Feet"));
                add.add(new SkinnedMeshParams("RFlipFlopShape", "Feet"));
            }
            break;
            case 2:
            {
                // Dress shoes
                load.add(new String("assets/models/collada/Shoes/DressShoes_M/MaleDressShoes.dae"));
                add.add(new SkinnedMeshParams("polySurfaceShape3", "Feet"));
            }
            break;
        }   
    }

    private void customizeHairPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        // load the head, eyes, teeth, and tongue
        load.add(new String("assets/models/collada/Heads/FemaleCaucasian/FemaleCHead.dae"));
        add.add(new SkinnedMeshParams("rightEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("leftEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("UpperTeethShape", "Head"));
        add.add(new SkinnedMeshParams("LowerTeethShape", "Head"));
        add.add(new SkinnedMeshParams("TongueGeoShape", "Head"));
        add.add(new SkinnedMeshParams("HeadGeoShape", "Head"));

        PMatrix oreintation = new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.05f, 1.05f, 1.05f), Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                load.add(new String("assets/models/collada/Hair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("curly", "Head", oreintation));
            }
            break;
            case 1:
            {
                load.add(new String("assets/models/collada/Hair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_ShavedFlatTop", "Head", oreintation));
            }
            break;
            case 2:
            {
                load.add(new String("assets/models/collada/Hair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Faux", "Head", oreintation));
            }
            break;
        }   
    }

    private void customizeLegsPresets(int preset,  ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Jeans
                load.add(new String("assets/models/collada/Pants/Jeans_M/Jeans.dae"));
                add.add(new SkinnedMeshParams("polySurface3Shape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Shorts
                load.add(new String("assets/models/collada/Pants/Shorts_M/Shorts.dae"));
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("MaleShortsShape", "LowerBody"));
            }
            break;
            case 2:
            {
                // Dress pants
                load.add(new String("assets/models/collada/Pants/DressPants_M/MaleDressPants1.dae"));
                add.add(new SkinnedMeshParams("Legs_LegsNudeShape", "LowerBody"));
            }
            break;
        }   
    }

    protected void customizeTorsoPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        // Add the hands in either way
        load.add(new String("assets/models/collada/Avatars/Male/Female_Hands.dae")); // change!
        add.add(new SkinnedMeshParams("RHandShape",  "Hands"));
        add.add(new SkinnedMeshParams("LHandShape",  "Hands"));

        switch(preset)
        {
            case 0:
            {
                // T Shirt
                load.add(new String("assets/models/collada/Shirts/TShirt_M/MaleTShirt.dae"));
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("ArmsShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Polo Strips
                load.add(new String("assets/models/collada/Shirts/PoloShirt_M/MalePolo.dae"));
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Dress shirt
                load.add(new String("assets/models/collada/Shirts/DressShirt_M/MaleDressShirt.dae"));
                add.add(new SkinnedMeshParams("DressShirtShape", "UpperBody"));
            }
            break;
        }   
    }

    private void loadDefaultBind() {
        ArrayList<String> load      = new ArrayList<String>();
        load.add(new String("assets/models/collada/Avatars/Female/Female_Bind.dae")); // change!

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("rightEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("leftEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("UpperTeethShape", "Head"));
        add.add(new SkinnedMeshParams("LowerTeethShape", "Head"));
        add.add(new SkinnedMeshParams("TongueGeoShape", "Head"));
        add.add(new SkinnedMeshParams("HeadGeoShape", "Head"));
        add.add(new SkinnedMeshParams("HandsShape",  "Hands"));
        add.add(new SkinnedMeshParams("ShoesShape",  "Feet"));
        add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
        add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }
}
