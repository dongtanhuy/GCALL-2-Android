package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 30/06/2016.
 * Data struct describes Hotline-to-buy entity (Old version)
 */
public class HotlineToBuy {
    private String number;
    private String friendlyName;

    public HotlineToBuy(String n,String fn){
        number=n;
        friendlyName=fn;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getNumber() {
        return number;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
