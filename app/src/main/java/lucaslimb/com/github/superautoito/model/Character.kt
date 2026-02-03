package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class CharacterType {
    CHILD,
    SUPERNATURAL,
    CORPSE,
    COMMON
}

enum class TriggerType {
    ON_KILL,
    ON_DEATH,
    ON_PREPARE,
    BEFORE_BATTLE,
    AFTER_BATTLE,
    NONE
}

@Parcelize
data class Character(
    val id: String,
    val name: String,
    val attack: Int,
    val defense: Int,
    val types: List<CharacterType>,
    val trigger: TriggerType,
    val imageResId: Int = 0
) : Parcelable {

    companion object {
        fun getDefaultCharacters(): List<Character> {
            return listOf(
                Character(
                    id = "char_001",
                    name = "Oshikiri",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_002",
                    name = "Professor Okabe",
                    attack = 4,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_003",
                    name = "Moço do Sorvete",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_004",
                    name = "Kuriko",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_005",
                    name = "Fuchi",
                    attack = 3,
                    defense = 5,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_006",
                    name = "Garoto da Encruzilhada",
                    attack = 2,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CHILD),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_007",
                    name = "Yuki",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_008",
                    name = "Tio Kingoro",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_PREPARE
                ),
                Character(
                    id = "char_009",
                    name = "Misuzu",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.BEFORE_BATTLE
                ),
                Character(
                    id = "char_010",
                    name = "Yuko",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.BEFORE_BATTLE
                ),
                Character(
                    id = "char_011",
                    name = "Mulher das Costelas",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.BEFORE_BATTLE
                ),
                Character(
                    id = "char_012",
                    name = "Goro",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.BEFORE_BATTLE
                ),
                Character(
                    id = "char_013",
                    name = "Binzo",
                    attack = 5,
                    defense = 3,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.BEFORE_BATTLE
                ),
                Character(
                    id = "char_014",
                    name = "Tomie",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_015",
                    name = "Souichi",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_016",
                    name = "Kazuya Tani",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_017",
                    name = "Terumi",
                    attack = 1,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CORPSE),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_018",
                    name = "Chiemi",
                    attack = 3,
                    defense = 1,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_019",
                    name = "Soldado Furukawa",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_020",
                    name = "Kumi",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_021",
                    name = "Reanimador",
                    attack = 4,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_022",
                    name = "Maya",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_023",
                    name = "Nakayama",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_024",
                    name = "Manchas do Beco",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH
                ),
                Character(
                    id = "char_025",
                    name = "Misaki",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_026",
                    name = "Numei",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_027",
                    name = "Ryo Tsukano",
                    attack = 3,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_028",
                    name = "Frankenstein",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_029",
                    name = "Ayumi",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_030",
                    name = "Esculturas Sem Cabeça",
                    attack = 4,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_031",
                    name = "Hideo",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_KILL
                ),
                Character(
                    id = "char_032",
                    name = "Tomoo",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.AFTER_BATTLE
                )
            )
        }
    }
}
