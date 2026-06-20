# CLAUDE.md

## Main Rules

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions
as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant
clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to
overcomplication, and clarifying questions come before implementation rather than after mistakes.

## Plan Mode

When instructed to create a plan or enter "Plan Mode", your primary goal is to generate a detailed,
step-by-step implementation guide that a junior developer or a smaller, cheaper AI agent can
seamlessly execute without needing deep architectural context. Always put the plan location after
finished
the tasks.

### Rules for Plan Mode

1. Ask First, Plan Second: Before writing any code or finalizing a plan, you must first ask me
   clarifying questions.
2. Wait for Alignment: Do not generate the execution plan until I have answered your questions
   and confirmed we are aligned.
3. Structure for Delegation: Once the context is clear, format the plan strictly as a sequence
   of atomic, isolated tasks. Each task must include:
    - Objective: A clear summary of what needs to be done.
    - Target Files: Exact file paths to be created or modified.
    - Implementation Details: Specific logic flow, expected inputs/outputs, or constraints.
    - Execution Commands: Exact terminal commands required (leveraging `rtk` where applicable).
    - Verification: How the junior dev/agent can test and prove that the specific step
      succeeded.
4. Zero-Ambiguity Guarantee: The final plan must be comprehensive enough that the executing
   developer or agent requires absolutely zero follow-up questions to complete the task.

---

## RTK (Rust Token Killer) - Token-Optimized Commands

### Golden Rule

Always prefix commands with `rtk`. If RTK has a dedicated filter, it uses it. If not, it passes
through unchanged. This means RTK is always safe to use.

Important: Even in command chains with `&&`, use `rtk`:

```bash
# ❌ Wrong
git add . && git commit -m "msg" && git push

# ✅ Correct
rtk git add . && rtk git commit -m "msg" && rtk git push
```

### Gradle Build Commands

```bash
rtk ./gradlew *
```
