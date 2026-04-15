package com.group.groupProject.game.appleActivity;

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

public class UnlockActivity_sing extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private RelativeLayout gameContainer, winContainer, loseContainer;
    private EditText passwordInput;
    private TextView timerText;
    private ImageView lockClosedImage, lockOpenImage;
    private Button winHomeButton;
    private Button loseRetryButton, loseHomeButton;
    private GestureDetectorCompat gestureDetector;
    private CountDownTimer countDownTimer;
    private String correctCode;
    private boolean isGameFinished = false;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private TextView finalScoreText;
    private TextView scoretext;
    private long remainingMillisGame2 = 25000;
    private int scoreFromGame1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_sing);

        gameContainer = findViewById(R.id.unlock_game_container);
        winContainer = findViewById(R.id.unlock_win_container);
        loseContainer = findViewById(R.id.unlock_lose_container);
        passwordInput = findViewById(R.id.password_input);
        timerText = findViewById(R.id.unlock_timer_text);
        lockClosedImage = findViewById(R.id.lock_closed_image);
        lockOpenImage = findViewById(R.id.lock_open_image);
        winHomeButton = findViewById(R.id.unlock_win_home_button);
        finalScoreText = findViewById(R.id.final_score_text);
        scoretext = findViewById(R.id.level_score_text);
        loseRetryButton = findViewById(R.id.unlock_lose_retry_button);
        loseHomeButton = findViewById(R.id.unlock_lose_home_button);
        correctCode = getIntent().getStringExtra("UNLOCK_CODE");
        gestureDetector = new GestureDetectorCompat(this, this);
        correctCode = getIntent().getStringExtra("UNLOCK_CODE");
        scoreFromGame1 = getIntent().getIntExtra("SCORE_FROM_GAME1", 0);

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

        winHomeButton.setOnClickListener(v -> goToHome());
        loseRetryButton.setOnClickListener(v -> startGame());
        loseHomeButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity_sing.class);
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
        passwordInput.setEnabled(true);
        passwordInput.setText("");
        lockClosedImage.setVisibility(View.VISIBLE);
        lockOpenImage.setVisibility(View.GONE);
        isGameFinished = false;
        remainingMillisGame2 = 25000;
        timerText.setText(String.format(Locale.getDefault(), "Time: %d", remainingMillisGame2 / 1000));
    }

    private void resumeGame() {
        if (!isGameFinished) {
            startTimer(remainingMillisGame2);
        }
    }

    private void pauseGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingMillisGame2 = millisUntilFinished;
                timerText.setText(String.format(Locale.getDefault(), "Time: %d", remainingMillisGame2 / 1000));
            }

            public void onFinish() {
                if (isGameFinished) return;
                isGameFinished = true;
                remainingMillisGame2 = 0;

                pauseGame();

                gameContainer.setVisibility(View.GONE);
                loseContainer.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void checkPassword() {
        if (isGameFinished) return;

        String userInput = passwordInput.getText().toString().toUpperCase();

        if (userInput.equals(correctCode)) {
            isGameFinished = true;
            pauseGame();

            passwordInput.setEnabled(false);

            lockClosedImage.setVisibility(View.GONE);
            lockOpenImage.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Unlocked!", Toast.LENGTH_SHORT).show();

            int scoreGame2 = (int) (remainingMillisGame2 / 1000);
            int totalScore = scoreFromGame1 + scoreGame2;
            scoretext.setText(String.format("Score: %d", scoreGame2));
            finalScoreText.setText(String.format("Total Score: %d", totalScore));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                gameContainer.setVisibility(View.GONE);
                winContainer.setVisibility(View.VISIBLE);
            }, 3000);

        } else {
            Toast.makeText(this, "Password Error!", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffY) > Math.abs(diffX)) {
                if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    checkPassword();
                    return true;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) { return false; }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    @Override
    public void onLongPress(MotionEvent e) {}

}
