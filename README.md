# Evolv
# **Evolv - Fitness Personalizado** 🏋️‍♂️

[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen?logo=android)](https://developer.android.com/)  
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.0-blue?logo=kotlin)](https://kotlinlang.org/)  
[![License](https://img.shields.io/badge/License-MIT-orange)](LICENSE)

**Evolv** es una aplicación Android que ofrece **entrenamientos 100% personalizables**, diseñada para resolver las limitaciones de las apps de fitness actuales: falta de adaptación real, experiencia fragmentada y accesibilidad limitada.

---

## **🚀 Características Principales**
✅ **Entrenamientos hiperpersonalizados**:
- Ajusta series, repeticiones, duración y equipamiento.
- Biblioteca de ejercicios categorizados (fuerza, cardio, flexibilidad).

📅 **Gestión integral**:
- Calendario para programar sesiones.
- Seguimiento de progreso con métricas (calorías, tiempo).

🎥 **Guía interactiva**:
- Vídeos demostrativos integrados (*ExoPlayer*).
- Temporizadores inteligentes para intervalos.

🔒 **Modo invitado**:
- Usa la app sin registro obligatorio.
- Compatible con dispositivos antiguos (*Android 8.0+*).

---

## **🛠️ Tecnologías Utilizadas**
- **Lenguaje**: Java
- **Arquitectura**: MVVM (*ViewModel + LiveData*)
- **Persistencia**: Room (*SQLite offline*)
- **Reproducción de vídeos**: ExoPlayer
- **Notificaciones**: WorkManager
- **UI**: Material Design 3

---

## **📊 Modelo de Datos**
![Diagrama Entidad-Relación](https://via.placeholder.com/600x400?text=Diagrama+E-R+de+Evolv)

### **Entidades clave**:
- **USER**: Datos del usuario (peso, género, email).
- **WORKOUT**: Sesiones de entrenamiento (tipo, calorías, tiempo).
- **EXERCISE**: Ejercicios predefinidos (vídeos, duración estándar).
