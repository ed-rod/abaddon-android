package uk.co.eduardo.abaddon.util;

/**
 * Class used for jumping. Initialise with a start position and a direction
 * and use the getNextPosition method to get the characters new location when
 * jumping
 * 
 * @author Eduardo Rodrigues
 */
public final class JumpInfo
{
   //================|  Fields             |====================================
   
   /** Left */
   private static final int LEFT = 1;

   /** Up */
   private static final int UP = 2;
   
   /** Right */
   private static final int RIGHT = 3;

   /** Down */
   private static final int DOWN = 4;
   
   /** The number of increments it takes to complete a jump */
   public static final int INCREMENT_COUNT = 8;
   
   /** The x increment during each jump increment */
   private final float xIncrement;
   
   /** The y increment during each jump increment */
   private final float yIncrement;
   
   private final float delta;
   
   /**
    * During each jump iteration the character is displaced in the y direction
    * to simulate going up and down. This is the cumulative displacement over
    * all the previous iterations
    */
   private float cumulativeDisplacement;
   
   /**
    * The amount by which the character will be displaced in the y 
    * direction during the next iteration
    */
   private float nextDisplacement;
   
   /** The pixel position from which we started jumping */
   private final float startX;
   
   /** The pixel position from which we started jumping */
   private final float startY;
   
   /** The current iteration */
   private int iteration;
   
   
   //================|  Constructors       |====================================
   
   /**
    * Constructs a new JumpInfo structure
    * @param startX the starting pixel position
    * @param startY the starting pixel position
    * @param direction the direction in which we should jump
    */
   public JumpInfo( final int startX, final int startY, final int direction )
   {
      // Work out the distance from the start position to the centre of the
      // start tile
      final int tileSize = ScreenSettings.tileSize;
      final int halfTileSize = tileSize / 2;
      
      final int xFrom = startX / tileSize;
      final int yFrom = startY / tileSize;
      
      int xTo;
      int yTo;
      
      switch( direction )
      {
      case LEFT:
         xTo = xFrom - 2;
         yTo = yFrom;
         break;
      case RIGHT:
         xTo = xFrom + 2;
         yTo = yFrom;
         break;
      case UP:
         xTo = xFrom;
         yTo = yFrom - 2;
         break;
      case DOWN:
         xTo = xFrom;
         yTo = yFrom + 2;
         break;
      default:
         throw new IllegalArgumentException( "Unknown Direction" ); //$NON-NLS-1$
      }
      
      final int xToCentre = xTo * tileSize + halfTileSize + 1;
      final int yToCentre = yTo * tileSize + halfTileSize + 1;
      
      this.xIncrement = ( xToCentre - startX ) / (float) INCREMENT_COUNT;
      this.yIncrement = ( yToCentre - startY ) / (float) INCREMENT_COUNT;
      this.startX = startX;
      this.startY = startY;
      this.cumulativeDisplacement = 0;
      this.nextDisplacement =
               DisplayResolutions.getConvertedFloat( ScreenSettings.densityDpi, INCREMENT_COUNT / 2 );
      
      this.delta = DisplayResolutions.getConvertedFloat( ScreenSettings.densityDpi, 1 );
      this.iteration = 0;
   }
   
   
   //================|  Public Methods     |====================================
   
   /**
    * @return the pixel coordinate for the next position or null if we've
    * finished
    */
   public Coordinate getNextPosition()
   {
      this.iteration++;
      if( this.iteration > INCREMENT_COUNT )
      {
         return null;
      }
      this.cumulativeDisplacement += this.nextDisplacement;
      this.nextDisplacement -= this.delta;
      if( this.nextDisplacement < 1e-3 ) // is zero
      {
         this.nextDisplacement = -this.delta;
      }
      final float currentX = this.startX + this.iteration * this.xIncrement;
      final float currentY = this.startY + this.iteration * this.yIncrement  - this.cumulativeDisplacement;
      
      return new Coordinate( (int) currentX, (int) currentY );
   }
   
   /**
    * @return the number of iterations we've completed
    */
   public int getIteration()
   {
      return this.iteration;
   }
}
