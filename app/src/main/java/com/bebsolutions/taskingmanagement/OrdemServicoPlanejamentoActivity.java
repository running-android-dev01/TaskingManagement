package com.bebsolutions.taskingmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;


import com.bebsolutions.taskingmanagement.model.OrdemServico;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrdemServicoPlanejamentoActivity extends AppCompatActivity {
    private static String TAG = OrdemServicoPlanejamentoActivity.class.getName();
    public static String CT_ORDEM_SERVICO = OrdemServicoPlanejamentoActivity.class + ".ORDEM_SERVICO";

    OrdemServico ordemServico;

    AppCompatEditText edtDataInicio;
    AppCompatImageButton btnDataInicio;
    AppCompatEditText edtHoraInicio;
    AppCompatImageButton btnHoraInicio;
    AppCompatEditText edtDataFim;
    AppCompatImageButton btnDataFim;
    AppCompatEditText edtHoraFim;
    AppCompatImageButton btnHoraFim;

    AppCompatButton btnConfirmar;

    private Calendar dataInicio = Calendar.getInstance();
    private Calendar dataFim = Calendar.getInstance();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordem_servico_planejamento);

        ordemServico = getIntent().getParcelableExtra(CT_ORDEM_SERVICO);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        edtDataInicio = findViewById(R.id.edtDataInicio);
        btnDataInicio = findViewById(R.id.btnDataInicio);
        edtHoraInicio = findViewById(R.id.edtHoraInicio);
        btnHoraInicio = findViewById(R.id.btnHoraInicio);
        edtDataFim = findViewById(R.id.edtDataFim);
        btnDataFim = findViewById(R.id.btnDataFim);
        edtHoraFim = findViewById(R.id.edtHoraFim);
        btnHoraFim = findViewById(R.id.btnHoraFim);

        btnConfirmar = findViewById(R.id.btnConfirmar);


        if (ordemServico.dataPrevisaoInicio!=null){
            dataInicio.setTime(ordemServico.dataPrevisaoInicio);
            String sDataInicio = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ordemServico.dataPrevisaoInicio);
            String sHoraInicio = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(ordemServico.dataPrevisaoInicio);
            edtDataInicio.setText(sDataInicio);
            edtHoraInicio.setText(sHoraInicio);

        }
        if (ordemServico.dataPrevisaoFim!=null){
            dataFim.setTime(ordemServico.dataPrevisaoFim);
            String sDataFim = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ordemServico.dataPrevisaoFim);
            String sHoraFim = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(ordemServico.dataPrevisaoFim);
            edtDataFim.setText(sDataFim);
            edtHoraFim.setText(sHoraFim);
        }


        btnDataInicio.setOnClickListener((view -> {
            DialogFragment newFragment = new DatePickerFragment(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    edtDataInicio.setText(day + "/" + (month + 1) + "/" + year);
                    dataInicio.set(year,month, day, dataInicio.get(Calendar.HOUR_OF_DAY), dataInicio.get(Calendar.MINUTE));
                }
            });
            newFragment.show(getSupportFragmentManager(), "datePicker");

        }));
        btnHoraInicio.setOnClickListener((view -> {
            DialogFragment newFragment = new TimePickerFragment(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    edtHoraInicio.setText(hourOfDay + ":" + minute);
                    dataInicio.set(dataInicio.get(Calendar.YEAR), dataInicio.get(Calendar.MONTH), dataInicio.get(Calendar.DATE), hourOfDay, minute);
                }
            });
            newFragment.show(getSupportFragmentManager(), "timePicker");
        }));
        btnDataFim.setOnClickListener((view -> {
            DialogFragment newFragment = new DatePickerFragment(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    edtDataFim.setText(day + "/" + (month + 1) + "/" + year);
                    dataFim.set(year,month, day, dataFim.get(Calendar.HOUR_OF_DAY), dataFim.get(Calendar.MINUTE));
                }
            });
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }));
        btnHoraFim.setOnClickListener((view -> {
            DialogFragment newFragment = new TimePickerFragment(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    edtHoraFim.setText(hourOfDay + ":" + minute);
                    dataFim.set(dataFim.get(Calendar.YEAR), dataFim.get(Calendar.MONTH), dataFim.get(Calendar.DATE), hourOfDay, minute);
                }
            });
            newFragment.show(getSupportFragmentManager(), "timePicker");

        }));

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean erro = false;

                edtDataInicio.setError(null);
                edtHoraInicio.setError(null);
                edtDataFim.setError(null);
                edtHoraFim.setError(null);

                if (TextUtils.isEmpty(edtDataInicio.getText())){
                    edtDataInicio.setError("Obrigatório");
                    erro = true;
                }
                if (TextUtils.isEmpty(edtHoraInicio.getText())){
                    edtHoraInicio.setError("Obrigatório");
                    erro = true;
                }
                if (TextUtils.isEmpty(edtDataFim.getText())){
                    edtDataFim.setError("Obrigatório");
                    erro = true;
                }
                if (TextUtils.isEmpty(edtHoraFim.getText())){
                    edtHoraFim.setError("Obrigatório");
                    erro = true;
                }

                if (dataInicio.compareTo(dataFim)>0){
                    edtDataFim.setError("Data final não pode ser menor que a inicial!");
                    erro = true;
                }



                if (erro){
                    return;
                }
                atualizarPrevisao();

            }
        });

        setUpToolbar();
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void atualizarPrevisao(){
        final ProgressoDialog progressoDialog = ProgressoDialog.newInstance("Por favor aguarde");
        progressoDialog.show(getSupportFragmentManager());

        Map<String, Object> data = new HashMap<>();
        data.put("dt_previsao_inicio", new Timestamp(dataInicio.getTime()));
        data.put("dt_previsao_fim", new Timestamp(dataFim.getTime()));
        data.put("flg_previsao", "S");

        ordemServico.dataPrevisaoInicio = dataInicio.getTime();
        ordemServico.dataPrevisaoFim = dataFim.getTime();

        db.collection("solicitacao").document(ordemServico.key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(TAG, "onSuccess");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error writing document", e);
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressoDialog.dismiss();

                Intent intent = getIntent();
                intent.putExtra(OrdemServicoAberturaActivity.CT_ORDEM_SERVICO, ordemServico);
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }
}
