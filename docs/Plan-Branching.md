# Estrategia de Branching

Para el desarrollo de la "Plataforma Inteligente para la Gestión y Prevención de Incendios", se ha implementado la estrategia de **GitHub Flow** adaptada para soportar un monorepositorio con múltiples componentes (Frontend, BFF, Microservicios).

## 1. Ramas Principales

- **`main`**: Es la rama principal. Contiene el código listo para producción. Cualquier commit en `main` debe ser estable, pasar las pruebas unitarias y ser desplegable.
- **`develop`**: Rama de integración. Es donde confluyen todas las nuevas funcionalidades antes de pasar a `main`. 

## 2. Ramas de Soporte

- **`feature/nombre-de-la-feature`**: Creadas a partir de `develop`. Se utilizan para desarrollar nuevas funcionalidades (ej. `feature/mapa-leaflet`, `feature/ms-usuarios-circuitbreaker`).
- **`bugfix/nombre-del-bug`**: Creadas a partir de `develop` o `main` para resolver problemas no críticos encontrados durante el desarrollo.
- **`hotfix/nombre-del-hotfix`**: Creadas directamente desde `main` para solucionar incidencias críticas en producción. Se integran de vuelta tanto a `main` como a `develop`.

## 3. Flujo de Trabajo (Workflow)

1. **Creación**: Un desarrollador crea una rama `feature/reporte-incendios` desde `develop`.
2. **Desarrollo**: Se realizan commits atómicos y descriptivos.
3. **Pull Request (PR)**: Una vez finalizada la tarea, se abre un PR hacia `develop`.
4. **Code Review**: Otro miembro del equipo (o el mismo desarrollador si trabaja solo) revisa el código, asegurando la cobertura de pruebas unitarias (>60%).
5. **Merge**: Se aprueba el PR y se hace merge a `develop`, resolviendo cualquier conflicto de integración.
6. **Release**: Cuando `develop` alcanza un estado maduro y estable (como la finalización de la Evaluación Parcial 2), se genera un PR hacia `main` y se crea un *Tag* de versión (ej. `v2.0.0`).

## 4. Gestión de Conflictos
En caso de que ocurran conflictos al integrar ramas (ej. dos desarrolladores modifican `docker-compose.yml`), el desarrollador encargado de la rama de la característica debe hacer un `git pull origin develop` hacia su rama, resolver el conflicto localmente en su IDE, confirmar los cambios y luego actualizar el PR.
