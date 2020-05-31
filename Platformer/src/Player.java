import java.awt.Color;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.VisiblePinholeImage;
import javalib.worldimages.WorldImage;

class Player extends AGameComponent{
	static final int WIDTH = 2;	// In blocks
	static final int HEIGHT = 3; // In blocks
	static final double HORIZ_SPEED = 15 * IConstant.TICK_RATE; //In blocks per second
	static final double JUMP_VELOC = -3; //In blocks per tick
	static final double TERMINAL_SPEED = 100 * IConstant.BLOCK_SIZE * IConstant.TICK_RATE; // In blocks per second
	Health health;
	Vector2D velocity; //Pixels per tick
	
	Player(Vector2D topLeft) {
		super(topLeft);
		this.health = new Health(3);
		this.velocity = Vector2D.ZERO;
	}

	WorldImage render() {
		return new VisiblePinholeImage(new RectangleImage(Player.WIDTH * IConstant.BLOCK_SIZE, 
				Player.HEIGHT * IConstant.BLOCK_SIZE,
				OutlineMode.SOLID, Color.RED));
	}
	
	void haltX() {
		this.velocity = this.velocity.setX(0);
	}
	
	void moveX(boolean isRight) {
		if(isRight) {
			this.velocity = this.velocity.setX(Player.HORIZ_SPEED * IConstant.BLOCK_SIZE);
		} else {
			this.velocity = this.velocity.setX(-1 * Player.HORIZ_SPEED * IConstant.BLOCK_SIZE);
		}
	}
	
	void moveOnTick() {
		this.topLeft = this.topLeft.addVectors(this.velocity);
		double nextVY = this.velocity.y + IConstant.GRAVITY;
		this.velocity = this.velocity.setY(Math.min(nextVY, Player.TERMINAL_SPEED));
	}
	
	boolean colliding(CollidingRect rc) {
		return rc.apply(this.topLeft, this.dimensions());
	}
	
	boolean willCollide(CollidingRect rc) {
		return rc.apply(this.topLeft.addVectors(this.velocity), this.dimensions());
	}
	
	void snapTo(Vector2D otherTopLeft, Vector2D otherDim) {
		if(this.colliding(new CollidingRect(otherTopLeft, otherDim))) {
			throw new RuntimeException("Player already colliding.");
		}
		Vector2D bottomRight = this.topLeft.addVectors(this.dimensions());
		Vector2D otherBottomRight = otherTopLeft.addVectors(otherDim);

		if(bottomRight.y < otherTopLeft.y) {
			this.velocity = this.velocity.setY(0);
			this.topLeft = this.topLeft.setY(otherTopLeft.y - this.dimensions().y);

		} else if (bottomRight.x < otherTopLeft.x) {
			this.velocity = this.velocity.setX(0);

		} else if (this.topLeft.x > otherBottomRight.x) {
			this.velocity = this.velocity.setX(0);

		} else if (this.topLeft.y > otherBottomRight.y) {
			this.velocity = this.velocity.setY(0);
			this.topLeft = this.topLeft.setY(otherBottomRight.y);
		} else {
			throw new RuntimeException("Could not snap.");
		}
	}
	
	Vector2D dimensions() {
		return new Vector2D(Player.WIDTH * IConstant.BLOCK_SIZE, Player.HEIGHT * IConstant.BLOCK_SIZE);
	}
}

class Health {
	int current;
	int max;
	
	Health(int current, int max) {
		if(current > max || current < 0 || max < 0) {
			throw new IllegalArgumentException("Health can't be negative, maximum cannot be exceeded.");
		}
		this.current = current;
		this.max = max;
	}
	
	Health(int max) {
		this(max, max);
	}
}
