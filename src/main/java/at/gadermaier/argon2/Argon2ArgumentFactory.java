package at.gadermaier.argon2;

import at.gadermaier.argon2.model.Argon2Type;
import org.apache.commons.cli.*;

import static java.lang.Integer.parseInt;

public class Argon2ArgumentFactory {

    static Argon2 parseArguments(String[] args){

        Options options = buildOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(options, args);

           if(commandLine.getArgs().length != 1)
               throw new ParseException("no password or salt");

            return createArgon2(commandLine);
        } catch (ParseException e) {
            formatter.printHelp("argon2 salt", options,true);
            System.out.println("Password is read from stdin");

            bailOut();
        }

        // not reachable
        return null;
    }

    private static Argon2 createArgon2(CommandLine commandLine) throws ParseException {
        Argon2 argon2 = new Argon2();
        String salt = commandLine.getArgs()[0];

        argon2.setSalt(salt);

        if(commandLine.hasOption("h"))
            throw new ParseException("usage");

        if(commandLine.hasOption("t")){
            argon2.setIterations(parseInt(commandLine.getOptionValue("t")));
        }

        if(commandLine.hasOption("p")){
            argon2.setParallelism(parseInt(commandLine.getOptionValue("p")));
        }

        if(commandLine.hasOption("m")){
            argon2.setMemory(parseInt(commandLine.getOptionValue("m")));
        }else if(commandLine.hasOption("k")){
            int k = parseInt(commandLine.getOptionValue("k"));
            if(k % 4*argon2.getLanes() != 0)
                throw new ParseException("k must be a multiple of p*4");
            argon2.setMemoryInKiB(k);
        }


        if(commandLine.hasOption("e")){
            argon2.setEncodedOnly(true);
        }else if(commandLine.hasOption("r")){
            argon2.setRawOnly(true);
        }

        if(commandLine.hasOption("i")){
            argon2.setType(Argon2Type.Argon2i);
        }else if(commandLine.hasOption("d")){
            argon2.setType(Argon2Type.Argon2d);
        }else if(commandLine.hasOption("id")){
            argon2.setType(Argon2Type.Argon2id);
        }

        if (commandLine.hasOption(("l"))) {
            argon2.setOutputLength(parseInt(commandLine.getOptionValue("l")));
        }

        if(commandLine.hasOption("v")){
            int version = parseInt(commandLine.getOptionValue("v"));
            if (!(version == 10 || version == 13)) {
                bailOut("wrong version");
            }
            argon2.setVersion(version);
        }

        return argon2;
    }

    private static Options buildOptions(){
        Options options = new Options();
        Option option;

        OptionGroup optionGroup = new OptionGroup();

        option = new Option("i", null, false, "Use Argon2i (this is the default)");
        optionGroup.addOption(option);
        option = new Option("d", null, false, "Use Argon2d instead of Argon2i");
        optionGroup.addOption(option);
        option = new Option("id", null, false, "Use Argon2id instead of Argon2i");
        optionGroup.addOption(option);

        options.addOptionGroup(optionGroup);


        option = new Option("t", null, true, "Sets the number of iterations to N (default = 3)");
        option.setArgName("N");
        option.setType(Integer.class);
        options.addOption(option);

        optionGroup = new OptionGroup();

        option = new Option("m", null, true, "Sets the memory usage of 2^N KiB (default 12)");
        option.setArgName("N");
        option.setType(Integer.class);
        optionGroup.addOption(option);

        option = new Option("k", null, true, "Sets the memory usage of N KiB (default 2^12)");
        option.setArgName("N");
        option.setType(Integer.class);
        optionGroup.addOption(option);

        options.addOptionGroup(optionGroup);


        option = new Option("p", null, true, "Sets parallelism to N (default 1)");
        option.setArgName("N");
        option.setType(Integer.class);
        options.addOption(option);

        option = new Option("l", null, true, "Sets hash output length to N bytes (default 32)");
        option.setArgName("N");
        option.setType(Integer.class);
        options.addOption(option);


        optionGroup = new OptionGroup();

        option = new Option("e", null, false, "Output only encoded hash");
        optionGroup.addOption(option);
        option = new Option("r", null, false, "Output only the raw bytes of the hash");
        optionGroup.addOption(option);

        options.addOptionGroup(optionGroup);

        option = new Option("h", null, false, "Print usage");
        options.addOption(option);

        return options;
    }

    private static void bailOut(String message){
        System.out.println(message);
        bailOut();
    }

    private static void bailOut(){
        System.exit(1);
    }
}
