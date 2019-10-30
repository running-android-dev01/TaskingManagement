package com.bebsolutions.taskingmanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bebsolutions.taskingmanagement.model.FotoDepois;
import com.bebsolutions.taskingmanagement.model.Material;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrdemServicoMaterialActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoMaterialActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoMaterialActivity.class + ".ORDEM_SERVICO";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;

    private OrdemServicoMaterialAdapter adapter;

    private RecyclerView rcwLista;
    AppCompatButton btnOk;

    OrdemServico ordemServico;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_material);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        rcwLista = findViewById(R.id.rcwLista);
        btnOk = findViewById(R.id.btnOk);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        setupRecycler();
        setUpToolbar();

        btnOk.setOnClickListener((v) -> {
            atualizarMaterial(ordemServico.key, ordemServico.materials);
        });
    }


    private void atualizarMaterial(String key, List<Material> lMaterial){
        Map<String, List<Material>> data = new HashMap<>();
        data.put("material", lMaterial);
        ordemServico.flgMaterials = true;
        for (Material material : ordemServico.materials) {
            if (ordemServico.flgMaterials){
                ordemServico.flgMaterials = !TextUtils.isEmpty(material.quantidade);
            }
        }


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
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent i = getIntent();
                i.putExtra(OrdemServicoExecucaoActivity.CT_ORDEM_SERVICO, ordemServico);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    private void setupRecycler() {
        Context ctx = this;

        adapter = new OrdemServicoMaterialAdapter(ctx);
        rcwLista.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcwLista.setLayoutManager(layoutManager);
        rcwLista.setAdapter(adapter);

        adapter.atualizarLista(ordemServico.materials);
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
    }

}
