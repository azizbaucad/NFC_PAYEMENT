package appl.innov.i_marchand;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Button bouton_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bouton_connect = (Button) findViewById(R.id.loginBtn);

        bouton_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this,
                        Test_NFC_Tag_Activity.class);

                startActivity(intent);
                // close this activity
                finish(); // terminer l'activité ou non

            }
        });
    }


}
