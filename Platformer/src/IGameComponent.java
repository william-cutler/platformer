import java.awt.Color;
import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
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
		return;
	}

	// Returns this' rectangular collision body
	public ICollisionBody getCollisionBody() {
		return this.body;
	}

	// A visual depiction of this independent of position
	abstract WorldImage render();
}

interface IEnemy extends IGameComponent {
	// Reduce this' health by the given amount
	void reduceHealth(int amt);
}

class MeleeEnemy extends AGameComponent implements IEnemy {
	BlockOscillation bo;
	boolean isDead;
	
	// Given starting block and finish block, initializes this with collision body at initial position
	MeleeEnemy(Posn start, Posn finish) {
		super(new Util().topLFromBlock(start), Player.DIM);
		this.bo = new BlockOscillation(new Util().topLFromBlock(start), 
				new Util().topLFromBlock(finish), Player.HORIZ_SPEED / 9);
		this.isDead = false;
	}

	// Renders this as a red rectangle
	WorldImage render() {
		return this.body.render(Color.RED);
	}
	
	// Ticks this player by moving and then updating collision body
	// EFFECT: Modifies this' BlockOscillation and CollisionBody
	public void tick() {
		this.bo = this.bo.onMove();
		this.body = this.body.setPosition(this.bo.getCurrPosn());
	}
	
	// Should remove this enemy if it is dead
	public boolean shouldRemove() {
		return this.isDead;
	}
	
	// Kills player
	public void reduceHealth(int amt) {
		this.isDead = true;
	}
	
	// Causes 1 damage to player if colliding
	// EFFECT: Calls Player.onHit()
	public void interactPlayer(Player pl) {
		if(this.body.collidingWith(pl.getCollisionBody())) {
			pl.onHit(1);
		}
	}
}

class BlockOscillation {
	Vector2D initPosn;
	Vector2D currPosn;
	Vector2D finalPosn;
	double speed;
	boolean towardFinal;
	
	BlockOscillation(Vector2D initPosn, Vector2D finalPosn, 
			Vector2D currPosn, double speed, boolean towardFinal) {
		this.initPosn = initPosn;
		this.finalPosn = finalPosn;
		this.currPosn = currPosn;
		this.speed = speed;
		this.towardFinal = towardFinal;
	}
	
	BlockOscillation(Vector2D initPosn, Vector2D finalPosn, double speed) {
		this(initPosn, finalPosn, initPosn, speed, true);
	}
	
	BlockOscillation nextMove(Vector2D currPosn, boolean towardFinal) {
		return new BlockOscillation(this.initPosn, this.finalPosn, currPosn, this.speed, towardFinal);
	}
	
	Vector2D getCurrPosn() {
		return this.currPosn;
	}
	
	BlockOscillation onMove() {
		if(towardFinal) {
			Vector2D next = this.initPosn.displacementTo(this.finalPosn).scaleTo(
					this.speed / IConstant.TICK_RATE);
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


// A count-down clock that decrements up to a given amount
class TimeTemporary {
	private final int total;
	private final int ticksLeft;

	TimeTemporary(int ticksLeft) {
		if (ticksLeft < 0) {
			throw new IllegalArgumentException("Ticks given must be positive.");
		}
		this.ticksLeft = ticksLeft;
		this.total = ticksLeft;
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
	
	TimeTemporary reset() {
		return new TimeTemporary(this.total);
	}
}
