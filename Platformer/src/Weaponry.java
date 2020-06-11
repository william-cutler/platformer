import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javalib.impworld.WorldScene;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;
import javalib.worldimages.BesideImage;


// To represent an inventory of unique weapons
class Weaponry implements IDrawable {
	static final int MAX_WEAPONS = 10;
	// To convert from inventory positions to the particular weapon held in the inventory
	private final HashMap<Integer, IWeapon> weapons;
	// Initially no weapon
	int currWeapon = 0;
	
	Weaponry() {
		this.weapons = new HashMap<>();
		this.weapons.put(0, new NoWeapon());
		this.weapons.put(1, new Knife());
		this.weapons.put(2,  new Pistol());
	}
	
	// Adds the given weapon only if that weapon has not been picked up before
	void addWeapon(IWeapon toAdd) {
		if(! this.weapons.containsKey(toAdd.inventoryPos())) {
			this.weapons.put(toAdd.inventoryPos(), toAdd);
		}
	}
	
	// Adds the given amount of ammunition to the weapon at the given inventory position if applicable
	// EFFECT: Modifies a weapon in this' weapons
	void addAmmo(int invPos, int amt) {
		if(invPos > 1 && this.weapons.keySet().contains(invPos)) {
			this.weapons.get(invPos).addAmmo(amt);
		}
	}
	
	// Sets the active weapon to the given inventory position, if it corresponds to a weapon in this set
	// EFFECT: Modifies this' pointer to the current weapon
	void changeWeaponTo(int nextWeapon) {
		if(this.weapons.containsKey(nextWeapon)) {
			this.currWeapon = nextWeapon;
		}
	}
	
	// Returns the actual weapon that is active
	IWeapon currentWeapon() {
		if(! this.weapons.containsKey(this.currWeapon)) {
			throw new RuntimeException("Do not currently have the current weapon.");
		}
		return this.weapons.get(this.currWeapon);
	}
	
	// Ticks every weapon in this inventory for reload/refresh purposes
	// EFFECT: Modifies weapons in this' collection of weapons
	void tickWeaponry() {
		for(IWeapon iw : this.weapons.values()) {
			iw.tickWeapon();
		}
	}

	// Draws the weapons in order at the top of the screen
	// with an indication of the active weapon
	// EFFECT: Modifies thie given scene by placing images on it
	public void drawOnto(WorldScene background) {
		WorldImage result = new EmptyImage();
		// foldr technique
		for(IWeapon w : this.weaponsInOrder()) {
			WorldImage wImg = w.inventoryPos() != this.currWeapon || w.inventoryPos() == 0 ? 
					w.drawInventory() : 
				new OverlayImage(w.drawInventory(), 
						new ImgUtil().drawActiveWeaponHighlight());
			result = new BesideImage(result, wImg);
		}
		background.placeImageXY(new ImgUtil().pinTopLeftFromCenter(result), 
				IConstant.BLOCK_SIZE, IConstant.BLOCK_SIZE);
	}
	
	// Returns a list of the weapons in order by their position
	ArrayList<IWeapon> weaponsInOrder() {
		ArrayList<IWeapon> result = new ArrayList<>();
		for(int inv = 0; inv < Weaponry.MAX_WEAPONS; inv += 1) {
			if(this.weapons.containsKey(inv)) {
				result.add(this.weapons.get(inv));
			}
		}
		return result;
	}
}

// To represent a weapon that inhabits a particular spot in the inventory
//  and can fire from the player at a given position
interface IWeapon {
	// The unique position this type of weapon occupies in the inventory
	int inventoryPos();
	
	// Causes this weapon to produce effects based on the position the player fires from and vector to
	// the target
	ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp);
	
	// Modifies this weapon on a tick for reload/refresh purposes
	void tickWeapon();
	
	// Draws an icon of this weapon for HUD including information like ammo remaining
	WorldImage drawInventory();
	
	// Add the given amount of ammunition to this weapon if applicable
	void addAmmo(int amt);
}

// A short-range, early game melee weapon
class Knife implements IWeapon {
	static final int INV = 1;
	// The amount of time that must pass between knife swings
	TimeTemporary reload;
	
	// A knife is initially ready to swing
	Knife() {
		this.reload = new TimeTemporary(0);
	}
	
	public int inventoryPos() {
		return Knife.INV;
	}

