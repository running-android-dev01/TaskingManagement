package com.bebsolutions.taskingmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.bebsolutions.taskingmanagement.model.FotoAntes;
import com.bebsolutions.taskingmanagement.model.FotoDepois;
import com.bebsolutions.taskingmanagement.model.OrdemServico;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdemServicoExecucaoActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoExecucaoActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoExecucaoActivity.class + ".ORDEM_SERVICO";

    OrdemServico ordemServico;

    AppCompatButton btnDescricao;
    AppCompatButton btnMateriais;
    AppCompatButton btnFotos;
    AppCompatButton btnEncerrar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final int REQUEST_DESCRICAO = 1;
    private static final int REQUEST_MATERIAL = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int REQUEST_TAKE_PHOTO_DESCRICAO = 4;

    private String mCurrentPhotoPath;
    private String imageFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_execucao);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        btnDescricao = findViewById(R.id.btnDescricao);
        btnMateriais = findViewById(R.id.btnMateriais);
        btnFotos = findViewById(R.id.btnFotos);
        btnEncerrar = findViewById(R.id.btnEncerrar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (ordemServico.dataInicio==null){
            ordemServico.dataInicio = Timestamp.now().toDate();
            atualizarDataInicio(ordemServico.key, ordemServico.dataInicio);
        }

        btnDescricao.setOnClickListener((v) -> {
            Intent intent = new Intent(OrdemServicoExecucaoActivity.this, OrdemServicoDescricaoActivity.class);
            intent.putExtra(OrdemServicoDescricaoActivity.CT_ORDEM_SERVICO, ordemServico);
            startActivityForResult(intent, REQUEST_DESCRICAO);
        });

        btnMateriais.setOnClickListener((v) -> {
            Intent intent = new Intent(OrdemServicoExecucaoActivity.this, OrdemServicoMaterialActivity.class);
            intent.putExtra(OrdemServicoMaterialActivity.CT_ORDEM_SERVICO, ordemServico);
            startActivityForResult(intent, REQUEST_MATERIAL);
        });

        btnFotos.setOnClickListener((v) -> {
            tirarFotoDepois();
        });

        btnEncerrar.setOnClickListener((v) -> {
            ordemServico.dataTermino = Timestamp.now().toDate();


            Map<String, Object> data = new HashMap<>();
            data.put("dt_termino", new Timestamp(ordemServico.dataTermino));

            db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge());

            Intent intent = new Intent(OrdemServicoExecucaoActivity.this, OrdemServicoEncerrarActivity.class);
            intent.putExtra(OrdemServicoEncerrarActivity.CT_ORDEM_SERVICO, ordemServico);
            startActivity(intent);

            /*
            db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.w(TAG, "onSuccess");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.w(TAG, "onComplete");
                    Intent intent = new Intent(OrdemServicoExecucaoActivity.this, OrdemServicoEncerrarActivity.class);
                    intent.putExtra(OrdemServicoEncerrarActivity.CT_ORDEM_SERVICO, ordemServico);
                    startActivity(intent);
                }
            });
            */
        });




        setUpToolbar();
    }


    private void tirarFotoDepois(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.bebsolutions.taskingmanagement.fileProvider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } else {
                    String url = photoFile.toString();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + url));
                }

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK && REQUEST_DESCRICAO==requestCode){
            ordemServico = data.getParcelableExtra(CT_ORDEM_SERVICO);
            verificarDescricao();
        }else if (resultCode== Activity.RESULT_OK && REQUEST_TAKE_PHOTO==requestCode){
            //setPic();
            //ordemServico.flgFotoDepois = true;
            //verificarFotosDepois();

            Intent intent = new Intent(OrdemServicoExecucaoActivity.this, OrdemServicoDescricaoFotoDepoisActivity.class);
            intent.putExtra(OrdemServicoDescricaoFotoDepoisActivity.CT_ORDEM_SERVICO, ordemServico);
            intent.putExtra(OrdemServicoDescricaoFotoDepoisActivity.CT_CAMINHO, mCurrentPhotoPath);
            intent.putExtra(OrdemServicoDescricaoFotoDepoisActivity.CT_NOME, imageFileName);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO_DESCRICAO);

        }else if (resultCode== Activity.RESULT_OK && REQUEST_MATERIAL==requestCode){
            ordemServico = data.getParcelableExtra(CT_ORDEM_SERVICO);
            verificarMateriais();
        }else if (resultCode== Activity.RESULT_OK && REQUEST_TAKE_PHOTO_DESCRICAO==requestCode){
            ordemServico = data.getParcelableExtra(CT_ORDEM_SERVICO);
            verificarFotosDepois();
        }


    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarDescricao();
        verificarMateriais();
        verificarFotosDepois();
    }

    private void verificarDescricao(){
        //btnFoto.end
        Drawable id_check = ContextCompat.getDrawable(
                OrdemServicoExecucaoActivity.this,
                R.mipmap.ic_check
        );

        if (ordemServico.flgDescricao){
            btnDescricao.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    id_check, // Drawable right
                    null // Drawable bottom
            );
        }else{
            btnDescricao.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    null, // Drawable right
                    null // Drawable bottom
            );
        }
    }


    private void verificarMateriais(){
        //btnFoto.end
        Drawable id_check = ContextCompat.getDrawable(
                OrdemServicoExecucaoActivity.this,
                R.mipmap.ic_check
        );

        if (ordemServico.flgMaterials){
            btnMateriais.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    id_check, // Drawable right
                    null // Drawable bottom
            );
        }else{
            btnMateriais.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    null, // Drawable right
                    null // Drawable bottom
            );
        }
    }

    private void verificarFotosDepois(){
        //ordemServico.flgFotoDepois = ordemServico.fotoDepois.size()>0;
        //btnFoto.end
        ordemServico.flgFotoDepois = ordemServico.fotoDepois.size()>0;


        Drawable id_check = ContextCompat.getDrawable(
                OrdemServicoExecucaoActivity.this,
                R.mipmap.ic_check
        );

        if (ordemServico.flgFotoDepois){
            btnFotos.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    id_check, // Drawable right
                    null // Drawable bottom
            );
        }else{
            btnFotos.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    null, // Drawable right
                    null // Drawable bottom
            );
        }
    }

    private void atualizarDataInicio(String key, Date dataInicio){
        Map<String, Timestamp> data = new HashMap<>();
        data.put("dt_inicio", new Timestamp(dataInicio));

        db.collection("solicitacao").document(key).set(data, SetOptions.merge());

        /*
        db.collection("solicitacao").document(key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(TAG, "onSuccess");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error writing document", e);
            }
        });
        */
    }
}
