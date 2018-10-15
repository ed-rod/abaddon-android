package uk.co.eduardo.abaddon.graphics.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.NinePatchDrawable;

import uk.co.eduardo.abaddon.R;
import uk.co.eduardo.abaddon.util.Res;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * A Game window
 * 
 * A window has a frame
 * @author Eduardo Rodrigues
 */
public class GameWindow implements Layer
{
   //================|  Fields             |====================================
   
   /** The window border */
   protected static final NinePatchDrawable patch = 
      (NinePatchDrawable) Res.resources.getDrawable( R.drawable.window );
   
   /** The padding around the window to allow for the border */
   protected static final int PADDING = 5;
   
   /** Used for centring windows */
   public static final int VERTICAL = 0x01;
   
   /** Used for centring windows */
   public static final int HORIZONTAL = 0x02;
   
   /** Top-left X in screen pixels */
   protected int x;
   
   /** Top-left Y in screen pixels */
   protected int y;
   
   /** Width of the window in pixels */
   protected int width;
   
   /** Height of the window in pixels */
   protected int height;
   
   /** Whether the window is currently visible on screen. */
   protected boolean visible;
   
   /** Window will not display after this timeout. -1 indicates that it does not timeout. */
   protected long timeout;
   
   /** Whether or not the window requires user interaction. */
   protected final boolean isModal;
   
   /** The count of how many game frames the window has been displayed. */
   private long counter = 0;
   
   
   //================|  Constructors       |====================================
   
   /**
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param width window width in screen pixels
    * @param height window height in screen pixels
    */
   public GameWindow( final int x,
                      final int y,
                      final int width,
                      final int height )
   {
      this( x, y, width, height, -1, false );
   }
   
   /**
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param width window width in screen pixels
    * @param height window height in screen pixels
    * @param timeout how many game frames the window will exist
    * @param isModal whether or not the window requires user input.
    */
   public GameWindow( final int x,
                      final int y,
                      final int width,
                      final int height,
                      final long timeout,
                      final boolean isModal )
   {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.timeout = timeout;
      this.visible = true;
      this.isModal = isModal;
   }
   
   
   //================|  Public Methods     |====================================

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
      if( !this.visible )
      {
         return;
      }
      patch.setBounds( this.x, this.y, this.x + this.width, this.y + this.height );
      patch.draw( canvas );
      
      // increase the counter
      this.counter++;
      if( this.timeout != -1 )
      {
         if( this.counter >= this.timeout )
         {
            this.visible = false;
         }
      }
   }
   
   /**
    * Shows or hides the window. If the window was due to be hidden after a 
    * timeout, that timeout is cleared and the window will stay shown (or 
    * hidden) indefinitely
    * 
    * @param show should the window be displayed
    */
   @Override
   public void setVisible( final boolean show )
   {
      this.visible = show;
      
      // Cancel any visibility change based on a timeout
      this.timeout = -1;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVisible()
   {
      return this.visible;
   }
   
   /**
    * @return whether or not the window requires user input.
    */
   public boolean isModal()
   {
      return this.isModal;
   }
   
   /**
    * Call this method for the window to process key interactions.
    * <p>
    * This method should only be called once every game frame if, and only if, {@link #isModal()}
    * is <code>true</code>.
    */
   public void processKeys()
   {
      // Do nothing.
   }
   
   /**
    * Change the dimensions of the window
    * 
    * @param newWidth window width in screen pixels
    * @param newHeight1 window height in screen pixels
    */
   public void resize( final int newWidth, final int newHeight1 )
   {
      move( this.x, this.y, newWidth, newHeight1 );
   }
   
   /**
    * Move the window to a new location
    * 
    * @param newX top-left x position in screen pixels
    * @param newY top-left y position in screen pixels
    */
   public void move( final int newX, final int newY )
   {
      move( newX, newY, this.width, this.height );
   }
   
   /**
    * Move the window to a new location and set new dimensions
    * 
    * @param newX top-left x position in screen pixels
    * @param newY top-left y position in screen pixels
    * @param newWidth window width in screen pixels
    * @param newHeight window height in screen pixels
    */
   public void move( final int newX,
                     final int newY,
                     final int newWidth,
                     final int newHeight )
   {
      this.x = newX;
      this.y = newY;
      this.width = newWidth;
      this.height = newHeight;
      this.visible = true;
   }
   
   /**
    * Centres the window in a given mode
    * 
    * @param mode composition of VERTICAL and HORIZONTAL bitmasks
    */
   public void centre( final int mode )
   {
      if( ( mode & VERTICAL ) == VERTICAL )
      {
         this.y = ( ScreenSettings.height - this.height ) / 2;
      }
      if( ( mode & HORIZONTAL ) == HORIZONTAL )
      {
         this.x = ( ScreenSettings.width - this.width ) / 2;
      }
   }
}
