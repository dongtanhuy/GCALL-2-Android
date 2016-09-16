package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 09/08/2016.
 * Data struct describes Button entity (in More tab)
 */
public class CustomButton {
    private String label;
    private int resourceID;

    public CustomButton(String lb,int id){
        label=lb;
        resourceID=id;
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }
}
