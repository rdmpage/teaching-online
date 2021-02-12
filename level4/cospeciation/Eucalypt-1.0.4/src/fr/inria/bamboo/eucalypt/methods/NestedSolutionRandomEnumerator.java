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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import fr.inria.bamboo.eucalypt.methods.NestedSolution.Composition;
import fr.inria.bamboo.eucalypt.methods.NestedSolution.Event;

public class NestedSolutionRandomEnumerator {

    /**
     * This class implements a nested solution iterator
     */
    public static class NestedSolutionRandomIterator implements Iterator<NestedSolution> {

        /** Stack of nested solutions. */
        private ArrayList<NestedSolution> stack = new ArrayList<NestedSolution>();

        /** Random generator. */
        private Random random;


        /**
         * Creates a nested solution iterator.
         * 
         * @param root
         *        Nested solution which has an optimal mapping for the root of the parasite tree.
         */
        public NestedSolutionRandomIterator(NestedSolution[] solutions) {
            this.random = new Random();
            stack.add(solutions[this.random.nextInt(solutions.length)]);
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
            NestedSolution current = stack.remove(stack.size() - 1);
            if (current.getCompostionType() == Composition.MULTIPLE) {
                stack.add(current.getChildAt(random.nextInt(current.getNumberOfChildren())));
            } else if (current.getCompostionType() == Composition.SIMPLE) {
                stack.add(current.getChildAt(0));
                stack.add(current.getChildAt(1));
            }
            return current;
        }


        /**
         * Do nothing. Throws a Runtime exception if called.
         */
        public void remove() {
            throw new RuntimeException();
        }

    }


    /** Writer object for results output. */
    private Writer writer;

    /** Nested solution which contains an optimal mapping for the parasite tree root node. */
    private NestedSolution[] solutions;


    /**
     * Creates a Nested Solution Random Enumerator.
     * 
     * @param root
     *        Optimal mapping for the parasite tree root node
     * @param writer
     *        FileWriter object which will be used to output the enumerated reconciliation mappings.
     */
    public NestedSolutionRandomEnumerator(NestedSolution[] solutions, FileWriter writer) {
        this.solutions = solutions;
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
    public void enumerate(BigInteger limit) throws IOException {

        BigInteger numSolutions = BigInteger.ZERO;

        do {

            NestedSolutionRandomIterator iterator = new NestedSolutionRandomIterator(solutions);
            boolean first = true;
            while (iterator.hasNext()) {
                NestedSolution next = iterator.next();
                if (next.getCompostionType() != Composition.MULTIPLE) {
                    if (!first) {
                        writer.write(", ");
                    }
                    writer.write(next.getAssociation().toString());
                    first = false;
                }
            }

            writer.write("\n");

            numSolutions = numSolutions.add(BigInteger.ONE);

        } while (numSolutions.compareTo(limit) < 0);

    }


    /**
     * Enumerates reconciliation mappings while the given limit is not reached. This method produces
     * statistics about the number of events observed into the
     * 
     * @param limit
     *        Threshold on the number of reconciliations to be enumerated.
     * @param eventVectorCount
     *        HashMap that will keep the count of event vectors for all solutions.
     * @throws IOException
     *         When something goes wrong while writing the output.
     */
    public void enumerateWithStatistics(BigInteger limit,
                                        HashMap<ArrayList<Integer>, Integer> eventVectorCount) throws IOException {

        BigInteger numSolutions = BigInteger.ZERO;

        do {

            NestedSolutionRandomIterator iterator = new NestedSolutionRandomIterator(solutions);

            ArrayList<Integer> eventVector = new ArrayList<Integer>();
            eventVector.add(0, 0);
            eventVector.add(1, 0);
            eventVector.add(2, 0);
            eventVector.add(3, 0);

            boolean first = true;
            while (iterator.hasNext()) {

                NestedSolution next = iterator.next();
                if (writer != null) {
                    if (next.getCompostionType() != Composition.MULTIPLE) {
                        if (!first) {
                            writer.write(", ");
                        }
                        writer.write(next.getAssociation().toString());
                        first = false;
                    }
                }

                if (next.getLosses() > 0) {
                    eventVector.set(3, eventVector.get(3) + next.getLosses());
                }

                Event kind = next.getEvent();
                if (kind == Event.COSPECIATION) {
                    eventVector.set(0, eventVector.get(0) + 1);
                } else if (kind == Event.DUPLICATION) {
                    eventVector.set(1, eventVector.get(1) + 1);
                } else if (kind == Event.HOSTSWITCH) {
                    eventVector.set(2, eventVector.get(2) + 1);
                }

            }

            if (eventVectorCount.containsKey(eventVector)) {
                eventVectorCount.put(eventVector, eventVectorCount.get(eventVector) + 1);
            } else {
                eventVectorCount.put(eventVector, 1);
            }

            if (writer != null) {
                writer.write("\n");
                writer.write(eventVector.toString() + "\n");
            }

            numSolutions = numSolutions.add(BigInteger.ONE);

        } while (numSolutions.compareTo(limit) < 0);

    }

}
