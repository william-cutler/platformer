import javalib.worldimages.Posn;


//To represent a vector in 2D space, useful for position and velocity
class Vector2D {
	// Like Posn class, fields do not support mutation
	final double x;
	final double y;
	
	// Various useful constant vectors
	static final Vector2D ZERO = new Vector2D(0, 0);
	static final Vector2D UP = new Vector2D(0, -1);
	static final Vector2D DOWN = new Vector2D(0, 1);
	static final Vector2D LEFT = new Vector2D(-1, 0);
	static final Vector2D RIGHT = new Vector2D(1, 0);

	// Standard constructor initializes fields
	Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	// Convenience constructor that initializes this from a position
	Vector2D(Posn pos) {
		this(pos.x, pos.y);
	}
	
	// Returns a 2D vector from the given polar coordinates (angle in degrees)
	static Vector2D polarToRectangular(double r, double theta) {
		double xComp = r * Math.cos(Angle.toRadians(theta));
		double yComp = r * Math.sin(Angle.toRadians(theta));

		return new Vector2D(xComp, yComp);
	}
	// Returns the radius of the circle that circumscribes the rectangle given by the dimensions
	// Used for producing a shell that does not instantly collide with the tank that fired it
	static double bufferFromDimensions(Posn dimensions) {
		return new Vector2D(dimensions).scaleVector(.5).magnitude();
	}
	
	// Returns this as an integer position
	Posn toPosn() {
		return new Posn((int) this.x, (int) this.y);
	}

	// The result of component-wise vector addition
	Vector2D addVectors(Vector2D other) {
		return new Vector2D(this.x + other.x, this.y + other.y);
	}
	
	// Scales this vector to have the given magnitude
	Vector2D scaleTo(double magnitude) {
		return this.getUnitVector().scaleVector(magnitude);
	}
	
	// The result of component-wise scaling by the given scaling vector
	Vector2D scaleByComponent(Vector2D scaling) {
		return new Vector2D(this.x * scaling.x, this.y * scaling.y);
	}

	// The result of component-wise vector addition with the vector <amt, 0>
	Vector2D addToX(double amt) {
		return new Vector2D(this.x + amt, this.y);
	}

	// The result of component-wise vector addition with the vector <0, amt>
	Vector2D addToY(double amt) {
		return new Vector2D(this.x, this.y + amt);
	}

	// Returns a new vector with the given x but the same y
	Vector2D setX(double speed) {
		return new Vector2D(speed, this.y);
	}

	// Returns a new vector with the given y but the same x
	Vector2D setY(double y) {
		return new Vector2D(this.x, y);
	}
	
	// Returns the vector 'v' such that vector addition of this and 'v' produces the given vector
	Vector2D displacementTo(Vector2D other) {
		return new Vector2D(other.x - this.x, other.y - this.y);
	}
	
	// Returns the vector anti-parallel to this with the same magnitude
	Vector2D opposite() {
		return this.scaleVector(-1.0);
	}
	
	// Returns the vector with both components scaled by the given amount
	Vector2D scaleVector(double scaling) {
		return new Vector2D(this.x * scaling, this.y * scaling);
	}

	// Normalizes this point to the given position and orientation
	Vector2D normalize(Vector2D normalPosition, double normalOrientation) {
		return normalPosition.displacementTo(this).rotateBy(normalOrientation);
	}
	
	// Returns the result of rotating this vector clockwise by the given displacement
	Vector2D rotateBy(double angularDisp) {
		return Vector2D.polarToRectangular(this.magnitude(), this.angle() + angularDisp);
	}

	// Returns the vector with magnitude 1 in the same direction as this
	// If this is the zero vector, the default up vector (0, -1) is returned
	Vector2D getUnitVector() {
		if(this.magnitude() == 0) {
			return UP;
		} else {
			return this.scaleVector(1 / this.magnitude());
		}
	}
	
	// Returns the pythagorean sum of the components of this vector
	double magnitude() {
		return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
	}
	
	// Returns the angle that this vector makes with the positive 'x' axis measured clockwise in degrees
	double angle() {
		return Angle.toDegrees(Math.atan2(this.y, this.x));
	}
	
	// Returns the clockwise angle from this to other
	double angleDifference(Vector2D other) {
		return this.angle() - other.angle();
	}
	
	// Returns the euclidian distance between this vector and the other
	double distanceTo(Vector2D other) {
		return this.displacementTo(other).magnitude();
	}
	
	// Two 2DVectors are equal if their coordinates are the same
	public boolean equals(Object o) {
		if(! (o instanceof Vector2D)) {
			return false;
		} else {
			Vector2D pt = (Vector2D) o;
			return pt.x == this.x && pt.y == this.y;
		}
	}

	// A string summary of this vector that displays the two coordinates
	public String toString() {
		return "X: " + Double.toString(this.x) + ", Y: " + Double.toString(this.y);
	}
}

//A class for static angle conversion formulae
class Angle {
	// Converts the given degree measure to radians
	static double toRadians(double degree) {
		return degree * Math.PI / 180.0;
	}
	
	// Converts the given radian measure to degrees
	static double toDegrees(double radian) {
		return radian * 180.0 / Math.PI;
	}
}

//A function object that determines if the player is colliding with a 
//rectangular game component at centered at a particular position
abstract class ARectCollisionFunc {
	Vector2D objectTL;
	Vector2D objectDim;
	
	ARectCollisionFunc(Vector2D objectTL, Vector2D objectDim) {
		this.objectTL = objectTL;
		this.objectDim = objectDim;
	}
	
	// Is any part of the Player (based on given values) in either the same column or same row
	// as any part of the object checking collision?
	boolean linearCollision(Vector2D topLeft, Vector2D dim, boolean checkingColumn) {
		if(checkingColumn) {
			return this.intervalOverlap(topLeft.x, dim.x, this.objectTL.x, this.objectDim.x / 2);
		} else {
			return this.intervalOverlap(topLeft.y, dim.y, this.objectTL.y, this.objectDim.y / 2);
		}
	}
	
	// Is any part of playerSource +/- playerRange overlapping with objectSource +/- objectRange, inclusive?
	boolean intervalOverlap(double playerLow, double playerRange, double objectLow, double objectRange) {
		if(playerRange <= 0 || objectRange <= 0) {
			throw new IllegalArgumentException("Player and object ranges must be positive.");
		}
		double objectHigh = objectLow + objectRange;
		double playerHigh = playerLow + playerRange;
		
		return !(playerHigh < objectLow || playerLow > objectHigh);
	}
}

//A function object applied by the player that determines if the player is colliding with the
//object that initialized this function
class CollidingRect extends ARectCollisionFunc{
	CollidingRect(Vector2D objectTL, Vector2D objectDim) {
		super(objectTL, objectDim);
	}

	public boolean apply(Vector2D topLeft, Vector2D dim) {
		boolean horizontalCheck = this.linearCollision(topLeft, dim, true);
		boolean verticalCheck = this.linearCollision(topLeft, dim, false);

		return horizontalCheck && verticalCheck;
	}
}