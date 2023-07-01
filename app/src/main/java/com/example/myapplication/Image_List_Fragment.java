package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Image_List_Fragment extends Fragment {

    private static final String TAG = "IMAGE_LIST_TAG";

    private static final int STORAGE_REQUEST_CODE =100;
    private static final int CAMERA_REQUEST_CODE = 101;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri imageUri = null;
    private Context mContext;
    private FloatingActionButton addImageF;

    private RecyclerView imageRv;

    private ArrayList<ModelImage> allImageArrayList;
    private AdapterImage adapterImage;

    public Image_List_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {

        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image__list_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraPermission = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        addImageF = view.findViewById(R.id.addImageF);
        imageRv = view.findViewById(R.id.imageRv);

        loadImages();

        addImageF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showInputImageDialog();

            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_images, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.images_delete){

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Delete Images")
                    .setMessage("Are you sure you want to delete all/selected images?")
                    .setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteImages(true);

                        }
                    })
                    .setNeutralButton("Delete selected ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteImages(false);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteImages(boolean deleteAll){

        ArrayList<ModelImage> imagesToDeleteList = new ArrayList<>();
        if (deleteAll){

            imagesToDeleteList = allImageArrayList;

        }
        else{

            for(int i = 0; i<allImageArrayList.size(); i++){

                if (allImageArrayList.get(i).isChecked()) {

                    imagesToDeleteList.add(allImageArrayList.get(i));
                }

            }

        }

        for ( int i=0; i <imagesToDeleteList.size(); i++){

            try{

                String pathOfImageToDelete = imagesToDeleteList.get(i).getImageUri().getPath();

                File file = new File(pathOfImageToDelete);

                if (file.exists()){

                    boolean isDeleted = file.delete();
                    Log.d(TAG, "deleteImages: isDeleted: "+isDeleted);
                }

            }
            catch (Exception e){
                Log.d(TAG, "deleteImages: ", e);

            }
        }
        Toast.makeText(mContext, "Deleted" , Toast.LENGTH_SHORT).show();
        loadImages();

    }

    private void loadImages(){
        Log.d(TAG, "loadImages: ");

        allImageArrayList = new ArrayList<>();
        adapterImage = new AdapterImage(mContext, allImageArrayList);

        imageRv.setAdapter(adapterImage);

        File folder = new File(mContext.getExternalFilesDir(null), Constants.IMAGES_FOLDER);

        if (folder.exists()){
            Log.d(TAG, "loadImages: folder exists");

            File[] files = folder.listFiles();

            if (files != null){
                Log.d(TAG, "loadImages: Folder exists and have images");

                for (File file: files){

                    Uri imageUri= Uri.fromFile(file);

                    ModelImage modelImage = new ModelImage(imageUri, false);

                    allImageArrayList.add(modelImage);
                    adapterImage.notifyItemInserted(allImageArrayList.size());
                }


            }
            else{
                Log.d(TAG, "loadImages: Folder exist but empty");
            }

        }
        else{
            Log.d(TAG, "loadImages: folder doesn't exists");
        }

    }

    private void saveImageToAppLevelDirectory(Uri imageUriToBeSaved){
        Log.d(TAG, "saveImageAppLevelDirectory: ");
        try {

            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){

                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(), imageUriToBeSaved));
            }
            else {

                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUriToBeSaved);
            }

            File directory = new File(mContext.getExternalFilesDir(null), Constants.IMAGES_FOLDER);
            directory.mkdirs();

            long timestamp = System.currentTimeMillis();
            String fileName = timestamp+ ".jpeg";

            File file = new File(mContext.getExternalFilesDir(null), ""+ Constants.IMAGES_FOLDER+ "/" + fileName);

            try{

                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Log.d(TAG, "saveImageToAppLevelDirectory: Image Saved");
//                Log.d(TAG, "saveImageToAppLevelDirectory: Failed to save image due to "+startPostponedEnterTransition());
                Toast.makeText(mContext, "Image saved", Toast.LENGTH_SHORT).show();

            }
            catch (Exception e){
                Log.d(TAG, "saveImageToAppLevelDirectory: ", e);

                Toast.makeText(mContext, "Failed to save image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }


        }

        catch (Exception e){

            Toast.makeText(mContext, "Failed to prepare image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

    private void showInputImageDialog(){
        Log.d(TAG, "showInputImageDialog: ");

        PopupMenu popupMenu = new PopupMenu(mContext, addImageF);

        popupMenu.getMenu().add(Menu.NONE , 1 , 1 , "CAMERA");
        popupMenu.getMenu().add(Menu.NONE , 2 , 2 ,"GALLERY");

        popupMenu.show();


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int itemId = menuItem.getItemId();
                if (itemId == 1){
                    Log.d(TAG, "onMenuItemClick: camera is clicked, check if camera permissions are granted or not ");
                    if (checkCameraPermissions()){
                        pickImageCamera();
                    }
                    else {
                        requestCameraPermission();
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Gallery is clicked, check if storage permission is granted or not");
                    if (checkStoragePermission()){
                        pickImageGallery();
                    }
                    else {
                        requestStoragePermission();
                    }

                }
                return true;
            }
        });


    }

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

//    private boolean checkStoragePermission(){
//
//        boolean result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        return result;
//    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent data = result.getData();

                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked image gallery: "+ imageUri);

                        saveImageToAppLevelDirectory(imageUri);


                        ModelImage modelImage = new ModelImage(imageUri, false);
                        allImageArrayList.add(modelImage);
                        adapterImage.notifyItemInserted(allImageArrayList.size());


                    }
                    else{
                        Toast.makeText(mContext, "Cancelled" , Toast.LENGTH_SHORT).show();

                    }


                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE , "TEMP IMAGE TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION , "TEMP IMAGE DESCRIPTION");

        imageUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT , imageUri);
        cameraActivityResultLauncher.launch(intent);



    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Picked image camera "+imageUri);

                        saveImageToAppLevelDirectory(imageUri);

                        ModelImage modelImage = new ModelImage(imageUri, false);
                        allImageArrayList.add(modelImage);
                        adapterImage.notifyItemInserted(allImageArrayList.size());

                    }
                    else{
                        Toast.makeText(mContext, "Cancelled" , Toast.LENGTH_SHORT).show();
                    }

                }
            }

    );

    private boolean checkStoragePermission(){
        Log.d(TAG, "checkStoragePermission: ");

        boolean result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result;
    }


    private void requestStoragePermission(){
        Log.d(TAG, "requestStoragePermission: ");
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        Log.d(TAG, "checkCameraPermissions: ");

        boolean cameraResult = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageResult = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return  cameraResult && storageResult;
    }

    private void requestCameraPermission(){
        Log.d(TAG, "requestCameraPermission");
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if (grantResults.length > 0){

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        Log.d(TAG, "onRequestPermissionsResult: both permissions (Camera and Gallery) are granted, we can launch camera intent ");
                        pickImageCamera();

                    }
                    else{
                        Log.d(TAG, "onRequestPermissionsRequest: Camera & Storage permission are required");
                        Toast.makeText(mContext, "Camera & Storage Permission are required" , Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.d(TAG, "onRequestPermissionsRequest: Cancelled");
                    Toast.makeText(mContext, "Cancelled" , Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                if (grantResults.length >0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        Log.d(TAG, "onRequestPermissionResult: storage permission granted, we can launch gallery intent");
                        pickImageGallery();

                    }
                    else {
                        Log.d(TAG, "onRequestPermissionResult: storage permission denied, can't launch gallery intent");
                        Toast.makeText(mContext, "Storage permission is required" , Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.d(TAG, "onRequestPermissionResult: Cancelled");
                    Toast.makeText(mContext, "Cancelled" , Toast.LENGTH_SHORT).show();
                }

            }
            break;
        }
    }
}