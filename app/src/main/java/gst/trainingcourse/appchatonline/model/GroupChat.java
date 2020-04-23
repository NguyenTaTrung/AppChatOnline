package gst.trainingcourse.appchatonline.model;

public class GroupChat {
    private String message;
    private String sender;
    private String groupname;
    private String imgurl;
    private boolean isseen;

    public GroupChat() {
    }

    public GroupChat(String message, String sender, String groupname, String imgurl, boolean isseen) {
        this.message = message;
        this.sender = sender;
        this.groupname = groupname;
        this.imgurl = imgurl;
        this.isseen = isseen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
