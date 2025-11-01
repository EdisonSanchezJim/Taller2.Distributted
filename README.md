Taller 2

Arquitectura 

<img width="870" height="607" alt="Captura de Pantalla 2025-10-31 a la(s) 9 34 14 p m" src="https://github.com/user-attachments/assets/76d78288-34a8-4c7f-a269-0a9f3fff83a1" />

En el Taller 2 de Sistemas Distribuidos desarrollamos una arquitectura orientada a la implementación de un sistema distribuido con replicación de datos, tolerancia a fallos y balanceo de carga. El sistema permite que múltiples clientes web registren sus nombres, los cuales se almacenan en una estructura replicada entre varios nodos backend. Esta solución está diseñada para funcionar de manera robusta en entornos dinámicos, donde los nodos pueden fallar o incorporarse en tiempo de ejecución sin comprometer la integridad del sistema.
La arquitectura se compone de cinco elementos principales: el cliente web asíncrono, el balanceador de carga, el registro de servicios (ServerRegistry), los nodos backend y la estructura de datos replicada. El cliente está desarrollado en JavaScript y utiliza llamadas HTTP asíncronas (por ejemplo, mediante `fetch`) para interactuar con el sistema sin necesidad de recargar la página. Este cliente no se comunica directamente con los nodos backend, sino que envía sus solicitudes al balanceador de carga.

El balanceador de carga está implementado en Java usando Spring Boot. Su función es distribuir las solicitudes entrantes entre los nodos backend disponibles, utilizando una estrategia de round-robin para garantizar una distribución equitativa. Esto evita que un solo nodo se sobrecargue y mejora la disponibilidad general del sistema. Internamente, el balanceador utiliza un componente de servicio que consulta el registro de nodos activos para decidir a cuál redirigir cada solicitud.
El ServerRegistry es el componente encargado de mantener actualizada la lista de nodos backend disponibles. Cada vez que un nodo se inicia, se registra automáticamente en este componente. Si un nodo falla o se desconecta, el registro lo elimina de la lista. El balanceador de carga consulta este registro constantemente para tomar decisiones de enrutamiento.

Los nodos backend son instancias independientes que también corren sobre Spring Boot. Cada uno expone un servicio REST que permite registrar nombres y consultarlos. Lo más importante es que cada nodo mantiene una copia sincronizada de la estructura de datos principal: un `ReplicatedHashMap`. Esta estructura se sincroniza entre nodos utilizando la biblioteca JGroups, que permite la comunicación en grupo. Cuando un nodo inserta un nuevo dato (por ejemplo, un nombre con su timestamp), los demás nodos reciben la actualización automáticamente. Esto asegura que todos los nodos compartan el mismo estado sin necesidad de una base de datos centralizada.
El comportamiento del sistema sigue una secuencia clara: el cliente envía una solicitud para registrar un nombre, el balanceador la recibe y la redirige a uno de los nodos backend activos. El nodo seleccionado guarda el dato en su estructura replicada, y JGroups se encarga de propagarlo a los demás nodos. Si algún nodo falla, los demás continúan funcionando con la información replicada. Si un nuevo nodo se une al sistema, recibe automáticamente una copia completa del estado actual desde uno de los nodos existentes.

En cuanto a la estructura del proyecto, se divide en dos módulos principales: `backend/` y `loadbalancer/`. El módulo `backend/` contiene todo lo relacionado con los nodos replicados, incluyendo la lógica de comunicación con JGroups y el servicio REST. Las clases más relevantes en este módulo son `BackendApplication.java`, que inicia el servicio; `RegistryController.java`, que expone los endpoints REST; y `SimpleChat.java`, que implementa la lógica de replicación con JGroups. También se incluye una clase de prueba llamada `TestJGroupsVersion.java`, útil para verificar la conectividad entre nodos.
Por otro lado, el módulo `loadbalancer/` contiene el balanceador de carga, el registro de nodos y la interfaz web. Las clases clave aquí son `LoadBalancerApplication.java`, que inicia el servidor; `LoadBalancerController.java`, que maneja las solicitudes del cliente; `LoadBalancerService.java`, que implementa la lógica de distribución de carga; y `NodeRegistryController.java`, que gestiona el registro dinámico de nodos. Este módulo también incluye el archivo `index.html`, que representa el cliente web y se encuentra en la carpeta `static/`.

