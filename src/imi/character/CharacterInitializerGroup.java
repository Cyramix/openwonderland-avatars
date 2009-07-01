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
package imi.character;

import java.util.List;
import javolution.util.FastTable;

/**
 * This class provides a convenient and simple way to chain together initializers.
 * Implementors of {@code CharacterInitializationInterface} may be added and they
 * will be executed in the order they were added.
 * @author Lou Hayt
 */
public class CharacterInitializerGroup implements CharacterInitializationInterface
{
    // Collection of initializers
    private final List<CharacterInitializationInterface> initers = new FastTable<CharacterInitializationInterface>();

    /**
     * Constructs a new default instance.
     */
    public CharacterInitializerGroup() {}

    /**
     * Constructs a new instance with all of provided initializers.
     * @param initializers A collection of initializers
     */
    public CharacterInitializerGroup(CharacterInitializationInterface... initializers)
    {
        initers.addAll(initers);
    }

    /**
     * Adds an initializer to the collection
     * @param initializer A non-null initializer to add
     * @throws IllegalArgumentException If {@code initializer == null}
     */
    public void addInitializer(CharacterInitializationInterface initializer)
    {
        if (initializer == null)
            throw new IllegalArgumentException("Null param");
        initers.add(initializer);
    }

    /**
     * Removes the specified initializer if found
     * @param init A non-null initializer to remove.
     * @throws IllegalArgumentException If {@code init == null}
     */
    public void removeInitializer(CharacterInitializationInterface init)
    {
        if (init == null)
            throw new IllegalArgumentException("Null param");
        initers.remove(init);
    }

    /**
     * {@inheritDoc CharacterInitializationInterface
     */
    public void initialize(Character character) {
        for(CharacterInitializationInterface i : initers)
            i.initialize(character);
    }

}
