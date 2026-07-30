---
name: bancopago-frontend
description: >-
  Design system and Angular patterns for BancoPago frontend (Bancolombia-inspired
  tokens, signals, SSE accounts UX). Use when building or refactoring anything
  under frontend/, accounts UI, Angular components, styles, or API client services.
---

# BancoPago Frontend

## Design system (marca pública, no npm interno)

Bancolombia DS oficial es interno. Aquí tokens propios:

- `--bp-yellow` `#FFD200` · `--bp-ink` `#1A1A1A` · `--bp-surface` `#F7F7F5`
- Tipografía: Plus Jakarta Sans (ya en `index.html` / `styles.scss`)
- Evitar: Inter/Roboto, purple gradients, dark mode forzado, Material azure-blue como marca

Brand first en pantallas de producto. Cards solo para interacción (formularios / filas de cuenta).

## Angular moderno

- Standalone + `inject()` + signals (`signal` / `computed`)
- Control flow `@if` / `@for` / `@empty`
- `takeUntilDestroyed` / unsubscribe explícito en SSE
- Reactive forms tipados; CLIENT → `clientNumber`; EMPLOYEE → `position` + `area`

## Contratos

- Persons: `POST/GET /api/v1/persons`
- Accounts: `POST /api/v1/accounts`, `GET ?ownerId=`
- Balance: `GET /{accountId}/balance`, SSE `/{accountId}/balance/stream` evento `balance`
- **SSE = accountId**, lista = **ownerId** (person id). Nunca intercambiarlos.

## UX mínima en /accounts

Flujo usable sin Swagger: registrar persona → abrir cuenta → listar + SSE live.
Persistir último `ownerId` en `localStorage` (`bancopago.lastOwnerId`).
Mensajes claros si SSE falla (EventSource no expone body 404).