Finalmente, el proyecto incluye archivos de configuración como `pom.xml` para definir las dependencias (Spring Boot, JGroups, etc.), `application.properties` para establecer puertos y direcciones, y los archivos `.jar` generados en la carpeta `target/` para desplegar cada servicio. También se utilizan claves `.pem` para habilitar comunicación segura entre nodos si se configura TLS.
En resumen, cada componente del sistema está claramente mapeado con su implementación en el código. El cliente web corresponde al archivo `index.html`, el balanceador de carga y su lógica están en las clases `LoadBalancerApplication`, `LoadBalancerController` y `LoadBalancerService`, el registro de servicios se encuentra en `NodeRegistryController`, y los nodos backend con su estructura replicada están implementados en `BackendApplication`, `RegistryController` y `SimpleChat`. La replicación se logra gracias a JGroups, cuya biblioteca está incluida como `jgroups-5.0.0.Final.jar`.


Còdigo

Proyecto Backend

Clases:
1. Application: Esta clase es la entrada principal de una aplicación backend construida con Spring Boot, y su propósito es iniciar el servidor y establecer la comunicación distribuida entre nodos. Al ejecutarse, Spring Boot arranca el servicio web, y mediante el método anotado con `@PostConstruct`, se invoca `simpleChat.initChannel()`, que activa el canal de JGroups para que el nodo se una al grupo de replicación. Esto permite que, desde el inicio, el nodo esté sincronizado con los demás y pueda enviar o recibir actualizaciones de datos en tiempo real, asegurando que todos los nodos compartan el mismo estado sin depender de una base de datos central.
2. BackendApplication: Es la entrada principal de una aplicación backend construida con Spring Boot. Su función es iniciar el servidor web que expone los servicios REST del nodo backend. Al ejecutar el método main(), Spring Boot arranca el contexto de la aplicación, configura automáticamente los componentes necesarios y deja el nodo listo para recibir solicitudes HTTP. Aunque esta clase no contiene lógica adicional, es esencial para poner en marcha el backend, que luego se conecta al sistema de replicación y al balanceador de carga para formar parte del sistema distribuido.
3. RegistrationClient: Se encarga de registrar automáticamente un nodo backend en el balanceador de carga al iniciar la aplicación. Utiliza Spring Boot para inyectar las URLs del balanceador y del backend desde el archivo de configuración (application.properties) mediante las anotaciones @Value. Al arrancar el servicio, el método register() marcado con @PostConstruct se ejecuta y envía una solicitud HTTP al endpoint del balanceador (/api/nodes/register), incluyendo la URL del backend como parámetro. Esto permite que el balanceador mantenga un registro actualizado de los nodos disponibles para distribuir las solicitudes entrantes. Además, la clase incluye un método unregister() anotado con @PreDestroy, que se ejecuta justo antes de que el nodo se apague, y aunque en este caso solo imprime un mensaje, puede extenderse para eliminar dinámicamente el nodo del registro. En conjunto, esta clase facilita el descubrimiento de servicios y la gestión dinámica de nodos dentro del sistema distribuido.
4. RegistryController: La clase `RegistryController` actúa como el controlador REST principal del nodo backend en el sistema distribuido. Su función es exponer los endpoints HTTP que permiten registrar nombres y consultar la lista de nombres almacenados. Al recibir una solicitud POST en el endpoint `/register`, el controlador extrae el nombre del cuerpo JSON, genera un timestamp con la hora actual, y lo almacena en la estructura replicada `ReplicatedHashMap` mediante el componente `SimpleChat`. Esta operación desencadena la replicación automática del dato hacia los demás nodos del sistema. Además, el endpoint GET `/names` permite consultar todos los nombres registrados, devolviendo el contenido actual del mapa replicado. También incluye un endpoint raíz `/` que puede servir el archivo `index.html` si se desea acceder a la interfaz web directamente desde el backend. En conjunto, esta clase conecta el sistema de replicación con las operaciones HTTP que permiten la interacción del cliente con el backend.
5. SimpleChat: La clase `SimpleChat` es el componente central encargado de manejar la replicación de datos entre los nodos backend del sistema distribuido. Utiliza la biblioteca JGroups para establecer un canal de comunicación en grupo (`JChannel`) que permite enviar y recibir actualizaciones de estado entre los nodos. Internamente, mantiene un mapa concurrente (`stateMap`) que almacena los pares clave-valor registrados por los clientes, como nombres y timestamps. Cuando se registra un nuevo dato mediante el método `put()`, este se guarda localmente y se envía como mensaje a los demás nodos del clúster. El método `receive()` se encarga de procesar los mensajes entrantes y actualizar el estado local con los datos recibidos. Además, los métodos `getState()` y `setState()` permiten compartir el estado completo del nodo con nuevos integrantes del clúster, facilitando la sincronización automática cuando un nodo se une. Finalmente, el método `viewAccepted()` imprime información sobre los cambios en la vista del grupo, como la incorporación o salida de nodos. En conjunto, esta clase garantiza que todos los nodos mantengan una copia sincronizada de los datos sin depender de una base de datos centralizada.


