import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javalib.impworld.WorldScene;
import javalib.worldimages.WorldImage;

// To represent an inventory of unique weapons
class Weaponry {
	private final HashMap<Integer, IWeapon> weapons;
	int currWeapon = 0;
	
	Weaponry() {
		this.weapons = new HashMap<>();
		this.weapons.put(0, new NoWeapon());
		this.weapons.put(1, new Knife());
	}
	
	// Adds the given weapon only if that weapon has not been picked up before
	void addWeapon(IWeapon toAdd) {
		if(! this.weapons.containsKey(toAdd.inventoryPos())) {
			this.weapons.put(toAdd.inventoryPos(), toAdd);
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
	
	void tickWeaponry() {
		for(IWeapon iw : this.weapons.values()) {
			iw.tickWeapon();
		}
	}
}

// To represent a weapon that inhabits a particular spot in the inventory
//  and can fire from the player at a given position
interface IWeapon {
	int inventoryPos();
	
	ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp);
	
	void tickWeapon();
}

// A short-range, early game melee weapon
class Knife implements IWeapon {
	TimeTemporary reload;
	
	Knife() {
		this.reload = new TimeTemporary(0);
	}
	
	public int inventoryPos() {
		return 1;
	}

	public ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp) {
		ArrayList<IWeaponEffect> effect = new ArrayList<>();
		System.out.println(disp);
		if(this.reload.finished()) {
			this.reload = new TimeTemporary((int) (.5 / IConstant.TICK_RATE));
			effect.add(new KnifeEffect(from, disp.x >= 0));
		}
		return effect;
	}
	
	public void tickWeapon() {
		if(! this.reload.finished()) {
			this.reload = this.reload.onTick();
		}
	}
	
}

// The absence of any weapon equipped (default state)
class NoWeapon implements IWeapon {
	public int inventoryPos() {
		return 0;
	}

	public ArrayList<IWeaponEffect> fire(Vector2D from, Vector2D disp) {
		return new ArrayList<IWeaponEffect>();
	}

	public void tickWeapon() {
		return;
	}
}

interface IWeaponEffect extends IGameComponent {
	boolean finished();
	
	void tickWeapon();
}

class KnifeEffect extends AGameComponent implements IWeaponEffect {
	TimeTemporary tt;
	
	KnifeEffect(Vector2D fromCenter, boolean facingRight) {
		super(fromCenter.addVectors(
				new Vector2D(facingRight ? IConstant.BLOCK_SIZE : -2 * IConstant.BLOCK_SIZE, 
				-IConstant.BLOCK_SIZE / 2)), 
				IConstant.BLOCK_DIM);
		System.out.println(facingRight);
		this.tt = new TimeTemporary((int) (.05 / IConstant.TICK_RATE));
	}

	public boolean finished() {
		return this.tt.finished();
	}

	WorldImage render() {
		return this.body.render(Color.GRAY);
	}

	public void tickWeapon() {
		this.tt = this.tt.onTick();
	}
}