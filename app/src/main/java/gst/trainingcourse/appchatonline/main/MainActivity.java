package gst.trainingcourse.appchatonline.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.activities.LoginActivity;
import gst.trainingcourse.appchatonline.activities.ProfileActivity;
import gst.trainingcourse.appchatonline.adapter.ViewPagerAdaper;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.Chat;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private CircleImageView mImgProfile;
    private TextView mTxtUsername;

    private static final int REQUEST_CODE_IMAGE = 1;

    private Account mAccount;
    private ViewPagerAdaper mViewPagerAdaper;
    private Dialog mDialog;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        addViewPagerAdapter();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReferenceFromUrl("gs://appchattest-51e06.appspot.com");

        initData();
        initAction();
    }

    private void addViewPagerAdapter() {
        mReference = FirebaseDatabase.getInstance().getReference("Chats");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mViewPagerAdaper = new ViewPagerAdaper(getSupportFragmentManager());
                int mes = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(mFirebaseUser.getUid()) && !chat.isIsseen()) {
                        mes++;
                    }
                }
                if (mes == 0) {
                    mViewPagerAdaper.getPageTitle(0);
                } else {
                    mViewPagerAdaper.mesTitleChat(mes);
                }
                mViewPager.setAdapter(mViewPagerAdaper);
                mTabLayout.setupWithViewPager(mViewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        mReference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
//        mReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                mViewPagerAdaper = new ViewPagerAdaper(getSupportFragmentManager());
//                int mes = 0;
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    GroupChat groupChat = snapshot.getValue(GroupChat.class);
//                    if (!GroupFragment.mList.isEmpty()) {
//                        for (int i = 0; i < GroupFragment.mList.size(); i++) {
//                            if (!groupChat.getSender().equals(mFirebaseUser.getUid()) && groupChat.getGroupname().equals(GroupFragment.mList.get(i).getGroupname()) && !groupChat.isIsseen()) {
//                                mes++;
//                            }
//                        }
//                    }
//                }
//                if (mes == 0) {
//                    mViewPagerAdaper.getPageTitle(1);
//                } else {
//                    mViewPagerAdaper.mesTitleGroup(mes);
//                }
//                mViewPager.setAdapter(mViewPagerAdaper);
//                mTabLayout.setupWithViewPager(mViewPager);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK && data != null) {
            mDialog.show();
            Uri uri = data.getData();
            try {
                //setBitmap cho ảnh
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mImgProfile.setImageBitmap(bitmap);

                //tạo tên file ảnh trong FireBase
                Calendar calendar = Calendar.getInstance();
                StorageReference storageReference = mStorageReference.child("imageUser/").child("imageUpdate" + calendar.getTimeInMillis() + ".png");

                //chuyển bitmap -> byte[]
                mImgProfile.setDrawingCacheEnabled(true);
                mImgProfile.buildDrawingCache();
                Bitmap newBitmap = mImgProfile.getDrawingCache();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] dataImg = byteArrayOutputStream.toByteArray();

                //upload ảnh lên FireBase
                UploadTask uploadTask = storageReference.putBytes(dataImg);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imgUrl = uri.toString();

                                Account account = new Account(mAccount.getId(), mTxtUsername.getText().toString(), imgUrl, mAccount.getIntroduce());
                                mReference.setValue(account);
                                status("online");
                                mDialog.dismiss();
                                Toast.makeText(getApplicationContext(), R.string.toast_update_image_success, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.profile:
                Intent intent1 = new Intent(MainActivity.this, ProfileActivity.class);
                intent1.putExtra("id", mAccount.getId());
                intent1.putExtra("username", mAccount.getUsername());
                intent1.putExtra("imgUrl", mAccount.getImgUrl());
                intent1.putExtra("introduce", mAccount.getIntroduce());
                startActivity(intent1);
                return true;
        }
        return false;
    }

    private void initView() {
        mToolBar = findViewById(R.id.toolBar);
        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);
        mImgProfile = findViewById(R.id.profileImg);
        mTxtUsername = findViewById(R.id.username);

        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_loading);
        TextView title = mDialog.findViewById(R.id.txtLogin);
        title.setText("Waiting...");
        mDialog.setCancelable(false);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");
    }

    private void initData() {
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAccount = dataSnapshot.getValue(Account.class);
                if (mAccount != null) {
                    mTxtUsername.setText(mAccount.getUsername());
                    Picasso.with(getApplicationContext()).load(mAccount.getImgUrl()).into(mImgProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initAction() {
        mImgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });
    }

    private void status(String status) {
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        mReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status("offline");
    }
}
