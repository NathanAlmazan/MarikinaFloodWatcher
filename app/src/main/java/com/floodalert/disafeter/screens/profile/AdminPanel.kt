package com.floodalert.disafeter.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.model.UserDetails
import com.floodalert.disafeter.screens.authentication.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanel(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    mainViewModel: MainViewModel,
    onNavigateToProfile: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        mainViewModel.hideActionButton = true
        mainViewModel.hideNavbar = false
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null
                )
            }
            Text(
                text = "Assign Admin",
                style = MaterialTheme.typography.displaySmall
            )
        }

        Card(
            elevation = CardDefaults.cardElevation(32.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
            shape = RectangleShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = TextFieldDefaults.textFieldColors(MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null
                    )
                }
            )
        }
        
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 8.dp)
        ) {
            items(viewModel.users.filter { it.username.contains(searchQuery) || searchQuery.isEmpty() }) { user ->
                UserProfile(user) { viewModel.setUserAsAdmin(user) }
            }
        }
    }
}

@Composable
fun UserProfile(user: UserDetails, onSetAdmin: () -> Unit) {

    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                IconButton(onClick = onSetAdmin) {
                    Icon(
                        imageVector = if (user.admin) Icons.Filled.Delete else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (user.admin) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f, true)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (user.admin) "Admin" else "Resident",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}