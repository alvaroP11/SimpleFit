package com.example.appgimnasiotfg.ui.login

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

// Clase abstraida para la gestión del login con Google
class GoogleLoginManager (
    activity: Activity,
    activityResultCaller: ActivityResultCaller,
    private val auth: FirebaseAuth,
    webClientId: String,
    private val onSuccess: (email: String?) -> Unit,
    private val onError: (message: String) -> Unit
) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(activity)
    private val signInRequest: BeginSignInRequest
    private var launcher: ActivityResultLauncher<IntentSenderRequest>

    init {
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        launcher = activityResultCaller.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onSuccess(task.result?.user?.email)
                                } else {
                                    onError("Error Firebase: ${task.exception?.message}")
                                }
                            }
                    } else {
                        onError("No se recibió el ID token")
                    }
                } catch (e: ApiException) {
                    onError("Error de Google: ${e.message}")
                }
            } else {
                onError("El usuario canceló el inicio de sesión")
            }
        }
    }

    fun startLogin() {
        oneTapClient.signOut().addOnCompleteListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    val request = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    launcher.launch(request)
                }
                .addOnFailureListener { exception ->
                    if (exception.message?.contains("No matching credentials") == true) {
                        onError("No existen cuentas de Google en el dispositivo. Por favor, agrega una.")
                    } else {
                        onError("No se pudo iniciar sesión: ${exception.message}")
                    }
                }
        }
    }
}