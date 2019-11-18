package com.bebsolutions.taskingmanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bebsolutions.taskingmanagement.model.Material;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opencensus.internal.StringUtils;

public class OrdemServicoMaterialAdicionarActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoMaterialAdicionarActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoMaterialAdicionarActivity.class + ".ORDEM_SERVICO";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    OrdemServico ordemServico;

    private AppCompatEditText edtMaterial;
    private AppCompatSpinner spnUnidade;
    private AppCompatButton btnAdicionar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_material_adicionar);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        edtMaterial = findViewById(R.id.edtMaterial);
        spnUnidade = findViewById(R.id.spnUnidade);
        btnAdicionar = findViewById(R.id.btnAdicionar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.unidades_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnUnidade.setAdapter(adapter);

        setUpToolbar();

        btnAdicionar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtMaterial.getText())){
                    edtMaterial.setError("Informar o material!");
                    return;
                }

                List<Material> m = ordemServico.materials;

                Material material = new Material();
                material.descricao = edtMaterial.getText().toString();
                material.unidade = spnUnidade.getSelectedItem().toString();
                material.quantidade = "";

                ordemServico.materials.add(material);

                final ProgressoDialog progressoDialog = ProgressoDialog.newInstance("Por favor aguarde");
                progressoDialog.show(getSupportFragmentManager());

                Map<String, List<Material>> data = new HashMap<>();
                data.put("material", ordemServico.materials);

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
                        progressoDialog.dismiss();

                        Intent i = getIntent();
                        i.putExtra(OrdemServicoMaterialActivity.CT_ORDEM_SERVICO, ordemServico);
                        setResult(RESULT_OK, i);
                        finish();
                    }
                });

            }
        });
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
