package com.bebsolutions.taskingmanagement.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdemServico implements Parcelable {
    public String key;
    public String uid;
    public String identificacao;
    public String dataCentralizador;
    public Date dataRecepcao;
    public Date dataAbertura;
    public Date dataInicio;
    public Date dataTermino;
    public Date dataPrevisaoInicio;
    public Date dataPrevisaoFim;
    public String unidade;
    public String local;
    public String solicitante;
    public String descricaoSolicitante;
    public String operadorUid;
    public String operador;
    public String descricaoOperador;
    public Boolean flgTransmitida;
    public Boolean flgTermino;
    public Boolean flgFotoAntes;
    public Boolean flgDescricao;
    public Boolean flgFotoDepois;
    public Boolean flgMaterials;
    public Boolean flgUrgencia;
    public Boolean flgPrevisao;
    public int idSituacao;
    public List<FotoAntes> fotoAntes;
    public List<FotoDepois> fotoDepois;
    public List<Material> materials;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(uid);
        parcel.writeString(identificacao);
        parcel.writeString(dataCentralizador);
        parcel.writeSerializable(dataRecepcao);
        parcel.writeSerializable(dataAbertura);
        parcel.writeSerializable(dataInicio);
        parcel.writeSerializable(dataTermino);
        parcel.writeSerializable(dataPrevisaoInicio);
        parcel.writeSerializable(dataPrevisaoFim);
        parcel.writeString(unidade);
        parcel.writeString(local);
        parcel.writeString(solicitante);
        parcel.writeString(descricaoSolicitante);
        parcel.writeString(operadorUid);
        parcel.writeString(operador);
        parcel.writeString(descricaoOperador);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(flgTransmitida);
            parcel.writeBoolean(flgTermino);
            parcel.writeBoolean(flgFotoAntes);
            parcel.writeBoolean(flgDescricao);
            parcel.writeBoolean(flgFotoDepois);
            parcel.writeBoolean(flgMaterials);
            parcel.writeBoolean(flgUrgencia);
            parcel.writeBoolean(flgPrevisao);
        }else{
            parcel.writeInt(flgTransmitida?1:0);
            parcel.writeInt(flgTermino?1:0);
            parcel.writeInt(flgFotoAntes?1:0);
            parcel.writeInt(flgDescricao?1:0);
            parcel.writeInt(flgFotoDepois?1:0);
            parcel.writeInt(flgMaterials?1:0);
            parcel.writeInt(flgUrgencia?1:0);
            parcel.writeInt(flgPrevisao?1:0);
        }

        parcel.writeList(fotoAntes);
        parcel.writeList(fotoDepois);
        parcel.writeList(materials);
    }

    public static final Parcelable.Creator<OrdemServico> CREATOR
            = new Parcelable.Creator<OrdemServico>() {
        public OrdemServico createFromParcel(Parcel in) {
            return new OrdemServico(in);
        }

        public OrdemServico[] newArray(int size) {
            return new OrdemServico[size];
        }
    };

    public OrdemServico(){

    }

    public OrdemServico(Parcel parcel){
        key = parcel.readString();
        uid = parcel.readString();
        identificacao = parcel.readString();
        dataCentralizador = parcel.readString();
        dataRecepcao = (Date)parcel.readSerializable();
        dataAbertura = (Date)parcel.readSerializable();
        dataInicio = (Date)parcel.readSerializable();
        dataTermino = (Date)parcel.readSerializable();
        dataPrevisaoInicio = (Date)parcel.readSerializable();
        dataPrevisaoFim = (Date)parcel.readSerializable();
        unidade = parcel.readString();
        local = parcel.readString();
        solicitante = parcel.readString();
        descricaoSolicitante = parcel.readString();
        operadorUid = parcel.readString();
        operador = parcel.readString();
        descricaoOperador = parcel.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            flgTransmitida = parcel.readBoolean();
            flgTermino = parcel.readBoolean();
            flgFotoAntes = parcel.readBoolean();
            flgDescricao = parcel.readBoolean();
            flgFotoDepois = parcel.readBoolean();
            flgMaterials = parcel.readBoolean();
            flgUrgencia = parcel.readBoolean();
            flgPrevisao = parcel.readBoolean();
        }else{
            flgTransmitida = parcel.readInt()==1;
            flgTermino = parcel.readInt()==1;
            flgFotoAntes = parcel.readInt()==1;
            flgDescricao = parcel.readInt()==1;
            flgFotoDepois = parcel.readInt()==1;
            flgMaterials = parcel.readInt()==1;
            flgUrgencia = parcel.readInt()==1;
            flgPrevisao = parcel.readInt()==1;
        }

        fotoAntes = new ArrayList<>();
        fotoDepois = new ArrayList<>();
        materials = new ArrayList<>();
        parcel.readList(fotoAntes, FotoAntes.class.getClassLoader());
        parcel.readList(fotoDepois, FotoDepois.class.getClassLoader());
        parcel.readList(materials, Material.class.getClassLoader());
    }

}
