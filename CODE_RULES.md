<?xml version="1.0" encoding="UTF-8"?>
<!--
  PCMS Code Rules - XML version
  Converted from CODE_RULES.md
  Generated: 2026-06-16
  See README/STANDARDS.md for the Markdown source.
-->
<code-rules
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    version="1.0.0"
    rfc-keywords="RFC 2119"
    language="vi-en">

    <!-- ============================================================ -->
    <!-- METADATA                                                      -->
    <!-- ============================================================ -->
    <metadata>
        <title>PCMS — Code Rules for Team</title>
        <icon>⚖️</icon>
        <version>1.0.0</version>
        <effective-date>2026-06-16</effective-date>
        <scope>Toàn bộ 16 modules PCMS (parent + 1 shared + 14 services)</scope>
        <enforcement-level>Tất cả PR merge vào main/develop PHẢI tuân thủ</enforcement-level>
        <review-cycle>Mỗi quarter</review-cycle>
        <related-docs>
            <doc path="STANDARDS.md" type="descriptive">Mô tả chuẩn hiện tại</doc>
            <doc path="README.md" type="guide">Hướng dẫn cài đặt, build, chạy</doc>
            <doc path="../SRS_PhamacyChainManagementSystem_v1.0.0.md" type="spec">Software Requirement Specification</doc>
            <doc path="../SDD_PhamacyChainManagementSystem_v1.0.0.md" type="design">Software Design Document</doc>
        </related-docs>
    </metadata>

    <!-- ============================================================ -->
    <!-- PURPOSE                                                       -->
    <!-- ============================================================ -->
    <purpose>
        <description>Tài liệu này định nghĩa luật code bắt buộc (MUST / MUST NOT) và khuyến nghị (SHOULD / SHOULD NOT) cho team PCMS.</description>
        <goals>
            <goal>Đảm bảo tính nhất quán giữa 16 modules</goal>
            <goal>Giảm thiểu code smell và technical debt</goal>
            <goal>Tăng tốc độ code review và onboarding thành viên mới</goal>
            <goal>Dễ dàng enforce qua CI/CD và code review checklist</goal>
        </goals>
    </purpose>

    <!-- ============================================================ -->
    <!-- RFC 2119 KEYWORDS                                             -->
    <!-- ============================================================ -->
    <keywords standard="RFC 2119">
        <keyword symbol="MUST" vi="BẮT BUỘC" meaning="Bắt buộc tuyệt đối. PR sẽ bị reject nếu vi phạm."/>
        <keyword symbol="MUST NOT" vi="CẤM" meaning="Tuyệt đối không được làm."/>
        <keyword symbol="SHOULD" vi="NÊN" meaning="Khuyến nghị mạnh. Có thể vi phạm nếu có lý do chính đáng (phải ghi rõ trong PR)."/>
        <keyword symbol="SHOULD NOT" vi="KHÔNG NÊN" meaning="Không nên. Chỉ chấp nhận ngoại lệ có lý do."/>
        <keyword symbol="MAY" vi="CÓ THỂ" meaning="Tùy ngữ cảnh."/>
    </keywords>

    <!-- ============================================================ -->
    <!-- TABLE OF CONTENTS                                             -->
    <!-- ============================================================ -->
    <toc>
        <item num="1" anchor="nguyen-tac-tong-quat">Nguyên tắc tổng quát</item>
        <item num="2" anchor="cau-truc-project">Cấu trúc project</item>
        <item num="3" anchor="quy-tac-dat-ten">Quy tắc đặt tên</item>
        <item num="4" anchor="java-code-style">Java &amp; code style</item>
        <item num="5" anchor="spring-boot-specifics">Spring Boot specifics</item>
        <item num="6" anchor="rest-api-design">REST API design</item>
        <item num="7" anchor="database-jpa">Database &amp; JPA</item>
        <item num="8" anchor="dto-validation">DTO &amp; validation</item>
        <item num="9" anchor="error-handling">Error handling</item>
        <item num="10" anchor="logging">Logging</item>
        <item num="11" anchor="service-to-service">Service-to-service</item>
        <item num="12" anchor="security">Security</item>
        <item num="13" anchor="testing">Testing</item>
        <item num="14" anchor="performance">Performance</item>
        <item num="15" anchor="documentation">Documentation</item>
        <item num="16" anchor="git-workflow">Git workflow</item>
        <item num="17" anchor="code-review-checklist">Code review checklist</item>
        <item num="18" anchor="anti-patterns-cam">Anti-patterns cấm</item>
    </toc>

    <!-- ============================================================ -->
    <!-- SECTION 1: NGUYÊN TẮC TỔNG QUÁT                              -->
    <!-- ============================================================ -->
    <section id="nguyen-tac-tong-quat" num="1" icon="🎯">
        <title>Nguyên tắc tổng quát</title>

        <subsection id="solid" num="1.1">
            <title>Nguyên tắc SOLID</title>
            <rules>
                <rule id="SR-001">
                    <level>MUST</level>
                    <text>Áp dụng Single Responsibility: một class chỉ có một lý do để thay đổi.</text>
                </rule>
                <rule id="SR-002">
                    <level>MUST</level>
                    <text>Tuân thủ Dependency Inversion: depend vào abstraction (interface), không depend vào concrete class.</text>
                </rule>
                <rule id="SR-003">
                    <level>SHOULD</level>
                    <text>Giữ class &lt; 300 dòng và method &lt; 50 dòng (&gt;50 dòng → refactor).</text>
                </rule>
                <rule id="SR-004">
                    <level>MUST</level>
                    <text>Giữ method chỉ làm một việc (do one thing well).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="clean-code" num="1.2">
            <title>Clean Code</title>
            <rules>
                <rule id="CC-001">
                    <level>MUST NOT</level>
                    <text>Để lại dead code, commented-out code, TODO không có issue link.</text>
                </rule>
                <rule id="CC-002">
                    <level>MUST</level>
                    <text>Ưu tiên code dễ đọc hơn code "thông minh".</text>
                </rule>
                <rule id="CC-003">
                    <level>MUST</level>
                    <text>Xóa <code>System.out.println</code>, <code>System.err.println</code>, <code>printStackTrace()</code> trước khi commit.</text>
                </rule>
                <rule id="CC-004">
                    <level>SHOULD</level>
                    <text>Tránh magic numbers/strings — dùng <code>private static final</code> constants.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="dry" num="1.3">
            <title>DRY (Don't Repeat Yourself)</title>
            <rules>
                <rule id="DR-001">
                    <level>MUST NOT</level>
                    <text>Copy-paste code giữa các service. Logic chung → đặt trong <code>pcms-common</code>.</text>
                </rule>
                <rule id="DR-002">
                    <level>SHOULD</level>
                    <text>Extract helper method khi thấy pattern lặp lại ≥ 2 lần.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="kiss" num="1.4">
            <title>KISS (Keep It Simple, Stupid)</title>
            <rules>
                <rule id="KS-001">
                    <level>MUST</level>
                    <text>Chọn giải pháp đơn giản nhất có thể hoạt động đúng.</text>
                </rule>
                <rule id="KS-002">
                    <level>SHOULD NOT</level>
                    <text>Over-engineer (không cần Strategy pattern cho 1 if/else).</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 2: CẤU TRÚC PROJECT                                  -->
    <!-- ============================================================ -->
    <section id="cau-truc-project" num="2" icon="📦">
        <title>Cấu trúc project</title>

        <subsection id="module-layout" num="2.1">
            <title>Module layout</title>
            <rules>
                <rule id="ML-001">
                    <level>MUST</level>
                    <text>Đặt Java code trong <code>src/main/java/com/pcms/&lt;servicename&gt;/</code>.</text>
                </rule>
                <rule id="ML-002">
                    <level>MUST</level>
                    <text>Đặt resource trong <code>src/main/resources/</code>.</text>
                </rule>
                <rule id="ML-003">
                    <level>MUST</level>
                    <text>Group class theo chức năng, không theo layer ngang.</text>
                </rule>
            </rules>
            <example type="good" lang="plain"><![CDATA[
com/pcms/orderservice/
├── controller/        # REST endpoints
├── service/impl/      # business logic
├── repository/
├── entity/
├── dto/request/
├── dto/response/
├── enums/
├── client/            # Feign clients
└── scheduler/
]]></example>
            <example type="bad" lang="plain"><![CDATA[
com/pcms/orderservice/
├── order/             # ❌ redundant
│   ├── OrderController.java
│   ├── OrderService.java
│   └── OrderEntity.java
]]></example>
        </subsection>

        <subsection id="dto-folder" num="2.2">
            <title>DTO folder structure</title>
            <rules>
                <rule id="DF-001">
                    <level>MUST</level>
                    <text>Đặt DTO trong <code>dto/request/</code> hoặc <code>dto/response/</code> (KHÔNG flat <code>dto/</code>).</text>
                </rule>
                <rule id="DF-002">
                    <level>MUST</level>
                    <text>Tách riêng <code>request</code> và <code>response</code> DTO (không dùng chung 1 class).</text>
                </rule>
            </rules>
            <example type="good" lang="plain"><![CDATA[dto/request/CreateOrderRequest.java, dto/response/OrderResponse.java]]></example>
            <example type="bad" lang="plain"><![CDATA[dto/OrderDTO.java (chứa cả request và response fields)]]></example>
        </subsection>

        <subsection id="one-class-one-file" num="2.3">
            <title>Quy tắc một file = một class</title>
            <rules>
                <rule id="OC-001">
                    <level>MUST</level>
                    <text>Đặt mỗi top-level class trong một file riêng, tên file trùng tên class.</text>
                </rule>
                <rule id="OC-002">
                    <level>MUST NOT</level>
                    <text>Định nghĩa hai public class trong cùng một file.</text>
                </rule>
                <rule id="OC-003">
                    <level>MAY</level>
                    <text>Đặt nested static class trong class cha nếu chỉ dùng nội bộ (ví dụ: <code>OrderItemResponse.OrderItemResponse</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="test-folder" num="2.4">
            <title>Test folder</title>
            <rules>
                <rule id="TF-001">
                    <level>MUST</level>
                    <text>Đặt test trong <code>src/test/java/...</code> (mirror theo main package).</text>
                </rule>
                <rule id="TF-002">
                    <level>MUST</level>
                    <text>Đặt test resource trong <code>src/test/resources/</code>.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 3: QUY TẮC ĐẶT TÊN                                  -->
    <!-- ============================================================ -->
    <section id="quy-tac-dat-ten" num="3" icon="📛">
        <title>Quy tắc đặt tên</title>

        <subsection id="package-naming" num="3.1">
            <title>Package</title>
            <rules>
                <rule id="PN-001">
                    <level>MUST</level>
                    <text>Dùng lowercase, không dấu gạch ngang, không viết hoa.</text>
                </rule>
                <rule id="PN-002">
                    <level>MUST</level>
                    <text>Theo pattern <code>com.pcms.&lt;servicename&gt;</code> (1 từ, không gạch ngang).</text>
                </rule>
            </rules>
            <example type="good" lang="plain"><![CDATA[com.pcms.orderservice]]></example>
            <example type="bad" lang="plain"><![CDATA[com.pcms.OrderService, com.pcms.order_service]]></example>
        </subsection>

        <subsection id="class-naming" num="3.2">
            <title>Class &amp; Interface</title>
            <rules>
                <rule id="CN-001">
                    <level>MUST</level>
                    <text>Dùng PascalCase, danh từ hoặc cụm danh từ.</text>
                </rule>
                <rule id="CN-002">
                    <level>MUST</level>
                    <text>Đặt tên theo vai trò (không theo layer).</text>
                </rule>
            </rules>
            <table caption="Naming pattern cho từng loại class">
                <header>
                    <col>Loại</col>
                    <col>Pattern</col>
                    <col>Ví dụ</col>
                </header>
                <row><cell>Main class</cell><cell><code>&lt;ServiceName&gt;Application</code></cell><cell><code>OrderServiceApplication</code></cell></row>
                <row><cell>Controller</cell><cell><code>&lt;Entity&gt;Controller</code></cell><cell><code>OrderController</code></cell></row>
                <row><cell>Service interface</cell><cell><code>&lt;Entity&gt;Service</code></cell><cell><code>OrderService</code></cell></row>
                <row><cell>Service impl</cell><cell><code>&lt;Entity&gt;ServiceImpl</code></cell><cell><code>OrderServiceImpl</code></cell></row>
                <row><cell>Repository</cell><cell><code>&lt;Entity&gt;Repository</code></cell><cell><code>OrderRepository</code></cell></row>
                <row><cell>Entity</cell><cell>danh từ số ít</cell><cell><code>Order</code>, <code>User</code></cell></row>
                <row><cell>Request DTO</cell><cell><code>&lt;Action&gt;&lt;Entity&gt;Request</code></cell><cell><code>CreateOrderRequest</code>, <code>UpdateUserRequest</code></cell></row>
                <row><cell>Response DTO</cell><cell><code>&lt;Entity&gt;Response</code></cell><cell><code>OrderResponse</code></cell></row>
                <row><cell>Enum</cell><cell>danh từ số ít (PascalCase)</cell><cell><code>OrderStatus</code></cell></row>
                <row><cell>Enum value</cell><cell>UPPER_SNAKE_CASE</cell><cell><code>PENDING_PAYMENT</code></cell></row>
                <row><cell>Exception</cell><cell><code>&lt;Situation&gt;Exception</code></cell><cell><code>ResourceNotFoundException</code></cell></row>
                <row><cell>Feign client</cell><cell><code>&lt;TargetService&gt;Client</code></cell><cell><code>CatalogClient</code></cell></row>
                <row><cell>Scheduler</cell><cell><code>&lt;Action&gt;Scheduler</code></cell><cell><code>OrderAutoCancelScheduler</code></cell></row>
            </table>
        </subsection>

        <subsection id="method-naming" num="3.3">
            <title>Method</title>
            <rules>
                <rule id="MN-001">
                    <level>MUST</level>
                    <text>Dùng camelCase, bắt đầu bằng động từ.</text>
                </rule>
                <rule id="MN-002">
                    <level>MUST</level>
                    <text>Đặt tên có ý nghĩa — KHÔNG dùng <code>processData()</code>, <code>handleStuff()</code>.</text>
                </rule>
            </rules>
            <example type="good" lang="plain"><![CDATA[findByOrderNumber, markAsPaid, consumeStock]]></example>
            <example type="bad" lang="plain"><![CDATA[process(), handle1(), doIt()]]></example>
        </subsection>

        <subsection id="variable-naming" num="3.4">
            <title>Variable</title>
            <rules>
                <rule id="VN-001">
                    <level>MUST</level>
                    <text>Dùng camelCase.</text>
                </rule>
                <rule id="VN-002">
                    <level>MUST</level>
                    <text>Tránh tên 1 ký tự (trừ <code>i</code>, <code>j</code> trong vòng for, hoặc lambda param ngắn).</text>
                </rule>
                <rule id="VN-003">
                    <level>MUST NOT</level>
                    <text>Dùng Hungarian notation (<code>strName</code>, <code>iCount</code>).</text>
                </rule>
                <rule id="VN-004">
                    <level>SHOULD</level>
                    <text>Dùng tên gợi nghĩa business (<code>orderNumber</code>, <code>branchId</code>) thay vì kỹ thuật (<code>data1</code>, <code>result</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="constant-naming" num="3.5">
            <title>Constant</title>
            <rules>
                <rule id="CO-001">
                    <level>MUST</level>
                    <text>Đặt trong <code>private static final</code> (UPPER_SNAKE_CASE).</text>
                </rule>
                <rule id="CO-002">
                    <level>MUST</level>
                    <text>Nhóm constant theo class sử dụng (không tạo <code>Constants.java</code> global).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;
]]></example>
            <example type="bad" lang="java"><![CDATA[
public static final int MAX = 5;  // tên không rõ nghĩa
]]></example>
        </subsection>

        <subsection id="db-naming" num="3.6">
            <title>Database naming</title>
            <table caption="Quy tắc đặt tên database">
                <header>
                    <col>Phần tử</col>
                    <col>Quy tắc</col>
                    <col>Ví dụ</col>
                </header>
                <row><cell>Table</cell><cell>snake_case + số nhiều</cell><cell><code>users</code>, <code>orders</code>, <code>inventory_batches</code></cell></row>
                <row><cell>Column</cell><cell>snake_case</cell><cell><code>password_hash</code>, <code>created_at</code></cell></row>
                <row><cell>Foreign key</cell><cell><code>&lt;entity&gt;_id</code></cell><cell><code>branch_id</code>, <code>customer_id</code></cell></row>
                <row><cell>Unique constraint</cell><cell><code>uk_&lt;table&gt;_&lt;col&gt;</code></cell><cell><code>uk_user_email</code></cell></row>
                <row><cell>Index</cell><cell><code>idx_&lt;table&gt;_&lt;col&gt;</code></cell><cell><code>idx_order_customer_id</code></cell></row>
            </table>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 4: JAVA & CODE STYLE                                 -->
    <!-- ============================================================ -->
    <section id="java-code-style" num="4" icon="☕">
        <title>Java &amp; code style</title>

        <subsection id="java-version" num="4.1">
            <title>Java version &amp; features</title>
            <rules>
                <rule id="JV-001">
                    <level>MUST</level>
                    <text>Dùng Java 21 (LTS).</text>
                </rule>
                <rule id="JV-002">
                    <level>MUST</level>
                    <text>Dùng <code>record</code> cho mọi DTO và value object immutable.</text>
                </rule>
                <rule id="JV-003">
                    <level>MUST</level>
                    <text>Dùng <code>var</code> cho local variable khi type rõ ràng từ context (Java 10+).</text>
                </rule>
                <rule id="JV-004">
                    <level>SHOULD</level>
                    <text>Dùng pattern matching cho <code>instanceof</code> (Java 16+).</text>
                </rule>
                <rule id="JV-005">
                    <level>SHOULD</level>
                    <text>Dùng text block (<code>"""..."""</code>) cho SQL/JSON dài (Java 15+).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="immutability" num="4.2">
            <title>Immutability</title>
            <rules>
                <rule id="IM-001">
                    <level>MUST NOT</level>
                    <text>Dùng setter trên entity sau khi persist (dùng method hành vi như <code>markAsPaid()</code>).</text>
                </rule>
                <rule id="IM-002">
                    <level>MUST</level>
                    <text>Mark field <code>final</code> cho mọi dependency injected.</text>
                </rule>
                <rule id="IM-003">
                    <level>MUST NOT</level>
                    <text>Dùng mutable static field (trừ cache <code>ConcurrentHashMap</code> có document rõ ràng).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="imports" num="4.3">
            <title>Import</title>
            <rules>
                <rule id="IM-IMP-001">
                    <level>MUST</level>
                    <text>Dùng <code>jakarta.*</code> (KHÔNG <code>javax.*</code>) cho Spring Boot 3+ API.</text>
                </rule>
                <rule id="IM-IMP-002">
                    <level>MUST</level>
                    <text>Group import theo thứ tự: <code>java.*</code> → <code>jakarta.*</code> → <code>org.*</code> → <code>com.*</code> → project internal.</text>
                </rule>
                <rule id="IM-IMP-003">
                    <level>MUST NOT</level>
                    <text>Dùng wildcard import (<code>import java.util.*;</code>) — IDE sẽ tự organize.</text>
                </rule>
                <rule id="IM-IMP-004">
                    <level>MUST NOT</level>
                    <text>Import class không sử dụng.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="formatting" num="4.4">
            <title>Formatting</title>
            <rules>
                <rule id="FM-001">
                    <level>MUST</level>
                    <text>Dùng 4 spaces indent (không tab).</text>
                </rule>
                <rule id="FM-002">
                    <level>MUST</level>
                    <text>Giữ line length ≤ 120 ký tự.</text>
                </rule>
                <rule id="FM-003">
                    <level>MUST</level>
                    <text>Dùng LF line ending (Unix style).</text>
                </rule>
                <rule id="FM-004">
                    <level>MUST</level>
                    <text>Kết thúc file bằng newline.</text>
                </rule>
                <rule id="FM-005">
                    <level>MUST NOT</level>
                    <text>Có trailing whitespace.</text>
                </rule>
                <rule id="FM-006">
                    <level>MUST</level>
                    <text>Dùng Allman style braces (mở brace trên dòng mới).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
public void method()
{
    if (condition)
    {
        doSomething();
    }
}
]]></example>
        </subsection>

        <subsection id="lombok" num="4.5">
            <title>Lombok</title>
            <rules>
                <rule id="LB-001">
                    <level>MUST NOT</level>
                    <text>Dùng Lombok trong project PCMS — toàn bộ getter/setter viết tay cho entity.</text>
                </rule>
                <rule id="LB-002">
                    <level>MAY</level>
                    <text>Dùng Lombok trong test code nếu cần (nhưng nên tránh).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="optional" num="4.6">
            <title>Optional</title>
            <rules>
                <rule id="OP-001">
                    <level>MUST</level>
                    <text>Dùng <code>Optional&lt;T&gt;</code> cho method return có thể null.</text>
                </rule>
                <rule id="OP-002">
                    <level>MUST NOT</level>
                    <text>Dùng <code>Optional</code> làm field, parameter, hoặc trong collection (<code>List&lt;Optional&lt;T&gt;&gt;</code>).</text>
                </rule>
                <rule id="OP-003">
                    <level>SHOULD</level>
                    <text>Dùng <code>.orElseThrow()</code> thay vì <code>.orElse(null)</code> + null check.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
