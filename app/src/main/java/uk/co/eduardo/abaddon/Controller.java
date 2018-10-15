package uk.co.eduardo.abaddon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent.PointerCoords;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import uk.co.eduardo.abaddon.character.CharacterClass;
import uk.co.eduardo.abaddon.graphics.layer.Direction;
import uk.co.eduardo.abaddon.graphics.layer.GameWindow;
import uk.co.eduardo.abaddon.graphics.layer.Layer;
import uk.co.eduardo.abaddon.graphics.layer.LayerManager;
import uk.co.eduardo.abaddon.graphics.layer.NPC;
import uk.co.eduardo.abaddon.graphics.layer.PC;
import uk.co.eduardo.abaddon.graphics.layer.PathDisplay;
import uk.co.eduardo.abaddon.graphics.layer.Sprite;
import uk.co.eduardo.abaddon.graphics.layer.TextWindow;
import uk.co.eduardo.abaddon.graphics.layer.TiledMap;
import uk.co.eduardo.abaddon.map.MapDefinition;
import uk.co.eduardo.abaddon.map.MapFactory;
import uk.co.eduardo.abaddon.map.PathFinder;
import uk.co.eduardo.abaddon.map.actions.ActionManager;
import uk.co.eduardo.abaddon.map.actions.ChangeLayerAction;
import uk.co.eduardo.abaddon.map.actions.MapAction;
import uk.co.eduardo.abaddon.map.actions.TeleportAction;
import uk.co.eduardo.abaddon.state.GameState;
import uk.co.eduardo.abaddon.state.StateMachine;
import uk.co.eduardo.abaddon.tileset.TileDescription;
import uk.co.eduardo.abaddon.tileset.TileDescriptionReader;
import uk.co.eduardo.abaddon.tileset.Tileset;
import uk.co.eduardo.abaddon.util.Coordinate;
import uk.co.eduardo.abaddon.util.DayNight;
import uk.co.eduardo.abaddon.util.Debug;
import uk.co.eduardo.abaddon.util.DisplayResolutions;
import uk.co.eduardo.abaddon.util.JumpInfo;
import uk.co.eduardo.abaddon.util.Res;
import uk.co.eduardo.abaddon.util.ResourceUtilities;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * The main game controller. Most logic is handled here.
 * 
 * @author Eduardo Rodrigues
 */
@SuppressWarnings( "nls" )
public class Controller extends Thread
{
   //================|  Fields             |====================================
   
   /** The hero's extent around his feet in each direction */
   public static final int EXTENT = 6;
   
   /** Centre */
   private static final int CENTRE = 0;
   
   /** Left */
   private static final int LEFT = 1;

   /** Up */
   private static final int UP = 2;
   
   /** Right */
   private static final int RIGHT = 3;

   /** Down */
   private static final int DOWN = 4;
   
   /** The number of pixels the main character moves during each frame */
   private static final int WALK_INCREMENT = 2;
   
   /** How many frames of pushing against the edge are needed before jumping */
   private static final int JUMP_TRIES = 15;
   
   /** Nanoseconds per millisecond. */
   private static final long NS_IN_MS = 1000000;
   
   /** The view used to display text information to the user */
   protected TextView statusTextView;
   
   /** Handle to the surface manager object we interact with. */
   private final SurfaceHolder surfaceHolder;
   
   /** Used to monitor jumping progress */
   private int lastJumpTryDirection = -1;
   
   /** Counts how many frames we have been trying to jump */
   private int jumpCounter;
   
   /** Is the main character jumping */
   private boolean jumping = false;
   
   /** The JumpInfo structure used for jumping */
   private JumpInfo jumpInfo = null;
   
   /** Frame counter */
   private int counter = 0;

   /** The paint object used for drawing */
   private final Paint paint = new Paint();
   
   /** For filling regions in black. */
   private final Paint blackPaint = new Paint();
   
   /** The main character */
   private Sprite hero;
   
   /** In touch interaction mode, this contains the path along which the hero is walking. */
   private final LinkedList< Coordinate > heroPath = new LinkedList< Coordinate >();
   
   /** Time of last FPS window update. Used for calculating FPS */
   private long lastFpsWindowUpdate = System.currentTimeMillis();
   
   /** How many frames had been drawn by last FPS window update. */
   private long lastCounter;
   
   /** The FPS being drawn */
   private float fps;
   
   /** The time the last frame was rendered. */
   private long oneFrameTime;
   
   /** The debug window that displays FPS information */
   private TextWindow fpsWindow;
   
   /** The debug window that displays accelerometer information */
   private TextWindow accelWindow;
   
   /** The debug window that displays touch information */
   private TextWindow touchWindow;

   /** The debug layer that displays path-finding information. */
   private final PathDisplay pathLayer = new PathDisplay( this.heroPath );
   
   /** Show map loading information */
   private final MapLoadHandler maploadHandler = new MapLoadHandler();
   
   /** Indicate whether the surface has been created & is ready to draw */
   private boolean mRun = false;
   
   
   //================|  Inner Classes      |====================================
   
   /**
    * Handles map loading progress screen
    * 
    * @author Eduardo Rodrigues
    */
   private static class MapLoadHandler extends Handler
   {
      /** Started loading message */
      public static final int LOAD_START = 0;
      
      /** Ended loading message */
      public static final int LOAD_END = 1;
      
      /** Error occurred while loading message */
      public static final int LOAD_ERR = 2;
      
      @Override
      public void handleMessage( final Message msg )
      {
         if( msg.what == LOAD_START )
         {
            // Show the loading screen
            StateMachine.getInstance().setState( GameState.LOADING );
         }
         else if( msg.what == LOAD_END )
         {
            // Start the main game loop
            StateMachine.getInstance().setState( GameState.RUNNING );
         }
         else if( msg.what == LOAD_ERR )
         {
            StateMachine.getInstance().setState( GameState.ERROR );
         }
      }
   }
   
