package com.bebsolutions.taskingmanagement;

import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class OrdemServicoMaterialViewHolder extends RecyclerView.ViewHolder {
    public final AppCompatTextView txtMaterial;
    public final AppCompatTextView txtUnidade;
    public final AppCompatEditText edtQuantidade;
    public OrdemServicoMaterialAdapter.OrdemServicoMaterialListener ordemServicoMaterialListener;

    OrdemServicoMaterialViewHolder(View itemView, OrdemServicoMaterialAdapter.OrdemServicoMaterialListener ordemServicoMaterialListener) {
        super(itemView);

        this.txtMaterial = itemView.findViewById(R.id.txtMaterial);
        this.txtUnidade = itemView.findViewById(R.id.txtUnidade);
        this.edtQuantidade = itemView.findViewById(R.id.edtQuantidade);
        this.ordemServicoMaterialListener = ordemServicoMaterialListener;
        this.edtQuantidade.addTextChangedListener(this.ordemServicoMaterialListener);
    }
}
