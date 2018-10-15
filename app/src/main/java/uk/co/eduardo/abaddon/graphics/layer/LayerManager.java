package uk.co.eduardo.abaddon.graphics.layer;

import java.util.ArrayList;

import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * Manages all the different game layers
 * 
 * @author Ed
 */
public final class LayerManager
{
   //================|  Fields             |====================================
   
   /** Layers rendered below the sprites */
   private static final ArrayList<TiledMap> MAPS = new ArrayList<TiledMap>();

   /** Sprites. Includes the hero and NPCs */
   private static final ArrayList<Sprite> SPRITES = new ArrayList<Sprite>();
   
   /** Overlays are drawn above the map and sprites but below windows. */
   private static final ArrayList<Layer> OVERLAYS =  new ArrayList<Layer>();

   /** The windows drawn above all other layers */
   private static final ArrayList<GameWindow> WINDOWS = new ArrayList<GameWindow>();
   
   /** The hero sprite layer */
   private static Sprite hero;
   
   
   //================|  Constructors       |====================================
   
   /**
    * Private constructor to prevent instantiation
    */
   private LayerManager()
   {
      // private constructor
   }
   
   
   //================|  Public Methods     |====================================
   
   /**
    * @return a list of {@link TiledMap}s that are being managed.
    */
   public static ArrayList<TiledMap> getMapLayers()
   {
      return MAPS;
   }
   
   /**
    * Adds a map to the map array. It will be drawn above any previously
    * added maps/
    * 
    * @param map a map to add to the map array
    */
   public static void pushMap( final TiledMap map )
   {
      MAPS.add( map );
   }
   
   /**
    * Adds a Collection of maps to the map array. They will be drawn
    * above any previously added maps.
    * 
    * @param maps a list of maps to add to the map array
    */
   public static void pushMaps( final ArrayList<TiledMap> maps )
   {
      MAPS.addAll( maps );
   }
   
   /**
    * @param maps the tiled maps
    */
   public static void setMaps( final ArrayList<TiledMap> maps )
   {
      clearMaps();
      MAPS.addAll( maps );
   }
   
   /**
    * Remove all maps
    */
   public static void clearMaps()
   {
      MAPS.clear();
   }

   /**
    * @return a list of sprites
    */
   public static ArrayList<Sprite> getSpriteLayers()
   {
      return SPRITES;
   }
   
   /**
    * Adds a sprite to the sprite array. It will be drawn above any previously
    * added sprites.
    * 
    * @param sprite the sprite to add
    * @see LayerManager#sortSprites()
    */
   public static void addSprite( final Sprite sprite )
   {
      SPRITES.add( sprite );
   }
   
   /**
    * Sets the sprites.
    * 
    * @param sprites set the sprites
    */
   public static void setSprites( final ArrayList< Sprite > sprites )
   {
      clearSprites();
      for( int s = 0; s < sprites.size(); s++ )
      {
         SPRITES.add( sprites.get( 0 ) );
      }
   }
   
   /**
    * Remove all sprites
    */
   public static void clearSprites()
   {
      SPRITES.clear();
   }
   
   /**
    * Adds an overlay that is drawn above all existing overlays.
    * @param overlay the overlay layer to add.
    */
   public static void addOverlay( final Layer overlay )
   {
      if( overlay != null )
      {
         OVERLAYS.add( overlay );
      }
   }
   
   /**
    * Adds a list of overlays to be drawn above all existing overlays.
    * @param overlays the overlays to add.
    */
   public static void addOverlays( final ArrayList<Layer> overlays )
   {
      if( overlays != null )
      {
         OVERLAYS.addAll( overlays );
      }
   }
   
   /**
    * Removes an overlay layer.
    * @param overlay the overlay layer to remove.
    */
   public static void removeOverlay( final Layer overlay )
   {
      OVERLAYS.remove( overlay );
   }
   
   /**
    * @return a list of all the overlay layers.
    */
   public static ArrayList<Layer> getOverlayLayers()
   {
      return OVERLAYS;
   }
   
   /**
    * Removes all overlay layers.
    */
   public static void clearOverlays()
   {
      OVERLAYS.clear();
   }
   
   /**
    * @return a list of windows to render above all others
    */
   public static ArrayList<GameWindow> getWindowLayers()
   {
      return WINDOWS;
   }
   
   /**
    * Adds a window to the window array. It will be drawn above any previously
    * added window.
    * 
    * @param window the window to add
    */
   public static void pushWindow( final GameWindow window )
   {
      WINDOWS.add( window );
   }
   
   /**
    * @param windows set all the windows
    */
   public static void setWindows( final ArrayList<GameWindow> windows )
   {
      clearWindows();
      WINDOWS.addAll( windows );
   }
   
   /**
    * Remove all windows.
    */
   public static void clearWindows()
   {
      WINDOWS.clear();
   }
   
   /**
    * Removes all layers from the game
    */
   public static void clearAll()
   {
      MAPS.clear();
      SPRITES.clear();
      WINDOWS.clear();
   }
   
