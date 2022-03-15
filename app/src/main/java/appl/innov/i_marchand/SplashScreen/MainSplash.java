package appl.innov.i_marchand.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import appl.innov.i_marchand.LoginActivity;
import appl.innov.i_marchand.MainActivity;
import appl.innov.i_marchand.R;


public class MainSplash extends AppCompatActivity {
    Animation animRotate;
    ImageView imgrotate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imgrotate = findViewById(R.id.imganim);
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        /** Duration of wait **/
        /* Create an Intent that will start the Menu-Activity. */
        animRotate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
        imgrotate.setVisibility(View.VISIBLE);
        imgrotate.startAnimation(animRotate);
        int SPLASH_DISPLAY_LENGTH = 2000;

        animRotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent mainIntent = new Intent(MainSplash.this, MainActivity.class); // Ecran de démarrage
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }

}
