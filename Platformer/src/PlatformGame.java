import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

class PlatformGame {
	Player player;
	ArrayList<GroundBlock> ground;
	
	PlatformGame() {
		this.player = new Player(new Vector2D(10, 200));
		ground = new ArrayList<>();
		for(int i = 0; i < 30; i += 1) {
			ground.add(new GroundBlock(new Posn(i, 60)));
		}
	}
	
	void drawSceneOnto(WorldScene background) {
		for(IGameComponent igc : this.gameComponents()) {
			igc.drawOnto(background);
		}
	}
	
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
	
	void playerJump() {
		return;
	}
	
	void playerMoveX(boolean isRight) {
		this.player.moveX(isRight);
	}
	
	void tickPlayer() {
		this.player.moveOnTick();
		for(IGameComponent igc : this.gameComponents()) {
			igc.modifyPlayer(this.player);
		}
	}
	
	boolean playerFalling() {
		return false;
	}
}

