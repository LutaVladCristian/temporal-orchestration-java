# Agent instructions

When changing code, also update documentation if behavior, APIs, setup, configuration, or architecture changes.

Documentation rules:
- Keep `README.md` focused on quickstart and common workflows.
- Put detailed architecture notes in `docs/architecture.md`.
- Put API changes in `docs/api.md`.
- Add decision records under `docs/decisions/` using the format `000N-title.md`.
- Do not invent behavior. Verify from code and tests.
- Prefer concise examples over long prose.
- If docs are outdated but not directly related to the task, mention this in the final summary instead of rewriting everything.
- For every code PR, include a `Docs updated` or `Docs not needed` note.
