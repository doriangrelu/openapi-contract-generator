# Contributing & Best Practices

Guidelines for working on **openapi-contract-generator**. They are enforced in review and,
where possible, by the build (`mvn -B verify`).

---

## 1. Getting started

```bash
mvn -B verify          # compile + unit tests + (in the example module) end-to-end generation
mvn -B -o validate     # quick POM sanity check, offline
```

| Module | What it is | Published |
|---|---|---|
| `openapi-contract-annotations` | `@ApiContract`, `@ApiDomain` markers | yes |
| `openapi-contract-generator` | discovery + validation + Spring wiring + base test | yes |
| `openapi-contract-example` | two bounded contexts + shared kernel; the reference consumer | no |

Inside `openapi-contract-generator`, the code is split into **specialised packages**, each
with a single responsibility and a `package-info.java`:

```
io.github.doriangrelu.generator            public surface: OpenApiSpecGeneratorTest,
                                           ApiContractConfigurationException
io.github.doriangrelu.generator.scan       classpath discovery (ClassGraph)
io.github.doriangrelu.generator.domain     validation + interface→domain attribution
io.github.doriangrelu.generator.proxy      documentation-only JDK proxies
io.github.doriangrelu.generator.openapi    swagger annotations → oas.models
io.github.doriangrelu.generator.spring     server-less Spring Boot wiring
```

Everything under `…generator.<sub>` is **Internal API**. Each such `package-info.java` says
so. Changes there are never breaking changes (see §5).

---

## 2. Coding standards

### Immutability first

- Model data as `record`s. Give every canonical constructor a defensive copy
  (`List.copyOf`, `Set.copyOf`) and `Objects.requireNonNull` for each component.
- Return immutable collections (`List.of`, `Stream.toList()`, `Collectors.toUnmodifiableList`).
- Never expose a mutable field or an internal array.

### `final` everywhere it is legal

- **Parameters**: always `final`.
- **Local variables**: `final` unless genuinely reassigned. `var` is welcome, but a `final`
  `var` still gets `final`.
- **Fields**: `final` unless a framework contract prevents it (e.g. a `*Aware` setter). When
  a field cannot be `final`, add a one-line comment saying why.
- Classes not designed for extension are `final`. Utility classes have a `private`
  constructor.

### Null handling

- `Objects.requireNonNull(x, "x")` at the top of every **public** constructor / factory /
  entry-point method. Fail fast, with the parameter name in the message.
- A public method that may have no result returns `Optional<T>` — **never** `null`.
- `null` is tolerated only for internal "unset" plumbing where the target model requires it
  (e.g. leaving an OpenAPI field unset). Keep it local and documented; never let it cross a
  public boundary.
- Apply the pattern consistently: callers must be able to trust it.

### Streams & lambdas

- Prefer a stream pipeline to an imperative loop for map / filter / group / reduce.
- Prefer a **method reference** to a lambda when it reads at least as clearly.
- Keep pipelines side-effect-free. Use `collect(...)` / `toList()` to produce a result;
  do not mutate external state from `map`/`peek`.
- Exceptions where a loop wins — and must carry a short comment:
  - the body throws a **checked** exception (e.g. `MockMvc.perform`);
  - the operation is pure side-effect registration and a stream would only obscure it.

### Structure

- One responsibility per class; one reason to change.
- One public type per internal package; mark the package Internal in `package-info.java`.
- Keep the **public surface minimal** — treat it as if a JPMS `exports` listed only
  `io.github.doriangrelu.generator`.
- No premature abstraction: introduce an interface only when there is a second
  implementation or a real seam to test.
- Prefer composition over inheritance. The one inheritance point is the consumer-facing
  `OpenApiSpecGeneratorTest`, by design.

### Misc

- Logging: `System.Logger` (JDK-native). No `System.out` / `System.err` in shipped code.
- Extract magic strings and paths to `private static final` constants.
- `@Override` on every override. Javadoc on every public / protected member **and** every
  package. Use `{@code ...}` / `<pre>{@code ...}</pre>` in Javadoc, not `{@snippet}`
  (keeps `mvn javadoc:jar` portable).
- Spring: **constructor injection only** in production code; no field injection. `*Aware`
  interfaces only when there is no constructor-time alternative.
- Error messages are actionable: name the offending element and say how to fix it
  (see `ApiDomainResolver`).

---

## 3. Testing

- Unit tests sit in the **same package** as the class under test (`src/test/java`), so they
  can exercise package-private members without widening visibility.
- **AAA**: arrange, act, assert — with a blank line between the phases. One behaviour per
  test. Add `@DisplayName` when the method name is not self-evident.
