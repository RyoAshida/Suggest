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
import android.text.Editable;
import android.text.TextWatcher;
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
    private Button clearButton;
    private ListView resultList;
    private ArrayAdapter<String> resultAdapter;

    private SuggestionsTask suggestionsTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        inputText = (EditText) findViewById(R.id.input_text);
        clearButton = (Button) findViewById(R.id.clear_button);
        resultList = (ListView) findViewById(R.id.result_list);

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                updateQuery(inputText.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputText.setText("");
                cancelQuery();
                resultAdapter.clear();
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

    private void cancelQuery() {
        if (suggestionsTask != null
                && suggestionsTask.getStatus() != SuggestionsTask.Status.FINISHED
                && !suggestionsTask.isCancelled())
            suggestionsTask.cancel(true);
    }

    private void updateQuery(String text) {
        cancelQuery();
        resultAdapter.clear();
        if (text.length() == 0)
            return;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            suggestionsTask = new SuggestionsTask(this) {
                @Override
                public void onPostExecute(List<String> result) {
                    resultAdapter.addAll(result);
                }
            };
            suggestionsTask.execute(text);
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
