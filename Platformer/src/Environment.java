import java.awt.Color;

import javalib.worldimages.AboveImage;
import javalib.worldimages.BesideImage;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RotateImage;
import javalib.worldimages.TriangleImage;
import javalib.worldimages.WorldImage;

// To represent some component of the game environment
interface IEnvironment extends IGameComponent {
	boolean playerOnTop(Player pl);
}
// A single block that blocks movement in all directions
class GroundBlock extends AGameComponent implements IEnvironment {

	// Intializes this as a block at the given block position with standard block
	// dimensions
	GroundBlock(Posn blockPosition) {
		super(new Util().topLFromBlock(blockPosition), IConstant.BLOCK_DIM);
	}

	GroundBlock(Rectangle rect) {
		super(rect);
	}

	// Draws this as a black square
	WorldImage render() {
		return this.body.render(Color.BLACK);// new ImgUtil().drawBlock(Color.BLACK);
	}

	// Prevents player from moving through this block
	// EFFECT: Modifies player position and velocity
	public void interactPlayerOnCollision(Player pl) {
		pl.resolveCollision(this.body);
	}

	// Is the given player on top of this
	public boolean playerOnTop(Player pl) {
		return pl.standingOnBlock(this.body);
	}

	public void tick() {
	}

	public boolean shouldRemove() {
		return false;
	}
}

// To represent a connected line of spikes in the same direction that deals damage to player if touched
class Spikes extends AGameComponent implements IEnvironment {
	Direction dir;
	
	Spikes(Posn topLeft, Direction dir, int numBlocks) {
		super(new Util().topLFromBlock(topLeft), new Util().vertical(dir) 
				? IConstant.BLOCK_DIM.setX(numBlocks * IConstant.BLOCK_SIZE) 
						: IConstant.BLOCK_DIM.setY(numBlocks * IConstant.BLOCK_SIZE));
		this.dir = dir;
	}
	
	Spikes(Posn blockPosition, Direction dir) {
		super(new Util().topLFromBlock(blockPosition), IConstant.BLOCK_DIM);
		this.dir = dir;
	}
	
	Spikes(Posn blockPosition) {
		this(blockPosition, Direction.UP);
	}
	
	// Draws this as a row or column of spikes according to dimensions adn direction
	WorldImage render() {
		Vector2D dimVector = this.body.getDimensions().scaleVector(1.0 / IConstant.BLOCK_SIZE);
		// The number of blocks along the line of spikes that this occupies
		int numBlocks = (this.dir.equals(Direction.UP) || this.dir.equals(Direction.DOWN)) 
				? (int) dimVector.x : (int) dimVector.y;
				
		// foldr with stacking images horizontally or vertically depending on spike direction
		WorldImage result = new EmptyImage();
		for(int num = 0; num < numBlocks; num += 1) {
			if(this.dir.equals(Direction.UP) || this.dir.equals(Direction.DOWN)) {
				result = new BesideImage(result, this.renderOne());
			} else {
				result = new AboveImage(result, this.renderOne());
			}
		}
		return result;
	}

	// Draws this as a black triangle pointed in this' direction
	WorldImage renderOne() {
		WorldImage spike = new TriangleImage(new Posn(0, IConstant.BLOCK_SIZE), 
				IConstant.BLOCK_DIM.toPosn(), 
				new Posn(IConstant.BLOCK_SIZE / 2, 0),
				OutlineMode.SOLID,
				Color.BLACK).movePinholeTo(new Posn(0, 0));
		switch (this.dir) {
		case DOWN:
			return new RotateImage(spike, 180);
		case LEFT:
			return new RotateImage(spike, -90);
		case RIGHT:
			return new RotateImage(spike, 90);
		case UP:
			return spike;
		default:
			throw new RuntimeException("Improper direction enum.");
		}
	}

	// Prevents player from moving through this block
	// EFFECT: Modifies player position and velocity
	public void interactPlayerOnCollision(Player pl) {
			pl.resolveCollision(this.body);
			pl.onHit(1);
	}

	// Is the given player on top of this
	public boolean playerOnTop(Player pl) {
		return pl.standingOnBlock(this.body);
	}

	public void tick() {
	}

	public boolean shouldRemove() {
		return false;
	}
}

enum Direction {
	UP, DOWN, LEFT, RIGHT
}

class EnvironmentGenerator {
	GroundBlock line(Posn topLeft, boolean horizontal, int length) {
		if (horizontal) {
			return this.rectangle(topLeft, new Posn(length, 1));
		} else {
			return this.rectangle(topLeft, new Posn(1, length));
		}
	}

	GroundBlock rectangle(Posn topLeft, Posn dims) {
		Rectangle rect = new Rectangle(new Util().topLFromBlock(topLeft),
				new Vector2D(dims).scaleVector(IConstant.BLOCK_SIZE));
		return new GroundBlock(rect);
	}
}