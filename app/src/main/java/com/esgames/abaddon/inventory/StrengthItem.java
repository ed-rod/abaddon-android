/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.inventory;

/**
 * Items that implement this interface have some notion of a property that has variable strength.
 * 
 * @author Eduardo Rodrigues
 */
public interface StrengthItem
{
   /**
    * @return the strength of the item.
    */
   int getStrength();
}
