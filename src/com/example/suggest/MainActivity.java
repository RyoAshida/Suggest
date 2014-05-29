package com.example.suggest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";

    private EditText inputText;
    private Button suggestButton;
    private ListView resultList;
    private ArrayAdapter<String> resultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        inputText = (EditText) findViewById(R.id.input_text);
        suggestButton = (Button) findViewById(R.id.suggest_button);
        resultList = (ListView) findViewById(R.id.result_list);

        suggestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuery(inputText.getText().toString().trim());
            }
        });

        resultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                doSearch((String) parent.getItemAtPosition(position));
            }

        });

        resultAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, new ArrayList<String>());
        resultList.setAdapter(resultAdapter);
    }

    private void startQuery(String text) {
        resultAdapter.clear();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SuggestionsTask(this) {
                @Override
                public void onPostExecute(List<String> result) {
                    resultAdapter.clear();
                    resultAdapter.addAll(result);
                }
            }.execute(text);
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.no_network_alert_title))
                    .setNeutralButton(getString(R.string.no_network_alert_ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {}
                            }).show();
        }
    }

    private void doSearch(String text) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, text);
        startActivity(intent);
    }
}
