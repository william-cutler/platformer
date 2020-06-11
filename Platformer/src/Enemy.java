import java.awt.Color;
import java.util.ArrayList;

import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;

// To represent some enemy that can fire at player and take damage
interface IEnemy extends IGameComponent {
	// Reduce this' health by the given amount
	void reduceHealth(int amt);
	
	// The group of weapon effects fired at the player
	ArrayList<IWeaponEffect> fireAt(Vector2D plCenter);
}

// To represent a basic enemy that hurts player if touched and moves between two points
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
	
	// Ticks this enemy by moving and then updating collision body
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
	
	// Reduces player health by 1 if collision
	// EFFECT: modifies player health
	void interactPlayerOnCollision(Player pl) {
		pl.onHit(1);
	}

	// Melee enemy does not fire weapons
	public ArrayList<IWeaponEffect> fireAt(Vector2D plCenter) {
		return new ArrayList<>();
	}
}

// To represent a stationary turret that fires bullets at player at constant time intervals
class SentryTurret extends AGameComponent implements IEnemy {
	static final double RELOAD = 2.0; //In seconds
	Health health;
	TimeTemporary reload;
	
	// Given starting block and finish block, initializes this with collision body at initial position
	SentryTurret(Posn topLeft) {
		super(new Util().topLFromBlock(topLeft), IConstant.BLOCK_DIM.scaleVector(2.0));
		this.health = new Health(3);
		this.reload = new TimeTemporary((int) (RELOAD / IConstant.TICK_RATE));
	}

	// Renders this as a red rectangle
	WorldImage render() {
		return this.body.render(Color.DARK_GRAY);
	}
	
	// Ticks this' reload if bullet is not ready
	// EFFECT: Modifies this' reload
	public void tick() {
		if(! this.reload.finished()) {
			this.reload = this.reload.onTick();
		}
	}
	
	// Should remove this enemy if at zero health
	public boolean shouldRemove() {
		return this.health.dead();
	}
	
	// Lowers health of sentry by amount
	public void reduceHealth(int amt) {
		if(amt < 0) {
			throw new IllegalArgumentException("Cannot take negative damage.");
		}
		this.health = this.health.changeCurrent(- amt);
	}
	
	// Ensures player cannot pass through this turret
	// EFFECT: Modifies player motion
	void interactPlayerOnCollision(Player pl) {
		pl.resolveCollision(this.body);
	}

	// Returns 1 shot fired at player position if shot is ready
	// EFFECT: Resets this' reload if shot is fired
	public ArrayList<IWeaponEffect> fireAt(Vector2D plCenter) {
		ArrayList<IWeaponEffect> toReturn = new ArrayList<>();
		if(this.reload.finished()) {
			toReturn.add(new EnemyBullet(this.body.center(), 
					this.body.center().displacementTo(plCenter).getUnitVector()));
			this.reload = new TimeTemporary((int) (RELOAD / IConstant.TICK_RATE));
		}
		return toReturn;
	}
}