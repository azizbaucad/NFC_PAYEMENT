package appl.innov.i_marchand;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import appl.innov.i_marchand.NFCManager.NFCManager;
import appl.innov.i_marchand.NFCManager.NFCWriteException;
import appl.innov.i_marchand.helper.DatabaseHelper;

public class NFCTagActivity extends AppCompatActivity implements
    NFCManager.TagReadListener, NFCManager.TagWriteListener, NFCManager.TagWriteErrorListener,
    NavigationView.OnNavigationItemSelectedListener {
    // Déclarations des variables
    DatabaseHelper myDb;
    EditText montant_a_payer;
    String montantaPayer;
    TextView soldeText;
    public String commision, fixedAmount;
    Button deconnexionBtn;
    CheckBox fixAmount;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;

    public static final String DATA_TO_WRITE_KEY = "DATA_TO_WRITE";
    public static final int READ_REQUEST_CODE = 1;
    public static final int WRITE_REQUEST_CODE = 2;
    public static final String ACTION_STATUS = "ACTION_STATUS";
    public static final String ACTION_TAG_ID = "ACTION_TAG_ID";
    public static final String ACTION_DATA_READ = "ACTION_DATA_READ";
    public static final String ACTION_DATA_CUSTOM = "ACTION_DATA_CUSTOM";
    private String dataToWrite;
    private String dataCustom;
    private Handler timer;
    private NFCManager nfcManager;

    // fin de la déclaration des variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfctag_activity);

        // Initialisation des variables
        montant_a_payer = findViewById(R.id.montantApayer);
        myDb = new DatabaseHelper(this);
        soldeText = findViewById(R.id.soldeId);
        fixAmount = findViewById(R.id.checkbox);
        //deconnexionBtn = findViewById(R.id.deconnexion);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {};

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        toggle.setHomeAsUpIndicator(R.drawable.ic_ios_menu_icon);

        // fonction on click pour voir la list des transactions
        soldeText.setOnClickListener(
                v -> {
                    runOnUiThread(() -> {
                        Intent i = new Intent(getApplicationContext(), ListTransaction.class);
                        startActivity(i);
                    });
                }
        );
        // Fin de la visualisation de la liste des Transactions

        // Button on Clikc permettant de fixer le montant de la variable à ajouter
        fixAmount.setOnClickListener(v -> {
            if (fixAmount.isChecked()) {
                Intent i = new Intent(NFCTagActivity.this.getApplicationContext(), Activityfixrmontant.class);
                NFCTagActivity.this.startActivity(i);
            } else {

            }

        });
        // Fin du button pour fixer la variables

        // Initialisation des variables liées au Tag NFC
        this.dataToWrite = this.getIntent().getStringExtra(NFCTagActivity.DATA_TO_WRITE_KEY);
        this.dataCustom = this.getIntent().getStringExtra(NFCTagActivity.ACTION_DATA_CUSTOM);

        this.timer = new Handler();
        this.nfcManager = new NFCManager(this);
        this.nfcManager.onActivityCreate();

        this.nfcManager.setOnTagReadListener(this);
        this.nfcManager.setOnTagWriteListener(this);
        this.nfcManager.setOnTagWriteErrorListener(this);
        // Fin de l'initialisation des var liées au Tag
        // Condition de la lecture/Ecriture du Tag
        if (!this.isReadingMode()) {
            this.nfcManager.writeText(this.dataToWrite);
        }
        // Fin de la condition Lecture/Ecriture du Tag

        //deconnexionBtn.setOnClickListener(v -> confirmDeconnexionDialogue());
        // Appels des fonctions de manière synchrone
        AsyncCallWS2 task = new AsyncCallWS2();
        task.execute();
        // fin de l'appel des fonctions de manière synchrone
    }
    // Démarrage du développement des foncions
    // création de la fonction confirmDeconnexionDialogue
    public void confirmDeconnexionDialogue() {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NFCTagActivity.this);
        // Set a title for alert dialog
        builder.setTitle("Avertissement");
        // Ask the final question
        builder.setMessage("Voulez-vous vraiment vous déconnecter ?");
        // Set the alert dialog yes button click listener
        builder.setPositiveButton("oui", (dialog, which) -> {
            AsyncCallWSDeconnexion2 task = new AsyncCallWSDeconnexion2();
            task.execute();
        });
        // Set the alert dialog no button click listener
        builder.setNegativeButton("non", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }
    // Fin de la fonction ConfirmDeconnexionDialog
    @Override
    // Alimentation de la fonction @Override onTagRead
    public void onTagRead(final String tagId, String tagRead) {
        if (!this.isReadingMode())
            return;
        final String ftagId = tagId;
        final String ftagRead = tagRead;
        this.changeStatus(true);
        this.timer.postDelayed(() -> {
            Intent intent = new Intent();
            intent.putExtra(NFCTagActivity.ACTION_STATUS, true);
            intent.putExtra(NFCTagActivity.ACTION_TAG_ID, ftagId);
            intent.putExtra(NFCTagActivity.ACTION_DATA_READ, ftagRead);
            //idc.setText(tagId);
            intent.putExtra(NFCTagActivity.ACTION_DATA_CUSTOM, NFCTagActivity.this.dataCustom);
            NFCTagActivity.this.setResult(NFCTagActivity.READ_REQUEST_CODE, intent);
            NFCTagActivity.this.finish();
        }, 10);
    }
    // Fin de l'alimentation de la fonction onTagRead

    @SuppressLint("StaticFieldLeak")
    // Appeles des fonctions de façons asunchrone
    class AsyncCallWSDirectSearch extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(NFCTagActivity.this);
        @Override
        protected String doInBackground(String... url) {
            getUoByCard();
            return null;
        }
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("chargement...");
            Dialog.show();
        }
        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
        }
    }
    // Fin de l'appel des fonctions de façons asynchrones

    @SuppressLint("StaticFieldLeak")
    // Appel de la fonction deconnerxionUser de façon synchrone
    class AsyncCallWSDeconnexion2 extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(NFCTagActivity.this);

        @Override
        protected String doInBackground(String... url) {
            deconnexionUser();
            return null;
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("chargement...");
            Dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
        }
    }
    // fin de l'appel de la fonction deconnexionUser de façon synchrone

    // Création de la fonction deconnexionUser
    public void deconnexionUser() {
        Cursor res = myDb.getAllData();
        if (res.moveToLast()) {

            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";

            String SOAP_ACTION = "";
            String METHOD_NAME = "deconnexionUser";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session = new PropertyInfo();
            session.setName("idSession");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            Log.e("result", "idddddd sessssssion avant appel de deconnexionUser" + idSession_);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                String error = result.getProperty(0).toString();

                Log.e("result", "responceeeeeeee from ws deconnexionUserrrrrr" + result);
                if (error.equals("1")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            deconnexionFailed();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Fin de la création de la fonction deconnexionUser

    // Création de la fonction deconnxionFailed
    public void deconnexionFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(NFCTagActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("la déconnexion a échoué");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
    // Fin de la crétation de la fonction deconnexionFailed

    @SuppressLint("StaticFieldLeak")
    // Appelle de la focntion gettAllListAccount de façon asynchrone
    class AsyncCallWS2 extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(NFCTagActivity.this);
        @Override
        protected String doInBackground(String... url) {
            getAllListAccount();
            return null;
        }
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("chargement...");
            Dialog.show();
        }
        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncCallWS extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(NFCTagActivity.this);

        @Override
        protected String doInBackground(String... url) {
            getCommission();
            payerDirectement();
            return null;
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("chargement...");
            Dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
        }
    }

    public void getUoByCard() {
        Cursor res = myDb.getAllData();
        if (res.moveToLast()) {
            String amount = res.getString(9);

            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";


            String SOAP_ACTION = "";
            String METHOD_NAME = "getUoByCard";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session = new PropertyInfo();
            session.setName("sessionId");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);
            Log.e("result", "id session avant entrée dans le try" + idSession_);

            String idCard;
            //Pass value for idCard variable of the web service
            PropertyInfo card_id = new PropertyInfo();
/*
            String idca = "26801F05";
*/
            card_id.setName("idcard");
            idCard = res.getString(3);
            card_id.setValue(idCard);
            card_id.setType(String.class);
            request.addProperty(card_id);

            String telephone = res.getString(4);
            String iduser = res.getString(5);
            Log.e("result", "id session avant entrée dans le try" + idSession_);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                String error = result.getProperty(0).toString();
                if (error.equals("0")) {

                    Log.e("result", "Commission insérée" + error);
                    String idcarte = result.getProperty(1).toString();
                    String message = result.getProperty(2).toString();
                    String nomFromCard = result.getProperty(3).toString();
                    String prenomFromCard = result.getProperty(4).toString();
                    String telephonefromCard = result.getProperty(5).toString();

                    Log.e("tellll", "telephone lié à la carte" + telephonefromCard);
                    Log.e("PRE", "prenom lié à la carte" + prenomFromCard);
                    Log.e("NOM", "nom lié à la carte" + nomFromCard);
                    boolean isInserted = myDb.insertData(idSession_, commision, idCard, telephone, iduser, telephonefromCard, prenomFromCard, nomFromCard, amount);

                    if (isInserted) {
                        Log.e("telephone", "telephone lié à la carte inséré" + telephonefromCard);
                        Log.e("prenom", "prenom lié à la carte inséré" + prenomFromCard);
                        Log.e("nom", "nom lié à la carte inséré" + nomFromCard);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    //maintenant si la carte est deja enregistrée appeler directement le ws paiementMarchand
    public void payerDirectement() {
        Cursor res = myDb.getAllData();
        if (res.moveToLast()) {

            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";

            String SOAP_ACTION = "";
            String METHOD_NAME = "W2WPaiementMarchandWithCard";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session = new PropertyInfo();
            session.setName("sessionId");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            String idCard;
            //Pass value for idCard variable of the web service
            PropertyInfo card_id = new PropertyInfo();
            card_id.setName("idcard");
            idCard = res.getString(3);
            card_id.setValue(idCard);
            card_id.setType(String.class);
            request.addProperty(card_id);

            montantaPayer = montant_a_payer.getText().toString();
            //Set the commission amount retrivied
            PropertyInfo montant = new PropertyInfo();
            montant.setName("montant");
            montant.setValue(montantaPayer);
            montant.setType(String.class);
            request.addProperty(montant);

            String commision;
            //set the commission retrieved
            PropertyInfo comm_ = new PropertyInfo();
            comm_.setName("commission");
            commision = res.getString(2);
            comm_.setValue(commision);
            comm_.setType(String.class);
            request.addProperty(comm_);
            //Pass value for code variable of the web service

            String telephonecode = res.getString(4);
            //set the commission retrieved
            PropertyInfo code = new PropertyInfo();
            code.setName("codeMarchand");
            //telephone =  res.getString(4);
            code.setValue(telephonecode);
            code.setType(String.class);
            request.addProperty(code);

            String objet__ = "PAIEMENT NFC";
            PropertyInfo objet = new PropertyInfo();
            objet.setName("objet");
            objet.setValue(objet__);
            objet.setType(String.class);
            request.addProperty(objet);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE" + result);
                String erreur = result.getProperty(0).toString();
                String message = result.getProperty(1).toString();
                Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE" + erreur);
                Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE" + message);
                Log.e("resultss", "id carte" + idCard);

                if (erreur.equals("0")) {
                    runOnUiThread(() -> {
                        PaiementSuccess();
                    });
                } else if (erreur.equals("-1")) {
                    /* Intent intent = new Intent(getApplicationContext(), SuccessSplash.class);
                        startActivity(intent);*/
                    runOnUiThread(this::SoldeInsuffisant);
                } else if (erreur.equals("12")) {
                    runOnUiThread(() -> onUserSession());
                } else {
                    runOnUiThread(() -> EnrolemmentDialogue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
                    String sold = stringArray[3];
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

    public void consultationSolde() {
        Cursor res = myDb.getAllData();
        if (res.moveToLast()) {

            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";

            String SOAP_ACTION = "";
            String METHOD_NAME = "consultationSolde";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session = new PropertyInfo();
            session.setName("sessionId");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);
            Log.e("resultttttttttt", "id session avant appel de consultationsolde" + idSession_);

            String idUser;
            //set the accountId retrieved
            PropertyInfo iduser = new PropertyInfo();
            iduser.setName("accountId");
            idUser = res.getString(5);
            iduser.setValue(idUser);
            iduser.setType(String.class);
            request.addProperty(iduser);
            Log.e("resulttttttttt", "id user avant appel de consultationsolde" + idUser);

            String trois = res.getString(3);
            String quatre = res.getString(4);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                String error = result.getProperty(1).toString();

                Log.e("result", "responceeeeeeee from ws RENVOIEOTPPPPPPPPPPP" + result);
                Log.e("result", "idSession " + idSession_);
                Log.e("result", "idUser " + idUser);
                Log.e("result", "trois " + trois);
                Log.e("result", "quatre " + quatre);

                if (error.equals("0")) {
                    String solde = result.getProperty(3).toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void getCommission() {
        Cursor res = myDb.getAllData();
        if (res.moveToLast()) {

            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";

            String SOAP_ACTION = "";
            String METHOD_NAME = "getCommissionsTTC";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session = new PropertyInfo();
            session.setName("sessionId");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            montantaPayer = montant_a_payer.getText().toString();
            //Set the commission amount retrivied
            PropertyInfo montant = new PropertyInfo();
            montant.setName("montant");
            montant.setValue(montantaPayer);
            montant.setType(String.class);
            request.addProperty(montant);

            String servic = "W2WMARCHAND";
            PropertyInfo service = new PropertyInfo();
            service.setName("service");
            service.setValue(servic);
            service.setType(String.class);
            request.addProperty(service);

            String type_ = "NORMAL";
            PropertyInfo type = new PropertyInfo();
            type.setName("type");
            type.setValue(type_);
            type.setType(String.class);
            request.addProperty(type);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE" + result);
                System.out.println("call Donnnnnnnnn getCmmissionnnnnnnnnnnnnnnnnnnn");
                commision = result.getProperty(0).toString();
                String tagId = res.getString(3);
                String telephone = res.getString(4);
                String iduser = res.getString(5);

                Log.e("result", "tag Id on getCommission : " + tagId);
                String amount = res.getString(9);

                boolean isInserted = myDb.insertData(
                        idSession_, commision, tagId, telephone, iduser, null, null, null, amount
                );

                if (isInserted) {
                    Log.e("result", "Commission : " + commision);

                    Log.e("result", "Commission insérée" + commision);
                    Log.e("result", "iduser on getCommission : " + iduser);


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void onUserSession() {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NFCTagActivity.this);

        // Set a title for alert dialog
        builder.setTitle("Information");

        // Ask the final question
        builder.setMessage("Votre session a expiré ! Voulez-vous vous reconnecter ?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when user clicked the Yes button
                // Set the TextView visibility GONE
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();


            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
                startActivity(intent);
                runOnUiThread(new Runnable() {
                    public void run() {
                        AsyncCallWS2 task = new AsyncCallWS2();
                        task.execute();
                    }
                });
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }

    public void EnrolemmentDialogue() {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NFCTagActivity.this);

        // Set a title for alert dialog
        builder.setTitle("Information");

        // Ask the final question
        builder.setMessage("Cette carte n'est pas enregistrée! Voulez-vous l'enroler et payer directement ?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when user clicked the Yes button
                // Set the TextView visibility GONE
                Intent intent = new Intent(getApplicationContext(), EnrollementActivity.class);
                startActivity(intent);
                dialog.dismiss();

            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("non", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
            startActivity(intent);
            runOnUiThread(() -> {
                AsyncCallWS2 task = new AsyncCallWS2();
                task.execute();
            });
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }


    public void SoldeInsuffisant() {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NFCTagActivity.this);

        // Set a title for alert dialog
        builder.setTitle("Alerte");
        builder.setIcon(R.drawable.ic_cross);
        // Ask the final question
        builder.setMessage("Votre solde est insuffisant! Voulez-vous procéder à un dépôt ?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when user clicked the Yes button
                // Set the TextView visibility GONE
                AsyncCallWSDirectSearch task = new AsyncCallWSDirectSearch();
                task.execute();

                Intent intent = new Intent(getApplicationContext(), EnrollementActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }

    public void PaiementSuccess() {
        AlertDialog alertDialog = new AlertDialog.Builder(NFCTagActivity.this).create();
        alertDialog.setTitle("Succès");
        alertDialog.setIcon(R.drawable.ic_tick);
        alertDialog.setMessage("Paiement effectué avec succès");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void openDialogueMontant() {
        AlertDialog alertDialog = new AlertDialog.Builder(NFCTagActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Entrez le montant d'abord");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onTagRead(final String tagId) {
        if (!this.isReadingMode())
            return;
        final String ftagId = tagId;
        this.changeStatus(true);
        this.timer.postDelayed(() -> {
            Intent intent = new Intent();
            intent.putExtra(NFCTagActivity.ACTION_STATUS, true);
            intent.putExtra(NFCTagActivity.ACTION_TAG_ID, ftagId);
            Cursor res = myDb.getAllData();
            Log.e("RESSSS", res.toString());

            if (res.moveToLast()) {
                String idSession_, commision, telephone, iduser, amount;
                idSession_ = res.getString(1);
                commision = res.getString(2);
                telephone = res.getString(4);
                iduser = res.getString(5);
                amount = res.getString(9);

                Log.e("result", "tagiddd from ontagread" + tagId);
                Log.e("result", "tagiddd from ontagread" + amount);

                boolean isInserted = myDb.insertData(idSession_, commision, tagId, telephone, iduser, null, null, null, amount);
                if (montant_a_payer.getText().length() != 0 && !montant_a_payer.getText().toString().equals("") && !fixAmount.isChecked()) {
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();
                } else if ((fixAmount.isChecked() && amount == null) || (amount == null)) {
                    youMustFixAmount();
                } else if (amount == null && !fixAmount.isChecked()) {
                    openDialogueMontant();
                } else {
                    montant_a_payer.setText(amount);
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();
                }

            }
            intent.putExtra(NFCTagActivity.ACTION_DATA_CUSTOM, NFCTagActivity.this.dataCustom);
            NFCTagActivity.this.setResult(NFCTagActivity.READ_REQUEST_CODE, intent);
            //NFCTagActivity.this.finish();
        }, 100);
    }

    private void youMustFixAmount() {
        AlertDialog alertDialog = new AlertDialog.Builder(NFCTagActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Merci de fixer un montant");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onTagWriteError(NFCWriteException exception) {
        this.changeStatus(false);
        this.timer.postDelayed(() -> {
            Intent intent = new Intent();
            intent.putExtra(NFCTagActivity.ACTION_STATUS, false);
            intent.putExtra(NFCTagActivity.ACTION_DATA_CUSTOM, NFCTagActivity.this.dataCustom);
            NFCTagActivity.this.setResult(NFCTagActivity.WRITE_REQUEST_CODE, intent);
            NFCTagActivity.this.finish();
        }, 100);
    }

    @Override
    public void onTagWritten() {
        this.changeStatus(true);
        this.timer.postDelayed(() -> {
            Intent intent = new Intent();
            intent.putExtra(NFCTagActivity.ACTION_STATUS, true);
            intent.putExtra(NFCTagActivity.ACTION_DATA_CUSTOM, NFCTagActivity.this.dataCustom);
            NFCTagActivity.this.setResult(NFCTagActivity.WRITE_REQUEST_CODE, intent);
            NFCTagActivity.this.finish();
        }, 100);
    }


    private void changeStatus(boolean isOperationOk) {
        // this.promptText.setText("");
        if (isOperationOk) {
            this.nfcManager.setOnTagReadListener(null);
            // this.promptIcon.setImageResource(R.drawable.ic_check);
        } else {
            //this.promptIcon.setImageResource(R.drawable.ic_cross);
        }
    }

    @Override
    protected void onResume() {
        this.nfcManager.onActivityResume();
        super.onResume();

    }

    @Override
    protected void onPause() {
        this.nfcManager.onActivityPause();
        super.onPause();

    }

    @Override
    public void onNewIntent(Intent intent) {
        this.nfcManager.onActivityNewIntent(intent);
        super.onNewIntent(intent);
    }

    private boolean
    isReadingMode() {
        return (this.dataToWrite == null);
    }

    public static Intent createIntentForWriting(Context context, String data, String customData) {
        Intent intent = new Intent(context, NFCTagActivity.class);
        intent.putExtra(NFCTagActivity.DATA_TO_WRITE_KEY, data);
        intent.putExtra(NFCTagActivity.ACTION_DATA_CUSTOM, customData);
        return intent;
    }

    public static Intent creatIntentForReading(Context context, String customData) {
        Intent intent = new Intent(context, NFCTagActivity.class);
        intent.putExtra(NFCTagActivity.ACTION_DATA_CUSTOM, customData);
        return intent;
    }

    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            backLogin();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.log_out) {
            // Handle the camera action
            confirmDeconnexionDialogue();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void backLogin() {
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NFCTagActivity.this);

        // Set a title for alert dialog
        builder.setTitle("Alerte");
        builder.setIcon(R.drawable.ic_cross);
        // Ask the final question
        builder.setMessage("Voulez-vous vraiment retourner en arrière ?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when user clicked the Yes button
                // Set the TextView visibility GONE
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }
}
