package levkaantonov.com.study.langlock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


public class AboutActivity extends AppCompatActivity{
    private ImageView logo;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logo = findViewById(R.id.logo);
        logo.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse(getString(R.string.baseUrl)));
                AboutActivity.this.startActivity(intent);
            }
        });
    }
}
