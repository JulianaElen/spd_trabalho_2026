# Freeway — Jogo Multiplayer

Jogo estilo Frogger onde cada jogador controla um carro que deve atravessar a estrada desviando das galinhas.

Suporta até **2 jogadores** em rede, com três implementações de comunicação diferentes.

## Pré-requisitos

- Java 17+
- Maven (incluído no IntelliJ em `plugins/maven/lib/maven3/bin/mvn.cmd`)
- Conexão com a internet (apenas para a versão MQTT)

## Como jogar

- **Setas do teclado** — mover o carro
- Chegue ao topo da estrada para marcar pontos
- Evite as galinhas

---

## Versão 1 — Sockets

Execute `rodar-sockets.bat` duas vezes: uma para o servidor e outra para o cliente.

**Terminal 1 — Servidor:**
```
rodar-sockets.bat  →  escolha 1
```

**Terminal 2 — Cliente (Jogador 1):**
```
rodar-sockets.bat  →  escolha 2  →  host: localhost
```

Para **2 jogadores**, abra um terceiro terminal e repita o passo do cliente.

---

## Versão 2 — gRPC

Execute `rodar-grpc.bat` duas vezes: uma para o servidor e outra para o cliente.

**Terminal 1 — Servidor (porta 5001):**
```
rodar-grpc.bat  →  escolha 1
```

**Terminal 2 — Cliente (Jogador 1):**
```
rodar-grpc.bat  →  escolha 2  →  host: localhost
```

Para **2 jogadores**, abra um terceiro terminal e repita o passo do cliente.

---

## Versão 3 — MQTT

Usa o broker público `broker.hivemq.com:1883`. Requer conexão com a internet.

Não é necessário informar host — a conexão é feita automaticamente.

**Terminal 1 — Servidor:**
```
rodar-mqtt.bat  →  escolha 1
```

**Terminal 2 — Cliente (Jogador 1):**
```
rodar-mqtt.bat  →  escolha 2
```

Para **2 jogadores**, abra um terceiro terminal e repita o passo do cliente.

---

## Jogar em máquinas diferentes

Substitua `localhost` pelo IP da máquina que está rodando o servidor.

- **Sockets / gRPC:** informe o IP quando solicitado no cliente
- **MQTT:** não precisa de configuração extra — usa broker em nuvem
