# Software Requirement Specification
## Pharmacy Chain Management System

| Field | Value |
|-------|-------|
| **Document ID** | SRS_PharmacyChainManagementSystem_v1.4.0 |
| **Version** | 1.4.0 |
| **Date** | 2026-06-17 |
| **Author** | SRS Agent |
| **Status** | Final |
| **Aligned Template** | Internal SRS Template v2.3 (Round 4) |

---

## I. Record of Changes

| Version | Date | A*M, D | In charge | Change Description |
| ----- | ----- | ----- | ----- | ----- |
| 1.0.0 | 2026-05-24 | A | SRS Agent | Initial version |
| 1.1.0 | 2026-06-12 | M | SRS Agent | Round 1: added Use Case Summary; converted Main Flow to Step/Actor/Action table; added Screen Mockup + Screen Definition; added Entity Details; expanded NFR; added Application Messages List + Common Requirements |
| 1.2.0 | 2026-06-12 | M | SRS Agent | Round 2: restructured per template — moved Use Case Details from §2.1.3 to §3.2; added §3.1 Functional Overview (Screens Flow, Screen Descriptions, Screen Authorization, Non-Screen Functions); moved ERD to §3.1.5 and Entity Details to §3.1.6; consolidated each UC into single Use Case Description table; converted Alternative Flows to table with reference ID; fixed Record of Changes header (A/M/D* → A*M, D) |
| 1.3.0 | 2026-06-13 | M | SRS Agent | Round 3: restructured actors — consolidated 6 actors to 5 internal (Admin, CEO, Branch Manager, Pharmacist, Customer) + 4 external (Payment Gateway, SMS Provider, Email Provider, Printer); updated Screen Authorization matrix; updated all UC actors; updated Entity User role enum; updated Functional Requirements; updated permission-based access control model |
| 1.4.0 | 2026-06-17 | M | SRS Agent | Round 4: added **Customer Portal (B2C)** — 52 màn hình khách hàng (theo chuẩn Long Châu: E-commerce, Tra cứu, Hệ thống nhà thuốc, Tiêm chủng, Tài khoản, Tĩnh); added **AI Features (UC15)** — tư vấn ảo, OCR đơn thuốc, cảnh báo tương tác thuốc, gợi ý thông minh, dự báo tồn kho, semantic search, chatbot CSKH; added **Pharmacist — Người bạn tại quầy (UC16)** — tư vấn chuyên sâu, hồ sơ khách quen, cảnh báo dị ứng, cross-sell, follow-up, lịch tái khám; added **Mobile App (UC17)** — push notification, GPS, calendar sync, AI tư vấn 24/7; added **Health Tools (UC18)** — bài kiểm tra sức khoẻ, bệnh theo mùa, cảnh báo dịch; added **E-commerce (UC19)** — voucher, trả góp, đánh giá, ví điện tử; updated Screen Descriptions to 80+ screens; updated Screen Authorization matrix; added 6 UCs mới (UC14-UC19); added Functional Requirements mới (§3.3.14-§3.3.19) |

*A = Added, M = Modified, D = Deleted

---

## II. Table of Contents

