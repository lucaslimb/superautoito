package lucaslimb.com.github.superautoito.engine

import android.content.Context
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.*

class BattleEngine(
    private val context: Context,
    private var playerTeam: MutableList<Character>,
    private var enemyTeam: MutableList<Character>
) {
    private val battleLog = mutableListOf<String>()
    private val graveyard = mutableListOf<Character>()
    private val lastKiller = mutableMapOf<Character, Character>()
    private val nextRoundBannedIds = mutableListOf<Int>()

    fun getPlayerTeam() = playerTeam.toList()
    fun getEnemyTeam() = enemyTeam.toList()
    fun getBattleLog() = battleLog.toList()
    fun getBannedIds() = nextRoundBannedIds.toList()
    private val abilityExecutor = AbilityExecutor (context) { bannedId ->
        nextRoundBannedIds.add(bannedId)
    }

    suspend fun executePreBattlePhase(onLog: (String) -> Unit, onUpdate: suspend () -> Unit) {
        log(context.getString(R.string.battle_pre_phase), onLog)
        var i = 0
        while (i < playerTeam.size) {
            val character = playerTeam[i]

            if (character.trigger == TriggerType.BEFORE_BATTLE && character.defense > 0) {
                val context = AbilityContext(
                    caster = character,
                    casterIndex = i,
                    allies = playerTeam,
                    enemies = enemyTeam,
                    graveyard = graveyard,
                    isPlayerTeam = true
                )

                abilityExecutor.execute(context, onLog)

                handleDeaths(onLog, onUpdate)
                onUpdate()
            }

            if (i < playerTeam.size && playerTeam[i] == character) {
                i++
            }
        }

        i = 0
        while (i < enemyTeam.size) {
            val character = enemyTeam[i]

            if (character.trigger == TriggerType.BEFORE_BATTLE && character.defense > 0) {
                val context = AbilityContext(
                    caster = character,
                    casterIndex = i,
                    allies = enemyTeam,
                    enemies = playerTeam,
                    graveyard = graveyard,
                    isPlayerTeam = false
                )

                abilityExecutor.execute(context, onLog)

                handleDeaths(onLog, onUpdate)
                onUpdate()
            }

            if (i < enemyTeam.size && enemyTeam[i] == character) {
                i++
            }
        }

        log(context.getString(R.string.battle_pre_phase_end), onLog)
    }

    suspend fun executeCombatTurn(
        onLog: (String) -> Unit,
        onUpdate: suspend () -> Unit
    ): Boolean {
        if (playerTeam.isEmpty() || enemyTeam.isEmpty()) return false

        val playerChar = playerTeam[0]
        val enemyChar = enemyTeam[0]

        val playerDefBefore = playerChar.defense
        val enemyDefBefore = enemyChar.defense

        playerChar.defense -= enemyChar.attack
        enemyChar.defense -= playerChar.attack

        if (playerChar.defense < 0) playerChar.defense = 0
        if (enemyChar.defense < 0) enemyChar.defense = 0

        onUpdate()

        val playerKilled = enemyChar.defense <= 0
        val enemyKilled = playerChar.defense <= 0

        if (playerKilled && playerChar.defense > 0) {
            lastKiller[enemyChar] = playerChar

            if (playerChar.trigger == TriggerType.ON_KILL) {
                val context = AbilityContext(
                    caster = playerChar,
                    casterIndex = 0,
                    allies = playerTeam,
                    enemies = enemyTeam,
                    graveyard = graveyard,
                    isPlayerTeam = true,
                    victim = enemyChar,
                    victimPreviousDefense = enemyDefBefore
                )

                abilityExecutor.execute(context, onLog)
                handleDeaths(onLog, onUpdate)
                onUpdate()
            }
        }

        if (enemyKilled && enemyChar.defense > 0) {
            lastKiller[playerChar] = enemyChar

            if (enemyChar.trigger == TriggerType.ON_KILL) {
                val context = AbilityContext(
                    caster = enemyChar,
                    casterIndex = 0,
                    allies = enemyTeam,
                    enemies = playerTeam,
                    graveyard = graveyard,
                    isPlayerTeam = false,
                    victim = playerChar,
                    victimPreviousDefense = playerDefBefore
                )

                abilityExecutor.execute(context, onLog)
                handleDeaths(onLog, onUpdate)
                onUpdate()
            }
        }

        handleDeaths(onLog, onUpdate)

        return playerTeam.isNotEmpty() && enemyTeam.isNotEmpty()
    }

    private suspend fun handleDeaths(onLog: (String) -> Unit, onUpdate: suspend () -> Unit) {
        var deathProcessed = true

        while (deathProcessed) {
            deathProcessed = false

            var i = 0
            while (i < playerTeam.size) {
                val character = playerTeam[i]

                if (character.defense <= 0) {
                    log(context.getString(R.string.battle_defeated_format, character.name), onLog)
                    playerTeam.removeAt(i)

                    graveyard.add(character)

                    if (character.trigger == TriggerType.ON_DEATH) {
                        val killer = lastKiller[character]
                        val context = AbilityContext(
                            caster = character,
                            casterIndex = 0,
                            allies = playerTeam,
                            enemies = enemyTeam,
                            graveyard = graveyard,
                            isPlayerTeam = true,
                            killer = killer
                        )
                        abilityExecutor.execute(context, onLog)
                    }

                    deathProcessed = true
                    onUpdate()
                } else {
                    i++
                }
            }

            var j = 0
            while (j < enemyTeam.size) {
                val character = enemyTeam[j]

                if (character.defense <= 0) {
                    log(context.getString(R.string.battle_defeated_format, character.name), onLog)
                    enemyTeam.removeAt(j)
                    graveyard.add(character)

                    if (character.trigger == TriggerType.ON_DEATH) {
                        val killer = lastKiller[character]
                        val context = AbilityContext(
                            caster = character,
                            casterIndex = 0,
                            allies = enemyTeam,
                            enemies = playerTeam,
                            graveyard = graveyard,
                            isPlayerTeam = false,
                            killer = killer
                        )
                        abilityExecutor.execute(context, onLog)
                    }

                    deathProcessed = true
                    onUpdate()
                } else {
                    j++
                }
            }
        }
    }

    fun getWinner(): Int {
        return when {
            playerTeam.isNotEmpty() && enemyTeam.isEmpty() -> 1
            enemyTeam.isNotEmpty() && playerTeam.isEmpty() -> -1
            else -> 0
        }
    }

    private fun log(message: String, onLog: (String) -> Unit) {
        battleLog.add(message)
        onLog(message)
    }


}