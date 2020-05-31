import javalib.impworld.World;
import javalib.impworld.WorldScene;

class PlatformWorld extends World {
	PlatformGame game;
	PlatformWorld() {
		this.game = new PlatformGame();
	}
	
	void run() {
		this.bigBang(IConstant.WINDX, IConstant.WINDY, IConstant.TICK_RATE);
	}
	
	public WorldScene makeScene() {
		WorldScene background = new WorldScene(IConstant.WINDX, IConstant.WINDY);
		this.game.drawSceneOnto(background);
		return background;
	}
	
	public void onKeyEvent(String key) {
		if(key.equals("a") || key.equals("d")) {
			this.game.playerMoveX(key.equals("d"));
		} else if(key.equals(" ")) {
			this.game.playerJump();
		}
	}
	
	public void onKeyReleased(String key) {
		if(key.equals("a") || key.equals("d")) {
			this.game.haltPlayerX();
		}
	}
	
	public void onTick() {
		this.game.tickPlayer();
	}

}
