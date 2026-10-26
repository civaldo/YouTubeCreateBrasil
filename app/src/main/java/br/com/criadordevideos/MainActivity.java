package br.com.criadordevideos;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_VIDEO = 1001;
    private VideoView videoView;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFFF7F7FB);

        TextView title = new TextView(this);
        title.setText("Criador de Vídeos BR");
        title.setTextSize(26);
        title.setTextColor(0xFF17172A);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 8, 0, 10);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText("Escolha um vídeo do celular e visualize aqui.");
        subtitle.setTextSize(15);
        subtitle.setTextColor(0xFF555566);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 18);
        root.addView(subtitle);

        Button choose = new Button(this);
        choose.setText("Escolher vídeo");
        choose.setOnClickListener(v -> chooseVideo());
        root.addView(choose, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        videoView = new VideoView(this);
        LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        videoParams.topMargin = 18;
        videoParams.bottomMargin = 12;
        root.addView(videoView, videoParams);

        status = new TextView(this);
        status.setText("Versão inicial: seleção e reprodução de vídeo.");
        status.setTextSize(13);
        status.setTextColor(0xFF555566);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, PICK_VIDEO);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir os vídeos.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) { }
                videoView.setVideoURI(uri);
                videoView.setMediaController(new MediaController(this));
                videoView.setOnPreparedListener(mp -> {
                    status.setText("Vídeo carregado. Use os controles para reproduzir.");
                    videoView.start();
                });
                videoView.setOnErrorListener((mp, what, extra) -> {
                    status.setText("Não foi possível reproduzir este formato de vídeo.");
                    return true;
                });
            }
        }
    }
}
