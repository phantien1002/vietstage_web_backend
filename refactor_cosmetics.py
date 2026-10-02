import os
import re

base_dir = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be"

def replace_in_file(rel_path, old_str, new_str):
    path = os.path.join(base_dir, rel_path)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        content = content.replace(old_str, new_str)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {rel_path}")
    else:
        print(f"Not found: {rel_path}")

def regex_replace_in_file(rel_path, pattern, new_str):
    path = os.path.join(base_dir, rel_path)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        content = re.sub(pattern, new_str, content)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Regex updated {rel_path}")

# 1. Update CosmeticItem.java
entity_path = os.path.join(base_dir, "entity", "CosmeticItem.java")
with open(entity_path, "r", encoding="utf-8") as f:
    entity_content = f.read()

entity_content = re.sub(r"private String unlockType;", "", entity_content)
entity_content = re.sub(r"private Integer unlockValue;", "private Integer starPrice;", entity_content)
with open(entity_path, "w", encoding="utf-8") as f:
    f.write(entity_content)

# 2. Update LearnerCosmetic.java (Add clientRequestId)
learner_cosmetic_path = os.path.join(base_dir, "entity", "LearnerCosmetic.java")
with open(learner_cosmetic_path, "r", encoding="utf-8") as f:
    lc_content = f.read()

if "clientRequestId" not in lc_content:
    lc_content = lc_content.replace(
        "private Boolean isEquipped;",
        "private Boolean isEquipped;\n\n    @Column(name = \"client_request_id\")\n    private String clientRequestId;"
    )
    with open(learner_cosmetic_path, "w", encoding="utf-8") as f:
        f.write(lc_content)

# 3. Update DTOs
dtos = [
    ("dto/request/CreateCosmeticRequest.java", "String unlockType;", ""),
    ("dto/request/CreateCosmeticRequest.java", "Integer unlockValue;", "Integer starPrice;"),
    ("dto/request/UpdateCosmeticRequest.java", "String unlockType;", ""),
    ("dto/request/UpdateCosmeticRequest.java", "Integer unlockValue;", "Integer starPrice;"),
    ("dto/response/CosmeticItemResponse.java", "String unlockType;", ""),
    ("dto/response/CosmeticItemResponse.java", "Integer unlockValue;", "Integer starPrice;"),
]
for p, o, n in dtos:
    replace_in_file(p, o, n)

replace_in_file("dto/request/PurchaseCosmeticRequest.java", "public class PurchaseCosmeticRequest {", "public class PurchaseCosmeticRequest {\n    private String clientRequestId;")


# 4. Update AdminController
admin_controller_code = """
    @GetMapping("/cosmetics")
    @Operation(summary = "Lấy danh sách CosmeticItem (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CosmeticItemResponse>>> getCosmetics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<CosmeticItemResponse>>builder()
                .message("Thành công")
                .data(cosmeticsService.getAllCosmetics(page, size))
                .build());
    }

    @PostMapping("/cosmetics")
    @Operation(summary = "Tạo mới CosmeticItem (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CosmeticItemResponse>> createCosmetic(
            @Valid @RequestBody CreateCosmeticRequest request) {
        return ResponseEntity.ok(ApiResponse.<CosmeticItemResponse>builder()
                .message("Thành công")
                .data(cosmeticsService.createCosmetic(request))
                .build());
    }

    @PutMapping("/cosmetics/{id}")
    @Operation(summary = "Cập nhật CosmeticItem (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CosmeticItemResponse>> updateCosmetic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCosmeticRequest request) {
        return ResponseEntity.ok(ApiResponse.<CosmeticItemResponse>builder()
                .message("Thành công")
                .data(cosmeticsService.updateCosmetic(id, request))
                .build());
    }

    @DeleteMapping("/cosmetics/{id}")
    @Operation(summary = "Xóa CosmeticItem (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCosmetic(@PathVariable Long id) {
        cosmeticsService.deleteCosmetic(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Thành công")
                .build());
    }
"""
admin_path = os.path.join(base_dir, "controller", "AdminController.java")
with open(admin_path, "r", encoding="utf-8") as f:
    admin_content = f.read()

if "/cosmetics" not in admin_content:
    admin_content = admin_content.replace(
        "import org.springframework.web.bind.annotation.*;",
        "import org.springframework.web.bind.annotation.*;\nimport com.example.vietstage_web_be.dto.response.*;\nimport com.example.vietstage_web_be.dto.request.*;\nimport com.example.vietstage_web_be.service.ICosmeticsService;"
    )
    admin_content = admin_content.replace(
        "private final IAdminReviewService adminReviewService;",
        "private final IAdminReviewService adminReviewService;\n    private final ICosmeticsService cosmeticsService;"
    )
    admin_content = admin_content.rsplit("}", 1)[0] + admin_controller_code + "}\n"
    with open(admin_path, "w", encoding="utf-8") as f:
        f.write(admin_content)

print("Entities, DTOs, and AdminController modified.")
