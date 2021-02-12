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
package fr.inria.bamboo.eucalypt.trees;

public class TreeNode {

    /** Children of this node. */
    private TreeNode[] children;

    /** Node key. */
    private int key;

    /** Node label. */
    private String label;

    /** Parent of this node. */
    private TreeNode parent;

    /** Node height. */
    private int height;


    /**
     * Creates a node which has the given key.
     * 
     * @param key
     *        Node key.
     */
    public TreeNode(int key) {
        this.children = new TreeNode[2];
        this.key = key;
        this.label = "" + key;
    }


    /**
     * Creates a node which has the given key and label.
     * 
     * @param key
     *        Node key.
     * @param label
     *        Node label.
     */
    public TreeNode(int key, String label) {
        this.children = new TreeNode[2];
        this.key = key;
        this.label = label;
    }


    /**
     * Set a child node for this node.
     * 
     * @param child
     *        Child node.
     * @param position
     *        0 or 1 - Position to insert the child node.
     */
    public void addChild(TreeNode child, int position) {
        children[position] = child;
        child.parent = this;
    }


    /**
     * Returns a child node of this node.
     * 
     * @param position
     *        Position of the desired child node (0 or 1).
     * @return A child node of this node.
     */
    public TreeNode getChild(int position) {
        return children[position];
    }


    /**
     * Returns the children of this node.
     * 
     * @return The children of this node.
     */
    public TreeNode[] getChildren() {
        return children;
    }


    /**
     * Returns the node key.
     * 
     * @return The node key.
     */
    public int getKey() {
        return key;
    }


    /**
     * Returns the node height.
     * 
     * @return The node height.
     */
    public int getHeight() {
        return height;
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
     * Returns true if this node is a leaf node and false, otherwise.
     * 
     * @return True if this node is a leaf node and false, otherwise.
     */
    public boolean isLeaf() {
        return (children[0] == null && children[1] == null);
    }


    /**
     * Returns true if this node is a root node and false, otherwise.
     * 
     * @return True if this node is a root node and false, otherwise.
     */
    public boolean isRoot() {
        return (null == parent);
    }


    /**
     * Returns the number of children that this node has.
     * 
     * @return The number of children that this node has.
     */
    public int numberChildren() {
        int r = 0;
        if (children[0] != null) {
            r = r + 1;
        }
        if (children[1] != null) {
            r = r + 1;
        }
        return r;
    }


    /**
     * Returns the parent of this node.
     * 
     * @return The parent of this node.
     */
    public TreeNode getParent() {
        return parent;
    }


    /**
     * Returns the sibling node of this node.
     * 
     * @return The sibling node of this node.
     */
    public TreeNode getSibling() {
        if (this.getParent() == null) {
            return null;
        }
        if (this == this.getParent().getChild(0)) {
            return this.getParent().getChild(1);
        } else {
            return this.getParent().getChild(0);
        }
    }


    /**
     * Sets the node label.
     * 
     * @param label
     *        New node label.
     */
    public void setLabel(String label) {
        this.label = label;
    }


    /**
     * Sets the node height.
     * 
     * @param height
     *        Node height.
     */
    public void setHeight(int height) {
        this.height = height;
    }


    /**
     * Returns true if the this node is ancestor of the given node.
     * 
     * @param node
     *        Tree node.
     * @return True if the this node is ancestor of the given node.
     */
    public boolean isAncestorOf(TreeNode node) {
        TreeNode tmp = node;
        do {
            if (tmp.getLabel().compareTo(this.getLabel()) == 0) {
                return true;
            }
            tmp = tmp.parent;
        } while (tmp != null);
        return false;
    }


    /**
     * Returns a string representation of this object.
     * 
     * @return A string representation of this object.
     */
    public String toString() {
        return label;
    }
}
