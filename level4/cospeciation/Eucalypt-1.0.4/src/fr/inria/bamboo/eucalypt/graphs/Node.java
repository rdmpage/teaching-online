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

public class Node {

    /** Node label */
    private String label;

    /** List of nodes which are in the in-neighborhood */
    private ArrayList<Node> inNeighborhood;

    /** List of nodes which are in the out-neighborhood */
    private ArrayList<Node> outNeighborhood;


    /**
     * Creates a new node with the given arguments.
     * 
     * @param label
     *        Node label.
     */
    public Node(String label) {
        this.inNeighborhood = new ArrayList<Node>();
        this.outNeighborhood = new ArrayList<Node>();
        this.label = label;
    }


    /**
     * Returns a hash code for this node.
     * 
     * @return A hash code for this node.
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + ((label == null) ? 0 : label.hashCode());
    }


    /**
     * Compares this node to the specified object. The result is true if and only if the argument is
     * not null and is a Node object that contains the same identifier.
     * 
     * @return True if and only if the argument is not null and is a Node object that contains the
     *         same identifier.
     */
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Node other = (Node)obj;
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }

        return true;
    }


    /**
     * Returns a string representation of this node (only label).
     * 
     * @return A string representation of this node (only label).
     */
    public String toString() {
        return this.label;
    }


    /**
     * Returns the node label.
     * 
     * @return The node label.
     */
    public String getLabel() {
        return label;
    }


    /**
     * Returns the node in-degree.
     * 
     * @return The node in-degree.
     */
    public int getInDegree() {
        return inNeighborhood.size();
    }


    /**
     * Returns the node out-degree.
     * 
     * @return The node out-degree.
     */
    public int getOutDegree() {
        return outNeighborhood.size();
    }


    /**
     * Returns the node degree.
     * 
     * @return The node degree.
     */
    public int getDegree() {
        return inNeighborhood.size() + outNeighborhood.size();
    }


    /**
     * Returns the list of nodes in the in-neigborhood.
     * 
     * @return The list of nodes in the in-neigborhood.
     */
    public ArrayList<Node> getInNeighborhood() {
        return inNeighborhood;
    }


    /**
     * Returns the list of nodes in the out-neigborhood.
     * 
     * @return The list of nodes in the out-neigborhood.
     */
    public ArrayList<Node> getOutNeighborhood() {
        return outNeighborhood;
    }


    /**
     * Add the node into the in-neighborhood of the node.
     * 
     * @param node
     *        Node to be addedd into the in-neighborhood.
     */
    public void addNodeIntoTheInNeighborhood(Node node) {
        if (!inNeighborhood.contains(node)) {
            inNeighborhood.add(node);
        }
    }


    /**
     * Add the node into the in-neighborhood of the node.
     * 
     * @param node
     *        Node to be addedd into the in-neighborhood.
     */
    public void addNodeIntoTheOutNeighborhood(Node node) {
        if (!outNeighborhood.contains(node)) {
            outNeighborhood.add(node);
        }
    }


    /**
     * Returns the parent of the node. If the node have more than one node in its in-neighborhood,
     * the method throws a RuntimeException.
     * 
     * @return The parent of the node.
     * @throws RuntimeException
     *         If the node have more than one node in its in-neighborhood.
     */
    public Node getParent() throws RuntimeException {
        if (inNeighborhood.size() > 1)
            throw new RuntimeException("Node has more than one parent.");
        if (inNeighborhood.size() == 1)
            return inNeighborhood.get(0);
        return null;
    }

}
