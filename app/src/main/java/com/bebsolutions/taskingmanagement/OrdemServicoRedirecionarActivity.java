package com.bebsolutions.taskingmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bebsolutions.taskingmanagement.model.Operador;
import com.bebsolutions.taskingmanagement.model.OrdemServico;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdemServicoRedirecionarActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static String TAG = OrdemServicoRedirecionarActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoRedirecionarActivity.class + ".ORDEM_SERVICO";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private OrdemServicoRedirecionarAdapter adapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rcwLista;

    OrdemServico ordemServico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_redirecionar);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        rcwLista = findViewById(R.id.rcwLista);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        setupRecycler();
        setUpToolbar();

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primaryColor,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    @Override
    public void onRefresh() {
        carregarLista();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    private void setupRecycler() {
        Context ctx = this;

        adapter = new OrdemServicoRedirecionarAdapter(ctx, ordemServico, db);
        rcwLista.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcwLista.setLayoutManager(layoutManager);
        rcwLista.setAdapter(adapter);

        //adapter.atualizarLista(ordemServico.materials);
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


    private void carregarLista(){
        mSwipeRefreshLayout.setRefreshing(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        CollectionReference usuariosRef = db.collection("usuarios");
        Query query = usuariosRef.whereEqualTo("tipo", 4).orderBy("nome");


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete");
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<Operador> lOperador = new ArrayList<>();
                    if (querySnapshot.size()>0){
                        for (DocumentSnapshot document : task.getResult()) {

                            Operador operador = new Operador();

                            Map<String, Object> data = document.getData();

                            operador.email = getString(data, "email");
                            operador.flg_ativo = getString(data, "flg_ativo");
                            operador.nome = getString(data, "nome");
                            operador.senha = getString(data, "senha");
                            operador.telefone = getString(data, "telefone");
                            operador.tipo = Integer.parseInt(getString(data, "tipo"));
                            operador.uid = getString(data, "uid");

                            if (operador.flg_ativo.equals("S")){
                                lOperador.add(operador);
                            }
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    }
                    adapter.atualizarLista(lOperador);
                } else {
                    Log.e(TAG, "Error getting documents."+ task.getException());
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private String getString(Map<String, Object> data, String key){
        return data.containsKey(key)?((data.get(key)!=null)?data.get(key).toString():""):"";
    }
}

