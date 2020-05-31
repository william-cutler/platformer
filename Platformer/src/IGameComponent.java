import java.awt.Color;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;

interface IConstant {
	int BLOCK_SIZE = 10; //In pixels
	int WINDX = BLOCK_SIZE * 120; // Window width in pixels
	int WINDY = BLOCK_SIZE * 80; // Window height in pixels
	double TICK_RATE = 1/28.0;
	double GRAVITY = 50 * TICK_RATE * TICK_RATE * BLOCK_SIZE; // In (blocks per second) per second
	Vector2D BLOCK_DIM = new Vector2D(BLOCK_SIZE, BLOCK_SIZE);
}

interface IGameComponent {
	void drawOnto(WorldScene background);
	
	void modifyPlayer(Player pl);
}

abstract class AGameComponent implements IGameComponent {
	Vector2D topLeft;
	
	AGameComponent(Vector2D topLeft) {
		this.topLeft = topLeft;
	}
	
	public void drawOnto(WorldScene background) {
		background.placeImageXY(new ImgUtil().pinTopLeftFromCenter(this.render()), (int) this.topLeft.x, (int) this.topLeft.y); 
	}
	
	public void modifyPlayer(Player pl) {
		return;
	}
	
	abstract WorldImage render();
}

class GroundBlock extends AGameComponent {
	Posn blockPos;
	
	GroundBlock(Posn blockPosition) {
		super(new ImgUtil().topLFromBlock(blockPosition));
		this.blockPos = blockPosition;
	}

	WorldImage render() {
		return new ImgUtil().drawBlock(Color.BLACK);
	}
	
	public void modifyPlayer(Player pl) {
		if(pl.colliding(new CollidingRect(this.topLeft, IConstant.BLOCK_DIM))) {
			//throw new RuntimeException("Player in ground.");
		} else if(pl.willCollide(new CollidingRect(this.topLeft, IConstant.BLOCK_DIM))) {
			pl.snapTo(this.topLeft, IConstant.BLOCK_DIM);
		}
	}
} 

class ImgUtil {
	WorldImage pinTopLeftFromCenter(WorldImage img) {
		return img.movePinhole(- img.getWidth() / 2, - img.getHeight() / 2);
	}
	
	Vector2D topLFromBlock(Posn blockPos) {
		return new Vector2D(blockPos.x * IConstant.BLOCK_SIZE, blockPos.y * IConstant.BLOCK_SIZE);
	}
	
	WorldImage drawBlock(Color c) {
		return new RectangleImage(IConstant.BLOCK_SIZE, IConstant.BLOCK_SIZE, OutlineMode.SOLID, c);
	}
}


