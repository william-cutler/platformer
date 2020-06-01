import java.awt.Color;
import java.util.ArrayList;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;

interface IConstant {
	int BLOCK_SIZE = 10; // In pixels
	int WINDX = BLOCK_SIZE * 120; // Window width in pixels
	int WINDY = BLOCK_SIZE * 80; // Window height in pixels
	double TICK_RATE = .01;
	double GRAVITY = 100 * TICK_RATE * TICK_RATE * BLOCK_SIZE; // In (blocks per second) per second
	Vector2D BLOCK_DIM = new Vector2D(BLOCK_SIZE, BLOCK_SIZE);
	double COL_TOL = .001 * BLOCK_SIZE; // Small number in pixels used for collision tolerances
}

// A component of the game that affects the other game components, 
// handles collisions, and is visible on screen
interface IGameComponent {
	// Draw a visual representation of this at a particular position onto the background
	void drawOnto(WorldScene background);
	// Modify the given player if an interaction occurs
	void modifyPlayer(Player pl);
	// The structure used for detecting and handling collisions
	ICollisionBody getCollisionBody();
}

// A rectangular game component
abstract class AGameComponent implements IGameComponent {
	Rectangle body;

	AGameComponent(Vector2D topLeft, Vector2D dimensions) {
		this.body = new Rectangle(topLeft, dimensions);
	}

	// Draws this at the proper position onto the background
	// EFFECT: Modifies the given scene
	public void drawOnto(WorldScene background) {
		background.placeImageXY(new ImgUtil().pinTopLeftFromCenter(this.render()), 
				(int) this.body.getPosition().x,
				(int) this.body.getPosition().y);
	}

	// By default, no modification to player occurs
	public void modifyPlayer(Player pl) {
		return;
	}
	
	// Returns this' rectangular collision body
	public ICollisionBody getCollisionBody() {
		return this.body;
	}

	// A visual depiction of this independent of position
	abstract WorldImage render();
}

// A single block that blocks movement in all directions
class GroundBlock extends AGameComponent {

	GroundBlock(Posn blockPosition) {
		super(new Util().topLFromBlock(blockPosition), IConstant.BLOCK_DIM);
	}

	// Draws this as a black square
	WorldImage render() {
		return new ImgUtil().drawBlock(Color.BLACK);
	}

	// Prevents player from moving through this block
	// EFFECT: Modifies player position and velocity
	public void modifyPlayer(Player pl) {
		if (pl.getCollisionBody().collidingWith(this.body)) {
			pl.resolveCollision(this.body);
		}
	}

	// Is the given player on top of this
	boolean playerOnTop(Player pl) {
		return pl.standingOnBlock(this.body);
	}
}

// Utility methods for images
class ImgUtil {
	// Moves pinhole to top left corner of the given image
	WorldImage pinTopLeftFromCenter(WorldImage img) {
		return img.movePinhole(-img.getWidth() / 2, -img.getHeight() / 2);
	}
	// Draws a square of constant size, solid fill of given color
	WorldImage drawBlock(Color c) {
		return new RectangleImage(IConstant.BLOCK_SIZE, IConstant.BLOCK_SIZE, OutlineMode.SOLID, c);
	}
}

// Utility methods
class Util {
	// Is the given middle number in between the low and high (inclusive)
	boolean inclusiveBetween(double low, double med, double high) {
		return low <= med && med <= high;
	}

	// Returns the top-left position of the block at the given block position
	Vector2D topLFromBlock(Posn blockPos) {
		return new Vector2D(blockPos.x * IConstant.BLOCK_SIZE, blockPos.y * IConstant.BLOCK_SIZE);
	}

	// Does the given predicate satisfy for at least one element in the given list
	<T> boolean ormap(ArrayList<T> al, IPred<T> pred) {
		for (T item : al) {
			if (pred.apply(item)) {
				return true;
			}
		}
		return false;
	}

	// Returns a positionally consistent list of the result of applying the given
	// function to each element in the list
	<T, U> ArrayList<U> map(ArrayList<T> al, IFunc<T, U> func) {
		ArrayList<U> result = new ArrayList<U>();
		for (T item : al) {
			result.add(func.apply(item));
		}
		return result;
	}

	// Returns the first item in the given list that minimizes the given function
	<T> T findMin(ArrayList<T> al, IFunc<T, Double> func) {
		if(al.size() == 0) {
			throw new IllegalArgumentException("Cannot find minimum of empty list.");
		}
		T min = al.get(0);
		for (T item : al.subList(1, al.size())) {
			if(func.apply(item) < func.apply(min)) {
				min = item;
			}
		}
		return min;
	}
}
