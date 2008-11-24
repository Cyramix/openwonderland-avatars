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
package imi.character.ninja;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.util.ArrayList;
import java.util.LinkedList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class NinjaAvatar extends Ninja 
{
    public class NinjaAvatarAttributes extends Attributes
    {   
        public NinjaAvatarAttributes(String name, boolean bRandomCustomizations) 
        {
            super(name);
            
            // Bind Pose
            setBindPoseFile("assets/models/collada/Avatars/Male/MaleBind.dae");
            
            // Animations
            ArrayList<String> anims = new ArrayList<String>();
            anims.add("assets/models/collada/Avatars/Male/Male_Idle.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_StandToSit.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Wave.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_FallFromSitting.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_FloorSitting.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_FloorGetup.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Walk.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_Sitting.dae");
            if (false)
            {
                anims.add("assets/models/collada/Avatars/Male/Male_Run.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Bow.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Cheer.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Clap.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Follow.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Jump.dae");
                anims.add("assets/models/collada/Avatars/Male/Male_Laugh.dae");
            }
            setAnimations(anims.toArray(new String[anims.size()]));
            
            // Facial Animations
            ArrayList<String> facialAnims = new ArrayList<String>();
            facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleSmile.dae");
            facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleFrown.dae");
            facialAnims.add("assets/models/collada/Avatars/MaleFacialAnimation/MaleScorn.dae");
            setFacialAnimations(facialAnims.toArray(new String[facialAnims.size()]));
            
            // Customizations
            if (bRandomCustomizations)
            {
                int preset        = -1;
                int numberOfFeet  = 3;
                int numberOfLegs  = 3;
                int numberOfTorso = 3;
                int numberOfHair  = 3;
             
                ArrayList<String> delete    = new ArrayList<String>();
                ArrayList<String> load      = new ArrayList<String>();
                ArrayList<String> add       = new ArrayList<String>();
                ArrayList<AttachmentParams> attachments = new ArrayList<AttachmentParams>();
                
                preset = (int) (Math.random() * 1000000 % numberOfFeet);
                customizeFeetPresets(preset, delete, load, add, attachments);
                preset = (int) (Math.random() * 1000000 % numberOfLegs);
                customizeLegsPresets(preset, delete, load, add, attachments);
                preset = (int) (Math.random() * 1000000 % numberOfTorso);
                customizeTorsoPresets(preset, delete, load, add, attachments);
                preset = (int) (Math.random() * 1000000 % numberOfHair);
                customizeHairPresets(preset, delete, load, add, attachments);
                
                setDeleteInstructions(delete.toArray(new String[delete.size()]));
                setLoadInstructions(load.toArray(new String[load.size()]));
                setAddInstructions(add.toArray(new String[add.size()]));
                setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
            }
        }

        private void customizeFeetPresets(int preset, ArrayList<String> delete, ArrayList<String> load, ArrayList<String> add, ArrayList<AttachmentParams> attachments) 
        {
            switch(preset)
            {
                case 0:
                {
                    // Tennis shoes
                    delete.add("LFootNudeShape");
                    delete.add("RFootNudeShape");
                    load.add("assets/models/collada/Shoes/TennisShoes_M/MaleTennisShoes.dae");
                    add.add("TennisShoesShape");
                }
                break;
                case 1:
                {
                    // Flip flops
                    delete.add("LFootNudeShape");
                    delete.add("RFootNudeShape");
                    load.add("assets/models/collada/Clothing/FlipFlopsFeet.dae");
                    add.add("LFootNudeShape");
                    add.add("RFootNudeShape");
                    add.add("LFlipFlopShape");
                    add.add("RFlipFlopShape");
                }
                break;
                case 2:
                {
                    // Dress shoes
                    delete.add("LFootNudeShape");
                    delete.add("RFootNudeShape");
                    load.add("assets/models/collada/Shoes/DressShoes_M/MaleDressShoes.dae");
                    add.add("polySurfaceShape3");
                }
                break;
            }   
        }

        private void customizeHairPresets(int preset, ArrayList<String> delete, ArrayList<String> load, ArrayList<String> add, ArrayList<AttachmentParams> attachments) 
        {
            PMatrix oreintation = new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.05f, 1.05f, 1.05f), Vector3f.ZERO);
            switch(preset)
            {
                case 0:
                {
                    load.add("assets/models/collada/Hair/HairPlaceable.dae");
                    attachments.add(new AttachmentParams("curly", "Head", oreintation));
                }
                break;
                case 1:
                {
                    load.add("assets/models/collada/Hair/HairPlaceable.dae");
                    attachments.add(new AttachmentParams("Hair_ShavedFlatTop", "Head", oreintation));
                }
                break;
                case 2:
                {
                    load.add("assets/models/collada/Hair/HairPlaceable.dae");
                    attachments.add(new AttachmentParams("Hair_Faux", "Head", oreintation));
                }
                break;
            }   
        }

        private void customizeLegsPresets(int preset, ArrayList<String> delete, ArrayList<String> load, ArrayList<String> add, ArrayList<AttachmentParams> attachments) 
        {
            switch(preset)
            {
                case 0:
                {
                    // Jeans
                    delete.add("LegsNudeShape");
                    load.add("assets/models/collada/Pants/Jeans_M/Jeans.dae");
                    add.add("polySurface3Shape");
                }
                break;
                case 1:
                {
                    // Shorts
                    delete.add("LegsNudeShape");
                    load.add("assets/models/collada/Pants/Shorts_M/Shorts.dae");
                    add.add("LegsNudeShape");
                    add.add("MaleShortsShape");
                }
                break;
                case 2:
                {
                    // Dress pants
                    delete.add("LegsNudeShape");
                    load.add("assets/models/collada/Pants/DressPants_M/MaleDressPants1.dae");
                    add.add("Legs_LegsNudeShape");
                }
                break;
            }   
        }

        protected void customizeTorsoPresets(int preset, ArrayList<String> delete, ArrayList<String> load, ArrayList<String> add, ArrayList<AttachmentParams> attachments) 
        {
            //preset = 2;
            
            switch(preset)
            {
                case 0:
                {
                    // T Shirt
                    delete.add("TorsoNudeShape");
                    load.add("assets/models/collada/Shirts/TShirt_M/MaleTShirt.dae");
                    add.add("PoloShape");
                    add.add("ArmsShape");
                }
                break;
                case 1:
                {
                    // Polo Strips
                    delete.add("TorsoNudeShape");
                    load.add("assets/models/collada/Shirts/PoloShirt_M/MalePolo.dae");
                    add.add("PoloShape");
                    add.add("TorsoNudeShape");
                }
                break;
                case 2:
                {
                    // Dress shirt
                    delete.add("TorsoNudeShape");
                    load.add("assets/models/collada/Shirts/DressShirt_M/MaleDressShirt.dae");
                    add.add("DressShirtShape");
                }
                break;
            }   
        }
    }
    
    public NinjaAvatar(String name, WorldManager wm) 
    {
        super(name, wm);
        
        // Tweak animation names and speeds
        m_context.getController().setReverseHeading(true);
        m_context.getStates().get(IdleState.class).setAnimationName("Male_Idle");
        m_context.getStates().get(PunchState.class).setAnimationName("Male_Wave");
        m_context.getStates().get(TurnState.class).setAnimationName("Male_Idle");
        m_context.getStates().get(WalkState.class).setAnimationName("Male_Walk");
        m_context.getStates().get(SitState.class).setAnimationName("Male_StandToSit");
        m_context.getStates().get(FlyState.class).setAnimationName("Male_Sitting");
        ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Male_Sitting");
        ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("Male_StandToSit");    
        
        // Test
        //m_context.getStates().get(PunchState.class).setAnimationName("MaleSmile");
        
        // For testing, no transitions
        if (false)
        {
            m_context.getStates().get(IdleState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(WalkState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(TurnState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(SitState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(PunchState.class).setTransitionDuration(0.0f);
            m_context.getStates().get(FlyState.class).setTransitionDuration(0.0f);
            ((SitState)m_context.getStates().get(SitState.class)).setGettingUpTransitionDuration(0.0f);
            ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingTransitionDuration(0.0f);
        }
        
        // For testing
        //m_context.getStates().get(PunchState.class).setAnimationSpeed(1.0f);
        if (false)
        {
            m_context.getStates().get(IdleState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(PunchState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(TurnState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(WalkState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(SitState.class).setAnimationName("Male_Walk");
            m_context.getStates().get(FlyState.class).setAnimationName("Male_Walk");
            ((SitState)m_context.getStates().get(SitState.class)).setIdleSittingAnimationName("Male_Walk");
            ((SitState)m_context.getStates().get(SitState.class)).setGettingUpAnimationName("Male_Walk"); 
        }
    }
        
    @Override
    protected Attributes createAttributes(String name)
    {
        return new NinjaAvatarAttributes(name, true);
    }
    
}
