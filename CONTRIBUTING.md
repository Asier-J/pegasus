# Contributing to Pegasus

## Branching Strategy

This project follows a simplified **Git Flow** branching model.

---

### Main Branches

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code. Always stable. |
| `develop` | Integration branch. All features are merged here first. |

---

### Supporting Branches

#### Feature Branches
- **Naming:** `feature/<short-description>` (e.g. `feature/splash-screen`)
- Branched from: `develop`
- Merged back into: `develop`
- Used for developing new features or enhancements.

#### Fix Branches
- **Naming:** `fix/<short-description>` (e.g. `fix/login-crash`)
- Branched from: `develop`
- Merged back into: `develop`
- Used for bug fixes.

---

### Workflow

1. Create a branch from `develop` following the naming convention above, unless the change is related to documentation, in which case it will be done directly in develop.
2. Commit changes with clear, descriptive messages.
3. Open a Pull Request (PR) targeting `develop`.
4. Ensure the code builds and runs without errors before merging.
5. Merge into `develop`. When stable, `develop` is merged into `main` for release.

---

### Commit Message Format

```
<type>: <short description>

Examples:
feat: add splash screen animation
fix: resolve crash on startup
refactor: clean up MainActivity
docs: update README
```

---

### Notes

- Never commit directly to `main`.
- Keep branches short-lived and focused on a single task.
- Delete branches after merging.