- Error paths use `assertThatThrownBy(...).isInstanceOf(...).hasMessageContaining(...)`.
- Use `@ParameterizedTest` (+ `@MethodSource` / `@CsvSource`) instead of copy-pasting a
  test body for several inputs.
- **Fixtures** live under `io.github.doriangrelu.generator.fixtures.*`. They are real
  `@ApiContract` / `@ApiDomain` declarations, but nothing in the generator module boots a
  Spring context, so the intentionally-broken fixtures (nested domains, duplicate id, …)
  are only ever fed to a resolver explicitly by a test.
- Prefer real fixtures to mocking value objects. Mocking is for awkward collaborators only.
- **End-to-end** coverage is the `openapi-contract-example` module: `mvn verify` there boots
  the server-less context and generates `billing.*` / `catalog.*`. CI uploads them as an
  artifact.
- Keep `verify` green in every commit. A red build is never "someone else's problem".

---

## 4. Commits & pull requests

- Subject line: imperative mood, ≤ 72 chars, no trailing period
  (`Split generator into specialised packages`).
- Body: explain **why**, not just what. Wrap at ~72.
- One logical change per PR. Update `README.md` / `docs/` in the same PR as the code.
- When pairing with an assistant, end the message with
  `Co-Authored-By: <name> <email>`.
- Do not merge with a failing or skipped `verify`.

---

## 5. Branching, versioning & releasing

### Branching — trunk-based

- `main` is the trunk: always green, always releasable.
- Work on short-lived branches (`feat/…`, `fix/…`) → PR → **squash-merge** to `main`. Even
  solo: the PR runs CI and keeps history linear.
- No long-lived `develop` / `release/*` branches. A patch for an already-released version
  (only if it is ever actually needed) is a branch `X.Y.x` created **on demand** from the
  `vX.Y.Z` tag.

### Versioning — SemVer

- While `0.x`, any release may change anything.
- Only types in `io.github.doriangrelu.generator` and the annotations module are API. A
  change to an `…generator.<sub>` package is **not** a breaking change, even if a signature
  changes.
- `main`'s POMs stay on `X.Y.Z-SNAPSHOT`. The concrete release version is stamped from the
  git tag by the workflow; there is no "release commit".

### Releasing

Run from a clean, up-to-date `main`:

```bash
scripts/release.sh 0.1.0        # release version; next dev version inferred (0.2.0-SNAPSHOT)
scripts/release.sh 0.1.0 0.1.1  # explicit next dev version
```

The script runs `mvn verify`, tags `vX.Y.Z` and pushes it (→
`.github/workflows/release.yml`: build, GPG-sign, upload a bundle to the Central Portal),
then bumps `main` to the next `-SNAPSHOT` and pushes. With `autoPublish=false` the bundle
is released with one click from
[central.sonatype.com/publishing/deployments](https://central.sonatype.com/publishing/deployments).

Toggles: `DRY_RUN=1`, `ASSUME_YES=1`, `SKIP_VERIFY=1`, `RELEASE_BRANCH=…`.

Release history: [RELEASES.md](RELEASES.md).

### One-time setup

- The `prod` GitHub Environment holds `CENTRAL_TOKEN_USERNAME`, `CENTRAL_TOKEN_PASSWORD`
  (Portal user token), `GPG_PRIVATE_KEY` (`gpg --armor --export-secret-keys <FPR>`, the
  *private* block) and `GPG_PASSPHRASE`.
- The signing key's **public** half must be on a keyserver Central queries, or every
  deployment fails validation with *"could not find a public key"*:

  ```bash
  gpg --keyserver hkps://keyserver.ubuntu.com --send-keys <FPR>
  # if that server "ignores" the key (seen with ed25519), also:
  #   curl --data-urlencode "keytext@<(gpg --armor --export <FPR>)" https://keys.openpgp.org/vks/v1/upload  (as JSON)
  # verify:
  curl -sS "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x<FPR>" | head -1
  ```

  RSA 4096 is accepted by every keyserver without fuss; EdDSA keys can be rejected by
  `keyserver.ubuntu.com`.

---

## 6. Definition of Done

- [ ] `mvn -B verify` is green (unit tests + example generation).
- [ ] New/changed public members have Javadoc; new packages have `package-info.java`.
- [ ] Parameters and non-reassigned locals are `final`; new value types are `record`s.
- [ ] Public entry points validate arguments with `Objects.requireNonNull`.
- [ ] New behaviour has a unit test; new error path has an `assertThatThrownBy` test.
- [ ] `README.md` / `docs/` updated if behaviour or setup changed.
- [ ] Commit messages follow §4.
