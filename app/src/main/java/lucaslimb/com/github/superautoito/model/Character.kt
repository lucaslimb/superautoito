package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import lucaslimb.com.github.superautoito.R

enum class CharacterType(val desc: Int) {
    CHILD(R.string.type_child),
    SUPERNATURAL(R.string.type_supernatural),
    CORPSE(R.string.type_corpse),
    COMMON(R.string.type_common),
    MASC(R.string.type_masc),
    FEM(R.string.type_fem)
}

enum class TriggerType(val desc: Int) {
    ON_KILL(R.string.trigger_on_kill),
    ON_DEATH(R.string.trigger_on_death),
    BEFORE_BATTLE(R.string.trigger_before_battle),
    NONE(R.string.trigger_none)
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
        fun getDefaultCharacters(context: android.content.Context): List<Character> {
            return listOf(
                Character(
                    id = 27, name = "Oshikiri", attack = 4, defense = 4,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.oshikiri,
                    power = context.getString(R.string.power_oshikiri),
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.SUPERNATURAL, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 16, name = "Professor Okabe", attack = 7, defense = 4,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.professor,
                    power = context.getString(R.string.power_prof_okabe),
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.SUPERNATURAL, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 12, name = "Moço do Sorvete", attack = 3, defense = 3,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.moco,
                    power = context.getString(R.string.power_moco_sorvete),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.CHILD, powerTarget = PowerTarget.ALL,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 8, name = "Kuriko", attack = 4, defense = 3,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.kuriko,
                    power = context.getString(R.string.power_kuriko),
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.CHILD, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 3, name = "Fuchi", attack = 6, defense = 6,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.fuchi,
                    power = context.getString(R.string.power_fuchi),
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.MASC, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 4, name = "Garoto da Encruzilhada", attack = 3, defense = 4,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.garoto,
                    power = context.getString(R.string.power_garoto),
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.FEM, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 19, name = "Yuki", attack = 2, defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.yuki,
                    power = context.getString(R.string.power_yuki),
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.CORPSE, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 15, name = "Tio Kingoro", attack = 3, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.tio,
                    power = context.getString(R.string.power_tio),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.CORPSE, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 11, name = "Misuzu", attack = 3, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.misuzu,
                    power = context.getString(R.string.power_misuzu),
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 26, name = "Yuko", attack = 3, defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.yuko,
                    power = context.getString(R.string.power_yuko),
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 5, name = "Mulher das Costelas", attack = 5, defense = 3,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.mulher,
                    power = context.getString(R.string.power_mulher_costelas),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 30, name = "Goro", attack = 2, defense = 4,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.goro,
                    power = context.getString(R.string.power_goro),
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 31, name = "Binzo", attack = 5, defense = 4,
                    types = listOf(CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.binzo,
                    power = context.getString(R.string.power_binzo),
                    powerType = PowerType.DEBUFF_ATA, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 1, name = "Tomie", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.tomie,
                    power = context.getString(R.string.power_tomie),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 2, name = "Souichi", attack = 3, defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.souichi,
                    power = context.getString(R.string.power_souichi),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 29, name = "Kazuya Tani", attack = 3, defense = 3,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.kazuya,
                    power = context.getString(R.string.power_kazuya),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 22, name = "Terumi", attack = 2, defense = 4,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.terumi,
                    power = context.getString(R.string.power_terumi),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 6, name = "Chiemi", attack = 5, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.chiemi,
                    power = context.getString(R.string.power_chiemi),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.WINNER, powerTarget = PowerTarget.NONE
                ),
                Character(
                    id = 9, name = "Soldado Furukawa", attack = 2, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.soldado,
                    power = context.getString(R.string.power_soldado),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ALLY
                ),
                Character(
                    id = 24, name = "Kumi", attack = 2, defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.kumi,
                    power = context.getString(R.string.power_kumi),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.LOOSER, powerTarget = PowerTarget.ALL
                ),
                Character(
                    id = 7, name = "Reanimador", attack = 5, defense = 4,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.reanimador,
                    power = context.getString(R.string.power_reanimador),
                    powerType = PowerType.INVOKE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ALLY
                ),
                Character(
                    id = 23, name = "Maya", attack = 3, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.maya,
                    power = context.getString(R.string.power_maya),
                    powerType = PowerType.BUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ALLY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 14, name = "Nakayama", attack = 2, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.nakayama,
                    power = context.getString(R.string.power_nakayama),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ALL,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 10, name = "Manchas do Beco", attack = 3, defense = 7,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.manchas,
                    power = context.getString(R.string.power_manchas),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.WINNER, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 32, name = "Misaki", attack = 4, defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.misaki,
                    power = context.getString(R.string.power_misaki),
                    powerType = PowerType.GENERAL, powerTargetType = PowerTargetType.LOOSER, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 21, name = "Numei", attack = 4, defense = 4,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.numei,
                    power = context.getString(R.string.power_numei),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 20, name = "Ryo Tsukano", attack = 3, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.ryo,
                    power = context.getString(R.string.power_ryo),
                    powerType = PowerType.GENERAL, powerTargetType = PowerTargetType.LOOSER, powerTarget = PowerTarget.ENEMY
                ),
                Character(
                    id = 28, name = "Frankenstein", attack = 4, defense = 6,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.frank,
                    power = context.getString(R.string.power_frankenstein),
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 25, name = "Ayumi", attack = 6, defense = 4,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.ayumi,
                    power = context.getString(R.string.power_ayumi),
                    powerType = PowerType.DEBUFF_DEF, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF
                ),
                Character(
                    id = 17, name = "Esculturas Sem Cabeça", attack = 4, defense = 5,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.esculturas,
                    power = context.getString(R.string.power_esculturas),
                    powerType = PowerType.DEBUFF_ATA, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.ENEMY,
                    statAmount = StatAmount.HALF
                ),
                Character(
                    id = 18, name = "Hideo", attack = 3, defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.hideo,
                    power = context.getString(R.string.power_hideo),
                    powerType = PowerType.BUFF_ATA, powerTargetType = PowerTargetType.NONE, powerTarget = PowerTarget.SELF,
                    statAmount = StatAmount.ONE
                ),
                Character(
                    id = 13, name = "Tomoo", attack = 2, defense = 3,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.tomoo,
                    power = context.getString(R.string.power_tomoo),
                    powerType = PowerType.DECK_CHANGE, powerTargetType = PowerTargetType.RANDOM, powerTarget = PowerTarget.ENEMY
                )
            )
        }
    }

}