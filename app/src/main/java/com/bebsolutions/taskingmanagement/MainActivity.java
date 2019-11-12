package com.bebsolutions.taskingmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getName();

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    private String currentDate;
    private String currentCity;

    AppCompatTextView txtTitulo;
    AppCompatImageView imgLogo;
    AppCompatTextView txtNome;
    AppCompatTextView txtEmail;
    AppCompatButton btnEntrar;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTitulo = findViewById(R.id.txtTitulo);
        imgLogo = findViewById(R.id.imgLogo);
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        btnEntrar = findViewById(R.id.btnEntrar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();



        if (currentUser==null){
            startActivity(new Intent(MainActivity.this, TelefoneAutenticarActivity.class));
            return;
        }

        db = FirebaseFirestore.getInstance();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    getLocation();
        //}
        getPermissionLocation();

        currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        currentCity = "";


        txtTitulo.setText("Sistema de ordem de serviço " + currentCity + ", " + currentDate);
        txtNome.setText("");
        txtEmail.setText("");


        CollectionReference usuariosRef = db.collection("usuarios");
        Query query = usuariosRef.whereEqualTo("uid", currentUser.getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete");
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot.size()>0){
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            if (data!=null){
                                String nome = data.containsKey("nome")?!TextUtils.isEmpty(data.get("nome").toString())?data.get("nome").toString():"":"";
                                String email ="";
                                if (data.containsKey("email")){
                                    Object o = data.get("email");
                                    if (o instanceof String){
                                        if (!TextUtils.isEmpty(o.toString())){
                                            email = o.toString();
                                        }
                                    }
                                }
                                txtNome.setText(nome);
                                txtEmail.setText(email);
                            }


                            SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                            pref.putString("TELEFONE", document.getData().get("telefone").toString());
                            pref.putInt("USUARIO_TIPO", Integer.parseInt(document.getData().get("tipo").toString()));
                            pref.apply();

                            //SharedPreferences pref1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            //Log.d(TAG, pref1.getString("TELEFONE", ""));


                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting documents."+ task.getException());
                }

            }
        });

        //btnEntrar.setOnClickListener((v) -> );
        btnEntrar.setOnClickListener((v) -> {
            startActivity(new Intent(MainActivity.this, ListOrdemActivity.class));
        });
    }


    private void getLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                try{
                                    Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
                                    List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses.size() > 0) {
                                        currentCity = addresses.get(0).getLocality();
                                        txtTitulo.setText("Sistema de ordem de serviço " + currentCity + ", " + currentDate);
                                    }
                                    // Logic to handle location object
                                }catch (Exception ex){

                                }
                            }
                        }
                    });
        }
    }

    private void getPermissionLocation(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

            /*// Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                getLocation();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }*/
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    getPermissionLocation();
                }
                return;
            }
        }
    }

}
