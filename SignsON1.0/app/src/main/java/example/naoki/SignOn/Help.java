package example.naoki.SignOn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;

import example.naoki.ble_myo.R;




public class Help extends ActionBarActivity {
    TextView help,myoNumber,glassNumber;
    String glass, myo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        Bundle extras = getIntent().getExtras();
        glass = extras.getString("GlassAdrres");
        myo = extras.getString("MyoAddress");
        help = (TextView) findViewById(R.id.textView2);
        myoNumber = (TextView) findViewById(R.id.textView3);
        glassNumber = (TextView) findViewById(R.id.textView4);
        help.setText("How do I use this app? \n " +
                "1) Connect your MYO to the app ussing the upper right menu \n " +
                "2) Connect your glass using the button Connect Glass \n" +
                "3) Check if your glass status is Connected and your myo works(Use the test button, your MYO should vibrate) \n " +
                "4) Click in teach me \n " +
                "5) Do the gesture, press save and wait. Note that you must do the gesture before you press save and during the Saving message \n" +
                "6) Click in detect, your moves will be printed in the app \n" +
                "\n" +
                "Who did this app?\n" +
                "Mariana Cristina and Lucas Saraiva");
        if (!glass.isEmpty()){
            glassNumber.setText(glass);
        }else{
            glassNumber.setText("Glass not connected yet");
        }
        if (!myo.isEmpty()){
            myoNumber.setText(myo);
        }else{
            myoNumber.setText("MYO not connected yet");
        }
    }

}
