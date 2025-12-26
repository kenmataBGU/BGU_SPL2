package spl.lae;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
        final int numThreads = Integer.parseInt(args[0].trim());
        final String inputPath = args[1];
        final String outputPath = args[2];

        try {
            InputParser inputParser = new InputParser();
            ComputationNode root = inputParser.parse(inputPath);

            LinearAlgebraEngine lae = new LinearAlgebraEngine(numThreads);
            ComputationNode resolved = lae.run(root);
            double[][] matrix = resolved.getMatrix();

            OutputWriter.write(matrix, outputPath);
        } catch (Exception e) {
            OutputWriter.write(e.getMessage(), outputPath);
        }

    }
}