Proyecto LoadBalancer
1. LoadBalancerApplication: La clase `LoadBalancerApplication` es el punto de entrada principal para el componente de balanceador de carga dentro del sistema distribuido. Está construida con Spring Boot y su única responsabilidad es iniciar el servidor HTTP que recibirá las solicitudes del cliente web. Al ejecutar el método `main()`, Spring Boot configura automáticamente todos los componentes necesarios, incluyendo los controladores REST, el servicio de balanceo y el registro de nodos. Aunque esta clase no contiene lógica adicional, es esencial para poner en marcha el balanceador, que luego se encargará de distribuir las solicitudes entre los nodos backend disponibles usando la estrategia de round-robin.
2. LoadBalancerController: La clase `LoadBalancerController` implementa el controlador REST del balanceador de carga dentro del sistema distribuido. Su función principal es recibir las solicitudes del cliente web y distribuirlas entre los nodos backend disponibles utilizando una estrategia de **round-robin**, que garantiza una rotación equitativa. Para ello, mantiene una lista segura y concurrente de URLs de backends (`CopyOnWriteArrayList`) y un índice rotativo (`AtomicInteger`) que determina a qué nodo se enviará la siguiente solicitud.

Al iniciar, el balanceador permite registrar dinámicamente nuevos nodos mediante el endpoint `/api/nodes/register`, donde cada backend envía su URL. Esta URL se almacena si no está previamente registrada. El endpoint `/api/nodes` permite consultar la lista actual de nodos registrados.

Cuando el cliente web envía una solicitud para registrar un nombre (`/api/register`), el balanceador selecciona el siguiente backend disponible y redirige la solicitud usando `RestTemplate`. Lo mismo ocurre al consultar nombres (`/api/names`), donde se elige un backend en turno para obtener los datos. Además, el endpoint `/api/all-names` permite consultar todos los nombres registrados en todos los nodos, combinando los resultados en un solo mapa.

En conjunto, esta clase permite que el sistema distribuido funcione de manera dinámica, tolerante a fallos y escalable, al gestionar la rotación de nodos, el descubrimiento de servicios y la distribución de carga sin necesidad de intervención manual.
3. LoadBalancerService: La clase `LoadBalancerService` encapsula la lógica principal del balanceador de carga en el sistema distribuido. Su responsabilidad es gestionar la lista de nodos backend disponibles, seleccionar el siguiente nodo para atender una solicitud y verificar la disponibilidad de cada uno. Para ello, mantiene una lista concurrente de URLs (`backends`) y un índice rotativo (`AtomicInteger`) que implementa la estrategia de round-robin. Cada vez que se llama a `nextBackend()`, se devuelve la URL del siguiente nodo en la lista, asegurando una distribución equitativa de las solicitudes.

Además, esta clase permite registrar (`addBackend`) y eliminar (`removeBackend`) nodos dinámicamente, lo que facilita la escalabilidad y la tolerancia a fallos. También incluye el método `isAlive()`, que realiza una verificación de salud sobre cada nodo mediante solicitudes HTTP a endpoints como `/health` o `/names`, asegurando que solo se utilicen nodos activos. La clase utiliza un `RestTemplate` configurado con tiempos de espera personalizados para mejorar la resiliencia ante fallos de red. En conjunto, `LoadBalancerService` proporciona una base sólida para el balanceo dinámico de carga y el descubrimiento de servicios dentro del sistema distribuido.
4. NodeRegistryController: La clase `NodeRegistryController` implementa el componente de descubrimiento de servicios dentro del balanceador de carga. Su función es mantener un registro dinámico de los nodos backend disponibles en el sistema distribuido. Utiliza una lista (`nodes`) para almacenar las URLs de los nodos registrados, y expone tres endpoints REST para interactuar con ella.

