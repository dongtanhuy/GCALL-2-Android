package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 23/06/2016.
 * Data struct describes Notification entity (in Notification Tab)
 */
public class Notification {
    private String groupID;
    private String groupName;
    private String hotline;
    private String addedBy;

    public Notification(String id, String name, String hl, String byWho){
        groupID=id;
        groupName=name;
        hotline=hl;
        addedBy=byWho;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String getHotline() {
        return hotline;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupID() {
        return groupID;
    }
}
