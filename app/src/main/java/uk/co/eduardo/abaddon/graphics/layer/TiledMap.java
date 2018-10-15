package uk.co.eduardo.abaddon.graphics.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import uk.co.eduardo.abaddon.Controller;
import uk.co.eduardo.abaddon.tileset.TileDescription;
import uk.co.eduardo.abaddon.tileset.Tileset;
import uk.co.eduardo.abaddon.util.Debug;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * A map represented as an array of tiles
 * <p>
 * For performance reasons we expose the fields. It's not such a bad thing
 * as all the fields are final.
 * 
 * @author Ed
 */
public class TiledMap implements Layer
{
   //================|  Fields             |====================================
   
   /** The {@link Tileset} to use for this map */
   private final Tileset tileset;
   
   /** The full map tile array. */
   public final int[][] fullMap;
   
   /** The width of the map in tiles */
   private final int width;
   
   /** The height of the map in tiles */
   private final int height;
   
   /** True if this layer should be rendered */
   private boolean visible;
   
   /** Line path used for wireframe rendering. */
   private final Path path = new Path();
   
   
   //================|  Constructors         |==================================
   
   /**
    * @param map the map tile array
    * @param tileset the tileset to use in this map
    */
   public TiledMap( final int[][] map, final Tileset tileset)
   {
      this.tileset = tileset;
      this.fullMap = map;
      this.width = map[0].length;
      this.height = map.length;
      this.visible = true;
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
      
      // Locally cache fields for performance.
      final int[][] cachedMap = this.fullMap;
      final Tileset cachedTileset = this.tileset;
      final boolean wireframe = Debug.wireframe;
      final boolean showHeroTile = Debug.showHeroTile;

      // Before we render this map, animate the tiles
      cachedTileset.animate();
      
      for( int yTile = yStart, yPixel = yStart * tileSize; 
           yTile < yEnd; 
           yTile++, yPixel += tileSize )
      {
         for( int xTile = xStart, xPixel = xStart * tileSize; 
                  xTile < xEnd; 
                  xTile++, xPixel += tileSize )
         {
            if( yTile >= 0 && yTile < this.height &&
                xTile >= 0 && xTile < this.width )
            {
               // Draw the bitmap on the canvas.
               final int xDraw = xPixel - xPos + xCentre;
               final int yDraw = yPixel - yPos + yCentre;
               
               final int tileId = cachedMap[yTile][xTile];
                     
               if( tileId == -1 )
               {
                  continue;
               }
               
               if( !wireframe )
               {  
                  canvas.drawBitmap( cachedTileset.getTile( tileId ),
                                     xDraw, yDraw, paint );
               }
               else
               {
                  boolean top = false;
                  boolean bottom = false;
                  boolean left = false;
                  boolean right = false;
                  // Render wireframe.
                  final int walkDirs = cachedTileset.getWalkDirections( tileId );
                  if( ( walkDirs & TileDescription.TOP ) != 0 )
                  {
                     // Draw top horizontal line
                     canvas.drawLine( xDraw, yDraw, 
                                      xDraw + tileSize, yDraw, paint );
                     top = true;
                  }
                  if( ( walkDirs & TileDescription.BOTTOM ) != 0 )
                  {
                     // Draw bottom horizontal line
                     canvas.drawLine( xDraw, yDraw + tileSize, 
                                      xDraw + tileSize, yDraw + tileSize, 
                                      paint );
                     bottom = true;
                  }
                  if( ( walkDirs & TileDescription.LEFT ) != 0 )
                  {
                     // Draw top horizontal line
                     canvas.drawLine( xDraw, yDraw, 
                                      xDraw, yDraw + tileSize , paint );
                     left = true;
                  }
                  if( ( walkDirs & TileDescription.RIGHT ) != 0 )
                  {
                     // Draw top horizontal line
                     canvas.drawLine( xDraw + tileSize, yDraw, 
                                      xDraw + tileSize, yDraw + tileSize, 
                                      paint );
                     right = true;
                  }
                  if( ( walkDirs & TileDescription.TL_BR_DIAG ) != 0 )
                  {
                     // Draw top horizontal line
                     canvas.drawLine( xDraw, yDraw, 
                                      xDraw + tileSize, yDraw + tileSize, 
                                      paint );
                     if( top && right && below )
                     {
                        this.path.reset();
                        this.path.moveTo( xDraw, yDraw );
                        this.path.lineTo( xDraw + tileSize, yDraw );
                        this.path.lineTo( xDraw + tileSize, yDraw + tileSize );
                        canvas.drawPath( this.path, paint );
                     }
                     else if( bottom && left && below )
                     {
                        this.path.reset();
                        this.path.moveTo( xDraw, yDraw );
                        this.path.lineTo( xDraw, yDraw + tileSize );
                        this.path.lineTo( xDraw + tileSize, yDraw + tileSize );
                        canvas.drawPath( this.path, paint );
                     }
                  }
                  if( ( walkDirs & TileDescription.TR_BL_DIAG ) != 0 )
                  {
                     // Draw top horizontal line
                     canvas.drawLine( xDraw + tileSize, yDraw, 
                                      xDraw, yDraw + tileSize, 
                                      paint );
                     if( left && top && below )
                     {
                        this.path.reset();
                        this.path.moveTo( xDraw, yDraw + tileSize );
                        this.path.lineTo( xDraw, yDraw );
                        this.path.lineTo( xDraw + tileSize, yDraw);
                        canvas.drawPath( this.path, paint );
                     }
                     else if( bottom && right && below )
                     {
                        this.path.reset();
                        this.path.moveTo( xDraw, yDraw + tileSize );
                        this.path.lineTo( xDraw + tileSize, yDraw + tileSize );
                        this.path.lineTo( xDraw + tileSize, yDraw);
                        canvas.drawPath( this.path, paint );
                     }
                  }
                  
                  if( ( top && bottom && left && right && below ) || 
                        LayerManager.isTileOccupied( xTile, yTile, null, true, false ) )
                  {
                     // fill the entire box.
                     canvas.drawRect( xDraw, yDraw,
                                      xDraw + tileSize, yDraw + tileSize,
                                      paint );
                  }
               }
            }
         }
      }
      
      if( showHeroTile )
      {
         // Draw the hero point
         canvas.drawPoint( xCentre, yCentre, paint );
         canvas.drawPoint( xCentre - Controller.EXTENT, yCentre, paint );
         canvas.drawPoint( xCentre, yCentre - Controller.EXTENT, paint );
         canvas.drawPoint( xCentre + Controller.EXTENT, yCentre, paint );
         canvas.drawPoint( xCentre, yCentre + Controller.EXTENT, paint );
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
   
   /**
    * @param xFrom the source X map <em>pixel<em> position
    * @param yFrom the source Y map <em>pixel<em> position
    * @param xTo the target X map <em>pixel<em> position
    * @param yTo the target Y map <em>pixel<em> position
    * 
    * @return 0 if cannot move to the new point
    *         1 if can move to the new point
    *         TL_BR_DIAG if cannot move and 'to' tile is diagonal
    *         TR_BL_DIAG if cannot move and 'to' tile is diagonal
    *         
    */
   public int canMove( final int xFrom, final int yFrom,
                       final int xTo, final int yTo )
   {
      final int tileSize = ScreenSettings.tileSize;
      
      final int xTileFrom = xFrom / tileSize;
      final int yTileFrom = yFrom / tileSize;
      final int xTileTo = xTo / tileSize;
      final int yTileTo = yTo / tileSize;
      
      final int tileFrom = this.fullMap[yTileFrom][xTileFrom];
      final int tileTo = this.fullMap[yTileTo][xTileTo];
      
      final Tileset cachedTileset = this.tileset;

      final boolean to = cachedTileset.canWalk( tileTo );
      
      if( !to )
      {
         // We can't walk into the new tile from any direction
         return 0;
      }
      
      final int fromWalkDirs = cachedTileset.getWalkDirections( tileFrom );
      final int toWalkDirs = cachedTileset.getWalkDirections( tileTo );
      
      int move = 1;
      
      if( yTileTo < yTileFrom )
      {
         // moved up a tile, check that the bottom of the new tile is
         // not blocked and that the top of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.TOP ) != 0 ||
             ( toWalkDirs & TileDescription.BOTTOM ) != 0 )
         {
            move = 0;
         }
      }
      if( yTileTo > yTileFrom )
      {
         // moved down a tile, check that the top of the new tile is
         // not blocked and that the bottom of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.BOTTOM ) != 0 ||
             ( toWalkDirs & TileDescription.TOP ) != 0 )
         {
            move = 0;
         }
      }
      if( xTileTo < xTileFrom )
      {
         // moved left a tile, check that the right of the new tile is
         // not blocked and that the left of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.LEFT ) != 0 ||
             ( toWalkDirs & TileDescription.RIGHT ) != 0 )
         {
            move = 0;
         }
      }
      if( xTileTo > xTileFrom )
      {
         // moved right a tile, check that the left of the new tile is
         // not blocked and that the right of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.RIGHT ) != 0 ||
             ( toWalkDirs & TileDescription.LEFT ) != 0 )
         {
            move = 0;
         }
      }
      if( move == 1 )
      {
         boolean diag;
         // Check halfspaces on diagonal tiles
         if( ( toWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
         {
            // Transform coordinates to tile-space.
            // (0,0) being the origin of the tile
            diag = checkHalfSpace( xFrom - ( xTileTo * tileSize ),
                                   yFrom - ( yTileTo * tileSize ),
                                   xTo - ( xTileTo * tileSize ),
                                   yTo - ( yTileTo * tileSize ),
                                   TileDescription.TR_BL_DIAG );
            
            move = diag ? move : TileDescription.TR_BL_DIAG;
         }
         if( ( toWalkDirs & TileDescription.TL_BR_DIAG ) != 0 )
         {
            // Transform coordinates to tile-space.
            // (0,0) being the origin of the tile
            diag = checkHalfSpace( xFrom - ( xTileTo * tileSize ),
                                   yFrom - ( yTileTo * tileSize ),
                                   xTo - ( xTileTo * tileSize ),
                                   yTo - ( yTileTo * tileSize ),
                                   TileDescription.TL_BR_DIAG );
            
            move = diag ? move : TileDescription.TL_BR_DIAG;
         }
         if( ( fromWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
         {
            // Transform coordinates to tile-space.
            // (0,0) being the origin of the tile
            diag = checkHalfSpace( xFrom - ( xTileFrom * tileSize ),
                                   yFrom - ( yTileFrom * tileSize ),
                                   xTo - ( xTileFrom * tileSize ),
                                   yTo - ( yTileFrom * tileSize ),
                                   TileDescription.TR_BL_DIAG );
            
            move = diag ? move : TileDescription.TR_BL_DIAG;
         }
         if( ( fromWalkDirs & TileDescription.TL_BR_DIAG ) != 0 )
         {
            // Transform coordinates to tile-space.
            // (0,0) being the origin of the tile
            diag = checkHalfSpace( xFrom - ( xTileFrom * tileSize ),
                                   yFrom - ( yTileFrom * tileSize ),
                                   xTo - ( xTileFrom * tileSize ),
                                   yTo - ( yTileFrom * tileSize ),
                                   TileDescription.TL_BR_DIAG );
            
            move = diag ? move : TileDescription.TL_BR_DIAG;
         }
      }
      return move;
   }
   
   /**
    * @param xFrom the source X map <em>pixel<em> position
    * @param yFrom the source Y map <em>pixel<em> position
    * @param xTo the target X map <em>pixel<em> position
    * @param yTo the target Y map <em>pixel<em> position
    * 
    * @return true if ( xFrom, yFrom ) and ( xTo, yTo ) are on different tiles
    * and it is not possible to move from the "from" tile to the "to" tile in
    * that direction and there is another tile that is possible to jump to
    * next in the same direction as "to" but one more tile across.
    */
   public boolean canJump(final int xFrom, final int yFrom,
                          final int xTo, final int yTo)
   {
      final int tileSize = ScreenSettings.tileSize;
      
      final int xTileFrom = xFrom / tileSize;
      final int yTileFrom = yFrom / tileSize;
      final int xTileTo = xTo / tileSize;
      final int yTileTo = yTo / tileSize;

      final int[][] cachedMap = this.fullMap;
      final Tileset cachedTileset = this.tileset;
      
      final int tileFrom = cachedMap[yTileFrom][xTileFrom];
      final int fromWalkDirs = cachedTileset.getWalkDirections(tileFrom);

      boolean jump = false;
      
      // if either tile is diagonal, then don't allow to jump
      if( ( fromWalkDirs & TileDescription.TL_BR_DIAG ) != 0 ||
          ( fromWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
      {
         return false;
      }

      if( yTileTo < yTileFrom )
      {
         // Moved up , check that the top of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.TOP ) != 0 )
         {
            // Check the tile one space above the "to" tile is not blocked from below
            final int tileJumpTo = cachedMap[yTileTo - 1][xTileTo];
            
            if( cachedTileset.canWalk( tileJumpTo ) )
            {
               final int toWalkDirs = 
                  cachedTileset.getWalkDirections( tileJumpTo );
               
               // if either tile is diagonal, then don't allow to jump
               if( ( toWalkDirs & TileDescription.TL_BR_DIAG ) != 0 ||
                   ( toWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  return false;
               }

               if( ( toWalkDirs & TileDescription.BOTTOM ) != 0 )
               {
                  jump = true;
               }
            }
         }
      }
      if( yTileTo > yTileFrom )
      {
         // Moved down. Check that the bottom of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.BOTTOM ) != 0 )
         {
            // Check the tile one space below the "to" tile is not enterable from above
            final int tileJumpTo = cachedMap[yTileTo + 1][xTileTo];
            
            if( cachedTileset.canWalk( tileJumpTo ) )
            {
               final int toWalkDirs = 
                  cachedTileset.getWalkDirections( tileJumpTo );
               
               // if either tile is diagonal, then don't allow to jump
               if( ( toWalkDirs & TileDescription.TL_BR_DIAG ) != 0 ||
                   ( toWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  return false;
               }
               
               if( ( toWalkDirs & TileDescription.TOP ) != 0 )
               {
                  jump = true;
               }
            }
         }
      }
      if( xTileTo < xTileFrom )
      {
         // Moved left. Check that the left of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.LEFT ) != 0 )
         {
            // Check the tile one space to the left of the "to" tile is not blocked from the right
            final int tileJumpTo = cachedMap[yTileTo][xTileTo - 1];
            
            if( cachedTileset.canWalk( tileJumpTo ) )
            {
               final int toWalkDirs = 
                  cachedTileset.getWalkDirections( tileJumpTo );
               
               // if either tile is diagonal, then don't allow to jump
               if( ( toWalkDirs & TileDescription.TL_BR_DIAG ) != 0 ||
                   ( toWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  return false;
               }
               
               if( ( toWalkDirs & TileDescription.RIGHT ) != 0 )
               {
                  jump = true;
               }
            }
         }
      }
      if( xTileTo > xTileFrom )
      {
         // Moved Right. Check that the right of the old tile is not blocked
         if( ( fromWalkDirs & TileDescription.RIGHT ) != 0 )
         {
            // Check the tile one space to the right of the "to" tile is not blocked from the left
            final int tileJumpTo = cachedMap[yTileTo][xTileTo + 1];
            
            if( cachedTileset.canWalk( tileJumpTo ) )
            {
               final int toWalkDirs = 
                  cachedTileset.getWalkDirections( tileJumpTo );
               
               // if either tile is diagonal, then don't allow to jump
               if( ( toWalkDirs & TileDescription.TL_BR_DIAG ) != 0 ||
                   ( toWalkDirs & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  return false;
               }
               
               if( ( toWalkDirs & TileDescription.LEFT ) != 0 )
               {
                  jump = true;
               }
            }
         }
      }
      return jump;
   }
   
   
   //================|  Private Methods      |==================================
   
   /**
    * Checks that the two coordinates, (xFrom, yFrom) and (xTo, yTo) are in the
    * same half-space relative to a diagonal.<pre>
    *  _______     _______
    * |      /|   |      /|
    * | .  /  |   | .  /  |
    * |. /    |   |  / .  |
    * |/______|   |/______|   
    * </pre>
    * The two points on the left are on the same half-space with respect to a
    * Top-Right to Bottom-Left diagonal (TR_BL_DIAG).
    * <p>
    * The two points on the right, however, are not.
    * 
    * @param xFrom
    * @param yFrom
    * @param xTo
    * @param yTo
    * @param type type of diagonal tile. Either 
    *        {@link TileDescription#TL_BR_DIAG} or 
    *        {@link TileDescription#TR_BL_DIAG}
    * @return true if they fall on the same side of the diagonal line
    */
   private boolean checkHalfSpace( final int xFrom,
                                   final int yFrom,
                                   final int xTo,
                                   final int yTo,
                                   final int type )
   {
      boolean sameHalf = false;
      if( type == TileDescription.TL_BR_DIAG )
      {
         final int from = yFrom - xFrom;
         final int to = yTo - xTo;
         if( ( from < 0 && to < 0 ) || ( from > 0 && to > 0 ) )
         {
            sameHalf = true;
         }
      }
      else if( type == TileDescription.TR_BL_DIAG )
      {
         final int from = ScreenSettings.tileSize - xFrom - yFrom;
         final int to = ScreenSettings.tileSize - xTo - yTo;
         if( ( from < 0 && to < 0 ) || ( from > 0 && to > 0 ) )
         {
            sameHalf = true;
         }
      }
      return sameHalf;
   }
}
