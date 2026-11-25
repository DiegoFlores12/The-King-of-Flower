# 🌱 The King of Flower — Rama `feature/firebase`

Aplicación móvil Android (`com.example.flowerking`) para el monitoreo agronómico inteligente (*Smart Agriculture*), evolucionada en la rama **`feature/firebase`** hacia una arquitectura híbrida en la nube con **Firebase Cloud Firestore**, telemetría física en tiempo real mediante **ESP32** y simulación dinámica automática.

---

## 🔄 Lo que se alteró en esta rama

* **Migración de Base de Datos (`DatabaseHelper.java`):** Se reemplazó la persistencia anterior en Neon PostgreSQL / SQLite local por **Firebase Cloud Firestore**, gestionando operaciones asíncronas con `ExecutorService` y tiempos de espera controlados (`Tasks.await` con timeout de 8–9 segundos) para evitar bloqueos en la interfaz.
* **Autenticación y Seguridad (`RegisterActivity.java`, `LoginActivity.java`, `DatabaseHelper.java`):** Se adaptó el flujo de registro e inicio de sesión a Firestore incorporando cifrado **SHA-256** (`hashPassword`) para las contraseñas de los usuarios.
* **Registro y Gestión de Cultivos (`AddCropActivity.java`):** Se actualizó el guardado de plantas (`insertCrop`) para almacenar en la nube parámetros agronómicos, umbrales botánicos (temperatura y humedad mínima, óptima y máxima) y estado inicial.
* **Listado de Cultivos en Vivo (`CropsActivity.java`):** Se modificó la carga estática de la lista por un ciclo automático de actualización cada **6 segundos** (`Handler` + `Runnable`) que sincroniza en segundo plano los cambios desde Firestore.
* **Ficha de Detalle y Diagnóstico (`CropDetailActivity.java`):** Se actualizó la vista de detalle para refrescar temperatura (con formato decimal `%.1f°C`), humedad y diagnóstico fitosanitario en tiempo real cada **6 segundos**, además de limpiar las referencias de estado hacia Firebase.
* **Configuración del Proyecto (`build.gradle.kts` y `.gitignore`):** Se añadieron las dependencias de **Firebase BoM** y **Cloud Firestore** en Kotlin DSL, y se excluyó `app/google-services.json` en `.gitignore` para proteger las credenciales del proyecto en el repositorio público.

---

## ✨ Lo que se agregó en esta rama

* **Estructura de Colecciones en Cloud Firestore:**
  * `users`: Perfiles de usuario y credenciales cifradas en SHA-256.
  * `crops`: Datos de cultivos, lecturas ambientales (`substrate_temp`, `moisture`, `water_level`) y límites botánicos.
  * `iot_devices`: Registro y estado de vinculación del nodo físico **`ESP32_PRINCIPAL`**.
  * `crop_history`: Bitácora cronológica de eventos por cultivo.
* **Integración IoT con Nodo Físico (`ESP32_PRINCIPAL`):**
  * Nuevos métodos en `DatabaseHelper.java` (`checkDeviceAssignment`, `assignDeviceToCrop`, `unassignDevice`, `getAssignedCropId` e `IotAssignmentInfo`) y controles interactivos en `CropDetailActivity.java` para **vincular, transferir o desvincular** el microcontrolador ESP32 a cualquier planta desde la app.
  * Sincronización directa vía HTTPS PATCH desde el ESP32 (sensor **DHT11** en `GPIO 4`, humedad de sustrato **YL-69** en `GPIO 34`, nivel de agua en `GPIO 35` y pantalla **OLED 0.96" I2C** en `GPIO 21/22`) hacia el documento del cultivo asignado en Firestore.
* **Simulación Ambiental Híbrida (`simulateUnassignedCropsData`):**
  * Nuevo motor en `DatabaseHelper.java` que identifica automáticamente qué cultivos **no** tienen un sensor ESP32 conectado y les aplica variaciones realistas de temperatura y humedad cada 6 segundos, manteniendo toda la huerta activa sin interferir con las lecturas reales del hardware.
* **Trazabilidad e Historial de Actividades (`registerCropHistory` / `getCropHistory`):**
  * Registro automático de eventos de **creación**, **cambio de etapa fenológica** y **riego manual**, visualizados cronológicamente en la ficha de cada planta mediante `CropHistoryAdapter`.

---

## ⚙️ Cómo ejecutar esta rama

1. **Clonar el repositorio y posicionarse en la rama:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   git checkout feature/firebase

The King of Flower 🌱 busca integrar tecnología y cuidado de las plantas mediante una aplicación móvil que facilite la administración de cultivos, el seguimiento de su crecimiento y la gestión del riego.

El proyecto representa una solución tecnológica orientada a mejorar la organización y el cuidado de los cultivos, incorporando herramientas digitales y posibilidades de automatización.
