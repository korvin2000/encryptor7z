package com.example.encryptor7z;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

final class EncryptorCli {
    private static final String TOOL_NAME = "encryptor7z";

    private final ArchiveProcessor archiveProcessor;
    private final HelpFormatter helpFormatter = new HelpFormatter();

    EncryptorCli(ArchiveProcessor archiveProcessor) {
        this.archiveProcessor = archiveProcessor;
    }

    void run(String[] args) {
        try {
            var options = buildOptions();
            var commandLine = new DefaultParser().parse(options, args);
            var request = buildRequest(commandLine);
            archiveProcessor.process(request);
        } catch (ParseException | IllegalArgumentException exception) {
            printUsage(exception.getMessage());
        }
    }

    private CliRequest buildRequest(CommandLine commandLine) {
        var operation = Operation.from(commandLine.getOptionValue("mode"));

	    String inputArg = commandLine.getOptionValue("input");
        var input = Path.of(inputArg);
	    String outputArg = commandLine.getOptionValue("output");
        var output = Path.of(StringUtils.isBlank(outputArg) ? inputArg : outputArg);
        var password = commandLine.getOptionValue("password").toCharArray();

        validatePaths(input, output);
        return new CliRequest(operation, input, output, password);
    }

    private void validatePaths(Path input, Path output) {
        if (!Files.isDirectory(input)) {
            throw new IllegalArgumentException("Input must be an existing directory: " + input);
        }
        if (!Files.isDirectory(output)) {
            throw new IllegalArgumentException("Output must be a directory path, not a file: " + output);
        }
    }

    private Options buildOptions() {
        var options = new Options();
        options.addOption(Option.builder("m")
                .longOpt("mode")
                .hasArg()
                .argName("encrypt|decrypt")
                .required()
                .desc("Operation to perform")
                .build());
        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("path")
                .required()
                .desc("Path to the source archive or directory")
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
				.required(false)
                .argName("path")
                .desc("Destination path")
                .build());
        options.addOption(Option.builder("p")
                .longOpt("password")
                .hasArg()
                .argName("secret")
                .required()
                .desc("Password used to protect or unlock archives")
                .build());
        return options;
    }

    private void printUsage(String errorMessage) {
        if (errorMessage != null && !errorMessage.isBlank()) {
            System.err.println(errorMessage);
        }
        helpFormatter.setWidth(100);
        helpFormatter.printHelp(TOOL_NAME, buildOptions());
    }

    enum Operation {
        ENCRYPT,
        DECRYPT;

        static Operation from(String mode) {
            if (mode == null || mode.isBlank()) {
                throw new IllegalArgumentException("Mode is required");
            }
            return switch (mode.toLowerCase(Locale.ROOT)) {
                case "encrypt", "enc" -> ENCRYPT;
                case "decrypt", "dec" -> DECRYPT;
                default -> throw new IllegalArgumentException("Unsupported mode: " + mode);
            };
        }
    }

    record CliRequest(Operation operation, Path input, Path output, char[] password) {
    }
}
