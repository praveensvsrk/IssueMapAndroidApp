package com.example.nikkitricky.issuemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

public class IssueDetailViewActivity extends AppCompatActivity {

    ProgressBar progressBar;
    String mediaHost = "http://153eaebe.ngrok.io/media/";
    TextView description;
    TextView department;
    TextView severity;
    ImageView img;
    Map<Integer, String> deptmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail_view);

        deptmap.put(1, "Fire");
        deptmap.put(2, "Sewage");
        deptmap.put(3, "Power");
        deptmap.put(4, "Police");

        description = (TextView) findViewById(R.id.det_desc2);
        department = (TextView) findViewById(R.id.det_dept2);
        severity = (TextView) findViewById(R.id.det_severe);
        img = (ImageView) findViewById(R.id.det_img);

        progressBar = (ProgressBar)findViewById(R.id.progressBarDetail);

        String url = "http://153eaebe.ngrok.io/issue/issuelist/";

        int issueId;
        Bundle extras = getIntent().getExtras();
        issueId = extras.getInt("id");

        url += issueId + "/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject obj) {
                Log.d("HIII", "YES");
                try {
                    description.setText(obj.getString("description"));
                    department.setText(deptmap.get(obj.getInt("department")));
                    severity.setText(obj.getDouble("severity") + "");
                    progressBar.setVisibility(View.INVISIBLE);
                    new DownloadImageTask(img).execute(mediaHost + obj.getString("image"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(IssueDetailViewActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                IssueDetailViewActivity.this.finish();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }
}

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    ImageView bmImage;
    public DownloadImageTask(ImageView bmImage) {

        this.bmImage = bmImage;

    }  protected Bitmap doInBackground(String... urls) {

        String urldisplay = urls[0];

        Bitmap mIcon11 = null;

        try {

            InputStream in = new java.net.URL(urldisplay).openStream();

            mIcon11 = BitmapFactory.decodeStream(in);

        } catch (Exception e) {

            Log.e("Error", e.getMessage());

            e.printStackTrace();

        }

        return mIcon11;

    }  protected void onPostExecute(Bitmap result) {

        bmImage.setImageBitmap(result);

    }

}
