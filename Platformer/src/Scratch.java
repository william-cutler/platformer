// To represent some tracker that increments on a tick until some maximum is reached
class TickedAttribute {
	private int ticksSoFar;
	private final int maximum;
	
	// Default constructor initializes this with 0 progress toward the given positive maximum
	TickedAttribute(int maximum) {
		this.ticksSoFar = 0;
		if(maximum <= 0) {
			throw new IllegalArgumentException("Total ticks must be positive.");
		}
		this.maximum = maximum;
	}
	
	// Returns the proportion of progress that has been made
	double proportionComplete() {
		return (1.0 * this.ticksSoFar) / this.maximum;
	}
	
	// Increments progress made unless the maximum has been reached
	// EFFECT: Modifies this' ticksSoFar
	void onTick() {
		if(this.ticksSoFar < maximum) {
			this.ticksSoFar += 1;
		}
	}
	
	// Has the maximum been reached?
	boolean maxReached() {
		return this.ticksSoFar == this.maximum;
	}
	
	// Resets progress to 0
	// EFFECT: Modifies this' ticksSoFar
	void reset() {
		this.ticksSoFar = 0;
	}
}

class Ray {
	private final Vector2D position;
	private final double rayDirection;
	
	Ray(Vector2D position, double rayDirection) {
		this.position = position;
		this.rayDirection = rayDirection;
	}
	
	Ray(Vector2D position, Vector2D directionVector) {
		this(position, directionVector.angle());
	}
	
	boolean intersects(Vector2D point) {
		return this.position.displacementTo(point).angle() == this.rayDirection;
	}
	
	boolean intersects(Ray other) {
		double angleFromOther = other.angleTo(this.position);
		double angleFromThis = this.angleTo(other.getPosition());
		return angleFromOther + angleFromThis < 180;
	}
	
	double angleTo(Vector2D position) {
		Vector2D displacement = this.position.displacementTo(position);
		return displacement.angle() - this.rayDirection;
	}
	
	Vector2D getPosition() {
		return this.position;
	}
	
	double getDirection() {
		return this.rayDirection;
	}
}