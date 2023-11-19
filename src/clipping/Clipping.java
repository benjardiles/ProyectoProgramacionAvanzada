/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package clipping;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.swing.JOptionPane;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Clipping extends JPanel {

    private ArrayList<Point> points;
    private ArrayList<Edge> edges;
    private double zoom = 30000.0;
    private int xOffset = 0;
    private int yOffset = 0;
    private int dragStartX, dragStartY;
    private boolean dragging = false;
    private boolean centerOnStart = true;
    private int zoomX, zoomY;
    private Point[] selectedPoints = new Point[2];
    private int selectedPointsCount = 0;
    private int clickCount = 0; // Contador de clics

    public static final double RADIUS_OF_EARTH_KM = 6371.0;

    private HashMap<String, Color> highwayColors = new HashMap<>();
    Map<Long, Point> pointMap = new HashMap<>();
    ArrayList<String> nombresCarreteras = new ArrayList<>();

    public Clipping(ArrayList<Point> points, ArrayList<Edge> edges,String nodesFile, String edgesFile) {
        this.points = points;
        this.edges = edges;

        Random random = new Random(8497);
        Color randomColor = getRandomColor(random);
        Set<Color> usedColors = new HashSet<>();
        obtenerNombresDeCarreteras();

        for (String highwayName : nombresCarreteras) {
            do {
                randomColor = getRandomColor(random);
            } while (usedColors.contains(randomColor));

            usedColors.add(randomColor);
            highwayColors.put(highwayName, randomColor);
        }

        for (Point point : points) {
            pointMap.put(point.getId(), point);
        }

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                double zoomFactor = 1.1;

                // Actualizar la posición del cursor para hacer zoom
                zoomX = e.getX();
                zoomY = e.getY();

                if (notches < 0) {
                    // Zoom in
                    zoom *= zoomFactor;
                    xOffset = (int) (zoomX - (zoomX - xOffset) * zoomFactor);
                    yOffset = (int) (zoomY - (zoomY - yOffset) * zoomFactor);
                } else {
                    // Zoom out
                    xOffset = (int) (zoomX - (zoomX - xOffset) / zoomFactor);
                    yOffset = (int) (zoomY - (zoomY - yOffset) / zoomFactor);
                    zoom /= zoomFactor;
                }

                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickCount == 2) {
                    // Deseleccionar todos los nodos en el tercer clic
                    deselectAllPoints();
                    clickCount = 0;
                } else {
                    // Llama al método para seleccionar el nodo más cercano
                    selectClosestPoint(e.getX(), e.getY());
                    clickCount++;

                    if (clickCount == 2) {
                        showDistanceBetweenSelectedPoints();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragStartX = e.getX();
                dragStartY = e.getY();
                dragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int dragEndX = e.getX();
                    int dragEndY = e.getY();
                    xOffset += dragEndX - dragStartX;
                    yOffset += dragEndY - dragStartY;
                    dragStartX = dragEndX;
                    dragStartY = dragEndY;
                    repaint();
                }
            }
        });
    }

    private void showDistanceBetweenSelectedPoints() {
        if (selectedPointsCount == 2) {
            Point point1 = selectedPoints[0];
            Point point2 = selectedPoints[1];

            double lat1 = point1.getY();
            double lon1 = point1.getX();
            double lat2 = point2.getY();
            double lon2 = point2.getX();

            double distance = haversine(lat1, lon1, lat2, lon2);

            DecimalFormat df = new DecimalFormat("#.##");
            String message = "Nodo 1:\nID: " + point1.getId() + "\nLatitud: " + lat1 + "\nLongitud: " + lon1 + "\n\n"
                    + "Nodo 2:\nID: " + point2.getId() + "\nLatitud: " + lat2 + "\nLongitud: " + lon2 + "\n\n"
                    + "Distancia entre nodos: " + df.format(distance) + " km";

            JOptionPane.showMessageDialog(this, message, "Información de Nodos", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar exactamente 2 nodos para calcular la distancia.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectClosestPoint(int x, int y) {
        double minDistance = Double.MAX_VALUE;
        Point closestPoint = null;

        for (Point point : points) {
            double px = point.getX() * zoom + xOffset;
            double py = point.getY() * zoom + yOffset;
            double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));

            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = point;
            }
        }

        // Verificar si el punto ya está seleccionado
        if (isSelected(closestPoint)) {
            // Deseleccionar el punto
            deselectPoint(closestPoint);
        } else if (selectedPointsCount < 2) {
            // Seleccionar el punto si no se han seleccionado dos aún
            selectPoint(closestPoint);
        }

        repaint();
    }

    private boolean isSelected(Point point) {
        return point != null && (point == selectedPoints[0] || point == selectedPoints[1]);
    }

    private void selectPoint(Point point) {
        if (selectedPointsCount < 2) {
            selectedPoints[selectedPointsCount] = point;
            selectedPointsCount++;
        }
    }

    private void deselectPoint(Point point) {
        if (point == selectedPoints[0]) {
            selectedPoints[0] = selectedPoints[1];
            selectedPoints[1] = null;
            selectedPointsCount--;
        } else if (point == selectedPoints[1]) {
            selectedPoints[1] = null;
            selectedPointsCount--;
        }
    }

    private void deselectAllPoints() {
        for (int i = 0; i < selectedPoints.length; i++) {
            selectedPoints[i] = null;
        }   
        selectedPointsCount = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (centerOnStart) {
            centerContent(panelWidth, panelHeight);
            centerOnStart = false;
        }

        Rectangle clipRect = g2d.getClipBounds();

        g2d.setColor(Color.BLACK); // Color de las aristas
        for (Edge edge : edges) {
            Point u = pointMap.get(edge.getU());
            Point v = pointMap.get(edge.getV());
            if (u != null && v != null) {
                double x1 = u.getX() * zoom + xOffset;
                double y1 = u.getY() * zoom + yOffset;
                double x2 = v.getX() * zoom + xOffset;
                double y2 = v.getY() * zoom + yOffset;
                //System.out.println(x1+"\n"+y1+"\n"+x2+"\n"+y2);
                if (clipRect.intersectsLine(x1, y1, x2, y2)) {
                    String highway = edge.getHighway();
                    Color color = highwayColors.get(highway);

                    // Utiliza un color predeterminado si el nombre de la carretera no tiene un color asignado
                    if (color == null) {
                        color = Color.GRAY;
                    }

                    g2d.setColor(color);
                    g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                }
            }
        }

        // Dibujar puntos
        /*g2d.setColor(Color.BLUE); // Color de los puntos
        for (Point point : points) {
            double x = point.getX() * zoom + xOffset;
            double y = point.getY() * zoom + yOffset;
            int radius = 1;

            // Verificar si el punto está dentro del área visible antes de dibujar
            if (clipRect.contains(x, y)) {
                int xCenter = (int) x - radius;
                int yCenter = (int) y - radius;

                // Dibuja el punto seleccionado de manera diferente
                if (isSelected(point)) {
                    g2d.setColor(Color.GREEN); // Color del punto seleccionado
                    radius = 3; // Tamaño del punto seleccionado
                }

                g2d.fillOval(xCenter, yCenter, 2 * radius, 2 * radius);
                g2d.setColor(Color.BLUE); // Restaura el color para otros puntos
            }
        }*/

        drawLineBetweenSelectedPoints(g2d); // Dibuja la línea entre los nodos seleccionados
    }

    private Point findPointById(long osmid) {
        for (Point point : points) {
            if (point.getId() == osmid) {
                return point;
            }
        }
        return null;
    }

    private void centerContent(int panelWidth, int panelHeight) {
        double centerX = 0.0;
        double centerY = 0.0;

        for (Point point : points) {
            centerX += point.getX();
            centerY += point.getY();
        }

        centerX /= points.size();
        centerY /= points.size();

        xOffset = (int) (panelWidth / 2 - centerX * zoom);
        yOffset = (int) (panelHeight / 2 - centerY * zoom);
    }

    public static void main(String[] args) {
        /*Random r = new Random();
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        System.out.println();
        r = new Random(8483);
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("XML Drawing with Points and Edges");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            SwingWorker<ArrayList<Point>, Void> dataLoader = new SwingWorker<>() {
                @Override
                protected ArrayList<Point> doInBackground() throws Exception {
                    ArrayList<Point> points = cargarPoint("C:\\Users\\majam\\OneDrive\\Documentos\\NetBeansProjects\\Clipping\\nodes.xml");
                    return points;
                }

                @Override
                protected void done() {
                    try {
                        ArrayList<Point> points = get();
                        ArrayList<Edge> edges = cargarEdge("C:\\Users\\majam\\OneDrive\\Documentos\\NetBeansProjects\\Clipping\\edges.xml");
                        System.out.println("Cantidad de nodos (points): " + points.size());
                        System.out.println("Cantidad de aristas (edges): " + edges.size());

                        Clipping clippingPanel = new Clipping(points, edges);
                        frame.add(clippingPanel);

                        frame.setSize(800, 600);
                        frame.setVisible(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            dataLoader.execute();
        });*/
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Crear una instancia de Asixd (ventana de carga de archivos)
                Panel panel = new Panel();
                panel.setVisible(true);
            }
        });
    }

    public static ArrayList<Point> cargarPoint(String filePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ArrayList<Point>> future = executor.submit(() -> {
            ArrayList<Point> points = new ArrayList<>();
            try {
                File file = new File(filePath);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                NodeList nodeList = document.getElementsByTagName("row");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    long osmid = Long.parseLong(element.getElementsByTagName("osmid").item(0).getTextContent());
                    double x = Double.parseDouble(element.getElementsByTagName("x").item(0).getTextContent());
                    double y = Double.parseDouble(element.getElementsByTagName("y").item(0).getTextContent());
                    points.add(new Point(x, y, osmid));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return points;
        });

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        return new ArrayList<>();
    }

    public static ArrayList<Edge> cargarEdge(String filePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ArrayList<Edge>> future = executor.submit(() -> {
            ArrayList<Edge> edges = new ArrayList<>();
            try {
                File file = new File(filePath);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                NodeList nodeList = document.getElementsByTagName("edge");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    long u = Long.parseLong(element.getElementsByTagName("u").item(0).getTextContent());
                    long v = Long.parseLong(element.getElementsByTagName("v").item(0).getTextContent());
                    String highway = element.getElementsByTagName("highway").getLength() > 0
                            ? element.getElementsByTagName("highway").item(0).getTextContent()
                            : "unknown"; // Valor predeterminado en caso de que <highway> no exista

                    edges.add(new Edge(u, v, highway));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return edges;
        });

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        return new ArrayList<>();
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        // Convierte las coordenadas de grados a radianes
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // Diferencia de latitud y longitud entre los dos puntos
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Fórmula de la distancia haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distancia en kilómetros
        double distance = RADIUS_OF_EARTH_KM * c;

        return distance;
    }

    public void obtenerNombresDeCarreteras() {
        for (Edge edge : edges) {
            String highway = edge.getHighway();
            // Verifica si el nombre de la carretera ya está en la lista
            if (!nombresCarreteras.contains(highway)) {
                nombresCarreteras.add(highway);
            }
        }
        imprimirNombresDeCarreteras(nombresCarreteras);
    }

    public void imprimirNombresDeCarreteras(ArrayList<String> nombresCarreteras) {
        System.out.println("Nombres de carreteras sin repetir:");
        for (String nombre : nombresCarreteras) {
            System.out.println(nombre);
        }
    }

    public static Color getRandomColor(Random random) {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return new Color(red, green, blue);
    }

    private void drawLineBetweenSelectedPoints(Graphics2D g2d) {
        if (selectedPointsCount == 2) {
            Point point1 = selectedPoints[0];
            Point point2 = selectedPoints[1];

            double x1 = point1.getX() * zoom + xOffset;
            double y1 = point1.getY() * zoom + yOffset;
            double x2 = point2.getX() * zoom + xOffset;
            double y2 = point2.getY() * zoom + yOffset;

            // Color de la línea entre los nodos seleccionados
            g2d.setColor(Color.GREEN);

            // Grosor de línea más grande (cambia el valor para ajustar el grosor)
            BasicStroke stroke = new BasicStroke(3.0f); // Grosor de línea de 3 píxeles
            g2d.setStroke(stroke);

            g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

            // Restaura el grosor de línea predeterminado
            g2d.setStroke(new BasicStroke(1.0f));
        }
    }

}
