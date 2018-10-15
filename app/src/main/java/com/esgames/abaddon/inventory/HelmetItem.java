/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.inventory;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.esgames.abaddon.character.CharacterClass;


/**
 * A item that can be equipped on the head to offer protection.
 * 
 * @author Eduardo Rodrigues
 */
public class HelmetItem extends ClassRestrictedStrengthItem
{
   //================|  Constructors       |====================================

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param characterClass the classes to which this item is available.
    * @param strength the strength of the item.
    */
   public HelmetItem( final String name,
                      final Drawable resource,
                      final CharacterClass characterClass,
                      final int strength )
   {
      super( name, resource, InventoryItemType.Helmet, characterClass, strength );
   }

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param classes the classes to which this item is available.
    * @param strength the strength of the item.
    */
   public HelmetItem( final String name,
                      final Drawable resource,
                      final List< CharacterClass > classes,
                      final int strength )
   {
      super( name, resource, InventoryItemType.Helmet, classes, strength );
   }
}
