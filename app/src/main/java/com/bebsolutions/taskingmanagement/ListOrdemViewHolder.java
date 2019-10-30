package com.bebsolutions.taskingmanagement;

import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class ListOrdemViewHolder extends RecyclerView.ViewHolder {

    final AppCompatTextView txtContrato;
    final AppCompatImageView imgRecebida;
    final AppCompatImageView imgAvalida;
    final AppCompatImageView imgAguardandoExecucao;
    final AppCompatImageView imgAguardandoTransmissao;
    final AppCompatImageView imgDisponivel;
    final AppCompatImageView imgUrgencia;

    ListOrdemViewHolder(View itemView) {
        super(itemView);

        txtContrato = itemView.findViewById(R.id.txtContrato);
        imgRecebida = itemView.findViewById(R.id.imgRecebida);
        imgAvalida = itemView.findViewById(R.id.imgAvalida);
        imgAguardandoExecucao = itemView.findViewById(R.id.imgAguardandoExecucao);
        imgAguardandoTransmissao = itemView.findViewById(R.id.imgAguardandoTransmissao);
        imgDisponivel = itemView.findViewById(R.id.imgDisponivel);
        imgUrgencia = itemView.findViewById(R.id.imgUrgencia);
    }
}
