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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.bamboo.eucalypt.methods.NestedSolution.Composition;
import fr.inria.bamboo.eucalypt.methods.NestedSolution.Event;
import fr.inria.bamboo.eucalypt.utilities.Util;

public class NestedSolutionEnumerator {

    /**
     * This class implements a nested solution iterator
     */
    public static class NestedSolutionIterator implements Iterator<NestedSolution> {

        /** Stack of nested solutions. */
        private ArrayList<NestedSolution> stack = new ArrayList<NestedSolution>();


        /**
         * Creates a nested solution iterator.
         * 
         * @param root
         *        Nested solution which has an optimal mapping for the root of the parasite tree.
         */
        public NestedSolutionIterator(NestedSolution root) {
            stack.add(root);
        }


        /**
         * Returns true if there exists an element on the stack.
         * 
         * @return True if there exists an element on the stack.
         */
        public boolean hasNext() {
            return !stack.isEmpty();
        }


        /**
         * Returns the next element in the iteration sequence.
         * 
         * @return The next element in the iteration sequence.
         */
        public NestedSolution next() {
            doNext();
            if (stack.isEmpty()) {
                return null;
            }
            NestedSolution current = stack.get(stack.size() - 1);
            return current;
        }


        /**
         * Update the stack for with the next element in the iteration sequence.
         */
        private void doNext() {
            NestedSolution current = stack.get(stack.size() - 1);
            if (current.getCompostionType() != Composition.FINAL) {
                stack.add(current.getChildAt(0));
            } else {
                current = stack.remove(stack.size() - 1);
                while (!stack.isEmpty()
                       && (current != stack.get(stack.size() - 1).getChildAt(0) || stack.get(stack.size() - 1).getCompostionType() == Composition.MULTIPLE)) {
                    current = stack.remove(stack.size() - 1);
                }
                if (!stack.isEmpty()) {
                    /* Add the left child into the stack. */
                    stack.add(stack.get(stack.size() - 1).getChildAt(1));
                }
            }
        }


        /**
         * Do nothing. Throws a Runtime exception if called.
         */
        public void remove() {
            throw new RuntimeException();
        }


        /**
         * Returns the next solution that is equivalent to the i_th element of the nested solution
         * which is in the top of the stack.
         * 
         * @param i
         *        Index of the desired element.
         * @return The next solution that is equivalent to the i_th element of the nested solution
         *         which is in the top of the stack.
         */
        /* This function is called after a merge of solutions. */
        public NestedSolution next(int i) {
            NestedSolution current = stack.get(stack.size() - 1);
            NestedSolution next = current.getChildAt(i);
            stack.add(next);
            return next;
        }

    }


    /** Writer object for results output. */
    private Writer writer;

    /** Nested solution which contains an optimal mapping for the parasite tree root node. */
    private NestedSolution root;

    /** Merge stack. Auxiliary stack to control the merge operations. */
    private ArrayList<Entry<NestedSolution, Integer>> mergeStack = new ArrayList<Map.Entry<NestedSolution, Integer>>();

    /** Auxiliary variable which keeps the current index on the stack. */
    private int indexInStack = 0;


    /**
     * Creates a Nested Solution Enumerator for the given root node (an optimal mapping for the
     * parasite tree root node).
     * 
     * @param root
     *        Optimal mapping for the parasite tree root node
     * @param writer
     *        FileWriter object which will be used to output the enumerated reconciliation mappings.
     */
    public NestedSolutionEnumerator(NestedSolution root, FileWriter writer) {
        this.root = root;
        this.writer = writer;
    }


    /**
     * Enumerates reconciliation mappings while the given limit is not reached.
     * 
     * @param limit
     *        Threshold on the number of reconciliations to be enumerated.
     * @return The number of enumerated reconciliation mappings.
     * @throws IOException
     *         When something goes wrong while writing the output.
     */
    public BigInteger enumerate(BigInteger limit) throws IOException {

        NestedSolutionIterator iterator = new NestedSolutionIterator(root);
        BigInteger numSolutions = BigInteger.ZERO;
        NestedSolution current = root;

        do {

            while (iterator.hasNext()) {
                NestedSolution next = process(current, iterator);
                current = next;
            }

            cleanStack();

            if (writer != null) {
                writer.write("\n");
            }

            numSolutions = numSolutions.add(BigInteger.ONE);
            if (limit != null && numSolutions.compareTo(limit) >= 0) {
                return numSolutions;
            }

            current = root;
            iterator = new NestedSolutionIterator(root);

        } while (!mergeStack.isEmpty());

        return numSolutions;
    }


