package com.example.langlock;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TranslateBackgroundTask extends AsyncTask<String, Void, String> {
    private Context ctx;
    TranslateBackgroundTask(Context ctx){
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        //String variables
        String textToBeTranslated = params[0];
        String languagePair = params[1];

        String jsonString;

        try {
            String key = ctx.getString(R.string.apiKey);
            String url = ctx.getString(R.string.yaUrl) + key
                         + "&text=" + textToBeTranslated + "&lang=" + languagePair;
            URL completeUrl = new URL(url)  ;

            HttpURLConnection httpJsonConnection = (HttpURLConnection) completeUrl.openConnection();
            InputStream inputStream = httpJsonConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n");
            }

            bufferedReader.close();
            inputStream.close();
            httpJsonConnection.disconnect();

            String resultString = jsonStringBuilder.toString().trim();

            resultString = resultString.substring(resultString.indexOf('[')+1);
            resultString = resultString.substring(0,resultString.indexOf("]"));

            resultString = resultString.substring(resultString.indexOf("\"")+1);
            resultString = resultString.substring(0,resultString.indexOf("\""));

            return resultString;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
