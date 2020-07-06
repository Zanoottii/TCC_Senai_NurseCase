package br.senai.tcc.nursecare.servicos;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Requisicao;
import br.senai.tcc.nursecare.telas.DetalhesActivity;
import br.senai.tcc.nursecare.telas.MainActivity;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;

public class NotificacaoService extends Service implements ServicosFirebase.ResultadoListener<List<Requisicao>> {

    private static final String CANAL = "NurseCare";
    private NotificationManager notificationManager;
    private ServicosFirebase servicosFirebase;
    private boolean iniciado;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CANAL, CANAL, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(0xFF6200EE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        servicosFirebase = new ServicosFirebase(null);
        iniciado = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (servicosFirebase.isLogado() && !iniciado) {
            servicosFirebase.notificarRequisicao(this);
            iniciado = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        servicosFirebase.notificarRequisicaoRemover();
        notificationManager.cancelAll();
        super.onDestroy();
    }

    @Override
    public void onSucesso(List<Requisicao> requisicoes) {
        for (Requisicao requisicao : requisicoes)
            notificar(getApplicationContext(), requisicao);
    }

    @Override
    public void onErro(String mensagem) {
    }

    private void notificar(Context context, Requisicao requisicao) {
        String titulo, mensagem;
        String enfermeiro = requisicao.getEnfermeiro();
        long datahora = requisicao.getDatahora();

        if (TextUtils.isEmpty(enfermeiro)) {
            titulo = "Iniciado solicitação";
            mensagem = TextUtils.isEmpty(requisicao.getCooperativa()) ?
                    "Você iniciou uma solicitação de atendimento para " :
                    "Foi iniciado uma solicitação de atendimento para você em ";
            requisicao.setEstado(1);
        } else if (enfermeiro.equals("0")) {
            titulo = "Atendimento cancelado";
            mensagem = "Foi cancelado seu atendimento para ";
            requisicao.setEstado(2);
        } else {
            titulo = "Atendimento agendado";
            mensagem = "Seu atendimento foi aceito e agendado para ";
            requisicao.setEstado(4);
        }
        mensagem += new SimpleDateFormat("dd/MM HH:mm", new Locale("pt", "BR")).format(datahora);

        Intent intent = new Intent(context, DetalhesActivity.class);
        intent.putExtra("requisicao", requisicao);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL)
                .setContentIntent(pendingIntent)
                .setSmallIcon(Comum.ESTADO_IMG[requisicao.getEstado()])
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground))
                .setColor(0xFF000000)
                .setTicker(CANAL)
                .setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(titulo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setContentText(mensagem);
        notificationManager.notify((int) (datahora / 10000), builder.build());
    }
}
