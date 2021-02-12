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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import fr.inria.bamboo.eucalypt.parser.NexusFileParserException;

public class TaskConfiguration {

    /** Enumeration of types of NestedSolution Nodes. */
    public static enum Task {
        Count, CleanCyclic, Enumeration, EnumerationStatistics;
    }


    /** Default Co-speciation cost. */
    public static final double DEFAULT_COSPECIATION_COST = -1.000;

    /** Default Duplication cost. */
    public static final double DEFAULT_DUPLICATION_COST = 1.000;

    /** Default Host-Switch cost. */
    public static final double DEFAULT_HOSTSWITCH_COST = 1.000;

    /** Default Loss cost. */
    public static final double DEFAULT_LOSS_COST = 1.000;

    /** Default Precision. */
    public static final int DEFAULT_PRECISION = 3;

    /** Default Maximum Jump. */
    public static final int DEFAULT_MAXIMUM_JUMP = Integer.MAX_VALUE;

    /** Default root-to-root mapping. */
    public static final boolean DEFAULT_ROOT_TO_ROOT = false;

    /** Default random-sampling. */
    public static final boolean DEFAULT_RANDOM_SAMPLING = false;

    /** Default cyclicity test. */
    public static final int DEFAULT_CYCLICITY_TEST = Cleaner.STOLZER;

    /** Desired task. */
    private Task task;

    /** Precision (number of decimal places to consider in the cost value) */
    private int precision;

    /** Co-speciation cost. */
    private long cospeciationCost;

    /** Duplication cost. */
    private long duplicationCost;

    /** Host-Switch cost. */
    private long hostSwitchCost;

    /** Loss cost. */
    private long lossCost;

    /** Divider/Multiplier to convert double to long and vice-versa. */
    private int divider_multiplier;

    /** Input File Name. */
    private String inputFile;

    /** Output File Name. */
    private String outputFile;

    /** Number of solutions to be printed. */
    private Long numberOfSolutions;

    /** Maximum Jump. */
    private Integer maximumJump;

    /** Root-to-root mapping. */
    private boolean rootToRoot;

    /** Random sampling. */
    private boolean randomSampling;

    /** Cyclicity test. */
    private int cyclicityTest;

    /** Number formatter. */
    private NumberFormat format;


    /**
     * Create a task manager filled with the default parameters.
     */
    public TaskConfiguration() {
        this.task = Task.Count;
        this.precision = DEFAULT_PRECISION;
        this.divider_multiplier = (int)Math.pow(10, this.precision);
        this.cospeciationCost = (long)(DEFAULT_COSPECIATION_COST * this.divider_multiplier);
        this.duplicationCost = (long)(DEFAULT_DUPLICATION_COST * this.divider_multiplier);
        this.hostSwitchCost = (long)(DEFAULT_HOSTSWITCH_COST * this.divider_multiplier);
        this.lossCost = (long)(DEFAULT_LOSS_COST * this.divider_multiplier);
        this.inputFile = null;
        this.outputFile = null;
        this.numberOfSolutions = null;
        this.maximumJump = DEFAULT_MAXIMUM_JUMP;
        this.rootToRoot = DEFAULT_ROOT_TO_ROOT;
        this.randomSampling = DEFAULT_RANDOM_SAMPLING;
        this.cyclicityTest = DEFAULT_CYCLICITY_TEST;
        this.format = NumberFormat.getInstance(Locale.US);
        this.format.setMinimumFractionDigits(precision);
        this.format.setMaximumFractionDigits(precision);
    }


    /**
     * Returns the co-speciation cost value.
     */
    public long getCospeciationCost() {
        return cospeciationCost;
    }


    /**
     * Returns the cyclicity test identifier.
     */
    public int getCyclicityTest() {
        return cyclicityTest;
    }


    /**
     * Returns the duplication cost value.
     */
    public long getDuplicationCost() {
        return duplicationCost;
    }


    /**
     * Returns the divider/multiplier number.
     * 
     * @return The divider/multiplier number.
     */
    protected int getDividerMultiplier() {
        return divider_multiplier;
    }


    /**
     * Returns the number formatter.
     * 
     * @return The number formatter.
     */
    protected NumberFormat getFormatter() {
        return format;
    }


    /**
     * Returns the host-switch cost value.
     */
    public long getHostSwitchCost() {
        return hostSwitchCost;
    }


    /**
     * Returns the input file name.
     */
    public String getInputFile() {
        return inputFile;
    }


    /**
     * Returns the loss cost value.
     */
    public long getLossCost() {
        return lossCost;
    }


    /**
     * Returns the maximum jump limit value.
     */
    public int getMaximumJump() {
        return maximumJump;
    }


    /**
     * Returns the number of solutions limit.
     */
    public Long getNumberOfSolutions() {
        return numberOfSolutions;
    }


    /**
     * Returns the output file name.
     */
    public String getOutputFile() {
        return outputFile;
    }


    /**
     * Returns the precision value.
     */
    public int getPrecision() {
        return precision;
    }


    /**
     * Returns the random sampling option value.
     */
    public boolean getRandomSampling() {
        return randomSampling;
    }


    /**
     * Returns the root-to-root mapping option value.
     */
    public boolean getRootToRoot() {
        return rootToRoot;
    }


    /**
     * Returns the task value.
     */
    public Task getTask() {
        return task;
    }


    /**
     * Set the co-speciation cost argument.
     * 
     * @param cospeciationCost
     *        The new co-speciation cost value.
     */
    public void setCospeciationCost(double cospeciationCost) {
        this.cospeciationCost = (long)(cospeciationCost * this.divider_multiplier);
    }


