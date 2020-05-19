package br.senai.tcc.nursecare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicosFirebase implements FirebaseAuth.AuthStateListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Activity activity;
    private Usuario usuario;

    public ServicosFirebase(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
        usuario = Usuario.getInstance();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (!isLogado() &&
                !(activity instanceof SplashActivity) &&
                !(activity instanceof LoginActivity) &&
                !(activity instanceof CadastroActivity)) {
            Usuario.clearInstance();
            Intent intent = new Intent(activity.getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }

    public interface ResultadoListener<T> {
        void onSucesso(T objeto);
        void onErro(String mensagem);
    }

    public boolean isLogado() {
        user = mAuth.getCurrentUser();
        return user != null;
    }

    public void iniciarBanco() {
        if (mStorageRef == null) mStorageRef = FirebaseStorage.getInstance().getReference();
        if (db == null) db = FirebaseFirestore.getInstance();
    }

    public void logar(String email, String senha, final ResultadoListener resultado) {
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (isLogado()) {
                                carregarUsuario(new ResultadoListener() {
                                    @Override
                                    public void onSucesso(Object objeto) {
                                        resultado.onSucesso(null);
                                    }

                                    @Override
                                    public void onErro(String mensagem) {
                                        resultado.onErro(mensagem);
                                    }
                                });
                            }
                        } else {
                            FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                            resultado.onErro(firebaseAuthException.getErrorCode());
                        }
                    }
                });
    }

    public void deslogar() {
        mAuth.signOut();
    }

    public void trocarEmail(String email, final ResultadoListener resultado) {
        user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    resultado.onSucesso("email");
                } else {
                    FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                    resultado.onErro(firebaseAuthException.getErrorCode());
                }
            }
        });
    }

    public void trocarSenha(String senha, final ResultadoListener resultado) {
        user.updatePassword(senha).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    resultado.onSucesso("senha");
                } else {
                    FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                    resultado.onErro(firebaseAuthException.getErrorCode());
                }
            }
        });
    }

    public void downloadFoto(String id, final ResultadoListener resultado) {
        iniciarBanco();
        StorageReference fileRef = mStorageRef.child(id + ".png");
        fileRef.getBytes(1048576).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                resultado.onSucesso(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                resultado.onErro("Erro ao descarregar a foto");
            }
        });
    }

    public void uploadFoto(Bitmap foto, final ResultadoListener resultado) {
        iniciarBanco();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.PNG, 90, stream);
        StorageReference fileRef = mStorageRef.child(usuario.getUid() + ".png");
        UploadTask uploadTask = fileRef.putBytes(stream.toByteArray());
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                resultado.onSucesso(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                resultado.onErro("Erro ao enviar a foto");
            }
        });
    }

    public void carregarUsuario(final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("usuarios")
                .whereEqualTo("id_firebase", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                if (querySnapshot.getDocuments().get(0).getString("tipo").equals("pacientes")) {
                                    usuario.setUid(querySnapshot.getDocuments().get(0).getId());
                                    usuario.setEmail(user.getEmail());
                                    carregarPaciente(new ResultadoListener<Paciente>() {
                                        @Override
                                        public void onSucesso(Paciente paciente) {
                                            usuario.setPaciente(paciente);
                                            downloadFoto(usuario.getUid(), new ResultadoListener<Bitmap>() {
                                                @Override
                                                public void onSucesso(Bitmap foto) {
                                                    usuario.setFoto(foto);
                                                    resultado.onSucesso(null);
                                                }

                                                @Override
                                                public void onErro(String mensagem) {
                                                    resultado.onErro(mensagem);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onErro(String mensagem) {
                                            resultado.onErro(mensagem);
                                        }
                                    });
                                } else {
                                    resultado.onErro("Este usuário não é um paciente");
                                }
                            } else {
                                resultado.onErro("Usuário logado não é mais válido");
                            }
                        } else {
                            resultado.onErro("Não foi possível carregar as informações do usuário");
                        }
                    }
                });
    }

    public void carregarPaciente(final ResultadoListener resultado) {
        db.collection("pacientes")
                .document(usuario.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                resultado.onSucesso(documentSnapshot.toObject(Paciente.class));
                            } else {
                                resultado.onErro("Não existe informações deste paciente");
                            }
                        } else {
                            resultado.onErro("Não foi possível carregar as informações do paciente");
                        }
                    }
                });
    }

    public void carregarEnfermeiro(String id, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("enfermeiros")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                resultado.onSucesso(documentSnapshot.toObject(Enfermeiro.class));
                            } else {
                                resultado.onErro("Não existe informações deste enfermeiro");
                            }
                        } else {
                            resultado.onErro("Não foi possível carregar as informações do enfermeiro");
                        }
                    }
                });
    }

    public void carregarCooperativa(String id, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("cooperativas")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                resultado.onSucesso(documentSnapshot.toObject(Cooperativa.class));
                            } else {
                                resultado.onErro("Não existe informações desta cooperativa");
                            }
                        } else {
                            resultado.onErro("Não foi possível carregar as informações da cooperativa");
                        }
                    }
                });
    }

    public void cadastrarPaciente(final String email, final String senha, final Paciente paciente, final Bitmap foto, final ResultadoListener resultado) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (isLogado()) {
                                obterPacienteId(paciente.getCpf(), new ResultadoListener<String>() {
                                    @Override
                                    public void onSucesso(String id) {
                                        if (id.length() > 0) {
                                            usuario.setUid(id);
                                            verificarUsuario(new ResultadoListener() {
                                                @Override
                                                public void onSucesso(Object objeto) {
                                                    atualizarUsuario(new ResultadoListener() {
                                                        @Override
                                                        public void onSucesso(Object objeto) {
                                                            gravarPaciente(paciente, new ResultadoListener() {
                                                                @Override
                                                                public void onSucesso(Object objeto) {
                                                                    uploadFoto(foto, new ResultadoListener() {
                                                                        @Override
                                                                        public void onSucesso(Object objeto) {
                                                                            usuario.setEmail(email);
                                                                            usuario.setPaciente(paciente);
                                                                            usuario.setFoto(foto);
                                                                            resultado.onSucesso(null);
                                                                        }

                                                                        @Override
                                                                        public void onErro(String mensagem) {
                                                                            apagarUsuario();
                                                                            user.delete();
                                                                            resultado.onErro(mensagem);
                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onErro(String mensagem) {
                                                                    apagarUsuario();
                                                                    user.delete();
                                                                    resultado.onSucesso(mensagem);
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onErro(String mensagem) {
                                                            user.delete();
                                                            resultado.onSucesso(mensagem);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onErro(String mensagem) {
                                                    user.delete();
                                                    resultado.onErro(mensagem);
                                                }
                                            });
                                        } else {
                                            gravarUsuario(new ResultadoListener<String>() {
                                                @Override
                                                public void onSucesso(String id) {
                                                    usuario.setUid(id);
                                                    gravarPaciente(paciente, new ResultadoListener() {
                                                        @Override
                                                        public void onSucesso(Object objeto) {
                                                            uploadFoto(foto, new ResultadoListener() {
                                                                @Override
                                                                public void onSucesso(Object objeto) {
                                                                    usuario.setEmail(email);
                                                                    usuario.setPaciente(paciente);
                                                                    usuario.setFoto(foto);
                                                                    resultado.onSucesso(null);
                                                                }

                                                                @Override
                                                                public void onErro(String mensagem) {
                                                                    apagarUsuario();
                                                                    user.delete();
                                                                    resultado.onErro(mensagem);
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onErro(String mensagem) {
                                                            apagarUsuario();
                                                            user.delete();
                                                            resultado.onErro(mensagem);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onErro(String mensagem) {
                                                    user.delete();
                                                    resultado.onErro(mensagem);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onErro(String mensagem) {
                                        user.delete();
                                        resultado.onErro(mensagem);
                                    }
                                });
                            } else {
                                user.delete();
                                resultado.onErro("Erro ao logar como usuário");
                            }
                        } else {
                            resultado.onErro("Erro ao criar o usuário");
                        }
                    }
                });
    }

    public void gravarUsuario(final ResultadoListener resultado) {
        Map<String, Object> mUsuario = new HashMap<>();
        mUsuario.put("tipo", "pacientes");
        mUsuario.put("id_firebase", user.getUid());
        db.collection("usuarios")
                .add(mUsuario)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        resultado.onSucesso(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultado.onErro("Erro ao gravar o usuário");
                    }
                });
    }

    public void gravarPaciente(Paciente paciente, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("pacientes")
                .document(usuario.getUid())
                .set(paciente)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        resultado.onSucesso("paciente");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultado.onErro("Erro ao gravar o paciente");
                    }
                });
    }

    public void atualizarUsuario(final ResultadoListener resultado) {
        Map<String, Object> mUsuario = new HashMap<>();
        mUsuario.put("tipo", "pacientes");
        mUsuario.put("id_firebase", user.getUid());
        db.collection("usuarios")
                .document(usuario.getUid())
                .set(mUsuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        resultado.onSucesso(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultado.onErro("Erro ao atualizar o usuário");
                    }
                });
    }

    public void apagarUsuario() {
        db.collection("usuarios")
                .document(usuario.getUid())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        usuario.setUid(null);
                    }
                });
    }

    public void verificarUsuario(final ResultadoListener resultado) {
        db.collection("usuarios")
                .document(usuario.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                String id = documentSnapshot.getString("id_firebase");
                                if (id.length() > 0) {
                                    resultado.onErro("Este usuário já está cadastrado");
                                } else {
                                    resultado.onSucesso(null);
                                }
                            } else {
                                resultado.onSucesso(null);
                            }
                        } else {
                            resultado.onErro("Não foi possível verificar as informações já cadastradas");
                        }
                    }
                });
    }

    public void obterPacienteId(String cpf, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("pacientes")
                .whereEqualTo("cpf", cpf)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot.isEmpty()) {
                                resultado.onSucesso("");
                            } else {
                                resultado.onSucesso(querySnapshot.getDocuments().get(0).getId());
                            }
                        } else {
                            resultado.onErro("Não foi possível verificar se este paciente já existe");
                        }
                    }
                });
    }

    public void agendarRequisicao(Requisicao requisicao, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("requisicoes")
                .add(requisicao)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        resultado.onSucesso(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultado.onErro("Erro ao agendar o atendimento");
                    }
                });
    }

    public void listarRequisicao(final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("requisicoes")
                .whereEqualTo("paciente", usuario.getUid())
                .orderBy("datahora", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Requisicao> requisicoes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Requisicao requisicao = document.toObject(Requisicao.class);
                                requisicao.setId(document.getId());
                                requisicoes.add(requisicao);
                            }
                            resultado.onSucesso(requisicoes);
                        } else {
                            resultado.onErro("Erro ao obter a lista");
                        }
                    }
                });
    }

    public void historicoRequisicao(long dataInicial, long dataFinal, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("requisicoes")
                .whereEqualTo("paciente", usuario.getUid())
                .whereGreaterThanOrEqualTo("datahora", dataInicial)
                .whereLessThan("datahora", dataFinal)
                .orderBy("datahora")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Requisicao> requisicoes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Requisicao requisicao = document.toObject(Requisicao.class);
                                String enfermeiro = requisicao.getEnfermeiro();
                                if (!TextUtils.isEmpty(enfermeiro) && !"0".equals(enfermeiro) && TextUtils.isEmpty(requisicao.getCooperativa())) {
                                    requisicao.setId(document.getId());
                                    requisicoes.add(requisicao);
                                }
                            }
                            resultado.onSucesso(requisicoes);
                        } else {
                            resultado.onErro("Erro ao obter o histórico");
                        }
                    }
                });
    }

    public void cancelarRequisicao(String id, final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("requisicoes")
                .document(id)
                .update("enfermeiro", "0")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        resultado.onSucesso(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultado.onErro("Erro ao cancelar");
                    }
                });
    }

    public void proximaRequisicao(final ResultadoListener resultado) {
        iniciarBanco();
        db.collection("requisicoes")
                .whereEqualTo("paciente", usuario.getUid())
                .orderBy("datahora", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long datahora = Calendar.getInstance().getTimeInMillis();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Requisicao requisicao = document.toObject(Requisicao.class);
                                if (requisicao != null) {
                                    String enfermeiro = requisicao.getEnfermeiro();
                                    if (!TextUtils.isEmpty(enfermeiro) && !"0".equals(enfermeiro) && requisicao.getDatahora() > datahora) {
                                        resultado.onSucesso(requisicao);
                                        break;
                                    }
                                }
                            }
                        } else {
                            resultado.onErro("Erro ao consultar o próximo atendimento");
                        }
                    }
                });
    }
}
