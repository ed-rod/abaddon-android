package uk.co.eduardo.abaddon.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.eduardo.abaddon.graphics.layer.Direction;
import uk.co.eduardo.abaddon.graphics.layer.LayerManager;
import uk.co.eduardo.abaddon.tileset.TileDescription;
import uk.co.eduardo.abaddon.util.Coordinate;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * <p>
 * Implements a path finding algorithm for a map.
 * </p>
 * <p>
 * The path finding is simplified as a path need only be found between the source point and
 * another point somewhere on the section of map that is being displayed. Due to this, the problem
 * is constrained to finding a path in a rectangular subsection of the map. Any paths that require
 * walking a loop off-screen and then returning will not be found. This is a benefit, as it would
 * almost be cheating if a difficult-to-reach section is visible but the path not immediately 
 * obvious. The player could then just click the target location and have the game do the hard
 * work!
 * </p>
 * <p>
 * Variation of the A* algorithm.
 * </p>
 * 
 * @author Eduardo Rodrigues
 */
public class PathFinder
{
   private PathFinder()
   {
      // Prevent instantiation.
   }
   
   /**
    * Finds a path from the source to the target.
    * 
    * @param xFrom the X pixel position of the source of the path.
    * @param yFrom the Y pixel position of the source of the path.
    * @param xTo the X pixel position of the target of the path.
    * @param yTo the Y pixel position of the target of the path.
    * @param path output variable. Will contain the path from the source to the target.
    */
   public static void findPath( final int xFrom,
                                final int yFrom,
                                final int xTo,
                                final int yTo,
                                final LinkedList< Coordinate > path )
   {
      path.clear();
      
      // Convert to tile positions
      final int tileSize = ScreenSettings.tileSize;
      final int xTileFrom = xFrom / tileSize;
      final int yTileFrom = yFrom / tileSize;
      final int xTileTo = xTo / tileSize;
      final int yTileTo = yTo / tileSize;
      
      // First, check to see if the target tile is accessible at all (from any direction)
      if( !isTargetAccessible( xTo, yTo ) )
      {
         return;
      }
      
      final int screenTileWidth = ScreenSettings.width / ScreenSettings.tileSize;
      final int screenTileHeight = ScreenSettings.height / ScreenSettings.tileSize;
      
      final int xMapEnd = LayerManager.getMapLayers().get( 0 ).fullMap[ 0 ].length;
      final int yMapEnd = LayerManager.getMapLayers().get( 0 ).fullMap.length;
      
      final int xTileStart = Math.max( xTileFrom - ( ( screenTileWidth + 1 ) / 2 ), 0 );
      final int yTileStart = Math.max( yTileFrom - ( ( screenTileHeight + 1 ) / 2 ), 0 );
      final int xTileEnd = Math.min( xTileFrom + ( ( screenTileWidth + 1 ) / 2 ), xMapEnd );
      final int yTileEnd = Math.min( yTileFrom + ( ( screenTileHeight + 1 ) / 2 ), yMapEnd );
      
      final List< PathItem > open = new ArrayList< PathItem >();
      final List< PathItem > closed = new ArrayList< PathItem >();
      
      int distance = ( Math.abs( xTileTo - xTileFrom ) + Math.abs( yTileTo - yTileFrom ) ) * 10;
      final PathItem start = new PathItem( new Coordinate( xTileFrom, yTileFrom), null, 0, distance );
      final Coordinate target = new Coordinate( xTileTo, yTileTo );
      
      open.add( start );
      while( !open.isEmpty() && !contains( closed, target ) )
      {
         final PathItem current = findWithLowestCost( open );
         remove( open, current );
         closed.add( current );
         
         final int xPixelFrom = current.coord.x * tileSize;
         final int yPixelFrom = current.coord.y * tileSize;
         
         for( int d = 0; d < Direction.values().length; d++ )
         {
            final Direction direction = Direction.values()[ d ];
            final Coordinate nextCoord = new Coordinate( current.coord.x + direction.vector.x,
                                                         current.coord.y + direction.vector.y );
            
            // If the new coordinate is out of bounds, ignore
            if( nextCoord.x < xTileStart || nextCoord.x > xTileEnd || nextCoord.y < yTileStart ||
                     nextCoord.y > yTileEnd )
            {
               continue;
            }
               
            // If on the the closed list, ignore.
            if( contains( closed, nextCoord ) )
            {
               continue;
            }
            
            final boolean isDiagonal = direction.vector.x != 0 && direction.vector.y != 0;
            
            if( !isDiagonal )
            {
               // If we cannot move to this location, ignore.
               final int xPixelTo = nextCoord.x * tileSize;
               final int yPixelTo = nextCoord.y * tileSize;
               if( LayerManager.canHeroMove( xPixelFrom, yPixelFrom, xPixelTo, yPixelTo ) != 1 )
               {
                  continue;
               }
            }
            else
            {
               // In the case of diagonal motion, we check that it's both possible to:
               // 1. Go horizontal and then vertical AND
               // 2. Go vertical and then horizontal
               final int xPixelTo = nextCoord.x * tileSize;
               final int yPixelTo = nextCoord.y * tileSize;
               final int xPixelHTo = xPixelTo;
               final int yPixelHTo = yPixelTo - ( direction.vector.y * tileSize );
               final int xPixelVTo = xPixelTo - ( direction.vector.x * tileSize );
               final int yPixelVTo = yPixelTo;
               final int moveH = LayerManager.canHeroMove( xPixelFrom, yPixelFrom, xPixelHTo, yPixelHTo );
               final int moveHV = LayerManager.canHeroMove( xPixelHTo, yPixelHTo, xPixelTo, yPixelTo );
               final int moveV = LayerManager.canHeroMove( xPixelFrom, yPixelFrom, xPixelVTo, yPixelVTo );
               final int moveVH = LayerManager.canHeroMove( xPixelVTo, yPixelVTo, xPixelTo, yPixelTo );
               if( moveH == 0 || moveHV == 0 || moveV == 0 || moveVH == 0 )
               {
                  continue;
               }
               
               // Now, if any of of the HV, or VH movements crosses a diagonal tile, we need to 
               // check the entry and exit directions.
               if( moveH != 1 || moveHV != 1 || moveV != 1 || moveVH != 1  )
               {
                  boolean allowed = false;
                  if( ( moveH == TileDescription.TL_BR_DIAG && moveHV == TileDescription.TL_BR_DIAG ) ||
                      ( moveV == TileDescription.TL_BR_DIAG && moveVH == TileDescription.TL_BR_DIAG ) )
                  {
                     allowed = ( direction.vector.x * direction.vector.y ) > 0;
                  }
                  if( ( moveH == TileDescription.TR_BL_DIAG && moveHV == TileDescription.TR_BL_DIAG ) ||
                      ( moveV == TileDescription.TR_BL_DIAG && moveVH == TileDescription.TR_BL_DIAG ) )
                  {
                     allowed = ( direction.vector.x * direction.vector.y ) < 0;
                  }
                  if( !allowed )
                  {
                     continue;
                  }
               }
            }
            
            final int localCost = isDiagonal ? 14 : 10;
            
            final PathItem previouslyVisited = find( open, nextCoord );
            if( previouslyVisited == null )
            {
               // If it's not on the open list, add it.
               distance = ( Math.abs( xTileTo - nextCoord.x ) + Math.abs( yTileTo - nextCoord.y ) ) * 10;
               open.add(  new PathItem( nextCoord, current, localCost, distance ) );
            }
            else
            {
               if( current.distanceCome + localCost < previouslyVisited.distanceCome )
               {
                  previouslyVisited.setParent( current, localCost );
               }
            }
         }
      }
      
      PathItem pathItem = find( closed, target );
      while( pathItem != null )
      {
         path.addFirst( pathItem.coord );
         pathItem = pathItem.parent;
      }
   }
   
