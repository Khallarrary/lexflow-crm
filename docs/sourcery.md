# Revisão automática com Sourcery

## Estado da integração

Este guia prepara a configuração e as regras de revisão do LexFlow. **A instalação
do GitHub App ainda precisa ser concluída pelo proprietário autenticado.** Adicionar
este documento não instala o app nem ativa revisões.

Sourcery revisa PRs; o GitHub Actions executa build e testes. Mantenha ambos como
apoio à revisão de um colega, conforme o acordo da equipe.

## Plano e instalação

O repositório estava privado quando este guia foi preparado. Na consulta de
15/11/2026, o fornecedor oferecia 14 dias de teste e exigia plano pago para
continuar usando repositórios privados. Repositórios públicos têm opção gratuita
para open source. Confira os [preços atuais](https://www.sourcery.ai/pricing)
antes de concluir o cadastro ou contratar um plano.

1. Acesse o [painel Sourcery](https://app.sourcery.ai/) e entre com GitHub.
2. Na instalação do GitHub App, selecione a conta **Khallarrary**.
3. Escolha **Only select repositories** e selecione apenas **lexflow-crm**.
4. Confira as permissões solicitadas antes de autorizar. A instalação permite
   ao serviço acessar código privado e interagir com PRs e outros recursos
   apresentados na tela; verifique os escopos atuais no GitHub.
5. Conclua a instalação somente com o plano e escopo de acesso desejados.

Também é possível começar pela [página oficial no Marketplace](https://github.com/marketplace/sourcery-ai).
Não forneça senhas ou tokens em arquivos do projeto. A integração pelo GitHub App
é separada do workflow de CI, sem necessidade de incluir uma chave no POM ou YAML
da esteira.

## Configuração sugerida para o LexFlow

No painel, abra as configurações do repositório. Se ele estiver herdando padrões
de outros projetos, use a configuração específica de **lexflow-crm**, para não
alterar o comportamento de outros repositórios.

- Habilite revisão nos PRs e comentários de revisão.
- Use perfil **Balanced**.
- Selecione português no campo de idioma, se disponível na lista atual.
- Para restringir à branch de destino, use o filtro de base branch **^main$**.
- Mantenha revisão de drafts desativada; peça revisão quando o PR estiver pronto.
- Exclua dos diffs revisados **docs/images/**, **target/** e **.idea/**.
- Mantenha código Java, migrations, POM e workflows no escopo da revisão.
- Se houver opção de aprovação automática de PRs, mantenha-a desativada. O acordo
  deste projeto continua exigindo aprovação de um colega.

As opções são configuradas no painel; revise a interface atual e aplique as
mudanças. O estado dessas opções não pode ser inferido apenas dos arquivos Git.

## Regras de arquitetura para cadastrar

Na aba **Review rules**, crie as regras abaixo usando os padrões e os textos
propostos. São instruções para o revisor automático, não bloqueios determinísticos
nem substitutos de testes. A análise pode ter falsos positivos ou deixar passar
problemas; o colega precisa conferir as sugestões.

### 1. Controllers e contratos HTTP

**Path patterns:** `src/main/java/com/lexflow/api/**/*.java`

```text
O projeto é um monólito em camadas. Controllers delegam operações aos services de
app.service; não devem acessar repositories, executar SQL ou concentrar regras
de negócio. Entrada e saída HTTP usam DTOs de api.dto. Verifique @Valid nas
entradas com validações Jakarta. O mapeamento explícito entre request, entidade
e response é permitido e segue o primeiro cadastro. Não exija uma biblioteca
de mapeamento nem a criação de camadas adicionais sem necessidade.
```

### 2. Services e transações

**Path patterns:** `src/main/java/com/lexflow/app/**/*.java`

```text
Services coordenam os casos de uso e usam modelos de domain e repositories de
infra. Não devem depender de api.dto, controllers, ResponseEntity ou códigos
HTTP. Confira o limite transacional das operações de escrita que precisam ser
atômicas e a coordenação das verificações que exigem consultas. Injeção por
construtor é o padrão. Não peça interface para todo service: classes concretas
são uma decisão deste projeto.
```

### 3. Domínio

**Path patterns:** `src/main/java/com/lexflow/domain/**/*.java`

```text
Domain contém entidades, enums e regras que devem valer independentemente da
entrada HTTP. Não deve depender de api, app ou repositories de infra. Anotações
JPA na própria entidade são uma decisão aceita; não solicite uma segunda
entidade de persistência nem a remoção dessas anotações só por preferência
arquitetural. Confira se comportamentos novos preservam as regras do negócio.
```

### 4. Persistência

**Path patterns:** `src/main/java/com/lexflow/infra/repository/**/*.java`

```text
Repositories persistem e consultam modelos do domínio. Não recebem DTOs da API
nem constroem respostas HTTP. Aproveite JpaRepository antes de sugerir consultas
ou implementações redundantes. Confira consultas novas, seus parâmetros e o
efeito esperado. É permitido app.service depender desses repositories; o projeto
não exige criar outra interface de repository no domínio.
```

### 5. Migrations

**Path patterns:** `src/main/resources/db/migration/*.sql`

```text
Flyway é o responsável pelas mudanças de estrutura. Migrations seguem
V<numero>__<descricao>.sql, com dois underscores e versões únicas. Sinalize
alterações em migrations já aplicadas e compartilhadas, indicando nova migration
como caminho de evolução. Confira constraints, relacionamentos e impacto sobre
dados existentes, especialmente em campos obrigatórios ou operações destrutivas.
Não sugira ddl-auto=update como substituto do histórico de migrations.
```

### 6. Configuração e CI

**Path patterns:** `.github/workflows/**`, `src/main/resources/**`, `src/test/**`, `pom.xml`

```text
Confira compatibilidade com Java 21 e o parent do Spring Boot. Credenciais reais
devem vir de variáveis de ambiente. Os valores lexflow_ci do serviço PostgreSQL
na CI são descartáveis e intencionais; não os trate como senha de produção.
Testes devem usar banco exclusivo, sem apontar para o banco de desenvolvimento.
Workflows de PR não precisam de permissão de escrita, deploy ou segredos reais
para executar build e testes. Priorize problemas concretos sobre preferências.
```

## Confirmar a ativação

Após instalar e habilitar o app, abra um PR não draft para `main`. Confirme que
o bot publica uma revisão; a presença deste guia não comprova a ativação.

Se precisar pedir uma revisão manualmente, o comando documentado é:

```text
@sourcery-ai review
```

Esse texto deve ser publicado por um integrante como comentário no PR quando
quiser acionar o serviço. Nenhum comentário foi enviado automaticamente durante
a preparação desta integração.

Confira o resultado em três partes:

1. O check **build-e-testes** valida compilação, testes e empacotamento.
2. Sourcery sugere melhorias e verifica as convenções cadastradas.
3. Um colega lê o código, discute os pontos e registra a aprovação humana.

O check do Sourcery, se habilitado, é separado da exigência de aprovação humana.
Durante a avaliação do serviço, mantenha o check build-e-testes obrigatório e
avalie com a equipe se o check adicional deve bloquear merges. Não configure o
bot como substituto do revisor nem como exceção para contornar a proteção da main.

## Referências

- [Instalação no GitHub](https://docs.sourcery.ai/start/quickstart-github/)
- [Configurações de revisão](https://docs.sourcery.ai/reviews/configure/)
- [Regras personalizadas](https://docs.sourcery.ai/reviews/review-rules/)
- [Planos e preços](https://www.sourcery.ai/pricing)
