import java.awt.Color;
import java.util.ArrayList;

import javalib.worldimages.CircleImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;

// To represent a visible two-dimensional shape with a position that can handle collisions with various other
// constructions
interface ICollisionBody {
	// A visual depiction with the given color
	WorldImage render(Color c);
	
	// The position of the geometric center of this collision body in image coordinates
	Vector2D getPosition();
	
	// Returns this collision body having added the given velocity vector to its position vector
	ICollisionBody onMove(Vector2D velocity);
	
	// Is this collision body in contact or overlapping with the given collision body
	boolean collidingWith(ICollisionBody other);
	
	// Is this collision body colliding with the point at the given position?
	boolean collidingWithPoint(Vector2D pointPos);
	
	// Is this collision body colliding with the given rectangle
	boolean collidingWithRectangle(Rectangle other);
	
	// Returns the vector that the other collision body would have to move to resolve collision with THIS
	Vector2D resolveCollision(ICollisionBody other);
	
	// Returns the vector that THIS would have to move to resolve collision with the given point
	Vector2D resolveCollisionPoint(Vector2D pointPos);
	
	// Returns the vector that THIS would have to move to resolve collision with the given rectangle
	Vector2D resolveCollisionRect(Rectangle other);
	
	// Returns the geometric center of this collision body
	Vector2D center();
}

// To represent a collision body with a definite position
abstract class ACollisionBody implements ICollisionBody {
	protected final Vector2D topLeft;
	
	ACollisionBody(Vector2D topLeft) {
		this.topLeft = topLeft;
	}
	// Returns the position of the geometric center of this
	public Vector2D getPosition() {
		return this.topLeft;
	}
}

// To represent a single point on the plane that can collide with other shapes
class Point extends ACollisionBody {
	Point(Vector2D topLeft) {
		super(topLeft);
	}
	
	// Draws this point as a circle of definite radius with the given color
	public WorldImage render(Color c) {
		return new CircleImage(5, OutlineMode.SOLID, c);
	}

	// Returns a new point having moved by the given amount (velocity assumes 1 tick has passed)
	public ICollisionBody onMove(Vector2D velocity) {
		return new Point(this.topLeft.addVectors(velocity));
	}

	// Is this point in contact with the given body?
	public boolean collidingWith(ICollisionBody other) {
		return other.collidingWithPoint(this.topLeft);
	}

	// Is this point at the same position as the given point?
	public boolean collidingWithPoint(Vector2D pointPos) {
		return this.topLeft.equals(pointPos);
	}
	// Is this point in contact with the given rectangle?
	public boolean collidingWithRectangle(Rectangle rect) {
		return rect.collidingWithPoint(this.topLeft);
	}

	// Vector to move this point to stop colliding with given collision body
	public Vector2D resolveCollision(ICollisionBody other) {
		return other.resolveCollisionPoint(this.topLeft);
	}

	// Points have no collision that needs to be resolved with another point
	public Vector2D resolveCollisionPoint(Vector2D pointPos) {
		return Vector2D.ZERO;
	}

	// Vector that this point would have to displace to move away from given rectangle,
	// given by opposite of what rectangle would have to move
	public Vector2D resolveCollisionRect(Rectangle other) {
		return other.resolveCollisionPoint(this.topLeft).opposite();
	}
	
	// Center same as top left
	public Vector2D center() {
		return this.topLeft;
	}
}
// To represent a rectangle on the plane with dimensions, a position, and an orientation
class Rectangle extends ACollisionBody {
	// Overall dimensions in pixels
	final Vector2D dimensions;
	
	Rectangle(Vector2D topLeft, Vector2D dimensions) {
		super(topLeft);
		if(dimensions.x <= 0 || dimensions.y <= 0) {
			throw new IllegalArgumentException("Dimensions must be positive.");
		}
		this.dimensions = dimensions;
	}
	
	// The following methods give the four corners of this rectangle in pixels
	Vector2D topLeft() {return this.topLeft;}
	Vector2D topRight() {return this.topLeft.addToX(this.dimensions.x);}
	Vector2D botLeft() {return this.topLeft.addToY(this.dimensions.y);}
	Vector2D botRight() {return this.topLeft.addVectors(this.dimensions);}
	public Vector2D center() {return this.topLeft.addVectors(this.dimensions.scaleVector(.5));}
	
	// Returns the dimensions of this rectangle as width * height
	Vector2D getDimensions() {return this.dimensions;}
	
	Rectangle setPosition(Vector2D next) {
		return new Rectangle(next, this.dimensions);
	}
	// Returns the points corresponding to the four corners of this rectangle
	ArrayList<Point> cornerPoints() {
		return new Util().map(this.cornerPositions(), (pt) -> new Point(pt));
	}
	
