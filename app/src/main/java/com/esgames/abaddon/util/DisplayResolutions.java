/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.util;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * Converts lengths between different display resolutions.
 * 
 * @author Eduardo Rodrigues
 */
// The field, DisplayMetrix.DENSITY_XXHIGH, is inlined and does not cause the map to break.
@TargetApi( Build.VERSION_CODES.JELLY_BEAN )
public class DisplayResolutions
{
   private static final float lowRatio = 1;
   private static final float mediumRatio = 1.354f;
   private static final float highRatio = 2f;
   private static final float extraHighRatio = 2.646f;
   private static final float extraExtraHighRatio = 3f;
   
   @SuppressLint( "UseSparseArrays" )
   private static final HashMap< Integer, Float > densityRatios = new HashMap< Integer, Float >();
   
   static
   {
      densityRatios.put( DisplayMetrics.DENSITY_LOW, lowRatio );
      densityRatios.put( DisplayMetrics.DENSITY_MEDIUM, mediumRatio );
      densityRatios.put( DisplayMetrics.DENSITY_HIGH, highRatio );
      densityRatios.put( DisplayMetrics.DENSITY_XHIGH, extraHighRatio );
      densityRatios.put( DisplayMetrics.DENSITY_XXHIGH, extraExtraHighRatio );
   }
   
   private static float getRatio( final int densityDpi )
   {
      final Float ratio = densityRatios.get( densityDpi );
      return ratio != null ? ratio : mediumRatio;
   }
   
   /**
    * Gets a length in pixels for the given DPI based on a low resolution length.
    * 
    * @param displayDpi the display DPI constant from {@link DisplayMetrics}.
    * @param lowDensityLength a length in pixels for a low resolution asset.
    * @return the density dependent length.
    */
   public static float getConvertedFloat( final int displayDpi, final int lowDensityLength )
   {
      return ( ( getRatio( displayDpi ) / lowRatio ) * lowDensityLength );
   }
   
   /**
    * Gets a length in pixels for the given DPI based on a low resolution length.
    * 
    * @param displayDpi the display DPI constant from {@link DisplayMetrics}.
    * @param lowDensityLength a length in pixels for a low resolution asset.
    * @return the density dependent length.
    */
   public static int getConvertedInt( final int displayDpi, final int lowDensityLength )
   {
      return Math.round( ( getRatio( displayDpi ) / lowRatio ) * lowDensityLength );
   }
   
   /**
    * Gets a length in pixels (rounded to the nearest even number) for the given
    * DPI based on a low resolution length.
    * 
    * @param displayDpi the display DPI constant from {@link DisplayMetrics}.
    * @param lowDensityLength a length in pixels for a low resolution asset.
    * @return the density dependent length.
    */
   public static int getConvertedEvenInt( final int displayDpi, final int lowDensityLength )
   {
      return Math.round( ( ( getRatio( displayDpi ) / lowRatio ) * lowDensityLength ) / 2 ) * 2;
   }
}
