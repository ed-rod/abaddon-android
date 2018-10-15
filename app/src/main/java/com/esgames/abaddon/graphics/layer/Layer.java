/*
 * Copyright © 2007 esgames.
 * All Rights Reserved.
 */
package com.esgames.abaddon.graphics.layer;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * A layer of the game. Tiled maps, sprites and message windows are all layers
 * 
 * @author Eduardo Rodrigues
 */
public interface Layer
{
   //================|  Public Methods     |====================================
   
   /**
    * @param xPos position of the main character
    * @param yPos position of the main character
    * @param below <code>true</code> if this layer is currently below the hero
    * @param canvas the <code>Canvas</code> on which to paint
    * @param paint the <code>Paint</code> object to use for drawing
    */
   void draw( final int xPos,
              final int yPos,
              final boolean below,
              final Canvas canvas,
              final Paint paint );
   
   /**
    * Show or hide this layer
    * @param show
    */
   void setVisible( final boolean show );
   
   /**
    * @return true if the layer is visible
    */
   boolean isVisible();
}
