package clipping;

/**
 * Representa un punto en el espacio 2D.
 * <p>
 * Esta clase proporciona métodos para obtener y establecer las coordenadas del punto.
 * </p>
 * <p>
 * Las coordenadas (x, y) representan la posición del punto, y el id es un identificador único.
 * </p>
 *
 * @author majam
 */
    public class Point {

    private double x;
    private double y;
    private long id;

    /**
     * Construye un Point con las coordenadas y el id especificados.
     *
     * @param x El valor x-coordinate del punto.
     * @param y El valor y-coordinate del punto.
     * @param id El identificador único del punto.
     */
    public Point(final double x, final double y, final long id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    /**
     * Obtiene el valor x-coordinate del punto.
     *
     * @return El valor x-coordinate del punto.
     */
    public double getX() {
        return x;
    }

    /**
     * Obtiene el valor y-coordinate del punto.
     *
     * @return El valor y-coordinate del punto.
     */
    public double getY() {
        return y;
    }

    /**
     * Establece el valor x-coordinate del punto.
     *
     * @param x El nuevo valor x-coordinate.
     */
    public void setX(final double x) {
        this.x = x;
    }

    /**
     * Establece el valor y-coordinate del punto.
     *
     * @param y El nuevo valor y-coordinate.
     */
    public void setY(final double y) {
        this.y = y;
    }

    /**
     * Obtiene el identificador único del punto.
     *
     * @return El identificador único del punto.
     */
    public long getId() {
        return id;
    }
}
