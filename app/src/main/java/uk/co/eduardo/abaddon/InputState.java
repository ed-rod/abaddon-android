package uk.co.eduardo.abaddon;

import java.util.Arrays;

import android.view.MotionEvent.PointerCoords;

/**
 * Manage the key states in the game
 * 
 * @author Ed
 */
public final class InputState
{
   //================|  Fields - Keys      |====================================
   
   /** True if the left key was pressed during the last game frame */
   public static boolean leftPressed = false;
   
   /** True if the right key was pressed during the last game frame */
   public static boolean rightPressed = false;
   
   /** True if the down key was pressed during the last game frame */
   public static boolean downPressed = false;
   
   /** True if the up key was pressed during the last game frame */
   public static boolean upPressed = false;
   
   /** True if the D_PAD centre button was pressed during the last game frame */
   public static boolean actionPressed = false;
   
   /** True if the Space Bar was pressed during the last game frame */
   public static boolean spacePressed = false;
   
   //================|  Fields - Tilt      |====================================
   
   /** The current tilt in the X direction. */
   public static float sensorX = 0;
   
   /** The current tilt in the Y direction. */
   public static float sensorY = 0;
   
   //================|  Fields - Touch     |====================================

   /** The maximum number of multi-touch events we will be processing. */
   public static final int maxTouch = 3;
   
   /** The location of touch coordinates that are down. */
   public static PointerCoords[] touchDown = new PointerCoords[ 3 ];
   
   /** The location of touch coordinates that have just been released. */
   public static PointerCoords[] touchUp = new PointerCoords[ 3 ];
   
   /** Mark the touch up events as having been read. */
   public static void markTouchUpEventsRead()
   {
      Arrays.fill( touchUp, null );
   }
}