return repository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("User", id));
]]></example>
            <example type="bad" lang="java"><![CDATA[
User user = repository.findById(id).orElse(null);
if (user == null) {
    throw new RuntimeException("Not found");
}
]]></example>
        </subsection>

        <subsection id="stream-lambda" num="4.7">
            <title>Stream &amp; Lambda</title>
            <rules>
                <rule id="SL-001">
                    <level>SHOULD</level>
                    <text>Dùng stream API cho collection transformation rõ ràng.</text>
                </rule>
                <rule id="SL-002">
                    <level>MUST NOT</level>
                    <text>Chain &gt; 3 stream operations trong 1 câu lệnh (tách ra cho dễ đọc).</text>
                </rule>
                <rule id="SL-003">
                    <level>MUST</level>
                    <text>Tránh side-effect trong stream (trừ <code>forEach</code> cuối cùng).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="null-safety" num="4.8">
            <title>Null safety</title>
            <rules>
                <rule id="NS-001">
                    <level>MUST</level>
                    <text>Dùng <code>@NotNull</code> / <code>@Nullable</code> annotation cho parameter/field quan trọng (dùng <code>org.springframework.lang</code> hoặc <code>jakarta.annotation</code>).</text>
                </rule>
                <rule id="NS-002">
                    <level>MUST NOT</level>
                    <text>Return <code>null</code> cho collection — return <code>Collections.emptyList()</code> hoặc <code>List.of()</code>.</text>
                </rule>
                <rule id="NS-003">
                    <level>SHOULD</level>
                    <text>Dùng <code>Objects.requireNonNull(x, "x must not be null")</code> ở method public đầu vào.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="generics" num="4.9">
            <title>Generics</title>
            <rules>
                <rule id="GE-001">
                    <level>MUST</level>
                    <text>Dùng diamond operator (<code>&lt;&gt;</code>) thay vì raw type.</text>
                </rule>
                <rule id="GE-002">
                    <level>MUST</level>
                    <text>Khai báo generic type rõ ràng cho <code>List</code>, <code>Map</code>, <code>Set</code>.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[List<OrderResponse> orders]]></example>
            <example type="bad" lang="java"><![CDATA[List orders]]></example>
        </subsection>

        <subsection id="exception-handling-style" num="4.10">
            <title>Exception handling</title>
            <rules>
                <rule id="EX-001">
                    <level>MUST</level>
                    <text>Bắt specific exception, không bắt <code>Exception</code> chung chung (trừ top-level handler).</text>
                </rule>
                <rule id="EX-002">
                    <level>MUST NOT</level>
                    <text>Swallow exception (<code>catch (Exception e) {}</code>).</text>
                </rule>
                <rule id="EX-003">
                    <level>MUST</level>
                    <text>Kèm message có ngữ cảnh khi rethrow.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
