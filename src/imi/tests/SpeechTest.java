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


import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.statemachine.GameContext;
import imi.scene.animation.speech.PhonemeChain;
import imi.scene.animation.speech.PhonemeChain.Phoneme;
import org.jdesktop.mtgame.WorldManager;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.DahlgrensInput;
import java.util.ArrayList;



/**
 * This test demonstrates how to instantiate an maleAvatar and provide customization
 * through a CharacterAttributes object.
 * @see DemoBase For information about the freebies that class provides
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class SpeechTest extends DemoBase
{
    /**
     * Construct a new instance. This method must be defined to subclass
     * DemoBase.
     * @param args Command-line arguments
     */
    public SpeechTest(String[] args)
    {
        super(args);
    }

    /**
     * Run this file!
     * @param args
     */
    public static void main(String[] args)
    {
        // Give ourselves a nice environment
        String[] ourArgs = new String[] { "-env:assets/models/collada/Environments/Garden/Garden.dae" };
        // Construction does all the work
        SpeechTest test = new SpeechTest(args);
    }

    /**
     * This is the overrride point at which the framework has already been set up.
     * Entities can be created and added to the provided world manager at this point.
     * @param wm
     */
    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // The event processor provides the linkage between AWT events and input controls
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
        // Set the input scheme that we intend to use
        DahlgrensInput control = (DahlgrensInput)eventProcessor.setDefault(new DahlgrensInput(null));

        // Create an attributes object describing the maleAvatar
        // We will use random customizations 
        MaleAvatarAttributes maleAttributes = new MaleAvatarAttributes("RobertTheTestGuy", true);
        Avatar maleAvatar = new Avatar(maleAttributes, wm);
        // control him
        control.setTargetAvatar(maleAvatar);

        // Wait for loading to finish
        while(maleAvatar.isInitialized() == false)
            Thread.yield();
        
        // Speech stuff
        PhonemeChain speech = new PhonemeChain(maleAvatar);
        ArrayList<Phoneme> series = new ArrayList<Phoneme>();
        series.add(Phoneme.Consonant);
        series.add(Phoneme.E);
        series.add(Phoneme.Consonant);
        series.add(Phoneme.FandV);
        series.add(Phoneme.U);
        series.add(Phoneme.Consonant);
        series.add(Phoneme.AI);
        series.add(Phoneme.Consonant);
        
        speech.initiateChain(series, 1.0f);
        // Disable the game context so that we can drive the animations
        maleAvatar.getContext().setEnable(false);
        // Speak publicly
        maleAvatar.getSkeleton().transitionTo("Male_PublicSpeaking", false);
    }
}
