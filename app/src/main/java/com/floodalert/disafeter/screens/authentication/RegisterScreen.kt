package com.floodalert.disafeter.screens.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.R

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    mainViewModel: MainViewModel,
    onNavigateToLogIn: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val email = viewModel.email
    val username = viewModel.username
    val contact = viewModel.contact
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val formError = viewModel.formError
    val submitError = viewModel.submitError
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.registered) {
        if (viewModel.registered) onNavigateToHome()
    }

    LaunchedEffect(Unit) {
        mainViewModel.hideNavbar = true
        mainViewModel.hideActionButton = true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 120.dp),
            colors = CardDefaults.cardColors(Color(0xFFF4EFEF))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_disafter_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(200.dp)
                        .height(120.dp)
                )
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFC6824B),
                    textAlign = TextAlign.Center
                )
            }
        }
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email = it },
            label = {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "e.g. example@email.com",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_email),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            isError = formError?.type == FormError.EmailError.type,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        if (formError != null && formError.type == FormError.EmailError.type) {
            Text(
                text = formError.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.username = it },
            label = {
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "e.g. John Doe",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_account_circle_24),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        OutlinedTextField(
            value = contact,
            onValueChange = { viewModel.contact = it },
            label = {
                Text(
                    text = "Contact Number",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "e.g. 09*********",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_phone),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = formError?.type == FormError.ContactError.type,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        if (formError != null && formError.type == FormError.ContactError.type) {
            Text(
                text = formError.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password = it },
            label = {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "At least 8 characters",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    if (passwordVisible) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_visibility_off_24),
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp),
            isError = submitError != null || formError?.type == FormError.PasswordError.type
        )

        if (formError != null && formError.type == FormError.PasswordError.type) {
            Text(
                text = formError.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (formError != null && formError.type == FormError.ConfirmPasswordError.type) {
            Text(
                text = formError.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { viewModel.confirmPassword = it },
            label = {
                Text(
                    text = "Confirm Password",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "At least 8 characters",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    if (confirmPasswordVisible) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_visibility_off_24),
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp),
            isError = submitError != null || formError?.type == FormError.PasswordError.type
        )

        if (formError != null && formError.type == FormError.PasswordError.type) {
            Text(
                text = formError.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (formError != null && formError.type == FormError.ConfirmPasswordError.type) {
            Text(
                text = formError.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                viewModel.registerUser()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        ) {
            Text(text = "Register")
        }

        OutlinedButton(
            onClick = onNavigateToLogIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(text = "Login")
        }

        if (submitError != null) {
            Text(
                text = submitError,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}