try {
    inventoryClient.consumeStock(request);
} catch (FeignException e) {
    log.warn("Failed to consume stock for order {}: {}", orderId, e.getMessage());
    throw new InvalidOperationException("Stock consumption failed", "Lỗi trừ tồn kho");
}
]]></example>
            <example type="bad" lang="java"><![CDATA[
try { ... } catch (Exception e) { }   // nuốt exception
]]></example>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 5: SPRING BOOT SPECIFICS                             -->
    <!-- ============================================================ -->
    <section id="spring-boot-specifics" num="5" icon="🌱">
        <title>Spring Boot specifics</title>

        <subsection id="app-class" num="5.1">
            <title>Application class</title>
            <rules>
                <rule id="AC-001">
                    <level>MUST</level>
                    <text>Annotate với <code>@SpringBootApplication(scanBasePackages = "com.pcms")</code> để scan <code>pcms-common</code>.</text>
                </rule>
                <rule id="AC-002">
                    <level>MUST</level>
                    <text>Thêm <code>@EnableJpaAuditing</code> cho mọi service có JPA.</text>
                </rule>
                <rule id="AC-003">
                    <level>MUST</level>
                    <text>Thêm <code>@EnableFeignClients</code> cho service gọi inter-service.</text>
                </rule>
                <rule id="AC-004">
                    <level>MUST</level>
                    <text>Thêm <code>@EnableScheduling</code> cho service có <code>@Scheduled</code>.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="dependency-injection" num="5.2">
            <title>Dependency Injection</title>
            <rules>
                <rule id="DI-001">
                    <level>MUST</level>
                    <text>Dùng constructor injection với field <code>final</code> (kể cả trong scheduler, security).</text>
                </rule>
                <rule id="DI-002">
                    <level>MUST NOT</level>
                    <text>Dùng <code>@Autowired</code> field injection.</text>
                </rule>
                <rule id="DI-003">
                    <level>MUST</level>
                    <text>Dùng interface cho dependency, không inject concrete class trực tiếp.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public OrderServiceImpl(OrderRepository orderRepository, CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }
}
]]></example>
            <example type="bad" lang="java"><![CDATA[
@Service
public class OrderServiceImpl {
    @Autowired
    private OrderRepository orderRepository;  // ❌ field injection
}
]]></example>
        </subsection>

        <subsection id="bean-config" num="5.3">
            <title>Bean &amp; Configuration</title>
            <rules>
                <rule id="BC-001">
                    <level>MUST</level>
                    <text>Dùng <code>@Configuration</code> + <code>@Bean</code> cho bean definition, không tạo static utility.</text>
                </rule>
                <rule id="BC-002">
                    <level>MUST</level>
                    <text>Đặt class config trong package <code>config/</code>.</text>
                </rule>
                <rule id="BC-003">
                    <level>SHOULD</level>
                    <text>Dùng <code>record</code> cho <code>@ConfigurationProperties</code> (Java 17+).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="transaction" num="5.4">
            <title>Transaction</title>
            <rules>
                <rule id="TX-001">
                    <level>MUST</level>
                    <text>Annotate <code>@Transactional</code> ở service class (write default).</text>
                </rule>
                <rule id="TX-002">
                    <level>MUST</level>
                    <text>Annotate <code>@Transactional(readOnly = true)</code> cho method chỉ đọc.</text>
                </rule>
                <rule id="TX-003">
                    <level>MUST NOT</level>
                    <text>Đặt <code>@Transactional</code> ở controller (transaction phải ở service layer).</text>
                </rule>
                <rule id="TX-004">
                    <level>MUST NOT</level>
                    <text>Gọi method <code>@Transactional</code> từ cùng class qua <code>this.method()</code> (sẽ bypass proxy).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
@Service
@Transactional   // write default
public class OrderServiceImpl implements OrderService {

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(...) { ... }

