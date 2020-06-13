import javalib.impworld.WorldScene;
import javalib.worldimages.BesideImage;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.WorldImage;

// A collection of global constants
interface IConstant {
	int BLOCK_SIZE = 10; // In pixels
	int WINDX = BLOCK_SIZE * 120; // Window width in pixels
	int WINDY = BLOCK_SIZE * 80; // Window height in pixels
	double TICK_RATE = .01;
	double GRAVITY = 100 * TICK_RATE * TICK_RATE * BLOCK_SIZE; // In (blocks per second) per second
	Vector2D BLOCK_DIM = new Vector2D(BLOCK_SIZE, BLOCK_SIZE);
	double COL_TOL = .001 * BLOCK_SIZE; // Small number in pixels used for collision tolerances
}

// To represent something visible on the screen
interface IDrawable {
	// Draw a visual representation of this at a particular position onto the
	// background
	void drawOnto(WorldScene background);
}

// A component of the game that affects the other game components, 
// handles collisions, and is visible on screen
interface IGameComponent extends IDrawable {
	// Modify the given player if an interaction occurs
	void interactPlayer(Player pl);

	// The structure used for detecting and handling collisions
	ICollisionBody getCollisionBody();
	
	// Should this game component be removed from play?
	boolean shouldRemove();
	
	// Adjusts this game component on a tick, independent of other components
	void tick();
}

// A rectangular game component
abstract class AGameComponent implements IGameComponent {
	Rectangle body;

	AGameComponent(Rectangle body) {
		this.body = body;
	}

	// Constructor initializes this with a Rectangle body from the given parameters
	AGameComponent(Vector2D topLeft, Vector2D dimensions) {
		this(new Rectangle(topLeft, dimensions));
	}

	// Draws this at the proper position onto the background
	// EFFECT: Modifies the given scene
	public void drawOnto(WorldScene background) {
		background.placeImageXY(new ImgUtil().pinTopLeftFromCenter(this.render()), 
				(int) this.body.getPosition().x,
				(int) this.body.getPosition().y);
	}

	// By default, no modification to player occurs
	public void interactPlayer(Player pl) {
		if(this.body.collidingWith(pl.getCollisionBody())) {
			this.interactPlayerOnCollision(pl);
		}
	}
	// Defaults to doing nothing on colliding with the player
	void interactPlayerOnCollision(Player pl) {}

	// Returns this' rectangular collision body
	public ICollisionBody getCollisionBody() {
		return this.body;
	}

	// A visual depiction of this independent of position
	abstract WorldImage render();
}

//The player's health bar, with a current health and maximum health
class Health {
	// Both are minimum 0, current <= max
	int current;
	int max;

	Health(int current, int max) {
		if (current > max || current < 0 || max < 0) {
			throw new IllegalArgumentException("Health can't be negative, maximum cannot be exceeded.");
		}
		this.current = current;
		this.max = max;
	}

	// Constructor initializes this at full health
	Health(int max) {
		this(max, max);
	}
	
	// Dead if current health is 0
	boolean dead() {
		return this.current == 0;
	}
	
	// Returns Health with updated current that is still within bounds of [0, max]
	Health changeCurrent(int change) {
		int next = this.current + change;
		next = Math.max(0, next);
		next = Math.min(next, max);
		return new Health(next, this.max);
	}
	
	// Returns Health with updated max >= 0. Lowers current so it doesn't exceed max if necessary
	Health changeMax(int change) {
		int nextMax = this.max + change;
		return new Health(Math.min(this.current, nextMax), Math.max(0, nextMax));
	}
	
	// Draws this as a health bar at the top right of the scene, empty boxes indicate lost health from maximum
	// EFFECT: Places image onto the given scene
	void drawOnto(WorldScene background) {
		WorldImage healthBar = new EmptyImage();
		for(int hNum = 0; hNum < this.max; hNum += 1) {
			healthBar = new BesideImage(healthBar, new ImgUtil().drawHealthBox(hNum < this.current));
		}
		background.placeImageXY(new ImgUtil().pinTopRightFromCenter(healthBar), (int) IConstant.WINDX, 0);
	}
}

//A count-down clock that decrements up to a given amount
class TimeTemporary {
	private final int ticksLeft;

	TimeTemporary(int ticksLeft) {
		if (ticksLeft < 0) {
			throw new IllegalArgumentException("Ticks given must be positive.");
		}
		this.ticksLeft = ticksLeft;
	}

	// Returns new TimeTemporary with one less tick left, assuming there are any
	TimeTemporary onTick() {
		if (this.ticksLeft <= 0) {
			throw new RuntimeException("Cannot tick past maximum.");
		}
		return new TimeTemporary(this.ticksLeft - 1);
	}

	// Are there zero ticks left?
	boolean finished() {
		return this.ticksLeft == 0;
	}
}

// To simulate the constant-speed movement back and forth between two points
class BlockOscillation {
	private final Vector2D initPosn;
	Vector2D currPosn;
	private final Vector2D finalPosn;
	private final double speed;
	boolean towardFinal;
	
	// Standard constructor initializes all fields
	BlockOscillation(Vector2D initPosn, Vector2D finalPosn, 
			Vector2D currPosn, double speed, boolean towardFinal) {
		this.initPosn = initPosn;
		this.finalPosn = finalPosn;
		this.currPosn = currPosn;
		this.speed = speed;
		this.towardFinal = towardFinal;
	}
	
	// Convenience constructor that beings at initial position heading toward final position
	BlockOscillation(Vector2D initPosn, Vector2D finalPosn, double speed) {
		this(initPosn, finalPosn, initPosn, speed, true);
	}
	
	// Returns updated B.O. with new current position and direction
	BlockOscillation nextMove(Vector2D currPosn, boolean towardFinal) {
		return new BlockOscillation(this.initPosn, this.finalPosn, currPosn, this.speed, towardFinal);
	}
	
	// The current position (and most useful result) of this
	Vector2D getCurrPosn() {
		return this.currPosn;
	}
	
	Vector2D direction() {
		return this.currPosn.displacementTo(this.onMove().getCurrPosn()).getUnitVector();
	}
	
	// Returns updates B.O. moved in the current direction at speed, 
	// and can switch directions if end-point is reached
	BlockOscillation onMove() {
		if(towardFinal) {
			// Naively calculate next move
			Vector2D next = this.initPosn.displacementTo(this.finalPosn).scaleTo(
					this.speed / IConstant.TICK_RATE);
			// Determine if next move eclipses end-point, go to end-point if so, use naive move if not
			if(next.magnitude() > this.currPosn.distanceTo(this.finalPosn)) {
				return this.nextMove(this.finalPosn, false);
			} else {
				return this.nextMove(this.currPosn.addVectors(next), true);
			}
		} else {
			Vector2D next = this.finalPosn.displacementTo(this.initPosn).scaleTo(
					this.speed / IConstant.TICK_RATE);
			if(next.magnitude() > this.currPosn.distanceTo(this.initPosn)) {
				return this.nextMove(this.initPosn, true);
			} else {
				return this.nextMove(this.currPosn.addVectors(next), false);
			}
		}
	}	
}