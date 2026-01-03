package com.example.insurance

import org.bukkit.inventory.ItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

/**
 * 粘液科技（Slimefun）兼容性处理类
 * 解决保险物品与各种合成系统不兼容的问题
 */
class SlimefunCompatibility : Listener {
    
    /**
     * 处理原版合成工作台中的保险物品
     * 在合成预处理阶段临时移除保险数据，使合成系统能正确识别物品
     */
    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val inventory = event.inventory
        
        // 检查合成网格中的物品是否带有保险
        for (i in 0 until inventory.matrix.size) {
            val item = inventory.matrix[i]
            if (item != null && InsurancePlugin.instance.hasInsurance(item)) {
                // 使用CraftingHelper为合成准备物品
                val tempItem = CraftingHelper.prepareItemForCrafting(item)
                inventory.matrix[i] = tempItem
            }
        }
    }
    
    /**
     * 处理合成完成事件，确保结果物品继承保险信息
     */
    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val recipe = event.recipe ?: return
        val result = recipe.result
        
        // 使用CraftingHelper转移保险信息到结果物品
        val finalResult = CraftingHelper.transferInsuranceToResult(event.inventory.matrix, result)
        event.inventory.result = finalResult
    }
    
    /**
     * 处理容器点击事件，防止在Slimefun机器等特殊容器中出现兼容性问题
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        
        // 检查是否是特殊合成容器（如Slimefun机器）
        if (inventory.type == InventoryType.WORKBENCH || 
            inventory.type == InventoryType.CRAFTING || 
            isSlimefunInventory(inventory)) {
            
            val currentItem = event.currentItem
            if (currentItem != null && InsurancePlugin.instance.hasInsurance(currentItem)) {
                // 在特殊容器中，可能需要特殊处理保险物品
                // 这里可以添加针对特定机器的逻辑
            }
        }
    }
    
    /**
     * 检查是否是Slimefun的特殊容器
     */
    private fun isSlimefunInventory(inventory: org.bukkit.inventory.Inventory): Boolean {
        // 这里检查是否是Slimefun的机器
        // 具体实现取决于Slimefun的API版本
        try {
            // 尝试检查是否为Slimefun机器
            return false // 简化实现，实际应用中需要根据Slimefun API检查
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * 为合成临时移除保险数据
     * 这是解决合成系统兼容性问题的关键方法
     */
    private fun removeInsuranceDataForCrafting(item: ItemStack): ItemStack {
        val clone = item.clone()
        val meta = clone.itemMeta ?: return clone
        
        // 移除保险相关的持久数据
        val insuranceLevelKey = org.bukkit.NamespacedKey(InsurancePlugin.instance, "insurance_level")
        val insuranceUsesKey = org.bukkit.NamespacedKey(InsurancePlugin.instance, "insurance_uses")
        
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
    
    /**
     * 在合成系统中添加特殊处理，允许保险物品参与Slimefun合成
     */
    fun enableSlimefunIntegration() {
        // 这里可以注册与Slimefun的深度集成
        // 比如添加特殊的合成规则或API钩子
        println("Slimefun integration enabled for insurance system")
    }
}