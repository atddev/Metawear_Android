package com.asaad.metawearnative;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    static DefaultHttpClient httpclient = new DefaultHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("Invalid Password");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Username field is empty!");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("Invalid username");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // hide keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return !email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

        /*    try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
*/


            //Get csrf cookie
            //   DefaultHttpClient httpclient = new DefaultHttpClient();

            HttpGet httpget = new HttpGet("http://43.251.157.170/");

            HttpResponse response = null;
            HttpEntity entity = null;
            try {
                response = httpclient.execute(httpget);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response == null) {
                return 2;
            } else {
                entity = response.getEntity();
            }


            // System.out.println("Login form get: " + response.getStatusLine());
            Log.e("Res: ", "Login form get: " + response.getStatusLine());
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // System.out.println("Initial set of cookies:");

            //   List<Cookie> cookies = httpclient.getCookieStore().getCookies();
            Log.e("Res: ", "Token name: " + httpclient.getCookieStore().getCookies().get(0).getName());
            Log.e("Res: ", "Token value: " + httpclient.getCookieStore().getCookies().get(0).getValue());
            Log.e("Res: ", "Token exp date: " + httpclient.getCookieStore().getCookies().get(0).getExpiryDate());


            //login

            HttpPost post = new HttpPost("http://43.251.157.170/session/");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("csrfmiddlewaretoken", httpclient.getCookieStore().getCookies().get(0).getValue()));
            nameValuePairs.add(new BasicNameValuePair("username", mEmail));
            nameValuePairs.add(new BasicNameValuePair("password", mPassword));

            try {
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                //  e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_LONG);
                toast.show();
            }

            //   String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.NO_WRAP));
            //  post.addHeader("Authorization", basicAuth);

            post.addHeader("X-CSRFToken", httpclient.getCookieStore().getCookies().get(0).getValue());
            try {
                HttpResponse Postresponse = httpclient.execute(post);
                HttpEntity Postentity = Postresponse.getEntity();
                Log.e("Res: ", "Post stat: " + Postresponse.getStatusLine());


                //start endcod
                BufferedReader rd = null;
                try {
                    rd = new BufferedReader(new InputStreamReader(Postresponse.getEntity().getContent()));
                } catch (IOException e) {
                    // e.printStackTrace();
                    Toast toast = Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_LONG);
                    toast.show();
                }
                String line = "";
                String pagina = "";
                try {
                    while ((line = rd.readLine()) != null) {
                        pagina = pagina + "\n" + line;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // System.out.println(pagina);
                Log.e("Res: ", "pagina: " + pagina);

                try {
                    JSONObject json = new JSONObject(pagina);
                    JSONObject json2 = json.getJSONObject("session");

                    if (!json2.getBoolean("success")) {
                        return 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            // TODO: register the new account here.
            //zero is true
            return 0;
        }

        @Override
        protected void onPostExecute(Integer success) {
            mAuthTask = null;
            showProgress(false);

            if (success == 0) {
                //finish();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

            } else if (success == 1) {
                Toast toast = Toast.makeText(getApplicationContext(), "Worng username or password!", Toast.LENGTH_LONG);
                toast.show();
                mPasswordView.setError("Worng username or password!");
                mEmailView.setError("Worng username or password!");
            } else if (success == 2) {
                Toast toast = Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_LONG);
                toast.show();
            }

        }


        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

