package com.project.pervsys.picaround;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.util.List;
import java.util.Locale;

public class UploadPhotoActivity extends AppCompatActivity {
    private final static String TAG = "UploadPhotoActivity";
    private static final String PHOTO_PATH = "photoPath";
    private static final String USER_PICTURE = "pictures";
    private static final String USERNAME = "username";
    private static final String SEPARATOR = "_";
    private static final String POINT_PICTURE = "points/pictures";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;
    private ImageView mImageView;
    private EditText nameField;
    private EditText descriptionField;
    private String mPhotoPath;
    private String name;
    private String description;
    private String username;
    private String photoId;
    private String timestamp;
    private String latitude;
    private String longitude;
    private com.project.pervsys.picaround.domain.Picture picture;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        // Set toolbar
        Toolbar toolbar  = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.upload_title);

        // Set status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mPhotoPath = getIntent().getStringExtra(PHOTO_PATH);
        username = getIntent().getStringExtra(USERNAME);
        Log.i(TAG, "Started activity, photo's path = " + mPhotoPath);
        mImageView = (ImageView) findViewById(R.id.image_to_upload);
        nameField = (EditText) findViewById(R.id.photo_name);
        descriptionField = (EditText) findViewById(R.id.photo_description);
        setPic();
        try {
            ExifInterface exif = new ExifInterface(mPhotoPath);
            takeExifInfo(exif);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();
        }
        if (latitude == null || longitude == null){
            Log.d(TAG, "Position not available in the metadata");
            // start a new activity that allows the user to select a place;
        }
        else{
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                //addresses = geocoder.getFromLocation(Double.parseDouble(latitude),
                 //       Double.parseDouble(longitude), 1);
                addresses = geocoder.getFromLocation(41.890638, 12.49075, 1);
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                Log.d(TAG, "address = " + address + " city = " + city);
            } catch (IOException e) {
                Log.e(TAG, "IOException " + e.toString());
            }
        }
        Log.i(TAG, "Photo put into the imageView");
    }


    private void takeExifInfo(ExifInterface exif) {
        timestamp = exif.getAttribute(ExifInterface.TAG_DATETIME);
        timestamp = timestamp.replace(" ", SEPARATOR);
        latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        //myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        //myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        //myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        //myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        //myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        Log.d(TAG, "Timestamp = " + timestamp + " lat = " + latitude + " long = " + longitude);
    }

    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed(){
        Intent i = getIntent();
        setResult(RESULT_CANCELED, i);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.upload:
                Log.i(TAG, "Upload has been selected");
                name = nameField.getText().toString();
                description = descriptionField.getText().toString();
                if(checkName() && checkDescription()) {
                    Log.d(TAG, "Ready for sending data to db");

                    //put the photo into the storage
                    Uri file = Uri.fromFile(new File(mPhotoPath));
                    //save the image as username_name-of-the-photo
                    Log.d(TAG, "Timestamp = " + timestamp);
                    photoId = username + SEPARATOR + timestamp;
                    StorageReference riversRef = mStorageRef.child(photoId);
                    riversRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Log.d(TAG, "OnSuccess");
                                    getPath();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    Log.e(TAG, "Error during the upload, " + exception.toString());
                                    Toast.makeText(getApplicationContext(),
                                            R.string.upload_failed,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    Intent i = getIntent();
                    setResult(RESULT_OK, i);
                    finish();
                }
                return true;
        }
        return false;
    }


    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap*/
        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath, bmOptions);

        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
    }

    private boolean checkName(){
        if (name.replace(" ", "").equals("")){
            Toast.makeText(this, R.string.name_missing, Toast.LENGTH_SHORT).show();
            nameField.setText("");
            return false;
        }
        return true;
    }

    private boolean checkDescription(){
        //TODO: remove this if no check is needed
        return true;
    }

    private void getPath(){
        Log.d(TAG, "getPath");
        StorageReference pathRef = mStorageRef.child(photoId);
        pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "MyDownloadLink:  " + uri);
                picture = new com.project.pervsys.picaround.domain.Picture(name, description, uri.toString());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(USER_PICTURE).push().setValue(picture);
                /*TODO: the picture has to be sent to pictures and
                  to the point associated with the position,
                  for now always the same point    */
                databaseReference.child("points/KgyIEDrixfImPdgKBaQ/pictures").push().setValue(picture);
                Log.i(TAG, "Picture's path sent to db");
                Toast.makeText(getApplicationContext(),
                        R.string.upload_ok,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                // ...
                Log.e(TAG, "Error during the upload, " + exception.toString());
                Toast.makeText(getApplicationContext(),
                        R.string.upload_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
