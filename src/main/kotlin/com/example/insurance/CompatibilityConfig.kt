package com.example.insurance

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * 兼容性配置类
 * 管理与不同合成系统（如Slimefun、GregTech等）的兼容性设置
 */
class CompatibilityConfig(plugin: InsurancePlugin) {
    
    private val configFile = File(plugin.dataFolder, "compatibility.yml")
    private var config: FileConfiguration = YamlConfiguration.loadConfiguration(configFile)
    
    // 兼容性选项
    var slimefunIntegration: Boolean = true
    var allowInsuranceInCrafting: Boolean = true
    var preserveInsuranceOnCraft: Boolean = true
    var maxInsuranceLevelForCrafting: Int = 5
    var consumeInsuranceOnFailedCraft: Boolean = false
    
    init {
        loadConfig()
    }
    
    /**
     * 加载配置文件
     */
    private fun loadConfig() {
        // 设置默认值
        config.addDefault("slimefun_integration", true)
        config.addDefault("allow_insurance_in_crafting", true)
        config.addDefault("preserve_insurance_on_craft", true)
        config.addDefault("max_insurance_level_for_crafting", 5)
        config.addDefault("consume_insurance_on_failed_craft", false)
        
        // 从文件读取配置
        if (!configFile.exists()) {
            plugin.dataFolder.mkdirs()
            config.options().copyDefaults(true)
            saveConfig()
        } else {
            config = YamlConfiguration.loadConfiguration(configFile)
        }
        
        // 读取配置值
        slimefunIntegration = config.getBoolean("slimefun_integration", true)
        allowInsuranceInCrafting = config.getBoolean("allow_insurance_in_crafting", true)
        preserveInsuranceOnCraft = config.getBoolean("preserve_insurance_on_craft", true)
        maxInsuranceLevelForCrafting = config.getInt("max_insurance_level_for_crafting", 5)
        consumeInsuranceOnFailedCraft = config.getBoolean("consume_insurance_on_failed_craft", false)
    }
    
    /**
     * 保存配置文件
     */
    private fun saveConfig() {
        try {
            config.save(configFile)
        } catch (ex: Exception) {
            InsurancePlugin.instance.logger.severe("无法保存兼容性配置文件: ${ex.message}")
        }
    }
    
    /**
     * 重载配置
     */
    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)
        loadConfig()
    }
    
    /**
     * 检查物品保险等级是否允许参与合成
     */
    fun isInsuranceLevelAllowedForCrafting(level: Int): Boolean {
        return level <= maxInsuranceLevelForCrafting
    }
    
    companion object {
        lateinit var instance: CompatibilityConfig
            private set
    }
}