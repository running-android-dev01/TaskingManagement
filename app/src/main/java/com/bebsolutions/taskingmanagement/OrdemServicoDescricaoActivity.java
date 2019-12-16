package com.bebsolutions.taskingmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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

import java.util.HashMap;
import java.util.Map;

public class OrdemServicoDescricaoActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoDescricaoActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoDescricaoActivity.class + ".ORDEM_SERVICO";

    private OrdemServico ordemServico;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private AppCompatEditText edtDescricaoFim;
    private AppCompatButton btnConfirmar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_descricao);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        edtDescricaoFim = findViewById(R.id.edtDescricaoFim);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarDescricao();
            }
        });


        edtDescricaoFim.setText(ordemServico.descricaoOperador);

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

    private void atualizarDescricao(){
        final ProgressoDialog progressoDialog = ProgressoDialog.newInstance("Por favor aguarde");
        progressoDialog.show(getSupportFragmentManager());

        ordemServico.flgDescricao = true;
        ordemServico.descricaoOperador = edtDescricaoFim.getText().toString();

        Map<String, String> data = new HashMap<>();
        data.put("descricao_realizado", edtDescricaoFim.getText().toString());

        db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge());

        Intent i = getIntent();
        i.putExtra(OrdemServicoExecucaoActivity.CT_ORDEM_SERVICO, ordemServico);
        setResult(RESULT_OK, i);
        finish();

        /*db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                progressoDialog.dismiss();

                Intent i = getIntent();
                i.putExtra(OrdemServicoExecucaoActivity.CT_ORDEM_SERVICO, ordemServico);
                setResult(RESULT_OK, i);
                finish();
            }
        });*/
    }
}
