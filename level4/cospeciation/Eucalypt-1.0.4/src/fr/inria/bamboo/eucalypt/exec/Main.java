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
package fr.inria.bamboo.eucalypt.exec;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import fr.inria.bamboo.eucalypt.parser.NexusFileParserException;
import fr.inria.bamboo.eucalypt.process.Cleaner;
import fr.inria.bamboo.eucalypt.process.TaskConfiguration;
import fr.inria.bamboo.eucalypt.process.TaskConfiguration.Task;

public class Main {

    /* -:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:- */
    public static void main(String[] args) {
        TaskConfiguration taskManager = new TaskConfiguration();

        Options opts = createOptions();
        String programUsage = "java -jar Eucalypt.jar -i <inputfile> [options]";
        try {
            if (processParameters(taskManager, args, opts, programUsage)) {
                taskManager.run();
            }
        } catch (UnrecognizedOptionException uoe) {
            System.out.println(uoe.getMessage());
            HelpFormatter f = new HelpFormatter();
            f.setWidth(120);
            f.printHelp(programUsage, opts);
        } catch (NexusFileParserException e) {
            System.out.println(e.getMessage());
            HelpFormatter f = new HelpFormatter();
            f.setWidth(120);
            f.printHelp(programUsage, opts);
        } catch (Exception e) {
            System.out.println("Unknown error:");
            e.printStackTrace();
            HelpFormatter f = new HelpFormatter();
            f.setWidth(120);
            f.printHelp(programUsage, opts);
        }
    }


