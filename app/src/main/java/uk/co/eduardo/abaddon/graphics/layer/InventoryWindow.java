package uk.co.eduardo.abaddon.graphics.layer;

import android.graphics.Canvas;
import android.graphics.Paint;

import uk.co.eduardo.abaddon.inventory.Inventory;
import uk.co.eduardo.abaddon.inventory.InventoryItem;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * A game window that displays the inventory screen.
 * <p>
 * The Inventory screen allows for selecting the equipped weapon, armor, shield and helmet.
 * It also allows to manage any other, non-equippable, items that are in the inventory.
 * 
 * @author Ed
 */
public class InventoryWindow extends GameWindow
{
   private static final int Padding = 10;
   
   private final Inventory inventory;
   
   /**
    * @param inventory the inventory to display.
    */
   public InventoryWindow( final Inventory inventory )
   {
      super( Padding, 
             Padding, 
             ScreenSettings.width - ( 2 * Padding ), 
             ScreenSettings.height - ( 2 * Padding ),
             -1,
             true );
      
      this.inventory = inventory;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void draw( final int posX,
                     final int posY,
                     final boolean below,
                     final Canvas canvas,
                     final Paint paint )
   {
      super.draw( posX, posY, below, canvas, paint );
      
      for( final InventoryItem item : this.inventory.getInventoryItems() )
      {
         System.out.println( item.getName() );
      }
   }
}
