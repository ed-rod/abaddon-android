package uk.co.eduardo.abaddon.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An inventory of items.
 * 
 * @author Ed
 */
public class Inventory
{
   //================|  Fields             |====================================

   private final List< InventoryEntry > entries = new ArrayList< InventoryEntry >();
   
   private final int entryLimit;
   
   private WeaponItem equippedWeapon;
   
   private ArmorItem equippedArmor;
   
   private ShieldItem equippedShield;
   
   private HelmetItem equippedHelmet;
   

   //================|  Inner Classes      |====================================
   
   private static final class InventoryEntry implements Comparable< InventoryEntry >
   {
      private static final int MaxEntryCount = 10;
      
      private final InventoryItem item;
      
      private int count;
      
      private InventoryEntry( final InventoryItem item )
      {
         this( item, 1 );
      }
      
      private InventoryEntry( final InventoryItem item, final int count )
      {
         this.item = item;
         this.count = count;
      }
      
      /**
       * {@inheritDoc}
       */
      @Override
      public int compareTo( final InventoryEntry another )
      {
         return this.item.compareTo( another.item );
      }
      
      boolean isEmpty()
      {
         return this.count == 0;
      }
      
      boolean canIncrement()
      {
         return this.count < MaxEntryCount;
      }
      
      void increment()
      {
         if( canIncrement() )
         {
            this.count++;
         }
      }
      
      void decrement()
      {
         if( this.count > 0 )
         {
            this.count--;
         }
      }
   }


   //================|  Constructors       |====================================
   
   /**
    * Initializes a new Inventory with the specified limit of entries.
    * 
    * @param entryLimit the maximum number of entries in the inventory.
    */
   public Inventory( final int entryLimit )
   {
      this.entryLimit = entryLimit;
   }

   
   //================|  Public Methods     |====================================
   
   /**
    * Determines whether or not an item can be added to the inventory. If the inventory already
    * contains an instance of the item, then then this method will return whether or not another
    * item of the same kind can be carried. If the inventory does not already contain an instance
    * of the item, then this method will return whether or not another entry can be added
    * into the inventory.
    * 
    * @param item the item to add.
    * @return whether or not the item can be added to the inventory.
    */
   public boolean canAddItem( final InventoryItem item )
   {
      if( item != null )
      {
         final InventoryEntry entry = getEntry( item );
         if( entry != null )
         {
            return entry.canIncrement();
         }
         return canAddEntry();
      }
      return false;
   }
   
   /**
    * Adds an item to the inventory. Call {@link #canAddItem(InventoryItem)} first to determine
    * whether or not the item can be added. If the item cannot be added, then this method 
    * will have no effect.
    * 
    * @param item the item to add.
    */
   public void addItem( final InventoryItem item )
   {
      if( item != null )
      {
         final InventoryEntry entry = getEntryAddingIfNecessary( item );
         if( entry != null )
         {
            if( entry.canIncrement() )
            {
               entry.increment();
            }
         }
      }
   }
   
   /**
    * Removes an item from the inventory. If the item was equipped, then the item is unequipped
    * first.
    * 
    * @param item the item to remove.
    */
   public void removeItem( final InventoryItem item )
   {
      final InventoryEntry entry = getEntry( item );
      if( entry != null )
      {
         entry.decrement();
         if( entry.isEmpty() )
         {
            this.entries.remove( entry );
            
            // Check to see if this item was equipped. If so, un-equip it.
            if( isEquipped( item ) )
            {
               unEquip( item );
            }
         }
      }
   }
   
   /**
    * Equips the item.
    * 
    * @param item the item to equip.
    */
   public void equip( final InventoryItem item )
   {
      if( item != null )
      {
         if( item instanceof WeaponItem )
         {
            this.equippedWeapon = (WeaponItem) item;
         }
         else if( item instanceof ArmorItem )
         {
            this.equippedArmor = (ArmorItem) item;
         }
         else if( item instanceof ShieldItem )
         {
            this.equippedShield = (ShieldItem) item;
         }
         else if( item instanceof HelmetItem )
         {
            this.equippedHelmet = (HelmetItem) item;
         }
      }
   }
   
   /**
    * Un-equips the item.
    * 
    * @param item the item to un-equip.
    */
   public void unEquip( final InventoryItem item )
   {
      if( this.equippedWeapon == item )
      {
         this.equippedWeapon = null;
      }
      else if( this.equippedArmor == item )
      {
         this.equippedArmor = null;
      }
      else if( this.equippedShield == item )
      {
         this.equippedShield = null;
      }
      else if( this.equippedHelmet == item )
      {
         this.equippedHelmet = null;
      }
   }
   
   /**
    * @return the equipped weapon or <code>null</code> if no weapon is equipped.
    */
   public WeaponItem getEquippedWeapon()
   {
      return this.equippedWeapon;
   }
   
   /**
    * @return the equipped armor or <code>null</code> if no armor is equipped.
    */
   public ArmorItem getEquippedArmor()
   {
      return this.equippedArmor;
   }
   
   /**
    * @return the equipped shield or <code>null</code> if no shield is equipped.
    */
   public ShieldItem getEquippedShield()
   {
      return this.equippedShield;
   }
   
   /**
    * @return the equipped helmet or <code>null</code> if no helmet is equipped.
    */
   public HelmetItem getEquippedHelmet()
   {
      return this.equippedHelmet;
   }
   
   /**
    * @param item the item to check.
    * @return whether or not the item is equipped. <code>null</code> items are never equipped.
    */
   public boolean isEquipped( final InventoryItem item )
   {
      if( item == null )
      {
         return false;
      }
      return ( getEquippedWeapon() == item ) ||
             ( getEquippedArmor()  == item ) ||
             ( getEquippedShield() == item ) ||
             ( getEquippedHelmet() == item );
   }
   
   /**
    * @return the items in the inventory.
    */
   public Collection< InventoryItem > getInventoryItems()
   {
      final Set< InventoryItem > items = new LinkedHashSet< InventoryItem >();
      items.add( getEquippedWeapon() );
      items.add( getEquippedArmor() );
      items.add( getEquippedShield() );
      items.add( getEquippedHelmet() );
      items.remove( null );
      
      for( final InventoryEntry entry : this.entries )
      {
         items.add( entry.item );
      }
      return items;
   }
   
   /**
    * @param item the item in question.
    * @return the number of items of that type in the inventory or <code>-1</code> if that item
    * does not exist in the inventory.
    */
   public int getInventoryCount( final InventoryItem item )
   {
      final InventoryEntry entry = getEntry( item );
      if( entry != null )
      {
         return entry.count;
      }
      return -1;
   }

   
   //================|  Private Methods    |====================================
   
   private boolean canAddEntry()
   {
      return this.entries.size() < this.entryLimit;
   }
   
   private InventoryEntry getEntry( final InventoryItem item )
   {
      for( final InventoryEntry entry : this.entries )
      {
         if( entry.item == item )
         {
            return entry;
         }
      }
      return null;
   }
   
   private InventoryEntry getEntryAddingIfNecessary( final InventoryItem item )
   {
      InventoryEntry entry = getEntry( item );
      if( entry == null && canAddEntry() )
      {
         entry = new InventoryEntry( item, 0 );
         this.entries.add( entry );
      }
      return entry;
   }
}
