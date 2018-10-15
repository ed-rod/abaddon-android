package uk.co.eduardo.abaddon.character;

import java.util.ArrayList;
import java.util.List;

import uk.co.eduardo.abaddon.graphics.layer.Sprite;
import uk.co.eduardo.abaddon.inventory.Inventory;

/**
 * Represents a player character.
 * 
 * @author Ed
 */
public class PlayerCharacter implements Character
{
   private final ArrayList< CharacterClass > characterClasses = new ArrayList< CharacterClass >();
   
   private final Sprite sprite;
   
   private final Inventory inventory;
   
   /**
    * Creates a new player character.
    * 
    * @param sprite the player character's sprite.
    * @param inventoryItemCount the maximum number of items that can be held in this character's
    *                           inventory.
    */
   public PlayerCharacter( final Sprite sprite, final int inventoryItemCount )
   {
      this.sprite = sprite;
      this.inventory = new Inventory( inventoryItemCount );
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isCharacterClass( final CharacterClass characterClass )
   {
      return this.characterClasses.contains( characterClass );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List< CharacterClass > getCharacterClasses()
   {
      return this.characterClasses;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addCharacterClass( final CharacterClass characterClass )
   {
      if( characterClass != null && !this.characterClasses.contains( characterClass ) )
      {
         this.characterClasses.add( characterClass );
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeCharacterClass( final CharacterClass characterClass )
   {
      this.characterClasses.remove( characterClass );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Sprite getSprite()
   {
      return this.sprite;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Inventory getInventory()
   {
      return this.inventory;
   }
}
