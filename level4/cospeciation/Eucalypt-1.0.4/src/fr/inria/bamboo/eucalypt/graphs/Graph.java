/*
############################################################################
#                             EUCALYPT                                     #
#              Efficient tree reconciliation enumerator                    #
############################################################################
#                                                                          #
# Copyright INRIA 2013                                                     #
#                                                                          #
# Contributors : Beatrice Donati                                           #
#                Christian Baudet                                          #
#                Blerina Sinaimeri                                         #
#                Pierluigi Crescenzi                                       #
#                Marie-France Sagot                                        #
#                                                                          #
# christian.baudet@inria.fr                                                #
# marie-france.sagot@inria.fr                                              #
# https://gforge.inria.fr/forum/forum.php?forum_id=11335                   #
#                                                                          #
# This software is a computer program whose purpose is to enumerate        #
# solutions for the co-phylogenetic reconcilation of a pair of host and    #
# parasite trees considering co-speciation, duplication, host-switch, and  #
# loss events. The program is able to enumerate solutions, generate        #
# statistics about event vectors, perform a random sampling of the space   #
# of solutions and extract acyclic solutions from a set of previously      #
# enumerated solutions.                                                    #
#                                                                          #
# This software is governed by the CeCILL  license under French law and    #
# abiding by the rules of distribution of free software.  You can  use,    # 
# modify and/ or redistribute the software under the terms of the CeCILL   #
# license as circulated by CEA, CNRS and INRIA at the following URL        #
# "http://www.cecill.info".                                                # 
#                                                                          #
# As a counterpart to the access to the source code and  rights to copy,   #
# modify and redistribute granted by the license, users are provided only  #
# with a limited warranty  and the software's author,  the holder of the   #
# economic rights,  and the successive licensors  have only  limited       #
# liability.                                                               #
#                                                                          #
# In this respect, the user's attention is drawn to the risks associated   #
# with loading,  using,  modifying and/or developing or reproducing the    #
# software by the user in light of its specific status of free software,   #
# that may mean  that it is complicated to manipulate,  and  that  also    #
# therefore means  that it is reserved for developers  and  experienced    #
# professionals having in-depth computer knowledge. Users are therefore    #
# encouraged to load and test the software's suitability as regards their  #
# requirements in conditions enabling the security of their systems and/or #
# data to be ensured and,  more generally, to use and operate it in the    # 
# same conditions as regards security.                                     #
#                                                                          #
# The fact that you are presently reading this means that you have had     #
# knowledge of the CeCILL license and that you accept its terms.           #
############################################################################
 */
package fr.inria.bamboo.eucalypt.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import fr.inria.bamboo.eucalypt.trees.Tree;
import fr.inria.bamboo.eucalypt.trees.TreeNode;

public class Graph {

    /** Indicate if the graph represents a tree (or a forest) . */
    private boolean isTree;

    /** List of nodes of this graph. */
    private ArrayList<Node> nodes;

    /** List of edges of this graph. */
    private ArrayList<Edge> edges;

    /** HashMap which keeps the mapping label -> node object. */
    private HashMap<String, Node> labelToNode;

    /** HashMap which keep the mapping source -> targets. */
    private HashMap<String, HashSet<String>> sourceToTargets;


    /**
     * Creates an empty graph.
     * 
     * @param isTree
     *        If true, this graph will represent a tree structure (or a forest of trees).
     */
    public Graph(boolean isTree) {
        this.isTree = isTree;
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        labelToNode = new HashMap<String, Node>();
        sourceToTargets = new HashMap<String, HashSet<String>>();
    }


    /**
     * Creates a graph that is a copy of the graph passed by argument.
     * 
     * @param graph
     *        Graph to be copied.
     * @param isTree
     *        If true, this graph will represent a tree structure (or a forest of trees).
     */
    public Graph(Graph graph, boolean isTree) {
        this.isTree = isTree;
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        labelToNode = new HashMap<String, Node>();
        sourceToTargets = new HashMap<String, HashSet<String>>();
        for (Node node: graph.getNodes()) {
            Node newNode = new Node(node.getLabel());
            this.addNode(newNode);
        }
        for (Edge edge: graph.getEdges()) {
            Edge newEdge = new Edge(getNode(edge.getSource().getLabel()),
                                    getNode(edge.getTarget().getLabel()));
            this.addEdge(newEdge);
        }
    }


    /**
     * Creates a graph that represents the tree passed by argument.
     * 
     * @param tree
     *        Tree which will be used as a base for the new graph.
     * @param isTree
     *        If true, this graph will represent a tree structure (or a forest of trees).
     */
    public Graph(Tree tree, boolean isTree) {
        this.isTree = isTree;
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        labelToNode = new HashMap<String, Node>();
        sourceToTargets = new HashMap<String, HashSet<String>>();
        /* Tree implements a post-order iterator for its nodes. */
        for (TreeNode node: tree) {
            if (node.isLeaf()) {
                Node newNode = new Node(node.getLabel());
                addNode(newNode);
            } else {
                Node newNode = new Node(node.getLabel());
                Node newC1 = getNode(node.getChild(0).getLabel());
                Node newC2 = getNode(node.getChild(1).getLabel());
                addNode(newNode);
                Edge e1 = new Edge(newNode, newC1);
                Edge e2 = new Edge(newNode, newC2);
                addEdge(e1);
                addEdge(e2);
            }
        }
    }


