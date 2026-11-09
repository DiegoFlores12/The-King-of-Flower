The King of Flower 🌱
Aplicación móvil Android para registrar, administrar y monitorear cultivos, consultar información botánica y organizar el riego de las plantas. El proyecto está desarrollado en Java con interfaces en XML, utilizando Android Studio y una base de datos PostgreSQL alojada en Neon mediante solicitudes HTTP.
📋 Tabla de contenidos
- Descripción del proyecto
- Objetivo
- Funcionalidades principales
- Tecnologías utilizadas
- Estructura del proyecto
- Descripción de las clases Java
- Requisitos de instalación
- Instalación y ejecución
- Funcionamiento de la aplicación
- Base de datos
- Integración IoT
- Permisos de Android
- Seguridad
- Mejoras futuras

1. Descripción del proyecto
The King of Flower es una aplicación móvil diseñada para facilitar el cuidado y seguimiento de plantas y cultivos desde un dispositivo Android.

La aplicación permite a los usuarios crear una cuenta, iniciar sesión, registrar diferentes tipos de cultivos, consultar información de sus plantas y administrar sus respectivos ciclos de crecimiento.

Además, incorpora funcionalidades relacionadas con el control del riego, información botánica e integración con dispositivos IoT, como ESP32, para facilitar la administración de los cultivos.

Su propósito es ofrecer una herramienta tecnológica que ayude a organizar el cuidado de las plantas, mejorar su seguimiento y facilitar el acceso a información relevante para su desarrollo.
2. Objetivo del proyecto
Desarrollar una aplicación móvil que permita gestionar cultivos de manera organizada y sencilla, incorporando herramientas tecnológicas para el seguimiento de las plantas, la consulta de información botánica y la administración del riego.
Objetivos específicos
- Facilitar el registro y administración de cultivos.
- Permitir la creación de cuentas de usuario.
- Almacenar información de las plantas registradas.
- Consultar características y parámetros botánicos.
- Administrar fechas y frecuencias de riego.
- Facilitar el seguimiento de las etapas de crecimiento.
- Incorporar la posibilidad de vincular dispositivos IoT.
3. Funcionalidades principales
3.1 Registro e inicio de sesión
La aplicación cuenta con un sistema de autenticación que permite:
- Registrar nuevos usuarios.
- Iniciar sesión mediante credenciales.
- Validar los datos de acceso.
- Mantener la sesión del usuario.
- Cerrar sesión de manera segura.
3.2 Panel principal
La pantalla principal permite acceder a las funcionalidades más importantes de la aplicación.

Desde este apartado se puede visualizar un resumen de los cultivos y acceder a las diferentes secciones mediante la navegación de la aplicación.
3.3 Gestión de cultivos
Permite administrar las plantas registradas por el usuario.

Entre sus funcionalidades se encuentran:
- Registrar nuevos cultivos.
- Consultar los cultivos existentes.
- Visualizar información individual.
- Actualizar información del cultivo.
- Eliminar cultivos.
- Registrar fotografías de las plantas.
3.4 Información botánica
La aplicación incorpora un servicio denominado BotanicalService, encargado de consultar información relacionada con las plantas.

Entre los parámetros considerados se encuentran:
- Nombre científico.
- Temperatura recomendada.
- Humedad.
- Frecuencia de riego.
- Recomendaciones generales de cultivo.
También cuenta con información local de respaldo para diferentes especies de plantas.
3.5 Control de riego
El módulo de riego permite administrar las actividades relacionadas con el cuidado de los cultivos.

Incluye:
- Registro de actividades de riego.
- Configuración de fechas.
- Programación de horarios.
- Configuración de frecuencias.
- Consulta del próximo riego.
3.6 Seguimiento de cultivos
Permite consultar y actualizar información relacionada con el desarrollo de las plantas, incluyendo sus etapas de crecimiento y estado.
3.7 Integración IoT
El proyecto contempla la posibilidad de asociar dispositivos ESP32 a los cultivos mediante la gestión de dispositivos IoT.

Esta funcionalidad permite preparar la aplicación para futuras implementaciones de monitoreo automatizado.
3.8 Configuración
La sección de configuración permite consultar información del perfil del usuario, administrar preferencias y cerrar sesión.

4. Tecnologías utilizadas
Tecnología	Descripción
Java 11	Lenguaje de programación principal.
XML	Diseño de las interfaces gráficas.
Android Studio	Entorno de desarrollo.
Android SDK 36	Plataforma de compilación.
AndroidX AppCompat	Compatibilidad de componentes Android.
Material Components	Componentes visuales y diseño Material.
ConstraintLayout	Organización de las interfaces.
RecyclerView	Visualización de listas de cultivos.
Neon PostgreSQL	Base de datos remota.
Trefle	Consulta de información botánica externa.
ESP32	Integración con dispositivos IoT.

5. Estructura del proyecto
El proyecto está organizado de la siguiente manera:
The-King-of-Flower-mejoras-version/

Aquí calmacion falta 


6. Descripción de las clases Java
Clase	Función
LoginActivity.java	Gestiona el inicio de sesión.
RegisterActivity.java	Permite registrar nuevos usuarios.
MainActivity.java	Controla el panel principal de la aplicación.
CropsActivity.java	Muestra el listado de cultivos.
AddCropActivity.java	Permite agregar nuevos cultivos.
CropDetailActivity.java	Muestra información detallada de cada cultivo.
IrrigationActivity.java	Gestiona las funciones de riego.
SettingsActivity.java	Administra las opciones de configuración.
DatabaseHelper.java	Gestiona las operaciones de comunicación con la base de datos.
BotanicalService.java	Consulta información botánica.
CropModel.java	Representa los datos de cada cultivo.
CropAdapter.java	Administra la visualización de cultivos en listas.
SessionManager.java	Gestiona la información de sesión del usuario.

