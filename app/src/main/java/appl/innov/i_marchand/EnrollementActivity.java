package appl.innov.i_marchand;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


import appl.innov.i_marchand.SplashScreen.SuccessSplash;
import appl.innov.i_marchand.helper.DatabaseHelper;
import at.markushi.ui.CircleButton;

public class EnrollementActivity  extends AppCompatActivity  {
    DatabaseHelper myDb;

    EditText nom;
    EditText prenom;
    EditText NumeroTel;
    EditText montantApayer;
    Button btn_save,searchfromcard;
    String tel, prenom_, nom_, montant__;
    FloatingActionButton search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollement);
        myDb = new DatabaseHelper(this);
        NumeroTel = findViewById(R.id.numTelTxtinscrip);
        prenom = findViewById(R.id.prenomEtudiantTxtinscrip);
        nom = findViewById(R.id.nonEtudiantTxtinscrip);
        System.out.println("foncion checkNumberForDirectSearch appelée");
        montantApayer = findViewById(R.id.montantApayer);
        btn_save = findViewById(R.id.enroler_id);
        search = findViewById(R.id.searchBnt);


        search.setOnClickListener(v -> {
            if(NumeroTel.getText().length() != 0 && !NumeroTel.getText().toString().equals("") ){
                AsyncCallWS2 task = new AsyncCallWS2();
                task.execute();
            }else {
                openDialogueSearch();
            }
        });


        btn_save.setOnClickListener(v -> {
            if (NumeroTel.getText().length()!=9){
                openDialoguValidNumber();
            }else {
            if (NumeroTel.getText().length() != 0 && !NumeroTel.getText().toString().equals("") ) {
                if(prenom.getText().length() != 0 && !prenom.getText().toString().equals("")) {
                    if (nom.getText().length() != 0 && !nom.getText().toString().equals("") ){
                        if (montantApayer.getText().length() != 0 && !montantApayer.getText().toString().equals("")){
                            AsyncCallWS task = new AsyncCallWS();
                            task.execute();
                        }
                        else{
                            montantApayer.setError("Entrez le montant à déposer");
                        }
                    }
                    else {
                        montantApayer.setError("Entrez le nom");
                    }
                }  //If Password text control is empty
                else{
                    montantApayer.setError("Entrez le prénom");
                }
            }else {

                montantApayer.setError("Entrez le numéro de téléphone");
            }

            if (NumeroTel.getText().length()!=9){
                openDialoguValidNumber();
            }

        }
        });

        Cursor res = myDb.getAllData();
        if(res.moveToLast()){
            String telephonefromCard = res.getString(6);

            if (telephonefromCard!=null){
                AsyncCallWSDirectSearch2 task = new AsyncCallWSDirectSearch2();
                task.execute();
           }
        }

    }
    @SuppressLint("StaticFieldLeak")
    class AsyncCallWSDirectSearch2 extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(EnrollementActivity.this);
        @Override
        protected String doInBackground(String... url) {
            getUoByCard2();
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

    public void getUoByCard2(){
        Cursor res = myDb.getAllData();
        if(res.moveToLast()){
            String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl";
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";
            String SOAP_ACTION = "";
            String METHOD_NAME = "getUoByCard";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session =new PropertyInfo();
            session.setName("sessionId");
            idSession_ =  res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);
            Log.e("result", "id session avant entrée dans le 22" + idSession_);

            String idCard;
            //Pass value for idCard variable of the web service
            PropertyInfo card_id = new PropertyInfo();
            card_id.setName("idcard");
            idCard =  res.getString(3);
            card_id.setValue(idCard);
            card_id.setType(String.class);
            request.addProperty(card_id);

            String commision =  res.getString(2);
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
                if (error.equals("0")){

                    Log.e("result", "Commission insérée" + error);
                    String idcarte = result.getProperty(1).toString();
                    String message = result.getProperty(2).toString();
                    String nomFromCard = result.getProperty(3).toString();
                    String prenomFromCard = result.getProperty(4).toString();
                    String telephonefromCard = result.getProperty(5).toString();
                    Log.e("tellll", "telephone lié à la carte" + telephonefromCard);
                    Log.e("PRE", "prenom lié à la carte" + prenomFromCard);
                    Log.e("NOM", "nom lié à la carte" + nomFromCard);
                    String amount = res.getString(9);

                    boolean isInserted = myDb.insertData(
                            idSession_,commision,idCard,telephone,iduser,telephonefromCard,prenomFromCard,nomFromCard,amount
                    );

                    if (isInserted){
                        Log.e("telephone", "telephone lié à la carte inséré" + telephonefromCard);
                        Log.e("prenom", "prenom lié à la carte inséré" + prenomFromCard);
                        Log.e("nom", "nom lié à la carte inséré" + nomFromCard);
                        if (!telephonefromCard.equals("null")){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    NumeroTel.setText(telephonefromCard);
                                    prenom.setText(prenomFromCard);
                                    nom.setText(nomFromCard);
                                }
                            });
                        }else {
                            //TODO
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    class AsyncCallWS extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(EnrollementActivity.this);

        @Override
        protected String doInBackground(String... url) {
             addAndPay();
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
    class AsyncCallWS2 extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(EnrollementActivity.this);

        @Override
        protected String doInBackground(String... url) {
            getUoByCellular();
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

    public void checkNumberForDirectSearch(){
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
            Log.e("tellll", "telephone from checkNumberForDirectSearchvvvvvvvvvvvvvvVVVVVVV" + telephonefromCard);

            if (telephonefromCard!=null){
                NumeroTel.setText(telephonefromCard);
                prenom.setText(prenomFromCard);
                nom.setText(nomFromCard);
            }

         /*          boolean isInserted = myDb.insertData(
                    idSession_,commision,idCard,telephone,iduser,telephonefromCard,prenomFromCard,nomFromCard
            );*/
        }

    }

    @SuppressLint("StaticFieldLeak")
    class AsyncCallWSDirectSearch extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(EnrollementActivity.this);
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
    public void openDialoguValidNumber(){
        AlertDialog alertDialog = new AlertDialog.Builder(EnrollementActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Entrez un numéro de 9 chiffres");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }
    public void openDialogueSearch(){
        AlertDialog alertDialog = new AlertDialog.Builder(EnrollementActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Entrez le numéro d'abord");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }
    public void outOfSession(){
        AlertDialog alertDialog = new AlertDialog.Builder(EnrollementActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_alert);
        alertDialog.setMessage("Votre session a expiré ! Veuillez vous reconnecter !");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
    public void EnrolementSuccess(){
        AlertDialog alertDialog = new AlertDialog.Builder(EnrollementActivity.this).create();
        alertDialog.setTitle("Succès");
        alertDialog.setIcon(R.drawable.ic_tick);
        alertDialog.setMessage("Enrolement/Dépôt réussi");
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

    /**
     * si la carte uo n'est pas enregistrée on appelle le ws suivant
     * pour l'enregistrement et la paiement marchand en un coup
     */

    public void getUoByCard(){
        Cursor res = myDb.getAllData();
        if(res.moveToLast()){
            String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl";
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";
            String SOAP_ACTION = "";
            String METHOD_NAME = "getUoByCard";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session =new PropertyInfo();
            session.setName("sessionId");
            idSession_ =  res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            String idCard;
            //Pass value for idCard variable of the web service
            PropertyInfo card_id = new PropertyInfo();
            card_id.setName("idcard");
            idCard =  res.getString(3);
            card_id.setValue(idCard);
            card_id.setType(String.class);
            request.addProperty(card_id);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                String error = result.getProperty(0).toString();
                if (error.equals("0")){
                    Log.e("result", "Commission insérée" + error);
                    String idcarte = result.getProperty(1).toString();
                    String message = result.getProperty(2).toString();
                    String nom = result.getProperty(3).toString();
                    String prenom = result.getProperty(4).toString();
                    String telephone = result.getProperty(5).toString();
                    Log.e("tellll", "telephone" + telephone);

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
    void addAndPay(){
        String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl";
        String NAMESPACE = "http://runtime.services.cash.innov.sn/";
        String SOAP_ACTION = "";
        String METHOD_NAME = "addUocardAndPay";
        Cursor res = myDb.getAllData();
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        if(res.moveToLast()){
            nom_ = nom.getText().toString();
            //Pass value for lastname variable of the web service
            PropertyInfo lastName =new PropertyInfo();
            lastName.setName("nom");
            lastName.setValue(nom_);
            lastName.setType(String.class);
            request.addProperty(lastName);

            prenom_ = prenom.getText().toString();
            //Pass value for firstname variable of the web service
            PropertyInfo firstname =new PropertyInfo();
            firstname.setName("prenom");
            firstname.setValue(prenom_);
            firstname.setType(String.class);
            request.addProperty(firstname);

            tel = NumeroTel.getText().toString();
            //Pass value for phone variable of the web service
            PropertyInfo phone =new PropertyInfo();
            phone.setName("telephone");//Define the variable name in the web service method
            phone.setValue(tel);//set value for userName variable
            phone.setType(String.class);//Define the type of the variable
            request.addProperty(phone);//Pass properties to the variable

            String idCard;
            //Pass value for idCard variable of the web service
            PropertyInfo card_id =new PropertyInfo();
            card_id.setName("idcarte");
            idCard =  res.getString(3);
            card_id.setValue(idCard);
            card_id.setType(String.class);
            request.addProperty(card_id);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session =new PropertyInfo();
            session.setName("sessionId");
            idSession_ =  res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            montant__ = montantApayer.getText().toString();
            //Pass value for montantàdéposer variable of the web service
            PropertyInfo PreMontantApayer =new PropertyInfo();
            PreMontantApayer.setName("montant");
            PreMontantApayer.setValue(montant__);
            PreMontantApayer.setType(String.class);
            request.addProperty(PreMontantApayer);

            String codem= res.getString(4);
            //Pass value for code variable of the web service
            PropertyInfo code_ =new PropertyInfo();
            code_.setName("code");
            code_.setValue(codem);
            code_.setType(String.class);
            request.addProperty(code_);

            try{
                Log.e("result", "idSession on addUoCardAndPay" + idSession_);
                Log.e("result", "id carteeee on addUoCardAndPay" + idCard);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();

                String message = result.getProperty(0).toString();
                Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE in addddddUoCardPayyyyyyyyyyyyyyyyyyy" + result);

                if (message.equals("0")){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), SuccessSplash.class);
                            startActivity(intent);
                        }
                    });
                }
                else if (message.equals("-1")){
                    {
                        runOnUiThread(() -> {
                            Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
                            startActivity(intent);
                        });
                    }
                }else{
                    //TO DO
                }
                System.out.println("call Donnnnnnnnnnnnnnnnnnnnnnnnnnne");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void getUoByCellular(){
        Cursor res = myDb.getAllData();
        if(res.moveToLast()){
            String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl";
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";
            String SOAP_ACTION = "";
            String METHOD_NAME = "getUOByCellular";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session =new PropertyInfo();
            session.setName("idSession");
            idSession_ =  res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            tel = NumeroTel.getText().toString();
            //Pass value for phone variable of the web service
            PropertyInfo phone =new PropertyInfo();
            phone.setName("cellular");//Define the variable name in the web service method
            phone.setValue(tel);//set value for userName variable
            phone.setType(String.class);//Define the type of the variable
            request.addProperty(phone);//Pass properties to the variable

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                System.out.println("call Donnnnnnnnnnnnnnnnnnnnnnnnnnne");
                String erreur = result.getProperty(0).toString();
                String deux = result.getProperty(1).toString();

                if (deux.equals("found")){
                    String trois = result.getProperty(2).toString();
                    String [] stringArray = trois.split(";");
                    String prenomfromWs = stringArray[4];
                    String nomfromWs = stringArray[2];
                    String[] nomArray = nomfromWs.split("=");
                    String[] prenomArray = prenomfromWs.split("=");
                    String nomBrute = nomArray[1];
                    String prenomBrute = prenomArray[1];
                    runOnUiThread(new Runnable() {
                        public void run() {
                            prenom.setText(prenomBrute);
                        }
                    });
                    runOnUiThread(() -> nom.setText(nomBrute));
                    Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE" + result);
                    Log.e("result", "errreur" + erreur);
                    Log.e("result", "deux" + deux);
                    Log.e("result", "trois " + trois);
                    Log.e("nom", "nom " + nomfromWs);

                    Log.e("prenom", "prenom " + prenomfromWs);
                }
                else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            UserNotFound();
                        }
                    });
                }

                Log.d("LOG_TAG", envelope.bodyIn.toString());
                Log.d("LOG_TAG", envelope.bodyIn.toString());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void UserNotFound(){
        AlertDialog alertDialog = new AlertDialog.Builder(EnrollementActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("Ce numéro n'existe pas");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
    public void soldeInsuffisant(){
        AlertDialog alertDialog = new AlertDialog.Builder(EnrollementActivity.this).create();
        alertDialog.setTitle("Alerte");
        alertDialog.setIcon(R.drawable.ic_cross);
        alertDialog.setMessage("votre solde est insuffisant");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
    public void backNFC(){
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(EnrollementActivity.this);

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

                Intent intent = new Intent(getApplicationContext(), NFCTagActivity.class);
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
    @Override
    public void onBackPressed() {
        backNFC();
    }
}