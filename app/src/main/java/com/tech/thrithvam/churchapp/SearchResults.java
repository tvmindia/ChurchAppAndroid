package com.tech.thrithvam.churchapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchResults extends AppCompatActivity {
    AsyncTask searching=null,getTowns=null;
    String searchKey;
    Bundle extras;
    AutoCompleteTextView searchText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        extras=getIntent().getExtras();
        searchKey=extras.getString("searchkey");
        if (isOnline()) {
            searching=new ChurchTownSearchResults().execute();
        } else {
            Toast.makeText(SearchResults.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();
            Intent noItemsIntent=new Intent(SearchResults.this,NothingToDisplay.class);
            noItemsIntent.putExtra("msg",getResources().getString(R.string.network_off_alert));
            noItemsIntent.putExtra("activityHead","Searching");
            startActivity(noItemsIntent);
            finish();
        }
        //Search Text box--------------
        Typeface typeCopperplateGothic = Typeface.createFromAsset(getAssets(),"fonts/copperplate-gothic.ttf");
        searchText=(AutoCompleteTextView) findViewById(R.id.searchViewText);
        searchText.setTypeface(typeCopperplateGothic);
        searchText.setText(searchKey);
        ImageView searchImage =(ImageView)findViewById(R.id.searchImage);
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText(searchText.getText().toString().trim());
                if(!searchText.getText().toString().equals("")) {
                Intent intent=new Intent(SearchResults.this,SearchResults.class);
                intent.putExtra("searchkey",searchText.getText().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                }
            }
        });
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchText.setText(searchText.getText().toString().trim());
                    if(!searchText.getText().toString().equals("")) {
                        Intent intent=new Intent(SearchResults.this,SearchResults.class);
                        intent.putExtra("searchkey",searchText.getText().toString());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                }
                return false;
            }
        });
    }
    //-----------------------------Async Tasks----------------------------------------
    public class ChurchTownSearchResults extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView loadingIndicator =(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
        TextView loadingText=(TextView)findViewById(R.id.loading_text);
        TextView requestChurch=(TextView)findViewById(R.id.request_church);
        ArrayList<String[]> churchItems=new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setOnLongClickListener(null);
            requestChurch.setVisibility(View.INVISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/SearchChurch";
            HttpURLConnection c = null;
            try {
                postData =  "{\"SearchTerm\":\"" + searchKey+ "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a, b + 1);
                        //   strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    String[] data=new String[5];
                    data[0]=jsonObject.optString("ID");
                    data[1]=jsonObject.optString("ChurchName");
                    data[2]=jsonObject.optString("Town");
                    data[3]=jsonObject.optString("ImageURL");
                    data[4]=jsonObject.optString("Address");
                    churchItems.add(data);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            loadingIndicator.setVisibility(View.GONE);
            if(!pass) {
                                loadingText.setText(R.string.no_items_to_display);
                                loadingText.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {
                                        loadingText.setText(msg);
                                        return true;
                                    }
                                });
                                requestChurch.setVisibility(View.VISIBLE);
            }
            else {
                loadingText.setVisibility(View.GONE);
                CustomAdapter adapter=new CustomAdapter(SearchResults.this, churchItems,"ChurchTownSearchResults");
                ListView churchList=(ListView) findViewById(R.id.resultsGrid);
                churchList.setAdapter(adapter);
                churchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(SearchResults.this,ChurchDetails.class);
                        intent.putExtra("churchID",churchItems.get(position)[0]);
                        intent.putExtra("churchname",churchItems.get(position)[1]);
                        intent.putExtra("town",churchItems.get(position)[2]);
                        intent.putExtra("address",churchItems.get(position)[4]);
                        startActivity(intent);
                        finish();
                    }
                });
                searchText.setSelectAllOnFocus(true);
            }
            getTowns=new GetAllTowns().execute();
        }
    }
    public class GetAllTowns extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String> townListItems =new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/AllTown";
            HttpURLConnection c = null;
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length","0");
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.connect();//Since no post data
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a, b + 1);
                        //   strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    townListItems.add(jsonObject.optString("Name"));
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
            }
            else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>
                        (SearchResults.this,android.R.layout.simple_list_item_activated_1,townListItems);
                searchText.setAdapter(adapter);
                searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent(SearchResults.this,SearchResults.class);
                        intent.putExtra("searchkey",searchText.getText().toString());
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void request_church(View view)
    {
        Intent intent=new Intent(SearchResults.this,RequestChurch.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(searching!=null)searching.cancel(true);
        if(getTowns!=null)getTowns.cancel(true);
    }
}
