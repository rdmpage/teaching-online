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
package fr.inria.bamboo.eucalypt.utilities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

public class Util {

    /**
     * Prints to the stdout some statistics about the observed number of events.
     * 
     * @param eventMap
     *        HashMap which contains the distribution of event vectors.
     * @param numberOfSolutions
     *        Total number of enumerated reconciliation mappings.
     * @param classification
     *        String to classify the vectors
     */
    public static void calculateAverage(HashMap<ArrayList<Integer>, Integer> eventMap,
                                        BigInteger numberOfSolutions,
                                        String classification) {

        System.out.println("Number of distinct event vectors " + classification + " = "
                           + eventMap.size());
        if (eventMap.size() > 0) {
            System.out.println(eventMap.toString());
            BigInteger cospeciationCount = BigInteger.ZERO;
            BigInteger duplicationCount = BigInteger.ZERO;
            BigInteger hostSwitchCount = BigInteger.ZERO;
            BigInteger lossCount = BigInteger.ZERO;
            for (ArrayList<Integer> eventVector: eventMap.keySet()) {
                cospeciationCount = cospeciationCount.add(BigInteger.valueOf(eventVector.get(0)
                                                                             * eventMap.get(eventVector)));
                duplicationCount = duplicationCount.add(BigInteger.valueOf(eventVector.get(1)
                                                                           * eventMap.get(eventVector)));
                hostSwitchCount = hostSwitchCount.add(BigInteger.valueOf(eventVector.get(2)
                                                                         * eventMap.get(eventVector)));
                lossCount = lossCount.add(BigInteger.valueOf(eventVector.get(3)
                                                             * eventMap.get(eventVector)));
            }

            BigDecimal cospeciation = new BigDecimal(cospeciationCount);
            BigDecimal duplication = new BigDecimal(duplicationCount);
            BigDecimal hostSwitch = new BigDecimal(hostSwitchCount);
            BigDecimal loss = new BigDecimal(lossCount);
            BigDecimal number = new BigDecimal(numberOfSolutions);

            if (!classification.equals("")) {
                classification = classification + ": ";
            }

            System.out.println(classification + "Average number of cospeciations = "
                               + cospeciation.divide(number, 2, RoundingMode.HALF_UP));
            System.out.println(classification + "Average number of duplications = "
                               + duplication.divide(number, 2, RoundingMode.HALF_UP));
            System.out.println(classification + "Average number of switches = "
                               + hostSwitch.divide(number, 2, RoundingMode.HALF_UP));
            System.out.println(classification + "Average number of losses = "
                               + loss.divide(number, 2, RoundingMode.HALF_UP));
        }

    }

}
