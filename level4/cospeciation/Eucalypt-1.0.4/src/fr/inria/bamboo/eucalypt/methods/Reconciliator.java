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
package fr.inria.bamboo.eucalypt.methods;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.bamboo.eucalypt.methods.NestedSolution.Composition;
import fr.inria.bamboo.eucalypt.methods.NestedSolution.Event;
import fr.inria.bamboo.eucalypt.trees.Tree;
import fr.inria.bamboo.eucalypt.trees.TreeNode;
import fr.inria.bamboo.eucalypt.utilities.Association;

public class Reconciliator {

    /** Matrix D. */
    private NestedSolution[][] mainMatrix;

    /** Matrix D_ST. */
    private NestedSolution[][] subtreeMatrix;

    /** Host tree. */
    private Tree host;

    /** Parasite tree. */
    private Tree parasite;

    /** Leaf mapping. */
    private Map<TreeNode, TreeNode> leafMap;

    /** Nodes of the host tree in post-order. */
    private Map<TreeNode, Integer> hostNodesPostOrder;

    /** Nodes of the parasite tree in post-order. */
    private Map<TreeNode, Integer> parasiteNodesPostOrder;

    /** Co-speciation cost. */
    private long speciationCost;

    /** Duplication cost. */
    private long duplicationCost;

    /** Host-Switch cost. */
    private long transferCost;

    /** Loss cost. */
    private long lossCost;

    /** Distance threshold. */
    private long distanceThreashold;

    /** If true, count the number of solutions while filling the matrix. */
    private boolean countWhileFillingMatrix;

    /** Map of allowed host-switches. */
    private Map<TreeNode, List<Entry<TreeNode, Long>>> allowedTransfersMap;


    /**
     * Create a reconciliation enumerator with the given parameters.
     * 
     * @param host
     *        Host tree.
     * @param parasite
     *        Parasite tree.
     * @param leafMap
     *        Leaf mapping.
     * @param speciationCost
     *        Co-speciation cost.
     * @param duplicationCost
     *        Duplication cost.
     * @param transferCost
     *        Host-switch cost.
     * @param lossCost
     *        Loss cost.
     * @param distanceThreashold
     *        Distance threshold.
     * @param countWhileFillingMatrix
     *        If true, count the number of solutions while filling the matrix.
     */
    public Reconciliator(Tree host,
                         Tree parasite,
                         Map<TreeNode, TreeNode> leafMap,
                         long speciationCost,
                         long duplicationCost,
                         long transferCost,
                         long lossCost,
                         long distanceThreashold,
                         boolean countWhileFillingMatrix) {
        this.host = host;
        this.parasite = parasite;
        this.leafMap = leafMap;
        this.speciationCost = speciationCost;
        this.duplicationCost = duplicationCost;
        this.transferCost = transferCost;
        this.lossCost = lossCost;
        this.distanceThreashold = distanceThreashold;
        this.allowedTransfersMap = new HashMap<TreeNode, List<Entry<TreeNode, Long>>>();
        this.countWhileFillingMatrix = countWhileFillingMatrix;
    }


