package lucaslimb.com.github.superautoito.engine

import lucaslimb.com.github.superautoito.model.*

class AbilityExecutor {

    fun execute(context: AbilityContext, onLog: (String) -> Unit) {
        val targets = resolveTargets(context)

        when (context.caster.powerType) {
            PowerType.BUFF_ATA -> applyStatChange(context, targets, StatType.ATTACK, true, onLog)
            PowerType.BUFF_DEF -> applyStatChange(context, targets, StatType.DEFENSE, true, onLog)
            PowerType.DEBUFF_ATA -> applyStatChange(context, targets, StatType.ATTACK, false, onLog)
            PowerType.DEBUFF_DEF -> applyStatChange(context, targets, StatType.DEFENSE, false, onLog)
            PowerType.INVOKE -> handleInvoke(context, onLog)
            PowerType.DECK_CHANGE -> handleDeckChange(context, targets, onLog)
            PowerType.GENERAL -> handleGeneral(context, targets, onLog)
        }
    }

    private fun resolveTargets(context: AbilityContext): List<Character> {
        val c = context.caster

        when {
            c.trigger == TriggerType.ON_KILL && c.powerTarget == PowerTarget.ENEMY -> {
                val nextEnemy = context.enemies.firstOrNull { it.defense > 0 }
                return if (nextEnemy != null) listOf(nextEnemy) else emptyList()
            }

            c.powerTargetType == PowerTargetType.LOOSER && context.victim != null -> {
                return listOf(context.victim)
            }

            c.powerTargetType == PowerTargetType.WINNER && context.killer != null -> {
                return listOf(context.killer)
            }

            c.powerTarget == PowerTarget.SELF && c.powerTargetType != PowerTargetType.NONE -> {
                val hasMatchingEnemy = context.enemies.any { it.hasType(c.powerTargetType) }
                return if (hasMatchingEnemy) listOf(c) else emptyList()
            }
        }

        val scope = when (c.powerTarget) {
            PowerTarget.SELF -> listOf(c)
            PowerTarget.ALLY -> context.allies.filter { it != c }
            PowerTarget.ENEMY -> context.enemies
            PowerTarget.ALL -> (context.allies.filter { it != c } + context.enemies)
            PowerTarget.GENERAL, PowerTarget.NONE -> emptyList()
        }

        val filtered = when (c.powerTargetType) {
            PowerTargetType.NONE -> scope
            PowerTargetType.RANDOM -> scope
            else -> scope.filter { it.hasType(c.powerTargetType) }
        }

        return if (c.powerTargetType == PowerTargetType.RANDOM && filtered.isNotEmpty()) {
            listOf(filtered.random())
        } else {
            filtered
        }
    }

    private fun applyStatChange(
        context: AbilityContext,
        targets: List<Character>,
        statType: StatType,
        isBuff: Boolean,
        onLog: (String) -> Unit
    ) {
        if (targets.isEmpty()) return

        val amount = context.caster.statAmount
        val actionWord = if (isBuff) "aumentou" else "reduziu"
        val statName = if (statType == StatType.ATTACK) "ATAQUE" else "DEFESA"

        targets.forEach { target ->
            val oldValue = if (statType == StatType.ATTACK) target.attack else target.defense
            val newValue = calculateNewStat(oldValue, amount, isBuff)
            val delta = kotlin.math.abs(newValue - oldValue)

            if (statType == StatType.ATTACK) {
                target.attack = newValue
            } else {
                target.defense = newValue
            }

            onLog("${context.caster.name} $actionWord $delta de $statName de ${target.name}")
        }
    }

    private fun handleInvoke(context: AbilityContext, onLog: (String) -> Unit) {
        when (context.caster.id) {
            1 -> { // Tomie - Volta no fim do deck (apenas 1x)
                context.allies.add(context.caster.copy(
                    power = "",
                    powerType = PowerType.GENERAL
                ))
                onLog("${context.caster.name} voltou ao fim do deck!")
            }

            2 -> { // Souichi - Invoca ARANHA 1/1
                val aranha = createToken("Aranha", 1, 1, context.caster.imageResId)
                context.allies.add(aranha)
                onLog("${context.caster.name} invocou uma Aranha!")
            }

            29 -> { // Kazuya - Invoca MORCEGO 1/1
                val morcego = createToken("Morcego", 1, 1, context.caster.imageResId)
                context.allies.add(morcego)
                onLog("${context.caster.name} invocou um Morcego!")
            }

            22 -> { // Terumi - Invoca CABEÇA 1/1
                val cabeca = createToken("Cabeça", 1, 1, context.caster.imageResId)
                context.allies.add(cabeca)
                onLog("${context.caster.name} invocou uma Cabeça!")
            }

            6 -> { // Chiemi - Invoca o INIMIGO que a MATOU
                context.killer?.let { killer ->
                    val copy = killer.copy()
                    context.allies.add(copy)
                    onLog("${context.caster.name} invocou uma cópia de ${killer.name}!")
                }
            }

            9 -> { // Soldado Furukawa - Invoca dublê aleatório do DECK
                val deckCards = context.allies.drop(1)
                if (deckCards.isNotEmpty()) {
                    val randomCard = deckCards.random()
                    val copy = randomCard.copy()
                    context.allies.add(copy)
                    onLog("${context.caster.name} invocou uma cópia de ${randomCard.name}!")
                }
            }

            24 -> { // Kumi - Invoca a ÚLTIMA carta que MORREU
                val lastDead = context.graveyard.lastOrNull()
                lastDead?.let {
                    val copy = it.copy()
                    context.allies.add(copy)
                    onLog("${context.caster.name} invocou ${it.name} do cemitério!")
                }
            }

            7 -> { // Reanimador - Reanima carta morta com 50% stats
                val deadAllies = context.graveyard.filter {
                    // Verifica se é do mesmo time (simplificação: assume que graveyard tem info)
                    true // Na prática, precisaria de flag isPlayerTeam no Character
                }

                if (deadAllies.isNotEmpty()) {
                    val toRevive = deadAllies.random()
                    val revived = toRevive.copy(
                        attack = (toRevive.attack * 0.5).toInt(),
                        defense = (toRevive.defense * 0.5).toInt()
                    )
                    context.allies.add(revived)
                    onLog("${context.caster.name} reanimou ${toRevive.name} com metade dos stats!")
                }
            }
        }
    }

