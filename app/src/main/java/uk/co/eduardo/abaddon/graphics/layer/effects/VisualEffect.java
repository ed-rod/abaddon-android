package uk.co.eduardo.abaddon.graphics.layer.effects;

import uk.co.eduardo.abaddon.graphics.layer.Layer;

/**
 * A visual effect layer.
 * 
 * @author Ed
 */
public interface VisualEffect extends Layer
{
   //================|  Public Methods     |====================================
   
   /**
    * Used to check to see if the effect has finished animating. If so
    * then this layer can be removed from the display.
    *  
    * @return <code>true</code> if the effect has finished animating.
    */
   boolean isEffectFinished();
}
