package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        loadImagesFragment();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.bottom_images){
                    loadImagesFragment();

                }

                else if (itemId == R.id.bottom_pdf){
                    loadPdfFragments();

                }
                return true;
            }
        });

    }

    private void loadImagesFragment() {
        setTitle("Images");
        Image_List_Fragment image_list_fragment = new Image_List_Fragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, image_list_fragment, "Image_List_Fragment");
        fragmentTransaction.commit();

    }

    private void loadPdfFragments() {

        setTitle("PDF List");
        Pdf_List_Fragment pdf_list_fragment = new Pdf_List_Fragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, pdf_list_fragment, "Pdf_List_Fragment");
        fragmentTransaction.commit();
    }

//    private void loadPdfFragments() {
//
//        setTitle("Images");
//        Image_List_Fragment image_list_fragment = new Image_List_Fragment();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.frame_layout, image_list_fragment, "Image_List_Fragment");
//        fragmentTransaction.commit();
//    }

//    private void loadImagesFragment() {
//
//        setTitle("PDF List");
//
//        Pdf_List_Fragment pdf_list_fragment = new Pdf_List_Fragment();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.frame_layout, pdf_list_fragment, "Pdf_List_Fragment");
//        fragmentTransaction.commit();
//
//    }


}