	// Returns a list with one knife-effect if the knife is ready to be swung
	public ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp) {
		ArrayList<IWeaponEffect> effect = new ArrayList<>();
		if(this.reload.finished()) {
			this.reload = new TimeTemporary((int) (.5 / IConstant.TICK_RATE));
			effect.add(new KnifeEffect(from, disp.x >= 0));
		}
		return effect;
	}
	
	// Refreshes the swing of knife by 1 tick, unless it is ready to swing again
	public void tickWeapon() {
		if(! this.reload.finished()) {
			this.reload = this.reload.onTick();
		}
	}

	// Draws this as a grey square in the inventory
	public WorldImage drawInventory() {
		WorldImage icon = new OverlayImage(new ImgUtil().drawBlock(Color.GRAY), 
				new ImgUtil().drawInventoryBox(this.inventoryPos(), -1));
		
		return icon;
	}

	public void addAmmo(int amt) {
		throw new RuntimeException("Knife has no ammo.");
	}
}

//A short-range, early game melee weapon
class Pistol implements IWeapon {
	static final int INV = 2;
	// The amount of time that must pass between knife swings
	TimeTemporary reload;
	
	int ammo;
	
	// A knife is initially ready to swing
	Pistol() {
		this.reload = new TimeTemporary(0);
		this.ammo = 10;
	}
	
	public int inventoryPos() {
		return Pistol.INV;
	}

	// Returns a list with one knife-effect if the knife is ready to be swung
	public ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp) {
		ArrayList<IWeaponEffect> toReturn = new ArrayList<>();
		if(this.reload.finished() && this.ammo > 0) {
			toReturn.add(new PlayerBullet(from, disp.getUnitVector()));
			this.reload = new TimeTemporary((int) (1 / IConstant.TICK_RATE));
			this.ammo -= 1;
		}
		return toReturn;
	}
	
	// Refreshes the swing of knife by 1 tick, unless it is ready to swing again
	public void tickWeapon() {
		if(! this.reload.finished()) {
			this.reload = this.reload.onTick();
		}
	}

	// Draws this as a grey square in the inventory
	public WorldImage drawInventory() {
		WorldImage icon = new OverlayImage(new ImgUtil().drawBlock(Color.BLACK), 
				new ImgUtil().drawInventoryBox(this.inventoryPos(), this.ammo));
		return icon;
	}

	// Adds given ammunition to this' ammo count
	// EFFECT: Modifies this' ammo
	public void addAmmo(int amt) {
		this.ammo += amt;
	}
}

// The absence of any weapon equipped (default state)
class NoWeapon implements IWeapon {
	static final int INV = 0;
	public int inventoryPos() {
		return NoWeapon.INV;
	}

	// has no effect on firing
	public ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp) {
		return new ArrayList<IWeaponEffect>();
	}

	// Has no effect on tick
	public void tickWeapon() {
		return;
	}
	
	// Has no image
	public WorldImage drawInventory() {
		return new EmptyImage();
	}

	public void addAmmo(int amt) {
		throw new RuntimeException("No-weapon has no ammo.");
	}
}

// To represent the physical effect of a weapon being activated
interface IWeaponEffect extends IGameComponent {
	// Interact with the given enemy likely if there is a collision
	void interactEnemy(IEnemy ie);
	
	// Interact with the given environment component likely if there is a collision
	void interactEnvironment(IEnvironment ie); //TODO: abstract collision detection
}

// To represent some projectile weapon effect flying through the air
abstract class AProjectile extends AGameComponent implements IWeaponEffect {
	Vector2D velocity;
	
	AProjectile(Rectangle body, Vector2D velocity) {
		super(body);
		this.velocity = velocity;
	}
	
	// Moves this projectile by its velocity
	// EFFECT: Modifies this' position
	void move() {
		this.body = this.body.onMove(this.velocity);
	}
}

// To represent a bullet fired by an enemy (presumably at the player)
class EnemyBullet extends AProjectile {
	static final int SIZE = 5;
	static final Vector2D DIM = new Vector2D(SIZE, SIZE);
	static final double SPEED = 20 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE;
	
	boolean hit;
	
	EnemyBullet(Vector2D start, Vector2D dir) {
		super(new Rectangle(start.addVectors(DIM.scaleVector(.5)), DIM), dir.scaleTo(SPEED));
	}

	// REnders as small orange square
	public WorldImage render() {
		return this.body.render(Color.ORANGE);
	}
	
