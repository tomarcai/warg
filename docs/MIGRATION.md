# Plan de migración Domination r2844 → WARG (Node.js)

## Alcance y fuente de verdad

Referencia: `Domination/src/net/yura/domination/engine/` del árbol r2844, en particular `Risk.java`, `core/RiskGame.java`, `core/Player.java`, `core/Country.java`, `core/Continent.java`, `core/Card.java`, `core/Mission.java`, `ai/`, y las pruebas originales de `Domination/test/`.

El objetivo es **compatibilidad observable**: dado el mismo mapa, opciones, secuencia de comandos y secuencia de números aleatorios, el estado resultante debe ser equivalente. El código nuevo no debe replicar la arquitectura Java por inercia.

## Inventario del progreso

| Responsabilidad Java | Nuevo módulo | Estado | Requisito antes de borrar Java |
|---|---|---|---|
| `RiskGame.loadMap` | `src/engine/map.js` | Parcial | Corpus de mapas reales, verificaciones de fronteras, nombres y variantes |
| `RiskGame.loadCards` | `src/engine/cards.js` | Parcial | Validar todas las misiones y archivos de cartas |
| `RiskGame.getTradeAbsValue` | `src/engine/cards.js` | Con pruebas | Combinaciones exhaustivas y progresión sin desviaciones |
| `RiskGame.placeArmy/endGo` | `src/engine/game.js` | Con pruebas | Secuencias aleatorias de todas las fases y variantes |
| `RiskGame.battle/moveArmies` | `src/engine/game.js` | Con pruebas | Dados 1–3, eliminaciones, canje obligatorio y estadísticas |
| `RiskGame.checkPlayerWon` | `src/engine/game.js` | Parcial | Victoria por dominación, capitales y misiones completas |
| `Risk.java` y comandos de red | `src/engine/session.js` | Parcial | Matriz completa de comandos y red/IA sin divergencias |
| `RiskGame.saveGame` | No implementado | Pendiente | Persistencia verificable e importación donde proceda |
| `ai/AIManager` + IA original | No implementado | Pendiente | Decisiones de referencia, modos y casos límite |
| `p2pclient/`, `p2pserver/`, lobby | No implementado | Pendiente | Sincronización remota, reconexión y autorización |
| UI Swing/Android/iOS | `public/` | Nueva UI local | Completar todas las funciones visibles y probar accesibilidad |
| MapStore y herramientas | No implementado | Pendiente | Elegir formatos y servicios que se conservarán |

## Puertas de calidad

1. **Pruebas unitarias:** cada transición debe tener un caso positivo, rechazos por estado inválido y límites enteros.
2. **Golden tests:** ejecutar secuencias equivalentes en el Java original y en Node, comparando el estado normalizado. Forzar la misma fuente de aleatoriedad cuando sea necesario.
3. **Mapas:** probar `world.map`, `risk.map` y el conjunto de mapas incluidos, más casos inválidos.
4. **Partidas:** comprobar partida completa con 2 a 6 jugadores, variantes de canje, capitales, misiones, eliminación y agotamiento de cartas.
5. **Multijugador:** impedir órdenes de jugadores no autorizados y verificar que clientes distintos convergen en el mismo estado.
6. **Persistencia:** round-trip tras cada fase; versionar formato y documentar incompatibilidades con la serialización Java.
7. **Desmantelamiento:** borrar únicamente rutas legacy cuyo comportamiento ya tenga un equivalente probado y cuyo uso desde Node esté desacoplado. En cada borrado, comprobar recursos, licencias y avisos de copyright.

## Decisiones iniciales

- Motor puro sin dependencia de HTTP ni DOM.
- API local con Node estándar para desarrollo; el HTTP local todavía **no es un servicio multijugador seguro**.
- Reutilizar provisionalmente `world.map`, `risk.cards` y la imagen del mundo incluidos en el árbol original.
- No añadir bibliotecas de producción antes de necesitarlas.
- Mantener GPLv3 y atribución explícita a Domination y Yura Mamyrin.

## Diferencias / riesgos conocidos

- Faltan estadísticas completas, parámetros históricos del protocolo y modos de guardado y repetición.
- La IA original no ha sido portada.
- La interfaz solo cubre los controles principales y no sustituye todavía a todas las plataformas originales.
- Algunas variantes y edge cases de misiones/cartas tienen cobertura insuficiente.
- No se ha ejecutado todavía una prueba diferencial automatizada completa Java ↔ Node.

**Por estos motivos no debe eliminarse `Domination/src` ni el resto del código histórico en esta etapa.**
