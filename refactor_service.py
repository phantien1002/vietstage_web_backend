import os
import re

base_dir = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be"

def replace_in_file(rel_path, old_str, new_str):
    path = os.path.join(base_dir, rel_path)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        if old_str in content:
            content = content.replace(old_str, new_str)
            with open(path, "w", encoding="utf-8") as f:
                f.write(content)
            print(f"Updated {rel_path}")

# 1. Update LearnerCosmeticResponse.java
replace_in_file("dto/response/LearnerCosmeticResponse.java", "String unlockType;", "")
replace_in_file("dto/response/LearnerCosmeticResponse.java", "Integer unlockValue;", "Integer starPrice;")

# 2. Update CosmeticsServiceImpl.java
path = os.path.join(base_dir, "service", "impl", "CosmeticsServiceImpl.java")
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix getMyCosmetics
content = content.replace(".unlockType(lc.getCosmeticItem().getUnlockType())", "")
content = content.replace(".unlockValue(lc.getCosmeticItem().getUnlockValue())", ".starPrice(lc.getCosmeticItem().getStarPrice())")

# Fix equipCosmetic (allow equipping INACTIVE if owned - actually equipCosmetic shouldn't check INACTIVE if it's already owned? Wait, requirement: "Chỉ cho dùng vật phẩm đã sở hữu... Vật phẩm đã chuyển INACTIVE nhưng người học đã sở hữu vẫn phải giữ quyền sở hữu và layout". So equip should NOT fail for INACTIVE.)
content = content.replace("""
        if (!"ACTIVE".equals(targetCosmetic.getCosmeticItem().getStatus())) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Should not equip inactive items
        }
""", "")

# Fix purchaseCosmetic
content = content.replace("item.getUnlockValue()", "item.getStarPrice()")

# Fix saveCosmeticLayout (allow INACTIVE items)
content = content.replace(""".filter(lc -> "ACTIVE".equals(lc.getCosmeticItem().getStatus()))
                    """, "")
content = content.replace("""// Item not owned or inactive""", """// Item not owned""")

# Fix createCosmeticItem
content = content.replace(".unlockType(request.getUnlockType() != null ? request.getUnlockType() : \"STARS\")", "")
content = content.replace(".unlockValue(request.getUnlockValue() != null ? request.getUnlockValue() : 0)", ".starPrice(request.getStarPrice() != null ? request.getStarPrice() : 0)")

# Fix updateCosmeticItem
content = content.replace("if (request.getUnlockType() != null) item.setUnlockType(request.getUnlockType());", "")
content = content.replace("if (request.getUnlockValue() != null) item.setUnlockValue(request.getUnlockValue());", "if (request.getStarPrice() != null) item.setStarPrice(request.getStarPrice());")

# Fix mapToResponse
content = content.replace(".unlockType(item.getUnlockType())", "")
content = content.replace(".unlockValue(item.getUnlockValue())", ".starPrice(item.getStarPrice())")

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("Updated CosmeticsServiceImpl")
