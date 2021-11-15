package levkaantonov.com.study.langlock;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Locale;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranslateBackgroundTask extends AsyncTask<String, Void, String>{
    private String apiKey;
    private String baseUrl;

    TranslateBackgroundTask(String apiKey, String baseUrl){
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    protected String doInBackground(String... params){
        try{
            if(isCancelled()) return null;

            String text = params[0];
            String from = params[1];
            String to   = params[2];

            String       url    = baseUrl + "/translate";
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("q", text.toLowerCase(Locale.ROOT))
                    .add("source", from)
                    .add("target", to)
                    .add("format", "text")
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Call     call = client.newCall(request);
            Response response;
            response = call.execute();
            String body = response.body().string();
            Gson   gson = new Gson();
            if(response.isSuccessful()){
                TranslatedText output = gson.fromJson(body, TranslatedText.class);
                return output.translatedText;
            } else {
                Error output = gson.fromJson(body, Error.class);
                Log.e("MYTAG", output.error);
            }
        }
        catch (Exception e){
            if(e instanceof InterruptedException){
                e.printStackTrace();
            }
        }
        return null;
    }
}

class TranslatedText{
    public String translatedText;
}

class Error{
    public String error;
}