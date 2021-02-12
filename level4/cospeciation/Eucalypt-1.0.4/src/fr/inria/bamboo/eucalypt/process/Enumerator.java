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

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.inria.bamboo.eucalypt.methods.NestedSolution;
import fr.inria.bamboo.eucalypt.methods.NestedSolutionEnumerator;
import fr.inria.bamboo.eucalypt.methods.NestedSolutionRandomEnumerator;
import fr.inria.bamboo.eucalypt.methods.Reconciliator;
import fr.inria.bamboo.eucalypt.parser.NexusFileParser;
import fr.inria.bamboo.eucalypt.parser.NexusFileParserException;
import fr.inria.bamboo.eucalypt.process.TaskConfiguration.Task;
import fr.inria.bamboo.eucalypt.trees.Tree;
import fr.inria.bamboo.eucalypt.trees.TreeNode;
import fr.inria.bamboo.eucalypt.utilities.Util;

public class Enumerator {

    /**
     * Obtains function that maps parasite leaf nodes to host leaf nodes.
     * 
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param parser
     *        Nexus File Parser.
     * @return A Hashmap that represents the function that maps parasite leaf nodes to host leaf
     *         nodes.
     */
    private static HashMap<TreeNode, TreeNode> getLeafMap(Tree hostTree,
                                                          Tree parasiteTree,
                                                          NexusFileParser parser) {
        HashMap<String, String> map = parser.getPhiNewickNames();
        Map<String, TreeNode> hostMap = new HashMap<String, TreeNode>();
        for (TreeNode node: hostTree) {
            hostMap.put(node.getLabel(), node);
        }
        HashMap<TreeNode, TreeNode> leafMap = new HashMap<TreeNode, TreeNode>();
        for (TreeNode parasiteNode: parasiteTree) {
            if (parasiteNode.isLeaf()) {
                String hostLabel = map.get(parasiteNode.getLabel());
                TreeNode hostNode = hostMap.get(hostLabel);
                leafMap.put(parasiteNode, hostNode);
            }
        }
        return leafMap;
    }


    /**
     * Prints the header.
     * 
     * @param writer
     *        FileWriter object.
     * @param configuration
     *        Configuration object.
     * @param optimumCost
     *        Optimum cost.
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param leafMap
     *        Mapping of the parasite leaf nodes into the host leaf nodes.
     * @throws IOException
     *         When something wrong happens while writing the file.
     */
    private static void printHeader(FileWriter writer,
                                    TaskConfiguration configuration,
                                    long optimumCost,
                                    Tree hostTree,
                                    Tree parasiteTree,
                                    HashMap<TreeNode, TreeNode> leafMap) throws IOException {

        NumberFormat format = configuration.getFormatter();

        String optimumCostString = format.format((double)optimumCost
                                                 / (double)configuration.getDividerMultiplier());
        if (writer != null) {
            writer.write("#--------------------\n");
            writer.write("#Host tree          = " + hostTree + "\n");
            writer.write("#Parasite tree      = " + parasiteTree + "\n");
            writer.write("#Host tree size     = " + hostTree.getSize() + "\n");
            writer.write("#Parasite tree size = " + parasiteTree.getSize() + "\n");
            writer.write("#Leaf mapping       = " + leafMap.toString() + "\n");
            writer.write("#--------------------\n");
            writer.write("#Co-speciation cost = "
                         + format.format(configuration.getCospeciationCost()
                                         / (double)configuration.getDividerMultiplier()) + "\n");
            writer.write("#Duplication cost   = "
                         + format.format(configuration.getDuplicationCost()
                                         / (double)configuration.getDividerMultiplier()) + "\n");
            writer.write("#Host-switch cost   = "
                         + format.format(configuration.getHostSwitchCost()
                                         / (double)configuration.getDividerMultiplier()) + "\n");
            writer.write("#Loss cost          = "
                         + format.format(configuration.getLossCost()
                                         / (double)configuration.getDividerMultiplier()) + "\n");
            if (configuration.getMaximumJump() == TaskConfiguration.DEFAULT_MAXIMUM_JUMP) {
                writer.write("#Maximum jump       = No limit\n");
            } else {
                writer.write("#Maximum jump       = " + configuration.getMaximumJump() + "\n");
            }
            writer.write("#Optimum cost       = " + optimumCostString + "\n");
            writer.write("#--------------------\n");
        }

        System.out.println("Optimum cost = " + optimumCostString);
        System.out.println("--------------------");

    }


