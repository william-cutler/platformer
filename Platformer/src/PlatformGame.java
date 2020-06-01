import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

class PlatformGame {
	Player player;
	ArrayList<GroundBlock> ground;
	
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

	}
	
	// Draws the current state of the game onto the background
	// EFFECT: Modifies the given scene
	void drawSceneOnto(WorldScene background) {
		for(IGameComponent igc : this.gameComponents()) {
			igc.drawOnto(background);
		}
	}
	
	// Returns a list of all game components in play
	ArrayList<IGameComponent> gameComponents() {
		ArrayList<IGameComponent> igc = new ArrayList<>();
		igc.add(this.player);
		igc.addAll(this.ground);
		return igc;
	}
	
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
	
	// Moves player on tick and then handles interactions between other game components and the player
	// EFFECT: Modifies the player
	void tickPlayer() {
		this.player.moveOnTick();
		for(IGameComponent igc : this.gameComponents()) {
			igc.modifyPlayer(this.player);
		}
	}
}

