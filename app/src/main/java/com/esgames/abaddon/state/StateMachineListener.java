/*
 * Copyright © 2009 esgames
 * All Rights Reserved.
 */
package com.esgames.abaddon.state;

/**
 * Notified when the game state changes.
 * 
 * @author Eduardo Rodrigues
 */
public interface StateMachineListener
{
   //================|  Public Methods     |====================================
   
   /**
    * Notification that the state of the game has changed.

    * @param oldState 
    * @param newState
    */
   void stateChanged( final GameState oldState, final GameState newState );
}
