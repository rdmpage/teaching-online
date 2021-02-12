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
package fr.inria.bamboo.eucalypt.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import fr.inria.bamboo.eucalypt.trees.Tree;
import fr.inria.bamboo.eucalypt.trees.TreeNode;

public class NexusFileParser {

    private final char OPEN_BRACKET = '(';

    private final char CLOSE_BRACKET = ')';

    private final char CHILD_SEPARATOR = ',';

    /** Host tree. */
    private Tree host;

    /** Parasite Tree. */
    private Tree parasite;

    /**
     * Map that translates the labels that appear in the newick representation of the tree into
     * their real label names.
     */
    private HashMap<String, String> translateNewickToReal;

    /**
     * Map that translates the real label names into the labels that appear in the newick
     * representation of the tree.
     */
    private HashMap<String, String> translateRealToNewick;

    /** Phi function: Parasite Leaves mapped into Host Leaves using real names. */
    private HashMap<String, String> phiRealNames;

    /** Phi function: Parasite Leaves mapped into Host Leaves using newick names. */
    private HashMap<String, String> phiNewickNames;


    /**
     * Constructor: Create a NexusFileParser Object filled with the content of the specified file.
     * 
     * @param fileName
     * @throws IOException
     *         When something wrongs happens while reading the file.
     * @throws NexusFileParserException
     *         When some unexpected feature is found while parsing the file.
     */
    public NexusFileParser(String fileName) throws IOException, NexusFileParserException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new IOException("File not found: " + fileName);
        }
        parseFile(file);
    }


    /**
     * Parse the given nexus file.
     * 
     * @param file
     *        File to be parsed.
     * @throws IOException
     *         When something wrongs happens while reading the file.
     * @throws NexusFileParserException
     *         When some unexpected feature is found while parsing the file.
     */
    private void parseFile(File file) throws IOException, NexusFileParserException {

        boolean success = false;
        BufferedReader reader = new BufferedReader(new FileReader(file));

        try {

            String line = reader.readLine();
            while (line != null) {
                line = line.trim().replaceAll(" +", " ");
                if (line.toLowerCase().contains("begin host")
                    || line.toLowerCase().contains("begin parasite")) {
                    parseStandardNexus(line, reader);
                    success = true;
                    break;
                } else if (line.toLowerCase().contains("begin trees")) {
                    parseCoRePaNexus(reader);
                    success = true;
                    break;
                }
                line = reader.readLine();
            }

        } finally {
            reader.close();
        }

        if (!success) {
            throw new NexusFileParserException("The program was not able to extract information from the given nexus file.");
        }

    }


    /**
     * Parse a standard nexus file.
     * 
     * @param lastLine
     *        Last line that was read.
     * @param reader
     *        Buffered reader object that handles the file.
     * @throws IOException
     *         When something wrongs happens while reading the file.
     * @throws NexusFileParserException
     *         When some unexpected feature is found while parsing the file.
     */
    private void parseStandardNexus(String lastLine, BufferedReader reader) throws IOException,
                                                                           NexusFileParserException {

        String hostTreeString = "";
        String parasiteTreeString = "";

        if (lastLine.toLowerCase().contains("begin host")) {
            hostTreeString = getTreeString(reader);
            String line = reader.readLine();
            while (line != null) {
                line = line.trim().replaceAll(" +", " ");
                if (line.toLowerCase().contains("begin parasite")) {
                    parasiteTreeString = getTreeString(reader);
                    break;
                }
                line = reader.readLine();
            }
        } else {
            parasiteTreeString = getTreeString(reader);
            String line = reader.readLine();
            while (line != null) {
                line = line.trim().replaceAll(" +", " ");
                if (line.toLowerCase().contains("begin host")) {
                    hostTreeString = getTreeString(reader);
                    break;
                }
                line = reader.readLine();
            }
        }

        if (hostTreeString.equals("")) {
            throw new NexusFileParserException("Could not read host tree from nexus file.");
        }

        if (parasiteTreeString.equals("")) {
            throw new NexusFileParserException("Could not read parasite tree from nexus file.");
        }

        String line = reader.readLine();
        while (line != null && !line.toLowerCase().contains("range")) {
            line = reader.readLine();
        }

        if (line == null) {
            throw new NexusFileParserException("Unexpected end of file while searching for DISTRIBUTION section. ");
        }

        String distribution = "";
        line = reader.readLine();
        while (line != null && !line.toLowerCase().contains("endblock;")
               && !line.toLowerCase().contains("end;")) {
            if (!isCommentLine(line)) {
                distribution += line.trim().replaceAll(" +", " ");
            }
            line = reader.readLine();
        }

        if (line == null) {
            throw new NexusFileParserException("Unexpected end of file while reading DISTRIBUTION section.");
        }

        phiRealNames = new HashMap<String, String>();
        phiNewickNames = new HashMap<String, String>();

        String[] arrayDistribution = distribution.replaceAll(";", "").trim().split(",");
        for (String pairString: arrayDistribution) {

            String[] pair = pairString.split(":");
            String pLabel = pair[0].trim();
            String hLabel = pair[1].trim();

            if (phiRealNames.containsKey(pLabel) && !phiRealNames.get(pLabel).equals(hLabel)) {
                throw new NexusFileParserException("A parasite label cannot be associated to two differents host labels.");
            } else {
                phiRealNames.put(pLabel, hLabel);
                phiNewickNames.put(pLabel, hLabel);
            }

        }

        host = parseNewickString(hostTreeString, "!H");
        parasite = parseNewickString(parasiteTreeString, "!P");

        translateNewickToReal = new HashMap<String, String>();
        translateRealToNewick = new HashMap<String, String>();

        for (TreeNode node: host.nodes) {
            translateNewickToReal.put(node.getLabel(), node.getLabel());
            translateRealToNewick.put(node.getLabel(), node.getLabel());
        }

        for (TreeNode node: parasite.nodes) {
            translateNewickToReal.put(node.getLabel(), node.getLabel());
            translateRealToNewick.put(node.getLabel(), node.getLabel());
        }

    }


    /**
     * Parse a CoRePa nexus file.
     * 
     * @param reader
     *        Buffered reader object that handles the file.
     * @throws IOException
     *         When something wrongs happens while reading the file.
     * @throws NexusFileParserException
     *         When some unexpected feature is found while parsing the file.
     */
    private void parseCoRePaNexus(BufferedReader reader) throws IOException,
                                                        NexusFileParserException {

        String hostTreeString = "";
        String parasiteTreeString = "";

        translateNewickToReal = new HashMap<String, String>();
        translateRealToNewick = new HashMap<String, String>();

        /* Find the translate section. */
        String line = reader.readLine();
        while (line != null && !line.toLowerCase().contains("translate")) {
            line = reader.readLine();
        }

        /* Read the translate section. */
        boolean endReached = false;
        line = reader.readLine();
        while (line != null && !endReached) {
            line = line.trim();
            if (!isCommentLine(line)) {
                endReached = line.contains(";");
                line = line.replaceAll(";", "").replaceAll(" +", " ").replaceAll(",", "").trim();
                if (line.contains("\t")) {
                    String[] pair = line.split("\t");
                    if (pair.length != 2) {
                        throw new NexusFileParserException("CoRePa Nexus File: Invalid Translate Section;");
                    }
                    translateNewickToReal.put(pair[0].trim(), pair[1].trim());
                    translateRealToNewick.put(pair[1].trim(), pair[0].trim());
                }
                if (endReached) {
                    break;
                }
            }
            line = reader.readLine();
        }

        if (line == null) {
            throw new NexusFileParserException("Unexpected end of nexus file while reading translate section.");
        }

        /* Read host and parasite trees. */
        line = reader.readLine();
        while (line != null && (hostTreeString.equals("") || parasiteTreeString.equals(""))) {
            line = line.trim().replaceAll(" +", " ");
            if (!isCommentLine(line)) {
                if (line.toLowerCase().contains("tree host")) {
                    String[] aux = line.split("=");
                    hostTreeString = aux[1].trim();
                } else if (line.toLowerCase().contains("tree parasite")) {
                    String[] aux = line.split("=");
                    parasiteTreeString = aux[1].trim();
                }
            }
            line = reader.readLine();
        }

        if (line == null) {
            throw new NexusFileParserException("Unexpected end of nexus file while reading trees in translate section.");
        }

        /* Find the cophylogeny section. */
        line = reader.readLine();
        while (line != null && !line.toLowerCase().contains("cophylogeny")) {
            line = reader.readLine();
        }

        /* Find the PHI section. */
        line = reader.readLine();
        while (line != null && !line.toLowerCase().trim().equals("phi")) {
            line = reader.readLine();
        }

        if (line == null) {
            throw new NexusFileParserException("Unexpected end of nexus file while searching for COPHYLOGENY -> PHI Section.");
        }

        phiRealNames = new HashMap<String, String>();
        phiNewickNames = new HashMap<String, String>();

        /* Read the translate section. */
        endReached = false;
        line = reader.readLine();
        while (line != null && !endReached) {
            line = line.trim();
            if (!isCommentLine(line)) {
                endReached = line.contains(";");
                line = line.replaceAll(";", "").replaceAll(" +", " ").replaceAll(",", "").trim();

                if (line.contains("\t")) {

                    String[] pair = line.split("\t");
                    if (pair.length != 2) {
                        throw new NexusFileParserException("CoRePa Nexus File: Invalid COPHYLOGENY -> PHI Section.");
                    }
                    String pLabel = pair[0].trim();
                    String hLabel = pair[1].trim();
                    if (phiRealNames.containsKey(pLabel)
                        && !phiRealNames.get(pLabel).equals(hLabel)) {
                        throw new NexusFileParserException("A parasite label cannot be associated to two differents host labels.");
                    } else {
                        phiRealNames.put(pLabel, hLabel);
                        phiNewickNames.put(translateRealToNewick.get(pLabel),
                                           translateRealToNewick.get(hLabel));
                    }
                }

                if (endReached) {
                    break;
                }

            }
            line = reader.readLine();
        }

        if (line == null) {
            throw new NexusFileParserException("Unexpected end of nexus file while reading for COPHYLOGENY -> PHI Section.");
        }

        host = parseNewickString(hostTreeString, "#H");
        parasite = parseNewickString(parasiteTreeString, "#P");

    }


    /**
     * Obtain a tree in newick format from the nexus file.
     * 
     * @param reader
     *        Buffered reader object that handles the file.
     * @return A string which may represent the tree in newick format.
     * @throws IOException
     *         When something wrongs happens while reading the file.
     * @throws NexusFileParserException
     *         When some unexpected feature is found while parsing the file.
     */
    private String getTreeString(BufferedReader reader) throws IOException,
                                                       NexusFileParserException {
        /* Read the host tree */
        String line = reader.readLine();
        while (line != null && line.trim().equals("")) {
            if (line.trim().contains("=") && !isCommentLine(line)) {
                break;
            }
            line = reader.readLine();
        }

        if (line != null) {
            line = line.trim().replaceAll(" +", " ");
            String[] array = line.split("=");
            if (array.length == 2) {
                return array[1].trim().replaceAll(";", "");
            }
        }

        throw new NexusFileParserException("Could not read host or parasite tree from nexus file.");
    }


    /**
     * Parse the string to extract a tree.
     * 
     * @param treeString
     *        String to be parsed.
     * @param labelPrefix
     *        Label prefix for internal nodes.
     * @return The parsed tree.
     * @throws NexusFileParserException
     *         When some unexpected feature is found while parsing the string.
     */
    private Tree parseNewickString(String treeString, String labelPrefix) throws NexusFileParserException {

        treeString = treeString.replaceAll(", +", ",");
        treeString = treeString.replaceAll("\\( +", "(");
        treeString = treeString.replaceAll("\\) +", ")");

        int key = 0;
        int cursor = 0;

        Tree tree = new Tree(key);
        TreeNode root = tree.getRoot();
        root.setLabel(labelPrefix + Integer.toString(key++));

        int brackets = 0;

        TreeNode currentNode = root;
        while (cursor < treeString.length()) {

            if (treeString.charAt(cursor) == OPEN_BRACKET) {
                currentNode.addChild(new TreeNode(key, labelPrefix + Integer.toString(key++)), 0);
                currentNode = currentNode.getChild(0);
                cursor++;
                brackets++;
            } else if (treeString.charAt(cursor) == CHILD_SEPARATOR) {
                currentNode = currentNode.getParent();

                currentNode.addChild(new TreeNode(key, labelPrefix + Integer.toString(key++)), 1);
                currentNode = currentNode.getChild(1);
                cursor++;
            } else if (treeString.charAt(cursor) == CLOSE_BRACKET) {
                currentNode = currentNode.getParent();
                cursor++;
                brackets--;
                if (brackets < 0) {
                    throw new NexusFileParserException("Malformed tree string :\n" + treeString
                                                       + "\n");
                }
            } else {
                String label = treeString.charAt(cursor) + "";
                cursor++;
                while (cursor < treeString.length() && treeString.charAt(cursor) != CHILD_SEPARATOR
                       && treeString.charAt(cursor) != CLOSE_BRACKET) {
                    label = label + treeString.charAt(cursor);
                    cursor++;
                }
                label = label.trim();
                currentNode.setLabel(label);
            }

        }
        tree.createNodeList();
        return tree;

    }


    private boolean isCommentLine(String line) {
        return line.startsWith("[") || line.startsWith("#");
    }


    /**
     * Return the host tree which was extracted from the nexus file.
     * 
     * @return The host tree which was extracted from the nexus file.
     */
    public Tree getHost() {
        return host;
    }


    /**
     * Return the parasite tree which was extracted from the nexus file.
     * 
     * @return The parasite tree which was extracted from the nexus file.
     */
    public Tree getParasite() {
        return parasite;
    }


    /**
     * Return the map that translates the labels that appear in the newick representation of the
     * tree to their real label names.
     * 
     * @return Map that translates the labels that appear in the newick representation of the tree
     *         to their real label names.
     */
    public HashMap<String, String> getTranslateNewickToReal() {
        return translateNewickToReal;
    }


    /**
     * Return the map that translates the real label names to the labels that appear in the newick
     * representation of the tree.
     * 
     * @return Map that translates the real label names to the labels that appear in the newick
     *         representation of the tree.
     */
    public HashMap<String, String> getTranslateRealToNewick() {
        return translateRealToNewick;
    }


    /**
     * Return the phi function which maps the parasite Leaves mapped into host Leaves using real
     * names.
     * 
     * @return The phi function which maps the parasite Leaves mapped into host Leaves using real
     *         names.
     */
    public HashMap<String, String> getPhiRealNames() {
        return phiRealNames;
    }


    /**
     * Return the phi function which maps the parasite Leaves mapped into host Leaves using newick
     * names.
     * 
     * @return The phi function which maps the parasite Leaves mapped into host Leaves using newick
     *         names.
     */
    public HashMap<String, String> getPhiNewickNames() {
        return phiNewickNames;
    }

}
