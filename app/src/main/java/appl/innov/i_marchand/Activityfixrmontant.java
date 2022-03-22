package appl.innov.i_marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import appl.innov.i_marchand.helper.DatabaseHelper;

public class Activityfixrmontant extends AppCompatActivity {
    DatabaseHelper myDb;
    EditText editTextmontantfix;
    Button fixer;
    String amountedit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fxrmontant);
        // Mettre le button back
        ImageView back = (ImageView)findViewById(R.id.btn_back_img);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Activityfixrmontant.this, Test_NFC_Tag_Activity.class);
                startActivity(i);
                finish();

            }
        });
        // Fin du button back

        // les fonctions de prospere
        editTextmontantfix = findViewById(R.id.edit_fix_montant);
        myDb = new DatabaseHelper(this);
        fixer = findViewById(R.id.fixmontant);
        fixer.setOnClickListener(v -> runOnUiThread(() -> fix()));
    }

    public void fix() {
        amountedit = editTextmontantfix.getText().toString();
        Cursor res = myDb.getAllData();

        if(res.moveToLast()){
            String idSession_ = res.getString(1);
            String commision = res.getString(2);
            String idCard = res.getString(3);
            String telephone = res.getString(4);
            String iduser = res.getString(5);
            String telephonefromCard = res.getString(6);
            String prenomFromCard = res.getString(7);
            String nomFromCard = res.getString(8);

            boolean isInserted = myDb.insertData(
                    idSession_, commision, idCard, telephone, iduser,telephonefromCard,prenomFromCard, nomFromCard,amountedit
            );
            if (isInserted){
                Intent i = new Intent(getApplicationContext(), NFCTagActivity.class);
                startActivity(i);
                Log.e("Wouhou sqlite", "amount edited in sqlite"+ amountedit);
            }

        }

    }
}
