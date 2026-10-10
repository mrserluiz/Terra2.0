#!/usr/bin/env python3
"""Real shaded-plugin test, isolated Paper server, including save/restart.

Third-party downloads are kept only in the CI artifact, never committed.
"""
import gzip, hashlib, json, os, pathlib, shutil, struct, subprocess, time, urllib.request, zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
SERVER = ROOT / 'build/native-smoke'
SERVER.mkdir(parents=True, exist_ok=True)
PLUGIN = SERVER / 'plugins/Terra2'
INPUT = PLUGIN / 'conversion/input'
INPUT.mkdir(parents=True, exist_ok=True)
UA = 'Terra2-CI/1.0 (https://github.com/mrserluiz/Terra2.0)'

def fetch(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent': UA}), timeout=90) as r:
        return r.read()

def api(url):
    return json.loads(fetch(url))

def download(url, path, algorithm=None, expected=None):
    data = fetch(url)
    if expected and hashlib.new(algorithm, data).hexdigest() != expected:
        raise RuntimeError('Download checksum mismatch: ' + url)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)

def text(s):
    b = s.encode(); return struct.pack('>H', len(b)) + b

def compound(fields):
    return b''.join(bytes([kind]) + text(name) + value for name, kind, value in fields) + b'\0'

def string(s): return text(s)
def integer(n): return struct.pack('>i', n)
def listed(kind, values): return bytes([kind]) + integer(len(values)) + b''.join(values)
def vector(v): return listed(3, [integer(i) for i in v])

def template(child):
    palette = [compound([('Name', 8, string('minecraft:stone_bricks'))]),
        compound([('Name', 8, string('minecraft:jigsaw')), ('Properties', 10,
            compound([('orientation', 8, string('west_up' if child else 'east_up'))]))]),
        compound([('Name', 8, string('minecraft:chest'))]),
        compound([('Name', 8, string('minecraft:emerald_block'))])]
    blocks = []
    def block(pos, state, nbt=None):
        fields = [('pos', 9, vector(pos)), ('state', 3, integer(state))]
        if nbt is not None: fields.append(('nbt', 10, compound(nbt)))
        blocks.append(compound(fields))
    for x in range(3):
        for z in range(3): block([x, 0, z], 0)
    block([0 if child else 2, 1, 1], 1, [(k, 8, string(v)) for k, v in {
        'id': 'minecraft:jigsaw', 'pool': 'minecraft:empty' if child else 'smoke:child',
        'name': 'smoke:socket', 'target': 'smoke:socket', 'joint': 'aligned', 'final_state': 'minecraft:air'}.items()])
    if not child:
        block([1, 1, 1], 2, [('id', 8, string('minecraft:chest')), ('LootTable', 8, string('smoke:chest')),
            ('LootTableSeed', 4, struct.pack('>q', 42))])
    else: block([1, 1, 1], 3)
    return gzip.compress(b'\x0a\0\0' + compound([('DataVersion', 3, integer(2586)),
        ('size', 9, vector([3, 3, 3])), ('palette', 9, listed(10, palette)),
        ('blocks', 9, listed(10, blocks)), ('entities', 9, listed(10, []))]))

