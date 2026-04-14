package com.learning.group_project_game;

// 引入所有需要的類別
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import java.util.Locale;

// 讓 Activity 實現手勢監聽介面
public class UnlockActivity_sing extends AppCompatActivity implements GestureDetector.OnGestureListener {

    // --- UI 元件 ---
    // 圖層
    private RelativeLayout gameContainer, winContainer, loseContainer;
    // 遊戲畫面元件
    private EditText passwordInput;
    private TextView timerText;
    private ImageView lockClosedImage, lockOpenImage; // 鎖頭圖片
    // 勝利畫面元件
    private Button winHomeButton;
    // 失敗畫面元件
    private Button loseRetryButton, loseHomeButton;

    // --- 邏輯變數 ---
    private GestureDetectorCompat gestureDetector;
    private CountDownTimer countDownTimer;
    private String correctCode;
    private boolean isGameFinished = false;
    // 手勢偵測的門檻值
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private TextView finalScoreText; // 新增 finalScoreText
    private TextView scoretext;
    // --- 邏輯變數 ---
    private long remainingMillisGame2 = 0; // 更改 1: 新增變數存儲 Game 2 剩餘時間
    private int scoreFromGame1 = 0;      // 更改 2: 新增變數存儲從 Game 1 傳來的分數

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_sing);

        // --- 初始化所有UI元件 (根據你最新的XML ID) ---
        // 圖層
        gameContainer = findViewById(R.id.unlock_game_container);
        winContainer = findViewById(R.id.unlock_win_container);
        loseContainer = findViewById(R.id.unlock_lose_container);
        // 遊戲畫面
        passwordInput = findViewById(R.id.password_input);
        timerText = findViewById(R.id.unlock_timer_text);
        lockClosedImage = findViewById(R.id.lock_closed_image);
        lockOpenImage = findViewById(R.id.lock_open_image);
        // 勝利畫面
        winHomeButton = findViewById(R.id.unlock_win_home_button);
        finalScoreText = findViewById(R.id.final_score_text); // 初始化 finalScoreText
        scoretext = findViewById(R.id.level_score_text);
        // 失敗畫面
        loseRetryButton = findViewById(R.id.unlock_lose_retry_button);
        loseHomeButton = findViewById(R.id.unlock_lose_home_button);

        // 接收密碼 (不變)
        correctCode = getIntent().getStringExtra("UNLOCK_CODE");

        // 初始化手勢偵測器
        gestureDetector = new GestureDetectorCompat(this, this);

        // --- 接收數據 ---
        correctCode = getIntent().getStringExtra("UNLOCK_CODE");
        // 更改 3: 接收從 Game 1 傳來的分數
        scoreFromGame1 = getIntent().getIntExtra("SCORE_FROM_GAME1", 0);

        // 設定所有按鈕的點擊事件
        setupButtonListeners();

        // 首次啟動遊戲
        startGame();
    }

    // --- 更改 1: setupButtonListeners() 中，修改 loseHomeButton 的點擊邏輯 ---
    private void setupButtonListeners() {
        // 勝利畫面的按鈕 (不變)
        winHomeButton.setOnClickListener(v -> goToHome());

        // 失敗畫面的按鈕
        loseRetryButton.setOnClickListener(v -> startGame());

        // ▼▼▼ 核心更改：將 loseHomeButton 的功能從「返回主頁」改為「關閉當前頁面」 ▼▼▼
        loseHomeButton.setOnClickListener(v -> {
            // finish() 會銷毀當前的 UnlockActivity，並自動返回到啟動它的上一個 Activity (MainActivity)
            finish();
        });
        // ▲▲▲ ▲▲▲ ▲▲▲ ▲▲▲ ▲▲▲ ▲▲▲ ▲▲▲ ▲▲▲ ▲▲▲
    }

    // goToHome() 方法現在只有 winHomeButton 會用到 (方法本身不變)
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity_sing.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // 開始或重玩遊戲
    private void startGame() {
        // 1. 重設UI：顯示遊戲畫面，隱藏結果畫面
        gameContainer.setVisibility(View.VISIBLE);
        winContainer.setVisibility(View.GONE);
        loseContainer.setVisibility(View.GONE);

        // 2. 重設元件狀態
        passwordInput.setEnabled(true);
        passwordInput.setText("");

        // 3. 重設鎖頭圖片的顯示狀態
        lockClosedImage.setVisibility(View.VISIBLE);
        lockOpenImage.setVisibility(View.GONE);

        // 4. 重設邏輯狀態
        isGameFinished = false;
        startTimer(25000); // 啟動25秒計時
    }

    // 計時器邏輯
    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.format(Locale.getDefault(), "Time: %d", millisUntilFinished / 1000));
                // 更改 4: 在 onTick 中不斷更新 Game 2 的剩餘時間
                remainingMillisGame2 = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                if (isGameFinished) return;
                isGameFinished = true;

                // 時間到，切換到失敗畫面
                gameContainer.setVisibility(View.GONE);
                loseContainer.setVisibility(View.VISIBLE);
                // 失敗時，剩餘時間為0
                remainingMillisGame2 = 0;
            }
        }.start();
    }

    // 驗證密碼的邏輯 (由手勢觸發)
    private void checkPassword() {
        if (isGameFinished) return;

        String userInput = passwordInput.getText().toString().toUpperCase();

        if (userInput.equals(correctCode)) {
            // 密碼正確！
            isGameFinished = true;
            if (countDownTimer != null) countDownTimer.cancel();

            // 1. 禁用輸入框
            passwordInput.setEnabled(false);

            // 2. 播放「開鎖」的視覺效果
            lockClosedImage.setVisibility(View.GONE);
            lockOpenImage.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Unlocked!", Toast.LENGTH_SHORT).show();

            // 更改 5: 計算總分並更新UI
            int scoreGame2 = (int) (remainingMillisGame2 / 1000);
            int totalScore = scoreFromGame1 + scoreGame2;
            scoretext.setText(String.format("Score: %d", scoreGame2));
            finalScoreText.setText(String.format("Total Score: %d", totalScore));

            // 3. 使用 Handler 安排一個3秒後的延遲任務
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // 3秒後，切換到勝利畫面
                gameContainer.setVisibility(View.GONE);
                winContainer.setVisibility(View.VISIBLE);
            }, 3000); // 延遲 3 秒

        } else {
            // 密碼錯誤
            Toast.makeText(this, "Password Error!", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
        }
    }

    // --- 手勢處理方法 ---

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 將所有觸摸事件交給 gestureDetector 處理
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true; // 必須返回 true
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // 當用戶快速滑動時
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffY) > Math.abs(diffX)) { // 垂直滑動
                if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) { // 向下快速滑動
                    // 執行開鎖邏輯！
                    checkPassword();
                    return true;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    // --- 其他 GestureEventListener 的空方法 ---
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) { return false; }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    @Override
    public void onLongPress(MotionEvent e) {}

    // --- onDestroy() ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
