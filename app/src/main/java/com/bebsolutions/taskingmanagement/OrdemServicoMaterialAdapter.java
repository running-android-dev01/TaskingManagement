package com.bebsolutions.taskingmanagement;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bebsolutions.taskingmanagement.model.Material;

import java.util.ArrayList;
import java.util.List;

public class OrdemServicoMaterialAdapter extends RecyclerView.Adapter<OrdemServicoMaterialViewHolder> {
    private final Context context;
    private List<Material> lMaterial;


    OrdemServicoMaterialAdapter(Context context) {
        this.context = context;
        lMaterial = new ArrayList<>();
    }

    public void atualizarLista(List<Material> lMaterial) {
        this.lMaterial = lMaterial;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public OrdemServicoMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_ordem_servico_material, parent, false);
        return new OrdemServicoMaterialViewHolder(view, new OrdemServicoMaterialListener());
    }

    @Override
    public void onBindViewHolder(@NonNull OrdemServicoMaterialViewHolder viewHolder, int position) {
        final Material material = lMaterial.get(position);

        viewHolder.txtMaterial.setText(material.descricao);
        viewHolder.txtUnidade.setText(material.unidade);
        viewHolder.ordemServicoMaterialListener.updatePosition(position);
        viewHolder.edtQuantidade.setText(material.quantidade);
    }

    @Override
    public int getItemCount() {
        return lMaterial != null ? lMaterial.size() : 0;
    }


    public class OrdemServicoMaterialListener implements TextWatcher {
        private int position;

        private void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            lMaterial.get(position).quantidade = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