1. [Product Overview](#1-product-overview)
2. [User Requirements](#2-user-requirements)
3. [Software Features](#3-software-features)
4. [Non-Functional Requirements](#4-non-functional-requirements)
5. [Requirement Appendix](#5-requirement-appendix)

---

## 1. Product Overview

### 1.1 Purpose

This document specifies the complete requirements for the **Pharmacy Chain Management System** version 1.3.0. The document describes the system architecture, functional and non-functional requirements, user interfaces, data model, and external interfaces.

This SRS is intended to:
- Provide a complete description of the system from the user's perspective
- Serve as a basis for software design and development
- Provide criteria for acceptance testing
- Establish a common understanding among all stakeholders

### 1.2 Product Scope

**Product Name:** Pharmacy Chain Management System (PCMS)

**Core Features:**
- Multi-branch pharmacy chain management
- Inventory management with real-time stock tracking
- Point-of-sale (POS) operations
- Customer management with loyalty points
- Prescription handling
- Comprehensive reporting and analytics
- Low stock and expiry alerts

- Microservice-based architecture

**Target Business:**
- Pharmacy chains with 5-10 branches initially
- 50-100 staff members
- On-premise infrastructure deployment
- **v1.4.0:** Mở rộng — bổ sung **Customer Portal (B2C)** với 52 màn hình khách hàng + **AI Engine** (12 tính năng) + **Mobile App** (iOS/Android) + **Pharmacist Tools** nâng cao. Hệ thống phục vụ cả **B2B** (dược sĩ tại quầy, admin, quản lý) lẫn **B2C** (khách hàng cuối).

### 1.3 Target Users

#### Internal Actors

| # | User Role | Description |
| :---- | :---- | :---- |
| 1 | **Admin** | System administrators with highest access to all functions |
| 2 | **CEO** | Executive with full operational access across all branches |
| 3 | **Branch Manager** | Manages one or more pharmacy branches; handles inventory import/export for their branches |
| 4 | **Pharmacist** | Works at pharmacy counter; handles sales, payments, inventory at counter, and prescription issuance |
| 5 | **Customer** | End customers purchasing medicines online or in-store |

#### External Actors

| # | Actor | Type | Description |
| :---- | :---- | :---- | :---- |
| 1 | **Payment Gateway** | System | Processes card and QR code payments |
| 2 | **SMS Provider** | System | Sends OTP and notification SMS |
| 3 | **Email Provider** | System | Sends verification emails and notifications |
| 4 | **Printer** | Hardware | Prints invoices and prescriptions |

### 1.4 Context Diagram

*Xem chi tiết:* [Context Diagram](./diagrams/context-diagram.puml)

---

## 2. User Requirements

> Per template v2.3, this section only identifies the actors and enumerates use cases. Detailed use case flows, screen designs, and step-by-step actions are documented in [§3.2 Use Case Details](#32-use-case-details).

### 2.1 Actors

#### 2.1.1 Internal Actors

| # | Actor | Description | Permissions |
| :---- | :---- | :---- | :---- |
| 1 | **Admin** | System administrator | Highest system access, user management, configuration |
| 2 | **CEO** | Executive director | Full operational access across all branches; all permissions below Admin |
| 3 | **Branch Manager** | Branch-level manager | Inventory import/export for managed branches, order approval, branch reports |
| 4 | **Pharmacist** | Counter staff at pharmacy branch | Sales, payments, counter inventory, prescription issuance, customer management |
| 5 | **Customer** | End customer | Browse medicines, place orders, view history |

#### 2.1.2 External Actors

| # | Actor | Type | Description |
| :---- | :---- | :---- | :---- |
| 1 | **Payment Gateway** | System | Processes card and QR code payments |
| 2 | **SMS Provider** | System | Sends OTP and notification SMS |
| 3 | **Email Provider** | System | Sends verification emails and notifications |
| 4 | **Printer** | Hardware | Prints invoices and prescriptions |

### 2.2 Use Cases Summary

| ID | Group | Use Case | Actors | Brief Description |
| :---- | :---- | :---- | :---- | :---- |
| UC01 | Authentication | Login | User, System | User authenticates via email/password; system validates and issues JWT. |
| UC02 | User Management | Manage Users | Admin, CEO | Admin creates, reads, updates, deletes user accounts. |
| UC03 | Branch Management | Manage Branches | Admin, CEO | Admin creates branches and assigns managers. |
| UC04 | Medicine Management | Manage Medicines | Admin, CEO, Pharmacist | CRUD medicine catalog with categories and pricing. |
| UC05 | Inventory | Manage Inventory | Branch Manager, Pharmacist | Import, export, transfer stock between branches. |
| UC06 | Order | Manage Orders | Pharmacist, Customer | Create/view/cancel orders (Customer: own orders only); auto-discount logic (BR04). |
| UC07 | Payment | Process Payment | Pharmacist, Payment Gateway | Process cash/card/QR payments and print invoice. |
| UC08 | Customer | Customer Profile & Loyalty | Customer, Pharmacist | Register customers, update profile, view purchase history and loyalty points. |
| UC09 | Reporting | View Reports | Admin, CEO, Branch Manager | Generate and export revenue, inventory, staff reports. |
| UC10 | Search | Search Medicines | All Actors | Search & filter medicines by name, category, price, stock. |
| UC11 | Supplier | Manage Suppliers | Admin, CEO | CRUD supplier records and supply history. |
| UC12 | Prescription | Issue Prescription | Pharmacist | Create prescriptions and link to orders. |
| UC13 | Notification | Notifications | System, Admin, Email Provider, SMS Provider | Send and manage system alerts and notifications. |
| UC14 | **Customer Portal (B2C)** | **Customer-Facing E-commerce & Tra cứu** | Customer, Guest, System, Payment Gateway, AI Engine | 52 màn hình khách hàng: trang chủ, danh mục, PDP, giỏ hàng, checkout, tra cứu thuốc/dược chất/dược liệu, tìm nhà thuốc, đặt lịch tiêm chủng, bài kiểm tra sức khoẻ, tài khoản gia đình, ví sức khoẻ, lịch sử đơn thuốc. Public-facing (guest xem được) + authenticated (đặt hàng, quản lý cá nhân). |
| UC15 | **AI Features** | **AI-Powered Assistance** | Customer, Pharmacist, AI Engine, System | 12 tính năng AI: (1) AI tư vấn dược sĩ ảo 24/7, (2) AI OCR đơn thuốc viết tay, (3) AI cảnh báo tương tác thuốc (drug-drug, drug-food, drug-allergy), (4) AI gợi ý sản phẩm cross-sell/up-sell, (5) AI semantic search (tìm theo triệu chứng thay vì tên), (6) AI dự báo tồn kho theo mùa, (7) AI phát hiện bất thường đơn thuốc, (8) AI tóm tắt bệnh án, (9) AI phân tích xu hướng bán hàng, (10) AI chatbot CSKH, (11) AI gợi ý tái khám, (12) AI kiểm tra liều lượng theo tuổi/cân nặng. |
| UC16 | **Pharmacist — Người bạn tại quầy** | **Pharmacist Trusted Advisor Tools** | Pharmacist, Customer, AI Engine | 10 tính năng nâng cao dược sĩ: (1) Tư vấn chuyên sâu 1-1 với khách, (2) Hồ sơ khách quen + lịch sử toàn diện, (3) Cảnh báo dị ứng chéo cho từng khách, (4) Gợi ý cross-sell/up-sell thông minh, (5) Đề xuất thuốc generic thay thế, (6) Follow-up sau bán (nhắc uống thuốc, đánh giá hiệu quả), (7) Đánh dấu khách VIP/khách quen, (8) Gợi ý chế độ ăn/uống kèm theo, (9) Lịch tái khám định kỳ, (10) Chấm điểm "mức độ trung thành" khách hàng. |
| UC17 | **Mobile App** | **Long Châu-style Mobile Experience** | Customer, Pharmacist, System, Push Provider | Mobile app (iOS + Android): push notification nhắc uống thuốc, GPS tìm nhà thuốc gần nhất, AI tư vấn 24/7, calendar sync lịch uống thuốc, quét mã vạch tra thuốc chính hãng, Tài khoản gia đình (nhiều thành viên), Ví Khỏe Nhà Ta, theo dõi đơn realtime, đăng nhập VNeID. |
| UC18 | **Health Tools** | **Self-service Health Utilities** | Customer, Guest, AI Engine | Công cụ sức khoẻ: (1) Bài kiểm tra trí nhớ + tập trung, (2) Sàng lọc tiền đái tháo đường, (3) Kiểm tra suy giáp, (4) Đánh giá kiểm soát hen, (5) Nguy cơ tim mạch, (6) Nguy cơ Alzheimer, (7) Trào ngược dạ dày, (8) Phụ thuộc bình xịt, (9) Bệnh theo mùa, (10) Cảnh báo dịch. |
| UC19 | **E-commerce Operations** | **Promotions, Reviews & Wallet** | Customer, Guest, Admin, CEO, Payment Gateway | Voucher/coupon, trả góp (Home Credit, FE Credit), đánh giá sản phẩm (1-5 sao + hình ảnh), ví điện tử Long Châu, chat realtime với dược sĩ, flash sale, combo deals. |

---

## 3. Software Features

### 3.1 Functional Overview

#### 3.1.1 Screens Flow

The system is organized into **13 functional areas** (7 internal B2B + 6 customer-facing B2C), each with its own screen flow. Detail per-screen flow diagrams are linked from each Use Case in [§3.2](#32-use-case-details); the diagram below shows the global navigation between top-level modules.

```
                          +-----------------------+
                          |   Login (SCR-LOGIN)   |
                          +----------+------------+
                                     |
                                     v
                          +-----------------------+
                          |  Dashboard (SCR-HOME) |     (B2B - staff/admin)
                          +----------+------------+
                                     |
        +----------+----------+-----+-----+----------+----------+
        |          |          |     |     |          |          |
        v          v          v     v     v          v          v
   +---------+ +-------+ +-------+ ... +--------+ +--------+ +--------+
   |Users    | |Branch | |Med/Inv|     |Orders  | |Reports | |Notif.  |
   |(UC02)   | |(UC03) | |(UC04-5|     |(UC06-8)| |(UC09)  | |(UC13)  |
   +---------+ +-------+ +-------+     +--------+ +--------+ +--------+
        (B2B Authenticated App — 7 functional groups, 28 screens)

  +----------------------------------------------------------------+
  |         CUSTOMER PORTAL (B2C) — public + authenticated         | (NEW v1.4.0)
  |                                                                |
  |   Trang chủ (SHOP-HOME) → Danh mục / PDP / Giỏ / Checkout     |
  |                       → Tra cứu (thuốc/dược chất/dược liệu)   |
  |                       → Hệ thống nhà thuốc (STORE-LOCATOR)    |
  |                       → Tài khoản (đăng nhập, ví, gia đình)   |
  |                       → AI Tư vấn (CHAT-AI)                    |
  |                       → Mobile App (iOS/Android)                |
  +----------------------------------------------------------------+
```

Per-UC screen flows (SCR-XXX in [§3.1.2](#312-screen-descriptions)):
- UC01: [screenflow](./diagrams/uc-01/uc-01-screenflow.puml)
- UC02: [screenflow](./diagrams/uc-02/uc-02-screenflow.puml)
- UC03: [screenflow](./diagrams/uc-03/uc-03-screenflow.puml)
- UC04: [screenflow](./diagrams/uc-04/uc-04-screenflow.puml)
- UC05: [screenflow](./diagrams/uc-05/uc-05-screenflow.puml)
- UC06: [screenflow](./diagrams/uc-06/uc-06-screenflow.puml)
- UC07: [screenflow](./diagrams/uc-07/uc-07-screenflow.puml)
- UC08: [screenflow](./diagrams/uc-08/uc-08-screenflow.puml)
- UC09: [screenflow](./diagrams/uc-09/uc-09-screenflow.puml)
- UC10: [screenflow](./diagrams/uc-10/uc-10-screenflow.puml)
- UC11: [screenflow](./diagrams/uc-11/uc-11-screenflow.puml)
- UC12: [screenflow](./diagrams/uc-12/uc-12-screenflow.puml)
- UC13: [screenflow](./diagrams/uc-13/uc-13-screenflow.puml)
- **UC14: [screenflow](./diagrams/uc-14-customer-portal/uc-14-screenflow.puml)** *(NEW)*
- **UC15: [screenflow](./diagrams/uc-15-ai/uc-15-screenflow.puml)** *(NEW)*
- **UC16: [screenflow](./diagrams/uc-16-pharmacist/uc-16-screenflow.puml)** *(NEW)*
- **UC17: [screenflow](./diagrams/uc-17-mobile/uc-17-screenflow.puml)** *(NEW)*
- **UC18: [screenflow](./diagrams/uc-18-health/uc-18-screenflow.puml)** *(NEW)*
- **UC19: [screenflow](./diagrams/uc-19-ecom/uc-19-screenflow.puml)** *(NEW)*

#### 3.1.2 Screen Descriptions

> **v1.4.0 mở rộng:** Tổng cộng **80+ màn hình** trong **13 nhóm chức năng** (7 B2B cũ + 6 B2C mới). Màn hình B2C phân biệt bằng prefix `SHOP-` / `STORE-` / `CUST-` / `CHAT-` / `WALLET-` / `HEALTH-` / `MOBILE-` để tránh trùng với prefix `SCR-` của B2B.

##### Phần A — B2B Authenticated App (28 màn hình, giữ nguyên từ v1.3.0)

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

##### Phần B — B2C Customer Portal (52 màn hình, NEW v1.4.0)

###### Nhóm 1 — Mua sắm (E-commerce, 11 màn hình)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| SHOP-HOME | Trang chủ | UC14 | Hero banner, sản phẩm bán chạy, danh mục nổi bật, thương hiệu, bài kiểm tra sức khỏe, video ngắn, góc sức khỏe, bệnh theo mùa |
| SHOP-CAT-1 | Danh mục cấp 1 | UC14 | Sidebar filter nâng cao, danh sách sản phẩm, sort, sub-category pills |
| SHOP-CAT-2 | Danh mục cấp 2 | UC14 | Breadcrumb, filter theo thuộc tính |
| SHOP-PDP | Chi tiết sản phẩm | UC14 | Gallery ảnh, giá/giảm giá, mô tả, công dụng, thành phần, hướng dẫn, đánh giá, sản phẩm liên quan |
| SHOP-SEARCH | Tìm kiếm (B2C) | UC14, UC15 | Kết quả full-text, filter, sort, gợi ý AI semantic search |
| SHOP-CART | Giỏ hàng | UC14 | Sửa SL, xoá, áp coupon, tính ship, mini-cart drawer |
| SHOP-CHECKOUT | Checkout | UC14 | 4 bước: địa chỉ → giao hàng → thanh toán → xác nhận |
| SHOP-ORDER-HISTORY | Lịch sử đơn hàng | UC14 | Trạng thái đơn, theo dõi ship, đánh giá, mua lại |
| SHOP-ORDER-TRACK | Theo dõi đơn | UC14 | Timeline trạng thái (đã xác nhận → đang giao → hoàn tất) |
| SHOP-RX-UPLOAD | Đặt thuốc theo toa | UC14, UC15 | Upload ảnh toa thuốc → AI OCR (UC15-AI-02) → dược sĩ xác nhận → giao tận nơi |
| SHOP-INSTALLMENT | Trả góp | UC14, UC19 | Mua trước trả sau qua Home Credit, FE Credit |

###### Nhóm 2 — Tra cứu (Information, 8 màn hình)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| SHOP-LOOKUP-DRUG | Tra cứu thuốc | UC14 | Tra theo tên thuốc, có thông tin hàm lượng, nhóm thuốc, chỉ định, A-Z filter |
| SHOP-LOOKUP-INGREDIENT | Tra cứu dược chất | UC14 | Tra theo hoạt chất (API) — Paracetamol, Amoxicillin |
| SHOP-LOOKUP-HERB | Tra cứu dược liệu | UC14 | Tra cây thuốc, vị thuốc cổ truyền |
| SHOP-VERIFY-ORIGIN | Tra thuốc chính hãng | UC14 | Quét QR / nhập mã vạch để verify nguồn gốc |
| SHOP-HEALTH-ARTICLE | Bài viết sức khoẻ | UC14 | Long-form: lọc theo Dinh dưỡng / Phòng chữa bệnh / Người cao tuổi / Khỏe đẹp / Mẹ và bé / Giới tính |
| SHOP-DISEASE-INFO | Bệnh thường gặp | UC14 | Bệnh theo đối tượng (Nam/Nữ/Người già/Trẻ em) + theo mùa |
| SHOP-CANCER-INFO | Chuyên trang ung thư | UC14 | Long-form về ung thư, hợp tác IHH Singapore |
| SHOP-VIDEO | Video ngắn | UC14 | Short video y khoa từ Bộ Y tế, WHO |

###### Nhóm 3 — Hệ thống nhà thuốc (4 màn hình)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| STORE-LOCATOR | Tìm nhà thuốc | UC14 | Search theo tỉnh/huyện, hiển thị danh sách + bản đồ, giờ mở cửa 6:00–23:00 |
| STORE-LIST-PROVINCE | DS nhà thuốc theo tỉnh | UC14 | Lọc theo tỉnh (Hà Nội, HCM, Cần Thơ...) |
| STORE-DETAIL | Chi tiết 1 nhà thuốc | UC14 | Địa chỉ, SĐT, giờ mở cửa, dịch vụ, bản đồ chỉ đường |
| STORE-CONSULT | Đặt lịch tư vấn dược sĩ | UC14, UC16 | Modal: 4 tiêu chí Long Châu (đúng thuốc, đúng liều, đúng cách, đúng giá) |

###### Nhóm 4 — Tiêm chủng (Long Châu 247, 3 màn hình)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| VACCINE-HOME | Trang tiêm chủng | UC14 | Danh sách vắc xin, gói tiêm, đặt lịch |
| VACCINE-BOOKING | Đặt lịch tiêm | UC14 | Chọn vắc xin, chọn chi nhánh, chọn ngày giờ |
| VACCINE-LEDGER | Sổ tiêm chủng điện tử | UC14, UC17 | Lưu lịch sử tiêm cho bản thân + người thân (Tài khoản gia đình) |

###### Nhóm 5 — Tài khoản khách hàng (9 màn hình)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| CUST-LOGIN | Đăng nhập / Đăng ký | UC14 | SMS OTP, VNeID (từ 01/01/2025) |
| CUST-PROFILE | Hồ sơ cá nhân | UC14 | Avatar, SĐT, email, địa chỉ mặc định |
| CUST-ADDRESS | Sổ địa chỉ | UC14 | Nhiều địa chỉ giao hàng |
| CUST-FAMILY | Tài khoản gia đình | UC14, UC17 | Liên kết nhiều thành viên (cha mẹ, con cái, vợ chồng) |
| CUST-HEALTH-WALLET | Ví Khỏe Nhà Ta | UC14, UC19 | Ví sức khoẻ tích thưởng cho cả nhà |
| CUST-RX-HISTORY | Lịch sử đơn thuốc | UC14 | Tất cả toa đã mua (cho phép tải lại) |
| CUST-POINTS | Điểm thưởng | UC14, UC19 | Tích điểm đổi quà, lịch sử giao dịch |
| CUST-FAVORITES | Danh sách yêu thích | UC14 | Sản phẩm đã lưu |
| CUST-NOTIF-SETTINGS | Cài đặt thông báo | UC14, UC17 | Bật/tắt push notification |

###### Nhóm 6 — AI Features (4 màn hình, UC15)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| CHAT-AI | AI Tư vấn dược sĩ ảo | UC15 | Chat với AI về triệu chứng, tương tác thuốc, liều lượng — 24/7 |
| AI-RX-OCR | AI OCR đơn thuốc | UC15 | Chụp ảnh → AI tự động nhận diện thuốc + liều lượng |
| AI-DRUG-CHECK | AI Kiểm tra tương tác | UC15 | Nhập danh sách thuốc → AI phát hiện drug-drug, drug-food, drug-allergy |
| AI-SEMANTIC-SEARCH | AI Semantic Search | UC15 | Tìm theo triệu chứng (vd: "đau đầu, sốt") thay vì tên thuốc |

###### Nhóm 7 — Pharmacist — Người bạn tại quầy (5 màn hình, UC16)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| RX-CONSULT | Tư vấn 1-1 | UC16 | Phiên tư vấn chuyên sâu giữa dược sĩ và khách (text + voice + video) |
| RX-CUST-PROFILE-360 | Hồ sơ khách 360° | UC16 | Lịch sử toàn diện: thuốc, dị ứng, bệnh nền, tư vấn trước, lịch tái khám |
| RX-CROSS-SELL | Gợi ý cross-sell | UC16, UC15 | AI gợi ý sản phẩm bổ sung kèm theo đơn thuốc |
| RX-FOLLOW-UP | Follow-up sau bán | UC16, UC17 | Lịch nhắc uống thuốc, đánh giá hiệu quả sau 3/7/14 ngày |
| RX-VIP-MARK | Đánh dấu khách VIP | UC16 | Flag khách quen, mức chi tiêu, tần suất mua, "điểm trung thành" |

###### Nhóm 8 — Health Tools (2 màn hình, UC18)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| HEALTH-QUIZ-LIST | DS bài kiểm tra sức khoẻ | UC18 | 8+ bài quiz: trí nhớ, tiểu đường, suy giáp, hen, tim mạch, Alzheimer, trào ngược, bình xịt |
| HEALTH-QUIZ-RESULT | Kết quả kiểm tra | UC18 | Kết quả + lời khuyên "xử trí phù hợp" + CTA đặt lịch khám |

###### Nhóm 9 — Mobile App (2 màn hình, UC17)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| MOBILE-HOME | Trang chủ App | UC17 | Menu tiện ích grid icon: Nhắc uống thuốc, Sổ tiêm, Đơn thuốc, Tư vấn, Tìm nhà thuốc, Vắc xin, Tra thuốc, Đặt hàng nhanh |
| MOBILE-MED-REMINDER | Nhắc uống thuốc | UC17, UC15 | 5 bước: Tạo lịch → Nhập/chụp đơn → Sửa thông tin → Hẹn giờ (calendar sync) → Theo dõi |

###### Nhóm 10 — E-commerce Operations (4 màn hình, UC19)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| SHOP-VOUCHER | Voucher / Coupon | UC19 | Nhập mã, danh sách voucher khả dụng, lịch sử dùng |
| SHOP-REVIEW | Đánh giá sản phẩm | UC19 | 1–5 sao, hình ảnh, bình luận, lọc theo rating |
| SHOP-LIVE-CHAT | Chat với dược sĩ | UC19, UC16 | Chat realtime trước/sau mua hàng, có thể chuyển sang gọi video |
| SHOP-FLASH-SALE | Flash sale / Combo | UC19 | Đếm ngược thời gian, giá sốc, combo deals, giới hạn số lượng |

##### Phần C — Tĩnh (Static pages, 7 màn hình, có sẵn trong footer B2B)

| Screen ID | Screen Name | Use Case | Description |
| :---- | :---- | :---- | :---- |
| PAGE-ABOUT | Giới thiệu | — | Về FPT Long Châu / PCMS |
| PAGE-NEWS | Tin tức sự kiện | — | PR, hợp tác chiến lược |
| PAGE-CAREERS | Tuyển dụng | — | Cơ hội nghề nghiệp |
| PAGE-SHIP-POLICY | Chính sách giao hàng | — | 12 chính sách chi tiết |
| PAGE-RETURN-POLICY | Chính sách đổi trả | — | Đổi trả trong 30 ngày |
| PAGE-PRIVACY | Chính sách bảo mật | — | Bảo vệ dữ liệu cá nhân |
| PAGE-TOS | Điều khoản sử dụng | — | |

**Tổng kết màn hình v1.4.0:**

| Nhóm | Số màn hình |
|:-----|:-----------:|
| B2B Authenticated App (SCR-*) | 28 |
| Customer Portal — Mua sắm (SHOP-*) | 11 |
| Customer Portal — Tra cứu | 8 |
| Customer Portal — Hệ thống nhà thuốc (STORE-*) | 4 |
| Customer Portal — Tiêm chủng (VACCINE-*) | 3 |
| Customer Portal — Tài khoản (CUST-*) | 9 |
| AI Features (CHAT-AI, AI-*) | 4 |
| Pharmacist — Người bạn tại quầy (RX-*) | 5 |
| Health Tools (HEALTH-*) | 2 |
| Mobile App (MOBILE-*) | 2 |
| E-commerce Operations | 4 |
| Tĩnh (PAGE-*) | 7 |
| **TỔNG CỘNG** | **85** |

#### 3.1.3 Screen Authorization

> **v1.4.0 mở rộng:** Matrix thêm 2 cột: **Guest** (khách vãng lai, chưa đăng nhập) + **AI Engine** (service role). Giá trị:
> ● = full access, ◐ = read-only, ○ = no access, − = not applicable, (own) = chỉ dữ liệu của chính mình, (own branch) = chỉ dữ liệu chi nhánh mình quản lý.

##### Phần A — B2B Authenticated App (28 màn hình — matrix giữ nguyên từ v1.3.0)

| Screen ID | Admin | CEO | Branch Manager | Pharmacist | Customer |
| :---- | :---- | :---- | :---- | :---- | :---- |
| SCR-LOGIN | ● | ● | ● | ● | ● |
| SCR-HOME | ● | ● | ● | ● | ● |
| SCR-USER-LIST | ● | ● | ◐ | ○ | ○ |
| SCR-USER-FORM | ● | ● | ○ | ○ | ○ |
| SCR-BRANCH-LIST | ● | ● | ◐ | ○ | ○ |
| SCR-BRANCH-FORM | ● | ● | ◐ | ○ | ○ |
| SCR-MED-LIST | ● | ● | ◐ | ● | ◐ |
| SCR-MED-FORM | ● | ● | ○ | ● | ○ |
| SCR-INV-LIST | ● | ● | ● | ◐ | ○ |
| SCR-INV-IMPORT | ● | ● | ● | ○ | ○ |
| SCR-INV-EXPORT | ● | ● | ● | ○ | ○ |
| SCR-INV-TRANSFER | ● | ● | ● | ○ | ○ |
| SCR-ORDER-LIST | ● | ● | ● | ● | ◐ (own) |
| SCR-ORDER-NEW | ● | ● | ◐ | ● | ● |
| SCR-ORDER-DETAIL | ● | ● | ● | ● | ◐ (own) |
| SCR-PAYMENT | ● | ● | ◐ | ● | − |
| SCR-INVOICE | ● | ● | ● | ● | ◐ (own) |
| SCR-CUST-LIST | ● | ● | ◐ | ● | ○ |
| SCR-CUST-FORM | ● | ● | ○ | ● | ● (own) |
| SCR-CUST-HISTORY | ● | ● | ◐ | ● | ◐ (own) |
| SCR-REPORT | ● | ● | ● (own branch) | ○ | ○ |
| SCR-REPORT-EXPORT | ● | ● | ● (own branch) | ○ | ○ |
| SCR-SEARCH | ● | ● | ● | ● | ● |
| SCR-SUPPLIER-LIST | ● | ● | ◐ | ◐ | ○ |
| SCR-SUPPLIER-FORM | ● | ● | ○ | ○ | ○ |
| SCR-RX | ● | ● | ◐ | ● | − |
| SCR-NOTIF-LIST | ● | ● | ● | ● | ● |
| SCR-NOTIF-COMPOSE | ● | ● | ○ | ○ | ○ |

##### Phần B — B2C Customer Portal (52 màn hình — NEW)

| Screen ID | Guest | Customer | Pharmacist | Admin / CEO | AI Engine |
| :---- | :---- | :---- | :---- | :---- | :---- |
| SHOP-HOME | ● | ● | ● | ◐ | − |
| SHOP-CAT-1 | ● | ● | ● | ◐ | − |
| SHOP-CAT-2 | ● | ● | ● | ◐ | − |
| SHOP-PDP | ● | ● | ● | ◐ | − |
| SHOP-SEARCH | ● | ● | ● | ◐ | ● (read) |
| SHOP-CART | ● | ● (own) | − | − | − |
| SHOP-CHECKOUT | ○ | ● (own) | − | − | − |
| SHOP-ORDER-HISTORY | ○ | ● (own) | ◐ (own branch) | ◐ | − |
| SHOP-ORDER-TRACK | ○ | ● (own) | ◐ (own branch) | ◐ | − |
| SHOP-RX-UPLOAD | ○ | ● | ● (review) | ◐ | ● (OCR) |
| SHOP-INSTALLMENT | ● (info) | ● | − | ◐ | − |
| SHOP-LOOKUP-DRUG | ● | ● | ● | ◐ | ● (semantic) |
| SHOP-LOOKUP-INGREDIENT | ● | ● | ● | ◐ | ● (semantic) |
| SHOP-LOOKUP-HERB | ● | ● | ● | ◐ | ● (semantic) |
| SHOP-VERIFY-ORIGIN | ● | ● | ● | ◐ | − |
| SHOP-HEALTH-ARTICLE | ● | ● | ● | ◐ | − |
| SHOP-DISEASE-INFO | ● | ● | ● | ◐ | − |
| SHOP-CANCER-INFO | ● | ● | ● | ◐ | − |
| SHOP-VIDEO | ● | ● | ● | ◐ | − |
| STORE-LOCATOR | ● | ● | ● | ◐ | − |
| STORE-LIST-PROVINCE | ● | ● | ● | ◐ | − |
| STORE-DETAIL | ● | ● | ● | ◐ | − |
| STORE-CONSULT | ◐ (book) | ● (book) | ● (consult) | ◐ | − |
| VACCINE-HOME | ● | ● | ● | ◐ | − |
| VACCINE-BOOKING | ◐ | ● | ◐ | ◐ | − |
| VACCINE-LEDGER | ○ | ● (own + family) | ◐ | ◐ | − |
| CUST-LOGIN | ● | − | − | − | − |
| CUST-PROFILE | ○ | ● (own) | ◐ (consult) | ◐ | − |
| CUST-ADDRESS | ○ | ● (own) | ◐ (need) | ◐ | − |
| CUST-FAMILY | ○ | ● (own) | ◐ | ◐ | − |
| CUST-HEALTH-WALLET | ○ | ● (own + family) | ◐ | ◐ | − |
| CUST-RX-HISTORY | ○ | ● (own) | ● (consult) | ◐ | − |
| CUST-POINTS | ○ | ● (own) | ◐ | ◐ | − |
| CUST-FAVORITES | ○ | ● (own) | ◐ (recommend) | ◐ | ● (read) |
| CUST-NOTIF-SETTINGS | ○ | ● (own) | − | − | − |
| CHAT-AI | ● (limited) | ● (full) | ● (pro) | ◐ | − |
| AI-RX-OCR | − | ● | ● | ◐ | − |
| AI-DRUG-CHECK | − | ● (own) | ● | ◐ | − |
| AI-SEMANTIC-SEARCH | − | − | − | − | ● (backend) |
| RX-CONSULT | ◐ (book) | ● (request) | ● (deliver) | ◐ | − |
| RX-CUST-PROFILE-360 | − | − | ● (own branch) | ◐ | − |
| RX-CROSS-SELL | − | − | ● | ◐ | ● (read) |
| RX-FOLLOW-UP | ○ | ● (own) | ● (manage) | ◐ | − |
| RX-VIP-MARK | − | − | ● (own branch) | ◐ | − |
| HEALTH-QUIZ-LIST | ● | ● | ● | ◐ | − |
| HEALTH-QUIZ-RESULT | ● (own) | ● (own) | ◐ | ◐ | − |
| MOBILE-HOME | ● | ● | ● | ◐ | − |
| MOBILE-MED-REMINDER | ○ | ● (own) | ◐ | ◐ | − |
| SHOP-VOUCHER | ● (browse) | ● (use) | − | ● (manage) | − |
| SHOP-REVIEW | ● (browse) | ● (post own) | ◐ (reply) | ◐ (moderate) | − |
| SHOP-LIVE-CHAT | ◐ (init) | ● (use) | ● (deliver) | ◐ | − |
| SHOP-FLASH-SALE | ● | ● | − | ● (manage) | − |

##### Phần C — Tĩnh (7 màn hình, công khai)

Tất cả role (kể cả Guest) đều có `●` truy cập: PAGE-ABOUT, PAGE-NEWS, PAGE-CAREERS, PAGE-SHIP-POLICY, PAGE-RETURN-POLICY, PAGE-PRIVACY, PAGE-TOS.

#### 3.1.4 Non-Screen Functions

Functions executed by the system (background jobs, scheduled tasks, internal APIs) that have no direct UI.

| ID | Function | Trigger | Frequency | Owner UC | Notes |
| :---- | :---- | :---- | :---- | :---- | :---- |
| NSF-01 | Auto-cancel unpaid orders | Cron | Every 15 min | UC06 | Applies BR01; cancels orders in PENDING_PAYMENT > 24h |
| NSF-02 | Low-stock evaluation | Inventory transaction commit | On event | UC05, UC13 | Applies BR02; emits notification when stock < min |
| NSF-03 | Batch expiry check | Cron | Daily 00:00 | UC05, UC13 | Applies BR03; alerts items expiring ≤ 30 days |
| NSF-04 | Loyalty points award | Order paid | On event | UC07 | Applies BR07; 1 pt / 1,000 VND |
| NSF-05 | Stock FIFO consumption | Order placed/paid | On event | UC06 | Consumes oldest batch by expiry |
| NSF-06 | Daily sales roll-up | Cron | Daily 01:00 | UC09 | Pre-aggregates revenue per branch |
| NSF-07 | Token blacklist sync | User logout | On event | UC01 | Pushes revoked JWTs to all API gateways |
| NSF-08 | Backup scheduler | Cron | Daily 02:00 incremental, Sun 02:00 full | — | §4.7 |
| NSF-09 | Notification retry | Channel failure | On event, exp backoff x3 | UC13 | Retries SMS/email 3x |
| NSF-10 | Account auto-unlock | Cron | Every 5 min | UC01 | Unlocks accounts whose 30-min lockout has expired |
| NSF-11 | Invoice number generator | Payment success | On event | UC07 | Generates INV-yyyymmdd-#### |
| NSF-12 | Order auto-number generator | Order created | On event | UC06 | Generates ORD-yyyymmdd-#### |
| **NSF-13** | **AI OCR processing pipeline** | Image uploaded | On event | **UC15** | Decodes prescription image, extracts medicine names + dosages via CV model. Posts to pharmacist review queue. |
| **NSF-14** | **AI semantic search indexer** | Medicine/category change | On event | **UC15** | Re-indexes vector embeddings (e.g. OpenAI text-embedding-3-small) for symptom-based search. |
| **NSF-15** | **AI drug interaction cache** | Drug added to catalog | On event | **UC15** | Pre-computes drug-drug interaction graph (twELVET / DrugBank / local rules) for instant lookup. |
| **NSF-16** | **AI demand forecasting** | Cron | Daily 02:30 | **UC15, UC09** | Predicts next 30/60/90-day stock demand by SKU per branch using Prophet / ARIMA. |
| **NSF-17** | **AI chat model inference** | User sends message | On event | **UC15** | Calls LLM (GPT-4o-mini / local Llama-3) with PCMS RAG context (medicines, symptoms, policies). |
| **NSF-18** | **AI prescription anomaly detector** | Prescription created | On event | **UC15, UC12** | Flags suspicious patterns: duplicate ingredients, contraindication, dosage > max, age mismatch. |
| **NSF-19** | **Customer follow-up scheduler** | Order paid | On event | **UC16** | Schedules 3-day, 7-day, 14-day follow-up push notifications asking "thuốc có hiệu quả không?" |
| **NSF-20** | **Voucher/coupon expiry** | Cron | Every 1 hour | **UC19** | Disables expired vouchers, notifies users before expiry (24h, 1h) |
| **NSF-21** | **Flash sale scheduler** | Cron | Every 1 min | **UC19** | Activates flash sales, decrements stock atomically, ends on timeout or sold-out |
| **NSF-22** | **GPS pharmacy locator index** | Branch update | On event | **UC14** | Re-indexes branches into PostGIS / Elasticsearch for `near me` queries |
| **NSF-23** | **Mobile push notification dispatcher** | Various triggers | On event | **UC17, UC13** | Routes to FCM (Android) + APNs (iOS), respects user preferences |
| **NSF-24** | **Calendar sync for medication reminders** | Reminder saved | On event | **UC17** | Pushes events to user's phone calendar (iOS EventKit / Android Calendar Provider) |

#### 3.1.5 Entity Relationship Diagram

*Xem chi tiết:* [Entity Relationship Diagram](./diagrams/entity-relationship.puml)

**Entity summary:**

| Entity | Description | Primary Fields | Managed By |
| :---- | :---- | :---- | :---- |
| User | System user account | id, email, role, branch_id | Admin, CEO |
| Branch | Pharmacy branch | id, code, name, manager_id, lat/lng | Admin, CEO |
| Medicine | Medicine / product | id, sku, name, price, category_id | Admin, CEO, Pharmacist |
| Category | Medicine category | id, name | Admin, CEO |
| Customer | End customer | id, code, phone, points, loyalty_tier | Customer, Pharmacist |
| Order | Sales order | id, order_number, customer_id, branch_id, total, status, channel | Pharmacist, Customer |
| OrderItem | Order line item | id, order_id, medicine_id, qty, unit_price | Pharmacist |
| InventoryBatch | Per-branch batch stock | id, medicine_id, branch_id, batch_no, qty_on_hand, expiry_date | Branch Manager |
| InventoryTransaction | Stock movement record | id, batch_id, type, qty, reason, actor_id | Branch Manager |
| Supplier | Medicine supplier | id, name, tax_code, phone | Admin, CEO |
| Prescription | Doctor's prescription | id, code, patient_id, doctor_id, diagnosis | Pharmacist |
| Notification | System notification | id, recipient_id, channel, status | System, Admin, CEO |
| **CustomerAddress** *(NEW)* | Delivery address book | customer_id, province, is_default | Customer |
| **CustomerFamily** *(NEW)* | Family member (Tài khoản gia đình) | owner_id, member_name, relationship | Customer |
| **ProductReview** *(NEW)* | Product review & rating | product_id, customer_id, rating, body | Customer, AI moderation |
| **Voucher** *(NEW)* | Coupon / flash-sale code | code, type, value, valid_to, used_count | Admin, CEO |
| **PrescriptionB2C** *(NEW)* | Uploaded prescription (with AI OCR) | image_url, ocr_result, pharmacist_status | Customer, Pharmacist, AI |
| **MedicationReminder** *(NEW)* | Nhắc uống thuốc | customer_id, schedule, start_date | Customer (via App) |
| **MedicationIntake** *(NEW)* | Actual intake log | reminder_id, status, taken_at | System auto |
| **AIChatSession** *(NEW)* | AI chat history | customer_id, messages, status | Customer, AI, Pharmacist |
| **DrugInteractionRule** *(NEW)* | Drug-drug interaction knowledge | drug_a_id, drug_b_id, severity | Admin, AI |
| **HealthQuizResult** *(NEW)* | Health quiz answers + advice | quiz_slug, score, risk_level | Customer, AI |
| **FollowUpTask** *(NEW)* | Post-sale follow-up tasks | customer_id, order_id, type, status | Pharmacist, System |
| **FlashSale** *(NEW)* | Time-limited promotion | name, starts_at, ends_at, items | Admin, CEO |

#### 3.1.6 Entity Details

> Per template v2.3: attribute-level breakdown of each core entity. PK = Primary Key.

**Entity 1 — User**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| email | - | varchar(100) | Yes | Login email, unique |
| password_hash | - | varchar(255) | Yes | bcrypt hash |
| full_name | - | varchar(100) | Yes | Display name |
| phone | - | varchar(20) | No | Contact phone |
| role | - | enum | Yes | ADMIN / CEO / BRANCH_MANAGER / PHARMACIST / CUSTOMER |
| branch_id | - | UUID (FK→branches) | No | Assigned branch (null for Admin/Customer) |
| status | - | enum | Yes | ACTIVE / INACTIVE / LOCKED |
| last_login_at | - | timestamp | No | Last successful login |
| last_login_ip | - | varchar(45) | No | IPv4/IPv6 |
| created_at | - | timestamp | Yes | Creation time |
| updated_at | - | timestamp | Yes | Last update |

**Entity 2 — Branch**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| code | - | varchar(10) | Yes | Unique branch code |
| name | - | varchar(100) | Yes | Branch display name |
| address | - | varchar(255) | Yes | Full address |
| phone | - | varchar(20) | Yes | Contact phone |
| manager_id | - | UUID (FK→users) | No | Branch manager |
| status | - | enum | Yes | ACTIVE / INACTIVE |
| created_at | - | timestamp | Yes | |
| updated_at | - | timestamp | Yes | |

**Entity 3 — Medicine**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| sku | - | varchar(20) | Yes | Unique SKU |
| name | - | varchar(200) | Yes | Medicine name |
| category_id | - | UUID (FK→categories) | Yes | Category reference |
| supplier_id | - | UUID (FK→suppliers) | No | Default supplier |
| price | - | decimal(15,2) | Yes | Unit price (VND) > 0 |
| unit | - | varchar(20) | Yes | box, bottle, strip |
| prescription_required | - | boolean | Yes | Rx flag |
| image_url | - | varchar(255) | No | Product image |
| status | - | enum | Yes | ACTIVE / INACTIVE |
| created_at / updated_at | - | timestamp | Yes | |

**Entity 4 — Category**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| name | - | varchar(100) | Yes | Category name, unique |
| description | - | varchar(255) | No | |
| created_at | - | timestamp | Yes | |

**Entity 5 — Customer**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| code | - | varchar(20) | Yes | CUST-yyyy#### unique |
| name | - | varchar(100) | Yes | Full name |
| phone | - | varchar(20) | Yes | Unique contact phone |
| email | - | varchar(100) | No | Optional, unique |
| address | - | varchar(255) | No | |
| dob | - | date | No | Date of birth |
| gender | - | enum | No | MALE / FEMALE / OTHER |
| points | - | int | Yes | Loyalty points balance |
| created_at / updated_at | - | timestamp | Yes | |

**Entity 6 — Order**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| order_number | - | varchar(30) | Yes | ORD-yyyymmdd-#### |
| customer_id | - | UUID (FK→customers) | Yes | Buyer |
| branch_id | - | UUID (FK→branches) | Yes | Fulfilling branch |
| staff_id | - | UUID (FK→users) | No | Staff who created (null for online) |
| subtotal | - | decimal(15,2) | Yes | Sum of line totals |
| discount | - | decimal(15,2) | Yes | Total discount |
| total | - | decimal(15,2) | Yes | Payable amount |
| status | - | enum | Yes | PENDING_PAYMENT / PAID / CANCELLED / COMPLETED |
| prescription_id | - | UUID (FK→prescriptions) | No | Linked Rx |
| created_at / updated_at | - | timestamp | Yes | |

**Entity 7 — OrderItem**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| order_id | - | UUID (FK→orders) | Yes | Parent order |
| medicine_id | - | UUID (FK→medicines) | Yes | Medicine reference |
| batch_id | - | UUID (FK→inventory_batches) | No | Auto-picked FIFO |
| qty | - | int | Yes | Quantity > 0 |
| unit_price | - | decimal(15,2) | Yes | Price at order time |
| discount | - | decimal(15,2) | Yes | Line discount |
| subtotal | - | decimal(15,2) | Yes | qty * unit_price − discount |

**Entity 8 — InventoryBatch**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| medicine_id | - | UUID (FK→medicines) | Yes | |
| branch_id | - | UUID (FK→branches) | Yes | |
| batch_no | - | varchar(30) | Yes | Unique per medicine |
| qty_on_hand | - | int | Yes | Remaining units |
| expiry_date | - | date | Yes | Must be future on import |
| received_at | - | timestamp | Yes | |

**Entity 9 — InventoryTransaction**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| batch_id | - | UUID (FK→inventory_batches) | Yes | |
| type | - | enum | Yes | IMPORT / EXPORT / TRANSFER_OUT / TRANSFER_IN / SALE |
| qty | - | int | Yes | Delta units |
| reason | - | varchar(255) | No | Required for EXPORT |
| ref_id | - | UUID | No | Order/PO reference |
| actor_id | - | UUID (FK→users) | Yes | Performed by |
| created_at | - | timestamp | Yes | |

**Entity 10 — Supplier**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| name | - | varchar(150) | Yes | Supplier name |
| tax_code | - | varchar(20) | Yes | Unique tax code |
| contact_person | - | varchar(100) | No | |
| phone | - | varchar(20) | Yes | |
| email | - | varchar(100) | No | |
| address | - | varchar(255) | No | |
| bank_name | - | varchar(100) | No | |
| bank_account | - | varchar(30) | No | |
| status | - | enum | Yes | ACTIVE / INACTIVE |

**Entity 11 — Prescription**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| code | - | varchar(20) | Yes | RX-yyyy#### unique |
| patient_id | - | UUID (FK→customers) | Yes | Patient |
| doctor_id | - | UUID (FK→users) | Yes | Prescribing pharmacist |
| diagnosis | - | text | Yes | |
| notes | - | text | No | |
| signature_hash | - | varchar(255) | Yes | Digital signature |
| issued_at | - | timestamp | Yes | |

**Entity 12 — Notification**

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | Primary identifier |
| recipient_id | - | UUID (FK→users) | Yes | |
| channel | - | enum | Yes | IN_APP / EMAIL / SMS / PUSH |
| template | - | varchar(50) | No | NTPL-* key |
| title | - | varchar(150) | Yes | |
| body | - | text | Yes | |
| status | - | enum | Yes | PENDING / SENT / FAILED / READ |
| sent_at | - | timestamp | No | |
| read_at | - | timestamp | No | |

---

#### 3.1.6.1 New Entities (v1.4.0 — B2C + AI + Pharmacist Tools)

> **NEW v1.4.0:** Bổ sung **12 entity mới** phục vụ Customer Portal, AI Engine, Pharmacist Tools, Mobile App, Health Tools, E-commerce.

**Entity 13 — CustomerAddress** *(UC14)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| customer_id | - | UUID (FK→customers) | Yes | |
| label | - | varchar(50) | No | "Nhà", "Công ty" |
| recipient_name | - | varchar(150) | Yes | |
| phone | - | varchar(20) | Yes | |
| address_line | - | text | Yes | |
| ward | - | varchar(100) | No | |
| district | - | varchar(100) | No | |
| province | - | varchar(100) | Yes | |
| is_default | - | boolean | Yes | default false |

**Entity 14 — CustomerFamily** *(UC14, UC17)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| owner_id | - | UUID (FK→customers) | Yes | Tài khoản chính |
| member_name | - | varchar(150) | Yes | |
| member_phone | - | varchar(20) | No | |
| relationship | - | enum | Yes | SPOUSE / CHILD / PARENT / SIBLING / OTHER |
| date_of_birth | - | date | No | |
| allergies | - | text[] | No | JSON array of allergy drug IDs |
| chronic_conditions | - | text[] | No | JSON array |
| created_at | - | timestamp | Yes | |

**Entity 15 — ProductReview** *(UC19)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| product_id | - | UUID (FK→medicines) | Yes | |
| customer_id | - | UUID (FK→customers) | Yes | |
| rating | - | int (1-5) | Yes | |
| title | - | varchar(150) | No | |
| body | - | text | No | |
| images | - | text[] | No | URL array |
| is_verified_purchase | - | boolean | Yes | Mua thật tại hệ thống |
| helpful_count | - | int | Yes | default 0 |
| status | - | enum | Yes | PENDING / APPROVED / REJECTED |
| created_at | - | timestamp | Yes | |

**Entity 16 — Voucher / Coupon** *(UC19)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| code | - | varchar(30) | Yes | Unique, e.g. "TET2026" |
| type | - | enum | Yes | PERCENT / FIXED_AMOUNT / FREE_SHIP |
| value | - | decimal | Yes | % hoặc VND |
| min_order | - | decimal | No | Đơn tối thiểu |
| max_discount | - | decimal | No | Trần giảm (cho PERCENT) |
| valid_from | - | timestamp | Yes | |
| valid_to | - | timestamp | Yes | |
| usage_limit | - | int | No | Tổng lượt dùng |
| usage_per_user | - | int | Yes | default 1 |
| used_count | - | int | Yes | default 0 |
| applicable_categories | - | UUID[] | No | NULL = tất cả |
| status | - | enum | Yes | ACTIVE / DISABLED / EXPIRED |

**Entity 17 — Order (B2C — extended)** *(UC14)*

> Tái sử dụng bảng `orders` từ v1.3.0 với 4 field bổ sung:

| Attribute name | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- |
| channel | enum | Yes | POS / WEB / APP / CALLCENTER |
| delivery_address_id | UUID (FK→customer_addresses) | No | NULL nếu POS tại quầy |
| voucher_id | UUID (FK→vouchers) | No | |
| expected_delivery_at | timestamp | No | |

**Entity 18 — Prescription (B2C — uploaded)** *(UC14, UC15)*

> Tách biệt với `prescriptions` (B2B do dược sĩ tạo):

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| customer_id | - | UUID (FK→customers) | Yes | |
| image_url | - | text | Yes | Ảnh gốc upload |
| ocr_status | - | enum | Yes | PENDING / PROCESSING / DONE / FAILED |
| ocr_result | - | jsonb | No | Kết quả OCR: [{medicine_name, dosage, frequency, duration}] |
| pharmacist_id | - | UUID (FK→users) | No | Dược sĩ review |
| pharmacist_status | - | enum | No | PENDING / APPROVED / REJECTED / NEEDS_INFO |
| pharmacist_notes | - | text | No | |
| created_at | - | timestamp | Yes | |

**Entity 19 — MedicationReminder** *(UC17, UC16)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| customer_id | - | UUID (FK→customers) | Yes | |
| family_member_id | - | UUID (FK→customer_family) | No | |
| name | - | varchar(150) | Yes | "Toa thuốc T2 2026" |
| source_type | - | enum | Yes | MANUAL / PHOTO / B2B_RX |
| source_id | - | UUID | No | FK tuỳ source_type |
| schedule | - | jsonb | Yes | [{time: "08:00", days: [1,2,3,4,5,6,7], dosage, instructions}] |
| start_date | - | date | Yes | |
| end_date | - | date | No | NULL = ongoing |
| status | - | enum | Yes | ACTIVE / PAUSED / DONE |
| adherence_rate | - | decimal(5,2) | No | % tuân thủ (tính real-time) |

**Entity 20 — MedicationIntake** *(UC16, UC17)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| reminder_id | - | UUID (FK→medication_reminders) | Yes | |
| scheduled_at | - | timestamp | Yes | |
| status | - | enum | Yes | TAKEN / SKIPPED / MISSED / LATE |
| taken_at | - | timestamp | No | |
| notes | - | text | No | |

**Entity 21 — AIChatSession** *(UC15)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| customer_id | - | UUID (FK→customers) | No | NULL nếu guest |
| channel | - | enum | Yes | WEB_CHAT / MOBILE_CHAT / POS_KIOSK |
| context_type | - | enum | Yes | SYMPTOM / DRUG_INFO / INTERACTION / GENERAL |
| messages | - | jsonb | Yes | [{role, content, timestamp, sources[]}] |
| escalated_to_pharmacist | - | boolean | Yes | default false |
| pharmacist_id | - | UUID (FK→users) | No | |
| status | - | enum | Yes | ACTIVE / CLOSED / ESCALATED |
| started_at | - | timestamp | Yes | |
| closed_at | - | timestamp | No | |

**Entity 22 — DrugInteractionRule** *(UC15)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| drug_a_id | - | UUID (FK→medicines) | Yes | |
| drug_b_id | - | UUID (FK→medicines) | Yes | |
| severity | - | enum | Yes | MINOR / MODERATE / MAJOR / CONTRAINDICATED |
| mechanism | - | varchar(200) | No | |
| clinical_effect | - | text | No | |
| recommendation | - | text | No | |
| source | - | varchar(100) | No | "DrugBank", "twELVET", "Local rule" |

**Entity 23 — HealthQuizResult** *(UC18)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| customer_id | - | UUID (FK→customers) | No | NULL nếu guest |
| quiz_slug | - | varchar(50) | Yes | "memory-test", "diabetes-risk" |
| score | - | int | Yes | |
| max_score | - | int | Yes | |
| risk_level | - | enum | No | LOW / MODERATE / HIGH |
| answers | - | jsonb | Yes | |
| advice_shown | - | text | Yes | |
| cta_clicked | - | varchar(50) | No | "BOOK_CONSULT" / "ORDER_TEST" |
| taken_at | - | timestamp | Yes | |

**Entity 24 — FollowUpTask** *(UC16)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| customer_id | - | UUID (FK→customers) | Yes | |
| order_id | - | UUID (FK→orders) | Yes | |
| pharmacist_id | - | UUID (FK→users) | No | |
| type | - | enum | Yes | CHECK_EFFECT / SIDE_EFFECT / REFILL_REMINDER / RE_EXAM |
| scheduled_at | - | timestamp | Yes | |
| status | - | enum | Yes | PENDING / SENT / DONE / SKIPPED |
| response | - | jsonb | No | {feeling, side_effects, needs_callback} |
| completed_at | - | timestamp | No | |

**Entity 25 — FlashSale** *(UC19)*

| Attribute name | PK | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | Yes | UUID | Yes | |
| name | - | varchar(150) | Yes | |
| starts_at | - | timestamp | Yes | |
| ends_at | - | timestamp | Yes | |
| items | - | jsonb | Yes | [{medicine_id, original_price, sale_price, qty_limit, qty_sold}] |
| banner_url | - | text | No | |
| status | - | enum | Yes | DRAFT / SCHEDULED / ACTIVE / ENDED |

**Entity 26 — Branch (extended — B2C fields)** *(UC14)*

> Bổ sung 4 field cho locator:

| Attribute name | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- |
| latitude | decimal(10,7) | No | |
| longitude | decimal(10,7) | No | |
| opening_hours | jsonb | Yes | {"mon": "06:00-23:00", ...} |
| services | string[] | No | ["Tư vấn", "Tiêm chủng", "Đo huyết áp", ...] |
| phone | varchar(20) | Yes | |

**Entity 27 — Customer (extended — v1.4.0)**

> Bổ sung 3 field cho Health Wallet + Loyalty mở rộng:

| Attribute name | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- |
| health_wallet_balance | decimal(12,2) | Yes | default 0 — VND |
| loyalty_tier | enum | Yes | BRONZE / SILVER / GOLD / PLATINUM |
| consent_ai | boolean | Yes | default false — GDPR-style consent cho AI processing |
| preferred_pharmacist_id | UUID (FK→users) | No | Pharmacist "người bạn tại quầy" |

### 3.2 Use Case Details

> Detailed specification of each Use Case from [§2.2](#22-use-cases-summary). Each UC follows the template:
> 1. **Use Case Description** — single consolidated table (ID/Name/Author/Version/Date/Actor/Description/Precondition/Trigger/Post-Condition/Priority).
> 2. **Main Flow** — 3-column Step/Actor/Action table.
> 3. **Alternative Flows** — 5-column table (ID / Ref Step / Sub step / Actor / Action).
> 4. **Screen Mockup** — ASCII concept (for high/medium priority UCs).
> 5. **Screen Definition Table** — Field/Type/Mandatory/Max Length/Description.
> 6. **Diagram links** — Use-case, screen flow, state, sequence, backend class, frontend class.

---

#### 3.2.1 Authentication & Authorization

##### UC01 - Login

**Use Case Diagram:** [UC01 - Login](./diagrams/uc-01/uc-01-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC01 |
| **Use Case Name** | Login |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | User |
| **Secondary Actor** | System |
| **Description** | User authenticates to the system using email and password. |
| **Trigger** | User accesses the login page. |
| **Precondition** | PRE-1: User has a valid account. PRE-2: User knows credentials. |
| **Post-Condition** | POST-1: User is authenticated and redirected to dashboard. |
| **Priority** | High |

**Main Flow:**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Enters email and password, clicks "Login". |
| 2 | System | Validates input format (email regex, password required). |
| 3 | System | Looks up user record by email. |
| 4 | System | Verifies password hash using bcrypt. |
| 5 | System | Generates JWT access token (15 min) and refresh token (7 days). |
| 6 | System | Updates `last_login_at` and `last_login_ip`. |
| 7 | System | Returns 200 OK with tokens and user profile. |
| 8 | System | Redirects user to dashboard according to role. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 4 | 1 | System | Display error MSG01 and clear password field. |
| AT2 | Step 4 | 1 | System | Increment failed-attempt counter; if ≥ 5, lock account 30 min and display MSG02. |
| AT3 | Step 4 | 1 | System | If email not verified, display MSG03 and offer resend link. |
| AT4 | Step 3 | 1 | System | Return 503 with MSG04 (database unavailable). |
| AT5 | Step 5 | 1 | System | Log error, return 500 with MSG05. |

**Screen Mockup — SCR-LOGIN:**

```
+--------------------------------------------------+
|  Pharmacy Chain Management System                |
+--------------------------------------------------+
|              [ Logo ]                            |
|         +-----------------------+               |
|         | Email    [_________]  |               |
|         | Password [_________]  |               |
|         | ( ) Show password     |               |
|         +-----------------------+               |
|         [ Forgot password? ]                     |
|              [    Login    ]                     |
|         Don't have account? Sign up              |
+--------------------------------------------------+
|  v1.2.0  |  © 2026 Pharmacy Chain                |
+--------------------------------------------------+
```

**Screen Definition Table — SCR-LOGIN:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| email | text (email) | Yes | 100 | User email; RFC 5322 |
| password | password | Yes | 64 | Hidden by default |
| show_password | checkbox | No | - | Toggle reveal |
| btn_login | button | - | - | Submit |
| lnk_forgot_password | link | - | - | Reset flow |
| lnk_signup | link | - | - | Customer registration |

**Diagrams:** [Screenflow](./diagrams/uc-01/uc-01-screenflow.puml) · [State](./diagrams/uc-01/uc-01-statediagram.puml) · [Sequence](./diagrams/uc-01/uc-01-sequence.puml) · [Backend](./diagrams/uc-01/uc-01-class-backend.puml) · [Frontend](./diagrams/uc-01/uc-01-class-frontend.puml)

---

##### UC02 - Manage Users

**Use Case Diagram:** [UC02 - Manage Users](./diagrams/uc-02/uc-02-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC02 |
| **Use Case Name** | Manage Users |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Admin, CEO |
| **Secondary Actor** | System |
| **Description** | Admin/CEO creates, reads, updates, and deletes user accounts. |
| **Trigger** | Admin/CEO clicks Add/Edit/Delete User. |
| **Precondition** | PRE-1: Admin/CEO is authenticated. PRE-2: Admin/CEO has USER_MGMT permission. |
| **Post-Condition** | POST-1: User data is saved/updated/deleted. |
| **Priority** | High |

**Main Flow (Create User):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Admin | Navigates to User Management page. |
| 2 | System | Displays user list with pagination and search. |
| 3 | Admin | Clicks "Add User". |
| 4 | System | Opens user creation form. |
| 5 | Admin | Fills name, email, phone, role, branch, status. |
| 6 | Admin | Clicks "Save". |
| 7 | System | Validates (email unique, phone format, role allowed). |
| 8 | System | Hashes temporary password and persists user. |
| 9 | System | Sends welcome email with set-password link. |
| 10 | System | Displays success MSG06 and refreshes list. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Admin | Clicks Edit on a row → load row, repeat Steps 4–10 with pre-populated fields; password unchanged. |
| AT2 | Step 1 | 1 | Admin | Clicks Delete → confirm MSG07; on confirm soft-delete (status=INACTIVE). |
| AT3 | Step 2 | 1 | Admin | Types in search box → debounce 300 ms → re-query list. |
| AT4 | Step 7 | 1 | System | Display inline errors and highlight invalid fields (MSG31/33). |
| AT5 | Step 9 | 1 | System | Email service fails → log error, show MSG34, do not rollback user (manual resend later). |

**Screen Mockup — SCR-USER-LIST:**

```
+----------------------------------------------------------+
|  User Management                       [+ Add User]      |
+----------------------------------------------------------+
| Search: [___________]  Role: [All v]  Branch: [All v]    |
+----------------------------------------------------------+
| ID | Name    | Email          | Role    | Branch | Act   |
|----|---------|----------------|---------|--------|-------|
| 01 | Nguyen A| a@pcms.vn      | ADMIN   | HQ     | [E][D]|
| 02 | Tran B  | b@pcms.vn      | MGR     | B01    | [E][D]|
| 03 | Le C    | c@pcms.vn      | STAFF   | B01    | [E][D]|
+----------------------------------------------------------+
|  < 1 2 3 ... 12 >                                        |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-USER-LIST:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| search_box | text | No | 100 | Live filter by name/email |
| filter_role | select | No | - | Role filter |
| filter_branch | select | No | - | Branch filter |
| btn_add | button | - | - | Open create form |
| btn_edit | icon | - | - | Open edit form |
| btn_delete | icon | - | - | Open confirm |
| pagination | pager | - | - | Page navigation |

**Diagrams:** [Screenflow](./diagrams/uc-02/uc-02-screenflow.puml) · [State](./diagrams/uc-02/uc-02-statediagram.puml) · [Sequence](./diagrams/uc-02/uc-02-sequence.puml) · [Backend](./diagrams/uc-02/uc-02-class-backend.puml) · [Frontend](./diagrams/uc-02/uc-02-class-frontend.puml)

---

#### 3.2.2 Branch & Inventory Management

##### UC03 - Manage Branches

**Use Case Diagram:** [UC03 - Manage Branches](./diagrams/uc-03/uc-03-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC03 |
| **Use Case Name** | Manage Branches |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Admin, CEO |
| **Secondary Actor** | Branch Manager |
| **Description** | Admin/CEO manages pharmacy branches including creation, updates, and manager assignment. |
| **Trigger** | Admin/CEO clicks Add/Edit/Assign Manager. |
| **Precondition** | PRE-1: Admin/CEO is authenticated. |
| **Post-Condition** | POST-1: Branch data is saved and manager is assigned. |
| **Priority** | High |

**Main Flow (Create Branch):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Admin | Navigates to Branch Management. |
| 2 | System | Lists branches with status and manager name. |
| 3 | Admin | Clicks "Add Branch". |
| 4 | System | Opens branch form. |
| 5 | Admin | Fills code, name, address, phone, manager. |
| 6 | Admin | Clicks "Save". |
| 7 | System | Validates unique code, valid phone, manager role = BRANCH_MANAGER. |
| 8 | System | Persists branch and assigns manager. |
| 9 | System | Shows success MSG08. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Admin | Clicks Edit → load existing data → save updates. |
| AT2 | Step 1 | 1 | Admin | Clicks "Assign Manager" → reassigns manager without creating new branch. |
| AT3 | Step 7 | 1 | System | Display MSG09 (duplicate code) / MSG10 (bad phone) / MSG11 (wrong role). |

**Screen Mockup — SCR-BRANCH-LIST:**

```
+----------------------------------------------------------+
|  Branch Management                   [+ Add Branch]      |
+----------------------------------------------------------+
| Code | Name        | Address      | Phone    | Manager   |
|------|-------------|--------------|----------|-----------|
| HQ   | Headquarter | 12 Le Loi    | 0901...  | Nguyen A  |
| B01  | Branch 01   | 30 Nguyen Hue| 0902...  | Tran B    |
| B02  | Branch 02   | 55 Tran Hung | 0903...  | [Assign]  |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-BRANCH-LIST / SCR-BRANCH-FORM:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| branch_code | text | Yes | 10 | Unique uppercase code |
| branch_name | text | Yes | 100 | Display name |
| address | textarea | Yes | 255 | Full address |
| phone | tel | Yes | 20 | Contact phone |
| manager_id | select | No | - | User with role BRANCH_MANAGER |
| status | toggle | - | - | Active / Inactive |

**Diagrams:** [Screenflow](./diagrams/uc-03/uc-03-screenflow.puml) · [State](./diagrams/uc-03/uc-03-statediagram.puml) · [Sequence](./diagrams/uc-03/uc-03-sequence.puml) · [Backend](./diagrams/uc-03/uc-03-class-backend.puml) · [Frontend](./diagrams/uc-03/uc-03-class-frontend.puml)

---

##### UC04 - Manage Medicines

**Use Case Diagram:** [UC04 - Manage Medicines](./diagrams/uc-04/uc-04-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC04 |
| **Use Case Name** | Manage Medicines |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Admin, CEO, Pharmacist |
| **Secondary Actor** | - |
| **Description** | Manage medicine/product catalog including CRUD operations and stock updates. |
| **Trigger** | User clicks Add/Edit/Search/Update Stock. |
| **Precondition** | PRE-1: User has MEDICINE_MGMT permission. |
| **Post-Condition** | POST-1: Medicine data is updated. |
| **Priority** | High |

**Main Flow (Create Medicine):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Opens Medicine Management. |
| 2 | System | Lists medicines with filters (name, category, supplier, status). |
| 3 | User | Clicks "Add Medicine". |
| 4 | System | Opens medicine form. |
| 5 | User | Enters name, SKU, category, price, unit, expiry, Rx flag, supplier. |
| 6 | User | Uploads medicine image (optional). |
| 7 | User | Clicks "Save". |
| 8 | System | Validates SKU uniqueness, price > 0, valid category. |
| 9 | System | Persists medicine and image. |
| 10 | System | Returns success MSG12. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | User | Clicks Edit → load data → update record. |
| AT2 | Step 1 | 1 | User | Clicks "Update Stock" → open stock modal → save new level. |
| AT3 | Step 6 | 1 | System | Image upload fails → display MSG13 and rollback save. |

**Screen Mockup — SCR-MED-LIST:**

```
+----------------------------------------------------------+
|  Medicine Management                [+ Add Medicine]     |
+----------------------------------------------------------+
| [Search ___] Category: [All v] Supplier: [All v]         |
+----------------------------------------------------------+
| SKU   | Name        | Category | Price  | Stock | Rx     |
|-------|-------------|----------|--------|-------|--------|
| MD001 | Paracetamol | Pain     | 5,000  | 120   | No     |
| MD002 | Amoxicillin | Antibiot | 25,000 | 30    | Yes    |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-MED-LIST / SCR-MED-FORM:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| sku | text | Yes | 20 | Unique SKU |
| name | text | Yes | 200 | Medicine name |
| category_id | select | Yes | - | Category |
| price | number | Yes | - | Unit price (VND) > 0 |
| unit | text | Yes | 20 | box / bottle / strip |
| expiry_date | date | No | - | Default batch expiry |
| prescription_required | checkbox | No | - | Rx flag |
| supplier_id | select | No | - | Default supplier |
| image | file | No | 2 MB | PNG/JPG |
| stock | number | - | - | Read-only per branch |
| btn_save / btn_cancel | button | - | - | Form actions |

**Diagrams:** [Screenflow](./diagrams/uc-04/uc-04-screenflow.puml) · [State](./diagrams/uc-04/uc-04-statediagram.puml) · [Sequence](./diagrams/uc-04/uc-04-sequence.puml) · [Backend](./diagrams/uc-04/uc-04-class-backend.puml) · [Frontend](./diagrams/uc-04/uc-04-class-frontend.puml)

---

##### UC05 - Manage Inventory

**Use Case Diagram:** [UC05 - Manage Inventory](./diagrams/uc-05/uc-05-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC05 |
| **Use Case Name** | Manage Inventory |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Branch Manager |
| **Secondary Actor** | Pharmacist |
| **Description** | Handle inventory transactions including import, export, and inter-branch transfers. |
| **Trigger** | User selects inventory action. |
| **Precondition** | PRE-1: User has INVENTORY_MGMT permission. |
| **Post-Condition** | POST-1: Stock levels are updated. |
| **Priority** | High |

**Main Flow (Import Stock):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Opens Inventory module. |
| 2 | System | Shows current stock by branch. |
| 3 | User | Clicks "Import". |
| 4 | System | Opens import form (medicine, qty, batch, expiry, supplier). |
| 5 | User | Fills form, attaches PO reference. |
| 6 | User | Clicks "Submit". |
| 7 | System | Validates qty > 0, batch unique per medicine, expiry in future. |
| 8 | System | Persists inventory transaction (type=IMPORT). |
| 9 | System | Updates stock level for that branch. |
| 10 | System | Shows success MSG14. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | User | Clicks Export → require reason → decrement stock FIFO by batch. |
| AT2 | Step 1 | 1 | User | Clicks Transfer → pick source/dest branch → execute 2 transactions. |
| AT3 | Step 9 | 1 | System | If resulting stock < min_level, queue notification (NSF-02, UC13). |
| AT4 | Step 7 | 1 | System | Display MSG15 (qty≤0) / MSG16 (dup batch) / MSG17 (past expiry). |

**Screen Mockup — SCR-INV-LIST:**

```
+----------------------------------------------------------+
|  Inventory - Branch: B01 [v]   [+ Import] [Transfer]     |
+----------------------------------------------------------+
| SKU   | Name      | Batch   | Qty | Expiry    | Action   |
|-------|-----------|---------|-----|-----------|----------|
| MD001 | Paracetamol| BTH-001 | 120 | 2027-05   | [Export] |
| MD002 | Amoxicillin| BTH-014 | 30  | 2026-09   | [Export] |
+----------------------------------------------------------+
|  [Low Stock Alerts: 3 items]                              |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-INV-LIST / SCR-INV-IMPORT:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| branch_id | select | Yes | - | Working branch |
| medicine_id | select | Yes | - | Medicine reference |
| qty | number | Yes | - | Quantity > 0 |
| batch_no | text | Yes | 30 | Batch/lot number |
| expiry_date | date | Yes | - | Future date |
| supplier_id | select | No | - | For imports |
| reason | textarea | Conditional | 255 | Required for export |
| dest_branch_id | select | Conditional | - | Required for transfer |
| po_ref | text | No | 50 | PO ref |

**Diagrams:** [Screenflow](./diagrams/uc-05/uc-05-screenflow.puml) · [State](./diagrams/uc-05/uc-05-statediagram.puml) · [Sequence](./diagrams/uc-05/uc-05-sequence.puml) · [Backend](./diagrams/uc-05/uc-05-class-backend.puml) · [Frontend](./diagrams/uc-05/uc-05-class-frontend.puml)

---

#### 3.2.3 Order & Payment Processing

##### UC06 - Manage Orders

**Use Case Diagram:** [UC06 - Manage Orders](./diagrams/uc-06/uc-06-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC06 |
| **Use Case Name** | Manage Orders |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Pharmacist, Customer |
| **Secondary Actor** | - |
| **Description** | Create, view, and cancel orders with automatic discount application. Customer scope is restricted to their own orders only (view, cancel own pending orders). |
| **Trigger** | User initiates new order or views existing order. |
| **Precondition** | PRE-1: Customer is selected (staff) or logged in (online). PRE-2: Customer can only access their own orders. |
| **Post-Condition** | POST-1: Order is created with correct totals. |
| **Priority** | High |

**Main Flow (Create Order):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Opens Create Order screen. |
| 2 | System | Loads customer picker and medicine search. |
| 3 | User | Selects customer. |
| 4 | User | Searches and adds medicine lines (qty, batch auto-picked FIFO). |
| 5 | System | Calculates line subtotal. |
| 6 | System | Applies BR04 (5% discount when qty ≥ 10 same medicine). |
| 7 | System | Computes grand total. |
| 8 | User | Clicks "Place Order". |
| 9 | System | Validates stock availability per line. |
| 10 | System | Generates order number (ORD-yyyymmdd-####). |
| 11 | System | Persists order in PENDING_PAYMENT status. |
| 12 | System | Returns order detail with MSG18. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 11 | 1 | System | NSF-01 cancels order after 24h in PENDING_PAYMENT and emits MSG19 (UC13). |
| AT2 | Step 8 | 1 | User | Clicks Cancel → if status ≠ COMPLETED, restore stock (BR06) and mark CANCELLED. |
| AT3 | Step 9 | 1 | System | Stock conflict → display MSG20 with available qty, allow re-quote. |
| AT4 | Step 4 | 1 | User | Enters coupon code → validate → apply discount on top of BR04. |

**Screen Mockup — SCR-ORDER-NEW:**

```
+----------------------------------------------------------+
|  Create Order                              Order #____   |
+----------------------------------------------------------+
| Customer: [Search customer... ___________]               |
+----------------------------------------------------------+
| Medicine         | Qty | Unit Price | Subtotal |  X      |
|------------------|-----|------------|----------|--------|
| Paracetamol      |  10 |  5,000     | 47,500 * |  [x]   |
| Amoxicillin (Rx) |   2 | 25,000     | 50,000   |  [x]   |
+----------------------------------------------------------+
| Coupon: [_______]   [Apply]                              |
|                                              Subtotal: 97,500
|                                              Discount: -2,500
|                                              TOTAL:    95,000 VND
+----------------------------------------------------------+
| [Cancel]                                  [Place Order]  |
+----------------------------------------------------------+
(* 5% bulk discount applied)
```

**Screen Definition Table — SCR-ORDER-NEW:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| customer_id | select/autocomplete | Yes | - | Customer reference |
| medicine_id | select/autocomplete | Yes | - | Medicine reference |
| qty | number | Yes | - | Integer > 0 |
| unit_price | number | - | - | Auto-filled |
| subtotal | number | - | - | Auto-calculated |
| coupon_code | text | No | 30 | Optional discount code |
| discount_amount | number | - | - | Auto (BR04 + coupon) |
| total | number | - | - | Final payable |
| btn_place_order | button | - | - | Submit |
| btn_cancel | button | - | - | Discard |

**Diagrams:** [Screenflow](./diagrams/uc-06/uc-06-screenflow.puml) · [State](./diagrams/uc-06/uc-06-statediagram.puml) · [Sequence](./diagrams/uc-06/uc-06-sequence.puml) · [Backend](./diagrams/uc-06/uc-06-class-backend.puml) · [Frontend](./diagrams/uc-06/uc-06-class-frontend.puml)

---

##### UC07 - Process Payment

**Use Case Diagram:** [UC07 - Process Payment](./diagrams/uc-07/uc-07-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC07 |
| **Use Case Name** | Process Payment |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Pharmacist |
| **Secondary Actor** | Payment Gateway |
| **Description** | Process payment with multiple methods (cash, card, QR) and generate invoice. |
| **Trigger** | Customer confirms payment. |
| **Precondition** | PRE-1: Order is created and total is calculated. |
| **Post-Condition** | POST-1: Payment is recorded and invoice is generated. |
| **Priority** | High |

**Main Flow (Cash Payment):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Opens Payment screen for an order. |
| 2 | System | Displays order total and method selector. |
| 3 | User | Selects "Cash" and enters amount tendered. |
| 4 | System | Calculates change = tendered − total. |
| 5 | User | Confirms payment. |
| 6 | System | Records payment, updates order to PAID. |
| 7 | System | Generates invoice number and prints receipt. |
| 8 | System | Decrements stock FIFO (NSF-05). |
| 9 | System | Awards loyalty points (NSF-04, BR07). |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 3 | 1 | User | Selects Card → POS terminal flow → record txn ref. |
| AT2 | Step 3 | 1 | User | Selects QR → show QR → wait for gateway callback. |
| AT3 | Step 4 | 1 | System | If tendered < total → block with MSG21. |
| AT4 | Step 6 | 1 | System | Gateway no response within 60 s → MSG22; allow manual reconciliation. |

**Screen Mockup — SCR-PAYMENT:**

```
+----------------------------------------------------------+
|  Payment — Order ORD-20260612-0042                       |
+----------------------------------------------------------+
| Total:        95,000 VND                                 |
| Method:       ( ) Cash  ( ) Card  ( ) QR                 |
|----------------------------------------------------------|
| Tendered:     [ 100,000 ]                                |
| Change:          5,000 VND                                |
|----------------------------------------------------------|
| Reference:    [____________] (auto for Card/QR)          |
+----------------------------------------------------------+
| [Cancel]                                  [Confirm Pay]  |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-PAYMENT:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| method | radio | Yes | - | cash / card / qr |
| amount_tendered | number | Conditional | - | Required for cash |
| change | number | - | - | Auto-calculated |
| txn_ref | text | Conditional | 50 | Required for card/qr |
| btn_confirm | button | - | - | Submit |
| btn_cancel | button | - | - | Abort |

**Diagrams:** [Screenflow](./diagrams/uc-07/uc-07-screenflow.puml) · [State](./diagrams/uc-07/uc-07-statediagram.puml) · [Sequence](./diagrams/uc-07/uc-07-sequence.puml) · [Backend](./diagrams/uc-07/uc-07-class-backend.puml) · [Frontend](./diagrams/uc-07/uc-07-class-frontend.puml)

---

#### 3.2.4 Customer & Reporting

##### UC08 - Manage Customers

**Use Case Diagram:** [UC08 - Manage Customers](./diagrams/uc-08/uc-08-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC08 |
| **Use Case Name** | Customer Profile & Loyalty |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Customer, Pharmacist |
| **Secondary Actor** | - |
| **Description** | Customer profile management, loyalty points tracking, and purchase history. Pharmacist can view all customers; Customer can only manage their own profile. |
| **Trigger** | User clicks Add/Edit/View Customer. |
| **Precondition** | PRE-1: User has appropriate permissions. |
| **Post-Condition** | POST-1: Customer data is updated. |
| **Priority** | Medium |

**Main Flow (Create Customer):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Opens Customer module. |
| 2 | System | Lists customers with search and filter. |
| 3 | User | Clicks "Add Customer". |
| 4 | System | Opens customer form. |
| 5 | User | Fills name, phone, email, address, DOB, gender. |
| 6 | User | Clicks "Save". |
| 7 | System | Validates unique phone/email; phone is required. |
| 8 | System | Generates customer code CUST-yyyy####. |
| 9 | System | Persists record; awards welcome points. |
| 10 | System | Shows success MSG23. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | User | Clicks Edit → update profile; preserve code and points. |
| AT2 | Step 1 | 1 | User | Clicks "History" → show past orders with totals and points. |
| AT3 | Step 7 | 1 | System | Phone already exists → display MSG24; offer to open existing. |

**Screen Mockup — SCR-CUST-LIST:**

```
+----------------------------------------------------------+
|  Customer Management                  [+ Add Customer]   |
+----------------------------------------------------------+
| [Search ___]                                              |
+----------------------------------------------------------+
| Code      | Name    | Phone     | Points | Joined   | Act |
|-----------|---------|-----------|--------|----------|-----|
| CUST-0001 | Nguyen V| 0901...   | 1,250  | 2025-12  | [E] |
| CUST-0002 | Tran T  | 0902...   |   340  | 2026-03  | [E] |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-CUST-LIST / SCR-CUST-FORM:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| code | text | - | 20 | Auto-generated |
| name | text | Yes | 100 | Full name |
| phone | tel | Yes | 20 | Unique contact phone |
| email | email | No | 100 | Optional, unique |
| address | textarea | No | 255 | |
| dob | date | No | - | Date of birth |
| gender | select | No | - | MALE / FEMALE / OTHER |
| points | number | - | - | Read-only loyalty points |
| btn_save / btn_cancel | button | - | - | Form actions |

**Diagrams:** [Screenflow](./diagrams/uc-08/uc-08-screenflow.puml) · [State](./diagrams/uc-08/uc-08-statediagram.puml) · [Sequence](./diagrams/uc-08/uc-08-sequence.puml) · [Backend](./diagrams/uc-08/uc-08-class-backend.puml) · [Frontend](./diagrams/uc-08/uc-08-class-frontend.puml)

---

##### UC09 - View Reports

**Use Case Diagram:** [UC09 - View Reports](./diagrams/uc-09/uc-09-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC09 |
| **Use Case Name** | View Reports |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Admin, CEO, Branch Manager |
| **Secondary Actor** | - |
| **Description** | Generate and export revenue, inventory, and staff performance reports. |
| **Trigger** | User selects report type and filters. |
| **Precondition** | PRE-1: User has REPORT_VIEW permission. |
| **Post-Condition** | POST-1: Report is generated and displayed. |
| **Priority** | Medium |

**Main Flow (Revenue Report):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Opens Reports module. |
| 2 | System | Lists report types (Revenue, Inventory, Staff). |
| 3 | User | Selects "Revenue" and picks date range, branch, group-by. |
| 4 | User | Clicks "Generate". |
| 5 | System | Aggregates data and renders chart + table. |
| 6 | User | Clicks "Export Excel" or "Export PDF". |
| 7 | System | Builds file and triggers download. |
| 8 | System | Logs export audit record. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | User | Clicks "Schedule" → set recurrence → email on run. |
| AT2 | Step 5 | 1 | System | Empty result → show MSG25 with empty-state graphic. |
| AT3 | Step 7 | 1 | System | Build error → show MSG26, offer retry. |

**Screen Mockup — SCR-REPORT:**

```
+----------------------------------------------------------+
|  Reports                                                  |
+----------------------------------------------------------+
| Type: [Revenue v]  From: [2026-06-01] To: [2026-06-12]  |
| Branch: [All v]   Group by: [Day v]    [Generate]        |
+----------------------------------------------------------+
|  [Bar chart: revenue per day]                             |
+----------------------------------------------------------+
| Date       | Orders | Gross   | Discount | Net           |
|------------|--------|---------|----------|---------------|
| 2026-06-12 |   42   | 8,500K  | 250K     | 8,250K        |
| 2026-06-11 |   37   | 7,900K  | 220K     | 7,680K        |
+----------------------------------------------------------+
| [Export Excel]  [Export PDF]  [Schedule]                  |
+----------------------------------------------------------+
```

**Screen Definition Table — SCR-REPORT:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| report_type | select | Yes | - | revenue / inventory / staff |
| from_date | date | Yes | - | Range start |
| to_date | date | Yes | - | Range end (≥ from) |
| branch_id | select | No | - | Branch filter |
| group_by | select | No | - | day / week / month |
| chart | chart | - | - | Visual block |
| table | table | - | - | Detailed rows |
| btn_export_excel | button | - | - | Download .xlsx |
| btn_export_pdf | button | - | - | Download .pdf |
| btn_schedule | button | - | - | Open schedule |

**Diagrams:** [Screenflow](./diagrams/uc-09/uc-09-screenflow.puml) · [State](./diagrams/uc-09/uc-09-statediagram.puml) · [Sequence](./diagrams/uc-09/uc-09-sequence.puml) · [Backend](./diagrams/uc-09/uc-09-class-backend.puml) · [Frontend](./diagrams/uc-09/uc-09-class-frontend.puml)

---

#### 3.2.5 Additional Features

##### UC10 - Search Medicines

**Use Case Diagram:** [UC10 - Search Medicines](./diagrams/uc-10/uc-10-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC10 |
| **Use Case Name** | Search Medicines |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | All Actors |
| **Secondary Actor** | - |
| **Description** | Search and filter medicines by name, category, price, and stock status. |
| **Trigger** | User enters search query or applies filters. |
| **Precondition** | PRE-1: User has system access. |
| **Post-Condition** | POST-1: Matching medicines are displayed. |
| **Priority** | High |

**Main Flow:**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | User | Types into global search bar. |
| 2 | System | Debounces 300 ms, then queries backend. |
| 3 | System | Returns paginated results with relevance score. |
| 4 | User | Applies filter (category, price range, in-stock). |
| 5 | System | Re-queries with combined criteria. |
| 6 | User | Clicks a result. |
| 7 | System | Navigates to detail view (or to order line for staff). |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 3 | 1 | System | No result → display MSG27. |
| AT2 | Step 1 | 1 | System | Suggest top 5 medicines as user types (autocomplete). |

**Screen Definition Table — SCR-SEARCH:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| query | text | No | 100 | Free-text |
| category_id | select | No | - | Category filter |
| price_min | number | No | - | Inclusive min |
| price_max | number | No | - | Inclusive max |
| in_stock | checkbox | No | - | Stock > 0 only |
| result_list | list | - | - | Medicine cards |

**Diagrams:** [Screenflow](./diagrams/uc-10/uc-10-screenflow.puml) · [State](./diagrams/uc-10/uc-10-statediagram.puml) · [Sequence](./diagrams/uc-10/uc-10-sequence.puml) · [Backend](./diagrams/uc-10/uc-10-class-backend.puml) · [Frontend](./diagrams/uc-10/uc-10-class-frontend.puml)

---

##### UC11 - Manage Suppliers

**Use Case Diagram:** [UC11 - Manage Suppliers](./diagrams/uc-11/uc-11-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC11 |
| **Use Case Name** | Manage Suppliers |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Admin, CEO |
| **Secondary Actor** | - |
| **Description** | CRUD operations for medicine suppliers. |
| **Trigger** | Admin/CEO clicks Add/Edit/Delete Supplier. |
| **Precondition** | PRE-1: Admin is authenticated. |
| **Post-Condition** | POST-1: Supplier data is updated. |
| **Priority** | Low |

**Main Flow (Create Supplier):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Admin | Opens Supplier Management. |
| 2 | System | Lists suppliers. |
| 3 | Admin | Clicks "Add Supplier". |
| 4 | System | Opens supplier form. |
| 5 | Admin | Fills name, tax code, contact, address, phone, email, bank info. |
| 6 | Admin | Clicks "Save". |
| 7 | System | Validates unique tax code, valid email/phone. |
| 8 | System | Persists record; shows MSG28. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Admin | Clicks Edit → load data → save update. |
| AT2 | Step 1 | 1 | Admin | Clicks Delete → confirm → soft-delete (status=INACTIVE). |

**Screen Definition Table — SCR-SUPPLIER:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| name | text | Yes | 150 | Supplier name |
| tax_code | text | Yes | 20 | Unique tax code |
| contact_person | text | No | 100 | |
| phone | tel | Yes | 20 | |
| email | email | No | 100 | |
| address | textarea | No | 255 | |
| bank_name | text | No | 100 | |
| bank_account | text | No | 30 | |
| status | toggle | - | - | active / inactive |

**Diagrams:** [Screenflow](./diagrams/uc-11/uc-11-screenflow.puml) · [State](./diagrams/uc-11/uc-11-statediagram.puml) · [Sequence](./diagrams/uc-11/uc-11-sequence.puml) · [Backend](./diagrams/uc-11/uc-11-class-backend.puml) · [Frontend](./diagrams/uc-11/uc-11-class-frontend.puml)

---

##### UC12 - Issue Prescription

**Use Case Diagram:** [UC12 - Issue Prescription](./diagrams/uc-12/uc-12-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC12 |
| **Use Case Name** | Issue Prescription |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | Pharmacist |
| **Secondary Actor** | - |
| **Description** | Create prescriptions with medicines and dosage instructions. |
| **Trigger** | Pharmacist creates new prescription. |
| **Precondition** | PRE-1: Pharmacist is authenticated. PRE-2: Pharmacist has valid license. |
| **Post-Condition** | POST-1: Prescription is created with unique code. |
| **Priority** | Medium |

**Main Flow:**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Pharmacist | Opens Prescription module. |
| 2 | System | Lists prescriptions. |
| 3 | Pharmacist | Clicks "New Prescription". |
| 4 | System | Opens Rx form (patient, diagnosis, medicine lines, dosage, notes). |
| 5 | Pharmacist | Fills form; adds N medicine lines with dosage and duration. |
| 6 | Pharmacist | Clicks "Save & Sign". |
| 7 | System | Validates license, mandatory patient, ≥ 1 medicine line. |
| 8 | System | Generates prescription code RX-yyyy####. |
| 9 | System | Stores record with digital signature hash. |
| 10 | System | Allows print and link to a new order (UC06). |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 6 | 1 | Pharmacist | Clicks "Save Draft" → status DRAFT, no signature yet. |
| AT2 | Step 7 | 1 | System | Missing license/patient/line → display MSG33 with field highlight. |

**Screen Definition Table — SCR-RX:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| patient_id | select | Yes | - | Customer reference |
| diagnosis | textarea | Yes | 500 | Clinical notes |
| medicine_id | select | Yes | - | Per line |
| dosage | text | Yes | 100 | e.g. "1 tab x 3/day" |
| duration_days | number | No | - | Length of treatment |
| notes | textarea | No | 500 | |
| license_no | text | - | 30 | Auto-filled |
| digital_signature | hidden | - | - | Server-side hash |

**Diagrams:** [Screenflow](./diagrams/uc-12/uc-12-screenflow.puml) · [State](./diagrams/uc-12/uc-12-statediagram.puml) · [Sequence](./diagrams/uc-12/uc-12-sequence.puml) · [Backend](./diagrams/uc-12/uc-12-class-backend.puml) · [Frontend](./diagrams/uc-12/uc-12-class-frontend.puml)

---

##### UC13 - Notifications

**Use Case Diagram:** [UC13 - Notifications](./diagrams/uc-13/uc-13-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC13 |
| **Use Case Name** | Notifications |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-05-24 |
| **Primary Actor** | System, Admin, CEO |
| **Secondary Actor** | - |
| **Description** | Send and manage system notifications for alerts and updates. |
| **Trigger** | System event or Admin/CEO action. |
| **Precondition** | PRE-1: Notification settings are configured. |
| **Post-Condition** | POST-1: Notification is sent or stored. |
| **Priority** | Low |

**Main Flow (Low Stock Alert):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | System | Detects stock < min_level (after UC05 transaction). |
| 2 | System | Builds notification payload per template (NTPL-LOW-STOCK). |
| 3 | System | Resolves recipients per branch (warehouse manager). |
| 4 | System | Sends via enabled channels (in-app, email, SMS). |
| 5 | System | Persists notification record with status SENT/FAILED. |
| 6 | User (Admin) | Views notifications list, marks as read. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Admin | Composes announcement → select audience and channel. |
| AT2 | Step 4 | 1 | System | SMS/email fails → retry 3x with backoff (NSF-09); persist FAILED. |

**Screen Definition Table — SCR-NOTIF:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| audience | multi-select | Yes | - | Roles / users / branches |
| channel | multi-select | Yes | - | in-app / email / sms |
| template | select | No | - | Predefined templates |
| title | text | Yes | 150 | |
| body | textarea | Yes | 1000 | |
| send_at | datetime | No | - | Scheduled send |

**Diagrams:** [Screenflow](./diagrams/uc-13/uc-13-screenflow.puml) · [State](./diagrams/uc-13/uc-13-statediagram.puml) · [Sequence](./diagrams/uc-13/uc-13-sequence.puml) · [Backend](./diagrams/uc-13/uc-13-class-backend.puml) · [Frontend](./diagrams/uc-13/uc-13-class-frontend.puml)

---

#### 3.2.6 Customer Portal (UC14) — NEW v1.4.0

##### UC14 - Customer Portal (B2C E-commerce + Tra cứu + Tài khoản)

**Use Case Diagram:** [UC14 - Customer Portal](./diagrams/uc-14-customer-portal/uc-14-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC14 |
| **Use Case Name** | Customer Portal (B2C) |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Primary Actor** | Customer, Guest |
| **Secondary Actor** | System, Payment Gateway, AI Engine (for UC15 integration) |
| **Description** | Customer-facing web portal: e-commerce (mua sắm, giỏ, checkout, theo dõi đơn), tra cứu (thuốc, dược chất, dược liệu, tra chính hãng), tìm nhà thuốc, đặt lịch tiêm chủng, hồ sơ cá nhân, Ví Khỏe Nhà Ta, Tài khoản gia đình, lịch sử đơn thuốc. Guest (chưa đăng nhập) có thể duyệt catalog + tra cứu. |
| **Trigger** | Khách truy cập website hoặc mở app; thêm vào giỏ; checkout; upload đơn thuốc; đặt lịch. |
| **Precondition** | PRE-1: Hệ thống backend hoạt động; PRE-2: Khách có kết nối internet; PRE-3: Cho mua hàng: khách đã đăng ký và xác thực SĐT. |
| **Post-Condition** | POST-1: Đơn hàng được tạo + thanh toán + theo dõi; POST-2: Đơn thuốc upload được AI OCR xử lý; POST-3: Lịch tiêm được xác nhận. |
| **Priority** | High |

**Sub-flows (52 màn hình — tham khảo §3.1.2):**

| # | Sub-flow | Screens liên quan | Actors |
|:--|:---------|:------------------|:-------|
| SF14.1 | Duyệt catalog | SHOP-HOME, SHOP-CAT-1, SHOP-CAT-2, SHOP-PDP, SHOP-SEARCH | Customer, Guest |
| SF14.2 | Mua hàng | SHOP-CART, SHOP-CHECKOUT, SHOP-ORDER-HISTORY, SHOP-ORDER-TRACK | Customer |
| SF14.3 | Đặt thuốc theo toa | SHOP-RX-UPLOAD (+ AI-RX-OCR) | Customer, Pharmacist, AI |
| SF14.4 | Trả góp | SHOP-INSTALLMENT | Customer |
| SF14.5 | Tra cứu thuốc | SHOP-LOOKUP-DRUG, SHOP-LOOKUP-INGREDIENT, SHOP-LOOKUP-HERB, SHOP-VERIFY-ORIGIN | Customer, Guest, AI |
| SF14.6 | Đọc bài viết sức khoẻ | SHOP-HEALTH-ARTICLE, SHOP-DISEASE-INFO, SHOP-CANCER-INFO, SHOP-VIDEO | Customer, Guest |
| SF14.7 | Tìm nhà thuốc | STORE-LOCATOR, STORE-LIST-PROVINCE, STORE-DETAIL, STORE-CONSULT | Customer, Guest |
| SF14.8 | Tiêm chủng | VACCINE-HOME, VACCINE-BOOKING, VACCINE-LEDGER | Customer |
| SF14.9 | Quản lý tài khoản | CUST-LOGIN, CUST-PROFILE, CUST-ADDRESS, CUST-FAMILY, CUST-HEALTH-WALLET, CUST-RX-HISTORY, CUST-POINTS, CUST-FAVORITES, CUST-NOTIF-SETTINGS | Customer |

**Main Flow (Mua hàng end-to-end):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Customer | Duyệt catalog (SF14.1), thêm sản phẩm vào giỏ (SHOP-CART). |
| 2 | System | Tính tổng tạm, áp voucher nếu có (FR19.1), tính phí ship theo địa chỉ. |
| 3 | Customer | Chọn địa chỉ giao (CUST-ADDRESS) hoặc tạo mới. |
| 4 | Customer | Chọn phương thức thanh toán: COD / MoMo / ZaloPay / VNPay / Card. |
| 5 | System | Tạo Order với status PENDING_PAYMENT, inventory reserve (NSF-05). |
| 6 | Customer | Hoàn tất thanh toán (redirect tới payment gateway hoặc nhập COD). |
| 7 | System | Cập nhật Order status = PAID, sinh Invoice (NSF-11), trừ tồn thực tế, ghi Loyalty (NSF-04), gửi email + SMS + push. |
| 8 | Customer | Theo dõi đơn (SHOP-ORDER-TRACK) qua timeline. |
| 9 | System | Auto-cancel nếu không thanh toán > 24h (NSF-01). |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Guest | Cố checkout → redirect CUST-LOGIN, giỏ hàng được bảo toàn (FR14.5). |
| AT2 | Step 2 | 1 | System | Phát hiện drug interaction (FR15.7) → hiện warning block, không chặn mua. |
| AT3 | Step 3 | 1 | Customer | Chọn đặt thuốc theo toa (SF14.3) → upload ảnh đơn thuốc → AI OCR (NSF-13) → pharmacist review → đơn tạo tự động. |
| AT4 | Step 4 | 1 | Customer | Chọn trả góp (SF14.4) → redirect Home Credit / FE Credit flow → duyệt khoản vay → quay lại thanh toán. |
| AT5 | Step 7 | 1 | System | Thanh toán online fail → rollback inventory, order vẫn PENDING_PAYMENT cho retry. |
| AT6 | Step 8 | 1 | Customer | Sau 3 ngày chưa nhận → "Báo cáo vấn đề" → escalation Pharmacist (UC16). |

**Screen Definition Table — SHOP-* (B2C representative):**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| search_query | text | No | 200 | Full-text + AI semantic |
| category_filter | multi-select | No | - | |
| price_range | range | No | - | min, max VND |
| sort_by | select | No | - | Bán chạy / Giá thấp / Giá cao / Mới |
| delivery_address | select | Yes | - | FK→customer_addresses |
| payment_method | radio | Yes | - | COD / MOMO / ZALOPAY / VNPAY / CARD |
| voucher_code | text | No | 30 | Auto-validate (FR19.2) |
| prescription_image | file | No | 10MB | JPG/PNG/PDF |
| review_rating | radio | No | - | 1-5 stars (FR19.5) |
| review_body | textarea | No | 1000 | |

**Diagrams:** [Screenflow](./diagrams/uc-14-customer-portal/uc-14-screenflow.puml) · [State](./diagrams/uc-14-customer-portal/uc-14-statediagram.puml) · [Sequence](./diagrams/uc-14-customer-portal/uc-14-sequence.puml) · [Backend](./diagrams/uc-14-customer-portal/uc-14-class-backend.puml) · [Frontend](./diagrams/uc-14-customer-portal/uc-14-class-frontend.puml)

---

#### 3.2.7 AI Features (UC15) — NEW v1.4.0

##### UC15 - AI-Powered Assistance

**Use Case Diagram:** [UC15 - AI Features](./diagrams/uc-15-ai/uc-15-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC15 |
| **Use Case Name** | AI-Powered Assistance |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Primary Actor** | Customer, Pharmacist |
| **Secondary Actor** | AI Engine, System |
| **Description** | 12 tính năng AI hỗ trợ khách hàng và dược sĩ: (1) AI tư vấn 24/7, (2) OCR đơn thuốc, (3) Drug interaction check, (4) Cross-sell suggestions, (5) Semantic search, (6) Demand forecasting, (7) Anomaly detection, (8) Medical record summary, (9) Sales trend analysis, (10) CSKH chatbot, (11) Re-examination reminder, (12) Dosage check by age/weight. |
| **Trigger** | User gửi tin nhắn chat; upload ảnh đơn thuốc; thêm thuốc vào đơn; cron schedule. |
| **Precondition** | PRE-1: Customer có `consent_ai = true` (FR15.19); PRE-2: AI Engine service available. |
| **Post-Condition** | POST-1: AI trả response kèm confidence score; POST-2: Escalation queue được tạo nếu cần. |
| **Priority** | High |

**Sub-flows (12 tính năng AI):**

| ID | Tính năng | Trigger | Output | Màn hình |
|:--|:----------|:--------|:-------|:---------|
| AI-01 | Tư vấn dược sĩ ảo 24/7 | User chat | Text + citation | CHAT-AI |
| AI-02 | OCR đơn thuốc | Upload ảnh | JSON: thuốc + liều | AI-RX-OCR |
| AI-03 | Drug interaction check | Thêm thuốc vào order | Cảnh báo 4 cấp độ | AI-DRUG-CHECK |
| AI-04 | Cross-sell / Up-sell | Pharmacist mở order | List gợi ý | RX-CROSS-SELL |
| AI-05 | Semantic search | User search "đau đầu" | Top 10 sản phẩm | SHOP-SEARCH |
| AI-06 | Demand forecasting | Cron daily | 30/60/90-day per SKU | (Reports) |
| AI-07 | Anomaly detection | Tạo prescription | Flag + reason | (SCR-RX) |
| AI-08 | Medical record summary | Pharmacist view | TL;DR 200 từ | RX-CUST-PROFILE-360 |
| AI-09 | Sales trend analysis | Admin/CEO view | Chart + insights | (SCR-REPORT) |
| AI-10 | CSKH chatbot tier-1 | Web chat | Auto-reply hoặc escalate | CHAT-AI |
| AI-11 | Re-exam reminder | Cron weekly | Push + email | (CUST-PROFILE) |
| AI-12 | Dosage check (age/weight) | Pharmacist kê đơn | Warning nếu vượt | SCR-RX |

**Main Flow (AI Tư vấn 24/7):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Customer | Mở CHAT-AI, gõ câu hỏi vd: "Tôi bị đau đầu, sốt nhẹ, nên uống gì?" |
| 2 | System | Gửi tới AI Engine: message + context (customer history + consent_ai flag). |
| 3 | AI Engine | RAG retrieval: top-5 sản phẩm/đơn thuốc liên quan từ knowledge base. |
| 4 | AI Engine | LLM generate response + cite sources (FR15.2). |
| 5 | System | Trả response cho customer, log session (FR15.4). |
| 6 | Customer | Follow-up hoặc end. Nếu chọn "Tư vấn dược sĩ" → escalate (FR15.3). |
| 7 | Pharmacist | Nhận escalated session, tiếp tục chat trong 24h (SLA). |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Customer | consent_ai = false → chỉ chatbot rule-based (FAQ), không LLM. |
| AT2 | Step 3 | 1 | AI Engine | Confidence < 0.6 → escalate ngay (FR15.3). |
| AT3 | Step 4 | 1 | AI Engine | Detect emergency keyword ("đau ngực", "khó thở") → escalate + khuyến nghị gọi 115. |
| AT4 | Step 5 | 1 | System | LLM API fail → trả message "Tạm thời AI không khả dụng, để lại SĐT dược sĩ sẽ gọi lại". |

**AI Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│                  AI Engine (separate microservice)           │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ LLM Gateway  │  │ Vector Store │  │ Knowledge Base   │  │
│  │ (OpenAI /    │  │ (Postgres    │  │ - Medicines      │  │
│  │  local Llama)│  │  pgvector /  │  │ - Symptoms       │  │
│  │              │  │  Pinecone)   │  │ - Drug rules     │  │
│  └──────┬───────┘  └──────┬───────┘  │ - Policies       │  │
│         │                 │          └─────────┬────────┘  │
│         └────────┬────────┘                    │           │
│                  ▼                             ▼           │
│          ┌──────────────────────────────────────────┐       │
│          │ RAG Pipeline (LangChain / LlamaIndex)    │       │
│          │ + Guardrails (toxicity, PII redaction)   │       │
│          └──────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

**Screen Definition Table — CHAT-AI:**

| Field Name | Type | Mandatory | Max Length | Description |
| :---- | :---- | :---- | :---- | :---- |
| message | textarea | Yes | 2000 | |
| context_type | radio | No | - | SYMPTOM / DRUG_INFO / INTERACTION / GENERAL |
| attached_image | file | No | 5MB | For AI-RX-OCR |
| escalated | boolean | - | - | Read-only, set by AI |

**Diagrams:** [Screenflow](./diagrams/uc-15-ai/uc-15-screenflow.puml) · [State](./diagrams/uc-15-ai/uc-15-statediagram.puml) · [Sequence](./diagrams/uc-15-ai/uc-15-sequence.puml) · [Backend](./diagrams/uc-15-ai/uc-15-class-backend.puml) · [Frontend](./diagrams/uc-15-ai/uc-15-class-frontend.puml)

---

#### 3.2.8 Pharmacist — Người bạn tại quầy (UC16) — NEW v1.4.0

##### UC16 - Pharmacist Trusted Advisor Tools

**Use Case Diagram:** [UC16 - Pharmacist Tools](./diagrams/uc-16-pharmacist/uc-16-use-case.puml)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC16 |
| **Use Case Name** | Pharmacist — Người bạn tại quầy |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Primary Actor** | Pharmacist |
| **Secondary Actor** | Customer, AI Engine |
| **Description** | Bộ công cụ nâng cao cho dược sĩ — vị trí "người bạn tại quầy" — giúp chăm sóc khách hàng toàn diện: tư vấn 1-1 (text/voice/video), hồ sơ khách 360°, cảnh báo dị ứng chéo, gợi ý cross-sell thông minh, đề xuất generic thay thế, follow-up sau bán (3/7/14 ngày), đánh dấu VIP, gợi ý chế độ ăn/uống, lịch tái khám, chấm điểm trung thành. |
| **Trigger** | Pharmacist mở order/profile khách; AI escalation; auto-cron theo dõi follow-up. |
| **Precondition** | PRE-1: Pharmacist đã đăng nhập; PRE-2: Customer có hồ sơ hoặc order đang xử lý. |
| **Post-Condition** | POST-1: Tư vấn được log; POST-2: Follow-up tasks được schedule (NSF-19). |
| **Priority** | High |

**Sub-flows (10 tính năng Pharmacist):**

| ID | Tính năng | Màn hình | Mô tả |
|:--|:----------|:---------|:------|
| RX-01 | Tư vấn 1-1 (text/voice/video) | RX-CONSULT | Phiên realtime với khách, có transcript + recording |
| RX-02 | Hồ sơ khách 360° | RX-CUST-PROFILE-360 | Timeline toàn bộ: đơn, dị ứng, bệnh nền, lịch tái khám |
| RX-03 | Cảnh báo dị ứng chéo | (inline) | Khi thêm thuốc vào order: AI check vs Customer.allergies |
| RX-04 | Cross-sell / Up-sell gợi ý | RX-CROSS-SELL | AI suggest sản phẩm bổ sung theo context đơn |
| RX-05 | Đề xuất generic thay thế | (inline) | Hiển thị generic rẻ hơn cùng hoạt chất |
| RX-06 | Follow-up sau bán | RX-FOLLOW-UP | Schedule 3/7/14-day push, nhận feedback |
| RX-07 | Đánh dấu VIP / khách quen | RX-VIP-MARK | Flag + lý do, hiển thị trên tất cả customer list |
| RX-08 | Gợi ý chế độ ăn/uống | (modal) | Theo thuốc + bệnh nền (vd: thuốc tiểu đường + ăn ít đường) |
| RX-09 | Lịch tái khám | (modal) | Suggest re-exam date theo prescription + chronic |
| RX-10 | Chấm điểm trung thành | (badge) | Score 0-100 dựa trên: tần suất, tổng chi, feedback, lịch sử |

**Main Flow (Tư vấn + bán thuốc tại quầy):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Customer | Đến quầy, đưa yêu cầu / toa thuốc. |
| 2 | Pharmacist | Tra cứu khách bằng SĐT → mở RX-CUST-PROFILE-360. |
| 3 | System | Hiển thị: ảnh đại diện, tên, tier, allergies, chronic, lịch sử mua, preferred_pharmacist. |
| 4 | Pharmacist | Hỏi triệu chứng, có thể chat với CHAT-AI (UC15-AI-01) để gợi ý nhanh. |
| 5 | Pharmacist | Tạo Order mới (SCR-ORDER-NEW) hoặc thêm thuốc vào đơn hiện tại. |
| 6 | System | Real-time check: drug-drug interaction (AI-03), allergy cross-check (RX-03), max-dose (AI-12), duplicate ingredient. |
| 7 | System | Nếu có cảnh báo: hiển thị modal với severity + recommendation (FR15.7). |
| 8 | Pharmacist | Quyết định: thay thế / giảm liều / giữ nguyên (với lý do override). |
| 9 | System | AI gợi ý cross-sell (RX-CROSS-SELL) — pharmacist chọn thêm/bỏ. |
| 10 | Pharmacist | Tư vấn chế độ ăn/uống (RX-08) + lịch tái khám (RX-09). |
| 11 | Pharmacist | Thanh toán (SCR-PAYMENT) → in hóa đơn (SCR-INVOICE). |
| 12 | System | Auto-schedule follow-up (NSF-19) ở 3/7/14 ngày. |

**Alternative Flows:**

| ID | Ref Step | Sub step | Actor | Action |
| :---- | :---- | :---- | :---- | :---- |
| AT1 | Step 1 | 1 | Customer | Khách mới chưa có hồ sơ → Pharmacist tạo nhanh (SCR-CUST-FORM, role=Customer). |
| AT2 | Step 6 | 1 | System | Allergy match → block đơn, bắt buộc pharmacist acknowledge (FR16.3). |
| AT3 | Step 8 | 1 | Pharmacist | Không chắc chắn → escalate UC15-AI-01 hoặc gọi dược sĩ cao cấp. |
| AT4 | Step 12 | 1 | Customer | Sau 3 ngày: "Thuốc có hiệu quả không?" → response TAKEN_OK / SIDE_EFFECT / NO_EFFECT → log vào MedicationIntake. |

**Pharmacist Workbench (RX-CONSOLE):**

Một dashboard hợp nhất cho pharmacist, bao gồm:
- Customer 360° panel (bên trái)
- Order builder (giữa)
- AI suggestions + interaction alerts (bên phải)
- Voice/video call control (header)
- Quick-tap common actions (footer)

**Screen Definition Table — RX-CUST-PROFILE-360:**

| Field Name | Type | Mandatory | Description |
| :---- | :---- | :---- | :---- |
| customer_id | UUID | Yes | |
| allergies | multi-select | No | FK→medicines |
| chronic_conditions | string[] | No | |
| current_medications | computed | - | From active orders |
| last_visit_at | timestamp | - | |
| loyalty_tier | enum | - | BRONZE/SILVER/GOLD/PLATINUM |
| loyalty_score | int | - | 0-100 |
| preferred_pharmacist_id | UUID | - | |
| ai_summary | textarea | - | Auto-generated by AI-08 |

**Diagrams:** [Screenflow](./diagrams/uc-16-pharmacist/uc-16-screenflow.puml) · [State](./diagrams/uc-16-pharmacist/uc-16-statediagram.puml) · [Sequence](./diagrams/uc-16-pharmacist/uc-16-sequence.puml) · [Backend](./diagrams/uc-16-pharmacist/uc-16-class-backend.puml) · [Frontend](./diagrams/uc-16-pharmacist/uc-16-class-frontend.puml)

---

#### 3.2.9 Mobile App (UC17) — NEW v1.4.0

##### UC17 - Mobile App (iOS + Android)

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC17 |
| **Use Case Name** | Mobile App |
| **Author** | SRS Agent |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Primary Actor** | Customer, Pharmacist |
| **Secondary Actor** | System, Push Provider (FCM/APNs), AI Engine |
| **Description** | Ứng dụng di động native (iOS + Android) cho khách hàng. Cung cấp: push notification nhắc uống thuốc, GPS tìm nhà thuốc, AI tư vấn 24/7, calendar sync lịch uống thuốc, quét mã vạch tra thuốc, Tài khoản gia đình (nhiều thành viên), Ví Khỏe Nhà Ta, theo dõi đơn realtime, login SMS OTP + VNeID. |
| **Trigger** | Open app; push notification; location change; calendar sync. |
| **Precondition** | PRE-1: iOS 14+ hoặc Android 9+; PRE-2: Customer đăng ký tài khoản. |
| **Post-Condition** | POST-1: Reminder tạo → push scheduled (NSF-23). |
| **Priority** | High |

**Mobile-specific screens (MOBILE-*):** MOBILE-HOME, MOBILE-MED-REMINDER. Xem §3.1.2 Phần B Nhóm 9.

**Main Flow (Nhắc uống thuốc 5 bước):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Customer | Mở MOBILE-HOME → tap "Nhắc uống thuốc" → "Tạo lịch nhắc". |
| 2 | Customer | Chọn 1 trong 2: Chụp ảnh đơn thuốc (AI-RX-OCR) hoặc Nhập tay. |
| 3 | AI Engine / Customer | OCR tự động điền tên thuốc + liều lượng. Hoặc Customer nhập thủ công. |
| 4 | Customer | Kiểm tra thông tin, sửa nếu sai. Thêm ghi chú (trước/sau ăn). |
| 5 | Customer | Đặt giờ nhắc cho từng buổi (sáng/trưa/chiều/tối). |
| 6 | System | Push reminder vào Calendar (iOS EventKit / Android Calendar Provider — NSF-24) + đặt push notification (NSF-23). |
| 7 | Customer | Đến giờ → nhận push → mở app → check TAKEN/SKIPPED. |
| 8 | System | Log MedicationIntake, cập nhật adherence_rate. |

**Diagrams:** [Screenflow](./diagrams/uc-17-mobile/uc-17-screenflow.puml) · [State](./diagrams/uc-17-mobile/uc-17-statediagram.puml) · [Sequence](./diagrams/uc-17-mobile/uc-17-sequence.puml)

---

#### 3.2.10 Health Tools & E-commerce (UC18 + UC19) — NEW v1.4.0

##### UC18 - Health Tools

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC18 |
| **Use Case Name** | Health Tools (Self-service Quizzes) |
| **Primary Actor** | Customer, Guest |
| **Description** | 8+ bài kiểm tra sức khoẻ tự đánh giá: trí nhớ, tiền đái tháo đường, suy giáp, hen, tim mạch, Alzheimer, trào ngược dạ dày, phụ thuộc bình xịt. Guest có thể làm (lead-gen). Kết quả có risk level + lời khuyên + CTA đặt lịch tư vấn. |
| **Priority** | Should |

**Available quizzes (HEALTH-QUIZ-LIST → HEALTH-QUIZ-RESULT):**

| # | Quiz | Risk Levels | CTA |
|:--|:-----|:------------|:----|
| QZ-01 | Trí nhớ & tập trung | LOW / MODERATE / HIGH | Tư vấn dược sĩ + đặt lịch khám |
| QZ-02 | Sàng lọc tiền đái tháo đường | LOW / MODERATE / HIGH | Đặt lịch xét nghiệm đường huyết |
| QZ-03 | Suy giáp | LOW / MODERATE / HIGH | Tư vấn + xét nghiệm TSH |
| QZ-04 | Kiểm soát hen (ACT) | WELL-CONTROLLED / NOT-WELL / POOR | Tư vấn + đặt lịch khám |
| QZ-05 | Nguy cơ tim mạch (Framingham) | LOW / INTERMEDIATE / HIGH | Tư vấn + khám tim mạch |
| QZ-06 | Alzheimer (mini-cog) | NORMAL / IMPAIRED | Tư vấn + khám thần kinh |
| QZ-07 | Trào ngược dạ dày (GERD-Q) | LOW / MODERATE / HIGH | Tư vấn + khám tiêu hóa |
| QZ-08 | Phụ thuộc bình xịt cắt cơn | OK / WARNING / ADDICTED | Cảnh báo + tư vấn cai thuốc |

---

##### UC19 - E-commerce Operations

| Field | Value |
| :---- | :---- |
| **Use Case ID** | UC19 |
| **Use Case Name** | E-commerce Operations (Vouchers, Flash Sales, Reviews, Installments) |
| **Primary Actor** | Customer, Guest, Admin, CEO |
| **Description** | Voucher/coupon, trả góp, đánh giá sản phẩm, ví Khỏe Nhà Ta, chat realtime với dược sĩ, flash sale, combo deals. |
| **Priority** | High |

**Screens:** SHOP-VOUCHER, SHOP-REVIEW, SHOP-LIVE-CHAT, SHOP-FLASH-SALE. Xem §3.1.2 Phần B Nhóm 10.

**Main Flow (Flash Sale):**

| Step | Actor | Action |
| :---- | :---- | :---- |
| 1 | Admin | Tạo FlashSale, set items + qty_limit + sale_price + start/end time. |
| 2 | System | NSF-21 cron activate FlashSale khi đến `starts_at`. |
| 3 | Customer | Tap vào FlashSale widget (real-time countdown). |
| 4 | Customer | Tap "Mua ngay" → atomic stock decrement (PostgreSQL `SELECT ... FOR UPDATE`). |
| 5 | System | Nếu sold out hoặc hết hạn → hide button + show "Đã hết". |
| 6 | Customer | Thêm vào giỏ với sale_price, tiếp tục checkout (UC14). |

---

### 3.3 Functional Requirements

> Functional requirements (FR) are derived from and traceable to the Use Cases in [§3.2](#32-use-case-details). Each FR is associated with its source UC.

#### 3.3.1 Authentication & Authorization (UC01)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR1.1 | System shall authenticate users using email and password | Must |
| FR1.2 | System shall generate JWT token upon successful login (access 15 min, refresh 7 days) | Must |
| FR1.3 | System shall validate password with minimum 8 characters, uppercase, number, special char | Must |
| FR1.4 | System shall implement role-based access control (RBAC) | Must |
| FR1.5 | System shall lock account after 5 failed login attempts for 30 minutes | Should |
| FR1.6 | System shall provide password reset via email verification | Should |

#### 3.3.2 User Management (UC02)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR2.1 | Admin/CEO shall create, update, and delete user accounts | Must |
| FR2.2 | Admin/CEO shall assign roles (Admin, CEO, Branch Manager, Pharmacist, Customer) | Must |
| FR2.3 | Admin/CEO shall assign users to specific branches | Must |
| FR2.4 | System shall support 5 user roles (Admin, CEO, Branch Manager, Pharmacist, Customer) | Must |
| FR2.5 | System shall soft-delete users (status = INACTIVE) to preserve audit trail | Should |

#### 3.3.3 Branch Management (UC03)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR3.1 | Admin/CEO shall create and manage pharmacy branches | Must |
| FR3.2 | Admin/CEO shall assign branch managers | Must |
| FR3.3 | System shall track branch performance | Should |

#### 3.3.4 Medicine Management (UC04)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR4.1 | Admin/Pharmacist shall manage medicine catalog | Must |
| FR4.2 | System shall support category-based classification | Must |
| FR4.3 | System shall track medicine expiry dates | Must |
| FR4.4 | System shall support prescription-required flag | Must |
| FR4.5 | System shall support image upload (PNG/JPG ≤ 2 MB) | Should |

#### 3.3.5 Inventory Management (UC05)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR5.1 | Branch Manager shall import stock with batch number | Must |
| FR5.2 | Branch Manager shall export stock with reason | Must |
| FR5.3 | System shall support inter-branch transfers | Must |
| FR5.4 | System shall alert when stock below minimum level | Must |
| FR5.5 | System shall alert for medicines expiring within 30 days | Must |
| FR5.6 | System shall apply 5% discount for quantity >= 10 same medicine | Must |
| FR5.7 | System shall consume stock FIFO by batch expiry | Should |

#### 3.3.6 Order Management (UC06)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR6.1 | Staff/Customer shall create orders | Must |
| FR6.2 | System shall auto-generate order number ORD-yyyymmdd-#### | Must |
| FR6.3 | System shall auto-cancel unpaid orders after 24 hours | Must |
| FR6.4 | System shall apply price discount rules (BR04) | Must |
| FR6.5 | Branch Manager shall approve/reject orders | Should |
| FR6.6 | System shall restore stock on cancel | Must |

#### 3.3.7 Payment Processing (UC07)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR7.1 | System shall support cash payment with change calculation | Must |
| FR7.2 | System shall support card payment | Should |
| FR7.3 | System shall support QR payment | Should |
| FR7.4 | System shall generate invoice for each successful payment | Must |
| FR7.5 | System shall print invoice | Must |
| FR7.6 | System shall award loyalty points per paid order (1 pt / 1,000 VND) | Should |

#### 3.3.8 Customer Management (UC08)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR8.1 | System shall register customers with phone/email | Must |
| FR8.2 | System shall generate unique customer code CUST-yyyy#### | Must |
| FR8.3 | System shall track customer purchase history | Should |
| FR8.4 | Customer shall view and update profile | Should |

#### 3.3.9 Reporting (UC09)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR9.1 | Admin/Manager shall view revenue reports by date range | Must |
| FR9.2 | Admin/Manager shall view inventory reports | Must |
| FR9.3 | System shall export reports to Excel | Should |
| FR9.4 | System shall export reports to PDF | Should |
| FR9.5 | System shall support scheduled report delivery by email | Could |

#### 3.3.10 Search (UC10)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR10.1 | System shall search medicines by name (with autocomplete) | Must |
| FR10.2 | System shall filter by category | Must |
| FR10.3 | System shall filter by price range | Should |
| FR10.4 | System shall filter by stock availability | Should |

#### 3.3.11 Supplier Management (UC11)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR11.1 | Admin/CEO shall manage supplier records | Should |
| FR11.2 | System shall track supply history per supplier | Should |

#### 3.3.12 Prescription (UC12)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR12.1 | Pharmacist shall create prescriptions with medicines | Should |
| FR12.2 | System shall generate unique prescription code RX-yyyy#### | Should |
| FR12.3 | System shall link prescription to order | Should |
| FR12.4 | System shall print prescription | Should |

#### 3.3.13 Notifications (UC13)

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR13.1 | System shall send low stock alerts | Should |
| FR13.2 | System shall send expiry date alerts (30 days ahead) | Should |
| FR13.3 | System shall support in-app, email and SMS channels | Should |
| FR13.4 | Admin/CEO shall configure notification settings | Should |

#### 3.3.14 Customer Portal (UC14) — NEW v1.4.0

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR14.1 | System shall display product catalog with category filter, sort, search | Must |
| FR14.2 | System shall display product detail page with images, description, ingredients, reviews | Must |
| FR14.3 | Guest shall be able to browse catalog, search, view PDP without login | Must |
| FR14.4 | Customer shall add/update/remove items in cart | Must |
| FR14.5 | System shall support cart multi-vendor (multiple sellers) and persistent cart | Should |
| FR14.6 | Customer shall checkout with 4 steps: address → shipping → payment → confirm | Must |
| FR14.7 | System shall support COD, MoMo, ZaloPay, VNPay, credit/debit card | Must |
| FR14.8 | System shall send order confirmation via email + SMS + push | Must |
| FR14.9 | System shall provide real-time order tracking with status timeline | Must |
| FR14.10 | Customer shall upload prescription image (JPG/PNG/PDF, max 10MB) | Must |
| FR14.11 | System shall process uploaded prescription via AI OCR (NSF-13) | Must |
| FR14.12 | Pharmacist shall review and approve/reject uploaded prescriptions | Must |
| FR14.13 | System shall provide drug lookup by name/ingredient/herb with A-Z filter | Must |
| FR14.14 | System shall support QR/barcode scan to verify product authenticity | Should |
| FR14.15 | System shall display pharmacy locations on map with GPS (NSF-22) | Must |
| FR14.16 | Customer shall book pharmacist consultation slot (in-store or video) | Must |
| FR14.17 | System shall support installment purchase (Home Credit, FE Credit) | Should |
| FR14.18 | System shall display vaccine catalog and allow booking | Should |
| FR14.19 | System shall maintain digital vaccination ledger per customer | Should |
| FR14.20 | System shall provide customer login via SMS OTP + VNeID (from 2025) | Must |
| FR14.21 | System shall support customer profile, address book (multi-address) | Must |
| FR14.22 | System shall support Tài khoản gia đình (link multiple family members) | Should |
| FR14.23 | System shall maintain Ví Khỏe Nhà Ta (Health Wallet) with tier system | Should |
| FR14.24 | System shall display customer's full prescription history with re-download | Must |
| FR14.25 | System shall provide favorites list | Should |
| FR14.26 | System shall respect customer notification preferences (push, email, SMS) | Must |

#### 3.3.15 AI Features (UC15) — NEW v1.4.0

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR15.1 | System shall provide AI Pharmacist Chatbot available 24/7 | Must |
| FR15.2 | AI chatbot shall use RAG over PCMS knowledge base (medicines, symptoms, policies) | Must |
| FR15.3 | AI chatbot shall escalate complex queries to human pharmacist | Must |
| FR15.4 | AI chatbot shall log all sessions for quality + audit (BR-AI-01) | Must |
| FR15.5 | System shall process prescription image via OCR with ≥95% accuracy target | Must |
| FR15.6 | OCR result shall be reviewed by pharmacist before order creation | Must |
| FR15.7 | System shall detect drug-drug interactions from 4 severity levels (NSF-15) | Must |
| FR15.8 | System shall display interaction warning in cart, order, prescription | Must |
| FR15.9 | System shall support AI semantic search (symptom → product) | Should |
| FR15.10 | System shall pre-index product embeddings on catalog change (NSF-14) | Should |
| FR15.11 | System shall forecast 30/60/90-day demand per SKU per branch (NSF-16) | Should |
| FR15.12 | AI forecast shall integrate into inventory alerts (BR02) | Should |
| FR15.13 | System shall detect prescription anomalies (duplicate, max-dose, age mismatch) | Must |
| FR15.14 | System shall summarize medical history for pharmacist review (NSF-18) | Should |
| FR15.15 | System shall analyze sales trends and surface insights to Admin/CEO | Could |
| FR15.16 | AI customer-service chatbot shall handle 80% of tier-1 questions | Should |
| FR15.17 | System shall suggest re-examination date based on prescription + chronic conditions | Should |
| FR15.18 | System shall check dosage against age/weight/condition rules | Must |
| FR15.19 | All AI processing shall respect customer `consent_ai` flag | Must |
| FR15.20 | AI shall log model version + confidence score for every inference (audit) | Must |

#### 3.3.16 Pharmacist — Người bạn tại quầy (UC16) — NEW v1.4.0

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR16.1 | Pharmacist shall conduct 1-on-1 consultation (text, voice, video) with customer | Must |
| FR16.2 | System shall auto-summarize customer history (allergies, chronic, prior purchases) for pharmacist | Must |
| FR16.3 | System shall display cross-allergy alert when pharmacist adds medicine to order | Must |
| FR16.4 | System shall suggest complementary products based on order (cross-sell/up-sell) | Should |
| FR16.5 | System shall suggest cheaper generic alternatives when available | Should |
| FR16.6 | System shall schedule automatic follow-up (3/7/14-day) post-purchase (NSF-19) | Should |
| FR16.7 | Customer shall report treatment effectiveness via push notification | Should |
| FR16.8 | Pharmacist shall mark customer as VIP based on spend/frequency rules | Should |
| FR16.9 | System shall compute "loyalty score" per customer for VIP detection | Could |
| FR16.10 | Pharmacist shall see customer's preferred pharmacist (if set) | Could |
| FR16.11 | System shall suggest diet/lifestyle tips relevant to prescribed medicines | Should |
| FR16.12 | System shall remind pharmacist to schedule customer re-examination | Should |
| FR16.13 | Pharmacist tools shall be available in BOTH web dashboard AND POS | Must |
| FR16.14 | All pharmacist consultations shall be logged for audit + continuity | Must |

#### 3.3.17 Mobile App (UC17) — NEW v1.4.0

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR17.1 | System shall provide iOS + Android native apps | Must |
| FR17.2 | App shall support push notification via FCM (Android) + APNs (iOS) | Must |
| FR17.3 | App shall provide medication reminder with 5-step onboarding | Must |
| FR17.4 | App shall sync reminders with iOS Calendar / Android Calendar (NSF-24) | Should |
| FR17.5 | App shall provide GPS-based pharmacy locator ("near me") | Must |
| FR17.6 | App shall allow barcode/QR scan to verify drug authenticity | Must |
| FR17.7 | App shall provide offline mode for cached product catalog + last orders | Should |
| FR17.8 | App shall support login via SMS OTP + VNeID | Must |
| FR17.9 | App shall support family account management (CRUD members) | Should |
| FR17.10 | App shall provide AI chatbot interface 24/7 | Must |
| FR17.11 | App shall show real-time order status with push notification on change | Must |
| FR17.12 | App shall support in-app payment via MoMo, ZaloPay, VNPay, cards | Must |
| FR17.13 | App shall provide dark mode toggle respecting system setting | Should |
| FR17.14 | App shall respect `prefers-reduced-motion` per platform guideline | Must |
| FR17.15 | App bundle size shall be ≤50MB (Android) / ≤150MB (iOS) | Should |

#### 3.3.18 Health Tools (UC18) — NEW v1.4.0

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR18.1 | System shall provide 8+ self-assessment health quizzes (memory, diabetes, thyroid, asthma, cardiac, alzheimer, gerd, inhaler) | Should |
| FR18.2 | Quizzes shall be accessible to guests (lead-gen) | Should |
| FR18.3 | Quiz results shall include risk level (LOW/MODERATE/HIGH) | Should |
| FR18.4 | Quiz results shall include actionable advice and CTA to book consultation | Should |
| FR18.5 | System shall display seasonal disease alerts (dengue, flu, etc.) | Could |
| FR18.6 | Quizzes shall be reviewed annually by medical content team | Should |
| FR18.7 | Customer's quiz history shall be viewable in profile (with consent) | Could |

#### 3.3.19 E-commerce Operations (UC19) — NEW v1.4.0

| ID | Requirement | Priority |
| :---- | :---- | :---- |
| FR19.1 | System shall support voucher/coupon with PERCENT / FIXED / FREE_SHIP types | Must |
| FR19.2 | System shall enforce voucher usage limit + per-user limit | Must |
| FR19.3 | System shall auto-expire vouchers on schedule (NSF-20) | Must |
| FR19.4 | System shall support flash sale with countdown + atomic stock decrement (NSF-21) | Must |
| FR19.5 | System shall support product reviews (1-5 stars + text + images) | Must |
| FR19.6 | Reviews shall be moderated (auto-AI + manual) before publishing | Should |
| FR19.7 | System shall support installment payment via Home Credit, FE Credit APIs | Should |
| FR19.8 | System shall provide live chat between customer and pharmacist (escalation from AI) | Should |
| FR19.9 | System shall provide combo deals (multi-product bundle pricing) | Should |
| FR19.10 | System shall support abandoned cart recovery via push/email | Should |
| FR19.11 | System shall provide wishlist / favorites with price-drop notification | Should |

### 3.4 Software Features Summary

| # | Feature Group | Use Cases | Description |
| :---- | :---- | :---- | :---- |
| FG1 | Authentication | UC01 | User login, logout, session management |
| FG2 | User Management | UC02 | CRUD for system users with role-based access |
| FG3 | Branch Management | UC03 | Manage pharmacy branches |
| FG4 | Medicine Management | UC04 | Product catalog and pricing |
| FG5 | Inventory Management | UC05 | Stock tracking, transfers, alerts |
| FG6 | Order Management | UC06 | Order creation, processing, cancellation |
| FG7 | Payment Processing | UC07 | Multiple payment methods, invoicing |
| FG8 | Customer Management | UC08 | Customer registration, loyalty points |
| FG9 | Reporting | UC09 | Revenue, inventory, and staff reports |
| FG10 | Search | UC10 | Medicine search with filters |
| FG11 | Supplier Management | UC11 | Supplier CRUD and supply history |
| FG12 | Prescription | UC12 | Prescription creation and management |
| FG13 | Notifications | UC13 | System alerts and messaging |
| **FG14** | **Customer Portal (B2C)** | **UC14** | **E-commerce + tra cứu + tài khoản gia đình + Health Wallet + voucher/flash sale** |
| **FG15** | **AI Features** | **UC15** | **AI chatbot 24/7 + OCR đơn thuốc + drug interaction + semantic search + demand forecasting + anomaly detection** |
| **FG16** | **Pharmacist — Người bạn tại quầy** | **UC16** | **Tư vấn 1-1 + hồ sơ khách 360° + cross-sell + follow-up + VIP marking** |
| **FG17** | **Mobile App** | **UC17** | **iOS/Android app + push notification + GPS + AI chatbot + calendar sync + barcode scan** |
| **FG18** | **Health Tools** | **UC18** | **8+ bài quiz sức khoẻ + cảnh báo dịch + bệnh theo mùa** |
| **FG19** | **E-commerce Operations** | **UC19** | **Voucher + flash sale + đánh giá + trả góp + combo deals + live chat** |

**Tổng kết v1.4.0:**

| Tiêu chí | v1.3.0 | v1.4.0 | Delta |
|:---------|:------:|:------:|:-----:|
| Use Cases (UC) | 13 | **19** | +6 |
| Functional Groups (FG) | 13 | **19** | +6 |
| Màn hình | 28 | **85** | +57 |
| Entities | 12 | **27** | +15 |
| Non-Screen Functions | 12 | **24** | +12 |
| Functional Requirements | ~52 | **~149** | +97 |
| Actors (internal + external) | 9 | **10** (+Guest) | +1 |
| Bề mặt triển khai | 1 (Web) | **3** (Web B2B + Web B2C + Mobile) | +2 |

---

## 4. Non-Functional Requirements

### 4.1 Performance

| Metric | Target | Ideal | Measurement |
| :---- | :---- | :---- | :---- |
| Page load time | < 3 s | < 1 s | P95 at client |
| API response time | < 1 s | < 200 ms | P95 at gateway |
| Search query time | < 2 s | < 500 ms | P95 |
| Order creation time | < 2 s | < 1 s | P95 end-to-end |
| Report generation (≤ 30 days) | < 5 s | < 2 s | P95 |
| Report generation (> 30 days) | < 15 s | < 8 s | P95 with async job |
| Concurrent users | ~500 peak / ~100 avg | - | Load test |
| CPU utilization (peak) | < 70% | < 50% | 5-min average |
| Memory utilization (peak) | < 80% | < 60% | 5-min average |

### 4.2 Scalability

| Metric | Value |
| :---- | :---- |
| Initial branches | 5-10 |
| Staff count | 50-100 |
| Concurrent users (peak) | ~500 |
| Architecture | Microservice |
| Horizontal scaling | Stateless app pods (2-8 replicas per service) |
| Database scaling | Read replicas for reporting |

### 4.3 Availability & Reliability

| Metric | Target | Notes |
| :---- | :---- | :---- |
| SLA | 99.9% | 8.76 h downtime/year, 43.8 min/month |
| MTBF | ≥ 720 h | Mean time between failures |
| MTTR | ≤ 30 min | Mean time to recover (P1 incident) |
| RPO | 24 h | Daily incremental backup |
| RTO | 4 h | Full restore from backup |
| Data accuracy | ≥ 99.99% | Reconciliation between order, payment, inventory |

### 4.4 Usability

| Requirement | Target |
| :---- | :---- |
| Time-to-first-action for new cashier | ≤ 30 min training |
| Steps to create an order | ≤ 6 clicks |
| Error message clarity | Plain language, no stack traces; field-level guidance |
| Language | Vietnamese (default), English (i18n-ready) |
| Accessibility | WCAG 2.1 Level A minimum |
| Browser support | Chrome 100+, Edge 100+, Safari 15+, Firefox 100+ |
| Mobile breakpoints | 360 px, 768 px, 1024 px, 1280 px |

### 4.5 Security

| Requirement | Value |
| :---- | :---- |
| Authentication | Email + Password |
| Password Minimum Length | 8 characters |
| Password Complexity | Uppercase, number, special character |
| 2FA | Not required in v1.2.0 (roadmap v1.3) |
| Transport | HTTPS only (TLS 1.2+) |
| Token | JWT (RS256), access 15 min, refresh 7 days |
| Rate limiting | 100 req/min per IP at gateway |
| Input validation | Mandatory on all endpoints |
| SQL injection prevention | Parameterized queries only |
| XSS prevention | Output encoding + CSP headers |
| Audit log | All write actions on User/Order/Payment/Inventory |
| Session timeout | 30 min inactivity |

### 4.6 External Interfaces

| Interface | Type | Protocol | Notes |
| :---- | :---- | :---- | :---- |
| Web Browser ↔ API Gateway | User | HTTPS / REST + JSON | Primary client channel |
| Mobile Web ↔ API Gateway | User | HTTPS / REST + JSON | Responsive web |
| Payment Gateway (Card/QR) | System | HTTPS / REST | Outbound to bank/gateway; idempotent webhook |
| SMS Provider | System | HTTPS / REST | Outbound OTP/alerts |
| Email Provider (SMTP) | System | SMTP / TLS | Outbound notifications |
| Internal Service-to-Service | System | gRPC or REST over mTLS | East-west traffic |
| Database | System | TCP (driver-specific) | Per-service DB |
| Print (Invoice/Rx) | User | LAN / IPP | Local receipt printer |

### 4.7 Infrastructure & Deployment

| Requirement | Value |
| :---- | :---- |
| Deployment | On-premise |
| Architecture | Microservice |
| Container | Docker |
| Orchestration | Docker Compose (v1.2); Kubernetes (roadmap) |
| Platforms | Web Browser (70%), Mobile Web (30%) |
| Backup | Daily incremental (02:00), weekly full (Sun), monthly archive |
| Monitoring | Prometheus + Grafana; centralized logging via ELK |

### 4.8 Compliance

- Data classification and privacy policies to be defined per Vietnam PDPD (where applicable).
- Audit trail for all critical operations (auth, order, payment, inventory writes).
- Logging must not include passwords, tokens, or PII in plain text.

---

## 5. Requirement Appendix

### 5.1 Business Rules

| Rule ID | Rule Name | Description | Trigger | Action |
| :---- | :---- | :---- | :---- | :---- |
| BR01 | Auto_cancel_order | Auto-cancel unpaid orders | Order unpaid > 24 hours | Cancel + notify customer (MSG19) |
| BR02 | Alert_low_stock | Alert when stock below minimum | Stock < min_stock_level | Alert to warehouse manager (MSG29) |
| BR03 | Batch_expiry_check | Check medicines nearing expiry | Daily at midnight | Alert for items expiring in 30 days (MSG30) |
| BR04 | Price_discount | 5% discount for bulk purchase | Quantity >= 10 same medicine | Apply 5% discount on that line |
| BR05 | Account_lockout | Lock account after failed attempts | 5 failed logins within 15 min | Lock 30 min, show MSG02 |
| BR06 | Stock_restore_on_cancel | Restore stock on order cancel | Order status → CANCELLED | Reverse SALE transactions for that order |
| BR07 | Loyalty_points_award | Award points on paid order | Order status → PAID | 1 point / 1,000 VND of order total |

### 5.2 Common Requirements

| ID | Requirement |
| :---- | :---- |
| CR-01 | All UI text must be externalized in i18n resource bundles. |
| CR-02 | All timestamps stored in UTC; rendered in local timezone (Asia/Ho_Chi_Minh default). |
| CR-03 | All amounts stored in VND as integer (smallest unit) or decimal(15,2); never use float. |
| CR-04 | All list endpoints must support pagination (default page_size = 20, max 100). |
| CR-05 | All write endpoints must be idempotent for retries (Idempotency-Key header). |
| CR-06 | All services must emit structured logs (JSON) with correlation_id. |
| CR-07 | All services must expose health check `/healthz` and readiness `/readyz`. |
| CR-08 | Soft-delete is the default for User, Customer, Supplier, Medicine. |
| CR-09 | Audit log fields (created_at, created_by, updated_at, updated_by) are mandatory on all business tables. |
| CR-10 | API versioning: URL prefix `/api/v1`. Breaking changes require `/api/v2`. |

### 5.3 Application Messages List

> Standardized message catalog referenced from Use Case flows. Codes are stable; content is i18n-ready.

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

### 5.4 Other Requirements

- **Internationalization:** Default `vi-VN`; bundle must include `en-US` keys for all MSG codes.
- **Time zone:** All scheduling and date math must use `Asia/Ho_Chi_Minh`; storage in UTC.
- **Currency:** VND only in v1.2.0; no FX conversion.
- **Hardware minimum (per branch):** POS terminal 4 GB RAM / 64 GB SSD, 80 mm thermal printer, barcode scanner.
- **Network:** Stable 10 Mbps symmetric per branch; offline POS is out of scope.
- **Open-source license:** MIT for code; assets and templates remain property of Pharmacy Chain.
- **Browser support:** Evergreen browsers only; no IE/legacy Edge.
- **Disaster Recovery:** Cross-tenant backup stored on separate physical machine; quarterly DR drill.

### 5.5 Diagrams Reference

All diagrams are located in the `./diagrams/` directory:

| Diagram Type | Location |
| :---- | :---- |
| Context Diagram | [./diagrams/context-diagram.puml](./diagrams/context-diagram.puml) |
| ER Diagram | [./diagrams/entity-relationship.puml](./diagrams/entity-relationship.puml) |

**Per-Use-Case Diagrams:** Each use case has 6 diagrams in its own folder:
- `uc-XX-use-case.puml` - Use case diagram
- `uc-XX-screenflow.puml` - Screen flow diagram
- `uc-XX-statediagram.puml` - State machine diagram
- `uc-XX-sequence.puml` - Sequence diagram
- `uc-XX-class-backend.puml` - Backend class diagram
- `uc-XX-class-frontend.puml` - Frontend class diagram

### 5.6 Glossary

| Term | Definition |
| :---- | :---- |
| **RBAC** | Role-Based Access Control |
| **JWT** | JSON Web Token |
| **SLA** | Service Level Agreement |
| **P95** | 95th percentile |
| **CRUD** | Create, Read, Update, Delete |
| **MTBF** | Mean Time Between Failures |
| **MTTR** | Mean Time To Recover |
| **RPO** | Recovery Point Objective |
| **RTO** | Recovery Time Objective |
| **FIFO** | First In, First Out |
| **Rx** | Prescription |
| **NSF** | Non-Screen Function |
| **FIFO** | First In, First Out |

### 5.7 Document History

| Version | Date | Author | Changes |
| :---- | :---- | :---- | :---- |
| 1.0.0 | 2026-05-24 | SRS Agent | Initial version |
| 1.1.0 | 2026-06-12 | SRS Agent | Round 1: aligned to Internal SRS Template v2.3 |
| 1.2.0 | 2026-06-12 | SRS Agent | Round 2: structural alignment (Functional Overview, single-table UC, table-based Alt Flows, ERD/Entity Details under Software Features) |
| 1.3.0 | 2026-06-13 | SRS Agent | Round 3: restructured actors (6→5 internal + 4 external); added CEO actor; updated Screen Authorization; updated all UC actors; updated Entity User role enum; updated Functional Requirements; permission-based access control model |

---

**END OF DOCUMENT**
