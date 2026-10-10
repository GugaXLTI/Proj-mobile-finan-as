package com.example.controle_gastos.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricHelper {

    public interface Callback {
        void onSucesso();
        void onFalha(String motivo);
    }

    /**
     * Verifica se o dispositivo tem biometria disponível (hardware + digital cadastrada).
     */
    public static boolean podeUsarBiometria(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        int resultado = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
        );
        return resultado == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Dispara o BiometricPrompt para autenticação.
     */
    public static void autenticar(FragmentActivity activity,
                                  String titulo,
                                  String subtitulo,
                                  Callback callback) {

        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt.AuthenticationCallback authCallback =
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSucesso();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Não chama callback aqui - o prompt continua aberto
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        callback.onFalha(errString.toString());
                    }
                };

        BiometricPrompt prompt = new BiometricPrompt(activity, executor, authCallback);

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(titulo)
                .setSubtitle(subtitulo)
                .setNegativeButtonText("Cancelar")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build();

        prompt.authenticate(info);
    }
}