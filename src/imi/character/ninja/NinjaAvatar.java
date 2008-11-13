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
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class NinjaAvatar extends Ninja 
{
    public class NinjaAvatarAttributes extends Attributes
    {   
        public NinjaAvatarAttributes(String name, int preset) 
        {
            super(name);
            
            // Bind Pose
            setBindPoseFile("assets/models/collada/Avatars/MaleZip/MaleBind.dae");
            //setBindPoseFile("assets/models/collada/Avatars/Male2/Male_Bind.dae");
            
            // Animations
            ArrayList<String> anims = new ArrayList<String>();
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Idle.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_StandToSit.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Wave.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_FallFromSitting.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_FloorSitting.dae");
            anims.add("assets/models/collada/Avatars/Male/Male_FloorGetup.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Walk.dae");
            anims.add("assets/models/collada/Avatars/MaleZip/Male_Sitting.dae");
            if (false)
            {
                anims.add("assets/models/collada/Avatars/MaleZip/Male_Run.dae");
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
            switch(preset)
            {
                case 0:
                {
                    // Jeans and shoes
                    ArrayList<String> delete = new ArrayList<String>();
                    delete.add("TorsoNudeShape");
                    delete.add("LegsNudeShape");
                    delete.add("LFootNudeShape");
                    delete.add("RFootNudeShape");
                    setDeleteInstructions(delete.toArray(new String[delete.size()]));

                    ArrayList<String> load = new ArrayList<String>();
                    load.add("assets/models/collada/Pants/Jeans_M/Jeans.dae");
                    load.add("assets/models/collada/Shoes/TennisShoes_M/MaleTennisShoes.dae");
                    load.add("assets/models/collada/Shirts/TShirt_M/MaleTShirt.dae");
                    load.add("assets/models/collada/Hair/HairPlaceable.dae");
                    setLoadInstructions(load.toArray(new String[load.size()]));

                    ArrayList<String> add = new ArrayList<String>();
                    add.add("polySurface3Shape");
                    add.add("TennisShoesShape");
                    add.add("PoloShape");
                    add.add("ArmsShape");
                    setAddInstructions(add.toArray(new String[add.size()]));

                    ArrayList<AttachmentParams> attachments = new ArrayList<AttachmentParams>();
                    attachments.add(new AttachmentParams("curly", "Head", new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.0f, 1.2f, 1.0f), Vector3f.ZERO)));
                    setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
                }
                break;
                case 1:
                {
                    // Shorts and flip flops
                    ArrayList<String> delete = new ArrayList<String>();
                    delete.add("TorsoNudeShape");
                    delete.add("LegsNudeShape");
                    delete.add("LFootNudeShape");
                    delete.add("RFootNudeShape");
                    setDeleteInstructions(delete.toArray(new String[delete.size()]));

                    ArrayList<String> load = new ArrayList<String>();
                    load.add("assets/models/collada/Pants/Shorts_M/Shorts.dae");
                    load.add("assets/models/collada/Clothing/FlipFlopsFeet.dae");
                    load.add("assets/models/collada/Shirts/PoloShirt_M/MalePolo.dae");
                    load.add("assets/models/collada/Hair/HairPlaceable.dae");
                    setLoadInstructions(load.toArray(new String[load.size()]));

                    ArrayList<String> add = new ArrayList<String>();
                    add.add("LegsNudeShape");
                    add.add("MaleShortsShape");
                    add.add("LFootNudeShape");
                    add.add("RFootNudeShape");
                    add.add("LFlipFlopShape");
                    add.add("RFlipFlopShape");
                    add.add("PoloShape");
                    add.add("TorsoNudeShape");
                    setAddInstructions(add.toArray(new String[add.size()]));
                    
                    ArrayList<AttachmentParams> attachments = new ArrayList<AttachmentParams>();
                    attachments.add(new AttachmentParams("Hair_ShavedFlatTop", "Head", new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.0f, 1.2f, 1.0f), Vector3f.ZERO)));
                    setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
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
        int preset = -1;
        if (Math.random() < 0.5)
            preset = 0;
        else 
            preset = 1;
        return new NinjaAvatarAttributes(name, preset);
    }

}
