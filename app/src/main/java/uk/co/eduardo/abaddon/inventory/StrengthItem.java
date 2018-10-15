package uk.co.eduardo.abaddon.inventory;

/**
 * Items that implement this interface have some notion of a property that has variable strength.
 * 
 * @author Ed
 */
public interface StrengthItem
{
   /**
    * @return the strength of the item.
    */
   int getStrength();
}
