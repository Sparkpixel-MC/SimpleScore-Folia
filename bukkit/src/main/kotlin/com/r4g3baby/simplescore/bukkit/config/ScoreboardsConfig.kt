package com.r4g3baby.simplescore.bukkit.config

import com.r4g3baby.simplescore.BukkitPlugin
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition
import com.r4g3baby.simplescore.api.scoreboard.effect.TextEffect
import com.r4g3baby.simplescore.core.config.BaseScoreboardsConfig
import com.r4g3baby.simplescore.core.scoreboard.Scoreboard
import com.r4g3baby.simplescore.core.scoreboard.ScoreboardScore
import com.r4g3baby.simplescore.core.scoreboard.condition.Negate
import com.r4g3baby.simplescore.core.scoreboard.line.AnimatedLine
import com.r4g3baby.simplescore.core.scoreboard.line.BlankLine
import com.r4g3baby.simplescore.core.scoreboard.line.ScoreboardLine.Companion.DEFAULT_RENDER_TICKS
import com.r4g3baby.simplescore.core.scoreboard.line.ScoreboardLine.Companion.DEFAULT_VISIBLE_TICKS
import com.r4g3baby.simplescore.core.scoreboard.line.StaticLine
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.Reader

class ScoreboardsConfig(
    private val plugin: BukkitPlugin, private val mainConfig: MainConfig
) : BaseScoreboardsConfig<Player, YamlConfiguration>(plugin.dataFolder) {
    override val resourceName: String = "scoreboards.yml" // 修复：移除错误的 "configs/" 前缀

    override fun parseConfigFile(reader: Reader?): YamlConfiguration {
        return if (reader != null) {
            YamlConfiguration.loadConfiguration(reader)
        } else YamlConfiguration()
    }

    override fun loadVariables(config: YamlConfiguration) {
        config.getKeys(false).forEach { name ->
            val section = config.getConfigurationSection(name) ?: return@forEach

            val defaultHideNumber = section.getBoolean("defaultHideNumber", false)
            val defaultVisibleFor = section.getInt("defaultVisibleFor", DEFAULT_VISIBLE_TICKS)
            val defaultRenderEvery = section.getInt("defaultRenderEvery", DEFAULT_RENDER_TICKS)

            val titles = section.parseScoreboardLines("titles", defaultVisibleFor, defaultRenderEvery)
            val scores = section.parseScoreboardScores(defaultHideNumber, defaultVisibleFor, defaultRenderEvery)
            val conditions = section.parseConditions()

            scoreboards[name] = Scoreboard(name, titles, scores, conditions)
        }
    }

    private fun ConfigurationSection.parseScoreboardScores(defaultHideNumber: Boolean, defaultVisibleFor: Int, defaultRenderEvery: Int): List<ScoreboardScore<Player>> {
        return when {
            isList("scores") -> {
                val scoresMapList = getMapList("scores")
                mutableListOf<ScoreboardScore<Player>>().also { scores ->
                    scoresMapList.forEachIndexed { i, scoreMap ->
                        val scoreSec = MemoryConfiguration().createSection("${this.currentPath}.scores[$i]").apply {
                            scoreMap.forEach { (key, value) -> set(key.toString(), value) }
                        }

                        val score = scoreSec.get("score")?.toString() ?: run {
                            plugin.logger.warning("Missing 'score' value for '${scoreSec.currentPath}'.")
                            return@forEachIndexed
                        }
                        val lines = scoreSec.parseScoreboardLines("lines", defaultVisibleFor, defaultRenderEvery)
                        val conditions = scoreSec.parseConditions()

                        var hideNumber = scoreSec.get("hideNumber")
                        if (hideNumber !is Boolean) hideNumber = defaultHideNumber

                        // 修复：使用正确的构造函数参数顺序
                        scores.add(ScoreboardScore(score, lines, hideNumber as Boolean, conditions))
                    }
                }
            }

            isConfigurationSection("scores") -> {
                val section = getConfigurationSection("scores") ?: return emptyList()
                mutableListOf<ScoreboardScore<Player>>().also { scores ->
                    section.getKeys(false).forEach { score ->
                        val scoreSec = section.getConfigurationSection(score) ?: run {
                            val lines = section.parseScoreboardLines(score, defaultVisibleFor, defaultRenderEvery)
                            // 修复：使用正确的构造函数参数顺序
                            scores.add(ScoreboardScore(score, lines, defaultHideNumber, emptyList()))
                            return@forEach
                        }

                        val lines = scoreSec.parseScoreboardLines("lines", defaultVisibleFor, defaultRenderEvery)
                        val hideNumber = scoreSec.getBoolean("hideNumber", defaultHideNumber)
                        val conditions = scoreSec.parseConditions()
                        // 修复：使用正确的构造函数参数顺序
                        scores.add(ScoreboardScore(score, lines, hideNumber, conditions))
                    }
                }
            }

            else -> {
                plugin.logger.warning("Invalid or missing 'scores' value for '${this.currentPath}'.")
                return emptyList()
            }
        }
    }

    private fun ConfigurationSection.parseScoreboardLines(path: String, defaultVisibleFor: Int, defaultRenderEvery: Int): List<ScoreboardLine<Player>> {
        return when {
            isString(path) -> {
                val text = getString(path) ?: ""
                listOf(if (text.isBlank()) BlankLine() else StaticLine(text, defaultRenderEvery))
            }

            isList(path) -> {
                val list = getList(path) ?: return emptyList()

                if (list.any { it !is String }) {
                    mutableListOf<ScoreboardLine<Player>>().also { lineList ->
                        list.forEachIndexed { i, line ->
                            when (line) {
                                is String -> lineList.add(StaticLine(line, defaultRenderEvery))
                                is Map<*, *> -> {
                                    val section = MemoryConfiguration().createSection("${this.currentPath}.$path[$i]").apply {
                                        line.forEach { (key, value) -> set(key.toString(), value) }
                                    }
                                    if (section.contains("frames")) {
                                        lineList.add(section.parseAnimatedLine(defaultVisibleFor, defaultRenderEvery))
                                    } else {
                                        lineList.add(section.parseStaticLine(defaultRenderEvery))
                                    }
                                }
                                else -> {
                                    plugin.logger.warning("Invalid frame value for '${this.currentPath}.$path[$i]'.")
                                }
                            }
                        }
                    }
                } else {
                    listOf(AnimatedLine(getStringList(path).map {
                        AnimatedLine.Frame(it, defaultVisibleFor, defaultRenderEvery)
                    }))
                }
            }

            isConfigurationSection(path) -> {
                val section = getConfigurationSection(path) ?: return emptyList()
                if (section.contains("frames")) {
                    listOf(section.parseAnimatedLine(defaultVisibleFor, defaultRenderEvery))
                } else {
                    listOf(section.parseStaticLine(defaultRenderEvery))
                }
            }

            else -> {
                plugin.logger.warning("Invalid or missing '$path' value for '${this.currentPath}'.")
                return emptyList()
            }
        }
    }

    private fun ConfigurationSection.parseStaticLine(defaultRenderEvery: Int): ScoreboardLine<Player> {
        val textEffects = emptyList<TextEffect>()
        val conditions = parseConditions()

        val text = getString("text") ?: run {
            plugin.logger.warning("Missing 'text' value for '${this.currentPath}'.")
            return BlankLine(conditions)
        }

        var renderEvery = get("renderEvery")
        if (renderEvery !is Int) renderEvery = defaultRenderEvery

        return if (!text.isBlank()) {
            StaticLine(text, renderEvery as Int, textEffects, conditions)
        } else BlankLine(conditions)
    }

    private fun ConfigurationSection.parseAnimatedLine(defaultVisibleFor: Int, defaultRenderEvery: Int): ScoreboardLine<Player> {
        val textEffects = emptyList<TextEffect>()
        val conditions = parseConditions()

        val textFrames = getList("frames") ?: run {
            if (isString("frames")) {
                return StaticLine(getString("frames")!!, defaultRenderEvery, textEffects, conditions)
            }
            plugin.logger.warning("Missing 'frames' value for '${this.currentPath}'.")
            return BlankLine(conditions)
        }

        val frames = mutableListOf<AnimatedLine.Frame>()
        textFrames.forEachIndexed { i, frame ->
            when (frame) {
                is String -> frames.add(AnimatedLine.Frame(frame, defaultVisibleFor, defaultRenderEvery))

                is Map<*, *> -> {
                    var visibleFor = frame["visibleFor"]
                    if (visibleFor !is Int) visibleFor = defaultVisibleFor

                    var renderEvery = frame["renderEvery"]
                    if (renderEvery !is Int) renderEvery = defaultRenderEvery

                    val text = frame["text"] ?: run {
                        plugin.logger.warning("Missing text value for frame '${this.currentPath}[$i]'.")
                        return@forEachIndexed
                    }

                    frames.add(AnimatedLine.Frame(text.toString(), visibleFor as Int, renderEvery as Int))
                }

                else -> {
                    plugin.logger.warning("Invalid frame value for '${this.currentPath}[$i]'.")
                }
            }
        }

        return if (frames.isNotEmpty()) {
            AnimatedLine(frames, textEffects, conditions)
        } else {
            BlankLine(conditions)
        }
    }

    private fun ConfigurationSection.parseConditions(): List<Condition<Player>> {
        fun getCondition(name: String): Condition<Player>? {
            return if (name.startsWith("!")) {
                val conditionName = name.substring(1)
                mainConfig.conditions[conditionName]?.let { Negate(it) }
            } else mainConfig.conditions[name]
        }

        return when {
            isString("conditions") -> {
                val name = getString("conditions") ?: return emptyList()
                getCondition(name)?.let { listOf(it) } ?: run {
                    plugin.logger.warning("Unknown condition '$name' in '${this.currentPath}.conditions'.")
                    emptyList()
                }
            }

            isList("conditions") -> getStringList("conditions").mapNotNull {
                getCondition(it) ?: run {
                    plugin.logger.warning("Unknown condition '$it' in '${this.currentPath}.conditions'.")
                    null
                }
            }

            else -> emptyList()
        }
    }
}