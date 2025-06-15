# EVOLV - Aplicación de Entrenamiento Personalizado

## 1. JUSTIFICACIÓN Y OBJETIVOS

### 1.1 Justificación del Proyecto

En la era digital actual, el bienestar físico y la actividad deportiva han cobrado una importancia vital en la vida de las personas. Sin embargo, muchas aplicaciones de fitness disponibles en el mercado presentan limitaciones significativas:

- **Falta de personalización**: Las rutinas son genéricas y no se adaptan a necesidades específicas
- **Interfaces complejas**: Demasiadas opciones que confunden al usuario
- **Ausencia de automatización**: Requieren intervención manual constante durante el entrenamiento
- **Limitaciones en la gestión de repeticiones**: No manejan adecuadamente ejercicios con múltiples repeticiones

**EVOLV** surge como respuesta a estas problemáticas, ofreciendo una solución integral que prioriza la simplicidad, personalización y automatización del entrenamiento.

### 1.2 Objetivos Principales

#### Objetivo General
Desarrollar una aplicación móvil Android que permita a los usuarios crear, gestionar y ejecutar rutinas de entrenamiento personalizadas de manera automatizada e intuitiva.

#### Objetivos Específicos

1. **Personalización Total**
   - Permitir la creación de plantillas de entrenamiento completamente personalizables
   - Configuración flexible de ejercicios, duraciones, repeticiones y períodos de descanso
   - Gestión independiente de cada ejercicio dentro de una rutina

2. **Automatización Inteligente**
   - Implementar un sistema de temporizadores automáticos que gestione repeticiones sin intervención manual
   - Progresión automática entre ejercicios y repeticiones
   - Gestión de períodos de descanso automatizados

3. **Experiencia de Usuario Optimizada**
   - Interfaz minimalista y fácil de usar
   - Feedback visual y auditivo durante el entrenamiento
   - Sistema de progreso claro y motivador

4. **Persistencia y Continuidad**
   - Guardar progreso de sesiones para permitir pausas y reanudaciones
   - Historial de entrenamientos realizados
   - Gestión de múltiples plantillas de entrenamiento

---

## 2. ESTUDIO DE MERCADO

### 2.1 Análisis del Mercado Actual

#### Aplicaciones Competidoras Analizadas

**Nike Training Club**
- ✅ Fortalezas: Gran variedad de ejercicios, calidad de contenido
- ❌ Debilidades: Rutinas predefinidas, poca personalización, interfaz compleja

**Adidas Training**
- ✅ Fortalezas: Integración con wearables, seguimiento avanzado
- ❌ Debilidades: Enfoque comercial, rutinas no totalmente personalizables

**7 Minute Workout**
- ✅ Fortalezas: Simplicidad, enfoque en ejercicios cortos
- ❌ Debilidades: Limitado a un tipo de entrenamiento, sin personalización

**MyFitnessPal + Ejercicios**
- ✅ Fortalezas: Amplia base de datos de ejercicios
- ❌ Debilidades: Enfoque en nutrición, gestión de ejercicios básica

### 2.2 Gaps del Mercado Identificados

1. **Automatización Limitada**: La mayoría requiere intervención manual constante
2. **Personalización Superficial**: Pocos permiten crear rutinas desde cero
3. **Gestión de Repeticiones Deficiente**: Manejan mal ejercicios con múltiples repeticiones
4. **Complejidad Innecesaria**: Interfaces sobrecargadas que dificultan el uso durante el ejercicio
5. **Falta de Flexibilidad**: Estructuras rígidas que no se adaptan a diferentes estilos de entrenamiento

### 2.3 Propuesta de Valor de EVOLV

**Diferenciadores Clave:**

1. **Automatización Completa**: Sistema de temporizadores que maneja automáticamente repeticiones y progresión
2. **Personalización Total**: Creación libre de plantillas sin restricciones predefinidas
3. **Simplicidad Funcional**: Interfaz minimalista enfocada en la experiencia durante el ejercicio
4. **Gestión Inteligente de Repeticiones**: Manejo automatizado de múltiples repeticiones por ejercicio
5. **Flexibilidad Absoluta**: Adaptación a cualquier tipo y estilo de entrenamiento

---

## 3. DISEÑO TÉCNICO Y ARQUITECTURA

### 3.1 Arquitectura General

#### Patrón Arquitectónico: MVC (Model-View-Controller)

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     VIEW        │    │   CONTROLLER    │    │     MODEL       │
│   (Activities)  │◄──►│  (Activities)   │◄──►│  (Data Layer)   │
│   (Layouts)     │    │   (Logic)       │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

#### Justificación de la Elección
- **Separación clara de responsabilidades**: Facilita mantenimiento y escalabilidad
- **Patrón familiar**: Amplio conocimiento del equipo de desarrollo
- **Integración natural con Android**: Se alinea con el paradigma de Activities y Fragments
- **Testabilidad**: Permite testing independiente de cada capa

### 3.2 Componentes Principales

#### 3.2.1 Capa de Presentación (View)
```
Activities/
├── WorkoutPlayerActivity_v2.java      # Reproductor principal de entrenamientos
├── EditWorkoutTemplateActivity.java   # Editor de plantillas
├── WorkoutTemplateListActivity.java   # Lista de plantillas disponibles
└── MainActivity.java                  # Pantalla principal

Layouts/
├── activity_workout_player_v2.xml     # UI del reproductor
├── activity_edit_template.xml         # UI del editor
└── item_selected_exercise.xml         # Items de ejercicios
```

