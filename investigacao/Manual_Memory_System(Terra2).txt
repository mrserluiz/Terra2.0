================================
UPDATES:
=============================
UPDATE > 15/08/26 - M002I

M002I
├── arquitetura do BiomeNoiseProperties ✓
├── base/elevation/carving ✓
├── NoiseSampler ✓
├── blending ✓
└── origem dos NoiseSampler 🔎

[M002I][FACT]
BiomeNoiseProperties foi analisado diretamente através do bytecode do addon:

Terra-chunk-generator-noise-3d-1.2.1-BETA+451683aff-all.jar

Classe:
com.dfsek.terra.addons.chunkgenerator.config.noise.BiomeNoiseProperties

A classe é:
public final class ... extends java.lang.Record
implements com.dfsek.terra.api.properties.Properties

Campos confirmados:
- NoiseSampler base
- NoiseSampler elevation
- NoiseSampler carving
- int blendDistance
- int blendStep
- double blendWeight
- double elevationWeight
- ThreadLocalNoiseHolder noiseHolder

Construtor confirmado:
BiomeNoiseProperties(
    NoiseSampler base,
    NoiseSampler elevation,
    NoiseSampler carving,
    int blendDistance,
    int blendStep,
    double blendWeight,
    double elevationWeight,
    ThreadLocalNoiseHolder noiseHolder
)

[M002I][FACT]
BiomeNoiseProperties NÃO cria os NoiseSampler.

Os três NoiseSampler são recebidos prontos pelo construtor.

Portanto:
BiomeNoiseProperties = container/record de propriedades de geração
já resolvidas, e não a fábrica dos Noise.

[M002I][FACT]
Métodos confirmados:
- base() -> NoiseSampler
- elevation() -> NoiseSampler
- carving() -> NoiseSampler
- blendDistance() -> int
- blendStep() -> int
- blendWeight() -> double
- elevationWeight() -> double
- noiseHolder() -> ThreadLocalNoiseHolder

[M002I][FACT]
A descoberta de carving adiciona uma terceira camada de Noise específica
ao sistema de terreno:

BiomeNoiseProperties
├── base
├── elevation
└── carving

[M002I][FACT]
BiomeNoiseProperties está localizado no addon:
Terra-chunk-generator-noise-3d

Não foi encontrado BiomeNoiseProperties nos outros JARs durante a busca
por nome de classe.

[M002I][FACT]
As implementações de NoiseSampler estão distribuídas principalmente no:
Terra-config-noise-function-1.2.0

Exemplos identificados anteriormente incluem:
- DimensionApplicableNoiseSampler
- CubicSplineNoiseSampler
- GaussianNoiseSampler
- PositiveWhiteNoiseSampler
- WhiteNoiseSampler
- GaborNoiseSampler

[M002I][ARCHITECTURE]
O fluxo já comprovado é:

Biome
  -> Context
  -> PropertyKey<BiomeNoiseProperties>
  -> BiomeNoiseProperties
  -> base/elevation/carving
  -> NoiseSampler
  -> ChunkInterpolator / ElevationInterpolator / carving pipeline

[M002I][IMPORTANT]
Ainda NÃO foi localizado quem executa:
new BiomeNoiseProperties(...)

Também ainda não está comprovado o caminho completo:

Pack
 -> configuração de Noise
 -> NoiseFunction
 -> NoiseSampler
 -> BiomeNoiseProperties

Essa ligação continua sendo hipótese até encontrarmos o código
responsável pela criação/injeção.

[M002I][INFERENCE]
A arquitetura indica forte desacoplamento:

configuração
  -> NoiseSampler
  -> BiomeNoiseProperties
  -> generator

O gerador trabalha com a interface NoiseSampler e não precisa conhecer
a implementação matemática específica do Noise.

Isso é considerado um princípio arquitetural desejável para o Terra 2.0.

[M002I][NEXT]
Próximo alvo:
1. Mapear classes de configuração do addon
   Terra-chunk-generator-noise-3d.
2. Mapear classes de configuração do
   Terra-config-noise-function-1.2.0.
3. Localizar a classe/factory/provider que constrói
   BiomeNoiseProperties.
4. Localizar quem transforma configuração em NoiseSampler.
5. Somente depois investigar Noise individuais.

[M002I][DECISION]
Não investigar Simplex, Perlin, Cellular ou outros Noise matemáticos
individualmente neste momento.

Primeiro descobrir:
CONFIG -> NoiseFunction -> NoiseSampler -> BiomeNoiseProperties

[M002I][EVIDENCE]
Arquivos:
investigacao/results/M002I-BiomeNoiseProperties-javap.txt
investigacao/results/M002I-BiomeNoiseProperties-signature.txt
investigacao/results/M002I-BiomeNoiseProperties-allrefs.txt
investigacao/results/M002I-BiomeNoiseProperties-map.txt
investigacao/results/M002I-BiomeNoiseProperties-refs.txt
investigacao/results/M002I-biome-noise-map.txt
investigacao/results/M002I-NoiseSampler-map.txt

[M002I][NEW COLLECTION]
Foram solicitados/gerados mapas:
investigacao/results/M002I-chunk-generator-config-map.txt
investigacao/results/M002I-noise-function-config-map.txt

Esses resultados devem ser analisados antes de qualquer nova coleta.

[CONTINUITY]
Ao continuar a investigação, consultar esta atualização antes de
interpretar M002I ou iniciar M002J.
Separar sempre FACT, INFERENCE e HYPOTHESIS.
UPDATE > 15/08/26 - M002H

[M002H][FACT]
ChunkInterpolator e ElevationInterpolator foram analisados através dos resultados
javap armazenados no GitHub.

1. ChunkInterpolator:
- Trabalha com PropertyKey<BiomeNoiseProperties>.
- Obtém BiomeNoiseProperties através do Context do Biome.
- Não recebe diretamente um Noise concreto.
- Obtém BiomeNoiseProperties.base().
- base() fornece um com.dfsek.terra.api.noise.NoiseSampler.
- O NoiseSampler é avaliado através de ThreadLocalNoiseHolder.
- O ChunkInterpolator portanto trabalha contra a abstração NoiseSampler.

Fluxo confirmado:
BiomeProvider
  -> Biome
  -> Context
  -> PropertyKey<BiomeNoiseProperties>
  -> BiomeNoiseProperties.base()
  -> NoiseSampler
  -> ThreadLocalNoiseHolder
  -> valor de Noise

2. ElevationInterpolator:
- Também utiliza PropertyKey<BiomeNoiseProperties>.
- Obtém BiomeNoiseProperties.elevation().
- elevation() fornece NoiseSampler.
- Avalia o sampler usando seed + coordenadas X/Z.
- Possui tratamento de blending entre biomas.

Fluxo confirmado:
Biome
  -> Context
  -> BiomeNoiseProperties
  -> elevation()
  -> NoiseSampler
  -> ElevationInterpolator
  -> valor de elevação

3. Blending:
- ChunkInterpolator utiliza propriedades associadas ao blending entre biomas,
  incluindo blendWeight, blendDistance e blendStep.
