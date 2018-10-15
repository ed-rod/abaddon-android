/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.inventory;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.esgames.abaddon.character.CharacterClass;


/**
 * A item used for attack.
 * 
 * @author Eduardo Rodrigues
 */
public class WeaponItem extends ClassRestrictedStrengthItem
{
   //================|  Constructors       |====================================

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param characterClass the classes to which this item is available.
    * @param strength the strength of the item.
    */
   public WeaponItem( final String name,
                      final Drawable resource,
                      final CharacterClass characterClass,
                      final int strength )
   {
      super( name, resource, InventoryItemType.Weapon, characterClass, strength );
   }

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param classes the classes to which this item is available.
    * @param strength the strength of the item.
    */
   public WeaponItem( final String name,
                      final Drawable resource,
                      final List< CharacterClass > classes,
                      final int strength )
   {
      super( name, resource, InventoryItemType.Weapon, classes, strength );
   }
}
