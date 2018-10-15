/*
 * Copyright © 2007 esgames.
 * All Rights Reserved.
 */
package com.esgames.abaddon.util;

import android.content.res.Resources;

/**
 * Typically only descendants of <code>View</code> have access to the resources.
 * This holds onto a reference to the resources to make them global and
 * accessible from anywhere
 * 
 * @author Eduardo Rodrigues
 */
public class Res
{
   //================|  Fields             |====================================
   
   /** Global resources */
   public static Resources resources;
}