El método `registerNode()` permite que un nodo backend se registre enviando su URL como parámetro. Si la URL no está en la lista, se agrega y se imprime un mensaje de confirmación; si ya está registrada, se notifica sin duplicarla. El método `getNodes()` devuelve la lista actual de nodos registrados, permitiendo al balanceador consultar qué instancias están activas. Finalmente, el método `removeNode()` elimina un nodo de la lista si se desea desconectarlo manualmente o en caso de fallo.

Todos los métodos están sincronizados para evitar problemas de concurrencia, lo que garantiza que el registro de nodos se mantenga consistente incluso si múltiples solicitudes llegan al mismo tiempo. Esta clase es clave para que el balanceador de carga pueda distribuir solicitudes de manera efectiva y adaptarse a cambios en la disponibilidad de los nodos.

5. Este archivo HTML representa la interfaz web del sistema distribuido, diseñada para que los usuarios puedan registrar sus nombres y visualizar en tiempo real los datos almacenados en los nodos backend. Utiliza JavaScript asíncrono para comunicarse con el balanceador de carga mediante llamadas HTTP, sin necesidad de recargar la página.

Al ingresar un nombre en el campo de texto y hacer clic en el botón "Registrar", se ejecuta la función `registerName()`, que envía una solicitud POST al endpoint `/api/register` con el nombre como cuerpo JSON. Si el registro es exitoso, se limpia el campo de entrada y se actualiza la lista de nombres.

La función `loadNames()` realiza una solicitud GET al endpoint `/api/names` para obtener el mapa completo de nombres registrados junto con sus timestamps. Los datos se renderizan dinámicamente en una lista HTML (`<ul>`), mostrando cada nombre y su fecha de registro.

Además, se establece un intervalo de actualización automática cada 2 segundos mediante `setInterval(loadNames, 2000)`, lo que permite que la interfaz refleje los cambios en tiempo real, incluso si otros usuarios están registrando nombres desde diferentes clientes. Esta interfaz es sencilla pero efectiva para demostrar la funcionalidad distribuida del sistema.


Proceso de configuraciòn:

Antes de ejecutar el sistema distribuido, asegúrate de tener el entorno correctamente configurado:

Requisitos previos

-Java 17 o superior
-Maven
-Git
-Navegador web moderno
-AWS CLI y acceso a EC2

Clonado del repositorio
git clone https://github.com/EdisonSanchezJim/Taller2.Distributted.git
cd DISTRIBUTED-KV-STORE

Compilación de los módulos
cd backend
mvn package

cd ../loadbalancer
mvn package

Esto generará los archivos .jar en la carpeta target/ de cada módulo.


Configuración de propiedades
Edita los archivos application.properties en cada módulo para definir:
Puertos de escucha (server.port)
URL del balanceador (loadbalancer.url)
URL del backend (backend.url)
Configuración de JGroups (si aplica)


Pasos para el despliegue:

Puedes ejecutar cada módulo en tu máquina local usando:

Usar bash
Bk1
java -Dbackend.url=http://34.228.18.69:8081 \
     -Dloadbalancer.url=http://3.82.60.200:8080 \
     -jar target/distributed-kv-store-1.0-SNAPSHOT.jar \
     --server.port=8081 \
     --debug



Bk2
java -Dbackend.url=http://54.235.17.161:8082 \
     -Dloadbalancer.url=http://3.82.60.200:8080 \
     -jar target/distributed-kv-store-1.0-SNAPSHOT.jar \
     --server.port=8082 \
     --debug

LB
java -Dbackends=http://34.228.18.69:8081,http://54.235.17.161:8082 \
     -jar target/loadbalancer-1.0.0.jar \
     --server.port=8080


Despliegue en EC2 (AWS)
Crea instancias EC2 para cada servicio (1 para el balanceador, 2+ para los backends).
Abre los puertos necesarios en el grupo de seguridad (por ejemplo, 8080, 8081, 8082).
Sube los .jar a cada instancia usando scp o SFTP.
Ejecuta los servicios con java -jar en cada instancia.
Verifica que los nodos backend se registren automáticamente en el balanceador.


Pruebas.
