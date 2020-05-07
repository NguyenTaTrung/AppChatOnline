package gst.trainingcourse.appchatonline.notification;

import java.util.HashMap;

public class Token {
    private String token;
//    private String groupname;
    private HashMap<String, Boolean> groupname;
    private String id;

    public Token(String token) {
        this.token = token;
    }

    public Token(String token, HashMap<String, Boolean> groupname, String id) {
        this.token = token;
        this.groupname = groupname;
        this.id = id;
    }

    //    public Token(String token, String groupname, String id) {
//        this.token = token;
//        this.groupname = groupname;
//        this.id = id;
//    }

    public Token() {
    }

    public HashMap<String, Boolean> getGroupname() {
        return groupname;
    }

    public void setGroupname(HashMap<String, Boolean> groupname) {
        this.groupname = groupname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

//    public String getGroupname() {
//        return groupname;
//    }

//    public void setGroupname(String groupname) {
//        this.groupname = groupname;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
