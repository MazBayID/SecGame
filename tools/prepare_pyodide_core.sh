#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="314.0.7"
ARCHIVE_NAME="pyodide-core-${VERSION}.tar.bz2"
ARCHIVE_URL="https://github.com/pyodide/pyodide/releases/download/${VERSION}/${ARCHIVE_NAME}"
EXPECTED_SHA256="2abdcc2e35208af406e07724cffa85bc582ced97e9028383ecf5462541393f95"
CACHE_DIR="${ROOT}/app/build/pyodide-core-download"
ARCHIVE_PATH="${CACHE_DIR}/${ARCHIVE_NAME}"
DEST_DIR="${ROOT}/app/src/debug/assets/runtime-poc/pyodide"

command -v curl >/dev/null 2>&1 || {
  echo "curl is required to download the pinned Pyodide runtime." >&2
  exit 1
}
command -v tar >/dev/null 2>&1 || {
  echo "tar is required to extract the pinned Pyodide runtime." >&2
  exit 1
}

mkdir -p "${CACHE_DIR}"
echo "Downloading Pyodide ${VERSION} core archive…"
curl --fail --location --retry 3 --output "${ARCHIVE_PATH}" "${ARCHIVE_URL}"

if command -v sha256sum >/dev/null 2>&1; then
  ACTUAL_SHA256="$(sha256sum "${ARCHIVE_PATH}" | awk '{print $1}')"
elif command -v shasum >/dev/null 2>&1; then
  ACTUAL_SHA256="$(shasum -a 256 "${ARCHIVE_PATH}" | awk '{print $1}')"
else
  echo "sha256sum or shasum is required to verify the runtime archive." >&2
  exit 1
fi

if [[ "${ACTUAL_SHA256}" != "${EXPECTED_SHA256}" ]]; then
  echo "Pyodide archive checksum mismatch." >&2
  echo "Expected: ${EXPECTED_SHA256}" >&2
  echo "Actual:   ${ACTUAL_SHA256}" >&2
  exit 1
fi

rm -rf "${DEST_DIR}"
mkdir -p "${DEST_DIR}"
tar -xjf "${ARCHIVE_PATH}" -C "${DEST_DIR}"

for required in pyodide.mjs pyodide.asm.mjs pyodide.asm.wasm python_stdlib.zip pyodide-lock.json; do
  if [[ ! -s "${DEST_DIR}/${required}" ]]; then
    echo "Required Pyodide runtime file missing after extraction: ${required}" >&2
    exit 1
  fi
done

echo "Pyodide ${VERSION} core assets verified and extracted to:"
echo "${DEST_DIR}"
echo "These assets are debug-build inputs and must not be committed."