    /**
     * Set the cyclicity test argument.
     * 
     * @param cospeciationCost
     *        The new cyclicity test identifier.
     */
    public void setCyclicityTest(int cyclicityTest) {
        this.cyclicityTest = cyclicityTest;
    }


    /**
     * Set the duplication cost argument.
     * 
     * @param duplicationCost
     *        The new duplication cost value.
     */
    public void setDuplicationCost(double duplicationCost) {
        this.duplicationCost = (long)(duplicationCost * this.divider_multiplier);
    }


    /**
     * Set the host-switch cost argument.
     * 
     * @param hostSwitchCost
     *        The new host-switch cost value.
     */
    public void setHostSwitchCost(double hostSwitchCost) {
        this.hostSwitchCost = (long)(hostSwitchCost * this.divider_multiplier);
    }


    /**
     * Set the input file name.
     * 
     * @param inputFile
     *        Input file name.
     */
    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }


    /**
     * Set the loss cost argument.
     * 
     * @param lossCost
     *        The new loss cost value.
     */
    public void setLossCost(double lossCost) {
        this.lossCost = (long)(lossCost * this.divider_multiplier);
    }


    /**
     * Set the maximum jump limit value.
     * 
     * @param maximumJump
     *        The new maximum jump limit value.
     */
    public void setMaximumJump(int maximumJump) {
        this.maximumJump = maximumJump;
    }


    /**
     * Set the number of solutions.
     * 
     * @param numberOfSolutions
     *        The new number of solutions limit value.
     */
    public void setNumberOfSolutions(long numberOfSolutions) {
        this.numberOfSolutions = numberOfSolutions;
    }


    /**
     * Set the output file name.
     * 
     * @param outputFile
     *        Output file name.
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }


    /**
     * Set the precision argument.
     * 
     * @param precision
     *        The new precision value.
     */
    public void setPrecision(int precision) {
        this.precision = precision;
        this.divider_multiplier = (int)Math.pow(10, this.precision);
        this.format.setMinimumFractionDigits(precision);
        this.format.setMaximumFractionDigits(precision);
    }


    /**
     * Set the random sampling option.
     * 
     * @param rootToRoot
     *        The new random sampling option value.
     */
    public void setRandomSampling(boolean randomSampling) {
        this.randomSampling = randomSampling;
    }


    /**
     * Set the root-to-root mapping option.
     * 
     * @param rootToRoot
     *        The new root-to-root mapping option value.
     */
    public void setRootToRoot(boolean rootToRoot) {
        this.rootToRoot = rootToRoot;
    }


    /**
     * Set the task argument.
     * 
     * @param task
     *        The new task value.
     */
    public void setTask(Task task) {
        this.task = task;
    }


    /**
     * Prints the task configuration.
     */
    private void printConfiguration() {

        System.out.println("---------------------------------------------------------------------");

        if (task == Task.Count) {
            System.out.println("Task 1 - Count the total number of solutions.");
        } else if (task == Task.Enumeration) {
            System.out.println("Task 2 - Enumerate solutions.");
        } else if (task == Task.EnumerationStatistics) {
            System.out.println("Task 3 - Enumerate solutions and produce some statistics.");
        } else {
            System.out.println("Task 4 - Filter out cyclic solutions from enumerator output.");
        }

        System.out.println("Input file         : " + inputFile);

        if (outputFile != null) {
            System.out.println("Output file        : " + outputFile);
        }

        if (task != Task.CleanCyclic) {
            System.out.println("Co-speciation cost : "
                               + format.format(cospeciationCost / (double)divider_multiplier));
            System.out.println("Duplication cost   : "
                               + format.format(duplicationCost / (double)divider_multiplier));
            System.out.println("Host-switch cost   : "
                               + format.format(hostSwitchCost / (double)divider_multiplier));
            System.out.println("Loss cost          : "
                               + format.format(lossCost / (double)divider_multiplier));

            if (maximumJump == DEFAULT_MAXIMUM_JUMP) {
                System.out.println("Maximum jump       : No limit");
            } else {
                System.out.println("Maximum jump       : " + maximumJump);
            }

            if (rootToRoot == DEFAULT_ROOT_TO_ROOT) {
                System.out.println("Root-to-root       : No");
            } else {
                System.out.println("Root-to-root       : Yes");
            }

        }

        if (task == Task.Enumeration || task == Task.EnumerationStatistics) {
            if (randomSampling == DEFAULT_RANDOM_SAMPLING) {
                System.out.println("Random sampling    : No");
            } else {
                System.out.println("Random sampling    : Yes");
            }
            if (numberOfSolutions == null) {
                System.out.println("Number of solutions: No limit");
            } else {
                System.out.println("Number of solutions: " + numberOfSolutions);
            }
        }

        if (task == Task.CleanCyclic) {
            if (cyclicityTest == DEFAULT_CYCLICITY_TEST) {
                System.out.println("Cyclicity Test     : Stolzer's cyclicity test");
            } else {
                System.out.println("Cyclicity Test     : Tofigh's cyclicity test");
            }
        }

        System.out.println("---------------------------------------------------------------------");

    }


    /**
     * Executes the desired task.
     * 
     * @throws IOException
     *         When something goes wrong while reading the file.
     * @throws NexusFileParserException
     *         When something goes wrong while parsing the file.
     */
    public void run() throws IOException, NexusFileParserException {
        printConfiguration();
        if (task == Task.Count) {
            Enumerator.countSolutions(this);
        } else if (task == Task.Enumeration) {
            Enumerator.enumerate(this);
        } else if (task == Task.EnumerationStatistics) {
            Enumerator.enumerateWithStatistics(this);
        } else {
            Cleaner.cleanCyclicSolutions(this);
        }

    }

}
