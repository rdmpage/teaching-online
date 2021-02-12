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

import java.math.BigInteger;
import java.util.ArrayList;

import fr.inria.bamboo.eucalypt.utilities.Association;

public class NestedSolution {

    /** Constant: Infinity value. */
    public static final long INFINITY = Integer.MAX_VALUE;


    /** Enumeration of types of NestedSolution Nodes. */
    public static enum Composition {
        SIMPLE, MULTIPLE, FINAL
    }

    /** Enumeration of type of reconciliation nodes. */
    public static enum Event {
        COSPECIATION, DUPLICATION, HOSTSWITCH, LOSS, LEAF;
    }


    /** Type of this nested solution object. */
    private final Composition compostionType;

    /** Cost of this nested solution object. */
    private final long cost;

    /** List of nested solutions which are subsolutions of this nested solution. */
    private final ArrayList<NestedSolution> children;

    /** Host-Parasite association which is represented by this nested solution. */
    private final Association association;

    /** Number of subsolutions which are represented by this nested solution. */
    private final BigInteger subsolutions;

    /** Reconciliation event that is associated to this nested solution. */
    private final Event event;

    /** Number of losses which are associated to this nested solution. */
    private final int losses;


    /**
     * Creates a new nested solution based on the given association (leaf-to-leaf associations).
     * 
     * @param association
     *        Base association.
     * @param accumulate
     *        If true, computes the number of subsolutions represented by the nested solution.
     * @return A new nested solution based on the given association (leaf-to-leaf associations).
     */
    static public NestedSolution fromAssociation(Association association, boolean accumulate) {
        return new NestedSolution(Composition.FINAL,
                                  0,
                                  null,
                                  association,
                                  Event.LEAF,
                                  0,
                                  accumulate);
    }


    /**
     * Sets the number of losses of the given nested solution.
     * 
     * @param lossCost
     *        Cost of a loss.
     * @param numberOfLosses
     *        Number of losses.
     * @param solution
     *        NestedSolution object.
     * @param accumulate
     *        If true, computes the number of subsolutions represented by the nested solution.
     * @return A new nested solution object with the updated information.
     */
    static public NestedSolution addLosses(long lossCost,
                                           int numLosses,
                                           NestedSolution solution,
                                           boolean accumulate) {
        ArrayList<NestedSolution> newChildren = new ArrayList<NestedSolution>();
        if (solution.compostionType == Composition.MULTIPLE) {
            for (int i = 0; i < solution.getNumberOfChildren(); i++) {
                NestedSolution ns = solution.getChildAt(i);
                newChildren.add(addLosses(lossCost, numLosses, ns, accumulate));
            }
            return new NestedSolution(Composition.MULTIPLE,
                                      newChildren.get(0).getCost(),
                                      newChildren,
                                      null,
                                      null,
                                      0,
                                      accumulate);
        } else {
            for (int i = 0; i < solution.getNumberOfChildren(); i++) {
                NestedSolution ns = solution.getChildAt(i);
                newChildren.add(ns);
            }
            long addedCost = lossCost * numLosses;
            return new NestedSolution(solution.getCompostionType(),
                                      solution.getCost() + addedCost,
                                      newChildren,
                                      solution.getAssociation(),
                                      solution.getEvent(),
                                      solution.getLosses() + numLosses,
                                      accumulate);
        }
    }


    /**
     * Returns the child that is at the given position (into the children list).
     * 
     * @param index
     *        The index of the desired child.
     * @return The child that is at the given position (into the children list).
     */
    public NestedSolution getChildAt(int index) {
        return children.get(index);
    }


    /**
     * Returns the number of children of this nested solution.
     * 
     * @return The number of children of this nested solution.
     */
    public int getNumberOfChildren() {
        return children.size();
    }


    /**
     * Creates a new nested solution that is result of the cartesian product of the given children.
     * 
     * @param addedCost
     *        Cost to be added on the total cost of the children to obtain the cost of this nested
     *        solution.
     * @param children
     *        List of children of this nested solution.
     * @param association
     *        Host-parasite association.
     * @param event
     *        Event type.
     * @param losses
     *        Number of losses.
     * @param accumulate
     *        If true, computes the number of subsolutions represented by the nested solution.
     * @return A new nested solution that is result of the cartesian product of the given children.
     */
    static public NestedSolution cartesian(long addedCost,
                                           ArrayList<NestedSolution> children,
                                           Association association,
                                           Event event,
                                           int losses,
                                           boolean accumulate) {
        long c = addedCost + children.get(0).getCost() + children.get(1).getCost();
        return new NestedSolution(Composition.SIMPLE,
                                  c,
                                  children,
                                  association,
                                  event,
                                  losses,
                                  accumulate);
    }


