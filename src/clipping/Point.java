/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clipping;

/**
 *
 * @author majam
    */
   public class Point {

       double x;
       double y;
       long id;

       public Point(double x, double y, long id) {
           this.x = x;
           this.y = y;
           this.id = id;
       }

       public double getX() {
           return x;
       }

       public double getY() {
           return y;
       }

       public void setX(double x) {
       this.x = x;
       }

       public void setY(double y) {
           this.y = y;
       }

       public long getId() {
           return id;
       }

       @Override
       public String toString() {
           return "Point(x=" + x + ", y=" + y + ", id=" + id + ")";
       }
   }