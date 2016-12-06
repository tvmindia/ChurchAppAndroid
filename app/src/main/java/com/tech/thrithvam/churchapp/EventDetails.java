package com.tech.thrithvam.churchapp;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class EventDetails extends AppCompatActivity {

    Bundle extras;
    String URL;
    TextView Date,Notice_head,Notice_content;
    ImageView Notice_image;
    Typeface typeQuicksand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        extras=getIntent().getExtras();

        URL=extras.getString("URL");
        typeQuicksand = Typeface.createFromAsset(getAssets(),"fonts/quicksandbold.otf");
        Notice_head=(TextView)findViewById(R.id.activity_Notice_head);
        Notice_head.setTypeface(typeQuicksand);
        Date=(TextView)findViewById(R.id.Event_date);
        Notice_content=(TextView)findViewById(R.id.Event_details);
        Notice_image=(ImageView)findViewById(R.id.Notice_img);

        if(getIntent().hasExtra("EventName")){
            Notice_head.setText(extras.getString("EventName"));
        }
        if(getIntent().hasExtra("StartDate")){
            Date.setText(extras.getString("StartDate"));
        }
        if(getIntent().hasExtra("Descrtiption")){
            Notice_content.setText(extras.getString("Descrtiption"));
        }
        //image loading using url
        if(!URL.equals("null")){
            Glide.with(EventDetails.this)
                    .load(getResources().getString(R.string.url) +URL.substring((URL).indexOf("img")))
                    .placeholder(R.drawable.notices)
                    .thumbnail(0.1f)
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Notice_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            Glide.with(EventDetails.this)
                                    .load(R.drawable.notices)
                                    .into(Notice_image)
                            ;
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(Notice_image)
            ;
        }
    }
}