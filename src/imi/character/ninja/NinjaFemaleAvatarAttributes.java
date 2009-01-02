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
     * @param bRandomCustomizations If false, avatar starts in the bind pose, if true then random clothing will be applied
     * @param bForceDressShirt If true, the dress shirt will be applied (overriding randomizations)
     */
    public NinjaFemaleAvatarAttributes(String name, boolean bRandomCustomizations, boolean bForceDressShirt) 
    {
        super(name);

        // Bind Pose
        setBindPoseFile("assets/models/collada/Avatars/Female/Female_Bind.dae");

        // Animations
        ArrayList<String> anims = new ArrayList<String>();
        anims.add("assets/models/collada/Avatars/Female/Female_Anim_Idle.dae");
        anims.add("assets/models/collada/Avatars/Female/Female_Anim_Sitting.dae");
        anims.add("assets/models/collada/Avatars/Female/Female_Anim_StandToSit.dae");
        anims.add("assets/models/collada/Avatars/Female/Female_Anim_Walk.dae");
//        anims.add("assets/models/collada/Avatars/female/Male_Anim_Wave.dae");
//        anims.add("assets/models/collada/Avatars/female/Male_Anim_FallFromSitting.dae");
//        anims.add("assets/models/collada/Avatars/female/Male_Anim_FloorSitting.dae");
//        anims.add("assets/models/collada/Avatars/female/Male_Anim_FloorGetup.dae");
        setAnimations(anims.toArray(new String[anims.size()]));

        // Facial Animations
        ArrayList<String> facialAnims = new ArrayList<String>();
//        facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleSmile.dae");
//        facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleFrown.dae");
//        facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleScorn.dae");
//        facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleDefault.dae");
        setFacialAnimations(facialAnims.toArray(new String[facialAnims.size()]));

        // Customizations
        if (false)//bRandomCustomizations)
        {
            int preset        = -1;
            int numberOfFeet  = 3;
            int numberOfLegs  = 3;
            int numberOfTorso = 3;
            int numberOfHair  = 3;

            ArrayList<String> delete    = new ArrayList<String>();
            ArrayList<String[]> load      = new ArrayList<String[]>();
            ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
            ArrayList<AttachmentParams> attachments = new ArrayList<AttachmentParams>();

            preset = (int) (Math.random() * 1000000 % numberOfFeet);
            customizeFeetPresets(preset, delete, load, add, attachments);
            preset = (int) (Math.random() * 1000000 % numberOfLegs);
            customizeLegsPresets(preset, delete, load, add, attachments);
            if (bForceDressShirt)
                customizeTorsoPresets(2, delete, load, add, attachments);
            else
            {
                preset = (int) (Math.random() * 1000000 % numberOfTorso);
                customizeTorsoPresets(preset, delete, load, add, attachments);
            }
            preset = (int) (Math.random() * 1000000 % numberOfHair);
            customizeHairPresets(preset, delete, load, add, attachments);

            setDeleteInstructions(delete.toArray(new String[delete.size()]));
            String[][] att = new String[load.size()][2];
            for (int i = 0; i < load.size(); i++) {
                for (int j = 0; j < 2; j++) {
                    att[i][j] = load.get(i)[j];
                }
            }
            setLoadInstructions(att);
            setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
            setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
        }
    }

    private void customizeFeetPresets(int preset, ArrayList<String> delete, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Tennis shoes
                delete.add("LFootNudeShape");
                delete.add("RFootNudeShape");
                load.add(new String[] {"assets/models/collada/Shoes/TennisShoes_M/MaleTennisShoes.dae", m_regions[4] });
                add.add(new SkinnedMeshParams("TennisShoesShape", "Feet"));
            }
            break;
            case 1:
            {
                // Flip flops
                delete.add("LFootNudeShape");
                delete.add("RFootNudeShape");
                load.add(new String[] {"assets/models/collada/Clothing/FlipFlopsFeet.dae", m_regions[4] });
                add.add(new SkinnedMeshParams("LFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("RFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("LFlipFlopShape", "Feet"));
                add.add(new SkinnedMeshParams("RFlipFlopShape", "Feet"));
            }
            break;
            case 2:
            {
                // Dress shoes
                delete.add("LFootNudeShape");
                delete.add("RFootNudeShape");
                load.add(new String[] {"assets/models/collada/Shoes/DressShoes_M/MaleDressShoes.dae", m_regions[4] });
                add.add(new SkinnedMeshParams("polySurfaceShape3", "Feet"));
            }
            break;
        }   
    }

    private void customizeHairPresets(int preset, ArrayList<String> delete, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        PMatrix oreintation = new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.05f, 1.05f, 1.05f), Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                load.add(new String[] {"assets/models/collada/Hair/HairPlaceable.dae", m_regions[5] });
                attachments.add(new AttachmentParams("curly", "Head", oreintation));
            }
            break;
            case 1:
            {
                load.add(new String[] {"assets/models/collada/Hair/HairPlaceable.dae", m_regions[5] });
                attachments.add(new AttachmentParams("Hair_ShavedFlatTop", "Head", oreintation));
            }
            break;
            case 2:
            {
                load.add(new String[] {"assets/models/collada/Hair/HairPlaceable.dae", m_regions[5] });
                attachments.add(new AttachmentParams("Hair_Faux", "Head", oreintation));
            }
            break;
        }   
    }

    private void customizeLegsPresets(int preset, ArrayList<String> delete, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Jeans
                delete.add("LegsNudeShape");
                load.add(new String[] {"assets/models/collada/Pants/Jeans_M/Jeans.dae", m_regions[3] });
                add.add(new SkinnedMeshParams("polySurface3Shape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Shorts
                delete.add("LegsNudeShape");
                load.add(new String[] {"assets/models/collada/Pants/Shorts_M/Shorts.dae", m_regions[3] });
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("MaleShortsShape", "LowerBody"));
            }
            break;
            case 2:
            {
                // Dress pants
                delete.add("LegsNudeShape");
                load.add(new String[] {"assets/models/collada/Pants/DressPants_M/MaleDressPants1.dae", m_regions[3] });
                add.add(new SkinnedMeshParams("Legs_LegsNudeShape", "LowerBody"));
            }
            break;
        }   
    }

    protected void customizeTorsoPresets(int preset, ArrayList<String> delete, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        //preset = 2;

        switch(preset)
        {
            case 0:
            {
                // T Shirt
                delete.add("TorsoNudeShape");
                load.add(new String[] {"assets/models/collada/Shirts/TShirt_M/MaleTShirt.dae", m_regions[2] });
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("ArmsShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Polo Strips
                delete.add("TorsoNudeShape");
                load.add(new String[] {"assets/models/collada/Shirts/PoloShirt_M/MalePolo.dae", m_regions[2] });
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Dress shirt
                delete.add("TorsoNudeShape");
                load.add(new String[] {"assets/models/collada/Shirts/DressShirt_M/MaleDressShirt.dae", m_regions[2] });
                add.add(new SkinnedMeshParams("DressShirtShape", "UpperBody"));
            }
            break;
        }   
    }
}