# warg

**warg** es un fork de [Domination](https://domination.sourceforge.net/), el juego de estrategia por turnos tipo Risk creado por Yura Mamyrin (yura.net).

## Origen

| Campo | Valor |
|---|---|
| Proyecto original | Domination, de Yura Mamyrin (yura@yura.net) |
| Repositorio upstream | SourceForge SVN: `svn.code.sf.net/p/domination/code` |
| Revisión importada | r2844 (Risk Game 1.3.6) |
| Commit de importación | Primer commit de este repositorio, sin modificaciones |

## Licencia

Este proyecto, como el original, se distribuye bajo la **GNU General Public License v3**. El texto completo está en [`LICENSE`](LICENSE), que es una copia de `Domination/gpl.txt`.

Se conservan todos los avisos de copyright del proyecto original. Los cambios hechos en este fork se publican también bajo GPLv3.

Algunas librerías de terceros incluidas como `.jar` (por ejemplo en `Domination/android/libs/`, `Domination/swingUI/lib/` y `RiskAI/source/*/lib/`) tienen sus propias licencias.

## Estructura

| Carpeta | Contenido |
|---|---|
| `Domination/` | Juego principal: motor, interfaces Swing, Android, iOS, servidor de lobby y build Gradle |
| `RiskAI/` | IA basada en redes bayesianas (proyecto académico) |
| `OldHardAI/` | IA antigua |
| `MapStore/` | Tienda de mapas (Django) |
| `website/` | Web del proyecto original |
