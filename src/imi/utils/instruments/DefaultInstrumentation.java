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
package imi.utils.instruments;

import com.jme.math.Vector3f;
import imi.character.CharacterParams;
import imi.character.avatar.Avatar;
import imi.character.FemaleAvatarParams;
import imi.character.MaleAvatarParams;
import imi.scene.PMatrix;
import java.util.Map;
import javolution.util.FastMap;
import org.jdesktop.mtgame.WorldManager;

/**
 * The default implementation of the instrumentation interface
 * @author Ronald E Dahlgren
 */
public class DefaultInstrumentation implements Instrumentation
{
    /** The manager of the world.  **/
    private WorldManager worldManager = null;
    /** State mapping **/
    private Map<InstrumentedSubsystem, Boolean> enabledMapping =
            new FastMap<InstrumentedSubsystem, Boolean>();
    /** The default to use for instanced avatars **/
    private CharacterParams defaultInstancedAttributes = null;

    public DefaultInstrumentation(WorldManager wm)
    {
        worldManager = wm;
        worldManager.addUserData(Instrumentation.class, this);
        // create the default attributes
        defaultInstancedAttributes = new MaleAvatarParams("InstanceMan!").build(false);

        enabledMapping.put(InstrumentedSubsystem.AnimationSystem, true);
        enabledMapping.put(InstrumentedSubsystem.PoseTransferToGPU, true);
        enabledMapping.put(InstrumentedSubsystem.Texturing, true);
        enabledMapping.put(InstrumentedSubsystem.VertexDeformation, true);
    }

    @Override
    public boolean addInstancedAvatar(Vector3f translation) {
        defaultInstancedAttributes.setOrigin(new PMatrix(translation));
        Avatar newAvatar = new Avatar.AvatarBuilder(defaultInstancedAttributes, worldManager)
                                     .build();
        if (newAvatar != null)
            return true;
        return false;
    }

    @Override
    public boolean addNonInstancedAvatar(Vector3f translation) {
        FemaleAvatarParams female = new FemaleAvatarParams("Chica").build();
        female.setOrigin(new PMatrix(translation));
        Avatar newAvatar = new Avatar.AvatarBuilder(female, worldManager)
                                     .build();
        if (newAvatar != null)
            return true;
        return false;
    }

    @Override
    public boolean addNonInstancedAvatar(CharacterParams specification, Vector3f translation) {
        return false;
    }

    @Override
    public boolean disableSubsytem(InstrumentedSubsystem system) {
        boolean result = false;
        switch (system)
        {
            case AnimationSystem:
                result = disableAnimationSystem();
                break;
            case PoseTransferToGPU:
                result  = disablePoseTransfer();
                break;
            case Texturing:
                result = disableTexturing();
                break;
            case VertexDeformation:
                result = disableVertexDeforming();
                break;
            default: // Shouldn't get here
                break;
        }
        return result;
    }

    @Override
    public boolean enableSubsystem(InstrumentedSubsystem system) {
        boolean result = false;
        switch (system)
        {
            case AnimationSystem:
                result = enableAnimationSystem();
                break;
            case PoseTransferToGPU:
                result  = enablePoseTransfer();
                break;
            case Texturing:
                result = enableTexturing();
                break;
            case VertexDeformation:
                result = enableVertexDeforming();
                break;
            default: // Shouldn't get here
                break;
        }
        return result;
    }

    @Override
    public boolean enableAllSubsystems() {
        return false;
    }

    @Override
    public boolean disableAllSubsystems() {
        return false;
    }

    @Override
    public boolean isSubsystemEnabled(InstrumentedSubsystem system) {
        Boolean result = enabledMapping.get(system);
        if (result != null)
            return result;
        return false;
    }

    private boolean disableAnimationSystem() {
        enabledMapping.put(InstrumentedSubsystem.AnimationSystem, false);
        return true;
    }

    private boolean disablePoseTransfer() {
        enabledMapping.put(InstrumentedSubsystem.PoseTransferToGPU, false);
        return true;
    }

    private boolean disableTexturing() {
        return true;
    }

    private boolean disableVertexDeforming() {
        return true;
    }

    private boolean enableAnimationSystem() {
        enabledMapping.put(InstrumentedSubsystem.AnimationSystem, true);
        return true;
    }

    private boolean enablePoseTransfer() {
        enabledMapping.put(InstrumentedSubsystem.PoseTransferToGPU, true);
        return true;
    }

    private boolean enableTexturing() {
        return true;
    }

    private boolean enableVertexDeforming() {
        return true;
    }
}
