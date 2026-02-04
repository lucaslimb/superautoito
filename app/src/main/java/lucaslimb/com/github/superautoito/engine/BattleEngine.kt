package lucaslimb.com.github.superautoito.engine

import lucaslimb.com.github.superautoito.model.*

class BattleEngine(
    private var playerTeam: MutableList<Character>,
    private var enemyTeam: MutableList<Character>
) {
    private val abilityExecutor = AbilityExecutor()
    private val battleLog = mutableListOf<String>()
    private val graveyard = mutableListOf<Character>()
    private val lastKiller = mutableMapOf<Character, Character>()

    fun getPlayerTeam() = playerTeam.toList()
    fun getEnemyTeam() = enemyTeam.toList()
    fun getBattleLog() = battleLog.toList()

    suspend fun executePreBattlePhase(onLog: (String) -> Unit, onUpdate: suspend () -> Unit) {
        log("--- FASE PRÉ-BATALHA ---", onLog)

        // Player team
        playerTeam.forEachIndexed { index, character ->
            if (character.trigger == TriggerType.BEFORE_BATTLE) {
                val context = AbilityContext(
                    caster = character,
                    casterIndex = index,
                    allies = playerTeam,
                    enemies = enemyTeam,
                    graveyard = graveyard,
                    isPlayerTeam = true
                )

                abilityExecutor.execute(context, onLog)
                onUpdate()
            }
        }

        // Enemy team
        enemyTeam.forEachIndexed { index, character ->
            if (character.trigger == TriggerType.BEFORE_BATTLE) {
                val context = AbilityContext(
                    caster = character,
                    casterIndex = index,
                    allies = enemyTeam,
                    enemies = playerTeam,
                    graveyard = graveyard,
                    isPlayerTeam = false
                )

                abilityExecutor.execute(context, onLog)
                onUpdate()
            }
        }

        log("Fase pré-batalha concluída!", onLog)
    }

    /**
     * Executa um turno de combate (primeiro personagem de cada time)
     * Retorna true se a batalha deve continuar
     */
    suspend fun executeCombatTurn(
        onLog: (String) -> Unit,
        onUpdate: suspend () -> Unit
    ): Boolean {
        if (playerTeam.isEmpty() || enemyTeam.isEmpty()) return false

        val playerChar = playerTeam[0]
        val enemyChar = enemyTeam[0]

        log("${playerChar.name} (${playerChar.attack}/${playerChar.defense}) VS ${enemyChar.name} (${enemyChar.attack}/${enemyChar.defense})", onLog)

        val playerDefBefore = playerChar.defense
        val enemyDefBefore = enemyChar.defense

        playerChar.defense -= enemyChar.attack
        enemyChar.defense -= playerChar.attack

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
                onUpdate()
            }
        }

        handleDeaths(onLog, onUpdate)

        return playerTeam.isNotEmpty() && enemyTeam.isNotEmpty()
    }

    private suspend fun handleDeaths(onLog: (String) -> Unit, onUpdate: suspend () -> Unit) {
        // Player team
        if (playerTeam.isNotEmpty() && playerTeam[0].defense <= 0) {
            val dead = playerTeam[0]
            log("${dead.name} foi derrotado!", onLog)

            if (dead.trigger == TriggerType.ON_DEATH) {
                val killer = lastKiller[dead]

                val context = AbilityContext(
                    caster = dead,
                    casterIndex = 0,
                    allies = playerTeam,
                    enemies = enemyTeam,
                    graveyard = graveyard,
                    isPlayerTeam = true,
                    killer = killer
                )

                abilityExecutor.execute(context, onLog)
                onUpdate()
            }

            graveyard.add(dead)
            playerTeam.removeAt(0)
            onUpdate()
        }

        if (enemyTeam.isNotEmpty() && enemyTeam[0].defense <= 0) {
            val dead = enemyTeam[0]
            log("${dead.name} foi derrotado!", onLog)

            if (dead.trigger == TriggerType.ON_DEATH) {
                val killer = lastKiller[dead]

                val context = AbilityContext(
                    caster = dead,
                    casterIndex = 0,
                    allies = enemyTeam,
                    enemies = playerTeam,
                    graveyard = graveyard,
                    isPlayerTeam = false,
                    killer = killer
                )

                abilityExecutor.execute(context, onLog)
                onUpdate()
            }

            graveyard.add(dead)
            enemyTeam.removeAt(0)
            onUpdate()
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