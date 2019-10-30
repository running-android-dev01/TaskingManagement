package com.bebsolutions.taskingmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bebsolutions.taskingmanagement.model.FotoAntes;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.model.value.ServerTimestampValue;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListOrdemActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static String TAG = ListOrdemActivity.class.getName();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recycler_view;
    private ListOrdemAdapter adapter;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_ordem);

        recycler_view = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();


        setUpToolbar();
        setupRecycler();

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primaryColor,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        /*mSwipeRefreshLayout.post(() -> {

            mSwipeRefreshLayout.setRefreshing(true);
            carregarListaOrdem();
        });*/
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));

        setSupportActionBar(toolbar);
    }

    private void setupRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layoutManager);

        adapter = new ListOrdemAdapter(this);
        recycler_view.setAdapter(adapter);

        recycler_view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onRefresh() {
        carregarListaOrdem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarListaOrdem();
    }

    private void carregarListaOrdem(){
        mSwipeRefreshLayout.setRefreshing(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Log.d(TAG, pref.getString("TELEFONE", ""));
        String telefone = pref.getString("TELEFONE", "");

        CollectionReference usuariosRef = db.collection("solicitacao");
        Query query = usuariosRef.whereEqualTo("telefone_operador", telefone);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete");
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<OrdemServico> lOrdemServico = new ArrayList<>();
                    if (querySnapshot.size()>0){
                        for (DocumentSnapshot document : task.getResult()) {

                            OrdemServico ordemServico = new OrdemServico();

                            Map<String, Object> data = document.getData();
                            ordemServico.key = document.getId();
                            ordemServico.uid = getString(data, "uid");
                            ordemServico.identificacao = getString(data, "descContrato") + " - " + getString(data, "dt_emissao") + " - OS " + getString(data, "numos");
                            ordemServico.dataCentralizador = getString(data, "dt_emissao");
                            ordemServico.dataRecepcao = getTimeStamp(document, "dt_recepcao");
                            ordemServico.dataAbertura = getTimeStamp(document, "dt_abertura");
                            ordemServico.dataInicio = getTimeStamp(document, "dt_inicio");
                            ordemServico.dataTermino = getTimeStamp(document, "dt_termino");
                            ordemServico.dataPrevisaoInicio = getTimeStamp(document, "dt_previsao_inicio");
                            ordemServico.dataPrevisaoFim = getTimeStamp(document, "dt_previsao_fim");
                            ordemServico.unidade = "";
                            ordemServico.local = getString(data,"local");
                            ordemServico.solicitante = getString(data,"solicitante");
                            ordemServico.descricaoSolicitante = getString(data,"descricao");
                            ordemServico.operadorUid = "";
                            ordemServico.operador = getString(data,"operador");
                            ordemServico.flgUrgencia = getString(data,"flg_prioridade").equals("S");
                            ordemServico.flgPrevisao = getString(data,"flg_previsao").equals("S");
                            String idSituacao = getString(data,"idSituacao");
                            idSituacao = TextUtils.isEmpty(idSituacao)?"0":idSituacao;
                            ordemServico.idSituacao = Integer.parseInt(idSituacao);
                            ordemServico.descricaoOperador = getString(data,"descricao_realizado");
                            ordemServico.flgTransmitida = getString(data,"flg_transmitir").equals("S");
                            ordemServico.flgTermino = false;
                            ordemServico.flgFotoAntes = false;
                            ordemServico.flgDescricao = !TextUtils.isEmpty(ordemServico.descricaoOperador );
                            ordemServico.flgFotoDepois = false;
                            ordemServico.flgMaterials = false;
                            ordemServico.fotoAntes = new ArrayList<>();
                            ordemServico.fotoDepois = new ArrayList<>();
                            ordemServico.materials = new ArrayList<>();


                            if (data.containsKey("foto_antes") && data.get("foto_antes")!=null){
                                List<Map<String, Object>> lFotoAntes = (List<Map<String, Object>>) data.get("foto_antes");
                                for (int i = 0; i<lFotoAntes.size(); i++){
                                    Map<String, Object> f = lFotoAntes.get(i);
                                    FotoAntes fotoAntes = new FotoAntes();
                                    fotoAntes.foto = getString(f, "foto");
                                    fotoAntes.nome = getString(f, "nome");
                                    fotoAntes.caminho = getString(f, "caminho");
                                    ordemServico.fotoAntes.add(fotoAntes);
                                }
                            }
                            ordemServico.flgFotoAntes = ordemServico.fotoAntes.size()>0;


                            if (data.containsKey("fotos") && data.get("fotos")!=null){
                                List<Map<String, Object>> lFotoDepois = (List<Map<String, Object>>) data.get("fotos");
                                for (int i = 0; i<lFotoDepois.size(); i++){
                                    Map<String, Object> f = lFotoDepois.get(i);
                                    FotoDepois fotoDepois = new FotoDepois();
                                    fotoDepois.foto = getString(f, "foto");
                                    fotoDepois.nome = getString(f, "nome");
                                    fotoDepois.caminho = getString(f, "caminho");
                                    ordemServico.fotoDepois.add(fotoDepois);
                                }
                            }
                            ordemServico.flgFotoDepois = ordemServico.fotoDepois.size()>0;

                            ordemServico.flgMaterials = true;
                            if (data.containsKey("material") && data.get("material")!=null){
                                List<Map<String, Object>> lMaterial = (List<Map<String, Object>>) data.get("material");
                                for (int i = 0; i<lMaterial.size(); i++){
                                    Map<String, Object> f = lMaterial.get(i);
                                    Material material = new Material();
                                    material.uid = getString(f, "id");
                                    material.descricao = getString(f, "descricao");
                                    material.unidade = getString(f, "unidade_medida");
                                    material.quantidade = getString(f, "quantidade");

                                    if (ordemServico.flgMaterials){
                                        ordemServico.flgMaterials = !TextUtils.isEmpty(material.quantidade);
                                    }
                                    ordemServico.materials.add(material);
                                }
                            }



                            if (ordemServico.dataRecepcao==null){
                                ordemServico.dataRecepcao = Timestamp.now().toDate();
                                atualizarDataRecebida(document.getId(), ordemServico.dataRecepcao);
                            }

                            /*int x = 0;
                            for (x = 0; x<5 ; x++){
                                Material material = new Material();
                                material.uid = "XXXXX" + i + "XXXXX" + x;
                                material.descricao = "Cimento CP- III" + x;
                                material.unidade = "SC";
                                material.quantidade = Integer.toString(x+1);
                                ordemServico.materials.add(material);
                            }*/

                            lOrdemServico.add(ordemServico);
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    }
                    adapter.atualizarLista(lOrdemServico);
                } else {
                    Log.e(TAG, "Error getting documents."+ task.getException());
                }
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void atualizarDataRecebida(String key, Date dataRecepcao){
        Map<String, Timestamp> data = new HashMap<>();
        data.put("dt_recepcao", new Timestamp(dataRecepcao));

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
                Log.w(TAG, "onComplete");
            }
        });
    }

    private String getString(Map<String, Object> data, String key){
        return data.containsKey(key)?((data.get(key)!=null)?data.get(key).toString():""):"";
    }


    private Date getTimeStamp(DocumentSnapshot document, String key){
        Date data = null;
        if (document.contains(key)){
            data = document.getDate(key);
        }
        return data;
    }
}