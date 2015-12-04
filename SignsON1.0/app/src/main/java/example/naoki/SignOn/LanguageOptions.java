package example.naoki.SignOn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import example.naoki.ble_myo.R;

public class LanguageOptions extends ActionBarActivity {
    Button portuguese;
    Button english;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_options);
        portuguese =(Button) findViewById(R.id.portuguese);
        english = (Button) findViewById(R.id.english);
        portuguese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "pt");
                setResult(MainActivity.RESULT_OK, returnIntent);
                finish();
            }
        });

       english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "eng");
                setResult(MainActivity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
