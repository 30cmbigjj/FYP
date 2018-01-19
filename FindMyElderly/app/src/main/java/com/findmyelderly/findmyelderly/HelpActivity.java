package com.findmyelderly.findmyelderly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import java.io.IOException;

public class HelpActivity extends AppCompatActivity {
    private ImageButton addButton;
    private ImageView imageView;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;
    boolean upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_page);

        addButton = (ImageButton) findViewById(R.id.add);
        imageView = (ImageView) findViewById(R.id.familyicon);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, Chatrm.class));
            }
        });*/
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            upload = true;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 450 , 450, true);
                imageView.setImageBitmap(resized);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

