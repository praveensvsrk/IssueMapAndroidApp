package com.example.nikkitricky.issuemap;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class NewIssue extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_issue);
        String path = "sdcard/issuemap/issue_image.jpg";
        ImageView img = (ImageView)findViewById(R.id.imageView);
        img.setImageDrawable(Drawable.createFromPath(path));
    }
}
