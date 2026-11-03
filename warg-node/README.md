# warg (Node.js)

Reescritura de **Domination** (Yura Mamyrin, yura.net) en JavaScript para Node.js.
Primera versión: **solo partida local** (tú contra la IA y/o varios humanos en el mismo ordenador).

## Arrancar

Requisitos: Node.js 18 o superior. No hay dependencias que instalar.

```bash
cd warg-node
npm start            # abre http://127.0.0.1:8080/
```

Variables opcionales: `PORT` (8080), `HOST` (127.0.0.1), `WARG_SAVES` (carpeta de partidas guardadas, por defecto `warg-node/saves`).

Consola de texto (equivalente a la interfaz "commandline" original):

```bash
npm run cli
```

Tests:

```bash
npm test
```

## Qué incluye

| Área | Estado |
|---|---|
| Reglas (motor `RiskGame`) | Portado 1:1 desde Java: colocación inicial, refuerzos, continentes, ataques y dados, conquista, fortificación, fin de turno, eliminación con robo de cartas, empate/bloqueo |
| Modos | Dominación, Capital, Misión secreta, "continuar" tras ganar Capital/Misión |
| Cartas | Incremental (4,6,8,10,12,15,20…), Fijo, Italiano; reciclar cartas; tope de 5 cartas y cambio obligatorio |
| Opciones de partida | Colocación automática, reciclar cartas, dados de defensa (`defenderdice=`), mapa y fichero de cartas |
| IA | Fácil, Media, Difícil (AIDomination / AIMission / AICapital) y Sumisa, portadas línea a línea |
| Comandos | Todos los del parser original: `newgame`, `newplayer`, `startgame`, `placearmies`, `autoplace`, `trade`, `attack`, `roll`, `retreat`, `move`, `movearmies`, `nomove`, `endgo`, `capital`, `continue`, `undo`, `showcards`, `showarmies`, `showmission`, `autoendgo`, `autodefend`, `savegame`, `loadgame`, `replay`, `play`… |
| Interfaz web | Mapa con las 6 vistas (continentes, dueño, fronteras, jugador actual, concentración, imperio conectado), batalla con dados y "aniquilar", mover tropas, cartas, misión, estadísticas, continentes, deshacer, guardar/cargar, consola de comandos, sonidos originales, español e inglés |
| Mapas | Los 13 mapas originales, convertidos a polígonos vectoriales (`vector/*.json`) |
| Mapa vectorial | Cada territorio se rellena con el color de su dueño y tiene un borde del mismo color. Mientras lo atacan, el territorio se parte por la mitad (misma área) y la mitad que da al atacante toma su color |
| Editor de mapas | Dibujar territorios (los puntos se pegan a las fronteras vecinas), islas, continentes con bonus y color, vecinos (detección automática y rutas por mar), posición de tropas, imagen de referencia, deshacer. Se puede partir de un mapa original. Los mapas se guardan en `maps-custom/` y aparecen en Nueva partida |

## Cómo se ha comprobado la fidelidad

- **Partidas grabadas del original**: las 10 partidas de `Domination/res/test_scripts` (todos los modos y tipos de cartas) se reproducen comando a comando y terminan igual que en Java.
- **Mapas vectoriales**: en cada mapa, más del 97 % de los píxeles muestreados de la imagen original caen dentro del polígono de su territorio.
- **Valores de cambio de cartas**: se ejecutan las 192 aserciones de `CardsTradeTest.java`.
- **IA**: 1.350 partidas IA contra IA (9 combinaciones modo/cartas) cumplen el mismo criterio que `AISimulationTest.java`: fácil ≤ media ≤ difícil en victorias. Para reproducir el comportamiento exacto se emulan detalles de Java (división entera, orden de `HashMap`, `PriorityQueue`, `binarySearch`).

## Estructura

| Ruta | Contenido |
|---|---|
| `src/engine/RiskGame.js` | Reglas y estado de la partida |
| `src/engine/Risk.js` | Parser de comandos y flujo de turnos |
| `src/engine/ai/` | Jugadores IA |
| `src/engine/save.js` | Guardar / cargar (JSON) |
| `src/server/` | Servidor web local y acceso a ficheros |
| `public/` | Interfaz del navegador |
| `resources/translation/` | Textos nuevos de la interfaz y misiones en español |
| `src/tools/vectorize.js` | Convierte los `_map.gif` originales en polígonos (`node src/tools/build-vectors.js`) |
| `src/tools/mapWriter.js` | Valida y escribe los mapas del editor (`.map`, `.cards`, `.json`) |
| `maps-custom/` | Mapas creados con el editor |

El motor es el mismo módulo en el navegador y en Node, así que el modo en red puede construirse más adelante enviando los mismos comandos entre jugadores, igual que hacía el original.

## Diferencias conocidas con el original

- Las partidas se guardan en JSON, no en el formato de serialización de Java (no se pueden abrir `.save` del juego Java).
- La animación de fin de partida usa una física de bolas simplificada.
- El mapa se dibuja con polígonos en vez de con la imagen original, y los mapas creados con el editor necesitan al menos 6 territorios (con menos, la regla original da 0 ejércitos iniciales).
- Sin juego en red, lobby ni tienda de mapas en esta versión.

## Licencia

GNU GPL v3, como Domination. Mapas, sonidos, imágenes y traducciones son los del proyecto original.
