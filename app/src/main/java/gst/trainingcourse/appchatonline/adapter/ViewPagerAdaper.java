package gst.trainingcourse.appchatonline.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import gst.trainingcourse.appchatonline.fragment.ChatsFragment;
import gst.trainingcourse.appchatonline.fragment.GroupFragment;
import gst.trainingcourse.appchatonline.fragment.UsersFragment;

//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdaper extends FragmentPagerAdapter {
    private int mMes;
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
                if (mMes == 0) return "Chat";
                else return "(" + mMes + ") Chat";
            case 1:
                return "Group";
            case 2:
                return "Users";
            default:
                return null;
        }
    }

    public int mesTitle(int mes) {
        mMes = mes;
        return mMes;
    }
}
