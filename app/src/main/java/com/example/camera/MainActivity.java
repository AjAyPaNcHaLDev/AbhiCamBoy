package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button capturePic;

    String currentPhotoPath;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private String TAG = "TAGA";
    File photoFile = null;
    EditText inpGIS, inpZONE;
    String GIS;
    String Zone;
    String imageFileName;
    private static final int PERMISSION_REQUEST_CODE = 100;

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

    public double latitude, longitude;
    LocationListener locationListener;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);
        capturePic = findViewById(R.id.capturePic);
        inpGIS = findViewById(R.id.inpGIS);
        inpZONE = findViewById(R.id.inpZONE);
        checkPermissions();
        capturePic.setOnClickListener(v -> {
            dispatchTakePictureIntent();
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                    longitude=0;
                    latitude=0;
                    showEnableLocationDialog();
                }
            };
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);

        });
    }
    private void showEnableLocationDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Enable Location Services");
        dialogBuilder.setMessage("Location services are required for this feature. Enable them now?");
        dialogBuilder.setPositiveButton("Yes", (dialog, which) -> {
            Intent locationSettingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(locationSettingsIntent);
        });
        dialogBuilder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
            showEnableLocationDialog();
        });
        dialogBuilder.show();
    }
    private void checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> permissionsToRequest = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
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

                String file_name = getGIS();
                if(longitude<0||latitude<0){
                    Toast.makeText(MainActivity.this,"Please check you location unable to print Geo code the recapture with gis id",Toast.LENGTH_LONG).show();
                }
                takenImage = bitmapWithGeoCode(takenImage,file_name);
//                imageView.setImageBitmap(takenImage);
                String yyyy = new SimpleDateFormat("yyyy",
                        Locale.getDefault()).format(new Date());
                String mm = new SimpleDateFormat("MM",
                        Locale.getDefault()).format(new Date());
                String dd = new SimpleDateFormat("dd",
                        Locale.getDefault()).format(new Date());
//                String file_name="Z-"+getZone()+"-"+getGIS();
//                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File myDir = new File(Environment.getExternalStorageDirectory(), "android/media/" + this.getPackageName() + "/TIS  Survey Images /" + dd + "-" + mm + "-" + yyyy);
                myDir.mkdirs();

                if (myDir.exists()) {

                    String fname = file_name + ".jpeg";
                    File file = new File(myDir, fname);


                    if (file.exists()) {

                        AlertDialog.Builder reName = new AlertDialog.Builder(MainActivity.this);
                        reName.setTitle("Alert");
                        reName.setMessage("File Name: " + file_name + "\nDo you went to update picture ?");
                        reName.setCancelable(false);

                        Bitmap finalTakenImage = takenImage;
                        reName.setPositiveButton("Yes reCapture",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        file.delete();
                                        try {

                                            FileOutputStream out = new FileOutputStream(file);
                                            finalTakenImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                            out.flush();
                                            out.close();
                                            imageView.setImageBitmap(finalTakenImage);
                                            Toast.makeText(MainActivity.this, file_name + ".jpeg" + "recapture successfully Saved", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }


                        );

                        reName.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imageView.setImageBitmap(null);
                                dialog.cancel();
                            }
                        });

                        AlertDialog alr = reName.create();
                        alr.show();

                    }
                    if (!file.exists()) {

                        try {
//                            Toast.makeText(MainActivity.this,"try block enter", Toast.LENGTH_LONG).show();
                            FileOutputStream out = new FileOutputStream(file);
                            takenImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                            imageView.setImageBitmap(takenImage);
                            Toast.makeText(MainActivity.this, file_name + ".jpeg" + " successfully Saved", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        }


                    }


                } else {
                    AlertDialog.Builder dirNotFound = new AlertDialog.Builder(MainActivity.this);
                    dirNotFound.setMessage("a Technical error  while create Folder !\n" + myDir);
                    dirNotFound.setTitle("Opps");
                    dirNotFound.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alr = dirNotFound.create();
                    alr.show();

                }

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }

        File del = new File(currentPhotoPath);
        del.delete();

    }
    public void openLinkDin(View view) {
        String url = "https://www.linkedin.com/in/ajaypanchal1/"; // Replace with your URL
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storagePermission = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (cameraPermission && storagePermission) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Camera and storage permissions are required.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        imageView.setImageBitmap(null);
        setGIS(inpGIS.getText().toString());
        setZone(inpZONE.getText().toString());
        if (!(!getGIS().isEmpty() && getGIS().length() >= 7 && getGIS().length() <= 15)) {
            Toast.makeText(MainActivity.this, "Please ensure that the GIS length is greater than 6 or less then 15  characters.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "dispatchTakePictureIntent: ");
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                Log.e(TAG, "dispatchTakePictureIntent: inside resoleActivity,");
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
            Log.e(TAG, "dispatchTakePictureIntent: end");
        }


    }
    private File createImageFile() throws IOException   {
        Log.e(TAG, "createImageFile: ");
        // Create an image file name

        imageFileName = "JPEG_TEMP";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.e(TAG, "createImageFile: end " + currentPhotoPath);


        return image;
    }
    public Bitmap bitmapWithGeoCode(Bitmap takenImage,String file_name) {
        Bitmap imageWithDateTimeAndGeo = Bitmap.createBitmap(takenImage.getWidth(), takenImage.getHeight(), takenImage.getConfig());
        Canvas canvas = new Canvas(imageWithDateTimeAndGeo);
        canvas.drawBitmap(takenImage, 0, 0, null);

// Add date and time text to the image
        Paint paint = new Paint();
        paint.setColor(Color.BLUE); // Text color
        paint.setTextSize(50); // Text size
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);
//Toast.makeText(MainActivity.this,"Location Code: "+longitude + " "+ latitude,Toast.LENGTH_LONG).show();
        String dateTimeString = getCurrentDateTime();
        String locationString = String.format(Locale.getDefault(), "%.6f  %.6f", longitude, latitude);
        String formattedText = dateTimeString + "\n" + locationString+"\n"+file_name;
        String[] lines = formattedText.split("\n");

        int x = 40;
        int y = 50;

        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += paint.getTextSize(); // Move to the next line
        }

        return imageWithDateTimeAndGeo;

    }
    private String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}
