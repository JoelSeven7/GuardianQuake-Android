# GuardianQuake — App Android (Kotlin + Jetpack Compose)

App Android que detecta movimiento sismico **100% offline** usando el
acelerometro del telefono, y se conecta al backend GuardianQuake
(FastAPI + MySQL) para todo lo que sí tiene sentido llevar a la nube:
historial, contenido educativo, sensibilidad sincronizada y suscripcion Pro.

## 1. Requisitos

- **Android Studio** Koala o más reciente
- **JDK 17** (Android Studio ya trae uno embebido normalmente)
- El **backend GuardianQuake** corriendo (ver el otro proyecto que te
  entregué, `guardianquake_backend`) — con `uvicorn app.main:app --host
  0.0.0.0 --port 8000`

## 2. Cómo abrir el proyecto

1. Abre Android Studio → **Open** → selecciona la carpeta `GuardianQuakeApp`.
2. Espera a que termine el **Gradle Sync** (barra inferior).
3. Si pide actualizar el Android Gradle Plugin o Gradle, puedes aceptar
   la sugerencia de Android Studio o dejar las versiones que ya vienen
   en el proyecto (`gradle-8.7`, AGP `8.5.2`).

## 3. Conectar la app con tu backend

La app apunta por defecto a:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
```

`10.0.2.2` es la IP especial que usa el **emulador** de Android Studio
para llegar a `localhost` de tu computador (donde corre tu backend
FastAPI). Si vas a probar en:

- **Emulador de Android Studio:** no cambies nada, ya apunta correcto.
- **Teléfono físico por USB/WiFi:** cambia `10.0.2.2` por la IP local de
  tu computador en la red (ej. `192.168.1.50`), y asegúrate de que el
  backend corra con `--host 0.0.0.0` (ya viene así en el README del
  backend) para que acepte conexiones de otros dispositivos.

Este valor está en `app/build.gradle.kts`, dentro de `defaultConfig`.

## 4. Levantar el backend antes de correr la app

```bash
cd guardianquake_backend
source venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Con el backend arriba, corre la app desde Android Studio (▶ Run).

## 5. Arquitectura del proyecto

```
app/src/main/java/com/guardianquake/app/
├── GuardianQuakeApp.kt        # Application: arma repos y canales de notificacion
├── MainActivity.kt            # Punto de entrada, decide Login vs Dashboard
├── data/
│   ├── remote/
│   │   ├── ApiService.kt       # Interfaz Retrofit (los 16 endpoints del backend)
│   │   ├── RetrofitClient.kt   # Cliente HTTP + interceptor de JWT
│   │   └── dto/                # Data classes que reflejan los esquemas del backend
│   ├── local/
│   │   ├── TokenManager.kt     # DataStore: guarda el JWT y el id de dispositivo
│   │   ├── AppDatabase.kt      # Room: cache local de eventos
│   │   ├── EventoLocalEntity.kt
│   │   └── EventoLocalDao.kt
│   └── repository/            # Une remoto + local, expone ApiResult<T>
├── sensor/
│   ├── SeismicDetector.kt      # Deteccion offline via acelerometro (RF01-RF07)
│   └── MonitoringService.kt    # Foreground Service que lo mantiene corriendo
└── ui/
    ├── theme/                  # Colores, tipografia, Theme.kt (Material3)
    ├── navigation/              # Screen.kt (rutas) + NavGraph.kt
    ├── auth/                    # Login, Registro
    ├── dashboard/                # Pantalla principal: switch de monitoreo
    ├── historial/                # Lista de eventos (locales + respaldados)
    ├── configuracion/            # Slider de sensibilidad (RF06)
    ├── contenido/                 # Lista y detalle de contenido educativo (RF09)
    ├── suscripcion/               # Activar/cancelar Pro (RF10)
    └── contactos/                  # CRUD de contactos de emergencia
```

**Patrón usado:** cada pantalla es un `@Composable` + un `ViewModel` que
expone un `StateFlow<UiState>`. El `ViewModel` habla con un
`Repository`, y el `Repository` decide si usar la API (`ApiService`) o
el cache local (`Room`). No se usó Hilt/Koin para mantener el proyecto
más fácil de leer — `GuardianQuakeApp.kt` actúa como localizador de
servicios simple.

## 6. Cómo funciona la detección offline (lo más importante del proyecto)

`SeismicDetector.kt` no depende de internet en absoluto (cumple RNF01):

1. Lee el acelerómetro con un filtro paso-bajo para aislar la gravedad.
2. Resta la gravedad de la lectura total → obtiene aceleración lineal.
3. Si la magnitud de esa aceleración supera un umbral (definido por el
   nivel de sensibilidad 1-10) de forma sostenida por al menos 1.5
   segundos, se considera un evento real (no solo un golpe corto).
4. Al detectarse, se guarda primero en Room (Sí funciona sin internet)
   y **después** se intenta respaldar en el backend — si falla por falta
   de conexión, queda marcado como pendiente y se reintenta después
   (`EventoRepository.sincronizarPendientes()`).

Este orden — local primero, nube después — es intencional: la detección
nunca debe depender de que el respaldo en la nube funcione.

## 7. Flujo de autenticación

- `MainActivity` revisa si hay un JWT guardado en DataStore.
  - Si hay → va directo a `Dashboard`.
  - Si no hay → va a `Login`.
- Al hacer login exitoso, se registra automáticamente el dispositivo
  (`DispositivoRepository.asegurarDispositivoRegistrado()`) para
  soportar la calibración por modelo (RNF04).
- El token se adjunta automáticamente a cada petición vía
  `AuthInterceptor` en `RetrofitClient.kt` — no necesitas pasarlo a mano
  en cada llamada.

## 8. Sobre el botón "Activar Pro"

En `SuscripcionScreen`, el botón "Activar Pro (demo)" simula una compra
generando un `id_transaccion` aleatorio y llamando a
`POST /api/suscripcion/verificar-compra`. En producción, ese
`id_transaccion` vendría de la **Google Play Billing Library** tras un
pago real — integrar esa librería es un paso adicional que no incluí
aquí porque requiere una cuenta de desarrollador de Google Play
configurada con productos in-app, algo específico de tu proyecto real.

## 9. Próximos pasos sugeridos (no incluidos)

- Integrar **Google Play Billing** de verdad en lugar del botón demo.
- Enviar SMS automático a los contactos de emergencia cuando se detecta
  un evento y el usuario es Pro (requiere permiso `SEND_SMS` en runtime
  y `SmsManager`).
- Splash screen con `androidx.core.splashscreen`.
- Tests unitarios para `SeismicDetector` (la lógica de umbral es pura
  Kotlin y se puede testear sin Android en absoluto).
