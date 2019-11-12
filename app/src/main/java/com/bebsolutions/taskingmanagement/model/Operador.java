package com.bebsolutions.taskingmanagement.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Operador implements Parcelable {
    public String email;
    public String flg_ativo;
    public String nome;
    public String senha;
    public String telefone;
    public int tipo;
    public String uid;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeString(flg_ativo);
        parcel.writeString(nome);
        parcel.writeString(senha);
        parcel.writeString(telefone);
        parcel.writeInt(tipo);
        parcel.writeString(uid);
    }

    public static final Parcelable.Creator<Operador> CREATOR = new Parcelable.Creator<Operador>() {
        public Operador createFromParcel(Parcel in) {
            return new Operador(in);
        }

        public Operador[] newArray(int size) {
            return new Operador[size];
        }
    };

    public Operador(){

    }

    public Operador(Parcel parcel){
        email = parcel.readString();
        flg_ativo = parcel.readString();
        nome = parcel.readString();
        senha = parcel.readString();
        telefone = parcel.readString();
        tipo = parcel.readInt();
        uid = parcel.readString();
    }
}