   /**
    * Loads a map in a background thread
    * 
    * @author Eduardo Rodrigues
    */
   private class MapLoadThread extends Thread
   {
      /** The name of the map to load */
      private final String map;
      
      /** The starting position on the map */
      private final Coordinate startPos;
      
      /**
       * @param map the name of the map to load
       * @param startPos the starting position on the map. If null, use the
       *                 default start position defined in the map
       */
      MapLoadThread( final String map, final Coordinate startPos )
      {
         this.map = map;
         this.startPos = startPos;
      }
      
      @Override
      public void run()
      {
         // Update the view to show we are loading a map
         Controller.this.maploadHandler.sendEmptyMessage( Controller.MapLoadHandler.LOAD_START );

         LayerManager.clearAll();

         System.gc();

         final Resources resources = Res.resources;

         // Load the map
         MapDefinition mapDef = null;
         try
         {
            final int resourceId = ResourceUtilities.getRawResourceId( this.map );
            mapDef = MapFactory.readMap( resources.openRawResource( resourceId ) );
         }
         catch( final IOException exception )
         {
            // Failed to load map. Set error state.
            Controller.this.maploadHandler.sendEmptyMessage( Controller.MapLoadHandler.LOAD_ERR );
            return;
         }

         mapDef.setMapName( this.map );

         // Load the tileset
         final String tilesetName = mapDef.headerSection.getTilesetName();
         final int tilesetId = ResourceUtilities.getDrawableResourceId( tilesetName );
         final Drawable tiles = resources.getDrawable( tilesetId );

         final int tilesetDscId = ResourceUtilities.getRawResourceId( tilesetName );
         final InputStream rawStream = resources.openRawResource( tilesetDscId );

         final TileDescription tileDesc = TileDescriptionReader.readStream( rawStream );

         final Tileset tileset = new Tileset( tiles, tileDesc );

         final int[][][] mapArrays = mapDef.mapsSection.getMaps();

         for( final int[][] currentMap : mapArrays )
         {
            final TiledMap tiledMap = new TiledMap( currentMap, tileset );
            LayerManager.pushMap( tiledMap );
         }

         // Create a sprite layer
         final Drawable spriteImage = resources.getDrawable( R.drawable.pc3 );
         Controller.this.hero = new PC( spriteImage, 
                                        ScreenSettings.tileSize, 
                                        ScreenSettings.spriteHeight, 
                                        Arrays.asList( CharacterClass.Warrior ) );
         Controller.this.hero.setLayerIndex( mapDef.headerSection.getLayerIndex() );
         LayerManager.addSprite( Controller.this.hero );
         LayerManager.setHero( Controller.this.hero );
         
         // Clear any path the hero may have been walking along
         Controller.this.heroPath.clear();

         // Create and add all of the sprite layers
         for( int npcToAdd = 0; npcToAdd < mapDef.npcsSection.getNpcCount(); npcToAdd++ )
         {
            final NPC npc = mapDef.npcsSection.getNpc( npcToAdd );
            LayerManager.addSprite( npc );

            // Occupy the tile the NPC is standing on
            final Coordinate npcTile = npc.getTilePosition();
            npc.occupy( npcTile.x, npcTile.y );
         }

         // Create a window with the map name. The x position is irrelevant as
         // the window will be centred.
         if( mapDef.isTownMap() || mapDef.isCaveMap() )
         {
            final TextWindow mapName = new TextWindow( 10, 10, mapDef.getStrippedName(), 50 );

            mapName.centre( GameWindow.HORIZONTAL );

            LayerManager.pushWindow( mapName );
         }
         
         final Coordinate start = this.startPos == null ?
                                  mapDef.headerSection.getStartPos() :
                                  this.startPos;
      
         final int tileSize = ScreenSettings.tileSize;
         
         // Transform the coordinate to pixel-space and centre on the tile
         final int xPos = ( start.x * tileSize ) + ( tileSize >> 1 ) + 1;
         final int yPos = ( start.y * tileSize ) + ( tileSize >> 1 ) + 1;
         
         Controller.this.hero.setPixelPosition( xPos, yPos );
         
         ActionManager.setActions( mapDef.actionsSection.getActions() );
         
         // Set the day-night cycle going depending on what type of map it is
         Debug.useDayNight = !( mapDef.isInteriorMap() || mapDef.isCaveMap() || mapDef.isTownMap() );
         
         // Inform that we have finished loading the map
         Controller.this.maploadHandler.sendEmptyMessage( Controller.MapLoadHandler.LOAD_END );
      }
   }
   
   
   //================|  Constructors       |====================================
      
   /**
    * Constructs a <code>Controller</code>
    * 
    * @param surfaceHolder surface holder from a {@link SurfaceView}.
    */
   public Controller( final SurfaceHolder surfaceHolder )
   {
      // get handles to some important objects
      this.surfaceHolder = surfaceHolder;
      this.paint.setColor( 0xFFFFFFFF );
      this.paint.setStyle( Paint.Style.FILL );
      
      this.blackPaint.setColor( 0xFF000000 );
      this.blackPaint.setStyle( Paint.Style.FILL );
   }
   
   
   //================|  Public Methods     |====================================
   
   /**
    * Save game state so that the user does not lose anything if the game 
    * process is killed while we are in the background.
    * 
    * @param bundle the <code>Bundle</code> into which we save our state.
    */
   public void saveState( final Bundle bundle )
   {
//      map.putLong( "mMoveDelay", Long.valueOf( mMoveDelay ) );
   }
   
