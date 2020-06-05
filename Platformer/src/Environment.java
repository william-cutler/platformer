import java.awt.Color;

import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;

// A single block that blocks movement in all directions
class GroundBlock extends AGameComponent {

	// Intializes this as a block at the given block position with standard block
	// dimensions
	GroundBlock(Posn blockPosition) {
		super(new Util().topLFromBlock(blockPosition), IConstant.BLOCK_DIM);
	}

	// Draws this as a black square
	WorldImage render() {
		return new ImgUtil().drawBlock(Color.BLACK);
	}

	// Prevents player from moving through this block
	// EFFECT: Modifies player position and velocity
	public void interactPlayer(Player pl) {
		if (pl.getCollisionBody().collidingWith(this.body)) {
			pl.resolveCollision(this.body);
		}
	}

	// Is the given player on top of this
	boolean playerOnTop(Player pl) {
		return pl.standingOnBlock(this.body);
	}
	
	public void tick() {}

	public boolean shouldRemove() {
		return false;
	}
}