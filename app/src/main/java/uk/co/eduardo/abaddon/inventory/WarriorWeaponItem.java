package uk.co.eduardo.abaddon.inventory;

import android.graphics.drawable.Drawable;

import uk.co.eduardo.abaddon.character.CharacterClass;

/**
 * A item used for attack that can only be wielded by warriors.
 * 
 * @author Eduardo Rodrigues
 */
public class WarriorWeaponItem extends WeaponItem
{
   //================|  Constructors       |====================================

   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param strength the strength of the item.
    */
   public WarriorWeaponItem( final String name, final Drawable resource, final int strength )
   {
      super( name, resource, CharacterClass.Warrior, strength );
   }
}