    /**
     * Runs the reconciliation algorithm (fill up the main matrix).
     */
    private void runReconciliationAlgorithm() {

        double optimum = Double.POSITIVE_INFINITY;
        initializeMatrices();
        initializedPostOrders();
        initializeLeavesCosts();

        for (TreeNode parasiteNode: parasite) {

            if (!parasiteNode.isLeaf()) {

                int parasiteIndex = parasiteNodesPostOrder.get(parasiteNode);
                int parasiteLeftChildIndex = parasiteNodesPostOrder.get(parasiteNode.getChild(0));
                int parasiteRightChildIndex = parasiteNodesPostOrder.get(parasiteNode.getChild(1));

                for (TreeNode hostNode: host) {

                    int hostIndex = hostNodesPostOrder.get(hostNode);
                    Association currentAssociation = new Association(parasiteNode, hostNode);

                    NestedSolution candidate;
                    if (hostNode.isLeaf()) {

                        /* H is a leaf node */
                        NestedSolution duplicationSol = computeDuplicationLeaf(parasiteIndex,
                                                                               parasiteLeftChildIndex,
                                                                               parasiteRightChildIndex,
                                                                               hostIndex,
                                                                               currentAssociation);
                        NestedSolution transferSol = computeTrSolution(parasiteLeftChildIndex,
                                                                       parasiteRightChildIndex,
                                                                       hostIndex,
                                                                       hostNode,
                                                                       currentAssociation);

                        /* Choose a set of solutions */
                        candidate = topSolutions(countWhileFillingMatrix,
                                                 duplicationSol,
                                                 transferSol);

                        /* Adjourn the matrices */
                        if (candidate.getCost() > optimum)
                            candidate = new NestedSolution();

                        subtreeMatrix[parasiteIndex][hostIndex] = candidate;
                        mainMatrix[parasiteIndex][hostIndex] = candidate;

                    } else {

                        /* h is an inner node */
                        int hostRightCIndex = hostNodesPostOrder.get(hostNode.getChild(1));
                        int hostLeftCIndex = hostNodesPostOrder.get(hostNode.getChild(0));
                        NestedSolution speciationSol = computeSpeciationSol(parasiteLeftChildIndex,
                                                                            parasiteRightChildIndex,
                                                                            hostLeftCIndex,
                                                                            hostRightCIndex,
                                                                            currentAssociation);
                        NestedSolution duplicationSol = computeDuSolution(parasiteIndex,
                                                                          parasiteLeftChildIndex,
                                                                          parasiteRightChildIndex,
                                                                          hostIndex,
                                                                          hostLeftCIndex,
                                                                          hostRightCIndex,
                                                                          currentAssociation);
                        NestedSolution transferSol = computeTrSolution(parasiteLeftChildIndex,
                                                                       parasiteRightChildIndex,
                                                                       hostIndex,
                                                                       hostNode,
                                                                       currentAssociation);

                        candidate = topSolutions(countWhileFillingMatrix,
                                                 speciationSol,
                                                 duplicationSol,
                                                 transferSol);

                        /* Adjourn the matrices */
                        if (candidate.getCost() > optimum)
                            candidate = new NestedSolution();

                        mainMatrix[parasiteIndex][hostIndex] = candidate;
                        NestedSolution opt1 = candidate;
                        NestedSolution opt2 = NestedSolution.addLosses(lossCost,
                                                                       1,
                                                                       subtreeMatrix[parasiteIndex][hostRightCIndex],
                                                                       countWhileFillingMatrix);
                        NestedSolution opt3 = NestedSolution.addLosses(lossCost,
                                                                       1,
                                                                       subtreeMatrix[parasiteIndex][hostLeftCIndex],
                                                                       countWhileFillingMatrix);
                        subtreeMatrix[parasiteIndex][hostIndex] = topSolutions(countWhileFillingMatrix,
                                                                               opt1,
                                                                               opt2,
                                                                               opt3);

                    }

                }

                cleanTables(parasiteLeftChildIndex);
                cleanTables(parasiteRightChildIndex);

            }

        }

    }


    /**
     * Runs the reconcilation algorithm and returns the row which contains all mappings for the
     * parasite tree root node.
     * 
     * @return All optimum mappings for the parasite tree root node.
     */
    public NestedSolution[] runReconciliator() {

        runReconciliationAlgorithm();

        NestedSolution[] parasiteRootRow = mainMatrix[mainMatrix.length - 1];

        long minimumCost = Long.MAX_VALUE;
        for (NestedSolution nestedSolution: parasiteRootRow) {
            long cost = nestedSolution.getCost();
            if (minimumCost > cost) {
                minimumCost = cost;
            }
        }

        ArrayList<NestedSolution> toReturn = new ArrayList<NestedSolution>();
        for (NestedSolution nestedSolution: parasiteRootRow) {
            long cost = nestedSolution.getCost();
            if (minimumCost == cost) {
                toReturn.add(nestedSolution);
            }
        }

        return toReturn.toArray(new NestedSolution[0]);
    }


