package com.mooresedge.buysellswap;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationCategory extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView)findViewById(R.id.listViewCategory);
        String[] categories = getResources().getStringArray(R.array.category_spinner_entries);
        final ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.checked_list_item, categories);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0; i < adapter.getCount(); i++)
                {
                    CheckedTextView ctv = ((CheckedTextView)listView.getChildAt(i));
                    String category = ctv.getText().toString().replace(" ", "-").replace(",", "");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(category);
                    if(ctv.isChecked()) {
                        FirebaseMessaging.getInstance().subscribeToTopic(category);
                    }
                }
                finish();
            }
        });
    }
}
