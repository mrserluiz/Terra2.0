#!/usr/bin/env python3
"""Official downloaded Geyser + real Paper registration/pack/position identity, two boots.
No Geyser fork is compiled and no Bedrock renderer is connected.
"""
import hashlib,json,os,pathlib,shutil,subprocess,urllib.request
ROOT=pathlib.Path(__file__).resolve().parents[1]
SERVER=ROOT/'build/official-geyser-smoke';PLUGINS=SERVER/'plugins';PLUGINS.mkdir(parents=True,exist_ok=True)
def fetch(url):
    req=urllib.request.Request(url,headers={'User-Agent':'Terra2-CI/1.0 (https://github.com/mrserluiz/Terra2.0)'})
    with urllib.request.urlopen(req,timeout=90) as r:return r.read()
build=next(b for b in json.loads(fetch('https://fill.papermc.io/v3/projects/paper/versions/26.2/builds')) if b['id']==133)
download=build['downloads']['server:default'];data=fetch(download['url']);assert hashlib.sha256(data).hexdigest()==download['checksums']['sha256'];(SERVER/'paper.jar').write_bytes(data)
meta=json.loads(fetch('https://download.geysermc.org/v2/projects/geyser/versions/2.11.3/builds/1251'))
version=meta['version'];number=meta['build'];info=meta['downloads']['spigot']
data=fetch(f'https://download.geysermc.org/v2/projects/geyser/versions/{version}/builds/{number}/downloads/spigot')
assert hashlib.sha256(data).hexdigest()==info['sha256'];(PLUGINS/'Geyser-Spigot.jar').write_bytes(data)
(SERVER/'geyser-release.json').write_text(json.dumps(meta,indent=2))
# Official published FAWE binary, pinned and verified; no WorldEdit fork maintained here.
fawe=fetch('https://cdn.modrinth.com/data/z4HZZnLr/versions/b9Fl6Ct5/FastAsyncWorldEdit-Bukkit-2.16.1.jar')
assert hashlib.sha512(fawe).hexdigest()=='239344f1e906aac37ffed4939ea30a5ba1a1a3935dd4930067e25b8f9864b081ce3ad72473f4a93af8235a70824900649c4450983b2b3dda57d2df655ea3cb4b'
(PLUGINS/'FastAsyncWorldEdit.jar').write_bytes(fawe)

terra=ROOT/'platforms/bukkit/build/libs/Terra2-bukkit-7.3.5-BETA.jar';shutil.copy(terra,PLUGINS/terra.name)
td=PLUGINS/'Terra2';(td/'packs').mkdir(parents=True,exist_ok=True);(td/'bedrock').mkdir(exist_ok=True)
shutil.copy(ROOT/'tools/fixtures/HYDRAXIA.zip',td/'packs/HYDRAXIA.zip')
(td/'bedrock/preview.enabled').write_text('terra2-position-preview-v1')
(td/'bedrock/color-calibration.yml').write_text("roles:\n  foliage: {saturation: 2.0, brightness: 0.5}\nbiomes:\n  'terra:hydraxia/hydraxia/autumnal_woodlands':\n    foliage: {color: '#800000', saturation: 1.0}\n")
(td/'terra2-settings.yml').write_text('generation:\n  enabled: true\n  protected-worlds: [world, world_nether, world_the_end]\nworlds:\n  terra2_bedrock_test:\n    pack: HYDRAXIA\n    biomes:\n      mode: VANILLA_COMPATIBLE\n  terra2_native_biome_test:\n    pack: HYDRAXIA\n')
gd=PLUGINS/'Geyser-Spigot';gd.mkdir(exist_ok=True)
(gd/'config.yml').write_text('bedrock:\n  address: 127.0.0.1\n  port: 19133\n  transport: raknet\njava:\n  auth-type: offline\ngameplay:\n  enable-custom-content: true\n  enable-integrated-pack: false\nnotify-on-new-bedrock-update: false\n')
classes=SERVER/'probe-classes';classes.mkdir(exist_ok=True)
cp=os.pathsep.join([str(terra)]+[str(p) for p in (pathlib.Path.home()/'.gradle/caches').rglob('*.jar')])
args=SERVER/'javac.args';args.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in ['-cp',cp,'-d',str(classes),str(ROOT/'tools/bedrock-probe/OfficialGeyserPreviewProbe.java')]))
subprocess.run(['javac','@'+str(args)],check=True)
(classes/'plugin.yml').write_text('name: OfficialGeyserPreviewProbe\nversion: 1\nmain: OfficialGeyserPreviewProbe\napi-version: "26.2"\ndepend: [Terra2, Geyser-Spigot]\n')
subprocess.run(['jar','--create','--file',str(PLUGINS/'OfficialGeyserPreviewProbe.jar'),'-C',str(classes),'.'],check=True)
(SERVER/'eula.txt').write_text('eula=true\n');(SERVER/'server.properties').write_text('server-ip=127.0.0.1\nonline-mode=false\nview-distance=2\nsimulation-distance=2\nmax-players=1\nspawn-protection=0\n')
for phase in ('create','restart'):
    logfile=SERVER/(phase+'.log')
    with logfile.open('w') as out:
        proc=subprocess.Popen(['java','-Xmx3G','-jar','paper.jar','--nogui'],cwd=SERVER,stdin=subprocess.PIPE,stdout=out,stderr=subprocess.STDOUT,text=True)
        try:proc.wait(timeout=300)
        except subprocess.TimeoutExpired:proc.kill();proc.wait();print(logfile.read_text()[-40000:]);raise RuntimeError('Official Geyser test timed out: '+phase)
    log=logfile.read_text()
    if 'TERRA2_OFFICIAL_GEYSER_PREVIEW_OK' not in log or 'TERRA2_OFFICIAL_GEYSER_PREVIEW_FAILED' in log:
        print(log[-40000:]);raise RuntimeError('Official Geyser test failed: '+phase)
    print(phase+': TERRA2_OFFICIAL_GEYSER_PREVIEW_OK',flush=True)
(SERVER/'result.json').write_text(json.dumps({'status':'PASSED','officialGeyserVersion':version,'officialGeyserBuild':number,'startup':True,'restart':True,'clientVisualTest':False},indent=2))
