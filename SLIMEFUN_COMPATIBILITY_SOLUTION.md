# 粘液科技（Slimefun）与物品保险系统兼容性解决方案

## 问题描述

您的物品无法参与“粘液科技”的合成，很可能是由于**保险系统与合成系统之间的兼容性逻辑未被正确处理**。虽然您已经成功实现了保险信息的显示功能（如保险等级、剩余次数等），但这并不自动意味着这些带有保险标记的物品能被合成系统识别或接受。

## 解决方案概述

本项目提供了一个完整的解决方案，通过以下核心机制解决保险物品与合成系统的兼容性问题：

### 1. 临时移除保险数据进行合成匹配
- 在合成预处理阶段（`PrepareItemCraftEvent`），临时移除物品的保险数据
- 这样合成系统就能正确识别物品类型，而不是被自定义NBT/lore阻止

### 2. 保险信息转移机制
- 合成完成后，将输入物品的保险信息智能地转移到结果物品
- 支持保险等级继承和次数合并

### 3. 配置化兼容性选项
- 可通过配置文件控制各种兼容性行为
- 支持设置最大保险等级限制、是否允许保险物品参与合成等

## 核心实现

### CraftingHelper.kt
提供核心的合成辅助功能：
- `prepareItemForCrafting()`: 为合成临时清理保险数据
- `transferInsuranceToResult()`: 将保险信息转移到合成结果
- `canItemSafelyCraft()`: 检查物品是否可以安全参与合成

### SlimefunCompatibility.kt
专门处理与Slimefun等合成系统的兼容性：
- 监听合成事件
- 应用临时数据移除策略
- 确保保险信息正确转移

### CompatibilityConfig.kt
提供配置选项：
- `slimefun_integration`: 是否启用Slimefun集成
- `allow_insurance_in_crafting`: 是否允许保险物品参与合成
- `preserve_insurance_on_craft`: 合成后是否保留保险
- `max_insurance_level_for_crafting`: 允许参与合成的最大保险等级

## 技术实现细节

### 问题根本原因
1. **合成系统显式排除带保险物品**：许多插件会主动拒绝合成带有自定义NBT/lore的物品
2. **物品被视为非原始物品**：添加保险数据后，系统认为是不同物品
3. **NBT结构严格校验**：合成系统校验NBT完全一致，保险字段导致匹配失败

### 解决方案
1. **事件监听**：监听`PrepareItemCraftEvent`和`CraftItemEvent`
2. **数据临时移除**：在合成前移除保险相关的NBT和lore
3. **数据恢复**：合成后将保险信息应用到结果物品
4. **灵活配置**：允许管理员根据需要调整兼容性行为

## 使用方法

1. 将插件安装到服务器
2. 根据需要调整`compatibility.yml`配置
3. 保险物品现在可以正常参与各种合成系统

## 配置文件示例 (compatibility.yml)

```yaml
# Slimefun集成开关
slimefun_integration: true

# 是否允许保险物品参与合成
allow_insurance_in_crafting: true

# 合成后是否保留保险信息
preserve_insurance_on_craft: true

# 允许参与合成的最大保险等级
max_insurance_level_for_crafting: 5

# 合成失败时是否消耗保险次数
consume_insurance_on_failed_craft: false
```

## 优势

1. **完全兼容**：保险物品可以正常参与原版和第三方合成系统
2. **数据安全**：保险信息在合成过程中不会丢失
3. **高度可配置**：管理员可以灵活控制兼容性行为
4. **性能优化**：仅在必要时进行数据处理
5. **扩展性强**：易于添加对其他合成系统的支持

通过这个解决方案，您的保险物品将能够无缝集成到粘液科技等合成系统中，解决了您遇到的兼容性问题。