    /**
     * Runs the reconcilation algorithm and returns the cell (NestedSolution) which is related to
     * the root-to-root mapping of host and parasite trees.
     * 
     * @return The cell (NestedSolution) which is related to the root-to-root mapping of host and
     *         parasite trees.
     */
    public NestedSolution runRootToRootReconciliator() {
        runReconciliationAlgorithm();
        int rootPindex = parasiteNodesPostOrder.get(parasite.getRoot());
        int rootHindex = hostNodesPostOrder.get(host.getRoot());
        return mainMatrix[rootPindex][rootHindex];
    }


    /**
     * Auxiliary function: Returns all transfer targets which are with a maximum transfer distance.
     * 
     * @param hostNode
     *        Base host node.
     * @return All transfer targets which are with a maximum transfer distance.
     */
    private List<Entry<TreeNode, Long>> getAllowedTransfers(TreeNode hostNode) {

        List<Entry<TreeNode, Long>> result = allowedTransfersMap.get(hostNode);

        if (result == null) {

            /* computes and memorizes the first time we calculate this jumps. */
            result = new ArrayList<Entry<TreeNode, Long>>();

            if (hostNode != host.getRoot()) {

                /* from the root you cannot jump */
                TreeNode target = hostNode.getSibling();
                long distance = 2;
                while (distance <= distanceThreashold) {

                    TreeNode previous = null;
                    TreeNode next = null;

                    /* iterate over the subtree with distance bound. */
                    while (target != next) {
                        if (previous == null) {
                            next = target;
                            while (!next.isLeaf() && distance < distanceThreashold) {
                                next = next.getChild(0);
                                ++distance;
                            }
                        } else {
                            next = previous.getParent();
                            --distance;
                            if (previous == next.getChild(0) && distance <= distanceThreashold) {
                                next = next.getChild(1);
                                ++distance;
                                while (!next.isLeaf() && distance <= distanceThreashold) {
                                    next = next.getChild(0);
                                    ++distance;
                                }
                            }
                        }
                        previous = next;
                        result.add(new AbstractMap.SimpleEntry<TreeNode, Long>(next, distance));
                    }

                    if (target.getParent().isRoot()) {
                        break;
                    }
                    target = target.getParent().getSibling();
                    ++distance;

                }

                allowedTransfersMap.put(hostNode, result);

            }

        }

        return result;

    }


    /**
     * Auxiliary function: Initialise the matrices.
     */
    private void initializeMatrices() {
        mainMatrix = new NestedSolution[parasite.size()][host.size()];
        for (int row = 0; row < mainMatrix.length; ++row) {
            for (int column = 0; column < mainMatrix[row].length; ++column) {
                mainMatrix[row][column] = new NestedSolution();
            }
        }
        subtreeMatrix = new NestedSolution[parasite.size()][host.size()];
        for (int row = 0; row < subtreeMatrix.length; ++row) {
            for (int column = 0; column < subtreeMatrix[row].length; ++column) {
                subtreeMatrix[row][column] = new NestedSolution();
            }
        }
    }


    /**
     * Auxiliary function: Initialise hash maps which store the post order traversal of host and
     * parasite trees.
     */
    private void initializedPostOrders() {
        hostNodesPostOrder = new HashMap<TreeNode, Integer>(host.size());
        int index = 0;
        for (TreeNode hostNode: host) {
            hostNodesPostOrder.put(hostNode, index++);
        }
        parasiteNodesPostOrder = new HashMap<TreeNode, Integer>(parasite.size());
        index = 0;
        for (TreeNode parasiteNode: parasite) {
            parasiteNodesPostOrder.put(parasiteNode, index++);
        }

    }


