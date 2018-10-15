package com.esgames.abaddon.graphics.layer.effects;

import com.esgames.abaddon.graphics.layer.Layer;

/**
 * A visual effect layer.
 * 
 * @author Eduardo Rodrigues
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
