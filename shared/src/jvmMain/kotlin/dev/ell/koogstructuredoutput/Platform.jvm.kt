package dev.ell.koogstructuredoutput

import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLModel

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override val llmModel: LLModel = OllamaModels.Meta.LLAMA_3_2_3B
    override val promptExecutor: PromptExecutor = simpleOllamaAIExecutor("http://localhost:11434")
}

actual fun getPlatform(): Platform = JVMPlatform()