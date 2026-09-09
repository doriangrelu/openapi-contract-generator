#!/usr/bin/env bash
#
# Cut a release in the trunk-based model:
#   1. verify the build,
#   2. tag `main` with vX.Y.Z and push the tag  ->  triggers .github/workflows/release.yml,
#   3. bump `main` to the next -SNAPSHOT and push.
#
# The release workflow stamps the concrete version from the tag, so the POMs on `main`
# always stay on a -SNAPSHOT version.
#
# Usage:
#   scripts/release.sh [RELEASE_VERSION] [NEXT_DEV_VERSION]
#
#   RELEASE_VERSION   e.g. 0.1.0. Default: the current POM version minus -SNAPSHOT.
#   NEXT_DEV_VERSION  e.g. 0.2.0. Default: RELEASE_VERSION with the minor bumped and the
#                     patch reset to 0. The -SNAPSHOT suffix is added automatically.
#
# Environment toggles:
#   RELEASE_BRANCH=main   branch to release from
#   SKIP_VERIFY=1         skip `mvn verify` (not recommended)
#   ASSUME_YES=1          do not prompt for confirmation
#   DRY_RUN=1             print the mutating commands instead of running them
#
set -euo pipefail

RELEASE_BRANCH="${RELEASE_BRANCH:-main}"
VERSIONS_PLUGIN="org.codehaus.mojo:versions-maven-plugin:2.17.1"

die() { printf 'release: %s\n' "$1" >&2; exit 1; }

run() {
  if [[ "${DRY_RUN:-}" == "1" ]]; then
    printf '  [dry-run] %s\n' "$*"
  else
    "$@"
  fi
}

command -v git >/dev/null || die "git not found on PATH"
command -v mvn >/dev/null || die "mvn not found on PATH"

REPO_ROOT="$(git rev-parse --show-toplevel)" || die "not inside a git repository"
cd "$REPO_ROOT"

# --- preconditions ---------------------------------------------------------------------
current_branch="$(git rev-parse --abbrev-ref HEAD)"
[[ "$current_branch" == "$RELEASE_BRANCH" ]] || die "on '$current_branch', expected '$RELEASE_BRANCH'"
[[ -z "$(git status --porcelain)" ]] || die "working tree is not clean"

git fetch --quiet origin "$RELEASE_BRANCH"
[[ "$(git rev-parse HEAD)" == "$(git rev-parse "origin/$RELEASE_BRANCH")" ]] \
  || die "'$RELEASE_BRANCH' is not in sync with origin -- pull/push first"

# --- resolve versions ----------------------------------------------------------------
pom_version="$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout)"
[[ "$pom_version" == *-SNAPSHOT ]] || die "POM version '$pom_version' is not a -SNAPSHOT"

release_version="${1:-${pom_version%-SNAPSHOT}}"
[[ "$release_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "release version '$release_version' must be X.Y.Z"

if [[ -n "${2:-}" ]]; then
  next_version="${2%-SNAPSHOT}"
else
  IFS=. read -r v_major v_minor _ <<<"$release_version"
  next_version="${v_major}.$((v_minor + 1)).0"
fi
[[ "$next_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "next dev version '$next_version' must be X.Y.Z"
next_snapshot="${next_version}-SNAPSHOT"

tag="v${release_version}"
if git rev-parse -q --verify "refs/tags/${tag}" >/dev/null 2>&1; then
  die "tag ${tag} already exists locally"
fi
if git ls-remote --exit-code --tags origin "${tag}" >/dev/null 2>&1; then
  die "tag ${tag} already exists on origin"
fi

# --- confirm ------------------------------------------------------------------------
cat <<EOF
Release from ${RELEASE_BRANCH} @ $(git rev-parse --short HEAD)
  tag              : ${tag}   (push -> triggers the release workflow)
  next dev version : ${next_snapshot}
EOF
if [[ "${ASSUME_YES:-}" != "1" && "${DRY_RUN:-}" != "1" ]]; then
  read -r -p "Proceed? [y/N] " reply
  [[ "$reply" == "y" || "$reply" == "Y" ]] || die "aborted"
fi

# --- verify -----------------------------------------------------------------------
if [[ "${SKIP_VERIFY:-}" != "1" ]]; then
  echo "==> mvn -B verify"
  mvn -B --no-transfer-progress verify
fi

# --- tag & push -----------------------------------------------------------------------
echo "==> tag ${tag}"
run git tag -a "${tag}" -m "openapi-contract ${release_version}"
run git push origin "${tag}"

# --- bump main to the next -SNAPSHOT -------------------------------------------------
echo "==> bump ${RELEASE_BRANCH} to ${next_snapshot}"
run mvn -q "${VERSIONS_PLUGIN}:set" \
  -DnewVersion="${next_snapshot}" -DprocessAllModules -DgenerateBackupPoms=false
if [[ "${DRY_RUN:-}" == "1" ]] || ! git diff --quiet; then
  run git commit -am "Prepare next development iteration ${next_snapshot}"
  run git push origin "${RELEASE_BRANCH}"
else
  echo "  (no POM change to commit)"
fi

cat <<EOF

Done.
  - Watch the workflow : https://github.com/doriangrelu/openapi-contract-generator/actions
  - When it is green, publish the validated bundle:
      https://central.sonatype.com/publishing/deployments
EOF