- ElevationInterpolator utiliza elevationWeight para ponderação.
- O Terra consegue portanto combinar Noise de diferentes biomas nas regiões
  de transição.

4. [ARCHITECTURE][FACT]
O ChunkInterpolator NÃO instancia nem conhece Noise concretos como Simplex,
Perlin ou Cellular.
Ele depende da interface:
com.dfsek.terra.api.noise.NoiseSampler

Isso demonstra desacoplamento entre:
Generator
e
Noise implementation.

5. [ARCHITECTURE][INFERENCE]
A arquitetura observada atualmente pode ser representada como:

Pack
  -> Biome configuration
  -> Biome Context
  -> BiomeNoiseProperties
  -> NoiseSampler
  -> ChunkInterpolator
  -> Sampler3D
  -> generateChunkData()

E paralelamente:

BiomeNoiseProperties
  -> elevation()
  -> NoiseSampler
  -> ElevationInterpolator
  -> Sampler3D

A parte "Pack -> BiomeNoiseProperties -> NoiseSampler" ainda não está
completamente comprovada. Precisamos localizar quem instancia/constrói
BiomeNoiseProperties e quem injeta os NoiseSampler.

6. [DECISION]
Não investigar individualmente Simplex, Perlin, Cellular etc. neste momento.
Primeiro descobrir o mecanismo que transforma configuração em NoiseSampler.

7. [NEXT]
M002I:
Investigar:
- BiomeNoiseProperties.class
- referências a BiomeNoiseProperties
- construtores/fábricas de BiomeNoiseProperties
- registro de PropertyKey relacionado a Noise
- criação/injeção de NoiseSampler
- relação entre configuração de Pack/Biome e NoiseSampler.

8. [EVIDENCE]
Resultados M002H:
investigacao/results/M002H-ChunkInterpolator-javap.txt
investigacao/results/M002H-ElevationInterpolator-javap.txt
investigacao/results/M002H-noise-properties-map.txt

9. [CONTINUITY]
Antes de iniciar M002I, consultar:
Manual_Memory_System(Terra2).txt
e os resultados M002H no GitHub.

Não considerar como FACT qualquer ligação entre Pack e NoiseSampler
até localizar o código responsável pela criação/injeção de BiomeNoiseProperties.

UPDATE > 15/08/26 - M002G

[M002G][FACT]
Investigação do addon Terra-chunk-generator-noise-3d-1.2.1-BETA+451683aff-all.jar confirmou:

1. SamplerProvider:
- Classe:
  com.dfsek.terra.addons.chunkgenerator.generation.math.samplers.SamplerProvider
- NÃO é o motor matemático de Noise.
- Atua como factory/cache de Sampler3D.
- getChunk(cx, cz, WorldProperties, BiomeProvider) cria/obtém Sampler3D associado ao contexto do mundo/chunk.
- Utiliza cache Caffeine configurado por PluginConfig.getSamplerCache().
- O contexto de geração inclui seed, altura mínima/máxima e coordenadas do chunk.

2. Sampler3D:
- Classe:
  com.dfsek.terra.addons.chunkgenerator.generation.math.samplers.Sampler3D
- Contém:
  ChunkInterpolator
  ElevationInterpolator
- sample(x,y,z) combina:
  ChunkInterpolator.getNoise(x,y,z)
  +
  ElevationInterpolator.getElevation(x,z)
- Portanto Sampler3D funciona como composição final dos valores usados pelo gerador.

3. Descoberta de arquitetura modular:
O addon Terra-chunk-generator-noise-3d não contém sozinho todos os Noise utilizados pelo Terra.
Foi identificado o addon:
  Terra-config-noise-function-1.2.0

Esse addon contém diversas funções/samplers de Noise, incluindo:
- Simplex
- OpenSimplex2
- Perlin
- Value
- Cellular
- Gabor
- Brownian Motion
- Ridged Fractal
- PingPong
- Domain Warp
- Arithmetic samplers:
  Addition
  Subtraction
  Multiplication
  Division
  Min
  Max
- Cache
- Translate
- PseudoErosion
- Distance
- Image
- Kernel
- LinearHeightmap
- entre outros.

4. [ARCHITECTURE][INFERENCE]
A arquitetura atualmente indica separação entre:
Terra Core/API
  ↓
Generator
  ↓
SamplerProvider
  ↓
Sampler3D
  ↓
ChunkInterpolator / ElevationInterpolator
  ↓
Noise Function / Sampler Graph

O mecanismo exato que conecta ChunkInterpolator às Noise Functions ainda NÃO está confirmado.

5. [DECISION]
Não investigar individualmente todas as Noise Functions neste momento.
Primeiro descobrir como ChunkInterpolator recebe/constrói o Noise configurado.
Depois investigar o sistema de Noise Graph/configuração.

6. [NEXT]
M002H:
Investigar:
ChunkInterpolator
ElevationInterpolator
BiomeNoiseProperties
NoiseProperties
NoiseFunction
e descobrir como a configuração do Pack é transformada em Samplers/Noise executáveis.

[MEMORY RULE]
Resultados M002G estão registrados nos arquivos:
investigacao/results/M002G-sampler-classes.txt
investigacao/results/M002G-SamplerProvider-javap.txt
investigacao/results/M002G-Sampler3D-javap.txt
investigacao/results/M002G-sampler-implementations.txt
investigacao/results/M002G-sampler-provider-refs.txt
investigacao/results/M002G-sampler-noise-map.txt

Sempre consultar os arquivos de results no GitHub quando uma conclusão anterior precisar ser confirmada.

UPDATE > 15/08/26 - M002F

MILESTONE=M002F
NAME=Palette selection and Slant material system identified
STATUS=CONFIRMED

FACT:F046
SOURCE=investigacao/results/M002F-paletteAt.txt
CLAIM=NoiseChunkGenerator3D selects terrain palettes through BiomePaletteInfo.
STATUS=CONFIRMED

FACT:F047
SOURCE=investigacao/results/M002F-paletteAt.txt
CLAIM=BiomePaletteInfo exposes PaletteHolder and SlantHolder for material selection.
STATUS=CONFIRMED

FACT:F048
SOURCE=investigacao/results/M002F-paletteAt.txt
CLAIM=When slant palettes are enabled, paletteAt() can select a slant palette according to terrain depth and calculated surface slant.
STATUS=CONFIRMED

FACT:F049
SOURCE=investigacao/results/M002F-paletteAt.txt
CLAIM=Palette.get(index,x,y,z,seed) returns a BlockState rather than merely selecting a static block.
STATUS=CONFIRMED

FACT:F050
SOURCE=investigacao/results/M002F-paletteAt.txt
CLAIM=The generator exposes getBlock(WorldProperties,x,y,z,BiomeProvider), allowing individual block-state evaluation without generating an entire chunk.
STATUS=CONFIRMED

ARCHITECTURE_UPDATE:
Terrain sampler value
 -> biome
 -> BiomePaletteInfo
 -> PaletteHolder / SlantHolder
 -> Palette
 -> Palette.get(...)
 -> BlockState

