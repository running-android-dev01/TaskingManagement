package com.bebsolutions.taskingmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TelefoneAutenticarActivity  extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TelefoneAutenticarActivity.class.getName();

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private String key = "";
    private Map<String, String> data;

    private FirebaseAuth mAuth;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    IntlPhoneInput edtTelefone;
    AppCompatEditText edtCodigoVerificacao;
    AppCompatTextView txtDetalhe;
    AppCompatButton btnIniciarVerificacao;
    AppCompatButton btnVerificar;
    AppCompatButton btnRevalidar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telefone_autenticar);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        edtTelefone = findViewById(R.id.edtTelefone);
        edtCodigoVerificacao = findViewById(R.id.edtCodigoVerificacao);
        txtDetalhe = findViewById(R.id.txtDetalhe);
        btnIniciarVerificacao = findViewById(R.id.btnIniciarVerificacao);
        btnVerificar = findViewById(R.id.btnVerificar);
        btnRevalidar = findViewById(R.id.btnRevalidar);


        btnIniciarVerificacao.setOnClickListener(this);
        btnVerificar.setOnClickListener(this);
        btnRevalidar.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;

                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    //edtTelefone.setError("Invalid phone number.");
                    Snackbar.make(findViewById(android.R.id.content), "Invalid phone number.",
                            Snackbar.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;

                updateUI(STATE_CODE_SENT);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        if(mVerificationInProgress && edtTelefone.isValid()) {
            startPhoneNumberVerification(edtTelefone.getNumber());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                edtCodigoVerificacao.setError("Invalid code.");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }


    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                enableViews(btnIniciarVerificacao, edtTelefone);
                disableViews(btnVerificar, btnRevalidar, edtCodigoVerificacao);
                txtDetalhe.setText(null);
                break;
            case STATE_CODE_SENT:
                enableViews(btnVerificar, btnRevalidar, edtTelefone, edtCodigoVerificacao);
                disableViews(btnIniciarVerificacao);
                txtDetalhe.setText("Código enviado");
                break;
            case STATE_VERIFY_FAILED:
                enableViews(btnIniciarVerificacao, btnVerificar, btnRevalidar, edtCodigoVerificacao,
                        btnIniciarVerificacao);
                txtDetalhe.setText("Falha na verificação");
                break;
            case STATE_VERIFY_SUCCESS:
                disableViews(btnIniciarVerificacao, btnVerificar, btnRevalidar, edtCodigoVerificacao,
                        btnIniciarVerificacao);
                txtDetalhe.setText("Sucesso na verificação");

                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        btnIniciarVerificacao.setText(cred.getSmsCode());
                    } else {
                        btnIniciarVerificacao.setText("(instant validation)");
                    }
                }
                break;
            case STATE_SIGNIN_FAILED:
                txtDetalhe.setText("Sign-in failed");
                break;
            case STATE_SIGNIN_SUCCESS:
                Map<String, String> data = new HashMap<>();
                data.put("uid", user.getUid());

                db.collection("usuarios").document(key).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent =new Intent(TelefoneAutenticarActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

                break;
        }

        if (user == null) {
            edtTelefone.setVisibility(View.VISIBLE);
        }
    }

    private boolean validatePhoneNumber() {
        if(!edtTelefone.isValid()) {
            Toast.makeText(this, "Número inválido.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnIniciarVerificacao:
                if (!validatePhoneNumber()) {
                    return;
                }
                String phone = edtTelefone.getNumber();
                CollectionReference usuariosRef = db.collection("usuarios");

                Query query = usuariosRef.whereEqualTo("telefone", phone);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "onComplete");
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot.size()>0){
                                for (DocumentSnapshot document : task.getResult()) {
                                    key = document.getId();
                                    //data = document.getData();
                                    //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("", document.getData().get("").toString()).apply();

                                    startPhoneNumberVerification(edtTelefone.getText().toString());
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            }else{
                                Toast.makeText(TelefoneAutenticarActivity.this, "Telefone não cadastrado.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error getting documents."+ task.getException());
                        }

                    }
                });

                /*
                if (query!=null){
                    startPhoneNumberVerification(edtTelefone.getText().toString());
                }else{
                    edtTelefone.setError("Telefone não cadastrado.");
                }*/

                //db
                //btnIniciarVerificacao



                break;
            case R.id.btnVerificar:
                String code = edtCodigoVerificacao.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    edtCodigoVerificacao.setError("Codigo em branco.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.btnRevalidar:
                resendVerificationCode(edtTelefone.getText().toString(), mResendToken);
                break;
        }
    }
}
