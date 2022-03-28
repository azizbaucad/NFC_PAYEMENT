package appl.innov.i_marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import appl.innov.i_marchand.helper.DatabaseHelper;
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;

public class Test_NFC_Tag_Activity extends AppCompatActivity {

    // déclarer les variables globales
    DatabaseHelper myDb;
    TextView soldeText;
    // Fin de la déclaration des variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test__n_f_c__tag_);
        // Initialisé les variables
       ThemedButton payNFC = (ThemedButton) findViewById(R.id.btn_pay_nfc);
       ThemedButton fixMontant = (ThemedButton) findViewById(R.id.btn_fix_montant);

       myDb = new DatabaseHelper(this);

       soldeText = findViewById(R.id.soldeId);
       //getAllListAccount();
       // Fin de l'initialisation des vraiables
        // Button payNFC pour étendre le paiement par NFC
        payNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Test_NFC_Tag_Activity.this, ValidePayementActivity.class);
                startActivity(i);
                finish();

            }
        });
        // Fin du Button payNFC pour étendre le paiement NFC

        // Button fixMontant pour étendre Fixer Montant
        fixMontant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Test_NFC_Tag_Activity.this, Activityfixrmontant.class);
                startActivity(i);
                finish(); // A revoir
            }
        });
        // Fin du Button fixMontant pour étendre Fixer Montant
    }
    // Création de la fonction getAllListAccount
    public void getAllListAccount() {
        Cursor res = myDb.getAllData();
        if (res.moveToLast()) {
            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";
            String SOAP_ACTION = "";
            String METHOD_NAME = "getAllListAccount";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session = new PropertyInfo();
            session.setName("sessionId");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            String telephone;
            PropertyInfo tel = new PropertyInfo();
            tel.setName("cellulaire");
            telephone = res.getString(4);
            tel.setValue(telephone);
            tel.setType(String.class);
            request.addProperty(tel);

            try {

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                String error = result.getProperty(1).toString();
                Log.e("result", "responceeeeeeee from ws getAllListAccount" + result);
                if (error.equals("0")) {

                    String accounts = result.getProperty(0).toString();
                    String[] stringArray = accounts.split(";");
                    String sold = stringArray[4]; // propriété du webservice
                    String[] soldeArray = sold.split("=");
                    String soldeBrute = soldeArray[1];

                    Log.e("result", "responceeeeeeee from ws accounts" + accounts);
                    Log.e("result", "responceeeeeeee from ws solde" + soldeBrute);
                    soldeText.setText(soldeBrute + getResources().getString(R.string.currency));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Fin de la création de la fonction getAllListAccount
    // Appel de la fonction get AllListAccount de façon Asynchrone
    class AsyncCallWS2 extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(Test_NFC_Tag_Activity.this);

        @Override
        protected String doInBackground(String... url) {
            getAllListAccount();
            return null;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            Dialog.setMessage("Chargement...");
            Dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(s);
            Dialog.dismiss();
        }
    }
    // Fin de l'appel de la fonction getAllListAccount
}