#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: ./init.sh [--dry-run] <new-group-id> <new-artifact-prefix>

Rewrites this template's placeholder Maven coordinates and base package
("se.example.order", artifact prefix "order") to your own, in place.

  --dry-run              print what would change, without touching anything
  <new-group-id>         also used as the base package, e.g. com.acme.shop
  <new-artifact-prefix>  e.g. shop -> shop-domain, shop-application, shop-service, ...

Example:
  ./init.sh com.acme.shop shop

This only renames Maven coordinates and the Java base package. It does NOT
rename the "Order" example domain (Order, OrderLine, the orders table, the
/orders REST endpoint, ...) - that's sample business logic meant for you to
replace by hand. See README.md, "Using this as a template".

Run this once, right after cloning, before adding your own code. If this
isn't already a git repository, commit first (git init && git add -A &&
git commit -m init) so the change is reversible.
EOF
}

DRY_RUN=false
if [[ "${1:-}" == "--dry-run" ]]; then
  DRY_RUN=true
  shift
fi

if [[ $# -ne 2 ]]; then
  usage
  exit 1
fi

NEW_GROUP_ID="$1"
NEW_ARTIFACT_PREFIX="$2"

OLD_GROUP_ID="se.example.order"
OLD_ARTIFACT_PREFIX="order"

PKG_RE='^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$'
PREFIX_RE='^[a-z][a-z0-9]*(-[a-z0-9]+)*$'

if [[ ! "$NEW_GROUP_ID" =~ $PKG_RE ]]; then
  echo "error: '$NEW_GROUP_ID' doesn't look like a valid Java groupId/package (e.g. com.acme.shop)" >&2
  exit 1
fi
if [[ ! "$NEW_ARTIFACT_PREFIX" =~ $PREFIX_RE ]]; then
  echo "error: '$NEW_ARTIFACT_PREFIX' should be lowercase, hyphen-separated (e.g. shop, acme-shop)" >&2
  exit 1
fi

cd "$(dirname "$0")"

if [[ "$DRY_RUN" == false && ! -d .git ]]; then
  echo "warning: no .git directory here. This rewrites files and moves directories" >&2
  echo "         in place with no undo. Consider 'git init && git add -A && git commit" >&2
  echo "         -m init' first so you can diff/revert if needed." >&2
  read -r -p "Continue anyway? [y/N] " reply
  [[ "$reply" =~ ^[Yy]$ ]] || exit 1
fi

echo "groupId / base package : $OLD_GROUP_ID -> $NEW_GROUP_ID"
echo "artifact prefix        : $OLD_ARTIFACT_PREFIX-*   -> $NEW_ARTIFACT_PREFIX-*"
echo "app name (properties)  : $OLD_ARTIFACT_PREFIX-service -> $NEW_ARTIFACT_PREFIX-service"
[[ "$DRY_RUN" == true ]] && echo "(dry run - no files will be changed)"
echo

apply_text_replacement() {
  local pattern="$1" replacement="$2" file="$3"
  grep -qF -- "$pattern" "$file" || return 0
  if [[ "$DRY_RUN" == true ]]; then
    echo "would update: $file ($pattern -> $replacement)"
  else
    sed "s#${pattern}#${replacement}#g" "$file" > "${file}.tmp" && mv "${file}.tmp" "$file"
  fi
}

while IFS= read -r -d '' file; do
  apply_text_replacement "$OLD_GROUP_ID" "$NEW_GROUP_ID" "$file"
  apply_text_replacement "${OLD_ARTIFACT_PREFIX}-service" "${NEW_ARTIFACT_PREFIX}-service" "$file"
  apply_text_replacement "${OLD_ARTIFACT_PREFIX}-domain" "${NEW_ARTIFACT_PREFIX}-domain" "$file"
  apply_text_replacement "${OLD_ARTIFACT_PREFIX}-application" "${NEW_ARTIFACT_PREFIX}-application" "$file"
  apply_text_replacement "${OLD_ARTIFACT_PREFIX}-adapter-in-rest" "${NEW_ARTIFACT_PREFIX}-adapter-in-rest" "$file"
  apply_text_replacement "${OLD_ARTIFACT_PREFIX}-adapter-out-persistence" "${NEW_ARTIFACT_PREFIX}-adapter-out-persistence" "$file"
  apply_text_replacement "${OLD_ARTIFACT_PREFIX}-bootstrap" "${NEW_ARTIFACT_PREFIX}-bootstrap" "$file"
done < <(find . \( -path './.git' -o -name target \) -prune -o \
  \( -name 'pom.xml' -o -name '*.properties' -o -name '*.java' -o -name '*.md' \) -print0)

OLD_PKG_PATH=$(printf '%s' "$OLD_GROUP_ID" | tr '.' '/')
NEW_PKG_PATH=$(printf '%s' "$NEW_GROUP_ID" | tr '.' '/')

pkg_dirs=()
while IFS= read -r -d '' dir; do
  pkg_dirs+=("$dir")
done < <(find . \( -path './.git' -o -name target \) -prune -o \
  -type d -path "*/src/*/java/${OLD_PKG_PATH}" -print0)

for dir in "${pkg_dirs[@]}"; do
  base="${dir%/"$OLD_PKG_PATH"}"
  dest="$base/$NEW_PKG_PATH"
  if [[ "$DRY_RUN" == true ]]; then
    echo "would move: $dir -> $dest"
  else
    mkdir -p "$(dirname "$dest")"
    mv "$dir" "$dest"
    rmdir -p "$(dirname "$dir")" 2>/dev/null || true
  fi
done

if [[ "$DRY_RUN" == true ]]; then
  echo
  echo "Dry run complete - nothing was changed."
else
  echo
  echo "Done. Next:"
  echo "  mvn install"
  echo "Then replace the sample Order domain (see README.md) and delete this script"
  echo "if you won't need it again."
fi
