package br.senai.tcc.nursecare;

import android.graphics.Bitmap;

public class Usuario {

    private static Usuario usuario;

    private String email;
    private String uid;
    private Paciente paciente;
    private Bitmap foto;

    private Usuario() {}

    public static Usuario getInstance() {
        if (usuario == null)
            usuario = new Usuario();
        return usuario;
    }

    public static void clearInstance() {
        usuario = null;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        Usuario.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Bitmap getFoto() {
        return foto;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }
}
