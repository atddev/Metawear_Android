package com.asaad.metawearnative;


import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class SendToServer extends AsyncTask<String, Void, Void> {

  //  static DefaultHttpClient httpclient = new DefaultHttpClient();

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */



        @Override
        protected Void doInBackground(String... params) {


            DefaultHttpClient httpclient =  LoginActivity.httpclient;

           String Event = params[0];
            String Stime  = params[1];
            String Sdate  = params[2];


            //Get csrf cookie
            //   DefaultHttpClient httpclient = new DefaultHttpClient();

            // System.out.println("Initial set of cookies:");

            //   List<Cookie> cookies = httpclient.getCookieStore().getCookies();
//            Log.e("Res: ", "Token name: " + httpclient.getCookieStore().getCookies().get(0).getName());
 //           Log.e("Res: ", "Token value: " + httpclient.getCookieStore().getCookies().get(0).getValue());
 //           Log.e("Res: ", "Token exp date: " + httpclient.getCookieStore().getCookies().get(0).getExpiryDate());


            //login

            HttpPost post = new HttpPost("http://43.251.157.170/api/loggs/");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("csrfmiddlewaretoken", httpclient.getCookieStore().getCookies().get(0).getValue()));
            nameValuePairs.add(new BasicNameValuePair("event", Event));
            nameValuePairs.add(new BasicNameValuePair("stime", Stime));
            nameValuePairs.add(new BasicNameValuePair("sdate", Sdate));
            try {
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {


            }

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


                try {
                    JSONObject json = new JSONObject(pagina);
                    JSONObject json2 = json.getJSONObject("session");

                    if (!json2.getBoolean("success")) {
                //        return 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

