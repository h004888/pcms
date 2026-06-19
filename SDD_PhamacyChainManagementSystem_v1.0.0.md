# Software Design Document
## Pharmacy Chain Management System

| Field | Value |
|-------|-------|
| **Document ID** | SDD_PharmacyChainManagementSystem_v1.4.0 |
| **Version** | 1.4.0 |
| **Date** | 2026-06-17 |
| **Author** | SDD Agent |
| **Status** | Final |
| **Related SRS** | SRS_PharmacyChainManagementSystem_v1.4.0 |

---

## I. Record of Changes

| Version | Date | A*M | In charge | Change Description |
| ----- | ----- | ----- | ----- | ----- |
| 1.0.0 | 2026-05-24 | A | SDD Agent | Initial version (aligned to SRS v1.0.0) |
| 1.1.0 | 2026-06-12 | M | SDD Agent | Round 1: aligned to SRS v1.1.0 — Use Case Summary, Screen Mockup, Screen Definition table, Entity Details, NFR expansion, Application Messages List, Common Requirements |
| 1.2.0 | 2026-06-12 | M | SDD Agent | Round 2: structural alignment to template v2.3 — Functional Overview, single-table UC, table-based Alt Flows, ERD/Entity Details under Software Features |
| 1.3.0 | 2026-06-13 | M | SDD Agent | Round 3: aligned to SRS v1.3.0 — restructured actors (6→5 internal + 4 external), added CEO role, updated Screen Authorization matrix, updated Non-Screen Functions (NSF-01..NSF-12), updated Database Schema to UUID + InventoryBatch, updated API endpoints (forgot/reset/verify-email), added NFR Performance/Scalability/Availability/External Interfaces/Compliance, added Application Messages & Common Requirements appendix |
| 1.4.0 | 2026-06-17 | M | SDD Agent | Round 4: aligned to SRS v1.4.0 — added **Customer Portal (B2C)** module with 52 màn hình khách hàng (Long Châu-style); added **AI Engine** module (12 tính năng AI: chatbot 24/7, OCR đơn thuốc, drug interaction check, semantic search, demand forecasting, anomaly detection, RAG pipeline); added **Pharmacist Workbench (RX-CONSOLE)** — 10 tính năng nâng cao cho dược sĩ như "người bạn tại quầy"; added **Mobile App** module (iOS+Android, push notification, GPS, calendar sync); added **Health Tools** (8+ bài quiz); added **E-commerce Operations** (voucher, flash sale, reviews, installment); expanded **Database Schema** to 27 tables; added 6 new UCs (UC14-UC19) to Screen Flow + Authorization Matrix; added new NFRs for AI performance, mobile bundle size, PWA offline mode; added 12 new NSF (NSF-13..NSF-24) |

*A = Added, M = Modified, D = Deleted

---

## Table of Contents

