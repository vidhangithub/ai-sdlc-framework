---
name: generate-openapi
version: 1.0.0
phase: "04 — Code Development"
description: >
  Generates a complete, validated OpenAPI 3.0.3 YAML specification from an
  approved LLD API contract section. Use this skill whenever a user wants to
  create an API spec, write OpenAPI YAML, generate Swagger docs, document REST
  endpoints, create API contracts, or produce API documentation. Trigger when
  the user says "generate OpenAPI spec", "write the API contract", "create
  Swagger YAML", "document this API", "generate the OpenAPI", "create the API
  definition", or "I need an OpenAPI file". The spec produced by this skill
  becomes the source of truth for API contract testing (Pact) and client SDK
  generation — it must match the LLD exactly.
inputs:
  - name: lld_api_section
    description: The API Contracts section from the approved LLD
    required: true
  - name: service_name
    description: "Service name in kebab-case, e.g. payment-service"
    required: true
  - name: base_path
    description: "API base path, e.g. /api/v1/payments"
    required: true
  - name: auth_type
    description: "One of: oauth2 | api_key | basic | none (default: oauth2)"
    required: false
    default: "oauth2"
  - name: token_url
    description: "OAuth2 token URL (required if auth_type is oauth2)"
    required: false
output: Complete OpenAPI 3.0.3 YAML specification
---

# Skill: Generate OpenAPI 3.0 Specification

## Purpose

Produce a standards-compliant, complete OpenAPI 3.0.3 YAML specification that can be imported into API gateways, used for contract testing, and published as developer documentation. Every field in the LLD API contracts section must appear in the spec.

---

## Step-by-Step Instructions

### Step 1 — Read the LLD API Section

Extract for every endpoint:
- HTTP method and path
- Summary and description
- Auth scope required
- All request parameters (path, query, header)
- Request body (all fields, types, validation constraints, examples)
- All success responses (status code, description, schema, example)
- All error responses (status codes)
- Idempotency key header (if applicable)

### Step 2 — Define Document Header

```yaml
openapi: "3.0.3"
info:
  title: "{Service Name} API"
  description: |
    {2-3 sentence description of what this service does}
    
    **LRS Reference:** {LRS-ID}
    **LLD Reference:** {LLD version}
    **Authentication:** OAuth2 Bearer Token
  version: "1.0.0"
  contact:
    name: "{Team Name}"
    email: "{team@company.com}"
  license:
    name: "Internal — Company Confidential"

servers:
  - url: "https://{service-name}.dev.company.com"
    description: Development
  - url: "https://{service-name}.sit.company.com"
    description: SIT
  - url: "https://{service-name}.uat.company.com"
    description: UAT
  - url: "https://{service-name}.company.com"
    description: Production
```

### Step 3 — Define Security Schemes

For OAuth2 (default):
```yaml
components:
  securitySchemes:
    oauth2:
      type: oauth2
      flows:
        clientCredentials:
          tokenUrl: "{token_url}"
          scopes:
            read: "Read access to {resource} resources"
            write: "Write access to {resource} resources"
            admin: "Administrative access"
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: "JWT Bearer token from OAuth2 authorization server"

security:
  - bearerAuth: []
  - oauth2: []
```

### Step 4 — Define Reusable Schemas

Define **all** schemas under `components/schemas`. Never inline schemas in endpoint definitions — always use `$ref`.

**Required schemas to always include:**

```yaml
components:
  schemas:
  
    # RFC 7807 Problem Detail — used by ALL error responses
    ProblemDetail:
      type: object
      description: "RFC 7807 Problem Details for HTTP APIs"
      required: [type, title, status]
      properties:
        type:
          type: string
          format: uri
          example: "https://api.company.com/errors/validation-error"
        title:
          type: string
          example: "Validation Failed"
        status:
          type: integer
          example: 400
        detail:
          type: string
          example: "Field 'amount' must be positive"
        instance:
          type: string
          format: uri
          example: "/api/v1/{resource}"
        traceId:
          type: string
          format: uuid
          example: "550e8400-e29b-41d4-a716-446655440000"
      additionalProperties: true

    # Request DTO — all fields from LLD
    {EntityName}Request:
      type: object
      required: [{list mandatory fields}]
      properties:
        {field}:
          type: {string|number|integer|boolean|array|object}
          description: "{field description}"
          example: "{example value}"
          # Add constraints:
          minLength: {n}
          maxLength: {n}
          pattern: "{regex if applicable}"
          minimum: {n}
          maximum: {n}
          
    # Response DTO — all fields from LLD
    {EntityName}Response:
      type: object
      required: [id, status, createdAt]
      properties:
        id:
          type: string
          format: uuid
          readOnly: true
          example: "550e8400-e29b-41d4-a716-446655440000"
        status:
          type: string
          enum: [{list valid statuses}]
          example: "PENDING"
        createdAt:
          type: string
          format: date-time
          readOnly: true
          example: "2024-01-15T10:30:00Z"

    # Paginated response wrapper (if applicable)
    Paginated{EntityName}Response:
      type: object
      required: [content, totalElements, totalPages, page, size]
      properties:
        content:
          type: array
          items:
            $ref: "#/components/schemas/{EntityName}Response"
        totalElements:
          type: integer
          example: 100
        totalPages:
          type: integer
          example: 10
        page:
          type: integer
          example: 0
        size:
          type: integer
          example: 10
```