    /**
     * Add a new edge to the graph
     * 
     * @param edge
     *        New edge to be added.
     */
    public void addEdge(Edge edge) {

        if (!nodes.contains(edge.getSource()) || !nodes.contains(edge.getTarget()))
            throw new RuntimeException("Edge between non-existent nodes.");

        HashSet<String> targets = sourceToTargets.get(edge.getSource().getLabel());
        if (targets == null) {
            targets = new HashSet<String>();
            sourceToTargets.put(edge.getSource().getLabel(), targets);
        }

        if (!targets.contains(edge.getTarget().getLabel())) {
            edges.add(edge);
            edge.getSource().addNodeIntoTheOutNeighborhood(edge.getTarget());
            edge.getTarget().addNodeIntoTheInNeighborhood(edge.getSource());
            if (isTree && edge.getTarget().getInNeighborhood().size() > 1)
                throw new RuntimeException("The graph represents a tree and a node cannot have in-degree higher than 1.");
            targets.add(edge.getTarget().getLabel());
        }

    }


    /**
     * Add a new node to this graph.
     * 
     * @param node
     *        New node.
     */
    public void addNode(Node node) {
        if (!this.labelToNode.containsKey(node.getLabel())) {
            this.nodes.add(node);
            this.labelToNode.put(node.getLabel(), node);
        }
    }


    /**
     * Returns the node of the graph that has the identifier passed by argument.
     * 
     * @param label
     *        Node label.
     * @return The node of the graph that has the label passed by argument or null if no node with
     *         the give identifier could be found.
     */
    public Node getNode(String label) {
        return labelToNode.get(label);
    }


    /**
     * Returns the list of nodes of the graph.
     * 
     * @return The list of nodes of the graph.
     */
    public ArrayList<Node> getNodes() {
        return nodes;
    }


    /**
     * Returns the list of edges of the graph.
     * 
     * @return The list of edges of the graph.
     */
    public ArrayList<Edge> getEdges() {
        return edges;
    }


    /**
     * Returns the edge object that have the source and target nodes which were passed by argument.
     * 
     * @param source
     *        Edge source node.
     * @param target
     *        Edge target node.
     * @return The edge object that have the source and target nodes which were passed by argument.
     */
    public Edge getEdgeByST(Node source, Node target) {
        for (Edge edge: edges) {
            if (edge.getSource().getLabel().equals(source.getLabel())
                && edge.getTarget().getLabel().equals(target.getLabel())) {
                return edge;
            }
        }
        return null;
    }


    /**
     * Returns a string representation of this graph with partial information.
     * 
     * @return A string representation of this graph with partial information.
     */
    public String toString() {
        return "Nodes(" + nodes.size() + ")=" + nodes.toString() + "\n" + "Edges(" + edges.size()
               + ")=" + edges.toString();
    }


    /**
     * Returns true if the graph has a cycle (considering the edge directions) and false, otherwise.
     * 
     * @return True if the graph has a cycle (considering the edge directions) and false, otherwise.
     */
    public boolean isCyclicDirected() {
        boolean toReturn = false;
        for (int i = 0; i < nodes.size(); ++i) {
            Node n = nodes.get(i);
            if (findCycle(n)) {
                toReturn = true;
                break;
            }
        }
        return toReturn;
    }


    /**
     * Auxiliary function for the cycle detection procedure.
     * 
     * @param node
     *        Node.
     * @return True if a cycle is detected.
     */
    private boolean findCycle(Node n) {
        ArrayList<Node> grey = new ArrayList<Node>();
        grey.add(n);
        ArrayList<Node> black = new ArrayList<Node>();
        Node current = n;
        while (!grey.isEmpty()) {
            Node next = null;
            ArrayList<Node> set = current.getOutNeighborhood();
            for (Node k: set) {
                if (!black.contains(k) && !(k.getDegree() == 1)) {
                    next = k;
                }
            }
            if (next == null) {
                black.add(current);
                grey.remove(current);
                if (grey.isEmpty()) {
                    break;
                } else {
                    current = grey.get(grey.size() - 1);
                }
            } else if (grey.contains(next)) {
                return true;

            } else {
                grey.add(next);
                current = next;
            }
        }
        return false;
    }


    /**
     * Returns the list of all descendants of the node in the tree.
     * 
     * @param node
     *        Subtree root node.
     * @return The list of all descendants of the node in the tree.
     */
    public ArrayList<Node> getDescendantsInTree(Node node) {
        if (!isTree)
            throw new RuntimeException("This method is available only for tree graphs.");
        ArrayList<Node> list = new ArrayList<Node>();
        list.add(node);
        for (Node d: node.getOutNeighborhood()) {
            list.addAll(getDescendantsInTree(d));
        }
        return list;
    }


    /**
     * Returns the list of all ancestors of the node in the tree.
     * 
     * @param node
     *        Subtree root node.
     * @return The list of all ancestors of the node in the tree.
     */
    public ArrayList<Node> getAncestorsInTree(Node node) {

        if (!isTree)
            throw new RuntimeException("This method is available only for tree graphs.");

        ArrayList<Node> list = new ArrayList<Node>();
        list.add(node);

        Node aux = node.getParent();
        while (aux != null) {
            list.add(aux);
            aux = aux.getParent();
        }
        return list;

    }


    /**
     * Returns true if both node are comparable.
     * 
     * @param n1
     *        Node 1.
     * @param n2
     *        Node 2.
     * @return True if both node are comparable and false, otherwise.
     */
    public boolean areComparable(Node n1, Node n2) {
        if (!isTree)
            throw new RuntimeException("This method is available only for tree graphs.");
        ArrayList<Node> descendantsN1 = getDescendantsInTree(n1);
        if (descendantsN1.contains(n2))
            return true;
        return getDescendantsInTree(n2).contains(n1);
    }

}
