import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

class PlatformGame {
	Player player;
	ArrayList<IEnvironment> ground;
	ArrayList<IWeaponEffect> weaponEffects;
	ArrayList<IEnemy> enemies;
	ArrayList<AItem> items;
	
	PlatformGame() {
		this.player = new Player(new Vector2D(5, 75).scaleByComponent(IConstant.BLOCK_DIM));
		
		EnvironmentGenerator gb = new EnvironmentGenerator();
		
		this.ground = new ArrayList<>();
		this.weaponEffects = new ArrayList<>();
		this.enemies = new ArrayList<>();
		this.items = new ArrayList<>();

		// Box around edge
		this.ground.add(gb.line(new Posn(0, 79), true, 120));
		this.ground.add(gb.line(new Posn(0, 0), true, 120));
		this.ground.add(gb.line(new Posn(0, 0), false, 100));
		this.ground.add(gb.line(new Posn(119, 0), false, 100));
		
		// Ceiling of section, initial floor and ceiling spikes a bit separated
		this.ground.add(gb.line(new Posn(0, 67), true, 112));
		this.ground.add(new Spikes(new Posn(20, 78), Direction.UP, 5));
		this.ground.add(new Spikes(new Posn(35, 68), Direction.DOWN, 5));

		this.enemies.add(new MeleeEnemy(new Posn(30, 76), new Posn(48, 76)));
		
		
		// Thick rectangle with left and partial-top spikes
		this.ground.add(gb.rectangle(new Posn(60, 75), new Posn(5, 4)));
		this.ground.add(new Spikes(new Posn(59, 75), Direction.LEFT, 4));
		this.ground.add(new Spikes(new Posn(60, 74), Direction.UP, 1));
		
		// Enemy after spike rectangle
		this.enemies.add(new MeleeEnemy(new Posn(70, 76), new Posn(90, 76)));
		
		// Line for jump up to next section, sentry turret below line
		this.ground.add(gb.line(new Posn(116, 74), true, 3));
		this.enemies.add(new SentryTurret(new Posn(117, 75)));

		// Next section, small box with top-right opening
		this.ground.add(gb.line(new Posn(90, 42), false, 25));
		this.ground.add(gb.line(new Posn(87, 36), true, 32));
		
		// Steps up through box
		this.ground.add(gb.line(new Posn(90, 60), true, 3));
		this.ground.add(gb.line(new Posn(100, 53), true, 3));
		this.ground.add(gb.line(new Posn(87, 42), true, 3));
		this.ground.add(gb.line(new Posn(90, 46), true, 3));

		this.enemies.add(new MeleeEnemy(new Posn(93, 64), new Posn(105, 64)));
		
		this.enemies.add(new SentryTurret(new Posn(110, 37)));
		
		this.ground.add(gb.line(new Posn(110, 48), true, 6));
		this.ground.add(new Spikes(new Posn(110, 47), Direction.UP, 1));

		this.items.add(new PistolAmmo(new Posn(114, 47), 3));


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