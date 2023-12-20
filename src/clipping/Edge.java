package clipping;
/**
 * Representa una arista en un grafo.
 *
 * <p>Esta clase contiene información sobre los nodos conectados por la arista y el tipo de carretera (si está disponible).</p>
 *
 * @author majam
 */
public class Edge {
    private final long u;
    private final long v;
    private final String highway;

    /**
     * Construye una Edge con los nodos conectados y el tipo de carretera especificados.
     *
     * @param u Uno de los nodos conectados por la arista.
     * @param v El otro nodo conectado por la arista.
     * @param highway El tipo de carretera, o "unknown" si no está disponible.
     */
    public Edge(long u, long v, String highway) {
        this.u = u;
        this.v = v;
        this.highway = highway;
    }

    /**
     * Obtiene uno de los nodos conectados por la arista.
     *
     * @return Uno de los nodos conectados por la arista.
     */
    public long getU() {
        return u;
    }

    /**
     * Obtiene el otro nodo conectado por la arista.
     *
     * @return El otro nodo conectado por la arista.
     */
    public long getV() {
        return v;
    }

    /**
     * Obtiene el tipo de carretera.
     *
     * @return El tipo de carretera, o "unknown" si no está disponible.
     */
    public String getHighway() {
        return highway;
    }
}
