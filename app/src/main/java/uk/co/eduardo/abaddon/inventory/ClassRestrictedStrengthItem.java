package uk.co.eduardo.abaddon.inventory;

import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;

import uk.co.eduardo.abaddon.character.CharacterClass;

/**
 * An abstract item that can only be equipped by certain classes. This item also has an
 * associated strength.
 * 
 * @author Eduardo Rodrigues
 */
public class ClassRestrictedStrengthItem extends ClassRestrictedItem implements StrengthItem
{
   //================|  Fields             |====================================

   private final int strength;
   
   
   //================|  Constructors       |====================================

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param itemType the type of item
    * @param characterClass the classes to which this item is available.
    * @param strength the strength of the item.
    */
   public ClassRestrictedStrengthItem( final String name,
                                       final Drawable resource,
                                       final InventoryItemType itemType,
                                       final CharacterClass characterClass,
                                       final int strength )
   {
      this( name, resource, itemType, Collections.singletonList( characterClass ), strength );
   }

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param itemType the type of item
    * @param classes the classes to which this item is available.
    * @param strength the strength of the item.
    */
   public ClassRestrictedStrengthItem( final String name,
                                       final Drawable resource,
                                       final InventoryItemType itemType,
                                       final List< CharacterClass > classes,
                                       final int strength )
   {
      super( name, resource, itemType, classes );
      this.strength = strength;
   }

   
   //================|  Public Methods     |====================================

   /**
    * {@inheritDoc}
    */
   @Override
   public int getStrength()
   {
      return this.strength;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int compareTo( final InventoryItem another )
   {
      int compare = super.compareTo( another );
      if( compare == 0 && another instanceof StrengthItem )
      {
         compare = this.strength - ( (StrengthItem) another ).getStrength();
      }
      return compare;
   }
}