   /**
    * The main loop of the game. Here we perform actions and redraw.
    * 
    * {@inheritDoc}
    */
   @Override
   public void run()
   {
      // Cache some variables locally for speed.
      final StateMachine machine = StateMachine.getInstance();
      final GameState running = GameState.RUNNING;
      while( this.mRun )
      {
         Canvas canvas = null;
         try
         {
            canvas = this.surfaceHolder.lockCanvas( null );
            synchronized( this.surfaceHolder )
            {
               if( machine.getState() == running )
               {
                  // Make sure we're not going too fast.
                  checkTiming();
                  
                  // Setup debug-related stuff
                  performDebugUpdate();
                  
                  // Update the new state of the game
                  performOneFrame();
                  
                  // Draw the new frame.
                  drawFrame( canvas );
               }
               else
               {
                  // draw a black background
                  canvas.drawRect( 0, 0, 
                              ScreenSettings.width, 
                              ScreenSettings.height, 
                              this.blackPaint );
               }
            }
         }
         finally
         {
            // Try to leave the surface in a consistent state.
            if( canvas != null )
            {
               this.surfaceHolder.unlockCanvasAndPost( canvas );
            }
         }
      }
   }
   
   /**
    * Restore game state if our process is being relaunched
    * 
    * @param bundle a Bundle containing the game state
    */
   public void restoreState( final Bundle bundle )
   {
      StateMachine.getInstance().setState( GameState.PAUSE );
   }

   /**
    * @param textView The view to use when the game is displaying messages to the user.
    */
   public void setTextView( final TextView textView )
   {
      this.statusTextView = textView;
   }

   /**
    * Starts a new game by loading the first map.
    */
   public void handleStartNewGame()
   {
      loadMap();
   }
   
   /**
    * Used to signal the thread whether it should be running or not. 
    * Passing true allows the thread to run; passing false will shut it down 
    * if it's already running. Calling start() after this was most recently 
    * called with false will result in an immediate shutdown.
    * 
    * @param b true to run, false to shut down
    */
   public void setRunning( final boolean b )
   {
      this.mRun = b;
   }
   
   
   //================|  Private Methods    |====================================
   
   /**
    * Loads a map
    */
   private void loadMap()
   {
      new MapLoadThread( "antiochtown", null ).start();
   }
   
   /**
    * Handles the basic update loop, performs actions based on which key 
    * events occurred in the during the last animation frame.
    */
   private void performOneFrame()
   {
      // Remove any closed windows
      updateWindows();
      
      LayerManager.sortSprites();
      
      checkScreenInteraction();
      // Check to see if any of the windows are modal
      boolean redirectToWindow = false;
      
      final ArrayList<GameWindow> windows = LayerManager.getWindowLayers();
      
      // Count down to redirect to the topmost modal window
      for( int w = windows.size() - 1; w >= 0 && !redirectToWindow; w-- )
      {           
         if( windows.get( w ).isModal() )
         {
            // Make this window process the current key events
            redirectToWindow = true;
            windows.get( w ).processKeys();
         }
      }
      if( !redirectToWindow )
      {
         walkNpcs();
         
         if( this.jumping )
         {
            doJump();
         }
         else
         {
            performInputActions();
         }
         
         // Check hero's location for action tiles
         checkForActions();
      }
   }
   
   /**
    * Check the hero's tile for actions and perform them
    */
   private void checkForActions()
   {
      final Coordinate heroPos = this.hero.getTilePosition();
      
      final int numActions = ActionManager.getActionCount();
      for( int index = 0; index < numActions; index++ )
      {
         final MapAction action = ActionManager.getAction( index );
         if( action.getSource().equals( heroPos ) )
         {
            // Perform this action
            if( action instanceof TeleportAction )
            {
               final TeleportAction ta = (TeleportAction) action;
               final String newMapName = ta.getDestinationMapName();
               final Coordinate destination = ta.getDestination();
               
               // Load the map in a separate thread to not slow down 
               // the UI thread
               new MapLoadThread( newMapName, destination ).start();
            }
            else if( action instanceof ChangeLayerAction )
            {
               final ChangeLayerAction cla = (ChangeLayerAction) action;
               this.hero.setLayerIndex( cla.getLayerIndex() );
            }
         }
      }
   }
   
   /**
    * Update the game windows
    */
   private void updateWindows()
   {
      final ArrayList<GameWindow> windows = LayerManager.getWindowLayers();
      int numWindows = windows.size();
      GameWindow window = null;
      
      for( int windowIdx = 0; windowIdx < numWindows; windowIdx++ )
      {
         window = windows.get( windowIdx );
         if( !window.isVisible() )
         {
            windows.remove( windowIdx );
            numWindows--;
            windowIdx--;
         }
      }
   }
   
   /**
    * Make the NPCs walk
    */
   private void walkNpcs()
   {
      final ArrayList<Sprite> cachedSprites = LayerManager.getSpriteLayers();
      final int numSprites = cachedSprites.size();
      Sprite sprite = null;
      for( int spriteIdx = 0; spriteIdx < numSprites; spriteIdx++ )
      {
         sprite = cachedSprites.get( spriteIdx );
         if( sprite instanceof NPC )
         {
            ( (NPC) sprite ).walk();
         }
      }
   }
   
   /**
    * Checks which keys were active during the last frame and process them 
    * accordingly
    */
   private void performInputActions()
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      
      final int oldHeroXPosition = this.hero.xPixel;
      final int oldHeroYPosition = this.hero.yPixel;
      
      boolean moveUp = false;
      boolean moveDown = false;
      boolean moveLeft = false;
      boolean moveRight = false;
      
