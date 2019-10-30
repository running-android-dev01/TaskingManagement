package com.bebsolutions.taskingmanagement.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FotoAntes implements Parcelable {
    public String foto;
    public String nome;
    public String caminho;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(foto);
        parcel.writeString(nome);
        parcel.writeString(caminho);
    }

    public static final Parcelable.Creator<FotoAntes> CREATOR
            = new Parcelable.Creator<FotoAntes>() {
        public FotoAntes createFromParcel(Parcel in) {
            return new FotoAntes(in);
        }

        public FotoAntes[] newArray(int size) {
            return new FotoAntes[size];
        }
    };

    public FotoAntes(){

    }

    public FotoAntes(Parcel parcel){
        foto = parcel.readString();
        nome = parcel.readString();
        caminho = parcel.readString();
    }
}
