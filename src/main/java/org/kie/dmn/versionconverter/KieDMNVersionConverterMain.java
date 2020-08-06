package org.kie.dmn.versionconverter;

import java.io.File;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

public class KieDMNVersionConverterMain {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option inputFileOpt = Option.builder("i").hasArg().required().longOpt("input").desc("input file").build();
        OptionGroup optgrp = new OptionGroup();
        Option overwriteOpt = Option.builder("w").longOpt("overwrite").desc("overwrite input file").build();
        Option outputFileOpt = Option.builder("o").hasArg().longOpt("output").desc("output file").build();
        optgrp.addOption(overwriteOpt);
        optgrp.addOption(outputFileOpt);
        options.addOption(inputFileOpt).addOptionGroup(optgrp);

        if (args.length < 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "kie-dmn-versionconverter", options, true );
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(overwriteOpt.getOpt()) && cmd.hasOption(outputFileOpt.getOpt())) {
            throw new IllegalArgumentException("Cannot supply both Overwrite and Output file options");
        }
        File inputFile = new File(cmd.getOptionValue(inputFileOpt.getOpt()));
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("The input file does not exists: "+inputFile);
        }
        File outputFile = null;
        if (cmd.hasOption(overwriteOpt.getOpt())) {
            outputFile = inputFile;
        } else if (cmd.hasOption(outputFileOpt.getOpt())) {
            outputFile = new File(cmd.getOptionValue(outputFileOpt.getOpt()));
            if (outputFile.exists()) {
                throw new IllegalArgumentException("The output file already exists: "+outputFile);
            }
        }

        process(inputFile, outputFile);
    }

    private static void process(File inputFile, File outputFile) throws SaxonApiException {
        Processor processor = new Processor(false);
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable stylesheet = compiler.compile(new StreamSource(KieDMNVersionConverterMain.class.getResourceAsStream("/v11TOv12.xslt")));
        Serializer out = processor.newSerializer(System.out);
        if (outputFile != null) {
            out = processor.newSerializer(outputFile);
        }
        Xslt30Transformer transformer = stylesheet.load30();
        transformer.transform(new StreamSource(inputFile), out);
    }
}