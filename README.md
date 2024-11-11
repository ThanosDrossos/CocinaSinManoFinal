# Recetas Sin Toque

**Versión:** 0.1.2  
**Desarrollado por:** Thanos Drossos

---

## Índice

- [Descripción](#descripción)
- [Características](#características)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Uso](#uso)
- [Configuración](#configuración)
- [Gestos Soportados](#gestos-soportados)
- [Contribución](#contribución)
- [Licencia](#licencia)
- [Contacto](#contacto)

---

## Descripción

**Recetas Sin Toque** es una aplicación de Android que permite a los usuarios navegar por recetas y pasos de preparación utilizando gestos de mano, eliminando la necesidad de tocar la pantalla. Es especialmente útil en entornos donde tocar el dispositivo no es práctico, como en la cocina.

---

## Características

- **Navegación por Gestos:** Utiliza gestos de mano para desplazarte por las recetas y sus pasos.
- **Reconocimiento de Gestos en Tiempo Real:** Implementa el reconocimiento de gestos en vivo utilizando MediaPipe.
- **Lista de Recetas Interactiva:** Muestra una lista de recetas con imágenes y descripciones.
- **Detalle de Recetas:** Visualiza instrucciones paso a paso con imágenes ilustrativas.
- **Configuración Personalizable:** Ajusta parámetros como la confianza mínima de detección y seguimiento, período de enfriamiento y el intervalo de reconocimiento de gestos.
- **Interfaz Intuitiva:** Diseño limpio y fácil de usar con soporte para temas claros y oscuros.

---

## Requisitos Previos

- **Android Studio Bumblebee** o superior.
- **SDK de Android:** Nivel de API mínimo 21 (Android 5.0 Lollipop).
- **Dispositivo Android** con cámara frontal y soporte para OpenGL ES 3.1 o superior.
- **Conexión a Internet** (para descargar dependencias y modelos).

---

## Instalación

1. **Clona este repositorio:**

   ```bash
   git clone https://github.com/tu-usuario/recetas-sin-toque.git
   ```
2. **Abre el proyecto en Android Studio:**
   Selecciona File > Open y navega hasta la carpeta del proyecto clonado.
3. **Configura el SDK y las Herramientas de Compilación:**
   Asegúrate de tener instalado el SDK de Android 21 o superior.
   Verifica que las herramientas de compilación estén actualizadas.
4. **Sincroniza las Dependencias:**
   Android Studio debería sincronizar automáticamente las dependencias especificadas en build.gradle.
5. **Ejecuta la Aplicacion**

## Uso

### Navegación General

- **Lista de Recetas:**
  - Al iniciar la aplicación, se muestra una lista de recetas.
  - Puedes desplazarte por las recetas utilizando gestos o tocando la pantalla.

- **Seleccionar Receta:**
  - Usa el gesto de **mano abierta** para seleccionar la receta destacada.

- **Detalles de la Receta:**
  - Navega entre los pasos de la receta utilizando gestos:
    - **Pulgar Arriba:** Paso anterior.
    - **Pulgar Abajo:** Siguiente paso.
    - **Puño Cerrado:** Volver al resumen de la receta.

### Gestos Soportados

- **Pulgar Arriba (`Thumb_Up`):** Mover hacia la receta anterior o paso anterior.
- **Pulgar Abajo (`Thumb_Down`):** Mover hacia la siguiente receta o siguiente paso.
- **Mano Abierta (`Open_Palm`):** Seleccionar receta.
- **Puño Cerrado (`Closed_Fist`):** Volver al menú anterior.

---

## Configuración

La aplicación permite ajustar varios parámetros para personalizar el comportamiento del reconocimiento de gestos.

### Acceder a la Configuración

1. Abre la aplicación y navega hasta la sección **"Acerca de"** desde el menú de navegación inferior.
2. En las secciones **"Ajustar Parámetros de Confianza"** y **"Ajustar Parámetros de Gestos"**, encontrarás los controles para ajustar los parámetros.

### Parámetros Ajustables

- **Confianza Mínima de Presencia de Mano:**
  - Controla la sensibilidad para detectar la presencia de una mano.
- **Confianza Mínima de Detección de Mano:**
  - Ajusta la confianza necesaria para que una mano sea detectada.
- **Confianza Mínima de Seguimiento de Mano:**
  - Configura la confianza requerida para el seguimiento continuo de la mano.
- **Período de Enfriamiento (ms):**
  - Establece el tiempo de espera después de reconocer un gesto antes de aceptar otro.
- **Intervalo de Reconocimiento de Gestos (ms):**
  - Determina el tiempo durante el cual se acumulan datos para reconocer un gesto.

### Guardar Configuración

- Después de ajustar los parámetros, pulsa el botón **"Guardar Parámetros"** para aplicar los cambios.
- Los ajustes se guardarán y persistirán incluso si cierras y vuelves a abrir la aplicación.

---

## Gestos Soportados

| Gesto            | Acción en Lista de Recetas       | Acción en Detalles de Receta |
|------------------|----------------------------------|------------------------------|
| **Pulgar Arriba**    | Receta anterior                  | Paso anterior                |
| **Pulgar Abajo**     | Siguiente receta                 | Siguiente paso               |
| **Mano Abierta**     | Seleccionar receta resaltada     | N/A                          |
| **Puño Cerrado**     | N/A                              | Volver al resumen            |

---

## Contribución

¡Contribuciones, sugerencias y mejoras son bienvenidas! Por favor, sigue estos pasos:

1. **Haz un Fork del Proyecto**
2. **Crea una Rama Feature**
   ```bash
   git checkout -b feature/AmazingFeature
3. **Realiza Commit de tus Cambios**
   ```bash
   git commit -m 'Add some AmazingFeature'
    ```
4. Haz Push a la Rama
    ```bash
    git push origin feature/AmazingFeature
    ```

## Licencia
Este proyecto está bajo la Licencia MIT. Consulta el archivo LICENSE para más detalles
