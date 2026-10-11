#!/usr/bin/env python3
"""Persisted engine biomes + vanilla projection on isolated Paper 26.2, two boots."""
import hashlib,json,os,pathlib,shutil,subprocess,urllib.request
ROOT=pathlib.Path(__file__).resolve().parents[1]
SERVER=ROOT/'build/biome-smoke'
PLUGINS=SERVER/'plugins';PLUGINS.mkdir(parents=True,exist_ok=True)
def fetch(url):
    request=urllib.request.Request(url,headers={'User-Agent':'Terra2-CI/1.0 (https://github.com/mrserluiz/Terra2.0)'})
    with urllib.request.urlopen(request,timeout=90) as response:return response.read()
build=next(b for b in json.loads(fetch('https://fill.papermc.io/v3/projects/paper/versions/26.2/builds')) if b['id']==133)
download=build['downloads']['server:default'];data=fetch(download['url'])
assert hashlib.sha256(data).hexdigest()==download['checksums']['sha256']
(SERVER/'paper.jar').write_bytes(data)
terra=ROOT/'platforms/bukkit/build/libs/Terra2-bukkit-7.0.27-BETA.jar'
shutil.copy(terra,PLUGINS/terra.name)
td=PLUGINS/'Terra2';(td/'packs').mkdir(parents=True,exist_ok=True)
shutil.copy(ROOT/'tools/fixtures/HYDRAXIA.zip',td/'packs/HYDRAXIA.zip')
(td/'terra2-settings.yml').write_text('generation:\n  enabled: true\n  protected-worlds: [world, world_nether, world_the_end]\nworlds:\n  terra2_biome_compat:\n    pack: HYDRAXIA\n    biomes:\n      mode: VANILLA_COMPATIBLE\n  terra2_biome_control:\n    pack: HYDRAXIA\n')
classes=SERVER/'probe-classes';classes.mkdir(exist_ok=True)
cp=os.pathsep.join([str(terra)]+[str(p) for p in (pathlib.Path.home()/'.gradle/caches').rglob('*.jar')])
args=SERVER/'javac.args';args.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in ['-cp',cp,'-d',str(classes),str(ROOT/'tools/biome-probe/BiomeCompatibilityProbe.java')]))
subprocess.run(['javac','@'+str(args)],check=True)
(classes/'plugin.yml').write_text('name: BiomeCompatibilityProbe\nversion: 1\nmain: BiomeCompatibilityProbe\napi-version: "26.2"\ndepend: [Terra2]\n')
subprocess.run(['jar','--create','--file',str(PLUGINS/'BiomeCompatibilityProbe.jar'),'-C',str(classes),'.'],check=True)
(SERVER/'eula.txt').write_text('eula=true\n')
(SERVER/'server.properties').write_text('online-mode=false\nview-distance=2\nsimulation-distance=2\nmax-players=1\nspawn-protection=0\n')
for phase in ('create','restart'):
    logfile=SERVER/(phase+'.log')
    with logfile.open('w') as out:
        process=subprocess.Popen(['java','-Xmx3G','-jar','paper.jar','--nogui'],cwd=SERVER,stdin=subprocess.PIPE,stdout=out,stderr=subprocess.STDOUT,text=True)
        try:process.wait(timeout=360)
        except subprocess.TimeoutExpired:
            process.kill();process.wait();print(logfile.read_text()[-30000:]);raise RuntimeError('Biome smoke timed out: '+phase)
    log=logfile.read_text()
    if 'TERRA2_BIOME_COMPAT_OK' not in log or 'TERRA2_BIOME_COMPAT_FAILED' in log:
        print(log[-36000:]);raise RuntimeError('Biome compatibility failed: '+phase)
    print(phase+': TERRA2_BIOME_COMPAT_OK',flush=True)
(SERVER/'result.json').write_text(json.dumps({'status':'PASSED','persistedIdentity':True,'vanillaBukkitBiome':True,'restart':True,'terrainControlComparison':True,'noRetrofit':True,'unloadedReadDoesNotLoad':True},indent=2))
print('TERRA2_BIOME_INTEGRATION_OK')
