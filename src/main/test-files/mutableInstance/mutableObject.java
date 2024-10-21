package badCode.mutableInstance;

public class mutableObject {
    private int data;

    public mutableObject(int val){
        data = val;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public void displayData(){
        System.out.println("data: "+data);
    }
}