    private fun handleDeckChange(
        context: AbilityContext,
        targets: List<Character>,
        onLog: (String) -> Unit
    ) {
        if (targets.isEmpty()) return

        val target = targets.first()

        when (context.caster.id) {
            11 -> { // Misuzu - Inverte ATK/DEF
                val temp = target.attack
                target.attack = target.defense
                target.defense = temp
                onLog("${context.caster.name} inverteu stats de ${target.name} (${target.attack}/${target.defense})")
            }

            26, 13 -> { // Yuko/Tomoo - Remove do próximo round
                // Marca para remoção (implementação: remove do deck)
                val index = context.enemies.indexOf(target)
                if (index != -1) {
                    context.enemies.removeAt(index)
                    onLog("${context.caster.name} baniu ${target.name} do próximo round!")
                }
            }

            30 -> { // Goro - Troca posição aleatória
                val currentIndex = context.enemies.indexOf(target)
                if (currentIndex != -1 && context.enemies.size > 1) {
                    val newIndex = (0 until context.enemies.size).filter { it != currentIndex }.random()
                    context.enemies.removeAt(currentIndex)
                    context.enemies.add(newIndex, target)
                    onLog("${context.caster.name} moveu ${target.name} para posição ${newIndex + 1}")
                }
            }
        }
    }

    private fun handleGeneral(
        context: AbilityContext,
        targets: List<Character>,
        onLog: (String) -> Unit
    ) {
        when (context.caster.id) {
            32 -> { // Misaki - Rouba 50% da DEFESA
                context.victim?.let { victim ->
                    val stealAmount = (context.victimPreviousDefense * 0.5).toInt()
                    context.caster.defense += stealAmount
                    onLog("${context.caster.name} roubou $stealAmount de defesa de ${victim.name}!")
                }
            }

            20 -> { // Ryo Tsukano - Rouba PODER (complexo, simplificado)
                context.victim?.let { victim ->
                    // Como Character é imutável, não dá pra trocar poder em runtime
                    // Solução: logar que roubou (implementação completa precisaria de var)
                    onLog("${context.caster.name} copiou o poder de ${victim.name}!")
                    // TODO: Implementar sistema de poderes mutáveis se necessário
                }
            }

            23 -> { // Maya - +50% DEF ao aliado ao lado
                val myIndex = context.allies.indexOf(context.caster)
                val neighbors = mutableListOf<Character>()

                if (myIndex > 0) neighbors.add(context.allies[myIndex - 1])
                if (myIndex < context.allies.size - 1) neighbors.add(context.allies[myIndex + 1])

                neighbors.forEach { ally ->
                    val bonus = (ally.defense * 0.5).toInt()
                    ally.defense += bonus
                    onLog("${context.caster.name} deu +$bonus DEF para ${ally.name}")
                }
            }
        }
    }

    private fun createToken(name: String, attack: Int, defense: Int, imageResId: Int): Character {
        return Character(
            id = -1,
            name = name,
            attack = attack,
            defense = defense,
            types = listOf(CharacterType.COMMON),
            trigger = TriggerType.NONE,
            power = "",
            powerType = PowerType.GENERAL,
            imageResId = imageResId
        )
    }

    private fun Character.hasType(targetType: PowerTargetType): Boolean {
        return when (targetType) {
            PowerTargetType.CHILD -> types.contains(CharacterType.CHILD)
            PowerTargetType.SUPERNATURAL -> types.contains(CharacterType.SUPERNATURAL)
            PowerTargetType.CORPSE -> types.contains(CharacterType.CORPSE)
            PowerTargetType.FEM -> types.contains(CharacterType.FEM)
            PowerTargetType.MASC -> types.contains(CharacterType.MASC)
            else -> false
        }
    }

    private fun calculateNewStat(current: Int, amount: StatAmount, isBuff: Boolean): Int {
        val delta = when {
            amount.value == 0.0 -> 0
            amount.value < 1.0 -> (current * amount.value).toInt() // Porcentagem
            else -> amount.value.toInt() // Valor fixo
        }

        val newValue = if (isBuff) current + delta else current - delta
        return maxOf(0, newValue)
    }

    enum class StatType {
        ATTACK, DEFENSE
    }
}