package uk.co.eduardo.abaddon;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import uk.co.eduardo.abaddon.state.GameState;
import uk.co.eduardo.abaddon.state.StateMachine;
import uk.co.eduardo.abaddon.util.Debug;
import uk.co.eduardo.abaddon.util.DisplayResolutions;
import uk.co.eduardo.abaddon.util.Res;
import uk.co.eduardo.abaddon.util.ScreenSettings;

/**
 * The main game view. This is a {@link SurfaceView}.
 * 
 * @author Ed
 */
class GameView extends SurfaceView implements SurfaceHolder.Callback 
{
   //================|  Fields             |====================================
   
   /** The main game controller. */
   private final Controller controller;

   
   //================|  Constructors       |====================================
   
   /**
    * @param context global information about the application environment.
    * @param attrs attributes from parsing the game view XML.
    */
   public GameView( final Context context, final AttributeSet attrs )
   {
      super( context, attrs );

      // register our interest in hearing about changes to our surface
      final SurfaceHolder holder = getHolder();
      holder.addCallback( this );
      
      // Attempt to set acceleration
      try
      {
         holder.setType( SurfaceHolder.SURFACE_TYPE_GPU );
      }
      catch( final Throwable t )
      {
         // Try a hardware accelerated surface
         try
         {
            holder.setType( SurfaceHolder.SURFACE_TYPE_HARDWARE );
         }
         catch( final Throwable t2 )
         {
            // last chance. Use a normal surface
            holder.setType( SurfaceHolder.SURFACE_TYPE_NORMAL );
         }
      }
      
      // Initialize resources
      Res.resources = context.getResources();

      // create thread only; it's started in surfaceCreated()
      this.controller = new Controller( holder );

      final TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.MainView );

      final int tileSize = a.getInt( R.styleable.MainView_tileSize, 16 );
      final int spriteHeight = a.getInt( R.styleable.MainView_spriteHeight, 24 );
      a.recycle();
      
      final int dpi = ScreenSettings.densityDpi;
      ScreenSettings.tileSize = DisplayResolutions.getConvertedEvenInt( dpi, tileSize );
      ScreenSettings.spriteHeight = DisplayResolutions.getConvertedEvenInt( dpi, spriteHeight );

      setFocusable( true ); // make sure we get key events
   }
   
   
   //================|  Public Methods     |====================================
   
   /**
    * @return the game controller
    */
   public Controller getController()
   {
      return this.controller;
   }

   /**
    * Handles key events in the main loop. Changes game mode on certain key
    * presses in some modes.
    * 
    * {@inheritDoc}
    */
   @Override
   public boolean onKeyDown( final int keyCode, final KeyEvent msg )
   {
      if( keyCode == KeyEvent.KEYCODE_DPAD_UP )
      {
         InputState.upPressed = true;
         return true;
      }

      if( keyCode == KeyEvent.KEYCODE_DPAD_DOWN )
      {
         InputState.downPressed = true;
         return true;
      }

      if( keyCode == KeyEvent.KEYCODE_DPAD_LEFT )
      {
         InputState.leftPressed = true;
         return true;
      }

      if( keyCode == KeyEvent.KEYCODE_DPAD_RIGHT )
      {
         InputState.rightPressed = true;
         return true;
      }

      if( keyCode == KeyEvent.KEYCODE_SPACE )
      {
         InputState.spacePressed = true;
         return true;
      }

      if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER || 
          keyCode == KeyEvent.KEYCODE_ENTER || 
          keyCode == KeyEvent.KEYCODE_SOFT_LEFT )
      {
         final GameState state = StateMachine.getInstance().getState();
         if( state == GameState.READY | state == GameState.LOSE )
         {
            // At the beginning of the game, or the end of a previous one,
            // we should start a new game.
            StateMachine.getInstance().setState( GameState.START_NEW );
            return true;
         }

         if( state == GameState.PAUSE )
         {
            // If the game is merely paused, we should just continue where we
            // left off.
            StateMachine.getInstance().setState( GameState.RUNNING );
            return true;
         }

         if( state == GameState.ERROR )
         {
            // Exit!
            System.exit( -1 );
         }

         InputState.actionPressed = true;
      }
      // Debugging
      if( keyCode == KeyEvent.KEYCODE_C )
         Debug.clipOff = !Debug.clipOff;

      if( keyCode == KeyEvent.KEYCODE_W )
         Debug.wireframe = !Debug.wireframe;

      if( keyCode == KeyEvent.KEYCODE_D )
         Debug.useDayNight = !Debug.useDayNight;

      if( keyCode == KeyEvent.KEYCODE_F )
         Debug.showFps = !Debug.showFps;

      if( keyCode == KeyEvent.KEYCODE_H )
         Debug.hideHero = !Debug.hideHero;

      if( keyCode == KeyEvent.KEYCODE_T )
         Debug.showHeroTile = !Debug.showHeroTile;

      return super.onKeyDown( keyCode, msg );
   }

   /**
    * Handles key events in the main loop. Changes game mode on certain key
    * presses in some modes.
    * 
    * {@inheritDoc}
    */
   @Override
   public boolean onKeyUp( final int keyCode, final KeyEvent msg )
   {
      if( keyCode == KeyEvent.KEYCODE_DPAD_UP )
      {
         InputState.upPressed = false;
         return true;
      }
      if( keyCode == KeyEvent.KEYCODE_DPAD_DOWN )
      {
         InputState.downPressed = false;
         return true;
      }
      if( keyCode == KeyEvent.KEYCODE_DPAD_LEFT )
      {
         InputState.leftPressed = false;
         return true;
      }
      if( keyCode == KeyEvent.KEYCODE_DPAD_RIGHT )
      {
         InputState.rightPressed = false;
         return true;
      }
      if( keyCode == KeyEvent.KEYCODE_SPACE )
      {
         InputState.spacePressed = false;
         return true;
      }
      if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER || 
          keyCode == KeyEvent.KEYCODE_ENTER || 
          keyCode == KeyEvent.KEYCODE_SOFT_LEFT )
      {
         InputState.actionPressed = false;
         return true;
      }

      return super.onKeyUp( keyCode, msg );
   }

   /**
    * Standard window-focus override. Notice focus lost so we can pause 
    * on focus lost. e.g. user switches to take a call.
    */
   @Override
   public void onWindowFocusChanged( final boolean hasWindowFocus )
   {
      if( !hasWindowFocus )
      {
         // thread.pause();
      }
   }

   @Override
   public void surfaceChanged( final SurfaceHolder holder, 
                               final int format, 
                               final int width,
                               final int height )
   {
      ScreenSettings.setWidth( width );
      ScreenSettings.setHeight( height );
   }

   @Override
   public void surfaceCreated( final SurfaceHolder holder )
   {
      // start the thread here so that we don't busy-wait in run()
      // waiting for the surface to be created
      this.controller.setRunning( true );
      this.controller.start();
   }

   /**
    * WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     * 
    * {@inheritDoc}
    */
   @Override
   public void surfaceDestroyed( final SurfaceHolder holder )
   {
      // we have to tell thread to shut down & wait for it to finish, or else
      // it might touch the Surface after we return and explode
      boolean retry = true;
      this.controller.setRunning( false );
      while( retry )
      {
         try
         {
            this.controller.join();
            retry = false;
         }
         catch( final InterruptedException e )
         {
            // try again.
         }
      }
   }
}
