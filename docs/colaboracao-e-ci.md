# Colaboração, revisão de PRs e integração contínua

## Fluxo da equipe

```text
Issue → branch de trabalho → commits → Pull Request para main
      → CI aprovada + revisão de outro integrante → merge
```

Aprovar é registrar uma revisão em **Files changed → Review changes → Approve**.
Fazer merge é integrar o PR na branch de destino. São ações diferentes: o autor
pode fazer o merge do próprio PR, se as regras e permissões permitirem, mas
não pode aprovar seu próprio PR. Pedir e aguardar a revisão de um colega é
um acordo da equipe; a configuração definida exige **0 aprovações**.

O GitHub impede o autor de aprovar seu próprio PR, independentemente das regras.
Nosso bloqueio técnico exige PR e impede escrita direta na main; não exige uma
aprovação formal para o merge. Arquivos deste repositório, sozinhos, não ativam
nem alteram essa proteção no GitHub.

## 1. Dar acesso aos integrantes

No repositório **Khallarrary/lexflow-crm**:

1. Abra **Settings → Collaborators**, na seção de acesso.
2. Use **Add people** e selecione o usuário GitHub do integrante.
3. O integrante deve aceitar o convite.

Este repositório pertence a uma conta pessoal. Colaboradores têm acesso de
escrita: podem criar branches, revisar PRs e fazer merge quando as regras forem
satisfeitas. Não precisam ser administradores nem proprietários para que suas
aprovações sejam usadas nas revisões da equipe.

Se no futuro o projeto for para uma organização, dê à equipe a função **Write**
no repositório; funções administrativas não são necessárias para revisar código.

## 2. Conferir o plano

O repositório estava **privado** quando esta configuração foi preparada.
Rulesets de branches e proteção de branches em repositórios privados exigem
GitHub Pro para conta pessoal, ou o plano organizacional compatível.
O benefício estudantil pode fornecer Pro; confirme que ele está ativo.

Em repositório público, essas proteções estão disponíveis no GitHub Free.
Não altere a visibilidade só para seguir este guia sem uma decisão da equipe.
Se o plano não permitir, as revisões podem continuar como acordo, mas não se
deve apresentar esse acordo como bloqueio técnico de merge.

## 3. Subir a CI antes de exigir seu status

Os arquivos da CI precisam ser commitados e enviados ao GitHub. Abra o primeiro
PR com eles e aguarde o workflow **CI Java**. O check é **build-e-testes**.

Somente depois da primeira execução, selecione esse check como obrigatório nas
regras da main. O GitHub pode não mostrar um check que ainda não foi executado.
Uma execução verde não configura a regra automaticamente.

Se Actions estiver desabilitado, um administrador precisa habilitá-lo em
**Settings → Actions → General**, respeitando a política da conta. O workflow
usa as actions oficiais **actions/checkout** e **actions/setup-java**.

## 4. Proteger main com um ruleset

Como proprietário, abra **Settings → Rules → Rulesets → New ruleset → New branch
ruleset**. Configure:

| Campo ou regra | Valor sugerido |
|---|---|
| Ruleset name | protecao-main |
| Enforcement status | Active |
| Bypass list | Vazia, para ninguém ficar dispensado, inclusive o proprietário |
| Target branches | Include by pattern: main |
| Restrict deletions | Marcado |
| Block force pushes | Marcado |
| Require a pull request before merging | Marcado |
| Required approvals | 0 |
| Dismiss stale pull request approvals when new commits are pushed | Desmarcado |
| Require approval of the most recent reviewable push | Desmarcado |
| Require review from Code Owners / specific teams | Desmarcado |
| Restrict updates | Desmarcado |
| Require conversation resolution before merging | Marcado |
| Require status checks to pass | Ativar e adicionar build-e-testes após sua primeira execução |
| Require branches to be up to date before merging | Marcar ao ativar os status checks |

Salve o ruleset. Não configure restrição de criação/atualização por usuário que
concentre todo o merge no proprietário; o objetivo é permitir que colaboradores
integrem PRs quando as condições técnicas forem satisfeitas, mantendo a revisão
como compromisso da equipe.

O primeiro ruleset foi criado com uma aprovação obrigatória. Se ele ainda
estiver assim no painel, edite-o conforme a tabela e salve. A mudança para zero
aprovações foi definida depois; esta documentação não confirma que o ajuste
remoto já foi salvo.

Não é necessário exigir CODEOWNERS para esta regra. Um CODEOWNERS apontando
somente para o proprietário, somado à exigência de aprovação dos code owners,
poderia voltar a concentrar as aprovações nele.

**O que as regras fazem:**

- Exigem que as mudanças cheguem por PR, em vez de push direto na main.
- Permitem merge pelo autor sem aprovação formal, desde que as outras condições sejam satisfeitas.
- Mantêm a revisão do colega como acordo de colaboração.
- Bloqueiam merge com conversas pendentes, conforme a regra de resolução.
- Depois de exigir o check build-e-testes, bloqueiam merge com esse check pendente ou falhando.
- Com a opção de atualização ativada, exigem atualizar a branch quando ela estiver atrás da main.

