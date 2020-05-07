package gst.trainingcourse.appchatonline.notification;

public class Data {
    private String user;
    private int icon;
    private String body;
    private String title;
    private String sented;
    private String groupname;
    private String imgurl;
    private String introduce;
    private String username;

    public Data(String user, int icon, String body, String title, String sented, String imgurl, String introduce, String username) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.imgurl = imgurl;
        this.introduce = introduce;
        this.username = username;
    }

    public Data(String user, int icon, String body, String title, String sented, String groupname) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.groupname = groupname;
    }

    public Data(String user, int icon, String body, String title, String sented, String groupname, String imgurl) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.groupname = groupname;
        this.imgurl = imgurl;
    }

    public Data() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
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

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
