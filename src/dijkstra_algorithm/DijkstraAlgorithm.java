package dijkstra_algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DijkstraAlgorithm {

    static class Vertex {

        private ArrayList<Edge> neighborhood;
        private String label;

        public Vertex(String label) {
            this.label = label;
            this.neighborhood = new ArrayList<Edge>();
        }

        public void addNeighbor(Edge edge) {
            if (this.neighborhood.contains(edge)) {
                return;
            }
            this.neighborhood.add(edge);
        }

        public boolean containsNeighbor(Edge other) {
            return this.neighborhood.contains(other);
        }

        public Edge getNeighbor(int index) {
            return this.neighborhood.get(index);
        }

        public void removeNeighbor(Edge e) {
            this.neighborhood.remove(e);
        }

        public int getNeighborCount() {
            return this.neighborhood.size();
        }

        public String getLabel() {
            return this.label;
        }

        public String toString() {
            return label;
        }

        public ArrayList<Edge> getNeighbors() {
            return new ArrayList<Edge>(this.neighborhood);
        }
    }

    static class Edge {

        private Vertex one, two;
        private int weight;

        public Edge(Vertex one, Vertex two, int weight) {
            this.one = (one.getLabel().compareTo(two.getLabel()) <= 0) ? one : two;
            this.two = (this.one == one) ? two : one;
            this.weight = weight;
        }

        public Vertex getNeighbor(Vertex current) {
            if (!(current.equals(one) || current.equals(two))) {
                return null;
            }

            return (current.equals(one)) ? two : one;
        }

        public Vertex getOne() {
            return this.one;
        }

        public Vertex getTwo() {
            return this.two;
        }

        public int getWeight() {
            return this.weight;
        }

        public int hashCode() {
            return (one.getLabel() + two.getLabel()).hashCode();
        }
    }

    static class Graph {

        private HashMap<String, Vertex> vertices;
        private HashMap<Integer, Edge> edges;

        public Graph() {
            this.vertices = new HashMap<String, Vertex>();
            this.edges = new HashMap<Integer, Edge>();
        }

        public boolean addEdge(Vertex one, Vertex two, int weight) {
            if (one.equals(two)) {
                return false;
            }
            Edge e = new Edge(one, two, weight);
            if (edges.containsKey(e.hashCode())) {
                return false;
            } else if (one.containsNeighbor(e) || two.containsNeighbor(e)) {
                return false;
            }
            edges.put(e.hashCode(), e);
            one.addNeighbor(e);
            two.addNeighbor(e);
            return true;
        }

        public Edge removeEdge(Edge e) {
            e.getOne().removeNeighbor(e);
            e.getTwo().removeNeighbor(e);
            return this.edges.remove(e.hashCode());
        }

        public Vertex getVertex(String label) {
            return vertices.get(label);
        }

        public boolean addVertex(Vertex vertex, boolean overwriteExisting) {
            Vertex current = this.vertices.get(vertex.getLabel());
            if (current != null) {
                if (!overwriteExisting) {
                    return false;
                }
                while (current.getNeighborCount() > 0) {
                    this.removeEdge(current.getNeighbor(0));
                }
            }
            vertices.put(vertex.getLabel(), vertex);
            return true;
        }

        public Set<String> vertexKeys() {
            return this.vertices.keySet();
        }
    }

    static class Dijkstra {

        private Graph graph;
        private String initialVertexLabel;
        private HashMap<String, String> predecessors;
        private HashMap<String, Integer> distances;
        private PriorityQueue<Vertex> availableVertices;
        private HashSet<Vertex> visitedVertices;

        public Dijkstra(Graph graph, String initialVertexLabel) {
            this.graph = graph;
            Set<String> vertexKeys = this.graph.vertexKeys();
            if (!vertexKeys.contains(initialVertexLabel)) {
                throw new IllegalArgumentException("The graph must contain the initial vertex.");
            }
            this.initialVertexLabel = initialVertexLabel;
            this.predecessors = new HashMap<String, String>();
            this.distances = new HashMap<String, Integer>();
            this.availableVertices = new PriorityQueue<Vertex>(vertexKeys.size(), new Comparator<Vertex>() {
                public int compare(Vertex one, Vertex two) {
                    int weightOne = Dijkstra.this.distances.get(one.getLabel());
                    int weightTwo = Dijkstra.this.distances.get(two.getLabel());
                    return weightOne - weightTwo;
                }
            });
            this.visitedVertices = new HashSet<Vertex>();
            for (String key : vertexKeys) {
                this.predecessors.put(key, null);
                this.distances.put(key, Integer.MAX_VALUE);
            }
            this.distances.put(initialVertexLabel, 0);
            Vertex initialVertex = this.graph.getVertex(initialVertexLabel);
            ArrayList<Edge> initialVertexNeighbors = initialVertex.getNeighbors();
            for (Edge e : initialVertexNeighbors) {
                Vertex other = e.getNeighbor(initialVertex);
                this.predecessors.put(other.getLabel(), initialVertexLabel);
                this.distances.put(other.getLabel(), e.getWeight());
                this.availableVertices.add(other);
            }
            this.visitedVertices.add(initialVertex);
            processGraph();

        }

        private void processGraph() {
            while (this.availableVertices.size() > 0) {
                Vertex next = this.availableVertices.poll();
                int distanceToNext = this.distances.get(next.getLabel());
                List<Edge> nextNeighbors = next.getNeighbors();
                for (Edge e : nextNeighbors) {
                    Vertex other = e.getNeighbor(next);
                    if (this.visitedVertices.contains(other)) {
                        continue;
                    }
                    int currentWeight = this.distances.get(other.getLabel());
                    int newWeight = distanceToNext + e.getWeight();

                    if (newWeight < currentWeight) {
                        this.predecessors.put(other.getLabel(), next.getLabel());
                        this.distances.put(other.getLabel(), newWeight);
                        this.availableVertices.remove(other);
                        this.availableVertices.add(other);
                    }
                }
                this.visitedVertices.add(next);
            }
        }

        public List<Vertex> getPathTo(String destinationLabel) {
            LinkedList<Vertex> path = new LinkedList<Vertex>();
            path.add(graph.getVertex(destinationLabel));

            while (!destinationLabel.equals(this.initialVertexLabel)) {
                Vertex predecessor = graph.getVertex(this.predecessors.get(destinationLabel));
                destinationLabel = predecessor.getLabel();
                path.add(0, predecessor);
            }
            return path;
        }

        public int getDistanceTo(String destinationLabel) {
            return this.distances.get(destinationLabel);
        }
    }

    public static ArrayList<String> getPairs(BufferedReader b) throws FileNotFoundException, IOException {
        ArrayList<String> s = new ArrayList<String>();
        String temp = b.readLine();
        temp = temp.replaceAll(",", "");
        temp = temp.replaceAll("\\s", "");
        for (int i = 0; i < temp.length(); i++) {
            if (i % 2 == 0) {
                char c1 = temp.charAt(i);
                char c2 = temp.charAt(i + 1);
                s.add(c1 + " " + c2);
            }
        }
        return s;
    }

    public static ArrayList<Integer> getLengths(BufferedReader b) throws FileNotFoundException, IOException {
        ArrayList<Integer> in = new ArrayList<Integer>();
        String temp = b.readLine();
        String[] sp = temp.split(",");
        for (int i = 0; i < sp.length; i++) {
            sp[i] = sp[i].replaceAll("\\s", "");
        }
        for (int i = 0; i < sp.length; i++) {
            in.add(Integer.parseInt(sp[i]));
        }
        return in;
    }

    public static String removeDuplicates(String s) {
        char[] c = s.toCharArray();
        boolean[] found = new boolean[256];
        StringBuilder sb = new StringBuilder();
        for (char ch : c) {
            if (!found[ch]) {
                found[ch] = true;
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanMain = new Scanner(System.in);
        System.out.println("Input File Name (without .txt extension):");
        String name = scanMain.nextLine();
        System.out.println("Input File Directory:\nWARNING: The output files will be printed to this directory.\nEXAMPLE: C:\\Users\\James\\Desktop\\Towson\\COSC336\\HW03_Waugh_James");
        String dir = scanMain.nextLine();
        File file = new File(dir + "\\" + name + ".txt");
        BufferedReader b = new BufferedReader(new FileReader(file));
        ArrayList<String> s = getPairs(b);
        ArrayList<Integer> in = getLengths(b);
        String temp = "";
        for (int i = 0; i < s.size(); i++) {
            temp += s.get(i);
        }
        temp = temp.replaceAll("\\s", "");
        temp = removeDuplicates(temp);
        Graph graph = new Graph();
        Vertex[] vertices = new Vertex[temp.length()];
        for (int i = 0; i < temp.length(); i++) {
            vertices[i] = new Vertex("" + temp.charAt(i));
            graph.addVertex(vertices[i], true);
        }
        Edge[] edges = new Edge[in.size()];
        for (int i = 0; i < in.size(); i++) {
            for (int j = 0; j < vertices.length; j++) {
                for (int k = 0; k < j; k++) {
                    if (s.get(i).contains(vertices[j].getLabel()) && s.get(i).contains(vertices[k].getLabel())) {
                        edges[i] = new Edge(vertices[j], vertices[k], in.get(i));
                    }
                }
            }
        }
        for (Edge e : edges) {
            graph.addEdge(e.getOne(), e.getTwo(), e.getWeight());
        }
        Dijkstra d = new Dijkstra(graph, "u");
        System.out.println("Beginning analysis\n");
        System.out.println(d.getDistanceTo("v"));
        System.out.println(d.getPathTo("v"));
        System.out.println("\nDone");
    }

}