    /**
     * Runs the reconciliation algorithm and return the cells with optimum cost.
     * 
     * @param configuration
     *        Configuration object.
     * @param hostTree
     *        Host tree.
     * @param parasiteTree
     *        Parasite tree.
     * @param leafMap
     *        Mapping of the parasite leaf nodes into the host leaf nodes.
     * @return The cells which contains the optimum solutions.
     */
    private static NestedSolution[] runReconciliator(TaskConfiguration configuration,
                                                     Tree hostTree,
                                                     Tree parasiteTree,
                                                     HashMap<TreeNode, TreeNode> leafMap) {

        boolean countWhileFillingMatrix = (configuration.getTask() == Task.Count)
                                          || (configuration.getTask() != Task.Count && configuration.getRandomSampling());

        Reconciliator reconciliator = new Reconciliator(hostTree,
                                                        parasiteTree,
                                                        leafMap,
                                                        configuration.getCospeciationCost(),
                                                        configuration.getDuplicationCost(),
                                                        configuration.getHostSwitchCost(),
                                                        configuration.getLossCost(),
                                                        configuration.getMaximumJump(),
                                                        countWhileFillingMatrix);
        NestedSolution[] solutions = null;
        if (configuration.getRootToRoot()) {
            NestedSolution parasiteRootMapping = reconciliator.runRootToRootReconciliator();
            solutions = new NestedSolution[1];
            solutions[0] = parasiteRootMapping;
        } else {
            solutions = reconciliator.runReconciliator();
        }

        return solutions;
    }


    /**
     * Runs the procedure to count the total number of solutions for the reconciliation of host and
     * parasite trees.
     * 
     * @param configuration
     *        Object that handles the configuration of the procedure.
     * @throws NexusFileParserException
     *         When something wrong happens while reading the nexus file.
     * @throws IOException
     *         When something wrong happens while writing the output.
     */
    protected static void countSolutions(TaskConfiguration configuration) throws NexusFileParserException,
                                                                         IOException {

        NexusFileParser parser = new NexusFileParser(configuration.getInputFile());
        Tree hostTree = parser.getHost();
        Tree parasiteTree = parser.getParasite();
        HashMap<TreeNode, TreeNode> leafMap = getLeafMap(hostTree, parasiteTree, parser);

        NestedSolution[] solutions = runReconciliator(configuration,
                                                      hostTree,
                                                      parasiteTree,
                                                      leafMap);

        long optimumCost = solutions[0].getCost();
        printHeader(null, configuration, optimumCost, hostTree, parasiteTree, leafMap);

        BigInteger totalNumber = BigInteger.ZERO;
        for (NestedSolution solution: solutions) {
            System.out.println("Root mapping = " + solution);
            BigInteger number = solution.getSubsolutions();
            System.out.println("Number of solutions = " + number);
            totalNumber = totalNumber.add(number);
            System.out.println("--------------------");
        }
        System.out.println("Total number of solutions = " + totalNumber);

    }


    /**
     * Runs the procedure to enumerate solutions.
     * 
     * @param configuration
     *        Object that handles the configuration of the procedure.
     * @throws NexusFileParserException
     *         When something wrong happens while reading the nexus file.
     * @throws IOException
     *         When something wrong happens while writing the output.
     */
    protected static void enumerate(TaskConfiguration configuration) throws NexusFileParserException,
                                                                    IOException {

        NexusFileParser parser = new NexusFileParser(configuration.getInputFile());
        Tree hostTree = parser.getHost();
        Tree parasiteTree = parser.getParasite();
        HashMap<TreeNode, TreeNode> leafMap = getLeafMap(hostTree, parasiteTree, parser);

        NestedSolution[] solutions = runReconciliator(configuration,
                                                      hostTree,
                                                      parasiteTree,
                                                      leafMap);

        long optimumCost = solutions[0].getCost();
        FileWriter writer = new FileWriter(configuration.getOutputFile());
        printHeader(writer, configuration, optimumCost, hostTree, parasiteTree, leafMap);

        BigInteger limit = null;
        if (configuration.getNumberOfSolutions() != null) {
            limit = BigInteger.valueOf(configuration.getNumberOfSolutions());
        }

        if (!configuration.getRandomSampling()
            || (configuration.getRandomSampling() && limit != null && limit.compareTo(solutions[0].getSubsolutions()) >= 0)) {

            BigInteger totalNumber = BigInteger.ZERO;

            for (NestedSolution solution: solutions) {

                System.out.println("Root mapping = " + solution);
                writer.write("#Root mapping = " + solution + "\n");

                NestedSolutionEnumerator enumerator = new NestedSolutionEnumerator(solution, writer);

                BigInteger number = enumerator.enumerate(limit);

                System.out.println("Number of solutions = " + number);
                writer.write("#Number of solutions = " + number + "\n");

                totalNumber = totalNumber.add(number);

                System.out.println("--------------------");
                writer.write("#--------------------\n");

                if (limit != null) {
                    limit = limit.subtract(number);
                    if (limit.compareTo(BigInteger.ZERO) <= 0) {
                        break;
                    }
                }
            }

            System.out.println("Total number of solutions = " + totalNumber);
            writer.write("#Total number of solutions = " + totalNumber + "\n");

        } else {

            NestedSolutionRandomEnumerator randomEnumerator = new NestedSolutionRandomEnumerator(solutions,
                                                                                                 writer);
            randomEnumerator.enumerate(limit);
            System.out.println("Number of sampled solutions = " + limit);
            writer.write("#Number of sampled solutions = " + limit + "\n");
            System.out.println("--------------------");
            writer.write("#--------------------\n");

        }

        writer.flush();
        writer.close();

    }


