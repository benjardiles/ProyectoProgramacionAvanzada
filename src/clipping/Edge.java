/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clipping;

/**
 *
 * @author majam
 */
public class Edge {
    private long u;
    private long v;
    private String highway;

    public Edge(long u, long v, String highway) {
        this.u = u;
        this.v = v;
        this.highway = highway;
    }

    public long getU() {
        return u;
    }

    public long getV() {
        return v;
    }

    public String getHighway() {
        return highway;
    }
}
