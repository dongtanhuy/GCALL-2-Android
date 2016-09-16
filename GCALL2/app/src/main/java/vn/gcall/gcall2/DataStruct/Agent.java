package vn.gcall.gcall2.DataStruct;

/**
 * Created by This PC on 18/06/2016.
 * Data struct describes Agent entity
 */
public class Agent {
    private String fullname;
    private String email;
    private String phone;
    private boolean accepted;

    public Agent(String name, String mail,String phoneNum, boolean isAccepted){
        fullname=name;
        email=mail;
        phone=phoneNum;
        accepted=isAccepted;
    }
    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
