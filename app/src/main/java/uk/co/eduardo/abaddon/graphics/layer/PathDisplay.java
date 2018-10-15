package uk.co.eduardo.abaddon.graphics.layer;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import uk.co.eduardo.abaddon.R;
import uk.co.eduardo.abaddon.util.Coordinate;
import uk.co.eduardo.abaddon.util.Res;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * A layer that displays a path.
 * 
 * @author Eduardo Rodrigues
 */
public class PathDisplay implements Layer
{
   //================|  Fields             |====================================
   
   /** The path to display. */
   private final LinkedList< Coordinate > path;
   
   
   /** True if this layer should be rendered */
   private boolean visible = true;
   
   /** A path marker tile **/
   private final Bitmap pathMarker;
   
   //================|  Constructors         |==================================
   
   /**
    * @param path the list that contains the path along which the hero is walking.
    */
   public PathDisplay( final LinkedList< Coordinate > path )
   {
      this.path=path;
      this.pathMarker = BitmapFactory.decodeResource( Res.resources, R.drawable.pathmarker );
   }
   
   //================|  Public Methods       |==================================
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void draw( final int xPos,
                     final int yPos,
                     final boolean below,
                     final Canvas canvas,
                     final Paint paint )
   {
      if( !this.visible )
      {
         return;
      }
      paint.setColor( 0xffffffff );
      final int tileSize = ScreenSettings.tileSize;
      
      final int xTilePos = xPos / tileSize;
      final int yTilePos = yPos / tileSize;
      
      final int xCentre = ScreenSettings.xCentre;
      final int yCentre = ScreenSettings.yCentre;
      
      final int visibleHorz = ( xCentre / tileSize ) + 1;
      final int visibleVert = ( yCentre / tileSize ) + 1;
      
      final int xStart = xTilePos - visibleHorz;
      final int xEnd   = xTilePos + visibleHorz + 1;
      final int yStart = yTilePos - visibleVert + 1;
      final int yEnd   = yTilePos + visibleVert + 1; // + 1
      
      
      for( final Coordinate coord : this.path )
      {
         final int xTile = coord.x;
         final int yTile = coord.y;
         
         if( xTile >= xStart && xTile < xEnd && yTile >= yStart && yTile < yEnd )
         {
            final int xPixel = xTile * tileSize;
            final int yPixel = yTile * tileSize;
            
            final int xDraw = xPixel - xPos + xCentre;
            final int yDraw = yPixel - yPos + yCentre;
            
            canvas.drawBitmap( this.pathMarker, xDraw, yDraw, null );
            
//            canvas.drawLine( xDraw, yDraw ,xDraw + tileSize, yDraw + tileSize, paint );
//            canvas.drawLine( xDraw + tileSize, yDraw,xDraw, yDraw + tileSize, paint );
         }
      }
   }                    
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setVisible( final boolean show )
   {
      this.visible = show;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVisible()
   {
      return this.visible;
   }
}
