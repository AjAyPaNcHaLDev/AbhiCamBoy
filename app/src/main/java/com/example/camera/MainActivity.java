package com.example.camera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button capturePic;
    public static final int REQUEST_IMAGE_CAPTURE=1;
    private String TAG = "TAGA";
    File photoFile = null;
    EditText inpGIS, inpZONE;
    String GIS;
    String Zone;
    String imageFileName;
    File storageDir;
    Bitmap takenImage;
    public String getGIS() {
        return GIS;
    }

    public void setGIS(String GIS) {
        this.GIS = GIS;
    }

    public String getZone() {
        return Zone;
    }

    public void setZone(String zone) {
        Zone = zone;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myPermission();
    }




    private void dispatchTakePictureIntent() {
        imageView.setImageBitmap(null);
        setGIS(inpGIS.getText().toString());
        setZone(inpZONE.getText().toString());
        if(getGIS().isEmpty() || getZone().isEmpty()){
            Toast.makeText(MainActivity.this,"please fill Sector and GIS", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.e(TAG, "dispatchTakePictureIntent: " );
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                Log.e(TAG, "dispatchTakePictureIntent: inside resoleActivity," );
                // Create the File where the photo should go

                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.camera.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Start the image capture intent to take photo
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
            Log.e(TAG, "dispatchTakePictureIntent: end" );
        }




    }



    String currentPhotoPath;

    private File createImageFile() throws IOException {
        Log.e(TAG, "createImageFile: " );
        // Create an image file name

        imageFileName =  "S-"+getZone()+"-"+getGIS()+"  __";
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image= File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.e(TAG, "createImageFile: end "+currentPhotoPath );



        return image;




    }
@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview

                imageView.setImageBitmap(takenImage);
                String yyyy = new SimpleDateFormat("yyyy",
                        Locale.getDefault()).format(new Date());
                String mm = new SimpleDateFormat("MM",
                        Locale.getDefault()).format(new Date());
                String dd = new SimpleDateFormat("dd",
                        Locale.getDefault()).format(new Date());
                String file_name="Z-"+getZone()+"-"+getGIS();
                String root = Environment.getExternalStorageDirectory().getPath();
                File myDir = new File(root + "/Android/data/com.example.camera/files/Pictures/Jalandhar Survey Images /"+dd+"-"+mm+"-"+yyyy+"/"+"Z-"+getZone());
                myDir.mkdirs();

                if(myDir.exists()){

                    String fname = file_name+".jpeg";
                    File file = new File (myDir, fname);


                    if(file.exists()){

                        AlertDialog.Builder reName=new AlertDialog.Builder(MainActivity.this);
                        reName.setTitle("Alert");
                        reName.setMessage("You went to "+getZone()+"-" +getGIS()+" reCapture picture ?"+currentPhotoPath+ imageFileName);
                        reName.setCancelable(false);

                        reName.setPositiveButton("Yes reCapture",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        file.delete ();
                                        try {

                                            FileOutputStream out = new FileOutputStream(file);
                                            takenImage.compress(Bitmap.CompressFormat.JPEG, 20, out);
                                            out.flush();
                                            out.close();
                                            imageView.setImageBitmap(takenImage);
                                            Toast.makeText(MainActivity.this,"S-"+getZone()+"-"+getGIS()+ "re recapture successfully Saved", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(MainActivity.this,e.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }


                        );

                        reName.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alr=reName.create();
                        alr.show();

                    }
                    if(!file.exists()){

                        try {
                            Toast.makeText(MainActivity.this,"try block enter", Toast.LENGTH_LONG).show();
                            FileOutputStream out = new FileOutputStream(file);
                            takenImage.compress(Bitmap.CompressFormat.JPEG, 20, out);
                            out.flush();
                            out.close();
                            imageView.setImageBitmap(takenImage);
                            Toast.makeText(MainActivity.this,"S-"+getZone()+"-"+getGIS()+ " successfully Saved", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,e.toString(), Toast.LENGTH_LONG).show();
                        }


                    }




                }else{

                    AlertDialog.Builder dirNotFound  =new AlertDialog.Builder(MainActivity.this);
                    dirNotFound.setMessage("a Technical error  while create Folder !\n"+myDir);
                    dirNotFound.setTitle("Opps");
                    dirNotFound.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alr=dirNotFound.create();
                    alr.show();

                }

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }

        File del = new File (currentPhotoPath);
        del.delete();

    }


    public  void myPermission(){
        Dexter.withContext(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                imageView=findViewById(R.id.image_view);
                capturePic=findViewById(R.id.capturePic);
                inpGIS=findViewById(R.id.inpGIS);
                inpZONE=findViewById(R.id.inpZONE);
                accessCamera();
                accessMemory();

                capturePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dispatchTakePictureIntent();
                    }
                });
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                capturePic=findViewById(R.id.capturePic);
                capturePic.setText("Please allow all permissions");
                capturePic.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPermission();
                                capturePic.setText("Capture & Save ");
                            }
                        }
                );
                Intent intent =new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri=Uri.fromParts("package",getPackageName(),null);
                intent.setData(uri);
                accessCamera();
                accessMemory();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();


    }

    public void accessCamera(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    }, 100);}

    }
    public  void accessMemory(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new  String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },100);
        }
    }
}