SLANT_SYSTEM:
BiomePaletteInfo
 -> SlantHolder
 -> depth check
 -> SlantCalculationMethod
 -> surface slant
 -> threshold
 -> slant palette OR normal palette

IMPORTANT:
Palette is a procedural BlockState selector and may receive coordinates and seed.
Therefore Terra material generation is not necessarily a static block lookup.

API_CAPABILITY:
getBlock(...) provides a direct coordinate-level terrain/block evaluation path.

CURRENT_STATE:
TerrainValue=CONFIRMED
BiomeResolution=CONFIRMED
PaletteResolution=CONFIRMED
SlantPaletteSystem=CONFIRMED
BlockStateSelection=CONFIRMED
SamplerConstruction=UNKNOWN

CURRENT_TARGET=M002G
Investigate SamplerProvider and the construction of Sampler3D.

NEXT_TARGET:
1=Identify exact SamplerProvider class.
2=Trace SamplerProvider.get().
3=Trace SamplerProvider.getChunk().
4=Identify Sampler3D implementation.
5=Determine how sampler graphs are constructed.
6=Identify configuration inputs.
7=Only after this investigate underlying Noise implementations.

DESIGN_VALUE_FOR_TERRA2:
The original architecture suggests separating:
Terrain sampling
Biome resolution
Material/palette resolution
BlockState generation

This separation should be preserved in Terra 2.0 and exposed through modern Pack/DataPack configuration.

MEMORY_RULE:
Do not move to Noise implementation before tracing SamplerProvider unless evidence shows Noise is directly constructed there.

UPDATE > 15/08/26 - M002D

MILESTONE=M002D
NAME=Concrete ChunkGeneratorProvider and instantiation path identified
STATUS=CONFIRMED

FACT:F041
SOURCE=investigacao/results/M002D-generator-provider-registration.txt
CLAIM=NoiseChunkGenerator3DAddon registers a ChunkGeneratorProvider in the ConfigPack ChunkGeneratorProvider registry during ConfigPackPreLoadEvent.
STATUS=CONFIRMED

FACT:F042
SOURCE=investigacao/results/M002D-generator-provider-registration.txt
CLAIM=The registered provider is created through an invokedynamic lambda corresponding to lambda$initialize$1.
STATUS=CONFIRMED

FACT:F043
SOURCE=investigacao/results/M002D-generator-provider-registration.txt
CLAIM=lambda$initialize$1 constructs the concrete NoiseChunkGenerator3D instance.
STATUS=CONFIRMED

FACT:F044
SOURCE=investigacao/results/M002D-generator-provider-registration.txt
CLAIM=NoiseChunkGenerator3D is instantiated with ConfigPack, Platform, elevationBlend, horizontalRes, verticalRes, biome PropertyKey, palette PropertyKey, SlantCalculationMethod and slant palette enablement.
STATUS=CONFIRMED

FACT:F045
SOURCE=investigacao/results/M002D-provider-registration-search.txt
CLAIM=The concrete NoiseChunkGenerator3D implementation exists exclusively in the Terra-chunk-generator-noise-3d addon among the scanned addon JARs.
STATUS=CONFIRMED

ARCHITECTURE_UPDATE:
ConfigPack
 -> ConfigPackPreLoadEvent
 -> NoiseChunkGenerator3DAddon.initialize()
 -> ChunkGeneratorProvider registry
 -> registered Noise 3D provider
 -> lambda$initialize$1(...)
 -> new NoiseChunkGenerator3D(...)
 -> generateChunkData()

GENERATOR_PROVIDER:
TYPE=ChunkGeneratorProvider
REGISTRATION=ChunkGeneratorProvider registry
IMPLEMENTATION=NoiseChunkGenerator3DAddon.lambda$initialize$1
OUTPUT=NoiseChunkGenerator3D

CONCRETE_GENERATOR:
CLASS=com.dfsek.terra.addons.chunkgenerator.generation.NoiseChunkGenerator3D
INTERFACE=com.dfsek.terra.api.world.chunk.generation.ChunkGenerator

M002D_RESULT:
The true executable terrain generator creation path has been identified.

PREVIOUS_UNKNOWN:
ChunkGeneratorProvider=UNKNOWN
ChunkGenerator=UNKNOWN

CURRENT_STATE:
ChunkGeneratorProvider=CONFIRMED
ChunkGenerator=CONFIRMED
ProviderInstantiationPath=CONFIRMED
generateChunkData=PARTIALLY_TRACED

HYPOTHESIS:H010
OLD=NoiseChunkGenerator3DAddon likely registers the concrete generator/configuration types into Terra's ConfigPack/registry during addon initialization.
NEW=Confirmed. NoiseChunkGenerator3DAddon registers a concrete ChunkGeneratorProvider during ConfigPackPreLoadEvent and the provider constructs NoiseChunkGenerator3D.
STATUS=CONFIRMED

CURRENT_TARGET=M002E
Trace NoiseChunkGenerator3D.generateChunkData() completely.

NEXT_TARGET:
1=Map generateChunkData() execution order.
2=Identify SamplerProvider inputs and outputs.
3=Identify biome lookup.
4=Identify BiomeNoiseProperties.
5=Identify BiomePaletteInfo.
6=Identify Palette selection.
7=Identify block placement loop.
8=Identify density/noise sampling.
9=Identify vertical/horizontal interpolation.
10=Identify slant palette logic.
11=Identify where caves/oceans/terrain shape are introduced.

DO_NOT_INVESTIGATE_YET:
Noise mathematical implementation should not be analyzed in isolation until its caller/input chain is mapped.

REUSE_VALUE_FOR_TERRA2:
The original Terra architecture provides a clean separation:
Core API
 -> addon registration
 -> provider registry
 -> generator implementation
 -> pack configuration
 -> generation pipeline.

This architecture is a strong candidate for Terra 2.0 modernization rather than rewriting the entire generator system from scratch.

UPDATE > 15/08/26 - M002C

MILESTONE=M002C
NAME=Concrete Noise 3D Chunk Generator identified
STATUS=CONFIRMED

FACT:F038
SOURCE=investigacao/results/M002-addon-jar-tree.txt
CLAIM=Terra contains a dedicated addon JAR named Terra-chunk-generator-noise-3d-1.2.1-BETA+451683aff-all.jar.
STATUS=CONFIRMED

FACT:F039
SOURCE=investigacao/results/M002-addon-generation-classes.txt
CLAIM=The Noise 3D addon contains NoiseChunkGenerator3D.class under com.dfsek.terra.addons.chunkgenerator.generation.
STATUS=CONFIRMED

FACT:F040
SOURCE=investigacao/results/M002-addon-generation-classes.txt
CLAIM=The Noise 3D addon contains NoiseChunkGenerator3DAddon.class, NoiseChunkGeneratorPackConfigTemplate, BiomeNoiseConfigTemplate, SamplerProvider, ThreadLocalNoiseHolder and multiple generation/math/interpolation components.
STATUS=CONFIRMED

