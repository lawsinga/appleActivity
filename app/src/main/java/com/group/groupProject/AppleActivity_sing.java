package com.learning.group_project_game;

// 引入所有需要的類別 (與你提供的版本相同)
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;
import java.util.Random;

public class AppleActivity_sing extends AppCompatActivity implements SensorEventListener {

    // --- 更改 1: UI元件變數宣告，加入失敗畫面的元件 ---
    private ConstraintLayout gameContainer, winContainer, loseContainer; // 新增 loseContainer
    private ImageView apple1, apple2, apple3;
    private TextView timerText, codeDisplayText;
    // 為了區分，將勝利和失敗畫面的按鈕分開宣告
    private Button nextLevelButton, winReplayButton, winHomeButton;
    private Button loseReplayButton, loseHomeButton;

    // --- 邏輯變數 (與你提供的版本相同) ---
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final int SHAKE_THRESHOLD = 800;
    private long lastUpdate;
    private float last_x, last_y, last_z;
    private CountDownTimer countDownTimer;
    private boolean hasFallen = false;
    private boolean isTimerFinished = false;
    private String generatedCode = "";
    private static final int UNLOCK_ACTIVITY_REQUEST_CODE = 1;
    private TextView winScoreTextGame1;
    private long remainingMillisGame1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple_sing);

        // --- 更改 2: 初始化所有UI元件，包括失敗畫面的 ---
        gameContainer = findViewById(R.id.game_container);
        winContainer = findViewById(R.id.win_container);
        loseContainer = findViewById(R.id.lose_container); // 初始化 loseContainer

        apple1 = findViewById(R.id.apple_1);
        apple2 = findViewById(R.id.apple_2);
        apple3 = findViewById(R.id.apple_3);
        timerText = findViewById(R.id.timer_text);
        codeDisplayText = findViewById(R.id.code_display_text);

        winScoreTextGame1 = findViewById(R.id.win_score_text_game1);  // 初始化分數 TextView

        // 為了ID不衝突，XML中的ID已改為 win_replay_button / lose_replay_button 等
        nextLevelButton = findViewById(R.id.next_level_button);
        winReplayButton = findViewById(R.id.win_replay_button); // 舊的 replayButton
        winHomeButton = findViewById(R.id.win_home_button);   // 舊的 homeButton

        // 初始化失敗畫面的按鈕
        loseReplayButton = findViewById(R.id.lose_replay_button);
        loseHomeButton = findViewById(R.id.lose_home_button);

        // --- 初始化感應器 (與你提供的版本相同) ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // --- 設定所有按鈕的點擊事件 ---
        setupButtonListeners();

        // --- 首次啟動遊戲 (與你提供的版本相同) ---
        startGame();
    }

    private void setupButtonListeners() {
        // 勝利畫面的按鈕
        winReplayButton.setOnClickListener(v -> startGame());
        winHomeButton.setOnClickListener(v -> goToHome());

        nextLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(AppleActivity_sing.this, UnlockActivity_sing.class);
            intent.putExtra("UNLOCK_CODE", generatedCode);

            // 更改 2: 將 Game 1 的得分也傳遞過去
            int scoreGame1 = (int) (remainingMillisGame1 / 1000);
            intent.putExtra("SCORE_FROM_GAME1", scoreGame1);

            startActivityForResult(intent, UNLOCK_ACTIVITY_REQUEST_CODE);
        });

        // 失敗畫面的按鈕 (不變)
        loseReplayButton.setOnClickListener(v -> startGame());
        loseHomeButton.setOnClickListener(v -> goToHome());
    }

    // --- 更改 4: 新增一個返回主頁的通用方法，避免程式碼重複 ---
    private void goToHome() {
        Intent intent = new Intent(AppleActivity_sing.this, HomeActivity_sing.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // --- 更改 5: startGame() 方法需要確保 loseContainer 也被隱藏 ---
    private void startGame() {
        gameContainer.setVisibility(View.VISIBLE);
        winContainer.setVisibility(View.GONE);
        loseContainer.setVisibility(View.GONE); // 也要隱藏失敗畫面

        hasFallen = false;
        isTimerFinished = false;
        apple1.setTranslationY(0);
        apple2.setTranslationY(0);
        apple3.setTranslationY(0);
        lastUpdate = System.currentTimeMillis();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        startTimer();
    }

    // --- 更改 6: startTimer() 的 onFinish() 失敗邏輯被徹底重寫 ---
    private void startTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.format(Locale.getDefault(), "Time: %d", millisUntilFinished / 1000));
                // 在 onTick 中不斷更新剩餘時間
                remainingMillisGame1 = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                isTimerFinished = true;
                timerText.setText("Time's Up!");
                remainingMillisGame1 = 0;

                // 只有在時間到時，蘋果還沒掉下來才算失敗
                if (!hasFallen) {
                    Toast.makeText(AppleActivity_sing.this, "失敗！", Toast.LENGTH_SHORT).show();
                    sensorManager.unregisterListener(AppleActivity_sing.this);

                    // 舊邏輯是自動重玩，新邏輯是切換到失敗畫面
                    gameContainer.setVisibility(View.GONE);
                    loseContainer.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    // --- onShake() 勝利邏輯 (與你提供的版本完全相同) ---
    private void onShake() {
        if (isTimerFinished || hasFallen) return;

        if (countDownTimer != null) countDownTimer.cancel();

        hasFallen = true;
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "成功！", Toast.LENGTH_SHORT).show();

        generatedCode = generateRandomCode(6);
        codeDisplayText.setText(generatedCode);

        int scoreGame1 = (int) (remainingMillisGame1 / 1000);
        winScoreTextGame1.setText(String.format("Score: +%d", scoreGame1));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(apple1, "translationY", screenHeight);
        animator1.setDuration(1500);
        animator1.setInterpolator(new AccelerateInterpolator());
        animator1.start();

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(apple2, "translationY", screenHeight);
        animator2.setDuration(1700);
        animator2.setStartDelay(200);
        animator2.setInterpolator(new AccelerateInterpolator());
        animator2.start();

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(apple3, "translationY", screenHeight);
        animator3.setDuration(1800);
        animator3.setStartDelay(200);
        animator3.setInterpolator(new AccelerateInterpolator());
        animator3.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            gameContainer.setVisibility(View.GONE);
            winContainer.setVisibility(View.VISIBLE);
        }, 2000);
    }

    // --- 其他所有方法 (generateRandomCode, onResume, onPause, onDestroy, onSensorChanged, onAccuracyChanged) ---
    // --- 這些方法的內部邏輯都與你提供的版本相同 ---
    private String generateRandomCode(int length) { /* ...與你提供的版本相同... */
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
    // --- 更改 4: onResume() 中移除了自動重玩的邏輯 ---
    @Override
    protected void onResume() {
        super.onResume();
        // 我們不再希望每次返回這個畫面時都自動重玩，
        // 因為可能是從 Game 2 失敗後回來看密碼。
        // 只有在遊戲還未結束的狀態下，才需要重新註冊感應器。
        if (!hasFallen && !isTimerFinished) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // --- 更改 5: 全新增加 onActivityResult() 方法 ---
    // 當一個由 startActivityForResult 啟動的 Activity (例如 UnlockActivity) 關閉後，
    // 這個方法會被自動呼叫。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 檢查返回的請求碼是否是我們啟動 UnlockActivity 時用的那一個
        if (requestCode == UNLOCK_ACTIVITY_REQUEST_CODE) {
            // 是的，玩家從 Game 2 回來了。
            // 我們在這裡不需要做任何特別的事情。
            // 由於 onResume() 不再自動重玩，畫面會自然地停留在勝利畫面上，
            // 讓玩家可以重新看到密碼。
            Toast.makeText(this, "find the password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() { /* ...與你提供的版本相同... */
        super.onPause();
        sensorManager.unregisterListener(this);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    @Override
    protected void onDestroy() { /* ...與你提供的版本相同... */
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) { /* ...與你提供的版本相同... */
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    onShake();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { /* ...與你提供的版本相同... */ }
}
