# Codex Project Instructions

## Model and Reasoning
- Use the `gpt-5.1-codex-max` model with `model_reasoning_effort` set to **high** and `approval_policy` set to **never**.
- Emphasize deliberate, concise reasoning with a focus on correctness and reliability.

## General Coding Style
- Target Java 17 for source and bytecode compatibility while keeping Gradle configured appropriately.
- Produce compact, well-structured Java code with minimal comments and Javadocs—only when they add concrete value.
- Prefer clear naming, immutability, and small, composable methods.
- Apply clean code principles and modern design patterns appropriate for a CLI application (e.g., Command pattern for actions, Builder for complex configurations).
- Avoid JUnit or other test framework scaffolding in generated snippets.
- Favor fail-fast validation, meaningful exceptions, and guard clauses over deeply nested conditionals.

## Project Practices
- Keep CLI concerns separated from encryption logic; isolate I/O from core services.
- Default to secure and explicit configuration; avoid implicit global state.
- Prefer dependency injection (constructor-based) for services and utilities; avoid static-heavy designs except for constants.
- Ensure logging/output is minimal and user-focused; avoid noisy diagnostics.

## Documentation and Comments
- Keep inline comments sparse and purposeful; rely on clear code structure instead of extensive narration.
- Keep README-style guidance succinct; prioritize actionable instructions.
