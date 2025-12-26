package com.heibeieast.campusnav.services;

import android.content.Context;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceService implements TextToSpeech.OnInitListener {
    private static final String TAG = "VoiceService";

    private Context context;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private boolean isInitialized = false;
    private DatabaseService databaseService;

    public VoiceService(Context context) {
        this.context = context.getApplicationContext();
        this.databaseService = DatabaseService.getInstance(context);
    }

    public void initializeTTS() {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, this);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Chinese language not supported");
                isInitialized = false;
            } else {
                isInitialized = true;
                applySettings();
                Log.d(TAG, "TTS initialized successfully");
            }
        } else {
            Log.e(TAG, "TTS initialization failed");
            isInitialized = false;
        }
    }

    private void applySettings() {
        if (textToSpeech != null && isInitialized) {
            textToSpeech.setSpeechRate((float) databaseService.getVoiceSpeed());
            // Note: setPitch() is available but setVolume() is not directly available in older APIs
        }
    }

    public void speak(String text) {
        if (!isInitialized || textToSpeech == null) {
            Log.w(TAG, "TTS not initialized");
            return;
        }

        // Stop any current speech before speaking new text
        stop();

        int speechStatus = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        if (speechStatus == TextToSpeech.ERROR) {
            Log.e(TAG, "Error speaking text: " + text);
        } else {
            Log.d(TAG, "Speaking: " + text);
        }
    }

    public void stop() {
        if (textToSpeech != null && isInitialized) {
            textToSpeech.stop();
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            isInitialized = false;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void speakAfterDelay(final String text, long delayMillis) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                speak(text);
            }
        }).start();
    }

    public void announceNavigation(String from, String to) {
        String message = "导航开始，从" + from + "到" + to;
        speak(message);
    }

    public void announceInstruction(String instruction) {
        speak(instruction);
    }

    public void announceDirection(double bearing) {
        String direction = LocationService.getDirectionName(bearing);
        String message = "请向" + direction + "方向前进";
        speak(message);
    }

    public void announceDistance(double distance, double steps) {
        String message;
        if (distance < 100) {
            message = String.format("前方%.0f米，约%d步", distance, (int) steps);
        } else {
            message = String.format("前方%.0f米", distance);
        }
        speak(message);
    }

    public void announceArrival(String destination) {
        String message = "已到达" + destination;
        speak(message);
    }

    public void announceNavigationCancelled() {
        speak("导航已取消");
    }

    public void announceLocationAdded(String locationName) {
        speak("已添加新位置：" + locationName);
    }

    public void announceLocationEdited(String locationName) {
        speak("已更新位置：" + locationName);
    }

    public void announceLocationDeleted(String locationName) {
        speak("已删除位置：" + locationName);
    }

    public void announceSettingsSaved() {
        speak("设置已保存");
    }

    // Speech recognition (optional feature)
    public void initializeSpeechRecognition(SpeechRecognitionCallback callback) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Ready for speech");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "End of speech");
                }

                @Override
                public void onError(int error) {
                    Log.e(TAG, "Speech recognition error: " + error);
                    if (callback != null) {
                        callback.onError(error);
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        Log.d(TAG, "Recognized: " + spokenText);
                        if (callback != null) {
                            callback.onResult(spokenText);
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }

    public void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(context));
        }
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public interface SpeechRecognitionCallback {
        void onResult(String result);
        void onError(int error);
    }

    // Update settings when user changes preferences
    public void updateSettings() {
        applySettings();
    }

    public void setSpeechRate(float rate) {
        if (textToSpeech != null && isInitialized) {
            textToSpeech.setSpeechRate(rate);
        }
    }
}
