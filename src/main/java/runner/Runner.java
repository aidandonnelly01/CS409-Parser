package runner;

import parser.Parser;

import java.io.IOException;

public class Runner {
    private static final String FILE_PATH = "test-files" ;

    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        parser.parse();
    }
}
