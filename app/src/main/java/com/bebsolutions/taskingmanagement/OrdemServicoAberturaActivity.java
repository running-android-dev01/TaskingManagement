package com.bebsolutions.taskingmanagement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bebsolutions.taskingmanagement.model.FotoAntes;
import com.bebsolutions.taskingmanagement.model.OrdemServico;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdemServicoAberturaActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoAberturaActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoAberturaActivity.class + ".ORDEM_SERVICO";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_TAKE_ORDEM = 2;

    private String mCurrentPhotoPath;
    private String imageFileName;

    OrdemServico ordemServico;

    AppCompatTextView txtIdentificacao;
    AppCompatTextView txtEnviado;
    AppCompatTextView txtRecebido;
    AppCompatTextView txtAbertura;
    AppCompatTextView txtUnidade;
    AppCompatTextView txtLocal;
    AppCompatTextView txtSolicitante;
    AppCompatTextView txtDescricaoServico;

    AppCompatButton btnPrevisao;
    AppCompatButton btnFoto;
    AppCompatButton btnExecutar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_abertura);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        txtIdentificacao = findViewById(R.id.txtIdentificacao);
        txtEnviado = findViewById(R.id.txtEnviado);
        txtRecebido = findViewById(R.id.txtRecebido);
        txtAbertura = findViewById(R.id.txtAbertura);
        txtUnidade = findViewById(R.id.txtUnidade);
        txtLocal = findViewById(R.id.txtLocal);
        txtSolicitante = findViewById(R.id.txtSolicitante);
        txtDescricaoServico = findViewById(R.id.txtDescricaoServico);

        btnPrevisao = findViewById(R.id.btnPrevisao);
        btnFoto = findViewById(R.id.btnFoto);
        btnExecutar = findViewById(R.id.btnExecutar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (ordemServico.dataAbertura==null){
            ordemServico.dataAbertura = Timestamp.now().toDate();
            atualizarDataAbertura(ordemServico.key, ordemServico.dataAbertura);
        }


        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        txtIdentificacao.setText(ordemServico.identificacao);
        txtEnviado.setText(ordemServico.dataCentralizador);
        txtRecebido.setText(sfd.format(ordemServico.dataRecepcao));
        txtAbertura.setText(sfd.format(ordemServico.dataAbertura));
        txtUnidade.setText(ordemServico.unidade);
        txtLocal.setText(ordemServico.local);
        txtSolicitante.setText(ordemServico.solicitante);
        txtDescricaoServico.setText(ordemServico.descricaoSolicitante);

        btnPrevisao.setOnClickListener((v)-> {
            Intent intent = new Intent(OrdemServicoAberturaActivity.this, OrdemServicoPlanejamentoActivity.class);
            intent.putExtra(OrdemServicoPlanejamentoActivity.CT_ORDEM_SERVICO, ordemServico);
            startActivityForResult(intent, REQUEST_TAKE_ORDEM);
        });

        btnFoto.setOnClickListener((v)-> {
            tirarFotoAntes();
        });


        btnExecutar.setOnClickListener((v)-> {
            if (ordemServico.flgFotoAntes){
                Intent intent = new Intent(OrdemServicoAberturaActivity.this, OrdemServicoExecucaoActivity.class);
                intent.putExtra(OrdemServicoExecucaoActivity.CT_ORDEM_SERVICO, ordemServico);
                startActivity(intent);
            }else{
                Toast.makeText(OrdemServicoAberturaActivity.this, "Tirar a foto antes de iniciar a execução!", Toast.LENGTH_SHORT).show();
            }

        });


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

    @Override
    protected void onResume() {
        super.onResume();
        verificarFotoAntes();
        verificarPrevisaoAntes();
    }

    private void verificarFotoAntes(){
        //btnFoto.end
        Drawable id_check = ContextCompat.getDrawable(
                OrdemServicoAberturaActivity.this,
                R.mipmap.ic_check
        );

        if (ordemServico.flgFotoAntes){
            btnFoto.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    id_check, // Drawable right
                    null // Drawable bottom
            );
        }else{
            btnFoto.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                     null, // Drawable right
                    null // Drawable bottom
            );
        }
    }


    private void verificarPrevisaoAntes(){
        //btnFoto.end
        Drawable id_check = ContextCompat.getDrawable(
                OrdemServicoAberturaActivity.this,
                R.mipmap.ic_check
        );

        if (ordemServico.dataPrevisaoFim!=null && ordemServico.dataPrevisaoInicio!=null){
            btnPrevisao.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    id_check, // Drawable right
                    null // Drawable bottom
            );
        }else{
            btnPrevisao.setCompoundDrawablesWithIntrinsicBounds(
                    null, // Drawable left
                    null, // Drawable top
                    null, // Drawable right
                    null // Drawable bottom
            );
        }
    }



    private void tirarFotoAntes(){
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

    private void atualizarDataAbertura(String key, Date dataAbertura){
        Map<String, Timestamp> data = new HashMap<>();
        data.put("dt_abertura", new Timestamp(dataAbertura));
        //data.put("foto_antes", null);

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                setPic();
                ordemServico.flgFotoAntes = true;
                verificarFotoAntes();
            }
            if (requestCode == REQUEST_TAKE_ORDEM && resultCode == RESULT_OK) {
                //setPic();
                ordemServico = data.getParcelableExtra(CT_ORDEM_SERVICO);
                verificarPrevisaoAntes();
            }


        } catch (Exception e) {
            Log.e(TAG, "ERRO = ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setPic() {
        try{
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
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FotoAntes fotoAntes = new FotoAntes();
                    fotoAntes.nome = imageFileName + ".jpg";
                    fotoAntes.foto = null;
                    fotoAntes.caminho = fotoImageRef.getPath();

                    ordemServico.fotoAntes.add(fotoAntes);

                    atualizarFotoAntes(ordemServico.key, ordemServico.fotoAntes);

                    File[] storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
                    for (File f: storageDir) {
                        f.delete();
                    }


                    ordemServico.flgFotoAntes = true;
                    verificarFotoAntes();
                }
            });


        }catch (Exception ex){
            Log.e(TAG, "ERRO = ", ex);
        }
    }


    private void atualizarFotoAntes(String key, List<FotoAntes> lFotoAntes){
        Map<String, List<FotoAntes>> data = new HashMap<>();
        data.put("foto_antes", lFotoAntes);


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
    }


}