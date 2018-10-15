package com.esgames.abaddon.state;

/**
 * The possible states the game may be in.
 * 
 * @author Eduardo Rodrigues
 */
public enum GameState
{
   //================|  Constants          |====================================
   
   /** Ready mode. */
   READY,
   
   /** Paused mode. */
   PAUSE,
   
   /** Loading map mode. */
   LOADING,
   
   /** Normal running mode. */
   RUNNING,
   
   /** Game ended mode. */
   LOSE,
   
   /** Start new game mode. */
   START_NEW,
   
   /** Error state. */
   ERROR;
}