   /**
    * @return an array of all the game layers in order where the the layer at
    * the lowest index is to be drawn first and the layer with the highest 
    * index is to be drawn last.
    * <p>
    * For performance reasons we return an <code>ArrayList</code> rather than
    * the <code>List</code> interface type.
    */
   public static ArrayList<Layer> getAllLayersInOrder()
   {
      final ArrayList<Layer> allLayers = new ArrayList<Layer>();
      
      // cache fields locally for performance.
      final ArrayList<Sprite> cachedSprites = SPRITES;
      final ArrayList<TiledMap> cachedMaps = MAPS;
      final int numSprites = cachedSprites.size();
      Sprite sprite = null;
      
      final int numMaps = cachedMaps.size();
      
      for( int map = 0; map < numMaps; map++ )
      {
         allLayers.add( cachedMaps.get( map ) );
         
         for( int spriteIdx = 0; spriteIdx < numSprites; spriteIdx++ )
         {
            sprite = cachedSprites.get( spriteIdx );
            if( sprite.getLayerIndex() == map )
            {
               allLayers.add( sprite );
            }
         }
      }
      
      // Add the overlay layers above all tiled layers and sprites
      allLayers.addAll( OVERLAYS );
      
      // Add the windows above all tiled layers, sprites and overlays.
      allLayers.addAll( WINDOWS );
      
      return allLayers;
   }
   
   /**
    * Re-order the sprite layers based on their y pixel value.
    */
   public static void sortSprites()
   {
      // Bubble-sort the layers sprite layers.
      // There's plenty of scope for improvement here...
      final ArrayList<Sprite> cachedSprites = SPRITES;
      final int numIterations = cachedSprites.size() - 1;
      
      for( int first = 0; first < numIterations; first++ )
      {
         for( int second = first; second < numIterations; second++ )
         {
            final Sprite left =  cachedSprites.get( second );
            final Sprite right = cachedSprites.get( second + 1 );
            if( right.getOffsetPixelPosition().y <
                left.getOffsetPixelPosition().y )
            {
               // swap
               cachedSprites.set( second, right );
               cachedSprites.set( second + 1, left );
            }
         }
      }
   }
   
   /**
    * @param hero sets the layer identified as the hero.
    * @see #isTileOccupied(int, int, Sprite, boolean, boolean)
    * @see TiledMap#canMove(int, int, int, int)
    */
   public static void setHero( final Sprite hero )
   {
      LayerManager.hero = hero;
   }
   
   /**
    * Checks all the layers below the hero sprite. Return the worst case
    * from all the layers checked.
    * 
    * @param xFrom the source X map <em>pixel<em> position
    * @param yFrom the source Y map <em>pixel<em> position
    * @param xTo the target X map <em>pixel<em> position
    * @param yTo the target Y map <em>pixel<em> position
    * 
    * @return 0 if cannot move to the new point<br>
    *         1 if can move to the new point<br>
    *         TL_BR_DIAG if cannot move and 'to' tile is diagonal<br>
    *         TR_BL_DIAG if cannot move and 'to' tile is diagonal<br>
    */
   public static int canHeroMove( final int xFrom, final int yFrom,
                              final int xTo, final int yTo )
   {
      return canMove( xFrom, yFrom, xTo, yTo, hero, true, false );
   }
   
   /**
    * Checks all the layers below the hero sprite. Return the worst case
    * from all the layers checked.
    * 
    * @param xFrom the source X map <em>pixel<em> position
    * @param yFrom the source Y map <em>pixel<em> position
    * @param xTo the target X map <em>pixel<em> position
    * @param yTo the target Y map <em>pixel<em> position
    * @param sprite the sprite that is trying to move
    * @param ignoreHero ignore the hero layer when checking what tile positions
    *                   are occupied
    * @param ignoreNpcs ignore the NPC layers when checking what tile positions
    *                   are occupied
    * 
    * @return 0 if cannot move to the new point<br>
    *         1 if can move to the new point<br>
    *         TL_BR_DIAG if cannot move and 'to' tile is diagonal<br>
    *         TR_BL_DIAG if cannot move and 'to' tile is diagonal<br>
    */
   public static int canMove( final int xFrom, final int yFrom,
                              final int xTo, final int yTo,
                              final Sprite sprite,
                              final boolean ignoreHero,
                              final boolean ignoreNpcs )
   {
      final int tileSize = ScreenSettings.tileSize;
      final ArrayList<TiledMap> cachedMaps = MAPS;
      
      final int xTileTo = xTo / tileSize;
      final int yTileTo = yTo / tileSize;
      
      if( isTileOccupied( xTileTo, yTileTo, sprite, ignoreHero, ignoreNpcs ) )
      {
         return 0;
      }
      
      final int layerIndex = sprite.getLayerIndex();
      
      // First check the map just below the hero. If both tiles are walkable and
      // are not -1, we can move.
      final TiledMap top = cachedMaps.get( layerIndex );
      
      if( xTileTo < 0 || yTileTo < 0 || yTileTo >= top.fullMap.length ||
               xTileTo >= top.fullMap[ 0 ].length )
      {
         return 0;
      }
      
      final int tileTo = top.fullMap[yTileTo][xTileTo];
      final int move = top.canMove( xFrom, yFrom, xTo, yTo );
      if( move != 0 && tileTo != -1 )
      {
         return move;
      }
      
      int retM = 1;
      for( int i = 0; i <= layerIndex; i++ )
      {
         final TiledMap map = cachedMaps.get( i );
         final int m = map == top ? move : map.canMove( xFrom, yFrom, xTo, yTo );
         if( m == 0 )
         {
            return 0;
         }
         else if( m != 1 )
         {
            retM = m;
         }
      }
      return retM;
   }
   
