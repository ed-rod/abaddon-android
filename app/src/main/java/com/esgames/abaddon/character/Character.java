/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.character;

import java.util.List;

import com.esgames.abaddon.graphics.layer.Sprite;
import com.esgames.abaddon.inventory.Inventory;

/**
 * Represents a character in the game.
 * 
 * @author Eduardo Rodrigues
 */
public interface Character
{
   /**
    * Tests whether or not the character is of a certain class.
    * 
    * @param characterClass the class to test.
    * @return whether or not the character is of that class.
    */
   boolean isCharacterClass( final CharacterClass characterClass );
   
   
   /**
    * Gets all of the character classes supported by this character.
    * <p>
    * Never returns <code>null</code>.
    * 
    * @return the list of all the character classes of this character.
    */
   List< CharacterClass > getCharacterClasses();
   
   /**
    * Adds a character class for this character.
    * <p>
    * If the character class is <code>null</code> or if the character is already of that class
    * (see {@link #isCharacterClass(CharacterClass)}, then calling this method has no effect. 
    * </p>
    * 
    * @param characterClass the class to add.
    */
   void addCharacterClass( final CharacterClass characterClass );
   
   /**
    * Removes a character class for this character.
    * <p>
    * If the character class is <code>null</code> or if the character is not already of that class
    * (see {@link #isCharacterClass(CharacterClass)}, then calling this method has no effect.
    * </p>
    * 
    * @param characterClass the class to remove.
    */
   void removeCharacterClass( final CharacterClass characterClass );
   
   /**
    * Gets the character's sprite.
    * 
    * @return the character's sprite.
    */
   Sprite getSprite();
   
   /**
    * Gets the character's inventory.
    * <p>
    * Never returns <code>null</code>.
    * </p>
    * 
    * @return the character's inventory.
    */
   Inventory getInventory();
}
