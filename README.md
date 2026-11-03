# WARG

**WARG** es una reimplementación progresiva en **Node.js** del juego de estrategia por turnos [Domination](https://domination.sourceforge.net/) (Yura Mamyrin), tomando como referencia el código **r2844 / Risk Game 1.3.6**.

El objetivo es mantener las reglas observables y los formatos de datos originales, con un nuevo motor modular y una interfaz web de estética militar. **La reimplementación todavía no está completa y no se garantiza equivalencia integral con el motor Java.**

## Ejecución

Requiere Node.js 20 o superior. Sin dependencias externas de npm.

```bash
npm test
npm start
```

Abrir http://127.0.0.1:3000 en el equipo donde se ejecuta el servidor. Por defecto, el servidor escucha únicamente en la interfaz local.

## Estructura nueva

| Ruta | Contenido |
|---|---|
| `src/engine/map.js` | Lector del formato original `.map` |
| `src/engine/cards.js` | Cartas, misiones y valores de canje |
| `src/engine/game.js` | Reglas, estado y transiciones de partida |
| `src/engine/session.js` | Coordinación de comandos, cartas y dados |
| `src/server.js` | API HTTP local sin dependencias |
| `public/` | Interfaz militar web y mapa interactivo |
| `test/` | Pruebas unitarias y de integración |

Las fuentes y los recursos originales permanecen temporalmente bajo `Domination/` para comprobación de compatibilidad y suministro de imágenes/mapas/cartas. **No se eliminarán hasta sustituir y verificar sus funcionalidades.** Los proyectos auxiliares `RiskAI/`, `OldHardAI/` y `MapStore/` también siguen siendo referencias.

## Estado de migración

| Subsistema | Estado |
|---|---|
| Lectura de mapas del mundo y cartas | Primera implementación, con pruebas |
| Despliegue, turnos, refuerzos y fronteras | Primera implementación |
| Ataque, dados, conquista, retirada y fortificación | Primera implementación, con pruebas |
| Canjes de cartas, modos de capital y misión | Primera implementación, pendiente de más casos límite |
| Interfaz web local por turnos | Implementación inicial |
| Reproducción exacta del protocolo y guardados Java | Pendiente |
| Paridad completa con IA original | Pendiente |
| Lobby, partidas multijugador remotas y sincronización | Pendiente |
| Compatibilidad exhaustiva de mapas personalizados y partidas antiguas | Pendiente |
| Sustitución y limpieza del árbol Java | Pendiente de paridad verificable |

Esta versión es **experimental**. La interfaz permite jugar en el mismo dispositivo; no ofrece todavía autenticación, salas en línea ni garantías de comportamiento idéntico en todas las variantes de Domination.

## Referencia y licencia

- Proyecto original: **Domination**, de **Yura Mamyrin** (yura.net).
- Código de referencia: SourceForge SVN `svn.code.sf.net/p/domination/code`, revisión **r2844**.
- Licencia de WARG: **GNU General Public License v3** (véase `LICENSE`).
- Se mantienen los avisos de copyright y las licencias de terceros del árbol original.

Los cambios de Node.js se han escrito como implementación separada, preservando la atribución al proyecto que ha servido de referencia. Esta transformación no implica que el motor sea todavía compatible al 100 %.