   /**
    * Checks all layers to see if it is possible to jump from the specified location to the other
    * location.
    * 
    * @param xFrom the source X map pixel position
    * @param yFrom the source Y map pixel position
    * @param xTo the target X map pixel position
    * @param yTo the target Y map pixel position
    * @return true if it is possible to jump from (xFrom, yFrom) to (xTo, yTo)
    *         in pixel coordinates.
    */     
   public static boolean canJump( final int xFrom, final int yFrom,
                                  final int xTo, final int yTo )
   {
      final int layerIndex = hero.getLayerIndex();
      
      // Cache locally.
      final ArrayList<TiledMap> cachedMaps = MAPS;
      TiledMap map = null;
      
      boolean retM = false;
      for( int m = 0; m <= layerIndex; m++ )
      {
         map = cachedMaps.get( m );
         retM = retM || map.canJump( xFrom, yFrom, xTo, yTo );
      }
      return retM;
   }
   
   /**
    * @param x pixel location to check
    * @param y pixel location to check
    * @param ignoreHero ignore the hero's occupied tile positions
    * @return true if any of the sprites are occupying that pixel location
    */
   public static boolean isPixelOccupied( final int x, final int y,
                                          final boolean ignoreHero )
   {
      final int tileSize = ScreenSettings.tileSize;
      
      return isTileOccupied( x / tileSize, y / tileSize,
                             null, ignoreHero, false );
   }
   
   /**
    * @param x tile location to check
    * @param y tile location to check
    * @param sprite the sprite that is checking whether the tile is occupied.
    * @param ignoreHero ignore the hero's occupied tile positions
    * @param ignoreNpcs ignore the NPC's occupied tile positions
    * @return true if any of the sprites are occupying that tile location
    */
   public static boolean isTileOccupied( final int x, final int y,
                                         final Sprite sprite,
                                         final boolean ignoreHero,
                                         final boolean ignoreNpcs )
   {
      // Avoid using the SPRITES iterator
      final ArrayList<Sprite> cachedSprites = SPRITES;
      final int numSprites = cachedSprites.size();
      Sprite currentSprite = null;
      
      // Check through the sprites to see if any of them are occupying this
      // tile location
      for( int spriteIdx = 0; spriteIdx < numSprites; spriteIdx++ )
      {
         currentSprite = cachedSprites.get( spriteIdx );
         if( ignoreHero && currentSprite == hero )
         {
            // Ignore the hero's occupied positions
            continue;
         }
         if( ignoreNpcs && currentSprite instanceof NPC )
         {
            continue;
         }
         boolean sameLayer = true;
         if( sprite != null )
         {
            sameLayer = currentSprite.getLayerIndex() ==
               sprite.getLayerIndex();
         }
         if( sameLayer && currentSprite.isOccupying( x, y ) )
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * @param x the position in pixel coordinates.
    * @param y the position in pixel coordinates.
    * @return the Sprite at the specified pixel location or null if no Sprite
    *         occupies that position
    */
   public static Sprite getSpriteAtPixel( final int x, final int y )
   {
      final int tileSize = ScreenSettings.tileSize;
      return getSpriteAtTile( x / tileSize, y / tileSize );
   }
   
   /**
    * Check to see if any sprites are occupying the tile with the given
    * coordinates.
    * 
    * @param x the position in tile coordinates.
    * @param y the position in tile coordinates.
    * @return the Sprite at the specified tile location or null if no Sprite
    *         occupies that tile
    */
   public static Sprite getSpriteAtTile( final int x, final int y )
   {
      // Avoid using the SPRITES iterator
      final ArrayList<Sprite> cachedSprites = SPRITES;
      final int numSprites = cachedSprites.size();
      Sprite sprite = null;
      
      // Check through the sprites to see if any of them are occupying this
      // tile location
      for( int spriteIdx = 0; spriteIdx < numSprites; spriteIdx ++ )
      {   
         sprite = cachedSprites.get( spriteIdx );
         if( sprite.isOccupying( x, y ) )
         {
            return sprite;
         }
      }
      return null;
   }
}
