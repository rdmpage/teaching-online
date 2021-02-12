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
package fr.inria.bamboo.eucalypt.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.bamboo.eucalypt.graphs.Edge;
import fr.inria.bamboo.eucalypt.graphs.Graph;
import fr.inria.bamboo.eucalypt.graphs.Node;
import fr.inria.bamboo.eucalypt.trees.Tree;
import fr.inria.bamboo.eucalypt.utilities.Util;

public class Cleaner {

    /** Cyclicity test proposed in Stolzer et al., 2013. */
    public static final int STOLZER = 1;

    /** Cyclicity test proposed in Tofigh et al., 2012. */
    public static final int TOFIGH = 2;


    /**
     * Cleans the cyclic solution from the input file.
     * 
     * @param configuration
     *        Task configuration.
     * @throws IOException
     *         When something wrongs happens while writing or reading files.
     */
    public static void cleanCyclicSolutions(TaskConfiguration configuration) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(configuration.getInputFile()));
        FileWriter writer = new FileWriter(configuration.getOutputFile());

        boolean discardedPrevious = false;
        Graph hostTreeGraph = null;
        Graph parasiteTreeGraph = null;
        BigInteger totalNumberOfAcyclicSolutions = BigInteger.ZERO;
        BigInteger partialNumberOfAcyclicSolutions = BigInteger.ZERO;
        BigInteger totalNumberOfCyclicSolutions = BigInteger.ZERO;
        BigInteger partialNumberOfCyclicSolutions = BigInteger.ZERO;
        HashMap<ArrayList<Integer>, Integer> acyclicEventVectorCount = new HashMap<ArrayList<Integer>, Integer>();
        HashMap<ArrayList<Integer>, Integer> cyclicEventVectorCount = new HashMap<ArrayList<Integer>, Integer>();
        HashMap<ArrayList<Integer>, Integer> partialAcyclicEventVectorCount = new HashMap<ArrayList<Integer>, Integer>();
        HashMap<ArrayList<Integer>, Integer> partialCyclicEventVectorCount = new HashMap<ArrayList<Integer>, Integer>();
        String line = reader.readLine();
        boolean statistics = false;
        while (line != null) {
            if (line.startsWith("#")) {

                /* Comment line */
                if (line.startsWith("#Root mapping = ")) {
                    System.out.println("---------------------------------------------------------------------");
                    System.out.println(line.replaceAll("#", ""));
                    writer.write(line + "\n");
                    partialNumberOfAcyclicSolutions = BigInteger.ZERO;
                    partialAcyclicEventVectorCount = new HashMap<ArrayList<Integer>, Integer>();
                } else if (line.startsWith("#Number of solutions")) {

                    /* Print acyclic partial summary */
                    String message = "Number of solutions (acyclic) = "
                                     + partialNumberOfAcyclicSolutions;
                    System.out.println(message);

                    if (statistics)
                        Util.calculateAverage(partialAcyclicEventVectorCount,
                                              partialNumberOfAcyclicSolutions,
                                              "(acyclic)");

                    writer.write("#" + message + "\n");

                    /* Print cyclic partial summary */
                    System.out.println("----");
                    message = "Number of solutions (cyclic) = " + partialNumberOfCyclicSolutions;
                    System.out.println(message);

                    if (statistics)
                        Util.calculateAverage(partialCyclicEventVectorCount,
                                              partialNumberOfCyclicSolutions,
                                              "(cyclic)");

                    writer.write("#" + message + "\n");

                } else if (line.startsWith("#Total number of solutions =")) {

                    /* Print acyclic full summary */
                    System.out.println("---------------------------------------------------------------------");
                    String message = "Total number of solutions (acyclic) = "
                                     + totalNumberOfAcyclicSolutions;
                    System.out.println(message);

                    if (statistics)
                        Util.calculateAverage(acyclicEventVectorCount,
                                              totalNumberOfAcyclicSolutions,
                                              "(acyclic)");

                    writer.write("#" + message + "\n");

                    /* Print cyclic full summary */
                    System.out.println("----");
                    message = "Total number of solutions (cyclic) = "
                              + totalNumberOfCyclicSolutions;
                    System.out.println(message);

                    if (statistics)
                        Util.calculateAverage(cyclicEventVectorCount,
                                              totalNumberOfCyclicSolutions,
                                              "(cyclic)");

                    writer.write("#" + message + "\n");

                } else if (line.startsWith("#Number of sampled solutions = ")) {

                    /* Print acyclic full summary (sampled version) */
                    String message = "Number of sampled solutions (acyclic) = "
                                     + totalNumberOfAcyclicSolutions;
                    System.out.println(message);

                    if (statistics)
                        Util.calculateAverage(acyclicEventVectorCount,
                                              totalNumberOfAcyclicSolutions,
                                              "(acyclic)");

                    writer.write("#" + message + "\n");

                    /* Print cyclic full summary (sampled version) */
                    System.out.println("----");
                    message = "Number of sampled solutions (cyclic) = "
                              + totalNumberOfCyclicSolutions;
                    System.out.println(message);

                    if (statistics)
                        Util.calculateAverage(cyclicEventVectorCount,
                                              totalNumberOfCyclicSolutions,
                                              "(cyclic)");

                    writer.write("#" + message + "\n");

                } else if (line.startsWith("#Host tree          = ")) {
                    writer.write(line + "\n");
                    line = line.replaceAll("#Host tree          = ", "");
                    hostTreeGraph = new Graph(new Tree(line), true);
                } else if (line.startsWith("#Parasite tree      = ")) {
                    writer.write(line + "\n");
                    line = line.replaceAll("#Parasite tree      = ", "");
                    parasiteTreeGraph = new Graph(new Tree(line), true);
                } else {
                    writer.write(line + "\n");
                }

            } else if (line.startsWith("[")) {

                statistics = true;

                /* Event vector line. */
                String vector = line;
                vector = vector.replaceAll("[\\[\\] ]", "");
                String[] values = vector.split(",");
                ArrayList<Integer> eventVector = new ArrayList<Integer>();
                eventVector.add(0, Integer.parseInt(values[0]));
                eventVector.add(1, Integer.parseInt(values[1]));
                eventVector.add(2, Integer.parseInt(values[2]));
                eventVector.add(3, Integer.parseInt(values[3]));

                if (!discardedPrevious) {
                    /* Acyclic */
                    writer.write(line + "\n");
                    if (partialAcyclicEventVectorCount.containsKey(eventVector)) {
                        partialAcyclicEventVectorCount.put(eventVector,
                                                           partialAcyclicEventVectorCount.get(eventVector) + 1);
                    } else {
                        partialAcyclicEventVectorCount.put(eventVector, 1);
                    }
                    if (acyclicEventVectorCount.containsKey(eventVector)) {
                        acyclicEventVectorCount.put(eventVector,
                                                    acyclicEventVectorCount.get(eventVector) + 1);
                    } else {
                        acyclicEventVectorCount.put(eventVector, 1);
                    }
                } else {
                    /* Cyclic */
                    if (partialCyclicEventVectorCount.containsKey(eventVector)) {
                        partialCyclicEventVectorCount.put(eventVector,
                                                          partialCyclicEventVectorCount.get(eventVector) + 1);
                    } else {
                        partialCyclicEventVectorCount.put(eventVector, 1);
                    }
                    if (cyclicEventVectorCount.containsKey(eventVector)) {
                        cyclicEventVectorCount.put(eventVector,
                                                   cyclicEventVectorCount.get(eventVector) + 1);
                    } else {
                        cyclicEventVectorCount.put(eventVector, 1);
                    }
                }

            } else {

                /* Parasite tree to Host tree Mapping */
                if (!isCyclic(configuration.getCyclicityTest(),
                              hostTreeGraph,
                              parasiteTreeGraph,
                              parseAssociations(line))) {
                    partialNumberOfAcyclicSolutions = partialNumberOfAcyclicSolutions.add(BigInteger.ONE);
                    totalNumberOfAcyclicSolutions = totalNumberOfAcyclicSolutions.add(BigInteger.ONE);
                    discardedPrevious = false;
                    writer.write(line + "\n");
                    writer.flush();
                } else {
                    discardedPrevious = true;
                    partialNumberOfCyclicSolutions = partialNumberOfCyclicSolutions.add(BigInteger.ONE);
                    totalNumberOfCyclicSolutions = totalNumberOfCyclicSolutions.add(BigInteger.ONE);
                }

            }

            line = reader.readLine();
        }

        writer.flush();
        writer.close();
        reader.close();

    }


    /**
     * Returns true if the reconciliation results in a cyclic scenario.
     * 
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param mapping
     *        Reconciliation data.
     * @return True if the reconciliation results in a cyclic scenario and false, otherwise.
     */
    public static boolean isCyclic(int model,
                                   Graph hostTree,
                                   Graph parasiteTree,
                                   HashMap<String, String> mapping) {
        if (model == Cleaner.STOLZER) {
            return isCyclicStolzer(hostTree, parasiteTree, mapping);
        } else {
            return isCyclicTofigh(hostTree, parasiteTree, mapping);
        }
    }


    /**
     * Returns true if the reconciliation results in a cyclic scenario.
     * 
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param mapping
     *        Reconciliation data.
     * @return True if the reconciliation results in a cyclic scenario and false, otherwise.
     */
    public static boolean isCyclicStolzer(Graph hostTree,
                                          Graph parasiteTree,
                                          HashMap<String, String> mapping) {

        /* Creates an empty graph. */
        Graph graph = new Graph(false);

        /* Identify the transfer edges */
        ArrayList<Edge> transferEdges = new ArrayList<Edge>();
        for (Edge edge: parasiteTree.getEdges()) {
            Node u = edge.getSource();
            Node v = edge.getTarget();
            Node gammaU = hostTree.getNode(mapping.get(u.getLabel()));
            Node gammaV = hostTree.getNode(mapping.get(v.getLabel()));
            if (!hostTree.areComparable(gammaU, gammaV)) {
                /* Add the edge into the list of transfer edges. */
                transferEdges.add(edge);
                /* Add the correspondent nodes and their parents to the graph. */
                Node parentGammaU = gammaU.getParent();
                Node parentGammaV = gammaV.getParent();
                Node newGammaU = new Node(gammaU.getLabel());
                Node newGammaV = new Node(gammaV.getLabel());
                Node newParentGammaU = new Node(parentGammaU.getLabel());
                Node newParentGammaV = new Node(parentGammaV.getLabel());
                graph.addNode(newGammaU);
                graph.addNode(newGammaV);
                graph.addNode(newParentGammaU);
                graph.addNode(newParentGammaV);
                graph.addEdge(new Edge(newParentGammaU, newGammaU));
                graph.addEdge(new Edge(newParentGammaV, newGammaV));
            }
        }

        /* if there are no transfer edges, the graph is acyclic for sure. */
        if (transferEdges.size() == 0)
            return false;

        /* Condition 1 */
        for (Node node: graph.getNodes()) {
            Node hostTreeNode = hostTree.getNode(node.getLabel());
            ArrayList<Node> descendantsInHostTree = hostTree.getDescendantsInTree(hostTreeNode);
            descendantsInHostTree.remove(hostTreeNode);
            for (Node descendantInHostTree: descendantsInHostTree) {
                Node descendant = graph.getNode(descendantInHostTree.getLabel());
                if (descendant != null) {
                    graph.addEdge(new Edge(node, descendant));
                }
            }
        }

        /* Condition 2 */
        for (Edge edge1: transferEdges) {
            ArrayList<Node> descendants = parasiteTree.getDescendantsInTree(edge1.getSource());
            for (Edge edge2: transferEdges) {
                if (edge1 != edge2 && descendants.contains(edge2.getSource())) {
                    String dLabel = mapping.get(edge1.getSource().getLabel());
                    String rLabel = mapping.get(edge1.getTarget().getLabel());
                    String dPrimeLabel = mapping.get(edge2.getSource().getLabel());
                    String rPrimeLabel = mapping.get(edge2.getTarget().getLabel());
                    Node dParentHostTree = hostTree.getNode(dLabel).getParent();
                    Node rParentHostTree = hostTree.getNode(rLabel).getParent();

                    Node dParent = graph.getNode(dParentHostTree.getLabel());
                    Node rParent = graph.getNode(rParentHostTree.getLabel());
                    Node dPrime = graph.getNode(dPrimeLabel);
                    Node rPrime = graph.getNode(rPrimeLabel);

                    graph.addEdge(new Edge(dParent, dPrime));
                    graph.addEdge(new Edge(dParent, rPrime));
                    graph.addEdge(new Edge(rParent, dPrime));
                    graph.addEdge(new Edge(rParent, rPrime));
                }
            }
        }

        /* Condition 3 */
        for (Edge edge: transferEdges) {
            Node g = edge.getSource();
            Node h = edge.getTarget();
            /* (g,h) = (sk,sj) */
            createEdgesForCriterium3(graph,
                                     hostTree,
                                     mapping.get(g.getLabel()),
                                     mapping.get(h.getLabel()));
            /* (g,h) = (sj,sk) */
            createEdgesForCriterium3(graph,
                                     hostTree,
                                     mapping.get(h.getLabel()),
                                     mapping.get(g.getLabel()));
        }

        return graph.isCyclicDirected();
    }


    /**
     * Create edges for the criterium 3 of Stolzer's test.
     * 
     * @param graph
     *        Graph to cyclicity test.
     * @param hostTree
     *        Host tree graph.
     * @param sk_label
     *        Label of node sk.
     * @param sj_label
     *        Label of node sj.
     */
    private static void createEdgesForCriterium3(Graph graph,
                                                 Graph hostTree,
                                                 String sk_label,
                                                 String sj_label) {
        Node sk_hostTree = hostTree.getNode(sk_label);
        Node sj = graph.getNode(sj_label);
        ArrayList<Node> ancestorsInHostTree = hostTree.getAncestorsInTree(sk_hostTree);
        ancestorsInHostTree.remove(sk_hostTree);
        for (Node si_hostTree: ancestorsInHostTree) {
            Node si = graph.getNode(si_hostTree.getLabel());
            if (si != null) {
                graph.addEdge(new Edge(si, sj));
            }
        }
    }


    /**
     * Returns true if the reconciliation results in a cyclic scenario.
     * 
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param mapping
     *        Reconciliation data.
     * @return True if the reconciliation results in a cyclic scenario and false, otherwise.
     */
    public static boolean isCyclicBea(Graph hostTree,
                                      Graph parasiteTree,
                                      HashMap<String, String> mapping) {

        /* Build the graph */
        Graph graph = new Graph(hostTree, false);

        /* Identify the transfer edges */
        ArrayList<Edge> transferEdges = new ArrayList<Edge>();
        for (Edge edge: parasiteTree.getEdges()) {
            Node u = edge.getSource();
            Node v = edge.getTarget();
            Node gammaU = hostTree.getNode(mapping.get(u.getLabel()));
            Node gammaV = hostTree.getNode(mapping.get(v.getLabel()));
            if (!hostTree.areComparable(gammaU, gammaV)) {
                transferEdges.add(edge);
            }
        }

        /* if there are no transfer edges, the graph is acyclic for sure. */
        if (transferEdges.size() == 0)
            return false;

        /* Declaring auxiliary variables. */
        Node u = null;
        Node v = null;
        String gammaU_label = null;
        String gammaV_label = null;
        Node gammaU_hostTree = null;
        Node gammaV_hostTree = null;
        String pGammaU_label = null;
        String pGammaV_label = null;
        Node pGammaU = null;
        Node pGammaV = null;
        Node u_prime = null;
        Node v_prime = null;
        String gammaU_prime_label = null;
        String gammaV_prime_label = null;
        Node gammaU_prime = null;
        Node gammaV_prime = null;

        /* Compare all pairs of transfer edges. */
        for (Edge edge1: transferEdges) {

            ArrayList<Node> descendants = parasiteTree.getDescendantsInTree(edge1.getSource());

            u = null;
            v = null;
            gammaU_label = null;
            gammaV_label = null;
            gammaU_hostTree = null;
            gammaV_hostTree = null;
            pGammaU_label = null;
            pGammaV_label = null;
            pGammaU = null;
            pGammaV = null;

            for (Edge edge2: parasiteTree.getEdges()) {

                boolean addArcs = edge1 == edge2;
                if (!addArcs) {
                    addArcs = descendants.contains(edge2.getSource());
                }

                if (addArcs) {

                    if (u == null) {
                        /* Get nodes on the parasite tree. */
                        /* (u,v) */
                        u = edge1.getSource();
                        v = edge1.getTarget();

                        /* Get the image labels. */
                        /* (gamma(u),gamma(v)) */
                        gammaU_label = mapping.get(u.getLabel());
                        gammaV_label = mapping.get(v.getLabel());

                        /* Get the nodes on the host tree. */
                        gammaU_hostTree = hostTree.getNode(gammaU_label);
                        gammaV_hostTree = hostTree.getNode(gammaV_label);

                        /* Identify the labels of the parents of hU and hV. */
                        /* Parent(gamma(u)) */
                        pGammaU_label = gammaU_hostTree.getParent().getLabel();
                        /* Parent(gamma(v)) */
                        pGammaV_label = gammaV_hostTree.getParent().getLabel();

                        /* Now, get the node on the transfer graph. */
                        pGammaU = graph.getNode(pGammaU_label);
                        pGammaV = graph.getNode(pGammaV_label);
                    }

                    /* Get nodes on the parasite tree. */
                    /* (u',v') */
                    u_prime = edge2.getSource();
                    v_prime = edge2.getSource();

                    /* Get the image labels. */
                    /* (gamma(u'),gamma(v')) */
                    gammaU_prime_label = mapping.get(u_prime.getLabel());
                    gammaV_prime_label = mapping.get(v_prime.getLabel());

                    /* Now, get the node on the transfer graph. */
                    gammaU_prime = graph.getNode(gammaU_prime_label);
                    gammaV_prime = graph.getNode(gammaV_prime_label);

                    /* Create the edges to add to the graph */
                    /* (P(gamma(u)),gamma(u')) */
                    Edge e1 = new Edge(pGammaU, gammaU_prime);
                    graph.addEdge(e1);

                    /* (P(gamma(u)),gamma(v')) */
                    Edge e2 = new Edge(pGammaU, gammaV_prime);
                    graph.addEdge(e2);

                    /* (P(gamma(v)),gamma(u')) */
                    Edge e3 = new Edge(pGammaV, gammaU_prime);
                    graph.addEdge(e3);

                    /* (P(gamma(v)),gamma(v')) */
                    Edge e4 = new Edge(pGammaV, gammaV_prime);
                    graph.addEdge(e4);

                }
            }
        }
        return graph.isCyclicDirected();
    }


    /**
     * Returns true if the reconciliation results in a cyclic scenario.
     * 
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param mapping
     *        Reconciliation data.
     * @return True if the reconciliation results in a cyclic scenario and false, otherwise.
     */
    public static boolean isCyclicTofigh(Graph hostTree,
                                         Graph parasiteTree,
                                         HashMap<String, String> mapping) {

        /* Build the graph */
        Graph graph = new Graph(hostTree, false);

        /* Identify the transfer edges */
        ArrayList<Edge> transferEdges = new ArrayList<Edge>();
        for (Edge edge: parasiteTree.getEdges()) {
            Node u = edge.getSource();
            Node v = edge.getTarget();
            Node gammaU = hostTree.getNode(mapping.get(u.getLabel()));
            Node gammaV = hostTree.getNode(mapping.get(v.getLabel()));
            if (!hostTree.areComparable(gammaU, gammaV)) {
                transferEdges.add(edge);
            }
        }

        /* if there are no transfer edges, the graph is acyclic for sure. */
        if (transferEdges.size() == 0)
            return false;

        /* Compare all pairs of transfer edges. */
        for (Edge edge1: transferEdges) {
            ArrayList<Node> descendants = parasiteTree.getDescendantsInTree(edge1.getSource());
            for (Edge edge2: transferEdges) {
                if (descendants.contains(edge2.getSource())) {
                    /* edge1 = (u,v) */
                    String gammaU_label = mapping.get(edge1.getSource().getLabel());

                    /* edge2 = (u',v') */
                    String gammaVprime_label = mapping.get(edge2.getTarget().getLabel());

                    /* Discover the label of the parent of gamma(u) */
                    String parentGammaU_label = hostTree.getNode(gammaU_label).getParent().getLabel();

                    /* Add into the graph an edge from (P(gamma(u)), gamma(v')) */
                    Node newSource = graph.getNode(parentGammaU_label);
                    Node newTarget = graph.getNode(gammaVprime_label);
                    graph.addEdge(new Edge(newSource, newTarget));
                }
            }
        }

        return graph.isCyclicDirected();
    }


    /**
     * Extracts the associations from the reconciliation string.
     * 
     * @param reconciliationString
     *        Reconciliation string.
     * @return A HashMap containing the associations which were extracted from the reconciliation
     *         string.
     */
    private static HashMap<String, String> parseAssociations(String reconciliationString) {
        HashMap<String, String> mapString = new HashMap<String, String>();
        String[] associations = reconciliationString.split(", ");
        for (String association: associations) {
            String[] split = association.split("@");
            mapString.put(split[0], split[1]);
        }
        return mapString;
    }

}