def fixture():
    files = {'pack.mcmeta': {'pack': {'pack_format': 88, 'description': 'Terra2 isolated native integration fixture'}},
        'data/smoke/tags/worldgen/biome/overworld.json': {'values': ['#minecraft:is_overworld']},
        'data/smoke/worldgen/structure/test.json': {'type': 'minecraft:jigsaw', 'biomes': '#smoke:overworld',
            'step': 'surface_structures', 'spawn_overrides': {}, 'start_pool': 'smoke:root', 'size': 2,
            'start_height': {'absolute': 100}, 'max_distance_from_center': 80, 'use_expansion_hack': False},
        'data/smoke/worldgen/structure_set/test.json': {'structures': [{'structure': 'smoke:test', 'weight': 1}],
            'placement': {'type': 'minecraft:random_spread', 'spacing': 1, 'separation': 0, 'salt': 123}},
        'data/smoke/worldgen/processor_list/gold.json': {'processors': [{'processor_type': 'minecraft:rule',
            'rules': [{'input_predicate': {'predicate_type': 'minecraft:block_match', 'block': 'minecraft:stone_bricks'},
                'location_predicate': {'predicate_type': 'minecraft:always_true'}, 'output_state': {'Name': 'minecraft:gold_block'}}]}]},
        'data/smoke/loot_table/chest.json': {'type': 'minecraft:chest', 'pools': [{'rolls': 1,
            'entries': [{'type': 'minecraft:item', 'name': 'minecraft:stone', 'functions': [{'function': 'minecraft:set_count', 'count': 3}]}]}]}}
    for name in ('root', 'child'):
        files[f'data/smoke/worldgen/template_pool/{name}.json'] = {'name': 'smoke:' + name, 'fallback': 'minecraft:empty',
            'elements': [{'weight': 1, 'element': {'element_type': 'minecraft:single_pool_element', 'location': 'smoke:' + name,
                'processors': 'smoke:gold', 'projection': 'rigid'}}]}
    files['data/smoke/loot_table/mob.json'] = {'type': 'minecraft:entity', 'pools': [{'rolls': 1,
        'entries': [{'type': 'minecraft:item', 'name': 'minecraft:stone'}]}]}
    with zipfile.ZipFile(INPUT / 'smoke-pack.zip', 'w', zipfile.ZIP_DEFLATED) as z:
        for path, data in files.items(): z.writestr(path, json.dumps(data))
        for name in ('root', 'child'): z.writestr(f'data/smoke/structure/{name}.nbt', template(name == 'child'))

def dnt():
    versions = api('https://api.modrinth.com/v2/project/dungeons-and-taverns/version')
    expected = 'a65740b20377521b8dbd3c1b0ac2439943d7851c4c37ea82ebdb0edfc79aa5f9'
    for version in versions:
        if version['version_number'].lstrip('v') != '5.1.0': continue
        for file in version['files']:
            if not file['filename'].endswith('.zip'): continue
            path = INPUT / 'dnt.zip'
            download(file['url'], path, 'sha512', file['hashes'].get('sha512'))
            with zipfile.ZipFile(path) as z:
                names = [n for n in z.namelist() if not n.endswith('/')]
                roots = [n[:-len('pack.mcmeta')] for n in names if n.endswith('pack.mcmeta')]
                if len(roots) != 1: continue
                root = roots[0]; files = {n[len(root):]: z.read(n) for n in names if n.startswith(root)}
                digest = hashlib.sha256()
                for name in sorted(files): digest.update(name.encode()); digest.update(b'\0'); digest.update(files[name])
                if digest.hexdigest() == expected and len(files) == 7210:
                    (SERVER / 'source-evidence.json').write_text(json.dumps({'url': file['url'], 'sha256': expected, 'files': len(files)}, indent=2))
                    return
    raise RuntimeError('Cannot locate the exact user Dungeons and Taverns 5.1.0 source fingerprint')

def wait(process, predicate, label, timeout=300):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if predicate(): return
        if process.poll() is not None: raise RuntimeError('Paper exited while waiting for ' + label)
        time.sleep(1)
    raise RuntimeError('Timeout: ' + label)

def run_server(phase):
    logfile = SERVER / (phase + '.log')
    with logfile.open('w') as output:
        process = subprocess.Popen(['java', '-Xmx3G', '-Dterra2.native.integration-test=true', '-jar', 'paper.jar', '--nogui'],
            cwd=SERVER, stdin=subprocess.PIPE, stdout=output, stderr=subprocess.STDOUT, text=True)
        def log(): return logfile.read_text(errors='replace')
        def command(s): process.stdin.write(s + '\n'); process.stdin.flush()
        try:
            wait(process, lambda: 'Done (' in log(), 'server ready', 420)
            if phase == 'create':
                for id, source in [('SmokeNative', 'smoke-pack.zip'), ('DNTNative', 'dnt.zip')]:
                    command(f'terra2 convert {id} {source}')
                    report = PLUGIN / f'conversion/reports/{id}.json'
                    wait(process, report.exists, 'conversion ' + id, 420)
                    data = json.loads(report.read_text())
                    if data['status'] != 'READY_NATIVE': raise RuntimeError('Conversion blocked: ' + report.read_text()[:12000])
            command('nativeprobe ' + ('create' if phase == 'create' else 'reload'))
            marker = 'TERRA2_NATIVE_SMOKE_CREATED' if phase == 'create' else 'TERRA2_NATIVE_SMOKE_RESTART_OK'
            wait(process, lambda: marker in log() or 'TERRA2_NATIVE_SMOKE_FAILED' in log(), 'native generation ' + phase, 420)
            if marker not in log(): raise RuntimeError('Probe failed; inspect ' + str(logfile))
            command('stop'); process.wait(timeout=120)
            if process.returncode != 0: raise RuntimeError('Unclean server shutdown')
        finally:
            if process.poll() is None:
                try: command('stop'); process.wait(timeout=30)
                except (BrokenPipeError, subprocess.TimeoutExpired): process.kill(); process.wait()
            print(logfile.read_text(errors='replace')[-18000:], flush=True)