    /* -:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:- */
    @SuppressWarnings("static-access")
    private static Options createOptions() {

        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setMinimumFractionDigits(TaskConfiguration.DEFAULT_PRECISION);
        format.setMaximumFractionDigits(TaskConfiguration.DEFAULT_PRECISION);

        String description = null;
        Options opts = new Options();

        /* -------------------------------------------------------------------------------------- */
        /* Help */
        opts.addOption("h", false, "Print the help message.\n-\n");

        /* -------------------------------------------------------------------------------------- */
        /* Cospeciation cost value */
        description = "Co-speciation cost. Default value = "
                      + format.format(TaskConfiguration.DEFAULT_COSPECIATION_COST)
                      + "\nThis parameter is optional for tasks 1, 2 and 3 and\n"
                      + "is ignored by task 4.\n-\n";
        Option cc = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("cc");
        opts.addOption(cc);

        /* -------------------------------------------------------------------------------------- */
        /* Duplication cost value */
        description = "Duplication cost. Default value = "
                      + format.format(TaskConfiguration.DEFAULT_DUPLICATION_COST)
                      + "\nThis parameter is optional for tasks 1, 2 and 3 and\n"
                      + "is ignored by task 4.\n-\n";
        Option cd = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("cd");
        opts.addOption(cd);

        /* -------------------------------------------------------------------------------------- */
        /* Host-switch cost value */
        description = "Host-switch cost. Default value = "
                      + format.format(TaskConfiguration.DEFAULT_HOSTSWITCH_COST)
                      + "\nThis parameter is optional for tasks 1, 2 and 3 and\n"
                      + "is ignored by task 4.\n-\n";
        Option ch = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("ch");
        opts.addOption(ch);

        /* -------------------------------------------------------------------------------------- */
        /* Loss cost value */
        description = "Loss cost. Default value = "
                      + format.format(TaskConfiguration.DEFAULT_LOSS_COST)
                      + "\nThis parameter is optional for tasks 1, 2 and 3 and\n"
                      + "is ignored by task 4.\n-\n";
        Option cl = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("cl");
        opts.addOption(cl);

        /* -------------------------------------------------------------------------------------- */
        /* Precision value */
        description = "Precision value. This parameter defines the number of\n"
                      + "decimal places that are going to be considered on\n"
                      + "each given event cost. For example, if co-speciation\n"
                      + "cost is equal to 0.0123 and precision is equal to 3,\n"
                      + "the considered cost will be 0.012. This parameter is\n"
                      + "optional for tasks 1, 2 and 3 and is ignored by task 4.\n"
                      + "Default value = " + TaskConfiguration.DEFAULT_PRECISION + "\n-\n";
        Option p = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("p");
        opts.addOption(p);

        /* -------------------------------------------------------------------------------------- */
        /* Task value */
        description = "Task to be performed:\n" + "1 - Count the total number of solutions.\n"
                      + "2 - Enumerate solutions.\n"
                      + "3 - Enumerate solutions and produce some statistics.\n"
                      + "4 - Filter out cyclic solutions from the output of tasks 2 or 3.\n"
                      + "Default task = 1\n-\n";
        Option task = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("task");
        opts.addOption(task);

        /* -------------------------------------------------------------------------------------- */
        /* Task value */
        description = "Cyclicity test model:\n"
                      + "1 - Stolzer et al., 2012\n"
                      + "2 - Tofigh et al., 2011\n"
                      + "Default cyclicity test = "
                      + TaskConfiguration.DEFAULT_CYCLICITY_TEST
                      + "\nThis parameter is optional for task 4 and ignored by all other tasks.\n-\n";
        Option test = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("test");
        opts.addOption(test);

        /* -------------------------------------------------------------------------------------- */
        /* Number of solutions */
        description = "Number of reconciliation scenarios to be output.\n"
                      + "By default, Eucalypt enumerates all reconciliation\n"
                      + "scenarios. This parameter defines a limit for the\n"
                      + "number of reconciliation scenarios that will be\n"
                      + "written to the output file. This parameter is\n"
                      + "optional for tasks 2 and 3, except if the option\n"
                      + "-random is given (the parameter becomes mandatory).\n"
                      + "It is ignored by tasks 1 and 4.\n" + "Default value = Infinite.\n-\n";
        Option n = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("n");
        opts.addOption(n);

        /* -------------------------------------------------------------------------------------- */
        /* Random */
        description = "Random sampling. By default, Eucalypt traverses the tree\n"
                      + "of solutions in a sequential way. If this parameter is\n"
                      + "given, Eucalypt performs a random sampling of the space\n"
                      + "of reconcliation scenarios. This parameter is optional\n"
                      + "for the tasks 2 and 3 and is ignored by tasks 1 and 4.\n"
                      + "If this parameter is given, the user must also set the\n"
                      + "parameter -n (Number of reconciliation scenarios to be\n"
                      + "output).\n-\n";
        opts.addOption("random", false, description);

        /* -------------------------------------------------------------------------------------- */
        /* Root-to-root mapping */
        description = "Root-to-root mapping. By default, Eucalypt outputs all\n"
                      + "optimal reconciliation scenarios independently of the\n"
                      + "mapping of the parasite root node. If this parameter\n"
                      + "is given, Eucalypt just counts/enumerates reconciliation\n"
                      + "scenarios which map the parasite tree root node to the\n"
                      + "host tree root node. This parameter is optional for\n"
                      + "tasks 1, 2 and 3.\n-\n";
        opts.addOption("root", false, description);

        /* -------------------------------------------------------------------------------------- */
        /* Input */
        description = "Input file. This parameter is mandatory for all tasks.\n"
                      + "For tasks 1, 2, and 3, Eucalypt receives a nexus file\n"
                      + "as input file. For task 4, Eucalypt receives, as input\n"
                      + "file, the output file produced by tasks 2 or 3.\n-\n";
        Option input = OptionBuilder.withArgName("file").hasArgs(1).withValueSeparator().withDescription(description).create("i");
        opts.addOption(input);

        /* -------------------------------------------------------------------------------------- */
        /* Output */
        description = "Output file. This parameter is ignored by task 1, is\n"
                      + "mandatory for tasks 2 and 4 and is optional for task 3.\n-\n";
        Option output = OptionBuilder.withArgName("file").hasArgs(1).withValueSeparator().withDescription(description).create("o");
        opts.addOption(output);

        /* -------------------------------------------------------------------------------------- */
        /* Maximum jump */
        description = "Maximum host-switch distance. This parameter defines the\n"
                      + "maximum host-switch distance which is allowed. It is\n"
                      + "optional for tasks 1, 2 and 3 and is ignored by task 4.\n"
                      + "Default value = Infinite.\n-\n";
        Option j = OptionBuilder.withArgName("value").hasArgs(1).withValueSeparator().withDescription(description).create("j");
        opts.addOption(j);

        return opts;
    }


