/*
 * Copyright © 2007 esgames.
 */
package com.esgames.abaddon.map.sections;

import com.esgames.abaddon.map.actions.MapAction;

/**
 * Contains action information from a map file.
 * 
 * @author Eduardo Rodrigues
 */
public class ActionSection implements FileSection
{
   //================|  Fields             |====================================
   
   /** The actions for the map */
   private final MapAction[] actions;
   
   
   //================|  Constructors       |====================================
   
   /**
    * @param actions the actions to set
    */
   public ActionSection( final MapAction[] actions )
   {
      this.actions = actions;
   }
   
   
   //================|  Public Methods     |====================================
   
   /**
    * @return the number of actions defined in this map.
    */
   public int getActionCount()
   {
      return this.actions.length;
   }

   /**
    * @param index the index of the action
    * @return the {@link MapAction} at the specified index.
    */
   public MapAction getAction( final int index )
   {
      return this.actions[ index ];
   }

   /**
    * @return all the {@link MapAction} defined for this map.
    */
   public MapAction[] getActions()
   {
      return this.actions;
   }
}