    /**
     * Auxiliary function: Initialise the cost of the cells which are related to the mapping of
     * leaves.
     */
    private void initializeLeavesCosts() {

        for (Entry<TreeNode, TreeNode> mapping: leafMap.entrySet()) {

            TreeNode parasiteLeaf = mapping.getKey();
            TreeNode hostLeaf = mapping.getValue();

            int row = parasiteNodesPostOrder.get(parasiteLeaf);
            int column = hostNodesPostOrder.get(hostLeaf);

            Association m = new Association(parasiteLeaf, hostLeaf);
            mainMatrix[row][column] = NestedSolution.fromAssociation(m, countWhileFillingMatrix);
            subtreeMatrix[row][column] = mainMatrix[row][column];

            int distance = 1;
            TreeNode ancestor = hostLeaf.getParent();

            while (ancestor != null) {
                int ancestorIndex = hostNodesPostOrder.get(ancestor);
                List<Association> s = new ArrayList<Association>();
                s.add(m);
                subtreeMatrix[row][ancestorIndex] = new NestedSolution(Composition.FINAL,
                                                                       distance * lossCost,
                                                                       null,
                                                                       m,
                                                                       Event.LEAF,
                                                                       distance,
                                                                       countWhileFillingMatrix);
                ancestor = ancestor.getParent();
                ++distance;
            }

        }

    }


    /**
     * Computes a duplication event that happens in a leaf node of the host tree.
     * 
     * @param parasiteIndex
     *        Index of the parasite node.
     * @param parasiteLeftChildIndex
     *        Index of the parasite left children.
     * @param parasiteRightChildIndex
     *        Index of the parasite right children.
     * @param hostIndex
     *        Index of the host node.
     * @param currentAssociation
     *        The host-parasite association.
     * @return A NestedSolution object which reflects a duplication event in a leaf node of the host
     *         tree.
     */
    private NestedSolution computeDuplicationLeaf(int parasiteIndex,
                                                  int parasiteLeftChildIndex,
                                                  int parasiteRightChildIndex,
                                                  int hostIndex,
                                                  Association currentAssociation) {
        NestedSolution first = mainMatrix[parasiteLeftChildIndex][hostIndex];
        NestedSolution second = mainMatrix[parasiteRightChildIndex][hostIndex];
        ArrayList<NestedSolution> children = new ArrayList<NestedSolution>();
        children.add(first);
        children.add(second);
        return NestedSolution.cartesian(duplicationCost,
                                        children,
                                        currentAssociation,
                                        Event.DUPLICATION,
                                        0,
                                        countWhileFillingMatrix);
    }


