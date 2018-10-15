package uk.co.eduardo.abaddon.graphics.layer;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;

import uk.co.eduardo.abaddon.InputState;

/**
 * Displays a window with text
 * 
 * @author Eduardo Rodrigues
 */
public class TextWindow extends GameWindow
{
   //================|  Fields             |====================================
   
   /** The message to display in this window */
   protected String message;
   
   /** The wrapped lines of text to display */
   protected ArrayList<String> lines = new ArrayList<String>();
   
   /** The Paint object used to draw text onto the canvas */
   protected final Paint textPaint = new Paint();
   
   /** The height in pixels of a line of text */
   protected int textHeight; 
   
   /** The number of pixels below the text base-line */
   protected int textDescent;
   
   /** How many lines of text are visible in the window */
   private int visibleLines;
   
   /** The number of pixels used for the icon */
   private static final int ICON_SIZE = 5;
   
   //---------------- Scrolling ------------------------------------------------
   
   /** The number of pixels to scroll during each scroll increment */
   private static final int SCROLL_INCREMENT = 2;
   
   /** True if the window cannot contain all the text */
   private boolean scrollable;
   
   /** Is this window scrolling text */
   private boolean scrolling = false;
   
   /** How many pixels have we scrolled */
   private int currentScroll = 0;
   
   
   //================|  Constructors       |====================================
   
   /**
    * Sizes the window to fit the text on a single line
    * 
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param message the message to display
    */
   public TextWindow( final int x,
                      final int y,
                      final String message )
   {
      this( x, y, message, -1 );
   }
   
   /**
    * Sizes the window to fit the text on a single line
    * 
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param message the message to display
    * @param timeout how many game frames the window will exist
    */
   public TextWindow( final int x,
                      final int y,
                      final String message,
                      final long timeout )
   {
      this( x, y, 100, 100, message, timeout, false );
      this.lines.clear();
      this.lines.add( message );
      
      // Plus one in case its rounded down
      final int w = (int) this.textPaint.measureText( message ) + 1 + ( 2 * PADDING );
      final int h = this.textHeight + ( 2 * PADDING );
      
      resize( w, h );     
   }
   
   /**
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param width window width in screen pixels
    * @param height window height in screen pixels
    * @param message the message to display
    */
   public TextWindow( final int x,
                      final int y,
                      final int width,
                      final int height,
                      final String message )
   {
      this( x, y, width, height, message, -1, false );
   }
   
   /**
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param width window width in screen pixels
    * @param height window height in screen pixels
    * @param message the message to display
    * @param modal does this window require user input
    */
   public TextWindow( final int x,
                      final int y,
                      final int width,
                      final int height,
                      final String message,
                      final boolean modal )
   {
      this( x, y, width, height, message, -1, modal );
   }
   
   /**
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param width window width in screen pixels
    * @param height window height in screen pixels
    * @param message the message to display
    * @param timeout how many game frames the window will exist
    */
   public TextWindow( final int x,
                      final int y,
                      final int width,
                      final int height,
                      final String message,
                      final long timeout )
   {
      this( x, y, width, height, message, timeout, false );
   }
   /**
    * @param x top-left x position in screen pixels
    * @param y top-left y position in screen pixels
    * @param width window width in screen pixels
    * @param height window height in screen pixels
    * @param message the message to display
    * @param timeout how many game frames the window will exist
    * @param modal does this window require user input
    */
   private TextWindow( final int x,
                       final int y,
                       final int width,
                       final int height,
                       final String message,
                       final long timeout,
                       final boolean modal )
   {
      super( x, y, width, height, timeout, modal );
      this.message = message;

      // White text colour
      this.textPaint.setColor( Color.WHITE );
      this.textPaint.setAntiAlias( true );
      final FontMetricsInt fmi = this.textPaint.getFontMetricsInt();
      
      this.textDescent = fmi.bottom;
      this.textHeight = this.textPaint.getFontMetricsInt( fmi );
      
      // Make the text height a multiple of SCROLL_INCREMENT
      this.textHeight += this.textHeight % SCROLL_INCREMENT;
      
      breakMessage();
   }
   
   
   //================|  Public  Methods    |====================================
   
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
      super.draw( posX, posY, below, canvas, paint );
      
      // save the current clip state
      canvas.save();
      
      // Cache fields for performance.
      final int cachedX = this.x;
      final int cachedY = this.y;
      final int cachedTextHeight = this.textHeight;
      
      // The padding in the window is 5 pixels all around
      canvas.clipRect( cachedX + PADDING,
                       cachedY + PADDING,
                       cachedX + this.width - PADDING, 
                       cachedY + PADDING + ( this.visibleLines * cachedTextHeight ) );    
      
