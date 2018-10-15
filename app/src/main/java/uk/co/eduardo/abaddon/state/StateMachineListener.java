package uk.co.eduardo.abaddon.state;

/**
 * Notified when the game state changes.
 * 
 * @author Ed
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
