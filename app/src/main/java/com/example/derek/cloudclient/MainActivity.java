package com.example.derek.cloudclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.List;

import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

public class MainActivity extends Activity {

    // Create an object to connect to your mobile app service
    private MobileServiceClient mClient;

    // Create an object for  a table on your mobile app service
    private MobileServiceTable<ToDoItem> ToDoTable;

    // global variable to update a TextView control text
    TextView display;

    // simple stringbulder to store textual data retrieved from mobile app service table
    StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       try {

           // using the MobileServiceClient global object, create a reference to YOUR service
           mClient = new MobileServiceClient(
                   "https://zaldosws2.azurewebsites.net",
                   this
           );

           authenticate();

           // using the MobileServiceTable object created earlier, create a reference to YOUR table
           ToDoTable = mClient.getTable(ToDoItem.class);

           display = (TextView) findViewById(R.id.displayData);


       } catch (MalformedURLException e) {
            e.printStackTrace();
       }
    }

    private void authenticate(){

        // Login using twitter id provider
        ListenableFuture<MobileServiceUser> mLogin = mClient.login(MobileServiceAuthenticationProvider.Twitter);

        Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
            @Override
            public void onSuccess(MobileServiceUser user) {
                //createAndShowDialog(String.format("You are now logged in - %1$2s", user.getUserId()), "Success");

                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                String uid = prefs.getString("uid", null);

                viewData(findViewById(R.id.clickViewData));
            }


            @Override
            public void onFailure(Throwable t) {
                createAndShowDialog(t.getMessage(), "Error");
            }
        });
    }

    private void createAndShowDialog(String message){createAndShowDialog(message,"");}

    private void createAndShowDialog(String message, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }
    // method to add data to mobile service table
    public void addData(View view) {

        // create reference to TextView input widgets
        TextView data1 = (TextView) findViewById(R.id.insertText1);
        // the below textview widget isn't used (yet!)
        TextView data2 = (TextView) findViewById(R.id.insertText2);

        // Create a new data item from the text input
        final ToDoItem item = new ToDoItem();
        item.text = data1.getText().toString();
        //item. = data2.getText().toString();

        // This is an async task to call the mobile service and insert the data
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //
                    final ToDoItem entity = ToDoTable.insert(item).get();  //addItemInTable(item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        // code inserted here can update UI elements, if required

                        }
                    });
                } catch (Exception exception) {
                    createAndShowDialog("Error adding data");
                }
                return null;
            }
        }.execute();
    }

    // method to view data from mobile service table
    public void viewData(View view) {

        display.setText("Loading...");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<ToDoItem> result = ToDoTable.select("id", "text").execute().get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // get all data from column 'text' only add add to the stringbuilder
                            for (ToDoItem item : result) {
                                sb.append(item.text + "\n");
                            }

                            // display stringbuilder text using scrolling method
                            display.setText(sb.toString());
                            display.setMovementMethod(new ScrollingMovementMethod());
                            sb.setLength(0);
                        }
                    });
                } catch (Exception exception) {
                    createAndShowDialog("Error viewing data");
                }
                return null;
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // class used to work with ToDoItem table in mobile service, this needs to be edited if you wish to use with another table
    public class ToDoItem {
        private String id;
        private String text;
    }




}
