package gst.trainingcourse.appchatonline.model;

import java.util.ArrayList;

public class Grouplist {
    private String groupname;
    private String imgurl;
    private ArrayList<String> member;

    public Grouplist() {
    }

    public Grouplist(String imgurl) {
        this.imgurl = imgurl;
    }

    public Grouplist(String groupname, String imgurl, ArrayList<String> member) {
        this.groupname = groupname;
        this.imgurl = imgurl;
        this.member = member;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public ArrayList<String> getMember() {
        return member;
    }

    public void setMember(ArrayList<String> member) {
        this.member = member;
    }
}
