import java.awt.Color;
import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.VisiblePinholeImage;
import javalib.worldimages.WorldImage;

// To represent the player character controlled by the used with health, weapons, and feelings (not really)
class Player extends AGameComponent {
	static final int WIDTH = 2; // In blocks
	static final int HEIGHT = 3; // In blocks
	static final Vector2D DIM = new Vector2D(Player.WIDTH * IConstant.BLOCK_SIZE, 
			Player.HEIGHT * IConstant.BLOCK_SIZE);
	static final double HORIZ_SPEED 
	= 1.5 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In pixels per tick, number is blocks per second
	static final double JUMP_VELOC 
	= -40 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In pixels per tick, number is blocks per second
	static final double TERMINAL_SPEED 
	= 100 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In pixels per tick, number is blocks per second

	Health health;
	Vector2D velocity; // Pixels per tick
	Weaponry weapons;

	// Constructor initializes this with the given top-left, constant dimensions, 3 health, and 0 velocity
	Player(Vector2D topLeft) {
		super(topLeft, Player.DIM);
		this.health = new Health(3);
		this.velocity = Vector2D.ZERO;
		this.weapons = new Weaponry();
	}
	
	//VISUALIZATIONS

	// Draws the player as a blue rectangle
	WorldImage render() {
		return new VisiblePinholeImage(this.body.render(Color.BLUE));
	}
	
	// Draws non-game component aspects of this player such as health, inventory, and weapons
	void drawHUD(WorldScene background) {
		this.weapons.drawOnto(background);
	}
	
	//MOVEMENT

	// Stops movement in the 'x' direction
	void haltX() {
		this.velocity = this.velocity.setX(0);
	}

	// Moves player in given x direction by constant speed
	// EFFECT: Modifies this' collision body's 'x' position
	void moveX(boolean isRight) {
		if (isRight) {
			this.velocity = this.velocity.setX(Player.HORIZ_SPEED * IConstant.BLOCK_SIZE);
		} else {
			this.velocity = this.velocity.setX(-1 * Player.HORIZ_SPEED * IConstant.BLOCK_SIZE);
		}
	}

	// Moves this player on tick according to its velocity, and adjusts velocity based on gravity
	// EFFECT: Modifies this' collision body's position and this' velocity
	void moveOnTick() {
		this.body = this.body.onMove(this.velocity);
		double nextVY = this.velocity.y + IConstant.GRAVITY;
		this.velocity = this.velocity.setY(Math.min(nextVY, Player.TERMINAL_SPEED));
	}

	// Gives this player an upwards y velocity to simulate jumping
	// EFFECT: Modifies this' velocity
	void jump() {
		this.velocity = this.velocity.setY(JUMP_VELOC);
	}

	// Is this player standing on top of the given rectangle?
	boolean standingOnBlock(Rectangle ground) {
		return (this.body.onTopOf(ground));
	}

	// Adjusts position and velocity of player to resolve a collision with the given rectangle
	// EFFECT: Modifies this' body's position and velocity
	void resolveCollision(Rectangle other) {
		Vector2D resolution = other.resolveCollision(this.body);
		this.body = this.body.onMove(resolution);
		if (resolution.magnitude() > IConstant.COL_TOL) {
			if (resolution.y == 0) {
				this.haltX();
			} else if (resolution.x == 0) {
				this.velocity = this.velocity.setY(0);
			} else {
				throw new RuntimeException("Collision resolution incorrectly calculated.");
			}
		}
	}

	//WEAPONS
	// Switches weapons to the one corresponding to position in inventory
	// EFFECT: Modifies this' Weaponry active weapon
	void switchWeapon(int next) {
		this.weapons.changeWeaponTo(next);
	}
	
	// Creates weapon effects due to firing weapon at the target
	// EFFECT: Modifies the active weapon on firing
	ArrayList<IWeaponEffect> fireCurrentWeapon(Vector2D target) {
		return this.weapons.currentWeapon().fire(this.body.center(), 
				this.body.center().displacementTo(target));
	}
	
	// Ticks weapons for purposes like refreshes/reloading
	// EFFECT: Modifies weapons in this' weaponry
	void tickWeapons() {
		this.weapons.tickWeaponry();
	}
}

class Health {
	int current;
	int max;

	Health(int current, int max) {
		if (current > max || current < 0 || max < 0) {
			throw new IllegalArgumentException("Health can't be negative, maximum cannot be exceeded.");
		}
		this.current = current;
		this.max = max;
	}

	Health(int max) {
		this(max, max);
	}
}
