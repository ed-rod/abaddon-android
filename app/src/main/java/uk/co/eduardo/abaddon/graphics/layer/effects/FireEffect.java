package uk.co.eduardo.abaddon.graphics.layer.effects;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

/**
 * A flame effect.
 * 
 * @author Eduardo Rodrigues
 */
public class FireEffect extends AbstractVisualEffect
{
   //================|  Fields             |====================================
   
   /** Maximum value uniformity can take. */
   private static final int MAX_UNIFORMITY = 5;
   
   /** The width of the fire box in pixels. */
   private final int width;
   
   /** The height of the fire box in pixels. */
   private final int height;
   
   /** The x position on screen of the top-left corner of the fire box. */
   private final int xStart;
   
   /** The y position on screen of the top-left corner of the fire box. */
   private final int yStart;
   
   /** The number of frames the effect should last. */
   private final int frames;
   
   /** The flame data */
   private final int[][] data;

   /** Bitmap representation of flame. */
   private final Bitmap bitmap;
   
   /** The oval area drawn at the bottom of the flame. */
   private final RectF oval;
   
   /** Paint object for the filled yellow colour used for the flame oval. */
   private final Paint yellowPaint;
   
   
   //================|  Public Methods     |====================================
   
   /**
    * @param width width of the fire box in pixels.
    * @param height height of the fire box in pixels.
    * @param xPos X position on screen of the top-left corner of the fire box.
    * @param yPos Y position on screen of the top-left corner of the fire box.
    * @param frames number of frames the effect should last.
    */
   public FireEffect( final int width,
                      final int height,
                      final int xPos,
                      final int yPos,
                      final int frames )
   {
      this.width = width;
      this.height = height;
      this.xStart = xPos;
      this.yStart = yPos;
      this.frames = frames;
      this.data = new int[height][width];
      this.bitmap = Bitmap.createBitmap( width, height, Config.ARGB_8888 );
      this.oval = new RectF();
      this.yellowPaint = new Paint();
      this.yellowPaint.setColor( 0xffffff3f ); // yellow
      this.yellowPaint.setStyle( Style.FILL );
   }
   
   
   //================|  Protected Methods  |====================================
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void drawImpl( final int xPos, 
                            final int yPos, 
                            final boolean below, 
                            final Canvas canvas, 
                            final Paint paint )
   {
      // All rows between height and roundHeight are drawn with a rounded bottom
      final int roundHeight = (int)( this.height * 0.95f );
      // All rows between roundHeight and reheatHeight are reheated
      final int reheatHeight = (int)( this.height * 0.90f );
      // This will temporarily hold the value of each flame pixel
      int curPixel;
      // The last random number generated.
      int random;
      // How much the current pixel is cooled by.
      int cooldown;
      // The new colour for the pixel.
      int colour = 0;
      // The current intensity of the pixel (used to derive its colour)
      int intensity;
      // Used to control how random the flame's vertical growth is.
      final int uniformity = 3;
      // The frame count at which we should stop growing the flame.
      final int endFrame = this.frames - ( this.height / uniformity );
      final boolean moveFlameUp = getFrameCount() < endFrame;
      // Random number generator
      final Random rng = new Random();

//      System.out.println( updateFire( 10, 12 ) );
      
      final int offset = moveFlameUp ? 0 : getFrameCount() - endFrame;
      this.oval.set( this.xStart + offset, 
                this.yStart + reheatHeight, 
                this.xStart + this.width - offset, 
                this.yStart + this.height );
      // Draw the rounded bottom
      canvas.drawArc( this.oval, 0f, 180f, true, this.yellowPaint );
      // Run a loop through the flames from the bottom up
      for( int x = 0; x < this.width; x++ )
      {
         // Calculate how quickly the flame should cool down at this point.
         // We cool down quicker near the edges.
         final int maxCool = 3;
         // The flames are cooled to a different amount across the width.
         // We give the flames a sinusoidal profile
         double coolScale = ( 1 - Math.sin( ( x / (double) this.width ) * Math.PI ) ) 
                             * maxCool + 1;
         if( !moveFlameUp )
         {
            // cool down even faster
            coolScale = coolScale * 2;
         }
         for( int y = roundHeight - 1; y >= MAX_UNIFORMITY; y-- )
         {
            curPixel = this.data[y][x];
            
            // If this pixel is dim enough, ignore it for sake of uniformity
            if( curPixel >= 10 )
            {
               // Give this pixel a random degree of cooldown
               random = rng.nextInt( uniformity );
               cooldown = (int) ( random * coolScale );
               this.data[y][x] = curPixel - cooldown;
               
               if( moveFlameUp )
               {
                  // In addition, move this pixel upward
                  this.data[y - random][x] = this.data[y][x];
               }
               // Generate a colour based on the "heat" value
               intensity = ( this.data[y][x] * 255 ) / this.height;
               final int scaledIntensity = Math.min( 255, 2 * intensity );
               colour = Color.argb( 255,
                                    scaledIntensity, 
                                    intensity, 
                                    intensity >> 2 );
               this.bitmap.setPixel( x, y, colour );
            }
            else
            {
               this.bitmap.setPixel( x, y, 0 );
            }
         }
      }
      if( moveFlameUp )
      {
         // Heat it from the bottom.
         for( int y = reheatHeight; y < roundHeight; y++ )
         {
            for( int x = 0; x < this.width; x++ )
            {
               this.data[y][x] = this.height;
            }
         }
      }
      // Draw the current frame.
      canvas.drawBitmap( this.bitmap, this.xStart, this.yStart, null );
      if( getFrameCount() >= this.frames )
      {
         setEffectFinished( true );
      }
   }
//   
//   /**
//    * Test method for native code.
//    * @param x
//    * @param y
//    * @return x + y
//    */
//   private native int updateFire( int x, int y );
}