def main():
    jars = list((ROOT / 'platforms/bukkit/build/libs').glob('Terra2-bukkit-*-BETA.jar'))
    if len(jars) != 1: raise RuntimeError('Expected exactly one engine JAR')
    shutil.copy(jars[0], SERVER / 'plugins' / jars[0].name)
    builds = api('https://fill.papermc.io/v3/projects/paper/versions/26.2/builds')
    build = next(b for b in builds if b['channel'] == 'STABLE')
    server = build['downloads']['server:default']
    checksum = server.get('checksums', {}).get('sha256')
    download(server['url'], SERVER / 'paper.jar', 'sha256', checksum)
    # Pinned official release content: avoid unauthenticated GitHub API rate limits.
    download('https://github.com/PolyhedralDev/TerraOverworldConfig/releases/download/latest/Overworld.zip',
        PLUGIN / 'packs/Overworld.zip', 'sha256', '64e715bc1e591f59d5835650a76fd638c1615772187d91e74eb0255e922afd27')
    download('https://github.com/PolyhedralDev/Tartarus/releases/download/latest/Tartarus.zip',
        PLUGIN / 'packs/filename-does-not-select-id.zip', 'sha256', 'e964ac8a9017aeab4dca7ae843e274421e82f3be7b6aa0351e6a7084eadf3412')
    fixture(); dnt()
    cp = os.pathsep.join([str(jars[0])] + [str(p) for p in (pathlib.Path.home() / '.gradle/caches').rglob('*.jar')])
    classes = SERVER / 'probe-classes'; classes.mkdir(exist_ok=True)
    arguments = SERVER / 'javac.args'
    arguments.write_text('\n'.join('"' + arg.replace('\\', '\\\\').replace('"', '\\"') + '"'
        for arg in ['-cp', cp, '-d', str(classes), str(ROOT / 'tools/native-probe/NativeIntegrationProbe.java')]))
    subprocess.run(['javac', '@' + str(arguments)], check=True)
    (classes / 'plugin.yml').write_text("name: Terra2NativeProbe\nversion: '1'\nmain: NativeIntegrationProbe\napi-version: '26.2'\ndepend: [Terra2]\ncommands:\n  nativeprobe: {}\n")
    subprocess.run(['jar', '--create', '--file', str(SERVER / 'plugins/NativeProbe.jar'), '-C', str(classes), '.'], check=True)
    (SERVER / 'eula.txt').write_text('eula=true\n')
    (SERVER / 'server.properties').write_text('online-mode=false\nview-distance=2\nsimulation-distance=2\nmax-players=2\nspawn-protection=0\n')
    (PLUGIN / 'terra2-settings.yml').write_text('generation:\n  enabled: true\n  protected-worlds: [world, world_nether, world_the_end]\nworlds:\n  terra2_native_smoke:\n    packs: [OVERWORLD, SmokeNative, DNTNative]\n    loot:\n      enabled: true\n      tables:\n        "smoke:chest":\n          name: "Smoke relic"\n          lore: ["Origin: {world}", "{dimension}"]\n          item-model: smoke:relic\n')
    with (PLUGIN / 'terra2-settings.yml').open('a') as settings:
        settings.write('        "smoke:mob":\n          name: "Smoke drop"\n          item-model: smoke:relic\n')
    run_server('create'); run_server('restart')
    (SERVER / 'result.json').write_text(json.dumps({'status': 'PASSED', 'paper': '26.2', 'jigsaw': True, 'processors': True, 'nativeLoot': True, 'saveRestart': True}, indent=2))

if __name__ == '__main__': main()
