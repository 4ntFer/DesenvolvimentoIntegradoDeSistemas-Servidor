# Introdução

#### O processamento de imagens de tomografia utiliza uma série de operações lineares que são muitos custosas cumputacionalmente. Esse projeto propões a implementação de um sistema de arquitetura cliente-servidor para lidar com esse problema.

#### Para o desenvolvimento, as seguintes problemas foram abordados:

1. Implementação de um algoritmo para solução de sistemas de equações
2. Integração entre cliente e servidor
3. Gerenciamento de recursos da máquina

#### Cada um desses problemas será abordado nesse texto, bem como a solução implementada para lidar com eles.

# Algoritmo para solução de sistemas de equações (O processamento de imagens)

#### As imagens são processadas a partir desses algoritmos, que recebem como entrada uma matriz modelo *H* e uma matriz de sinal *g*, que resulta em uma matriz de numeros decimais, a qual cada elemento é um o brilho de um pixel na respectiva posição.

#### Por exemplo:

#### Dada a matriz:

| |  |
| ------------- | ------------- |
| 255 | 0  |
| 0  | 255 |

#### A imagem resultante dela é:

<img width= "30%" src="https://raw.githubusercontent.com/4ntFer/DesenvolvimentoIntegradoDeSistemas-Servidor/main/readmedis/resultimgexemple.png"/>
<br/>

## Algoritmos

#### Os algoritmos implementados foram:

1. *Conjugate Gradient Normal Residual* (CGNR)
   #### Segue o algortimo:
   <img width= "40%" src="https://raw.githubusercontent.com/4ntFer/DesenvolvimentoIntegradoDeSistemas-Servidor/main/readmedis/cgnralg.png"/>
   
   <br/>

   <br/>
2. *Conjugate Gradient Method Normal Error* (CGNE)
   #### Segue o algortimo:
   <img width= "40%" src="https://raw.githubusercontent.com/4ntFer/DesenvolvimentoIntegradoDeSistemas-Servidor/main/readmedis/cgnealg.png"/>
   
   <br/>

   <br/>

#### Foi considerado ponto de convergência o momento em que o erro é menor que 1e10-4. Onde:
![image](https://github.com/4ntFer/DesenvolvimentoIntegradoDeSistemas-Servidor/assets/121190153/ca23bab5-f3bf-45cb-ba68-664588dab7d6)
<br/>


## Escolha da biblioteca de operações linerares

#### Apesar de existirem bibliotecas mais eficiente como a ojAlgo, foi optado pela utilização da bliblioteca JBLAS, pela sua facilidade e ampla documentação.

# Integração entre cliente e servidor

## Comunicação entre cliente e servidor

#### A comunicação entre o cliente e o servidor é parte essencial do problema. Uma vez que, muitas vezes, em aplicações reais, a máquina cliente não terá capacidade de processamento suficiente para a tarefa, então é trabalho do servidor executa-la quando o cliente requisitar.

#### No caso abordado aqui, as matrizes modelo estão no servidor enquanto o sinal que originará a imagem estará no cliente. Para o envio desse sinal e algumas outras informações como identificação do usuário, dimensões da imagem resultante do sinal, matriz modelo a ser utilizada e algoritmo a ser utilizado, o cliente envia um arquivo no formato json para o servidor.

#### A comunicação é feita por meio de trocas de mensagem HTTP. A implementação do lado servidor não utiliza nenhuma ferramenta para a troca dessas mensagens além daquelas ofericidas pelas bliotecas padrão do Java.

#### A escolha de usar o protocolo HTTP para fazer a comunicação entre cliente e servidor se deu pelo fato de o lado cliente ser uma aplicação de navegador, assim, não oferecendo suporte para a manipulação de sockets TCP.

## Tratamente de requisição do cliente

#### Ao receber uma solicitação do cliente, o servidor inicia uma thread dedicada para seu processamento. Essa thread tem a responsabilidade de desserializar o documento JSON recebido e verificar a validade da requisição. Se a requisição for considerada válida, a thread a coloca na fila do gerenciador de recursos utilizando um método público do mesmo, ao qual é passada a requisição e uma referência para a thread. Em seguida a thread entra em um estado de espera, adormecendo até ser notificada pelo gerenciador de recursos.

<br/>

<img width= "100%" src="https://raw.githubusercontent.com/4ntFer/DesenvolvimentoIntegradoDeSistemas-Servidor/main/readmedis/diagramaDeEstadosConcorrentes.png"/>

###### Diagrama de tarefas concorrente de HandleCliente e ResoucesManager.

<br/>

#### Ao retomar a execução, a thread de tratamento procede ao processamento da imagem conforme especificado e, ao concluir, envia a resposta ao cliente. No caso de a requisição ser considerada inválida, o servidor responde ao cliente com o código de erro 404.

# Gerenciamento de Recursos

#### Em geral esse problema costuma ser tratado como uma fila, porém identificamos um problema não trivial nessa implementação ocasionado pelo acesso do usuário ao servidor. Basicamente, se um usuário realizar varias requisições consecutivas ele poderia monopolizar o processamento por um longo período de tempo, resultando em uma lentidão na resposta para aqueles usuários que fizessem requisições após isso.

#### Por isso desenvolvemos uma solução diferente baseada no algoritmo de Round-Robin que evita esse problema. Nesse modelo, o servidor armazena uma lista de usuários ativos e associa a cada usuário da lista uma fila de requisiçÕes. O gerenciador de recursos, por sua vez, percorre iterativamente a lista de usuários ativos, concedendo processamento conforme a disponibilidade a cada um em sequência, encerrando um ciclo ao atingir o final da lista e reiniciando-o a partir do início. Assim, cada usuário tem um acesso ao processamento por ciclo, tornando a distribuição de acesso mais coesa.


###### Todos os testes foram realizados em um computador com Ryzen 1600af, Geforce GTX 1050 TI e 16 gb de ram
