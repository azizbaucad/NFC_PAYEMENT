package appl.innov.i_marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;

public class Test_NFC_Tag_Activity extends AppCompatActivity {

    // déclarer les variables
    //private Button payNFC ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test__n_f_c__tag_);

        // Initialisé les variables
       ThemedButton payNFC = (ThemedButton) findViewById(R.id.btn_pay_nfc);
       ThemedButton fixMontant = (ThemedButton) findViewById(R.id.btn_fix_montant);

        payNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Test_NFC_Tag_Activity.this, ValidePayementActivity.class);
                startActivity(i);
                finish();

            }
        });

        fixMontant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Test_NFC_Tag_Activity.this, Activityfixrmontant.class);
                startActivity(i);
                finish(); // A revoir
            }
        });
    }
}