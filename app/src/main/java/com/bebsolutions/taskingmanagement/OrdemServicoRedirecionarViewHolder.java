package com.bebsolutions.taskingmanagement;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class OrdemServicoRedirecionarViewHolder extends RecyclerView.ViewHolder {
    public AppCompatTextView txtNomeOperador;

    OrdemServicoRedirecionarViewHolder(View itemView) {
        super(itemView);

        this.txtNomeOperador = itemView.findViewById(R.id.txtNomeOperador);
    }


}
