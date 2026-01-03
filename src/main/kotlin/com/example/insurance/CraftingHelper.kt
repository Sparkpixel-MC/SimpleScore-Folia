package com.example.insurance

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.ChatColor
import org.bukkit.Material

/**
 * 合成辅助工具类
 * 提供处理保险物品参与合成的各种工具方法
 */
object CraftingHelper {
    
    /**
     * 为合成临时清理物品的保险数据
     * 这是解决合成系统无法识别带保险物品的核心方法
     */
    fun prepareItemForCrafting(item: ItemStack): ItemStack {
        val config = CompatibilityConfig.instance
        
        // 如果配置不允许带保险的物品参与合成，则直接返回
        if (!config.allowInsuranceInCrafting) {
            return item
        }
        
        val clone = item.clone()
        val meta = clone.itemMeta ?: return clone
        
        // 获取保险信息
        val insuranceLevelKey = NamespacedKey(InsurancePlugin.instance, "insurance_level")
        val insuranceUsesKey = NamespacedKey(InsurancePlugin.instance, "insurance_uses")
        
        val hasInsurance = meta.persistentDataContainer.has(insuranceLevelKey, PersistentDataType.INTEGER)
        
        if (hasInsurance) {
            val insuranceLevel = meta.persistentDataContainer.get(insuranceLevelKey, PersistentDataType.INTEGER) ?: 0
            
            // 检查保险等级是否超过允许的最大值
            if (!config.isInsuranceLevelAllowedForCrafting(insuranceLevel)) {
                return item // 不允许此等级的保险参与合成
            }
            
            // 移除保险相关的持久数据
            meta.persistentDataContainer.remove(insuranceLevelKey)
            meta.persistentDataContainer.remove(insuranceUsesKey)
            
            // 移除可能影响合成的lore
            val lore = meta.lore
            if (lore != null) {
                val filteredLore = lore.filter { 
                    !it.contains("保险") && 
                    !it.contains("Insurance") && 
                    !it.contains("Level") && 
                    !it.contains("等级") &&
                    !it.contains("Uses") &&
                    !it.contains("次数")
                }
                if (filteredLore.size != lore.size) {
                    meta.lore = filteredLore
                }
            }
            
            clone.itemMeta = meta
        }
        
        return clone
    }
    
    /**
     * 将保险信息从输入物品转移到输出物品
     */
    fun transferInsuranceToResult(inputItems: Array<ItemStack?>, result: ItemStack): ItemStack {
        var highestLevel = 0
        var totalUses = 0
        var insuranceItemCount = 0
        
        for (item in inputItems) {
            if (item != null && InsurancePlugin.instance.hasInsurance(item)) {
                val level = InsurancePlugin.instance.getInsuranceLevel(item)
                val uses = InsurancePlugin.instance.getInsuranceUses(item)
                
                if (level > highestLevel) {
                    highestLevel = level
                }
                totalUses += uses
                insuranceItemCount++
                
                // 消耗输入物品的保险次数
                InsurancePlugin.instance.consumeInsurance(item)
            }
        }
        
        // 如果有带保险的输入物品，将保险应用到结果物品
        if (insuranceItemCount > 0 && highestLevel > 0) {
            return InsurancePlugin.instance.setInsurance(result, highestLevel, totalUses)
        }
        
        return result
    }
    
    /**
     * 检查物品是否可以安全参与合成（不会被合成系统拒绝）
     */
    fun canItemSafelyCraft(item: ItemStack): Boolean {
        val config = CompatibilityConfig.instance
        
        if (!config.allowInsuranceInCrafting) {
            return !InsurancePlugin.instance.hasInsurance(item)
        }
        
        if (!InsurancePlugin.instance.hasInsurance(item)) {
            return true
        }
        
        val level = InsurancePlugin.instance.getInsuranceLevel(item)
        return config.isInsuranceLevelAllowedForCrafting(level)
    }
    
    /**
     * 获取物品的保险摘要信息
     */
    fun getInsuranceSummary(item: ItemStack): String {
        if (!InsurancePlugin.instance.hasInsurance(item)) {
            return "无保险"
        }
        
        val level = InsurancePlugin.instance.getInsuranceLevel(item)
        val uses = InsurancePlugin.instance.getInsuranceUses(item)
        
        return "${ChatColor.YELLOW}保险等级: ${ChatColor.WHITE}${level}${ChatColor.YELLOW}, 剩余次数: ${ChatColor.WHITE}${uses}"
    }
    
    /**
     * 为合成界面显示创建保险物品的简化版本（隐藏保险信息）
     */
    fun createDisplayVersion(item: ItemStack): ItemStack {
        val clone = item.clone()
        val meta = clone.itemMeta ?: return clone
        
        // 移除可能影响显示的保险相关lore，但保留物品的其他特性
        val lore = meta.lore
        if (lore != null) {
            val filteredLore = lore.filter { 
                !it.contains("保险") && 
                !it.contains("Insurance") && 
                !it.contains("Level") && 
                !it.contains("等级") &&
                !it.contains("Uses") &&
                !it.contains("次数")
            }
            if (filteredLore.size != lore.size) {
                meta.lore = filteredLore
            }
        }
        
        clone.itemMeta = meta
        return clone
    }
}