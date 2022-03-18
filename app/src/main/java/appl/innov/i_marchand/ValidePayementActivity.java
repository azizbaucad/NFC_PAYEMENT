package appl.innov.i_marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ValidePayementActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valide_payement);

        ImageView back = (ImageView)findViewById(R.id.btn_back_img);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ValidePayementActivity.this, Test_NFC_Tag_Activity.class);
                startActivity(i);
                finish();
            }
        });
    }
}