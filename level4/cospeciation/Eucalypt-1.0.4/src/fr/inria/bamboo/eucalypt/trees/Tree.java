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

import java.util.ArrayList;
import java.util.Iterator;

public class Tree implements Iterable<TreeNode> {

    /**
     * Class which implements a post-order iterator for the nodes of a tree.
     */
    public class POIterator implements Iterator<TreeNode> {

        private TreeNode previous = null;


        public TreeNode next() {
            TreeNode next = null;
            if (previous == null) {
                next = root;
                while (!next.isLeaf()) {
                    next = next.getChild(0);
                }
            } else {
                next = previous.getParent();
                if (previous == next.getChild(0)) {
                    next = next.getChild(1);
                    while (!next.isLeaf()) {
                        next = next.getChild(0);
                    }
                }
            }
            previous = next;
            return next;
        }


        public boolean hasNext() {
            return root != previous;
        }


        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    /** Tree root. */
    protected TreeNode root;

    /** List of nodes of the tree. */
    public ArrayList<TreeNode> nodes;


    /**
     * Constructor: Creates a tree with a root that is identified by the key passed by parameter.
     * 
     * @param key
     *        The key value of the root of the tree.
     */
    public Tree(int key) {
        root = new TreeNode(key);
    }


    /**
     * Recursive method: Creates the list of nodes of the tree.
     * 
     * @param node
     *        The root of the subtree to be explored.
     */
    public void createNodeList(TreeNode node) {
        if (node.isLeaf()) {
            nodes.add(node);
        } else {
            if (node.getChild(0) != null) {
                createNodeList(node.getChild(0));
            }
            if (node.getChild(1) != null) {
                createNodeList(node.getChild(1));
            }
            nodes.add(node);
        }
    }


    /**
     * Creates the list of nodes of the tree.
     */
    public void createNodeList() {
        nodes = new ArrayList<TreeNode>();
        createNodeList(root);
    }


    /**
     * Creates a tree object.
     * 
     * @param newickString
     *        Newick string which represents the tree.
     */
    public Tree(String newickString) {
        char openBracket = '(';
        char closeBracket = ')';
        char childSeparator = ',';
        int cursor = 0;
        int key = 0;
        root = new TreeNode(key++);
        TreeNode currentNode = root;
        while (cursor < newickString.length()) {
            if (newickString.charAt(cursor) == openBracket) {
                currentNode.addChild(new TreeNode(key++), 0);
                currentNode = currentNode.getChild(0);
                cursor++;
            } else if (newickString.charAt(cursor) == childSeparator) {
                currentNode = currentNode.getParent();
                currentNode.addChild(new TreeNode(key++), 1);
                currentNode = currentNode.getChild(1);
                cursor++;
            } else if (newickString.charAt(cursor) == closeBracket) {
                currentNode = currentNode.getParent();
                cursor++;
            } else {
                String label = newickString.charAt(cursor) + "";
                cursor++;
                while (cursor < newickString.length()
                       && newickString.charAt(cursor) != childSeparator
                       && newickString.charAt(cursor) != closeBracket) {
                    label = label + newickString.charAt(cursor);
                    cursor++;
                }
                currentNode.setLabel(label);
            }
        }
    }


    /**
     * Returns the tree size.
     * 
     * @return The tree size.
     */
    public int size() {
        return getSize();
    }


    /**
     * Returns the tree root node.
     * 
     * @return The tree root node.
     */
    public TreeNode getRoot() {
        return root;
    }


    /**
     * Returns a string representation of the subtree rooted in the given node (in post-order).
     * 
     * @param node
     *        Subtree root node.
     * @return A string representation of the subtree rooted in the given node (in post-order).
     */
    public String postOrderString(TreeNode node) {
        if (node.isLeaf()) {
            return node.getLabel();
        } else {
            if (node.numberChildren() == 2) {
                String ls = postOrderString(node.getChild(0));
                String rs = postOrderString(node.getChild(1));
                return "(" + ls + "," + rs + ")" + node.getLabel();
            } else if (node.getChild(0) != null) {
                String ls = postOrderString(node.getChild(0));
                return "(" + ls + ")" + node.getLabel();
            } else {
                String rs = postOrderString(node.getChild(1));
                return "(" + rs + ")" + node.getLabel();
            }
        }
    }


    /**
     * Returns true if both nodes are comparable and false, otherwise.
     * 
     * @param n1
     *        Node 1.
     * @param n2
     *        Node 2.
     * @return True if both nodes are comparable and false, otherwise.
     */
    public boolean areComparable(TreeNode n1, TreeNode n2) {
        boolean b = false;
        if (n1.isAncestorOf(n2) || n2.isAncestorOf(n1)) {
            b = true;
        }
        return b;
    }


    /**
     * Returns a string represetantion of this tree.
     * 
     * @return A string represetantion of this tree.
     */
    public String toString() {
        return postOrderString(root);
    }


    /**
     * Returns a post-order iterator for this tree object.
     * 
     * @return A post-order iterator for this tree object.
     */
    public POIterator iterator() {
        return new POIterator();
    }


    /**
     * Returns the size of this tree (number of nodes).
     * 
     * @return The size of this tree.
     */
    public int getSize() {
        if (nodes == null)
            createNodeList(root);
        return nodes.size();
    }

}