7. Requisitos de instalación
Para ejecutar el proyecto se necesitan los siguientes elementos:
- Android Studio.
- JDK 11 o la versión compatible requerida por Gradle.
- Android SDK Platform 36.
- Dispositivo Android o emulador.
- Android 7.0 (API 24) o superior.
- Conexión a Internet.
- Configuración de la base de datos Neon PostgreSQL.
8. Instalación y ejecución
Para ejecutar la aplicación:
1. Descargar el proyecto.
2. Descomprimir el archivo ZIP.
3. Abrir Android Studio.
4. Seleccionar la opción Open.
5. Buscar la carpeta principal del proyecto.
6. Esperar la sincronización de Gradle.
7. Configurar la conexión con la base de datos.
8. Seleccionar un dispositivo físico o emulador.
9. Presionar el botón Run ▶ para ejecutar la aplicación.
Compilación mediante terminal
En Windows:
.\gradlew.bat assembleDebug


En Linux o macOS:
./gradlew assembleDebug


El archivo APK se genera normalmente en:
app/build/outputs/apk/debug/app-debug.apk


9. Funcionamiento general de la aplicación
El funcionamiento de The King of Flower se desarrolla mediante el siguiente flujo:
1. El usuario abre la aplicación.
2. Se presenta la pantalla de inicio de sesión.
3. El usuario puede ingresar con su cuenta o registrarse.
4. Una vez autenticado, accede al panel principal.
5. Desde el panel puede ingresar al módulo de cultivos.
6. Puede registrar nuevas plantas y completar sus características.
7. Puede consultar información botánica.
8. Puede acceder a los detalles de cada cultivo.
9. Puede gestionar el riego y actualizar las etapas de crecimiento.
10. Finalmente, puede acceder a la configuración de su cuenta.
10. Base de datos
El proyecto utiliza Neon PostgreSQL como sistema de almacenamiento remoto.

La clase DatabaseHelper.java contiene la lógica encargada de comunicarse con el servicio de datos mediante solicitudes HTTP.

Entre las entidades utilizadas se encuentran:
- users: información de los usuarios.
- crops: información de los cultivos.
- iot_devices: información relacionada con los dispositivos IoT.
La base de datos permite almacenar y consultar información relacionada con los usuarios, sus plantas y los dispositivos asociados.

Importante: para ejecutar el proyecto en otro entorno es necesario contar con una base de datos configurada con las tablas y columnas que espera el código.
11. Integración IoT
El proyecto contempla la integración con dispositivos ESP32 para ampliar las funcionalidades de monitoreo de cultivos.

La aplicación incluye lógica para gestionar la asociación de dispositivos con determinados cultivos.

Esta integración permite preparar el sistema para futuras implementaciones que puedan incorporar sensores de:
- Humedad del suelo.
- Temperatura ambiental.
- Humedad ambiental.
- Condiciones de crecimiento.
La recepción de mediciones reales requiere implementar y configurar también la comunicación entre el ESP32 y el backend.
12. Permisos de Android
La aplicación utiliza los siguientes permisos:
Permiso	Función
INTERNET	Permite conectarse a servicios externos.
ACCESS_NETWORK_STATE	Permite consultar el estado de la conexión.
CAMERA	Permite utilizar la cámara para fotografiar cultivos.

Estos permisos se encuentran declarados en AndroidManifest.xml.
13. Seguridad
Para mantener la seguridad de la aplicación se deben considerar los siguientes aspectos:
- Proteger las credenciales de acceso a la base de datos.
- Evitar almacenar contraseñas en texto plano.
- Utilizar conexiones HTTPS.
- Validar los datos ingresados por los usuarios.
- Controlar los permisos de acceso a la información.
- Proteger los datos personales de cada cuenta.
Se detectó una credencial de conexión a Neon dentro de DatabaseHelper.java. Se recomienda rotarla y trasladar las operaciones de acceso a la base de datos a un backend seguro, evitando incluir credenciales privilegiadas dentro de la aplicación Android.
14. Mejoras futuras
Entre las posibles mejoras que se pueden implementar se encuentran:
- Incorporar notificaciones automáticas para recordar los horarios de riego.
- Implementar monitoreo en tiempo real mediante ESP32.
- Agregar gráficos de crecimiento de los cultivos.
- Incorporar más especies de plantas en la base de datos botánica.
- Implementar un historial de riegos.
- Mejorar las recomendaciones de cuidado según las condiciones ambientales.
- Incorporar fotografías históricas para observar el crecimiento de las plantas.
- Implementar alertas cuando la humedad del suelo sea insuficiente.

15. Información del proyecto
Característica	Información
Nombre	The King of Flower
Tipo	Aplicación móvil Android
Lenguaje	Java
Interfaces	XML
Entorno	Android Studio
Base de datos	Neon PostgreSQL
Integración	IoT / ESP32
Versión	1.0
Package	com.example.flowerking
SDK mínimo	API 24


The King of Flower 🌱 busca integrar tecnología y cuidado de las plantas mediante una aplicación móvil que facilite la administración de cultivos, el seguimiento de su crecimiento y la gestión del riego.

El proyecto representa una solución tecnológica orientada a mejorar la organización y el cuidado de los cultivos, incorporando herramientas digitales y posibilidades de automatización.
