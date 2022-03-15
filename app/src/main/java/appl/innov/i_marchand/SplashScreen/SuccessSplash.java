package appl.innov.i_marchand.SplashScreen;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import appl.innov.i_marchand.NFCTagActivity;
import appl.innov.i_marchand.R;

public class SuccessSplash extends AppCompatActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_success_splash);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.cash);
        mp.start();
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SuccessSplash.this, NFCTagActivity.class);
                SuccessSplash.this.startActivity(mainIntent);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
