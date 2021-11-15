package levkaantonov.com.study.langlock;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TranslateBackgroundTask extends AsyncTask<String, Void, String>{
    private String apiKey;
    private String yaUrl;

    TranslateBackgroundTask(String apiKey, String yaUrl){
        this.apiKey = apiKey;
        this.yaUrl = yaUrl;
    }

    @Override
    protected String doInBackground(String... params){
        try{
            if(isCancelled()) return null;
            String textToBeTranslated = params[0];
            String languagePair       = params[1];

            String jsonString;

            Log.d("MYTAG", "doInBackground " + isCancelled());

            String key = apiKey;
            String url = yaUrl + key + "&text=" +
                         textToBeTranslated + "&lang=" + languagePair;
            Log.d("MYTAG", url);
            URL completeUrl = new URL(url);

            HttpURLConnection httpJsonConnection = (HttpURLConnection) completeUrl.openConnection();
            InputStream       inputStream        = httpJsonConnection.getInputStream();
            BufferedReader    bufferedReader     = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n");
            }

            bufferedReader.close();
            inputStream.close();
            httpJsonConnection.disconnect();

            String resultString = jsonStringBuilder.toString().trim();

            resultString = resultString.substring(resultString.indexOf('[') + 1);
            resultString = resultString.substring(0, resultString.indexOf("]"));

            resultString = resultString.substring(resultString.indexOf("\"") + 1);
            resultString = resultString.substring(0, resultString.indexOf("\""));

            return resultString;

        }
        catch (Exception e){
            if(e instanceof InterruptedException){
                Log.d("MYTAG", "Interrupted");
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override protected void onCancelled(){
        super.onCancelled();
        Log.d("MYTAG", "onCancelled: ");
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values){
        super.onProgressUpdate(values);
    }
}
