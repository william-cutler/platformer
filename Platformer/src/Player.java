import java.awt.Color;
import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.BesideImage;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.FromFileImage;
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
	static final int HIT_IMMUNITY = (int) (2 / IConstant.TICK_RATE);

	Health health;
	Vector2D velocity; // Pixels per tick
	Weaponry weapons;
	TimeTemporary hitImmunity;
	boolean facingRight;

	// Constructor initializes this with the given top-left, constant dimensions, 3 health, and 0 velocity
	Player(Vector2D topLeft) {
		super(topLeft, Player.DIM);
		this.health = new Health(3);
		this.velocity = Vector2D.ZERO;
		this.weapons = new Weaponry();
		this.hitImmunity = new TimeTemporary(Player.HIT_IMMUNITY);
		this.facingRight = true;
	}
	
	//VISUALIZATIONS

	// Draws the player as a blue rectangle
	WorldImage render() {
		WorldImage icon = new FromFileImage(this.facingRight ? "brash.jpg" : "brash-l.jpg");
		return new ImgUtil().scaleImgTo(icon, IConstant.BLOCK_SIZE * Player.WIDTH, 
				IConstant.BLOCK_SIZE * Player.HEIGHT);
	}
	
	// Draws non-game component aspects of this player such as health, inventory, and weapons
	void drawHUD(WorldScene background) {
		this.weapons.drawOnto(background);
		this.health.drawOnto(background);
	}
	
	void face(Vector2D pos) {
		this.facingRight = pos.x >= this.body.center().x;
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
	private void moveOnTick() {
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
	
	void addAmmo(int invPos, int amt) {
		this.weapons.addAmmo(invPos, amt);
	}
	
	// INTERFACING AND WORLD EXISTENCE
	
	// The Player should never be removed
	public boolean shouldRemove() {
		return false;
	}

	// Updates player motion according to velocity and gravity, ticks inventory weapons, and ticks
	// hit immunity if active
	// EFFECT: Modifies this' position, velocity, weaponry, and hitImmunity
	public void tick() {
		this.moveOnTick();
		this.weapons.tickWeaponry();
		if(! this.hitImmunity.finished()) {
			this.hitImmunity = this.hitImmunity.onTick();
		}
	}
	
	// Adjusts health upon taking damage if not hitImmune, and activates hitImmunity once hit
	// EFFECT: Modifies this' health and hitImmunity
	void onHit(int damage) {
		if(damage < 0) {throw new IllegalArgumentException("Cannot take negative damage.");}
		if(this.hitImmunity.finished()) {
			this.health = this.health.changeCurrent(-1 * damage);
			this.hitImmunity = new TimeTemporary(Player.HIT_IMMUNITY);
		}
	}
}

// The player's health bar, with a current health and maximum health
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
