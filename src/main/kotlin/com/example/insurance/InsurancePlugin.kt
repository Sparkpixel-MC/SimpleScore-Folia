package com.example.insurance

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.CraftItemEvent

class InsurancePlugin : JavaPlugin(), Listener {
    
    lateinit var compatibilityConfig: CompatibilityConfig
        private set
    lateinit var slimefunCompatibility: SlimefunCompatibility
        private set
    
    companion object {
        lateinit var instance: InsurancePlugin
            private set
    }
    
    override fun onEnable() {
        instance = this
        logger.info("InsurancePlugin已启用")
        
        // 初始化配置
        compatibilityConfig = CompatibilityConfig(this)
        CompatibilityConfig.instance = compatibilityConfig
        
        // 注册事件监听器
        server.pluginManager.registerEvents(this, this)
        
        // 初始化Slimefun兼容性处理（如果启用）
        if (compatibilityConfig.slimefunIntegration) {
            slimefunCompatibility = SlimefunCompatibility()
            server.pluginManager.registerEvents(slimefunCompatibility, this)
            slimefunCompatibility.enableSlimefunIntegration()
        }
        
        logger.info("InsurancePlugin已成功启用，兼容性配置已加载")
    }
    
    override fun onDisable() {
        logger.info("InsurancePlugin已禁用")
    }
    
    // 保险相关键值
    private val INSURANCE_LEVEL_KEY = NamespacedKey(this, "insurance_level")
    private val INSURANCE_USES_KEY = NamespacedKey(this, "insurance_uses")
    
    /**
     * 检查物品是否带有保险
     */
    fun hasInsurance(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(INSURANCE_LEVEL_KEY, PersistentDataType.INTEGER)
    }
    
    /**
     * 获取物品保险等级
     */
    fun getInsuranceLevel(item: ItemStack): Int {
        val meta = item.itemMeta ?: return 0
        return meta.persistentDataContainer.get(INSURANCE_LEVEL_KEY, PersistentDataType.INTEGER) ?: 0
    }
    
    /**
     * 获取物品剩余保险次数
     */
    fun getInsuranceUses(item: ItemStack): Int {
        val meta = item.itemMeta ?: return 0
        return meta.persistentDataContainer.get(INSURANCE_USES_KEY, PersistentDataType.INTEGER) ?: 0
    }
    
    /**
     * 设置物品保险
     */
    fun setInsurance(item: ItemStack, level: Int, uses: Int): ItemStack {
        val meta = item.itemMeta ?: return item
        meta.persistentDataContainer.set(INSURANCE_LEVEL_KEY, PersistentDataType.INTEGER, level)
        meta.persistentDataContainer.set(INSURANCE_USES_KEY, PersistentDataType.INTEGER, uses)
        item.itemMeta = meta
        return item
    }
    
    /**
     * 消耗一次保险
     */
    fun consumeInsurance(item: ItemStack): Boolean {
        if (!hasInsurance(item)) return false
        
        val meta = item.itemMeta ?: return false
        val currentUses = getInsuranceUses(item)
        
        if (currentUses <= 0) return false
        
        meta.persistentDataContainer.set(INSURANCE_USES_KEY, PersistentDataType.INTEGER, currentUses - 1)
        item.itemMeta = meta
        return true
    }
    
    /**
     * 合成事件处理 - 解决保险物品无法参与合成的问题
     */
    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val inventory = event.inventory
        
        // 检查合成网格中的物品是否带有保险
        for (i in 0 until inventory.matrix.size) {
            val item = inventory.matrix[i]
            if (item != null && hasInsurance(item)) {
                // 临时移除保险数据以便合成系统识别
                val tempItem = removeInsuranceData(item)
                inventory.matrix[i] = tempItem
            }
        }
    }
    
    /**
     * 合成完成事件处理
     */
    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val recipe = event.recipe ?: return
        val result = recipe.result
        
        // 检查是否需要继承保险（例如，相同类型的物品合成）
        val matrix = event.inventory.matrix
        
        // 查找是否有带保险的输入物品，决定是否将保险应用到结果物品
        var highestInsuranceLevel = 0
        var totalInsuranceUses = 0
        var insuranceItemsCount = 0
        
        for (item in matrix) {
            if (item != null && hasInsurance(item)) {
                val level = getInsuranceLevel(item)
                val uses = getInsuranceUses(item)
                
                if (level > highestInsuranceLevel) {
                    highestInsuranceLevel = level
                }
                totalInsuranceUses += uses
                insuranceItemsCount++
                
                // 消耗保险次数
                consumeInsurance(item)
            }
        }
        
        // 如果有带保险的输入物品，将保险应用到结果物品
        if (highestInsuranceLevel > 0 && insuranceItemsCount > 0) {
            setInsurance(result, highestInsuranceLevel, totalInsuranceUses)
            event.inventory.result = result
        }
    }
    
    /**
     * 临时移除保险数据，用于合成匹配
     */
    private fun removeInsuranceData(item: ItemStack): ItemStack {
        val clone = item.clone()
        val meta = clone.itemMeta ?: return clone
        
        // 移除保险相关的持久数据
        meta.persistentDataContainer.remove(INSURANCE_LEVEL_KEY)
        meta.persistentDataContainer.remove(INSURANCE_USES_KEY)
        
        // 也移除可能影响合成的lore
        val lore = meta.lore
        if (lore != null) {
            val filteredLore = lore.filter { !it.contains("保险") && !it.contains("Insurance") }
            if (filteredLore.size != lore.size) {
                meta.lore = filteredLore
            }
        }
        
        clone.itemMeta = meta
        return clone
    }
}