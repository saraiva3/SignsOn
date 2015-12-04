package example.naoki.SignOn;

import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import example.naoki.ble_myo.R;


public class LoginActivity extends ActionBarActivity  {


    private static final ArrayList<String> DUMMY_CREDENTIALS = new ArrayList<String>();

    private UserLoginTask mAuthTask = null;

     private EditText login;
    private EditText password;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        DUMMY_CREDENTIALS.add("lucas:lucas");
        DUMMY_CREDENTIALS.add("mari:luma");
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
        status = (TextView) findViewById(R.id.status);


        Button login = (Button) findViewById(R.id.loginBtn);
        Button create = (Button) findViewById(R.id.createBtn);
        create.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreate();
            }
        });
        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


    }
    private void attemptCreate() {
        String email = login.getText().toString();
        String passwords = password.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (!TextUtils.isEmpty(passwords) && !isPasswordValid(passwords)) {
            password.setError(getString(R.string.error_invalid_password));
            focusView = password;
            cancel = true;
        }


        if (TextUtils.isEmpty(email)) {
            login.setError(getString(R.string.error_field_required));
            focusView = login;
            cancel = true;
        } else if (!isEmailValid(email)) {
            login.setError(getString(R.string.error_invalid_email));
            focusView = login;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();

            status.setText("Login Creation Failed");
        } else {
            DUMMY_CREDENTIALS.add(email+":"+passwords);
            status.setText("Login Created");
        }

    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        login.setError(null);
        password.setError(null);

        // Store values at the time of the login attempt.
        String email = login.getText().toString();
        String passwords = password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwords) && !isPasswordValid(passwords)) {
            password.setError(getString(R.string.error_invalid_password));
            focusView = password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            login.setError(getString(R.string.error_field_required));
            focusView = login;
            cancel = true;
        } else if (!isEmailValid(email)) {
            login.setError(getString(R.string.error_invalid_email));
            focusView = login;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
           // showProgress(true);
            mAuthTask = new UserLoginTask(email, passwords);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {

        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {

        return password.length() >= 6;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
           // showProgress(false);

            if (success) {
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
               // myIntent.putExtra("key", value); //Optional parameters
                LoginActivity.this.startActivity(myIntent);
                finish();
            } else {
                password.setError(getString(R.string.error_incorrect_password));
                password.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
          //  showProgress(false);
        }
    }
}

