package lucaslimb.com.github.superautoito.engine

import android.content.Context
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.*

class AbilityExecutor (
    private val context: Context,
    private val onBanCharacter: (Int) -> Unit) {

    private val baseCharactersCache by lazy {
        Character.getDefaultCharacters(context).associateBy { it.id }
    }

    fun execute(abContext: AbilityContext, onLog: (String) -> Unit) {
        val targets = resolveTargets(abContext)

        when (abContext.caster.powerType) {
            PowerType.BUFF_ATA -> applyStatChange(abContext, targets, StatType.ATTACK, true, onLog)
            PowerType.BUFF_DEF -> applyStatChange(abContext, targets, StatType.DEFENSE, true, onLog)
            PowerType.DEBUFF_ATA -> applyStatChange(abContext, targets, StatType.ATTACK, false, onLog)
            PowerType.DEBUFF_DEF -> applyStatChange(abContext, targets, StatType.DEFENSE, false, onLog)
            PowerType.INVOKE -> handleInvoke(abContext, onLog)
            PowerType.DECK_CHANGE -> handleDeckChange(abContext, targets, onLog)
            PowerType.GENERAL -> handleGeneral(abContext, targets, onLog)
        }
    }

    private fun resolveTargets(abContext: AbilityContext): List<Character> {
        val c = abContext.caster

        when {
            c.trigger == TriggerType.ON_KILL && c.powerTarget == PowerTarget.ENEMY -> {
                val nextEnemy = abContext.enemies.firstOrNull { it.defense > 0 }
                return if (nextEnemy != null) listOf(nextEnemy) else emptyList()
            }

            c.powerTargetType == PowerTargetType.LOOSER && abContext.victim != null -> {
                return listOf(abContext.victim)
            }

            c.powerTargetType == PowerTargetType.WINNER && abContext.killer != null -> {
                return listOf(abContext.killer)
            }

            c.powerTarget == PowerTarget.SELF && c.powerTargetType != PowerTargetType.NONE -> {
                val hasMatchingEnemy = abContext.enemies.any { it.hasType(c.powerTargetType) }
                return if (hasMatchingEnemy) listOf(c) else emptyList()
            }
        }

        val scope = when (c.powerTarget) {
            PowerTarget.SELF -> listOf(c)
            PowerTarget.ALLY -> abContext.allies.filter { it != c }
            PowerTarget.ENEMY -> abContext.enemies
            PowerTarget.ALL -> (abContext.allies.filter { it != c } + abContext.enemies)
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
        abContext: AbilityContext,
        targets: List<Character>,
        statType: StatType,
        isBuff: Boolean,
        onLog: (String) -> Unit
    ) {
        if (targets.isEmpty()) return

        val amount = abContext.caster.statAmount
        val actionWord = if (isBuff)
            context.getString(R.string.stat_increased)
        else
            context.getString(R.string.stat_decreased)
        val statName = if (statType == StatType.ATTACK)
            context.getString(R.string.stat_attack)
        else
            context.getString(R.string.stat_defense)

        targets.forEach { target ->
            val oldValue = if (statType == StatType.ATTACK) target.attack else target.defense
            val newValue = calculateNewStat(oldValue, amount, isBuff)
            val delta = kotlin.math.abs(newValue - oldValue)

            if (statType == StatType.ATTACK) {
                target.attack = newValue
            } else {
                target.defense = newValue
            }

            onLog(
                context.getString(
                    R.string.ability_stat_change_format,
                    abContext.caster.name,
                    actionWord,
                    delta,
                    statName,
                    target.name
                )
            )
        }
    }

    private fun handleInvoke(abContext: AbilityContext, onLog: (String) -> Unit) {

        val spawnIndex = (abContext.casterIndex + 1).coerceAtMost(abContext.allies.size)

        when (abContext.caster.id) {
            1 -> { // Tomie - Volta no fim do deck (apenas 1x)
                abContext.allies.add(abContext.caster.copy(
                    attack = 1,
                    defense = 1,
                    power = "",
                    powerType = PowerType.GENERAL
                ))
                onLog(context.getString(R.string.invoke_reborn, abContext.caster.name))
            }

            2 -> { // Souichi - Invoca ARANHA 1/1
                val aranha = createToken("Aranha", 1, 1, abContext.caster.imageResId)
                abContext.allies.add(spawnIndex, aranha)
                onLog(context.getString(R.string.invoke_spider, abContext.caster.name))
            }

            29 -> { // Kazuya - Invoca MORCEGO 1/1
                val morcego = createToken("Morcego", 1, 1, abContext.caster.imageResId)
                abContext.allies.add(spawnIndex,morcego)
                onLog(context.getString(R.string.invoke_bat, abContext.caster.name))
            }

            22 -> { // Terumi - Invoca CABEÇA 1/1
                val cabeca = createToken("Cabeça", 1, 1, abContext.caster.imageResId)
                abContext.allies.add(spawnIndex,cabeca)
                onLog(context.getString(R.string.invoke_head, abContext.caster.name))
            }

            6 -> { // Chiemi - Invoca o INIMIGO que a MATOU
                abContext.killer?.let { killer ->
                    val originalKiller = getOriginalChar(killer.id) ?: killer.copy(defense = 1)

                    // Cria uma cópia limpa com vida cheia
                    val copy = originalKiller.copy()
                    abContext.allies.add(spawnIndex, copy)
                    onLog(context.getString(R.string.invoke_copy, abContext.caster.name, killer.name))
                }
            }

            9 -> { // Soldado Furukawa - Invoca dublê aleatório do DECK
                val deckCards = abContext.allies.drop(1)
                if (deckCards.isNotEmpty()) {
                    val randomCard = deckCards.random()
                    val original = getOriginalChar(randomCard.id) ?: randomCard
                    val copy = original.copy()

                    abContext.allies.add(spawnIndex, copy)
                    onLog(context.getString(R.string.invoke_copy, abContext.caster.name, randomCard.name))
                }
            }

            24 -> { // Kumi - Invoca a ÚLTIMA carta que MORREU
                val lastDead = abContext.graveyard.lastOrNull { it != abContext.caster }

                lastDead?.let { deadChar ->
                    val original = getOriginalChar(deadChar.id) ?: deadChar.copy(defense = 1)

                    val copy = original.copy()
                    abContext.allies.add(spawnIndex, copy)
                    onLog(context.getString(R.string.invoke_graveyard, abContext.caster.name, original.name))
                }
            }

            7 -> { // Reanimador - Reanima carta morta com 50% stats
                val deadAllies = abContext.graveyard.filter {
                    it != abContext.caster
                }

                if (deadAllies.isNotEmpty()) {
                    val toReviveDead = deadAllies.random()

                    val originalInfo = getOriginalChar(toReviveDead.id) ?: toReviveDead.copy(defense = 2, attack = 2)

                    val newAttack = maxOf(1, (originalInfo.attack * 0.5).toInt())
                    val newDefense = maxOf(1, (originalInfo.defense * 0.5).toInt())

                    val revived = originalInfo.copy(
                        attack = newAttack,
                        defense = newDefense
                    )
                    abContext.allies.add(spawnIndex, revived)
                    onLog(context.getString(R.string.invoke_revive_half, abContext.caster.name, originalInfo.name))
                }
            }
        }
    }

    private fun getOriginalChar(id: Int): Character? {
        if (id == -1) return null
        return baseCharactersCache[id]
    }

    private fun handleDeckChange(
        abContext: AbilityContext,
        targets: List<Character>,
        onLog: (String) -> Unit
    ) {
        if (targets.isEmpty()) return

        val target = targets.first()

        when (abContext.caster.id) {
            11 -> { // Misuzu - Inverte ATK/DEF
                val temp = target.attack
                target.attack = target.defense
                target.defense = temp
                onLog(
                    context.getString(
                        R.string.deck_swap_stats,
                        abContext.caster.name,
                        target.name,
                        target.attack,
                        target.defense
                    )
                )
            }

            26, 13 -> { // Yuko/Tomoo - Remove do próximo round

                if (abContext.isPlayerTeam) {

                } else {
                    onBanCharacter(target.id)
                }

                val index = abContext.enemies.indexOf(target)
                if (index != -1) {
                    onLog(
                        context.getString(
                            R.string.deck_ban,
                            abContext.caster.name,
                            target.name
                        )
                    )
                }
            }

            30 -> { // Goro - Troca posição aleatória
                val currentIndex = abContext.enemies.indexOf(target)
                if (currentIndex != -1 && abContext.enemies.size > 1) {
                    val newIndex = (0 until abContext.enemies.size).filter { it != currentIndex }.random()
                    abContext.enemies.removeAt(currentIndex)
                    abContext.enemies.add(newIndex, target)
                    onLog(
                        context.getString(
                            R.string.deck_move,
                            abContext.caster.name,
                            target.name,
                            newIndex + 1
                        )
                    )
                }
            }
        }
    }

    private fun handleGeneral(
        abContext: AbilityContext,
        targets: List<Character>,
        onLog: (String) -> Unit
    ) {
        when (abContext.caster.id) {
            32 -> { // Misaki - Rouba 50% da DEFESA
                abContext.victim?.let { victim ->
                    val stealAmount = (abContext.victimPreviousDefense * 0.5).toInt()
                    abContext.caster.defense += stealAmount
                    onLog(
                        context.getString(
                            R.string.general_steal_def,
                            abContext.caster.name,
                            stealAmount,
                            victim.name
                        )
                    )
                }
            }

            20 -> { // Ryo Tsukano - Rouba PODER e EXECUTA AGORA
                val victim = abContext.victim

                // 1. Validação básica: precisa de vítima e ela não pode ser outro Ryo (evita loop)
                if (victim != null && victim.id != 20) {

                    // 2. Busca os dados originais da vítima (para pegar o poder full, não o estado morto)
                    val originalVictim = getOriginalChar(victim.id) ?: victim

                    onLog(context.getString(R.string.general_copy_power, abContext.caster.name, originalVictim.name))

                    // 3. Cria o "Ryo Mímico"
                    // Mantemos o NOME e IMAGEM do Ryo, mas trocamos ID e DADOS DO PODER pelos da vítima.
                    // Isso engana o 'when' do execute() e handleInvoke() para rodar a lógica da vítima.
                    val mimicRyo = abContext.caster.copy(
                        id = originalVictim.id, // O TRUQUE: Mudamos o ID temporariamente para cair no case certo
                        powerType = originalVictim.powerType,
                        powerTarget = originalVictim.powerTarget,
                        powerTargetType = originalVictim.powerTargetType,
                        statAmount = originalVictim.statAmount,
                        trigger = originalVictim.trigger, // Copia gatilho (irrelevante aqui, mas bom pra consistência)
                        power = originalVictim.power // Texto do poder
                    )

                    // 4. Cria um novo contexto com o Ryo Mímico como conjurador
                    val mimicContext = abContext.copy(
                        caster = mimicRyo,
                        // Removemos vítima/killer do contexto clonado para evitar efeitos colaterais estranhos
                        // a menos que o poder da vítima precise deles.
                        // Geralmente powers ativos (Buff/Invoke) não usam victim/killer no momento da execução.
                        victim = originalVictim,
                        killer = mimicRyo
                    )

                    // 5. RECURSIVIDADE: Executa a habilidade roubada imediatamente
                    // O 'execute' vai ler o powerType da vítima e despachar para a função correta
                    this.execute(mimicContext, onLog)
                }
            }

            23 -> { // Maya - +50% DEF ao aliado ao lado
                val myIndex = abContext.allies.indexOf(abContext.caster)
                val neighbors = mutableListOf<Character>()

                if (myIndex > 0) neighbors.add(abContext.allies[myIndex - 1])
                if (myIndex < abContext.allies.size - 1) neighbors.add(abContext.allies[myIndex + 1])

                neighbors.forEach { ally ->
                    val bonus = (ally.defense * 0.5).toInt()
                    ally.defense += bonus
                    onLog(
                        context.getString(
                            R.string.general_buff_neighbor,
                            abContext.caster.name,
                            bonus,
                            ally.name
                        )
                    )
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
            amount.value < 1.0 -> (current * amount.value).toInt()
            else -> amount.value.toInt()
        }

        val newValue = if (isBuff) current + delta else current - delta
        return maxOf(0, newValue)
    }

    enum class StatType {
        ATTACK, DEFENSE
    }
}