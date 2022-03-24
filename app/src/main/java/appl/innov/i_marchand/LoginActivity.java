package appl.innov.i_marchand;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import appl.innov.i_marchand.helper.DatabaseHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

public class LoginActivity extends AppCompatActivity {

    // Declaration des variables
    private final BroadcastReceiver MyReceiver = null;
    DatabaseHelper myDb;
    String login;
    String password;
    String token;
    // Instanciation de la fonction TrustManager
    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {

                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {

                }
            }
    };
    // Fin de l'instanciation de la fonction TrustManager

    private SQLiteDatabase sql;
    // sur le front
    private EditText loginEdit;

    // private Button bouton_connect;
    private EditText passwordText;
    // La fonction Principale on Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisé les variables décalrés
        myDb = new DatabaseHelper(this);
        loginEdit = findViewById(R.id.emailOubTxt);
        passwordText = findViewById(R.id.passworOubTxt);

        Button btn_con = findViewById(R.id.loginBtn);


        // Filtre du login et du Mot de passe : Le bon Filtre
        btn_con.setOnClickListener(v -> {
            // Mettre les traitements ici
            if (loginEdit.getText().length() != 0 && !loginEdit.getText().toString().equals("")) {
               if (passwordText.getText().length() != 0 && !passwordText.getText().toString().equals("")) {
                   broadcastIntent();
                   AsyncCallWS task = new AsyncCallWS();
                   task.execute();
               }
               // Si le Mot de passe est vide
                else {
                    passwordText.setError("Entrer le mot de passe");
               }
            } else {
                passwordText.setError("Entrer le Login");
            }
        });
        // fin de la connexion

        /*btn_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                *//*Intent intent = new Intent(LoginActivity.this,
                        Test_NFC_Tag_Activity.class);

                startActivity(intent);*//*
                // close this activity
                // finish(); // terminer l'activité ou non

            }
        });*/
    }




    // creer la fonction boradcastIntent
    private void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }
    // fin de la fonction braodcastIntent

    // creation de la fonction onPause
    @Override
    protected void onPause() {
        super.onPause();
        // Do Something
    }
    // fin de la fonction onPause

    // creer la fonction AsyncCallWSDeconnexion pour appeler le WS
    class AsyncCallWSDeconnexion extends AsyncTask<String, Void, String> {
        // creons le ProgressDialog
        private final ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);
        // la fonction doInBAckground
        @Override
        protected String doInBackground(String... url) {
            // la fonction deconnexionUser doit etre appelé ici
            deconnexionUser();
            return null;
        }
        // La fonction onPreExecute
        @Override
        protected void onPreExecute() {
            // super.onPreExecute();
            Dialog.setMessage("ChargementLoading....");
            Dialog.show();
        }
        //La fonction onPostExecute
        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(s);
            Dialog.dismiss();
        }
    }
    // fin de la classe AsyncCallWSDeconnexion

    // créaton de la classe AsyncCallWS
    class AsyncCallWS extends AsyncTask<String, Void, String> {
        // Mettre le ProgressDialog
        //private final ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

        // Tester le SweetAlert
        //private final SweetAlertDialog pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        SweetAlertDialog pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);

        //La fonction doInBackground
        @Override
        protected String doInBackground(String... url) {
            //La fonction loginAction doit etre appelé
            loginAction();
            return null;
        }

        //La fonction onPreExecute
        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            //Dialog.setMessage("ChargmentLoadingLoginAction...");
            //Dialog.show();
            // Mettre les traitements du SweetAlertDialog
            /*pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Chargement");
            pDialog.setCancelable(false);
            pDialog.show();*/
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF3D00"));
            pDialog.setTitleText("Chargement...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        //La fonction onPostExecute

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(s);
            //Dialog.dismiss();
            //pDialog.dismiss();
            pDialog.dismiss();
        }
    }
    // Fin de la fonction AsyncCallWS

    // Création de la fonction loginAction
    private void loginAction() {
        // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
       // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
        String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
        String NAMESPACE = "http://runtime.services.cash.innov.sn/";
        String SOAP_ACTION = "";
        String METHOD_NAME = "login";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        login = loginEdit.getText().toString();
        password = passwordText.getText().toString();

        // Faire passer les valeurs du username du web service
        PropertyInfo unameProp = new PropertyInfo();

        // récupération
        unameProp.setName("login"); // Définition du nom de la variables tel qu'il est dans le webservice
        unameProp.setValue(login); // Modification de la valeur du variable username
        unameProp.setType(String.class); // Défini le type de la varibale
        request.addProperty(unameProp); // Faire passer les proproétés de la varaible dans la requete

        // faire passer les valeurs du mot de passe dans le webservices
        PropertyInfo passwordProp = new PropertyInfo();
        passwordProp.setName("password");
        passwordProp.setValue(password);
        passwordProp.setType(String.class);
        request.addProperty(passwordProp);

        // faire passer les variables du mot de passe du webservices
        String mod = "APP";
        PropertyInfo mode = new PropertyInfo();
        mode.setName("mode");
        mode.setValue(mod);
        mode.setType(String.class);
        request.addProperty(mode);

        // Try Catché la requete

        try {

            SSLContext sc = SSLContext.getInstance(SSL); // requeter par le protocole SSL
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = false;

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            Log.d("login", login);
            Log.d("password", password);
            Log.d("request", request.toString());

            androidHttpTransport.call(SOAP_ACTION, envelope);
            SoapObject result = (SoapObject) envelope.getResponse();

            System.out.println("Call Donnnnnnnnnnne");
            String erreur = result.getProperty(0).toString();
            String iduser = result.getProperty(1).toString();

            Log.e("result", "responseeeeeeeeeeeeeeeee" + result);
            Log.e("XXenvelope", "envelope" + envelope.toString());
            Log.e("token", "tokennn" + token);

            // Indexer la BDD
            Cursor res = myDb.getAllData();

            if(erreur.equals("0")) {

                String message1 = result.getProperty(2).toString();
                String nom = result.getProperty(3).toString();
                String prenom = result.getProperty(4).toString();
                String profil = result.getProperty(5).toString();
                String telephone = result.getProperty(6).toString();

                token = result.getProperty(7).toString();
                Log.e("token", "tokennnn" + token);
                Log.e("result", "Message1" + message1);

                boolean isInserted = myDb.insertData(
                        token, null, null, telephone,
                        iduser, null, null,
                        null, null
                );

                if (isInserted) {
                    Log.e("result", "Donnéeeees Inséréééééééééée" + token);
                    Log.e("result", "iduser form login fucntion" + iduser);

                    // Run In NFC TAG ACTIVITY TEST
                    runOnUiThread(()-> {
                        Intent i = new Intent(getApplicationContext(), Test_NFC_Tag_Activity.class);
                        startActivity(i);
                        // on doit tuer cette activité par la fonction finish
                    });
                }
            } else if (erreur.equals("13")) {
                // Doit appeler la fonction OnUserSession
                runOnUiThread(this::OnUserSession);
            } else if (erreur.equals("10")) {
                // Doit appeler la fonction changePasswordDialog
                runOnUiThread(this::changePasswordDialog);
                // Doit appeler la fonction changeOTPDialog
                /* Ceci est est un test pour se passer du mot de passe */
                /*Intent i = new Intent(LoginActivity.this, Test_NFC_Tag_Activity.class);
                startActivity(i);*/
                /* Fin du Test pour se passer du mot de passe expirer */
            } else if (erreur.equals("11")) {
                // Doit appeler la fonction changeOTPDialog
                runOnUiThread(this::changeOTPDialog);
            } else {
                // Doit appeler la fonction ErrorLogin
                runOnUiThread(this::ErrorLogin);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // Fin de la fonction LoginAction

    // La fonction deconnexionSucces
    public void deconnexionSucces() {

    }
    // fin de la fonction deconnexionSuccess

    // Création de la fonction deconnexionUser
    public void deconnexionUser(){

        Cursor res = myDb.getAllData();

        if (res.moveToLast()) {

            // http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl // server de test
            // String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de prod
            String URL = "http://50.116.97.25:8080/cash-ws/CashWalletServiceWS?wsdl"; // serveur de test
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";
            String SOAP_ACTION = "";
            String METHOD_NAME = "deconnexionUser";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_ ;
            // Manipuler le idSession
            PropertyInfo session = new PropertyInfo();
            session.setName("idSession");
            idSession_ = res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            Log.e("result", "idddddddddd sesssssion avant appel de deconnexinser" + idSession_);

            // Try Catché la requete
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                String error = result.getProperty(1).toString();

                Log.e("result", "responseeeeeee from ws deconnexionUserrrrr" + result);

                if (error.equals("1")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deconnexionSucces();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Fin de la fonction deconnexionUser

    // Creation de la fonction youAreNotCostumer : vous n test pas un client
    public void youAreNotCostumer() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Vous n'avez pas de compte marchand !");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ok",
                new DialogInterface.OnClickListener() {
                   // @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    // Fin de la fonction youAreNotCostumer

    // Creation de le fonction OnUserSession pour gérer les sessions des utilisateurs
    public void OnUserSession() {

        // Creation de ALerte Dialog pour confirmation
        SweetAlertDialog confirmDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.WARNING_TYPE);
        //confirmDialog.setTitleText("Are you sure? ");
        confirmDialog.setContentText("Vous ètes déjàs connecté sur i-pay \n Voulez-vous poursuivre la connexion ?");
        confirmDialog.setConfirmText("Oui");
        // creation du bouton oui
        confirmDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog oui) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getApplicationContext(), Test_NFC_Tag_Activity.class);
                        startActivity(i);
                    }
                });

            }
        });

        confirmDialog.show();
        // fin de la cration du bouton oui*
        // creation du bouton non
        confirmDialog.setCancelButton("Non", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog nonDialog) {

                //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                //startActivity(intent);
                nonDialog.dismiss();
                finish(); // Tuer cette activité permmettrant de quitter la page de connexion.

               // nonDialog.dismissWithAnimation();

            }
        });


        // fin de la cretaion du bouton non
        // fin de la creation

        // créer un AlertDialog
        //AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        // Modifier le titre de l'alerte Dialog
       // builder.setTitle("Alerte");
        // Demande la question final
      //  builder.setMessage("Vous etes déjà connecté sur i-pay, Voulez-vous forcer la connexion ?");
        // Modifier les buttons de confirmation de l'alertDialog
        /*builder.setPositiveButton("oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getApplicationContext(), Test_NFC_Tag_Activity.class);
                        startActivity(i);
                    }
                });

                dialog.dismiss();
            }
        });*/

        // Modifier le button dialog alert par la reponse no
        /*builder.setNegativeButton("non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });*/

      //  AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
     //   dialog.show();
    }
    // fin de la fonction OnUserSession

    // Création de la fonction ErrorLogin
    public void ErrorLogin() {

        // Mettre le Sweet Alert Dialog Error Message
        SweetAlertDialog sDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE);
        //sDialog.setTitleText("");
        sDialog.setContentText("Téléphone ou Mot de passe Incorrect !");
        sDialog.setCancelable(false);
        sDialog.show();

/*AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Identifiants Incorrects");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ok",
                new DialogInterface.OnClickListener() {
            //@Override
            public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
            }
        });
        alertDialog.show();*/

    }
    // Fin de la fonction ErrorLogin

    // Creation de la fonction outOfSession
    public void outOfSession() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_alert);
        alertDialog.setMessage("Votre session a expiré ! Veuillez vous reconnecter !");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ok",
                new DialogInterface.OnClickListener() {
                    //@Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    // fin de la fonction outOfSession

    // Creation de la fonction changePasswordDialog
    public void changePasswordDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_alert);
        alertDialog.setMessage("Veuillez changer votre mot de passe !");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    //@Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    // fin de la fonction changePasswordDialog

    // Creation de la fonction changeOTPDialog
    public void changeOTPDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_alert);
        alertDialog.setMessage("Merci d'aller sur i-pay.africa pour veérifier votre mail et/ou votre OTP. ");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    // fin de la fonction changeOTPDialog


}
