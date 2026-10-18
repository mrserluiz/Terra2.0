#!/usr/bin/env python3
import pathlib,urllib.request,json,hashlib,shutil,subprocess,os,zipfile
ROOT=pathlib.Path(__file__).resolve().parents[1];SERVER=ROOT/'build/aether-smoke';PLUGINS=SERVER/'plugins';PLUGINS.mkdir(parents=True,exist_ok=True)
def fetch(url):
 with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Terra2-CI/1.0'}),timeout=90) as r:return r.read()
build=next(b for b in json.loads(fetch('https://fill.papermc.io/v3/projects/paper/versions/26.2/builds')) if b['id']==133)
info=build['downloads']['server:default'];data=fetch(info['url']);assert hashlib.sha256(data).hexdigest()==info['checksums']['sha256'];(SERVER/'paper.jar').write_bytes(data)
terra=next((ROOT/'platforms/bukkit/build/libs').glob('Terra2-bukkit-*-BETA.jar'));shutil.copy(terra,PLUGINS/terra.name)
td=PLUGINS/'Terra2';(td/'packs').mkdir(parents=True,exist_ok=True)
pack=td/'packs/AETHER-0.2.0-terra2.zip'
with zipfile.ZipFile(pack,'w',zipfile.ZIP_DEFLATED) as z:
 for path in sorted((ROOT/'tools/fixtures/aether').rglob('*')):
  if path.is_file():z.write(path,path.relative_to(ROOT/'tools/fixtures/aether').as_posix())
(td/'terra2-settings.yml').write_text('generation:\n  enabled: true\n  protected-worlds: [world, world_nether, world_the_end]\nworlds:\n  terra2_aether_test:\n    pack: AETHER\n')
classes=SERVER/'probe-classes';classes.mkdir(exist_ok=True);cp=os.pathsep.join([str(terra)]+[str(p) for p in (pathlib.Path.home()/'.gradle/caches').rglob('*.jar')]);args=SERVER/'javac.args';args.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in ['-cp',cp,'-d',str(classes),str(ROOT/'tools/aether-probe/AetherPackProbe.java')]))
subprocess.run(['javac','@'+str(args)],check=True);(classes/'plugin.yml').write_text('name: AetherPackProbe\nversion: 1\nmain: AetherPackProbe\napi-version: "26.2"\ndepend: [Terra2]\n');subprocess.run(['jar','--create','--file',str(PLUGINS/'AetherPackProbe.jar'),'-C',str(classes),'.'],check=True)
(SERVER/'eula.txt').write_text('eula=true\n');(SERVER/'server.properties').write_text('server-ip=127.0.0.1\nonline-mode=false\nview-distance=2\nsimulation-distance=2\nmax-players=1\nspawn-protection=0\n')
for phase in ['create','restart']:
 log=SERVER/(phase+'.log')
 with log.open('w') as out:
  proc=subprocess.Popen(['java','-Xmx3G','-jar','paper.jar','--nogui'],cwd=SERVER,stdout=out,stderr=subprocess.STDOUT)
  try:proc.wait(timeout=300)
  except subprocess.TimeoutExpired:proc.kill();proc.wait();raise RuntimeError('Aether test timed out')
 text=log.read_text()
 if 'AETHER_PACK_OK' not in text or 'AETHER_PACK_FAILED' in text:
  print(text[-80000:]);raise RuntimeError('Aether test failed: '+phase)
 print(phase+': '+next(line for line in text.splitlines() if 'AETHER_PACK_OK' in line),flush=True)