    /**
     * Creates a new nested solution based on the merge of two other nested solution.
     * 
     * @param first
     *        First nested solution to be merged.
     * @param second
     *        Second nested solution to be merged.
     * @param accumulate
     *        If true, computes the number of subsolutions represented by the nested solution.
     * @return A new nested solution based on the merge of two other nested solution.
     */
    static public NestedSolution merge(NestedSolution first,
                                       NestedSolution second,
                                       boolean accumulate) {

        if (first.getCost() != second.getCost())
            throw new IllegalArgumentException();

        long c = first.getCost();

        if (first.getCost() == INFINITY) {
            return new NestedSolution();
        } else {
            ArrayList<NestedSolution> newSons = new ArrayList<NestedSolution>();
            if (first.getCompostionType() == Composition.MULTIPLE) {
                newSons.addAll(first.getChildren());
            } else {
                newSons.add(first);
            }
            if (second.getCompostionType() == Composition.MULTIPLE) {
                newSons.addAll(second.getChildren());
            } else {
                newSons.add(second);
            }
            return new NestedSolution(Composition.MULTIPLE, c, newSons, null, null, 0, accumulate);
        }
    }


    /**
     * Returns the list of children of this nested solution.
     * 
     * @return The list of children of this nested solution.
     */
    public ArrayList<NestedSolution> getChildren() {
        return children;
    }


    /**
     * Returns the number of subsolutions of this nested solution.
     * 
     * @return The number of subsolutions of this nested solution.
     */
    public BigInteger getSubsolutions() {
        return subsolutions;
    }


    /**
     * Creates an empty nested solution.
     */
    public NestedSolution() {
        this.compostionType = Composition.FINAL;
        this.cost = INFINITY;
        this.children = new ArrayList<NestedSolution>();
        this.association = null;
        this.subsolutions = BigInteger.ONE;
        this.event = Event.LEAF;
        this.losses = 0;
    }


    /**
     * Creates a nested solution.
     * 
     * @param compostionType
     *        Type of the nested solution.
     * @param cost
     *        Cost of the nested solution.
     * @param children
     *        Children of the nested solution.
     * @param association
     *        Host-parasite association which are represented by this nested solution.
     * @param event
     *        Event type associated to this nested solution.
     * @param losses
     *        Number of losses.
     * @param accumulate
     *        If true, computes the number of subsolutions represented by the nested solution.
     */
    public NestedSolution(Composition compostionType,
                          long cost,
                          ArrayList<NestedSolution> children,
                          Association association,
                          Event event,
                          int losses,
                          boolean accumulate) {
        this.compostionType = compostionType;
        this.cost = cost;
        this.children = children;
        this.association = association;
        this.event = event;
        this.losses = losses;
        if (accumulate) {
            BigInteger subsol = BigInteger.ONE;
            if (compostionType == Composition.SIMPLE) {
                subsol = children.get(0).getSubsolutions().multiply(children.get(1).getSubsolutions());
            } else if (compostionType == Composition.MULTIPLE) {
                subsol = BigInteger.ZERO;
                for (NestedSolution ne: children) {
                    subsol = subsol.add(ne.getSubsolutions());
                }
            }
            this.subsolutions = subsol;
        } else {
            this.subsolutions = BigInteger.ONE;
        }

    }


    /**
     * Returns the type of this nested solution.
     * 
     * @return The type of this nested solution.
     */
    public Composition getCompostionType() {
        return compostionType;
    }


    /**
     * Returns the host-parasite association related to this nested solution.
     * 
     * @return The host-parasite association related to this nested solution.
     */
    public Association getAssociation() {
        return association;
    }


    /**
     * Returns the cost of this nested solution.
     * 
     * @return The cost of this nested solution.
     */
    public long getCost() {
        return cost;
    }


    /**
     * Returns a string representation of this nested solution.
     * 
     * @return A string representation of this nested solution.
     */
    public String toString() {
        if (association == null) {
            return this.children.get(0).toString() + "*";
        } else {
            return association.toString();
        }

    }


    /**
     * Returns the event type of this nested solution.
     * 
     * @return The event type of this nested solution.
     */
    public Event getEvent() {
        return event;
    }


    /**
     * Returns the number of losses of this nested solution.
     * 
     * @return The number of losses of this nested solution.
     */
    public int getLosses() {
        return losses;
    }

}
