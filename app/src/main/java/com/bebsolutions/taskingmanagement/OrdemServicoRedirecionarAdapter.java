package com.bebsolutions.taskingmanagement;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bebsolutions.taskingmanagement.model.Operador;
import com.bebsolutions.taskingmanagement.model.OrdemServico;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdemServicoRedirecionarAdapter extends RecyclerView.Adapter<OrdemServicoRedirecionarViewHolder> {
    private static String TAG = OrdemServicoRedirecionarAdapter.class.getName();
    private final Context context;
    private OrdemServico ordemServico;
    private FirebaseFirestore db;
    private List<Operador> lOperador;


    OrdemServicoRedirecionarAdapter(Context context, OrdemServico ordemServico, FirebaseFirestore db) {
        this.context = context;
        this.ordemServico = ordemServico;
        this.db = db;
        lOperador = new ArrayList<>();
    }

    public void atualizarLista(List<Operador> lMaterial) {
        this.lOperador = lMaterial;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public OrdemServicoRedirecionarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_ordem_servico_redirecionar, parent, false);
        return new OrdemServicoRedirecionarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdemServicoRedirecionarViewHolder viewHolder, int position) {
        final Operador operador = lOperador.get(position);

        viewHolder.txtNomeOperador.setText(operador.nome);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final ProgressoDialog progressoDialog = ProgressoDialog.newInstance("Por favor aguarde");
                progressoDialog.show(((AppCompatActivity)context).getSupportFragmentManager());

                Map<String, Object> data = new HashMap<>();
                data.put("idSituacao", 4);
                data.put("nome_operador", operador.nome);
                data.put("idOperador", operador.telefone);
                data.put("telefone_operador", operador.telefone);

                //data.put("foto_antes", null);

                db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge());

                Intent intent = new Intent(context, ListOrdemActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                Log.w(TAG, "onSuccess");

                /*db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressoDialog.dismiss();

                        Intent intent = new Intent(context, ListOrdemActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        Log.w(TAG, "onSuccess");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });*/

            }
        });
    }

    @Override
    public int getItemCount() {
        return lOperador != null ? lOperador.size() : 0;
    }
}