	// Remove this bullet if it has hit something (environment, player)
	public boolean shouldRemove() {
		return this.hit;
	}

	// Moves this bullet on a tick
	// EFFECT: Modifies this' position
	public void tick() {
		this.move();
	}

	// No interaction with other enemies
	public void interactEnemy(IEnemy ie) {}

	// Flags 'hit' if collision with environment (to be removed)
	// EFFECT: Modifies this' hit flag
	public void interactEnvironment(IEnvironment ie) {
		if(this.body.collidingWith(ie.getCollisionBody())) {
			this.hit = true;
		}
	}
	
	// Flags 'hit' and reduces player health
	// EFFECT: Modifies this' hit and player's health
	public void interactPlayerOnCollision(Player pl) {
		this.hit = true;
		pl.onHit(1);
	}
}

// A bullet fired by the player that hurts enemies and cannot hurt player
class PlayerBullet extends EnemyBullet {
	PlayerBullet(Vector2D start, Vector2D dir) {
		super(start, dir);
	}
	
	// No player interaction
	public void interactPlayer(Player pl) {}
	
	// Flags 'hit' and reduces enemy health
	// EFFECT: Modifies this' hit and enemy's health
	public void interactEnemy(IEnemy ie) {
		if(this.body.collidingWith(ie.getCollisionBody())) {
			this.hit = true;
			ie.reduceHealth(1);
		}
	}
	
	// Renders as a small green square
	public WorldImage render() {
		return this.body.render(Color.GREEN);
	}
}

// To represent the swing of a knife
class KnifeEffect extends AGameComponent implements IWeaponEffect {
	// The amount of time a knife-swing exists
	TimeTemporary tt;
	
	// Constructor creates a Rectangle body just outside the player depending on which direction was swung at
	// And lasts for 1/20th of a second
	KnifeEffect(Vector2D fromCenter, boolean facingRight) {
		super(fromCenter.addVectors(
				new Vector2D(facingRight ? IConstant.BLOCK_SIZE : -2 * IConstant.BLOCK_SIZE, 
				-IConstant.BLOCK_SIZE / 2)), 
				IConstant.BLOCK_DIM);
		this.tt = new TimeTemporary((int) (.05 / IConstant.TICK_RATE));
	}

	// The knife-effect lasts as long as the given effect
	public boolean shouldRemove() {
		return this.tt.finished();
	}

	// Renders this as a grey square
	WorldImage render() {
		return this.body.render(Color.GRAY);
	}

	// Ticks this time-temporary  so that it lasts for one less tick
	// EFFECT: Effectively decrements this' TimeTemporary
	public void tick() {
		this.tt = this.tt.onTick();
	}
	
	// Reduces health of enemy by 1 if hit
	// EFFECT: Modifies enemy according to health loss method
	public void interactEnemy(IEnemy ie) {
		if(this.body.collidingWith(ie.getCollisionBody())) {
			ie.reduceHealth(1);
		}
	}

	// No environment interaction
	public void interactEnvironment(IEnvironment ie) {}
}

// Some cache of ammunition that can be picked up by the player
abstract class AAmmoPickup extends AGameComponent {
	int amount;
	boolean taken;
	
	AAmmoPickup(Posn blockPos, int amount) {
		super(new Util().topLFromBlock(blockPos), IConstant.BLOCK_DIM);
		this.amount = amount;
		this.taken = false;
	}
	
	// The inventory position of the corresponding weapon
	abstract int inventoryPosition();
	
	public void tick() {}
	
	// Remove this if it has been picked up by player
	public boolean shouldRemove() {
		return this.taken;
	}
	
	// Adds this' ammunition value to the corresponding weapon in player inventory
	// EFFECT: Modifies a weapon in player inventory
	public void interactPlayerOnCollision(Player pl) {
		if(! this.taken) {
			pl.addAmmo(this.inventoryPosition(), this.amount);
			this.taken = true;
		} else {
			throw new RuntimeException("Ammo already taken.");
		}
	}
}

// A cache of pistol ammunition
class PistolAmmo extends AAmmoPickup {

	PistolAmmo(Posn blockPos, int amount) {
		super(blockPos, amount);
	}

	// Corresponds to pistol
	int inventoryPosition() {
		return Pistol.INV;
	}

	// Grey square rendering
	WorldImage render() {
		return this.body.render(Color.GRAY);
	}
}