    @Override
    @Transactional   // write
    public OrderResponse create(...) { ... }
}
]]></example>
        </subsection>

        <subsection id="validation-spring" num="5.5">
            <title>Validation</title>
            <rules>
                <rule id="VL-001">
                    <level>MUST</level>
                    <text>Dùng <code>jakarta.validation.constraints.*</code> (không <code>javax.validation.*</code>).</text>
                </rule>
                <rule id="VL-002">
                    <level>MUST</level>
                    <text>Annotate DTO với validation annotations (<code>@NotBlank</code>, <code>@NotNull</code>, <code>@Email</code>, <code>@Size</code>, <code>@Min</code>, <code>@DecimalMin</code>).</text>
                </rule>
                <rule id="VL-003">
                    <level>MUST</level>
                    <text>Dùng <code>@Valid</code> trên <code>@RequestBody</code> và nested DTO (<code>@Valid List&lt;@Valid OrderItemRequest&gt; items</code>).</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 6: REST API DESIGN                                    -->
    <!-- ============================================================ -->
    <section id="rest-api-design" num="6" icon="🌐">
        <title>REST API design</title>

        <subsection id="url-structure" num="6.1">
            <title>URL structure</title>
            <rules>
                <rule id="US-001">
                    <level>MUST</level>
                    <text>Dùng lowercase, dấu gạch ngang cho multi-word resource: <code>/order-items</code>, <code>/payment-methods</code>.</text>
                </rule>
                <rule id="US-002">
                    <level>MUST</level>
                    <text>Dùng số nhiều cho collection: <code>/users</code>, <code>/orders</code>, <code>/branches</code>.</text>
                </rule>
                <rule id="US-003">
                    <level>MUST</level>
                    <text>Prefix toàn cục là <code>/api/v1/</code> (do API Gateway xử lý).</text>
                </rule>
                <rule id="US-004">
                    <level>MUST NOT</level>
                    <text>Dùng verb trong URL: ❌ <code>/getUsers</code>, <code>/createOrder</code>.</text>
                </rule>
                <rule id="US-005">
                    <level>MUST NOT</level>
                    <text>Dùng file extension trong URL: ❌ <code>/users.json</code>.</text>
                </rule>
            </rules>
            <example type="good" lang="plain"><![CDATA[GET /api/v1/orders/{id}]]></example>
            <example type="bad" lang="plain"><![CDATA[GET /api/v1/getOrder?id=123]]></example>
        </subsection>

        <subsection id="http-methods" num="6.2">
            <title>HTTP methods</title>
            <rules>
                <rule id="HM-001">
                    <level>MUST</level>
                    <text>Dùng đúng HTTP method theo REST convention.</text>
                </rule>
            </rules>
            <table caption="HTTP method convention">
                <header>
                    <col>Method</col>
                    <col>Ý nghĩa</col>
                    <col>Idempotent</col>
                    <col>Body</col>
                </header>
                <row><cell><code>GET</code></cell><cell>Read</cell><cell>Yes</cell><cell>No</cell></row>
                <row><cell><code>POST</code></cell><cell>Create</cell><cell>No</cell><cell>Yes</cell></row>
                <row><cell><code>PUT</code></cell><cell>Update full</cell><cell>Yes</cell><cell>Yes</cell></row>
                <row><cell><code>PATCH</code></cell><cell>Update partial</cell><cell>Yes</cell><cell>Yes</cell></row>
                <row><cell><code>DELETE</code></cell><cell>Delete/Soft delete</cell><cell>Yes</cell><cell>No</cell></row>
            </table>
        </subsection>

        <subsection id="http-status" num="6.3">
            <title>HTTP status codes</title>
            <rules>
                <rule id="HS-001">
                    <level>MUST</level>
                    <text>Trả status code chính xác theo bảng dưới.</text>
                </rule>
            </rules>
            <table caption="HTTP status code mapping">
                <header>
                    <col>Code</col>
                    <col>Khi nào</col>
                </header>
                <row><cell>200 OK</cell><cell>GET, PUT, PATCH thành công</cell></row>
                <row><cell>201 Created</cell><cell>POST tạo resource mới (kèm <code>Location</code> header)</cell></row>
                <row><cell>204 No Content</cell><cell>DELETE soft delete thành công</cell></row>
                <row><cell>400 Bad Request</cell><cell>Validation fail, malformed request (MSG33)</cell></row>
                <row><cell>401 Unauthorized</cell><cell>Auth required (MSG01)</cell></row>
                <row><cell>403 Forbidden</cell><cell>Access denied (MSG31)</cell></row>
                <row><cell>404 Not Found</cell><cell>Resource không tồn tại (MSG31)</cell></row>
                <row><cell>409 Conflict</cell><cell>Duplicate, data integrity, state conflict (MSG09)</cell></row>
                <row><cell>500 Internal Error</cell><cell>Unknown exception (MSG34)</cell></row>
            </table>
        </subsection>

        <subsection id="pagination-api" num="6.4">
            <title>Pagination</title>
            <rules>
                <rule id="PG-001">
                    <level>MUST</level>
                    <text>Dùng <code>page</code> (zero-based, default 0) và <code>size</code> (default 20, max 100) cho mọi list endpoint.</text>
                </rule>
                <rule id="PG-002">
                    <level>MUST</level>
                    <text>Clamp <code>size</code> ở service: <code>Math.min(size, 100)</code>.</text>
                </rule>
                <rule id="PG-003">
                    <level>MUST</level>
                    <text>Sort theo <code>createdAt DESC</code> mặc định.</text>
                </rule>
                <rule id="PG-004">
                    <level>MUST</level>
                    <text>Wrap kết quả trong <code>PageResponse&lt;T&gt;</code> (sẽ chuẩn hóa 5 fields trong <code>pcms-common</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="controller" num="6.5">
            <title>Controller</title>
            <rules>
                <rule id="CT-001">
                    <level>MUST</level>
                    <text>Annotate với <code>@RestController</code> (không <code>@Controller</code> + <code>@ResponseBody</code>).</text>
                </rule>
                <rule id="CT-002">
                    <level>MUST</level>
                    <text>Đặt <code>@RequestMapping("/&lt;resource&gt;")</code> ở class level.</text>
                </rule>
                <rule id="CT-003">
                    <level>MUST</level>
                    <text>Dùng constructor injection (không <code>@Autowired</code> field).</text>
                </rule>
                <rule id="CT-004">
                    <level>MUST</level>
                    <text>Giữ controller thin — chỉ nhận request, gọi service, trả response.</text>
                </rule>
                <rule id="CT-005">
                    <level>MUST NOT</level>
                    <text>Chứa business logic trong controller.</text>
                </rule>
                <rule id="CT-006">
                    <level>SHOULD</level>
                    <text>Viết Javadoc cho mỗi endpoint (mô tả UC reference, auth requirement).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
/** GET /api/v1/orders - List with pagination. UC06 - Pharmacist/Admin/Manager */
@GetMapping
public ResponseEntity<PageResponse<OrderResponse>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(orderService.list(page, size));
}
]]></example>
        </subsection>

        <subsection id="api-doc" num="6.6">
            <title>API documentation</title>
            <rules>
                <rule id="AD-001">
                    <level>SHOULD</level>
                    <text>Bổ sung OpenAPI/Swagger annotation (<code>@Operation</code>, <code>@ApiResponse</code>) trong giai đoạn tiếp theo.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 7: DATABASE & JPA                                     -->
    <!-- ============================================================ -->
    <section id="database-jpa" num="7" icon="🗄">
        <title>Database &amp; JPA</title>

        <subsection id="entity-rules" num="7.1">
            <title>Entity rules</title>
            <rules>
                <rule id="EN-001">
                    <level>MUST</level>
                    <text>Extend <code>@Entity</code> + <code>@Table(name = "&lt;snake_case_plural&gt;")</code>.</text>
                </rule>
                <rule id="EN-002">
                    <level>MUST</level>
                    <text>Dùng UUID làm primary key: <code>@Id @GeneratedValue(strategy = GenerationType.UUID)</code>.</text>
                </rule>
                <rule id="EN-003">
                    <level>MUST NOT</level>
                    <text>Dùng <code>GenerationType.IDENTITY</code> (Long auto-increment).</text>
                </rule>
                <rule id="EN-004">
                    <level>MUST</level>
                    <text>Annotate <code>@EntityListeners(AuditingEntityListener.class)</code> để enable audit.</text>
                </rule>
                <rule id="EN-005">
                    <level>MUST</level>
                    <text>Có field <code>createdAt</code> (<code>@CreatedDate</code>, <code>updatable = false</code>) và <code>updatedAt</code> (<code>@LastModifiedDate</code>).</text>
                </rule>
                <rule id="EN-006">
                    <level>MUST</level>
                    <text>Dùng <code>LocalDateTime</code> cho timestamp (KHÔNG <code>Instant</code> / <code>OffsetDateTime</code> ở entity).</text>
                </rule>
                <rule id="EN-007">
                    <level>MUST</level>
                    <text>Dùng <code>BigDecimal</code> cho monetary field, với <code>precision = 15, scale = 2</code>.</text>
                </rule>
                <rule id="EN-008">
                    <level>MUST</level>
                    <text>Lưu enum dưới dạng STRING: <code>@Enumerated(EnumType.STRING)</code>.</text>
                </rule>
                <rule id="EN-009">
                    <level>MUST NOT</level>
                    <text>Dùng Lombok — viết getter/setter thủ công.</text>
                </rule>
                <rule id="EN-010">
                    <level>MUST</level>
                    <text>Dùng <code>Optional</code> cho method return có thể null (<code>findByX</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="soft-delete" num="7.2">
            <title>Soft delete</title>
            <rules>
                <rule id="SD-001">
                    <level>MUST</level>
                    <text>Implement soft delete bằng <code>status</code> enum (VD: <code>INACTIVE</code>, <code>CANCELLED</code>, <code>REFUNDED</code>).</text>
                </rule>
                <rule id="SD-002">
                    <level>MUST NOT</level>
                    <text>Xóa cứng record (trừ khi có yêu cầu legal/GDPR rõ ràng).</text>
                </rule>
                <rule id="SD-003">
                    <level>MUST NOT</level>
                    <text>Dùng <code>@SQLDelete</code> + <code>@Where</code> (dễ gây bug khi query khác table).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="repository" num="7.3">
            <title>Repository</title>
            <rules>
                <rule id="RP-001">
                    <level>MUST</level>
                    <text>Extend <code>JpaRepository&lt;Entity, UUID&gt;</code> (không dùng <code>CrudRepository</code>).</text>
                </rule>
                <rule id="RP-002">
                    <level>MUST</level>
                    <text>Dùng method query cho query đơn giản: <code>findByEmail</code>, <code>existsByPhone</code>.</text>
                </rule>
                <rule id="RP-003">
                    <level>MUST</level>
                    <text>Dùng <code>@Query</code> JPQL cho query phức tạp.</text>
                </rule>
                <rule id="RP-004">
                    <level>MUST NOT</level>
                    <text>Dùng native SQL (trừ khi không thể dùng JPQL — phải comment lý do).</text>
                </rule>
                <rule id="RP-005">
                    <level>SHOULD</level>
                    <text>Đặt custom query method trong repository của entity liên quan, không tạo <code>*QueryRepository</code> riêng.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="n-plus-one" num="7.4">
            <title>N+1 problem</title>
            <rules>
                <rule id="N1-001">
                    <level>MUST</level>
                    <text>Tránh N+1 query — dùng <code>@EntityGraph</code> hoặc <code>JOIN FETCH</code> khi cần eager load.</text>
                </rule>
                <rule id="N1-002">
                    <level>MUST</level>
                    <text>Cân nhắc giữa <code>LAZY</code> (default) và <code>EAGER</code> fetch:
                        <sub-rule>LAZY cho collection (<code>@OneToMany</code>, <code>@ManyToMany</code>).</sub-rule>
                        <sub-rule>EAGER chỉ cho <code>@ManyToOne</code> khi chắc chắn luôn cần.</sub-rule>
                    </text>
                </rule>
                <rule id="N1-003">
                    <level>SHOULD</level>
                    <text>Dùng <code>Page.map(...)</code> thay vì loop thủ công để map entity → DTO.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="datasource-config" num="7.5">
            <title>Datasource config</title>
            <rules>
                <rule id="DS-001">
                    <level>MUST</level>
                    <text>Dùng <code>MySQLDialect</code> (Spring Boot auto-detect nhưng explicit vẫn tốt hơn).</text>
                </rule>
                <rule id="DS-002">
                    <level>MUST</level>
                    <text>Dùng <code>ddl-auto: update</code> trong dev, <code>validate</code> trong production.</text>
                </rule>
                <rule id="DS-003">
                    <level>MUST</level>
                    <text>Khai báo connection qua Config Server (không hardcode trong code).</text>
                </rule>
                <rule id="DS-004">
                    <level>SHOULD</level>
                    <text>Dùng connection pool HikariCP (default Spring Boot).</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 8: DTO & VALIDATION                                   -->
    <!-- ============================================================ -->
    <section id="dto-validation" num="8" icon="📝">
        <title>DTO &amp; validation</title>

        <subsection id="dto-design" num="8.1">
            <title>DTO design</title>
            <rules>
                <rule id="DD-001">
                    <level>MUST</level>
                    <text>Dùng Java <code>record</code> cho mọi DTO (immutable, equals/hashCode/toString auto-generated).</text>
                </rule>
                <rule id="DD-002">
                    <level>MUST</level>
                    <text>Tách riêng <code>Request</code> và <code>Response</code> DTO (không dùng chung).</text>
                </rule>
                <rule id="DD-003">
                    <level>MUST NOT</level>
                    <text>Expose <code>Entity</code> ra ngoài — luôn convert qua DTO.</text>
                </rule>
                <rule id="DD-004">
                    <level>MUST</level>
                    <text>Dùng <code>static factory method</code> (<code>from(Entity)</code>, <code>of(...)</code>) cho DTO mapping.</text>
                </rule>
                <rule id="DD-005">
                    <level>MUST NOT</level>
                    <text>Dùng <code>BeanUtils.copyProperties</code> (ẩn bug, khó debug).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
public record UserResponse(
    UUID id,
    String email,
    String fullName,
    Role role,
    UserStatus status,
    LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(),
                                u.getRole(), u.getStatus(), u.getCreatedAt());
    }
}
]]></example>
        </subsection>

        <subsection id="dto-validation-rules" num="8.2">
            <title>Validation</title>
            <rules>
                <rule id="DV-001">
                    <level>MUST</level>
                    <text>Annotate field DTO với Jakarta validation: <code>@NotBlank</code>, <code>@NotNull</code>, <code>@Email</code>, <code>@Size(max=...)</code>, <code>@Min</code>, <code>@DecimalMin</code>, <code>@Pattern</code>.</text>
                </rule>
                <rule id="DV-002">
                    <level>MUST</level>
                    <text>Dùng <code>@Valid</code> trên <code>@RequestBody</code> parameter.</text>
                </rule>
                <rule id="DV-003">
                    <level>MUST</level>
                    <text>Dùng <code>@Valid</code> cho nested collection: <code>List&lt;@Valid OrderItemRequest&gt; items</code>.</text>
                </rule>
                <rule id="DV-004">
                    <level>MUST</level>
                    <text>Đặt message tiếng Việt trong validation annotation: <code>@NotBlank(message = "Email không được để trống")</code>.</text>
                </rule>
                <rule id="DV-005">
                    <level>SHOULD</level>
                    <text>Tạo custom validator (<code>@ConstraintValidator</code>) cho rule phức tạp.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="pagination-dto" num="8.3">
            <title>Pagination DTO</title>
            <rules>
                <rule id="PD-001">
                    <level>MUST</level>
                    <text>Dùng <code>PageResponse&lt;T&gt;</code> từ <code>pcms-common</code> (sau khi promote lên common) với 5 fields:
                        <sub-rule><code>List&lt;T&gt; data</code></sub-rule>
                        <sub-rule><code>int page</code></sub-rule>
                        <sub-rule><code>int size</code></sub-rule>
                        <sub-rule><code>long total</code></sub-rule>
                        <sub-rule><code>int totalPages</code></sub-rule>
                    </text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 9: ERROR HANDLING                                     -->
    <!-- ============================================================ -->
    <section id="error-handling" num="9" icon="⚠️">
        <title>Error handling</title>

        <subsection id="exception-hierarchy" num="9.1">
            <title>Exception hierarchy</title>
            <rules>
                <rule id="EH-001">
                    <level>MUST</level>
                    <text>Extend <code>BusinessException</code> (từ <code>pcms-common</code>) cho mọi domain exception.</text>
                </rule>
                <rule id="EH-002">
                    <level>MUST NOT</level>
                    <text>Throw <code>RuntimeException</code> raw hoặc custom exception ngoài hệ thống.</text>
                </rule>
                <rule id="EH-003">
                    <level>MUST NOT</level>
                    <text>Extend <code>Exception</code> trực tiếp — phải qua <code>BusinessException</code> để được GlobalExceptionHandler xử lý.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="bilingual-messages" num="9.2">
            <title>Bilingual messages (CR-01)</title>
            <rules>
                <rule id="BM-001">
                    <level>MUST</level>
                    <text>Cung cấp CẢ TIẾNG ANH VÀ TIẾNG VIỆT cho mọi exception message.</text>
                </rule>
                <rule id="BM-002">
                    <level>MUST</level>
                    <text>Truyền message Việt làm parameter thứ 2 cho constructor <code>BusinessException</code>.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
