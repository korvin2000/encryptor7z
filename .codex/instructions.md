# Codex Authoring Guidelines

## Model
- model: gpt-5.1-codex-max
- model_reasoning_effort: high
- approval_policy: never

## Output Expectations
- Produce compact, well-structured Java code with minimal comments and Javadocs while preserving clarity through naming and decomposition.
- Prefer reliable, modern Java 17 features appropriate for CLI tools (e.g., records where helpful, switch expressions, text blocks when suitable).
- Apply clean code and common design patterns; keep methods small and cohesive.
- Avoid JUnit or other test frameworks unless explicitly required.

## Project Context
- Target Java 17 CLI builds (Gradle, Groovy DSL) with source/target compatibility retained at 17.
- Optimize for maintainability: immutable data where practical, clear configuration points, and sensible defaults.
- Favor explicit error handling and user-friendly CLI messaging without excessive verbosity.

## Style Preferences
- Keep formatting compact, avoiding superfluous blank lines and inline comments.
- Use descriptive identifiers instead of commentary to convey intent.
- Maintain consistent package structure and avoid God classes; extract helpers where they improve readability.