#### 3.2.2 Capa de Control (Controller)
```
Controllers/
├── WorkoutPlayerActivity_v2.java
│   ├── updateExerciseDisplay()        # Gestión de UI
│   ├── avanzarAutomatico()           # Lógica de progresión automática
│   ├── startExerciseCountdown()      # Control de temporizadores
│   └── showRestPeriod()              # Gestión de descansos
│
└── EditWorkoutTemplateActivity.java
    ├── addExercise()                 # Agregar ejercicios
    ├── removeExercise()              # Eliminar ejercicios
    └── saveTemplate()                # Persistir plantillas
```

#### 3.2.3 Capa de Modelo (Model)
```
Models/
├── WorkoutTemplate_v2.java           # Plantilla de entrenamiento
├── WorkoutTemplateExercise_v2.java   # Ejercicio individual
├── WorkoutExerciseItem.java          # Item de ejercicio base
└── User.java                         # Usuario del sistema

Utils/
├── SessionProgressManager_v2.java    # Gestión de progreso
├── EnhancedSoundManager.java         # Gestión de audio
└── DatabaseHelper.java               # Acceso a datos
```

### 3.3 Tecnologías y Herramientas

#### 3.3.1 Plataforma de Desarrollo
- **Lenguaje**: Java
- **SDK**: Android SDK (API Level 21+)
- **IDE**: Android Studio
- **Build System**: Gradle

#### 3.3.2 Base de Datos
- **Motor**: SQLite
- **ORM**: Android Room (recomendado para futuras versiones)
- **Gestión**: DatabaseHelper personalizado

#### 3.3.3 Componentes Android Utilizados
```java
// Temporizadores
CountDownTimer              # Gestión de tiempo de ejercicios
Handler + Runnable          # Actualizaciones de UI

// Persistencia
SharedPreferences           # Configuración y progreso
SQLiteDatabase             # Almacenamiento de plantillas

// UI Components
ProgressBar                # Indicador visual de progreso
RecyclerView              # Listas de ejercicios
CardView                  # Diseño de tarjetas

// Audio
MediaPlayer               # Reproducción de sonidos
SoundPool                 # Gestión eficiente de efectos de sonido
```

### 3.4 Flujo de Datos

#### 3.4.1 Creación de Plantillas
```
Usuario → EditWorkoutTemplateActivity → WorkoutTemplate_v2 → SQLite
```

#### 3.4.2 Ejecución de Entrenamientos
```
SQLite → WorkoutTemplate_v2 → WorkoutPlayerActivity_v2 → 
CountDownTimer → UI Updates → SessionProgressManager_v2 → SharedPreferences
```

#### 3.4.3 Gestión de Progreso
```
WorkoutPlayerActivity_v2 → SessionProgressManager_v2 → SharedPreferences
                        ↓
                  Persistencia de estado
```

### 3.5 Patrones de Diseño Implementados

#### 3.5.1 Singleton
```java
// SessionProgressManager_v2
public class SessionProgressManager_v2 {
    private static SessionProgressManager_v2 instance;
    public static SessionProgressManager_v2 getInstance() { ... }
}
```

#### 3.5.2 Observer (Callback Pattern)
```java
// CountDownTimer callbacks
exerciseTimer = new CountDownTimer(duration, interval) {
    @Override
    public void onTick(long millisUntilFinished) { ... }
    
    @Override
    public void onFinish() { 
        avanzarAutomatico(); // Progresión automática
    }
};
```

#### 3.5.3 State Pattern (Implícito)
```java
enum WorkoutState {
    INTRO,      // Presentación del ejercicio
    EXERCISE,   // Ejecución del ejercicio
    REST,       // Período de descanso
    PAUSED,     // Entrenamiento pausado
    COMPLETED   // Entrenamiento finalizado
}
```

### 3.6 Arquitectura de Seguridad y Rendimiento

#### 3.6.1 Gestión de Memoria
- Cancelación proactiva de temporizadores para evitar memory leaks
- Uso eficiente de recursos de imagen con placeholders
- Gestión cuidadosa del ciclo de vida de Activities

#### 3.6.2 Persistencia Robusta
- Guardado automático de progreso en cada cambio de estado
- Recuperación de sesiones interrumpidas
- Validación de datos antes de persistir

#### 3.6.3 Experiencia de Usuario
- Feedback inmediato en todas las acciones
- Animaciones suaves en progressBars (50ms intervals)
- Gestión inteligente de sonidos (automático vs manual)

---

## 4. CONCLUSIONES TÉCNICAS

### 4.1 Fortalezas de la Arquitectura Elegida
1. **Escalabilidad**: Fácil adición de nuevas funcionalidades
2. **Mantenibilidad**: Código organizado y modular
3. **Testabilidad**: Separación clara permite testing unitario
4. **Rendimiento**: Gestión eficiente de recursos del sistema

### 4.2 Consideraciones Futuras
1. **Migración a MVP/MVVM**: Para mayor testabilidad
2. **Implementación de Room**: Para mejor ORM
3. **Modularización**: Separación en módulos independientes
4. **Inyección de Dependencias**: Uso de Dagger/Hilt

---

*Documento técnico v1.0 - Proyecto EVOLV*
*Fecha: 2024*
