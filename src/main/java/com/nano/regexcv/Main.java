/*
 * Copyright 2021 nano1
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nano.regexcv;

import com.nano.regexcv.dfa.Dfa2DigraphPass;
import com.nano.regexcv.dfa.DfaMinimizer;
import com.nano.regexcv.dfa.SubsetConstructionPass;
import com.nano.regexcv.nfa.Nfa;
import com.nano.regexcv.nfa.Nfa2DigraphPass;
import com.nano.regexcv.nfa.RExpTree2NfaPass;
import com.nano.regexcv.nfa.RemoveEpsilonClosurePass;
import com.nano.regexcv.syntax.RegexParser;
import com.nano.regexcv.syntax.RegexSyntaxErrorException;
import com.nano.regexcv.table.CharacterSetCollector;
import com.nano.regexcv.util.Digraph;
import com.nano.regexcv.util.DigraphDotGenerator;
import com.nano.regexcv.util.MergingDigraphEdges;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  public interface Msg {
    String NFA_DESCRIPTION = "Convert the regex into the NFA digraph";
    String DFA_DESCRIPTION = "Convert the regex into the DFA digraph";
    String E_DESCRIPTION = "Remove the ε-closure of the NFA digraph";
    String REDUCE_DESCRIPTION = "Reduce the edges of the generated digraph";
    String MINIMIZE_DFA_DESCRIPTION = "Minimize the generated DFA.";

    String CMD_USAGE = "regexcv [OPTIONS] <regular expression>\n\n";

    String ERROR_MISSING_ARG =
        "Error: regexcv requires an argument representing the regular expression";
  }

  public static void main(String... args) {
    Options options = defineOptions();
    CommandLine cl = parseOptions(options, args);
    run(cl);
  }

  private static Options defineOptions() {
    Options options = new Options();
    options.addOption("v", "version", false, "Print version and exit");
    options.addOption("h", "help", false, "Print usage and exit");
    options.addOption("N", "Nfa", false, Msg.NFA_DESCRIPTION);
    options.addOption("D", "Dfa", false, Msg.DFA_DESCRIPTION);
    options.addOption("e", false, Msg.E_DESCRIPTION);
    options.addOption("m", false, Msg.MINIMIZE_DFA_DESCRIPTION);
    options.addOption("r", false, Msg.REDUCE_DESCRIPTION);
    return options;
  }

  private static CommandLine parseOptions(Options options, String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cl = parser.parse(options, args);
      if (cl.hasOption("v")) {
        printVersion();
        System.exit(0);
      }
      if (cl.hasOption("h")) {
        printUsage(options);
        System.exit(0);
      }
      if (cl.getArgs().length <= 0) {
        throw new ParseException(Msg.ERROR_MISSING_ARG);
      }
      return cl;
    } catch (ParseException e) {
      System.err.println(e.getMessage() + "\n");
      printUsage(options);
      System.exit(22);
    }
    return null;
  }

  private static void printVersion() {
    System.out.printf("regexcv version \"%s\" (%d)\n", Config.VERSION, Config.VERSION_CODE);
  }

  private static void printUsage(Options options) {
    HelpFormatter hf = new HelpFormatter();
    hf.setLongOptPrefix(" --");
    hf.setOptionComparator(null);
    hf.printHelp(Msg.CMD_USAGE, options);
  }

  private static void run(CommandLine cl) {
    var regex = cl.getArgs()[0];
    var pass = combinePasses(cl);
    if (cl.hasOption("r")) {
      pass = pass.next(new MergingDigraphEdges());
    }
    try {
      System.out.println(pass.next(new DigraphDotGenerator()).accept(regex));
    } catch (RegexSyntaxErrorException e) {
      System.err.println(e.getMessage());
      System.exit(8);
    }
  }

  private static Pass<String, Digraph> combinePasses(CommandLine cl) {
    var pass = new RegexParser().next(new CharacterSetCollector()).next(new RExpTree2NfaPass());
    if (cl.hasOption('D')) {
      return getDfaPass(pass, cl);
    } else {
      return getNfaPass(pass, cl);
    }
  }

  private static Pass<String, Digraph> getDfaPass(Pass<String, Nfa> pass, CommandLine cl) {
    var dfaPass = pass.next(new SubsetConstructionPass());
    if (cl.hasOption("m")) {
      dfaPass = dfaPass.next(new DfaMinimizer());
    }
    return dfaPass.next(new Dfa2DigraphPass());
  }

  private static Pass<String, Digraph> getNfaPass(Pass<String, Nfa> pass, CommandLine cl) {
    if (cl.hasOption("e")) {
      pass = pass.next(new RemoveEpsilonClosurePass());
    }
    return pass.next(new Nfa2DigraphPass());
  }
}
