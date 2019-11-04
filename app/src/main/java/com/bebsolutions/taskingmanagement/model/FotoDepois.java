package com.bebsolutions.taskingmanagement.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FotoDepois implements Parcelable {
    public String foto;
    public String nome;
    public String caminho;
    public String descricao;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(foto);
        parcel.writeString(nome);
        parcel.writeString(caminho);
        parcel.writeString(descricao);
    }

    public static final Parcelable.Creator<FotoDepois> CREATOR
            = new Parcelable.Creator<FotoDepois>() {
        public FotoDepois createFromParcel(Parcel in) {
            return new FotoDepois(in);
        }

        public FotoDepois[] newArray(int size) {
            return new FotoDepois[size];
        }
    };

    public FotoDepois(){

    }

    public FotoDepois(Parcel parcel){
        foto = parcel.readString();
        nome = parcel.readString();
        caminho = parcel.readString();
        descricao = parcel.readString();
    }
}
