package com.yohannes.chess;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class FirstPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 25) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_first_page);

        final ImageView FirstLayout = (ImageView) findViewById(R.id.FirstLayout);
        final LinearLayout SecondLayout = (LinearLayout) findViewById(R.id.SecondLayout);

        SecondLayout.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SecondLayout.setVisibility(View.VISIBLE);
                FirstLayout.setVisibility(View.INVISIBLE);
            }
        },5000);

        Button StarttwoplayerGame = (Button) findViewById(R.id.twoPlayersButton);

        StarttwoplayerGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FirstPage.this , MainActivity.class);
                FirstPage.this.startActivity(i);
            }
        });

        /*Button StartSinglePlayerGame = (Button) findViewById(R.id.singlePlayerButton);

        StartSinglePlayerGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FirstPage.this , SinglePlayerModewithBotActivity.class);
                FirstPage.this.startActivity(i);
            }
        });*/
        // Inside your FirstPage class click listener configuration logic
        TextView singlePlayerButton = findViewById(R.id.singlePlayerButton);
        singlePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String[] levels = {
                        "🛡️ Casual Woodcutter (Easy)",
                        "⚔️ Master Tactician (Medium)",
                        "👑 Grandmaster Core (Hard)"
                };
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FirstPage.this);
                builder.setTitle("Select Bot Difficulty Level");
                builder.setItems(levels, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        // Transfer level token parameters down to game activities via Intents
                        Intent intent = new Intent(FirstPage.this, SinglePlayerModewithBotActivity.class);
                        intent.putExtra("SELECTED_BOT_LEVEL", which); // 0 = Beginner, 1 = Inter, 2 = Prod
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        });

    }


}