      // If we're walking along a path, check that now
      if( !this.heroPath.isEmpty() )
      {
         final int tileSize = ScreenSettings.tileSize;
         
         Coordinate nextTarget = this.heroPath.getFirst();
         int xTarget = ( nextTarget.x * tileSize ) + ( tileSize >> 1 ) + 1;
         int yTarget = ( nextTarget.y * tileSize ) + ( tileSize >> 1 ) + 1;
         
         if( Math.abs( xTarget - oldHeroXPosition ) < walkIncrement &&
                  Math.abs( yTarget - oldHeroYPosition ) < walkIncrement )
         {
            this.heroPath.removeFirst();
            
            if( !this.heroPath.isEmpty() )
            {
               nextTarget = this.heroPath.getFirst();
               xTarget = ( nextTarget.x * tileSize ) + ( tileSize >> 1 ) + 1;
               yTarget = ( nextTarget.y * tileSize ) + ( tileSize >> 1 ) + 1;
            }
         }
         
         moveUp = yTarget < oldHeroYPosition;
         moveDown = yTarget > oldHeroYPosition;
         moveLeft = xTarget < oldHeroXPosition;
         moveRight = xTarget > oldHeroXPosition;
      }
      
      // Cache values locally
      final boolean actionPressed = InputState.actionPressed;
      final boolean spacePressed = InputState.spacePressed;
      
      moveUp |= InputState.upPressed || ( Debug.useAccelerometer && InputState.sensorY < 3 );
      moveDown |= InputState.downPressed || ( Debug.useAccelerometer && InputState.sensorY > 5 );
      moveLeft |= InputState.leftPressed || ( Debug.useAccelerometer && InputState.sensorX > 2 );
      moveRight |= InputState.rightPressed || ( Debug.useAccelerometer && InputState.sensorX < -2 );

      if( actionPressed )
      {
         if( performContextAction() )
         {
            InputState.actionPressed = false;
         }
      }
      if( moveUp )
      {
         // declare intent to move up
         moveUp();
         // If we're running, move twice
         if( spacePressed )
         {
            moveUp();
         }
      }
      if( moveDown )
      {
         // declare intent to move down
         moveDown();
         // If we're running, move twice
         if( spacePressed )
         {
            moveDown();
         }
      }
      if( moveLeft )
      {
         // declare intent to move left
         moveLeft();
         // If we're running, move twice
         if( spacePressed )
         {
            moveLeft();
         }
      }
      if( moveRight )
      {
         // declare intent to move right
         moveRight();
         // If we're running, move twice
         if( spacePressed )
         {
            moveRight();
         }
      }
      
      if( moveLeft || moveRight || moveUp || moveDown )
      {
         this.hero.setDirection( Direction.fromPoints( moveLeft, moveRight, moveUp, moveDown ) );
         
         // Change his animation
         this.hero.animate();
      }
      else
      {
         // Set his animation frame to a stopped frame
         this.hero.rest();
         resetJumping();
      }
      
