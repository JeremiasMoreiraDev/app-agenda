# App Agenda — catálogo de produtos

Este guia descreve a execução local do catálogo: API Java com MySQL e aplicativo Android em Java/XML. A integração do catálogo com a API foi testada em celular físico. A integração com a navegação e autenticação do aplicativo completo da equipe ainda está pendente.

## Organização

| Pasta | Conteúdo | Onde abrir |
| --- | --- | --- |
| `Api Android/` | Backend Spring Boot e API de produtos | IntelliJ IDEA |
| `CatalogoProdutos/` | Aplicativo Android do catálogo | Android Studio |

São dois projetos Gradle separados. Abra cada pasta na IDE correspondente.

## Funcionalidades do catálogo

- Listar produtos ativos, ordenados pelo nome.
- Mostrar nome, categoria, preço, disponibilidade, descrição e imagem por URL.
- Cadastrar e editar produtos.
- Desativar produtos: o registro permanece no banco com `ativo=false` e deixa de aparecer na listagem.
- Mostrar estados de carregamento, lista vazia e falha de comunicação.

## Requisitos registrados no código

| Componente | Configuração |
| --- | --- |
| Backend | Java/JDK 17, Spring Boot 3.2.5 |
| Banco para execução local | MySQL, porta 3306, banco `demo` |
| Android | Android Studio compatível com as versões Gradle/AGP abaixo |
| Android Gradle Plugin | 9.4.1 |
| Gradle do Android | 9.6.0, fornecido pelo wrapper |
| JVM do daemon Gradle Android | Toolchain 25 em `gradle/gradle-daemon-jvm.properties` |
| Código Java Android | Compatibilidade de linguagem Java 11 |
| Android SDK | compileSdk e targetSdk 37; minSdk 24 |
| Imagens | Glide 5.0.9 |

Essas são as versões declaradas no repositório, não uma indicação de versões mais recentes. O JDK 17 do backend e a JVM do Gradle Android têm funções distintas. Mantenha as configurações de cada projeto ao importar.

## 1. Obter o código

Para uma nova cópia da entrega:

```powershell
git clone --branch luan/catalogo-completo https://github.com/JeremiasMoreiraDev/app-agenda.git app-agenda-catalogo
```

