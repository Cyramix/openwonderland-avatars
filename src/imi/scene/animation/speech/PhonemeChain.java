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
package imi.scene.animation.speech;

import imi.character.Character;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.TransitionCommand;
import imi.scene.animation.TransitionQueue;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles giving a character a chain of phoneme commands.
 * @author Ronald E Dahlgren
 */
public class PhonemeChain
{
    /** list of phonemes to use **/
    public enum Phoneme
    {
        AI,
        E,
        FandV,
        L,
        MBP,
        O,
        U,
        WQ,
        Consonant
    }

    /** Mapping of the animations to the **/
    private Map<Phoneme, Integer> phonemeToAnimationIndex = new HashMap<Phoneme, Integer>();
    /** The avatar that will be speaking  **/
    private Character speaker = null;
    /** The animation queue for facial animations **/
    private TransitionQueue facialAnimations = null;

    /**
     * Construct a new PhonemeChain with the specified speaker as a target.
     * @param speaker
     */
    public PhonemeChain(Character speaker)
    {
        this.speaker = speaker;
        facialAnimations = speaker.getFacialAnimationQ();
        AnimationComponent animationComponent = speaker.getSkeleton().getAnimationComponent();

        // Set up mapping of animation names to indices
        if (speaker.getAttributes().isMale())
        {
            phonemeToAnimationIndex.put(Phoneme.AI, animationComponent.findCycle("Male_Pho_AI", 1));
            phonemeToAnimationIndex.put(Phoneme.E, animationComponent.findCycle("Male_Pho_E", 1));
            phonemeToAnimationIndex.put(Phoneme.FandV, animationComponent.findCycle("Male_Pho_FandV", 1));
            phonemeToAnimationIndex.put(Phoneme.L, animationComponent.findCycle("Male_Pho_L", 1));
            phonemeToAnimationIndex.put(Phoneme.MBP, animationComponent.findCycle("Male_Pho_MBP", 1));
            phonemeToAnimationIndex.put(Phoneme.O, animationComponent.findCycle("Male_Pho_O", 1));
            phonemeToAnimationIndex.put(Phoneme.U, animationComponent.findCycle("Male_Pho_U", 1));
            phonemeToAnimationIndex.put(Phoneme.WQ, animationComponent.findCycle("Male_Pho_WQ", 1));
            phonemeToAnimationIndex.put(Phoneme.Consonant, animationComponent.findCycle("Male_Pho_Cons", 1));
        }
        else
        {
            // No female phonemes yet
        }
    }

    /**
     * Start a chain of phoneme animations
     * @param phonemes
     */
    public void initiateChain(Iterable<Phoneme> phonemes, float delayBetweenElements)
    {
        TransitionCommand nextCommand = null;
        for (Phoneme phoneme : phonemes)
        {
            nextCommand = new TransitionCommand(phonemeToAnimationIndex.get(phoneme),
                                                delayBetweenElements,
                                                PlaybackMode.Loop);
            facialAnimations.addTransition(nextCommand);
        }
    }
}
