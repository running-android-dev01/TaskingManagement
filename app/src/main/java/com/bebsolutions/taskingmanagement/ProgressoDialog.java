package com.bebsolutions.taskingmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class ProgressoDialog extends DialogFragment {
    private static String TAG = ProgressoDialog.class.getName();
    private static String CT_MENSAGEM = TAG + ".MENSAGEM";
    private String mensagem;


    public static ProgressoDialog newInstance(String mensagem){
        ProgressoDialog f = new ProgressoDialog();

        Bundle args = new Bundle();
        args.putString(CT_MENSAGEM, mensagem);
        f.setArguments(args);

        return f;
    }

    public void show(FragmentManager manager) {
        show(manager, "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mensagem = getArguments().getString(CT_MENSAGEM, "");
        setShowsDialog(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View parentView = inflater.inflate(R.layout.dialog_progress, container);

        AppCompatTextView txtMensagem = parentView.findViewById(R.id.txtMensagem);
        txtMensagem.setText(this.mensagem);
        this.setCancelable(false);

        return parentView;
    }
}
