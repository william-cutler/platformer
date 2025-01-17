import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.FromFileImage;
import javalib.worldimages.WorldImage;

// To represent the player character controlled by the used with health, weapons, and feelings (not really)
class Player extends AGameComponent {
	static final int WIDTH = 2; // In blocks
	static final int HEIGHT = 3; // In blocks
	static final Vector2D DIM = new Vector2D(Player.WIDTH * IConstant.BLOCK_SIZE, Player.HEIGHT * IConstant.BLOCK_SIZE);
	static final double HORIZ_SPEED = 1.5 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In pixels per tick, number is
																						// blocks per second
	static final double JUMP_VELOC = -40 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In pixels per tick, number is
																						// blocks per second
	static final double TERMINAL_SPEED = 100 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In pixels per tick, number
																							// is blocks per second
	static final int HIT_IMMUNITY = (int) (1.5 / IConstant.TICK_RATE);

	Health health;
	Vector2D velocity; // Pixels per tick
	Weaponry weapons;
	TimeTemporary hitImmunity;
	boolean facingRight;

	// Constructor initializes this with the given top-left, constant dimensions, 3
	// health, and 0 velocity
	Player(Vector2D topLeft) {
		super(topLeft, Player.DIM);
		this.health = new Health(3);
		this.velocity = Vector2D.ZERO;
		this.weapons = new Weaponry();
		this.hitImmunity = new TimeTemporary(Player.HIT_IMMUNITY);
		this.facingRight = true;
	}

	// VISUALIZATIONS

	// Draws the player as a blue rectangle
	WorldImage render() {
		String fname = this.facingRight ? "brash.jpg" : "brash-l.jpg";
		return this.body.render(fname);
	}

	// Draws non-game component aspects of this player such as health, inventory,
	// and weapons
	void drawHUD(WorldScene background) {
		this.weapons.drawOnto(background);
		this.health.drawOnto(background);
	}

	void face(Vector2D pos) {
		this.facingRight = pos.x >= this.body.center().x;
	}

	// MOVEMENT

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

	// Moves this player on tick according to its velocity, and adjusts velocity
	// based on gravity
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

	// Adjusts position and velocity of player to resolve a collision with the given
	// rectangle
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

	// WEAPONS
	// Switches weapons to the one corresponding to position in inventory
	// EFFECT: Modifies this' Weaponry active weapon
	void switchWeapon(int next) {
		this.weapons.changeWeaponTo(next);
	}

	// Creates weapon effects due to firing weapon at the target
	// EFFECT: Modifies the active weapon on firing
	ArrayList<IWeaponEffect> fireCurrentWeapon(Vector2D target) {
		return this.weapons.currentWeapon().fire(this.body.center(), this.body.center().displacementTo(target));
	}

	// Add the given ammo amount to the weapon at the corresponding inventory
	// position
	// EFFECT: Modifies a weapon in this' inventory of weapons
	void addAmmo(int invPos, int amt) {
		this.weapons.addAmmo(invPos, amt);
	}

	// INTERFACING AND WORLD EXISTENCE

	// The Player should never be removed
	public boolean shouldRemove() {
		return false;
	}

	// Updates player motion according to velocity and gravity, ticks inventory
	// weapons, and ticks
	// hit immunity if active
	// EFFECT: Modifies this' position, velocity, weaponry, and hitImmunity
	public void tick() {
		this.moveOnTick();
		this.weapons.tickWeaponry();
		if (!this.hitImmunity.finished()) {
			this.hitImmunity = this.hitImmunity.onTick();
		}
	}

	// Adjusts health upon taking damage if not hitImmune, and activates hitImmunity
	// once hit
	// EFFECT: Modifies this' health and hitImmunity
	void onHit(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("Cannot take negative damage.");
		}
		if (this.hitImmunity.finished()) {
			this.health = this.health.changeCurrent(-1 * damage);
			this.hitImmunity = new TimeTemporary(Player.HIT_IMMUNITY);
		}
	}

	// Adjusts health upon taking damage if not hitImmune, and activates hitImmunity
	// once hit
	// EFFECT: Modifies this' health and hitImmunity
	void gainHealth(int amt) {
		if (amt < 0) {
			throw new IllegalArgumentException("Cannot gain negative health.");
		}
		this.health = this.health.changeCurrent(amt);
	}
}