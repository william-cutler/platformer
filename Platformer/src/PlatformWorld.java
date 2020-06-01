import javalib.impworld.World;
import javalib.impworld.WorldScene;

// To handle player interactions with the game and provide visualization
class PlatformWorld extends World {
	PlatformGame game;
	
	PlatformWorld() {
		this.game = new PlatformGame();
	}
	// Runs the game with constant window size and tick rate
	void run() {
		this.bigBang(IConstant.WINDX, IConstant.WINDY, IConstant.TICK_RATE);
	}
	// Returns the visual depiction of the current state of the game
	public WorldScene makeScene() {
		WorldScene background = new WorldScene(IConstant.WINDX, IConstant.WINDY);
		this.game.drawSceneOnto(background);
		return background;
	}
	
	// Responds to user key input to control the player
	// EFFECT: Modifies the player in PlatformGame
	public void onKeyEvent(String key) {
		if(key.equals("a") || key.equals("d")) {
			this.game.playerMoveX(key.equals("d"));
		} else if(key.equals(" ")) {
			this.game.playerJump();
		}
	}
	
	// Responds to user key input to control the player
	// EFFECT: Modifies the player in PlatformGame
	public void onKeyReleased(String key) {
		if(key.equals("a") || key.equals("d")) {
			this.game.haltPlayerX();
		}
	}
	
	// Causes game components to advance one tick in the PlatformGame
	// EFFECT: Modifies game components in PlatformGame
	public void onTick() {
		this.game.tickPlayer();
	}

}
