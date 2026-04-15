package com.group.groupProject.game.appleActivity;

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


    private ConstraintLayout gameContainer, winContainer, loseContainer;
    private ImageView apple1, apple2, apple3;
    private TextView timerText, codeDisplayText;
    private Button nextLevelButton, winReplayButton, winHomeButton;
    private Button loseReplayButton, loseHomeButton;

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
    private long remainingMillisGame1 = 30000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple_sing);

        gameContainer = findViewById(R.id.game_container);
        winContainer = findViewById(R.id.win_container);
        loseContainer = findViewById(R.id.lose_container);
        apple1 = findViewById(R.id.apple_1);
        apple2 = findViewById(R.id.apple_2);
        apple3 = findViewById(R.id.apple_3);
        timerText = findViewById(R.id.timer_text);
        codeDisplayText = findViewById(R.id.code_display_text);
        winScoreTextGame1 = findViewById(R.id.win_score_text_game1);
        nextLevelButton = findViewById(R.id.next_level_button);
        winReplayButton = findViewById(R.id.win_replay_button);
        winHomeButton = findViewById(R.id.win_home_button);
        loseReplayButton = findViewById(R.id.lose_replay_button);
        loseHomeButton = findViewById(R.id.lose_home_button);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        setupButtonListeners();

        startGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    private void setupButtonListeners() {
        winReplayButton.setOnClickListener(v -> startGame());
        winHomeButton.setOnClickListener(v -> goToHome());

        nextLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(AppleActivity_sing.this, UnlockActivity_sing.class);
            intent.putExtra("UNLOCK_CODE", generatedCode);

            int scoreGame1 = (int) (remainingMillisGame1 / 1000);
            intent.putExtra("SCORE_FROM_GAME1", scoreGame1);

            startActivityForResult(intent, UNLOCK_ACTIVITY_REQUEST_CODE);
        });

        loseReplayButton.setOnClickListener(v -> startGame());
        loseHomeButton.setOnClickListener(v -> goToHome());
    }

    private void goToHome() {
        Intent intent = new Intent(AppleActivity_sing.this, HomeActivity_sing.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void startGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        gameContainer.setVisibility(View.VISIBLE);
        winContainer.setVisibility(View.GONE);
        loseContainer.setVisibility(View.GONE);

        hasFallen = false;
        isTimerFinished = false;
        remainingMillisGame1 = 30000;

        apple1.setTranslationY(0);
        apple2.setTranslationY(0);
        apple3.setTranslationY(0);
        timerText.setText(String.format(Locale.getDefault(), "Time: %d", remainingMillisGame1 / 1000));

    }

    private void resumeGame() {
        if (!hasFallen && !isTimerFinished) {
            lastUpdate = System.currentTimeMillis();
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            startTimer(remainingMillisGame1);
        }
    }

    private void pauseGame() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingMillisGame1 = millisUntilFinished;
                timerText.setText(String.format(Locale.getDefault(), "Time: %d", remainingMillisGame1 / 1000));
            }

            public void onFinish() {
                isTimerFinished = true;
                remainingMillisGame1 = 0;
                if (!hasFallen) {
                    pauseGame();
                    gameContainer.setVisibility(View.GONE);
                    loseContainer.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    private void onShake() {
        if (isTimerFinished || hasFallen) return;

        hasFallen = true;
        pauseGame();

        Toast.makeText(this, "success！", Toast.LENGTH_SHORT).show();

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

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UNLOCK_ACTIVITY_REQUEST_CODE) {

            Toast.makeText(this, "find the password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseGame();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }
}

