package com.floodalert.disafeter.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    mainViewModel: MainViewModel,
    onNavigateToLogin: () -> Unit
) {
    var editable by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (mainViewModel.currentUser != null) {
            val user = mainViewModel.currentUser!!
            viewModel.username = user.username
            viewModel.email = user.email
            viewModel.contact = user.contact
            viewModel.uid = user.uid
        }
    }

    LaunchedEffect(Unit) {
        if (mainViewModel.currentUser != null) {
            mainViewModel.hideActionButton = !mainViewModel.currentUser!!.superuser
        }
        mainViewModel.hideNavbar = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (card, form) = createRefs()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .constrainAs(card) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                shape = RoundedCornerShape(bottomStart = 200.dp, bottomEnd = 200.dp),
                colors = CardDefaults.cardColors(Color(0xFFF4EFEF))
            ) {
                Column(modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f, true),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_disafter_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .width(200.dp)
                                .height(200.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = viewModel.username,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFC6824B),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (mainViewModel.currentUser!!.admin) "ADMIN" else "RESIDENT",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFC6824B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { editable = !editable },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiaryContainer),
                            modifier= Modifier
                                .size(60.dp)
                                .padding(8.dp)
                        ) {
                            Icon(
                                if (editable) painterResource(R.drawable.ic_baseline_close_24) else painterResource(R.drawable.ic_baseline_edit_24),
                                contentDescription = stringResource(R.string.button_icon),
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.logOut()
                                onNavigateToLogin()
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiaryContainer),
                            modifier= Modifier
                                .size(60.dp)
                                .padding(8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_baseline_logout_24),
                                contentDescription = stringResource(R.string.button_icon),
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 16.dp)
                    .constrainAs(form) {
                        top.linkTo(card.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = { viewModel.username = it },
                    label = {
                        Text(
                            text = "Username",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_account_circle_24),
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    readOnly = !editable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 8.dp)
                )

                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelLarge
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
                    readOnly = !editable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 8.dp)
                )

                OutlinedTextField(
                    value = viewModel.contact,
                    onValueChange = { viewModel.contact = it },
                    label = {
                        Text(
                            text = "Contact Number",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_phone),
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    readOnly = !editable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 8.dp)
                )

                if (editable) {
                    Button(
                        onClick = {
                            viewModel.editProfile()
                            mainViewModel.getCurrentUser()
                            editable = false
                        },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp, 8.dp)
                    ) {
                        Text(text = "Edit Profile", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (viewModel.submitError != null) {
                    Text(
                        text = viewModel.submitError!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}