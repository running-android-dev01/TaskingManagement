package com.bebsolutions.taskingmanagement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.bebsolutions.taskingmanagement.model.FotoAntes;
import com.bebsolutions.taskingmanagement.model.OrdemServico;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdemServicoDescricaoFotoActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoDescricaoFotoActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoDescricaoFotoActivity.class + ".ORDEM_SERVICO";
    public static String CT_CAMINHO = OrdemServicoDescricaoFotoActivity.class + ".CAMINHO";
    public static String CT_NOME = OrdemServicoDescricaoFotoActivity.class + ".NOME";

    private OrdemServico ordemServico;

    private String mCurrentPhotoPath;
    private String imageFileName;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;

    private AppCompatEditText edtDescricaoFim;
    private AppCompatButton btnConfirmar;

    private ProgressoDialog progressoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_descricao_foto);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);
        mCurrentPhotoPath = getIntent().getStringExtra(CT_CAMINHO);
        imageFileName = getIntent().getStringExtra(CT_NOME);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        edtDescricaoFim = findViewById(R.id.edtDescricaoFim);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPic();
            }
        });

        edtDescricaoFim.setText("");

        setUpToolbar();
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

    private void setPic() {
        try{
            progressoDialog = ProgressoDialog.newInstance("Por favor aguarde");
            progressoDialog.show(getSupportFragmentManager());

            ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
            String orientacao = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int codigoOrientacao = Integer.parseInt(orientacao);

            // Get the dimensions of the View
            //ExifInterface exif = new ExifInterface(mCurrentPhotoPath);

            int targetW = 2560;
            int targetH = 1440;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);


            Matrix matrix = new Matrix();
            switch (codigoOrientacao) {
                case ExifInterface.ORIENTATION_NORMAL:
                    matrix.postRotate(0);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            StorageReference storageRef = storage.getReference();
            StorageReference fotoImageRef = storageRef.child("images/" + ordemServico.key + "/" + imageFileName + ".jpg");

            UploadTask uploadTask = fotoImageRef.putBytes(byteArray);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot snapshot) {
                    //System.out.println(snapshot.getBytesTransferred().toString());
                    System.out.println(Long.toString(snapshot.getBytesTransferred()));
                }
            });

            FotoAntes fotoAntes = new FotoAntes();
            fotoAntes.nome = imageFileName + ".jpg";
            fotoAntes.foto = null;
            fotoAntes.caminho = fotoImageRef.getPath();
            fotoAntes.descricao = edtDescricaoFim.getText().toString();

            ordemServico.fotoAntes.add(fotoAntes);

            atualizarFotoAntes(ordemServico.key, ordemServico.fotoAntes);

            File[] storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
            for (File f: storageDir) {
                f.delete();
            }


            ordemServico.flgFotoAntes = true;

            /*uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    progressoDialog.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FotoAntes fotoAntes = new FotoAntes();
                    fotoAntes.nome = imageFileName + ".jpg";
                    fotoAntes.foto = null;
                    fotoAntes.caminho = fotoImageRef.getPath();
                    fotoAntes.descricao = edtDescricaoFim.getText().toString();

                    ordemServico.fotoAntes.add(fotoAntes);

                    atualizarFotoAntes(ordemServico.key, ordemServico.fotoAntes);

                    File[] storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
                    for (File f: storageDir) {
                        f.delete();
                    }


                    ordemServico.flgFotoAntes = true;
                }
            });*/


        }catch (Exception ex){
            Log.e(TAG, "ERRO = ", ex);
        }
    }

    private void atualizarFotoAntes(String key, List<FotoAntes> lFotoAntes){
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i<lFotoAntes.size(); i++){
            lFotoAntes.get(i).ordemFoto = (i+1);
        }
        data.put("foto_antes", lFotoAntes);

        db.collection("solicitacao").document(key).set(data, SetOptions.merge());

        progressoDialog.dismiss();

        ordemServico.flgFotoAntes = true;
        Intent i = getIntent();
        i.putExtra(OrdemServicoAberturaActivity.CT_ORDEM_SERVICO, ordemServico);
        setResult(RESULT_OK, i);
        finish();

        /*
        db.collection("solicitacao").document(key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(TAG, "onSuccess");
                progressoDialog.dismiss();

                ordemServico.flgFotoAntes = true;
                Intent i = getIntent();
                i.putExtra(OrdemServicoAberturaActivity.CT_ORDEM_SERVICO, ordemServico);
                setResult(RESULT_OK, i);
                finish();

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