**Por que desmarcar a aprovação do último push:** essa opção pode exigir outra
aprovação mesmo com a contagem geral em zero. Não faz parte do bloqueio técnico
desejado. Revisões adicionais após correções continuam sendo combinadas pelos
integrantes.

Mesmo com bypass vazio, o proprietário continua podendo editar as configurações
do repositório. A regra controla o fluxo normal de contribuição, não remove os
poderes administrativos da conta.

## 5. Como confirmar que a proteção funciona

Em um PR de teste para main, confira:

1. Um push direto na main é rejeitado: mudanças precisam chegar por PR.
2. O PR não exige aprovação apenas por estar sem review; a contagem deve ser zero.
3. O autor pode fazer merge se tiver escrita e todas as outras condições estiverem satisfeitas.
4. Quando adicionado como obrigatório, o check build-e-testes precisa ficar verde.
5. Conversas pendentes ainda precisam ser resolvidas. Uma revisão com pedido de alterações também deve ser discutida; zero aprovações não dispensa conferir os demais bloqueios exibidos pelo GitHub.

Não faça force push ou alterações destrutivas na main para testar as regras.

## O que a esteira executa

O arquivo [.github/workflows/ci.yml](../.github/workflows/ci.yml) executa:

1. Em PRs para main, pushes na main e por disparo manual em Actions.
2. Cria um runner Linux temporário com Java 21.
3. Inicia PostgreSQL 18 com banco exclusivo **lexflow_ci**.
4. Aguarda o banco ficar disponível pelo health check.
5. Executa **mvn clean verify**: compila, executa testes e empacota o JAR.
6. Os testes iniciam a aplicação em uma porta HTTP aleatória; Flyway cria as
   tabelas e Hibernate valida o mapeamento.
7. Um teste cadastra cliente por HTTP, confere 201 e verifica o registro no banco.
8. Outro envia nome em branco, confere 400 e verifica que nada foi persistido
   para aquele cadastro.
9. Ao final, runner e PostgreSQL são descartados. Em falhas de testes, os
   relatórios textuais aparecem nos logs.

Isso é **integração contínua (CI)**. Não exige uma aplicação já publicada.
O runner executa no GitHub, mas não constitui hospedagem do CRM. Deploy contínuo
para um ambiente de execução será uma etapa futura. Uso de Actions em
repositórios privados está sujeito à franquia e à política de cobrança da conta.

As credenciais lexflow_ci no YAML são descartáveis e exclusivas do serviço
temporário. Não use credenciais reais nem copie senhas locais para esse arquivo.
O workflow só precisa de **contents: read** e não faz deploy ou push automático.

## Executar os testes localmente

O perfil de teste está em
[application-test.properties](../src/test/resources/application-test.properties).
Ele usa um banco separado, sem depender de DB_USERNAME/DB_PASSWORD do IntelliJ.

Crie um banco dedicado a testes e conceda ao usuário de teste permissão de criar
objetos nele. Configure os valores conforme seu ambiente:

```powershell
$env:TEST_DATABASE_URL = 'jdbc:postgresql://localhost:5432/lexflow_ci'
$env:TEST_DATABASE_USERNAME = 'seu_usuario_de_teste'
$env:TEST_DATABASE_PASSWORD = 'sua_senha_de_teste'
mvn --batch-mode --no-transfer-progress clean verify
```

É necessário Maven no PATH para esse comando; o projeto ainda não tem Wrapper.
Também é possível executar a classe ClienteCadastroTest no IntelliJ, definindo
essas variáveis na configuração JUnit correspondente. Se a configuração JUnit
não estiver disponível, sincronize o Maven após a inclusão do starter de testes.

Use **somente banco de testes**. Os testes fazem gravações reais, e os cadastros
de sucesso ficam nele. Na CI isso não acumula porque cada job começa com um banco
novo. Alterar as variáveis de teste para apontar a um banco de desenvolvimento
ou produção não é parte do fluxo suportado.

## Referências

Para usar a análise automática no fluxo de revisão, veja
[Sourcery no dia a dia](../README.md#sourcery-no-dia-a-dia).
A instalação do app é uma etapa separada da esteira Java. A revisão de outro
integrante permanece como acordo da equipe, sem aprovação obrigatória no ruleset.

- [Convidar colaboradores de repositório pessoal](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/inviting-collaborators-to-a-personal-repository)
- [Revisar PRs e restrição de autoaprovação](https://docs.github.com/en/pull-requests/how-tos/review-pull-requests/reviewing-proposed-changes-in-a-pull-request)
- [Regras disponíveis em rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/available-rules-for-rulesets)
- [Disponibilidade de rulesets por plano](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets)
- [PostgreSQL temporário no GitHub Actions](https://docs.github.com/en/actions/tutorials/use-containerized-services/create-postgresql-service-containers)