### Step 5 — Define Reusable Parameters

```yaml
components:
  parameters:
    PathId:
      name: id
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: "Unique identifier of the {resource}"
      example: "550e8400-e29b-41d4-a716-446655440000"

    IdempotencyKey:
      name: X-Idempotency-Key
      in: header
      required: false
      schema:
        type: string
        maxLength: 64
      description: "Client-supplied key for idempotent POST requests"
      example: "client-request-abc-123"

    PageParam:
      name: page
      in: query
      schema:
        type: integer
        minimum: 0
        default: 0

    SizeParam:
      name: size
      in: query
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 20
```

### Step 6 — Define Reusable Responses

```yaml
components:
  responses:
    Unauthorized:
      description: "Missing or invalid authentication token"
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/ProblemDetail"
          example:
            type: "https://api.company.com/errors/unauthorized"
            title: "Unauthorized"
            status: 401

    Forbidden:
      description: "Authenticated but insufficient permissions"
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/ProblemDetail"

    NotFound:
      description: "Resource not found"
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/ProblemDetail"
          example:
            type: "https://api.company.com/errors/not-found"
            title: "Resource Not Found"
            status: 404

    ValidationError:
      description: "Request validation failed"
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/ProblemDetail"

    InternalServerError:
      description: "Unexpected server error"
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/ProblemDetail"
```

### Step 7 — Define Each Endpoint

For every endpoint from the LLD:

```yaml
paths:
  {base_path}:
    post:
      tags: ["{EntityName}"]
      summary: "Create {entityName}"
      description: |
        Creates a new {entityName} resource.
        **LRS Reference:** {LRS-FR-ID}
        **Idempotency:** Supported via X-Idempotency-Key header
      operationId: "create{EntityName}"
      security:
        - bearerAuth: ["write"]
      parameters:
        - $ref: "#/components/parameters/IdempotencyKey"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/{EntityName}Request"
            example:
              {field1}: "{value}"
              {field2}: 100.00
      responses:
        "201":
          description: "{EntityName} created successfully"
          headers:
            Location:
              description: "URL of the created resource"
              schema:
                type: string
                format: uri
            X-Correlation-Id:
              description: "Correlation ID for request tracing"
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/{EntityName}Response"
        "400":
          $ref: "#/components/responses/ValidationError"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "409":
          description: "Duplicate request — idempotency key already used"
          content:
            application/problem+json:
              schema:
                $ref: "#/components/schemas/ProblemDetail"
        "500":
          $ref: "#/components/responses/InternalServerError"

  {base_path}/{id}:
    get:
      tags: ["{EntityName}"]
      summary: "Get {entityName} by ID"
      operationId: "get{EntityName}ById"
      security:
        - bearerAuth: ["read"]
      parameters:
        - $ref: "#/components/parameters/PathId"
      responses:
        "200":
          description: "{EntityName} found"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/{EntityName}Response"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "500":
          $ref: "#/components/responses/InternalServerError"
```

### Step 8 — Add Tags

```yaml
tags:
  - name: "{EntityName}"
    description: "Operations related to {entityName} management"
  - name: "Health"
    description: "Service health and readiness endpoints"
```

---

## Quality Checklist

- [ ] Every LLD endpoint has a corresponding path definition
- [ ] All schemas defined under `components/schemas` — none inline in paths
- [ ] `ProblemDetail` schema present and used for ALL error responses
- [ ] Every endpoint has a security requirement (or explicit `security: []` for public)
- [ ] All response codes present: 2xx success, 400, 401, 403 (if applicable), 404 (if applicable), 409 (if idempotent), 500
- [ ] Every schema has `example` values (not placeholders)
- [ ] All `operationId` values are unique and camelCase
- [ ] `X-Correlation-Id` response header on success responses
- [ ] All servers defined (dev, sit, uat, prod)
- [ ] YAML is syntactically valid (no indentation errors)

---

## Rules

- **Never use `additionalProperties: true` on request schemas** — be explicit about what is accepted
- **Always use `$ref`** — never inline schemas in path definitions
- **All UUIDs use `type: string, format: uuid`** — not `type: string` alone
- **All dates use `format: date-time`** with UTC timezone in examples
- **Never use `type: object` without defining properties** — always be explicit
- **operationId must be globally unique** — used for SDK generation