ARCHITECTURE_UPDATE:
The concrete terrain generator is not located in Terra Core.
It is supplied by the Terra-chunk-generator-noise-3d addon.

CURRENT_PIPELINE:
Terra Core
 -> Addon System
 -> Terra-chunk-generator-noise-3d
 -> NoiseChunkGenerator3DAddon
 -> NoiseChunkGenerator3D
 -> generation/math/samplers
 -> noise/biome configuration
 -> palette
 -> chunk generation

HYPOTHESIS:H009
OLD=Concrete ConfigType/Factory/GeneratorProvider may be supplied by addons.
NEW=Concrete terrain-generation implementation is confirmed to exist inside the Noise 3D addon.
STATUS=PARTIALLY_CONFIRMED

HYPOTHESIS:H010
CLAIM=NoiseChunkGenerator3DAddon likely registers the concrete generator/configuration types into Terra's ConfigPack/registry during addon initialization.
STATUS=STRONG
REQUIRES=M002D-javap-analysis

CURRENT_TARGET=M002D
Analyze NoiseChunkGenerator3DAddon registration and NoiseChunkGenerator3D execution.

NEXT_TARGET:
1=Identify registration mechanism.
2=Identify concrete ConfigType/ConfigFactory.
3=Identify GeneratorProvider.
4=Trace newInstance(pack).
5=Trace generateChunkData().
6=Trace SamplerProvider.
7=Trace Noise configuration.
8=Trace Palette/Biome integration.

RULE:
Do not modify the addon JAR.
Use javap/decompilation only for structural investigation.

UPDATE > 15/08/26 - 16:59

MEMORY_FILE_RENAME:
OLD=TERRA2_CONTEXT.txt
NEW=Manual_Memory_System(Terra2).txt

STATUS=CONFIRMED

RULE:
Manual_Memory_System(Terra2).txt is now the official persistent manual memory file for the Terra2 project and technical interaction.

MEMORY_REFERENCE_UPDATE:
All future references to TERRA2_CONTEXT.txt as the active memory file should be interpreted as Manual_Memory_System(Terra2).txt.

GITHUB_PATH:
investigacao/Manual_Memory_System(Terra2).txt

EVIDENCE:
The file was successfully verified in the main branch of the GitHub repository.

NEXT:
Update internal protocol references from TERRA2_CONTEXT.txt to Manual_Memory_System(Terra2).txt where appropriate.
UPDATE > 15/08/26 - 16:40

MEMORY_PROTOCOL_UPDATE:
TERRA2_CONTEXT.txt permanece como a MEMÓRIA OPERACIONAL EXTERNA PRINCIPAL do projeto e da interação técnica.

MEMORY_STRUCTURE:
- FACTS = fatos confirmados por código/evidência.
- HYPOTHESES = interpretações ainda não totalmente comprovadas.
- DECISIONS = decisões arquiteturais do projeto Terra 2.0.
- CURRENT_STATUS = estado consolidado da investigação.
- CURRENT_TARGET = objetivo técnico imediato.
- NEXT_STEP = próximo passo recomendado.
- UPDATE = histórico cronológico das mudanças relevantes.

UPDATE_RULE:
Quando uma descoberta alterar significativamente o entendimento da arquitetura, investigação ou objetivo do projeto, registrar um novo bloco:
UPDATE > DD/MM/AA - HH:MM

O UPDATE deve registrar de forma compacta:
- milestone;
- novas descobertas;
- fatos confirmados;
- hipóteses criadas/confirmadas/refutadas;
- mudanças arquiteturais;
- decisões;
- estado atual;
- próximo objetivo.

HISTORY_RULE:
Não apagar updates históricos.
Não reescrever fatos antigos sem registrar a alteração.
Updates representam evolução cronológica da investigação.
A memória consolidada deve refletir o estado atual, enquanto UPDATE preserva como esse estado foi alcançado.

ASSISTANT_MEMORY_BEHAVIOR:
Antes de continuar uma investigação complexa:
1=Consultar TERRA2_CONTEXT.txt no GitHub.
2=Consultar os evidence files relevantes em investigacao/results/.
3=Usar os FACTS já confirmados para evitar repetir investigação.
4=Usar HYPOTHESES somente como hipóteses.
5=Verificar se novas evidências confirmam/refutam hipóteses anteriores.
6=Continuar a partir de CURRENT_TARGET.
7=Quando houver marco relevante, informar USER que uma atualização de memória é necessária.
8=Fornecer o bloco UPDATE pronto para ser adicionado ao TERRA2_CONTEXT.txt.
9=Não exigir que USER copie grandes resultados para o chat quando eles puderem ser persistidos no GitHub.
10=Quando faltar evidência local, fornecer o comando PowerShell para gerar um arquivo em investigacao/results/.
11=Após o USER atualizar o GitHub, consultar diretamente o arquivo/repositório e continuar a investigação.

