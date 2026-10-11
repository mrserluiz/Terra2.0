#!/usr/bin/env python3
"""Real Terra2 + experimental Geyser startup/registration/resource pack on Paper, two boots.
No Bedrock client is connected by this test.
"""
import hashlib,json,os,pathlib,shutil,subprocess,urllib.request
ROOT=pathlib.Path(__file__).resolve().parents[1]
GEYSER=ROOT.parent/'integration-geyser'
SERVER=ROOT/'build/bedrock-smoke'
PLUGINS=SERVER/'plugins';PLUGINS.mkdir(parents=True,exist_ok=True)
def fetch(url):
    req=urllib.request.Request(url,headers={'User-Agent':'Terra2-CI/1.0 (https://github.com/mrserluiz/Terra2.0)'})
    with urllib.request.urlopen(req,timeout=90) as r:return r.read()
build=next(b for b in json.loads(fetch('https://fill.papermc.io/v3/projects/paper/versions/26.2/builds')) if b['id']==133)
download=build['downloads']['server:default'];data=fetch(download['url'])
assert hashlib.sha256(data).hexdigest()==download['checksums']['sha256']
(SERVER/'paper.jar').write_bytes(data)
terra=ROOT/'platforms/bukkit/build/libs/Terra2-bukkit-7.0.26-BETA.jar'
geyser=GEYSER/'bootstrap/spigot/build/libs/Geyser-Spigot.jar'
for jar in (terra,geyser):shutil.copy(jar,PLUGINS/jar.name)
td=PLUGINS/'Terra2';(td/'packs').mkdir(parents=True,exist_ok=True)
shutil.copy(ROOT/'tools/fixtures/HYDRAXIA.zip',td/'packs/HYDRAXIA.zip')
(td/'terra2-settings.yml').write_text('generation:\n  enabled: false\n  protected-worlds: [world, world_nether, world_the_end]\nworlds: {}\n')
gd=PLUGINS/'Geyser-Spigot';(gd/'custom_mappings').mkdir(parents=True,exist_ok=True)
mapping=GEYSER/'core/src/test/resources/configuration/terra2-biomes.json'
shutil.copy(mapping,gd/'custom_mappings/terra2-biomes.json')
(gd/'config.yml').write_text('bedrock:\n  address: 127.0.0.1\n  port: 19133\n  transport: raknet\njava:\n  auth-type: offline\ngameplay:\n  enable-custom-content: true\n  enable-integrated-pack: false\nnotify-on-new-bedrock-update: false\n')
classes=SERVER/'probe-classes';classes.mkdir(exist_ok=True)
cp=os.pathsep.join([str(terra)]+[str(p) for p in (pathlib.Path.home()/'.gradle/caches').rglob('*.jar')])
args=SERVER/'javac.args';args.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in ['-cp',cp,'-d',str(classes),str(ROOT/'tools/bedrock-probe/BedrockIntegrationProbe.java')]))
subprocess.run(['javac','@'+str(args)],check=True)
(classes/'plugin.yml').write_text('name: BedrockIntegrationProbe\nversion: 1\nmain: BedrockIntegrationProbe\napi-version: "26.2"\ndepend: [Terra2, Geyser-Spigot]\n')
subprocess.run(['jar','--create','--file',str(PLUGINS/'BedrockIntegrationProbe.jar'),'-C',str(classes),'.'],check=True)
(SERVER/'eula.txt').write_text('eula=true\n')
(SERVER/'server.properties').write_text('server-ip=127.0.0.1\nonline-mode=false\nview-distance=2\nsimulation-distance=2\nmax-players=1\nspawn-protection=0\n')
for phase in ('create','restart'):
    logfile=SERVER/(phase+'.log')
    with logfile.open('w') as out:
        proc=subprocess.Popen(['java','-Xmx3G','-jar','paper.jar','--nogui'],cwd=SERVER,stdin=subprocess.PIPE,stdout=out,stderr=subprocess.STDOUT,text=True)
        try:proc.wait(timeout=300)
        except subprocess.TimeoutExpired:
            proc.kill();proc.wait();print(logfile.read_text()[-40000:]);raise RuntimeError('Geyser smoke timed out: '+phase)
    log=logfile.read_text()
    if 'TERRA2_GEYSER_STARTUP_OK' not in log or 'TERRA2_GEYSER_STARTUP_FAILED' in log:
        print(log[-40000:]);raise RuntimeError('Geyser startup failed: '+phase)
    print(phase+': TERRA2_GEYSER_STARTUP_OK',flush=True)
(SERVER/'result.json').write_text(json.dumps({'status':'PASSED','startup':True,'restart':True,'runtimePack':True,'clientVisualTest':False},indent=2))