    /**
     * Runs the procedure to enumerate solutions and compute statistics.
     * 
     * @param configuration
     *        Object that handles the configuration of the procedure.
     * @throws NexusFileParserException
     *         When something wrong happens while reading the nexus file.
     * @throws IOException
     *         When something wrong happens while writing the output.
     */
    protected static void enumerateWithStatistics(TaskConfiguration configuration) throws NexusFileParserException,
                                                                                  IOException {

        NexusFileParser parser = new NexusFileParser(configuration.getInputFile());
        Tree hostTree = parser.getHost();
        Tree parasiteTree = parser.getParasite();
        HashMap<TreeNode, TreeNode> leafMap = getLeafMap(hostTree, parasiteTree, parser);

        NestedSolution[] solutions = runReconciliator(configuration,
                                                      hostTree,
                                                      parasiteTree,
                                                      leafMap);

        long optimumCost = solutions[0].getCost();
        FileWriter writer = null;
        if (configuration.getOutputFile() != null) {
            writer = new FileWriter(configuration.getOutputFile());
        }
        printHeader(writer, configuration, optimumCost, hostTree, parasiteTree, leafMap);

        BigInteger limit = null;
        if (configuration.getNumberOfSolutions() != null) {
            limit = BigInteger.valueOf(configuration.getNumberOfSolutions());
        }

        if (!configuration.getRandomSampling()
            || (configuration.getRandomSampling() && limit != null && limit.compareTo(solutions[0].getSubsolutions()) >= 0)) {

            BigInteger totalNumber = BigInteger.ZERO;
            HashMap<ArrayList<Integer>, Integer> eventVectorCount = new HashMap<ArrayList<Integer>, Integer>();

            for (NestedSolution solution: solutions) {

                System.out.println("Root mapping = " + solution);
                if (writer != null) {
                    writer.write("#Root mapping = " + solution + "\n");
                }

                NestedSolutionEnumerator enumerator = new NestedSolutionEnumerator(solution, writer);
                BigInteger number = enumerator.enumerateWithStatistics(limit, eventVectorCount);

                System.out.println("Number of solutions = " + number);
                if (writer != null) {
                    writer.write("#Number of solutions = " + number + "\n");
                }

                totalNumber = totalNumber.add(number);

                System.out.println("--------------------");
                if (writer != null) {
                    writer.write("#--------------------\n");
                }

                if (limit != null) {
                    limit = limit.subtract(number);
                    if (limit.compareTo(BigInteger.ZERO) <= 0) {
                        break;
                    }
                }
            }

            System.out.println("All root mappings");
            Util.calculateAverage(eventVectorCount, totalNumber, "");
            System.out.println("Total number of solutions = " + totalNumber);
            if (writer != null) {
                writer.write("#Total number of solutions = " + totalNumber + "\n");
                writer.flush();
                writer.close();
            }

        } else {

            HashMap<ArrayList<Integer>, Integer> eventVectorCount = new HashMap<ArrayList<Integer>, Integer>();
            NestedSolutionRandomEnumerator randomEnumerator = new NestedSolutionRandomEnumerator(solutions,
                                                                                                 writer);
            randomEnumerator.enumerateWithStatistics(limit, eventVectorCount);
            Util.calculateAverage(eventVectorCount, limit, "");
            System.out.println("Number of sampled solutions = " + limit);
            System.out.println("--------------------");
            if (writer != null) {
                writer.write("#Number of sampled solutions = " + limit + "\n");
                writer.write("#--------------------\n");
                writer.flush();
                writer.close();
            }

        }

    }

}