      if( this.scrolling )
      {
         this.currentScroll += SCROLL_INCREMENT;
         if( this.currentScroll == cachedTextHeight )
         {
            this.currentScroll = 0;
            this.scrolling = false;
            this.lines.remove( 0 );
            this.scrollable = this.lines.size() > this.visibleLines;
         }
      }
      
      int yOffset = cachedY + PADDING + cachedTextHeight - 
            this.textDescent - this.currentScroll;
      
      for( final String line : this.lines )
      {
         canvas.drawText( line, cachedX + PADDING, yOffset, this.textPaint );
         yOffset += cachedTextHeight;
      }
      // restore the clip state
      canvas.restore();
      
      // if the window is modal, draw the icon at the bottom
      if( this.isModal )
      {
         final int xCentre = ( cachedX + cachedX + this.width ) >> 1;
         final int yCentre = cachedY + this.height - PADDING;
         final Paint iconPaint = new Paint();
         if( this.scrollable )
         {
            // draw a green arrow
            iconPaint.setColor( Color.GREEN );
            for( int yIcon = 0; yIcon < ICON_SIZE; yIcon++ )
            {
               canvas.drawPoint( xCentre, yCentre - yIcon, iconPaint );
               for( int xIcon = 1; xIcon <= yIcon; xIcon++ )
               {
                  canvas.drawPoint( xCentre - xIcon, yCentre - yIcon, iconPaint );
                  canvas.drawPoint( xCentre + xIcon, yCentre - yIcon, iconPaint );
               }
            }
         }
         else
         {
            // draw a red square
            iconPaint.setColor( Color.RED );
            for( int yIcon = 0; yIcon < ICON_SIZE; yIcon++ )
            {
               for( int xIcon = 0; xIcon < ICON_SIZE - 1; xIcon++ )
               {
                  canvas.drawPoint( xCentre - xIcon, yCentre - yIcon, iconPaint );
                  canvas.drawPoint( xCentre + xIcon, yCentre - yIcon, iconPaint );
               }
            }
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void resize( final int newWidth, final int newHeight1 )
   {
      super.move( this.x, this.y, newWidth, newHeight1 );
      breakMessage();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void move( final int newX, final int newY )
   {
      super.move( newX, newY, this.width, this.height );
      breakMessage();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void move( final int newX,
                     final int newY,
                     final int newWidth,
                     final int newHeight )
   {
      super.move( newX, newY, newWidth, newHeight );
      breakMessage();
   }
   
   /**
    * @param newMessage the new text to display
    */
   public void setText( final String newMessage )
   {
      this.message = newMessage;
      breakMessage();
   }
   
   /**
    * Perform action based on key press
    */
   @Override
   public void processKeys()
   {
      if( !this.isModal )
      {
         return;
      }
      // Do not accept key input if it is scrolling
      if( this.scrolling )
      {
         return;
      }
      if( this.scrollable )
      {
         if( InputState.downPressed )
         {
            this.scrolling = true;
         }
      }
      else
      {
         if( InputState.actionPressed )
         {
            // close the window
            this.visible = false;
            InputState.actionPressed = false;
         }
      }
   }
   
   
   //================|  Private Methods    |====================================
   
   /**
    * Break the text into lines wrapping on the space character
    */
   private void breakMessage()
   {
      final int availWidth = this.width - PADDING - PADDING;
      this.lines.clear();
      
      final float[] widths = new float[this.message.length()];
      this.textPaint.getTextWidths( this.message, widths );
      
      int lineStart = 0;
      int lineEnd = 0;
      float lineWidth = 0;
      for( int c = 0; c < this.message.length(); c++ )
      {
         final char currentChar = this.message.charAt( c );
         lineWidth += widths[c];
         if( currentChar == ' ' )
         {
            // Finished a word. Check if the line length with this word fits
            if( lineWidth > availWidth )
            {
               // Start a new line
               final String currentLine = this.message.substring( lineStart, lineEnd );
               this.lines.add( currentLine );
               c = lineEnd;
               lineStart = lineEnd + 1;
               lineEnd = lineStart;
               lineWidth = 0;
            }
            else
            {
               lineEnd = c;
            }
         }
      }
      // We finished the last word. Check if the line length with this word fits
      if( lineWidth > availWidth )
      {
         final String currentLine = this.message.substring( lineStart, lineEnd );
         this.lines.add( currentLine );
         lineStart = lineEnd + 1;
      }
      final String currentLine = this.message.substring( lineStart );
      this.lines.add( currentLine );   
      final int iconSpace = this.isModal ? ICON_SIZE : 0;
      this.visibleLines = ( this.height - PADDING - PADDING - iconSpace ) / this.textHeight;
      this.scrollable = this.lines.size() > this.visibleLines;
   }
}
