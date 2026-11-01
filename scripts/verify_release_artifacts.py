#!/usr/bin/env python3
"""Verify ZIP integrity and required release entries; never imply signing or Play acceptance."""
import hashlib
from pathlib import Path
from zipfile import ZipFile

outputs = Path("app/build/outputs")
artifacts = list((outputs / "apk/release").glob("*.apk")) + list((outputs / "bundle/release").glob("*.aab"))
if not any(p.suffix == ".apk" for p in artifacts) or not any(p.suffix == ".aab" for p in artifacts):
    raise SystemExit("Both release APK and AAB are required")
lines = []
for path in sorted(artifacts):
    with ZipFile(path) as archive:
        bad = archive.testzip()
        if bad:
            raise SystemExit(f"Corrupt ZIP entry: {path}: {bad}")
        names = set(archive.namelist())
        required = ({"BundleConfig.pb", "base/manifest/AndroidManifest.xml", "base/dex/classes.dex"}
                    if path.suffix == ".aab" else {"AndroidManifest.xml", "classes.dex"})
        if not required <= names:
            raise SystemExit(f"Missing artifact entries in {path}: {required - names}")
    lines.append(f"{hashlib.sha256(path.read_bytes()).hexdigest()}  {path}")
(outputs / "artifact-sha256.txt").write_text("\n".join(lines) + "\n")
print("\n".join(lines))
print("ZIP structure and checksums verified. Signing, manifest values, native page size and device behavior require separate checks.")
