package com.bebsolutions.taskingmanagement;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bebsolutions.taskingmanagement.model.OrdemServico;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ListOrdemAdapter extends RecyclerView.Adapter<ListOrdemViewHolder> {
    private List<OrdemServico> lOrdemServico;
    private final Context context;
    private Date dataAtual;

    ListOrdemAdapter(Context context){
        this.context = context;
    }

    void atualizarLista(List<OrdemServico> lOrdemServico){
        this.lOrdemServico = lOrdemServico;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListOrdemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListOrdemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_list_ordem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListOrdemViewHolder holder, int position) {
        final OrdemServico ordemServico = lOrdemServico.get(position);


        holder.txtContrato.setText(ordemServico.identificacao);
        holder.imgRecebida.setVisibility(View.GONE);
        holder.imgAvalida.setVisibility(View.GONE);
        holder.imgAguardandoExecucao.setVisibility(View.GONE);
        holder.imgAguardandoTransmissao.setVisibility(View.GONE);
        holder.imgDisponivel.setVisibility(View.GONE);
        holder.imgUrgencia.setVisibility(View.GONE);

        if (ordemServico.flgTermino && !ordemServico.flgTransmitida){
            holder.imgDisponivel.setVisibility(View.VISIBLE);
        }

        if (ordemServico.flgUrgencia){
            holder.imgUrgencia.setVisibility(View.VISIBLE);
        }

        if (ordemServico.dataRecepcao!=null && ordemServico.idSituacao!=3){
            holder.imgRecebida.setVisibility(View.VISIBLE);
        }
        if (ordemServico.dataAbertura!=null && ordemServico.idSituacao!=3){
            holder.imgAvalida.setVisibility(View.VISIBLE);
        }
        if (ordemServico.dataInicio!=null && ordemServico.idSituacao!=3){
            holder.imgAguardandoExecucao.setVisibility(View.VISIBLE);
        }


        if (ordemServico.dataTermino!=null && ordemServico.idSituacao!=3){
            holder.imgAguardandoTransmissao.setVisibility(View.VISIBLE);
        }
        if (ordemServico.dataTermino!=null && !ordemServico.flgTransmitida && ordemServico.idSituacao!=3){
            holder.imgAguardandoTransmissao.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener((v) -> {
            if (ordemServico.idSituacao==3){
                Toast.makeText(context, "Ordem já transmitida!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, OrdemServicoAberturaActivity.class);
            intent.putExtra(OrdemServicoAberturaActivity.CT_ORDEM_SERVICO, ordemServico);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lOrdemServico != null ? lOrdemServico.size() : 0;
    }
}
