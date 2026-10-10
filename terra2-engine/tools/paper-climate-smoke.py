#!/usr/bin/env python3
"""Terra2 -> optional bridge -> Aeternum on real HYDRAXIA, including restart."""
import hashlib,json,os,pathlib,shutil,subprocess,urllib.request

ROOT=pathlib.Path(__file__).resolve().parents[1]
REPO=ROOT.parent
SERVER=ROOT/'build/climate-smoke'
PLUGINS=SERVER/'plugins'; PLUGINS.mkdir(parents=True,exist_ok=True)
UA='Terra2-CI/1.0 (https://github.com/mrserluiz/Terra2.0)'
def fetch(url):
    with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':UA}),timeout=90) as r: return r.read()
builds=json.loads(fetch('https://fill.papermc.io/v3/projects/paper/versions/26.2/builds'))
build=next(b for b in builds if b['id']==133)
download=build['downloads']['server:default']; binary=fetch(download['url'])
assert hashlib.sha256(binary).hexdigest()==download['checksums']['sha256']
(SERVER/'paper.jar').write_bytes(binary)
terra=next((ROOT/'platforms/bukkit/build/libs').glob('Terra2-bukkit-*-BETA.jar'))
aet=next((REPO/'integration-aeternum/aeternum-build/target').glob('AeternumSeasons-*-BETA.jar'))
bridge=REPO/'integrations/aeternum/target/Terra2AeternumBridge-1.0.0-BETA.jar'
for jar in (terra,aet,bridge):shutil.copy(jar,PLUGINS/jar.name)
td=PLUGINS/'Terra2';(td/'packs').mkdir(parents=True,exist_ok=True)
shutil.copy(ROOT/'tools/fixtures/HYDRAXIA.zip',td/'packs/HYDRAXIA.zip')
(td/'terra2-settings.yml').write_text('climate:\n  enabled: true\ngeneration:\n  enabled: true\n  protected-worlds: [world, world_nether, world_the_end]\nworlds:\n  terra2_climate_smoke:\n    pack: HYDRAXIA\n    climate:\n      season: WINTER\n      reference-biome: minecraft:snowy_plains\n')
ad=PLUGINS/'AeternumSeasons';ad.mkdir(exist_ok=True)
(ad/'config.yml').write_text('features:\n  portals:\n    frost:\n      enabled: false\n    heat:\n      enabled: false\n')
(ad/'climate.yml').write_text('biome_spoof:\n  enabled: true\nworld_climate:\n  profiles: {}\n')
classes=SERVER/'probe-classes';classes.mkdir(exist_ok=True)
cp=os.pathsep.join([str(terra),str(aet),(REPO/'integration-aeternum/aeternum-build/target/compile-classpath.txt').read_text().strip()])
args=SERVER/'javac.args'
args.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in ['-cp',cp,'-d',str(classes),str(ROOT/'tools/climate-probe/ClimateIntegrationProbe.java')]))
subprocess.run(['javac','@'+str(args)],check=True)
(classes/'plugin.yml').write_text('name: ClimateIntegrationProbe\nversion: 1\nmain: ClimateIntegrationProbe\napi-version: "26.2"\ndepend: [Terra2, AeternumSeasons]\nsoftdepend: [Terra2AeternumBridge]\n')
subprocess.run(['jar','--create','--file',str(PLUGINS/'ClimateIntegrationProbe.jar'),'-C',str(classes),'.'],check=True)
(SERVER/'eula.txt').write_text('eula=true\n')
(SERVER/'server.properties').write_text('online-mode=false\nview-distance=2\nsimulation-distance=2\nmax-players=1\nspawn-protection=0\n')
for phase in ('create','restart','without-bridge'):
    if phase=='without-bridge': (PLUGINS/bridge.name).unlink()
    logfile=SERVER/(phase+'.log')
    with logfile.open('w') as out:
        command=['java','-Xmx3G','-Dterra2.climate.no-bridge='+str(phase=='without-bridge').lower(),'-jar','paper.jar','--nogui']
        p=subprocess.Popen(command,cwd=SERVER,stdin=subprocess.PIPE,stdout=out,stderr=subprocess.STDOUT,text=True)
        try:p.wait(timeout=360)
        except subprocess.TimeoutExpired:
            p.kill();p.wait();print(logfile.read_text()[-26000:]);raise RuntimeError('Climate integration timed out: '+phase)
    log=logfile.read_text()
    marker='TERRA2_CLIMATE_NO_BRIDGE_OK' if phase=='without-bridge' else 'TERRA2_CLIMATE_BRIDGE_OK'
    if marker not in log or 'TERRA2_CLIMATE_BRIDGE_FAILED' in log:
        print(log[-32000:]);raise RuntimeError('Climate integration failed: '+phase)
    print(phase+': '+marker,flush=True)
(SERVER/'result.json').write_text(json.dumps({'status':'PASSED','customBiomesPreserved':True,'reload':True,'protectedWorlds':True,'restart':True,'withoutBridge':True},indent=2))
print('TERRA2_AETERNUM_INTEGRATION_OK')
