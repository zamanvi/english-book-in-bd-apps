package com.abmn.englishhub.Helper;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

// Replaces the third-party com.abmn.texttospeech.TextToSpeechHelper, which had two bugs:
// 1) it called setSpeechRate() BEFORE setLanguage() inside onInit() - selecting a language
//    re-initializes the engine and resets the rate back to 1.0x, so every speed option except
//    the one matching the reset default was silently ignored.
// 2) its speed field was static, shared across every screen's TTS instance.
// Same public surface (constructor, speak, shutdown) as the old helper so callers don't change.
public class AppTextToSpeechHelper {

    private TextToSpeech textToSpeech;
    private final float speechRate;
    private boolean ready = false;
    private String pendingText;

    public AppTextToSpeechHelper(Context context, String speed) {
        this.speechRate = mapSpeed(speed);
        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS && textToSpeech != null) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setSpeechRate(speechRate);
                ready = true;
                if (pendingText != null) {
                    String text = pendingText;
                    pendingText = null;
                    speak(text);
                }
            }
        });
    }

    private static float mapSpeed(String speed) {
        if (speed == null) return 1.0f;
        switch (speed) {
            case "slower": return 0.5f;
            case "slow": return 0.7f;
            case "fast": return 1.5f;
            case "faster": return 2.0f;
            case "normal":
            default: return 1.0f;
        }
    }

    public void speak(String text) {
        if (text == null || text.isEmpty() || textToSpeech == null) return;
        if (!ready) {
            pendingText = text;
            return;
        }
        textToSpeech.setSpeechRate(speechRate);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_" + System.nanoTime());
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}
