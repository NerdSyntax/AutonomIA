# AutonomIA

Aplicación Android existente en Kotlin, Jetpack Compose y MVVM, con StateFlow,
Navigation Compose e inyección manual. Se mantienen el módulo `app`, el package
`com.nerdsyntax.juntalucas` y el nombre técnico del proyecto `JuntaLucas`.

## Organización y flujo real

Todo el código principal está en `app/src/main/java/com/nerdsyntax/juntalucas`.

| Área | Responsabilidad y conexiones |
| --- | --- |
| `MainActivity` | Aplica el tema y abre `AppNavigation`. |
| `di` | `AppContainer` conserva los repositorios Firebase durante el proceso; `AppViewModelFactory` los inyecta en los ViewModels. No se crean repositorios desde las pantallas. |
| `core/navigation` | `Routes` reúne las rutas, `SessionNavigation` decide las redirecciones, `AppNavigation` conecta estado y eventos, y `AppBottomNavigation` dibuja las cinco pestañas. |
| `core/session` | Observa autenticación y consulta el negocio. Solo permite entrar con correo verificado y configuración completa; los errores de lectura permiten reintentar. |
| `feature/auth` | Firebase Auth: correo/contraseña, Google mediante Credential Manager, registro, verificación, recuperación, cierre y eliminación de cuenta. Perfil y sesión observan el mismo `AuthRepository`. |
| `feature/onboarding` | Tres pantallas comparten el ViewModel asociado a `business_info`. Guardan `Business` mediante `BusinessRepository` y luego refrescan la sesión. |
| `feature/business` | `domain` y `data` contienen el contrato y persistencia del negocio y del catálogo Firebase. `ui` observa el catálogo y el perfil real; `ui/product` crea y edita productos o servicios. |
| `feature/dashboard` | Lee correo, nombre del negocio y meta desde los repositorios reales. Las cifras financieras permanecen en cero y los movimientos vacíos hasta su implementación. |
| `feature/movements` | Listas de demostración y formularios de venta/gasto. `MovementTextField` comparte la presentación idéntica de ambos formularios. |
| `feature/profile` | La pantalla actual usa el correo de Auth. `RoomUserProfileRepository`, DAO, entidad y contrato conservan la implementación local todavía no conectada. |
| `core/database` | Definición Room `JuntaLucasDatabase`, versión 1. Actualmente no hay una llamada a `Room.databaseBuilder`. |
| `feature/ai` | Pantalla, estado y ViewModel reservados para el asistente pendiente. |
| `core/ui/theme` | Tema Compose existente; los estilos específicos de las pantallas se conservan. |

Los modelos de presentación (`ProductItem`, `MovementItem`, `MovimientoUi`) no
son documentos Firebase. Tienen campos y formatos diferentes, por lo que no se
fusionan artificialmente en un modelo persistente durante esta organización.

## Datos reales, ejemplos y trabajo pendiente

- Firebase guarda el negocio en `users/{uid}/business/profile`, con los campos
  actuales de `Business`. Se conserva la escritura con merge y la lectura desde
  servidor. `firestore.rules` no fue modificado ni desplegado.
- `MovementsDemoData` se conserva únicamente para la pestaña Gastos, que queda
  fuera de esta etapa. Mi Negocio ya no consume `BusinessDemoData`: catálogo,
  stock, búsqueda y filtros vienen de Firestore y una cuenta sin productos queda
  vacía.
- `onboarding/data/OnboardingSampleData` conserva los cinco movimientos numéricos
  que se preparan al elegir «ejemplo», después de guardar el negocio. Es memoria
  temporal y aún no la consume ninguna pantalla. No equivale a los ejemplos de
  `MovementsDemoData`. «Importar» guarda la elección, pero no importa archivos.
- Los botones de guardar venta, gasto y producto todavía cierran el formulario
  sin persistirlo. Se mantienen los formularios, cálculos y diseños para completar
  esa integración después. La repetición mensual y los selectores simulados
  tampoco están implementados.
- Notificaciones, selección de períodos y «Ver todos» del dashboard siguen
  pendientes de conexión.
- El nombre completo del registro y «Recordar sesión» son controles locales,
  todavía sin conexión al repositorio. Los textos de términos y privacidad no
  abren documentos. La eliminación actual borra la cuenta Auth, sin una operación
  de borrado de documentos Firestore implementada en este proyecto.
- La capa Room de perfil se conserva por su propósito identificable. Su campo
  `onboardingCompleted` local no participa en el control de acceso: ese control
  usa el negocio de Firebase. No se modificó su tabla ni su versión.
- La IA conserva su pantalla pendiente; no se añade un proveedor ni un modelo.

## Ventas implementadas

La pantalla de ventas usa ahora `users/{uid}/sales` y el catálogo mínimo usa
`users/{uid}/products`. El listado escucha únicamente el UID autenticado, consulta
un mes civil (`yyyy-MM`), ordena por fecha descendente y calcula el total y ticket
del mismo conjunto visible después de aplicar búsqueda y medio de pago. La fecha
del mes se puede cambiar con las flechas del encabezado.

El formulario de venta carga productos/servicios activos, usa la fecha actual,
selector de fecha, pagos compartidos con los filtros, cantidades y dinero CLP
enteros, descuento acotado al subtotal y nota opcional. El precio y costo quedan
copiados en la venta: editar el catálogo no cambia ventas históricas.

