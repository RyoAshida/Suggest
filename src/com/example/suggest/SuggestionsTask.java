package com.example.suggest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

public class SuggestionsTask extends AsyncTask<String, Void, List<String>> {
    private final static String TAG = "SuggestionsTask";

    private final static String SUGGEST_URL =
            "http://google.com/complete/search?hl=ja&ie=utf_8&oe=utf_8&output=toolbar&q=";
    private final static String SUGGESTION_TAG = "suggestion";
    private final static String SUGGESTION_ATTR = "data";

    private Context context;

    public SuggestionsTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<String> doInBackground(String... params) {
        List<String> result = new ArrayList<String>();
        HttpURLConnection conn = null;
        String error = null;
        try {
            String query = URLEncoder.encode(params[0], "UTF-8");
            URL url = new URL(SUGGEST_URL + query);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(conn.getInputStream(), "UTF-8");
            for (int et = xpp.getEventType(); et != XmlPullParser.END_DOCUMENT; 
                     et = xpp.next()) {
                if (et == XmlPullParser.START_TAG
                        && xpp.getName().equalsIgnoreCase(SUGGESTION_TAG)) {
                    for (int i = 0; i < xpp.getAttributeCount(); i++)
                        if (xpp.getAttributeName(i).equalsIgnoreCase(SUGGESTION_ATTR))
                            result.add(xpp.getAttributeValue(i));
                }
            }
        }
        catch (Exception e) {
            error = context.getString(R.string.error_message) + ": " + e.toString();
            Log.e(TAG, error);
        }
        finally {
            if (conn != null)
                conn.disconnect();
        }
        if (error != null) {
            result.clear();
            result.add(error);
        }
        if (result.size() == 0)
            result.add(context.getString(R.string.no_result_message));

        return result;
    }
}
