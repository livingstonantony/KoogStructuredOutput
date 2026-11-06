package dev.ell.koogstructuredoutput

import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel

interface Platform {
    val name: String

    val llmModel: LLModel
    val promptExecutor: PromptExecutor
}

expect fun getPlatform(): Platform