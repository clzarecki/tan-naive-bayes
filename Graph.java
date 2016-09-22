import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class Graph {

	private HashMap<String, Node> graph = new HashMap<String, Node>();
	
	public void addNode(Attribute attr) {
		Node node = new Node(attr, new LinkedList<Node>());
		graph.put(attr.toString(), node);
	}
	
	/**
	 * Adds a new node with the given attribute and its adjacent attributes,
	 * adding if edges or nodes not already present
	 * @param attr the attribute to add
	 * @param edges the attributes adjacent attributes
	 */
	public void addEdge(Attribute a, Attribute b) {
		Node aNode = graph.get(a.toString());
		Node bNode = graph.get(b.toString());
		
		// create node if it doesn't exist
		if(aNode == null) {
			aNode = new Node(a, new LinkedList<Node>());
			graph.put(a.toString(), aNode);
		}
		if(bNode == null) {
			bNode = new Node(b, new LinkedList<Node>());
			graph.put(b.toString(), bNode);
		}
		
		// add edge between them
		aNode.adjacencyList.add(bNode);
		bNode.adjacencyList.add(aNode);
	}
	
	/**
	 * Returns a list of the adjacent attributes
	 * @param attr the attribute to get adjacent attributes from
	 * @return the list of adjacent attributes, null if attribute doesn't exist
	 */
	public List<Attribute> adjacentEdges(Attribute attr) {
		Node currNode = graph.get(attr.toString());
		if(currNode == null) return null;
		
		// add all attributes in adjacency list
		List<Attribute> adjAttrs = new ArrayList<Attribute>(currNode.adjacencyList.size());
		for(Node n : currNode.adjacencyList) {
			adjAttrs.add(n.value);
		}
		
		return adjAttrs;
	}
	
//	public void printEdges() {
//		Node root = graph.get(TBMain.attrs.get(0).toString());
//		printNode(root, null);
//	}
//	
//	private void printNode(Node cur, Node parent) {
//		for(Node child : cur.adjacencyList) {
//			if(!child.equals(parent)) {
//				int curIndex = TBMain.attrs.indexOf(cur.value);
//				int childIndex = TBMain.attrs.indexOf(child.value);
//				System.out.print("(" + curIndex + "," + childIndex + ") ");
//				System.out.println(cur.value.toString() + " " + child.value.toString());
//				printNode(child, cur);
//			}
//		}
//	}
	
	public List<int[]> treeEdges() {
		List<int[]> edges = new ArrayList<int[]>();
		addEdges(edges, graph.get(TNMain.attrs.get(0).toString()), null);
		return edges;
	}
	
	private void addEdges(List<int[]> edges, Node cur, Node parent) {
		for(Node child : cur.adjacencyList) {
			if(!child.equals(parent)) {
				int curIndex = TNMain.attrs.indexOf(cur.value);
				int childIndex = TNMain.attrs.indexOf(child.value);
				int[] edge = new int[2];
				edge[0] = curIndex;
				edge[1] = childIndex;
				edges.add(edge);
				addEdges(edges, child, cur);
			}
		}
	}
	
	public void deleteEdge(Attribute a, Attribute b) {
		Node nodeA = graph.get(a.toString());
		Node nodeB = graph.get(b.toString());
		if(nodeA != null) {
			nodeA.adjacencyList.remove(nodeB);
		}
		if(nodeB != null) {
			nodeB.adjacencyList.remove(nodeA);
		}
	}
	
	class Node {
		
		Attribute value;
		List<Node> adjacencyList;
		
		/**
		 * @param value attribute
		 * @param adjacencyList list of nodes
		 */
		public Node(Attribute value, List<Node> adjacencyList) {
			super();
			this.value = value;
			this.adjacencyList = adjacencyList;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Node)) return false;
			Node n = (Node) obj;
			return this.value.equals(n.value);
		}
		
	}
	
}
