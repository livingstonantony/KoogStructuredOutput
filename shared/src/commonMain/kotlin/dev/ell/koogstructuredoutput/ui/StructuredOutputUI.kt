package dev.ell.koogstructuredoutput.ui

import ai.koog.agents.core.agent.AIAgent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.ell.koogstructuredoutput.agent.AgentProvider
import dev.ell.koogstructuredoutput.agent.UserDetailsResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val samples = listOf<Pair<String, String>>(
    ("Sample 1" to "Generate a profile for a 25 year old developer from Canada"),
    ("Sample 2" to "Generate a profile with name Jack, with age of 40 from London"),
    ("Sample 3" to "Generate a sample user profile with name, age, and nationality "),
)

@Composable
fun StructuredOutputUI(
    initialUser: UserDetailsResponse = UserDetailsResponse(
        name = "",
        age = 0,
        nationality = ""
    )
) {

    var text by remember { mutableStateOf("") }
    var displayText by remember { mutableStateOf("") }
    var user by remember { mutableStateOf(initialUser) }
    var isLoading by remember { mutableStateOf(value = false) }

    var agent by remember { mutableStateOf<AIAgent<String, String>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (agent != null) return@LaunchedEffect
        try {
            val newAgent = AgentProvider().provideAgent()
            agent = newAgent
            println("Agent successfully initialized")
        } catch (e: Exception) {
            println("Initialization error: ${e.message}")
        }
    }

    val onRunClick = {
        if (text.isNotBlank() && !isLoading) {
            coroutineScope.launch {
                isLoading = true
                displayText = "Running agent..."
                try {
                    val currentAgent = agent ?: AgentProvider().provideAgent().also { agent = it }
                    val responseJson = currentAgent.run(text)
                    displayText = responseJson

                    // Directly decode JSON into UserDetailsResponse and pass to user profile view
                    user = Json.decodeFromString<UserDetailsResponse>(responseJson)
                } catch (e: Exception) {
                    displayText = "Error running agent: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow() {
            items(samples.size) {
                OutlineChipsView(data = samples[it], onClick = {
                    text = samples[it].second
                })
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                InputField(
                    text = text,
                    onTextChange = { text = it },
                    displayText = displayText,
                    isLoading = isLoading,
                    onRunClick = onRunClick,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                UserProfile(user = user)
            }
        }
    }
}


@Composable
fun OutlineChipsView(data: Pair<String, String>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.LightGray)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Name: ${data.first}",
                style = MaterialTheme.typography.bodyMedium,

                )

        }
    }
}

@Composable
fun InputField(
    text: String,
    onTextChange: (String) -> Unit,
    displayText: String,
    isLoading: Boolean = false,
    onRunClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {


        OutlinedTextField(
            value = text,
            onValueChange = { onTextChange(it) },
            label = { Text("Enter prompt / request") },
            placeholder = { Text("e.g. Generate a profile for a 25 year old developer from Canada") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = false,
            minLines = 4,
            maxLines = 50,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRunClick,
            enabled = !isLoading && text.isNotBlank(),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(if (isLoading) "Running..." else "Run")
        }
        Spacer(modifier = Modifier.height(8.dp))

        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .background(Color.LightGray)
                .clip(MaterialTheme.shapes.medium)
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun UserProfile(user: UserDetailsResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(80.dp)
                .background(Color.LightGray),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.name.ifBlank { "User Profile" },
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Age: ${if (user.age > 0) user.age else "-"}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nationality: ${user.nationality.ifBlank { "-" }}",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
