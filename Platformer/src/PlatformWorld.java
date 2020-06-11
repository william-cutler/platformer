import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

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
		} else if(key.matches("[0-9]+")) {
			this.game.playerSwitchWeapon(Integer.parseInt(key));
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
		this.game.tickWeaponEffects();
		this.game.tickEnemies();
		this.game.weaponInteract();
		this.game.enemiesInteractPlayer();
		this.game.removeComponents();
	}
	
	// Causes the player to fire current weapon in click direction
	// EFFECT: Modifies the WeaponEffects in PlatformGame
	public void onMousePressed(Posn pos, String buttonName) {
		if(buttonName.equals("LeftButton")) {
			this.game.playerFireAt(new Vector2D(pos));
		}
	}
	
	public void onMouseMoved(Posn pos) {
		this.game.playerFace(pos);
	}

}
