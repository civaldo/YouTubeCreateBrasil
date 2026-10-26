package br.com.criadordevideos;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int PICK_VIDEO = 1001;
    private static final int PICK_PHOTOS = 1002;
    private static final int PICK_AUDIO = 1003;

    private VideoView videoView;
    private TextView status;
    private TextView titleOverlay;
    private EditText projectName;
    private EditText overlayText;
    private Uri videoUri;
    private Uri audioUri;
    private final ArrayList<Uri> photoUris = new ArrayList<>();
    private MediaRecorder recorder;
    private boolean recording = false;
    private Uri recordingUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildEditor();
        loadProject();
    }

    private void buildEditor() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(18));
        root.setBackgroundColor(Color.rgb(18, 18, 25));

        TextView title = new TextView(this);
        title.setText("Criador de Vídeos BR");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(4));
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Seu estúdio de vídeo no celular");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dp(12));
        root.addView(subtitle, matchWrap());

        projectName = new EditText(this);
        projectName.setSingleLine(true);
        projectName.setTextColor(Color.WHITE);
        projectName.setHintTextColor(Color.GRAY);
        projectName.setHint("Nome do projeto");
        projectName.setText("Meu vídeo");
        root.addView(projectName, matchWrap());

        FrameLayout preview = new FrameLayout(this);
        preview.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        previewParams.topMargin = dp(8);
        previewParams.bottomMargin = dp(8);
        root.addView(preview, previewParams);

        videoView = new VideoView(this);
        preview.addView(videoView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        titleOverlay = new TextView(this);
        titleOverlay.setTextColor(Color.WHITE);
        titleOverlay.setTextSize(24);
        titleOverlay.setGravity(Gravity.CENTER);
        titleOverlay.setShadowLayer(5f, 2f, 2f, Color.BLACK);
        titleOverlay.setPadding(dp(10), dp(10), dp(10), dp(10));
        titleOverlay.setVisibility(View.GONE);
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        preview.addView(titleOverlay, overlayParams);

        LinearLayout importRow = new LinearLayout(this);
        importRow.setOrientation(LinearLayout.HORIZONTAL);
        Button addVideo = button("Vídeo");
        addVideo.setOnClickListener(v -> choose(PICK_VIDEO, "video/*", false));
        Button addPhotos = button("Fotos");
        addPhotos.setOnClickListener(v -> choose(PICK_PHOTOS, "image/*", true));
        Button addMusic = button("Música");
        addMusic.setOnClickListener(v -> choose(PICK_AUDIO, "audio/*", false));
        importRow.addView(addVideo, weightParams());
        importRow.addView(addPhotos, weightParams());
        importRow.addView(addMusic, weightParams());
        root.addView(importRow);

        overlayText = new EditText(this);
        overlayText.setSingleLine(true);
        overlayText.setTextColor(Color.WHITE);
        overlayText.setHintTextColor(Color.GRAY);
        overlayText.setHint("Digite um título para o vídeo");
        root.addView(overlayText, matchWrap());

        LinearLayout textRow = new LinearLayout(this);
        textRow.setOrientation(LinearLayout.HORIZONTAL);
        Button applyText = button("Aplicar texto");
        applyText.setOnClickListener(v -> {
            String value = overlayText.getText().toString().trim();
            titleOverlay.setText(value);
            titleOverlay.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
            status.setText("Texto aplicado à prévia. A renderização final com texto será adicionada na etapa de exportação.");
        });
        Button clearText = button("Limpar texto");
        clearText.setOnClickListener(v -> {
            overlayText.setText("");
            titleOverlay.setText("");
            titleOverlay.setVisibility(View.GONE);
        });
        textRow.addView(applyText, weightParams());
        textRow.addView(clearText, weightParams());
        root.addView(textRow);

        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(100);
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (videoView != null && fromUser) status.setText("Volume de prévia: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        root.addView(label("Controle de volume da prévia"));
        root.addView(volume, matchWrap());

        LinearLayout actionRow = new LinearLayout(this);
        Button save = button("Salvar projeto");
        save.setOnClickListener(v -> saveProject());
        Button share = button("Compartilhar vídeo");
        share.setOnClickListener(v -> shareVideo());
        actionRow.addView(save, weightParams());
        actionRow.addView(share, weightParams());
        root.addView(actionRow);

        Button voice = button("Gravar / parar narração");
        voice.setOnClickListener(v -> toggleRecording());
        root.addView(voice, matchWrap());

        status = label("Importe um vídeo para começar. Os arquivos permanecem no seu celular.");
        status.setPadding(0, dp(8), 0, 0);
        root.addView(status, matchWrap());

        setContentView(root);
    }

    private void choose(int requestCode, String mimeType, boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (multiple) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            toast("Não foi possível abrir os arquivos.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == PICK_PHOTOS) {
            photoUris.clear();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    photoUris.add(uri);
                    keepPermission(uri, data.getFlags());
                }
            } else if (data.getData() != null) {
                photoUris.add(data.getData());
                keepPermission(data.getData(), data.getFlags());
            }
            status.setText(photoUris.size() + " foto(s) selecionada(s). A montagem em sequência será feita na etapa de renderização.");
        } else if (requestCode == PICK_AUDIO) {
            audioUri = data.getData();
            keepPermission(audioUri, data.getFlags());
            status.setText("Áudio selecionado. A mixagem na exportação ainda precisa ser implementada.");
        } else if (requestCode == PICK_VIDEO) {
            videoUri = data.getData();
            keepPermission(videoUri, data.getFlags());
            videoView.setVideoURI(videoUri);
            videoView.setMediaController(new MediaController(this));
            videoView.setOnPreparedListener(mp -> {
                status.setText("Vídeo carregado. Use os controles para reproduzir e buscar.");
                videoView.start();
            });
            videoView.setOnErrorListener((mp, what, extra) -> {
                status.setText("Não foi possível reproduzir esse formato de vídeo.");
                return true;
            });
        }
    }

    private void keepPermission(Uri uri, int flags) {
        if (uri == null) return;
        try {
            getContentResolver().takePersistableUriPermission(uri,
                    flags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
        } catch (Exception ignored) { }
    }

    private void saveProject() {
        SharedPreferences prefs = getSharedPreferences("editor_project", MODE_PRIVATE);
        prefs.edit()
                .putString("name", projectName.getText().toString())
                .putString("video", videoUri == null ? "" : videoUri.toString())
                .putString("audio", audioUri == null ? "" : audioUri.toString())
                .putString("photos", joinUris(photoUris))
                .putString("overlay", overlayText.getText().toString())
                .apply();
        status.setText("Projeto salvo neste celular.");
        toast("Projeto salvo.");
    }

    private void loadProject() {
        SharedPreferences prefs = getSharedPreferences("editor_project", MODE_PRIVATE);
        projectName.setText(prefs.getString("name", "Meu vídeo"));
        String overlay = prefs.getString("overlay", "");
        overlayText.setText(overlay);
        titleOverlay.setText(overlay);
        titleOverlay.setVisibility(overlay.isEmpty() ? View.GONE : View.VISIBLE);
        String savedVideo = prefs.getString("video", "");
        if (!savedVideo.isEmpty()) {
            try {
                videoUri = Uri.parse(savedVideo);
                videoView.setVideoURI(videoUri);
                videoView.setMediaController(new MediaController(this));
                videoView.setOnPreparedListener(mp -> status.setText("Projeto recuperado. Vídeo pronto para reproduzir."));
            } catch (Exception ignored) { }
        }
        String savedAudio = prefs.getString("audio", "");
        if (!savedAudio.isEmpty()) audioUri = Uri.parse(savedAudio);
        String savedPhotos = prefs.getString("photos", "");
        if (!savedPhotos.isEmpty()) {
            for (String item : savedPhotos.split("\\n")) {
                if (!item.isEmpty()) photoUris.add(Uri.parse(item));
            }
        }
    }

    private String joinUris(ArrayList<Uri> uris) {
        StringBuilder result = new StringBuilder();
        for (Uri uri : uris) {
            if (result.length() > 0) result.append('\n');
            result.append(uri.toString());
        }
        return result.toString();
    }

    private void shareVideo() {
        if (videoUri == null) {
            toast("Escolha um vídeo primeiro.");
            return;
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("video/*");
        send.putExtra(Intent.EXTRA_STREAM, videoUri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(send, "Compartilhar vídeo"));
    }

    private void toggleRecording() {
        if (!recording && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 2001);
            status.setText("Autorize o microfone e toque novamente para gravar.");
            return;
        }
        if (recording) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                recording = false;
                status.setText("Narração gravada em " + recordingUri + ". Ela ainda não é combinada automaticamente ao vídeo.");
                toast("Gravação concluída.");
            } catch (RuntimeException e) {
                if (recorder != null) recorder.release();
                recorder = null;
                recording = false;
                toast("A gravação ficou muito curta. Tente novamente.");
            }
            return;
        }
        try {
            recordingUri = Uri.fromFile(new java.io.File(getCacheDir(), "narracao_" + System.currentTimeMillis() + ".m4a"));
            if (Build.VERSION.SDK_INT >= 31) {
                recorder = new MediaRecorder(this);
            } else {
                recorder = new MediaRecorder();
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(recordingUri.getPath());
            recorder.prepare();
            recorder.start();
            recording = true;
            status.setText("Gravando narração. Toque novamente para parar.");
        } catch (IOException | RuntimeException e) {
            if (recorder != null) recorder.release();
            recorder = null;
            recording = false;
            toast("Não foi possível iniciar o microfone.");
        }
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);
        return button;
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.LTGRAY);
        view.setTextSize(13);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams weightParams() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (recorder != null) {
            try { recorder.release(); } catch (Exception ignored) { }
            recorder = null;
        }
        super.onDestroy();
    }
}