# SQLi Academy

Plataforma web educacional sobre **SQL Injection**, desenvolvida como Trabalho de Conclusão de Curso de Ciência da Computação na URI Erechim.

- **Autor:** Eduardo Ongaratto
- **Orientador:** Prof. Fabio Zanin

O aluno segue uma trilha de 3 módulos. Cada módulo tem um texto introdutório e um laboratório prático, e o módulo seguinte só é liberado quando o anterior é concluído.

| Módulo | Conteúdo | Laboratório |
|---|---|---|
| 1. Introdução | O que é, por que acontece, impacto e relevância | **Atacar:** burlar um login fictício e ver a query alterada em tempo real |
| 2. Identificação | Pontos de entrada e sinais de código vulnerável | **Identificar:** apontar a linha vulnerável em trechos de código Java (ou "nenhuma", se o trecho for seguro) |
| 3. Prevenção | Consultas parametrizadas, ORM e boas práticas | **Corrigir:** escolher a versão segura entre 4 "correções" e repetir o ataque contra ela |

Cada laboratório resolvido vale 100 pontos (máximo 300). O aluno acompanha o progresso no dashboard, e o administrador tem um relatório com o status de todos os alunos.

## Segurança dos laboratórios

Os laboratórios são **simulados**. A entrada do aluno serve apenas para montar e exibir a query resultante como texto, e a validação é feita por regras em Java (uma classe validadora por laboratório, no pacote `lab`). **Nenhuma entrada do aluno é executada no banco de dados.** Todo acesso real ao banco é feito via JPA com consultas parametrizadas.

## Tecnologias

- Java 21, Spring Boot 4.1 e Maven
- Spring Web MVC, Thymeleaf, Spring Security (BCrypt), Spring Data JPA e Bean Validation
- MySQL 8
- Bootstrap 5 (via CDN) e JavaScript puro nos laboratórios

## Pré-requisitos

- JDK 21
- MySQL 8 rodando em `localhost:3306`
- Maven não é necessário: o projeto inclui o Maven Wrapper (`mvnw`)

## Como executar

1. Clone o repositório:

   ```bash
   git clone https://github.com/EduardoOngaratto/sqliacademy.git
   cd sqliacademy
   ```

2. Defina as variáveis de ambiente:

   | Variável | Uso |
   |---|---|
   | `DB_PASSWORD` | Senha do usuário `root` do MySQL (vazia, se não definida) |
   | `ADMIN_PASSWORD` | Senha do administrador criado na primeira execução (8 a 64 caracteres). Sem ela, o admin não é criado. |

   Windows (PowerShell):

   ```powershell
   $env:DB_PASSWORD = "sua_senha_mysql"
   $env:ADMIN_PASSWORD = "senha_do_admin"
   ```

   Linux/macOS:

   ```bash
   export DB_PASSWORD=sua_senha_mysql
   export ADMIN_PASSWORD=senha_do_admin
   ```

3. Inicie a aplicação:

   ```bash
   ./mvnw spring-boot:run      # Linux/macOS
   mvnw.cmd spring-boot:run    # Windows
   ```

4. Acesse <http://localhost:8080>.

O banco `sqli_academy` é criado automaticamente (`createDatabaseIfNotExist=true`) e as tabelas são geradas pelo Hibernate. Na inicialização, a carga inicial cadastra os módulos, os laboratórios, os textos dos módulos e o usuário administrador.

Para alterar o usuário ou a URL do banco, edite `src/main/resources/application.properties`.

## Acesso

- **Aluno:** crie uma conta em `/cadastro`.
- **Administrador:** entre com `admin@sqliacademy.local` e a senha definida em `ADMIN_PASSWORD`. O relatório de progresso fica em `/admin`.

## Testes

Os testes unitários dos validadores dos laboratórios e os testes funcionais dos fluxos principais (autenticação, trilha, laboratórios, dashboard e relatório) usam JUnit e o contexto do Spring. Eles precisam do MySQL rodando, com as mesmas variáveis de ambiente.

```bash
./mvnw test
```

## Estrutura do projeto

```
src/main/java/br/edu/uri/sqliacademy
├── config        SecurityConfig e carga inicial de dados
├── entity        Entidades JPA
├── repository    Repositórios Spring Data
├── service       Regras de negócio (progresso, laboratórios, usuários)
├── controller    Controllers do aluno
│   └── admin     Relatório administrativo
├── dto           Objetos de formulário e de exibição
└── lab           Motor de simulação: interface do validador e um validador por laboratório

src/main/resources
├── conteudos     Textos dos módulos (HTML)
├── templates     Páginas Thymeleaf
└── static        CSS e JavaScript
```
