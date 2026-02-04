package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import lucaslimb.com.github.superautoito.R

enum class CharacterType(val desc: String) {
    CHILD("Criança"),
    SUPERNATURAL("Sobrenatural"),
    CORPSE("Cadáver"),
    COMMON("Comum"),
    MASC("Masculino"),
    FEM("Feminino")
}

enum class TriggerType(val desc: String) {
    ON_KILL("Ao matar"),
    ON_DEATH("Ao morrer"),
    BEFORE_BATTLE("Antes da batalha"),
    NONE("Em nenhum momento")
}

enum class PowerType {
    BUFF_ATA, BUFF_DEF,
    DEBUFF_ATA, DEBUFF_DEF,
    INVOKE,
    DECK_CHANGE,
    GENERAL
}

enum class PowerTargetType {
    CHILD, SUPERNATURAL, CORPSE, FEM, MASC,
    LOOSER,
    WINNER,
    RANDOM,
    NONE
}

enum class PowerTarget {
    SELF,
    ALL,
    ENEMY,
    ALLY,
    GENERAL,
    NONE
}

enum class StatAmount(val value: Double) {
    HALF(0.5), ONE(1.0), NONE(0.0)
}

@Parcelize
data class Character(
    val id: Int,
    val name: String,
    var attack: Int,
    var defense: Int,
    val types: List<CharacterType>,
    val trigger: TriggerType,
    val imageResId: Int = 0,
    val power: String = "",
    val powerType: PowerType = PowerType.GENERAL,
    val powerTargetType: PowerTargetType = PowerTargetType.NONE,
    val powerTarget: PowerTarget = PowerTarget.NONE,
    val statAmount: StatAmount = StatAmount.NONE
) : Parcelable {

    companion object {
        fun getDefaultCharacters(): List<Character> {
            return listOf(
                Character(
                    id = 27, name = "Oshikiri", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.oshikiri,
                    power = "+50% de DEFESA para amigo SOBRENATURAL",
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.SUPERNATURAL, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF

                ),
                Character(
                    id = 16, name = "Professor Okabe", attack = 4, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.professor,
                    power = "+50% de ATAQUE contra inimigo SOBRENATURAL",
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.SUPERNATURAL, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 12, name = "Moço do Sorvete", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.moco,
                    power = "-50% de DEFESA para todas as CRIANÇAS",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.CHILD, powerTarget = PowerTarget.ALL,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 8, name = "Kuriko", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.kuriko,
                    power = "+50% de DEFESA para amigo CRIANÇA",
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.CHILD, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 3, name = "Fuchi", attack = 3, defense = 5,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.fuchi,
                    power = "+1 de ATAQUE contra inimigo MASCULINO",
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.MASC, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 4, name = "Garoto da Encruzilhada", attack = 2, defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.garoto,
                    power = "+1 de ATAQUE contra inimigo FEMININO",
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.FEM, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 19, name = "Yuki", attack = 1, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.yuki,
                    power = "+50% de DEFESA para amigo CADÁVER",
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.CORPSE, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 15, name = "Tio Kingoro", attack = 2, defense = 1,
                    types = listOf(CharacterType.CORPSE, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.tio,
                    power = "-50% de DEFESA para inimigo CADÁVER",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.CORPSE, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 11, name = "Misuzu", attack = 1, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.misuzu,
                    power = "Inverte ATAQUE e DEFESA de inimigo aleatório",
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 26, name = "Yuko", attack = 2, defense = 1,
                    types = listOf(CharacterType.CHILD, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.yuko,
                    power = "Inimigo aleatório removido do DECK no PRÓXIMO round",
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 5, name = "Mulher das Costelas", attack = 3, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.mulher,
                    power = "-1 de DEFESA para inimigo aleatório",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 30, name = "Goro", attack = 1, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.goro,
                    power = "Troca a POSIÇÃO de inimigo aleatório",
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 31, name = "Binzo", attack = 5, defense = 3,
                    types = listOf(CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.binzo,
                    power = "-1 de ATAQUE para inimigo aleatório",
                    powerType = PowerType.DEBUFF_ATA, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 1, name = "Tomie", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.tomie,
                    power = "Volta no FIM do DECK (apenas 1x)",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 2, name = "Souichi", attack = 3, defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.souichi,
                    power = "Invoca uma ARANHA 1/1",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 29, name = "Kazuya Tani", attack = 2, defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.kazuya,
                    power = "Invoca um MORCEGO 1/1",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 22, name = "Terumi", attack = 1, defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.terumi,
                    power = "Invoca uma CABEÇA 1/1",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 6, name = "Chiemi", attack = 3, defense = 1,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.chiemi,
                    power = "Invoca o INIMIGO que a MATOU",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.WINNER, powerTarget = PowerTarget.NONE
                ),
                Character(
                    id = 9, name = "Soldado Furukawa", attack = 2, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.soldado,
                    power = "Invoca um dublê de uma CARTA aleatória do DECK (se houver)",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ALLY
                ),
                Character(
                    id = 24, name = "Kumi", attack = 1, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.kumi,
                    power = "Invoca a ÚLTIMA carta que MORREU",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.LOOSER, powerTarget = PowerTarget.ALL
                ),
                Character(
                    id = 7, name = "Reanimador", attack = 4, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.reanimador,
                    power = "Reanima uma CARTA morta no ROUND com 50% de stats",
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ALLY
                ),
                Character(
                    id = 23, name = "Maya", attack = 2, defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.maya,
                    power = "+50% de DEFESA ao amigo ao LADO",
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 14, name = "Nakayama", attack = 1, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.nakayama,
                    power = "-1 de DEFESA a TODOS",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ALL,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 10, name = "Manchas do Beco", attack = 3, defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.manchas,
                    power = "-1 de DEFESA para o INIMIGO que o MATOU",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.WINNER, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 32, name = "Misaki", attack = 3, defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.misaki,
                    power = "Rouba 50% da DEFESA de quem MATOU",
                    powerType = PowerType.GENERAL, powerTargetType = PowerTargetType.LOOSER, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 21, name = "Numei", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.numei,
                    power = "-50% de DEFESA para o PRÓXIMO inimigo",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 20, name = "Ryo Tsukano", attack = 3, defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.ryo,
                    power = "Rouba o PODER de quem MATOU",
                    powerType = PowerType.GENERAL, powerTargetType = PowerTargetType.LOOSER, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 28, name = "Frankenstein", attack = 3, defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.frank,
                    power = "+50% de ATAQUE a si próprio",
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 25, name = "Ayumi", attack = 3, defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.ayumi,
                    power = "+50% de DEFESA a si próprio",
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 17, name = "Esculturas Sem Cabeça", attack = 4, defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.esculturas,
                    power = "-50% de ATAQUE para o PRÓXIMO inimigo",
                    powerType = PowerType.DEBUFF_ATA, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 18, name = "Hideo", attack = 2, defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.hideo,
                    power = "+1 de ATAQUE a si próprio",
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 13, name = "Tomoo", attack = 3, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.tomoo,
                    power = "Inimigo aleatório removido do DECK no PRÓXIMO round",
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                )
            )
        }
    }
}