package com.example.nikkitricky.issuemap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class NewIssue extends Activity {

    Button btn;
    EditText description;
    Double lat, lon;
    SeekBar severity;
    Spinner dept;
    ProgressBar progressBar;
    Switch tweet;


    String url = "http://80ae51a6.ngrok.io/issue/new_issue/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_issue);

        btn = (Button)findViewById(R.id.submit);
        description = (EditText)findViewById(R.id.description);
        dept = (Spinner)findViewById(R.id.department);
        severity = (SeekBar)findViewById(R.id.severity);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tweet = (Switch) findViewById(R.id.tweet);

        progressBar.setVisibility(View.INVISIBLE);

        Bundle bundle = getIntent().getExtras();

        lat = Double.valueOf(bundle.getString("lat"));
        lon = Double.valueOf(bundle.getString("lon"));

        String path = "sdcard/issuemap/issue_image.jpg";
        ImageView imgView = (ImageView)findViewById(R.id.imageView);
        imgView.setImageDrawable(Drawable.createFromPath(path));

    }

    public void onClick(View v){
        progressBar.setVisibility(View.VISIBLE);

        Bitmap bitmap = BitmapFactory.decodeFile("sdcard/issuemap/issue_image.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        Log.i("HELLO", lat+"");

        JSONObject postreq = null;
        try {
            postreq = new JSONObject("{\"longitude\": " + lon + ",\"latitude\": " + lat + "," +
                    "\"description\": \"" + description.getText() + "\",\"department\": \"" +
                    dept.getSelectedItem().toString() + "\",\"severity\": " + severity.getProgress() + ", " +
                    "\"creation_date\": \"2018-06-02\",\"username\": 1,\"image\": \"" +
                    imageString +
                    "\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //sending image to server
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postreq, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject s) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(NewIssue.this, "Uploaded Successfully", Toast.LENGTH_LONG).show();
                if(!tweet.isEnabled()){
                    NewIssue.this.finish();
                }
                else {
                    Twitter.initialize(NewIssue.this);

                    Uri imageUri = Uri.fromFile(new File("sdcard/issuemap/issue_image.jpg"));

                    TweetComposer.Builder builder = new TweetComposer.Builder(NewIssue.this)
                            .text("just setting up my Twitter Kit.")
                            .image(imageUri);
                    builder.show();
                    Toast.makeText(NewIssue.this, "Tweet posted", Toast.LENGTH_LONG).show();
                    NewIssue.this.finish();
                }

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(NewIssue.this, "Upload failed", Toast.LENGTH_LONG).show();
                NewIssue.this.finish();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(this).addToRequestQueue(request);

    }
}

