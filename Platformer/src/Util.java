import java.awt.Color;
import java.util.ArrayList;

import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;


interface IFunc<A, R> {
	R apply(A arg);
}

interface IPred<T> extends IFunc<T, Boolean>{}

// Utility methods for images
class ImgUtil {
	// Moves pinhole to top left corner of the given image
	WorldImage pinTopLeftFromCenter(WorldImage img) {
		return img.movePinhole(-img.getWidth() / 2, -img.getHeight() / 2);
	}

	// Moves pinhole to top left corner of the given image
	WorldImage pinTopRightFromCenter(WorldImage img) {
		return img.movePinhole(img.getWidth() / 2, -img.getHeight() / 2);
	}

	// Draws a square of constant size, solid fill of given color
	WorldImage drawBlock(Color c) {
		return new RectangleImage(IConstant.BLOCK_SIZE, IConstant.BLOCK_SIZE, OutlineMode.SOLID, c);
	}

	// Returns an image of an inventory box to display a weapon for HUD that scales
	// with block size
	WorldImage drawInventoryBox() {
		return new OverlayImage(
				new RectangleImage(IConstant.BLOCK_SIZE * 3 - 3, 
						IConstant.BLOCK_SIZE * 3 - 3, OutlineMode.SOLID, Color.WHITE),
				new RectangleImage(IConstant.BLOCK_SIZE * 3, 
						IConstant.BLOCK_SIZE * 3, OutlineMode.SOLID, Color.BLACK));
	}

	// Returns an image of an inventory box to display a weapon for HUD that scales
	// with block size
	WorldImage drawHealthBox(boolean hasHealth) {
		Color boxColor = hasHealth ? Color.red : Color.white;
		return new OverlayImage(
				new RectangleImage(IConstant.BLOCK_SIZE * 2 - 2, 
						IConstant.BLOCK_SIZE * 2 - 2, OutlineMode.SOLID, boxColor),
				new RectangleImage(IConstant.BLOCK_SIZE * 2, 
						IConstant.BLOCK_SIZE * 2, OutlineMode.SOLID, Color.BLACK));
	}

	// Returns an orange square that fits under an inventory box to indicate the
	// active weapon
	WorldImage drawActiveWeaponHighlight() {
		return new RectangleImage(IConstant.BLOCK_SIZE * 3 + 2, IConstant.BLOCK_SIZE * 3 + 2, OutlineMode.SOLID,
				Color.ORANGE);
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
		if (al.size() == 0) {
			throw new IllegalArgumentException("Cannot find minimum of empty list.");
		}
		T min = al.get(0);
		for (T item : al.subList(1, al.size())) {
			if (func.apply(item) < func.apply(min)) {
				min = item;
			}
		}
		return min;
	}

	// Returns a new list without any elements that satisfy the given predicate
	<T> ArrayList<T> filterOut(ArrayList<T> al, IPred<T> pred) {
		ArrayList<T> result = new ArrayList<T>();
		for (T item : al) {
			if (!pred.apply(item)) {
				result.add(item);
			}
		}
		return result;
	}
}