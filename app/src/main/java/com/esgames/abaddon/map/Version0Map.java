/**
 * Copyright esgames 2010
 */
package com.esgames.abaddon.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.esgames.abaddon.map.sections.ActionSectionProducer;
import com.esgames.abaddon.map.sections.HeaderSectionProducer;
import com.esgames.abaddon.map.sections.MapSectionProducer;
import com.esgames.abaddon.map.sections.MonsterSectionProducer;
import com.esgames.abaddon.map.sections.NpcSectionProducer;

/**
 * Support for version 0 map files.
 * 
 * @author Eduardo Rodrigues
 */
public class Version0Map implements MapVersion
{
   /**
    * {@inheritDoc}
    */
   @Override
   public int getVersion()
   {
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public MapDefinition readMap( final DataInputStream stream ) throws IOException
   {
      return new MapDefinition( new HeaderSectionProducer().readSection( stream ), 
                                new NpcSectionProducer().readSection( stream ), 
                                new ActionSectionProducer().readSection( stream ), 
                                new MapSectionProducer().readSection( stream ), 
                                new MonsterSectionProducer().readSection( stream ) );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void writeMap( final DataOutputStream stream, final MapDefinition map ) throws IOException
   {
      new HeaderSectionProducer().writeSection( stream, map.headerSection );
      new NpcSectionProducer().writeSection( stream, map.npcsSection );
      new ActionSectionProducer().writeSection( stream, map.actionsSection );
      new MapSectionProducer().writeSection( stream, map.mapsSection );
      new MonsterSectionProducer().writeSection( stream, map.monstersSection );
   }
}
