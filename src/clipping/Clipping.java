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

    // Lista de puntos y aristas.
    private ArrayList<Point> points;
    private ArrayList<Edge> edges;
    // Factores de zoom y desplazamiento.
    private double zoom = 30000.0;
    private int xOffset = 0;
    private int yOffset = 0;

    // Coordenadas de inicio para arrastrar.
    private int dragStartX, dragStartY;
    private boolean dragging = false;

    // Opciones de inicio.
    private boolean centerOnStart = true;

    // Coordenadas y contador para puntos seleccionados.
    private int zoomX, zoomY;
    private Point[] selectedPoints = new Point[2];
    private int selectedPointsCount = 0;

    // Contador de clics.
    private int clickCount = 0;

    // Radio de la Tierra en kilómetros.
    public static final double RADIUS_OF_EARTH_KM = 6371.0;

    // Colores para carreteras.
    private HashMap<String, Color> highwayColors = new HashMap<>();

    // Mapa de puntos y lista de nombres de carreteras.
    private Map<Long, Point> pointMap = new HashMap<>();
    private final ArrayList<String> nombresCarreteras = new ArrayList<>();

    private ArrayList<Point> shortestPath;

    // Distancia focal (ajusta según sea necesario)
    private boolean perspectivaOriginal =  true;
    private double focalLength = 1000.0;

    /**
     * Construye un nuevo panel Clipping con los puntos y aristas dados.
     *
     * @param points La lista de puntos a mostrar.
     * @param edges La lista de aristas que conectan los puntos.
     * @param nodesFile La ruta del archivo de datos de nodos.
     * @param edgesFile La ruta del archivo de datos de aristas.
     */
    public Clipping(ArrayList<Point> points, ArrayList<Edge> edges,
            String nodesFile, String edgesFile) {
        this.points = points;
        this.edges = edges;

        // Cambiar los colores de las carreteras según su nombre
        Random random = new Random(8497);
        Color randomColor = getRandomColor(random);
        Set<Color> usedColors = new HashSet<>();
        obtenerNombresDeCarreteras();

        // Crear y agregar el botón al panel
        JButton changePerspectiveButton = new JButton("Cambiar Perspectiva");
        changePerspectiveButton.addActionListener(e -> cambiarPerspectiva());
        add(changePerspectiveButton);

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
            shortestPath = astar(point1, point2);
            printShortestPath();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar exactamente 2 nodos para calcular la distancia.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectClosestPoint(int x, int y) {
        double minDistance = Double.MAX_VALUE;
        Point closestPoint = null;
    
        for (Point point : points) {
            double px = point.getX() * zoom + xOffset;
            double py = point.getY() * zoom + yOffset;
    
            // Aplicar perspectiva solo si la perspectiva alternativa está activada
            if (!perspectivaOriginal) {
                double depth = focalLength / (focalLength + (-py));
                px = px * depth;
                py = py * depth;
            }
    
            double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
    
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = point;
            }
        }
    
        // Verificar si el punto ya está seleccionado
        handleSelectedPoint(closestPoint);
        repaint();
    }
    
    private void handleSelectedPoint(Point closestPoint) {
        if (isSelected(closestPoint)) {
            // Deseleccionar el punto
            deselectPoint(closestPoint);
        } else if (selectedPointsCount < 2) {
            // Seleccionar el punto si no se han seleccionado dos aún
            selectPoint(closestPoint);
        }
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
        shortestPath = null;
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

        drawEdges(g2d, clipRect);
        drawPoints(g2d, clipRect);
        if (shortestPath != null && shortestPath.size() >= 2) {
            drawShortestPath((Graphics2D) g, shortestPath);
        }
        drawLineBetweenSelectedPoints(g2d);
    }

     /**
     * Dibuja las aristas en el contexto gráfico dentro del rectángulo de recorte especificado.
     *
     * @param g2d El contexto gráfico.
     * @param clipRect El rectángulo de recorte.
     */
    private void drawEdges(Graphics2D g2d, Rectangle clipRect) {
        g2d.setColor(Color.BLACK);
    
        for (Edge edge : edges) {
            drawEdgeIfVisible(g2d, clipRect, edge);
        }
    }
    
    /**
     * Dibuja una arista si es visible en el contexto gráfico dentro del rectángulo de recorte especificado.
     *
     * @param g2d El contexto gráfico.
     * @param clipRect El rectángulo de recorte.
     * @param edge La arista a dibujar.
     */
    private void drawEdgeIfVisible(Graphics2D g2d, Rectangle clipRect, Edge edge) {
        Point u = pointMap.get(edge.getU());
        Point v = pointMap.get(edge.getV());
    
        if (u != null && v != null) {
            double[] coords1 = calculateCoordinates(u);
            double[] coords2 = calculateCoordinates(v);
    
            if (isEdgeVisible(clipRect, coords1, coords2)) {
                drawEdge(g2d, edge, coords1[0], coords1[1], coords2[0], coords2[1]);
            }
        }
    }
    
    /**
     * Calcula las coordenadas transformadas de un punto.
     *
     * @param point El punto a transformar.
     * @return Un arreglo de coordenadas [x, y].
     */
    private double[] calculateCoordinates(Point point) {
        double x = point.getX() * zoom + xOffset;
        double y = point.getY() * zoom + yOffset;
    
        if (!perspectivaOriginal) {
            double depth = focalLength / (focalLength + (-y));
            x = x * depth;
            y = y * depth;
        }
    
        return new double[]{x, y};
    }
    
     /**
     * Verifica si una arista es visible en el contexto gráfico dentro del rectángulo de recorte especificado.
     *
     * @param clipRect El rectángulo de recorte.
     * @param coords1 Las coordenadas del primer extremo de la arista.
     * @param coords2 Las coordenadas del segundo extremo de la arista.
     * @return Verdadero si la arista es visible, falso en caso contrario.
     */
    private boolean isEdgeVisible(Rectangle clipRect, double[] coords1, double[] coords2) {
        return clipRect.contains(coords1[0], coords1[1]) || clipRect.contains(coords2[0], coords2[1]);
    }
    
    /**
     * Dibuja una sola arista en el contexto gráfico.
     *
     * @param g2d El contexto gráfico.
     * @param edge La arista a dibujar.
     * @param x1 La coordenada x del punto de inicio.
     * @param y1 La coordenada y del punto de inicio.
     * @param x2 La coordenada x del punto final.
     * @param y2 La coordenada y del punto final.
     */
    private void drawEdge(Graphics2D g2d, Edge edge, double x1, double y1, double x2, double y2) {
        String highway = edge.getHighway();
        Color color = highwayColors.get(highway);

        if (color == null) {
            color = Color.GRAY;
        }

        g2d.setColor(color);
        g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    /**
     * Dibuja puntos en el contexto gráfico dentro del rectángulo de recorte
     * especificado.
     *
     * @param g2d El contexto gráfico.
     * @param clipRect El rectángulo de recorte.
     */
    private void drawPoints(Graphics2D g2d, Rectangle clipRect) {
        g2d.setColor(Color.BLUE);
    
        for (Point point : points) {
            double x = point.getX() * zoom + xOffset;
            double y = point.getY() * zoom + yOffset;
            int radius = 1;
    
            // Aplicar perspectiva solo si la perspectiva alternativa está activada
            if (!perspectivaOriginal) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                double depth = focalLength / (focalLength + (-y));
                x = x * depth;
                y = y * depth;
                x = Math.max(0, Math.min(panelWidth, x));
                y = Math.max(0, Math.min(panelHeight, y));
            }
    
            if (clipRect.contains(x, y)) {
                drawPoint(g2d, point, x, y, radius);
            }
        }
    }    

    /**
     * Dibuja un solo punto en el contexto gráfico.
     *
     * @param g2d El contexto gráfico.
     * @param point El punto a dibujar.
     * @param x La coordenada x del punto.
     * @param y La coordenada y del punto.
     * @param radius El radio del punto.
     */
    private void drawPoint(Graphics2D g2d, Point point, double x, double y, int radius) {
        int xCenter = (int) x - radius;
        int yCenter = (int) y - radius;

        if (isSelected(point)) {
            g2d.setColor(Color.GREEN);
            radius = 2;
        }

        g2d.fillOval(xCenter, yCenter, 2 * radius, 2 * radius);
        g2d.setColor(Color.BLUE);
    }

    /**
     * Centra el contenido en el panel según las coordenadas promedio de los
     * puntos.
     *
     * @param panelWidth El ancho del panel.
     * @param panelHeight La altura del panel.
     */
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

    /**
     * Código principal para ejecutar la aplicación.
     *
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Crear una instancia de Asixd (ventana de carga de archivos)
                Panel panel = new Panel();
                panel.setVisible(true);
            }
        });
    }

    /**
     * Calcula la distancia en kilómetros entre dos puntos dados en la Tierra
     * utilizando la fórmula de haversine.
     *
     * @param lat1 Latitud del primer punto.
     * @param lon1 Longitud del primer punto.
     * @param lat2 Latitud del segundo punto.
     * @param lon2 Longitud del segundo punto.
     * @return La distancia en kilómetros entre los dos puntos.
     */
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
    
            // Aplicar perspectiva solo si la perspectiva alternativa está activada
            if (!perspectivaOriginal) {
                double depth1 = focalLength / (focalLength + (-y1));
                double depth2 = focalLength / (focalLength + (-y2));
                x1 = x1 * depth1;
                y1 = y1 * depth1;
                x2 = x2 * depth2;
                y2 = y2 * depth2;
            }
    
            // Set the color of the line between selected points
            g2d.setColor(Color.GREEN);
    
            // Set the thickness of the line (change the value to adjust thickness)
            BasicStroke stroke = new BasicStroke(3.0f); // Line thickness of 3 pixels
            g2d.setStroke(stroke);
    
            // Draw the line between selected points
            g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    
            // Restore the default line thickness
            g2d.setStroke(new BasicStroke(1.0f));
        }
    }    

    /*private ArrayList<Point> dijkstra(Point start, Point end) {
        HashMap<Point, Double> distances = new HashMap<>();
        HashMap<Point, Point> predecessors = new HashMap<>();
        HashSet<Point> visited = new HashSet<>();
        distances.put(start, 0.0);
        while (true) {
            Point current = null;
            double minDistance = Double.MAX_VALUE;
    
            for (Map.Entry<Point, Double> entry : distances.entrySet()) {
                Point node = entry.getKey();
                double distance = entry.getValue();
    
                if (!visited.contains(node) && distance < minDistance) {
                    minDistance = distance;
                    current = node;
                }
            }
    
            if (current == null) break;
            visited.add(current);
    
            for (Point neighbor : getVisibleNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
    
                double tentativeDistance = distances.getOrDefault(current, 0.0) + calculateDistance(current, neighbor);
    
                if (tentativeDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, tentativeDistance);
                    predecessors.put(neighbor, current);
                }
            }
        }
    
        ArrayList<Point> path = new ArrayList<>();
        Point current = end;
        while (current != null) {
            path.add(current);
            current = predecessors.get(current);
        }
    
        Collections.reverse(path);
        return path;
    }
     */
    private double calculateDistance(Point start, Point end) {
        double lat1 = start.getY();
        double lon1 = start.getX();
        double lat2 = end.getY();
        double lon2 = end.getX();
        return haversine(lat1, lon1, lat2, lon2);
    }

    private void drawShortestPath(Graphics2D g2d, ArrayList<Point> path) {
        if (path.size() < 2) {
            return; // No hay suficientes nodos para dibujar un camino
        }
    
        g2d.setColor(Color.RED); // Color del camino más corto
    
        // Ajusta el grosor de la línea aquí (cambia el valor según tus preferencias)
        BasicStroke stroke = new BasicStroke(3.0f); // Grosor de línea de 3 píxeles
        g2d.setStroke(stroke);
    
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
    
            double x1 = p1.getX() * zoom + xOffset;
            double y1 = p1.getY() * zoom + yOffset;
            double x2 = p2.getX() * zoom + xOffset;
            double y2 = p2.getY() * zoom + yOffset;
    
            // Aplicar perspectiva solo si la perspectiva alternativa está activada
            if (!perspectivaOriginal) {
                double depth1 = focalLength / (focalLength + (-y1));
                double depth2 = focalLength / (focalLength + (-y2));
                x1 = x1 * depth1;
                y1 = y1 * depth1;
                x2 = x2 * depth2;
                y2 = y2 * depth2;
            }
    
            g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        }
    
        // Restaura el grosor de línea predeterminado
        g2d.setStroke(new BasicStroke(1.0f));
    }
    

    private Point getNeighbor(Point point, Edge edge) {
        if (edge.getU() == point.getId()) {
            return pointMap.get(edge.getV());
        } else if (edge.getV() == point.getId()) {
            return pointMap.get(edge.getU());
        }
        return null;
    }

    private boolean isValidNeighbor(Point point, Point neighbor, Edge edge, Point destination) {
        return neighbor != null && isVisible(point, neighbor) && isEdgeOnShortestPath(edge, destination);
    }

    private ArrayList<Point> getVisibleNeighbors(Point point, Point destination) {
        ArrayList<Point> visibleNeighbors = new ArrayList<>();

        for (Edge edge : edges) {
            Point neighbor = getNeighbor(point, edge);

            if (isValidNeighbor(point, neighbor, edge, destination)) {
                visibleNeighbors.add(neighbor);
            }
        }

        return visibleNeighbors;
    }

    private boolean isEdgeOnShortestPath(Edge edge, Point destination) {
        // Verificar si el edge es parte del camino más corto al destino
        // Puedes implementar lógica específica para tu aplicación aquí
        // En este ejemplo, se asume que todos los edges son necesarios
        return true;
    }

    private boolean isVisible(Point p1, Point p2) {
        double x1 = p1.getX() * zoom + xOffset;
        double y1 = p1.getY() * zoom + yOffset;
        double x2 = p2.getX() * zoom + xOffset;
        double y2 = p2.getY() * zoom + yOffset;

        Rectangle clipRect = getVisibleRect();

        return clipRect.intersectsLine(x1, y1, x2, y2);
    }

    private void printShortestPath() {
        if (shortestPath != null && shortestPath.size() >= 2) {
            System.out.println("Ruta más cercana encontrada:");
            for (Point point : shortestPath) {
                System.out.println("ID: " + point.getId() + ", Latitud: " + point.getY() + ", Longitud: " + point.getX());
            }
        }
    }

    ArrayList<Point> astar(Point start, Point end) {
        HashMap<Point, Double> distances = new HashMap<>();
        HashMap<Point, Point> predecessors = new HashMap<>();
        HashSet<Point> visited = new HashSet<>();
        distances.put(start, 0.0);

        while (!distances.isEmpty()) {
            Point current = findNextNode(distances, visited, end);
            if (current == null || current.equals(end)) {
                break;
            }

            visited.add(current);
            distances.remove(current);
            updateDistancesAndPredecessors(current, distances, predecessors, visited, end);
        }

        return reconstructPath(predecessors, end);
    }

    private Point findNextNode(HashMap<Point, Double> distances, HashSet<Point> visited, Point end) {
        Point current = null;
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<Point, Double> entry : distances.entrySet()) {
            Point node = entry.getKey();
            double distance = entry.getValue() + heuristic(node, end);

            if (!visited.contains(node) && distance < minDistance) {
                minDistance = distance;
                current = node;
            }
        }

        return current;
    }

    private void updateDistancesAndPredecessors(Point current, HashMap<Point, Double> distances,
            HashMap<Point, Point> predecessors, HashSet<Point> visited, Point end) {
        for (Point neighbor : getVisibleNeighbors(current, end)) {
            if (visited.contains(neighbor)) {
                continue;
            }

            double tentativeDistance = distances.getOrDefault(current, 0.0) + calculateDistance(current, neighbor);

            if (tentativeDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                distances.put(neighbor, tentativeDistance);
                predecessors.put(neighbor, current);
            }
        }
    }

    private ArrayList<Point> reconstructPath(HashMap<Point, Point> predecessors, Point end) {
        ArrayList<Point> path = new ArrayList<>();
        Point current = end;

        while (current != null) {
            path.add(current);
            current = predecessors.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    private double heuristic(Point start, Point end) {
        // Puedes usar la distancia euclidiana como heurística
        return Math.sqrt(Math.pow(end.getX() - start.getX(), 2) + Math.pow(end.getY() - start.getY(), 2));
    }

    private void cambiarPerspectiva() {
        perspectivaOriginal = !perspectivaOriginal;
        repaint();  // Vuelve a pintar la escena con la nueva perspectiva
    }
}
