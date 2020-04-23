package gst.trainingcourse.appchatonline.listener;

import gst.trainingcourse.appchatonline.model.Account;

public interface ClickCheckInGroup {
    void onClickCheck(Account account);
    void onClickUnCheck(Account account);
}
