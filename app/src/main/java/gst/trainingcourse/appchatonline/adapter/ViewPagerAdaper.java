package gst.trainingcourse.appchatonline.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import gst.trainingcourse.appchatonline.fragments.ChatsFragment;
import gst.trainingcourse.appchatonline.fragments.GroupFragment;
import gst.trainingcourse.appchatonline.fragments.UsersFragment;

public class ViewPagerAdaper extends FragmentPagerAdapter {
    private int mMesChat, mMesGroup;
    public ViewPagerAdaper(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return new ChatsFragment();
        } else if (i == 1) {
            return new GroupFragment();
        } else {
            return new UsersFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                if (mMesChat == 0) return "Chat";
                else return "(" + mMesChat + ") Chat";
            case 1:
                if (mMesGroup == 0) return "Group";
                else return "(" + mMesGroup + ") Group";
            case 2:
                return "Users";
            default:
                return null;
        }
    }

    public int mesTitleChat(int mes) {
        mMesChat = mes;
        return mMesChat;
    }

    public int mesTitleGroup(int mes) {
        mMesGroup = mes;
        return mMesGroup;
    }
}
