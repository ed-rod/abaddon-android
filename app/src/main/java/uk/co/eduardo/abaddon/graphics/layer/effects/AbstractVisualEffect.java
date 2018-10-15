package uk.co.eduardo.abaddon.graphics.layer.effects;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Abstract base class for a visual effect.
 * 
 * @author Ed
 */
public abstract class AbstractVisualEffect implements VisualEffect
{
   //================|  Fields             |====================================
   
   /** Whether the effect has finished. */
   private boolean finished;
   
   /** The number of frames that have been animated. */
   private int frameCount;
   
   /** Whether the layer is visible or not. */
   private boolean visible = true;
   
   
   //================|  Public Methods     |====================================
   
   /**
    * {@inheritDoc}
    */
   @Override
   public final void draw( final int xPos, 
                           final int yPos, 
                           final boolean below, 
                           final Canvas canvas, 
                           final Paint paint )
   {
      if( !this.visible )
      {
         return;
      }
      if( isEffectFinished() )
      {
         return;
      }
      this.frameCount++;
      drawImpl( xPos, yPos, below, canvas, paint );
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isEffectFinished()
   {
      return this.finished;
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
    * {@inheritDoc}
    */
   @Override
   public void setVisible( final boolean show )
   {
      this.visible = show;
   }
   
   
   //================|  Protected Methods  |====================================
   
   /**
    * Sets
    * @param finished whether the effect has finished.
    */
   protected void setEffectFinished( final boolean finished )
   {
      this.finished = finished;
   }
   
   /**
    * @return the number of frames that have been drawn so far.
    */
   protected int getFrameCount()
   {
      return this.frameCount;
   }
   
   /**
    * Called from the {{@link #draw(int, int, boolean, Canvas, Paint)} method.
    * <p>
    * Subclasses must implement this method instead as the <code>draw</code>
    * method is already implemented to keep track of the frame count.
    * 
    * @param xPos position of the main character
    * @param yPos position of the main character
    * @param below <code>true</code> if this layer is currently below the hero
    * @param canvas the <code>Canvas</code> on which to paint
    * @param paint the <code>Paint</code> object to use for drawing
    */
   protected abstract void drawImpl( final int xPos,
                                     final int yPos,
                                     final boolean below,
                                     final Canvas canvas,
                                     final Paint paint );
}