    /**
     * Computes a duplication event which happens in an internal node of the host tree.
     * 
     * @param parasiteIndex
     *        Index of the parasite node.
     * @param parasiteLeftChildIndex
     *        Index of the parasite left children.
     * @param parasiteRightChildIndex
     *        Index of the parasite right children.
     * @param hostIndex
     *        Index of the host node.
     * @param hostLeftChildIndex
     *        Index of the host left children.
     * @param hostRightChildIndex
     *        Index of the host right children.
     * @param currentAssociation
     *        The host-parasite association.
     * @return A NestedSolution object which reflects a duplication event that happens in an
     *         internal node of the host tree.
     */
    private NestedSolution computeDuSolution(int parasiteIndex,
                                             int parasiteLeftChildIndex,
                                             int parasiteRightChildIndex,
                                             int hostIndex,
                                             int hostLeftChildIndex,
                                             int hostRightChildIndex,
                                             Association currentAssociation) {

        NestedSolution first1 = mainMatrix[parasiteLeftChildIndex][hostIndex];
        NestedSolution second1 = mainMatrix[parasiteRightChildIndex][hostIndex];
        ArrayList<NestedSolution> children1 = new ArrayList<NestedSolution>();
        children1.add(first1);
        children1.add(second1);
        NestedSolution case1 = NestedSolution.cartesian(duplicationCost,
                                                        children1,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        0,
                                                        countWhileFillingMatrix);

        NestedSolution first2 = mainMatrix[parasiteLeftChildIndex][hostIndex];
        NestedSolution second2 = subtreeMatrix[parasiteRightChildIndex][hostRightChildIndex];
        ArrayList<NestedSolution> children2 = new ArrayList<NestedSolution>();
        children2.add(first2);
        children2.add(second2);
        NestedSolution case2 = NestedSolution.cartesian(duplicationCost + lossCost,
                                                        children2,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        1,
                                                        countWhileFillingMatrix);

        NestedSolution first3 = mainMatrix[parasiteLeftChildIndex][hostIndex];
        NestedSolution second3 = subtreeMatrix[parasiteRightChildIndex][hostLeftChildIndex];
        ArrayList<NestedSolution> children3 = new ArrayList<NestedSolution>();
        children3.add(first3);
        children3.add(second3);
        NestedSolution case3 = NestedSolution.cartesian(duplicationCost + lossCost,
                                                        children3,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        1,
                                                        countWhileFillingMatrix);

        NestedSolution first4 = subtreeMatrix[parasiteLeftChildIndex][hostRightChildIndex];
        NestedSolution second4 = mainMatrix[parasiteRightChildIndex][hostIndex];
        ArrayList<NestedSolution> children4 = new ArrayList<NestedSolution>();
        children4.add(first4);
        children4.add(second4);
        NestedSolution case4 = NestedSolution.cartesian(duplicationCost + lossCost,
                                                        children4,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        1,
                                                        countWhileFillingMatrix);

        NestedSolution first5 = subtreeMatrix[parasiteLeftChildIndex][hostLeftChildIndex];
        NestedSolution second5 = mainMatrix[parasiteRightChildIndex][hostIndex];
        ArrayList<NestedSolution> children5 = new ArrayList<NestedSolution>();
        children5.add(first5);
        children5.add(second5);
        NestedSolution case5 = NestedSolution.cartesian(duplicationCost + lossCost,
                                                        children5,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        1,
                                                        countWhileFillingMatrix);

        NestedSolution first6 = subtreeMatrix[parasiteLeftChildIndex][hostLeftChildIndex];
        NestedSolution second6 = subtreeMatrix[parasiteRightChildIndex][hostLeftChildIndex];
        ArrayList<NestedSolution> children6 = new ArrayList<NestedSolution>();
        children6.add(first6);
        children6.add(second6);
        NestedSolution case6 = NestedSolution.cartesian(duplicationCost + lossCost + lossCost,
                                                        children6,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        2,
                                                        countWhileFillingMatrix);

        NestedSolution first7 = subtreeMatrix[parasiteLeftChildIndex][hostRightChildIndex];
        NestedSolution second7 = subtreeMatrix[parasiteRightChildIndex][hostRightChildIndex];
        ArrayList<NestedSolution> children7 = new ArrayList<NestedSolution>();
        children7.add(first7);
        children7.add(second7);
        NestedSolution case7 = NestedSolution.cartesian(duplicationCost + lossCost + lossCost,
                                                        children7,
                                                        currentAssociation,
                                                        Event.DUPLICATION,
                                                        2,
                                                        countWhileFillingMatrix);

        NestedSolution duplicationSol = topSolutions(countWhileFillingMatrix,
                                                     case1,
                                                     case2,
                                                     case3,
                                                     case4,
                                                     case5,
                                                     case6,
                                                     case7);

        return duplicationSol;

    }


    /**
     * Computes a co-speciation event.
     * 
     * @param parasiteLeftChildIndex
     *        Index of the parasite left children.
     * @param parasiteRightChildIndex
     *        Index of the parasite right children.
     * @param hostLeftChildIndex
     *        Index of the host left children.
     * @param hostRightChildIndex
     *        Index of the host right children.
     * @param currentAssociation
     *        The host-parasite association.
     * @return A NestedSolution object which reflects a co-speciation event.
     */
    private NestedSolution computeSpeciationSol(int parasiteLeftChildIndex,
                                                int parasiteRightChildIndex,
                                                int hostLeftChildIndex,
                                                int hostRightChildIndex,
                                                Association currentAssociation) {
        NestedSolution first1 = subtreeMatrix[parasiteLeftChildIndex][hostLeftChildIndex];
        NestedSolution second1 = subtreeMatrix[parasiteRightChildIndex][hostRightChildIndex];
        ArrayList<NestedSolution> children1 = new ArrayList<NestedSolution>();
        children1.add(first1);
        children1.add(second1);
        NestedSolution option1 = NestedSolution.cartesian(speciationCost,
                                                          children1,
                                                          currentAssociation,
                                                          Event.COSPECIATION,
                                                          0,
                                                          countWhileFillingMatrix);
        NestedSolution first2 = subtreeMatrix[parasiteLeftChildIndex][hostRightChildIndex];
        NestedSolution second2 = subtreeMatrix[parasiteRightChildIndex][hostLeftChildIndex];
        ArrayList<NestedSolution> children2 = new ArrayList<NestedSolution>();
        children2.add(first2);
        children2.add(second2);
        NestedSolution option2 = NestedSolution.cartesian(speciationCost,
                                                          children2,
                                                          currentAssociation,
                                                          Event.COSPECIATION,
                                                          0,
                                                          countWhileFillingMatrix);

        return topSolutions(countWhileFillingMatrix, option1, option2);
    }


