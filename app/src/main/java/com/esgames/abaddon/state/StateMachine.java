/*
 * Copyright © 2009 esgames
 * All Rights Reserved.
 */
package com.esgames.abaddon.state;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * State machine for the game.
 * 
 * @author Eduardo Rodrigues
 */
public class StateMachine
{
   //================|  Fields             |====================================
   
   /** Singleton instance. */
   private static StateMachine instance;
   
   /** Listener list. */
   private final CopyOnWriteArrayList< StateMachineListener > listenerList = 
      new CopyOnWriteArrayList< StateMachineListener >();
   
   /** The current state. */
   private GameState currentState = GameState.READY;
   
   
   //================|  Constructors       |====================================
   
   /**
    * Constructs a StateMachine object.
    */
   private StateMachine()
   {
      // private constructor maintains singleton nature
   }
   
   
   //================|  Public Methods     |====================================
   
   /**
    * @return the singleton instance.
    */
   public static StateMachine getInstance()
   {
      if( instance == null )
      {
         instance = new StateMachine();
      }
      return instance;
   }
   
   /**
    * Adds a listener that will be notified when the internal state of the
    * game changes.
    * 
    * @param listener the listener to add.
    */
   public void addListener( final StateMachineListener listener )
   {
      if( listener != null && !this.listenerList.contains( listener ) )
      {
         this.listenerList.add( listener );
      }
   }
   
   /**
    * Removes the specified listener from the list.
    * 
    * @param listener the listener to remove.
    */
   public void removeListener( final StateMachineListener listener )
   {
      this.listenerList.remove( listener );
   }
   
   /**
    * @return the current state of the game.
    */
   public GameState getState()
   {
      return this.currentState;
   }
   
   /**
    * Sets the new state for the game.
    * 
    * @param state Cannot be <code>null</code>
    */
   public void setState( final GameState state )
   {
      if( state != null )
      {
         if( this.currentState != state )
         {
            final GameState oldState = this.currentState;
            this.currentState = state;
            
            fireStateChanged( oldState, state );
         }
      }
   }
   
   
   //================|  Protected Methods  |====================================
   
   /**
    * Notify all listeners that the state has changed.
    * @param oldState the old state.
    * @param newState the new state.
    */
   protected void fireStateChanged( final GameState oldState,
                                    final GameState newState )
   {
      for( final StateMachineListener listener : this.listenerList )
      {
         listener.stateChanged( oldState, newState );
      }
   }
}
