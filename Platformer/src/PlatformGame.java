import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

class PlatformGame {
	Player player;
	ArrayList<IEnvironment> ground;
	ArrayList<IWeaponEffect> weaponEffects;
	ArrayList<IEnemy> enemies;
	ArrayList<IGameComponent> items;
	
	PlatformGame() {
		this.player = new Player(new Vector2D(10, 350));
		
		EnvironmentGenerator gb = new EnvironmentGenerator();
		
		ground = new ArrayList<>();
		ground.add(gb.line(new Posn(0, 60), true, 100));
		ground.add(gb.line(new Posn(22, 53), true, 5));
		ground.add(gb.line(new Posn(50, 55), true, 10));

		ground.add(gb.line(new Posn(0, 0), false, 100));
		ground.add(new Spikes(new Posn(20, 59), Direction.UP, 10));
		
		this.weaponEffects = new ArrayList<>();
		this.enemies = new ArrayList<>();
		this.enemies.add(new MeleeEnemy(new Posn(70, 57), new Posn(90, 57)));
		
		this.items = new ArrayList<>();
		this.items.add(new PistolAmmo(new Posn(55, 54), 5));
		this.enemies.add(new SentryTurret(new Posn(50, 10)));
	}
	
	// Draws the current state of the game onto the background
	// EFFECT: Modifies the given scene
	void drawSceneOnto(WorldScene background) {
		for(IGameComponent igc : this.gameComponents()) {
			igc.drawOnto(background);
		}
		this.player.drawHUD(background);
	}
	
	// Returns a list of all game components in play
	ArrayList<IGameComponent> gameComponents() {
		ArrayList<IGameComponent> igc = new ArrayList<>();
		igc.add(this.player);
		igc.addAll(this.ground);
		igc.addAll(this.weaponEffects);
		igc.addAll(this.enemies);
		igc.addAll(this.items);
		return igc;
	}
	
	// PLAYER CONTROLS
	
	//Player controls
	void haltPlayerX() {
		this.player.haltX();
	}
	
	// Causes player to jump if player is standing on a solid surface
	// EFFECT: Modifies velocity of player
	void playerJump() {
		for(IEnvironment ie : this.ground) {
			if (ie.playerOnTop(this.player)){
				this.player.jump();
				return;
			}
		}
	}
	
	// Moves player in given direction horizontally
	// EFFECT: Modifies player velocity in 'x' direction
	void playerMoveX(boolean isRight) {
		this.player.moveX(isRight);
	}
	
	// Causes the player to fire at the target
	// EFFECT: Modifies this' list of WeaponEffects and weapon itself on firing
	void playerFireAt(Vector2D target) {
		this.weaponEffects.addAll(this.player.fireCurrentWeapon(target));
	}

	// Causes the player to switch current weapon based on given key input
	// EFFECT: Modifies active weapon in Player's Weaponry
	void playerSwitchWeapon(int next) {
		this.player.switchWeapon(next);
	}
	
	void playerFace(Posn pos) {
		this.player.face(new Vector2D(pos));
	}
	
	//TICKING AND INTERACTIONS
	
	// Moves player on tick and then handles interactions between other game components and the player
	// EFFECT: Modifies the player
	void tickPlayer() {
		this.player.tick();
		for(IGameComponent igc : this.gameComponents()) {
			igc.interactPlayer(this.player);
		}
	}
	
	// Causes weapon effects such as bullets, melee swings, etc to move/tick
	// EFFECT: Removes weapon effects that are no longer in play and ticks those remaining
	void tickWeaponEffects() {
		for (IWeaponEffect iwe : this.weaponEffects) {
			iwe.tick();
		}
	}
	
	// Have weapon effects interact with the enemies in play
	// EFFECT: Modifies this' weapon effects and enemies on collision
	void weaponInteract() {
		for (IWeaponEffect iwe : this.weaponEffects) {
			for(IEnemy ie : this.enemies) {
				iwe.interactEnemy(ie);
			}
			for (IEnvironment ie : this.ground) {
				iwe.interactEnvironment(ie);
			}
		}
	}
	
	// Have enemies interact with the player
	// EFFECT: Modifies player according to being hit by enemy
	void enemiesInteractPlayer() {
		for(IEnemy ie : this.enemies) {
			ie.interactPlayer(this.player);
		}
	}
	
	// Ticks enemies independent of other enemies and registers enemies' using weapons
	// EFFECT: Modifies the enemies in play and this' list of weapon effects
	void tickEnemies() {
		for (IEnemy ie : this.enemies) {
			ie.tick();
			this.weaponEffects.addAll(ie.fireAt(this.player.getCollisionBody().center()));
		}
	}
	
	// Removes components from play that are no longer necessary
	// EFFECT: Modifies this' list of enemies and weapon effects
	void removeComponents() {
		this.enemies = new Util().filterOut(this.enemies, (e) -> e.shouldRemove());
		this.weaponEffects = new Util().filterOut(this.weaponEffects, (we) -> we.shouldRemove());
		this.items = new Util().filterOut(this.items, (i) -> i.shouldRemove());
	}
}