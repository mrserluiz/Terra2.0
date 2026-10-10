#!/usr/bin/env python3
"""Inspect an unmodified official binary; never infer compatibility from version alone."""
import hashlib, json, pathlib, subprocess, urllib.request, zipfile
root = pathlib.Path(__file__).resolve().parents[1]
out = root / 'build/official-climate'
out.mkdir(parents=True, exist_ok=True)
revision = 'c0cfd8b7c0093c9b7ff15d8112a1c26b7be584ed'
url = f'https://raw.githubusercontent.com/KinkinxD12/Aeternum-Seasons/{revision}/AeternumSeasons-4.7.jar'
with urllib.request.urlopen(url, timeout=90) as response:
    data = response.read()
assert hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == '80a48f5a619900740368b973ddd66abaea504a3d'
jar = out / 'official.jar'
jar.write_bytes(data)
with zipfile.ZipFile(jar) as archive:
    descriptor = archive.read('plugin.yml').decode()
    # Inspect all public plugin classes, not just one guessed service implementation.
    classes = [n[:-6].replace('/', '.') for n in archive.namelist()
               if n.endswith('.class') and n.startswith('Kinkin/')]
result = subprocess.run(['javap', '-public', '-classpath', str(jar), *classes],
                        check=True, capture_output=True, text=True).stdout
(out / 'public-api.txt').write_text(result)
(out / 'plugin.yml').write_text(descriptor)
register = 'registerWorldClimateProvider(org.bukkit.plugin.Plugin, java.util.function.Function' in result
unregister = 'unregisterWorldClimateProvider(org.bukkit.plugin.Plugin)' in result
report = {'officialCommit': revision, 'sha256': hashlib.sha256(data).hexdigest(),
          'registerMethodFound': register, 'unregisterMethodFound': unregister,
          'runtimeIntegrationValidated': False,
          'note': 'Methods present alone do not prove reachable receiver or protocol semantics.'}
(out / 'capabilities.json').write_text(json.dumps(report, indent=2))
print(json.dumps(report))
