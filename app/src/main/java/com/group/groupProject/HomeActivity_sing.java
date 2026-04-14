package com.learning.group_project_game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity_sing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_sing);

        // 1. 找到 "Start Game" 按鈕
        Button startGameButton = findViewById(R.id.start_game_button);
//        Button goBackToMainPageButton = findViewById(R.id.go_back_to_main_page);

        // 2. 為按鈕設定點擊事件
        startGameButton.setOnClickListener(v -> {
            // 建立一個意圖 (Intent)，準備從 HomeActivity 跳轉到 MainActivity
            Intent intent = new Intent(HomeActivity_sing.this, AppleActivity_sing.class);
            // 執行跳轉
            startActivity(intent);
        });

//        goBackToMainPageButton.setOnClickListener(v -> {
//            // 建立一個意圖，準備跳轉到我們即將建立的 GrandMainPageActivity
//            Intent intent = new Intent(HomeActivity.this, GrandMainPageActivity.class);
//            // 同樣，加入Flag來確保導航正確
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish(); // 結束當前的遊戲主頁
//        });

    }
}
