package com.shivendra.imagerecognizer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.shivendra.imagerecognizer.ml.FlowerModel;
import com.shivendra.imagerecognizer.ml.JetstreamModel;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView currentImageName;
    ImageView image;
    Button browseButton;
    TextView resultText;
    public static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentImageName = (TextView)findViewById(R.id.currentImageName);
        image = (ImageView)findViewById(R.id.currentImage);
        browseButton = (Button)findViewById(R.id.button);
        resultText = (TextView)findViewById(R.id.resultText);
    }
    public void onBrowse(View view){
        System.out.print("onBrowse");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image.setImageBitmap(selectedImage);
                image.setRotation(90);
                resultText.setText("Processing Started");
                processImage(Bitmap.createScaledBitmap(selectedImage,448,448,false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(MainActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
        }

        public void processImage(Bitmap selectedImage){
            try {
                JetstreamModel model = JetstreamModel.newInstance(this);

                TensorImage tImage = TensorImage.fromBitmap(selectedImage);
                JetstreamModel.Outputs outputs = model.process(tImage);
                List<Category> predictionProbabilities = outputs.getProbabilityAsCategoryList();
                Collections.sort(predictionProbabilities, new CustomComparator());
                resultText.setText(getProcessedResults(predictionProbabilities.get(1)));
                model.close();
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }

    private String getProcessedResults(Category category) {
        String label= "Unsure ("+category.getLabel() + " ("+category.getScore()+")"+")";
        if(category.getScore()>0.7){
            label = category.getLabel() + " ("+category.getScore()+")";
        }
        return label;
    }

    public class CustomComparator implements Comparator<Category> {
        @Override
        public int compare(Category o1, Category o2) {
            int result = 0;
            if(o1.getScore() < o2.getScore()){
                result = -1;
            }
            else if(o1.getScore()> o2.getScore()){
                result = 1;
            }
            return result;
        }
    }
//    public void processFlower(Bitmap selectedImage){
//        try {
//            JetstreamModel jModel = JetstreamModel.newInstance(this);
//            FlowerModel model = FlowerModel.newInstance(this);
//
//            TensorImage tImage = TensorImage.fromBitmap(selectedImage);
////            TensorBuffer inputBuffer = tImage.getTensorBuffer();
////            FlowerModel.Outputs outputs = model.process(tImage.getTensorBuffer());
//            List predictionProbabilities = model.process(tImage).getProbabilityAsCategoryList();
//            resultText.setText(predictionProbabilities.get(0) +"");
//            model.close();
//        } catch (IOException e) {
//            // TODO Handle the exception
//        }
//    }
}