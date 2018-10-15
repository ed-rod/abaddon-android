package uk.co.eduardo.abaddon.inventory;

import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;

import uk.co.eduardo.abaddon.character.CharacterClass;
import uk.co.eduardo.abaddon.graphics.layer.PC;

/**
 * Defines an item that can only be used by restricted character classes.
 * 
 * @author Ed
 */
public class ClassRestrictedItem extends InventoryItem
{
   //================|  Fields             |====================================

   private final List< CharacterClass > classes;

   
   //================|  Constructors       |====================================

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param itemType the type of item
    * @param characterClass the character class which can use this item.
    */
   public ClassRestrictedItem( final String name,
                               final Drawable resource,
                               final InventoryItemType itemType,
                               final CharacterClass characterClass )
   {
      this( name, resource, itemType, Collections.singletonList( characterClass ) );
   }

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param itemType the type of item
    * @param classes the classes to which this item is available.
    */
   public ClassRestrictedItem( final String name,
                               final Drawable resource,
                               final InventoryItemType itemType,
                               final List< CharacterClass > classes )
   {
      super( name, resource, itemType );
      this.classes = classes;
   }

   
   //================|  Public Methods     |====================================

   /**
    * @param characterClass the character's class.
    * @return whether or not this item can be equipped by that character class.
    */
   public boolean isValidClass( final CharacterClass characterClass )
   {
      return this.classes.contains( characterClass );
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canEquip( final PC character )
   {
      if( !super.canEquip( character ) )
      {
         return false;
      }
      // Check to see if the character contains any classes compatible with this item.
      for( final CharacterClass characterClass : character.getCharacterClasses() )
      {
         if( isValidClass( characterClass ) )
         {
            return true;
         }
      }
      return false;
   }
}
