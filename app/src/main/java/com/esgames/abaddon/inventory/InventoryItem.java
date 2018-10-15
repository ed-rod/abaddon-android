/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.inventory;

import android.graphics.drawable.Drawable;

import com.esgames.abaddon.graphics.layer.PC;

/**
 * An item that can appear in a character's inventory.
 * 
 * @author Eduardo Rodrigues
 */
public class InventoryItem implements Comparable< InventoryItem >
{
   //================|  Fields             |====================================
   
   private final String name;
   
   private final Drawable resource;
   
   private final InventoryItemType itemType;
   
   
   //================|  Constructors       |====================================
   
   /**
    * @param name the display name of the item.
    * @param resource the drawable resource for the inventory item.
    * @param itemType the type of item
    */
   public InventoryItem( final String name,
                         final Drawable resource,
                         final InventoryItemType itemType )
   {
      this.name = name;
      this.resource = resource;
      this.itemType = itemType;
   }
   
   
   //================|  Public Methods     |====================================

   /**
    * @return the name of the item.
    */
   public String getName()
   {
      return this.name;
   }
   
   /**
    * @return the resource for the item.
    */
   public Drawable getResource()
   {
      return this.resource;
   }
   
   /**
    * @return the type of the inventory item.
    */
   public InventoryItemType getItemType()
   {
      return this.itemType;
   }
   
   /**
    * @return whether or not this item can be equipped.
    */
   public final boolean isEquippable()
   {
      switch( this.itemType )
      {
         case Weapon:
         case Armor:
         case Shield:
         case Helmet:
            return true;
            
         default:
            return false;
      }
   }
   
   /**
    * @return whether or not this item can be used.
    */
   public boolean isUsable()
   {
      return this.itemType == InventoryItemType.Item;
   }
   
   /**
    * Determines whether a player character can equip a certain item.
    * 
    * @param character the player character.
    * @return whether or not the item may be equipped by the specified character.
    */
   public boolean canEquip( final PC character )
   {
      return isEquippable();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int compareTo( final InventoryItem another )
   {
      if( another != null )
      {
         return this.itemType.ordinal() - another.itemType.ordinal();
      }
      // Shunt nulls to the end.
      return -1;
   }
}
