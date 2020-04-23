package gst.trainingcourse.appchatonline;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.model.Chatlist;

public class ProfileActivity extends AppCompatActivity {

    private String mIdUser, mStateBtn;
    private CircleImageView mImgProfile;
    private TextView mTxtUser, mTxtIntroduce;
    private Button mBtnMakeFriend, mBtnUnFriend;
    private FloatingActionButton mBtnEdit;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReferenceChatRequest, mReferenceContact, mReferenceChatlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();

        mReferenceChatRequest = FirebaseDatabase.getInstance().getReference("ChatRequest");
        mReferenceContact = FirebaseDatabase.getInstance().getReference("Contacts");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        initData();
        initAction();
    }

    private void initAction() {
        if (mIdUser.equals(mFirebaseUser.getUid())) {
            mBtnMakeFriend.setVisibility(View.INVISIBLE);

            mBtnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(ProfileActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_fragment_editprofile);

                    Button btnOk = dialog.findViewById(R.id.btnOK);
                    final EditText edtIntroduce = dialog.findViewById(R.id.editTextProfile);
                    edtIntroduce.setText(mTxtIntroduce.getText().toString());

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String edt = edtIntroduce.getText().toString();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(mFirebaseUser.getUid()).child("introduce").setValue(edt);
                            mTxtIntroduce.setText(edt);
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });
        } else {
            mBtnEdit.setVisibility(View.INVISIBLE);
            mBtnMakeFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnMakeFriend.setEnabled(false);
                    if (mStateBtn.equals("new")) {
                        sendRequest();
                    } else if (mStateBtn.equals("request_sent")) {
                        cancelRequest(0);
                    } else if (mStateBtn.equals("request_received")) {
                        acceptRequest();
                    } else if (mStateBtn.equals("friends")) {
                        removeRequest();
                    }
                }
            });

            mBtnUnFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelRequest(0);
                }
            });
        }

    }

    private void initData() {
        Intent intent = getIntent();
        mIdUser = intent.getStringExtra("id");
        mTxtUser.setText(intent.getStringExtra("username"));
        mTxtIntroduce.setText(intent.getStringExtra("introduce"));
        Picasso.with(this).load(intent.getStringExtra("imgUrl")).noFade().into(mImgProfile);

        mReferenceChatRequest.child(mFirebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(mIdUser)) {
                            String request_type = dataSnapshot.child(mIdUser).child("request_type").getValue(String.class);
                            if (request_type.equals("sent")) {
                                mStateBtn = "request_sent";
                                mBtnMakeFriend.setText("Hủy yêu cầu");
                            } else if (request_type.equals("received")) {
                                mStateBtn = "request_received";
                                mBtnMakeFriend.setText("Đồng ý kết bạn");

                                mBtnUnFriend.setVisibility(View.VISIBLE);
                                mBtnUnFriend.setEnabled(true);
                            }
                        } else {
                            mReferenceContact.child(mFirebaseUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(mIdUser)) {
                                                mStateBtn = "friends";
                                                mBtnMakeFriend.setText("Xóa bạn bè");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initView() {
        mImgProfile = findViewById(R.id.imgProfile);
        mTxtUser = findViewById(R.id.username);
        mTxtIntroduce = findViewById(R.id.txtIntroduce);
        mBtnMakeFriend = findViewById(R.id.btnMakeFriend);
        mBtnUnFriend = findViewById(R.id.btnUnFriend);
        mBtnEdit = findViewById(R.id.btnEdit);
        mStateBtn = "new";
    }

    private void sendRequest() {
        mReferenceChatRequest.child(mFirebaseUser.getUid()).child(mIdUser)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mReferenceChatRequest.child(mIdUser).child(mFirebaseUser.getUid())
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mBtnMakeFriend.setEnabled(true);
                                                mStateBtn = "request_sent";
                                                mBtnMakeFriend.setText("Hủy yêu cầu");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelRequest(final int TAG) {
        mReferenceChatRequest.child(mFirebaseUser.getUid()).child(mIdUser)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mReferenceChatRequest.child(mIdUser).child(mFirebaseUser.getUid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mBtnMakeFriend.setEnabled(true);
                                                if (TAG == 0) {
                                                    mStateBtn = "new";
                                                    mBtnMakeFriend.setText("Gửi lời mời kết bạn");
                                                } else if (TAG == 1) {
                                                    mStateBtn = "friends";
                                                    mBtnMakeFriend.setText("Xóa bạn bè");
                                                }
                                                mBtnUnFriend.setVisibility(View.INVISIBLE);
                                                mBtnUnFriend.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptRequest() {
        mReferenceContact.child(mFirebaseUser.getUid()).child(mIdUser)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mReferenceContact.child(mIdUser).child(mFirebaseUser.getUid())
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                cancelRequest(1);
                                            }
                                        }
                                    });
                        }
                    }
                });

        Chatlist chatlistReceive = new Chatlist(mFirebaseUser.getUid());
        Chatlist chatlistSent = new Chatlist(mIdUser);
        FirebaseDatabase.getInstance().getReference("Chatlist").child(mFirebaseUser.getUid()).child(mIdUser).setValue(chatlistSent);
        FirebaseDatabase.getInstance().getReference("Chatlist").child(mIdUser).child(mFirebaseUser.getUid()).setValue(chatlistReceive);
    }

    private void removeRequest() {
        mReferenceContact.child(mFirebaseUser.getUid()).child(mIdUser)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mReferenceContact.child(mIdUser).child(mFirebaseUser.getUid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mBtnMakeFriend.setEnabled(true);
                                                mStateBtn = "new";
                                                mBtnMakeFriend.setText("Gửi lời mời kết bạn");

                                                mBtnUnFriend.setVisibility(View.INVISIBLE);
                                                mBtnUnFriend.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

        mReferenceChatlist = FirebaseDatabase.getInstance().getReference("Chatlist");
        mReferenceChatlist.child(mFirebaseUser.getUid()).child(mIdUser)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mReferenceChatlist.child(mIdUser).child(mFirebaseUser.getUid())
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                    }
                });
    }
}
