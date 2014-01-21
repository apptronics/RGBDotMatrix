package com.example.rgbdotmatrix;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SaveDialog extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_dialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_save_dialog, menu);
        return true;
    }
}