    /* -:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:-:- */
    private static boolean processParameters(TaskConfiguration taskManager,
                                             String[] args,
                                             Options opts,
                                             String usageString) throws UnrecognizedOptionException,
                                                                ParseException {

        BasicParser bp = new BasicParser();
        CommandLine cl = bp.parse(opts, args);
        if (cl.hasOption("h")) {
            HelpFormatter f = new HelpFormatter();
            f.setWidth(120);
            f.printHelp(usageString, opts);
            return false;
        }

        /* -------------------------------------------------------------------------------------- */
        /* Precision value */
        if (cl.hasOption("p")) {
            int precision = Integer.parseInt(cl.getOptionValue("p"));
            if (precision <= 0) {
                throw new UnrecognizedOptionException("Invalid value for option -p");
            }
            taskManager.setPrecision(precision);
        }

        /* -------------------------------------------------------------------------------------- */
        /* Cospeciation cost value */
        if (cl.hasOption("cc")) {
            taskManager.setCospeciationCost(Double.parseDouble(cl.getOptionValue("cc")));
        }

        /* -------------------------------------------------------------------------------------- */
        /* Duplication cost value */
        if (cl.hasOption("cd")) {
            taskManager.setDuplicationCost(Double.parseDouble(cl.getOptionValue("cd")));
        }

        /* -------------------------------------------------------------------------------------- */
        /* Host-switch cost value */
        if (cl.hasOption("ch")) {
            taskManager.setHostSwitchCost(Double.parseDouble(cl.getOptionValue("ch")));
        }

        /* -------------------------------------------------------------------------------------- */
        /* Loss cost value */
        if (cl.hasOption("cl")) {
            taskManager.setLossCost(Double.parseDouble(cl.getOptionValue("cl")));
        }

        /* -------------------------------------------------------------------------------------- */
        /* Task value */
        if (cl.hasOption("task")) {
            int task = Integer.parseInt(cl.getOptionValue("task"));
            switch (task) {
                case 1:
                    taskManager.setTask(Task.Count);
                    break;
                case 2:
                    taskManager.setTask(Task.Enumeration);
                    break;
                case 3:
                    taskManager.setTask(Task.EnumerationStatistics);
                    break;
                case 4:
                    taskManager.setTask(Task.CleanCyclic);
                    break;
                default:
                    throw new UnrecognizedOptionException("Invalid value for option -task");
            }
        }

        /* -------------------------------------------------------------------------------------- */
        /* Cyclicity test */
        if (cl.hasOption("test")) {
            if (taskManager.getTask() == Task.CleanCyclic) {
                int test = Integer.parseInt(cl.getOptionValue("test"));
                switch (test) {
                    case Cleaner.STOLZER:
                    case Cleaner.TOFIGH:
                        taskManager.setCyclicityTest(test);
                        break;
                    default:
                        throw new UnrecognizedOptionException("Invalid value for option -test");
                }
            }
        }

        /* -------------------------------------------------------------------------------------- */
        /* Number of solutions */
        if (cl.hasOption("n")) {
            long n = Long.parseLong(cl.getOptionValue("n"));
            if (n > 0) {
                taskManager.setNumberOfSolutions(n);
            } else {
                throw new UnrecognizedOptionException("Invalid value for option -n");
            }
        }

        /* -------------------------------------------------------------------------------------- */
        /* Random */
        taskManager.setRandomSampling(cl.hasOption("random"));

        /* -------------------------------------------------------------------------------------- */
        /* Root-to-root mapping */
        taskManager.setRootToRoot(cl.hasOption("root"));

        /* -------------------------------------------------------------------------------------- */
        /* Input */
        if (cl.hasOption("i")) {
            taskManager.setInputFile(cl.getOptionValue("i"));
        } else {
            throw new UnrecognizedOptionException("Missing input file.");
        }

        /* -------------------------------------------------------------------------------------- */
        /* Output */
        if (cl.hasOption("o")) {
            taskManager.setOutputFile(cl.getOptionValue("o"));
        }

        /* -------------------------------------------------------------------------------------- */
        /* Maximum jump */
        if (cl.hasOption("j")) {
            int maximumJump = Integer.parseInt(cl.getOptionValue("j"));
            if (maximumJump <= 0) {
                throw new UnrecognizedOptionException("Invalid value for option -j");
            }
            taskManager.setMaximumJump(maximumJump);
        }

        /* -------------------------------------------------------------------------------------- */
        /* Additional verifications */

        /* Output file. */
        if (taskManager.getOutputFile() == null) {
            if (taskManager.getTask() == Task.CleanCyclic
                || taskManager.getTask() == Task.Enumeration) {
                throw new UnrecognizedOptionException("Missing output file");
            }
        }

        if (taskManager.getRandomSampling() && taskManager.getNumberOfSolutions() == null) {
            if (taskManager.getTask() == Task.EnumerationStatistics
                || taskManager.getTask() == Task.Enumeration) {
                throw new UnrecognizedOptionException("For random sampling, the parameter -n must be specified.");
            }
        }

        return true;
    }

}
