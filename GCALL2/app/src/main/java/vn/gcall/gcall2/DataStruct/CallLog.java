package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 28/06/2016.
 * Data struct describes Call log entity
 */
public class CallLog {
    private String group;
    private String dateCreated;
    private String duration;
    private String from;
    private String status;
    private String objectID;
    private String groupID;

    public CallLog(String date, String lenght, String f,String to, String stt,String gid){
        dateCreated=date;
        duration=lenght;
        from=f;
        status=stt;
        group=to;
        objectID="";
        groupID=gid;
    }

    public CallLog(String date,String f,String to,String objID,String gid){
        dateCreated=date;
        duration="0 secs";
        from=f;
        status="unsolved";
        group=to;
        objectID=objID;
        groupID=gid;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getDuration() {
        return duration;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getFrom() {
        return from;
    }

    public String getGroup() {
        return group;
    }

    public String getStatus() {
        return status;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