      final int newHeroXPosition = this.hero.xPixel;
      final int newHeroYPosition = this.hero.yPixel;
      if( oldHeroXPosition == newHeroXPosition && 
          oldHeroYPosition == newHeroYPosition )
      {
         // We didn't move
         this.hero.rest();
      }
   }
   
   /**
    * The action button has been pressed. Check what's happening and perform
    * the correct action.
    * @return true if an action was performed
    */
   private boolean performContextAction()
   {
      boolean actionPerformed = false;
      
      // Check to see if  we're facing an NPC
      final Direction dir = this.hero.getDirection();
      final Coordinate vec = dir.vector;
      
      final Coordinate heroPos = this.hero.getTilePosition();
      
      // Check that we can walk from the hero tile to the check tile
      final int tileSize = ScreenSettings.tileSize;

      final int candidateX = this.hero.xPixel + ( vec.x * tileSize );
      final int candidateY = this.hero.yPixel + ( vec.y * tileSize );
      
      boolean canMoveThere = true;
      
      if( LayerManager.canMove( this.hero.xPixel, this.hero.yPixel,
                                candidateX, candidateY,
                                this.hero, true, true ) != 1 )
      {
         canMoveThere = false;
      }
      
      final ArrayList<Sprite> sprites = LayerManager.getSpriteLayers();
      
      for( final Sprite sprite : sprites )
      {
         if( sprite instanceof NPC )
         {
            if( ( (NPC) sprite ).isOccupying( heroPos.x + vec.x, 
                                              heroPos.y + vec.y ) &&
                !( (NPC) sprite ).isWalking() && canMoveThere )
            {
               final Coordinate npcPos = sprite.getTilePosition();
               
               final double dx = heroPos.x - npcPos.x;
               final double dy = heroPos.y - npcPos.y;
               
               sprite.rest();
               
               if( Math.abs( dx ) > Math.abs( dy ) )
               {
                  if( dx < 0 )
                  {
                     // Make the NPC look left
                     sprite.setDirection( Direction.LEFT );
                  }
                  else
                  {
                     // Make the sprite look right
                     sprite.setDirection( Direction.RIGHT );
                  }
               }
               else
               {
                  if( dy < 0 )
                  {
                     // Make the sprite look up
                     sprite.setDirection( Direction.UP );
                  }
                  else
                  {
                     // Make the sprite look down
                     sprite.setDirection( Direction.DOWN );
                  }
               }
               // Speak to the NPC
               final String text = ( (NPC) sprite ).getSpeech( 0 );
               final int pad = 10;
               final int winHeight = 50 + pad;
               final int winWidth = ScreenSettings.width - 8 * pad;
               final int xStart = 4 * pad;
               final int yStart = ScreenSettings.height - winHeight;
               
               
               final TextWindow win = new TextWindow( xStart, yStart,
                                                winWidth, winHeight,
                                                text, true );
               
               LayerManager.pushWindow( win );
               
               actionPerformed = true;
            }
         }
      }
      return actionPerformed;
   }
   
   /**
    * Moves the hero character's intended position left
    */
   private void moveLeft()
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      
      setNewPosition( this.hero.xPixel - walkIncrement, this.hero.yPixel, LEFT );
      
      checkJump( LEFT );
      checkShove( LEFT );
   }

   /**
    * Moves the hero character's intended position right
    */
   private void moveRight()
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      
      setNewPosition( this.hero.xPixel + walkIncrement, this.hero.yPixel, RIGHT );
      
      checkJump( RIGHT );
      checkShove( RIGHT );
   }

   /**
    * Moves the hero characters intended position up
    */
   private void moveUp()
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      
      setNewPosition( this.hero.xPixel, this.hero.yPixel - walkIncrement, UP );
      
      checkJump( UP );
      checkShove( UP );
   }

   /**
    * Moves the hero characters intended position down
    */
   private void moveDown()
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      
      setNewPosition( this.hero.xPixel, this.hero.yPixel + walkIncrement, DOWN );
      
      checkJump( DOWN );
      checkShove( DOWN );
   }
   
   /**
    * Checks whether a pixel location is free. If so we update the character's
    * position to that coordinate.
    * 
    * @param checkX the x pixel coordinate to check
    * @param checkY the y pixel coordinate to check
    * @param direction the direction we're moving in
    * @return true if the character's position was set to the check position
    *         or if it was nudged into the closest matching position.
    *         false if the position cannot be moved into.
    */
   private boolean setNewPosition( final int checkX,
                                   final int checkY,
                                   final int direction )
   {
      return checkAndSetNewPosition( checkX, checkY, direction, true );
   }
   
   /**
    * Checks if the position we intend to move into is clear.
    * If it is mostly clear, we nudge the character
    * into a completely clear position in the general direction he was moving.
    * <p>
    * If the canSet flag is true we update the player's position. If it is false
    * we simply return true or false depending on whether the location is free.
    * <p>
    * We achieve this by testing 5 points on and around the character's current
    * position. The five points are positioned as follows:
    * <pre>
    *             2
    *            o
    *            |
    *       1    |0      3
    *      o-----o-----o
    *            |
    *            |4
    *            o
    * 
    * </pre>
    * <p>
    * The points are separated by a predefined distance that represents the
    * extent of the character.
    * 
    * @param checkX the x pixel coordinate to check
    * @param checkY the y pixel coordinate to check
    * @param direction the direction we're moving in
    * @param canSet true if we should also set the position if it is free
    * 
    * @return true if the tile at that pixel position can be walked on
    */
   private boolean checkAndSetNewPosition( final int checkX,
                                           final int checkY,
                                           final int direction,
                                           final boolean canSet )
   {
      final int posX = this.hero.xPixel;
      final int posY = this.hero.yPixel;
      
      if( Debug.clipOff )
      {
         this.hero.setPixelPosition( checkX, checkY );
         return true;
      }
      
      // The X values of the 5 points to check (where the hero wants to be)
      final int[] newXs = 
      {
         checkX, checkX - EXTENT, checkX, checkX + EXTENT, checkX    
      };
      // The Y values of the 5 points to check (where the hero wants to be)
      final int[] newYs = 
      {
         checkY, checkY, checkY - EXTENT, checkY, checkY + EXTENT
      };
      // The X values of the current 5 points (where the hero is now)
      final int[] oldXs = 
      {
         posX, posX - EXTENT, posX, posX + EXTENT, posX
      };
      // The Y values of the current 5 points (where the hero is now)
      final int[] oldYs = 
      {
         posY, posY, posY - EXTENT, posY, posY + EXTENT
      };
      final boolean[] canMove = { false, false, false, false, false };
      final int[] moves = new int[5];
      
      for( int i = 0; i < 5; i++ )
      {
         moves[ i ] = LayerManager.canHeroMove( oldXs[ i ], oldYs[ i ], newXs[ i ], newYs[ i ] );
         canMove[i] = moves[i] == 1;
      }
      final boolean[] points = { true, true, true, true, true };
      
      points[LEFT] = LayerManager.canHeroMove( newXs[CENTRE], newYs[CENTRE],
                                               newXs[LEFT], newYs[LEFT]) != 0;
      
      points[RIGHT] = LayerManager.canHeroMove( newXs[CENTRE], newYs[CENTRE],
                                                newXs[RIGHT], newYs[RIGHT] ) != 0;
      
      points[UP] = LayerManager.canHeroMove( newXs[CENTRE], newYs[CENTRE],
                                             newXs[UP], newYs[UP]) != 0;
      
      points[DOWN] = LayerManager.canHeroMove( newXs[CENTRE], newYs[CENTRE],
                                               newXs[DOWN], newYs[DOWN]) != 0;
   
      int nudgeX = 0;
      int nudgeY = 0;
      boolean moved = false;
      
      if( !points[LEFT] & points[UP] & points[RIGHT] & points[DOWN] )
      {
         // Nudge right
         nudgeX = 2;
         moved = true;
      }
      else if( points[LEFT] & points[UP] & !points[RIGHT] & points[DOWN] )
      {
         // Nudge left
         nudgeX = -2;
         moved = true;
      }
      else if( points[LEFT] & !points[UP] & points[RIGHT] & points[DOWN] )
      {
         // Nudge down
         nudgeY = 2;
         moved = true;
      }
      else if( points[LEFT] & points[UP] & points[RIGHT] & !points[DOWN] )
      {
         // Nudge up
         nudgeY = -2;
         moved = true;
      }
      
      // The easy case. All five test points are clear! the character can move.
      if( !moved )
      {
         moved = canMove[CENTRE] & canMove[LEFT] & canMove[UP] & canMove[RIGHT] & canMove[DOWN];
      }
      
      // If some of the test points are not clear then the character gets nudged
      // around (e.g. if he bumps into diagonal tiles or if he is hindered on
      // only one side. The next chunk of code calculates all the different
      // types of nudges for all possible walk directions.
      //
      // It looks like a long complicated section but the logic is really only
      // a quarter of what's below as it was duplicated many times for the
      // slightly different conditions for walking in different directions.
      if( !moved )
      {
         if( ( canMove[CENTRE] & canMove[UP] & canMove[RIGHT] & canMove[DOWN] ) )
         {
            // All four directions are free except the left
            if( direction == UP || direction == DOWN )
            {
               // Nudge right
               nudgeX = 2;
               moved = true;
            }
            else if( direction == LEFT )
            {
               // If we're moving in the direction that is blocked
               if( ( moves[LEFT] & TileDescription.TL_BR_DIAG ) != 0 )
               {
                  // Nudge up
                  nudgeY = -2;
                  moved = true;
               }
               else if( ( moves[LEFT] & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  // Nudge down
                  nudgeY = 2;
                  moved = true;
               }
            }
         }
         else if( canMove[CENTRE] & canMove[LEFT] & canMove[UP] & canMove[DOWN] )
         {
            // All four directions are free except the right
            if( direction == UP || direction == DOWN )
            {
               // Nudge left
               nudgeX = -2;
               moved = true;
            }
            else if( direction == RIGHT )
            {
               // If we're moving in the direction that is blocked
               if( ( moves[RIGHT] & TileDescription.TL_BR_DIAG ) != 0 )
               {
                  // Nudge down
                  nudgeY = 2;
                  moved = true;
               }
               else if( ( moves[RIGHT] & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  // Nudge up
                  nudgeY = -2;
                  moved = true;
               }
            }
         }
         else if( canMove[CENTRE] & canMove[LEFT] & canMove[RIGHT] & canMove[DOWN] )
         {
            // All four directions are free except the top
            if( direction == LEFT || direction == RIGHT )
            {
               // Nudge down
               nudgeY = 2;
               moved = true;
            }
            else if( direction == UP )
            {
               // If we're moving in the direction that is blocked
               if( ( moves[UP] & TileDescription.TL_BR_DIAG ) != 0 )
               {
                  // Nudge left
                  nudgeX = -2;
                  moved = true;
               }
               else if( ( moves[UP] & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  // Nudge right
                  nudgeX = 2;
                  moved = true;
               }
            }
         }
         else if( canMove[CENTRE] & canMove[LEFT] & canMove[UP] & canMove[RIGHT] )
         {
            // All four directions are free except the bottom
            if( direction == LEFT || direction == RIGHT )
            {
               // Nudge up
               nudgeY = -2;
               moved = true;
            }
            else if( direction == DOWN )
            {
               // If we're moving in the direction that is blocked
               if( ( moves[DOWN] & TileDescription.TL_BR_DIAG ) != 0 )
               {
                  // Nudge right
                  nudgeX = 2;
                  moved = true;
               }
               else if( ( moves[DOWN] & TileDescription.TR_BL_DIAG ) != 0 )
               {
                  // Nudge left
                  nudgeX = -2;
                  moved = true;
               }
            }
         }
         
         // See if we can nudge the guy along a diagonal
         else if( ( canMove[CENTRE] & canMove[RIGHT] & canMove[DOWN] ) && 
                  ( moves[LEFT] & TileDescription.TR_BL_DIAG ) != 0 &&
                  ( moves[UP] & TileDescription.TR_BL_DIAG ) != 0 )
         {
            if( direction == UP )
            {
               // Nudge right
               nudgeX = 2;
               moved = true;
            }
            else if( direction == LEFT )
            {
               // Nudge down
               nudgeY = 2;
               moved = true;
            }
         }
         else if( ( canMove[CENTRE] & canMove[LEFT] & canMove[DOWN] ) && 
                  ( moves[UP] & TileDescription.TL_BR_DIAG ) != 0 &&
                  ( moves[RIGHT] & TileDescription.TL_BR_DIAG ) != 0 )
         {
            if( direction == UP )
            {
               // Nudge left
               nudgeX = -2;
               moved = true;
            }
            else if( direction == RIGHT )
            {
               // Nudge down
               nudgeY = 2;
               moved = true;
            }
         }
         else if( ( canMove[CENTRE] & canMove[LEFT] & canMove[UP] ) && 
                  ( moves[RIGHT] & TileDescription.TR_BL_DIAG ) != 0 &&
                  ( moves[DOWN] & TileDescription.TR_BL_DIAG ) != 0 )
         {
            if( direction == DOWN )
            {
               // Nudge left
               nudgeX = -2;
               moved = true;
            }
            else if( direction == RIGHT )
            {
               // Nudge up
               nudgeY = -2;
               moved = true;
            }
         }
         else if( ( canMove[CENTRE] & canMove[UP] & canMove[RIGHT] ) && 
               ( moves[LEFT] & TileDescription.TL_BR_DIAG ) != 0 &&
               ( moves[DOWN] & TileDescription.TL_BR_DIAG ) != 0 )
         {
            if( direction == DOWN )
            {
               // Nudge right
               nudgeX = 2;
               moved = true;
            }
            else if( direction == LEFT )
            {
               // Nudge up
               nudgeY = -2;
               moved = true;
            }
         }
      }
            
      if( moved )
      {
         // Check to see if we were nudged
         if( nudgeX != 0 || nudgeY != 0 )
         {
            if( canSet )
            {
               // We can update the player position. Check to see if the
               // position we were nudged into is free before we set.
               final boolean can = checkAndSetNewPosition( checkX + nudgeX,
                                                     checkY + nudgeY,
                                                     direction,
                                                     false );
               if( can )
               {
                  this.hero.setPixelPosition( checkX + nudgeX, checkY + nudgeY );
               }
            }
            else
            {
               // We can't set the location. Check to see how many sides are
               // free. If all but one are free, we return true. The reason not
               // all sides have to be free is that one may be currently blocked
               // if we are in the process of being nudged.
               int sum = 0;
               for( int count = 0; count < canMove.length; count++ )
               {
                  sum += canMove[count] ? 1 : 0;
               }
               if( sum < 4 )
               {
                  moved = false;
               }
            }
         }
         else
         {
            if( canSet )
            {
               this.hero.setPixelPosition( checkX, checkY );
            }
         }
      }
      return moved;
   }
   
   /**
    * Check whether the character can jump in the specified direction from
    * where he is standing. If the number of jump attempts increases to the
    * number required, we start jumping
    * 
    * @param direction
    */
   private void checkJump( final int direction )
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      
      int oldX = this.hero.xPixel;
      int oldY = this.hero.yPixel;
      int newX = oldX;
      int newY = oldY;
      switch( direction )
      {
      case LEFT:
         oldX -= EXTENT;
         newX = oldX - walkIncrement;
         break;
      
      case RIGHT:
         oldX += EXTENT;
         newX = oldX + walkIncrement;
         break;
      
      case UP:
         oldY -= EXTENT;
         newY = oldY - walkIncrement;
         break;
      
      case DOWN:
         oldY += EXTENT;
         newY = oldY + walkIncrement;
         break;
       
      default:
            return;
      }
      if( LayerManager.canJump( oldX, oldY, newX, newY ) )
      {
         if( this.lastJumpTryDirection != direction )
         {
            this.jumpCounter = 0;
            this.lastJumpTryDirection = direction;
         }
         if( ++this.jumpCounter == JUMP_TRIES )
         {
            resetJumping();
            this.jumping = true;
            this.jumpInfo = new JumpInfo( this.hero.xPixel, this.hero.yPixel, direction );
         }
      }
      else
      {
         resetJumping();
      }
   }
   
   
   /**
    * If we are jumping update the character's location. If we've landed
    * we set the jumping flag to false
    */
   private void doJump()
   {
      if( this.jumpInfo.getIteration() == 0 )
      {
         // If we've just started jumping, set the animation to a moving frame
         this.hero.setFrame( 1 );
      }
      else if( this.jumpInfo.getIteration() == JumpInfo.INCREMENT_COUNT / 2 )
      {
         // If we've reached the top of the jump, set the animation to the 
         // opposite moving frame
         this.hero.setFrame( 3 );
      }
      
      final Coordinate newPos = this.jumpInfo.getNextPosition();
      this.hero.setPixelPosition( newPos.x, newPos.y );
      
      if( this.jumpInfo.getIteration() == JumpInfo.INCREMENT_COUNT )
      {
         // We've landed
         this.jumping = false;
         this.jumpInfo = null;
      }
   }
   
   /**
    * Reset the jump counter and the current jump direction
    */
   private void resetJumping()
   {
      this.jumpCounter = 0;
      this.lastJumpTryDirection = -1;
   }
   
   /**
    * Check to see if there is an NPC in the specified direction that can be
    * shoved
    * @param direction
    */
   private void checkShove( final int direction )
   {
      final int walkIncrement =
               DisplayResolutions.getConvertedEvenInt( ScreenSettings.densityDpi, WALK_INCREMENT );
      int oldX = this.hero.xPixel;
      int oldY = this.hero.yPixel;
      int newX = oldX;
      int newY = oldY;
      switch( direction )
      {
      case LEFT:
         oldX -= EXTENT;
         newX = oldX - walkIncrement;
         break;
      
      case RIGHT:
         oldX += EXTENT;
         newX = oldX + walkIncrement;
         break;
      
      case UP:
         oldY -= EXTENT;
         newY = oldY - walkIncrement;
         break;
      
      case DOWN:
         oldY += EXTENT;
         newY = oldY + walkIncrement;
         break;
       
      default:
            return;
      }
    
      if( LayerManager.canMove( oldX, oldY, newX, newY, this.hero, true, true ) == 1 )
      {
         final Sprite found = LayerManager.getSpriteAtPixel( newX, newY );
         if( found != null && found instanceof NPC )
         {
            ( (NPC) found ).shove();
         }
      }
      
   }
   
   private void checkScreenInteraction()
   {
      try
      {
         if( InputState.touchUp[ 0 ] != null && InputState.touchUp[ 1 ] != null )
         {
            // Just released a double-tap
         }
         else if( InputState.touchUp[ 0 ] != null )
         {
            // Release the first finger touch.
            // this.hero.xPixel, this.hero.yPixel,
            final int screenLeft = this.hero.xPixel - ScreenSettings.xCentre;
            final int screenTop = this.hero.yPixel - ScreenSettings.yCentre;
            
            final int touchX = (int)( InputState.touchUp[ 0 ].x ) + screenLeft;
            final int touchY = (int)( InputState.touchUp[ 0 ].y ) + screenTop;
            
            PathFinder.findPath( this.hero.xPixel, this.hero.yPixel, touchX, touchY, this.heroPath );
         }
      }
      finally
      {
         InputState.markTouchUpEventsRead();
      }
   }
   
   /**
    * Paints the current frame.
    *
    * @param canvas 
    */
   private void drawFrame( final Canvas canvas )
   {
      if( canvas == null )
      {
         return;
      }
      
      // Only draw ourselves if the text view is not visible
      if( this.statusTextView.getVisibility() != View.VISIBLE )
      {
         final ArrayList<Layer> allLayers = LayerManager.getAllLayersInOrder();
         
         // If we're in wireframe mode, clear the screen
         if( Debug.wireframe )
         {
            canvas.drawRect( 0, 0, 
                             ScreenSettings.width, 
                             ScreenSettings.height, 
                             this.blackPaint );
         }
         final int numLayers = allLayers.size();
         // Delegate drawing to each of the layers
         
         boolean below = true;
         for( int layerIdx = 0; layerIdx < numLayers; layerIdx++ )
         {
            final Layer layer = allLayers.get( layerIdx );
            layer.draw( this.hero.xPixel, this.hero.yPixel, below, canvas, this.paint );
            if( layer == this.hero )
            {
               below = false;
            }
         }
         
         // Set the overlay for the current time of day
         if( Debug.useDayNight )
         {
            DayNight.tick();
            canvas.drawARGB( DayNight.getTimeAlpha(),
                             DayNight.RED,
                             DayNight.GREEN,
                             DayNight.BLUE );
         }
         
         // We draw all open windows again so that the text isn't shifted in colour due to the 
         // day/night cycle.
         for( final Layer layer : LayerManager.getWindowLayers() )
         {
            layer.draw( this.hero.xPixel, this.hero.yPixel, false, canvas, this.paint );
         }
      }
   }
   
   /**
    * Checks how long the delay has been since the last frame was rendered
    * and pauses for an appropriate length of time.
    */
   private void checkTiming()
   {
      this.counter ++;
      final long FRAME_CAP = 40 * NS_IN_MS; // nanoseconds between frames = 40 ms
      
      final long now = System.nanoTime();
      // Calculate how long the last frame took
      final long frameRenderDuration = now - this.oneFrameTime;
      final long delayNeeded = FRAME_CAP - frameRenderDuration;
      if( delayNeeded > ( 0 * NS_IN_MS ) )
      {
         // We don't bother sleeping if the difference between the target frame duration and the
         // actual frame duration is 5ms or less as the context switch for the thread will likely
         // take longer than that.
         try
         {
            Thread.sleep( delayNeeded / NS_IN_MS, (int)( delayNeeded % NS_IN_MS ) );
         }
         catch( final InterruptedException exception )
         {
            // ignore and keep going.
         }
      }
      this.oneFrameTime = now;
   }
   
   @SuppressLint( "DefaultLocale" )
   private void performDebugUpdate()
   {
      // FPS display.
      if( Debug.showFps )
      {
         final long now = this.oneFrameTime;
         if( now - this.lastFpsWindowUpdate > ( 1000 * NS_IN_MS ) )
         {
            this.fps = (float) ( this.counter - this.lastCounter ) / 
                  (float) ( now - this.lastFpsWindowUpdate );
            this.fps = Math.round( this.fps * ( 10000 * NS_IN_MS ) );
            this.fps = this.fps / 10;
            
            this.lastCounter = this.counter;
            this.lastFpsWindowUpdate = now;
         }
         
         final String message = "update: " + this.counter + //$NON-NLS-1$
                                "  fps:" + this.fps;        //$NON-NLS-1$
         
         
         if( this.fpsWindow == null )
         {
            this.fpsWindow = new TextWindow( 10, 10, 140, 25, "" ); //$NON-NLS-1$
         }
         this.fpsWindow.setText( message );
         if( !LayerManager.getWindowLayers().contains( this.fpsWindow ) )
         {
            LayerManager.pushWindow( this.fpsWindow );
            this.fpsWindow.setVisible( true );
         }
      }
      else
      {
         if( this.fpsWindow != null )
         {
            this.fpsWindow.setVisible( false );
         }
      }
      
      // Accelerometer information.
      if( Debug.showAccel )
      {
         final String message =
                  String.format( "X: %.3f  Y: %.3f", InputState.sensorX, InputState.sensorY );
         
         if( this.accelWindow == null )
         {
            this.accelWindow = new TextWindow( 600, 10, 150, 25, "" );
         }
         this.accelWindow.setText( message );
         if( !LayerManager.getWindowLayers().contains( this.accelWindow ) )
         {
            LayerManager.pushWindow( this.accelWindow );
            this.accelWindow.setVisible( true );
         }
      }
      else
      {
         if( this.accelWindow != null )
         {
            this.accelWindow.setVisible( false );
         }
      }
      
      // Multi-touch debugging.
      if( Debug.showTouch )
      {
         int index = 1;
         final StringBuilder builder = new StringBuilder();
         for( final PointerCoords coord : InputState.touchDown )
         {
            builder.append( index++ +"_");
            if( coord != null )
            {
               builder.append( String.format( "down_%.2f,_%.2f ", coord.x, coord.y ) );
            }
            else
            {
               builder.append( "_____________________ " );
            }
         }
         index = 1;
         for( final PointerCoords coord: InputState.touchUp )
         {
            builder.append( index++ +"_");
            if( coord != null )
            {
               builder.append( String.format( "up_%.2f,_%.2f ", coord.x, coord.y ) );
            }
            else
            {
               builder.append( "_____________________ " );
            }
         }
         if( this.touchWindow == null )
         {
            this.touchWindow  = new TextWindow( 600, 40, 150, 100, "" );
         }
         this.touchWindow.setText( builder.toString() );
         if( !LayerManager.getWindowLayers().contains( this.touchWindow ) )
         {
            LayerManager.pushWindow( this.touchWindow );
            this.touchWindow.setVisible( true );
         }
      }
      else
      {
         if( this.touchWindow != null )
         {
            this.touchWindow.setVisible( false );
         }
      }
      
      // Path-finding
      if( Debug.showPath )
      {
         if( !LayerManager.getOverlayLayers().contains( this.pathLayer ) )
         {
            LayerManager.addOverlay( this.pathLayer );
         }
      }
      this.pathLayer.setVisible( Debug.showPath );
      
      // Whether or not to hide the main sprite layer.
      this.hero.setVisible( !Debug.hideHero );
   }
 }