throw new InvalidOperationException(
    "Order can only be updated while PENDING_PAYMENT",
    "Đơn hàng chỉ có thể cập nhật khi đang chờ thanh toán");
]]></example>
            <example type="bad" lang="java"><![CDATA[
throw new RuntimeException("Order cannot be updated");  // ❌ thiếu VI message
]]></example>
        </subsection>

        <subsection id="error-codes" num="9.3">
            <title>Error codes</title>
            <rules>
                <rule id="EC-001">
                    <level>MUST</level>
                    <text>Dùng MSGxx code từ catalog (MSG01..MSG34). Xem chi tiết trong <code>STANDARDS.md</code> §8.2.</text>
                </rule>
                <rule id="EC-002">
                    <level>MUST NOT</level>
                    <text>Tự tạo error code mới nếu MSGxx hiện có đáp ứng.</text>
                </rule>
                <rule id="EC-003">
                    <level>SHOULD</level>
                    <text>Mở PR mới vào <code>pcms-common</code> nếu cần thêm code mới.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="error-response" num="9.4">
            <title>Error response</title>
            <rules>
                <rule id="ER-001">
                    <level>MUST</level>
                    <text>Trả về <code>ErrorResponse</code> envelope (RFC 7807-inspired) từ <code>pcms-common</code>.</text>
                </rule>
                <rule id="ER-002">
                    <level>MUST</level>
                    <text>Để <code>GlobalExceptionHandler</code> (trong <code>pcms-common</code>) tự động map — KHÔNG viết <code>@ExceptionHandler</code> trong service.</text>
                </rule>
                <rule id="ER-003">
                    <level>MUST</level>
                    <text>Đảm bảo <code>scanBasePackages = "com.pcms"</code> ở main class để <code>GlobalExceptionHandler</code> được scan.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="logging-exception" num="9.5">
            <title>Logging exception</title>
            <rules>
                <rule id="LE-001">
                    <level>MUST</level>
                    <text>Log ở level <code>WARN</code> cho business exception (kèm code, path, message EN, message VI).</text>
                </rule>
                <rule id="LE-002">
                    <level>MUST</level>
                    <text>Log ở level <code>ERROR</code> cho unknown exception (kèm stack trace đầy đủ).</text>
                </rule>
                <rule id="LE-003">
                    <level>MUST NOT</level>
                    <text>Log sensitive data (password, token, PII).</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 10: LOGGING                                           -->
    <!-- ============================================================ -->
    <section id="logging" num="10" icon="📋">
        <title>Logging</title>

        <subsection id="logger" num="10.1">
            <title>Logger</title>
            <rules>
                <rule id="LG-001">
                    <level>MUST</level>
                    <text>Dùng SLF4J: <code>private static final Logger log = LoggerFactory.getLogger(ClassName.class);</code></text>
                </rule>
                <rule id="LG-002">
                    <level>MUST NOT</level>
                    <text>Dùng <code>System.out.println</code>, <code>System.err.println</code>, <code>printStackTrace()</code>.</text>
                </rule>
                <rule id="LG-003">
                    <level>MUST NOT</level>
                    <text>Dùng <code>java.util.logging</code>.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="log-levels" num="10.2">
            <title>Log levels</title>
            <table caption="Log level guideline">
                <header>
                    <col>Level</col>
                    <col>Khi nào dùng</col>
                </header>
                <row><cell><code>ERROR</code></cell><cell>Unknown exception, system failure, lỗi nghiêm trọng (kèm stack trace)</cell></row>
                <row><cell><code>WARN</code></cell><cell>Business exception, retry, fallback, deprecation, scheduler thông báo</cell></row>
                <row><cell><code>INFO</code></cell><cell>Khởi động service, scheduled job milestone, thanh toán thành công</cell></row>
                <row><cell><code>DEBUG</code></cell><cell>Chi tiết flow (chỉ bật khi debug)</cell></row>
                <row><cell><code>TRACE</code></cell><cell>Rất chi tiết (KHÔNG dùng trong production code)</cell></row>
            </table>
        </subsection>

        <subsection id="log-message" num="10.3">
            <title>Log message</title>
            <rules>
                <rule id="LM-001">
                    <level>MUST</level>
                    <text>Dùng placeholder <code>{}</code> thay vì string concatenation.</text>
                </rule>
                <rule id="LM-002">
                    <level>MUST</level>
                    <text>Viết message rõ nghĩa, có context (entity ID, action).</text>
                </rule>
                <rule id="LM-003">
                    <level>MUST NOT</level>
                    <text>Log sensitive data: password, JWT token, credit card, OTP.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[log.info("User {} logged in at {}", userId, loginTime);]]></example>
            <example type="bad" lang="java"><![CDATA[log.info("User " + userId + " logged in at " + loginTime);]]></example>
        </subsection>

        <subsection id="log-format" num="10.4">
            <title>Format</title>
            <rules>
                <rule id="LF-001">
                    <level>MUST</level>
                    <text>Dùng Logback default của Spring Boot (không custom pattern trừ khi cần structured logging).</text>
                </rule>
                <rule id="LF-002">
                    <level>SHOULD</level>
                    <text>Cấu hình JSON log trong giai đoạn tích hợp ELK/Loki.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 11: SERVICE-TO-SERVICE                                -->
    <!-- ============================================================ -->
    <section id="service-to-service" num="11" icon="🔌">
        <title>Service-to-service</title>

        <subsection id="feign-client" num="11.1">
            <title>Feign client</title>
            <rules>
                <rule id="FC-001">
                    <level>MUST</level>
                    <text>Dùng OpenFeign + Eureka cho mọi inter-service call.</text>
                </rule>
                <rule id="FC-002">
                    <level>MUST</level>
                    <text>Dùng URL <code>lb://&lt;service-name&gt;</code> (không hardcode IP/port).</text>
                </rule>
                <rule id="FC-003">
                    <level>MUST</level>
                    <text>Annotate <code>@FeignClient(name = "&lt;service-name&gt;")</code>.</text>
                </rule>
                <rule id="FC-004">
                    <level>MUST</level>
                    <text>Đặt file trong package <code>client/</code>.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="circuit-breaker" num="11.2">
            <title>Circuit breaker</title>
            <rules>
                <rule id="CB-001">
                    <level>MUST</level>
                    <text>Annotate <code>@CircuitBreaker(name = "&lt;uniqueName&gt;", fallbackMethod = "&lt;fallback&gt;")</code>.</text>
                </rule>
                <rule id="CB-002">
                    <level>MUST</level>
                    <text>Cung cấp fallback method là <code>default</code> method cùng class.</text>
                </rule>
                <rule id="CB-003">
                    <level>MUST</level>
                    <text>Có signature <code>(args..., Throwable t)</code> cho fallback.</text>
                </rule>
                <rule id="CB-004">
                    <level>MUST</level>
                    <text>Return giá trị safe default trong fallback (không throw lại).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/medicines/{id}")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackGetMedicine")
    Map<String, Object> getMedicineById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetMedicine(UUID id, Throwable t) {
        log.warn("Catalog service unavailable for medicine {}: {}", id, t.getMessage());
        return Map.of("id", id, "name", "Unknown", "price", BigDecimal.ZERO);
    }
}
]]></example>
        </subsection>

        <subsection id="retry-timeout" num="11.3">
            <title>Retry &amp; timeout</title>
            <rules>
                <rule id="RT-001">
                    <level>SHOULD</level>
                    <text>Cấu hình timeout qua <code>application.yml</code> (feign.client.config.&lt;service&gt;.connectTimeout, readTimeout).</text>
                </rule>
                <rule id="RT-002">
                    <level>SHOULD</level>
                    <text>Dùng <code>@Retry</code> (Resilience4J) cho idempotent operation.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="saga" num="11.4">
            <title>Saga &amp; compensation</title>
            <rules>
                <rule id="SG-001">
                    <level>MUST</level>
                    <text>Đảm bảo idempotency cho mọi inter-service write (dùng <code>idempotencyKey</code> nếu cần).</text>
                </rule>
                <rule id="SG-002">
                    <level>SHOULD</level>
                    <text>Xử lý partial failure bằng try-catch + log warning (không fail toàn bộ flow).</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 12: SECURITY                                          -->
    <!-- ============================================================ -->
    <section id="security" num="12" icon="🔐">
        <title>Security</title>

        <subsection id="password" num="12.1">
            <title>Password</title>
            <rules>
                <rule id="PW-001">
                    <level>MUST</level>
                    <text>Hash password bằng BCrypt (strength ≥ 10).</text>
                </rule>
                <rule id="PW-002">
                    <level>MUST NOT</level>
                    <text>Lưu password dạng plain text.</text>
                </rule>
                <rule id="PW-003">
                    <level>MUST NOT</level>
                    <text>Log password, hash, hoặc token.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="jwt" num="12.2">
            <title>JWT</title>
            <rules>
                <rule id="JT-001">
                    <level>MUST</level>
                    <text>Dùng <code>io.jsonwebtoken:jjwt 0.12.6</code> (api/impl/jackson).</text>
                </rule>
                <rule id="JT-002">
                    <level>MUST</level>
                    <text>Dùng HS256 hoặc mạnh hơn.</text>
                </rule>
                <rule id="JT-003">
                    <level>MUST</level>
                    <text>Set expiration: access token ≤ 15 phút, refresh token ≤ 7 ngày.</text>
                </rule>
                <rule id="JT-004">
                    <level>MUST</level>
                    <text>Validate token signature, expiration, và issuer ở mọi request.</text>
                </rule>
                <rule id="JT-005">
                    <level>MUST NOT</level>
                    <text>Lưu sensitive data trong JWT claims (chỉ <code>uid</code>, <code>email</code>, <code>role</code>, <code>branch_id</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="authorization" num="12.3">
            <title>Authorization</title>
            <rules>
                <rule id="AZ-001">
                    <level>MUST</level>
                    <text>Kiểm tra role ở service layer hoặc gateway, không chỉ client.</text>
                </rule>
                <rule id="AZ-002">
                    <level>MUST</level>
                    <text>Enforce <code>@PreAuthorize</code> hoặc custom annotation cho endpoint nhạy cảm.</text>
                </rule>
                <rule id="AZ-003">
                    <level>MUST NOT</level>
                    <text>Rely solely on client-side validation.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="account-lockout" num="12.4">
            <title>Account lockout</title>
            <rules>
                <rule id="AL-001">
                    <level>MUST</level>
                    <text>Implement lockout sau 5 lần đăng nhập sai (BR05).</text>
                </rule>
                <rule id="AL-002">
                    <level>MUST</level>
                    <text>Auto-unlock sau 30 phút (NSF-10) bằng scheduler.</text>
                </rule>
                <rule id="AL-003">
                    <level>MUST</level>
                    <text>Không tiết lộ email có tồn tại hay không qua error message (security).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="input-validation" num="12.5">
            <title>Input validation</title>
            <rules>
                <rule id="IV-001">
                    <level>MUST</level>
                    <text>Validate MỌI input từ client (path, query, body).</text>
                </rule>
                <rule id="IV-002">
                    <level>MUST</level>
                    <text>Dùng <code>@Valid</code> + Jakarta validation.</text>
                </rule>
                <rule id="IV-003">
                    <level>SHOULD</level>
                    <text>Sanitize input trước khi query DB (chống SQL injection — JPQL tự handle, nhưng cẩn thận native query).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="cors-csrf" num="12.6">
            <title>CORS &amp; CSRF</title>
            <rules>
                <rule id="CC-001">
                    <level>MUST</level>
                    <text>Cấu hình CORS cho phép domain frontend cụ thể (không <code>*</code> trong production).</text>
                </rule>
                <rule id="CC-002">
                    <level>MUST</level>
                    <text>Bật CSRF protection cho state-changing endpoint (sẽ implement trong phase 2).</text>
                </rule>
                <rule id="CC-003">
                    <level>SHOULD</level>
                    <text>Dùng HTTPS-only cookie cho refresh token.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 13: TESTING                                           -->
    <!-- ============================================================ -->
    <section id="testing" num="13" icon="🧪">
        <title>Testing</title>
        <warning level="critical">Hiện tại: 0 tests trong toàn project. Đây là technical debt nghiêm trọng cần giải quyết trong sprint tiếp theo.</warning>

        <subsection id="test-pyramid" num="13.1">
            <title>Test pyramid</title>
            <rules>
                <rule id="TP-001">
                    <level>MUST</level>
                    <text>Viết unit test cho mọi service method (mục tiêu 80% coverage).</text>
                </rule>
                <rule id="TP-002">
                    <level>MUST</level>
                    <text>Viết integration test cho mỗi controller (Slice test với <code>@WebMvcTest</code>).</text>
                </rule>
                <rule id="TP-003">
                    <level>SHOULD</level>
                    <text>Viết end-to-end test cho mỗi use case quan trọng (UC01-UC13) dùng Postman/Newman hoặc Testcontainers.</text>
                </rule>
                <rule id="TP-004">
                    <level>SHOULD</level>
                    <text>Viết contract test giữa các service giao tiếp qua Feign.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="unit-test" num="13.2">
            <title>Unit test rules</title>
            <rules>
                <rule id="UT-001">
                    <level>MUST</level>
                    <text>Dùng JUnit 5 (<code>@Test</code>, <code>@BeforeEach</code>, <code>@AfterEach</code>).</text>
                </rule>
                <rule id="UT-002">
                    <level>MUST</level>
                    <text>Dùng Mockito cho mock dependency.</text>
                </rule>
                <rule id="UT-003">
                    <level>MUST</level>
                    <text>Đặt tên test method theo pattern: <code>methodName_condition_expectedResult</code>. VD: <code>login_withInvalidPassword_throwsInvalidCredentialsException</code>.</text>
                </rule>
                <rule id="UT-004">
                    <level>MUST</level>
                    <text>Dùng AssertJ cho assertion (fluent, dễ đọc hơn JUnit).</text>
                </rule>
                <rule id="UT-005">
                    <level>MUST</level>
                    <text>Follow AAA pattern (Arrange-Act-Assert).</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
@Test
void create_withDuplicateEmail_throwsDuplicateResourceException() {
    // Arrange
    when(repository.existsByEmail("test@example.com")).thenReturn(true);
    CreateUserRequest request = new CreateUserRequest("test@example.com", "Test", null, Role.CUSTOMER, null);

    // Act & Assert
    assertThatThrownBy(() -> userService.create(request, "password"))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Email");
}
]]></example>
        </subsection>

        <subsection id="test-data" num="13.3">
            <title>Test data</title>
            <rules>
                <rule id="TD-001">
                    <level>MUST</level>
                    <text>Dùng builder pattern hoặc factory method để tạo test data (không hardcode JSON).</text>
                </rule>
                <rule id="TD-002">
                    <level>SHOULD</level>
                    <text>Dùng Testcontainers cho integration test với MySQL thực.</text>
                </rule>
                <rule id="TD-003">
                    <level>MUST NOT</level>
                    <text>Dùng data thật từ production trong test.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="coverage" num="13.4">
            <title>Coverage</title>
            <rules>
                <rule id="CV-001">
                    <level>MUST</level>
                    <text>Đạt ≥ 80% line coverage cho mọi service mới.</text>
                </rule>
                <rule id="CV-002">
                    <level>MUST</level>
                    <text>Đạt 100% coverage cho business logic trong <code>*ServiceImpl</code> quan trọng.</text>
                </rule>
                <rule id="CV-003">
                    <level>MUST NOT</level>
                    <text>Merge PR nếu giảm coverage dưới threshold.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="test-isolation" num="13.5">
            <title>Test isolation</title>
            <rules>
                <rule id="TI-001">
                    <level>MUST</level>
                    <text>Mỗi test độc lập, không phụ thuộc thứ tự chạy.</text>
                </rule>
                <rule id="TI-002">
                    <level>MUST</level>
                    <text>Reset state DB trước mỗi integration test (<code>@Transactional</code> + rollback, hoặc <code>@Sql</code>).</text>
                </rule>
                <rule id="TI-003">
                    <level>MUST NOT</level>
                    <text>Dùng static state giữa các test.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 14: PERFORMANCE                                       -->
    <!-- ============================================================ -->
    <section id="performance" num="14" icon="⚡">
        <title>Performance</title>

        <subsection id="db-perf" num="14.1">
            <title>Database</title>
            <rules>
                <rule id="DP-001">
                    <level>MUST</level>
                    <text>Có index cho các column thường xuyên query: FK, status, createdAt.</text>
                </rule>
                <rule id="DP-002">
                    <level>MUST</level>
                    <text>Tránh <code>SELECT *</code> — chỉ lấy field cần thiết (dùng projection hoặc DTO).</text>
                </rule>
                <rule id="DP-003">
                    <level>MUST</level>
                    <text>Tránh query trong vòng lặp (N+1).</text>
                </rule>
                <rule id="DP-004">
                    <level>SHOULD</level>
                    <text>Dùng pagination cho mọi list endpoint (đã enforce ở §6.4).</text>
                </rule>
                <rule id="DP-005">
                    <level>SHOULD</level>
                    <text>Dùng batch insert/update cho bulk operation (<code>@Modifying</code> + <code>clearAutomatically</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="caching" num="14.2">
            <title>Caching</title>
            <rules>
                <rule id="CH-001">
                    <level>SHOULD</level>
                    <text>Cache reference data (role list, category list) với Spring <code>@Cacheable</code> + Caffeine/Redis.</text>
                </rule>
                <rule id="CH-002">
                    <level>MUST NOT</level>
                    <text>Cache data thay đổi thường xuyên (order status, payment status) trừ khi có TTL ngắn.</text>
                </rule>
                <rule id="CH-003">
                    <level>MUST</level>
                    <text>Invalidate cache khi data thay đổi (<code>@CacheEvict</code>).</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="async" num="14.3">
            <title>Async &amp; concurrency</title>
            <rules>
                <rule id="AS-001">
                    <level>SHOULD</level>
                    <text>Dùng <code>@Async</code> cho operation không blocking (email, notification, audit log).</text>
                </rule>
                <rule id="AS-002">
                    <level>MUST</level>
                    <text>Cấu hình thread pool riêng cho async (không dùng default).</text>
                </rule>
                <rule id="AS-003">
                    <level>MUST NOT</level>
                    <text>Block controller thread với long-running task.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="memory" num="14.4">
            <title>Memory</title>
            <rules>
                <rule id="MM-001">
                    <level>MUST NOT</level>
                    <text>Load toàn bộ table lên memory (<code>findAll()</code> không phân trang).</text>
                </rule>
                <rule id="MM-002">
                    <level>MUST</level>
                    <text>Dùng stream (<code>Stream&lt;Entity&gt;</code> với <code>@QueryHints</code>) cho xử lý lớn.</text>
                </rule>
                <rule id="MM-003">
                    <level>MUST</level>
                    <text>Đóng resource (Stream, Connection) trong try-with-resources.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 15: DOCUMENTATION                                     -->
    <!-- ============================================================ -->
    <section id="documentation" num="15" icon="📖">
        <title>Documentation</title>

        <subsection id="javadoc" num="15.1">
            <title>Javadoc</title>
            <rules>
                <rule id="JD-001">
                    <level>MUST</level>
                    <text>Viết Javadoc cho mọi public class và public method ở service layer.</text>
                </rule>
                <rule id="JD-002">
                    <level>MUST</level>
                    <text>Ghi rõ UC reference trong Javadoc của controller method: <code>@author, @see, UCxx reference</code>.</text>
                </rule>
                <rule id="JD-003">
                    <level>SHOULD</level>
                    <text>Ghi precondition / postcondition / side-effect cho method phức tạp.</text>
                </rule>
            </rules>
            <example type="good" lang="java"><![CDATA[
/**
 * UC06 - Create new order.
 * <p>BR04: 5% discount when qty >= 10 same medicine.
 * <br>NSF-12: Auto-generate order number ORD-yyyymmdd-####.
 *
 * @param request order creation payload
 * @return created order with generated order number
 * @throws InvalidOperationException if items empty
 * @throws ResourceNotFoundException if medicine not found
 */
@Transactional
public OrderResponse create(CreateOrderRequest request) { ... }
]]></example>
        </subsection>

        <subsection id="inline-comment" num="15.2">
            <title>Inline comment</title>
            <rules>
                <rule id="IC-001">
                    <level>SHOULD</level>
                    <text>Comment cho logic phức tạp (regex, business rule, algorithm).</text>
                </rule>
                <rule id="IC-002">
                    <level>MUST NOT</level>
                    <text>Comment cho code hiển nhiên (<code>i++; // increment i</code>).</text>
                </rule>
                <rule id="IC-003">
                    <level>MUST NOT</level>
                    <text>Để lại commented-out code — xóa hẳn.</text>
                </rule>
                <rule id="IC-004">
                    <level>SHOULD</level>
                    <text>Link issue/task ID khi để TODO: <code>// TODO(PCMS-123): implement retry</code>.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="readme" num="15.3">
            <title>README</title>
            <rules>
                <rule id="RD-001">
                    <level>MUST</level>
                    <text>Update README.md khi thêm service mới hoặc thay đổi architecture.</text>
                </rule>
                <rule id="RD-002">
                    <level>MUST</level>
                    <text>Document mọi environment variable và config key trong README.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 16: GIT WORKFLOW                                      -->
    <!-- ============================================================ -->
    <section id="git-workflow" num="16" icon="🌿">
        <title>Git workflow</title>

        <subsection id="branch-naming" num="16.1">
            <title>Branch naming</title>
            <rules>
                <rule id="BN-001">
                    <level>MUST</level>
                    <text>Dùng format <code>&lt;type&gt;/&lt;ticket-id&gt;-&lt;short-description&gt;</code>.</text>
                </rule>
                <rule id="BN-002">
                    <level>MUST</level>
                    <text>Dùng các type: <code>feat/</code>, <code>fix/</code>, <code>refactor/</code>, <code>docs/</code>, <code>test/</code>, <code>chore/</code>, <code>hotfix/</code>.</text>
                </rule>
            </rules>
            <example type="good" lang="plain"><![CDATA[feat/PCMS-123-add-prescription-service]]></example>
            <example type="bad" lang="plain"><![CDATA[feature/newStuff, my-branch]]></example>
        </subsection>

        <subsection id="commit-message" num="16.2">
            <title>Commit message (Conventional Commits)</title>
            <rules>
                <rule id="CM-001">
                    <level>MUST</level>
                    <text>Tuân thủ Conventional Commits (https://www.conventionalcommits.org/).</text>
                </rule>
                <rule id="CM-002">
                    <level>MUST</level>
                    <text>Format: <code>&lt;type&gt;(&lt;scope&gt;): &lt;subject&gt;</code>.</text>
                </rule>
            </rules>
            <note>Type: feat, fix, refactor, docs, test, build, chore, perf, style, ci. Scope: module name (vd: user-service, order-service, pcms-common, pom). Subject: ngắn gọn, ≤ 72 ký tự, imperative ("add" không "added").</note>
            <example type="good" lang="plain"><![CDATA[
feat(order-service): add bulk discount for orders >= 10 items

Implement BR04: 5% discount when qty >= 10 same medicine.
Add order.bulk-discount-threshold and order.bulk-discount-rate config.
Generate order number ORD-yyyymmdd-#### per NSF-12.

Closes PCMS-456
]]></example>
            <example type="bad" lang="plain"><![CDATA[update code, fixed bug, WIP]]></example>
        </subsection>

        <subsection id="commit-hygiene" num="16.3">
            <title>Commit hygiene</title>
            <rules>
                <rule id="CH-001">
                    <level>MUST</level>
                    <text>Mỗi commit đơn lẻ cho một thay đổi logic (atomic commit).</text>
                </rule>
                <rule id="CH-002">
                    <level>MUST</level>
                    <text>Build pass trước khi commit (<code>mvn clean package -DskipTests</code>).</text>
                </rule>
                <rule id="CH-003">
                    <level>MUST NOT</level>
                    <text>Commit file binary (image, jar) — dùng Git LFS hoặc artifact repo.</text>
                </rule>
                <rule id="CH-004">
                    <level>MUST NOT</level>
                    <text>Commit <code>application-local.yml</code> chứa secret thật.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="pull-request" num="16.4">
            <title>Pull Request</title>
            <rules>
                <rule id="PR-001">
                    <level>MUST</level>
                    <text>Tạo PR khi code ready for review (không push trực tiếp main/develop).</text>
                </rule>
                <rule id="PR-002">
                    <level>MUST</level>
                    <text>PR title match Conventional Commits format.</text>
                </rule>
                <rule id="PR-003">
                    <level>MUST</level>
                    <text>PR description có: Context / why, What changed, How to test, Screenshots (nếu UI), Linked issue: <code>Closes #123</code> hoặc <code>Refs #456</code>.</text>
                </rule>
                <rule id="PR-004">
                    <level>MUST</level>
                    <text>Có ít nhất 1 reviewer approve trước khi merge.</text>
                </rule>
                <rule id="PR-005">
                    <level>MUST NOT</level>
                    <text>Self-merge PR (trừ hotfix production cần owner duyệt).</text>
                </rule>
                <rule id="PR-006">
                    <level>MUST</level>
                    <text>Dùng Squash and merge để giữ history sạch.</text>
                </rule>
            </rules>
        </subsection>

        <subsection id="gitignore" num="16.5">
            <title>.gitignore</title>
            <rules>
                <rule id="GI-001">
                    <level>MUST NOT</level>
                    <text>Commit <code>target/</code>, <code>*.class</code>, <code>*.jar</code>, <code>.idea/</code>, <code>*.iml</code>, <code>logs/</code>, <code>.env</code>, <code>application-local.yml</code>.</text>
                </rule>
            </rules>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 17: CODE REVIEW CHECKLIST                             -->
    <!-- ============================================================ -->
    <section id="code-review-checklist" num="17" icon="✅">
        <title>Code review checklist</title>
        <description>Mỗi PR MUST pass qua checklist này trước khi merge. Reviewer check từng mục.</description>

        <subsection id="checklist-quality" num="17.1">
            <title>Code quality</title>
            <checklist>
                <item>Code tuân thủ <code>STANDARDS.md</code> và <code>CODE_RULES.md</code></item>
                <item>Không có code trùng lặp (DRY)</item>
                <item>Không có dead code, commented-out code</item>
                <item>Không có magic number/string</item>
                <item>Không có System.out / printStackTrace</item>
                <item>Constructor injection (không @Autowired field)</item>
                <item>Không Lombok ở entity (viết tay getter/setter)</item>
                <item>DTO dùng record</item>
                <item>Tên biến/hàm có ý nghĩa, không viết tắt khó hiểu</item>
            </checklist>
        </subsection>

        <subsection id="checklist-architecture" num="17.2">
            <title>Architecture</title>
            <checklist>
                <item>Controller thin (không chứa business logic)</item>
                <item>Service có interface + impl</item>
                <item>Repository extend JpaRepository&lt;Entity, UUID&gt;</item>
                <item>Entity có UUID PK + audit fields + @EntityListeners</item>
                <item>Soft delete bằng status enum (không xóa cứng)</item>
                <item>DTO mapping qua static factory method</item>
                <item>Entity KHÔNG expose ra ngoài (luôn convert qua DTO)</item>
            </checklist>
        </subsection>

        <subsection id="checklist-error" num="17.3">
            <title>Error handling</title>
            <checklist>
                <item>Mọi business exception extend <code>BusinessException</code> (pcms-common)</item>
                <item>Mọi exception message có CẢ EN + VI</item>
                <item>Không throw <code>RuntimeException</code> raw</item>
                <item>GlobalExceptionHandler được scan (check <code>scanBasePackages</code>)</item>
            </checklist>
        </subsection>

        <subsection id="checklist-security" num="17.4">
            <title>Security</title>
            <checklist>
                <item>Password hash bằng BCrypt</item>
                <item>JWT có expiration</item>
                <item>Không log sensitive data</item>
                <item>Có validation cho MỌI input</item>
            </checklist>
        </subsection>

        <subsection id="checklist-testing" num="17.5">
            <title>Testing</title>
            <checklist>
                <item>Có unit test cho service method mới/thay đổi</item>
                <item>Coverage ≥ 80% cho file thay đổi</item>
                <item>Test pass trong CI</item>
                <item>Có test cho happy path + edge case + error case</item>
            </checklist>
        </subsection>

        <subsection id="checklist-documentation" num="17.6">
            <title>Documentation</title>
            <checklist>
                <item>Javadoc cho public class/method mới</item>
                <item>Javadoc có UC reference (nếu liên quan UC01-UC13)</item>
                <item>README update nếu thêm service / config</item>
            </checklist>
        </subsection>

        <subsection id="checklist-build" num="17.7">
            <title>Build &amp; CI</title>
            <checklist>
                <item><code>mvn clean package -DskipTests</code> thành công local</item>
                <item>Không có warning mới (warning cũ OK)</item>
                <item>Không import thừa</item>
                <item>Không vi phạm SpotBugs / Checkstyle (sẽ config ở phase 2)</item>
            </checklist>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SECTION 18: ANTI-PATTERNS CẤM                                 -->
    <!-- ============================================================ -->
    <section id="anti-patterns-cam" num="18" icon="🚫">
        <title>Anti-patterns cấm</title>
        <description>Danh sách các pattern CẤM trong PCMS. Nếu thấy trong code review → REJECT PR.</description>

        <subsection id="anti-arch" num="18.1">
            <title>Architecture anti-patterns</title>
            <anti-patterns>
                <item name="God class">class &gt; 500 dòng, làm quá nhiều việc.</item>
                <item name="Anemic domain model">entity chỉ có getter/setter, không có business method.</item>
                <item name="Fat controller">controller chứa business logic, validation phức tạp, gọi repository trực tiếp.</item>
                <item name="Repository trong controller">inject repository thẳng vào controller.</item>
                <item name="Service gọi service qua Feign đồng bộ trong transaction dài">dùng async + event.</item>
            </anti-patterns>
        </subsection>

        <subsection id="anti-jpa" num="18.2">
            <title>JPA anti-patterns</title>
            <anti-patterns>
                <item name="findAll() không phân trang">load toàn bộ table.</item>
                <item name="@Transactional trên controller">vi phạm layer rule.</item>
                <item name="@Transactional trên private method">không có tác dụng.</item>
                <item name="open-in-view: true">default Spring Boot, nhưng nên disable trong PCMS.</item>
                <item name="EAGER fetch trên collection">@OneToMany(fetch = EAGER).</item>
                <item name="Query trong loop">N+1.</item>
                <item name="Native SQL không cần thiết">luôn prefer JPQL.</item>
                <item name="Field injection với @Autowired trong service">vi phạm DI rule.</item>
            </anti-patterns>
        </subsection>

        <subsection id="anti-exception" num="18.3">
            <title>Exception anti-patterns</title>
            <anti-patterns>
                <item name="throw new RuntimeException(...)">dùng BusinessException subclass.</item>
                <item name="catch (Exception e) {}">nuốt exception im lặng.</item>
                <item name="throw new Exception(...)">không thể throw checked exception như vậy, dùng BusinessException.</item>
                <item name="e.printStackTrace()">dùng logger.</item>
                <item name="Throw exception chỉ để control flow">dùng return value hoặc Optional.</item>
            </anti-patterns>
        </subsection>

        <subsection id="anti-style" num="18.4">
            <title>Code style anti-patterns</title>
            <anti-patterns>
                <item name="System.out.println / System.err.println cho logging">dùng SLF4J.</item>
                <item name="Wildcard import (import java.util.*;)">IDE organize.</item>
                <item name="Lombok trong entity">viết tay getter/setter.</item>
                <item name="Hardcode secret/password trong code">dùng Config Server.</item>
                <item name="Hardcode URL/port">dùng Config Server.</item>
                <item name="Magic number (if (status == 3))">dùng enum.</item>
                <item name="Variable 1 ký tự (User u;)">dùng User user;.</item>
                <item name="Commented-out code để lại trong file">xóa hẳn.</item>
                <item name="TODO không có issue link">phải có ID.</item>
            </anti-patterns>
        </subsection>

        <subsection id="anti-api" num="18.5">
            <title>API anti-patterns</title>
            <anti-patterns>
                <item name="Verb trong URL (/getUsers, /createOrder)">dùng HTTP method.</item>
                <item name="Expose entity ra response (return user;)">dùng UserResponse.from(user).</item>
                <item name="Trả 200 OK cho error">đúng status code.</item>
                <item name="Không pagination cho list endpoint">bắt buộc pagination.</item>
                <item name="Hardcode page size > 100">clamp tại 100.</item>
                <item name="Thiếu @Valid trên @RequestBody">bắt buộc @Valid.</item>
            </anti-patterns>
        </subsection>

        <subsection id="anti-security" num="18.6">
            <title>Security anti-patterns</title>
            <anti-patterns>
                <item name="Log password / token / secret ở bất kỳ level nào">cấm log sensitive data.</item>
                <item name="Lưu password plain text">dùng BCrypt.</item>
                <item name="Disable CSRF trong production">bật CSRF protection.</item>
                <item name="permitAll() cho endpoint nhạy cảm trong production">cấm.</item>
                <item name="Trust client-side validation">luôn validate ở server.</item>
                <item name="Hardcode JWT secret trong code">qua Config Server.</item>
            </anti-patterns>
        </subsection>

        <subsection id="anti-test" num="18.7">
            <title>Testing anti-patterns</title>
            <anti-patterns>
                <item name="Test phụ thuộc thứ tự chạy">không isolate.</item>
                <item name="Test dùng data production thật">dùng test data.</item>
                <item name="Skip test trong CI (-DskipTests cho PR)">cấm.</item>
                <item name="Test chỉ assert happy path">test cả error case.</item>
                <item name="Test ngủ (Thread.sleep)">dùng Awaitility.</item>
            </anti-patterns>
        </subsection>
    </section>

    <!-- ============================================================ -->
    <!-- SUMMARY — MUST / MUST NOT                                     -->
    <!-- ============================================================ -->
    <summary>
        <must-list count="35" label="BẮT BUỘC">
            <item>Dùng UUID cho mọi ID</item>
            <item>Dùng <code>record</code> cho DTO</item>
            <item>Constructor injection (không <code>@Autowired</code> field)</item>
            <item><code>@Transactional</code> ở service (không ở controller)</item>
            <item>Bilingual messages (EN + VI) cho mọi exception</item>
            <item>Extend <code>BusinessException</code> (không throw raw <code>RuntimeException</code>)</item>
            <item>SLF4J logging (không <code>System.out</code> / <code>printStackTrace</code>)</item>
            <item>OpenFeign + <code>@CircuitBreaker</code> + fallback cho inter-service</item>
            <item>BCrypt password + JWT cho security</item>
            <item>Validation cho MỌI input</item>
            <item>Conventional Commits cho commit message</item>
            <item>Code review checklist trước merge</item>
            <item>... và 23 luật MUST khác</item>
        </must-list>
        <must-not-list count="30" label="CẤM">
            <item>Lombok ở entity</item>
            <item>Hardcode secret/password/URL</item>
            <item><code>System.out</code> / <code>printStackTrace</code> / <code>System.err</code></item>
            <item>Throw raw <code>RuntimeException</code> / <code>Exception</code></item>
            <item><code>findAll()</code> không phân trang</item>
            <item>EAGER fetch trên collection</item>
            <item><code>@Transactional</code> trên controller / private method</item>
            <item>Expose entity ra API response</item>
            <item>Verb trong URL</item>
            <item>Log sensitive data</item>
            <item>Skip test trong CI</item>
            <item>... và 19 luật MUST NOT khác</item>
        </must-not-list>
    </summary>

    <!-- ============================================================ -->
    <!-- UPDATE PROCESS                                                -->
    <!-- ============================================================ -->
    <update-process>
        <step num="1">Mọi thay đổi luật phải qua team review + Tech Lead approval.</step>
        <step num="2">Versioning tài liệu theo semver: 1.0.0 → 1.1.0 (thêm luật) → 2.0.0 (breaking change).</step>
        <step num="3">Migration plan PHẢI kèm theo nếu thay đổi breaking.</step>
        <step num="4">CI enforcement sẽ được bổ sung trong phase 2 (Checkstyle, SpotBugs, ArchUnit).</step>
    </update-process>

    <!-- ============================================================ -->
    <!-- COMMITMENT                                                    -->
    <!-- ============================================================ -->
    <commitment>
        <statement>Bằng việc merge PR vào develop / main, mỗi thành viên cam kết đã đọc, hiểu và tuân thủ toàn bộ luật trong tài liệu này.</statement>
        <effective-date>2026-06-16</effective-date>
        <review-cycle>Mỗi quarter</review-cycle>
    </commitment>
</code-rules>