	// Returns the points in image (x,y) coordinates that correspond to the corners of this rectangle
	ArrayList<Vector2D> cornerPositions() {
		ArrayList<Vector2D> pts = new ArrayList<>();
		pts.add(this.topLeft);
		pts.add(this.topRight());
		pts.add(this.botLeft());
		pts.add(this.botRight());
		return pts;
	}
	
	// Draws this as a rectangle of the appropriate dimensions and the given color,
	// rotated by this' appropriate orientationAngle
	public WorldImage render(Color c) {
		return new RectangleImage((int) this.dimensions.x, (int) this.dimensions.y, OutlineMode.SOLID, c);
	}
	
	// Returns a new rectangle having moved by the given amount (velocity assumes 1 tick has passed)
	public Rectangle onMove(Vector2D velocity) {
		return new Rectangle(this.topLeft.addVectors(velocity), this.dimensions);
	}

	// Is this rectangle in contact with the given body?
	public boolean collidingWith(ICollisionBody other) {
		return other.collidingWithRectangle(this);
	}
	// Is the point at the given position contained within this rectangle?
	public boolean collidingWithPoint(Vector2D pointPos) {
		Util u = new Util();
		return u.inclusiveBetween(this.topLeft.x, pointPos.x, this.topRight().x)
				&& u.inclusiveBetween(this.topLeft.y, pointPos.y, this.botLeft().y);
	}

	// Is this rectangle in contact with the given rectangle?
	public boolean collidingWithRectangle(Rectangle other) {
		Util u = new Util();
		// Are any of this' 4 corners within the other rectangle?
		boolean otherContainsThis = u.ormap(this.cornerPoints(), 
				(pt) -> pt.collidingWithRectangle(other));
		// Are any of the other's 4 corners within this?
		boolean thisContainsOther = u.ormap(other.cornerPoints(), 
				(pt) -> pt.collidingWithRectangle(this));
		// Are any of either rectangle's corners contained within the other?
		return otherContainsThis || thisContainsOther;
	}

	// Vector that other would have to move to resolve collision with this
	public Vector2D resolveCollision(ICollisionBody other) {
		return other.resolveCollisionRect(this);
	}

	// Vector that this rectangle would have to move to resolve collision with point
	public Vector2D resolveCollisionPoint(Vector2D pointPos) {
		if(! this.collidingWithPoint(pointPos)) {
			throw new IllegalArgumentException("Rectangle not colliding with point.");
		}
		Vector2D disp = this.topLeft.displacementTo(pointPos);
		
		Vector2D left = new Vector2D(disp.x - this.dimensions.x, 0);
		Vector2D right = new Vector2D(disp.x, 0);
		Vector2D up = new Vector2D(0, disp.y - this.dimensions.y);
		Vector2D down = new Vector2D(0, disp.y);
		return this.smallestVector(left, right, up, down);
	}

	// Vector that this rectangle would have to move to resolve collision with other rectangle
	public Vector2D resolveCollisionRect(Rectangle other) {
		Vector2D left = new Vector2D(other.topLeft().x - this.botRight().x, 0);
		Vector2D right = new Vector2D(other.botRight().x - this.topLeft.x, 0);
		Vector2D up = new Vector2D(0, other.topLeft().y - this.botRight().y);
		Vector2D down = new Vector2D(0, other.botRight().y - this.topLeft.y);
		return this.smallestVector(left, right, up, down);
	}
	
	// Returns the smallest magnitude vector of the four given
	Vector2D smallestVector(Vector2D left, Vector2D right, Vector2D up, Vector2D down) {
		ArrayList<Vector2D> resolution = new ArrayList<>();
		resolution.add(left);
		resolution.add(right);
		resolution.add(up);
		resolution.add(down);
		return new Util().findMin(resolution, (v) -> v.magnitude());
	}
	
	// Is this rectangle on top of the other rectangle (within the collision tolerance)
	boolean onTopOf(Rectangle other) {
		return (this.linearCollision(other, true)) 
				&& Math.abs(this.botRight().y - other.topLeft().y) < IConstant.COL_TOL;
	}
	
	// Is this rectangle within the other rectangle on the given axis 
	// (does not necessarily correspond to collision)
	boolean linearCollision(Rectangle other, boolean checkingColumn) {
		Util u = new Util();
		if(checkingColumn) {
			return (u.inclusiveBetween(this.topLeft.x, other.topLeft().x, this.topRight().x))
					|| u.inclusiveBetween(this.topLeft.x, other.topRight().x, this.topRight().x);
		} else {
			return (u.inclusiveBetween(this.topLeft.y, other.topLeft().y, this.topRight().y))
			|| u.inclusiveBetween(this.topLeft.y, other.topRight().y, this.topRight().y);
		}
	}
}