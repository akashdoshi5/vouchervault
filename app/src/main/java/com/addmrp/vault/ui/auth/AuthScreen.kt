package com.addmrp.vault.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.addmrp.vault.R
import com.addmrp.vault.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Google Sign-In Setup
    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Requires default_web_client_id in strings.xml
            .requestEmail()
            .build()
    }
    
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.authenticateGoogle(idToken, onLoginSuccess)
                }
            } catch (e: ApiException) {
                viewModel.setError("Google sign-in failed: ${e.message}")
            }
        } else {
            viewModel.setError("Google sign-in cancelled")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo / Title
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = VaultGold,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "VoucherVault",
            style = MaterialTheme.typography.displaySmall,
            color = VaultTextPrimary,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(8.dp))

        // Error message
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                color = VaultRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }

        EmailAuthSection(state, viewModel, onLoginSuccess)

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = VaultElevatedSurface)
            Text(" OR ", color = VaultTextTertiary, modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = VaultElevatedSurface)
        }
        Spacer(Modifier.height(24.dp))

        // Google Sign In Button
        Button(
            onClick = { 
                viewModel.setAuthMethod(AuthMethod.GOOGLE)
                launcher.launch(googleSignInClient.signInIntent) 
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VaultSurface)
        ) {
            Text("Continue with Google", color = VaultTextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmailAuthSection(state: AuthUiState, viewModel: AuthViewModel, onSuccess: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = state.email,
        onValueChange = { viewModel.updateField("email", it) },
        label = { Text("Email", color = VaultTextTertiary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VaultSurface,
            unfocusedContainerColor = VaultSurface,
            focusedBorderColor = VaultPrimary,
            unfocusedBorderColor = VaultElevatedSurface,
            focusedTextColor = VaultTextPrimary,
            unfocusedTextColor = VaultTextPrimary
        )
    )

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = { viewModel.updateField("password", it) },
        label = { Text("Password", color = VaultTextTertiary) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = null, tint = VaultTextTertiary)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VaultSurface,
            unfocusedContainerColor = VaultSurface,
            focusedBorderColor = VaultPrimary,
            unfocusedBorderColor = VaultElevatedSurface,
            focusedTextColor = VaultTextPrimary,
            unfocusedTextColor = VaultTextPrimary
        )
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = { viewModel.authenticateEmail(onSuccess) },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
    ) {
        if (state.isLoading && state.currentMethod == AuthMethod.EMAIL) {
            CircularProgressIndicator(color = VaultBlack, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(if (state.isLoginMode) "Sign In" else "Sign Up", fontWeight = FontWeight.Bold, color = VaultBlack)
        }
    }
    
    TextButton(onClick = viewModel::toggleMode) {
        Text(if (state.isLoginMode) "Don't have an account? Sign up" else "Already have an account? Sign in", color = VaultPrimary)
    }
}
