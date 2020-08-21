package com.example.buddy_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GetStarted extends AppCompatActivity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openMobAuth();
            }
        });

        updateUI();
    }

    public void updateUI() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
        public void openMobAuth(){
            Intent intent = new Intent(this,MobAuth.class);
            startActivity(intent);
        }
}