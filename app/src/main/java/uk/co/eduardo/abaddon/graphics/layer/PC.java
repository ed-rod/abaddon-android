package uk.co.eduardo.abaddon.graphics.layer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

import uk.co.eduardo.abaddon.character.CharacterClass;
import uk.co.eduardo.abaddon.graphics.Animation;
import uk.co.eduardo.abaddon.inventory.Inventory;

/**
 * Defines a player character sprite.
 * 
 * @author Ed
 */
public class PC extends Sprite
{
   private static final int MaxItemLimit = 10;
   
   private final Inventory inventory = new Inventory( MaxItemLimit );
   
   private final List< CharacterClass > classes;
   
   /**
    * Construct a Player Character from an animation sequence
    * 
    * @param animation the animated frames for this sprite
    * @param classes the classes the player character fulfils.
    */
   public PC( final Animation animation,
              final List< CharacterClass > classes )
   {
      super( animation );
      this.classes = new ArrayList< CharacterClass >( classes );
   }

   /**
    * Construct a Sprite from an image resource
    * 
    * @param resource the image resource from which to create the sprite
    * @param frameWidth the width of each animation frame in pixels
    * @param frameHeight the height of each animation frame in pixels
    * @param classes the classes the player character fulfils.
    */
   public PC( final Drawable resource,
              final int frameWidth,
              final int frameHeight,
              final List< CharacterClass > classes )
   {
      super( resource, frameWidth, frameHeight );
      this.classes = new ArrayList< CharacterClass >( classes );
   }
   
   /**
    * @return the list of character classes for this player character.
    */
   public List< CharacterClass > getCharacterClasses()
   {
      return this.classes;
   }
   
   /**
    * Adds a character class to this player character.
    * 
    * @param characterClass the class to add.
    */
   public void addCharacterClass( final CharacterClass characterClass )
   {
      if( !this.classes.contains( characterClass ) )
      {
         this.classes.add( characterClass );
      }
   }
   
   /**
    * Removes a character class from this player character.
    * 
    * @param characterClass the class to remove.
    */
   public void removeCharacterClass( final CharacterClass characterClass )
   {
      this.classes.remove( characterClass );
   }
   
   /**
    * @return the inventory for the player character.
    */
   public Inventory getInventory()
   {
      return this.inventory;
   }
}