   private static boolean contains( final List< PathItem > list, final Coordinate check )
   {
      return find( list, check ) != null;
   }
   
   private static PathItem find( final List< PathItem > list, final Coordinate check )
   {
      for( int i = 0; i < list.size(); i++ )
      {
         final PathItem pathItem = list.get( i );
         if( pathItem.coord.equals( check ) )
         {
            return pathItem;
         }
      }
      return null;
   }
   
   private static PathItem findWithLowestCost( final List< PathItem > list )
   {
      int lowestCost = Integer.MAX_VALUE;
      PathItem lowest = null;
      for( int i = 0; i < list.size(); i++ )
      {
         final PathItem check = list.get( i );
         if( check.cost < lowestCost )
         {
            lowestCost = check.cost;
            lowest = check;
         }
      }
      return lowest;
   }
   
   private static void remove( final List< PathItem > list, final PathItem check )
   {
      for( int i = 0; i < list.size(); i++ )
      {
         if( list.get( i ) == check )
         {
            list.remove( i );
         }
      }
   }
   
   private static boolean isTargetAccessible( final int xTo, final int yTo )
   {
      final int tileSize = ScreenSettings.tileSize;
      
      // Check whether it's possible to get to the tile at all:
      if( LayerManager.canHeroMove( xTo - tileSize, yTo, xTo, yTo ) == 0 &&
               LayerManager.canHeroMove( xTo, yTo - tileSize, xTo, yTo ) == 0 &&
               LayerManager.canHeroMove( xTo + tileSize, yTo, xTo, yTo ) == 0 &&
               LayerManager.canHeroMove( xTo, yTo + tileSize, xTo, yTo ) == 0 )
      {
         return false;
      }
      
      return true;
   }
   
   private static final class PathItem
   {
      private final Coordinate coord;
      
      private final int distanceToGo;
      
      private PathItem parent;
      
      private int distanceCome;
      
      private int cost;
      
      private PathItem( final Coordinate coord, 
                        final PathItem parent,
                        final int localDistance,
                        final int distanceToGo )
      {
         this.coord = coord;
         this.parent = parent;
         this.distanceCome = parent != null ?  parent.distanceCome + localDistance : localDistance;
         this.distanceToGo = distanceToGo;
         this.cost = this.distanceCome + this.distanceToGo;
      }
      
      void setParent( final PathItem parent, final int localDistance )
      {
         this.parent = parent;
         this.distanceCome = parent.distanceCome + localDistance;
         this.cost = this.distanceCome + this.distanceToGo;
      }
      
      @Override
      @SuppressWarnings( "nls" )
      public String toString()
      {
         return this.coord + "  G=" + this.distanceCome + "  H=" + this.distanceToGo + "  F=" +
                  this.cost + "  [" + this.parent.coord + "]";
      }
   }
}
