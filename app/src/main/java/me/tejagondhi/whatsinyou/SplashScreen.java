package me.tejagondhi.whatsinyou;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        launchMainActivity();

    }

    private void launchMainActivity() {
         new Thread(new Runner()).start();

    }
    public class Runner implements Runnable{

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                SplashScreen.this.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
