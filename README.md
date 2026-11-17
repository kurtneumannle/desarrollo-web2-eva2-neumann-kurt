# Sabor Gourmet: sistema de reservas

Evaluación sumativa de la Unidad 2 de Desarrollo de Software Web II (IPSS). Aplicación web para que los clientes de un restaurante vean la disponibilidad de mesas, reserven en línea y modifiquen o cancelen sus reservas, con una zona de administración para el personal.

**Stack:** Java 21, Spring Boot 3.3.5, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, H2 (desarrollo) y PostgreSQL (producción), Maven.

## Qué hace

- **Clientes (sin cuenta):** consultan mesas libres por fecha, hora y cantidad de personas, reservan, consultan sus reservas por correo, y modifican o cancelan con un enlace único (código aleatorio, no un número adivinable).
- **Personal (`/admin`, con usuario y clave):** ve las reservas de cualquier día, cancela reservas y administra las mesas (agregar, cambiar número y capacidad, activar o desactivar, eliminar si nunca tuvo reservas).
- **Reglas:** no se puede reservar una mesa ya ocupada en ese horario, ni con capacidad insuficiente, ni en el pasado ni fuera del horario de 12:00 a 21:00.
- **Diseño responsivo:** las vistas se adaptan a celular y escritorio (las tablas se vuelven tarjetas en pantallas angostas).

## Estructura (patrón MVC)

```
src/main/java/cl/ipss/reservas/
  model/        entidades JPA: Cliente, Mesa, Reserva, EstadoReserva
  repository/   Spring Data JPA: ClienteRepository, MesaRepository, ReservaRepository
  service/      reglas de negocio: ReservaService, MesaService, ReservaException
  web/          controladores MVC: ReservaController, AdminController, LoginController, ManejadorDeErrores
  dto/          formularios validados: ReservaForm, MesaForm
  config/       SecurityConfig, AppConfig (reloj y mesas de ejemplo)
src/main/resources/templates/   vistas Thymeleaf (fragments, reservas, admin)
src/main/resources/static/css/  estilos responsivos
```

## Ejecutar

```bash
mvn test                  # 17 pruebas (servicio y capa web)
mvn spring-boot:run       # perfil dev, http://localhost:8080
```

Datos de desarrollo: se cargan 8 mesas al iniciar. Zona de personal: usuario `admin`, clave `admin-dev` (solo desarrollo).

Producción: `SPRING_PROFILES_ACTIVE=prod` con las variables `DB_URL`, `DB_USER`, `DB_PASSWORD` y `ADMIN_PASSWORD`. Los secretos nunca van en el repositorio.

## Pruebas

`ReservaServiceTest` cubre las reglas de negocio (crear, doble reserva, capacidad, fechas, cancelar, modificar, cliente por correo, mesas inactivas). `ReservaWebTest` cubre las vistas, la validación del formulario, el 404 y la protección de `/admin`. Usan un reloj fijo para no depender de la fecha real.
