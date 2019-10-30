package com.bebsolutions.taskingmanagement.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Material implements Parcelable {
    public String uid;
    public String descricao;
    public String unidade;
    public String quantidade;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(descricao);
        parcel.writeString(unidade);
        parcel.writeString(quantidade);
    }

    public static final Parcelable.Creator<Material> CREATOR
            = new Parcelable.Creator<Material>() {
        public Material createFromParcel(Parcel in) {
            return new Material(in);
        }

        public Material[] newArray(int size) {
            return new Material[size];
        }
    };

    public Material(){

    }

    public Material(Parcel parcel){
        uid = parcel.readString();
        descricao = parcel.readString();
        unidade = parcel.readString();
        quantidade = parcel.readString();
    }
}
