# Evolv Tech Stack 🛠️ (2025)

## **Core Technologies**
| Categoría || Tecnología |
|-----------||-------------|
| **Plataforma** || Android |
| **Lenguaje Principal** || Java 8 |
| **Build System** || Gradle (Kotlin DSL) |
| **Versiones Android** || - Min SDK: 24 (Android 7.0)
 - Target SDK: 34 (Android 14) |

## **Base de Datos**
- **Nombre**: `evolv.db`
- **Motor**: SQLite (nativo)
- **Versión**: 1.0
- **Ubicación**:
    - Código: `com.evolv.app.database.EvolvDBHelper`
    - Assets: `/app/src/main/assets/databases/evolv_init.sql`

### **UI & Componentes Android**
- AndroidX AppCompat (1.6.1)
- Material Design Components (1.11.0)
- ConstraintLayout (2.1.4)

### **Seguridad**
- jBCrypt (0.4) - Para hash de contraseñas

### **Testing** - Pirámide de pruebas
- Mockito -(4.5.1): Para pruebas unitarias
- AndroidX Test (1.1.5)
- Espresso (3.5.1)

## **Arquitectura**
- Arquitectura basada en Activities
- SQLite para persistencia de datos local
- Implementación de autenticación local

## **Herramientas de Desarrollo**
- Android Studio
- ProGuard para optimización y ofuscación del código
