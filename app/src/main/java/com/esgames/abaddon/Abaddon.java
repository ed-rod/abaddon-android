/*
 * Copyright (C) 2009 esgames.
 */

package com.esgames.abaddon;

import android.app.Activity;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.esgames.abaddon.state.GameState;
import com.esgames.abaddon.state.StateMachine;
import com.esgames.abaddon.state.StateMachineListener;
import com.esgames.abaddon.util.Res;
import com.esgames.abaddon.util.ScreenSettings;

/**
 * Abaddon.
 */
public class Abaddon extends Activity implements StateMachineListener, SensorEventListener, View.OnTouchListener
{
   //================|  Fields             |====================================
   
   /** A handle to the thread that's actually running the animation. */
   private Controller controller;

   /** A handle to the View in which the game is running. */
   private GameView gameView;
   
   /** Text view. */
   private TextView statusTextView;
   
   /** The window manager **/
   private WindowManager windowManager;
   
   /** Sensor manager.  */
   private SensorManager sensorManager;

   
   //================|  Public Methods     |====================================
   
   /**
    * Called when {@link Activity} is first created. Turns off the title bar,
    * sets up the content views, and fires up the <code>GameView</code>.
    */
   @Override
   protected void onCreate( final Bundle savedInstanceState )
   {
      super.onCreate( savedInstanceState );

      // turn off the window's title bar
      requestWindowFeature( Window.FEATURE_NO_TITLE );

      final DisplayMetrics metrics = new DisplayMetrics();
      getWindowManager().getDefaultDisplay().getMetrics( metrics );
      ScreenSettings.densityDpi = metrics.densityDpi;
      
      // tell system to use the layout defined in our XML file
      setContentView( R.layout.game_layout );

      // get handles to the GameView and TextView from XML inflation
      this.gameView = (GameView) findViewById( R.id.abaddon);
      this.statusTextView = (TextView) findViewById( R.id.text );

      this.controller = this.gameView.getController();
      
      // Pass the TextView to the game controller
      this.controller.setTextView( this.statusTextView );
      
      // Set up the sensor listener
      this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
      this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
      if( this.sensorManager.getSensorList( Sensor.TYPE_ACCELEROMETER ).size() != 0 )
      {
         final Sensor s = this.sensorManager.getSensorList( Sensor.TYPE_ACCELEROMETER ).get( 0 );
         this.sensorManager.registerListener( this, s, SensorManager.SENSOR_DELAY_NORMAL );
      }
      
      // Set up a touch listener
      this.gameView.setOnTouchListener( this );

      // Set up a listener so we get notified when the internal game state
      // changes.
      StateMachine.getInstance().addListener( this );
      
      // Check to see if we're restoring or starting a new instance.
      if( savedInstanceState == null )
      {
         // we were just launched
         StateMachine.getInstance().setState( GameState.START_NEW );
      }
      else
      {
         // we are being restored: resume a previous game
//         controller.restoreState( savedInstanceState );
      }
   }

   /**
    * Notification that something is about to happen, to give the 
    * Activity a chance to save state.
    * 
    * @param outState a Bundle into which this Activity should save its state
    */
   @Override
   protected void onSaveInstanceState( final Bundle outState )
   {
      // just have the View's thread save its state into our Bundle
      super.onSaveInstanceState( outState );
      this.controller.saveState( outState );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stateChanged( final GameState oldState, final GameState newState )
   {
      if( newState == GameState.RUNNING & oldState != GameState.RUNNING )
      {
         // Map has finished loading. Hide the text view.
         this.statusTextView.setVisibility( View.INVISIBLE );
         return;
      }
      
      // All the following states show the text view and do not display the
      // game view

      final Resources res = Res.resources;
      CharSequence str = ""; //$NON-NLS-1$
      if( newState == GameState.START_NEW )
      {
         this.controller.handleStartNewGame();
      }
      if( newState == GameState.LOADING )
      {
         str = res.getText( R.string.mode_loading );
      }
      if( newState == GameState.PAUSE )
      {
         str = res.getText( R.string.mode_pause );
      }
      if( newState == GameState.READY )
      {
         str = res.getText( R.string.mode_ready );
      }
      if( newState == GameState.LOSE )
      {
         str = res.getText( R.string.mode_lose );
      }
      if( newState == GameState.ERROR )
      {
         str = res.getText( R.string.mode_error );
      }

      this.statusTextView.setText( str );
      
      // Show text view
      this.statusTextView.setVisibility( View.VISIBLE );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onAccuracyChanged(final Sensor sensor, final int accuracy)
   {
      // Do nothing.
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onSensorChanged(final SensorEvent event)
   {
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
      {
         switch (this.windowManager.getDefaultDisplay().getRotation())
         {
            case Surface.ROTATION_0:
               InputState.sensorX = event.values[0];
               InputState.sensorY = event.values[1];
               break;
               
            case Surface.ROTATION_90:
               InputState.sensorX = -event.values[1];
               InputState.sensorY = event.values[0];
               break;
               
            case Surface.ROTATION_180:
               InputState.sensorX = -event.values[0];
               InputState.sensorY = -event.values[1];
               break;
               
            case Surface.ROTATION_270:
               InputState.sensorX = event.values[1];
               InputState.sensorY = -event.values[0];
               break;
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean onTouch( final View view, final MotionEvent event )
   {
      // Update the input to match what is currently happening. For now, we're only processing
      // touch events when they're up.
      final int action = event.getAction() & MotionEvent.ACTION_MASK;
      
      switch( action )
      {
         case MotionEvent.ACTION_DOWN:
         {
            final PointerCoords coord = new PointerCoords();
            InputState.touchDown[ 0 ] = coord;
            event.getPointerCoords( 0, coord );
            break;
         }
         case MotionEvent.ACTION_MOVE:
         {
            for( int index = 0; index < event.getPointerCount() && index < InputState.maxTouch; index++ )
            {
               final int id = event.getPointerId( index );
               final PointerCoords coord = new PointerCoords();
               InputState.touchDown[ id ] = coord;
               event.getPointerCoords( index, coord );
            }
            break;
         }
         case MotionEvent.ACTION_POINTER_DOWN:
         {
            final int index =
                     ( event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int id = event.getPointerId( index );
            
            final PointerCoords coord = new PointerCoords();
            if( id < InputState.maxTouch )
            {
               InputState.touchDown[ id ] = coord;
            }
            event.getPointerCoords( index, coord );
            break;
         }
         case MotionEvent.ACTION_POINTER_UP:
         {
            final int index =
                     ( event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int id = event.getPointerId( index );
            
            final PointerCoords coord = new PointerCoords();
            if( id < InputState.maxTouch )
            {
               InputState.touchDown[ id ] = null;
               InputState.touchUp[ id ] = coord;
            }
            event.getPointerCoords( index, coord );
            break;
         }
         case MotionEvent.ACTION_UP:
         {
            final int index =
                     ( event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int id = event.getPointerId( index );

            final PointerCoords coord = new PointerCoords();
            if( id < InputState.maxTouch )
            {
               InputState.touchDown[ id ] = null;
               InputState.touchUp[ id ] = coord;
            }
            event.getPointerCoords( index, coord );
            break;
         }
         default:
         {
            // nothing
         }
      }
      return true;
   }
}
