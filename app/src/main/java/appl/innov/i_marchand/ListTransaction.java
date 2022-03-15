package appl.innov.i_marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.ArrayList;

import appl.innov.i_marchand.helper.DatabaseHelper;

public class ListTransaction extends AppCompatActivity {
    DatabaseHelper myDb; // DataBase
    ListView listView;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter; // this is a comment this is a comment this is a comment this is a comment ok
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transaction);
        myDb = new DatabaseHelper(this);
        listView = findViewById(R.id.listTransaction);

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(ListTransaction.this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        AsyncCallWSListTransaction task = new AsyncCallWSListTransaction();
        task.execute();

    }
    @SuppressLint("StaticFieldLeak")
    class AsyncCallWSListTransaction extends AsyncTask<String, Void, String> {
        private final ProgressDialog Dialog = new ProgressDialog(ListTransaction.this);
        @Override
        protected String doInBackground(String... url) {
            getListTransaction();
            return null;
        }
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("chargement...");
            Dialog.show();
        }
        @Override
        // this is a comment this is a comment this is a comment ok
        protected void onPostExecute(String result) {
            Dialog.dismiss();
        }
    }
    public void getListTransaction(){
        Cursor res = myDb.getAllData();
        if(res.moveToLast()){

            String URL = "http://ibusinesscompanies.com:18080/cash-ws/CashWalletServiceWS?wsdl";
            String NAMESPACE = "http://runtime.services.cash.innov.sn/";
            String SOAP_ACTION = "";
            String METHOD_NAME = "listOperationHistoriqueTransaction";
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String telephone;
            PropertyInfo tel = new PropertyInfo();
            tel.setName("telephone");
            telephone = res.getString(4);
            tel.setValue(telephone);
            tel.setType(String.class);
            request.addProperty(tel);

            String idSession_;
            //set the idSession retrieved
            PropertyInfo session =new PropertyInfo();
            session.setName("IdSession");
            idSession_ =  res.getString(1);
            session.setValue(idSession_);
            session.setType(String.class);
            request.addProperty(session);

            Log.e("result", "id session avant entrée dans le try" + idSession_);

            Log.e("result", "id session avant entrée dans le try" + idSession_);

            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject result = (SoapObject) envelope.getResponse();






                String error = result.getProperty(0).toString();
                String message = result.getProperty(1).toString();


                if (error.equals("0")){
                    Log.e("getlistTransaction", "getlistTransaction Called with success");
                    Log.e("result", "responceEEEEEEEEEEEEEEEEEEEEEEEE" + result);
                    runOnUiThread(() -> {

                    });
                }else {
                    Log.e("ERROR", "getlistTransaction NOT CALLED WITH SUCCESS");
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }



    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), NFCTagActivity.class);
        startActivity(i);
    }
}
