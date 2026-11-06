package dev.ell.koogstructuredoutput.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.structure.StructuredResponse
import dev.ell.koogstructuredoutput.getPlatform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@SerialName("UserDetailsResponse")
@LLMDescription("A fictional user's profile information")
data class UserDetailsResponse(

    @property:LLMDescription("Full name of the user")
    val name: String,

    @property:LLMDescription("Age of the user as an integer")
    val age: Int,

    @property:LLMDescription("Nationality of the user")
    val nationality: String,
)

// Few-shot examples for structured output
val exampleUserDetails = listOf(
    UserDetailsResponse(
        name = "Alice Johnson",
        age = 25,
        nationality = "American",
    ),
    UserDetailsResponse(
        name = "David Smith",
        age = 30,
        nationality = "Canadian",
    ),
    UserDetailsResponse(
        name = "James Wilson",
        age = 22,
        nationality = "British",
    ),
)

private val jsonFormatter = Json { prettyPrint = true }

class AgentProvider {

    fun provideAgent(): AIAgent<String, String> {

        val platform = getPlatform()

        // Agent strategy
        val agentStrategy = strategy<String, String>("mock-user-details") {

            // Structured output node
            val generateUserDetails by nodeLLMRequestStructured<UserDetailsResponse>(
                name = "generate-user-details",
                examples = exampleUserDetails,
            )

            // Process structured response
            val processUserDetails by node<Result<StructuredResponse<UserDetailsResponse>>, String> { result ->

                val userDetails = result.getOrThrow().data

                println("Structured response: $userDetails")
                println("Name: ${userDetails.name}")
                println("Age: ${userDetails.age}")
                println("Nationality: ${userDetails.nationality}")

                jsonFormatter.encodeToString(UserDetailsResponse.serializer(), userDetails)
            }

            edge(nodeStart forwardTo generateUserDetails)
            edge(generateUserDetails forwardTo processUserDetails)
            edge(processUserDetails forwardTo nodeFinish)
        }

        // Agent configuration
        val agentConfig = AIAgentConfig(
            prompt = prompt("mock-user-prompt") {

                system(
                    """
You are an intelligent Mock User Profile Generator.

Your task is to generate realistic and diverse fictional user profile based on the user's request.

Follow these rules:

1. Generate fictional user information only.
2. Always generate a realistic full name.
3. The user's age must be between 18 and 80.
4. Age must always be an integer.
5. Nationality must be a valid nationality.
                    """.trimIndent(),
                )
            },
            model = platform.llmModel,
            maxAgentIterations = 5,
        )

        return AIAgent(
            promptExecutor = platform.promptExecutor,
            toolRegistry = ToolRegistry.EMPTY,
            strategy = agentStrategy,
            agentConfig = agentConfig,
        )
    }
}
