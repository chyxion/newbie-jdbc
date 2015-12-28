package me.chyxion.dao;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 20, 2015 4:58:12 PM
 */
public class Order {
	public static final String ASC = "asc";
	public static final String DESC = "desc";
	private String col;
	private String direction;
	/**
	 * @param col
	 * @param direction
	 */
	public Order(String col, String direction) {
		super();
		this.col = col;
		this.direction = direction;
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
		this.direction = direction;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return col + " " + direction;
	}
}