EVIDENCE_MEMORY_RELATION:
investigacao/results/*.txt = evidência bruta produzida pelos comandos locais.
investigacao/*.java = arquivos de investigação selecionados.
investigacao/default-pack-tree.txt = evidência estrutural do default pack.
TERRA2_CONTEXT.txt = conhecimento consolidado + estado + histórico.
GitHub = fonte persistente de continuidade técnica.

GITHUB_WORKFLOW:
USER executa comando local
 -> saída é salva em investigacao/results/
 -> USER atualiza GitHub
 -> ASSISTANT verifica GitHub
 -> ASSISTANT analisa evidência
 -> ASSISTANT atualiza entendimento
 -> se necessário, ASSISTANT fornece UPDATE para TERRA2_CONTEXT.txt
 -> investigação continua.

MEMORY_UPDATE_TRIGGER:
Atualizar memória quando:
- novo núcleo arquitetural for identificado;
- uma hipótese importante for confirmada/refutada;
- novo componente crítico for localizado;
- pipeline de geração for confirmado;
- decisão arquitetural do Terra 2.0 for tomada;
- objetivo/ordem da investigação mudar;
- nova dependência importante for descoberta.

DO_NOT_UPDATE_FOR:
- resultados triviais;
- buscas sem resultado que não alterem o entendimento;
- pequenos comandos auxiliares;
- informações já registradas sem mudança de estado.

CURRENT_MEMORY_FORMAT:
FACTS/HYPOTHESES/DECISIONS = estado consolidado.
UPDATE = histórico cronológico.
RESULTS = evidências brutas.

PROJECT_INTERACTION_PRINCIPLE:
A memória não deve servir somente para lembrar a investigação.
Ela deve preservar a continuidade técnica do projeto Terra 2.0 e das decisões tomadas durante sua construção.

M002_MEMORY_STATE:
A investigação confirmou a existência de uma camada de configuração baseada em ConfigType/ConfigFactory/registry e identificou uma arquitetura de Addons contendo JARs externos e loaders.
Nenhum ConfigType ou ConfigFactory concreto foi encontrado no núcleo Java investigado até o momento.
Hipótese atual: componentes concretos podem ser fornecidos/registrados por Addons.
Esta hipótese ainda requer análise direta dos JARs.

CURRENT_TARGET:
M002C=Analisar arquitetura dos Addon JARs.

NEXT_INVESTIGATION:
1=Listar conteúdo dos Addon JARs.
2=Identificar manifests.
3=Identificar entrypoints/classes principais.
4=Localizar ConfigType/ConfigFactory dentro dos Addons.
5=Localizar GeneratorProvider.
6=Seguir GeneratorProvider.newInstance(pack).
7=Localizar ChunkGenerator concreto.
8=Somente depois investigar Noise e demais sistemas de TerraFormação.

END_UPDATE

UPDATE > 15/08/26 - 16:38
MILESTONE:M002B
NAME=Addon architecture identified
STATUS=CONFIRMED/PENDING_JAR_ANALYSIS

FACT:F031
SOURCE=repository/investigacao/results/M002-addon-directories.txt
CLAIM=The Terra project contains a dedicated addons/ directory.
STATUS=CONFIRMED

FACT:F032
SOURCE=repository/investigacao/results/M002-addon-files.txt
CLAIM=The addons directory contains Terra-command-addons and bootstrap addon loader JARs, while the core contains BootstrapAddonLoader, EphemeralAddon, InternalAddon, BaseAddon, BukkitAddon, NMSAddon and ConfigPackAddonsTemplate.
STATUS=CONFIRMED

FACT:F033
SOURCE=repository/investigacao/results/M002-concrete-config-types.txt
CLAIM=No concrete ConfigType implementation was found in the currently investigated Java source tree.
STATUS=CONFIRMED

FACT:F034
SOURCE=repository/investigacao/results/M002-config-factories.txt
CLAIM=No concrete ConfigFactory implementation was found in the currently investigated Java source tree.
STATUS=CONFIRMED

FACT:F035
SOURCE=repository/investigacao/results/M002-register-config-types.txt
CLAIM=ConfigPack exposes registerConfigType(), but no concrete registration call was found in the investigated core Java source.
STATUS=CONFIRMED

FACT:F036
SOURCE=repository/investigacao/results/M002-config-type-registry.txt
CLAIM=ConfigPackImpl maintains a ConfigType registry and resolves configuration objects through ConfigType.getFactory().build(...).
STATUS=CONFIRMED

FACT:F037
SOURCE=repository/investigacao/results/M002-config-load-events.txt
CLAIM=Configuration loading uses ProtoConfig and ConfigType before materializing configuration objects.
STATUS=CONFIRMED

HYPOTHESIS:H008
CLAIM=Concrete ConfigType/ConfigFactory implementations are supplied by Terra addons or external addon modules rather than the investigated core.
STATUS=STRONG
REQUIRES=M002-addon-jar-analysis

HYPOTHESIS:H009
CLAIM=The concrete ChunkGeneratorProvider may be implemented inside an addon JAR and registered into the Terra ConfigType system during addon initialization.
STATUS=STRONG
REQUIRES=M002-addon-jar-analysis

ARCH_UPDATE:
Terra Core
 -> Addon System
 -> external/internal addon JAR
 -> ConfigType registration
 -> ConfigFactory
 -> runtime configuration object
 -> possible GeneratorProvider
 -> ChunkGenerator

NEXT_TARGET:M002C
Inspect addon JAR contents, manifests, entrypoints and generation-related classes.

EVIDENCE_REQUIRED:
investigacao/results/M002-addon-jar-tree.txt
investigacao/results/M002-addon-generation-classes.txt
investigacao/results/M002-addon-manifests.txt

RULE:
Do not modify addon JARs yet.
First identify ownership, entrypoints, dependencies, registration mechanism and generation classes.

END_UPDATE

UPDATE > 15/08/26 - 15:40
MILESTONE:M002
NAME=Configuration pipeline identified
STATUS=PARTIALLY_COMPLETED

FACT:F024
SOURCE=com/dfsek/terra/config/pack/ConfigPackImpl.java
CLAIM=ConfigPackImpl creates ConfigPackTemplate and loads it through selfLoader.load(template, packManifest).
STATUS=CONFIRMED

FACT:F025
SOURCE=com/dfsek/terra/config/pack/ConfigPackImpl.java
CLAIM=Before loading ConfigPackTemplate, Terra discovers configurations and registers Meta preprocessors through registerMeta().
STATUS=CONFIRMED

FACT:F026
SOURCE=com/dfsek/terra/config/pack/ConfigPackTemplate.java
CLAIM=generator is mapped by @Value("generator") to a @Meta ChunkGeneratorProvider field.
STATUS=CONFIRMED

FACT:F027
SOURCE=com/dfsek/terra/config/preprocessor/MetaValuePreprocessor.java
CLAIM=@Meta string values beginning with $ can resolve another discovered configuration using file:key syntax.
STATUS=CONFIRMED

FACT:F028
SOURCE=com/dfsek/terra/api/config/ConfigType.java
CLAIM=ConfigType provides getTemplate(), getFactory(), and getTypeKey().
STATUS=CONFIRMED

FACT:F029
SOURCE=com/dfsek/terra/api/config/ConfigFactory.java
CLAIM=ConfigFactory.build(template, platform) constructs the runtime object represented by a configuration type.
STATUS=CONFIRMED

FACT:F030
SOURCE=com/dfsek/terra/config/pack/ConfigPackImpl.java
CLAIM=Discovered configurations containing "type" are grouped by ConfigType and materialized through ConfigType.getFactory().build(...).
STATUS=CONFIRMED

ARCH_UPDATE:
ConfigPackImpl
 -> discoverConfigurations()
 -> registerMeta()
 -> selfLoader.load(ConfigPackTemplate, packManifest)
 -> @Meta generator
 -> configuration resolution
 -> ConfigType/ConfigFactory system
 -> runtime object

M002_RESULT:
The concrete ChunkGeneratorProvider is likely supplied through Terra's configuration/type system rather than directly instantiated by ConfigPackTemplate Java code.

M002_STATUS:
CONFIGURATION_PIPELINE=CONFIRMED
CONCRETE_GENERATOR_PROVIDER=UNKNOWN
CHUNK_GENERATOR=UNKNOWN

NEXT_TARGET:
Identify ConfigType/ConfigFactory registration responsible for ChunkGeneratorProvider.

NEXT_SEARCH:
Search for:
- ConfigType implementations related to ChunkGeneratorProvider
- ConfigFactory implementations returning ChunkGeneratorProvider
- registerConfigType(...)
- TypeKey<ChunkGeneratorProvider>
- generator configuration files containing "type"
- addon registration of generator-related ConfigTypes

M002 — Configuration / Addon Architecture

ConfigPackTemplate              ✓
@Meta                           ✓
Tectonic                        ✓
ConfigType registry             ✓
ConfigFactory                   ✓
Concrete ConfigType no Core     ✗
Concrete Factory no Core        ✗
Addon directory                 ✓
Addon JARs                      ✓ ← NOVA DESCOBERTA
Addon loader                    ✓
Generation addon                ? ← PRÓXIMO
GeneratorProvider               ?
ChunkGenerator                  ?

END_UPDATE

START_MEMORY_SYSTEM

T2CTX|v1.0
PROJECT=Terra2.0
PROJECT_TYPE=REVIVE_MODERNIZE_EXPAND
SOURCE_REPO=https://github.com/mrserluiz/Terra2.0
ORIGINAL_PLUGIN=Terra
ORIGINAL_MC=1.21.8
TARGET_MC=26.2
LANG=Java
PRIMARY_SOURCE=GitHub_repository_plus_local_decompiled_source

EVIDENCE_WORKFLOW:

PURPOSE:
Permitir que resultados de comandos executados localmente sejam persistidos no GitHub
e analisados posteriormente sem depender do histórico da conversa.

RULE:
Quando uma investigação exigir execução local:
1=Assistant fornece comando PowerShell.
2=Comando deve salvar saída em investigacao/...
3=User executa localmente.
4=User faz git add/commit/push pelo GitHub/PowerShell.
5=Assistant lê o arquivo diretamente do repositório.
6=Assistant analisa resultado.
7=Se necessário, Assistant fornece próximo comando.
8=Não solicitar que User copie grandes outputs para o chat quando um arquivo pode ser usado.

RESULT_NAMING:
Preferir nomes descritivos:
investigacao/results/<tema>.txt
investigacao/dumps/<tema>.txt

EXAMPLE:
Get-ChildItem -Recurse -Filter *.java |
    Select-String "Tectonic" |
    Out-File ".\investigacao\results\tectonic-search.txt" -Encoding UTF8

GITHUB_SYNC:
git add investigacao/
git commit -m "Investigation: <description>"
git push

CONTEXT_UPDATE:
TERRA2_CONTEXT.txt deve registrar quais evidence files existem e para que servem.

MEMORY_ROLE:
TERRA2_CONTEXT.txt não é apenas investigação.
É a memória operacional persistente do projeto e da interação técnica:
- objetivo;
- decisões;
- preferências de workflow;
- protocolo de investigação;
- estado atual;
- fatos;
- hipóteses;
- resultados;
- próximos passos;
- regras para continuidade.

ASSISTANT_BEHAVIOR:
Sempre lembrar:
- usar GitHub como fonte persistente;
- consultar TERRA2_CONTEXT antes de retomar trabalho complexo;
- consultar evidence files quando referenciados;
- fornecer comando de coleta quando faltar evidência local;
- avisar quando TERRA2_CONTEXT precisar ser atualizado;
- fornecer exatamente o bloco que deve ser adicionado/alterado;
- não exigir upload manual de grandes outputs no chat.

EVIDENCE_INDEX:

E001
FILE=investigacao/results/generator-provider-search.txt
PURPOSE=Busca por implementações de ChunkGeneratorProvider
STATUS=ANALYZED

E002
FILE=investigacao/results/tectonic-search.txt
PURPOSE=Localizar sistema Tectonic
STATUS=PENDING

E003
FILE=investigacao/default-pack-tree.txt
PURPOSE=Árvore do default.zip
STATUS=PENDING

MISSION:
1=Ressuscitar o plugin Terra de TerraFormação abandonado pelo desenvolvedor.
2=Entender completamente seu funcionamento antes de reescrever.
3=Atualizar inicialmente para Minecraft 26.2.
4=Preservar/reaproveitar o máximo possível do Core original.
5=Substituir/modernizar as partes dependentes da versão antiga do Minecraft.
6=Expandir o sistema de Packs.
7=Permitir criação de novos TerraPacks altamente personalizados.
8=Adicionar integração com DataPacks.
9=Permitir que DataPacks e TerraPacks coexistam de forma controlada.
10=Transformar o projeto em uma engine moderna de geração de terreno, não apenas em um patch temporário.

PROJECT_PRINCIPLE:
"Entender primeiro; modificar depois."
Não reconstruir o Terra do zero sem antes localizar e compreender o pipeline real.
Não assumir função de uma classe apenas pelo nome.
Toda afirmação estrutural importante deve ser classificada como FACT, HYPOTHESIS ou DECISION.

PERSISTENCE:
Este arquivo é a MEMÓRIA OPERACIONAL EXTERNA do projeto.
Ele existe para preservar continuidade entre conversas.
O GitHub é a fonte persistente do código, evidências, dumps e estado da investigação.
Ao iniciar/retomar investigação, ler este arquivo antes de tomar decisões arquiteturais.
Atualizar este arquivo quando um marco importante for concluído, uma hipótese for confirmada/refutada ou uma decisão arquitetural for tomada.
Não apagar fatos históricos; alterar status e adicionar novas evidências.

RULES:
R1=FACT != HYPOTHESIS.
R2=Não tratar hipótese como fato.
R3=Não repetir investigação já concluída sem motivo.
R4=Registrar arquivo/origem das evidências quando possível.
R5=Seguir o fluxo de execução real, não busca genérica por nomes.
R6=Não investigar Noise antes de entender quem chama o Noise.
R7=Não modernizar NMS antes de compreender o Core.
R8=Preservar arquitetura útil; substituir somente o necessário.
R9=Manter separação entre Core Terra e Adapter Minecraft.
R10=Usar GitHub como fonte persistente do projeto.

ARCHITECTURE_CURRENT_MODEL:
ConfigPack
 -> ConfigPackTemplate
 -> ConfigPackImpl
 -> GeneratorProvider
 -> ChunkGenerator
 -> generateChunkData()

ConfigPack
 -> BiomeProvider

ConfigPack
 -> GenerationStage[]
 -> GenerationStage.populate()

Minecraft_boundary:
TerraBukkitPlugin
 -> BukkitChunkGeneratorWrapper
 -> NMSChunkGeneratorDelegate[v1_21_8]
 -> Terra ChunkGenerator

ARCHITECTURE_TARGET:
TerraCore
  |-- ConfigPack
  |-- GeneratorProvider
  |-- ChunkGenerator
  |-- BiomeProvider
  |-- GenerationStage
  |-- Noise/Terrain systems
  |-- Pack system
  |
  +--> MinecraftAdapter
         +--> Paper/Bukkit 26.2
         +--> NMS/version-specific bridge

TARGET_PACK_ARCHITECTURE:
TerraPack
 + pack metadata
 + generator configuration
 + biome configuration
 + stages
 + addons
 + terrain/noise configuration
 + structures/features/vegetation
 + optional DataPack integration

FACTS_CONFIRMED:

F001:
SOURCE=investigacao/ConfigPack.java
CLAIM=ConfigPack exposes getStages() and getGeneratorProvider().
STATUS=CONFIRMED

F002:
SOURCE=investigacao/ConfigPackImpl.java
LINE≈306
CLAIM=ConfigPackImpl.getStages() exists.
STATUS=CONFIRMED

F003:
SOURCE=investigacao/ConfigPackImpl.java
LINE≈341-342
CLAIM=ConfigPackImpl.getGeneratorProvider() returns template.getGeneratorProvider().
STATUS=CONFIRMED

F004:
SOURCE=investigacao/ConfigPackTemplate.java
LINE≈69
CLAIM=ConfigPackTemplate contains private @Meta ChunkGeneratorProvider generatorProvider.
STATUS=CONFIRMED

F005:
SOURCE=investigacao/ConfigPackTemplate.java
LINE≈114
CLAIM=ConfigPackTemplate exposes getGeneratorProvider().
STATUS=CONFIRMED

F006:
SOURCE=investigacao/ChunkGeneratorProvider.java
CLAIM=ChunkGeneratorProvider defines ChunkGenerator newInstance(ConfigPack).
STATUS=CONFIRMED

F007:
SOURCE=com/dfsek/terra/bukkit/TerraBukkitPlugin.java
LINE≈118
CLAIM=TerraBukkitPlugin executes pack.getGeneratorProvider().newInstance(pack).
STATUS=CONFIRMED

F008:
SOURCE=com/dfsek/terra/bukkit/generator/BukkitChunkGeneratorWrapper.java
LINE≈46
CLAIM=Wrapper delegates generation through ChunkGenerator.generateChunkData(...).
STATUS=CONFIRMED

F009:
SOURCE=com/dfsek/terra/bukkit/generator/BukkitChunkGeneratorWrapper.java
LINE≈76
CLAIM=Wrapper sets delegate using pack.getGeneratorProvider().newInstance(pack).
STATUS=CONFIRMED

F010:
SOURCE=com/dfsek/terra/bukkit/nms/v1_21_8/NMSChunkGeneratorDelegate.java
LINE≈43
CLAIM=1.21.8 NMS delegate obtains its Terra generator through pack.getGeneratorProvider().newInstance(pack).
STATUS=CONFIRMED

F011:
SOURCE=investigacao/ChunkGenerator.java
CLAIM=ChunkGenerator API defines generateChunkData(ProtoChunk, WorldProperties, BiomeProvider, int, int).
STATUS=CONFIRMED

F012:
SOURCE=investigacao/GenerationStage.java
CLAIM=GenerationStage is an independent generation-stage abstraction.
STATUS=CONFIRMED

F013:
SOURCE=investigacao/GenerationStageProvider.java
CLAIM=GenerationStageProvider defines GenerationStage newInstance(ConfigPack).
STATUS=CONFIRMED

F014:
SOURCE=com/dfsek/terra/bukkit/generator/BukkitBlockPopulator.java
CLAIM=pack.getStages().forEach(stage -> stage.populate(...)).
STATUS=CONFIRMED

F015:
SOURCE=com/dfsek/terra/config/pack/ConfigPackImpl.java
LINE≈81
CLAIM=ConfigPackImpl creates a ConfigPackTemplate instance.
STATUS=CONFIRMED

F016:
SEARCH=implements ChunkGeneratorProvider
RESULT=No Java match in current decompiled source.
STATUS=CONFIRMED_NEGATIVE

F017:
SEARCH=generatorProvider\s*=
RESULT=No Java match in current decompiled source.
STATUS=CONFIRMED_NEGATIVE

F018:
SEARCH=GeneratorProvider.*new
RESULT=No direct concrete GeneratorProvider construction found in current Java search.
STATUS=CONFIRMED_NEGATIVE

F019:
SOURCE=investigacao/ConfigPackTemplate.java
CLAIM=generatorProvider is stored as @Meta rather than visibly instantiated by ordinary Java assignment.
STATUS=CONFIRMED

F020:
SOURCE=search results
CLAIM=The concrete ChunkGeneratorProvider is not yet identified.
STATUS=CONFIRMED

F021:
SOURCE=search results
CLAIM=The actual mechanism that populates ConfigPackTemplate.generatorProvider is not yet identified.
STATUS=CONFIRMED

F022:
SOURCE=project history/investigation
CLAIM=Original plugin target/version is Minecraft 1.21.8.
STATUS=CONFIRMED

F023:
SOURCE=current project goal
CLAIM=Modern target is Minecraft 26.2.
STATUS=CONFIRMED

INVESTIGATION_NEGATIVES:
N001=No class found with "implements ChunkGeneratorProvider".
N002=No ordinary Java assignment found matching "generatorProvider =".
N003=No direct "new <ConcreteGeneratorProvider>" found by current searches.
N004=Do not conclude that the provider does not exist; current evidence indicates it is likely supplied through configuration/deserialization/registration.

HYPOTHESES:

H001:
CLAIM=generatorProvider is populated by the Terra configuration system rather than direct Java instantiation.
EVIDENCE=@Meta field + absence of concrete assignment.
CONFIDENCE=HIGH
STATUS=OPEN

H002:
CLAIM=Tectonic is involved in transforming configuration into typed objects such as GeneratorProvider.
EVIDENCE=Terra architecture uses @Meta and prior package references/known Terra configuration architecture.
CONFIDENCE=HIGH_BUT_UNPROVEN
STATUS=OPEN

H003:
CLAIM=The concrete GeneratorProvider can be located by tracing ConfigPackTemplate population and the Tectonic ConfigType/loader system.
CONFIDENCE=HIGH
STATUS=OPEN

H004:
CLAIM=GenerationStage is a post/base-population layer separate from ChunkGenerator.generateChunkData().
EVIDENCE=BukkitBlockPopulator explicitly iterates stages and calls populate().
CONFIDENCE=HIGH
STATUS=CONFIRMED_AS_ARCHITECTURAL_HYPOTHESIS; continue tracing exact timing.

H005:
CLAIM=The original Terra architecture is sufficiently modular to preserve a large part of the Core while replacing Minecraft-version-specific adapters.
CONFIDENCE=HIGH
STATUS=DESIGN_DIRECTION

CURRENT_PIPELINE:
A=ConfigPack
B=ConfigPackTemplate
C=configuration/deserialization/registration [UNKNOWN]
D=GeneratorProvider [UNKNOWN_CONCRETE]
E=ChunkGenerator [UNKNOWN_CONCRETE]
F=generateChunkData()
G=GenerationStage[]
H=BiomeProvider
I=Noise
J=features/structures/vegetation/etc.

CURRENT_EXECUTION_PATH_CONFIRMED:
TerraBukkitPlugin
 -> pack
 -> pack.getGeneratorProvider()
 -> provider.newInstance(pack)
 -> ChunkGenerator

BukkitChunkGeneratorWrapper
 -> pack.getGeneratorProvider()
 -> provider.newInstance(pack)
 -> delegate.generateChunkData(...)

BukkitBlockPopulator
 -> pack.getStages()
 -> GenerationStage.populate(...)

1.21.8_NMS:
NMSChunkGeneratorDelegate[v1_21_8]
 -> pack.getGeneratorProvider().newInstance(pack)

INVESTIGATION_ORDER:
1=ConfigPack
2=ConfigPackTemplate
3=ConfigPackTemplate population
4=Tectonic/@Meta/ConfigType/loader
5=GeneratorProvider concrete
6=ChunkGenerator concrete
7=generateChunkData()
8=GenerationStage pipeline
9=BiomeProvider concrete
10=Noise
11=default.zip structure/configuration
12=structures
13=features
14=vegetation
15=rivers
16=oceans
17=caves
18=world registration/preset integration
19=1.21.8 NMS adapter
20=26.2 adapter design
21=DataPack integration
22=TerraPack v2 design
23=new custom TerraFormation packs

DO_NOT_JUMP:
Não ir para Noise antes de identificar o ChunkGenerator concreto.
Não assumir que o nome de uma classe revela sua função.
Não substituir o Core antes de entender o pipeline.
Não remover o sistema de configuração original antes de entender Tectonic.
Não confundir Bukkit wrapper/NMS adapter com o algoritmo real de TerraFormação.

CURRENT_TARGET:
T003=Localizar mecanismo que preenche ConfigPackTemplate.generatorProvider.
T004=Localizar Tectonic/@Meta/ConfigType/loader relacionado.
T005=Localizar GeneratorProvider concreto.
T006=Seguir newInstance(pack).
T007=Localizar ChunkGenerator concreto.
T008=Seguir generateChunkData().

NEXT_COMMANDS:
Get-ChildItem -Recurse -Filter *.java | Select-String "com.dfsek.tectonic"
Get-ChildItem -Recurse -Filter *.java | Select-String "@Meta"
Get-ChildItem -Recurse -Filter *.java | Select-String "TypeRegistry"
Get-ChildItem -Recurse -Filter *.java | Select-String "Tectonic"
Get-Content ".\com\dfsek\terra\config\pack\ConfigPackImpl.java" | Select-Object -Skip 60 -First 220
Get-Content ".\com\dfsek\terra\config\pack\ConfigPackTemplate.java"

DEFAULT_PACK:
STATUS=NOT_YET_MAPPED
TARGET=packs/default.zip
ACTION=list_zip_tree_then_correlate_files_to_ConfigPackTemplate/@Meta

GITHUB_WORKFLOW:
LOCAL_CHANGE
 -> git add .
 -> git commit -m "..."
 -> git push
 -> assistant reads GitHub
 -> investigation continues

FILES_CURRENTLY_EXPECTED:
investigacao/ConfigPack.java
investigacao/ConfigPackImpl.java
investigacao/ConfigPackTemplate.java
investigacao/ChunkGenerator.java
investigacao/ChunkGeneratorProvider.java
investigacao/GenerationStage.java
investigacao/GenerationStageProvider.java
investigacao/TERRA2_CONTEXT.txt [THIS FILE]

FUTURE_CONTEXT_UPDATE_RULE:
Quando houver novo marco:
1=Adicionar FACTS novos.
2=Atualizar HYPOTHESES confirmadas/refutadas.
3=Atualizar CHECKLIST/STATUS.
4=Atualizar CURRENT_TARGET.
5=Registrar DECISIONS.
6=Registrar arquivos-fonte relevantes.
7=Preservar histórico importante.
8=Não reescrever fatos antigos sem registrar a mudança.

DECISIONS:

D001:
DECISION=Investigar antes de modernizar.
STATUS=ACTIVE

D002:
DECISION=Preservar Core modular do Terra quando tecnicamente viável.
STATUS=ACTIVE

D003:
DECISION=Separar Terra Core de Minecraft Adapter.
STATUS=ACTIVE

D004:
DECISION=Terra 2.0 deve suportar DataPacks e novos TerraPacks.
STATUS=ACTIVE

D005:
DECISION=GitHub é o repositório persistente da investigação e código.
STATUS=ACTIVE

D006:
DECISION=TERRA2_CONTEXT.txt é a memória operacional externa da investigação.
STATUS=ACTIVE

LONG_TERM_ARCHITECTURE_GOAL:
TerraCore
 -> Pack Engine
 -> Config Engine
 -> Generator Engine
 -> Biome Engine
 -> Noise/Terrain Engine
 -> Stage/Feature Engine
 -> DataPack bridge
 -> Minecraft 26.2 Adapter

DESIRED_RESULT:
Um Terra moderno que:
- gere terrenos personalizados;
- permita novos TerraPacks;
- permita configuração profunda;
- possa reutilizar DataPacks;
- possa combinar sistemas de geração;
- minimize dependência de NMS;
- seja atualizável para futuras versões do Minecraft;
- mantenha arquitetura modular;
- preserve o máximo possível do conhecimento/algoritmos do Terra original.

END_STATE:
INVESTIGATION_ACTIVE
CURRENT_MILESTONE=FIND_CONCRETE_GENERATOR_PROVIDER
NEXT_MILESTONE=TRACE_CHUNK_GENERATOR_AND_REAL_TERRAFORMATION_PIPELINE

CHECKLIST:

[✓] GitHub repository
[✓] Decompiled Terra source
[✓] ConfigPack
[✓] ConfigPackImpl
[✓] ConfigPackTemplate
[✓] ChunkGenerator API
[✓] ChunkGeneratorProvider API
[✓] GenerationStage API
[✓] GenerationStageProvider API
[✓] Bukkit generation bridge
[✓] NMS 1.21.8 bridge
[✓] GeneratorProvider call path
[ ] ConfigPackTemplate population
[ ] Tectonic loader
[ ] @Meta resolution
[ ] ConfigType/registry
[ ] GeneratorProvider concrete
[ ] ChunkGenerator concrete
[ ] generateChunkData pipeline
[ ] GenerationStage concrete implementations
[ ] BiomeProvider concrete
[ ] Noise system
[ ] default.zip mapping
[ ] Terrain configuration
[ ] Rivers
[ ] Oceans
[ ] Caves
[ ] Structures
[ ] Vegetation
[ ] Features
[ ] World registration
[ ] NMS 26.2 adapter
[ ] DataPack bridge
[ ] TerraPack v2
[ ] Custom TerraFormation packs

MILESTONE:M001
NAME=Core API execution path identified
STATUS=COMPLETED

RESULT:
ConfigPack -> GeneratorProvider -> ChunkGenerator
and
ConfigPack -> GenerationStage[]
are confirmed execution paths.

[ACTIVE INVESTIGATION]
CURRENT = M002H
STATUS = IN_PROGRESS

[INVESTIGATION RULE]
Não avançar para componentes matemáticos individuais antes de descobrir
como o pipeline de configuração os instancia e conecta.

[EVIDENCE RULE]
FACT = confirmado por código/bytecode/evidência do repositório.
INFERENCE = conclusão arquitetural ainda sujeita a confirmação.
HYPOTHESIS = possibilidade ainda não comprovada.
DECISION = decisão tomada para o projeto Terra 2.0.

[CONTINUITY RULE]
Antes de continuar uma investigação:
1. Ler Manual_Memory_System(Terra2).txt no GitHub.
2. Consultar os results relevantes do GitHub.
3. Manter a nomenclatura M002/M002H/etc.
4. Registrar descobertas arquiteturais importantes antes de mudar de marco.
5. Sempre fornecer os comandos PowerShell necessários para gerar novos arquivos de evidência.
6. Usuário atualiza a memória manual pelo GitHub quando solicitado.
NAME=Identify configuration mechanism that populates generatorProvider
STATUS=ACTIVE