- [I. Record of Changes](#i-record-of-changes)
1. [Introduction](#1-introduction)
2. [System Architecture](#2-system-architecture)
3. [Software Architecture Design](#3-software-architecture-design)
   - 3.1 [Functional Overview](#31-functional-overview)
   - 3.2 [Frontend Architecture](#32-frontend-architecture)
   - 3.3 [Backend Architecture](#33-backend-architecture)
4. [Detailed Component Design](#4-detailed-component-design)
5. [Database Design](#5-database-design)
6. [API Design](#6-api-design)
7. [Security Design](#7-security-design)
8. [Deployment Architecture](#8-deployment-architecture)
9. [Non-Functional Requirements](#9-non-functional-requirements)
10. [Application Messages & Common Requirements](#10-application-messages--common-requirements)
11. [Appendix](#11-appendix)
9. [Non-Functional Requirements](#9-non-functional-requirements)
10. [Application Messages & Common Requirements](#10-application-messages--common-requirements)
11. [Appendix](#11-appendix)

---

## 1. Introduction

### 1.1 Purpose

This Software Design Document (SDD) describes the architectural and detailed design of the **Pharmacy Chain Management System (PCMS)** version 1.3.0. The document provides a complete description of the system's architecture, component designs, database schema, and API specifications.

This SDD is intended to:
- Provide a complete description of the system architecture from the technical perspective
- Serve as a basis for software development and implementation
- Define clear interfaces between system components
- Guide developers in implementing the system correctly

### 1.2 Scope

This document covers:
- System architecture and design patterns
- Frontend and backend component designs
- Database schema and ER diagram
- REST API endpoints and data formats
- Security mechanisms and authentication flows
- Deployment architecture

### 1.3 Relationship with SRS

This SDD is derived from the Software Requirement Specification document:
- **SRS Document:** [SRS_PharmacyChainManagementSystem_v1.3.0.md](./SRS_PharmacyChainManagementSystem_v1.3.0.md)
- **SRS Version:** 1.3.0 (Round 3 — restructured actors, added CEO, added External Actors)
- **Requirements Summary:** [requirements-summary.md](./requirements-summary.md)

All 13 Use Cases (UC01..UC13), 65 Functional Requirements (FR1..FR13.x), 7 Business Rules (BR01..BR07), and 34 Application Messages (MSG01..MSG34) from the SRS have been translated into component designs, database tables, and API endpoints in this document.

**Traceability summary:**

| SRS Section | SDD Section(s) |
| :---- | :---- |
| §1.3 Target Users / Actors | §3.1.3 System User Roles |
| §2.2 Use Cases Summary | §4 Detailed Component Design (per UC) |
| §3.1.1 Screens Flow | §3.1.1 Screen Flow |
| §3.1.2 Screen Descriptions | §3.1.2 Screen Descriptions |
| §3.1.3 Screen Authorization | §3.1.4 Screen Authorization Matrix |
| §3.1.4 Non-Screen Functions | §3.1.5 Non-Screen Functions |
| §3.1.5–3.1.6 ERD & Entity Details | §3.1.6 + §5 Database Design |
| §3.3 Functional Requirements | §4 Detailed Component Design + §6 API Design |
| §4 NFR | §7 Security + §9 Non-Functional Requirements |
| §5.1 Business Rules | §4 + §6 (per-UC rules) |
| §5.2 Common Requirements | §10 Common Requirements |
| §5.3 Application Messages | §10 Application Messages List |

### 1.4 Definitions, Acronyms, and Abbreviations

| Term | Definition |
|------|-----------|
| SRS | Software Requirement Specification |
| SDD | Software Design Document |
| PCMS | Pharmacy Chain Management System |
| JWT | JSON Web Token |
| RBAC | Role-Based Access Control |
| REST | Representational State Transfer |
| CRUD | Create, Read, Update, Delete |
| API | Application Programming Interface |
| UUID | Universally Unique Identifier |
| SLA | Service Level Agreement |

---

## 2. System Architecture

### 2.1 System Overview

The Pharmacy Chain Management System follows a **Microservice Architecture** with independent services for Auth, Branch, Medicine, Inventory, Order, Payment, Customer, Report, Supplier, Prescription, and Notification.

**System Overview Diagram:** [system-overview.puml](./diagrams/system-overview.puml)

**Context Diagram:** [context-diagram.puml](./diagrams/context-diagram.puml)

### 2.2 Architectural Patterns

| Pattern | Description | Application |
|---------|-------------|-------------|
| **Layered Architecture** | Separation into Presentation, Business Logic, and Data layers | Each microservice |
| **Repository Pattern** | Data access abstraction | Database operations |
| **Service-Oriented** | Business logic encapsulation | Microservices |
| **REST API** | Communication between frontend and backend | All services |
| **JWT Authentication** | Stateless authentication | Auth service |

### 2.3 System Components

The system is composed of **12 microservices** plus an API Gateway, mapped 1:1 to the 13 Use Cases in SRS v1.3.0 §2.2. (UC01 Authentication is split into a dedicated **Auth Service** for token management; the user CRUD lives in **User Service**.)

| Component | Port | Use Case | Description | Responsibilities |
| :---- | :---- | :---- | :---- | :---- |
| **API Gateway** | 3000 | — | Single entry point | AuthN filter, rate limiting (100 req/min/IP), request routing, CORS, TLS termination |
| **Auth Service** | 3001 | UC01 | Authentication & session | Login, logout, JWT (RS256) issuance, refresh, password reset, email verify, account lockout, token blacklist (NSF-07) |
| **User Service** | 3002 | UC02 | User management | CRUD users, role assignment, branch assignment, soft-delete (status=INACTIVE) |
| **Branch Service** | 3003 | UC03 | Branch management | CRUD branches, assign manager, branch performance aggregations |
| **Medicine Service** | 3004 | UC04 | Medicine catalog | CRUD medicines, categories, image upload, Rx flag, expiry tracking |
| **Inventory Service** | 3005 | UC05 | Stock & batches | Per-batch stock (InventoryBatch), IMPORT/EXPORT/TRANSFER_OUT/TRANSFER_IN transactions, low-stock & expiry alerts (NSF-02, NSF-03) |
| **Order Service** | 3006 | UC06 | Order processing | Create/view/cancel orders, ORD- number generation (NSF-12), BR04 discount, BR01 auto-cancel (NSF-01), BR06 stock restore |
| **Payment Service** | 3007 | UC07 | Payment & invoice | Cash/Card/QR, change calc, invoice generation (INV- number, NSF-11), stock FIFO consume (NSF-05), loyalty points (NSF-04, BR07) |
| **Customer Service** | 3008 | UC08 | Customer & loyalty | CRUD customers (CUST- code), purchase history, points balance |
| **Report Service** | 3009 | UC09 | Reports & analytics | Revenue/Inventory/Staff reports, daily roll-up (NSF-06), Excel/PDF export, scheduled delivery |
| **Supplier Service** | 3010 | UC11 | Supplier management | CRUD suppliers, tax code uniqueness, bank info, supply history |
| **Prescription Service** | 3011 | UC12 | Prescription handling | Create/sign Rx (RX- code, signature_hash), link to order, print |
| **Notification Service** | 3012 | UC13 | Multi-channel notifications | In-app/email/SMS, retry 3x (NSF-09), templates, broadcast composer |
| **Search Service** | 3013 | UC10 | Global search | Medicine search w/ autocomplete, filters, relevance ranking |

**Database per service (Database-per-Service pattern):**

| Service | DB | Notes |
| :---- | :---- | :---- |
| auth_service | PostgreSQL | users (subset), refresh_tokens, blacklisted_tokens, login_history |
| user_service | PostgreSQL | users (full), audit_log |
| branch_service | PostgreSQL | branches |
| medicine_service | PostgreSQL | medicines, categories |
| inventory_service | PostgreSQL | inventory_batches, inventory_transactions |
| order_service | PostgreSQL | orders, order_items |
| payment_service | PostgreSQL | payments, invoices |
| customer_service | PostgreSQL | customers |
| report_service | PostgreSQL (read replica) | report_schedules, report_exports, denormalized aggregates |
| supplier_service | PostgreSQL | suppliers, supply_history |
| prescription_service | PostgreSQL | prescriptions |
| notification_service | PostgreSQL | notifications, notification_templates |
| search_service | Elasticsearch | medicine index (read-only projection) |

**Inter-service communication:**

| Pattern | When | Example |
| :---- | :---- | :---- |
| Synchronous REST over mTLS | Direct query (read-only) | Order Service → Inventory Service (check stock) |
| Asynchronous event (Kafka / RabbitMQ) | State change notification | Order paid → Payment publishes `order.paid` → Notification consumes |
| gRPC (optional) | Internal hot-path | Report Service pulls aggregates from Order Service |

**Integration Diagram:** [integration.puml](./diagrams/integration.puml)

### 2.4 Layered Architecture

**Layered Architecture Diagram:** [layered-architecture.puml](./diagrams/layered-architecture.puml)

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (React Web App, Mobile Web, POS Terminal)             │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                      API Gateway                         │
│            (Authentication, Rate Limiting)              │
└─────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Auth Service │  │Order Service│  │Inventory Svc │
│   :3001      │  │   :3003     │  │   :3004      │
└──────────────┘  └──────────────┘  └──────────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                           │
│     (Per-Service Databases - MySQL)                     │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Software Architecture Design

### 3.1 Functional Overview

This section provides a comprehensive overview of the system's functionality including screen flow, screen descriptions, user roles, screen authorization matrix, non-screen functions, and the entity relationship diagram.

#### 3.1.1 Screen Flow

The system follows a hierarchical navigation structure with a sidebar-based layout. The system now has **3 surfaces**: (1) B2B Web Dashboard, (2) B2C Customer Portal, (3) Mobile App. The main screen flow is organized into the following modules:

**System Screen Flow:** [system-screen-flow.puml](./diagrams/system-screen-flow.puml)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              PCMS PLATFORM                                    │
│                                                                              │
│  ┌────────────────────────────────┐  ┌────────────────────────────────────┐    │
│  │   B2B WEB DASHBOARD            │  │  B2C CUSTOMER PORTAL (NEW v1.4)  │    │
│  │   (Pharmacist/Admin/CEO)       │  │  (Customer/Guest)                 │    │
│  │                                │  │                                    │    │
│  │  Login → Dashboard             │  │  /shop (SHOP-HOME)                │    │
│  │    │                            │  │    ├─ Catalog (SHOP-CAT-1/2)      │    │
│  │    ├─ Users/Branches            │  │    ├─ PDP / Search                 │    │
│  │    ├─ Medicines/Inventory      │  │    ├─ Cart → Checkout (4 bước)   │    │
│  │    │   (Import/Export/Transfer) │  │    ├─ Tra cứu thuốc/dược chất   │    │
│  │    ├─ Orders/Payments          │  │    ├─ Tìm nhà thuốc (map)         │    │
│  │    │   (POS / e-commerce)      │  │    ├─ Đặt lịch tiêm chủng         │    │
│  │    ├─ Customers                 │  │    ├─ Tài khoản gia đình           │    │
│  │    ├─ Suppliers/RX              │  │    ├─ Ví Khỏe Nhà Ta              │    │
│  │    ├─ Reports/Notifications     │  │    └─ AI Chat (CHAT-AI)            │    │
│  │    └─ RX-CONSOLE (Pharmacist)   │  │                                    │    │
│  │       (360° profile + AI)       │  │                                    │    │
│  └────────────────────────────────┘  └────────────────────────────────────┘    │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │   MOBILE APP (NEW v1.4) - iOS + Android                                │  │
│  │   Customer: Nhắc uống thuốc, GPS, AI Chat, Order tracking, VNeID      │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │   AI ENGINE (NEW v1.4) - separate microservice                         │  │
│  │   LLM Gateway + Vector Store + RAG Pipeline + Knowledge Base           │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Detailed Screen Flow per Use Case:**

| UC | Screen Flow Diagram | Description |
|----|---------------------|-------------|
| UC01 | [uc-01-screenflow.puml](./diagrams/uc-01/uc-01-screenflow.puml) | Login → Validate → Dashboard |
| UC02 | [uc-02-screenflow.puml](./diagrams/uc-02/uc-02-screenflow.puml) | User List → Add/Edit/Delete User |
| UC03 | [uc-03-screenflow.puml](./diagrams/uc-03/uc-03-screenflow.puml) | Branch List → Add/Edit/Assign Manager |
| UC04 | [uc-04-screenflow.puml](./diagrams/uc-04/uc-04-screenflow.puml) | Medicine List → Add/Edit/Search/Stock |
| UC05 | [uc-05-screenflow.puml](./diagrams/uc-05/uc-05-screenflow.puml) | Inventory Dashboard → Import/Export/Transfer |
| UC06 | [uc-06-screenflow.puml](./diagrams/uc-06/uc-06-screenflow.puml) | Order List → Create/View/Cancel Order |
| UC07 | [uc-07-screenflow.puml](./diagrams/uc-07/uc-07-screenflow.puml) | Payment → Select Method → Process → Invoice |
| UC08 | [uc-08-screenflow.puml](./diagrams/uc-08/uc-08-screenflow.puml) | Customer List → Register/Edit/View History |
| UC09 | [uc-09-screenflow.puml](./diagrams/uc-09/uc-09-screenflow.puml) | Reports Dashboard → Select Type → View/Export |
| UC10 | [uc-10-screenflow.puml](./diagrams/uc-10/uc-10-screenflow.puml) | Search Bar → Filter → Results |
| UC11 | [uc-11-screenflow.puml](./diagrams/uc-11/uc-11-screenflow.puml) | Supplier List → Add/Edit/View History |
| UC12 | [uc-12-screenflow.puml](./diagrams/uc-12/uc-12-screenflow.puml) | Prescription Form → Add Medicines → Print |
| UC13 | [uc-13-screenflow.puml](./diagrams/uc-13/uc-13-screenflow.puml) | Notification List → Mark Read/Configure |
| **UC14** | **[uc-14-screenflow.puml](./diagrams/uc-14-customer-portal/uc-14-screenflow.puml)** | **Customer Portal: Shop → Cart → Checkout → Track (NEW v1.4.0)** |
| **UC15** | **[uc-15-screenflow.puml](./diagrams/uc-15-ai/uc-15-screenflow.puml)** | **AI: Chat → RAG → Response; OCR RX → AI parse → Pharmacist review (NEW v1.4.0)** |
| **UC16** | **[uc-16-screenflow.puml](./diagrams/uc-16-pharmacist/uc-16-screenflow.puml)** | **Pharmacist Workbench: 360° profile → Order → AI alerts → Follow-up (NEW v1.4.0)** |
| **UC17** | **[uc-17-screenflow.puml](./diagrams/uc-17-mobile/uc-17-screenflow.puml)** | **Mobile: Home → Reminder setup → Push → Calendar sync (NEW v1.4.0)** |
| **UC18** | **[uc-18-screenflow.puml](./diagrams/uc-18-health/uc-18-screenflow.puml)** | **Health Quiz: List → Take → Result → CTA Consult (NEW v1.4.0)** |
| **UC19** | **[uc-19-screenflow.puml](./diagrams/uc-19-ecom/uc-19-screenflow.puml)** | **E-com: Voucher/Flash Sale/Reviews/Installment (NEW v1.4.0)** |

---

#### 3.1.2 Screen Descriptions

> **v1.4.0 mở rộng:** Tổng cộng **85 màn hình** trong **13 nhóm chức năng** (7 B2B cũ + 6 B2C mới). Màn hình B2C phân biệt bằng prefix `SHOP-` / `STORE-` / `CUST-` / `CHAT-` / `WALLET-` / `HEALTH-` / `MOBILE-` / `VACCINE-` / `AI-` / `RX-` để tránh trùng với prefix `SCR-` của B2B. Catalog đầy đủ xem SRS v1.4.0 §3.1.2 (Phần A: 28 SCR-*, Phần B: 50 B2C, Phần C: 7 PAGE-*).

**B2B Authenticated App (28 màn hình — giữ nguyên từ v1.3.0, chỉ mirror):**

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| SCR-LOGIN | Login | UC01 | Email/password authentication form |
| SCR-HOME | Dashboard | UC01 | Role-based landing page with KPI widgets |
| SCR-USER-LIST | User List | UC02 | Searchable list of system users |
| SCR-USER-FORM | User Form | UC02 | Create/edit user modal |
| SCR-BRANCH-LIST | Branch List | UC03 | Searchable list of branches |
| SCR-BRANCH-FORM | Branch Form | UC03 | Create/edit branch with manager picker |
| SCR-MED-LIST | Medicine List | UC04 | Searchable/filterable medicine catalog |
| SCR-MED-FORM | Medicine Form | UC04 | Create/edit medicine with image upload |
| SCR-INV-LIST | Inventory List | UC05 | Per-branch stock by batch |
| SCR-INV-IMPORT | Inventory Import | UC05 | Import stock (qty/batch/expiry) |
| SCR-INV-EXPORT | Inventory Export | UC05 | Export stock with reason |
| SCR-INV-TRANSFER | Inventory Transfer | UC05 | Inter-branch transfer wizard |
| SCR-ORDER-LIST | Order List | UC06 | Searchable order history |
| SCR-ORDER-NEW | Create Order | UC06 | New order with medicine lines |
| SCR-ORDER-DETAIL | Order Detail | UC06 | Order view with items/totals |
| SCR-PAYMENT | Payment | UC07 | Cash/Card/QR payment form |
| SCR-INVOICE | Invoice | UC07 | Printable invoice view |
| SCR-CUST-LIST | Customer List | UC08 | Searchable customer list |
| SCR-CUST-FORM | Customer Form | UC08 | Create/edit customer with history panel |
| SCR-CUST-HISTORY | Customer History | UC08 | Past orders & loyalty points |
| SCR-REPORT | Reports | UC09 | Filterable report with chart + table |
| SCR-REPORT-EXPORT | Export Dialog | UC09 | Excel/PDF export trigger |
| SCR-SEARCH | Search | UC10 | Global medicine search with filters |
| SCR-SUPPLIER-LIST | Supplier List | UC11 | Searchable supplier list |
| SCR-SUPPLIER-FORM | Supplier Form | UC11 | Create/edit supplier with bank info |
| SCR-RX | Prescription | UC12 | Create prescription with medicine lines |
| SCR-NOTIF-LIST | Notification List | UC13 | In-app notification inbox |
| SCR-NOTIF-COMPOSE | Compose Notification | UC13 | Admin broadcast composer |

**Screen group summary (7 groups):**

| Group | Screens | Primary Use Cases |
| :---- | :---- | :---- |
| Authentication | SCR-LOGIN, SCR-HOME | UC01 |
| User & Branch | SCR-USER-LIST, SCR-USER-FORM, SCR-BRANCH-LIST, SCR-BRANCH-FORM | UC02, UC03 |
| Medicine & Inventory | SCR-MED-LIST, SCR-MED-FORM, SCR-INV-LIST, SCR-INV-IMPORT, SCR-INV-EXPORT, SCR-INV-TRANSFER | UC04, UC05 |
| Order & Payment | SCR-ORDER-LIST, SCR-ORDER-NEW, SCR-ORDER-DETAIL, SCR-PAYMENT, SCR-INVOICE | UC06, UC07 |
| Customer | SCR-CUST-LIST, SCR-CUST-FORM, SCR-CUST-HISTORY | UC08 |
| Reports & Search | SCR-REPORT, SCR-REPORT-EXPORT, SCR-SEARCH | UC09, UC10 |
| Supplier / Rx / Notification | SCR-SUPPLIER-LIST, SCR-SUPPLIER-FORM, SCR-RX, SCR-NOTIF-LIST, SCR-NOTIF-COMPOSE | UC11, UC12, UC13 |

---

#### 3.1.3 System User Roles

The system defines **5 internal roles** and **4 external actors** (per SRS v1.3.0 §1.3):

**Internal Actors:**

| # | Role | Description | Key Responsibilities |
|---|------|-------------|---------------------|
| 1 | **Admin** | System administrator with highest access | User management, configuration, full operational visibility |
| 2 | **CEO** | Executive director with full operational access | All branches, all reports, all operations except Admin-level config |
| 3 | **Branch Manager** | Manages one or more pharmacy branches | Inventory import/export/transfers for their branches, order approval, branch reports |
| 4 | **Pharmacist** | Counter staff at pharmacy branch | Sales, payments, counter inventory, prescription issuance, customer management |
| 5 | **Customer** | End customer | Browse medicines, place orders (own only), view history, manage own profile |

> **Note (SRS Round 3 change):** The previous "Sales Staff" and "Warehouse Staff" roles have been removed. Their responsibilities are now merged into **Pharmacist** (counter sales/payment/customer) and **Branch Manager** (inventory import/export/transfer). The **CEO** role is newly added with full operational access across all branches.

**External Actors:**

| # | Actor | Type | Description |
| :---- | :---- | :---- | :---- |
| 1 | **Payment Gateway** | System | Processes card and QR code payments; idempotent webhook callbacks |
| 2 | **SMS Provider** | System | Sends OTP and notification SMS via HTTPS/REST |
| 3 | **Email Provider** | System | Sends verification emails and notifications via SMTP/TLS |
| 4 | **Printer** | Hardware | Prints invoices and prescriptions via LAN/IPP |

**Role Hierarchy (v1.3.0):**

```
                    ┌─────────┐
                    │  Admin  │  (Highest access, configuration)
                    └────┬────┘
                         │
                    ┌────┴────┐
                    │   CEO   │  (Full operational, all branches)
                    └────┬────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
   ┌─────────────┐ ┌──────────┐ ┌───────────┐
   │   Branch    │ │Pharmacist│ │           │
   │   Manager   │ │          │ │  Customer │
   └─────────────┘ └──────────┘ │           │
                                └───────────┘
```

---

#### 3.1.4 Screen Authorization Matrix

The following matrix defines which screens are accessible by each role, aligned with SRS v1.4.0 §3.1.3.

> **v1.4.0 mở rộng:** Thêm 2 cột mới — **Guest** (khách vãng lai) + **AI Engine** (service role). B2C customer-facing screens dùng Guest/Customer access; AI screens cho AI Engine = ● (backend only).
>
> **Legend:** ● = full access, ◐ = read-only (or own-data only), ○ = no access, − = not applicable, (own) = chỉ dữ liệu của chính mình, (own branch) = chỉ dữ liệu chi nhánh mình quản lý

##### Phần A — B2B Authenticated App (28 màn hình — matrix giữ nguyên từ v1.3.0)

> Catalog đầy đủ xem SRS v1.4.0 §3.1.3. Tóm tắt: Admin/CEO/BM/Pharmacist/Customer — 5 cột, 28 hàng.

##### Phần B — B2C Customer Portal (52 màn hình — NEW)

> Catalog đầy đủ xem SRS v1.4.0 §3.1.3 Phần B. Tóm tắt 5 cột: Guest, Customer, Pharmacist, Admin/CEO, AI Engine — 50 hàng (mỗi screen có pattern: Guest ● read-only, Customer ● sau khi login, Pharmacist ● cho support cases, AI Engine ● cho backend AI screens).

**Implementation notes cho B2C matrix:**
- Tất cả 52 màn hình B2C đều có Guest ● cho read-only browse (catalog, lookup, articles, store locator, health quiz)
- Customer cần đăng nhập mới có ● cho: cart, checkout, order history, profile, wallet, family, reviews, AI chatbot full
- Pharmacist (back-office staff) có ● cho: prescription review (SHOP-RX-UPLOAD), consultation (STORE-CONSULT), customer history view (CUST-*)
- AI Engine có ● cho: AI-DRUG-CHECK, AI-SEMANTIC-SEARCH backend, CHAT-AI inference

##### Phần C — Tĩnh (7 màn hình)

> Tất cả role (kể cả Guest) đều có ● truy cập.

| Screen ID | Screen Name | Admin | CEO | Branch Manager | Pharmacist | Customer |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| SCR-LOGIN | Login | ● | ● | ● | ● | ● |
| SCR-HOME | Dashboard | ● | ● | ● | ● | ● |
| SCR-USER-LIST | User List | ● | ● | ◐ | ○ | ○ |
| SCR-USER-FORM | User Form | ● | ● | ○ | ○ | ○ |
| SCR-BRANCH-LIST | Branch List | ● | ● | ◐ | ○ | ○ |
| SCR-BRANCH-FORM | Branch Form | ● | ● | ◐ | ○ | ○ |
| SCR-MED-LIST | Medicine List | ● | ● | ◐ | ● | ◐ |
| SCR-MED-FORM | Medicine Form | ● | ● | ○ | ● | ○ |
| SCR-INV-LIST | Inventory List | ● | ● | ● | ◐ | ○ |
| SCR-INV-IMPORT | Inventory Import | ● | ● | ● | ○ | ○ |
| SCR-INV-EXPORT | Inventory Export | ● | ● | ● | ○ | ○ |
| SCR-INV-TRANSFER | Inventory Transfer | ● | ● | ● | ○ | ○ |
| SCR-ORDER-LIST | Order List | ● | ● | ● | ● | ◐ (own) |
| SCR-ORDER-NEW | Create Order | ● | ● | ◐ | ● | ● |
| SCR-ORDER-DETAIL | Order Detail | ● | ● | ● | ● | ◐ (own) |
| SCR-PAYMENT | Payment | ● | ● | ◐ | ● | − |
| SCR-INVOICE | Invoice | ● | ● | ● | ● | ◐ (own) |
| SCR-CUST-LIST | Customer List | ● | ● | ◐ | ● | ○ |
| SCR-CUST-FORM | Customer Form | ● | ● | ○ | ● | ● (own) |
| SCR-CUST-HISTORY | Customer History | ● | ● | ◐ | ● | ◐ (own) |
| SCR-REPORT | Reports | ● | ● | ● (own branch) | ○ | ○ |
| SCR-REPORT-EXPORT | Export Dialog | ● | ● | ● (own branch) | ○ | ○ |
| SCR-SEARCH | Search | ● | ● | ● | ● | ● |
| SCR-SUPPLIER-LIST | Supplier List | ● | ● | ◐ | ◐ | ○ |
| SCR-SUPPLIER-FORM | Supplier Form | ● | ● | ○ | ○ | ○ |
| SCR-RX | Prescription | ● | ● | ◐ | ● | − |
| SCR-NOTIF-LIST | Notification List | ● | ● | ● | ● | ● |
| SCR-NOTIF-COMPOSE | Compose Notification | ● | ● | ○ | ○ | ○ |

**Permission Summary (full-access count per role):**

| Role | Full Access (●) | Read-only (◐) | No Access (○) | N/A (−) |
|------|:---:|:---:|:---:|:---:|
| Admin | 28 | 0 | 0 | 0 |
| CEO | 28 | 0 | 0 | 0 |
| Branch Manager | 7 | 11 | 8 | 2 |
| Pharmacist | 9 | 5 | 13 | 1 |
| Customer | 3 | 5 | 19 | 1 |

---

#### 3.1.5 Non-Screen Functions

The following background functions operate without direct user interface interaction. They are aligned with SRS v1.4.0 §3.1.4 (NSF-01..NSF-24).

| ID | Function | Trigger | Frequency | Owner UC | Notes |
| :---- | :---- | :---- | :---- | :---- | :---- |
| NSF-01 | Auto-cancel unpaid orders | Cron | Every 15 min | UC06 | Applies BR01; cancels orders in PENDING_PAYMENT > 24h |
| NSF-02 | Low-stock evaluation | Inventory transaction commit | On event | UC05, UC13 | Applies BR02; emits notification when stock < min |
| NSF-03 | Batch expiry check | Cron | Daily 00:00 | UC05, UC13 | Applies BR03; alerts items expiring ≤ 30 days |
| NSF-04 | Loyalty points award | Order paid | On event | UC07 | Applies BR07; 1 pt / 1,000 VND |
| NSF-05 | Stock FIFO consumption | Order placed/paid | On event | UC06 | Consumes oldest batch by expiry |
| NSF-06 | Daily sales roll-up | Cron | Daily 01:00 | UC09 | Pre-aggregates revenue per branch |
| NSF-07 | Token blacklist sync | User logout | On event | UC01 | Pushes revoked JWTs to all API gateways |
| NSF-08 | Backup scheduler | Cron | Daily 02:00 incremental, Sun 02:00 full | — | Per §4.7 NFR |
| NSF-09 | Notification retry | Channel failure | On event, exp backoff ×3 | UC13 | Retries SMS/email 3x |
| NSF-10 | Account auto-unlock | Cron | Every 5 min | UC01 | Unlocks accounts whose 30-min lockout has expired |
| NSF-11 | Invoice number generator | Payment success | On event | UC07 | Generates `INV-yyyymmdd-####` |
| NSF-12 | Order auto-number generator | Order created | On event | UC06 | Generates `ORD-yyyymmdd-####` |
| **NSF-13** | **AI OCR processing pipeline** *(NEW v1.4.0)* | Image uploaded | On event | **UC15** | Decodes prescription image, extracts medicine names + dosages via CV model. Posts to pharmacist review queue. |
| **NSF-14** | **AI semantic search indexer** *(NEW v1.4.0)* | Medicine/category change | On event | **UC15** | Re-indexes vector embeddings (e.g. OpenAI text-embedding-3-small) for symptom-based search. |
| **NSF-15** | **AI drug interaction cache** *(NEW v1.4.0)* | Drug added to catalog | On event | **UC15** | Pre-computes drug-drug interaction graph (twELVET / DrugBank / local rules) for instant lookup. |
| **NSF-16** | **AI demand forecasting** *(NEW v1.4.0)* | Cron | Daily 02:30 | **UC15, UC09** | Predicts next 30/60/90-day stock demand by SKU per branch using Prophet / ARIMA. |
| **NSF-17** | **AI chat model inference** *(NEW v1.4.0)* | User sends message | On event | **UC15** | Calls LLM (GPT-4o-mini / local Llama-3) with PCMS RAG context (medicines, symptoms, policies). |
| **NSF-18** | **AI prescription anomaly detector** *(NEW v1.4.0)* | Prescription created | On event | **UC15, UC12** | Flags suspicious patterns: duplicate ingredients, contraindication, dosage > max, age mismatch. |
| **NSF-19** | **Customer follow-up scheduler** *(NEW v1.4.0)* | Order paid | On event | **UC16** | Schedules 3-day, 7-day, 14-day follow-up push notifications asking "thuốc có hiệu quả không?" |
| **NSF-20** | **Voucher/coupon expiry** *(NEW v1.4.0)* | Cron | Every 1 hour | **UC19** | Disables expired vouchers, notifies users before expiry (24h, 1h) |
| **NSF-21** | **Flash sale scheduler** *(NEW v1.4.0)* | Cron | Every 1 min | **UC19** | Activates flash sales, decrements stock atomically, ends on timeout or sold-out |
| **NSF-22** | **GPS pharmacy locator index** *(NEW v1.4.0)* | Branch update | On event | **UC14** | Re-indexes branches into PostGIS / Elasticsearch for `near me` queries |
| **NSF-23** | **Mobile push notification dispatcher** *(NEW v1.4.0)* | Various triggers | On event | **UC17, UC13** | Routes to FCM (Android) + APNs (iOS), respects user preferences |
| **NSF-24** | **Calendar sync for medication reminders** *(NEW v1.4.0)* | Reminder saved | On event | **UC17** | Pushes events to user's phone calendar (iOS EventKit / Android Calendar Provider) |

**Code Generation Patterns (SRS §5.4 + §3.2):**

| Code Type | Pattern | Example | Generator (NSF) |
| :---- | :---- | :---- | :---- |
| Order number | `ORD-yyyymmdd-####` | `ORD-20260613-0042` | NSF-12 |
| Invoice number | `INV-yyyymmdd-####` | `INV-20260613-0042` | NSF-11 |
| Customer code | `CUST-yyyy####` | `CUST-20260001` | On customer create |
| Prescription code | `RX-yyyy####` | `RX-20260001` | On prescription sign |

**Background Job Architecture:**

```
┌─────────────────────────────────────────────────────────┐
│                   Scheduler Service                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  Cron Jobs   │  │  Event      │  │  Queue      │    │
│  │  (Scheduled) │  │  Listeners  │  │  Workers    │    │
│  │  NSF-01,03,  │  │  NSF-02,04, │  │  NSF-09     │    │
│  │  06,08,10    │  │  05,07,11,12│  │             │    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │
│         └────────────────┼────────────────┘            │
│                          ▼                             │
│  ┌─────────────────────────────────────────────────┐  │
│  │              Job Processing Queue                │  │
│  │     (Redis-backed, with dead-letter handling)    │  │
│  └─────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

#### 3.1.6 Entity Relationship Diagram

The system's data model consists of **27 core entities** (12 B2B + 15 B2C/AI/Pharmacist/Mobile) with well-defined relationships, aligned with SRS v1.4.0 §3.1.5 + §3.1.6.1.

**ER Diagram:** [entity-relationship.puml](./diagrams/entity-relationship.puml)

**Entity Summary (12 B2B — giữ nguyên từ v1.3.0):**

| Entity | Description | Primary Fields | Managed By |
| :---- | :---- | :---- | :---- |
| User | System user account | id (UUID), email, role, branch_id | Admin, CEO |
| Branch | Pharmacy branch | id (UUID), code, name, manager_id, lat/lng, opening_hours | Admin, CEO |
| Medicine | Medicine / product | id (UUID), sku, name, price, category_id | Admin, CEO, Pharmacist |
| Category | Medicine category | id (UUID), name | Admin, CEO |
| Customer | End customer | id (UUID), code, phone, points, loyalty_tier, health_wallet_balance, consent_ai, preferred_pharmacist_id | Customer, Pharmacist |
| Order | Sales order | id (UUID), order_number, customer_id, branch_id, total, status, channel, delivery_address_id, voucher_id | Pharmacist, Customer |
| OrderItem | Order line item | id (UUID), order_id, medicine_id, qty, unit_price, batch_id | Pharmacist |
| InventoryBatch | Per-branch batch stock | id (UUID), medicine_id, branch_id, batch_no, qty_on_hand, expiry_date | Branch Manager |
| InventoryTransaction | Stock movement record | id (UUID), batch_id, type, qty, reason, actor_id | Branch Manager |
| Supplier | Medicine supplier | id (UUID), name, tax_code, phone, bank_account | Admin, CEO |
| Prescription | Doctor's prescription | id (UUID), code, patient_id, doctor_id, diagnosis, signature_hash | Pharmacist |
| Notification | System notification | id (UUID), recipient_id, channel, status | System, Admin, CEO |

**Entity Summary (15 NEW v1.4.0 — B2C + AI + Pharmacist + Mobile + Health + E-com):**

| Entity | Description | Primary Fields | Managed By |
| :---- | :---- | :---- | :---- |
| CustomerAddress | Delivery address book | customer_id, province, is_default | Customer |
| CustomerFamily | Family member (Tài khoản gia đình) | owner_id, member_name, relationship, allergies, chronic_conditions | Customer |
| ProductReview | Product review & rating | product_id, customer_id, rating, body, is_verified_purchase, status | Customer, AI moderation |
| Voucher | Coupon / flash-sale code | code, type, value, valid_to, used_count, usage_limit | Admin, CEO |
| PrescriptionB2C | Uploaded prescription (with AI OCR) | image_url, ocr_result, pharmacist_status | Customer, Pharmacist, AI |
| MedicationReminder | Nhắc uống thuốc | customer_id, schedule, start_date, adherence_rate | Customer (via App) |
| MedicationIntake | Actual intake log | reminder_id, status, taken_at | System auto |
| AIChatSession | AI chat history | customer_id, messages, status, escalated_to_pharmacist | Customer, AI, Pharmacist |
| DrugInteractionRule | Drug-drug interaction knowledge | drug_a_id, drug_b_id, severity, recommendation | Admin, AI |
| HealthQuizResult | Health quiz answers + advice | quiz_slug, score, risk_level, advice_shown | Customer, AI |
| FollowUpTask | Post-sale follow-up tasks | customer_id, order_id, type, status | Pharmacist, System |
| FlashSale | Time-limited promotion | name, starts_at, ends_at, items | Admin, CEO |

> **Note (SRS Round 4 alignment):** Entity list mở rộng 12 → 27. Key additions: `CustomerAddress` + `CustomerFamily` cho B2C shipping + Tài khoản gia đình; `MedicationReminder` + `MedicationIntake` cho app nhắc uống thuốc; `AIChatSession` + `DrugInteractionRule` cho AI Engine; `FollowUpTask` cho Pharmacist Tools; `Customer.health_wallet_balance` + `consent_ai` + `preferred_pharmacist_id` mở rộng. Branch bổ sung `lat/lng/opening_hours/services` cho GPS locator. Order bổ sung `channel/delivery_address_id/voucher_id` cho B2C.

**Database Schema xem §5 — bổ sung 13 bảng mới:**
- `customer_addresses` (Entity 13)
- `customer_family` (Entity 14)
- `product_reviews` (Entity 15)
- `vouchers` + `voucher_usages` (Entity 16)
- `prescriptions_b2c` (Entity 18)
- `medication_reminders` + `medication_intakes` (Entity 19, 20)
- `ai_chat_sessions` (Entity 21)
- `drug_interaction_rules` (Entity 22)
- `health_quiz_results` (Entity 23)
- `follow_up_tasks` (Entity 24)
- `flash_sales` (Entity 25)

**Key Relationships:**

| Relationship | Type | Description |
| :---- | :---- | :---- |
| User → Branch | N:1 | Each user (except Admin/Customer) is assigned to one branch |
| Branch → User (Manager) | N:1 | Each branch has one branch manager |
| Medicine → Category | N:1 | Each medicine belongs to one category |
| Medicine → Supplier | N:1 | Each medicine has one default supplier |
| Medicine → InventoryBatch | 1:N | Each medicine has many batches per branch |
| InventoryBatch → Branch | N:1 | Each batch lives at one branch |
| InventoryBatch → InventoryTransaction | 1:N | Each batch has many stock movements |
| Order → Customer | N:1 | Each order belongs to one customer |
| Order → Branch | N:1 | Each order is fulfilled at one branch |
| Order → User (Staff) | N:1 | Each in-store order has a creating staff |
| Order → OrderItem | 1:N | Each order contains multiple items |
| OrderItem → Medicine | N:1 | Each item references one medicine |
| OrderItem → InventoryBatch | N:1 | Each item consumes one FIFO-picked batch |
| Order → Prescription | N:1 | An order may link to a prescription |
| Prescription → Customer (Patient) | N:1 | Each Rx is for one patient |
| Prescription → User (Doctor) | N:1 | Each Rx is signed by one pharmacist |
| Notification → User | N:1 | Each notification is sent to one recipient |

**ER Diagram Visual (v1.3.0):**

```
                          ┌────────────┐
                          │  Category  │
                          └──────┬─────┘
                                 │ N:1
                                 ▼
┌──────────┐     ┌──────────┐  ┌──────────┐     ┌──────────────┐
│  Branch  │────<│   User   │  │ Medicine │>────│  Supplier    │
└────┬─────┘     └──────────┘  └────┬─────┘     └──────────────┘
     │ 1:N                          │ 1:N
     ▼                              ▼
┌────────────────┐            ┌──────────────────┐
│InventoryBatch  │            │   OrderItem      │
└────┬───────────┘            └────────┬─────────┘
     │ 1:N                            │ N:1
     ▼                                ▼
┌────────────────────┐          ┌──────────┐
│InventoryTransaction│          │  Order   │──> Prescription
└────────────────────┘          └────┬─────┘
                                     │ N:1
                                     ▼
                                ┌──────────┐
                                │ Customer │
                                └──────────┘

  (Notification is a stand-alone entity referencing User)
```

---

#### 3.1.7 AI Engine Architecture — NEW v1.4.0

> Một microservice mới trong hệ thống PCMS — phục vụ 12 tính năng AI cho cả B2B (pharmacist) và B2C (customer).

**High-level architecture:**

```
┌──────────────────────────────────────────────────────────────────────┐
│                      PCMS PLATFORM (v1.4.0)                            │
│                                                                       │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │  B2B Web   │  │  B2C Portal  │  │  Mobile App  │  │  POS App  │  │
│  │  (Next.js) │  │  (Next.js)   │  │  (RN/Flutter)│  │  (PWA)    │  │
│  └──────┬─────┘  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘  │
│         │                │                 │                │         │
│         └────────────────┴────────┬────────┴────────────────┘         │
│                                   │ gRPC / WebSocket                  │
│                                   ▼                                    │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                       API Gateway                                │ │
│  │  /api/v1/* (B2B)  /shop/* (B2C)  /mobile/* (app)               │ │
│  └────────────────────────────┬─────────────────────────────────────┘ │
│                                │                                       │
│        ┌───────────────────────┼───────────────────────┐              │
│        ▼                       ▼                       ▼              │
│  ┌──────────┐            ┌──────────┐            ┌──────────────┐     │
│  │  Order   │            │ Customer │            │  AI Engine   │     │
│  │  Service │            │  Service │            │  (NEW)       │     │
│  └──────────┘            └──────────┘            └──────┬───────┘     │
│                                                        │              │
│                          ┌─────────────────────────────┤              │
│                          │                             │              │
│                          ▼                             ▼              │
│                  ┌──────────────┐            ┌──────────────────┐     │
│                  │  PostgreSQL  │            │  Vector Store    │     │
│                  │  + Redis     │            │  (pgvector /     │     │
│                  │  + PostGIS   │            │   Pinecone)      │     │
│                  └──────────────┘            └──────────────────┘     │
│                                                                       │
│                  ┌──────────────┐            ┌──────────────────┐     │
│                  │   LLM API    │            │  Knowledge Base  │     │
│                  │  (OpenAI /   │            │  - Medicines     │     │
│                  │  local Llama)│            │  - Symptoms      │     │
│                  │              │            │  - Drug Rules    │     │
│                  └──────────────┘            │  - Policies      │     │
│                                               └──────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

**AI Engine internal components:**

| Component | Tech stack | Purpose |
|:----------|:-----------|:--------|
| **LLM Gateway** | OpenAI GPT-4o-mini (default) hoặc local Llama-3 8B | Inference, generation, classification |
| **Embedding Service** | OpenAI text-embedding-3-small (1536 dim) | Vector hóa sản phẩm + triệu chứng + policy |
| **Vector Store** | PostgreSQL pgvector (dev) hoặc Pinecone (prod) | Semantic search (NSF-14) |
| **RAG Pipeline** | LangChain / LlamaIndex | Context retrieval + prompt assembly + LLM call |
| **Guardrails** | NeMo Guardrails / custom regex | Toxicity check, PII redaction, scope limit |
| **OCR Service** | Tesseract + custom CV model / Google Vision API | NSF-13: parse prescription image |
| **Drug Interaction KB** | Local rules + DrugBank API | NSF-15: pre-computed interaction graph |
| **Forecasting Engine** | Prophet / ARIMA / LightGBM | NSF-16: demand forecast 30/60/90 days |
| **Conversation Memory** | Redis + Postgres | AIChatSession storage (NSF-17) |

**AI inference flow (CHAT-AI):**

```
Customer → CHAT-AI → API Gateway → AI Engine
                                    ↓
                  1. Sanitize input (PII redaction, length cap)
                                    ↓
                  2. Embed query (text-embedding-3-small)
                                    ↓
                  3. Vector search → top-5 documents (pgvector)
                                    ↓
                  4. Retrieve customer context (last 5 orders, allergies)
                                    ↓
                  5. Build prompt: {system + context + customer + query}
                                    ↓
                  6. LLM inference (GPT-4o-mini, temp=0.3)
                                    ↓
                  7. Post-process: extract citations, check safety
                                    ↓
                  8. Check confidence → escalate if < 0.6 (FR15.3)
                                    ↓
                  9. Log session → AIChatSession table
                                    ↓
                  10. Return response to client
```

**Performance NFRs (AI):**

| Metric | Target | Ideal |
|:-------|:------|:-----|
| Chat response time (P95) | < 3s | < 1.5s |
| OCR processing time (P95) | < 10s | < 5s |
| Drug interaction check | < 100ms | < 50ms |
| Semantic search (P95) | < 500ms | < 200ms |
| Forecast generation | < 30s (30-day) | < 10s |
| LLM API uptime target | 99.5% | 99.9% |
| Concurrent AI sessions | 500 | 2000 |

**Fallback strategy:**
- LLM API fail → trả message "Tạm thời AI không khả dụng" + offer form liên hệ dược sĩ
- Confidence < 0.6 → auto-escalate to human pharmacist (FR15.3)
- Emergency keywords → bypass RAG, immediate escalate to 115 (Vietnamese emergency number)
- Rate limit > 100 req/min/user → queue + reject

**Data privacy (GDPR + Vietnamese data laws):**
- Tất cả AI processing log kèm `consent_ai` flag
- Không gửi raw PII (SĐT, email) tới LLM API — chỉ gửi medical context
- AIChatSession lưu trên server VN, không sync cross-border
- User có thể xoá lịch sử chat (right to be forgotten)

---

#### 3.1.8 Mobile App Architecture — NEW v1.4.0

> Ứng dụng native iOS + Android, dùng React Native hoặc Flutter (TBD).

**Recommended stack:** React Native + Expo (TypeScript) cho code reuse ~80% với web Next.js.

**Key modules:**

| Module | Native API | Purpose |
|:-------|:-----------|:--------|
| Push | FCM (Android) + APNs (iOS) | NSF-23 |
| GPS | react-native-geolocation | STORE-LOCATOR, "near me" |
| Camera | react-native-camera | AI-RX-OCR (NSF-13) |
| Barcode | react-native-vision-camera + ML Kit | SHOP-VERIFY-ORIGIN |
| Calendar | iOS EventKit + Android Calendar Provider | NSF-24 |
| Biometric | Face ID / Touch ID / Fingerprint | Quick login |
| Offline | AsyncStorage + redux-persist | NSF cache catalog |
| Video call | WebRTC + Agora/Twilio | RX-CONSULT |
| AI chat | WebSocket to AI Engine | CHAT-AI |

**Bundle size targets:** Android ≤ 50MB, iOS ≤ 150MB (FR17.15).

---

### 3.2 Frontend Architecture

**Technology Stack:**
- **Framework:** React 18 with TypeScript
- **State Management:** Redux Toolkit with RTK Query
- **UI Library:** Material-UI v5
- **Routing:** React Router v6
- **Forms:** React Hook Form with Zod validation
- **HTTP Client:** Axios with interceptors

**Component Hierarchy:** [frontend-class-diagram.puml](./diagrams/frontend-class-diagram.puml)

**State Management:** [frontend-state.puml](./diagrams/frontend-state.puml)

**Key Frontend Modules:**

| Module | Description | Key Components |
|--------|-------------|----------------|
| AuthModule | Authentication flows | LoginPage, LogoutButton, AuthGuard |
| DashboardModule | Main dashboard | DashboardLayout, Sidebar, Header |
| BranchModule | Branch management | BranchList, BranchForm, BranchDetail |
| MedicineModule | Medicine catalog | MedicineList, MedicineCard, MedicineDetail |
| InventoryModule | Stock operations | StockList, ImportForm, TransferDialog |
| OrderModule | Order processing | OrderList, OrderForm, CartView |
| PaymentModule | Payment handling | PaymentForm, InvoicePrint, QRScanner |
| CustomerModule | Customer management | CustomerList, CustomerProfile |
| ReportModule | Reports and analytics | ReportChart, ExportButton |

### 3.3 Backend Architecture

**Technology Stack:**
- **Runtime:** Node.js 20 LTS
- **Framework:** Express.js v4
- **Language:** TypeScript
- **ORM:** Prisma
- **Validation:** Zod
- **Authentication:** Passport.js with JWT

**Backend Classes:** [backend-class-diagram.puml](./diagrams/backend-class-diagram.puml)

**Service Layer:** [service-layer.puml](./diagrams/service-layer.puml)

**Key Backend Modules:**

| Module | Description | Key Classes |
|--------|-------------|-------------|
| AuthController | Auth endpoints | AuthController, AuthMiddleware |
| UserController | User CRUD | UserController, UserService |
| BranchController | Branch management | BranchController, BranchService |
| MedicineController | Medicine operations | MedicineController, MedicineService |
| InventoryController | Stock operations | InventoryController, InventoryService |
| OrderController | Order processing | OrderController, OrderService |
| PaymentController | Payment handling | PaymentController, PaymentService |
| CustomerController | Customer management | CustomerController, CustomerService |
| ReportController | Report generation | ReportController, ReportService |
| SupplierController | Supplier management | SupplierController, SupplierService |
| PrescriptionController | Prescription handling | PrescriptionController, PrescriptionService |
| NotificationController | Notifications | NotificationController, NotificationService |

---

## 4. Detailed Component Design

### 4.1 Authentication Module

**Component Location:** `./component-designs/auth-*`

**Class Diagrams:**
- Backend: [auth-class-backend.puml](./component-designs/auth-class-backend.puml)
- Frontend: [auth-class-frontend.puml](./component-designs/auth-class-frontend.puml)

**Sequence Diagram:** [auth-sequence.puml](./component-designs/auth-sequence.puml)

**State Diagram:** [auth-state.puml](./component-designs/auth-state.puml)

**Database Tables:** [auth_tables.sql](./db/tables/auth_tables.sql)

**Module Responsibilities (aligned with SRS UC01, v1.3.0):**
- User authentication with email/password (FR1.1)
- JWT (RS256) generation and validation; access 15 min, refresh 7 days (FR1.2)
- Account lockout after 5 failed attempts within 15 min, 30 min duration (BR05, FR1.5)
- Refresh token rotation (single-use) with blacklist sync (NSF-07)
- Forgot / reset password via email token (FR1.6, 15 min TTL)
- Email verification + resend (FR1.6)
- Role-based access control for 5 roles (FR1.4)
- `/healthz` + `/readyz` probes (CR-07)

**Key Classes:**

| Class | Responsibility | Public Methods |
| :---- | :---- | :---- |
| AuthController | HTTP request handling | login, logout, refreshToken, validateToken, changePassword, forgotPassword, resetPassword, verifyEmail, resendVerification, unlockAccount |
| AuthService | Business logic | authenticate, generateToken, validateCredentials, hashPassword, verifyPassword, lockAccount, unlockAccount, isAccountLocked |
| TokenService | Token management | createAccessToken, createRefreshToken, verifyToken, decodeToken, blacklistJti, isJtiRevoked |
| UserRepository | Data access | findByEmail, findById, updateLastLogin, incrementFailedAttempts, setEmailVerified |
| AuthMiddleware | Request filtering | authenticate, authorize(roles), checkPermission, rateLimit |
| AuthValidator | Input validation | validateEmail, validatePassword, validateLoginRequest, validateResetToken |

**Data Flow:**
1. User submits login credentials
2. AuthController receives request
3. AuthValidator validates input format
4. AuthService checks credentials against database
5. TokenService generates JWT access and refresh tokens
6. UserRepository updates last login timestamp
7. Response returned with tokens and user info

### 4.2 User Management Module

**Component Location:** `./component-designs/user-*`

**Class Diagrams:**
- Backend: [user-management-class-backend.puml](./diagrams/uc-02/uc-02-class-backend.puml)
- Frontend: [user-management-class-frontend.puml](./diagrams/uc-02/uc-02-class-frontend.puml)

**Sequence Diagram:** [user-management-sequence.puml](./diagrams/uc-02/uc-02-sequence.puml)

**State Diagram:** [user-management-state.puml](./diagrams/uc-02/uc-02-statediagram.puml)

**Database Tables:** [user_management_tables.sql](./db/tables/user_management_tables.sql)

**Module Responsibilities:**
- CRUD operations for user accounts
- Role assignment and management
- Branch assignment for staff
- User profile management

### 4.3 Branch Management Module

**Component Location:** `./component-designs/branch-*`

**Class Diagrams:**
- Backend: [branch-management-class-backend.puml](./component-designs/branch-management-class-backend.puml)
- Frontend: [branch-management-class-frontend.puml](./component-designs/branch-management-class-frontend.puml)

**Sequence Diagram:** [branch-management-sequence.puml](./component-designs/branch-management-sequence.puml)

**State Diagram:** [branch-management-state.puml](./component-designs/branch-management-state.puml)

**Database Tables:** [branch_management_tables.sql](./db/tables/branch_management_tables.sql)

**Module Responsibilities:**
- CRUD operations for pharmacy branches
- Branch manager assignment
- Branch performance tracking

### 4.4 Medicine Management Module

**Component Location:** `./component-designs/medicine-*`

**Class Diagrams:**
- Backend: [medicine-management-class-backend.puml](./diagrams/uc-04/uc-04-class-backend.puml)
- Frontend: [medicine-management-class-frontend.puml](./diagrams/uc-04/uc-04-class-frontend.puml)

**Sequence Diagram:** [medicine-management-sequence.puml](./diagrams/uc-04/uc-04-sequence.puml)

**State Diagram:** [medicine-management-state.puml](./diagrams/uc-04/uc-04-statediagram.puml)

**Database Tables:** [medicine_management_tables.sql](./db/tables/medicine_management_tables.sql)

**Module Responsibilities:**
- Medicine catalog CRUD
- Category management
- Price and stock level management
- Expiry date tracking
- Prescription-required flag management

### 4.5 Inventory Management Module

**Component Location:** `./component-designs/inventory-*`

**Class Diagrams:**
- Backend: [inventory-management-class-backend.puml](./diagrams/uc-05/uc-05-class-backend.puml)
- Frontend: [inventory-management-class-frontend.puml](./diagrams/uc-05/uc-05-class-frontend.puml)

**Sequence Diagram:** [inventory-management-sequence.puml](./diagrams/uc-05/uc-05-sequence.puml)

**State Diagram:** [inventory-management-state.puml](./diagrams/uc-05/uc-05-statediagram.puml)

**Database Tables:** [inventory_tables.sql](./db/tables/inventory_tables.sql) (`inventory_batches`, `inventory_transactions`)

**Module Responsibilities (aligned with SRS UC05, v1.3.0):**
- **Per-batch stock tracking** via `InventoryBatch` (medicine × branch × batch_no × expiry_date × qty_on_hand)
- **Stock import** — create new batch + IMPORT transaction; batch_no unique per medicine; expiry must be future
- **Stock export** — decrement qty_on_hand FIFO (oldest expiry first); EXPORT transaction; reason required (BR06 on cancel)
- **Inter-branch transfer** — atomic TRANSFER_OUT + TRANSFER_IN pair (BR06 stock restore on cancel)
- **Low-stock alerts** — emit notification when qty_on_hand < min_stock_level (BR02, NSF-02)
- **Expiry alerts** — daily scan for batches expiring ≤ 30 days (BR03, NSF-03, MSG30)
- Low stock alerts
- Expiry date alerts

### 4.6 Order Management Module

**Component Location:** `./component-designs/order-*`

**Class Diagrams:**
- Backend: [order-management-class-backend.puml](./diagrams/uc-06/uc-06-class-backend.puml)
- Frontend: [order-management-class-frontend.puml](./diagrams/uc-06/uc-06-class-frontend.puml)

**Sequence Diagram:** [order-management-sequence.puml](./diagrams/uc-06/uc-06-sequence.puml)

**State Diagram:** [order-management-state.puml](./diagrams/uc-06/uc-06-statediagram.puml)

**Database Tables:** [order_tables.sql](./db/tables/order_tables.sql)

**Module Responsibilities:**
- Order creation and management
- Automatic discount application (5% for qty >= 10)
- Order auto-cancellation after 24 hours
- Order approval workflow

### 4.7 Payment Module

**Component Location:** `./component-designs/payment-*`

**Class Diagrams:**
- Backend: [payment-class-backend.puml](./diagrams/uc-07/uc-07-class-backend.puml)
- Frontend: [payment-class-frontend.puml](./diagrams/uc-07/uc-07-class-frontend.puml)

**Sequence Diagram:** [payment-sequence.puml](./diagrams/uc-07/uc-07-sequence.puml)

**State Diagram:** [payment-state.puml](./diagrams/uc-07/uc-07-statediagram.puml)

**Database Tables:** [payment_tables.sql](./db/tables/payment_tables.sql)

**Module Responsibilities:**
- Multiple payment methods (cash, card, QR)
- Change calculation for cash payments
- Invoice generation
- Transaction recording

### 4.8 Customer Management Module

**Component Location:** `./component-designs/customer-*`

**Class Diagrams:**
- Backend: [customer-management-class-backend.puml](./component-designs/customer-management-class-backend.puml)
- Frontend: [customer-management-class-frontend.puml](./component-designs/customer-management-class-frontend.puml)

**Sequence Diagram:** [customer-management-sequence.puml](./component-designs/customer-management-sequence.puml)

**State Diagram:** [customer-state.puml](./diagrams/uc-08/uc-08-statediagram.puml)

**Database Tables:** [customer_management_tables.sql](./db/tables/customer_management_tables.sql)

**Module Responsibilities:**
- Customer registration
- Unique customer code generation
- Purchase history tracking
- Loyalty points management

### 4.9 Report Module

**Component Location:** `./component-designs/report-*`

**Database Tables:** [report_tables.sql](./db/tables/report_tables.sql)

**Module Responsibilities:**
- Revenue reports by date range
- Inventory reports
- Staff performance reports
- Excel and PDF export

### 4.10 Supplier Management Module

**Database Tables:** [supplier_management_tables.sql](./db/tables/supplier_management_tables.sql)

**Module Responsibilities:**
- Supplier CRUD operations
- Supply history tracking
- Supplier performance analysis

### 4.11 Prescription Module

**Class Diagrams:**
- Backend: [prescription-class-backend.puml](./diagrams/uc-12/uc-12-class-backend.puml)
- Frontend: [prescription-class-frontend.puml](./diagrams/uc-12/uc-12-class-frontend.puml)

**Module Responsibilities:**
- Prescription creation with unique code
- Prescription items management
- Prescription printing

### 4.12 Notification Module

**Database Tables:** [notification_tables.sql](./db/tables/notification_tables.sql)

**Module Responsibilities:**
- Low stock alerts
- Expiry date alerts
- Email and SMS notifications
- In-app notification management

### 4.13 Common Design Patterns

**Error Handling:** [error-handling.puml](./diagrams/common/error-handling.puml)

**Logging:** [logging.puml](./diagrams/common/logging.puml)

**Configuration:** [configuration.puml](./diagrams/common/configuration.puml)

---

### 4.14 Customer Portal (B2C) Module — NEW v1.4.0

**Component Location:** `./component-designs/customer-portal-*`

**Database Tables:** `customers_extended`, `customer_addresses`, `customer_family`, `vouchers`, `voucher_usages`, `product_reviews`, `flash_sales`, `flash_sale_items`

**Module Responsibilities:**
- B2C e-commerce: catalog browse, PDP, cart, checkout, order tracking
- Tra cứu thuốc/dược chất/dược liệu (3 tầng)
- Tìm nhà thuốc gần nhất (PostGIS + Leaflet/Mapbox)
- Đặt lịch tiêm chủng + sổ tiêm điện tử
- Quản lý tài khoản + Tài khoản gia đình + Ví Khỏe Nhà Ta
- Flash sale với atomic stock decrement
- Voucher system với limit + per-user enforcement
- Product review + moderation

**Public routes (Guest OK):** `/`, `/shop`, `/shop/[id]`, `/shop/catalog/[slug]`, `/tra-cuu-thuoc`, `/he-thong-cua-hang`, `/tai-khoan-gia-dinh`, `/bai-viet/[slug]`, `/health-quiz/[slug]`, `/tin-tuc/[slug]`

**Authenticated routes (Customer):** `/cart`, `/checkout`, `/orders`, `/orders/[id]`, `/profile`, `/wallet`, `/family`, `/prescriptions/upload`, `/reviews/new`, `/flash-sale/[id]`

**Key flows:** See SRS v1.4.0 §3.2.6 (UC14).

---

### 4.15 AI Engine Module — NEW v1.4.0

**Component Location:** `./component-designs/ai-engine-*` (separate microservice)

**Database Tables:** `ai_embeddings`, `ai_knowledge_base`, `ai_conversations`, `ocr_jobs`, `drug_interaction_rules`, `health_quiz_results`, `ai_chat_sessions`

**Module Responsibilities:**
- 12 AI features per SRS v1.4.0 §3.2.7 (UC15)
- RAG pipeline (LangChain / LlamaIndex)
- LLM Gateway (OpenAI GPT-4o-mini default, local Llama-3 fallback)
- Vector Store (pgvector dev / Pinecone prod)
- OCR prescription parsing
- Drug interaction lookup
- Demand forecasting
- Conversation memory

**Public API:** `POST /ai/chat`, `POST /ai/ocr/prescription`, `POST /ai/drug-check`, `GET /ai/semantic-search`, `GET /ai/forecast/:medicineId`

**Internal API (private):** `POST /ai/embed`, `POST /ai/rerank`, `GET /ai/knowledge/:id`

**Tech stack:** Python 3.11 + FastAPI + LangChain + pgvector + Celery (background jobs)

**Container:** Docker image `pcms-ai-engine:v1.4.0`, deploy to internal k8s cluster, replica = 3 (HA).

**See §3.1.7 AI Engine Architecture for full details.**

---

### 4.16 Pharmacist Workbench Module — NEW v1.4.0

**Component Location:** `./component-designs/pharmacist-workbench-*`

**Database Tables:** `follow_up_tasks`, `medication_reminders`, `medication_intakes` (read), plus reuse of `customers`, `orders`, `prescriptions`

**Module Responsibilities:**
- RX-CONSOLE: unified dashboard cho pharmacist tại quầy
- Customer 360° profile (timeline đầy đủ)
- Tư vấn 1-1 (text/voice/video) với khách
- Real-time AI alerts: drug interaction, allergy cross-check, max-dose, duplicate ingredient
- AI cross-sell / up-sell suggestions
- Generic substitution proposals
- Follow-up scheduling (3/7/14-day)
- VIP marking
- Loyalty score (0-100)

**Available to:** Pharmacist (primary), Admin/CEO (read), Customer (read own only)

**Key flows:** See SRS v1.4.0 §3.2.8 (UC16).

---

### 4.17 Mobile App Module — NEW v1.4.0

**Component Location:** `./mobile-app/` (React Native + Expo)

**Database Tables (server-side):** `push_tokens`, `push_logs`, `calendar_syncs`, reuses `medication_reminders` + `medication_intakes`

**Module Responsibilities:**
- iOS + Android native app
- Push notification (FCM + APNs)
- GPS-based pharmacy locator
- Barcode/QR scan
- AI chat interface
- Medication reminder with calendar sync
- Family account management
- Real-time order tracking
- VNeID login

**Tech stack:** React Native + Expo (TypeScript) + WatermelonDB (offline) + React Navigation + Zustand (state)

**Backend integration:** REST API + WebSocket (for AI chat streaming)

**See §3.1.8 Mobile App Architecture for full details.**

---

### 4.18 Health Tools Module — NEW v1.4.0

**Component Location:** `./features/health/` (frontend-only, uses existing Customer/AI service)

**Database Tables:** `health_quiz_results`, plus reuse of `ai_conversations`

**Module Responsibilities:**
- 8+ self-assessment health quizzes
- Risk scoring (LOW/MODERATE/HIGH)
- AI-generated advice
- CTA to consultation / lab booking
- Lead-gen for guests

**Quizzes:** Trí nhớ, tiền đái tháo đường, suy giáp, hen (ACT), tim mạch (Framingham), Alzheimer (mini-cog), GERD-Q, bình xịt.

---

### 4.19 E-commerce Operations Module — NEW v1.4.0

**Component Location:** `./features/ecom-ops/`

**Database Tables:** `vouchers`, `voucher_usages`, `flash_sales`, `flash_sale_items`, `product_reviews`

**Module Responsibilities:**
- Voucher/coupon CRUD (Admin)
- Flash sale scheduling + atomic stock control
- Product review moderation
- Installment integration (Home Credit, FE Credit APIs)
- Live chat (reuses AI engine + pharmacist tools)

---

## 5. Database Design

> All schema details below are aligned with SRS v1.4.0 §3.1.6 (Entity Details) + §3.1.6.1 (New Entities v1.4.0). Primary keys are **UUID** (CHAR(36) or BINARY(16)). Timestamps are stored in **UTC** and rendered in `Asia/Ho_Chi_Minh` (CR-02). Amounts use **DECIMAL(15,2)** in VND (CR-03). All business tables include `created_at`, `updated_at`, `created_by`, `updated_by` audit fields (CR-09). Soft-delete is the default (`status` enum) for User, Customer, Supplier, Medicine (CR-08).

### 5.1 Entity Relationship Diagram

**ER Diagram:** [entity-relationship.puml](./diagrams/entity-relationship.puml)

### 5.2 Database Schema (per service)

**Full Schema:** [schema.sql](./db/schema.sql)

| Service | Tables | Description |
| :---- | :---- | :---- |
| auth_service | users (subset), refresh_tokens, blacklisted_tokens, login_history, password_resets | User auth and session management |
| user_service | users, audit_log | User accounts and roles |
| branch_service | branches | Pharmacy branch information |
| medicine_service | medicines, categories | Medicine catalog and categories |
| inventory_service | inventory_batches, inventory_transactions | Per-batch stock and stock movements |
| order_service | orders, order_items | Order and order line items (FK to inventory_batches) |
| payment_service | payments, invoices | Payment transactions and invoices |
| customer_service | customers | Customer information (with CUST- code) |
| report_service | report_schedules, report_exports, daily_sales_rollup | Scheduled and exported reports |
| supplier_service | suppliers, supply_history | Supplier management (with tax_code, bank info) |
| prescription_service | prescriptions | Prescription records (signature_hash) |
| notification_service | notifications, notification_templates | System notifications |
| **customer_service** *(NEW v1.4.0)* | **customers_extended, customer_addresses, customer_family, health_wallet_ledger, vouchers, voucher_usages, product_reviews, ai_chat_sessions, drug_interaction_rules, health_quiz_results, follow_up_tasks, flash_sales, flash_sale_items, prescriptions_b2c, medication_reminders, medication_intakes** | **B2C customer data + AI + Mobile + Health + E-com** |
| **ai_engine_service** *(NEW v1.4.0)* | **ai_embeddings, ai_knowledge_base, ai_conversations, ocr_jobs** | **AI Engine microservice** |
| **mobile_push_service** *(NEW v1.4.0)* | **push_tokens, push_logs, calendar_syncs** | **Mobile app push + calendar sync** |

### 5.3 Table Definitions

> **v1.4.0 mở rộng:** 27 bảng (12 B2B cũ + 15 B2C/AI/Pharmacist/Mobile/Health/E-com mới). Schema cho 12 bảng B2B giữ nguyên; 15 bảng mới liệt kê ở §5.3.x.

#### users (auth_service + user_service)
```sql
CREATE TABLE users (
    id              CHAR(36) PRIMARY KEY,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            ENUM('ADMIN','CEO','BRANCH_MANAGER','PHARMACIST','CUSTOMER') NOT NULL,
    branch_id       CHAR(36) NULL,
    status          ENUM('ACTIVE','INACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    last_login_at   DATETIME NULL,
    last_login_ip   VARCHAR(45) NULL,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      CHAR(36) NULL,
    updated_by      CHAR(36) NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_branch (branch_id)
);
```

#### branches
```sql
CREATE TABLE branches (
    id          CHAR(36) PRIMARY KEY,
    code        VARCHAR(10) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    manager_id  CHAR(36) NULL,
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  CHAR(36) NULL,
    updated_by  CHAR(36) NULL,
    FOREIGN KEY (manager_id) REFERENCES users(id)
);
```

#### medicines
```sql
CREATE TABLE medicines (
    id                    CHAR(36) PRIMARY KEY,
    sku                   VARCHAR(20) NOT NULL UNIQUE,
    name                  VARCHAR(200) NOT NULL,
    category_id           CHAR(36) NOT NULL,
    supplier_id           CHAR(36) NULL,
    price                 DECIMAL(15,2) NOT NULL CHECK (price > 0),
    unit                  VARCHAR(20) NOT NULL,    -- box / bottle / strip
    prescription_required BOOLEAN NOT NULL DEFAULT FALSE,
    image_url             VARCHAR(255) NULL,
    status                ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by            CHAR(36) NULL,
    updated_by            CHAR(36) NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);
```

#### categories
```sql
CREATE TABLE categories (
    id          CHAR(36) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### customers
```sql
CREATE TABLE customers (
    id          CHAR(36) PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,    -- CUST-yyyy####
    name        VARCHAR(100) NOT NULL,
    phone       VARCHAR(20) NOT NULL UNIQUE,
    email       VARCHAR(100) NULL UNIQUE,
    address     VARCHAR(255) NULL,
    dob         DATE NULL,
    gender      ENUM('MALE','FEMALE','OTHER') NULL,
    points      INT NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  CHAR(36) NULL,
    updated_by  CHAR(36) NULL
);
```

#### orders
```sql
CREATE TABLE orders (
    id              CHAR(36) PRIMARY KEY,
    order_number    VARCHAR(30) NOT NULL UNIQUE,  -- ORD-yyyymmdd-####
    customer_id     CHAR(36) NOT NULL,
    branch_id       CHAR(36) NOT NULL,
    staff_id        CHAR(36) NULL,                  -- NULL for online orders
    subtotal        DECIMAL(15,2) NOT NULL,
    discount        DECIMAL(15,2) NOT NULL DEFAULT 0,
    total           DECIMAL(15,2) NOT NULL,
    status          ENUM('PENDING_PAYMENT','PAID','CANCELLED','COMPLETED') NOT NULL,
    prescription_id CHAR(36) NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      CHAR(36) NULL,
    updated_by      CHAR(36) NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    FOREIGN KEY (staff_id) REFERENCES users(id),
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id),
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_branch (branch_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created (created_at)
);
```

#### order_items
```sql
CREATE TABLE order_items (
    id          CHAR(36) PRIMARY KEY,
    order_id    CHAR(36) NOT NULL,
    medicine_id CHAR(36) NOT NULL,
    batch_id    CHAR(36) NULL,            -- Auto-picked FIFO (NSF-05)
    qty         INT NOT NULL CHECK (qty > 0),
    unit_price  DECIMAL(15,2) NOT NULL,
    discount    DECIMAL(15,2) NOT NULL DEFAULT 0,
    subtotal    DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id)
);
```

#### inventory_batches (NEW in v1.3.0)
```sql
CREATE TABLE inventory_batches (
    id           CHAR(36) PRIMARY KEY,
    medicine_id  CHAR(36) NOT NULL,
    branch_id    CHAR(36) NOT NULL,
    batch_no     VARCHAR(30) NOT NULL,    -- Unique per medicine
    qty_on_hand  INT NOT NULL DEFAULT 0 CHECK (qty_on_hand >= 0),
    expiry_date  DATE NOT NULL,
    received_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_batch (medicine_id, batch_no),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    INDEX idx_inv_branch_med (branch_id, medicine_id),
    INDEX idx_inv_expiry (expiry_date)
);
```

#### inventory_transactions
```sql
CREATE TABLE inventory_transactions (
    id        CHAR(36) PRIMARY KEY,
    batch_id  CHAR(36) NOT NULL,
    type      ENUM('IMPORT','EXPORT','TRANSFER_OUT','TRANSFER_IN','SALE') NOT NULL,
    qty       INT NOT NULL,
    reason    VARCHAR(255) NULL,           -- Required for EXPORT
    ref_id    CHAR(36) NULL,               -- Order/PO reference
    actor_id  CHAR(36) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id),
    FOREIGN KEY (actor_id) REFERENCES users(id)
);
```

#### suppliers
```sql
CREATE TABLE suppliers (
    id              CHAR(36) PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    tax_code        VARCHAR(20) NOT NULL UNIQUE,
    contact_person  VARCHAR(100) NULL,
    phone           VARCHAR(20) NOT NULL,
    email           VARCHAR(100) NULL,
    address         VARCHAR(255) NULL,
    bank_name       VARCHAR(100) NULL,
    bank_account    VARCHAR(30) NULL,
    status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      CHAR(36) NULL,
    updated_by      CHAR(36) NULL
);
```

#### prescriptions
```sql
CREATE TABLE prescriptions (
    id              CHAR(36) PRIMARY KEY,
    code            VARCHAR(20) NOT NULL UNIQUE,    -- RX-yyyy####
    patient_id      CHAR(36) NOT NULL,
    doctor_id       CHAR(36) NOT NULL,             -- FK to users (PHARMACIST)
    diagnosis       TEXT NOT NULL,
    notes           TEXT NULL,
    signature_hash  VARCHAR(255) NOT NULL,
    issued_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES customers(id),
    FOREIGN KEY (doctor_id) REFERENCES users(id)
);
```

#### payments
```sql
CREATE TABLE payments (
    id              CHAR(36) PRIMARY KEY,
    order_id        CHAR(36) NOT NULL,
    method          ENUM('CASH','CARD','QR') NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    amount_tendered DECIMAL(15,2) NULL,
    change_amount   DECIMAL(15,2) NULL,
    txn_ref         VARCHAR(50) NULL,             -- Card/QR gateway ref
    status          ENUM('PENDING','SUCCESS','FAILED') NOT NULL,
    paid_at         DATETIME NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

#### invoices
```sql
CREATE TABLE invoices (
    id              CHAR(36) PRIMARY KEY,
    invoice_number  VARCHAR(30) NOT NULL UNIQUE,  -- INV-yyyymmdd-####
    order_id        CHAR(36) NOT NULL,
    payment_id      CHAR(36) NOT NULL,
    printed_at      DATETIME NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);
```

#### notifications
```sql
CREATE TABLE notifications (
    id            CHAR(36) PRIMARY KEY,
    recipient_id  CHAR(36) NOT NULL,
    channel       ENUM('IN_APP','EMAIL','SMS') NOT NULL,
    template      VARCHAR(50) NULL,                 -- NTPL-* key
    title         VARCHAR(150) NOT NULL,
    body          TEXT NOT NULL,
    status        ENUM('PENDING','SENT','FAILED','READ') NOT NULL,
    sent_at       DATETIME NULL,
    read_at       DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(id)
);
```

### 5.4 Indexes (key)

| Table | Index | Purpose |
| :---- | :---- | :---- |
| users | idx_users_email | Fast email lookup |
| users | idx_users_role | Role-based queries |
| users | idx_users_branch | Branch staff queries |
| medicines | idx_medicines_sku | SKU lookup |
| medicines | idx_medicines_category | Category filter |
| customers | idx_customers_phone | Phone lookup |
| customers | idx_customers_code | Code lookup |
| orders | idx_orders_customer | Customer order history |
| orders | idx_orders_branch | Branch order queries |
| orders | idx_orders_status | Status filter |
| orders | idx_orders_created | Range queries |
| order_items | idx_oi_order | Order join |
| order_items | idx_oi_medicine | Medicine join |
| inventory_batches | uq_batch (medicine_id, batch_no) | Unique batch per medicine |
| inventory_batches | idx_inv_branch_med | Branch stock lookups |
| inventory_batches | idx_inv_expiry | Expiry scans (NSF-03) |
| inventory_transactions | idx_it_batch | Batch movement history |
| suppliers | idx_suppliers_tax_code | Tax code lookup |
| prescriptions | idx_rx_patient | Patient Rx history |
| prescriptions | idx_rx_doctor | Doctor queries |
| notifications | idx_notif_recipient | Inbox queries |

---

## 6. API Design

### 6.1 OpenAPI Specification

**OpenAPI Specification:** [openapi.yaml](./api/openapi.yaml)

### 6.2 API Overview

**Base URL:** `/api/v1`

**Authentication:** Bearer JWT token in Authorization header

**Content-Type:** `application/json`

### 6.3 Authentication Endpoints

> Aligned with SRS v1.3.0 §UC01 + §3.3.1. JWT signed with **RS256**, access token TTL 15 min, refresh TTL 7 days (CR-05). All write endpoints accept `Idempotency-Key` header.

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| POST | /auth/login | Email + password login; returns access+refresh tokens | No | All |
| POST | /auth/logout | Revoke refresh token; blacklist access token (NSF-07) | Yes | All |
| POST | /auth/refresh | Issue new access token using refresh token | Yes (refresh) | All |
| GET | /auth/me | Get current user profile | Yes | All |
| PUT | /auth/password | Change own password | Yes | All |
| POST | /auth/forgot-password | Request password-reset email (returns MSG* for invalid email) | No | All |
| POST | /auth/reset-password | Submit token + new password | No (token) | All |
| POST | /auth/verify-email | Verify email using emailed token | No | All |
| POST | /auth/resend-verification | Resend verification email (rate-limited) | No | All |
| GET | /auth/healthz | Liveness probe | No | — |
| GET | /auth/readyz | Readiness probe | No | — |

**Token payload (JWT, RS256):**

```json
{
  "sub": "uuid",
  "email": "user@example.com",
  "role": "PHARMACIST",
  "branch_id": "uuid|null",
  "iat": 1718000000,
  "exp": 1718000900,
  "jti": "uuid"
}
```

**Account lockout (BR05):** 5 failed attempts within 15 min → 30 min lockout. Auto-unlock by NSF-10 every 5 min.

### 6.4 User Management Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /users | List all users (paginated) | Yes | Admin, CEO (read-only) |
| GET | /users/{id} | Get user by ID | Yes | Admin, CEO |
| POST | /users | Create new user | Yes | Admin, CEO |
| PUT | /users/{id} | Update user (name/phone/role/branch/status) | Yes | Admin, CEO |
| DELETE | /users/{id} | Soft-delete user (status=INACTIVE) | Yes | Admin, CEO |
| PUT | /users/{id}/role | Assign user role (5 values) | Yes | Admin |
| PUT | /users/{id}/branch | Assign user to branch | Yes | Admin |
| POST | /users/{id}/unlock | Manually unlock a locked account | Yes | Admin |

### 6.5 Branch Management Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /branches | List branches (paginated) | Yes | Admin, CEO, Branch Manager (read-only) |
| GET | /branches/{id} | Get branch by ID | Yes | Admin, CEO, Branch Manager |
| POST | /branches | Create new branch | Yes | Admin, CEO |
| PUT | /branches/{id} | Update branch | Yes | Admin, CEO |
| DELETE | /branches/{id} | Soft-delete branch (status=INACTIVE) | Yes | Admin |
| PUT | /branches/{id}/manager | Assign / reassign branch manager (role must = BRANCH_MANAGER) | Yes | Admin, CEO |
| GET | /branches/{id}/staff | List staff assigned to a branch | Yes | Admin, CEO, Branch Manager |

### 6.6 Medicine Management Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /medicines | List medicines (paginated, filterable) | Yes | All authenticated |
| GET | /medicines/{id} | Get medicine by ID | Yes | All authenticated |
| GET | /medicines/sku/{sku} | Get medicine by SKU | Yes | All authenticated |
| POST | /medicines | Create medicine (with optional image upload) | Yes | Admin, CEO, Pharmacist |
| PUT | /medicines/{id} | Update medicine | Yes | Admin, CEO, Pharmacist |
| DELETE | /medicines/{id} | Soft-delete medicine (status=INACTIVE) | Yes | Admin |
| GET | /medicines/categories | List categories | Yes | All authenticated |
| POST | /medicines/categories | Create category | Yes | Admin, CEO |
| PUT | /medicines/categories/{id} | Update category | Yes | Admin, CEO |
| DELETE | /medicines/categories/{id} | Delete category (if no medicine references) | Yes | Admin |

### 6.7 Inventory Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /inventory/batches | List batches (paginated, per branch/medicine) | Yes | Admin, CEO, Branch Manager, Pharmacist (read-only) |
| GET | /inventory/batches/{id} | Get batch detail | Yes | Admin, CEO, Branch Manager, Pharmacist |
| POST | /inventory/import | Import stock (creates InventoryBatch + IMPORT transaction) | Yes | Admin, CEO, Branch Manager |
| POST | /inventory/export | Export stock (EXPORT transaction; reason required) | Yes | Admin, CEO, Branch Manager |
| POST | /inventory/transfer | Inter-branch transfer (TRANSFER_OUT + TRANSFER_IN pair) | Yes | Admin, CEO, Branch Manager |
| GET | /inventory/transactions | List transactions (filterable by type, date, branch) | Yes | Admin, CEO, Branch Manager |
| GET | /inventory/alerts/low-stock | Low-stock list (BR02, NSF-02) | Yes | Admin, CEO, Branch Manager |
| GET | /inventory/alerts/expiry | Expiry list (BR03, NSF-03; items expiring ≤ 30 days) | Yes | Admin, CEO, Branch Manager, Pharmacist |

### 6.8 Order Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /orders | List orders (paginated; customer sees own only) | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| GET | /orders/{id} | Get order detail | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| GET | /orders/number/{orderNumber} | Get by order number (ORD-yyyymmdd-####) | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| POST | /orders | Create order (PENDING_PAYMENT); auto-number (NSF-12); auto-cancel in 24h (NSF-01, BR01) | Yes | Admin, CEO, Pharmacist, Customer |
| PUT | /orders/{id} | Update order lines (only if PENDING_PAYMENT) | Yes | Admin, CEO, Pharmacist |
| POST | /orders/{id}/cancel | Cancel order; restore stock (BR06) | Yes | Admin, CEO, Pharmacist, Customer (own pending) |
| POST | /orders/{id}/approve | Manager approval of a pending order | Yes | Admin, CEO, Branch Manager |
| POST | /orders/{id}/recompute | Re-apply BR04 discount + stock check | Yes | Admin, CEO, Pharmacist |

### 6.9 Payment Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /payments | List payments (paginated) | Yes | Admin, CEO, Branch Manager, Pharmacist |
| GET | /payments/{id} | Get payment by ID | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| POST | /payments | Create payment (cash / card / qr); on success → generate invoice (NSF-11), FIFO consume stock (NSF-05), award points (NSF-04, BR07) | Yes | Admin, CEO, Pharmacist |
| GET | /payments/order/{orderId} | Get payment by order | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| GET | /payments/{id}/invoice | Get printable invoice | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| POST | /payments/{id}/print | Trigger printer (LAN/IPP) | Yes | Admin, CEO, Pharmacist |
| POST | /payments/webhook | Gateway callback (idempotent) | No (HMAC) | System |

### 6.10 Customer Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /customers | List customers (paginated, search) | Yes | Admin, CEO, Branch Manager (read-only), Pharmacist |
| GET | /customers/{id} | Get customer by ID | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| GET | /customers/code/{code} | Get by CUST-yyyy#### code | Yes | Admin, CEO, Branch Manager, Pharmacist |
| POST | /customers | Create customer (auto-generate CUST- code) | Yes | Admin, CEO, Pharmacist, Customer (own) |
| PUT | /customers/{id} | Update profile (preserve code & points) | Yes | Admin, CEO, Pharmacist, Customer (own) |
| GET | /customers/{id}/orders | Get customer order history | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| GET | /customers/{id}/points | Get loyalty points balance | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |
| GET | /customers/{id}/history | Full history panel (orders + points) | Yes | Admin, CEO, Branch Manager, Pharmacist, Customer (own) |

### 6.11 Search Endpoints (UC10)

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /search/medicines | Global search (debounce 300 ms) with filters (name / category / price range / in-stock) | Yes | All authenticated |
| GET | /search/medicines/autocomplete | Top-5 autocomplete suggestions | Yes | All authenticated |
| GET | /search/medicines/{id} | Get medicine detail from search index | Yes | All authenticated |

### 6.12 Report Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /reports/revenue | Revenue report (date range / branch / group-by day-week-month) | Yes | Admin, CEO, Branch Manager (own branch) |
| GET | /reports/inventory | Inventory report (stock levels, movements) | Yes | Admin, CEO, Branch Manager |
| GET | /reports/staff | Staff performance report | Yes | Admin, CEO, Branch Manager |
| POST | /reports/export/excel | Export to .xlsx | Yes | Admin, CEO, Branch Manager |
| POST | /reports/export/pdf | Export to .pdf | Yes | Admin, CEO, Branch Manager |
| POST | /reports/schedule | Schedule recurring report; email on run | Yes | Admin, CEO |
| GET | /reports/schedules | List scheduled reports | Yes | Admin, CEO |
| DELETE | /reports/schedules/{id} | Cancel a scheduled report | Yes | Admin, CEO |

### 6.13 Supplier Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /suppliers | List suppliers (paginated, search) | Yes | Admin, CEO, Branch Manager (read-only), Pharmacist (read-only) |
| GET | /suppliers/{id} | Get supplier by ID | Yes | Admin, CEO, Branch Manager, Pharmacist |
| POST | /suppliers | Create supplier (unique tax_code) | Yes | Admin, CEO |
| PUT | /suppliers/{id} | Update supplier (incl. bank info) | Yes | Admin, CEO |
| DELETE | /suppliers/{id} | Soft-delete supplier (status=INACTIVE) | Yes | Admin |
| GET | /suppliers/{id}/history | Supply history (inventory transactions where supplier_id matches) | Yes | Admin, CEO, Branch Manager |

### 6.14 Prescription Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /prescriptions | List prescriptions (paginated, filter) | Yes | Admin, CEO, Branch Manager (read-only), Pharmacist |
| GET | /prescriptions/{id} | Get prescription by ID | Yes | Admin, CEO, Branch Manager, Pharmacist |
| GET | /prescriptions/code/{code} | Get by RX-yyyy#### code | Yes | Admin, CEO, Pharmacist |
| POST | /prescriptions | Create + sign Rx (signature_hash; status SIGNED) | Yes | Pharmacist |
| POST | /prescriptions/draft | Save draft Rx (no signature yet) | Yes | Pharmacist |
| PUT | /prescriptions/{id} | Update draft (not allowed if SIGNED) | Yes | Pharmacist |
| POST | /prescriptions/{id}/sign | Sign a draft (server-side signature hash) | Yes | Pharmacist |
| POST | /prescriptions/{id}/link-order | Link an Rx to a new order (UC06) | Yes | Pharmacist |
| POST | /prescriptions/{id}/print | Print prescription (LAN/IPP) | Yes | Pharmacist |
| DELETE | /prescriptions/{id} | Cancel Rx (only if not yet linked to paid order) | Yes | Pharmacist |

### 6.15 Notification Endpoints

| Method | Endpoint | Description | Auth Required | Role(s) |
| :---- | :---- | :---- | :---- | :---- |
| GET | /notifications | List user's notifications (paginated; filter by status/channel) | Yes | All authenticated |
| GET | /notifications/{id} | Get notification by ID | Yes | Recipient only |
| PUT | /notifications/{id}/read | Mark as read | Yes | Recipient only |
| PUT | /notifications/read-all | Mark all as read | Yes | All authenticated |
| DELETE | /notifications/{id} | Delete notification | Yes | Recipient only |
| POST | /notifications/compose | Compose & broadcast (multi-audience, multi-channel) | Yes | Admin, CEO |
| GET | /notifications/templates | List available templates (NTPL-LOW-STOCK, NTPL-EXPIRY, etc.) | Yes | Admin, CEO |

### 6.16 Common Headers & Error Envelope

All endpoints honor:

| Header | Required | Notes |
| :---- | :---- | :---- |
| `Authorization: Bearer <jwt>` | Yes (except /auth/login, /auth/forgot-password, /auth/reset-password, /auth/verify-email) | Access token |
| `Idempotency-Key` | Strongly recommended on POST/PUT/DELETE | Prevents duplicate side-effects (CR-05) |
| `X-Correlation-Id` | Optional | Logged end-to-end (CR-06) |
| `Accept-Language` | Optional | `vi-VN` (default) or `en-US` |

**Error envelope (RFC 7807–inspired):**

```json
{
  "code": "MSG33",
  "status": 400,
  "message": "Invalid input data",
  "details": { "field": "phone", "reason": "format" },
  "correlation_id": "uuid",
  "timestamp": "2026-06-13T03:11:00Z"
}
```
| GET | /suppliers/{id}/history | Supply history | Yes |

---

## 7. Security Design

> Aligned with SRS v1.3.0 §4.5 Security. JWT uses **RS256** (asymmetric). All write endpoints require `Idempotency-Key` (CR-05). All services emit structured logs with `correlation_id` (CR-06) and never log passwords/tokens/PII in plain text (§4.8 Compliance).

### 7.1 Authentication Flow

**Auth Flow Diagram:** [auth-sequence.puml](./component-designs/auth-sequence.puml)

**Authentication Method:** JWT Bearer Token (RS256)

**Token Structure:**
- Access Token: 15 minutes expiration (RS256-signed)
- Refresh Token: 7 days expiration, single-use rotation
- JTI claim for blacklist (NSF-07)

**Login Flow (BR05, UC01):**
1. User submits email and password
2. Server validates input format (email regex, password required)
3. Server looks up user record by email
4. Server verifies bcrypt password hash
5. On failure: increment failed-attempt counter; if ≥ 5 within 15 min → lock account 30 min (BR05); show MSG01 / MSG02
6. On success: generate JWT access (15 min) + refresh (7 days); update `last_login_at`, `last_login_ip`
7. Return 200 OK with `{ access_token, refresh_token, user }`
8. Client includes `Authorization: Bearer <access_token>` for subsequent requests

**Logout Flow:**
1. Client calls `/auth/logout` with access token
2. Server adds JTI to blacklist (NSF-07) — distributed via Redis pub/sub
3. Server revokes refresh token in DB
4. Client clears stored tokens

**Forgot / Reset password flow:**
1. `POST /auth/forgot-password` with email → server emails a single-use reset token (15 min TTL)
2. `POST /auth/reset-password` with `{ token, new_password }` → server validates, hashes, updates

**Email verification flow:**
1. On signup, server sends verification email with token
2. `POST /auth/verify-email` with token → server sets `email_verified = true`
3. `POST /auth/resend-verification` (rate-limited) if lost

### 7.2 Authorization (RBAC, SRS v1.3.0)

**Permission Matrix:** [permissions.puml](./diagrams/permissions.puml)

**Role-Based Access Control (RBAC) — 5 roles + 4 external actors:**

| Role | Permissions (high level) |
| :---- | :---- |
| ADMIN | Full system access; user management, configuration, all reports, all branches, all data |
| CEO | Full operational access across all branches; all reports; all business functions except Admin-level configuration |
| BRANCH_MANAGER | Inventory import/export/transfer for managed branches, order approval, branch-scoped reports, customer read, prescription read |
| PHARMACIST | Sales, payments (cash/card/QR), counter stock view, prescription issue/sign, customer CRUD (incl. create), order CRUD, search, own history |
| CUSTOMER | Browse medicines, place orders (own only), cancel own pending orders, view own invoice/history/points, update own profile |

**External actors** (per SRS §1.3):
- **Payment Gateway** — invoked outbound from Payment Service; receives idempotent webhooks back
- **SMS Provider** — invoked outbound from Notification Service (rate-limited per channel)
- **Email Provider** — invoked outbound from Notification Service (SMTP/TLS)
- **Printer** — LAN/IPP endpoint inside each branch, called by Payment/Prescription Service

**Permission resource map (SRS FR2.2 / FR3.2 / FR4.1 / FR5.1 / FR6.1 / FR7.1 / FR8.1 / FR9.1 / FR11.1 / FR12.1 / FR13.4):**

| Resource | ADMIN | CEO | BRANCH_MANAGER | PHARMACIST | CUSTOMER |
| :---- | :---: | :---: | :---: | :---: | :---: |
| User (CRUD) | ● | ● | ○ | ○ | ○ (own) |
| Branch (CRUD) | ● | ● | ◐ | ○ | ○ |
| Medicine (CRUD) | ● | ● | ◐ | ● (create/edit) | ◐ |
| Inventory (import/export/transfer) | ● | ● | ● (own branches) | ○ | ○ |
| Inventory (read) | ● | ● | ● | ◐ | ○ |
| Order (CRUD) | ● | ● | ● (read) | ● | ● (own) |
| Payment (process) | ● | ● | ◐ | ● | — |
| Customer (CRUD) | ● | ● | ◐ | ● | ● (own) |
| Reports (revenue/inventory/staff) | ● | ● | ● (own branch) | ○ | ○ |
| Supplier (CRUD) | ● | ● | ◐ (read) | ◐ (read) | ○ |
| Prescription (issue) | ● | ● | ◐ | ● | — |
| Notification (compose) | ● | ● | ○ | ○ | ○ |
| Search | ● | ● | ● | ● | ● |
| Notification (inbox) | ● | ● | ● | ● | ● (own) |

### 7.3 Password Policy (SRS §4.5)

- Minimum 8 characters
- At least one uppercase letter
- At least one number
- At least one special character (`!@#$%^&*`)
- Stored as bcrypt hash (cost factor 12)
- Never logged in plain text (CR / Compliance)

### 7.4 Account Security (BR05, NSF-10)

- Account locked after **5 failed login attempts within 15 min**
- Lockout duration: **30 min**
- Auto-unlock by NSF-10 every 5 min
- Manual unlock via `POST /users/{id}/unlock` (Admin)
- Password reset requires email verification (token TTL 15 min)
- Session timeout: 30 min of inactivity (idle token expiration)

### 7.5 API Security (SRS §4.5)

- All endpoints require HTTPS (TLS 1.2+)
- Rate limiting: **100 req/min per IP** at API Gateway
- Input validation on all endpoints (Zod schema, mandatory)
- SQL injection prevention via parameterized queries (Prisma / prepared statements)
- XSS prevention via output encoding + CSP headers
- CORS allow-list (configured per environment)
- `Idempotency-Key` required for write endpoints (CR-05)
- Structured JSON logs with `correlation_id`, no passwords/tokens/PII (CR-06, §4.8)

### 7.6 Audit Logging (SRS §4.5 + §4.8)

- All write actions on **User / Order / Payment / Inventory** are audit-logged
- Fields: `actor_id`, `action`, `resource_type`, `resource_id`, `before`, `after`, `ip`, `correlation_id`, `at`
- Audit table is append-only; retained per data-retention policy

---

## 8. Deployment Architecture

**Deployment Diagram:** [deployment.puml](./diagrams/deployment.puml)

### 8.1 Infrastructure

| Environment | Description | Servers |
|-------------|-------------|---------|
| Development | Local development | Local machine |
| Staging | Pre-production testing | 2x App servers, 1x DB |
| Production | Production environment | 4x App servers, 2x DB (replicated) |

### 8.2 Container Architecture

**Technology:** Docker with Docker Compose

**Services:**
- API Gateway (Nginx)
- Auth Service
- User Service
- Branch Service
- Medicine Service
- Inventory Service
- Order Service
- Payment Service
- Customer Service
- Report Service
- Supplier Service
- Prescription Service
- Notification Service
- MySQL Database (per service)
- Redis Cache

### 8.3 Scalability

| Component | Scaling Strategy |
|-----------|------------------|
| API Gateway | Horizontal (load balancer) |
| Microservices | Horizontal (auto-scaling) |
| Database | Vertical + Read replicas |
| Cache | Redis Cluster |
| File Storage | NFS shared storage |

### 8.4 Backup and Recovery

- Daily incremental backup at 2:00 AM
- Weekly full backup on Sunday
- Monthly archive backup
- Recovery Point Objective (RPO): 24 hours
- Recovery Time Objective (RTO): 4 hours

---

## 9. Non-Functional Requirements

> All NFR below are derived from SRS v1.3.0 §4 and are binding for design and acceptance.

### 9.1 Performance (SRS §4.1)

| Metric | Target (P95) | Ideal (P95) | Measurement |
| :---- | :---- | :---- | :---- |
| Page load time | < 3 s | < 1 s | Client-side |
| API response time | < 1 s | < 200 ms | API gateway |
| Search query time | < 2 s | < 500 ms | Search service |
| Order creation (end-to-end) | < 2 s | < 1 s | Order + Inventory |
| Report generation (≤ 30 days) | < 5 s | < 2 s | Report service |
| Report generation (> 30 days) | < 15 s | < 8 s | Async job |
| Concurrent users | ~500 peak / ~100 avg | — | Load test |
| CPU utilization (peak) | < 70 % | < 50 % | 5-min average |
| Memory utilization (peak) | < 80 % | < 60 % | 5-min average |

**Performance design choices:**
- Read-heavy endpoints (medicine list, customer list) use Redis cache (TTL 60 s)
- Report generation > 30 days is dispatched to async queue; client polls or receives webhook
- Inventory stock check uses row-level locking with short transactions (< 500 ms typical)
- Payment service uses prepared statements and connection pooling (HikariCP / Prisma)

### 9.2 Scalability (SRS §4.2)

| Metric | Value |
| :---- | :---- |
| Initial branches | 5–10 |
| Staff count | 50–100 |
| Customers (year 1) | ~50,000 |
| Orders/day (peak) | ~2,000 |
| Concurrent users (peak) | ~500 |
| Architecture | Microservice (12 services + gateway) |
| Horizontal scaling | Stateless app pods, 2–8 replicas per service |
| Database scaling | Read replicas for reporting (NSF-06) |
| Cache | Redis cluster for sessions, idempotency, blacklist |

### 9.3 Availability & Reliability (SRS §4.3)

| Metric | Target | Notes |
| :---- | :---- | :---- |
| SLA | **99.9 %** | 8.76 h downtime/year, 43.8 min/month |
| MTBF | ≥ 720 h | Mean time between failures |
| MTTR | ≤ 30 min | P1 incident |
| RPO | 24 h | Daily incremental backup (NSF-08) |
| RTO | 4 h | Full restore from backup |
| Data accuracy | ≥ 99.99 % | Reconciliation between order, payment, inventory |
| Disaster recovery | Cross-host backup on separate physical machine; quarterly DR drill |

**Reliability patterns:**
- Circuit breaker on inter-service calls (e.g., Order → Inventory)
- Idempotent webhooks from Payment Gateway (§4.6)
- Database transactions with optimistic locking for stock updates
- Health checks: `/healthz` (liveness) + `/readyz` (readiness) on every service (CR-07)
- Graceful shutdown on SIGTERM (drain in-flight, close DB pool)

### 9.4 Usability (SRS §4.4)

| Requirement | Target |
| :---- | :---- |
| Time-to-first-action for new cashier | ≤ 30 min training |
| Steps to create an order | ≤ 6 clicks |
| Error message clarity | Plain language (MSG*), no stack traces; field-level guidance |
| Language | Vietnamese (default `vi-VN`), English (i18n-ready, `en-US`) |
| Accessibility | WCAG 2.1 Level A minimum |
| Browser support | Chrome 100+, Edge 100+, Safari 15+, Firefox 100+ |
| Mobile breakpoints | 360 px, 768 px, 1024 px, 1280 px |
| Printout | A4 / 80 mm thermal receipt |

### 9.5 External Interfaces (SRS §4.6)

| Interface | Type | Protocol | Notes |
| :---- | :---- | :---- | :---- |
| Web Browser ↔ API Gateway | User | HTTPS / REST + JSON | Primary client channel |
| Mobile Web ↔ API Gateway | User | HTTPS / REST + JSON | Responsive web (70% web / 30% mobile) |
| Payment Gateway (Card/QR) | System | HTTPS / REST | Outbound to bank/gateway; idempotent webhook (`POST /payments/webhook`) |
| SMS Provider | System | HTTPS / REST | Outbound OTP/alerts; retry 3× (NSF-09) |
| Email Provider (SMTP) | System | SMTP / TLS | Outbound notifications; retry 3× (NSF-09) |
| Internal Service-to-Service | System | gRPC or REST over mTLS | East-west traffic (12 services) |
| Database | System | TCP (driver-specific) | Per-service DB (PostgreSQL) |
| Search Index | System | HTTP (Elasticsearch) | Read-only projection from medicine_service |
| Print (Invoice/Rx) | User | LAN / IPP | Local receipt / A4 printer, per branch |

### 9.6 Infrastructure & Deployment (SRS §4.7)

| Requirement | Value |
| :---- | :---- |
| Deployment | On-premise |
| Architecture | Microservice |
| Container | Docker |
| Orchestration | Docker Compose (v1.3); Kubernetes (roadmap) |
| Platforms | Web Browser (70 %), Mobile Web (30 %) |
| Backup | Daily incremental 02:00 (NSF-08), weekly full (Sun), monthly archive |
| Monitoring | Prometheus + Grafana; centralized logging via ELK |
| CI/CD | GitHub Actions (build, test, scan, push image) |

### 9.7 Compliance (SRS §4.8)

- Data classification and privacy policies per Vietnam **PDPD** (where applicable)
- **Audit trail** for all critical operations (auth, order, payment, inventory writes) — §7.6
- Logging must **not** include passwords, tokens, or PII in plain text
- All sensitive config (DB passwords, JWT private key, gateway secrets) stored in a **secrets manager** (HashiCorp Vault or sealed-secrets)
- Code-level: dependency scanning, SAST, secret scanning in CI

---

## 10. Application Messages & Common Requirements

> Mirrors SRS v1.3.0 §5.2 (Common Requirements) and §5.3 (Application Messages List). Message codes are stable contracts between frontend and backend; content is i18n-ready (`vi-VN` default, `en-US` secondary).

### 10.1 Common Requirements (CR-01 … CR-10)

| ID | Requirement | Implementation |
| :---- | :---- | :---- |
| CR-01 | All UI text must be externalized in i18n resource bundles | react-i18next, server returns MSG codes; UI maps to bundle |
| CR-02 | All timestamps stored in UTC; rendered in `Asia/Ho_Chi_Minh` | DB columns `DATETIME` (UTC); UI uses `date-fns-tz` |
| CR-03 | Amounts stored as `DECIMAL(15,2)` in VND; never float | DB + DTO + JSON serialization enforce string-or-decimal |
| CR-04 | All list endpoints paginate (default `page_size=20`, max 100) | Query params `?page&page_size` |
| CR-05 | All write endpoints idempotent for retries (`Idempotency-Key` header) | Redis-backed dedup window 24 h |
| CR-06 | Services emit structured JSON logs with `correlation_id` | winston / pino + AsyncLocalStorage |
| CR-07 | Every service exposes `/healthz` and `/readyz` | Probes wired to k8s / compose |
| CR-08 | Soft-delete is default for User, Customer, Supplier, Medicine | `status` enum + filter `WHERE status='ACTIVE'` |
| CR-09 | Audit log fields (`created_at`, `created_by`, `updated_at`, `updated_by`) mandatory on all business tables | DB triggers + ORM interceptors |
| CR-10 | API versioning: URL prefix `/api/v1`; breaking changes require `/api/v2` | API Gateway route rules |

### 10.2 Application Messages List (MSG01 … MSG34)

| Code | Type | Context | Content (vi) | Content (en) |
| :---- | :---- | :---- | :---- | :---- |
| MSG01 | error | Login | Email hoặc mật khẩu không đúng | Invalid email or password |
| MSG02 | warning | Login | Tài khoản đã bị khóa. Vui lòng thử lại sau 30 phút | Account locked. Try again in 30 minutes |
| MSG03 | info | Login | Email chưa được xác thực | Email not verified |
| MSG04 | error | Login | Dịch vụ tạm thời gián đoạn. Vui lòng thử lại | Service temporarily unavailable |
| MSG05 | error | Login | Lỗi hệ thống. Vui lòng liên hệ quản trị viên | System error. Contact administrator |
| MSG06 | success | User | Tạo người dùng thành công | User created successfully |
| MSG07 | confirm | User | Bạn có chắc muốn xóa người dùng này? | Confirm delete this user? |
| MSG08 | success | Branch | Tạo chi nhánh thành công | Branch created successfully |
| MSG09 | error | Branch | Mã chi nhánh đã tồn tại | Branch code already exists |
| MSG10 | error | Branch | Số điện thoại không hợp lệ | Invalid phone number |
| MSG11 | error | Branch | Quản lý phải có vai trò Quản lý chi nhánh | Manager must have BRANCH_MANAGER role |
| MSG12 | success | Medicine | Tạo thuốc thành công | Medicine created successfully |
| MSG13 | error | Medicine | Tải ảnh thất bại | Image upload failed |
| MSG14 | success | Inventory | Nhập kho thành công | Stock imported successfully |
| MSG15 | error | Inventory | Số lượng phải lớn hơn 0 | Quantity must be greater than 0 |
| MSG16 | error | Inventory | Số lô đã tồn tại cho thuốc này | Batch number already exists |
| MSG17 | error | Inventory | Ngày hết hạn phải lớn hơn hôm nay | Expiry date must be in the future |
| MSG18 | success | Order | Đặt hàng thành công | Order placed successfully |
| MSG19 | info | Order | Đơn hàng đã bị hủy do quá thời gian thanh toán | Order auto-cancelled (24h timeout) |
| MSG20 | error | Order | Không đủ tồn kho | Insufficient stock |
| MSG21 | error | Payment | Số tiền khách đưa không đủ | Insufficient tendered amount |
| MSG22 | error | Payment | Cổng thanh toán không phản hồi | Payment gateway timeout |
| MSG23 | success | Customer | Tạo khách hàng thành công | Customer created successfully |
| MSG24 | error | Customer | Số điện thoại đã tồn tại | Phone already exists |
| MSG25 | info | Report | Không có dữ liệu trong khoảng thời gian này | No data for the selected range |
| MSG26 | error | Report | Xuất báo cáo thất bại | Export failed |
| MSG27 | info | Search | Không tìm thấy kết quả | No results found |
| MSG28 | success | Supplier | Tạo nhà cung cấp thành công | Supplier created successfully |
| MSG29 | warning | Inventory | Cảnh báo: tồn kho dưới mức tối thiểu | Low stock alert |
| MSG30 | warning | Inventory | Cảnh báo: thuốc sắp hết hạn (trong 30 ngày) | Expiry alert (within 30 days) |
| MSG31 | error | Common | Không có quyền truy cập | Permission denied |
| MSG32 | error | Common | Phiên đăng nhập đã hết hạn | Session expired |
| MSG33 | error | Common | Dữ liệu không hợp lệ | Invalid input data |
| MSG34 | error | Common | Lỗi máy chủ nội bộ | Internal server error |

### 10.3 Business Rules (BR-01 … BR-07)

| ID | Name | Trigger | Action | Owner UC / NSF |
| :---- | :---- | :---- | :---- | :---- |
| BR01 | Auto_cancel_order | Order unpaid > 24 h | Cancel + emit MSG19 (UC13) | NSF-01, UC06 |
| BR02 | Alert_low_stock | Stock < min_stock_level | Alert warehouse manager (MSG29) | NSF-02, UC05/UC13 |
| BR03 | Batch_expiry_check | Daily 00:00 | Alert items expiring ≤ 30 d (MSG30) | NSF-03, UC05/UC13 |
| BR04 | Price_discount | Qty ≥ 10 of same medicine | Apply 5 % discount on that line | UC06 |
| BR05 | Account_lockout | 5 failed logins / 15 min | Lock 30 min, MSG02 | NSF-10, UC01 |
| BR06 | Stock_restore_on_cancel | Order → CANCELLED | Reverse SALE transactions for that order | UC06 |
| BR07 | Loyalty_points_award | Order → PAID | **1 point / 1,000 VND** of order total | NSF-04, UC07 |

---

## Appendix A: Component Diagram Files

All component diagrams are located in the following directories:

- **System Diagrams:** `./diagrams/`
- **UC Diagrams:** `./diagrams/uc-01/` through `./diagrams/uc-13/`
- **Component Designs:** `./component-designs/`
- **Database Tables:** `./db/tables/`

## Appendix B: Database Schema Files

- **Full Schema:** `./db/schema.sql`
- **Per-Component Tables:** `./db/tables/*.sql`

## Appendix C: API Specification

- **OpenAPI YAML:** `./api/openapi.yaml`

## Appendix D: Document History

| Version | Date | Author | Changes |
| :---- | :---- | :---- | :---- |
| 1.0.0 | 2026-05-24 | SDD Agent | Initial version (aligned to SRS v1.0.0) |
| 1.1.0 | 2026-06-12 | SDD Agent | Round 1: aligned to SRS v1.1.0 — Use Case Summary, Screen Mockup, Screen Definition table, Entity Details, NFR expansion, Application Messages List, Common Requirements |
| 1.2.0 | 2026-06-12 | SDD Agent | Round 2: structural alignment to template v2.3 — Functional Overview, single-table UC, table-based Alt Flows, ERD/Entity Details under Software Features |
| 1.3.0 | 2026-06-13 | SDD Agent | Round 3: aligned to SRS v1.3.0 — restructured actors (6→5 internal + 4 external), added CEO role, updated Screen Authorization matrix (●/◐/○/− over 28 screens), updated Non-Screen Functions (NSF-01..NSF-12), updated Database Schema to UUID + InventoryBatch, added /auth/forgot-password, /auth/reset-password, /auth/verify-email, added Search endpoints (UC10), added §9 NFR (Performance/Scalability/Availability/Usability/External Interfaces/Compliance), added §10 Application Messages & Common Requirements, added Business Rules table |

## Appendix E: Glossary

| Term | Definition |
| :---- | :---- |
| RS256 | RSA-SHA256 asymmetric signing algorithm for JWT |
| PDPD | Vietnam Personal Data Protection Decree (Nghị định bảo vệ dữ liệu cá nhân) |
| NSF | Non-Screen Function (background job / scheduled task) |
| FIFO | First In, First Out (batch consumption strategy) |
| MTBF / MTTR | Mean Time Between Failures / Mean Time To Recover |
| RPO / RTO | Recovery Point Objective / Recovery Time Objective |
| P95 | 95th percentile latency |
| LAN/IPP | Local Area Network / Internet Printing Protocol |
| CR | Common Requirement (SRS §5.2) |
| BR | Business Rule (SRS §5.1) |

---

**Document Status:** Final
**Document Version:** 1.3.0
**Date:** 2026-06-13
**Author:** SDD Agent
**Related SRS:** v1.3.0

**END OF DOCUMENT**