    /**
     * Enumerates reconciliation mappings while the given limit is not reached. This method produces
     * statistics about the number of events observed into the
     * 
     * @param limit
     *        Threshold on the number of reconciliations to be enumerated.
     * @param eventVectorCount
     *        HashMap that will keep the count of event vectors for all solutions.
     * @return The number of enumerated reconciliation mappings.
     * @throws IOException
     *         When something goes wrong while writing the output.
     */
    public BigInteger enumerateWithStatistics(BigInteger limit,
                                              HashMap<ArrayList<Integer>, Integer> eventVectorCount) throws IOException {

        HashMap<ArrayList<Integer>, Integer> eventMap = new HashMap<ArrayList<Integer>, Integer>();

        NestedSolutionIterator iterator = new NestedSolutionIterator(root);
        NestedSolution current = root;

        BigInteger numberOfSolutions = BigInteger.ZERO;

        do {

            ArrayList<Integer> eventVector = new ArrayList<Integer>();
            eventVector.add(0, 0);
            eventVector.add(1, 0);
            eventVector.add(2, 0);
            eventVector.add(3, 0);

            while (iterator.hasNext()) {

                if (current != null) {

                    if (current.getLosses() > 0) {
                        eventVector.set(3, eventVector.get(3) + current.getLosses());
                    }

                    Event kind = current.getEvent();
                    if (kind == Event.COSPECIATION) {
                        eventVector.set(0, eventVector.get(0) + 1);
                    } else if (kind == Event.DUPLICATION) {
                        eventVector.set(1, eventVector.get(1) + 1);
                    } else if (kind == Event.HOSTSWITCH) {
                        eventVector.set(2, eventVector.get(2) + 1);
                    }

                }

                NestedSolution next = process(current, iterator);

                current = next;

            }

            cleanStack();

            numberOfSolutions = numberOfSolutions.add(BigInteger.ONE);

            if (writer != null) {
                writer.write("\n");
                writer.write(eventVector.toString() + "\n");
            }

            if (eventMap.containsKey(eventVector)) {
                eventMap.put(eventVector, eventMap.get(eventVector) + 1);
            } else {
                eventMap.put(eventVector, 1);
            }

            if (eventVectorCount.containsKey(eventVector)) {
                eventVectorCount.put(eventVector, eventVectorCount.get(eventVector) + 1);
            } else {
                eventVectorCount.put(eventVector, 1);
            }

            if (limit != null && numberOfSolutions.compareTo(limit) >= 0) {
                break;
            }

            current = root;
            iterator = new NestedSolutionIterator(root);

        } while (!mergeStack.isEmpty());

        Util.calculateAverage(eventMap, numberOfSolutions, "");
        return numberOfSolutions;
    }


    /**
     * Auxiliary function: clean-up the merge stack.
     */
    private void cleanStack() {
        indexInStack = 0;
        boolean stop = false;
        while (!stop && !mergeStack.isEmpty()) {
            Entry<NestedSolution, Integer> lastEntry = mergeStack.get(mergeStack.size() - 1);
            NestedSolution ns = lastEntry.getKey();
            int lastIndex = ns.getNumberOfChildren() - 1;
            if (lastEntry.getValue() == lastIndex) {
                mergeStack.remove(lastEntry);
            } else {
                stop = true;
            }
        }
    }


    /**
     * Auxiliary function: processes the current nested solution and returns the next one.
     * 
     * @param current
     *        Current nested solution.
     * @param iterator
     *        NestedSolutionIterator object.
     * @return The next nested solution object in the iteration sequence.
     * @throws IOException
     *         When something goes wrong while writing the output.
     */
    private NestedSolution process(NestedSolution current, NestedSolutionIterator iterator) throws IOException {

        NestedSolution next = null;
        if (current.getCompostionType() == Composition.MULTIPLE) {
            /* We have a multiple nested solution node. */
            if (indexInStack >= mergeStack.size()) {
                /* The element is not into the stack */
                Entry<NestedSolution, Integer> entry = new AbstractMap.SimpleEntry<NestedSolution, Integer>(current,
                                                                                                            0);
                mergeStack.add(entry);
                next = iterator.next(0);
            } else if (indexInStack == mergeStack.size() - 1) {
                /* The element is the last into the stack. */
                int childIndex = mergeStack.get(indexInStack).getValue() + 1;
                mergeStack.get(indexInStack).setValue(childIndex);
                next = iterator.next(childIndex);
            } else {
                /* The element is in the stack but it is not the last one. */
                Integer index = mergeStack.get(indexInStack).getValue();
                next = iterator.next(index);
            }
            indexInStack += 1;
        } else {
            /* We have something other than a multiple nested solution node. */
            if (writer != null)
                writer.write(current.getAssociation().toString());
            next = iterator.next();
            if (iterator.hasNext() && writer != null) {
                writer.write(", ");
            }
        }

        return next;
    }

}
