package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 17/06/2016.
 * Data struct describes Hotline entity
 */
public class Hotline {
    private String hotline;
    private String type;
    private int length;

    public Hotline(String hl,String t, int le){
        hotline=hl;
        type=t;
        length=le;
    }

    public String getHotline(){
        String hl=hotline;
        return hl;
    }

    public void setHotline(String hl){
        hotline=hl;
    }

    public String getType(){
        String t=type;
        return t;
    }

    public void setType(String t){
        type=t;
    }

    public int getLength(){
        int i=length;
        return i;
    }

    public void setLength(int i){
        length=i;
    }
}