Cada venta recibe un UUID persistente en `SavedStateHandle`. Firestore la guarda
en una transacción; si el producto controla stock, la lectura, validación de
disponibilidad, creación de venta y descuento se confirman atómicamente. Un
reintento con el mismo UUID es idempotente. Las reglas bloquean modificar o borrar
ventas y validan el propietario, los importes, el producto activo y el estado
posterior del producto.

El formulario de productos existente ahora crea documentos reales mínimos, con
precio, costo opcional (desconocido queda como `null`), estado activo y stock.
Desde «Registrar venta» se puede crear un producto y volver al formulario.

## Cambios de organización

- Se extrajeron la fábrica de ViewModels y la barra inferior de `AppNavigation`.
  Auth pasa al contenedor para evitar crear listeners Firebase nuevos al recrear
  la composición. Se mantiene la inyección manual.
- Se centralizaron todas las rutas sin cambiar sus cadenas. `AddMovementScreen`
  se llama ahora `AddSaleScreen` porque registra ventas; su ruta continúa siendo
  `add_movement` para mantener compatibilidad con el estado de navegación.
- `AddProductScreen`, su estado y ViewModel se agrupan en `business/ui/product`.
  La pantalla recibe callbacks, como las demás pantallas, en lugar del ViewModel.
- `BusinessSetupScreens.kt` se separó en `ActivitySelectionScreen.kt`,
  `StartingPointScreen.kt` y el componente compartido `SelectableCard.kt`.
- Los ejemplos y su inicialización salieron de los ViewModels. El antiguo
  `MockAppDatabase` se renombró y trasladó a `OnboardingSampleData`: no era una
  base de datos ni una fuente común de movimientos.
- `UserProfileRepositoryImpl` se renombró a `RoomUserProfileRepository` para
  identificar su almacenamiento. Se conserva íntegra su implementación.
- Se unificaron los campos idénticos de venta/gasto y el doble de Auth de tres
  suites de pruebas. Se reemplazó la prueba de plantilla `2 + 2` por pruebas de
  acceso y redirección de todas las rutas protegidas.
- `.kotlin/` queda ignorado como salida local del compilador. No se actualizaron
  versiones de dependencias, SDK, plugins, recursos gráficos, package ni esquemas
  de datos. Las dependencias y plugins de los scripts de build se centralizaron
  en `gradle/libs.versions.toml`, conservando todas sus versiones y configuraciones.
- Se eliminó `res/values/colors.xml`: sus siete colores de plantilla no tenían
  referencias en código o XML, ni búsquedas dinámicas; lint también los identificó
  como no utilizados. Las pantallas usan sus colores Compose existentes.

No se eliminó ninguna pantalla ni funcionalidad pendiente. Los archivos antiguos
que desaparecen del diff corresponden a los movimientos/divisiones anteriores,
salvo la prueba aritmética de plantilla, los colores sin uso y los cuerpos
duplicados extraídos.

## Compilación y pruebas

Se requiere el SDK indicado en `local.properties`, JDK 21 conforme a los criterios
del daemon y el archivo local `app/google-services.json` del proyecto existente.
Este último está ignorado por Git; no debe sustituirse por credenciales de ejemplo.
La configuración Google debe proporcionar `default_web_client_id` para el login.

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --console=plain
./gradlew.bat :app:connectedDebugAndroidTest --console=plain
```

Las pruebas unitarias cubren sesión, cambios de cuenta, carga del dashboard,
guardado del onboarding, login Google y decisiones de navegación. Usan dobles,
no cuentas ni escrituras en Firebase. La prueba instrumentada existente comprueba
el package instalado; no constituye una prueba de todos los flujos o del diseño.

Informes generados: `app/build/reports/tests/testDebugUnitTest/`,
`app/build/reports/lint-results-debug.html` y
`app/build/reports/androidTests/connected/`.

### Resultado de la revisión (30 de septiembre de 2026)

- Git estaba limpio antes de editar. La compilación y las pruebas originales
  pasaron antes de la refactorización.
- Verificación final: `assembleDebug`, `testDebugUnitTest`, `lintDebug` y
  `connectedDebugAndroidTest` finalizaron correctamente, con las 85 tareas
  ejecutadas en esa última invocación.
- Pasaron 27 pruebas unitarias y la prueba instrumentada de package en
  `Medium_Phone`, Android 17. También se generó correctamente el APK de pruebas.
- Lint: 0 errores y 17 advertencias. Quedan avisos de versiones/target Android,
  etiqueta redundante del manifest y posición de un parámetro Compose; no se
  actualizaron tecnologías para silenciarlos. El compilador señala además iconos
  y un constructor de Locale deprecados.
- `git diff --check` no encontró problemas de espacios. Se compararon con Git
  las pantallas de onboarding separadas, el estado/cálculos de producto y la
  implementación Room para confirmar que su contenido funcional se conservó.
- No se verificaron recorridos visuales completos ni operaciones contra Firebase
  real (registro, Google OAuth, correo, persistencia remota o reglas desplegadas).
  Las pruebas de navegación validan decisiones de destino, no todo el back stack
  de Compose ni la restauración después de muerte del proceso.
- Las reglas nuevas están preparadas en `firestore.rules`, pero no se desplegaron:
  Firebase CLI no está instalado en este entorno. El paso exacto pendiente es
  `firebase deploy --only firestore:rules --project juntalucas`, ejecutado desde
  la raíz con una cuenta autorizada para ese proyecto.
