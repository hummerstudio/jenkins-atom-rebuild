# jenkins-atom-rebuild

The [rebuild](https://plugins.jenkins.io/rebuild/) plugin (≈42k installations) as an **atom**: no Java, no Jelly, no
plugin. Two JSON files.

* `task.json` — the atom contract: code, name, and a target it never runs.
* `ui.json` — the declaration: a `run` surface with a text block and one action that starts the same job with the
  parameters the build ran with.

Installed through **Manage Jenkins → Atom Center**, it adds a "Rebuild" entry to every build page. Uninstalled, it
disappears from all of them; nothing is downloaded or executed to render it.

## What it does not do

* Rebuilding from a specific stage, or with edited parameters (the action kind `build` copies the parameters as-is).
* The rebuilt-by / rebuild-of badge in the build list (that needs an extension the contract does not have yet).

## Package and publish

```bash
./build.sh                                   # → dist/rebuild/{task.json,ui.json}
tools/publish-atom.sh --center <center-dir> --atom-dir dist/rebuild --version 1.0.0 \
    --key key.pem --key-id <keyId>           # tools/ 来自 jenkins-atom-plugin 仓库
```

The signed center inlines `ui.json`, so an installed atom renders offline; `./build.sh` also runs `tools/check.sh`,
which asserts the declaration is one this runner can honour (schema 1, `where: run`, `action.kind: build`).

Requires a runner with the declarative-surface contract (`ui.json`, runner ≥ 0.3).
