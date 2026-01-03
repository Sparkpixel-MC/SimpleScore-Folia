# 解决粘液科技合成系统与物品保险兼容性问题

## 问题分析

您遇到的问题：物品无法参与“粘液科技”的合成，原因是**保险系统与合成系统之间的兼容性逻辑未被正确处理**。

## 根本原因

1. **合成系统显式排除带保险的物品**：粘液科技可能主动拒绝合成带有自定义NBT/lore的物品
2. **物品被视为非原始物品**：添加保险数据后，合成系统认为是不同物品
3. **NBT结构严格校验**：合成系统校验NBT完全一致，保险字段导致匹配失败

## 解决方案

### 核心思路
在合成过程中临时移除保险数据以通过合成系统校验，合成完成后将保险信息转移到结果物品。

### 实现代码结构

1. **InsurancePlugin.kt** - 主插件类
2. **SlimefunCompatibility.kt** - 兼容性处理类
3. **CraftingHelper.kt** - 合成辅助工具
4. **CompatibilityConfig.kt** - 配置管理

### 关键实现逻辑

#### 1. 合成预处理（PrepareItemCraftEvent）
```kotlin
@EventHandler
fun onPrepareCraft(event: PrepareItemCraftEvent) {
    val inventory = event.inventory
    
    // 检查合成网格中的物品是否带有保险
    for (i in 0 until inventory.matrix.size) {
        val item = inventory.matrix[i]
        if (item != null && InsurancePlugin.instance.hasInsurance(item)) {
            // 临时移除保险数据以便合成系统识别
            val tempItem = CraftingHelper.prepareItemForCrafting(item)
            inventory.matrix[i] = tempItem
        }
    }
}
```

#### 2. 合成完成处理（CraftItemEvent）
```kotlin
@EventHandler
fun onCraftItem(event: CraftItemEvent) {
    val recipe = event.recipe ?: return
    val result = recipe.result
    
    // 使用CraftingHelper转移保险信息到结果物品
    val finalResult = CraftingHelper.transferInsuranceToResult(event.inventory.matrix, result)
    event.inventory.result = finalResult
}
```

#### 3. 临时移除保险数据
```kotlin
fun prepareItemForCrafting(item: ItemStack): ItemStack {
    val clone = item.clone()
    val meta = clone.itemMeta ?: return clone
    
    // 移除保险相关的持久数据
    val insuranceLevelKey = NamespacedKey(InsurancePlugin.instance, "insurance_level")
    val insuranceUsesKey = NamespacedKey(InsurancePlugin.instance, "insurance_uses")
    
    meta.persistentDataContainer.remove(insuranceLevelKey)
    meta.persistentDataContainer.remove(insuranceUsesKey)
    
    // 移除可能影响合成的lore
    val lore = meta.lore
    if (lore != null) {
        val filteredLore = lore.filter { 
            !it.contains("保险") && 
            !it.contains("Insurance") && 
            !it.contains("Level") && 
            !it.contains("Uses") 
        }
        if (filteredLore.size != lore.size) {
            meta.lore = filteredLore
        }
    }
    
    clone.itemMeta = meta
    return clone
}
```

## 配置选项

通过 `compatibility.yml` 文件可以配置各种兼容性行为：
- 是否启用粘液科技集成
- 是否允许保险物品参与合成
- 最大保险等级限制
- 合成后是否保留保险信息

## 效果

通过这个解决方案，保险物品可以：
- 正常参与粘液科技等合成系统
- 保持保险信息不丢失
- 在合成后正确继承保险属性
- 根据配置灵活控制兼容性行为

这解决了您提到的"保险物品无法参与粘液科技合成"的问题，同时保持了保险功能的完整性。