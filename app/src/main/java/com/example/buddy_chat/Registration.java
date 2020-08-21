package com.example.buddy_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity {


    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int galleryPick=1;
    private StorageReference userProfileImagesReference;

    private ProgressDialog loadingBar;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        userProfileImagesReference= FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);

        InitializeFields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPick);

            }
        });

    }

    private void UpdateSettings() {

        String setUserName= userName.getText().toString();
        String setUserStatus= userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Write Your User Name First", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Write Your User Status First", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);
            RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(Registration.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message= task.getException().toString();
                                Toast.makeText(Registration.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

            RetrieveUserInfo();
        }



    }



    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(Registration.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitializeFields() {
        UpdateAccountSettings=(Button) findViewById(R.id.update_settings_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);

        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPick &&  resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait!");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();


                StorageReference filePath = userProfileImagesReference.child(currentUserId + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        RootRef.child("Users").child(currentUserId).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(Registration.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                        else{
                                                            String message = task.getException().toString();
                                                            Toast.makeText(Registration.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();

                                                        }

                                                    }
                                                });

                                    }
                                });

                            }
                        });

            }
        }

    }




    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) &&(dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")) ){

                    String retrieveUserName= Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    String retrieveStatus= Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                    String retrieveProfileImage= dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);


                }
                else if((dataSnapshot.exists()) &&(dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("status")) ){
                    String retrieveUserName= Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    String retrieveStatus= Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);

                }
                else{

                    Toast.makeText(Registration.this, "Set & Update your information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

