package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 20/06/2016.
 * Data struct describes Subgroup entity
 */
public class SubGroup {
    private String groupName;
    private String groupDescription;
    private String typeInside;
    private int extension;
    private int size;
    private String groupID;
    private String hotline;

    public SubGroup(String name,String desciption,String type,int ext, int length){
        groupName=name;
        groupDescription=desciption;
        typeInside=type;
        extension=ext;
        size=length;
        groupID="";
        hotline="";
    }

    public SubGroup(String id,String name,String type,int length){
        groupID=id;
        groupName=name;
        groupDescription="";
        hotline="";
        typeInside=type;
        size=length;
        extension=0;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTypeInside() {
        return typeInside;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public int getExtension() {
        return extension;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setExtension(int extension) {
        this.extension = extension;
    }

    public void setTypeInside(String typeInside) {
        this.typeInside = typeInside;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getHotline() {
        return hotline;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }
}
