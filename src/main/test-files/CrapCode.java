public class CrapCode {

    public String pub1;
    public String pub2;
    private String priv1;
    private String priv2;

    public void CrapMethod1() {
        int counter;
        counter = 0;
        char a = b = 'c';
        int x, y;
        for (int i, j = 0; i < 5; i++, j++) {
            counter++;
        }
        for (int i = 0; i < 5; i++) {
            counter++;
            counter = i + counter;
            i++;
        }
        for (int i = 0; i < 5;) {
            i++;
        }
    }

    public void literalMethod(int a, String b) {
        if ((a == 255) && (b.equals("admin123"))) {
            System.out.println("Literally!");
        }
        if (a == -1 || a == 0 || a == 1) {
            a++;
        }
    }

    public void CrapMethod2() {
        int counter;
        if (1 < 2) {
            int counter = 0;
        }
    }

    public void CrapMethod3() {
        switch (input) {
            case 1:
            case 2:
                doOneOrTwo();
                // fall through
            case 3:
                doOneTwoOrThree();
                break;
            default:
                doTheRest(input);
        }
        switch (input) {
            case 1:
            case 2:
                doOneOrTwo();
            case 3:
                doOneTwoOrThree();
                break;
            default:
                doTheRest(input);
        }
        switch (input) {
            case 1:
            case 2:
                doOneOrTwo();
                break;
            case 3:
                doOneTwoOrThree();
                break;
            default:
                doTheRest(input);
        }
        switch (input) {
            case 0:
                doOneOrTwo();
                break;
            case 1:
                doOneTwoOrThree();
                break;
        }
        switch (input) {
            case 0:
                doOneOrTwo();
                break;
            case 1:
                doOneTwoOrThree();
                break;
            case 2:
                doOneTwoOrThree();
                //fall through
            case 3:
                doOneTwoOrThree();
                //fall through
            case 4:
                doOneTwoOrThree();
                break;
            case 5:
                doOneTwoOrThree();
                break;
            case 6:
                doOneTwoOrThree();
                //fall through
            case 7:
                doOneTwoOrThree();
                break;
            case 8:
                doOneTwoOrThree();
                break;
        }
    }

}