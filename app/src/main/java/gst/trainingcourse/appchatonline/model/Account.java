package gst.trainingcourse.appchatonline.model;

public class Account {

    private String id;
    private String username;
    private String imgUrl;
    private String status;
    private boolean typing;
    private String introduce;

    public Account() {
    }

    public Account(String id, String username, String imgUrl, String status, boolean typing, String introduce) {
        this.id = id;
        this.username = username;
        this.imgUrl = imgUrl;
        this.status = status;
        this.typing = typing;
        this.introduce = introduce;
    }

    public String getId() {
        return id;
    }

    public Account(String id, String username, String imgUrl, String introduce) {
        this.id = id;
        this.username = username;
        this.imgUrl = imgUrl;
        this.introduce = introduce;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
