package me.chyxion.jdbc;

/**
 * SQL Order
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 20, 2015 4:58:12 PM
 */
public class Order {
	public static final String ASC = "asc";
	public static final String DESC = "desc";

	private String col;
	private String direction;

	/**
	 * @param col order column
	 * @param direction order direction
	 */
	public Order(String col, String direction) {
		this.col = col;
		setDirection(direction);
	}

	public Order(String col) {
		this.col = col;
	}

	/**
	 * @return the col
	 */
	public String getCol() {
		return col;
	}

	/**
	 * @param col the col to set
	 */
	public void setCol(String col) {
		this.col = col;
	}

	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		if (direction != null && 
			!ASC.equalsIgnoreCase(direction) &&
			!DESC.equalsIgnoreCase(direction)) {
			throw new IllegalArgumentException(
				"Invalid Order Direction [" + direction + "]");
		}
		this.direction = direction;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return direction != null ? col + " " + direction : col;
	}
}
