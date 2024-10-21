package badCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
;

public class multipleBadCodeInstances {

    private class localClass{
        // Public field variable
        public int aVar;
        public localClass(int i){
            aVar = i;
        }

        private int getVar(){
            return aVar;
        }
    }

    // The following are multiple declarations and should be flagged up
    private int fldi1, fldi2;
    private localClass fldc1, fldc2;

    // public instance variable
    public double fldd;

    // public instance AND multiple declaration
    public String fls1, fls2;

    // Accessor naming error
    public int fldi1Getter(){
        return fldi1;
    }

    // Accessor naming error
    public double fldd(){
        return fldd;
    }

    // Accessor naming error
    public String getterForfls1(){
        return fls1;
    }

    // Mutator naming error
    public void fldi1Setter(int val){
        fldi1 =val;
    }

    // Mutator naming error
    public void fldd(double val){
        fldd = val;
    }

    // Mutator naming error
    public void setterForfls1(String val){
        fls1 = val;
    }


    public void declarationAndAssignmentErrors() {
        // Three instances of uninitialised local variables
        char c;
        float f;
        localClass lc;

        // Three instances of multiple declarations
        char a, b, d;
        localClass lc1, lc2;
        int x, y, z;

        // Two instances of multiple assignments
        x = y = z = 123;
        c = 'X'; // OK
        a = b = c;

        // OK
        fldd = Math.pi;

        if (a == b) {
            // duplicate local declaration
            char d = c;
            if (x < y) {
                // duplicate local declaration
                int x = z;
                // OK
                double dx = fldd;
                while (x > 0) {
                    // duplicate local declaration
                    double dx = fldd * fldd;
                }
            }
        }
    }

    public int switchStatementErrors(String param){
        // missing fall through comment (3 times)
        // missing defaul statement
        int result = 0;
        switch(param) {
            case "one":
                result = 1;
            case "two":
                result = result + 1;
                result = 2;
            case "three":
                result = result + 1;
                result = 3;
            }
        return result;
    }

    public void constantUsageErrors(int a, char b, double c, String d){
        // 4 constant usage errors in the boolean expressions
        while(a != 10 && c > 3.14159 ){
            if (b == '!'){
                c = c-0.01; // this is okay
            } else if (d.equals("Trellis")) {
                c = c-0.05; // this is okay
            }
            a++;
        }
    }

    public FileInputStream exceptionHandlingErrors(FileInputStream fis){
        // 1 caught exception ignored
        try{
            File file = new File("a/file/path/location");
            fis = new FileInputStream(file);
        }catch(FileNotFoundException e){
        }
        return fis;
    }

    // 2 changes of for loop iteration variable
    // Also two constantUSageErrors: 100 and -10
    public void forLoopError(){
        double trouble = 0.0;
        for(int i = 0; i < 100; i++){
            for(int j = 1; j > -10; --j){
                trouble += 0.7; // this is okay
                i++;
                j = i;
            }
        }
    }

    // instance of private data being exposed by inner class
    private int veryPrivateData;

    public multipleBadCodeInstances() {
        veryPrivateData = 42;
    }

    public class accessPoint{
        public int exposePrivateData(){
            return veryPrivateData;
        }
    }
}
