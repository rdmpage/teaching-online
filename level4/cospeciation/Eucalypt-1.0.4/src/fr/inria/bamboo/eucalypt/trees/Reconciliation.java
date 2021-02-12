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

import java.util.HashMap;
import java.util.Map;

public class Reconciliation {

    /** Parasite tree. */
    private Tree parasite;

    /** Host tree. */
    private Tree host;

    /** Reconciliation cost. */
    private double cost;

    /** Reconciliation mapping. */
    private Map<String, String> mapping;


    /**
     * Creates a reconciliation object with the given parameters and an empty mapping.
     * 
     * @param parasite
     *        Parasite tree.
     * @param host
     *        Host tree.
     * @param cost
     *        Reconciliation cost.
     */
    public Reconciliation(Tree parasite, Tree host, double cost) {
        this.parasite = parasite;
        this.host = host;
        this.cost = cost;
        mapping = new HashMap<String, String>();
        for (TreeNode p: parasite) {
            mapping.put(p.getLabel(), null);
        }
    }


    /**
     * Creates a reconciliation object with the given parameters.
     * 
     * @param parasite
     *        Parasite tree.
     * @param host
     *        Host tree.
     * @param cost
     *        Reconciliation cost.
     * @param mapping
     *        Reconciliation mapping.
     */
    public Reconciliation(Tree parasite, Tree host, double cost, Map<String, String> mapping) {
        this.parasite = parasite;
        this.host = host;
        this.cost = cost;
        this.mapping = mapping;
    }


    /**
     * Returns the reconciliation cost.
     * 
     * @return The reconciliation cost.
     */
    public double getCost() {
        return cost;
    }


    /**
     * Set the reconciliation cost.
     * 
     * @param cost
     *        The reconciliation cost.
     */
    public void setCost(double cost) {
        this.cost = cost;
    }


    /**
     * Returns the host tree.
     * 
     * @return The host tree.
     */
    public Tree getHost() {
        return host;
    }


    /**
     * Returns the parasite tree.
     * 
     * @return The parasite tree.
     */
    public Tree getParasite() {
        return parasite;
    }


    /**
     * Returns true if the reconciliation is cyclic and false, otherwise.
     * 
     * @return True if the reconciliation is cyclic and false, otherwise.
     */
    public boolean isCyclic() {
        return false;
    }


    /**
     * Returns the reconciliation mapping.
     * 
     * @return The reconciliation mapping.
     */
    public Map<String, String> getMapping() {
        return mapping;
    }

}