    /**
     * Computes a host-switch event.
     * 
     * @param parasiteLeftChildIndex
     *        Index of the parasite left children.
     * @param parasiteRightChildIndex
     *        Index of the parasite right children.
     * @param hostLeftChildIndex
     *        Index of the host left children.
     * @param hostRightChildIndex
     *        Index of the host right children.
     * @param currentAssociation
     *        The host-parasite association.
     * @return A NestedSolution object which reflects a host-switch event.
     */
    private NestedSolution computeTrSolution(int parasiteLeftChildIndex,
                                             int parasiteRightChildIndex,
                                             int hostIndex,
                                             TreeNode hostNode,
                                             Association currentAssociation) {

        NestedSolution bestOption = new NestedSolution();
        List<Entry<TreeNode, Long>> allowedTransfers = getAllowedTransfers(hostNode);
        for (Entry<TreeNode, Long> e: allowedTransfers) {
            int nIndex = hostNodesPostOrder.get(e.getKey());

            NestedSolution first1 = mainMatrix[parasiteLeftChildIndex][nIndex];
            NestedSolution second1 = subtreeMatrix[parasiteRightChildIndex][hostIndex];
            ArrayList<NestedSolution> children1 = new ArrayList<NestedSolution>();
            children1.add(first1);
            children1.add(second1);
            NestedSolution option_1 = NestedSolution.cartesian(transferCost,
                                                               children1,
                                                               currentAssociation,
                                                               Event.HOSTSWITCH,
                                                               0,
                                                               countWhileFillingMatrix);

            NestedSolution first2 = mainMatrix[parasiteRightChildIndex][nIndex];
            NestedSolution second2 = subtreeMatrix[parasiteLeftChildIndex][hostIndex];
            ArrayList<NestedSolution> children2 = new ArrayList<NestedSolution>();
            children2.add(first2);
            children2.add(second2);
            NestedSolution option_2 = NestedSolution.cartesian(transferCost,
                                                               children2,
                                                               currentAssociation,
                                                               Event.HOSTSWITCH,
                                                               0,
                                                               countWhileFillingMatrix);

            bestOption = topSolutions(countWhileFillingMatrix, bestOption, option_1, option_2);
        }
        return bestOption;

    }


    /**
     * Returns a nested solution which is a combination of all solutions which have the same optimum
     * cost.
     * 
     * @param accumulate
     *        If true, computes the number of subsolutions represented by the nested solution.
     * @param solutions
     *        List of solutions to be processed.
     * @return A nested solution which is a combination of all solutions which have the same optimum
     *         cost.
     */
    public static NestedSolution topSolutions(boolean accumulate, NestedSolution... solutions) {
        NestedSolution top = null;
        for (NestedSolution solution: solutions) {
            if (top == null) {
                top = solution;
            } else {
                if (top.getCost() > solution.getCost()) {
                    top = solution;
                } else if (top.getCost() == solution.getCost()) {
                    top = NestedSolution.merge(top, solution, accumulate);
                }
            }
        }
        return top;
    }


    /**
     * Clean up the given row of the main and auxiliary matrices.
     * 
     * @param index
     *        Row index.
     */
    private void cleanTables(int index) {
        mainMatrix[index] = null;
        subtreeMatrix[index] = null;
    }

}
