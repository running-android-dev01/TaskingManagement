package com.bebsolutions.taskingmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrdemServicoEncerrarActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoEncerrarActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoEncerrarActivity.class + ".ORDEM_SERVICO";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;


    OrdemServico ordemServico;

    AppCompatTextView txtIdentificacao;
    AppCompatTextView txtRecebido;
    AppCompatTextView txtAbertura;
    AppCompatTextView txtInicioExecucao;
    AppCompatTextView txtEncerramento;
    AppCompatTextView txtUnidade;
    AppCompatTextView txtLocal;
    AppCompatTextView txtSolicitante;
    AppCompatTextView txtOperador;
    AppCompatTextView txtDescricaoServicoExecutado;
    AppCompatButton btnCorrigir;
    AppCompatButton btnTransmite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_encerrar);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        txtIdentificacao = findViewById(R.id.txtIdentificacao);
        txtRecebido = findViewById(R.id.txtRecebido);
        txtAbertura = findViewById(R.id.txtAbertura);
        txtInicioExecucao = findViewById(R.id.txtInicioExecucao);
        txtEncerramento = findViewById(R.id.txtEncerramento);
        txtUnidade = findViewById(R.id.txtUnidade);
        txtLocal = findViewById(R.id.txtLocal);
        txtSolicitante = findViewById(R.id.txtSolicitante);
        txtOperador = findViewById(R.id.txtOperador);
        txtDescricaoServicoExecutado = findViewById(R.id.txtDescricaoServicoExecutado);
        btnCorrigir = findViewById(R.id.btnCorrigir);
        btnTransmite = findViewById(R.id.btnTransmite);

        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        txtIdentificacao.setText(ordemServico.identificacao);
        txtRecebido.setText(sfd.format(ordemServico.dataRecepcao));
        txtAbertura.setText(sfd.format(ordemServico.dataAbertura));
        txtInicioExecucao.setText(sfd.format(ordemServico.dataInicio));
        txtEncerramento.setText(sfd.format(ordemServico.dataTermino));
        txtUnidade.setText(ordemServico.unidade);
        txtLocal.setText(ordemServico.local);
        txtSolicitante.setText(ordemServico.solicitante);
        txtOperador.setText(ordemServico.operador);
        txtDescricaoServicoExecutado.setText(ordemServico.descricaoOperador);

        btnCorrigir.setOnClickListener((v) -> finish());
        btnTransmite.setOnClickListener((v) -> {
            ordemServico.flgTransmitida = true;

            Map<String, Object> data = new HashMap<>();
            data.put("flg_transmitir", "S");
            ordemServico.idSituacao = 5;
            data.put("idSituacao", 5);
            data.put("dt_transmissao", new Timestamp(new Date()));

            db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge());

            Intent intent = new Intent(OrdemServicoEncerrarActivity.this, ListOrdemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            //Map<String, Object> data = new HashMap<>();


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

                    Map<String, Object> data = new HashMap<>();
                    ordemServico.idSituacao = 5;
                    data.put("idSituacao", 5);
                    data.put("dt_transmissao", new Timestamp(new Date()));

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
                            Log.w(TAG, "onComplete");
                            Intent intent = new Intent(OrdemServicoEncerrarActivity.this, ListOrdemActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                }
            });
            */
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
}
