import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

class PlatformGame {
	Player player;
	ArrayList<GroundBlock> ground;
	ArrayList<IWeaponEffect> weaponEffects;
	ArrayList<IEnemy> enemies;
	
	PlatformGame() {
		this.player = new Player(new Vector2D(10, 350));
		
		ground = new ArrayList<>();
		for(int i = 0; i < 100; i += 1) {
			ground.add(new GroundBlock(new Posn(i, 60)));
		}
		ground.add(new GroundBlock(new Posn(5, 59)));
		ground.add(new GroundBlock(new Posn(35, 57)));
		ground.add(new GroundBlock(new Posn(40, 55)));
		ground.add(new GroundBlock(new Posn(45, 53)));
		ground.add(new GroundBlock(new Posn(47, 53)));
		ground.add(new GroundBlock(new Posn(49, 53)));

		ground.add(new GroundBlock(new Posn(50, 51)));
		ground.add(new GroundBlock(new Posn(10, 50)));
		ground.add(new GroundBlock(new Posn(35, 37)));
		ground.add(new GroundBlock(new Posn(38, 43)));
		ground.add(new GroundBlock(new Posn(43, 47)));
		
		this.weaponEffects = new ArrayList<>();
		this.enemies = new ArrayList<>();
		this.enemies.add(new MeleeEnemy(new Posn(70, 57), new Posn(90, 57)));
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
		for(GroundBlock gb : this.ground) {
			if (gb.playerOnTop(this.player)){
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
			//iwe.interactPlayer();
		}
	}
	
	// Have enemies interact with the player
	// EFFECT: Modifies player according to being hit by enemy
	void enemiesInteractPlayer() {
		for(IEnemy ie : this.enemies) {
			ie.interactPlayer(this.player);
		}
	}
	
	// Ticks enemies independent of other enemies
	// EFFECT: Modifies the enemies in play
	void tickEnemies() {
		for (IEnemy ie : this.enemies) {
			ie.tick();
		}
	}
	
	// Removes components from play that are no longer necessary
	// EFFECT: Modifies this' list of enemies and weapon effects
	void removeComponents() {
		this.enemies = new Util().filterOut(this.enemies, (e) -> e.shouldRemove());
		this.weaponEffects = new Util().filterOut(this.weaponEffects, (we) -> we.shouldRemove());
	}
}