A branch de entrega é revisada no [Pull Request nº 2](https://github.com/JeremiasMoreiraDev/app-agenda/pull/2).

## 2. Preparar o banco

1. Inicie o serviço MySQL.
2. Entre no MySQL Workbench com seu usuário local.
3. Execute:

```sql
CREATE DATABASE IF NOT EXISTS demo
CHARACTER SET utf8mb4;
```

A configuração está em `Api Android/src/main/resources/application.yaml`. Ela aponta para `localhost:3306/demo`, usuário `root`, porta HTTP `8080` e usa `ddl-auto: update`. A senha `sua_senha` no arquivo é um marcador e precisa ser substituída em tempo de execução.

No IntelliJ, abra **Run → Edit Configurations**, selecione a configuração de execução de `DemoApplication` e preencha **Environment variables**:

| Variável | Valor |
| --- | --- |
| `SPRING_DATASOURCE_USERNAME` | Seu usuário MySQL local |
| `SPRING_DATASOURCE_PASSWORD` | Sua senha MySQL local |
| `SPRING_DATASOURCE_URL` | Opcional: outra URL JDBC se seu banco não estiver em localhost:3306/demo |

Use o editor de variáveis da IDE para informar cada nome e valor. Não envie senhas reais ao GitHub.

O Hibernate cria/atualiza as tabelas mapeadas quando a aplicação inicia e consegue acessar o banco. Use um banco de desenvolvimento. Os produtos cadastrados no computador de um integrante não são transferidos pelo Git: cada instalação tem seus próprios dados.

## 3. Executar a API

1. No IntelliJ, abra a pasta `Api Android`.
2. Configure JDK 17 para o projeto e aguarde a importação Gradle.
3. Configure as variáveis do banco conforme a seção anterior.
4. Execute a classe `com.example.demo.DemoApplication`.
5. Aguarde a conclusão da inicialização no console.
6. No navegador do computador, abra:

```text
http://localhost:8080/api/produtos
```

Uma resposta `[]` significa que a API respondeu e não há produtos ativos cadastrados. Uma lista JSON contém os produtos disponíveis. Mantenha a API em execução durante os testes do Android.

## 4. Abrir o aplicativo Android

1. No Android Studio, escolha **Open** e selecione a pasta `CatalogoProdutos`.
2. Aguarde a sincronização Gradle.
3. Instale os componentes SDK solicitados pelo projeto, incluindo a plataforma 37.
4. Se houver erro de compatibilidade, compare as versões configuradas com a tabela de requisitos antes de alterar arquivos.
5. Configure o SDK local no Android Studio. O arquivo `local.properties` contém o caminho do SDK de cada máquina e não deve ser versionado.

## 5. Conectar um celular por USB

1. Ative as opções de desenvolvedor e a depuração USB no celular Android.
2. Conecte-o ao computador com cabo de dados.
3. Desbloqueie a tela e aceite a autorização de depuração USB.
4. No PowerShell do Windows, execute:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices -l
```

O dispositivo deve aparecer como `device`. Se aparecer `unauthorized`, aceite a autorização no celular.

Copie o identificador exibido na primeira coluna e substitua `SERIAL_DO_CELULAR` no comando:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s SERIAL_DO_CELULAR reverse tcp:8080 tcp:8080
```

Esse comando encaminha a porta 8080 do celular para a porta 8080 do computador pelo ADB. A API do aplicativo está configurada como:

```text
http://127.0.0.1:8080/api/produtos
```

Sem o encaminhamento, esse endereço aponta para o próprio celular. Refaça o comando após reconectar o aparelho ou reiniciar o ADB, se necessário. A opção `-s` seleciona o aparelho correto mesmo quando há um emulador aberto.

Se o SDK estiver em outro local, ajuste o caminho do `adb.exe`.

5. No navegador do celular, abra `http://127.0.0.1:8080/api/produtos` para conferir a conexão.
6. No Android Studio, selecione o celular e clique em **Run ▶**.
7. No aplicativo, toque em **Atualizar produtos**.

A configuração atual destina-se a testes locais. Ela permite HTTP somente para `127.0.0.1` em `network_security_config.xml`. Para uma entrega que funcione sem esse computador e sem USB, será necessário hospedar a API e configurar seu endereço HTTPS no aplicativo.

## 6. Testar o catálogo

Use apenas dados fictícios nesta validação:

1. Toque em **Cadastrar produto**.
2. Informe nome e categoria, preço `1200.00` e estoque `5`. O ponto funciona como separador decimal quando o teclado não oferece vírgula. Não use separador de milhares.
3. Informe uma descrição. A imagem é opcional; quando usada, informe uma URL HTTPS direta para uma imagem autorizada para uso.
4. Salve e confira se o produto aparece na listagem.
5. Abra os detalhes e verifique os dados.
6. Edite o estoque para `7`, salve e confira detalhes e catálogo.
7. Desative o produto e confirme que ele desaparece da lista após atualizar.

O aplicativo carrega a imagem pela URL; não existe upload de fotos nesta entrega. O celular precisa acessar o servidor da imagem. Uma imagem ausente ou indisponível usa o ícone de substituição.

Listagem, cadastro, edição e desativação foram testados manualmente por Luan em um Moto G24 conectado à API local. Essa validação não equivale a uma suíte automatizada completa nem à validação do aplicativo inteiro da equipe.

## API do catálogo

| Método | Caminho | Comportamento |
| --- | --- | --- |
| GET | `/api/produtos` | Lista somente ativos, em ordem de nome |
| GET | `/api/produtos/{id}` | Busca por ID; pode retornar produto desativado |
| POST | `/api/produtos` | Cadastra produto ativo; resposta 201 |
| PUT | `/api/produtos/{id}` | Atualiza os dados; resposta 200 |
| DELETE | `/api/produtos/{id}` | Desativa sem remover o registro; resposta 204 |

Exemplo de corpo JSON para cadastro ou edição:

```json
{
  "nome": "Painel Solar - TESTE",
  "descricao": "Produto fictício para demonstração.",
  "categoria": "Painéis solares",
  "preco": 500.00,
  "quantidadeEstoque": 10,
  "urlImagem": ""
}
```

Nome e categoria são obrigatórios. Preço e estoque não podem ser negativos. O preço aceita até 10 dígitos inteiros e 2 decimais. Limites: nome 150, categoria 100, descrição 2000 e URL 1000 caracteres. A API gera o ID e define `ativo=true` no cadastro.

## Problemas comuns

| Sintoma | O que verificar |
| --- | --- |
| API não inicia | JDK 17, serviço MySQL, banco demo, credenciais e console do IntelliJ |
| `Access denied` | Usuário e senha MySQL configurados na execução |
| Porta 8080 em uso | Outra instância da API; mantenha somente a instância desejada |
| `Failed to connect` ou `timeout` | Teste a API no computador, confira USB/ADB e refaça o encaminhamento |
| `more than one device/emulator` | Use `-s SERIAL_DO_CELULAR` para escolher o aparelho |
| `unauthorized` | Desbloqueie o celular e autorize a depuração |
| Nenhum produto disponível | A lista pode estar vazia; cadastre um produto de teste |
| Imagem não aparece | URL HTTPS direta, acesso à internet e disponibilidade da imagem |

Após uma falha ao salvar ou desativar, atualize o catálogo antes de repetir a operação: uma resposta perdida não garante que o servidor deixou de processar a solicitação.

## Integração e pendências

- Integrar as telas à navegação e ao login da equipe.
- Implementar autorização no servidor para restringir cadastro, edição e desativação aos responsáveis. Ocultar botões no Android não substitui essa proteção.
- Definir o comportamento de compra com a equipe: carrinho, pedido e pagamento não estão implementados.
- Confirmar produtos, fotos e ajustes finais do design.
- Definir hospedagem da API e configuração do ambiente compartilhado.
- Executar testes de integração com as demais funcionalidades da equipe.

O catálogo é um aplicativo Android separado dentro deste repositório. Mesclar os arquivos não conecta automaticamente a navegação, login ou outros módulos.

## Trabalhar na cópia correta

Para continuar esta entrega, abra `CatalogoProdutos` dentro da cópia do repositório da equipe usada na branch `luan/catalogo-completo`. Alterações em outro projeto Android independente não são copiadas automaticamente para essa pasta.

Antes de enviar alterações, confira `git status` e a branch. Não versione senhas, `local.properties`, configurações pessoais da IDE ou pastas de build. As alterações devem passar pela revisão da equipe antes da incorporação à `main`.


## Importar o produto de demonstração

Com a API e o MySQL em execução, abra o PowerShell na pasta que contém
`produtos-exemplo.json` e execute:

```powershell
$produtos = Get-Content -Raw -Encoding UTF8 ".\produtos-exemplo.json" | ConvertFrom-Json

foreach ($produto in $produtos) {
    $json = $produto | ConvertTo-Json -Depth 5
    $corpo = [System.Text.Encoding]::UTF8.GetBytes($json)

    Invoke-RestMethod -Method Post `
        -Uri "http://localhost:8080/api/produtos" `
        -ContentType "application/json; charset=utf-8" `
        -Body $corpo
}
```

Execute somente uma vez no banco de demonstração: repetir a importação
cria outro produto. Se o exemplo já estiver cadastrado, não execute.
Depois, toque em "Atualizar produtos" no aplicativo.

A foto é ilustrativa: "Monocrystalline silicon solar panel", de ArticCynda,
disponibilizada como CC0 no Wikimedia Commons:
https://commons.wikimedia.org/wiki/File:Monocrystalline_silicon_solar_panel.jpg
