# Manejo de claves RSA para ms-usuarios

Este documento explica cómo generar, almacenar y montar las claves RSA (private/public) necesarias para que `ms-usuarios` emita y verifique tokens JWT con RS256.

1) Generar par de claves (localmente, con OpenSSL)

```bash
# Generar private key (PKCS#8)
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048

# Generar public key (X.509)
openssl rsa -pubout -in private_key.pem -out public_key.pem
```

2) Probar en local (modo desarrollo)

- Coloca `private_key.pem` y `public_key.pem` en una carpeta fuera del repo (ej. `/opt/keys/ms-usuarios/`).
- Arranca la app pasando las rutas mediante variables de entorno o `application.yml`:

```yaml
security:
  jwt:
    private-key-path: /opt/keys/ms-usuarios/private_key.pem
    public-key-path: /opt/keys/ms-usuarios/public_key.pem
    issuer: http://ms-usuarios:8082
```

3) Kubernetes — crear Secret y montar en Deployment

- Crear secret desde los archivos generados:

```bash
kubectl create secret generic ms-usuarios-keys \
  --from-file=private_key.pem=/path/to/private_key.pem \
  --from-file=public_key.pem=/path/to/public_key.pem -n your-namespace
```

- Ejemplo de fragmento de `Deployment` (volumen y mounts):

```yaml
volumes:
  - name: ms-usuarios-keys
    secret:
      secretName: ms-usuarios-keys

containers:
  - name: ms-usuarios
    image: your-registry/ms-usuarios:latest
    env:
      - name: SECURITY_JWT_PRIVATE_KEY_PATH
        value: /etc/keys/private_key.pem
      - name: SECURITY_JWT_PUBLIC_KEY_PATH
        value: /etc/keys/public_key.pem
    volumeMounts:
      - name: ms-usuarios-keys
        mountPath: /etc/keys
        readOnly: true
```

4) Docker Compose (desarrollo)

Ejemplo de servicio `docker-compose.yml` snippet que monta las claves locales:

```yaml
services:
  ms-usuarios:
    image: ms-usuarios:local
    environment:
      - SECURITY_JWT_PRIVATE_KEY_PATH=/run/secrets/private_key.pem
      - SECURITY_JWT_PUBLIC_KEY_PATH=/run/secrets/public_key.pem
    volumes:
      - /opt/keys/ms-usuarios/private_key.pem:/run/secrets/private_key.pem:ro
      - /opt/keys/ms-usuarios/public_key.pem:/run/secrets/public_key.pem:ro
```

5) Seguridad y rotación

- No almacenes `private_key.pem` en el repo.
- Para rotación, publica nueva JWK con `kid` distinto y mantén la anterior hasta que expiren tokens.
- Utiliza RBAC y permisos mínimos para el secreto.

6) Pruebas rápidas (curl)

- Registrar usuario (frontend usa `/api/usuarios/auth/register`):

```bash
curl -X POST http://localhost:8082/api/usuarios/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123","correo":"alice@example.com"}'
```

- Login y obtén token:

```bash
curl -X POST http://localhost:8082/api/usuarios/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123"}'

# Respuesta: { "token": "<JWT>", "user": { ... } }
```

- Obtener JWKs públicas:

```bash
curl http://localhost:8082/.well-known/jwks.json
```

7) Notas para integración con frontend

- El frontend del proyecto (`mfe-mapeo`) envía `username`/`password` y espera `res.data.token` y `res.data.user`. El token se guarda en `localStorage` como `token` y se añade en `Authorization: Bearer <token>`.
- Asegúrate de que el `API_BASE` del frontend apunte al gateway/BFF que enruta a `ms-usuarios` (`/api/usuarios/auth`).

Si quieres, puedo generar ejemplos de manifiestos `Deployment` completos y añadir un `Makefile` con comandos para generar claves y crear el secret.
