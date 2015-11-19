package example.naoki.SignOn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import example.naoki.ble_myo.R;

public class LanguageOptions extends ActionBarActivity {
    Button portuguese;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_options);
            portuguese =(Button) findViewById(R.id.portuguese);

        portuguese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                setResult(MainActivity.RESULT_OK, returnIntent);
                finish();
            }
        });

    }

}
