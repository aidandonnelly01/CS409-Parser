package badCode.mutableInstance;

public class mutableReferenceExposer {

    private mutableObject mObj;

    public mutableReferenceExposer(){
        mObj = new mutableObject(10);
    }

    public void displayData(){
        mObj.displayData();
    }

    // returns reference to private mutable class member
    public mutableObject returnReference(){
        return mObj;
    }
}

